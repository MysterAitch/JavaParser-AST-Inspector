package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.config_panel;

import org.jetbrains.annotations.NotNull;

public class ExportAsComboBox extends CustomComboBox<String> {

    public ExportAsComboBox() {
        super();
        this.setToolTipText("Output format.");
        this.setupOptions();
    }


    @Override
    protected void setupOptions() {
        // Populate
        this.addItem(new ExportAsComboItem("DOT", "DOT"));
        this.addItem(new ExportAsComboItem("XML", "XML"));
        this.addItem(new ExportAsComboItem("Java", "Java"));
        this.addItem(new ExportAsComboItem("Java (pretty print)", "Java (pretty print)"));
        this.addItem(new ExportAsComboItem("ASCII Tree", "ASCII Tree"));
        this.addItem(new ExportAsComboItem("YAML", "YAML"));
        this.addItem(new ExportAsComboItem("Custom DOT", "Custom DOT"));
        this.addItem(new ExportAsComboItem("Custom DOT Image", "Custom DOT Image"));
        this.addItem(new ExportAsComboItem("Custom JSON", "Custom JSON"));
        this.addItem(new ExportAsComboItem("Cypher", "Cypher"));
        this.addItem(new ExportAsComboItem("GraphML", "GraphML"));
    }


    private static class ExportAsComboItem extends CustomComboItem<String> {
    
        public ExportAsComboItem(@NotNull String key, @NotNull String value) {
            super(key, value);
        }
    
    }
}
