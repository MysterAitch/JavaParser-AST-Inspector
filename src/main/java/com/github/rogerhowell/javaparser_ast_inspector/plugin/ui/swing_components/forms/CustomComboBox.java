package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.forms;

import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.combo_items.CustomComboItem;
import com.intellij.openapi.ui.ComboBox;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CustomComboBox<T> extends ComboBox<CustomComboItem<T>> {

//
//    public <I extends CustomComboItem<T>> void setSelectedValue(JComboBox<I> comboBox, T value) {
//        I item;
//        for (int i = 0; i < comboBox.getItemCount(); i++) {
//            item = comboBox.getItemAt(i);
//            if (item.getValue().equals(value)) {
//                comboBox.setSelectedIndex(i);
//                break;
//            }
//        }
//    }


    public static <E, I extends CustomComboItem<E>> void setSelectedValue(JComboBox<I> comboBox, E value) {
        I item;
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            item = comboBox.getItemAt(i);
            if (item.getValue().equals(value)) {
                comboBox.setSelectedIndex(i);
                break;
            }
        }
    }


    public void setSelectedByValue(@NotNull T value) {
        setSelectedValue(this, value);
    }


    public T getSelected() {
        Object itemObject  = this.getSelectedItem();
        final CustomComboItem<T> item = (CustomComboItem<T>) itemObject;

        return item.getValue();
    }


}
