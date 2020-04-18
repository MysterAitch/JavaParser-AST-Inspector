package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.components;

import com.intellij.ui.JBColor;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

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
