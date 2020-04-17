package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.extensions;

import com.github.javaparser.ast.CompilationUnit;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.service.HighlightingService;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;


public class SimpleAnnotator implements Annotator {

    private final HighlightingService hls = HighlightingService.getInstance();

    private final TextAttributesKey highlightYellow;
    private final TextAttributesKey highlightOrange;
    private final TextAttributesKey highlightGreen;


    public SimpleAnnotator() {
        super();

        TextAttributes taYellow = new TextAttributes();
        taYellow.setBackgroundColor(JBColor.YELLOW);

        TextAttributes taOrange = new TextAttributes();
        taOrange.setBackgroundColor(JBColor.ORANGE);

        TextAttributes taGreen = new TextAttributes();
        taGreen.setBackgroundColor(JBColor.GREEN);

        // Setup highlighting styles.
        this.highlightYellow = TextAttributesKey.createTextAttributesKey("javaparser_ast_inspector_node_highlighting_yellow", taYellow);
        this.highlightOrange = TextAttributesKey.createTextAttributesKey("javaparser_ast_inspector_node_highlighting_orange", taOrange);
        this.highlightGreen = TextAttributesKey.createTextAttributesKey("javaparser_ast_inspector_node_highlighting_green", taGreen);
    }


    @Override
    public void annotate(@NotNull final PsiElement element, @NotNull final AnnotationHolder holder) {

//        // Get the deepest ancestor
//        PsiElement parent = element;
//        while (parent.getParent() != null) {
//            parent = parent.getParent();
//        }

        final PsiFile psiFile = element.getContainingFile();
        String        text    = psiFile.getText();
//        System.out.println("text = " + text);

        final String canonicalPath = psiFile.getVirtualFile().getCanonicalPath();
//        System.out.println("psiFile canonicalPath = " + canonicalPath);


//        final TextRange textRange = element.getTextRange();
//
//        final int start = textRange.getStartOffset();
//        final int end   = textRange.getEndOffset();
//
//        if (start > 5 && start < 15) {
//            Annotation keyAnnotation = holder.createInfoAnnotation(textRange, "Annotation message");
//            keyAnnotation.setTooltip("tooltip text");
//            keyAnnotation.setTextAttributes(this.highlightYellow);
//        }
//
//        holder.newAnnotation(HighlightSeverity.INFORMATION, "Test error message")
//              .range(new TextRange(35, 35 + new Random().nextInt(20)))
//              .needsUpdateOnTyping(true)
//              .textAttributes(this.highlightOrange)
//              .tooltip("orange tooltip text")
//              .create();


//        holder.newAnnotation(HighlightSeverity.INFORMATION, "Test message")
//              .range(new TextRange(55, 55 + new Random().nextInt(20)))
//              .textAttributes(this.highlightGreen)
//              .tooltip("green tooltip text")
//              .create();


        this.hls.getSelectedNode().ifPresent(selectedNode -> {
            System.out.println();
            System.out.println();
            System.out.println();

//            System.out.println("selectedNode = " + selectedNode);
            System.out.println("selectedNode.getClass() = " + selectedNode.getClass());
            selectedNode.findCompilationUnit()
                        .flatMap(CompilationUnit::getStorage)
                        .ifPresent(storage -> {
                            try {
                                final String selectedNodeCanonicalPath = storage.getPath().toFile().getCanonicalPath();
                                System.out.println("selectedNodeCanonicalPath = " + selectedNodeCanonicalPath);

                                System.out.println("selectedNodeCanonicalPath.equals(canonicalPath) = " + selectedNodeCanonicalPath.equals(canonicalPath));


                                final Path nodePath = Paths.get(selectedNodeCanonicalPath).normalize();
                                final Path psiPath = Paths.get(canonicalPath).normalize();

                                System.out.println("nodePath.equals(psiPath) = " + nodePath.equals(psiPath));

                                if (nodePath.equals(psiPath)) {
                                    System.out.println("paths do match: " + "" +
                                                       "\n IJ: " + nodePath.toString() +
                                                       "\n JP: " + psiPath.toString() +
                                                       "");

                                    // We have confirmed that the file in the editor is the same as the file we have parsed
                                    // ... but now what?


                                    selectedNode.getRange().ifPresent(range -> {
                                        System.out.println("range = " + range);

                                        final TextRange textRange2 = this.hls.javaparserRangeToIntellijOffsetRange(psiFile, range);
                                        System.out.println("textRange2 = " + textRange2);

                                        holder.newAnnotation(HighlightSeverity.INFORMATION, "Test message")
                                              .range(textRange2)
                                              .textAttributes(this.highlightGreen)
                                              .tooltip("green tooltip text")
                                              .needsUpdateOnTyping(true)
                                              .create();


                                    });

                                } else {
                                    System.out.println("paths do not match: " + "" +
                                                       "\n IJ: " + nodePath.toString() +
                                                       "\n JP: " + psiPath.toString() +
                                                       "");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });

        });

    }
}
