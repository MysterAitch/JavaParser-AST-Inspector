package com.github.rogerhowell.javaparser_ast_inspector.plugin.util;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

public final class PsiUtil {

    private PsiUtil() {}


    public static Optional<PsiFile> getCurrentFileInEditor(@NotNull Project project) {
        FileEditorManager manager = FileEditorManager.getInstance(project);
        VirtualFile[]     files   = manager.getSelectedFiles();
        if (files.length == 0) {
            return Optional.empty();
        }

        final VirtualFile currentFile = files[0];
        final PsiFile     psiFile     = PsiManager.getInstance(project).findFile(currentFile);

        return Optional.ofNullable(psiFile);
    }


    public static Optional<String> getInputText(@NotNull Project project) {
        final Optional<PsiFile> psiFile = getCurrentFileInEditor(project);
        return psiFile.map(PsiElement::getText);
    }


    public static Path pathForPsi(@NotNull PsiFile psiFile) {
        Objects.requireNonNull(psiFile);

        final VirtualFile virtualFile   = Objects.requireNonNull(psiFile.getVirtualFile());
        final String      canonicalPath = Objects.requireNonNull(virtualFile.getCanonicalPath());

        return Paths.get(canonicalPath);
    }

}
