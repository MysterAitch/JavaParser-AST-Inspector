package com.github.rogerhowell.JavaCodeBrowser.ui.tool_window;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Providers;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.DotPrinter;
import com.github.javaparser.printer.XmlPrinter;
import com.github.javaparser.printer.YamlPrinter;
import com.github.rogerhowell.JavaCodeBrowser.parsing.Parsing;
import com.github.rogerhowell.JavaCodeBrowser.printers.ASCIITreePrinter;
import com.github.rogerhowell.JavaCodeBrowser.printers.CustomDotPrinter;
import com.github.rogerhowell.JavaCodeBrowser.printers.CustomJsonPrinter;
import com.github.rogerhowell.JavaCodeBrowser.printers.CypherPrinter;
import com.github.rogerhowell.JavaCodeBrowser.printers.GraphMLPrinter;
import com.github.rogerhowell.JavaCodeBrowser.ui.components.CharacterEncodingComboItem;
import com.github.rogerhowell.JavaCodeBrowser.ui.components.LanguageLevelComboItem;
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
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.treeStructure.Tree;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.engine.Renderer;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.parse.Parser;
import org.jetbrains.annotations.SystemIndependent;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

import static com.github.javaparser.ParserConfiguration.LanguageLevel;

public class ParseSingleForm {

    private final Parsing    parsing;
    private final Project    project;
    private final ToolWindow toolWindow;

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
    private JLabel                                imageLabel;
    private ParseResult<CompilationUnit>          result;


    public ParseSingleForm(final Project project, final ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
        this.parsing = new Parsing();
        final ParserConfiguration defaultConfiguration = this.parsing.getDefaultConfiguration();

        // Setup form (e.g. combobox values)
        this.setupLanguageLevelOptions();
        this.setupCharacterEncodingOptions();
        this.setupOutputFormatCombobox();
        this.setOutputNodeType(true);

        // Set defaults/values
        this.setTabSize(defaultConfiguration.getTabSize());
        this.setAttributeComments(defaultConfiguration.isAttributeComments());
        this.setStoreTokens(defaultConfiguration.isStoreTokens());

        // Add event handlers
        this.parseButton.addActionListener(e -> this.parseButtonClickHandler());

    }


    private void createUIComponents() {
        // TODO: place custom component creation code here

        //create the root node
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root");
        //create the child nodes
        DefaultMutableTreeNode vegetableNode = new DefaultMutableTreeNode("Vegetables");
        DefaultMutableTreeNode fruitNode     = new DefaultMutableTreeNode("Fruits");
        //add the child nodes to the root node
        root.add(vegetableNode);
        root.add(fruitNode);

        this.tree1 = new Tree(root);

//        this.tree1.setModel(new DefaultTreeModel(root, false));
//        this.tree1.setSelectionPath(vegetableNode..path);
    }


    public String getOutputFormat() {
        Object item  = this.outputFormatComboBox.getSelectedItem();
        String value = (String) item;
        return value;
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


    public void setAttributeComments(boolean attributeComments) {
        this.attributeCommentsCheckbox.setSelected(attributeComments);
    }


    public boolean getAttributeComments() {
        return this.attributeCommentsCheckbox.isSelected();
    }


    public void setOutputNodeType(boolean outputNodeType) {
        this.outputNodeTypeCheckBox.setSelected(outputNodeType);
    }


    public boolean getOutputNodeType() {
        return this.outputNodeTypeCheckBox.isSelected();
    }


    public void setStoreTokens(boolean storeTokens) {
        this.storeTokensCheckbox.setSelected(storeTokens);
    }


    public boolean getStoreTokens() {
        return this.storeTokensCheckbox.isSelected();
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


    public void doParse() {
        int           tabSize       = this.getTabSize();
        Charset       charset       = this.getSelectedCharacterSet();
        LanguageLevel languageLevel = this.getSelectedLanguageLevel();

        ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration.setTabSize(tabSize);
        parserConfiguration.setCharacterEncoding(charset);
        parserConfiguration.setLanguageLevel(languageLevel);

        JavaParser javaParser = new JavaParser(parserConfiguration);
        this.result = javaParser.parse(this.getInputText());

        this.setParseResultTextPane("Result Present: " + this.result.getResult().isPresent() + "\n" + "Parse Result: " + this.result.toString());
    }


    public void outputParsedJava() {
        if (this.result.getResult().isPresent()) {
            this.setParseResult(this.result.getResult().get().toString());
        }
    }


    public void outputAsciiTreeText() {
        if (this.result.getResult().isPresent()) {
            ASCIITreePrinter printer = new ASCIITreePrinter();
            String           output  = printer.output(this.result.getResult().get());
            System.out.println(output);
            this.setParseResult(output);
        }
    }


    public void outputDot() {
        if (this.result.getResult().isPresent()) {
            DotPrinter printer = new DotPrinter(this.getOutputNodeType());
            this.setParseResult(printer.output(this.result.getResult().get()));
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
                final Optional<PsiFile> currentFile = getCurrentFile();

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


    public void outputGraphMl() {
        if (this.result.getResult().isPresent()) {
            GraphMLPrinter printer = new GraphMLPrinter(this.getOutputNodeType());
            this.setParseResult(printer.output(this.result.getResult().get()));
        }
    }


    public void outputYaml() {
        if (this.result.getResult().isPresent()) {
            YamlPrinter printer = new YamlPrinter(this.getOutputNodeType());
            this.setParseResult(printer.output(this.result.getResult().get()));
        }
    }


    public void outputXml() {
        if (this.result.getResult().isPresent()) {
            XmlPrinter printer = new XmlPrinter(this.getOutputNodeType());
            this.setParseResult(printer.output(this.result.getResult().get()));
        }
    }


    public void setParseResultTextPane(String string) {
        this.parseResultTextPane.setText(string);
    }


    public void setParseResult(String resultString) {
        this.outputTextArea.setText(resultString);
    }


    public void setTabSize(int tabSize) {
        this.tabSizeTextField.setText(String.valueOf(tabSize));
    }


    public int getTabSize() {
        return Integer.parseInt(this.tabSizeTextField.getText(), 10);
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


}
