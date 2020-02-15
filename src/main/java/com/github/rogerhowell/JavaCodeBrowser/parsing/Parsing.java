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

}
