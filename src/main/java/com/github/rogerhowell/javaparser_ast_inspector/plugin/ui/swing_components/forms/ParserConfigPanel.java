package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.forms;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Providers;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.services.JavaParserService;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.combo_items.CharacterEncodingComboItem;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.combo_items.LanguageLevelComboItem;
import com.intellij.notification.NotificationDisplayType;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.swing.*;
import java.awt.*;
import java.nio.charset.Charset;
import java.util.Optional;

public class ParserConfigPanel extends JPanel implements DumbAwareForm {

    private static final Logger LOGGER = Logger.getInstance(ParserConfigPanel.class.getName());


    public static final NotificationGroup GROUP_DISPLAY_ID_INFO = new NotificationGroup(
            "ParserConfigPanel group",
            NotificationDisplayType.STICKY_BALLOON,
            true
    );


    private final Project    project;
    private final ToolWindow toolWindow;

    //
    private JCheckBox                            attributeCommentsCheckbox;
    private ComboBox<String>                     outputFormatComboBox;
    private ComboBox<LanguageLevelComboItem>     languageLevelComboBox;
    private ComboBox<CharacterEncodingComboItem> characterEncodingComboBox;
    private JTextField                           tabSizeTextField;
    private JCheckBox                            storeTokensCheckbox;
    private JCheckBox                            outputNodeTypeCheckBox;
    private JPanel                               root;


    public ParserConfigPanel(final Project project, final ToolWindow toolWindow) {
        super();

        this.project = project;
        this.toolWindow = toolWindow;

        if (this.project == null) {
            LOGGER.warn("Skipping setup of services until project is defined.");
//            GROUP_DISPLAY_ID_INFO.createNotification(
//                    "WARNING",
//                    "",
//                    "Skipping setup of services until project is defined.",
//                    NotificationType.INFORMATION
//            ).notify(null);
        }

    }


    private void createUIComponents() {

        // Initialise form elements
        this.outputFormatComboBox = new ComboBox<>();
        this.languageLevelComboBox = new ComboBox<>();
        this.characterEncodingComboBox = new ComboBox<>();

        this.attributeCommentsCheckbox = new JBCheckBox();
        this.outputNodeTypeCheckBox = new JBCheckBox();
        this.storeTokensCheckbox = new JBCheckBox();

        this.tabSizeTextField = new JBTextField();

        // Setup defaults/values for the form (e.g. combobox values)
        this.setupLanguageLevelOptions();
        this.setupCharacterEncodingOptions();
        this.setupOutputFormatCombobox();
        this.setOutputNodeType(true);


        // Update the form to reflect defaults/values for the JavaParser config
        final ParserConfiguration config = JavaParserService.getInstance(this.project).getConfiguration();
        this.setTabSize(config.getTabSize());
        this.setAttributeComments(config.isAttributeComments());
        this.setStoreTokens(config.isStoreTokens());
    }


    public boolean getAttributeComments() {
        return this.attributeCommentsCheckbox.isSelected();
    }


    @Override
    public Optional<JPanel> getMainPanel() {
        return Optional.of(this);
    }


    public void setAttributeComments(boolean attributeComments) {
        this.attributeCommentsCheckbox.setSelected(attributeComments);
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


    public Charset getSelectedCharacterSet() {
        Object  item  = this.characterEncodingComboBox.getSelectedItem();
        Charset value = ((CharacterEncodingComboItem) item).getValue();
        return value;
    }


    public ParserConfiguration.LanguageLevel getSelectedLanguageLevel() {
        Object                            item  = this.languageLevelComboBox.getSelectedItem();
        ParserConfiguration.LanguageLevel value = ((LanguageLevelComboItem) item).getValue();
        return value;
    }


    private void setSelectedLanguageLevel(ParserConfiguration.LanguageLevel languageLevel) {
        this.languageLevelComboBox.setSelectedItem(ParserConfiguration.LanguageLevel.CURRENT);
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


    private void setupCharacterEncodingOptions() {
        this.characterEncodingComboBox.addItem(new CharacterEncodingComboItem("UTF-8", Providers.UTF8));
    }


    private void setupLanguageLevelOptions() {
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("CURRENT (13)", ParserConfiguration.LanguageLevel.CURRENT));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("BLEEDING EDGE (14)", ParserConfiguration.LanguageLevel.BLEEDING_EDGE));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("POPULAR (8)", ParserConfiguration.LanguageLevel.POPULAR));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("RAW", ParserConfiguration.LanguageLevel.RAW));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 14", ParserConfiguration.LanguageLevel.JAVA_14));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 13", ParserConfiguration.LanguageLevel.JAVA_13));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 12", ParserConfiguration.LanguageLevel.JAVA_12));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 11", ParserConfiguration.LanguageLevel.JAVA_11));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 10", ParserConfiguration.LanguageLevel.JAVA_10));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 9", ParserConfiguration.LanguageLevel.JAVA_9));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 8", ParserConfiguration.LanguageLevel.JAVA_8));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 7", ParserConfiguration.LanguageLevel.JAVA_7));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 6", ParserConfiguration.LanguageLevel.JAVA_6));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 5", ParserConfiguration.LanguageLevel.JAVA_5));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 1.4", ParserConfiguration.LanguageLevel.JAVA_1_4));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 1.3", ParserConfiguration.LanguageLevel.JAVA_1_3));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 1.2", ParserConfiguration.LanguageLevel.JAVA_1_2));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 1.1", ParserConfiguration.LanguageLevel.JAVA_1_1));
        this.languageLevelComboBox.addItem(new LanguageLevelComboItem("JAVA 1.0", ParserConfiguration.LanguageLevel.JAVA_1_0));
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


}
