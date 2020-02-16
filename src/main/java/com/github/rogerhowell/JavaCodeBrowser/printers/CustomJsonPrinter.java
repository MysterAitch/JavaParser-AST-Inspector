package com.github.rogerhowell.JavaCodeBrowser.printers;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.metamodel.NodeMetaModel;
import com.github.javaparser.metamodel.PropertyMetaModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.javaparser.utils.Utils.assertNotNull;
import static java.util.stream.Collectors.toList;

/**
 * Outputs a JSON file containing the AST meant for inspecting it.
 */
public class CustomJsonPrinter {
    private final boolean outputNodeType;

    public CustomJsonPrinter(final boolean outputNodeType) {
        this.outputNodeType = outputNodeType;
    }

    private static String q(final String value) {
        return "\"" + value.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r") + "\"";
    }

    public String output(final Node node) {
        return output(node, null, 0);
    }

    public String output(final Node node, final String name, final int level) {
        assertNotNull(node);
        final NodeMetaModel           metaModel             = node.getMetaModel();
        final List<PropertyMetaModel> allPropertyMetaModels = metaModel.getAllPropertyMetaModels();
        final List<PropertyMetaModel> attributes            = allPropertyMetaModels.stream().filter(PropertyMetaModel::isAttribute).filter(PropertyMetaModel::isSingular).collect(toList());
        final List<PropertyMetaModel> subNodes              = allPropertyMetaModels.stream().filter(PropertyMetaModel::isNode).filter(PropertyMetaModel::isSingular).collect(toList());
        final List<PropertyMetaModel> subLists              = allPropertyMetaModels.stream().filter(PropertyMetaModel::isNodeList).collect(toList());

        final List<String> content = new ArrayList<>();

        if (this.outputNodeType) {
            content.add(CustomJsonPrinter.q("_type") + ":" + CustomJsonPrinter.q(metaModel.getTypeName()));
        }

        for (final PropertyMetaModel attributeMetaModel : attributes) {
            content.add(CustomJsonPrinter.q(attributeMetaModel.getName()) + ":" + CustomJsonPrinter.q(attributeMetaModel.getValue(node).toString()));
        }


        // Custom: If range is present, add it.
        if (node.getRange().isPresent()) {
            content.add(CustomJsonPrinter.q("_start_line") + ":" + node.getRange().get().begin.line);
            content.add(CustomJsonPrinter.q("_start_column") + ":" + node.getRange().get().begin.column);
            content.add(CustomJsonPrinter.q("_end_line") + ":" + node.getRange().get().end.line);
            content.add(CustomJsonPrinter.q("_end_column") + ":" + node.getRange().get().end.column);
        }

        // Object creation
        if (node.getClass().getSimpleName().equals("ObjectCreationExpr")) {
            final ObjectCreationExpr objectCreationExpr = (ObjectCreationExpr) node;
            final String             foo                = objectCreationExpr.getType().getName().asString();
            content.add(CustomJsonPrinter.q("_typeNameString") + ":" + CustomJsonPrinter.q(foo));
        }


        for (final PropertyMetaModel subNodeMetaModel : subNodes) {
            final Node value = (Node) subNodeMetaModel.getValue(node);
            if (value != null) {
                content.add(output(value, subNodeMetaModel.getName(), level + 1));
            }
        }

        for (final PropertyMetaModel subListMetaModel : subLists) {
            final NodeList<? extends Node> subList = (NodeList<? extends Node>) subListMetaModel.getValue(node);
            if (subList != null && !subList.isEmpty()) {
                final List<String> listContent = new ArrayList<>();
                for (final Node subListNode : subList) {
                    listContent.add(output(subListNode, null, level + 1));
                }
                content.add(listContent.stream().collect(Collectors.joining(",", CustomJsonPrinter.q(subListMetaModel.getName()) + ":[", "]")));
            }
        }

        if (name == null) {
            return content.stream().collect(Collectors.joining(",", "{", "}"));
        }
        return content.stream().collect(Collectors.joining(",", CustomJsonPrinter.q(name) + ":{", "}"));
    }
}
