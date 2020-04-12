package com.github.rogerhowell.javaparser_ast_inspector.plugin.printers;

import com.github.javaparser.Position;
import com.github.javaparser.ast.Node;
import com.github.javaparser.printer.PrettyPrinterConfiguration;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

/**
 * ASCII printable text tree (of the Nodes within an AST) i.e.
 * <PRE>
 * ASCIITreePrinter.print(StaticJavaParser.parseExpression( "new Object(){\n    int i;\n}") );
 * </PRE>
 * prints:
 * <PRE>
 * "new Object() {...}" ObjectCreationExpr : (1,1)-(3,1)
 * ├─"Object" ClassOrInterfaceType : (1,5)-(1,10)
 * │ └─"Object" SimpleName : (1,5)-(1,10)
 * └─"int i;" FieldDeclaration : (2,5)-(2,10)
 *   └─"i" VariableDeclarator : (2,9)-(2,9)
 *     ├─"int" PrimitiveType : (2,5)-(2,7)
 *     └─"i" SimpleName : (2,9)-(2,9)
 * </PRE>
 *
 * ...for
 * <PRE>
 * new Object(){
 *     int i;
 * }
 * </PRE>
 *
 * NOTE: this is handy for printing a summary of the {@link Node}s in the AST and how they are connected in the tree
 * for debugging what the structure of the AST is (directly in the console).
 *
 * The POINT of this tool is to have a "quick and dirty" way of "sanity checking" that the structure of the AST is.
 *
 * modified from:
 * https://stackoverflow.com/questions/4965335/how-to-print-binary-tree-diagram
 *
 * @author Eric DeFazio
 */
public class ASCIITreePrinter {

    /**
     * Print a Tree to System.out defining the contents with the AST node
     * @param rootNode any AST node to describe the contents of in tree form
     */
    public static void print(Node rootNode) {
        System.out.println(TNode.of( new TNode(rootNode)).output(SUMMARY_CLASS_RANGE_FORMAT));
    }

    /**
     * Print a Tree to System.out defining the contents with the AST node
     * @param rootNode the root
     * @param nodeFormat
     */
    public static void print(Node rootNode, Function<Node, String> nodeFormat){
        System.out.println(TNode.of( new TNode(rootNode)).output(nodeFormat));
    }

    /**
     * Print format each {@link Node} in the tree (prints to a single line) for example:
     *  <PRE>
     *  "@Deprecated...}" CompilationUnit : (1,1)-(15,3)
     *  \_______________/ \____________/   \__________/
     *    node summary      node class      node range
     *  </PRE>
     * @see #printNodeSummary(Node)
     * @see #printRange(Node)
     */
    public static Function<Node,String> SUMMARY_CLASS_RANGE_FORMAT = n->
            "\""+ printNodeSummary(n)+"\" "+n.getClass().getSimpleName()+" : " + printRangeCoordinates(n);

    /**
     * Print format each {@link Node} in the tree (prints to a single line) for example:
     *  <PRE>
     *  "@Deprecated...}" [CompilationUnit]
     *  \_______________/ \_______________/
     *    node summary      node class
     *  </PRE>
     * @see #printNodeSummary(Node)
     */
    public static Function<Node,String> SUMMARY_CLASS_FORMAT = n->
            "\""+ printNodeSummary(n)+"\" ["+n.getClass().getSimpleName()+"]";

    /** Create an ASCIITreePrinter with the default format */
    public ASCIITreePrinter(){
        this(SUMMARY_CLASS_RANGE_FORMAT);
    }

    /**
     * An ASCIITreePrinter with a specified nodeFormat for formatting each node
     * @param nodeFormat
     */
    public ASCIITreePrinter(Function<Node,String> nodeFormat){
        this.nodeFormat = nodeFormat;
    }

    /** DEFAULT format for printing each node (on a single line in the tree)*/
    public Function<Node, String> nodeFormat = SUMMARY_CLASS_RANGE_FORMAT;

    /**
     * Change the Node Format for printing the contents of each node to a line
     * @param nodeFormat
     * @return
     */
    public ASCIITreePrinter setNodeFormat(Function<Node, String> nodeFormat){
        this.nodeFormat = nodeFormat;
        return this;
    }

    /**
     * Build the output as a String and return it
     * @param rootNode the top AST {@link Node} to print the contents of
     * @return a String representing an ASCII tree
     */
    public String output(Node rootNode){
        return TNode.of( new TNode(rootNode)).output(this.nodeFormat);
    }

    /**
     *
     * @param rootNode
     * @param nodeFormat how to print out each node
     * @return a String representing an ASCII tree
     */
    public String output(Node rootNode, Function<Node, String> nodeFormat){
        return TNode.of( new TNode(rootNode)).output(nodeFormat);
    }

    /**
     * Underlying Nodes that will print out
     */
    private static class TNode{
        final Node node;
        public List<TNode> children = new ArrayList<>();

