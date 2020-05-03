package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.output_results_tabs;

import com.github.javaparser.ParseResult;
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
import com.github.rogerhowell.javaparser_ast_inspector.plugin.util.NotificationLogger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.components.JBTabbedPane;
import com.intellij.ui.components.JBTextArea;
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
import java.util.List;
import java.util.Optional;

public class ParseResultsTabPane extends JPanel {

    private static final NotificationLogger notificationLogger = new NotificationLogger(ParseResultsTabPane.class);

    private final PanelExport  panel_export;
    private final PanelInpsect panel_inspect;
    private final JPanel       panel_log;
    private final JPanel       panel_parseResults;
    private final JPanel       panel_tokens;

    @NotNull
    private final ParseResult<CompilationUnit> parseResult;

    @NotNull
    private final Project project;

    @NotNull
    private final PsiFile psiFile;

    private final JBTabbedPane tabbedPane;


    public ParseResultsTabPane(@NotNull Project project, @NotNull PsiFile psiFile, @NotNull ParseResult<CompilationUnit> parseResult) {
        super();
        notificationLogger.traceEnter(project);

        // Parameters
        this.project = project;
        this.psiFile = psiFile;
        this.parseResult = parseResult;

        // Panes
        this.panel_inspect = new PanelInpsect(this.project, this.psiFile, this.parseResult);
        this.panel_export = new PanelExport(this.project, this.psiFile, this.parseResult);
        this.panel_log = new PanelNotImplemented(this.project, this.psiFile, this.parseResult);
        this.panel_parseResults = new PanelNotImplemented(this.project, this.psiFile, this.parseResult);
        this.panel_tokens = new PanelNotImplemented(this.project, this.psiFile, this.parseResult);

        // Create pane container
        this.tabbedPane = new JBTabbedPane();
        this.tabbedPane.setTabPlacement(SwingConstants.LEFT);
        this.tabbedPane.add("Inspect", this.panel_inspect);
        this.tabbedPane.add("Export", this.panel_export);
        this.tabbedPane.add("Log", this.panel_log);
        this.tabbedPane.add("Parse Result", this.panel_parseResults);
        this.tabbedPane.add("Tokens", this.panel_tokens);

        //
        this.setLayout(new GridLayout(0, 1));
        this.add(this.tabbedPane);
    }


    public String getPaneTitle() {
        notificationLogger.traceEnter(this.project);
        return this.psiFile.getName();
    }


    public void handleParseResult(ConfigPanel configPanel, final PsiFile psiFile, ParseResult<CompilationUnit> parseResult) {
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
        }

        // Parse successful
        notificationLogger.debug(this.project, "Parse result: " + parseResult.toString());


