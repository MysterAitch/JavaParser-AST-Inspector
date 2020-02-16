package com.github.rogerhowell.JavaCodeBrowser;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import com.github.rogerhowell.JavaCodeBrowser.parsing.Parsing;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BasicAction extends AnAction {
    /**
     * Implement this method to provide your action handler.
     *
     * @param e Carries information on the invocation place
     */
    @Override
    public void actionPerformed(@NotNull final AnActionEvent e) {
        final Project       project         = e.getProject();
        final String        projectName     = project.getName();
        final VirtualFile[] vFiles          = ProjectRootManager.getInstance(project).getContentSourceRoots();
        final String        sourceRootsList = Arrays.stream(vFiles).map(VirtualFile::getUrl).collect(Collectors.joining("\n"));


        final Parsing          parsing = new Parsing();
        final List<SourceRoot> roots   = parsing.vFilesToSourceRoots(vFiles);

        final StringBuilder sb = new StringBuilder(1000);

        for (int i = 0; i < roots.size(); i++) {
            final SourceRoot sourceRoot = roots.get(i);
            try {
                List<ParseResult<CompilationUnit>> parseResults = sourceRoot.tryToParse();

                sb.append("\n");
                sb.append("\n");
                sb.append(" === source root #").append(i).append(": ").append(sourceRoot.getRoot().toString()).append(" === ");
                sb.append("\n");
                for (int j = 0; j < parseResults.size(); j++) {
                    ParseResult<CompilationUnit> parseResult = parseResults.get(j);
                    sb.append("    ");
                    sb.append(i + "." + j + " - " + parseResult.isSuccessful());
                    sb.append(": ");
                    sb.append(sourceRoot.getRoot().toString());
                    sb.append("\n");
                }

            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }

        Messages.showMessageDialog(e.getProject(), sb.toString(), "Test Title", Messages.getInformationIcon());

    }
}
