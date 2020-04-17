package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.extensions;

import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.editor.colors.TextAttributesKey;

import java.awt.*;
import java.awt.font.TextAttribute;


public class SimpleAnnotator implements Annotator {

    private final TextAttributesKey highlightKey;

    public SimpleAnnotator() {
        super();

        // Setup highlighting styles.
        this.highlightKey = TextAttributesKey.createTextAttributesKey("javaparser_ast_inspector_node_highlighting");
        this.highlightKey.getDefaultAttributes()
                         .setBackgroundColor(JBColor.YELLOW);
    }


    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull final AnnotationHolder holder) {

        final TextRange textRange = element.getTextRange();

        final int start = textRange.getStartOffset();
        final int end   = textRange.getEndOffset();

        if (start > 10 && start < 15) {
            Annotation keyAnnotation = holder.createInfoAnnotation(textRange, "Annotation message");
//            keyAnnotation.setHighlightType(ProblemHighlightType.ERROR);
            keyAnnotation.setTooltip("tooltip text");
            keyAnnotation.setTextAttributes(this.highlightKey);

//            keyAnnotation.setHighlightType();
//            keyAnnotation.setTextAttributes(DefaultLanguageHighlighterColors.KEYWORD);
        }
    }
}