        // Update panels
        this.updateInspectPanel(configPanel);
        this.updateExportPanel(configPanel);
//        this.updateLogPanel(configPanel);
//        this.updateParseResultPanel(configPanel);
//        this.updateTokensPanel(configPanel);
    }


    private void updateExportPanel(ConfigPanel configPanel) {
        notificationLogger.traceEnter(this.project);

        String  outputFormat    = configPanel.getSelectedExportType(); // FIXME: Take this into account
        boolean includeNodeType = configPanel.getOutputNodeType();

        final Optional<CompilationUnit> optionalCu = this.parseResult.getResult();
        if (optionalCu.isPresent()) {
            CompilationUnit compilationUnit = optionalCu.get();
            this.panel_export.updateExport(outputFormat, compilationUnit);
        } else {
            notificationLogger.warn(this.project, "Compilation Unit not found.");
        }
    }


    private void updateInspectPanel(ConfigPanel configPanel) {
        this.updateTree();
    }


    private void updateTree() {
        notificationLogger.traceEnter(this.project);

        final Optional<CompilationUnit> optionalCu = this.parseResult.getResult();
        if (optionalCu.isPresent()) {
            CompilationUnit compilationUnit = optionalCu.get();
            this.panel_inspect.updateTree(compilationUnit);
        } else {
            notificationLogger.warn(this.project, "Compilation Unit not found.");
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

    private static class PanelExport extends JPanel {

        private final JBTextArea                   exportTextDisplay;
        private final ParseResult<CompilationUnit> parseResult;
        private final Project                      project;
        private final PsiFile                      psiFile;
        private       NodeDetailsTextPane          nodeDetailsTextPane;
        private       Tree                         tree;


        PanelExport(Project project, PsiFile psiFile, final ParseResult<CompilationUnit> parseResult) {
            super();

            this.project = project;
            this.psiFile = psiFile;
            this.parseResult = parseResult;


            this.exportTextDisplay = new JBTextArea();
            if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
                this.exportTextDisplay.setText(parseResult.getResult().get().toString());
            } else {
                this.exportTextDisplay.setText(parseResult.getResult().toString());
            }


            JBScrollPane jbScrollPane = new JBScrollPane(this.exportTextDisplay);

            this.setLayout(new GridLayout(0, 1));
            this.add(jbScrollPane);

        }


        public void setExportText(String text) {
            notificationLogger.traceEnter(this.project);
            this.exportTextDisplay.setText(text);
        }


        public void updateExport(final String outputFormat, final CompilationUnit compilationUnit) {
            notificationLogger.traceEnter(this.project);

            String output = PrinterService.getInstance(this.project).outputAs(outputFormat, compilationUnit);

            // If custom dot image, do the image in addition to the textual dot string
            if ("Custom DOT Image".equals(outputFormat)) {
//            this.outputCustomDotImage(this.project.getBasePath());
                notificationLogger.warn(this.project, "Custom DOT Image is temporarily unavailable -- see the export text for the raw DOT text output.");
            }

            this.setExportText(output);
        }

    }

    private static class PanelInpsect extends JPanel {

        private final NodeDetailsTextPane          nodeDetailsTextPane;
        private final ParseResult<CompilationUnit> parseResult;
        private final Project                      project;
        private final PsiFile                      psiFile;
        private final Tree                         tree;


        PanelInpsect(Project project, PsiFile psiFile, final ParseResult<CompilationUnit> parseResult) {
            super();
            this.project = project;
            this.psiFile = psiFile;
            this.parseResult = parseResult;

            //
            this.tree = this.setupTree();
            this.nodeDetailsTextPane = new NodeDetailsTextPane();


            //
            final JBScrollPane treeScrollPane        = new JBScrollPane(this.tree);
            final JBScrollPane nodeDetailsScrollPane = new JBScrollPane(this.nodeDetailsTextPane);

            //
            final JSplitPane splitPane = new JSplitPane();
            splitPane.setDividerLocation(400);
            splitPane.setLeftComponent(treeScrollPane);
            splitPane.setRightComponent(nodeDetailsScrollPane);

            //
            this.setLayout(new GridLayout(0, 1));
            this.add(splitPane);
        }


        private void astDisplaySelectionListener(TreeSelectionEvent e) {
            notificationLogger.traceEnter(this.project);

            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) this.tree.getLastSelectedPathComponent();
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

                HighlightingService.getInstance().updateHighlight(this.psiFile, editor);
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
                    int      selRow  = tree.getRowForLocation(e.getX(), e.getY());
                    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                    if (selRow != -1) {
                        if (e.getClickCount() == 1) {
                            notificationLogger.debug(null, String.format("SINGLE CLICK:: selRow: %d ;; selPath: %s", selRow, selPath));
//                        mySingleClick(selRow, selPath);
                        } else if (e.getClickCount() == 2) {
                            notificationLogger.debug(null, String.format("DOUBLE CLICK:: selRow: %d ;; selPath: %s", selRow, selPath));
//                        myDoubleClick(selRow, selPath);
                        }
                    }
                }
            });

            return tree;
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


        public void updateTree(CompilationUnit compilationUnit) {
            notificationLogger.traceEnter(this.project);

            if (compilationUnit == null) {
                final DefaultMutableTreeNode root = new DefaultMutableTreeNode("Not yet parsed.");
                this.tree.setModel(new DefaultTreeModel(root, false));

            } else {
                final DefaultMutableTreeNode root = this.buildTreeNodes(null, compilationUnit);
                this.tree.setModel(new DefaultTreeModel(root, false));
            }

            // Nudge the UI to update
            this.tree.updateUI();
        }


    }

    private static class PanelNotImplemented extends JPanel {
        private final ParseResult<CompilationUnit> parseResult;
        private final Project                      project;
        private final PsiFile                      psiFile;


        PanelNotImplemented(Project project, PsiFile psiFile, final ParseResult<CompilationUnit> parseResult) {
            super();
            this.project = project;
            this.psiFile = psiFile;
            this.parseResult = parseResult;

            this.add(new JLabel("No content yet."));
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

}
