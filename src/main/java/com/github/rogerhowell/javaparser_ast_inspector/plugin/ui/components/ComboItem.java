package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.components;

public class ComboItem {
    private String key;
    private String value;


    public ComboItem(String key, String value) {
        this.key = key;
        this.value = value;
    }


    public String getKey() {
        return this.key;
    }


    public String getValue() {
        return this.value;
    }


    @Override
    public String toString() {
        return this.key;
    }

}
