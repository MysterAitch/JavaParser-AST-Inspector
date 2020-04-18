package com.github.rogerhowell.javaparser_ast_inspector.plugin.services.impl;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParseStart;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Provider;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.services.JavaParserService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JavaParserServiceImpl implements JavaParserService {

    private final Project             project;
    private final JavaParser          javaParser;
    private final ParserConfiguration configuration;


    public JavaParserServiceImpl(Project project) {
        this.project = project;
        this.configuration = this.getDefaultConfiguration();
        this.javaParser = new JavaParser(this.configuration);
    }


    @Override
    public ParserConfiguration getConfiguration() {
        return this.configuration;
    }


    @Override
    public ParserConfiguration getDefaultConfiguration() {
        return new ParserConfiguration();
    }


    @Override
    public JavaParser getJavaParserInstance() {
        return this.javaParser;
    }


    @Override
    public ParseResult<CompilationUnit> parseCu(Provider provider) {
        return this.javaParser.parse(ParseStart.COMPILATION_UNIT, provider);
    }


    @Override
    public List<SourceRoot> vFilesToSourceRoots(VirtualFile[] vFiles) {
        return Arrays.stream(vFiles)
                     .map(VirtualFile::getPath)
                     .map(Paths::get)
                     .map(SourceRoot::new)
                     .collect(Collectors.toList());
    }


    @Override
    public String vFilesToSourceRoots(VirtualFile[] vFiles, String delimiter) {
        return this.vFilesToSourceRoots(vFiles).stream()
                   .map(sourceRoot -> sourceRoot.getRoot().toString())
                   .collect(Collectors.joining(delimiter));
    }

}
