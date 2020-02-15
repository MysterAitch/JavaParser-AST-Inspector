package com.github.rogerhowell.JavaCodeBrowser;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

public class BasicAction extends AnAction {
    /**
     * Implement this method to provide your action handler.
     *
     * @param e Carries information on the invocation place
     */
    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
        Messages.showMessageDialog(e.getProject(), "Test message", "Test Title", Messages.getInformationIcon());
    }
}
