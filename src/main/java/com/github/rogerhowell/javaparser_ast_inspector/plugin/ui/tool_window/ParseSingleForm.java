package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.tool_window;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Providers;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.metamodel.NodeMetaModel;
import com.github.javaparser.metamodel.PropertyMetaModel;
import com.github.javaparser.printer.DotPrinter;
import com.github.javaparser.printer.XmlPrinter;
import com.github.javaparser.printer.YamlPrinter;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.printers.ASCIITreePrinter;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.printers.CustomDotPrinter;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.printers.CustomJsonPrinter;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.printers.CypherPrinter;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.printers.GraphMLPrinter;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.services.HighlightingService;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.services.JavaParserService;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.components.CharacterEncodingComboItem;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.components.LanguageLevelComboItem;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileSystemItem;
import com.intellij.psi.PsiManager;
import com.intellij.ui.JBColor;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.Renderer;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import org.jetbrains.annotations.SystemIndependent;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
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
import java.util.function.Function;

import static com.github.javaparser.ParserConfiguration.LanguageLevel;
import static java.util.stream.Collectors.toList;

public class ParseSingleForm {

    /**
     * Print format each {@link Node} in the tree (prints to a single line) for example:
     * <PRE>
     * CompilationUnit (1,1)-(15,3) : "@Deprecated...}"
     * \____________/  \__________/ : \_______________/
     * node class     node range  :   node summary
     * </PRE>
     *
     * @see ASCIITreePrinter#printNodeSummary(Node)
     * @see ASCIITreePrinter#printRange(Node)
     */
    public static final Function<Node, String> CLASS_RANGE_SUMMARY_FORMAT = n -> n.getClass().getSimpleName() + " " + ASCIITreePrinter.printRangeCoordinates(n) + " : \"" + ASCIITreePrinter.printNodeSummary(n) + "\"";

    private static final String     EOL = System.lineSeparator();
    private final        Project    project;
    private final        ToolWindow toolWindow;

    private final HighlightingService hls;
    private final JavaParserService   javaParserService;


    private JPanel                                panel1;
    private JCheckBox                             attributeCommentsCheckbox;
    private JComboBox<LanguageLevelComboItem>     languageLevelComboBox;
    private JComboBox<CharacterEncodingComboItem> characterEncodingComboBox;
    private JButton                               parseButton;
    private JTextField                            tabSizeTextField;
    private JCheckBox                             storeTokensCheckbox;
    private JTextArea                             outputTextArea;
    private JTextPane                             parseResultTextPane;
    private JComboBox<String>                     outputFormatComboBox;
    private JPanel                                imagePanel;
    private JCheckBox                             outputNodeTypeCheckBox;
    private Tree                                  tree1;
    private JTabbedPane                           tabbedPane1;
    private JTextPane                             sidebar_label;
    private JLabel                                label_selected;
    private JLabel                                imageLabel;
    private ParseResult<CompilationUnit>          result;


    public ParseSingleForm(final Project project, final ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;

        // Services
        this.hls = HighlightingService.getInstance();
        this.javaParserService = JavaParserService.getInstance(this.project);


        // Setup form (e.g. combobox values)
        this.setupLanguageLevelOptions();
        this.setupCharacterEncodingOptions();
        this.setupOutputFormatCombobox();
        this.setOutputNodeType(true);

        // Set defaults/values
        final ParserConfiguration config = this.javaParserService.getConfiguration();
        this.setTabSize(config.getTabSize());
        this.setAttributeComments(config.isAttributeComments());
        this.setStoreTokens(config.isStoreTokens());

        // Add event handlers
        this.parseButton.addActionListener(e -> this.parseButtonClickHandler());

    }


    private void appendLine(StyledDocument doc, String s, SimpleAttributeSet style) {
        this.appendString(doc, s + EOL, style);
    }


