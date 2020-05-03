package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.output_results_tabs;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.printers.ASCIITreePrinter;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.services.HighlightingService;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.NodeDetailsTextPane;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.forms.AstInspectorToolWindow;
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

public class ParseResultsTabPane extends JPanel {

    private static final NotificationLogger notificationLogger = new NotificationLogger(ParseResultsTabPane.class);

    private final JPanel panel_export;
    private final JPanel panel_inspect;
    private final JPanel panel_log;
    private final JPanel panel_parseResults;
    private final JPanel panel_tokens;

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
        this.add(this.tabbedPane);
    }


    public String getPaneTitle() {
        notificationLogger.traceEnter(this.project);
        return this.psiFile.getName();
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

        private final JBTextArea                   exportTextarea;
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


            this.exportTextarea = new JBTextArea();
            if (parseResult.isSuccessful() && parseResult.getResult().isPresent()) {
                this.exportTextarea.setText(parseResult.getResult().get().toString());
            } else {
                this.exportTextarea.setText(parseResult.getResult().toString());
            }

            this.add(this.exportTextarea);
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
            final JSplitPane   splitPane             = new JSplitPane();
            final JBScrollPane treeScrollPane        = new JBScrollPane();
            final JBScrollPane nodeDetailsScrollPane = new JBScrollPane();

            //
            treeScrollPane.add(this.tree);
            nodeDetailsScrollPane.add(this.nodeDetailsTextPane);
            //
            splitPane.setDividerLocation(300);
            splitPane.setLeftComponent(treeScrollPane);
            splitPane.setRightComponent(nodeDetailsScrollPane);

            //
            this.setLayout(new GridLayout(1, 1));
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


        private Tree setupTree() {
            notificationLogger.traceEnter(this.project);

            final Tree tree = new Tree();
            new TreeSpeedSearch(tree); // Note: Just calling the constructor is enough to enable speed search.

            // Custom renderer -- e.g. to set colours on the nodes
            tree.setCellRenderer(new AstInspectorToolWindow.MyTreeCellRenderer());

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


        private void updateTree(CompilationUnit compilationUnit) {
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
