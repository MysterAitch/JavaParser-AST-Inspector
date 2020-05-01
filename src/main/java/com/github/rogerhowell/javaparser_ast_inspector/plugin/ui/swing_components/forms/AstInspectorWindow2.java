package com.github.rogerhowell.javaparser_ast_inspector.plugin.ui.swing_components.forms;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.util.Optional;

public class AstInspectorWindow2 implements DumbAwareForm {

    private static final Logger LOGGER = Logger.getInstance(AstInspectorWindow2.class.getName());

    private static final String URL_GITHUB_PLUGIN     = "https://github.com/MysterAitch/JavaParser-AST-Inspector";
    private static final String URL_GITHUB_JAVAPARSER = "https://github.com/JavaParser/JavaParser";
    private static final String URL_WEBSITE_JP        = "http://javaparser.org/";

    @NotNull
    private final Project project;

    @NotNull
    private final ToolWindow toolWindow;

    // Form Elements
    private JPanel     mainPanel;
    private JCheckBox  attributeCommentsCheckBox;
    private JCheckBox  storeTokensCheckBox;
    private JCheckBox outputNodeTypeCheckBox;
    private JComboBox exportAsCombobox;
    private JComboBox characterEncodingCombobox;
    private JComboBox languageLevelCombobox;
    private JTree     tree1;
    private JTextField textField1;
    private JSpinner   tabSizeSpinner;
    private JButton    gitHubButton;
    private JButton    javaParserButton;
    private JButton resetButton;
    private JButton parseButton;


    public AstInspectorWindow2(@NotNull final Project project, @NotNull final ToolWindow toolWindow) {
        this.project = project;
        this.toolWindow = toolWindow;
    }

    private JButton buttonWithIcon(@NotNull final String resourcePath) {
        final JButton jButton = new JButton();

        final Icon icon = IconLoader.getIcon(resourcePath);
        jButton.setIcon(icon);

        return jButton;
    }

    private void createUIComponents() {
        LOGGER.info("TRACE: private void createUIComponents() {");

        // Button
//        this.gitHubButton = this.buttonWithIcon("/logos/jp-logo.png");
        this.gitHubButton = new JButton();
        this.gitHubButton.addActionListener(e -> browseToUrl(URL_GITHUB_PLUGIN));

        // Button
        this.javaParserButton = this.buttonWithIcon("/logos/jp-logo_13x13.png");
        this.javaParserButton.addActionListener(e -> browseToUrl(URL_WEBSITE_JP));
    }


    private static void browseToUrl(@NotNull final String url) {
        LOGGER.info("BUTTON CLICK: URL=" + url);
        try {
            java.awt.Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException ioException) {
            ioException.printStackTrace();
            LOGGER.warn(ioException.getMessage());
        }
    }


    public Optional<JPanel> getMainPanel() {
        return Optional.ofNullable(this.mainPanel);
    }

}
