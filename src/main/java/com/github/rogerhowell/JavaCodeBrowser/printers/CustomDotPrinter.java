
/*
 * Copyright (C) 2007-2010 Júlio Vilmar Gesser.
 * Copyright (C) 2011, 2013-2016 The JavaParser Team.
 *
 * This file is part of JavaParser.
 *
 * JavaParser can be used either under the terms of
 * a) the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * b) the terms of the Apache License
 *
 * You should have received a copy of both licenses in LICENCE.LGPL and
 * LICENCE.APACHE. Please refer to those files for details.
 *
 * JavaParser is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 */

package com.github.rogerhowell.JavaCodeBrowser.printers;


import com.github.javaparser.Range;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.metamodel.NodeMetaModel;
import com.github.javaparser.metamodel.PropertyMetaModel;
import com.github.javaparser.resolution.UnsolvedSymbolException;
import com.github.javaparser.resolution.types.ResolvedType;
import org.apache.commons.text.StringEscapeUtils;

import java.util.List;

import static com.github.javaparser.utils.Utils.assertNotNull;
import static java.util.stream.Collectors.toList;

/**
 * Outputs a Graphviz diagram of the AST.
 */
public class CustomDotPrinter {

    private final boolean outputNodeType;
    private final boolean defaultResolveTypes;
    private       int     nodeCount;

    public CustomDotPrinter(final boolean outputNodeType) {
        this.outputNodeType = outputNodeType;
        this.defaultResolveTypes = false;
        this.nodeCount = 0;
    }

    private static String escape(String value) {
        return value.replace("\"", "\\\"");
    }

    private String nextNodeName() {
        return "n" + (this.nodeCount++);
    }

    public String output(final Node node, final boolean resolveTypes) {
        this.nodeCount = 0;
        final StringBuilder output = new StringBuilder();
        output.append("digraph {");
        output(node, null, "root", output, resolveTypes);
        output.append(System.lineSeparator()).append("}");
        return output.toString();
    }

    public void output(final Node node, final String parentNodeName, final String name, final StringBuilder builder) {
        this.output(node, parentNodeName, name, builder, this.defaultResolveTypes);
    }

