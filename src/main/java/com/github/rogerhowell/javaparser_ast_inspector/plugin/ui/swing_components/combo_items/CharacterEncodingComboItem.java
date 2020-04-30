package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.combo_items;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;


public class CharacterEncodingComboItem extends CustomComboItem<Charset> {

    public CharacterEncodingComboItem(@NotNull String key, @NotNull Charset value) {
        super(key, value);
    }


    @Override
    public String toString() {
        return "CharacterEncodingComboItem{" +
               "key='" + key + '\'' +
               ", value=" + value +
               '}';
    }

}
