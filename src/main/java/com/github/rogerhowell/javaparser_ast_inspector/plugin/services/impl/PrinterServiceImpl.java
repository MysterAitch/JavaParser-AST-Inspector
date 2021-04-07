package com.github.rogerhowell.javaparser_ast_inspector.plugin.services.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.printer.DotPrinter;
import com.github.javaparser.printer.XmlPrinter;
import com.github.javaparser.printer.YamlPrinter;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.logging.NotificationLogger;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.printers.ASCIITreePrinter;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.printers.CustomDotPrinter;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.printers.CustomJsonPrinter;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.printers.CypherPrinter;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.printers.GraphMLPrinter;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.services.PrinterService;
import com.intellij.openapi.project.Project;

public class PrinterServiceImpl implements PrinterService {

    private static final boolean defaultOutputNodeType = true;

    private static final NotificationLogger notificationLogger = new NotificationLogger(PrinterServiceImpl.class);
    private final        Project            project;


    public PrinterServiceImpl(Project project) {
        this.project = project;
    }


    @Override
    public String asAsciiTreeText(Node node) {
        return this.asAsciiTreeText(node, defaultOutputNodeType);
    }


    @Override
    public String asAsciiTreeText(Node node, boolean outputNodeType) {
        ASCIITreePrinter printer = new ASCIITreePrinter();
        return printer.output(node);
    }


    @Override
    public String asCypher(Node node) {
        return this.asCypher(node, defaultOutputNodeType);
    }


    @Override
    public String asCypher(Node node, boolean outputNodeType) {
        CypherPrinter printer = new CypherPrinter(outputNodeType);
        return printer.output(node);
    }


    @Override
    public String asDot(Node node) {
        return this.asDot(node, defaultOutputNodeType);
    }


    @Override
    public String asDot(Node node, boolean outputNodeType) {
        DotPrinter printer = new DotPrinter(outputNodeType);
        return printer.output(node);
    }


    @Override
    public String asDotCustom(Node node) {
        return this.asDotCustom(node, defaultOutputNodeType);
    }


    @Override
    public String asDotCustom(Node node, boolean outputNodeType) {
        CustomDotPrinter printer = new CustomDotPrinter(outputNodeType);
        return printer.output(node);
    }


    @Override
    public String asGraphMl(Node node) {
        return this.asGraphMl(node, defaultOutputNodeType);
    }


    @Override
    public String asGraphMl(Node node, boolean outputNodeType) {
        GraphMLPrinter printer = new GraphMLPrinter(outputNodeType);
        return printer.output(node);
    }


    @Override
    public String asJavaPrettyPrint(Node node) {
        return node.toString();
    }


    @Override
    public String asJsonCustom(Node node) {
        return this.asJsonCustom(node, defaultOutputNodeType);
    }


    @Override
    public String asJsonCustom(Node node, boolean outputNodeType) {
        CustomJsonPrinter printer = new CustomJsonPrinter(outputNodeType);
        return printer.output(node);
    }


    @Override
    public String asXml(Node node, boolean outputNodeType) {
        XmlPrinter printer = new XmlPrinter(outputNodeType);
        return printer.output(node);
    }


    @Override
    public String asXml(Node node) {
        return this.asXml(node, defaultOutputNodeType);
    }


    @Override
    public String asYaml(Node node, boolean outputNodeType) {
        YamlPrinter printer = new YamlPrinter(outputNodeType);
        return printer.output(node);
    }


    @Override
    public String asYaml(Node node) {
        return this.asYaml(node, defaultOutputNodeType);
    }


    @Override
    public String outputAs(final String outputFormat, final CompilationUnit compilationUnit, boolean includeNodeType) {

        String output = null;

        if ("YAML".equals(outputFormat)) {
            output = this.asYaml(compilationUnit, includeNodeType);
        } else if ("XML".equals(outputFormat)) {
            output = this.asXml(compilationUnit, includeNodeType);
        } else if ("DOT".equals(outputFormat)) {
            output = this.asDot(compilationUnit, includeNodeType);
//        } else if ("Java (lexically preserving)".equals(outputFormat)) {
//            notificationLogger.info("Note that the lexically preserving printer does not use the setting 'include node type'. ");
//            output = this.asJavaPrettyPrint(compilationUnit);
        } else if ("Java (pretty print)".equals(outputFormat)) {
            notificationLogger.info("Note that the pretty printer does not use the setting 'include node type'. ");
            output = this.asJavaPrettyPrint(compilationUnit);
        } else if ("ASCII Tree".equals(outputFormat)) {
            output = this.asAsciiTreeText(compilationUnit, includeNodeType);
        } else if ("Custom DOT".equals(outputFormat)) {
            output = this.asDotCustom(compilationUnit, includeNodeType);
        } else if ("Custom DOT Image".equals(outputFormat)) {
            output = this.asDotCustom(compilationUnit, includeNodeType);
        } else if ("Custom JSON".equals(outputFormat)) {
            output = this.asJsonCustom(compilationUnit, includeNodeType);
        } else if ("Cypher".equals(outputFormat)) {
            output = this.asCypher(compilationUnit, includeNodeType);
        } else if ("GraphML".equals(outputFormat)) {
            output = this.asGraphMl(compilationUnit, includeNodeType);
        } else {
            notificationLogger.error("Unrecognised output format: " + outputFormat);
        }

        return output;
    }

}
