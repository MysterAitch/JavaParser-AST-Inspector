package com.github.rogerhowell.JavaCodeBrowser.tool_window;

import com.github.rogerhowell.JavaCodeBrowser.parsing.Parsing;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;

import javax.swing.*;
import java.util.Arrays;
import java.util.stream.Collectors;


public class CodeGraphBrowserToolWindow {

    protected Parsing parsing;

    // UI Components
    private JPanel  sourceRootsListingPanel;
    private JTextArea textArea1;
    private JButton   refreshButton;


    public CodeGraphBrowserToolWindow(final Project project, final ToolWindow toolWindow) {
        this.parsing = new Parsing();

        this.refreshButton.addActionListener(e -> this.updateProjectSources(project));

        this.updateProjectSources(project);
    }


    private void updateProjectSources(final Project project) {
        String        projectName     = project.getName();
        VirtualFile[] vFiles          = ProjectRootManager.getInstance(project).getContentSourceRoots();

//        String        sourceRootsList = Arrays.stream(vFiles).map(VirtualFile::getUrl).collect(Collectors.joining("\n"));
//        textArea1.setText(sourceRootsList);

        textArea1.setText(
                parsing.vFilesToSourceRoots(vFiles).stream()
                       .map(sourceRoot -> sourceRoot.getRoot().toString())
                       .collect(Collectors.joining("\n"))
        );
    }


    public JPanel getSourceRootsListingPanel() {
        return this.sourceRootsListingPanel;
    }


    @Override
    public String toString() {
        return "CodeGraphBrowserToolWindow{}";
    }
}
