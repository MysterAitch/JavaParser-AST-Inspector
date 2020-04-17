package com.github.rogerhowell.javaparser_ast_inspector.plugin.service.impl;

import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.service.HighlightingService;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;

import java.util.Optional;

public class HighlightingServiceImpl implements HighlightingService {

    private Node selectedNode;


    @Override
    public void setSelectedNode(Node node) {
        this.selectedNode = node;
    }


    @Override
    public Optional<Node> getSelectedNode() {
        return Optional.of(this.selectedNode);
    }


    @Override
    public TextRange textRangeFromLineCol(final PsiFile psiFile, final int line, final int col) {
        return null;
    }




    @Override
    public TextRange javaparserRangeToIntellijOffsetRange(final PsiFile psiFile, final Range range) {
        int startOffset = 1;
        int endOffset = 1;

        int currentLine = 1;
        int currentCol = 0;

        // TODO: Use the line separator in some way when determining the current line...
        final String lineSeparator = psiFile.getVirtualFile().getDetectedLineSeparator();

        final String text = psiFile.getText();
        final char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            final char currentChar = chars[i];
            if(currentChar == '\n') { // FIXME: Use either the detected line separator or something else...
                currentLine++;
                currentCol = 0;
            } else {
                currentCol++;
            }

            if(currentLine == range.begin.line && currentCol == range.begin.column) {
                startOffset = i;
            }
            if(currentLine == range.end.line && currentCol == range.end.column) {
                endOffset = i;
            }
        }

        return new TextRange(startOffset, endOffset + 1);
    }

}
