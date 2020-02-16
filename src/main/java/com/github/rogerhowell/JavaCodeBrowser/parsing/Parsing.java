package com.github.rogerhowell.JavaCodeBrowser.parsing;

import com.github.javaparser.utils.SourceRoot;
import com.intellij.openapi.vfs.VirtualFile;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Parsing {

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
