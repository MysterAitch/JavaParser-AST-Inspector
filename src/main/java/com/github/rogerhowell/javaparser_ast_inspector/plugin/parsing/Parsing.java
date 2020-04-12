package com.github.rogerhowell.javaparser_ast_inspector.plugin.parsing;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParseStart;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.Provider;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import com.intellij.openapi.vfs.VirtualFile;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Parsing {

    JavaParser          javaParser;
    ParserConfiguration configuration;


    public Parsing() {
        this(new ParserConfiguration());
    }


    public Parsing(ParserConfiguration configuration) {
        this.configuration = configuration;
        this.javaParser = new JavaParser(this.configuration);
    }


    public JavaParser getJavaParser() {
        return this.javaParser;
    }


    public ParserConfiguration getConfiguration() {
        return this.configuration;
    }


    public ParserConfiguration getDefaultConfiguration() {
        return new ParserConfiguration();
    }


    public ParseResult<CompilationUnit> parseCu(Provider provider) {
        return this.javaParser.parse(ParseStart.COMPILATION_UNIT, provider);
    }


    public List<SourceRoot> vFilesToSourceRoots(VirtualFile[] vFiles) {
        return Arrays.stream(vFiles)
                     .map(VirtualFile::getPath)
                     .map(Paths::get)
                     .map(SourceRoot::new)
                     .collect(Collectors.toList());
    }


    public String vFilesToSourceRoots(VirtualFile[] vFiles, String delimiter) {
        return this.vFilesToSourceRoots(vFiles).stream()
                   .map(sourceRoot -> sourceRoot.getRoot().toString())
                   .collect(Collectors.joining(delimiter));
    }


}
