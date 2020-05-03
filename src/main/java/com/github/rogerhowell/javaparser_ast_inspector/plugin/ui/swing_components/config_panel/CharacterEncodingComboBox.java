package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.config_panel;

import com.github.javaparser.Providers;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;

public class CharacterEncodingComboBox extends CustomComboBox<Charset> {

    public CharacterEncodingComboBox() {
        super();
        this.setToolTipText("Which language features should be considered valid or invalid when validating the AST?");
        this.setupOptions();
    }


    @Override
    protected void setupOptions() {
        // Populate
        this.addItem(new CharacterEncodingComboItem("UTF-8", Providers.UTF8));
    }


    private static class CharacterEncodingComboItem extends CustomComboItem<Charset> {

        public CharacterEncodingComboItem(@NotNull String key, @NotNull Charset value) {
            super(key, value);
        }

    }
}
