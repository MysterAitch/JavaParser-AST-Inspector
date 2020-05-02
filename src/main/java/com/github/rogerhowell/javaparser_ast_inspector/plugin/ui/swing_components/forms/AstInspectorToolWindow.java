package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.forms;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Problem;
import com.github.javaparser.Providers;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.util.PsiUtil;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.printers.ASCIITreePrinter;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.services.HighlightingService;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.services.PrinterService;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.NodeDetailsTextPane;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.combo_items.CharacterEncodingComboItem;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.combo_items.CustomComboItem;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.combo_items.LanguageLevelComboItem;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.combo_items.StringComboItem;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.util.Constants;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.util.NotificationLogger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.components.JBCheckBox;
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
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class AstInspectorToolWindow implements DumbAwareForm {

    private static final NotificationLogger notificationLogger = new NotificationLogger(AstInspectorToolWindow.class);

    @NotNull
    private final Project project;

    @NotNull
    private final ToolWindow toolWindow;

    // Form Elements
    private JPanel mainPanel;

    private JComboBox<StringComboItem>            exportAsCombobox;
    private JComboBox<CharacterEncodingComboItem> characterEncodingCombobox;
    private JComboBox<LanguageLevelComboItem>     languageLevelCombobox;

    private JCheckBox           attributeCommentsCheckbox;
    private JCheckBox           storeTokensCheckbox;
    private JCheckBox           outputNodeTypeCheckBox;
    private JTree               tree1;
    private JSpinner            tabSizeSpinner;
    private JButton             gitHubButton;
    private JButton             javaParserButton;
    private JButton             resetButton;
    private JButton             parseButton;
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


    public static <E, I extends CustomComboItem<E>> void setSelectedValue(JComboBox<I> comboBox, E value) {
        I item;
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            item = comboBox.getItemAt(i);
            if (item.getValue().equals(value)) {
                comboBox.setSelectedIndex(i);
                break;
            }
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
        final ParserConfiguration defaultConfig = new ParserConfiguration();
        this.initConfigForm(defaultConfig);

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
            final JavaParser javaParser = new JavaParser(this.getConfigFromForm());

            // parse result
//            final Optional<ParseResult<CompilationUnit>> optionalParseResult = parsePsiFile_editorContents(psiFile);
            final Optional<ParseResult<CompilationUnit>> optionalParseResult = this.parsePsiFile_diskContents(javaParser, psiFile);

            if (optionalParseResult.isPresent()) {
                final ParseResult<CompilationUnit> parseResult = optionalParseResult.get();

                if (!parseResult.isSuccessful()) {
                    notificationLogger.warn(this.project, "Parsing has been unsuccessful.");
                }
                if (!parseResult.getProblems().isEmpty()) {
                    StringBuilder message = new StringBuilder("Found " + parseResult.getProblems().size() + " problems found when parsing: ");
                    final List<Problem> problems = parseResult.getProblems();
                    for (int i = 0; i < problems.size(); i++) {
                        final Problem problem = problems.get(i);
                        message.append("\n")
                               .append("\t").append("Problem #").append(i).append(": ").append(problem.getMessage());
                    }
                    notificationLogger.warn(this.project, message.toString());
                }

                final Optional<CompilationUnit> optionalCu = this.handleParseResult(parseResult);
                return optionalCu;
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


    public boolean getAttributeComments() {
        return this.attributeCommentsCheckbox.isSelected();
    }


    public ParserConfiguration getConfigFromForm() {
        notificationLogger.traceEnter(this.project);

        ParserConfiguration config = new ParserConfiguration();

        config.setLanguageLevel(this.getSelectedLanguageLevel());
        config.setCharacterEncoding(this.getSelectedCharacterSet());
        config.setTabSize(this.getTabSize());
        config.setAttributeComments(this.getAttributeComments());
        config.setStoreTokens(this.getStoreTokens());

        return config;
    }


    @Override
    public Optional<JPanel> getMainPanel() {
        return Optional.ofNullable(this.mainPanel);
    }


    public String getOutputFormat() {
        Object item  = this.exportAsCombobox.getSelectedItem();
        String value = String.valueOf(item);
        return value;
    }


    public boolean getOutputNodeType() {
        return this.outputNodeTypeCheckBox.isSelected();
    }


    public Charset getSelectedCharacterSet() {
        Object  item  = this.characterEncodingCombobox.getSelectedItem();
        Charset value = ((CharacterEncodingComboItem) item).getValue();
        return value;
    }


    public ParserConfiguration.LanguageLevel getSelectedLanguageLevel() {
        Object                            item  = this.languageLevelCombobox.getSelectedItem();
        ParserConfiguration.LanguageLevel value = ((LanguageLevelComboItem) item).getValue();
        return value;
    }


    public boolean getStoreTokens() {
        return this.storeTokensCheckbox.isSelected();
    }


    public int getTabSize() {
        return Integer.parseInt(this.tabSizeSpinner.getValue().toString(), 10);
    }


    private Optional<CompilationUnit> handleParseResult(ParseResult<CompilationUnit> parseResult) {
        notificationLogger.traceEnter(this.project);

        if (parseResult.getResult().isPresent()) {
            notificationLogger.debug(this.project, "Parse result present");
            return parseResult.getResult();
        } else {
            notificationLogger.error(this.project, "Parse result null or not present.");
            notificationLogger.info(this.project, "optionalResult = " + parseResult);
            return Optional.empty();
        }
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


    private void initConfigForm(ParserConfiguration parserConfiguration) {
        notificationLogger.traceEnter(this.project);

        // Initialise form elements
        this.attributeCommentsCheckbox = new JBCheckBox();
        this.storeTokensCheckbox = new JBCheckBox();
        this.tabSizeSpinner = new JBIntSpinner(0, 0, 50, 1);
        this.outputNodeTypeCheckBox = new JBCheckBox();

        // Setup combobox and populate its options
        this.initialiseAndPopulateLanguageLevelOptions();
        this.initialiseAndPopulateCharacterEncodingOptions();
        this.initialiseAndPopulateExportAsCombobox();

        // Tooltips
        this.attributeCommentsCheckbox.setToolTipText("When false, all comments will be orphaned.");
        this.storeTokensCheckbox.setToolTipText("");
        this.tabSizeSpinner.setToolTipText(
                "How many characters should a tab character be considered equal to? " +
                "\nNote that a tab character is only a single character within a string." +
                "\nYou might opt to shift the column of the range by e.g. 4 characters for each single tab character" +
                "\n (and this will be reflected in the node's range)," +
                " but you must ensure that any other tools take this into account.");
        this.outputNodeTypeCheckBox.setToolTipText("In the exported text, should the node type be included?");
        this.languageLevelCombobox.setToolTipText("Which language features should be considered valid or invalid when validating the AST?");
        this.characterEncodingCombobox.setToolTipText("The file encoding of the input file - currently only UTF8 supported.");
        this.exportAsCombobox.setToolTipText("Output format.");


        // Set parser defaults
        this.updateConfigUi(parserConfiguration);

        // Set export / printer defaults
        this.setExportAs("Custom DOT");
        this.setOutputNodeType(true);

    }


    private void initialiseAndPopulateCharacterEncodingOptions() {
        // Initialise
        this.characterEncodingCombobox = new ComboBox<>();

        // Populate
        this.characterEncodingCombobox.addItem(new CharacterEncodingComboItem("UTF-8", Providers.UTF8));
    }


    public void initialiseAndPopulateExportAsCombobox() {
        // Initialise
        this.exportAsCombobox = new ComboBox<>();

        // Populate
        this.exportAsCombobox.addItem(new StringComboItem("DOT", "DOT"));
        this.exportAsCombobox.addItem(new StringComboItem("XML", "XML"));
        this.exportAsCombobox.addItem(new StringComboItem("Java", "Java"));
        this.exportAsCombobox.addItem(new StringComboItem("Java (pretty print)", "Java (pretty print)"));
        this.exportAsCombobox.addItem(new StringComboItem("ASCII Tree", "ASCII Tree"));
        this.exportAsCombobox.addItem(new StringComboItem("YAML", "YAML"));
        this.exportAsCombobox.addItem(new StringComboItem("Custom DOT", "Custom DOT"));
        this.exportAsCombobox.addItem(new StringComboItem("Custom DOT Image", "Custom DOT Image"));
        this.exportAsCombobox.addItem(new StringComboItem("Custom JSON", "Custom JSON"));
        this.exportAsCombobox.addItem(new StringComboItem("Cypher", "Cypher"));
        this.exportAsCombobox.addItem(new StringComboItem("GraphML", "GraphML"));
    }


    private void initialiseAndPopulateLanguageLevelOptions() {
        // Initialise
        this.languageLevelCombobox = new ComboBox<>();

        // Populate
        this.languageLevelCombobox.addItem(new LanguageLevelComboItem("CURRENT (13)", ParserConfiguration.LanguageLevel.CURRENT));
        this.languageLevelCombobox.addItem(new LanguageLevelComboItem("BLEEDING EDGE (14)", ParserConfiguration.LanguageLevel.BLEEDING_EDGE));
        this.languageLevelCombobox.addItem(new LanguageLevelComboItem("POPULAR (8)", ParserConfiguration.LanguageLevel.POPULAR));
        this.languageLevelCombobox.addItem(new LanguageLevelComboItem("RAW", ParserConfiguration.LanguageLevel.RAW));
        this.languageLevelCombobox.addItem(new LanguageLevelComboItem("JAVA 14", ParserConfiguration.LanguageLevel.JAVA_14));
        this.languageLevelCombobox.addItem(new LanguageLevelComboItem("JAVA 13", ParserConfiguration.LanguageLevel.JAVA_13));
        this.languageLevelCombobox.addItem(new LanguageLevelComboItem("JAVA 12", ParserConfiguration.LanguageLevel.JAVA_12));
        this.languageLevelCombobox.addItem(new LanguageLevelComboItem("JAVA 11", ParserConfiguration.LanguageLevel.JAVA_11));
        this.languageLevelCombobox.addItem(new LanguageLevelComboItem("JAVA 10", ParserConfiguration.LanguageLevel.JAVA_10));
        this.languageLevelCombobox.addItem(new LanguageLevelComboItem("JAVA 9", ParserConfiguration.LanguageLevel.JAVA_9));
        this.languageLevelCombobox.addItem(new LanguageLevelComboItem("JAVA 8", ParserConfiguration.LanguageLevel.JAVA_8));
        this.languageLevelCombobox.addItem(new LanguageLevelComboItem("JAVA 7", ParserConfiguration.LanguageLevel.JAVA_7));
        this.languageLevelCombobox.addItem(new LanguageLevelComboItem("JAVA 6", ParserConfiguration.LanguageLevel.JAVA_6));
        this.languageLevelCombobox.addItem(new LanguageLevelComboItem("JAVA 5", ParserConfiguration.LanguageLevel.JAVA_5));
        this.languageLevelCombobox.addItem(new LanguageLevelComboItem("JAVA 1.4", ParserConfiguration.LanguageLevel.JAVA_1_4));
        this.languageLevelCombobox.addItem(new LanguageLevelComboItem("JAVA 1.3", ParserConfiguration.LanguageLevel.JAVA_1_3));
        this.languageLevelCombobox.addItem(new LanguageLevelComboItem("JAVA 1.2", ParserConfiguration.LanguageLevel.JAVA_1_2));
        this.languageLevelCombobox.addItem(new LanguageLevelComboItem("JAVA 1.1", ParserConfiguration.LanguageLevel.JAVA_1_1));
        this.languageLevelCombobox.addItem(new LanguageLevelComboItem("JAVA 1.0", ParserConfiguration.LanguageLevel.JAVA_1_0));
    }


    private void parseButtonClickHandler() {
        notificationLogger.traceEnter(this.project);

        final Optional<CompilationUnit> optionalCu = this.doParse();
        if (optionalCu.isPresent()) {
            CompilationUnit compilationUnit = optionalCu.get();
            String          outputFormat    = this.getOutputFormat();
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


    public void setAttributeComments(boolean attributeComments) {
        this.attributeCommentsCheckbox.setSelected(attributeComments);
    }


    public void setCharacterEncoding(Charset charset) {
        setSelectedValue(this.characterEncodingCombobox, charset);
    }


    public void setExportAs(String key) {
        setSelectedValue(this.exportAsCombobox, key);
    }


    public void setOutputNodeType(boolean outputNodeType) {
        this.outputNodeTypeCheckBox.setSelected(outputNodeType);
    }


    private void setSelectedLanguageLevel(@NotNull ParserConfiguration.LanguageLevel languageLevel) {
        setSelectedValue(this.languageLevelCombobox, languageLevel);
    }


    public void setStoreTokens(boolean storeTokens) {
        this.storeTokensCheckbox.setSelected(storeTokens);
    }


    public void setTabSize(int tabSize) {
        this.tabSizeSpinner.setValue(tabSize);
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


    private void updateConfigUi(ParserConfiguration parserConfiguration) {
        notificationLogger.traceEnter(this.project);

        // Comboboxes
        this.setSelectedLanguageLevel(parserConfiguration.getLanguageLevel());
        this.setCharacterEncoding(parserConfiguration.getCharacterEncoding());

        // Inputs
        this.setTabSize(parserConfiguration.getTabSize());

        // Checkboxes
        this.attributeCommentsCheckbox.setSelected(parserConfiguration.isAttributeComments());
        this.storeTokensCheckbox.setSelected(parserConfiguration.isStoreTokens());
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
