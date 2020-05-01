package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.combo_items;

import org.jetbrains.annotations.NotNull;

import static com.github.javaparser.ParserConfiguration.LanguageLevel;

public class LanguageLevelComboItem extends CustomComboItem<LanguageLevel> {

    public LanguageLevelComboItem(@NotNull String key, LanguageLevel value) {
        super(key, value);
    }

}
