package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.output_results_tabs;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.util.NotificationLogger;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.ui.components.JBTabbedPane;
import org.jetbrains.annotations.NotNull;

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


    public void doReset(@NotNull Project project) {
        notificationLogger.traceEnter(project);

//        this.updateTree(null);

//        this.setParseResultTextPane("Reset");
//        this.setParseResult(""); // The parse result is the output textbox

//        // Reset the sidebar content, ready to be inserted into again:
//        this.nodeDetailsTextPane.clear();
//        this.nodeDetailsTextPane.appendLine("No node selected");

//        //
//        if (this.tabbedPane != null && this.tabbedPane.getTabCount() > 0) {
//            this.tabbedPane.setTitleAt(0, "No file parsed.");
//        } else {
//            notificationLogger.debug(this.project, "Tabbed pane either null or empty: " + this.tabbedPane);
//        }
    }


    @NotNull
    public List<ParseResultsTabPane> getPanes() {
        notificationLogger.traceEnter();
        return this.panes;
    }

}
