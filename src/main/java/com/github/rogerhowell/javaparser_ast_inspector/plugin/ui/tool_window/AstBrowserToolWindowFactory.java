package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.tool_window;

import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.forms.AstInspectorToolWindow;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.forms.AstInspectorWindow2;
import com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.forms.Form;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Optional;

public class AstBrowserToolWindowFactory implements ToolWindowFactory {

    private static final Logger LOGGER = Logger.getInstance(AstBrowserToolWindowFactory.class.getName());


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
        LOGGER.trace("TRACE: Entering createToolWindowContent");

        //
        final AstInspectorToolWindow toolWindowContent  = new AstInspectorToolWindow(project, toolWindow);
        final AstInspectorWindow2    toolWindowContent2 = new AstInspectorWindow2(project, toolWindow);

        //            final String  panelTitle   = "JavaParser AST Inspector #2";
        this.addContent(toolWindow, "JavaParser AST Inspector #2", toolWindowContent2);
        this.addContent(toolWindow, "JavaParser AST Inspector #1", toolWindowContent);
    }


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
            LOGGER.warn("ERROR: The panel is unexpectedly null -- unable to produce the tool window.");
        }

    }


}
