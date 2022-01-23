package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.tool_window;

import com.github.javaparser.ParserConfiguration;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.logging.NotificationLogger;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.forms.AstInspectorToolWindow;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.forms.Form;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.util.LanguageLevelUtil;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

public class AstBrowserToolWindowFactory implements ToolWindowFactory, DumbAware {

    private static final NotificationLogger notificationLogger = new NotificationLogger(AstBrowserToolWindowFactory.class);


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
        parserConfiguration.setLanguageLevel(LanguageLevelUtil.getLanguageLevelFromProject(project));

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
