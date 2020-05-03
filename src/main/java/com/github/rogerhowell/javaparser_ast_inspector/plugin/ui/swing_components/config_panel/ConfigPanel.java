package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.config_panel;

import com.github.javaparser.ParserConfiguration;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.util.FontUtil;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.util.NotificationLogger;
import com.intellij.ui.JBIntSpinner;
import com.intellij.ui.components.JBCheckBox;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.nio.charset.Charset;

public class ConfigPanel extends JPanel {

    private static final NotificationLogger notificationLogger = new NotificationLogger(ConfigPanel.class);

    private final ExportConfigPanel exportConfigPanel;
    private final ParserConfigPanel parserConfigPanel;


    public ConfigPanel() {
        this(new ParserConfiguration());
    }


    public ConfigPanel(ParserConfiguration parserConfiguration) {
        super();

        this.parserConfigPanel = new ParserConfigPanel();
        this.exportConfigPanel = new ExportConfigPanel();


        this.setLayout(new GridBagLayout());

        addToGrid(this, 0, this.parserConfigPanel);
        addToGrid(this, 1, this.exportConfigPanel);

        // Set parser defaults
        this.updateConfigUi(parserConfiguration);
    }


    public static void addToGrid(Container container, int y, JComponent... components) {
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.gridy = y;
        c.gridwidth = components.length;

        for (int i = 0; i < components.length; i++) {
            c.gridx = i;
            container.add(components[i], c);
        }

    }


    public ParserConfiguration getConfigFromForm() {
        notificationLogger.traceEnter();

        ParserConfiguration config = new ParserConfiguration();

        config.setLanguageLevel(this.parserConfigPanel.getLanguageLevel());
        config.setCharacterEncoding(this.parserConfigPanel.getCharacterEncoding());
        config.setTabSize(this.parserConfigPanel.getTabSize());
        config.setAttributeComments(this.parserConfigPanel.getAttributeComments());
        config.setStoreTokens(this.parserConfigPanel.getStoreTokens());

        return config;
    }


    public boolean getOutputNodeType() {
        return this.exportConfigPanel.getOutputNodeType();
    }


    public String getSelectedExportType() {
        return this.exportConfigPanel.getSelectedExportType();
    }


    public void updateConfigUi(ParserConfiguration parserConfiguration) {
        notificationLogger.traceEnter();
        this.parserConfigPanel.updateConfigUi(parserConfiguration);
    }


    private static class ExportConfigPanel extends JPanel {

        // Export Options
        private final ExportAsComboBox exportAsCombobox;
        private final JLabel           label_exportAs;
        private final JCheckBox        outputNodeTypeCheckBox;


        ExportConfigPanel() {
            super();

            final TitledBorder titledBorder = BorderFactory.createTitledBorder("Export Config");
            titledBorder.setTitleFont(FontUtil.TITLE);

            this.setBorder(BorderFactory.createCompoundBorder(
                    titledBorder,
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));


            // Initialise basic form elements
            this.outputNodeTypeCheckBox = new JBCheckBox("Include Node Type in Export");

            // Setup combobox and populate its options
            this.exportAsCombobox = new ExportAsComboBox();

            // Tooltips
            this.outputNodeTypeCheckBox.setToolTipText("In the exported text, should the node type be included?");

            // Set export / printer defaults
            this.exportAsCombobox.setSelectedByValue("Custom DOT");
            this.outputNodeTypeCheckBox.setSelected(true);


            this.label_exportAs = new JLabel("Export As:");
            this.label_exportAs.setLabelFor(this.exportAsCombobox);

            this.setLayout(new GridBagLayout());
            addToGrid(this, 0, this.label_exportAs, this.exportAsCombobox);
            addToGrid(this, 1, this.outputNodeTypeCheckBox);
        }


        public boolean getOutputNodeType() {
            return this.outputNodeTypeCheckBox.isSelected();
        }


        public String getSelectedExportType() {
            return this.exportAsCombobox.getSelected();
        }


        public void setOutputNodeType(boolean outputNodeType) {
            this.outputNodeTypeCheckBox.setSelected(outputNodeType);
        }


