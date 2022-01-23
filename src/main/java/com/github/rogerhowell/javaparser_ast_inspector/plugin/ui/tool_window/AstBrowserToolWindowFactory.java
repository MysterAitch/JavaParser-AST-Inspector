package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.tool_window;

import com.github.javaparser.ParserConfiguration;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.logging.NotificationLogger;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.forms.AstInspectorToolWindow;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.forms.Form;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.LanguageLevelProjectExtension;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Collectors;

public class AstBrowserToolWindowFactory implements ToolWindowFactory, DumbAware {

    private static final NotificationLogger notificationLogger = new NotificationLogger(AstBrowserToolWindowFactory.class);


    @NotNull
    private static ParserConfiguration.LanguageLevel getLanguageLevelFromProject(@NotNull Project project) {
        final LanguageLevelProjectExtension languageLevelProjectExtension = LanguageLevelProjectExtension.getInstance(project);

        final LanguageLevel projectLanguageLevel = languageLevelProjectExtension.getLanguageLevel();
        final String        languageLevelName    = projectLanguageLevel.name();


        // Note that not all of these will be present in each IDE
        // Hence using String as the key, instead of LanguageLevel
        final HashMap<String, ParserConfiguration.LanguageLevel> languageLevelMap = new HashMap<>();
        for (ParserConfiguration.LanguageLevel javaParserLanguageLevel : ParserConfiguration.LanguageLevel.values()) {
            // JavaParser uses `JAVA_{}`, while IntelliJ uses `JDK_`.
            String intellijLanguageLevelName = javaParserLanguageLevel.name().replace("JAVA_", "JDK_");
            languageLevelMap.put(intellijLanguageLevelName, javaParserLanguageLevel);
        }
        for (ParserConfiguration.LanguageLevel javaParserLanguageLevel : ParserConfiguration.LanguageLevel.values()) {
            // JavaParser uses `JAVA_{}`, while IntelliJ uses `JDK_`.
            // Some versions have a `1.` prefix (e.g. JDK 6 is also known as 1.6, thus is listed as JDK_1_6 as opposed to JDK_6).
            String intellijLanguageLevelName = javaParserLanguageLevel.name().replace("JAVA_", "JDK_1_");
            languageLevelMap.put(intellijLanguageLevelName, javaParserLanguageLevel);
        }

        // Default to whatever the "CURRENT" version is if it isn't found.
        ParserConfiguration.LanguageLevel selectedLanguageLevel = languageLevelMap.getOrDefault(
                languageLevelName,
                ParserConfiguration.LanguageLevel.CURRENT
        );
        if(languageLevelMap.containsKey(languageLevelName)) {
            System.out.println("projectLanguageLevel.name() = " + projectLanguageLevel.name());
            System.out.println("Selected: " + selectedLanguageLevel);
        }
        return selectedLanguageLevel;
    }


    /**
     * Helper method for adding content (tabs) to the tool window.
     */
    public void addContent(final ToolWindow toolWindow, String panelTitle, Form form) {

        /*
         * https://www.jetbrains.org/intellij/sdk/docs/user_interface_components/tool_windows.html
         * """As mentioned previously, tool windows can contain multiple tabs, or contents.
         * To manage the contents of a tool window, you can call ToolWindow.getContentManager().
         * To add a tab (content), you first need to create it by calling ContentManager.getFactory().createContent(),
         * and then to add it to the tool window using ContentManager.addContent()."""
         */
        final ContentManager contentManager        = toolWindow.getContentManager();
        final ContentFactory contentManagerFactory = contentManager.getFactory();

        final Optional<JPanel> mainPanel = form.getMainPanel();
        if (mainPanel.isPresent()) {
            final Content panelContent = contentManagerFactory.createContent(mainPanel.get(), panelTitle, false);
            contentManager.addContent(panelContent);
        } else {
            notificationLogger.warn("The panel is unexpectedly null -- unable to produce the tool window.");
        }

    }


    /**
     * https://www.jetbrains.org/intellij/sdk/docs/user_interface_components/tool_windows.html
     * In addition to that, you specify the factory class - the name of a class implementing the ToolWindowFactory interface.
     *
     * When the user clicks on the tool window button, the createToolWindowContent() method of the factory class is called,
     * and initializes the UI of the tool window.
     *
     * This procedure ensures that unused tool windows don’t cause any overhead in startup time or memory usage: if a user
     * does not interact with the tool window of your plugin, no plugin code will be loaded or executed.
     */
    @Override
    public void createToolWindowContent(@NotNull final Project project, @NotNull final ToolWindow toolWindow) {
        notificationLogger.traceEnter(project);


        // Parse Only Panel
        ParserConfiguration parserConfiguration = new ParserConfiguration();
        parserConfiguration.setLanguageLevel(getLanguageLevelFromProject(project));

        String             projectName        = project.getName();
        ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
        VirtualFile[]      vFiles             = projectRootManager.getContentSourceRoots();
        String sourceRootsList = Arrays.stream(vFiles)
                                       .map(VirtualFile::getUrl)
                                       .map(s -> " - " + s)
                                       .collect(Collectors.joining(String.format("%n")));

        notificationLogger.info(project, String.format("Detected JavaParser language level for %s:%n%s", projectName, parserConfiguration.getLanguageLevel().toString()));
        notificationLogger.info(project, String.format("Source roots for the %s plugin:%n%s", projectName, sourceRootsList));

        final AstInspectorToolWindow parseOnlyPanel = new AstInspectorToolWindow(project, toolWindow, parserConfiguration);
        this.addContent(toolWindow, "Parse Only", parseOnlyPanel);


        // Parse and Resolve Panel
        // ...


        // Parse and Export Panel
        // ...

    }


}
