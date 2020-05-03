package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.output_results_tabs;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.util.NotificationLogger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.ui.components.JBTabbedPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ParseResultsTabPanesContainer extends JBTabbedPane {

    private static final NotificationLogger notificationLogger = new NotificationLogger(ParseResultsTabPanesContainer.class);

    @NotNull
    private final List<ParseResultsTabPane> panes;


    public ParseResultsTabPanesContainer() {
        super();
        notificationLogger.traceEnter();

        this.panes = new ArrayList<>();

        this.doReset(null);
    }


    public ParseResultsTabPane addParseResultPane(@NotNull Project project, @NotNull PsiFile psiFile, @NotNull ParseResult<CompilationUnit> parseResult) {
        notificationLogger.traceEnter(project);

        // Remove previous
        this.removeAll();

        // Add new
        final ParseResultsTabPane parseResultsTabPane = new ParseResultsTabPane(project, psiFile, parseResult);
        this.add(parseResultsTabPane.getPaneTitle(), parseResultsTabPane);
        this.setSelectedComponent(parseResultsTabPane);

        return parseResultsTabPane;
    }


    public void doReset(@Nullable Project project) {
        notificationLogger.traceEnter(project);

        // Remove previous
        this.removeAll();

        // Add blank
        final EmptyPane newPanel = new EmptyPane();
        this.add(newPanel);
        this.setSelectedComponent(newPanel);
    }


    @NotNull
    public List<ParseResultsTabPane> getPanes() {
        notificationLogger.traceEnter();
        return this.panes;
    }


    private static class EmptyPane extends JPanel {

        EmptyPane() {
            super();
            this.add(new JLabel("No files parsed."), SwingConstants.CENTER);
        }

    }

}
