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

    private final ParserConfigPanel parserConfigPanel;
    private final ExportConfigPanel exportConfigPanel;


    public ConfigPanel() {
        this(new ParserConfiguration());
    }


    public ConfigPanel(ParserConfiguration parserConfiguration) {
        super();

        this.parserConfigPanel = new ParserConfigPanel();
        this.exportConfigPanel = new ExportConfigPanel();


        this.setLayout(new GridBagLayout());
        GridBagConstraints c;

        //
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridx = 0;
        this.add(this.parserConfigPanel, c);


        //
        c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridx = 0;
        this.add(this.exportConfigPanel, c);


        // Set parser defaults
        this.updateConfigUi(parserConfiguration);
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


    public String getSelectedExportType() {
        return exportConfigPanel.getSelectedExportType();
    }


    public boolean getOutputNodeType() {
        return exportConfigPanel.getOutputNodeType();
    }


    public void updateConfigUi(ParserConfiguration parserConfiguration) {
        notificationLogger.traceEnter();
        this.parserConfigPanel.updateConfigUi(parserConfiguration);
    }


    private static class ExportConfigPanel extends JPanel {

        // Export Options
        private final ExportAsComboBox exportAsCombobox;
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


            this.setLayout(new GridBagLayout());
            GridBagConstraints c;
            //
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.WEST;
            c.gridy = 0;
            c.gridwidth = 1;
            c.gridx = 0;
            this.add(new JLabel("Export As:"), c);
            c.gridx = 1;
            this.add(this.exportAsCombobox, c);

            //
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.WEST;
            c.gridy = 1;
            c.gridwidth = 2;
            c.gridx = 0;
            this.add(this.outputNodeTypeCheckBox, c);

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

        // Parser Options
        private final LanguageLevelComboBox     languageLevelCombobox;
        private final CharacterEncodingComboBox characterEncodingCombobox;
        private final JCheckBox                 attributeCommentsCheckbox;
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


            this.setLayout(new GridBagLayout());
            GridBagConstraints c;
            //
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.WEST;
            c.gridy = 1;
            c.gridwidth = 2;
            c.gridx = 0;
            this.add(new JLabel("Language Level:"), c);
            c.gridx = 1;
            this.add(this.languageLevelCombobox, c);

            //
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.WEST;
            c.gridy = 2;
            c.gridwidth = 2;
            c.gridx = 0;
            this.add(new JLabel("Character Encoding:"), c);
            c.gridx = 1;
            this.add(this.characterEncodingCombobox, c);

            //
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.WEST;
            c.gridy = 3;
            c.gridwidth = 2;
            c.gridx = 0;
            this.add(new JLabel("Tab Size:"), c);
            c.gridx = 1;
            this.add(this.tabSizeSpinner, c);

            //
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.WEST;
            c.gridy = 4;
            c.gridwidth = 1;
            c.gridx = 0;
            this.add(this.attributeCommentsCheckbox, c);

            //
            c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.anchor = GridBagConstraints.WEST;
            c.gridy = 5;
            c.gridwidth = 2;
            c.gridx = 0;
            this.add(this.storeTokensCheckbox, c);
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