    public void output(final Node node, final String parentNodeName, final String name, final StringBuilder builder, final boolean resolveTypes) {
        assertNotNull(node);
        final NodeMetaModel           metaModel             = node.getMetaModel();
        final List<PropertyMetaModel> allPropertyMetaModels = metaModel.getAllPropertyMetaModels();
        final List<PropertyMetaModel> attributes            = allPropertyMetaModels.stream().filter(PropertyMetaModel::isAttribute).filter(PropertyMetaModel::isSingular).collect(toList());
        final List<PropertyMetaModel> subNodes              = allPropertyMetaModels.stream().filter(PropertyMetaModel::isNode).filter(PropertyMetaModel::isSingular).collect(toList());
        final List<PropertyMetaModel> subLists              = allPropertyMetaModels.stream().filter(PropertyMetaModel::isNodeList).collect(toList());

        final String typeName = metaModel.getTypeName();
        String       range    = "";

        // Custom: If range is present, add it.
        if (node.getRange().isPresent()) {
            range += "";
            range += rangeAsString(node.getRange().get());
            range += "";
        }


        final String lineColor;
        if (name.equals("comment")) {
//            lineColor = "gray";
            lineColor = "LightGray";
        } else if (name.equals("name")) {
//            lineColor="darkgreen";
//            lineColor="blue";
//            lineColor="SlateBlue";
            lineColor = "SteelBlue";
        } else if (typeName.equals("StringLiteralExpr")) {
//            lineColor="SlateBlue";
            lineColor = "SeaGreen";
        } else {
            lineColor = "black";
        }

        final String  ndName  = nextNodeName();
        StringBuilder nodeDot = new StringBuilder();
        nodeDot.append(System.lineSeparator());
        nodeDot.append(ndName);
        nodeDot.append(" [");
        nodeDot.append("shape=none");
        nodeDot.append(",");
        nodeDot.append("label=<");

        nodeDot.append("<font color='").append(lineColor).append("'>");

        nodeDot.append("<table" + " border='0'" + " color='").append(lineColor).append("'").append(" cellspacing='0'").append(" cellborder='1'").append(">");
        nodeDot.append("<tr>");
        nodeDot.append("<td colspan='2'>");
        nodeDot.append("<font color='").append(lineColor).append("'>");
        nodeDot.append(escape(name));
        if (this.outputNodeType) {
            nodeDot.append(" (").append(typeName).append(")");
        }
        nodeDot.append("</font>");
        nodeDot.append("<br/>");
        nodeDot.append("<font color='#aaaaaa' size='8'>");
        nodeDot.append(range);
        nodeDot.append("</font>");


        if (resolveTypes && node instanceof Expression) {
            final Expression bar              = (Expression) node;
            String           returnTypeString = null;

            try {
//                if (!bar.toString().equals("System") && !bar.toString().equals("String")) {
                ResolvedType returnType = bar.calculateResolvedType();
                returnTypeString = StringEscapeUtils.escapeHtml4(returnType.describe());
//                }
            } catch (final UnsolvedSymbolException e) {
//                returnTypeString = "Unable to resolve type of " + bar + " (UnsolvedSymbolException)";
                System.err.println("Unable to resolve type of " + bar + " (UnsolvedSymbolException)");
                e.printStackTrace();
            } catch (final Exception e) {
//                returnTypeString = "Unable to resolve type of " + bar + " (Exception - " + e.getClass().getName() + ")";
                System.err.println("Unable to resolve type of " + bar + " (Exception - " + e.getClass().getName() + ")");
                e.printStackTrace();
            }

            if (returnTypeString != null) {
                nodeDot.append("<br/>");
                nodeDot.append("<font color='red' size='8'>");
                nodeDot.append("Resolved Type: ");
                nodeDot.append(returnTypeString);
                nodeDot.append("</font>");
            }
        }

        nodeDot.append("</td>");
        nodeDot.append("</tr>");

        for (final PropertyMetaModel a : attributes) {
            nodeDot.append("<tr>");
            nodeDot.append("<td>").append(a.getName()).append("</td>");
            nodeDot.append("<td align='left'>");

            String   value = a.getValue(node).toString();
            String[] lines = value.trim().split("\\r?\\n");

            String cellAlignment = lines.length > 1 ? "left" : "center";
            nodeDot.append("<table border='0' cellspacing='0' cellpadding='0'>");
            for (final String line : lines) {
                nodeDot.append("<tr><td align='").append(cellAlignment).append("'>").append(StringEscapeUtils.escapeHtml4(line)).append("</td></tr>");
            }
            nodeDot.append("</table>");

            nodeDot.append("</td>");
            nodeDot.append("</tr>");
        }

        nodeDot.append("</table>");

        nodeDot.append("</font>");
        nodeDot.append(">];");

        builder.append(nodeDot.toString());


        if (parentNodeName != null) {
            builder.append(System.lineSeparator())
                   .append(parentNodeName).append(" -> ").append(ndName)
                   .append(" [color = ").append(lineColor).append("]")
                   .append(";");
        }

        for (final PropertyMetaModel sn : subNodes) {
            final Node nd = (Node) sn.getValue(node);
            if (nd != null) {
                output(nd, ndName, sn.getName(), builder, resolveTypes);
            }
        }

        String color;

        for (final PropertyMetaModel sl : subLists) {
            final NodeList<? extends Node> nl = (NodeList<? extends Node>) sl.getValue(node);
            if (nl != null && nl.isNonEmpty()) {
//                color = "FireBrick";
//                color = "red";
                color = "OrangeRed";
                final String ndLstName = nextNodeName();
                builder.append(System.lineSeparator()).append(ndLstName).append(" [shape=ellipse,color=").append(color).append(",label=\"").append(escape(sl.getName())).append("\"];");
                builder.append(System.lineSeparator()).append(ndName).append(" -> ").append(ndLstName).append(" [color = ").append(color).append("];");
                final String slName = sl.getName().substring(0, sl.getName().length() - 1);
                for (final Node nd : nl) {
                    output(nd, ndLstName, slName, builder, resolveTypes);
                }
            }
        }
    }

    public String output(final Node node) {
        return this.output(node, this.defaultResolveTypes);
    }

    private String rangeAsString(final Range range) {
        final int startLine   = range.begin.line;
        final int startColumn = range.begin.column;
        final int endLine     = range.end.line;
        final int endColumn   = range.end.column;

        return "[" + startLine + ":" + startColumn + "-" + endLine + ":" + endColumn + "]";
    }
}
