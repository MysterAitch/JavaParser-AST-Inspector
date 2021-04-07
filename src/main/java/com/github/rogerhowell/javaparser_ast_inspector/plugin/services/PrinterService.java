package com.github.rogerhowell.javaparser_ast_inspector.plugin.services;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface PrinterService {

    static PrinterService getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, PrinterService.class);
    }


    String asAsciiTreeText(Node node);

    String asAsciiTreeText(Node node, boolean outputNodeType);

    String asCypher(Node node);

    String asCypher(Node node, boolean outputNodeType);


    String asDot(Node node);

    String asDot(Node node, boolean outputNodeType);


    String asDotCustom(Node node);

    String asDotCustom(Node node, boolean outputNodeType);


    String asGraphMl(Node node);

    String asGraphMl(Node node, boolean outputNodeType);


    String asJavaPrettyPrint(Node node);


    String asJsonCustom(Node node);

    String asJsonCustom(Node node, boolean outputNodeType);


    String asXml(Node node, boolean outputNodeType);

    String asXml(Node node);


    String asYaml(Node node, boolean outputNodeType);

    String asYaml(Node node);


    default String outputAs(String outputFormat, CompilationUnit compilationUnit) {
        return this.outputAs(outputFormat, compilationUnit, false);
    }

    String outputAs(String outputFormat, CompilationUnit compilationUnit, boolean includeNodeType);

}
