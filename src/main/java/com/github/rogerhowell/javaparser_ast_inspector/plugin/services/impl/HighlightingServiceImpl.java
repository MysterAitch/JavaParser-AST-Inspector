package com.github.rogerhowell.javaparser_ast_inspector.plugin.services.impl;

import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.services.HighlightingService;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;

import java.util.Optional;

public class HighlightingServiceImpl implements HighlightingService {

    private static final Logger LOGGER = Logger.getInstance(HighlightingServiceImpl.class.getName());

    private final TextAttributes taYellow;
    private final TextAttributes taOrange;
    private final TextAttributes taGreen;

    private Node             selectedNode = null;
    private RangeHighlighter highlighter;


    public HighlightingServiceImpl() {

        // Setup colours
        this.taYellow = new TextAttributes();
        this.taYellow.setBackgroundColor(JBColor.YELLOW);

        this.taOrange = new TextAttributes();
        this.taOrange.setBackgroundColor(JBColor.ORANGE);

        this.taGreen = new TextAttributes();
        this.taGreen.setBackgroundColor(JBColor.GREEN);

    }


    /**
     * Which character indicates the the last character of the line, at which point the line number increments?
     *
     * Examples:
     * <ul>
     *   <li>{@code \r} (CR) - Characters after the {@code \r} are considered to be on the next line.</li>
     *   <li>{@code \n} (LF) - Characters after the {@code \n} are considered to be on the next line.</li>
     *   <li>{@code \r\n} (CRLF) - Characters after the {@code \n} are considered to be on the next line.</li>
     * </ul>
     */
    public static char lastLineSeparator(String lineSeparatorString) {
        char defaultSeparator = '\n';
        char lineSeparatorChar;

        if (lineSeparatorString == null) {
            lineSeparatorChar = defaultSeparator;
        } else if (lineSeparatorString.length() == 1) {
            lineSeparatorChar = lineSeparatorString.toCharArray()[0];
        } else if (lineSeparatorString.length() == 2) {
            lineSeparatorChar = lineSeparatorString.toCharArray()[1];
        } else {
            lineSeparatorChar = defaultSeparator;
        }

        return lineSeparatorChar;
    }


    @Override
    public Optional<Node> getSelectedNode() {
        return Optional.ofNullable(this.selectedNode);
    }


    @Override
    public void setSelectedNode(Node node) {
        this.selectedNode = node;
    }


    public void updateHighlight(PsiFile psiFile, Editor editor) {
        LOGGER.trace("public void updateHighlight(PsiFile psiFile, Editor editor) {");
        if (this.selectedNode != null) {
            TextRange textRange = javaparserRangeToIntellijOffsetRange(psiFile, this.selectedNode.getRange().get());
            LOGGER.trace("textRange = " + textRange);

//            // TODO: Investigate using the document / offsets
//            Document document = editor.getDocument();
//
//            int lineNumber  = 10;
//            int startOffset = document.getLineStartOffset(lineNumber);
//            int endOffset   = document.getLineEndOffset(lineNumber);

            int layer = HighlighterLayer.ERROR + 200;

            final MarkupModel markupModel = editor.getMarkupModel();
            if (this.highlighter != null) {
                markupModel.removeHighlighter(this.highlighter);
            }
            this.highlighter = markupModel.addRangeHighlighter(
                    textRange.getStartOffset(),
                    textRange.getEndOffset(),
                    layer,
                    this.taYellow,
                    HighlighterTargetArea.EXACT_RANGE
            );
        }
    }


    @Override
    public TextRange javaparserRangeToIntellijOffsetRange(final PsiFile psiFile, final Range range) {
        // Note: The JavaParser {@code Range} uses a pair of {@code Position}s, which is one-indexed line and column numbers.
        // This means the first character of a file is at (1,1).

        int startOffset = 1;
        int endOffset   = 1;

        int currentLine = 1;
        int currentCol  = 0; // Start before the line, so that moving to the "next" character moves to the first.

        final String lineSeparatorString = psiFile.getVirtualFile().getDetectedLineSeparator();
        final char   lineSeparatorChar   = lastLineSeparator(lineSeparatorString);

        final String text  = psiFile.getText();
        final char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            final char currentChar = chars[i];
            if (currentChar == lineSeparatorChar) {
                // Reached the start of the next line
                currentLine++;
                currentCol = 0;
            } else {
                // Continuing along the same line
                currentCol++;
            }

            // If we've reached either the desired begin/end line/col combination, set the relevant offset.
            if (currentLine == range.begin.line && currentCol == range.begin.column) {
                startOffset = i;
            }
            if (currentLine == range.end.line && currentCol == range.end.column) {
                endOffset = i;
            }
        }

        return new TextRange(startOffset, endOffset + 1);
    }

}
