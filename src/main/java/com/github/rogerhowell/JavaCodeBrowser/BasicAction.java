package com.github.rogerhowell.JavaCodeBrowser;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Collectors;

public class BasicAction extends AnAction {
    /**
     * Implement this method to provide your action handler.
     *
     * @param e Carries information on the invocation place
     */
    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
        final Project project         = e.getProject();
        String        projectName     = project.getName();
        VirtualFile[] vFiles          = ProjectRootManager.getInstance(project).getContentSourceRoots();
        String        sourceRootsList = Arrays.stream(vFiles).map(VirtualFile::getUrl).collect(Collectors.joining("\n"));

        Messages.showInfoMessage(
                "Source roots for the " + projectName + " plugin:\n" + sourceRootsList,
                "Project Properties"
        );
    }
}
