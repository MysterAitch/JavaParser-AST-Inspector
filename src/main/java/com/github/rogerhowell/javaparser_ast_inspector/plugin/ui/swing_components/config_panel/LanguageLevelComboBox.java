package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.config_panel;

import com.github.javaparser.ParserConfiguration;
import org.jetbrains.annotations.NotNull;

public class LanguageLevelComboBox extends CustomComboBox<ParserConfiguration.LanguageLevel> {

    public LanguageLevelComboBox() {
        super();
        this.setToolTipText("Which language features should be considered valid or invalid when validating the AST?");
        this.setupOptions();
    }


    @Override
    protected void setupOptions() {
//        this.addItem(new LanguageLevelComboItem("CURRENT (" + ParserConfiguration.LanguageLevel.CURRENT.name() + ")", ParserConfiguration.LanguageLevel.CURRENT));
//        this.addItem(new LanguageLevelComboItem("BLEEDING EDGE (" + ParserConfiguration.LanguageLevel.BLEEDING_EDGE.name() + ")", ParserConfiguration.LanguageLevel.BLEEDING_EDGE));
//        this.addItem(new LanguageLevelComboItem("POPULAR (" + ParserConfiguration.LanguageLevel.POPULAR.name() + ")", ParserConfiguration.LanguageLevel.POPULAR));

        // The "RAW" language level doesn't perform any validations (e.g. checking if 'yield' is permitted as an identifier).
        this.addItem(new LanguageLevelComboItem("RAW", ParserConfiguration.LanguageLevel.RAW));

        // List all available language levels (in descending order - recent to older)
        ParserConfiguration.LanguageLevel[] languageLevels = ParserConfiguration.LanguageLevel.values();
        for (int i = languageLevels.length - 1; i >= 0; i--) {
            this.addItem(new LanguageLevelComboItem(languageLevels[i].name(), languageLevels[i]));
        }

    }


    private static class LanguageLevelComboItem extends CustomComboItem<ParserConfiguration.LanguageLevel> {

        public LanguageLevelComboItem(@NotNull String key, ParserConfiguration.LanguageLevel value) {
            super(key, value);
        }

    }
}
