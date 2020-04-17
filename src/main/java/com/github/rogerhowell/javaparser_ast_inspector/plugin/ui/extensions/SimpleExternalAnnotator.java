package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.extensions;


import com.intellij.diagnostic.DefaultIdeaErrorLogger;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.diagnostic.DefaultLogger;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiAnchor;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;


public class SimpleExternalAnnotator extends ExternalAnnotator<PsiFile, List<SimpleExternalAnnotator.AnnotationInfo>> {

    private final TextAttributesKey highlightKey;


    public SimpleExternalAnnotator() {
        super();

        // Setup highlighting styles.
        this.highlightKey = TextAttributesKey.createTextAttributesKey("javaparser_ast_inspector_node_highlighting_highlightKey");
        this.highlightKey.getDefaultAttributes()
                         .setBackgroundColor(JBColor.YELLOW);

    }


    /**
     * Called first; return file
     */
    @Override
    @Nullable
    public PsiFile collectInformation(@NotNull PsiFile file) {
        System.out.println("collectInformation(@NotNull PsiFile file) file = " + file);
        return file;
    }


    /**
     * Called 2nd; run antlr on file
     */
    @Nullable
    @Override
    public List<AnnotationInfo> doAnnotate(final PsiFile file) {
        System.out.println("doAnnotate(final PsiFile file) file = " + file);
        final AnnotationInfo annotationInfo = new AnnotationInfo((PsiAnchor) file, new TextRange(15, 30));

        final ArrayList<AnnotationInfo> annotationInfos = new ArrayList<>();
        annotationInfos.add(annotationInfo);
        return annotationInfos;
    }


    /**
     * Called 3rd
     */
    @Override
    public void apply(@NotNull PsiFile file, List<AnnotationInfo> infos, @NotNull AnnotationHolder holder) {

        Logger x = new DefaultLogger("category ?? ");
        x.info("DefaultLogger x = new DefaultLogger(\"category ?? \");");

        System.out.println("apply(@NotNull PsiFile file, List<AnnotationInfo> infos, @NotNull AnnotationHolder holder) file = " + file);
        System.out.println("apply(@NotNull PsiFile file, List<AnnotationInfo> infos, @NotNull AnnotationHolder holder) infos = " + infos);
        System.out.println("apply(@NotNull PsiFile file, List<AnnotationInfo> infos, @NotNull AnnotationHolder holder) holder = " + holder);

        holder.newAnnotation(HighlightSeverity.ERROR, "Test error message")
              .range(new TextRange(0, 15))
              .needsUpdateOnTyping(true)
              .textAttributes(this.highlightKey)
              .create();

        holder.newAnnotation(HighlightSeverity.WARNING, "Test message")
              .range(new TextRange(15, 30))
              .textAttributes(this.highlightKey)
              .create();

    }

    protected static class AnnotationInfo {
        final PsiAnchor myAnchor;
        final TextRange myRangeInElement;

        volatile boolean myResult;


        private AnnotationInfo(PsiAnchor anchor, TextRange rangeInElement) {
            this.myAnchor = anchor;
            this.myRangeInElement = rangeInElement;
        }
    }

}
