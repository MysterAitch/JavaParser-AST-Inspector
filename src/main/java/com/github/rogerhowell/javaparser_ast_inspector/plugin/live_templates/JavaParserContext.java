package com.github.rogerhowell.javaparser_ast_inspector.plugin.live_templates;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.intellij.codeInsight.template.TemplateActionContext;
import com.intellij.codeInsight.template.TemplateContextType;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiImportStatementBase;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiJavaFile;
import org.jetbrains.annotations.NotNull;

public class JavaParserContext extends TemplateContextType {

    protected JavaParserContext() {
        super("JAVAPARSER", "JavaParser");
    }


    @Override
    public boolean isInContext(@NotNull TemplateActionContext templateActionContext) {
        PsiFile psiFile = templateActionContext.getFile();

        if (!psiFile.getName().endsWith(".java")) {
            // Only consider Java files
//            System.err.println("JAVAPARSER (live template context) -- Not a file ending in .java");
            return false;
        }

        if (!(psiFile.getContainingFile() instanceof PsiJavaFile)) {
            // Only consider Java files
//            System.err.println("JAVAPARSER (live template context) -- Not a PsiJavaFile");
            return false;
        }

//        System.out.println("JAVAPARSER (live template context) -- valid context found");

        return true;

//        /* THE FILTERING BELOW WORKS, BUT I'M DISABLING */
//        PsiJavaFile psiJavaFile = (PsiJavaFile) psiFile;
//        for (final PsiImportStatementBase importStatement : psiJavaFile.getImportList().getAllImportStatements()) {
//            PsiJavaCodeReferenceElement importReference = importStatement.getImportReference();
//
//            boolean isInJavaParserNamespace = importReference.getQualifiedName().startsWith("com.github.javaparser.");
//            if (isInJavaParserNamespace) {
//                return true;
//            }
//        }
//
//        // If no matches, assume none of them are JavaParser, thus we are not in context.
//        return false;
    }

}