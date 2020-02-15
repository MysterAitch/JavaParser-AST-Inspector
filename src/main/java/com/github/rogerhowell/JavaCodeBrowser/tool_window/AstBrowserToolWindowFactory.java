package com.github.rogerhowell.JavaCodeBrowser.tool_window;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;

public class AstBrowserToolWindowFactory implements com.intellij.openapi.wm.ToolWindowFactory {
    @Override
    public void createToolWindowContent(@NotNull final Project project, @NotNull final ToolWindow toolWindow) {

        CodeGraphBrowserToolWindow codeGraphBrowserToolWindow = new CodeGraphBrowserToolWindow(toolWindow);

    }
}
