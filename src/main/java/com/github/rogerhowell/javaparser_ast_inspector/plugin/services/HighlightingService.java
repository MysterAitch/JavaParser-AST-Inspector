package com.github.rogerhowell.javaparser_ast_inspector.plugin.services;

import com.github.javaparser.ast.Node;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiFile;

import java.util.Optional;

public interface HighlightingService {
    static HighlightingService getInstance() {
        return ServiceManager.getService(HighlightingService.class);
    }

    Optional<Node> getSelectedNode();

    void setSelectedNode(Node node);

    void updateHighlight(PsiFile psiFile, Editor editor);

}
