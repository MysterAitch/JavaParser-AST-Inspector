package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.components;

import org.jetbrains.annotations.NotNull;

import static com.github.javaparser.ParserConfiguration.LanguageLevel;

public class LanguageLevelComboItem {
    private final String        key;
    private final LanguageLevel value;


    public LanguageLevelComboItem(@NotNull String key, LanguageLevel value) {
        this.key = key;
        this.value = value;
    }


    public String getKey() {
        return this.key;
    }


    public LanguageLevel getValue() {
        return this.value;
    }


    @Override
    public int hashCode() {
        return this.value.hashCode();
    }


    @Override
    public boolean equals(final Object obj) {
        if (obj == this) { return true; }
        if (!(obj instanceof LanguageLevelComboItem)) { return false; }

        final LanguageLevelComboItem other = (LanguageLevelComboItem) obj;

        //
        return this.value == other.value;
    }


    @Override
    public String toString() {
        return this.key;
    }

}
