package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.forms;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Problem;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.printers.ASCIITreePrinter;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.services.HighlightingService;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.services.PrinterService;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.NodeDetailsTextPane;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.config_panel.ConfigPanel;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.util.Constants;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.util.NotificationLogger;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.util.PsiUtil;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class AstInspectorToolWindow implements Form {

    private static final NotificationLogger notificationLogger = new NotificationLogger(AstInspectorToolWindow.class);

    @NotNull
    private final Project project;

    @NotNull
    private final ToolWindow toolWindow;

    // Form Elements
    private JPanel mainPanel;

    private ConfigPanel configPanel;

    // Buttons
    private JButton gitHubButton;
    private JButton javaParserButton;
    private JButton resetButton;
    private JButton parseButton;

    private JTree               tree1;
    private NodeDetailsTextPane nodeDetailsTextPane;


    public AstInspectorToolWindow(@NotNull final Project project, @NotNull final ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
    }


    private static void browseToUrl(@NotNull final String url) {
        notificationLogger.info("BUTTON CLICK: URL=" + url);
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException ioException) {
            ioException.printStackTrace();
            notificationLogger.warn(ioException.getMessage(), ioException);
        }
    }


    private void astDisplaySelectionListener(TreeSelectionEvent e) {
        notificationLogger.traceEnter(this.project);

        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) this.tree1.getLastSelectedPathComponent();
        if (selectedNode != null) {
            final Object node  = selectedNode.getUserObject();
            final TNode  tNode = (TNode) node;

            // Update "selected" label
//            this.label_selected.setText("Selected: [" + tNode.toString() + "]");

            // Update the side panel
            this.updateSidebar(selectedNode);

            // Update the shared service/record of the currently selected node
            // TODO: Observer pattern and notify watchers?
            HighlightingService.getInstance().setSelectedNode(tNode.getNode());

            FileEditorManager manager = FileEditorManager.getInstance(this.project);
            final Editor      editor  = manager.getSelectedTextEditor();

            PsiUtil.getCurrentFileInEditor(this.project).ifPresent(psiFile -> {
                HighlightingService.getInstance().updateHighlight(psiFile, editor);
            });
        }
    }


    private DefaultMutableTreeNode buildTreeNodes(DefaultMutableTreeNode parent, Node node) {
        notificationLogger.traceEnter(this.project);

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


    private JButton buttonWithIcon(@NotNull final String resourcePath) {
        final JButton jButton = new JButton();

        final Icon icon = IconLoader.getIcon(resourcePath);
        jButton.setIcon(icon);

        return jButton;
    }


    private void createUIComponents() {
        notificationLogger.traceEnter(this.project);

        //
        this.initButtons();

        //
        this.configPanel = new ConfigPanel(new ParserConfiguration());

        //
        this.tree1 = this.setupTree();
        this.nodeDetailsTextPane = new NodeDetailsTextPane();

        this.doReset();
    }


    public Optional<CompilationUnit> doParse() {
        notificationLogger.traceEnter(this.project);

        final Optional<PsiFile> currentFileInEditor = PsiUtil.getCurrentFileInEditor(this.project);
        if (currentFileInEditor.isPresent()) {
            final PsiFile    psiFile    = currentFileInEditor.get();
            final JavaParser javaParser = new JavaParser(this.configPanel.getConfigFromForm());

            // parse result
//            final Optional<ParseResult<CompilationUnit>> optionalParseResult = parsePsiFile_editorContents(psiFile);
            final Optional<ParseResult<CompilationUnit>> optionalParseResult = this.parsePsiFile_diskContents(javaParser, psiFile);

            if (optionalParseResult.isPresent()) {
                final ParseResult<CompilationUnit> parseResult = optionalParseResult.get();
                return this.handleParseResult(parseResult);
            } else {
                notificationLogger.warn(this.project, "No parse result available for file: " + psiFile);
            }
        } else {
            notificationLogger.warn(this.project, "No file selected in editor.");
        }

        return Optional.empty();
    }


    public void doReset() {
        notificationLogger.traceEnter(this.project);

        this.updateTree(null);

//        this.setParseResultTextPane("Reset");
//        this.setParseResult(""); // The parse result is the output textbox

        // Reset the sidebar content, ready to be inserted into again:
        this.nodeDetailsTextPane.clear();
        this.nodeDetailsTextPane.appendLine("No node selected");
    }


    @Override
    public Optional<JPanel> getMainPanel() {
        return Optional.ofNullable(this.mainPanel);
    }


    private Optional<CompilationUnit> handleParseResult(ParseResult<CompilationUnit> parseResult) {
        notificationLogger.traceEnter(this.project);

        // Parse result not present or not successful
        if (!parseResult.isSuccessful()) {
            notificationLogger.warn(this.project, "Parsing has been unsuccessful.");
        }
        if (!parseResult.getProblems().isEmpty()) {
            StringBuilder       message  = new StringBuilder("Found " + parseResult.getProblems().size() + " problems found when parsing: ");
            final List<Problem> problems = parseResult.getProblems();
            for (int i = 0; i < problems.size(); i++) {
                final Problem problem = problems.get(i);
                message.append("\n")
                       .append("\t").append("Problem #").append(i).append(": ").append(problem.getMessage());
            }
            notificationLogger.warn(this.project, message.toString());
        }
        if (!parseResult.getResult().isPresent()) {
            notificationLogger.error(this.project, "Parse result null or not present.");
            notificationLogger.info(this.project, "parseResult.getResult() = " + parseResult.getResult());
            return Optional.empty();
        }

        notificationLogger.debug(this.project, "Parse result: " + parseResult.toString());
        return parseResult.getResult();
    }


    private void initButtons() {
        notificationLogger.traceEnter(this.project);

        // Create buttons
        this.parseButton = new JButton();
        this.resetButton = new JButton();
        this.gitHubButton = new JButton();
        this.javaParserButton = this.buttonWithIcon("/logos/jp-logo_13x13.png");

        // Add button click handlers
        this.parseButton.addActionListener(e -> this.parseButtonClickHandler());
        this.resetButton.addActionListener(e -> this.resetButtonClickHandler());
        this.gitHubButton.addActionListener(e -> browseToUrl(Constants.URL_GITHUB_PLUGIN));
        this.javaParserButton.addActionListener(e -> browseToUrl(Constants.URL_WEBSITE_JP));

    }


    private void parseButtonClickHandler() {
        notificationLogger.traceEnter(this.project);

        final Optional<CompilationUnit> optionalCu = this.doParse();
        if (optionalCu.isPresent()) {
            CompilationUnit compilationUnit = optionalCu.get();

            String  outputFormat    = this.configPanel.getSelectedExportType();
            boolean includeNodeType = this.configPanel.getOutputNodeType();

            this.updateExport(outputFormat, compilationUnit);
            this.updateTree(compilationUnit);
        } else {
            notificationLogger.warn(this.project, "Compilation Unit not found.");
        }
    }


    private Optional<ParseResult<CompilationUnit>> parsePsiFile_diskContents(JavaParser javaParser, PsiFile psiFile) {
        notificationLogger.traceEnter(this.project);
        try {
            final Path                         path   = PsiUtil.pathForPsi(psiFile);
            final ParseResult<CompilationUnit> result = javaParser.parse(path);
            return Optional.of(result);
        } catch (IOException e) {
            notificationLogger.warn(this.project, "Error trying to parse file.", e);
            e.printStackTrace();
            return Optional.empty();
        }
    }


    private void resetButtonClickHandler() {
        notificationLogger.traceEnter(this.project);
        this.doReset();
    }


    private Tree setupTree() {
        notificationLogger.traceEnter(this.project);

        final Tree tree = new Tree();
        new TreeSpeedSearch(tree); // Note: Just calling the constructor is enough to enable speed search.

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
                        notificationLogger.debug(AstInspectorToolWindow.this.project, String.format("SINGLE CLICK:: selRow: %d ;; selPath: %s", selRow, selPath));
//                        mySingleClick(selRow, selPath);
                    } else if (e.getClickCount() == 2) {
                        notificationLogger.debug(AstInspectorToolWindow.this.project, String.format("DOUBLE CLICK:: selRow: %d ;; selPath: %s", selRow, selPath));
//                        myDoubleClick(selRow, selPath);
                    }
                }
            }
        });

        return tree;
    }


    private void updateExport(String outputFormat, CompilationUnit compilationUnit) {
        notificationLogger.traceEnter(this.project);

        // Do exporting stuff
        notificationLogger.info(this.project, "Exporting of the parsed file in various versions is temporarily unavailable.");
        String output = PrinterService.getInstance(this.project).outputAs(outputFormat, compilationUnit);

        // If custom dot image, do the image in addition to the textual dot string
        if ("Custom DOT Image".equals(outputFormat)) {
//            this.outputCustomDotImage(this.project.getBasePath());
            notificationLogger.info(this.project, "Custom DOT Image is temporarily unavailable.");
        }
    }


    private void updateSidebar(DefaultMutableTreeNode selectedTreeNode) {
        notificationLogger.traceEnter(this.project);

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


    private void updateTree(CompilationUnit compilationUnit) {
        notificationLogger.traceEnter(this.project);

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

        private final Color COLOUR_COMMENT    = JBColor.GRAY.darker();
        private final Color COLOUR_IDENTIFIER = JBColor.BLUE.darker();
        private final Color COLOUR_LITERALS   = JBColor.GREEN.darker().darker();


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
    }

}