    private void appendString(StyledDocument doc, String s, SimpleAttributeSet style) {
        try {
            doc.insertString(doc.getLength(), s, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
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
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Not yet parsed.");
        this.tree1 = new Tree(root);

        // Custom renderer -- e.g. to set colours on the nodes
        this.tree1.setCellRenderer(new MyTreeCellRenderer());

        // Click handler for selection of AST nodes
        this.tree1.getSelectionModel().addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) this.tree1.getLastSelectedPathComponent();
            if (selectedNode != null) {
                final Object node  = selectedNode.getUserObject();
                final TNode  tNode = (TNode) node;

                // Update "selected" label
                this.label_selected.setText("Selected: [" + tNode.toString() + "]");

                // Update the side panel
                this.updateSidebar(selectedNode);

                this.hls.setSelectedNode(tNode.getNode());
            }
        });
    }


    public void doParse() {
        int           tabSize       = this.getTabSize();
        Charset       charset       = this.getSelectedCharacterSet();
        LanguageLevel languageLevel = this.getSelectedLanguageLevel();

        ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration.setTabSize(tabSize);
        parserConfiguration.setCharacterEncoding(charset);
        parserConfiguration.setLanguageLevel(languageLevel);

        JavaParser javaParser = new JavaParser(parserConfiguration);
//        this.result = javaParser.parse(this.getInputText());

        this.getCurrentFile().ifPresent(psiFile -> {
            final Path path = Paths.get(Objects.requireNonNull(psiFile.getVirtualFile().getCanonicalPath()));

            try {
                this.result = javaParser.parse(path);

                this.setParseResultTextPane("Result Present: " + this.result.getResult().isPresent() + "\n" + "Parse Result: " + this.result.toString());

                final CompilationUnit compilationUnit = this.result.getResult().get();

                final DefaultMutableTreeNode root = this.buildTreeNodes(null, compilationUnit);
                this.tree1.setModel(new DefaultTreeModel(root, false));

            } catch (IOException e) {
                e.printStackTrace();
            }

        });


    }


    public boolean getAttributeComments() {
        return this.attributeCommentsCheckbox.isSelected();
    }


    public void setAttributeComments(boolean attributeComments) {
        this.attributeCommentsCheckbox.setSelected(attributeComments);
    }


    public Optional<PsiFile> getCurrentFile() {
        FileEditorManager manager = FileEditorManager.getInstance(this.project);
        VirtualFile[]     files   = manager.getSelectedFiles();
        if (files.length == 0) {
            return Optional.empty();
        }

        final VirtualFile currentFile = files[0];
        final PsiFile     psiFile     = PsiManager.getInstance(this.project).findFile(currentFile);

        return Optional.ofNullable(psiFile);
    }


    public String getInputText() {
        final Optional<PsiFile> psiFile = this.getCurrentFile();
        if (!psiFile.isPresent()) {
            return "";
        }
        return psiFile.get().getText();
    }


    public String getOutputFormat() {
        Object item  = this.outputFormatComboBox.getSelectedItem();
        String value = (String) item;
        return value;
    }


    public boolean getOutputNodeType() {
        return this.outputNodeTypeCheckBox.isSelected();
    }


    public void setOutputNodeType(boolean outputNodeType) {
        this.outputNodeTypeCheckBox.setSelected(outputNodeType);
    }


    public JPanel getPanel() {
        return this.panel1;
    }


    public Charset getSelectedCharacterSet() {
        Object  item  = this.characterEncodingComboBox.getSelectedItem();
        Charset value = ((CharacterEncodingComboItem) item).getValue();
        return value;
    }


    public LanguageLevel getSelectedLanguageLevel() {
        Object        item  = this.languageLevelComboBox.getSelectedItem();
        LanguageLevel value = ((LanguageLevelComboItem) item).getValue();
        return value;
    }


    private void setSelectedLanguageLevel(LanguageLevel languageLevel) {
        this.languageLevelComboBox.setSelectedItem(LanguageLevel.CURRENT);
    }


    public boolean getStoreTokens() {
        return this.storeTokensCheckbox.isSelected();
    }


    public void setStoreTokens(boolean storeTokens) {
        this.storeTokensCheckbox.setSelected(storeTokens);
    }


    public int getTabSize() {
        return Integer.parseInt(this.tabSizeTextField.getText(), 10);
    }


    public void setTabSize(int tabSize) {
        this.tabSizeTextField.setText(String.valueOf(tabSize));
    }


    public void outputAsciiTreeText() {
        if (this.result.getResult().isPresent()) {
            ASCIITreePrinter printer = new ASCIITreePrinter();
            String           output  = printer.output(this.result.getResult().get());
            System.out.println(output);
            this.setParseResult(output);
        }
    }


    public void outputCustomDot() {
        if (this.result.getResult().isPresent()) {
            CustomDotPrinter printer = new CustomDotPrinter(this.getOutputNodeType());
            this.setParseResult(printer.output(this.result.getResult().get()));
        }
    }


    public void outputCustomDotImage(final @SystemIndependent String basePath) {
        if (this.result.getResult().isPresent()) {
            CustomDotPrinter printer   = new CustomDotPrinter(this.getOutputNodeType());
            String           dotOutput = printer.output(this.result.getResult().get());
            this.setParseResult(dotOutput);

            // Try to parse the dot file and generate a png image, that is then included.
            try {
                final MutableGraph      g           = new Parser().read(dotOutput);
                final Renderer          pngRenderer = Graphviz.fromGraph(g).render(Format.PNG);
                final Optional<PsiFile> currentFile = this.getCurrentFile();

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


    public void outputCustomJson() {
        if (this.result.getResult().isPresent()) {
            CustomJsonPrinter printer = new CustomJsonPrinter(this.getOutputNodeType());
            this.setParseResult(printer.output(this.result.getResult().get()));
        }
    }


    public void outputCypher() {
        if (this.result.getResult().isPresent()) {
            CypherPrinter printer = new CypherPrinter(this.getOutputNodeType());
            this.setParseResult(printer.output(this.result.getResult().get()));
        }
    }


    public void outputDot() {
        if (this.result.getResult().isPresent()) {
            DotPrinter printer = new DotPrinter(this.getOutputNodeType());
            this.setParseResult(printer.output(this.result.getResult().get()));
        }
    }


    public void outputGraphMl() {
        if (this.result.getResult().isPresent()) {
            GraphMLPrinter printer = new GraphMLPrinter(this.getOutputNodeType());
            this.setParseResult(printer.output(this.result.getResult().get()));
        }
    }


    public void outputParsedJava() {
        if (this.result.getResult().isPresent()) {
            this.setParseResult(this.result.getResult().get().toString());
        }
    }


    public void outputXml() {
        if (this.result.getResult().isPresent()) {
            XmlPrinter printer = new XmlPrinter(this.getOutputNodeType());
            this.setParseResult(printer.output(this.result.getResult().get()));
        }
    }


    public void outputYaml() {
        if (this.result.getResult().isPresent()) {
            YamlPrinter printer = new YamlPrinter(this.getOutputNodeType());
            this.setParseResult(printer.output(this.result.getResult().get()));
        }
    }


    private void parseButtonClickHandler() {
        this.doParse();

        if ("YAML".equals(this.getOutputFormat())) {
            this.outputYaml();
        } else if ("XML".equals(this.getOutputFormat())) {
            this.outputXml();
        } else if ("DOT".equals(this.getOutputFormat())) {
            this.outputDot();
        } else if ("Java".equals(this.getOutputFormat())) {
            this.outputParsedJava();
        } else if ("ASCII Tree".equals(this.getOutputFormat())) {
            this.outputAsciiTreeText();
        } else if ("Custom DOT".equals(this.getOutputFormat())) {
            this.outputCustomDot();
        } else if ("Custom DOT Image".equals(this.getOutputFormat())) {
            this.outputCustomDotImage(this.project.getBasePath());
        } else if ("Custom JSON".equals(this.getOutputFormat())) {
            this.outputCustomJson();
        } else if ("Cypher".equals(this.getOutputFormat())) {
            this.outputCypher();
        } else if ("GraphML".equals(this.getOutputFormat())) {
            this.outputGraphMl();
        } else {
            System.err.println("Unrecognised output format: " + this.getOutputFormat());
        }
    }


    public void setParseResult(String resultString) {
        this.outputTextArea.setText(resultString);
    }


    public void setParseResultTextPane(String string) {
        this.parseResultTextPane.setText(string);
    }


    private void setupCharacterEncodingOptions() {
        this.characterEncodingComboBox.addItem(new CharacterEncodingComboItem("UTF-8", Providers.UTF8));
    }


    private void setupLanguageLevelOptions() {
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("CURRENT (13)", LanguageLevel.CURRENT));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("BLEEDING EDGE (14)", LanguageLevel.BLEEDING_EDGE));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("POPULAR (8)", LanguageLevel.POPULAR));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("RAW", LanguageLevel.RAW));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 14", LanguageLevel.JAVA_14));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 13", LanguageLevel.JAVA_13));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 12", LanguageLevel.JAVA_12));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 11", LanguageLevel.JAVA_11));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 10", LanguageLevel.JAVA_10));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 9", LanguageLevel.JAVA_9));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 8", LanguageLevel.JAVA_8));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 7", LanguageLevel.JAVA_7));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 6", LanguageLevel.JAVA_6));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 5", LanguageLevel.JAVA_5));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 1.4", LanguageLevel.JAVA_1_4));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 1.3", LanguageLevel.JAVA_1_3));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 1.2", LanguageLevel.JAVA_1_2));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 1.1", LanguageLevel.JAVA_1_1));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 1.0", LanguageLevel.JAVA_1_0));
    }


    public void setupOutputFormatCombobox() {
        this.outputFormatComboBox.addItem("DOT");
        this.outputFormatComboBox.addItem("XML");
        this.outputFormatComboBox.addItem("Java");
        this.outputFormatComboBox.addItem("Java");
        this.outputFormatComboBox.addItem("ASCII Tree");
        this.outputFormatComboBox.addItem("YAML");
        this.outputFormatComboBox.addItem("Custom DOT");
        this.outputFormatComboBox.addItem("Custom DOT Image");
        this.outputFormatComboBox.addItem("Custom JSON");
        this.outputFormatComboBox.addItem("Cypher");
        this.outputFormatComboBox.addItem("GraphML");
    }


    private void updateSidebar(DefaultMutableTreeNode selectedTreeNode) {
        final Object node         = selectedTreeNode.getUserObject();
        final TNode  tNode        = (TNode) node;
        final Node   selectedNode = tNode.getNode();

        // Reset the sidebar content, ready to be inserted into again:
        this.sidebar_label.setText("");
        StyledDocument doc = (StyledDocument) this.sidebar_label.getDocument();


        SimpleAttributeSet normal = new SimpleAttributeSet();
        StyleConstants.setFontFamily(normal, "Monospaced");
//        StyleConstants.setFontFamily(normal, "SansSerif");
//        StyleConstants.setFontSize(normal, 12);

        SimpleAttributeSet boldBlue = new SimpleAttributeSet(normal);
        StyleConstants.setBold(boldBlue, true);
        StyleConstants.setForeground(boldBlue, JBColor.BLUE);

        SimpleAttributeSet highAlert = new SimpleAttributeSet(boldBlue);
//        StyleConstants.setFontSize(highAlert, 14);
        StyleConstants.setItalic(highAlert, true);
        StyleConstants.setForeground(highAlert, JBColor.RED);


        // Update the side panel

        final String H_LINE = "----------------------------------------";

        final NodeMetaModel           metaModel             = selectedNode.getMetaModel();
        final List<PropertyMetaModel> allPropertyMetaModels = metaModel.getAllPropertyMetaModels();
        final List<PropertyMetaModel> attributes            = allPropertyMetaModels.stream().filter(PropertyMetaModel::isAttribute).filter(PropertyMetaModel::isSingular).collect(toList());
        final List<PropertyMetaModel> subNodes              = allPropertyMetaModels.stream().filter(PropertyMetaModel::isNode).filter(PropertyMetaModel::isSingular).collect(toList());
        final List<PropertyMetaModel> subLists              = allPropertyMetaModels.stream().filter(PropertyMetaModel::isNodeList).collect(toList());


        this.appendLine(doc, "DETAILS ", boldBlue);
        this.appendLine(doc, H_LINE, boldBlue);
        this.appendLine(doc, " - TYPE: " + metaModel.getTypeName(), normal);
        this.appendString(doc, " - RANGE: ", normal);
        if (selectedNode.getRange().isPresent()) {
            this.appendLine(doc, selectedNode.getRange().get().toString(), normal);
        } else {
            this.appendLine(doc, "[NOT PRESENT]", normal);
        }
        this.appendLine(doc, " - NODE SUMMARY: " + ASCIITreePrinter.printNodeSummary(selectedNode), normal);


        // Object creation
        if (selectedNode.getClass().getSimpleName().equals("ObjectCreationExpr")) {
            this.appendLine(doc, "", boldBlue);
            this.appendLine(doc, "", boldBlue);
            this.appendLine(doc, "ObjectCreationExpr", boldBlue);
            this.appendLine(doc, H_LINE, boldBlue);

            final ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr) selectedNode;
            this.appendLine(doc, " - _typeNameString:" + objectCreationExpr.getType().getName().asString(), normal);
        }


        this.appendLine(doc, "", normal);
        this.appendLine(doc, "", normal);
        this.appendLine(doc, "ATTRIBUTES ", boldBlue);
        this.appendLine(doc, H_LINE, boldBlue);
        for (final PropertyMetaModel attributeMetaModel : attributes) {
            this.appendLine(doc, " - " + attributeMetaModel.getName() + ":" + attributeMetaModel.getValue(selectedNode).toString(), normal);
        }


        this.appendLine(doc, "", normal);
        this.appendLine(doc, "", normal);
        this.appendLine(doc, "SubNode Meta Model" + " (count: " + subNodes.size() + ")", boldBlue);
        this.appendLine(doc, H_LINE, boldBlue);
        for (final PropertyMetaModel subNodeMetaModel : subNodes) {
            final Node value = (Node) subNodeMetaModel.getValue(selectedNode);
            if (value != null) {
                this.appendLine(doc, " - " + subNodeMetaModel.getName() + ": " + value, normal);
            }
        }

        this.appendLine(doc, "", normal);
        this.appendLine(doc, "", normal);
        this.appendLine(doc, "SubList Meta Model" + " (count: " + subLists.size() + ")", boldBlue);
        this.appendLine(doc, H_LINE, boldBlue);
        for (int index_allSublists = 0; index_allSublists < subLists.size(); index_allSublists++) {
            final PropertyMetaModel        subListMetaModel = subLists.get(index_allSublists);
            final NodeList<? extends Node> subList          = (NodeList<? extends Node>) subListMetaModel.getValue(selectedNode);
            if (subList != null && !subList.isEmpty()) {
                this.appendLine(doc, subListMetaModel.getName() + " (count: " + subList.size() + ")", normal);
                for (int index_sublist = 0; index_sublist < subList.size(); index_sublist++) {
                    Node subListNode = subList.get(index_sublist);
                    this.appendLine(doc, index_sublist + ": " + CLASS_RANGE_SUMMARY_FORMAT.apply(subListNode), normal);
                }
            }
            if (index_allSublists < (subLists.size() - 1)) {
                this.appendLine(doc, "", normal);
            }
        }

    }

    private class TNode {
        private final Node node;


        TNode(Node node) {
            this.node = node;
        }


        public Node getNode() {
            return this.node;
        }


        public String toString() {
            return CLASS_RANGE_SUMMARY_FORMAT.apply(this.node);
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
                    this.setForeground(Color.BLUE);
                } else if (selectedNode instanceof Comment) {
                    this.setForeground(Color.GRAY);
                } else if (selectedNode instanceof LiteralExpr) {
                    this.setForeground(Color.GREEN);
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


