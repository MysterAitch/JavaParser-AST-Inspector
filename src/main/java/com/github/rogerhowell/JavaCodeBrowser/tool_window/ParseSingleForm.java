package com.github.rogerhowell.JavaCodeBrowser.tool_window;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Providers;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.printer.DotPrinter;
import com.github.javaparser.printer.XmlPrinter;
import com.github.javaparser.printer.YamlPrinter;
import com.github.rogerhowell.JavaCodeBrowser.CharacterEncodingComboItem;
import com.github.rogerhowell.JavaCodeBrowser.LanguageLevelComboItem;
import com.github.rogerhowell.JavaCodeBrowser.parsing.Parsing;
import com.github.rogerhowell.JavaCodeBrowser.printers.CustomDotPrinter;
import com.github.rogerhowell.JavaCodeBrowser.printers.CustomJsonPrinter;
import com.github.rogerhowell.JavaCodeBrowser.printers.CypherPrinter;
import com.github.rogerhowell.JavaCodeBrowser.printers.GraphMLPrinter;

import javax.swing.*;
import java.nio.charset.Charset;

import static com.github.javaparser.ParserConfiguration.LanguageLevel;

public class ParseSingleForm {

    final private Parsing parsing;

    private JPanel                                panel1;
    private JCheckBox                             attributeCommentsCheckbox;
    private JComboBox<LanguageLevelComboItem>     languageLevelComboBox;
    private JComboBox<CharacterEncodingComboItem> characterEncodingComboBox;
    private JButton                               parseButton;
    private JTextArea                             inputTextarea;
    private JTextField                            tabSizeTextField;
    private JCheckBox                             storeTokensCheckbox;
    private JTextArea                             outputTextArea;
    private JTextPane    parseResultTextPane;
    private JComboBox<String>    outputFormatComboBox;
    private ParseResult<CompilationUnit> result;


    public ParseSingleForm() {
        this.parsing = new Parsing();
        final ParserConfiguration defaultConfiguration = this.parsing.getDefaultConfiguration();

        // Setup form (e.g. combobox values)
        this.setupLanguageLevelOptions();
        this.setupCharacterEncodingOptions();
        this.setupOutputFormatCombobox();

        // Set defaults/values
        this.setTabSize(defaultConfiguration.getTabSize());
        this.setAttributeComments(defaultConfiguration.isAttributeComments());
        this.setStoreTokens(defaultConfiguration.isStoreTokens());

        // Add event handlers
        this.parseButton.addActionListener(e -> this.parseButtonClickHandler());

    }

    public String getOutputFormat() {
        Object  item  = this.outputFormatComboBox.getSelectedItem();
        String value = (String) item;
        return value;
    }

    public void setupOutputFormatCombobox() {
        this.outputFormatComboBox.addItem("DOT");
        this.outputFormatComboBox.addItem("XML");
        this.outputFormatComboBox.addItem("Java");
        this.outputFormatComboBox.addItem("YAML");
        this.outputFormatComboBox.addItem("Custom DOT");
        this.outputFormatComboBox.addItem("Custom JSON");
        this.outputFormatComboBox.addItem("Cypher");
        this.outputFormatComboBox.addItem("GraphML");
    }

    private void parseButtonClickHandler() {
        this.doParse();

        if("YAML".equals(this.getOutputFormat())) {
            this.outputYaml();
        } else if("XML".equals(this.getOutputFormat())) {
            this.outputXml();
        } else if("DOT".equals(this.getOutputFormat())) {
            this.outputDot();
        } else if("Java".equals(this.getOutputFormat())) {
            this.outputParsedJava();
        } else if("Custom DOT".equals(this.getOutputFormat())) {
            this.outputCustomDot();
        } else if("Custom JSON".equals(this.getOutputFormat())) {
            this.outputCustomJson();
        } else if("Cypher".equals(this.getOutputFormat())) {
            this.outputCypher();
        } else if("GraphML".equals(this.getOutputFormat())) {
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


    public void setStoreTokens(boolean storeTokens) {
        this.storeTokensCheckbox.setSelected(storeTokens);
    }


    public boolean getStoreTokens() {
        return this.storeTokensCheckbox.isSelected();
    }


    public String getInputText() {
        return this.inputTextarea.getText();
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
    public void outputDot() {
        if (this.result.getResult().isPresent()) {
            DotPrinter printer = new DotPrinter(true);
            this.setParseResult(printer.output(this.result.getResult().get()));
        }
    }
    public void outputCustomDot() {
        if (this.result.getResult().isPresent()) {
            CustomDotPrinter printer = new CustomDotPrinter(true);
            this.setParseResult(printer.output(this.result.getResult().get()));
        }
    }
    public void outputCustomJson() {
        if (this.result.getResult().isPresent()) {
            CustomJsonPrinter printer = new CustomJsonPrinter(true);
            this.setParseResult(printer.output(this.result.getResult().get()));
        }
    }
    public void outputCypher() {
        if (this.result.getResult().isPresent()) {
            CypherPrinter printer = new CypherPrinter(true);
            this.setParseResult(printer.output(this.result.getResult().get()));
        }
    }
    public void outputGraphMl() {
        if (this.result.getResult().isPresent()) {
            GraphMLPrinter printer = new GraphMLPrinter(true);
            this.setParseResult(printer.output(this.result.getResult().get()));
        }
    }
    public void outputYaml() {
        if (this.result.getResult().isPresent()) {
            YamlPrinter printer = new YamlPrinter(true);
            this.setParseResult(printer.output(this.result.getResult().get()));
        }
    }
    public void outputXml() {
        if (this.result.getResult().isPresent()) {
            XmlPrinter printer = new XmlPrinter(true);
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
