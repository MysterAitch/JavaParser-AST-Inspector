package com.github.rogerhowell.javaparser_ast_inspector.plugin.services;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Provider;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface JavaParserService {

    static JavaParserService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, JavaParserService.class);
    }


    ParserConfiguration getConfiguration();

    ParserConfiguration getDefaultConfiguration();

    JavaParser getJavaParserInstance();

    ParseResult<CompilationUnit> parseCu(Provider provider);

    List<SourceRoot> vFilesToSourceRoots(VirtualFile[] vFiles);

    String vFilesToSourceRoots(VirtualFile[] vFiles, String delimiter);

}
