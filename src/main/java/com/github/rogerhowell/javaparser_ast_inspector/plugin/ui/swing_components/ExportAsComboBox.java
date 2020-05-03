package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components;

import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.combo_items.StringComboItem;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.forms.CustomComboBox;

public class ExportAsComboBox extends CustomComboBox<String> {

    public ExportAsComboBox() {
        super();
        this.setToolTipText("Output format.");
        this.setupOptions();
    }


    @Override
    protected void setupOptions() {
        // Populate
        this.addItem(new StringComboItem("DOT", "DOT"));
        this.addItem(new StringComboItem("XML", "XML"));
        this.addItem(new StringComboItem("Java", "Java"));
        this.addItem(new StringComboItem("Java (pretty print)", "Java (pretty print)"));
        this.addItem(new StringComboItem("ASCII Tree", "ASCII Tree"));
        this.addItem(new StringComboItem("YAML", "YAML"));
        this.addItem(new StringComboItem("Custom DOT", "Custom DOT"));
        this.addItem(new StringComboItem("Custom DOT Image", "Custom DOT Image"));
        this.addItem(new StringComboItem("Custom JSON", "Custom JSON"));
        this.addItem(new StringComboItem("Cypher", "Cypher"));
        this.addItem(new StringComboItem("GraphML", "GraphML"));
    }
}