        /**
         * builds and returns a TNode and resolves
         * @param tn
         * @return
         */
        public static TNode of( TNode tn ){
            tn.node.stream(Node.TreeTraversal.DIRECT_CHILDREN).forEach(c-> {
                TNode child = new TNode(c);
                tn.children.add( child );
                of(child);
            } );
            return tn;
        }

        /**
         * Build a ROOT TNode that can contain children
         * @param rootNode
         */
        public TNode(Node rootNode){
            this.node = rootNode;
        }

        /**
         * Builds the Ascii tree into the buffer
         * @param nodeStringFunction
         * @param buffer
         * @param prefix
         * @param childrenPrefix
         */
        private void build(Function<Node,String> nodeStringFunction, StringBuilder buffer, String prefix, String childrenPrefix) {
            buffer.append(prefix);
            buffer.append( nodeStringFunction.apply(node) );
            buffer.append('\n');
            for (Iterator<TNode> it = children.iterator(); it.hasNext();) {
                TNode next = it.next();
                /* this is the more "open" format
                if (it.hasNext()) {
                    next.build(nodeStringFunction, buffer, childrenPrefix + "├── ", childrenPrefix + "│   ");
                } else {
                    next.build(nodeStringFunction, buffer, childrenPrefix + "└── ", childrenPrefix + "    ");
                }
                 */
                // this is the "dense"/compact format
                if (it.hasNext()) {
                    next.build(nodeStringFunction, buffer, childrenPrefix + "├─", childrenPrefix + "│ ");
                } else {
                    next.build(nodeStringFunction, buffer, childrenPrefix + "└─", childrenPrefix + "  ");
                }
            }
        }

        public String output(Function<Node,String> nodeToStringFunction) {
            StringBuilder buffer = new StringBuilder();
            build(nodeToStringFunction, buffer, "", "");
            return buffer.toString();
        }
    }

    /**
     * Breaks the single String into an array of String Lines
     * @param string a single String
     * @return
     */
    public static List<String> lines( String string ) {
        if( string == null ) {
            return Collections.emptyList();
        }
        BufferedReader br = new BufferedReader(
                new StringReader( string ) );

        List<String> strLine = new ArrayList<>();

        try {
            String line = br.readLine();
            while( line != null ) {
                strLine.add( line );
                line = br.readLine();
            }
            return strLine;
        }
        catch( IOException e ) {
            //this shouldnt happen
            throw new RuntimeException( "Error formatting Lines" );
        }
    }

    /**
     * Tries to print the Range of the Node n, if the Range is not present
     * (which happens in UnknownType of Lambda for instance) prints (-)
     */
    public static String printRange(Node n){
        if( n.getRange().isPresent() ){
            return n.getRange().get().toString();
        }
        //this sometimes happens (i.e. a Unknown type AST node has no text and no range)
        return "(-)";
    }

    /**
     * Prints range coordinates with (line,column)-(line,column) i.e.
     * (1,1)-(5,1) = line 1, column 1, to line 5 column 1
     *
     * @param n the node to print
     * @return String representing the line,column range coordinates
     */
    public static String printRangeCoordinates(Node n){
        if( n.getRange().isPresent() ){
            return printPosition(n.getRange().get().begin)+"-"+printPosition(n.getRange().get().end);
        }
        //this sometimes happens (i.e. a Unknown type AST node has no text and no range)
        return "(-)";
    }

    public static String printPosition(Position p){
        return "("+p.line+","+p.column+")";
    }

    /**
     * Prints an abbreviated view of a AST node (as to keep the content all on one line)
     * in the event the text is truncated, appends "..." and the last non-empty character
     * on the last non-empty line of the node.  i.e. for this ObjectCreationExpr AST which spans multiple lines:
     * <PRE>
     *     new Object(){
     *         int i=0;
     *     }
     * </PRE>
     * prints:
     * <PRE>"new Object() {...}"</PRE>
     *
     * @param n
     * @return
     */
    public static String printNodeSummary(Node n){
        String s = n.toString(PRINT_NO_COMMENTS).trim();
        if( s.isEmpty() ){
            return ""; //this happens, sometimes we have UnknownType (for Lambda) with NO text
        }
        List<String> lines = lines(s);
        if( lines.get(lines.size()-1).isEmpty() ){
            lines.remove(lines.size() -1);
        }
        if( lines.size() == 1 ){
            return lines.get(0); //its all on one line
        }
        String lastLine = lines.get(Math.max(lines.size()-1, 0));
        //returns the first line, then "..." then the last character on the last line; usually ( '}', ';' or ')' )
        return lines.get(0)+"..."+lastLine.charAt(lastLine.length()-1);
    }

    /**
     * The ASCIITreePrinter doesn't do comments by design
     */
    private static final PrettyPrinterConfiguration PRINT_NO_COMMENTS = new PrettyPrinterConfiguration()
            .setPrintComments(false).setPrintJavadoc(false);
}
