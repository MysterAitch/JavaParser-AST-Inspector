package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.extensions;


import com.github.javaparser.ast.CompilationUnit;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.services.HighlightingService;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiFile;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


public class JavaParserExternalAnnotator extends ExternalAnnotator<PsiFile, List<Object>> {

    private static final Logger LOGGER = Logger.getInstance(JavaParserExternalAnnotator.class.getName());

    private final HighlightingService hls = HighlightingService.getInstance();

    private final TextAttributesKey highlightYellow;
    private final TextAttributesKey highlightOrange;
    private final TextAttributesKey highlightGreen;


    public JavaParserExternalAnnotator() {
        super();
        LOGGER.trace("TRACE: public SimpleExternalAnnotator() {");


        // Setup colours
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


    /**
     * Called first; return file
     */
    @Override
    @Nullable
    public PsiFile collectInformation(@NotNull PsiFile file) {
        LOGGER.trace("TRACE: collectInformation(@NotNull PsiFile file) file = " + file);
        return file;
    }


    /**
     * Called 2nd; run on file
     */
    @Nullable
    @Override
    public List<Object> doAnnotate(final PsiFile file) {
        LOGGER.trace("TRACE: doAnnotate(final PsiFile file) file = " + file);
        return new ArrayList<>();
    }


    /**
     * Called 3rd
     */
    @Override
    public void apply(@NotNull PsiFile file, List<Object> infos, @NotNull AnnotationHolder holder) {
        LOGGER.trace("TRACE: public void apply");

        final String canonicalPath = file.getVirtualFile().getCanonicalPath();
        final Path   psiPath       = Paths.get(canonicalPath).normalize();

        String x = "File contents appear to have changed since parsing with JavaParser - highlighted areas may not line up correctly until it is re-parsed.";

        this.hls.getSelectedNode().ifPresent(selectedNode -> {
            selectedNode.findCompilationUnit()
                        .flatMap(CompilationUnit::getStorage)
                        .ifPresent(storage -> {
                            try {
                                final String selectedNodeCanonicalPath = storage.getPath().toFile().getCanonicalPath();

                                final Path nodePath = Paths.get(selectedNodeCanonicalPath).normalize();
                                if (nodePath.equals(psiPath)) {

                                    // check if the file contents have changed - if so, add a file annotation
                                    boolean isDirty = true; // TODO (#14): Determine this dynamically.
                                    if (isDirty) {
                                        holder.newAnnotation(HighlightSeverity.WARNING, x)
                                              .fileLevel()
                                              .create();
                                    }

                                    // We have confirmed that the file in the editor is the same as the file we have parsed
                                    selectedNode.getRange().ifPresent(range -> {
                                        final TextRange textRange = this.hls.javaparserRangeToIntellijOffsetRange(file, range);
                                        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
                                              .range(textRange)
                                              .textAttributes(this.highlightGreen)
                                              .needsUpdateOnTyping(true)
                                              .create();
                                    });


                                } else {
                                    LOGGER.warn("paths do not match: " +
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
