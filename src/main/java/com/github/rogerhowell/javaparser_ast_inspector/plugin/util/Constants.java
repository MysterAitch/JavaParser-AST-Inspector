package com.github.rogerhowell.javaparser_ast_inspector.plugin.util;

import java.io.File;

public final class Constants {

    public static final String PLUGIN_NAME        = "JavaParser AST Inspector";
    public static final String TOOL_WINDOW_ID     = "JavaParser AST Inspector"; // see plugin.xml
    public static final String DEFAULT_EXPORT_DIR = System.getProperty("user.home") + File.separatorChar + TOOL_WINDOW_ID;

    public static final String URL_GITHUB_PLUGIN     = "https://github.com/MysterAitch/JavaParser-AST-Inspector";
    public static final String URL_GITHUB_JAVAPARSER = "https://github.com/JavaParser/JavaParser";
    public static final String URL_WEBSITE_JP        = "http://javaparser.org/";


    private Constants() {
        // Empty private constructor, to prevent instantiation.
    }

}
