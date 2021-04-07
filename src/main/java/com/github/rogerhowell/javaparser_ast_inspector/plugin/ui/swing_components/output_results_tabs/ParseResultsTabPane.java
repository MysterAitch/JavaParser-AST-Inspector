package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.output_results_tabs;

import com.github.javaparser.GeneratedJavaParserConstants;
import com.github.javaparser.JavaToken;
import com.github.javaparser.ParseResult;
import com.github.javaparser.Problem;
import com.github.javaparser.Range;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.logging.NotificationLogger;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.printers.ASCIITreePrinter;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.services.HighlightingService;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.services.PrinterService;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.NodeDetailsTextPane;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.config_panel.ConfigPanel;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.util.StringUtil;
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

import static com.github.rogerhowell.javaparser_ast_inspector.plugin.util.StringUtil.padEnd;

public class ParseResultsTabPane extends JPanel {

    private static final NotificationLogger notificationLogger = new NotificationLogger(ParseResultsTabPane.class);

    private static final String NEWLINE = String.format("%n");

    private static int selectedInnerTab = 0; // TODO: Can this be done without a static field? A plugin service maybe?

    private final PanelExport      panel_export;
    private final PanelInpsect     panel_inspect;
    private final PanelLog         panel_log;
    private final PanelParseResult panel_parseResults;
    private final PanelTokens      panel_tokens;

    @NotNull
    private final ParseResult<CompilationUnit> parseResult;

    @NotNull
    private final Project project;

    @NotNull
    private final PsiFile psiFile;

    @NotNull
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
        this.panel_log = new PanelLog(this.project, this.psiFile, this.parseResult);
        this.panel_parseResults = new PanelParseResult(this.project, this.psiFile, this.parseResult);
        this.panel_tokens = new PanelTokens(this.project, this.psiFile, this.parseResult);

        // Create pane container
        this.tabbedPane = new JBTabbedPane();
        this.tabbedPane.setTabPlacement(SwingConstants.LEFT);
        this.tabbedPane.add("Inspect", this.panel_inspect);
        this.tabbedPane.add("Export", this.panel_export);
        this.tabbedPane.add("Log", this.panel_log);
        this.tabbedPane.add("Parse Result", this.panel_parseResults);
        this.tabbedPane.add("Tokens", this.panel_tokens);

        this.tabbedPane.setSelectedIndex(selectedInnerTab);
        this.tabbedPane.addChangeListener(e -> {
            final int selectedIndex = this.tabbedPane.getSelectedIndex();
            if (selectedInnerTab != selectedIndex) {
                selectedInnerTab = selectedIndex;
            }
        });

