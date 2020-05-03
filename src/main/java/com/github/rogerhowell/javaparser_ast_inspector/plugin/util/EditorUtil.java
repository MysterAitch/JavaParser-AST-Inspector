package com.github.rogerhowell.javaparser_ast_inspector.plugin.util;

import com.github.javaparser.Range;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.util.TextRange;

public final class EditorUtil {

    private EditorUtil() {
        // Empty private constructor, to prevent instantiation.
    }


    public static TextRange javaParserRangeToIntellijOffsetRange(final Editor editor, final Range range) {
        // Note that the JavaParser Range has 1-indexed lines, while the IntelliJ LogicalPosition is 0-indexed.
        final LogicalPosition startPosition = new LogicalPosition(range.begin.line - 1, range.begin.column - 1); // start highlighting just before the given character/column
        final LogicalPosition endPosition   = new LogicalPosition(range.end.line - 1, range.end.column); // end highlighting at the end of the given character/column

        final int startOffset = editor.logicalPositionToOffset(startPosition);
        final int endOffset   = editor.logicalPositionToOffset(endPosition);

        return new TextRange(startOffset, endOffset);
    }


    public static void scrollToPosition(Editor editor, int offset) {
        // Scroll to position
        editor.getCaretModel().moveToOffset(offset);
        editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
    }

}
