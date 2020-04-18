package com.github.rogerhowell.javaparser_ast_inspector.plugin.service;

import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;

import java.util.Optional;

public interface HighlightingService {
    static HighlightingService getInstance() {
        return ServiceManager.getService(HighlightingService.class);
    }

    Optional<Node> getSelectedNode();

    void setSelectedNode(Node node);

    TextRange javaparserRangeToIntellijOffsetRange(PsiFile psiFile, Range range);

}