        //
        this.setLayout(new GridLayout(0, 1));
        this.add(this.tabbedPane);
    }


    public void appendToLog(String text) {
        this.panel_log.appendToLog(text);
    }


    public String getPaneTitle() {
        notificationLogger.traceEnter(this.project);
        return this.psiFile.getName();
    }


    public void handleParseResult(ConfigPanel configPanel, final PsiFile psiFile, ParseResult<CompilationUnit> parseResult) {
        notificationLogger.traceEnter(this.project);

        this.appendToLog(NEWLINE + "Handling parse result.");

        this.appendToLog(NEWLINE);
        this.appendToLog(NEWLINE + "Parser Configurations Options set:");
        this.appendToLog(NEWLINE + "==================================");
        this.appendToLog(NEWLINE + configPanel.parserOptionsAsPlaintextDisplayString());
        this.appendToLog(NEWLINE);

        this.appendToLog(NEWLINE);
        this.appendToLog(NEWLINE + "Export Configurations Options set:");
        this.appendToLog(NEWLINE + "==================================");
        this.appendToLog(NEWLINE + configPanel.exportOptionsAsPlaintextDisplayString());
        this.appendToLog(NEWLINE);


        this.appendToLog(NEWLINE);
        this.appendToLog(NEWLINE + "Parse result (overview)");
        this.appendToLog(NEWLINE + "=======================");
        this.appendToLog(NEWLINE + "Is successful: " + parseResult.isSuccessful());

        // Parse result not present or not successful
        if (!parseResult.isSuccessful()) {
            notificationLogger.warn(this.project, "Parsing has been unsuccessful.");
            this.appendToLog(NEWLINE);
            this.appendToLog(NEWLINE + "Parsing has been unsuccessful.");
        }
        if (!parseResult.getProblems().isEmpty()) {
            StringBuilder       message  = new StringBuilder("Found " + parseResult.getProblems().size() + " problems found when parsing: ");
            final List<Problem> problems = parseResult.getProblems();
            for (int i = 0; i < problems.size(); i++) {
                final Problem problem = problems.get(i);
                message.append(NEWLINE)
                       .append("\t").append("Problem #").append(i).append(": ").append(problem.getMessage());
            }
            notificationLogger.warn(this.project, message.toString());
            this.appendToLog(NEWLINE + message.toString());
        }
        if (!parseResult.getResult().isPresent()) {
            notificationLogger.error(this.project, "Parse result null or not present.");
            notificationLogger.info(this.project, "parseResult.getResult() = " + parseResult.getResult());
            this.appendToLog(NEWLINE + "Parse result null or not present.");
        }

        // Parse successful
        notificationLogger.debug(this.project, "Parse result: " + parseResult.toString());
        this.appendToLog(NEWLINE);
        this.appendToLog(NEWLINE + "Parse result (details)");
        this.appendToLog(NEWLINE + "======================");
        this.appendToLog(NEWLINE + parseResult.toString());


        // Update panels
        this.updateInspectPanel(configPanel);
        this.updateExportPanel(configPanel);
        this.updateLogPanel(configPanel);
        this.updateParseResultPanel(configPanel);
        this.updateTokensPanel(configPanel);
    }


    private void updateExportPanel(ConfigPanel configPanel) {
        notificationLogger.traceEnter(this.project);

        String  outputFormat    = configPanel.getSelectedExportType(); // FIXME: Take this into account
        boolean includeNodeType = configPanel.getOutputNodeType();

        final Optional<CompilationUnit> optionalCu = this.parseResult.getResult();
        if (optionalCu.isPresent()) {
            CompilationUnit compilationUnit = optionalCu.get();
            this.panel_export.updateExport(outputFormat, compilationUnit, includeNodeType);
        } else {
            notificationLogger.warn(this.project, "Compilation Unit not found.");
        }
    }


    private void updateInspectPanel(ConfigPanel configPanel) {
        this.updateTree();
    }


    private void updateLogPanel(final ConfigPanel configPanel) {
        // TODO
    }


    private void updateParseResultPanel(final ConfigPanel configPanel) {
        this.panel_parseResults.updateOutput(this.parseResult);
    }


    private void updateTokensPanel(final ConfigPanel configPanel) {
        // TODO
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


    private static class MyTreeCellRenderer extends DefaultTreeCellRenderer {

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


        public void updateExport(final String outputFormat, final CompilationUnit compilationUnit, boolean includeNodeType) {
            notificationLogger.traceEnter(this.project);

            String output = PrinterService.getInstance(this.project).outputAs(outputFormat, compilationUnit, includeNodeType);

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
            tree.addMouseListener(new MyMouseAdapter(tree));

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


        private static class MyMouseAdapter extends MouseAdapter {
            private final Tree tree;


            private MyMouseAdapter(Tree tree) {
                super();
                this.tree = tree;
            }


            @Override
            public void mousePressed(MouseEvent e) {
                int      selRow  = this.tree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = this.tree.getPathForLocation(e.getX(), e.getY());
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
        }
    }

    private static class PanelLog extends JPanel {

        private final JBTextArea                   logTextDisplay;
        private final ParseResult<CompilationUnit> parseResult;
        private final Project                      project;
        private final PsiFile                      psiFile;


        PanelLog(Project project, PsiFile psiFile, final ParseResult<CompilationUnit> parseResult) {
            super();

            this.project = project;
            this.psiFile = psiFile;
            this.parseResult = parseResult;

            this.logTextDisplay = new JBTextArea();
            this.logTextDisplay.setText("Panel Created: " + System.currentTimeMillis());

            JBScrollPane jbScrollPane = new JBScrollPane(this.logTextDisplay);

            this.setLayout(new GridLayout(0, 1));
            this.add(jbScrollPane);


            // Update
            //            this.updateOutput(null);

        }


        public void appendToLog(String text) {
            this.logTextDisplay.append(text);
        }


        public void updateOutput(final ParseResult<? extends Node> parseResult) {
            notificationLogger.traceEnter(this.project);

            String output = "Not Yet Implemented.";

            //
            this.logTextDisplay.setText(output);
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

    private static class PanelParseResult extends JPanel {

        private final JBTextArea                   exportTextDisplay;
        private final ParseResult<CompilationUnit> parseResult;
        private final Project                      project;
        private final PsiFile                      psiFile;


        PanelParseResult(Project project, PsiFile psiFile, final ParseResult<CompilationUnit> parseResult) {
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


        public void updateOutput(final ParseResult<? extends Node> parseResult) {
            notificationLogger.traceEnter(this.project);

            String output = "";

            //
            output += NEWLINE + "== PARSE RESULT SUMMARY ==";
            output += NEWLINE + " - Is successful: " + parseResult.isSuccessful();
            output += NEWLINE + " - Problem count: " + parseResult.getProblems().size();
            if (!parseResult.getProblems().isEmpty()) {
                StringBuilder message = new StringBuilder();
                message.append(NEWLINE + " - Found " + parseResult.getProblems().size() + " problems found when parsing: ");
                final List<Problem> problems = parseResult.getProblems();
                for (int i = 0; i < problems.size(); i++) {
                    final Problem problem = problems.get(i);
                    message.append(NEWLINE)
                           .append("\t").append("Problem #").append(i).append(": ").append(problem.getMessage());
                }

                output += message.toString();
            }

            //
            output += NEWLINE + NEWLINE;
            output += NEWLINE + "== PARSE RESULT DETAILS ==";
            output += NEWLINE + " - Parse result present: " + parseResult.getResult().isPresent();
            if (parseResult.getResult().isPresent()) {
                final Node node = parseResult.getResult().get();
                output += NEWLINE + " - Parse result type: " + node.getClass().getSimpleName();
                output += NEWLINE + " - Node summary: " + ASCIITreePrinter.CLASS_RANGE_SUMMARY_FORMAT.apply(node);
            } else {
                output += NEWLINE + " - Parse result not found -- parse catastrophically failed, perhaps?";
            }

            //
            output += NEWLINE + NEWLINE;
            output += NEWLINE + "== STORAGE ==";
            if (parseResult.getResult().isPresent()) {
                final Node                      node  = parseResult.getResult().get();
                final Optional<CompilationUnit> optCu = node.findCompilationUnit();
                if (optCu.isPresent()) {
                    final CompilationUnit                   cu              = optCu.get();
                    final Optional<CompilationUnit.Storage> optionalStorage = cu.getStorage();
                    if (optionalStorage.isPresent()) {
                        final CompilationUnit.Storage storage = optionalStorage.get();
                        output += NEWLINE + " - Filename: " + storage.getFileName();
                        output += NEWLINE + " - Path: " + storage.getPath();
                        output += NEWLINE + " - Encoding: " + storage.getEncoding();
                        output += NEWLINE + " - Source Root: " + storage.getSourceRoot();
                    } else {
                        output += NEWLINE + " - No storage found -- parsed from a string fragment, perhaps?";
                    }
                } else {
                    output += NEWLINE + " - No compilation unit found -- parsed from a string fragment, perhaps?";
                }
            } else {
                output += NEWLINE + " - No result found -- parse failed, perhaps?";
            }

            //
            this.exportTextDisplay.setText(output);
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

    private class PanelTokens extends JPanel {

        private final ParseResult<CompilationUnit> parseResult;
        private final Project                      project;
        private final PsiFile                      psiFile;
        private final JBTextArea                   tokensTextDisplay;


        PanelTokens(Project project, PsiFile psiFile, final ParseResult<CompilationUnit> parseResult) {
            super();

            this.project = project;
            this.psiFile = psiFile;
            this.parseResult = parseResult;

            this.tokensTextDisplay = new JBTextArea();

            JBScrollPane jbScrollPane = new JBScrollPane(this.tokensTextDisplay);

            this.setLayout(new GridLayout(0, 1));
            this.add(jbScrollPane);


            // Update
            this.updateOutput(parseResult);

        }


        public void updateOutput(final ParseResult<? extends Node> parseResult) {
            notificationLogger.traceEnter(this.project);

            StringBuilder output = new StringBuilder();

            if (parseResult.getResult().isPresent()) {
                final Node node = parseResult.getResult().get();
                if (node.getTokenRange().isPresent()) {
                    final TokenRange tokenRange = node.getTokenRange().get();

                    output.append(NEWLINE)
                          .append(padEnd("Token #", 15))
                          .append(padEnd("Token Category", 20))
                          .append(padEnd("Type (number)", 15))
                          .append(padEnd("Type (description)", 20))
                          .append(padEnd("Range", 35))
                          .append(padEnd("Text", 30));
                    output.append(NEWLINE)
                          .append(padEnd("-------", 15))
                          .append(padEnd("--------------", 20))
                          .append(padEnd("-------------", 15))
                          .append(padEnd("------------------", 20))
                          .append(padEnd("-----", 35))
                          .append(padEnd("----", 30));

                    int tokenIndex = 0;

                    JavaToken currentToken = tokenRange.getBegin();
                    while (currentToken.getNextToken().isPresent()) {

                        String text = StringUtil.escapeWhitespace(currentToken.getText());

                        output.append(NEWLINE)
                              .append(padEnd("Token #" + tokenIndex + ": ", 15))
                              .append(padEnd(currentToken.getCategory().toString(), 20))
                              .append(padEnd("<" + currentToken.getKind() + ">", 15))
                              .append(padEnd(GeneratedJavaParserConstants.tokenImage[currentToken.getKind()], 20))
                              .append(padEnd(currentToken.getRange().map(Range::toString).orElse("(?)-(?)"), 35))
                              .append(padEnd(text, 30));

                        tokenIndex++;
                        currentToken = currentToken.getNextToken().get();
                    }
                } else {
                    output.append(NEWLINE + "Parse result found, but no token range present -- unable to present tokens.");
                    ParseResultsTabPane.this.appendToLog(NEWLINE + "Parse result found, but no token range present -- unable to present tokens.");
                }
            } else {
                output.append(NEWLINE + "Parse result not present -- unable to present tokens.");
                ParseResultsTabPane.this.appendToLog(NEWLINE + "Parse result not present -- unable to present tokens.");
            }

            //
            this.tokensTextDisplay.setText(NEWLINE + output.toString());
        }
    }

}
