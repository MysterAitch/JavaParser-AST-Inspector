package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.combo_items;

import org.jetbrains.annotations.NotNull;

public class StringComboItem extends CustomComboItem<String> {

    public StringComboItem(@NotNull String key, @NotNull String value) {
        super(key, value);
    }


    @Override
    public String toString() {
        return "StringComboItem{" +
               "key='" + key + '\'' +
               ", value='" + value + '\'' +
               '}';
    }

}
