package com.github.rogerhowell.javaparser_ast_inspector.plugin.printers;

import com.github.javaparser.ast.Node;

public interface NodePrinter {


    /**
     * @param node The node to be printed - typically a CompilationUnit.
     * @return The formatted equivalent of node.
     */
    String output(Node node);

}