        public void setSelectedExportType(String value) {
            this.exportAsCombobox.setSelectedByValue(value);
        }


    }


    private static class ParserConfigPanel extends JPanel {

        private final JCheckBox                 attributeCommentsCheckbox;
        private final CharacterEncodingComboBox characterEncodingCombobox;
        private final JLabel                    label_CharacterEncoding;
        private final JLabel                    label_LanguageLevel;
        private final JLabel                    label_TabSize;
        private final LanguageLevelComboBox     languageLevelCombobox;
        private final JCheckBox                 storeTokensCheckbox;
        private final JSpinner                  tabSizeSpinner;


        ParserConfigPanel() {
            super();

            final TitledBorder titledBorder = BorderFactory.createTitledBorder("Parser Config");
            titledBorder.setTitleFont(FontUtil.TITLE);

            this.setBorder(BorderFactory.createCompoundBorder(
                    titledBorder,
                    BorderFactory.createEmptyBorder(5, 5, 5, 5)));


            this.attributeCommentsCheckbox = new JBCheckBox("Attribute Comments");
            this.storeTokensCheckbox = new JBCheckBox("Store Tokens");
            this.tabSizeSpinner = new JBIntSpinner(0, 0, 50, 1);

            this.languageLevelCombobox = new LanguageLevelComboBox();
            this.characterEncodingCombobox = new CharacterEncodingComboBox();


            this.attributeCommentsCheckbox.setToolTipText("When false, all comments will be orphaned.");
            this.storeTokensCheckbox.setToolTipText("");
            this.tabSizeSpinner.setToolTipText(
                    "How many characters should a tab character be considered equal to? " +
                    "\nNote that a tab character is only a single character within a string." +
                    "\nYou might opt to shift the column of the range by e.g. 4 characters for each single tab character" +
                    "\n (and this will be reflected in the node's range)," +
                    " but you must ensure that any other tools take this into account.");


            this.label_LanguageLevel = new JLabel("Language Level:");
            this.label_CharacterEncoding = new JLabel("Character Encoding:");
            this.label_TabSize = new JLabel("Tab Size:");

            this.label_LanguageLevel.setLabelFor(this.languageLevelCombobox);
            this.label_CharacterEncoding.setLabelFor(this.characterEncodingCombobox);
            this.label_TabSize.setLabelFor(this.tabSizeSpinner);

            this.setLayout(new GridBagLayout());
            addToGrid(this, 0, this.label_LanguageLevel, this.languageLevelCombobox);
            addToGrid(this, 1, this.label_CharacterEncoding, this.characterEncodingCombobox);
            addToGrid(this, 2, this.label_TabSize, this.tabSizeSpinner);
            addToGrid(this, 3, this.attributeCommentsCheckbox);
            addToGrid(this, 4, this.storeTokensCheckbox);

        }


        public boolean getAttributeComments() {
            return this.attributeCommentsCheckbox.isSelected();
        }


        public Charset getCharacterEncoding() {
            return this.characterEncodingCombobox.getSelected();
        }


        public ParserConfiguration.LanguageLevel getLanguageLevel() {
            return this.languageLevelCombobox.getSelected();
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


        public void setStoreTokens(boolean storeTokens) {
            this.storeTokensCheckbox.setSelected(storeTokens);
        }


        public void setTabSize(int tabSize) {
            this.tabSizeSpinner.setValue(tabSize);
        }


        public void updateConfigUi(ParserConfiguration parserConfiguration) {
            notificationLogger.traceEnter();

            // Comboboxes
            this.languageLevelCombobox.setSelectedByValue(parserConfiguration.getLanguageLevel());
            this.characterEncodingCombobox.setSelectedByValue(parserConfiguration.getCharacterEncoding());

            // Inputs
            this.tabSizeSpinner.setValue(parserConfiguration.getTabSize());

            // Checkboxes
            this.attributeCommentsCheckbox.setSelected(parserConfiguration.isAttributeComments());
            this.storeTokensCheckbox.setSelected(parserConfiguration.isStoreTokens());
        }

    }

}
