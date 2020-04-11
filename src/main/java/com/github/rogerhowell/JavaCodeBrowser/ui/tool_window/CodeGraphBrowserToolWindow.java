package com.github.rogerhowell.JavaCodeBrowser.ui.tool_window;

import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.CompilationUnit.Storage;
import com.github.javaparser.printer.YamlPrinter;
import com.github.javaparser.utils.SourceRoot;
import com.github.rogerhowell.JavaCodeBrowser.parsing.Parsing;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.ui.treeStructure.Tree;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class CodeGraphBrowserToolWindow {

    protected Parsing parsing;
    Map<Path, SourceRoot>                   sourceRootMap      = new HashMap<>();
    Map<Path, ParseResult<CompilationUnit>> pathParseResultMap = new HashMap<>();
    // UI Components
    private JPanel    sourceRootsListingPanel;
    private JPanel    parseResultAstPanel;
    private JTextArea textArea1;
    private JButton   refreshButton;
    private Tree      tree1;


    public CodeGraphBrowserToolWindow(final Project project, final ToolWindow toolWindow) {
        this.parsing = new Parsing();
        this.refreshButton.addActionListener(e -> this.updateProjectSources(project));
        this.updateProjectSources(project);

        this.tree1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) CodeGraphBrowserToolWindow.this.tree1.getLastSelectedPathComponent();
                    if (node == null) { return; }
                    Object nodeInfo = node.getUserObject();
                    // Cast nodeInfo to your object and do whatever you want
                    System.out.println("nodeInfo = " + nodeInfo);
                }
            }
        });


        // Set the icon for leaf nodes.
        ImageIcon graphIcon = createImageIcon("/JavaCodeBrowser/graph icon -- 13x13.png");
        ImageIcon errorIcon = createImageIcon("/JavaCodeBrowser/graph icon -- error -- 13x13.png");
        if (graphIcon != null && errorIcon != null) {
            this.tree1.setCellRenderer(new MyRenderer(graphIcon, errorIcon));
        } else {
            System.err.println("Icon missing; using default.");
        }
    }


    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    protected static ImageIcon createImageIcon(String path) {
        URL imgURL = CodeGraphBrowserToolWindow.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }


    public JComponent getGraphPanel(final Project project) {

        Document    currentDoc  = Objects.requireNonNull(FileEditorManager.getInstance(project).getSelectedTextEditor()).getDocument();
        VirtualFile currentFile = FileDocumentManager.getInstance().getFile(currentDoc);
        String      fileName    = Objects.requireNonNull(currentFile).getPath();

        this.parseResultAstPanel = new JPanel();
        JTextArea textArea = new JTextArea();

        final ParseResult<CompilationUnit> parseResult = this.pathParseResultMap.get(currentFile.getPath());
        if (parseResult != null) {
            final YamlPrinter printer = new YamlPrinter(true);
            if (parseResult.getResult().isPresent()) {
                textArea.setText(printer.output(parseResult.getResult().get()));
            } else {
                textArea.setText("");
            }
        } else {
            textArea.setText("ERROR PARSING");
        }
        return this.parseResultAstPanel;
    }


    public JComponent getParseSinglePanel() {
        return null;
    }


    public JPanel getSourceRootsListingPanel() {
        return this.sourceRootsListingPanel;
    }


    @Override
    public String toString() {
        return "CodeGraphBrowserToolWindow{}";
    }


    public MutableTreeNode tree(final Path basePath, final MutableTreeNode parent, final SourceRoot sourceRoot, final ParseResult<CompilationUnit> parseResult) {
        return new DefaultMutableTreeNode(parseResult);
    }


    public MutableTreeNode tree(final Path basePath, final MutableTreeNode parent, final SourceRoot sourceRoot) {
        final Path                   sourceRootPath = sourceRoot.getRoot();
        final DefaultMutableTreeNode newChild       = new DefaultMutableTreeNode(basePath.relativize(sourceRootPath).toString());

        try {
            List<ParseResult<CompilationUnit>> parseResults = sourceRoot.tryToParse();
            parseResults.sort(Comparator.comparing(o -> o.getResult().get().getStorage().get().getPath()));
            for (ParseResult<CompilationUnit> parseResult : parseResults) {
                this.pathParseResultMap.put(parseResult.getResult().get().getStorage().get().getPath(), parseResult);
                newChild.add(this.tree(basePath, parent, sourceRoot, parseResult));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return newChild;
    }


    public DefaultMutableTreeNode tree(@NotNull final Path basePath, @Nullable final MutableTreeNode parent, @NotNull final List<? extends SourceRoot> sourceRoots) {
        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Root: " + basePath.getFileName().toString());

        sourceRoots.sort(Comparator.comparing(SourceRoot::getRoot));
        for (SourceRoot sourceRoot : sourceRoots) {
            this.sourceRootMap.put(sourceRoot.getRoot(), sourceRoot);
            root.add(this.tree(basePath, parent, sourceRoot));
        }

        return root;
    }


    private void updateProjectSources(@NotNull final Project project) {
        final String           projectName = project.getName();
        final VirtualFile[]    vFiles      = ProjectRootManager.getInstance(project).getContentSourceRoots();
        final List<SourceRoot> sourceRoots = this.parsing.vFilesToSourceRoots(vFiles);
        this.tree1.setModel(new DefaultTreeModel(this.tree(Paths.get(project.getBasePath()), null, sourceRoots), false));
    }


    protected static class MyRenderer extends DefaultTreeCellRenderer {

        private final Icon folderIcon;
        private final Icon graphIcon;
        private final Icon errorIcon;


        public MyRenderer(Icon graphIcon, Icon errorIcon) {
            super();
            this.graphIcon = graphIcon;
            this.errorIcon = errorIcon;
            this.folderIcon = UIManager.getIcon("FileView.directoryIcon");
        }


        private void getTreeCellRendererComponent(JTree tree, ParseResult<? extends CompilationUnit> parseResult, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            if (parseResult.isSuccessful()) {
                this.setIcon(this.graphIcon);
            } else {
                this.setIcon(this.errorIcon);
            }

            if (parseResult.getResult().isPresent()) {
                final CompilationUnit cu             = parseResult.getResult().get();
                final Storage         cuStorage      = cu.getStorage().get();
                final Path            sourceRootPath = cuStorage.getSourceRoot();
                this.setText(sourceRootPath.relativize(cuStorage.getPath()).toString());
                this.setToolTipText(cuStorage.getPath().toString());
            } else {
                this.setText("ERROR: Unable to get storage information.");
                this.setToolTipText(null);
            }
        }


        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            if (value instanceof DefaultMutableTreeNode) {
                final DefaultMutableTreeNode value2     = (DefaultMutableTreeNode) value;
                final Object                 userObject = value2.getUserObject();
                if (userObject instanceof ParseResult) {
                    final ParseResult<CompilationUnit> parseResult = (ParseResult<CompilationUnit>) userObject;
                    this.getTreeCellRendererComponent(tree, parseResult, sel, expanded, leaf, row, hasFocus);

                } else if (userObject instanceof String) {
                    final String string = (String) userObject;
                    this.setText(string);
                    this.setIcon(this.folderIcon);
                } else if (userObject instanceof SourceRoot) {
                    final SourceRoot sourceRoot = (SourceRoot) userObject;
                    this.setText("SOURCE ROOT: " + sourceRoot.toString());
                } else {
                    this.setText("Unknown value2: " + value2.toString());
                }
            } else {
                this.setText("Unknown value: " + value.toString());
            }

            return this;
        }


    }
}
