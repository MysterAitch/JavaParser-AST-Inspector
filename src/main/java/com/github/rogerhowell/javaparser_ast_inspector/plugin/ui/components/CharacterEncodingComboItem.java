package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.components;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;


public class CharacterEncodingComboItem {
    private final String  key;
    private final Charset value;


    public CharacterEncodingComboItem(@NotNull String key, Charset value) {
        this.key = key;
        this.value = value;
    }


    public String getKey() {
        return this.key;
    }


    public Charset getValue() {
        return this.value;
    }


    @Override
    public int hashCode() {
        return this.value.hashCode();
    }


    @Override
    public boolean equals(final Object obj) {
        if (obj == this) { return true; }
        if (!(obj instanceof CharacterEncodingComboItem)) { return false; }

        final CharacterEncodingComboItem other = (CharacterEncodingComboItem) obj;

        //
        return this.value == other.value;
    }


    @Override
    public String toString() {
        return this.key;
    }

}
