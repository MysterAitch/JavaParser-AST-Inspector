package com.github.rogerhowell.javaparser_ast_inspector.plugin.services.impl;

import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.services.HighlightingService;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.util.EditorUtil;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.util.NotificationLogger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.MarkupModel;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HighlightingServiceImpl implements HighlightingService {

    private static final int HIGHLIGHT_LAYER = HighlighterLayer.ERROR + 200;

    private static final NotificationLogger notificationLogger = new NotificationLogger(HighlightingServiceImpl.class);

    private final Map<Editor, RangeHighlighter> highlighters;

    private final TextAttributes taGreen;
    private final TextAttributes taOrange;
    private final TextAttributes taSelectedNodeInEditor;
    private final TextAttributes taYellow;

    private Node selectedNode = null;


    public HighlightingServiceImpl() {

        this.highlighters = new HashMap<>();

        // Setup colours
        this.taSelectedNodeInEditor = new TextAttributes();
        this.taSelectedNodeInEditor.setBackgroundColor(JBColor.YELLOW);
        this.taSelectedNodeInEditor.withAdditionalEffect(EffectType.BOXED, JBColor.RED);

        this.taYellow = new TextAttributes();
        this.taYellow.setBackgroundColor(JBColor.YELLOW);
        this.taYellow.withAdditionalEffect(EffectType.BOXED, JBColor.RED);

        this.taOrange = new TextAttributes();
        this.taOrange.setBackgroundColor(JBColor.ORANGE);
        this.taOrange.withAdditionalEffect(EffectType.BOXED, JBColor.RED);

        this.taGreen = new TextAttributes();
        this.taGreen.setBackgroundColor(JBColor.GREEN);
        this.taGreen.withAdditionalEffect(EffectType.BOXED, JBColor.RED);

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


    @Override
    public void updateHighlight(PsiFile psiFile, Editor editor) {
        notificationLogger.traceEnter();

        if (this.selectedNode != null) {
            if (this.selectedNode.getRange().isPresent()) {
                final Range range = this.selectedNode.getRange().get();

                final MarkupModel markupModel = editor.getMarkupModel();

                // Remove all current highlighters for this editor
                if (this.highlighters.containsKey(editor)) {
                    RangeHighlighter highlighter = this.highlighters.get(editor);

                    List<RangeHighlighter> allHighlighters = Arrays.asList(markupModel.getAllHighlighters());
                    if (allHighlighters.contains(highlighter)) {
                        markupModel.removeHighlighter(highlighter);
                    }
                }

                // Create a new highlighter.
                TextRange textRange = EditorUtil.javaParserRangeToIntellijOffsetRange(editor, range);
                final RangeHighlighter newHighlighter = markupModel.addRangeHighlighter(
                        textRange.getStartOffset(),
                        textRange.getEndOffset(),
                        HIGHLIGHT_LAYER,
                        this.taSelectedNodeInEditor,
                        HighlighterTargetArea.EXACT_RANGE
                );

                // Add to per-editor cache of highlighters.
                this.highlighters.put(editor, newHighlighter);

                // Scroll to the start of the highlighted range.
                EditorUtil.scrollToPosition(editor, newHighlighter.getStartOffset());

            } else {
                notificationLogger.warn("Selected node does not have a range, thus unable to update highlighting.");
            }
        }
    }

}
