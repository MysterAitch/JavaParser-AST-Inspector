package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.config_panel;

import com.intellij.openapi.ui.ComboBox;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Objects;

public abstract class CustomComboBox<T> extends ComboBox<CustomComboBox.CustomComboItem<T>> {

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


    public T getSelected() {
        Object                   itemObject = this.getSelectedItem();
        final CustomComboItem<T> item       = (CustomComboItem<T>) itemObject;

        return item.getValue();
    }


    public void setSelectedByValue(@NotNull T value) {
        setSelectedValue(this, value);
    }


    protected abstract void setupOptions();


    protected static class CustomComboItem<E> {

        @NotNull
        protected final String key;

        protected final E value;


        public CustomComboItem(@NotNull String key, E value) {
            this.key = key;
            this.value = value;
        }


        @Override
        public boolean equals(final Object obj) {
            if (obj == this) { return true; }
            if (!(obj instanceof CustomComboBox.CustomComboItem)) { return false; }

            final CustomComboItem<?> other = (CustomComboItem<?>) obj;
            return Objects.equals(this.value, other.value);
        }


        @NotNull
        public String getKey() {
            return this.key;
        }


        public E getValue() {
            return this.value;
        }


        @Override
        public int hashCode() {
            return Objects.hash(this.value);
        }


        //    @Override
        //    public String toString() {
        //        return "CustomComboItem{" +
        //               "key='" + key + '\'' +
        //               ", value='" + String.valueOf(value) + '\'' +
        //               '}';
        //    }


        /**
         * Note that this is used as the text on the combo item.
         */
        @Override
        public String toString() {
            return this.key;
        }
    }
}
