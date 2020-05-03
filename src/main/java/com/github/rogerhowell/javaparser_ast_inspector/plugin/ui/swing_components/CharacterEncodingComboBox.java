package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components;

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Providers;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.combo_items.CharacterEncodingComboItem;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.combo_items.LanguageLevelComboItem;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.forms.CustomComboBox;
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
}
