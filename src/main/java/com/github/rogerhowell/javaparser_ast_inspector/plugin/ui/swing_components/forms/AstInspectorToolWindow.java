package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.forms;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.PsiUtil;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.printers.ASCIITreePrinter;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.services.HighlightingService;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.services.JavaParserService;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.services.PrinterService;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.NodeDetailsTextPane;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.Renderer;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.SystemIndependent;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AstInspectorToolWindow {

    private static final Logger LOGGER = Logger.getInstance(AstInspectorToolWindow.class.getName());

    public static final NotificationGroup GROUP_DISPLAY_ID_INFO = new NotificationGroup(
            "AstInspectorToolWindow group",
            NotificationDisplayType.STICKY_BALLOON,
            true
    );

    private final Project    project;
    private final ToolWindow toolWindow;

    private final HighlightingService hls;
    private final JavaParserService   javaParserService;
    private final PrinterService      printerService;


    // Layout
    private JPanel mainPanel;

    // Form
    private JButton           parseButton;
    private JPanel            configPanelContainer;
    private ParserConfigPanel configPanel;

    // Export
    private Tree                tree1;
    private JTextArea           outputTextArea;
    private JLabel              label_selected;
    private NodeDetailsTextPane nodeDetailsTextPane;

    private JTextPane parseResultTextPane;
    private JButton   resetButton;

    //
    private ParseResult<CompilationUnit> result;


    public AstInspectorToolWindow(@NotNull final Project project, @NotNull final ToolWindow toolWindow) {
        LOGGER.trace("TRACE: public AstInspectorToolWindow(final Project project, final ToolWindow toolWindow) {");
        Objects.requireNonNull(project);
        Objects.requireNonNull(toolWindow);

        this.project = project;
        this.toolWindow = toolWindow;

        // Services
        this.javaParserService = JavaParserService.getInstance(this.project);
        this.printerService = PrinterService.getInstance(this.project);
        this.hls = HighlightingService.getInstance();

    }


    private void astDisplaySelectionListener(TreeSelectionEvent e) {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) this.tree1.getLastSelectedPathComponent();
        if (selectedNode != null) {
            final Object node  = selectedNode.getUserObject();
            final TNode  tNode = (TNode) node;

            // Update "selected" label
            this.label_selected.setText("Selected: [" + tNode.toString() + "]");

            // Update the side panel
            this.updateSidebar(selectedNode);

            // Update the shared service/record of the currently selected node
            // TODO: Observer pattern and notify watchers?
            this.hls.setSelectedNode(tNode.getNode());

            FileEditorManager manager = FileEditorManager.getInstance(this.project);
            final Editor      editor  = manager.getSelectedTextEditor();

            PsiUtil.getCurrentFileInEditor(this.project).ifPresent(psiFile -> {
                this.hls.updateHighlight(psiFile, editor);
            });
        }
    }


    private DefaultMutableTreeNode buildTreeNodes(DefaultMutableTreeNode parent, Node node) {
        // Setup tree node for the given node
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(new TNode(node));

        // Add this tree node to its parent, if given
        if (parent != null) {
            parent.add(treeNode);
        }

        // Recursively add children
        List<Node> children = node.getChildNodes();
        children.forEach(childNode -> {
            treeNode.add(this.buildTreeNodes(treeNode, childNode));
        });

        return treeNode;
    }


    private Tree setupTree() {
        final Tree tree = new Tree();

        // Custom renderer -- e.g. to set colours on the nodes
        tree.setCellRenderer(new MyTreeCellRenderer());

        // Click handler for selection of AST nodes
        tree.getSelectionModel().addTreeSelectionListener(this::astDisplaySelectionListener);

        // Add click handler
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int      selRow  = AstInspectorToolWindow.this.tree1.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = AstInspectorToolWindow.this.tree1.getPathForLocation(e.getX(), e.getY());
                if (selRow != -1) {
                    if (e.getClickCount() == 1) {
                        LOGGER.info(String.format("SINGLE CLICK:: selRow: %d ;; selPath: %s", selRow, String.valueOf(selPath)));
//                        mySingleClick(selRow, selPath);
                    } else if (e.getClickCount() == 2) {
                        LOGGER.info(String.format("DOUBLE CLICK:: selRow: %d ;; selPath: %s", selRow, String.valueOf(selPath)));
//                        myDoubleClick(selRow, selPath);
                    }
                }
            }
        });

        return tree;
    }

    /**
     * TODO: place custom component creation code here
     */
    private void createUIComponents() {
        this.configPanel = new ParserConfigPanel(this.project, this.toolWindow);

        this.tree1 = this.setupTree();

        this.doReset();


        // Add button click handlers
        this.parseButton.addActionListener(e -> this.parseButtonClickHandler());
        this.resetButton.addActionListener(e -> this.resetButtonClickHandler());

    }


    public void doReset() {
        LOGGER.trace("TRACE: public void doReset() {");

        this.result = null;
        this.updateTree(null);

        this.setParseResultTextPane("Reset");
        this.setParseResult(""); // The parse result is the output textbox

        // Reset the sidebar content, ready to be inserted into again:
        this.nodeDetailsTextPane.clear();
        this.nodeDetailsTextPane.appendLine("No node selected");
    }


    private void updateTree(CompilationUnit compilationUnit) {
        LOGGER.trace("TRACE: private void updateTree(CompilationUnit compilationUnit) {");
        if (compilationUnit == null) {
            final DefaultMutableTreeNode root = new DefaultMutableTreeNode("Not yet parsed.");
            this.tree1.setModel(new DefaultTreeModel(root, false));

        } else {
            final DefaultMutableTreeNode root = this.buildTreeNodes(null, compilationUnit);
            this.tree1.setModel(new DefaultTreeModel(root, false));
        }

        // Nudge the UI to update
        this.tree1.updateUI();
    }


    public void doParse() {
        LOGGER.trace("TRACE: public void doParse() {");
        int     tabSize = this.configPanel.getTabSize();
        Charset charset = this.configPanel.getSelectedCharacterSet();

        ParserConfiguration.LanguageLevel languageLevel = this.configPanel.getSelectedLanguageLevel();

        ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration.setTabSize(tabSize);
        parserConfiguration.setCharacterEncoding(charset);
        parserConfiguration.setLanguageLevel(languageLevel);

        JavaParser javaParser = new JavaParser(parserConfiguration);
//        this.result = javaParser.parse(this.getInputText());

        PsiUtil.getCurrentFileInEditor(this.project).ifPresent(psiFile -> {
            final Path path = Paths.get(Objects.requireNonNull(psiFile.getVirtualFile().getCanonicalPath()));

            try {
                this.result = javaParser.parse(path);
            } catch (IOException e) {
                this.setParseResultTextPane("Error trying to parse file: " + "\n" + e.getMessage());
                LOGGER.error("ERROR: Error trying to parse file.", e);
                e.printStackTrace();

                GROUP_DISPLAY_ID_INFO.createNotification(
                        "Error trying to parse file - Error trying to parse file.",
                        "",
                        path.toString(),
                        NotificationType.WARNING
                ).notify(this.project);
            }

            if (this.result != null && this.result.getResult().isPresent()) {
                LOGGER.trace("TRACE: result not null, and present");
                this.setParseResultTextPane("Result Present: " + this.result.getResult().isPresent() + "\n" + "Parse Result: " + this.result.toString());

                final CompilationUnit compilationUnit = this.result.getResult().get();

                this.updateTree(compilationUnit);

            } else {
                LOGGER.trace("TRACE: result not null or not present");
                LOGGER.error("ERROR: Parse result not present.");
                LOGGER.info("this.result = " + this.result);
                this.setParseResultTextPane("Result Present: " + this.result.getResult().isPresent() + "\n" + "Parse Result: " + this.result.toString());


                GROUP_DISPLAY_ID_INFO.createNotification(
                        "Error trying to parse file - Error trying to parse file.",
                        "",
                        path.toString(),
                        NotificationType.WARNING
                ).notify(this.project);

                Notification notification = new Notification("EFG" + System.currentTimeMillis(), "Error trying to parse file - Parse result not present.", path.toString() + "\n\n" + this.result.toString(), NotificationType.WARNING);
                notification.notify(this.project);
            }

        });


    }


    /**
     * @return Might be null if the panel hasn't been created yet.
     */
    public Optional<JPanel> getMainPanel() {
        return Optional.ofNullable(this.mainPanel);
    }


    public void outputCustomDotImage(final @SystemIndependent String basePath) {
        if (this.result.getResult().isPresent()) {
            String dotOutput = this.printerService.asDot(this.result.getResult().get(), this.configPanel.getOutputNodeType());
            this.setParseResult(dotOutput);

            // Try to parse the dot file and generate a png image, that is then included.
            try {
                final MutableGraph      g           = new Parser().read(dotOutput);
                final Renderer          pngRenderer = Graphviz.fromGraph(g).render(Format.PNG);
                final Optional<PsiFile> currentFile = PsiUtil.getCurrentFileInEditor(this.project);

                final String filename       = currentFile.map(PsiFileSystemItem::getName).orElse("unknown filename");
                final String prefix         = "JP_AST";
                final String suffix         = String.valueOf(System.currentTimeMillis());
                final String separator      = "_";
                final String filePathString = String.format("%s%s%s%s%s%s%s.png", basePath, File.separator, prefix, separator, filename, separator, suffix);
                final File   file           = pngRenderer.toFile(new File(filePathString));

                JPanel          panel      = new JPanel();
                JBScrollPane    scrollPane = new JBScrollPane();
                final ImageIcon icon       = new ImageIcon(file.toString());
                final JLabel    label      = new JLabel(icon);
                panel.add(scrollPane);
                scrollPane.add(label);
                final ComponentPopupBuilder x = JBPopupFactory.getInstance().createComponentPopupBuilder(scrollPane, label);
                final JBPopup               y = x.createPopup();
                y.showCenteredInCurrentWindow(this.project);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void resetButtonClickHandler() {
        LOGGER.trace("TRACE: public void parseButtonClickHandler() {");
        this.doReset();
    }


    private void parseButtonClickHandler() {
        LOGGER.trace("TRACE: public void parseButtonClickHandler() {");
        this.doParse();

        this.result.getResult().ifPresent(compilationUnit -> {
            String outputFormat = this.configPanel.getOutputFormat();
            String output       = this.printerService.outputAs(outputFormat, compilationUnit);

            // If custom dot image, do the image in addition to the textual dot string
            if ("Custom DOT Image".equals(outputFormat)) {
                this.outputCustomDotImage(this.project.getBasePath());
            }

            this.setParseResult(output);
        });

    }


    public void setParseResult(String resultString) {
        LOGGER.trace("TRACE: public void setParseResult(String resultString) {");
        this.outputTextArea.setText(resultString);
    }


    public void setParseResultTextPane(String string) {
        LOGGER.trace("TRACE: public void setParseResultTextPane(String string) {");
        if (this.parseResultTextPane != null) {
            this.parseResultTextPane.setText(string);
        }
    }


    private void updateSidebar(DefaultMutableTreeNode selectedTreeNode) {
        final Object node         = selectedTreeNode.getUserObject();
        final TNode  tNode        = (TNode) node;
        final Node   selectedNode = tNode.getNode();


        // Log the selected node to the panel
        if (selectedNode == null) {
            // Reset the sidebar content, ready to be inserted into again:
            this.nodeDetailsTextPane.clear();
            this.nodeDetailsTextPane.appendLine("No node selected");
        } else {
            // Reset the sidebar content, ready to be inserted into again:
            this.nodeDetailsTextPane.clear();
            this.nodeDetailsTextPane.logNodeToTextPane(selectedNode);
        }

    }


    /**
     * A helper class used to model nodes within a UI tree displayed via a tool window/panel.
     */
    private static class TNode {
        private final Node node;


        /**
         * @param node The AST node that this UI tree node contains.
         */
        TNode(@NotNull Node node) {
            this.node = node;
        }


        /**
         * @return The AST node that this UI tree node contains.
         */
        public Node getNode() {
            return this.node;
        }


        /**
         * @return A string representation/summary of the AST node that this UI tree node contains.
         */
        public String toString() {
            return ASCIITreePrinter.CLASS_RANGE_SUMMARY_FORMAT.apply(this.node);
        }

    }

    public static class MyTreeCellRenderer extends DefaultTreeCellRenderer {

        private Color COLOUR_COMMENT    = JBColor.GRAY.darker();
        private Color COLOUR_IDENTIFIER = JBColor.BLUE.darker();
        private Color COLOUR_LITERALS   = JBColor.GREEN.darker().darker();


        private void setColourForNode(Node node) {
            if (node instanceof Name || node instanceof SimpleName) {
                // Formatting of names / identifiers
                this.setForeground(this.COLOUR_IDENTIFIER);
            } else if (node instanceof Comment) {
                // Formatting of comments
                this.setForeground(this.COLOUR_COMMENT);
            } else if (node instanceof LiteralExpr) {
                // Formatting of literals (e.g. strings, numbers)
                this.setForeground(this.COLOUR_LITERALS);
            } else {
                // Use defaults
            }
        }


        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);

            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject instanceof String) {
                return this;
            } else if (userObject instanceof TNode) {
                final TNode tNode        = (TNode) userObject;
                final Node  selectedNode = tNode.getNode();
                this.setColourForNode(selectedNode);
                return this;
            } else {
                return this;
            }
        }
    }

}


