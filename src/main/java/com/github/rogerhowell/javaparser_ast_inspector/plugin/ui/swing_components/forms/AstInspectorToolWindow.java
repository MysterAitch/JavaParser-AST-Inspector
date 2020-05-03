package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.forms;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.config_panel.ConfigPanel;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.output_results_tabs.ParseResultsTabPane;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.output_results_tabs.ParseResultsTabPanesContainer;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.util.Constants;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.util.NotificationLogger;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.util.PsiUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.Optional;

public class AstInspectorToolWindow implements Form {

    private static final NotificationLogger notificationLogger = new NotificationLogger(AstInspectorToolWindow.class);

    @NotNull
    private final Project project;

    @NotNull
    private final ToolWindow toolWindow;

    private ConfigPanel                   configPanel;
    private JButton                       gitHubButton;
    private JButton                       javaParserButton;
    private JPanel                        mainPanel;
    private JButton                       parseButton;
    private ParseResultsTabPanesContainer parseResultsTabPanesContainer1;
    private JButton                       resetButton;


    public AstInspectorToolWindow(@NotNull final Project project, @NotNull final ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
    }


    private static void browseToUrl(@NotNull final String url) {
        notificationLogger.info("BUTTON CLICK: URL=" + url);
        try {
            Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException ioException) {
            ioException.printStackTrace();
            notificationLogger.warn(ioException.getMessage(), ioException);
        }
    }


    private JButton buttonWithIcon(@NotNull final String resourcePath) {
        final JButton jButton = new JButton();

        final Icon icon = IconLoader.getIcon(resourcePath);
        jButton.setIcon(icon);

        return jButton;
    }


    private void createUIComponents() {
        notificationLogger.traceEnter(this.project);

        //
        this.initButtons();

        //
        this.configPanel = new ConfigPanel(new ParserConfiguration());
        this.parseResultsTabPanesContainer1 = new ParseResultsTabPanesContainer();
        this.parseResultsTabPanesContainer1.doReset(this.project);
    }


    @Override
    public Optional<JPanel> getMainPanel() {
        return Optional.ofNullable(this.mainPanel);
    }


    private void initButtons() {
        notificationLogger.traceEnter(this.project);

        // Create buttons
        this.parseButton = new JButton();
        this.resetButton = new JButton();
        this.gitHubButton = new JButton();
        this.javaParserButton = this.buttonWithIcon("/logos/jp-logo_13x13.png");

        // Add button click handlers
        this.parseButton.addActionListener(e -> this.parseButtonClickHandler());
        this.resetButton.addActionListener(e -> this.resetButtonClickHandler());
        this.gitHubButton.addActionListener(e -> browseToUrl(Constants.URL_GITHUB_PLUGIN));
        this.javaParserButton.addActionListener(e -> browseToUrl(Constants.URL_WEBSITE_JP));

    }


    private void parseButtonClickHandler() {
        notificationLogger.traceEnter(this.project);

//        this.parseResultsTabPanesContainer1.

        final Optional<PsiFile> currentFileInEditor = PsiUtil.getCurrentFileInEditor(this.project);
        if (currentFileInEditor.isPresent()) {
            final PsiFile    psiFile    = currentFileInEditor.get();
            final JavaParser javaParser = new JavaParser(this.configPanel.getConfigFromForm());


            // parse result
//            final Optional<ParseResult<CompilationUnit>> optionalParseResult = parsePsiFile_editorContents(psiFile);
            final Optional<ParseResult<CompilationUnit>> optionalParseResult = this.parsePsiFile_diskContents(javaParser, psiFile);

            if (optionalParseResult.isPresent()) {
                final ParseResult<CompilationUnit> parseResult = optionalParseResult.get();

                // FIXME
                final ParseResultsTabPane pane = this.parseResultsTabPanesContainer1.addParseResultPane(this.project, psiFile, parseResult);
                pane.handleParseResult(this.configPanel, psiFile, parseResult);

                this.parseResultsTabPanesContainer1.setSelectedComponent(pane);

            } else {
                notificationLogger.warn(this.project, "No parse result available for file: " + psiFile);
            }
        } else {
            notificationLogger.warn(this.project, "No file selected in editor.");
        }

    }


    private Optional<ParseResult<CompilationUnit>> parsePsiFile_diskContents(JavaParser javaParser, PsiFile psiFile) {
        notificationLogger.traceEnter(this.project);
        try {
            final Path                         path   = PsiUtil.pathForPsi(psiFile);
            final ParseResult<CompilationUnit> result = javaParser.parse(path);
            return Optional.of(result);
        } catch (IOException e) {
            notificationLogger.warn(this.project, "Error trying to parse file.", e);
            e.printStackTrace();
            return Optional.empty();
        }
    }


    private void resetButtonClickHandler() {
        notificationLogger.traceEnter(this.project);
        this.parseResultsTabPanesContainer1.doReset(this.project);
    }


}
