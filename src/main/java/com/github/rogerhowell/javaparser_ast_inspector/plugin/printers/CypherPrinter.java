package com.github.rogerhowell.javaparser_ast_inspector.plugin.printers;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.metamodel.NodeMetaModel;
import com.github.javaparser.metamodel.PropertyMetaModel;
import com.github.javaparser.utils.LineSeparator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.github.javaparser.utils.Utils.assertNotNull;
import static java.util.stream.Collectors.toList;

public class CypherPrinter implements NodePrinter {


    private static final int DEFAULT_STRINGBUILDER_CAPACITY = 5000;

    private static final String EOL = LineSeparator.SYSTEM.asRawString();

    private final Set<String> currentIds;
    private final boolean     outputNodeType;
    private       int         nodeCount;


    public CypherPrinter(boolean outputNodeType) {
        this.outputNodeType = outputNodeType;
        this.currentIds = new HashSet<>();
    }


    private static String escapeQuotes(String value) {
        return value.replace("'", "\\'");
    }


    private String nextNodeName() {
        return "n" + (this.nodeCount++);
    }


    public void output(Node node, String parentNodeName, String name, StringBuilder builder) {
        assertNotNull(node);


        NodeMetaModel           metaModel             = node.getMetaModel();
        List<PropertyMetaModel> allPropertyMetaModels = metaModel.getAllPropertyMetaModels();

        List<PropertyMetaModel> attributes = allPropertyMetaModels
                .stream()
                .filter(PropertyMetaModel::isAttribute)
                .filter(PropertyMetaModel::isSingular)
                .collect(toList());
        List<PropertyMetaModel> subNodes = allPropertyMetaModels
                .stream()
                .filter(PropertyMetaModel::isNode)
                .filter(PropertyMetaModel::isSingular)
                .collect(toList());
        List<PropertyMetaModel> subLists = allPropertyMetaModels
                .stream()
                .filter(PropertyMetaModel::isNodeList)
                .collect(toList());

        String ndName = this.nextNodeName();
        this.currentIds.add(ndName);

        builder.append(EOL)
               .append("WITH ").append(String.join(", ", this.currentIds)).append(EOL);


        builder.append("MERGE(").append(ndName).append(":Node:").append(metaModel.getTypeName()).append(" {");

        if (this.outputNodeType) {
            builder.append(EOL)
                   .append("  type: '").append(metaModel.getTypeName()).append("'");
        }

//        builder.append(
//                EOL + "  nodeName: '" + ndName + "'," +
//                EOL + "  parentName: '" + parentNodeName + "'" +
//                "");

        builder.append(",").append(EOL)
               .append("  ").append("name").append(": '").append(escapeQuotes(name)).append("'");

        for (PropertyMetaModel a : attributes) {
            String x = "," + EOL + "  " + escapeQuotes(a.getName()) + ": '" + escapeQuotes(a.getValue(node).toString()) + "'";
            builder.append(x);
        }

        builder.append(EOL)
               .append("})");

        // Do relationships
        if (parentNodeName != null) {
            builder.append(EOL)
                   .append("MERGE (").append(parentNodeName).append(")<-[:PARENT]-(").append(ndName).append(")");
        }
        builder.append(EOL);
        builder.append(EOL);


        for (PropertyMetaModel sn : subNodes) {
            Node nd = (Node) sn.getValue(node);
            if (nd != null) {
                this.output(nd, ndName, sn.getName(), builder);
            }
        }

        for (PropertyMetaModel sl : subLists) {
            NodeList<? extends Node> nl = (NodeList<? extends Node>) sl.getValue(node);
            if (nl != null && nl.isNonEmpty()) {
                String slName = sl.getName().substring(0, sl.getName().length() - 1);
                for (Node nd : nl) {
                    this.output(nd, ndName, slName, builder);
                }
            }
        }

        this.currentIds.remove(name);
    }


    @Override
    public String output(Node node) {
        this.nodeCount = 0;
        StringBuilder output = new StringBuilder(DEFAULT_STRINGBUILDER_CAPACITY);
        this.output(node, null, "root", output);
        return output.toString();
    }


    public void output2(Node node, String parentNodeName, String name, StringBuilder builder) {
        assertNotNull(node);

        NodeMetaModel           metaModel             = node.getMetaModel();
        List<PropertyMetaModel> allPropertyMetaModels = metaModel.getAllPropertyMetaModels();

        List<PropertyMetaModel> attributes = allPropertyMetaModels
                .stream()
                .filter(PropertyMetaModel::isAttribute)
                .filter(PropertyMetaModel::isSingular)
                .collect(toList());
        List<PropertyMetaModel> subNodes = allPropertyMetaModels
                .stream()
                .filter(PropertyMetaModel::isNode)
                .filter(PropertyMetaModel::isSingular)
                .collect(toList());
        List<PropertyMetaModel> subLists = allPropertyMetaModels
                .stream()
                .filter(PropertyMetaModel::isNodeList)
                .collect(toList());

        String ndName = this.nextNodeName();
        if (this.outputNodeType) {
            builder.append(EOL)
                   .append(ndName).append(" [label=\"").append(escapeQuotes(name)).append(" (").append(metaModel.getTypeName()).append(")\"];");
        } else {
            builder.append(EOL)
                   .append(ndName).append(" [label=\"").append(escapeQuotes(name)).append("\"];");
        }

        if (parentNodeName != null) {
            builder.append(EOL)
                   .append(parentNodeName).append(" -> ").append(ndName).append(";");
        }

        for (PropertyMetaModel a : attributes) {
            String attrName = this.nextNodeName();
            builder.append(EOL)
                   .append(attrName).append(" [label=\"").append(escapeQuotes(a.getName())).append("='").append(escapeQuotes(a.getValue(node).toString())).append("'\"];");
            builder.append(EOL)
                   .append(ndName).append(" -> ").append(attrName).append(";");

        }

        for (PropertyMetaModel sn : subNodes) {
            Node nd = (Node) sn.getValue(node);
            if (nd != null) {
                this.output(nd, ndName, sn.getName(), builder);
            }
        }

        for (PropertyMetaModel sl : subLists) {
            NodeList<? extends Node> nl = (NodeList<? extends Node>) sl.getValue(node);
            if (nl != null && nl.isNonEmpty()) {
                String ndLstName = this.nextNodeName();
                builder.append(EOL)
                       .append(ndLstName).append(" [label=\"").append(escapeQuotes(sl.getName())).append("\"];");
                builder.append(EOL)
                       .append(ndName).append(" -> ").append(ndLstName).append(";");
                String slName = sl.getName().substring(0, sl.getName().length() - 1);
                for (Node nd : nl) {
                    this.output(nd, ndLstName, slName, builder);
                }
            }
        }
    }


    @Override
    public String toString() {
        return "CypherPrinter{" +
               "currentIds=" + this.currentIds +
               ", outputNodeType=" + this.outputNodeType +
               ", nodeCount=" + this.nodeCount +
               '}';
    }
}
