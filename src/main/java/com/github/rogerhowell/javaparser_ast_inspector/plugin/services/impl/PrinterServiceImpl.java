package com.github.rogerhowell.javaparser_ast_inspector.plugin.services.impl;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.printer.DotPrinter;
import com.github.javaparser.printer.XmlPrinter;
import com.github.javaparser.printer.YamlPrinter;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.printers.ASCIITreePrinter;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.printers.CustomJsonPrinter;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.printers.CypherPrinter;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.printers.GraphMLPrinter;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.services.PrinterService;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.util.NotificationLogger;
import com.intellij.openapi.project.Project;

public class PrinterServiceImpl implements PrinterService {

    private static final boolean defaultOutputNodeType = true;

    private static final NotificationLogger notificationLogger = new NotificationLogger(PrinterServiceImpl.class);


    public PrinterServiceImpl(Project project) {
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
        DotPrinter printer = new DotPrinter(outputNodeType);
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
        return this.asJavaPrettyPrint(node, defaultOutputNodeType);
    }


    @Override
    public String asJavaPrettyPrint(Node node, boolean outputNodeType) {
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
    public String outputAs(final String outputFormat, final CompilationUnit compilationUnit) {

        String output = null;

        if ("YAML".equals(outputFormat)) {
            output = this.asYaml(compilationUnit);
        } else if ("XML".equals(outputFormat)) {
            output = this.asXml(compilationUnit);
        } else if ("DOT".equals(outputFormat)) {
            output = this.asDot(compilationUnit);
//        } else if ("Java (lexically preserving)".equals(outputFormat)) {
//            output = this.asJavaPrettyPrint(compilationUnit);
        } else if ("Java (pretty print)".equals(outputFormat)) {
            output = this.asJavaPrettyPrint(compilationUnit);
        } else if ("ASCII Tree".equals(outputFormat)) {
            output = this.asAsciiTreeText(compilationUnit);
        } else if ("Custom DOT".equals(outputFormat)) {
            output = this.asDotCustom(compilationUnit);
        } else if ("Custom DOT Image".equals(outputFormat)) {
            output = this.asDotCustom(compilationUnit);
        } else if ("Custom JSON".equals(outputFormat)) {
            output = this.asJsonCustom(compilationUnit);
        } else if ("Cypher".equals(outputFormat)) {
            output = this.asCypher(compilationUnit);
        } else if ("GraphML".equals(outputFormat)) {
            output = this.asGraphMl(compilationUnit);
        } else {
            notificationLogger.error("Unrecognised output format: " + outputFormat);
        }

        return output;
    }

}
