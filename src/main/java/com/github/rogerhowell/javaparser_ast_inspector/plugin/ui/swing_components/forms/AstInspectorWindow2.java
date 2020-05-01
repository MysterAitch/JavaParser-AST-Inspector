package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.forms;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Providers;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.NodeDetailsTextPane;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.combo_items.CharacterEncodingComboItem;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.combo_items.CustomComboItem;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.combo_items.LanguageLevelComboItem;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.combo_items.StringComboItem;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.components.JBCheckBox;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Optional;

public class AstInspectorWindow2 implements DumbAwareForm {

    private static final Logger LOGGER = Logger.getInstance(AstInspectorWindow2.class.getName());

    private static final String URL_GITHUB_PLUGIN     = "https://github.com/MysterAitch/JavaParser-AST-Inspector";
    private static final String URL_GITHUB_JAVAPARSER = "https://github.com/JavaParser/JavaParser";
    private static final String URL_WEBSITE_JP        = "http://javaparser.org/";

    @NotNull
    private final Project project;

    @NotNull
    private final ToolWindow toolWindow;

    private ParserConfiguration parserConfiguration;

    // Form Elements
    private JPanel mainPanel;

    private JComboBox<StringComboItem>            exportAsCombobox;
    private JComboBox<CharacterEncodingComboItem> characterEncodingCombobox;
    private JComboBox<LanguageLevelComboItem>     languageLevelCombobox;

    private JCheckBox  attributeCommentsCheckbox;
    private JCheckBox  storeTokensCheckbox;
    private JCheckBox  outputNodeTypeCheckBox;
    private JTree      tree1;
    private JSpinner   tabSizeSpinner;
    private JButton    gitHubButton;
    private JButton    javaParserButton;
    private JButton    resetButton;
    private JButton             parseButton;
    private NodeDetailsTextPane nodeDetailsTextPane;


    public AstInspectorWindow2(@NotNull final Project project, @NotNull final ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
        this.parserConfiguration = new ParserConfiguration();
    }


    private static void browseToUrl(@NotNull final String url) {
        LOGGER.info("BUTTON CLICK: URL=" + url);
        try {
            java.awt.Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException ioException) {
            ioException.printStackTrace();
            LOGGER.warn(ioException.getMessage());
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


    private JButton buttonWithIcon(@NotNull final String resourcePath) {
        final JButton jButton = new JButton();

        final Icon icon = IconLoader.getIcon(resourcePath);
        jButton.setIcon(icon);

        return jButton;
    }


    private void createUIComponents() {
        LOGGER.info("TRACE: private void createUIComponents() {");

//        this.mainPanel = new JPanel();
//
        // Initialise form elements
        this.languageLevelCombobox = new ComboBox<>();
        this.characterEncodingCombobox = new ComboBox<>();

        this.attributeCommentsCheckbox = new JBCheckBox();
        this.storeTokensCheckbox = new JBCheckBox();
        this.tabSizeSpinner = new JBIntSpinner(0, 0, 50, 1);

        this.exportAsCombobox = new ComboBox<>();
        this.outputNodeTypeCheckBox = new JBCheckBox();


        // Setup combobox options for the form (e.g. combobox values)
        this.setupLanguageLevelOptions();
        this.setupCharacterEncodingOptions();
        this.setupExportAsCombobox();

        //
        this.nodeDetailsTextPane = new NodeDetailsTextPane();


        // Set parser defaults
        this.updateConfigUi(this.getParserConfiguration());

        // Set default export / printer options
        this.setExportAs("Custom DOT");
        this.setOutputNodeType(true);


        // Button
        this.gitHubButton = new JButton();
        this.gitHubButton.addActionListener(e -> browseToUrl(URL_GITHUB_PLUGIN));

        // Button
        this.javaParserButton = this.buttonWithIcon("/logos/jp-logo_13x13.png");
        this.javaParserButton.addActionListener(e -> browseToUrl(URL_WEBSITE_JP));
    }


    public boolean getAttributeComments() {
        return this.attributeCommentsCheckbox.isSelected();
    }


    @Override
    public Optional<JPanel> getMainPanel() {
        return Optional.ofNullable(this.mainPanel);
    }


    public String getOutputFormat() {
        Object item  = this.exportAsCombobox.getSelectedItem();
        String value = (String) item;
        return value;
    }


    public boolean getOutputNodeType() {
        return this.outputNodeTypeCheckBox.isSelected();
    }


    public ParserConfiguration getParserConfiguration() {
        if (this.parserConfiguration == null) {
            this.setParserConfiguration(new ParserConfiguration());
        }

        return this.parserConfiguration;
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


    public void setParserConfiguration(@NotNull final ParserConfiguration parserConfiguration) {
        this.parserConfiguration = parserConfiguration;
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


    private void setupCharacterEncodingOptions() {
        this.characterEncodingCombobox.addItem(new CharacterEncodingComboItem("UTF-8", Providers.UTF8));
    }


    public void setupExportAsCombobox() {
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


    private void setupLanguageLevelOptions() {
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


    private void updateConfigUi(ParserConfiguration parserConfiguration) {
        this.parserConfiguration = parserConfiguration;

        // Comboboxes
        this.setSelectedLanguageLevel(this.parserConfiguration.getLanguageLevel());
        this.setCharacterEncoding(this.parserConfiguration.getCharacterEncoding());

        // Inputs
        this.setTabSize(this.parserConfiguration.getTabSize());

        // Checkboxes
        this.attributeCommentsCheckbox.setSelected(this.parserConfiguration.isAttributeComments());
        this.storeTokensCheckbox.setSelected(this.parserConfiguration.isStoreTokens());
    }

}
