package com.github.rogerhowell.javaparser_ast_inspector.plugin;

import com.intellij.openapi.components.PersistentStateComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Settings implements PersistentStateComponent<SettingState> {

    SettingState pluginState = new SettingState();


    @Nullable
    @Override
    public SettingState getState() {
        return this.pluginState;
    }


    @Override
    public void loadState(@NotNull final SettingState state) {
        this.pluginState = state;
    }


}
