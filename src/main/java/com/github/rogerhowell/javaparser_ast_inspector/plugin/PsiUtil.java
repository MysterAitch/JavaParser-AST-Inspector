package com.github.rogerhowell.javaparser_ast_inspector.plugin;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

import java.util.Optional;

public final class PsiUtil {

    private PsiUtil() {}


    public static Optional<PsiFile> getCurrentFileInEditor(Project project) {
        FileEditorManager manager = FileEditorManager.getInstance(project);
        VirtualFile[]     files   = manager.getSelectedFiles();
        if (files.length == 0) {
            return Optional.empty();
        }

        final VirtualFile currentFile = files[0];
        final PsiFile     psiFile     = PsiManager.getInstance(project).findFile(currentFile);

        return Optional.ofNullable(psiFile);
    }


    public static Optional<String> getInputText(Project project) {
        final Optional<PsiFile> psiFile = getCurrentFileInEditor(project);
        return psiFile.map(PsiElement::getText);
    }

}
