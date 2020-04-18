package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.components;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.metamodel.NodeMetaModel;
import com.github.javaparser.metamodel.PropertyMetaModel;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.printers.ASCIITreePrinter;
import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class NodeDetailsTextPane extends JTextPane {

    private static final String EOL    = System.lineSeparator();
    private static final String H_LINE = "----------------------------------------";

    private SimpleAttributeSet styleNormal;
    private SimpleAttributeSet styleBoldBlue;
    private SimpleAttributeSet styleHighAlert;


    public NodeDetailsTextPane() {
        super();
        this.setupStyles();
    }


    public NodeDetailsTextPane(StyledDocument doc) {
        super(doc);
        this.setupStyles();
    }


    public void addLineSeparator() {
        this.appendLine(H_LINE, this.styleBoldBlue);
    }


    public void appendHeading(String s) {
        this.appendString(s + EOL, this.styleBoldBlue);
    }


    public void appendLine(String s) {
        this.appendString(s + EOL, this.styleNormal);
    }


    public void appendLine(String s, SimpleAttributeSet style) {
        this.appendString(s + EOL, style);
    }


    public void appendString(String s) {
        this.appendString(s, this.styleNormal);
    }


    public void appendString(String s, SimpleAttributeSet style) {
        try {
            final StyledDocument doc = this.getStyledDocument();
            doc.insertString(doc.getLength(), s, style);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }


    public void clear() {
        this.setText("");
    }


    @Override
    public StyledDocument getStyledDocument() {
        return (StyledDocument) this.getDocument();
    }


    public void logNodeToTextPane(Node selectedNode) {

        // Update the side panel
        final NodeMetaModel           metaModel             = selectedNode.getMetaModel();
        final List<PropertyMetaModel> allPropertyMetaModels = metaModel.getAllPropertyMetaModels();
        final List<PropertyMetaModel> attributes            = allPropertyMetaModels.stream().filter(PropertyMetaModel::isAttribute).filter(PropertyMetaModel::isSingular).collect(toList());
        final List<PropertyMetaModel> subNodes              = allPropertyMetaModels.stream().filter(PropertyMetaModel::isNode).filter(PropertyMetaModel::isSingular).collect(toList());
        final List<PropertyMetaModel> subLists              = allPropertyMetaModels.stream().filter(PropertyMetaModel::isNodeList).collect(toList());


        this.appendHeading("DETAILS ");

        this.addLineSeparator();
        this.appendLine(" - TYPE: " + metaModel.getTypeName());
        this.appendString(" - RANGE: ");
        if (selectedNode.getRange().isPresent()) {
            this.appendLine(selectedNode.getRange().get().toString());
        } else {
            this.appendLine("[NOT PRESENT]");
        }
        this.appendLine(" - NODE SUMMARY: " + ASCIITreePrinter.printNodeSummary(selectedNode));


        // Object creation
        if ("ObjectCreationExpr".equals(selectedNode.getClass().getSimpleName())) {
            this.appendHeading("");
            this.appendHeading("");
            this.appendHeading("ObjectCreationExpr");
            this.addLineSeparator();

            final ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr) selectedNode;
            this.appendLine(" - _typeNameString:" + objectCreationExpr.getType().getName().asString());
        }


        this.appendLine("");
        this.appendLine("");
        this.appendHeading("ATTRIBUTES ");
        this.addLineSeparator();
        for (final PropertyMetaModel attributeMetaModel : attributes) {
            this.appendLine(" - " + attributeMetaModel.getName() + ":" + attributeMetaModel.getValue(selectedNode).toString());
        }


        this.appendLine("");
        this.appendLine("");
        this.appendHeading("SubNode Meta Model" + " (count: " + subNodes.size() + ")");
        this.addLineSeparator();
        for (final PropertyMetaModel subNodeMetaModel : subNodes) {
            final Node value = (Node) subNodeMetaModel.getValue(selectedNode);
            if (value != null) {
                this.appendLine(" - " + subNodeMetaModel.getName() + ": " + value);
            }
        }

        this.appendLine("");
        this.appendLine("");
        this.appendHeading("SubList Meta Model" + " (count: " + subLists.size() + ")");
        this.addLineSeparator();
        for (int index_allSublists = 0; index_allSublists < subLists.size(); index_allSublists++) {
            final PropertyMetaModel        subListMetaModel = subLists.get(index_allSublists);
            final NodeList<? extends Node> subList          = (NodeList<? extends Node>) subListMetaModel.getValue(selectedNode);
            if (subList != null && !subList.isEmpty()) {
                this.appendLine(subListMetaModel.getName() + " (count: " + subList.size() + ")");
                for (int index_sublist = 0; index_sublist < subList.size(); index_sublist++) {
                    Node subListNode = subList.get(index_sublist);
                    this.appendLine(index_sublist + ": " + ASCIITreePrinter.CLASS_RANGE_SUMMARY_FORMAT.apply(subListNode));
                }
            }
            if (index_allSublists < (subLists.size() - 1)) {
                this.appendLine("");
            }
        }
    }


    private void setupStyles() {
        // Setup styles
        this.styleNormal = new SimpleAttributeSet();
        StyleConstants.setFontFamily(this.styleNormal, "Monospaced");

        this.styleBoldBlue = new SimpleAttributeSet(this.styleNormal);
        StyleConstants.setBold(this.styleBoldBlue, true);
        StyleConstants.setForeground(this.styleBoldBlue, JBColor.BLUE);

        this.styleHighAlert = new SimpleAttributeSet(this.styleBoldBlue);
        StyleConstants.setItalic(this.styleHighAlert, true);
        StyleConstants.setForeground(this.styleHighAlert, JBColor.RED);
    }


}
