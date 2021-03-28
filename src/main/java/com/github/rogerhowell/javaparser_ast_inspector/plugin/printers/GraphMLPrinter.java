package com.github.rogerhowell.javaparser_ast_inspector.plugin.printers;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.metamodel.NodeMetaModel;
import com.github.javaparser.metamodel.PropertyMetaModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static com.github.javaparser.utils.Utils.assertNotNull;
import static java.util.stream.Collectors.toList;

/**
 * Outputs an GraphML file containing the AST for import into a graph database.
 */
public class GraphMLPrinter implements NodePrinter {

    private static final int DEFAULT_STRINGBUILDER_CAPACITY = 5000;

    private static final String DATA_INDENT  = "            ";
    private static final String EDGE_INDENT  = "        ";
    private static final String GRAPH_INDENT = "    ";
    private static final String KEY_INDENT   = "    ";
    private static final String NODE_INDENT  = "        ";

    private final Set<String>  edgeKeys;
    private final List<String> edges;
    private final Set<String>  nodeKeys;
    private final List<String> nodes;
    private final boolean      outputNodeType;

    private int edgeCount;
    private int nodeCount;


    public GraphMLPrinter(boolean outputNodeType) {
        this.edgeCount = 0;
        this.nodeCount = 0;
        this.outputNodeType = outputNodeType;
        this.nodeKeys = new TreeSet<>();
        this.edgeKeys = new TreeSet<>();
        this.nodes = new ArrayList<>();
        this.edges = new ArrayList<>();
    }


    private String attribute(String name, String value) {
        return " " + name + "=\"" + value + "\"";
    }


    private String dataEntry(String key, String value) {
        String escapedValue = value
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;");

        return "<data" +
               this.attribute("key", key)
               + ">" +
               escapedValue +
               "</data>";
    }


    private String keyEntry(String name, String elemType, String type) {
        return "<key" +
               this.attribute("id", name) +
               this.attribute("for", elemType) +
               this.attribute("attr.name", name) +
               this.attribute("attr.type", type) +
               "/>";
    }


    private String nextEdgeName() {
        return "e" + (this.edgeCount++);
    }


    private String nextNodeName() {
        return "n" + (this.nodeCount++);
    }


    private void output(Node node, String name, int level, String parentNdName) {
        assertNotNull(node);
        NodeMetaModel           metaModel             = node.getMetaModel();
        List<PropertyMetaModel> allPropertyMetaModels = metaModel.getAllPropertyMetaModels();
        List<PropertyMetaModel> attributes            = allPropertyMetaModels.stream().filter(PropertyMetaModel::isAttribute).filter(PropertyMetaModel::isSingular).collect(toList());
        List<PropertyMetaModel> subNodes              = allPropertyMetaModels.stream().filter(PropertyMetaModel::isNode).filter(PropertyMetaModel::isSingular).collect(toList());
        List<PropertyMetaModel> subLists              = allPropertyMetaModels.stream().filter(PropertyMetaModel::isNodeList).collect(toList());

        String        ndName      = this.nextNodeName();
        StringBuilder nodeBuilder = new StringBuilder(DEFAULT_STRINGBUILDER_CAPACITY);
        String        typeName    = metaModel.getTypeName();

        this.nodeKeys.add("id");
        this.nodeKeys.add("labels");
        nodeBuilder
                .append(NODE_INDENT)
                .append("<node" +
                        this.attribute("id", ndName) +
                        this.attribute("labels", ":Node" + ":" + typeName) +
                        ">");

        if (this.outputNodeType) {
            this.nodeKeys.add("type");
            nodeBuilder.append("\n").append(DATA_INDENT).append(this.dataEntry("type", typeName));
        }

        for (PropertyMetaModel attributeMetaModel : attributes) {
            String attributeName = attributeMetaModel.getName();
            String value         = attributeMetaModel.getValue(node).toString();
            this.nodeKeys.add(attributeName);
            nodeBuilder.append("\n").append(DATA_INDENT).append(this.dataEntry(attributeName, value));
        }

        nodeBuilder.append("\n").append(NODE_INDENT).append("</node>");
        this.nodes.add(nodeBuilder.toString());

        if (parentNdName != null) {
            String edgeName  = this.nextEdgeName();
            String edgeLabel = "PARENT";

            this.edgeKeys.add("id");
            this.edgeKeys.add("source");
            this.edgeKeys.add("target");
            this.edgeKeys.add("label");

            String edge = "";
            edge += EDGE_INDENT;
            edge += "<edge" +
                    this.attribute("id", edgeName) +
                    this.attribute("source", ndName) +
                    this.attribute("target", parentNdName) +
                    this.attribute("label", edgeLabel) +
                    ">";
            edge += "\n" + DATA_INDENT + this.dataEntry(edgeLabel, edgeLabel);
            edge += "\n" + EDGE_INDENT + "</edge>";

            this.edges.add(edge);
        }

        for (PropertyMetaModel subNodeMetaModel : subNodes) {
            Node value = (Node) subNodeMetaModel.getValue(node);
            if (value != null) {
                this.output(value, subNodeMetaModel.getName(), level + 1, ndName);
            }
        }
//
        for (PropertyMetaModel subListMetaModel : subLists) {
            NodeList<? extends Node> subList = (NodeList<? extends Node>) subListMetaModel.getValue(node);
            if (subList != null && !subList.isEmpty()) {
                String listName = subListMetaModel.getName();
                String singular = listName.substring(0, listName.length() - 1);
                for (Node subListNode : subList) {
                    this.output(subListNode, singular, level + 1, ndName);
                }
            }
        }
    }


    @Override
    public String output(Node node) {
        StringBuilder output = new StringBuilder(DEFAULT_STRINGBUILDER_CAPACITY);
        output.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                      "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\"\n" +
                      "         xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                      "         xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://graphml.graphdrawing.org/xmlns/1.0/graphml.xsd\">\n");

        this.output(node, "root", 0, null);

        this.nodeKeys.forEach(s -> {
            output.append("\n").append(KEY_INDENT).append(this.keyEntry(s, "node", "string"));
        });
        this.edgeKeys.forEach(s -> {
            output.append("\n").append(KEY_INDENT).append(this.keyEntry(s, "edge", "string"));
        });

        output.append("\n").append(GRAPH_INDENT).append("<graph id=\"G\" edgedefault=\"directed\">");
        this.nodes.forEach(s -> {
            output.append("\n").append(s);
        });

        this.edges.forEach(s -> {
            output.append("\n").append(s);
        });

        output.append("\n").append(GRAPH_INDENT).append("</graph>");
        output.append("\n").append("</graphml>");

        return output.toString();
    }

    @Override
    public String toString() {
        return "GraphMLPrinter{" +
                "nodes=" + this.nodes +
                ", outputNodeType=" + this.outputNodeType +
                ", edgeCount=" + this.edgeCount +
                ", nodeCount=" + this.nodeCount +
                '}';
    }
}

