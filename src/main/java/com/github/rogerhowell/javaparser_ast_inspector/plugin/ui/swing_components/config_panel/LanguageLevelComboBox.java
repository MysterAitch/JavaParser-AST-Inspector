package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.config_panel;

import com.github.javaparser.ParserConfiguration;
import org.jetbrains.annotations.NotNull;

public class LanguageLevelComboBox extends CustomComboBox<ParserConfiguration.LanguageLevel> {

    public LanguageLevelComboBox() {
        super();
        this.setToolTipText("Which language features should be considered valid or invalid when validating the AST?");
        this.setupOptions();
    }


    protected void setupOptions() {
        // Populate
        this.addItem(new LanguageLevelComboItem("CURRENT (13)", ParserConfiguration.LanguageLevel.CURRENT));
        this.addItem(new LanguageLevelComboItem("BLEEDING EDGE (14)", ParserConfiguration.LanguageLevel.BLEEDING_EDGE));
        this.addItem(new LanguageLevelComboItem("POPULAR (8)", ParserConfiguration.LanguageLevel.POPULAR));
        this.addItem(new LanguageLevelComboItem("RAW", ParserConfiguration.LanguageLevel.RAW));
        this.addItem(new LanguageLevelComboItem("JAVA 14", ParserConfiguration.LanguageLevel.JAVA_14));
        this.addItem(new LanguageLevelComboItem("JAVA 13", ParserConfiguration.LanguageLevel.JAVA_13));
        this.addItem(new LanguageLevelComboItem("JAVA 12", ParserConfiguration.LanguageLevel.JAVA_12));
        this.addItem(new LanguageLevelComboItem("JAVA 11", ParserConfiguration.LanguageLevel.JAVA_11));
        this.addItem(new LanguageLevelComboItem("JAVA 10", ParserConfiguration.LanguageLevel.JAVA_10));
        this.addItem(new LanguageLevelComboItem("JAVA 9", ParserConfiguration.LanguageLevel.JAVA_9));
        this.addItem(new LanguageLevelComboItem("JAVA 8", ParserConfiguration.LanguageLevel.JAVA_8));
        this.addItem(new LanguageLevelComboItem("JAVA 7", ParserConfiguration.LanguageLevel.JAVA_7));
        this.addItem(new LanguageLevelComboItem("JAVA 6", ParserConfiguration.LanguageLevel.JAVA_6));
        this.addItem(new LanguageLevelComboItem("JAVA 5", ParserConfiguration.LanguageLevel.JAVA_5));
        this.addItem(new LanguageLevelComboItem("JAVA 1.4", ParserConfiguration.LanguageLevel.JAVA_1_4));
        this.addItem(new LanguageLevelComboItem("JAVA 1.3", ParserConfiguration.LanguageLevel.JAVA_1_3));
        this.addItem(new LanguageLevelComboItem("JAVA 1.2", ParserConfiguration.LanguageLevel.JAVA_1_2));
        this.addItem(new LanguageLevelComboItem("JAVA 1.1", ParserConfiguration.LanguageLevel.JAVA_1_1));
        this.addItem(new LanguageLevelComboItem("JAVA 1.0", ParserConfiguration.LanguageLevel.JAVA_1_0));
    }


    private static class LanguageLevelComboItem extends CustomComboItem<ParserConfiguration.LanguageLevel> {

        public LanguageLevelComboItem(@NotNull String key, ParserConfiguration.LanguageLevel value) {
            super(key, value);
        }

    }
}
