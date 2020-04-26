package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.tool_window;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.PsiUtil;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.printers.ASCIITreePrinter;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.services.HighlightingService;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.services.JavaParserService;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.services.PrinterService;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.components.NodeDetailsTextPane;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.components.ParserConfigPanel;
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
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ParseSingleForm {

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


    public ParseSingleForm(final Project project, final ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;

        // Add event handlers
        this.parseButton.addActionListener(e -> this.parseButtonClickHandler());
        this.resetButton.addActionListener(e -> this.resetButtonClickHandler());

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


    /**
     * TODO: place custom component creation code here
     */
    private void createUIComponents() {
        this.configPanel = new ParserConfigPanel(this.project, this.toolWindow);

        this.tree1 = new Tree();

        // Custom renderer -- e.g. to set colours on the nodes
        this.tree1.setCellRenderer(new MyTreeCellRenderer());

        // Click handler for selection of AST nodes
        this.tree1.getSelectionModel().addTreeSelectionListener(this::astDisplaySelectionListener);

        this.doReset();
    }


    public void doReset() {
        System.out.println("TRACE: public void doReset() {");
        this.setParseResultTextPane("Reset");
        this.result = null;
        this.updateTree(null);
    }


    private void updateTree(CompilationUnit compilationUnit) {
        System.out.println("TRACE: private void updateTree(CompilationUnit compilationUnit) {");
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
        System.out.println("TRACE: public void doParse() {");
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
                System.err.println("ERROR: Error trying to parse file.");
                e.printStackTrace();
            }

            if (this.result != null && this.result.getResult().isPresent()) {
                System.out.println("TRACE: result not null, and present");
                this.setParseResultTextPane("Result Present: " + this.result.getResult().isPresent() + "\n" + "Parse Result: " + this.result.toString());

                final CompilationUnit compilationUnit = this.result.getResult().get();

                this.updateTree(compilationUnit);

            } else {
                System.out.println("TRACE: result not null or not present");
                System.err.println("ERROR: Parse result not present.");
                System.out.println("this.result = " + this.result);
                this.setParseResultTextPane("Result Present: " + this.result.getResult().isPresent() + "\n" + "Parse Result: " + this.result.toString());
            }

        });


    }


    public JPanel getMainPanel() {
        return this.mainPanel;
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
        System.out.println("TRACE: public void parseButtonClickHandler() {");
        this.doReset();
        this.setParseResult("Reset 2");
    }


    private void parseButtonClickHandler() {
        System.out.println("TRACE: public void parseButtonClickHandler() {");
        this.doParse();

        this.result.getResult().ifPresent(compilationUnit -> {
            String output       = "";
            String outputFormat = this.configPanel.getOutputFormat();

            if ("YAML".equals(outputFormat)) {
                output = this.printerService.asYaml(compilationUnit);
            } else if ("XML".equals(outputFormat)) {
                output = this.printerService.asXml(compilationUnit);
            } else if ("DOT".equals(outputFormat)) {
                output = this.printerService.asDot(compilationUnit);
            } else if ("Java".equals(outputFormat)) {
                output = this.printerService.asJavaPrettyPrint(compilationUnit);
            } else if ("ASCII Tree".equals(outputFormat)) {
                output = this.printerService.asAsciiTreeText(compilationUnit);
            } else if ("Custom DOT".equals(outputFormat)) {
                output = this.printerService.asDotCustom(compilationUnit);
            } else if ("Custom DOT Image".equals(outputFormat)) {
                this.outputCustomDotImage(this.project.getBasePath());
            } else if ("Custom JSON".equals(outputFormat)) {
                output = this.printerService.asJsonCustom(compilationUnit);
            } else if ("Cypher".equals(outputFormat)) {
                output = this.printerService.asCypher(compilationUnit);
            } else if ("GraphML".equals(outputFormat)) {
                output = this.printerService.asGraphMl(compilationUnit);
            } else {
                System.err.println("Unrecognised output format: " + outputFormat);
            }

            this.setParseResult(output);
        });

    }


    public void setParseResult(String resultString) {
        System.out.println("TRACE: public void setParseResult(String resultString) {");
        this.outputTextArea.setText(resultString);
    }


    public void setParseResultTextPane(String string) {
        System.out.println("TRACE: public void setParseResultTextPane(String string) {");
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
    private class TNode {
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

    public class MyTreeCellRenderer extends DefaultTreeCellRenderer {

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);

            Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
            if (userObject instanceof String) {
                return this;
            } else if (userObject instanceof TNode) {
                TNode tNode        = (TNode) userObject;
                Node  selectedNode = tNode.getNode();
                if (selectedNode instanceof Name) {
                    this.setForeground(JBColor.BLUE);
                } else if (selectedNode instanceof Comment) {
                    this.setForeground(JBColor.GRAY);
                } else if (selectedNode instanceof LiteralExpr) {
                    this.setForeground(JBColor.GREEN);
                } else {
                    // Use defaults
                }
                return this;
            } else {
                return this;
            }
        }
    }

}


