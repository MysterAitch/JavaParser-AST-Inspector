package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.combo_items;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class CustomComboItem<E> {

    @NotNull
    protected final String key;

    protected final E value;


    public CustomComboItem(@NotNull String key, E value) {
        this.key = key;
        this.value = value;
    }


    @NotNull
    public String getKey() {
        return this.key;
    }


    @NotNull
    public E getValue() {
        return this.value;
    }


    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }


    @Override
    public boolean equals(final Object obj) {
        if (obj == this) { return true; }
        if (!(obj instanceof CustomComboItem)) { return false; }

        final CustomComboItem other = (CustomComboItem) obj;
        return Objects.equals(this.value, other.value);
    }


//    @Override
//    public String toString() {
//        return "CustomComboItem{" +
//               "key='" + key + '\'' +
//               ", value='" + String.valueOf(value) + '\'' +
//               '}';
//    }

    @Override
    public String toString() {
        return this.key;
    }
}
