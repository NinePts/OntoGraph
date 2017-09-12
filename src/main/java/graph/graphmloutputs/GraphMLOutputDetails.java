/**
 * Copyright (c) Nine Points Solutions, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package graph.graphmloutputs;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import graph.models.EdgeDetailsModel;
import graph.models.EdgeFlagsModel;
import graph.models.NodeDetailsModel;
import graph.models.NoteDetailsModel;
import graph.models.PrefixModel;

/**
 * GraphMLOutputDetails contains the explicit GraphML XML to output
 * text and boxes (for the title and prefixes box), nodes (for classes and 
 * datatypes), edges (for subclass of, type of, connectives and properties) 
 * images (required in VOWL) and notes (defined as UMLNotes, used for 
 * enumerations and restrictions).
 * 
 * Note that when creating a node, text box, UMLNote, etc., the x,y 
 * coordinates in the GraphML don't matter. This is because they will be
 * changed/laid out by yEd.
 *
 */
public class GraphMLOutputDetails {
    
	// Frequently used strings
    protected static final String BLACK = "#000000";
	protected static final String CIRCLE = "circle";
	protected static final String CLOSE_DATA_XML = "</data>";
	protected static final String CLOSE_NODE_LABEL_XML = "</y:NodeLabel>";
	protected static final String CLOSE_NODE_XML = "</node>";
	protected static final String DASHED = "dashed";
	protected static final String DATA_KEY_XML = "  <data key=\"d6\">";
	protected static final String DIAMOND = "diamond";
    protected static final String DISJOINT = "disjoint";
	protected static final String DOTTED = "dotted";
	protected static final String ELLIPSE = "ellipse";
	protected static final String EMPTY_STRING = "";
	protected static final String FONT_XML = "fontSize=\"12\" fontStyle=\"plain\" hasBackgroundColor=\"false\" "
	        + "hasLineColor=\"false\" ";
	protected static final String GEOMETRY_HEIGHT_XML = "      <y:Geometry height=\"";
    protected static final String GRAFFOO = "graffoo";
    protected static final String INTER = "inter";
    protected static final String INTERNAL = "internal";
    protected static final String NEW_LINE = System.getProperty("line.separator");
	protected static final String NODE_ID_XML = "<node id=\"";
	protected static final String NODE_LABEL_CENTER_XML = "      <y:NodeLabel alignment=\"center\" autoSizePolicy="
	        + "\"content\" fontFamily=\"Dialog\" ";
	protected static final String NODE_LABEL_LEFT_XML = "      <y:NodeLabel alignment=\"left\" autoSizePolicy=\"content\""
	        + " fontFamily=\"Dialog\" ";
	protected static final String OWL_THING = "owl:Thing";
	protected static final String RECTANGLE = "rectangle";
	protected static final String ROUND_RECTANGLE = "roundRectangle";
	protected static final String ROUND_RECTANGLE_LOWER_CASE = "roundrectangle";
	protected static final String SHAPE_RECTANGLE_XML = "          <y:Shape type=\"rectangle\"/>";
	protected static final String SMALL_CIRCLE = "smallCircle";
	protected static final String SOLID = "solid";
    protected static final String SOURCE_ARROW = "sourceArrow";
	protected static final String SPACES_CLOSE_DATA_XML = "  </data>";
    protected static final String TARGET_ARROW = "targetArrow";
    protected static final String TEXT_COLOR = "textColor";
    protected static final String UML = "uml";
    protected static final String UN = "un";
    protected static final String VOWL = "vowl";
    protected static final String WHITE = "#FFFFFF";
	protected static final String WIDTH_XML = "\" width=\"";
    
    // GraphML node and arrow shapes, line types and colors as supported by yEd
    protected static final Map<String,String> arrowShapes = createArrowMap();
    protected static final Map<String,String> lineTypes = createLineMap();
    protected static final Map<String,String> nodeShapes = createNodeMap();

    // Not meant to be instantiated
    protected GraphMLOutputDetails() {
      throw new IllegalAccessError("GraphMLOutputDetails is a utility class and should not be instantiated.");
    }
    
    // Mapping to yEd shape labels
    protected static Map<String, String> createNodeMap() {
        Map<String,String> nodeMap = new HashMap<>();
        nodeMap.put(CIRCLE, CIRCLE);
        nodeMap.put(SMALL_CIRCLE, SMALL_CIRCLE);
        nodeMap.put(DIAMOND, DIAMOND);
        nodeMap.put(ELLIPSE, ELLIPSE);
        nodeMap.put("hexagon", "hexagon");
        nodeMap.put("parallelogramRight", "parallelogram");
        nodeMap.put("parallelogramLeft", "parallelogram2");
        nodeMap.put(ROUND_RECTANGLE, ROUND_RECTANGLE_LOWER_CASE);
        nodeMap.put("squareRectangle", RECTANGLE);
        nodeMap.put("none", RECTANGLE);
        return nodeMap;
    }
    
    // Mapping to yEd arrow labels
    protected static Map<String, String> createArrowMap() {
        Map<String,String> arrowMap = new HashMap<>();
        arrowMap.put("angleBracket", "plain");
        arrowMap.put("backslash", "skewed_dash");
        arrowMap.put("circleSolid", CIRCLE);
        arrowMap.put("circleEmpty", "transparent_circle");
        arrowMap.put("diamondSolid", DIAMOND);
        arrowMap.put("diamondEmpty", "white_diamond");
        arrowMap.put("triangleSolid", "delta");
        arrowMap.put("triangleEmpty", "white_delta");
        arrowMap.put("none", "none");
        return arrowMap;
    }
    
    // Mapping to yEd line types
    protected static Map<String, String> createLineMap() {
        Map<String,String> lineMap = new HashMap<>();
        lineMap.put(SOLID, "line");
        lineMap.put(DASHED, DASHED);
        lineMap.put(DOTTED, DOTTED);
        lineMap.put("dashedDotted", "dashed_dotted");
        lineMap.put("none", "none");
        lineMap.put("Graffoo Connectives", DOTTED);
        return lineMap;
    }

    /**
	 * Creates a GraphML grouping box with either the graph information (title, ontology URI and 
	 * generation date) or prefixes and their namespaces.
	 * 
	 * @param  text1 String contains either an empty string or the URIs that correspond to the prefixes 
	 *              depending on whether this is a graph info box or prefixes box
	 * @param  text2 String contains either the graph information OR the prefixes depending on whether
	 *              this is a graph info box or prefixes box
	 * @return GraphML String output
	 * 
	 */
	public static String addBox(final String text1, final String text2) {
		
		StringBuilder sb = new StringBuilder();
		
		String id;
		String title;
		String backgroundColor;
		if (text2.isEmpty()) {
			// Title box
			id = "title";
			title = "Graph Information   ";
			backgroundColor = "#99CCFF";
		} else {
			// Prefixes box
			id = "prefixes";
			title = "Prefixes   ";
			backgroundColor = "#B7B69E";
		}
		
		sb.append(NODE_ID_XML + id + "\" yfiles.foldertype=\"group\">" + NEW_LINE)
	      .append("  <data key=\"d4\"/>" + NEW_LINE)
	      .append(DATA_KEY_XML + NEW_LINE)
	      .append("    <y:ProxyAutoBoundsNode>" + NEW_LINE)
	      .append("      <y:Realizers active=\"0\">" + NEW_LINE)
	      .append("        <y:GroupNode>" + NEW_LINE)
	      .append("          <y:Geometry height=\"112.2\" width=\"449.12\" x=\"123.59\" y=\"878.39\"/>" + NEW_LINE)
	      .append("          <y:Fill color=\"#FFFFFF\" transparent=\"false\"/>" + NEW_LINE)
	      .append("          <y:BorderStyle color=\"#000000\" type=\"line\" width=\"1.0\"/>" + NEW_LINE)
	      .append("          <y:NodeLabel alignment=\"right\" autoSizePolicy=\"node_width\" backgroundColor=\""
	    		  + backgroundColor + "\" borderDistance=\"0.0\" fontFamily=\"Dialog\" fontSize=\"15\" fontStyle=\"bold\" "
	              + "hasLineColor=\"false\" height=\"21.67\" horizontalTextPosition=\"center\" iconTextGap=\"4\" "
	              + "modelName=\"internal\" modelPosition=\"t\" textColor=\"#000000\" verticalTextPosition=\"bottom\" "
	              + "visible=\"true\" width=\"449.12\" x=\"0.0\" y=\"0.0\">" + title + CLOSE_NODE_LABEL_XML + NEW_LINE)
	      .append(SHAPE_RECTANGLE_XML + NEW_LINE)
	      .append("          <y:DropShadow color=\"#D2D2D2\" offsetX=\"4\" offsetY=\"4\"/>" + NEW_LINE)
	      .append("          <y:State closed=\"false\" closedHeight=\"74.51\" closedWidth=\"387.83\" "
	    		  + "innerGraphDisplayEnabled=\"false\"/>" + NEW_LINE)
	      .append("          <y:Insets bottom=\"15\" bottomF=\"15.0\" left=\"15\" leftF=\"15.0\" right=\"15\" "
	    		  + "rightF=\"15.0\" top=\"15\" topF=\"15.0\"/>" + NEW_LINE)
	      .append("          <y:BorderInsets bottom=\"0\" bottomF=\"0.0\" left=\"4\" leftF=\"4.47\" "
	    		  + "right=\"0\" rightF=\"0.0\" top=\"0\" topF=\"0.0\"/>" + NEW_LINE)
	      .append("        </y:GroupNode>" + NEW_LINE)
	      .append("        <y:GroupNode>" + NEW_LINE)
	      .append("          <y:Geometry height=\"50.0\" width=\"50.0\" x=\"-25.0\" y=\"-25.0\"/>" + NEW_LINE)
	      .append("          <y:Fill color=\"#F2F0D8\" transparent=\"false\"/>" + NEW_LINE)
	      .append("          <y:BorderStyle color=\"#000000\" type=\"line\" width=\"1.0\"/>" + NEW_LINE)
	      .append("          <y:NodeLabel alignment=\"right\" autoSizePolicy=\"node_width\" backgroundColor=\"#B7B69E\""
	              + " borderDistance=\"0.0\" fontFamily=\"Dialog\" fontSize=\"15\" fontStyle=\"plain\" hasLineColor="
	              + "\"false\" height=\"21.67\" horizontalTextPosition=\"center\" iconTextGap=\"4\" modelName="
	              + "\"internal\" modelPosition=\"t\" textColor=\"#000000\" verticalTextPosition=\"bottom\" visible="
	              + "\"true\" width=\"63.76\" x=\"-6.88\" y=\"0.0\"> " + CLOSE_NODE_LABEL_XML + NEW_LINE)
	      .append(SHAPE_RECTANGLE_XML + NEW_LINE)
	      .append("          <y:DropShadow color=\"#D2D2D2\" offsetX=\"4\" offsetY=\"4\"/>" + NEW_LINE)
	      .append("          <y:State closed=\"true\" closedHeight=\"50.0\" closedWidth=\"50.0\" "
	    		  + "innerGraphDisplayEnabled=\"false\"/>" + NEW_LINE)
	      .append("          <y:Insets bottom=\"5\" bottomF=\"5.0\" left=\"5\" leftF=\"5.0\" right=\"5\" "
	    		  + "rightF=\"5.0\" top=\"5\" topF=\"5.0\"/>" + NEW_LINE)
	      .append("          <y:BorderInsets bottom=\"0\" bottomF=\"0.0\" left=\"0\" leftF=\"0.0\" right=\"0\" "
	    		  + "rightF=\"0.0\" top=\"0\" topF=\"0.0\"/>" + NEW_LINE)
	      .append("        </y:GroupNode>" + NEW_LINE)
	      .append("      </y:Realizers>" + NEW_LINE)
	      .append("    </y:ProxyAutoBoundsNode>" + NEW_LINE)
	      .append(SPACES_CLOSE_DATA_XML + NEW_LINE)
	      .append("  <graph edgedefault=\"directed\" id=\"" + id + ":\">" + NEW_LINE)
	      .append("    <node id=\"" + id + "::n0\">" + NEW_LINE)
	      .append("    " + DATA_KEY_XML + NEW_LINE)
	      .append("        <y:ShapeNode>" + NEW_LINE)
	      .append("          <y:Geometry height=\"25.0\" width=\"150.0\" x=\"310.0\" y=\"934.20\"/>" + NEW_LINE)
	      .append("          <y:Fill hasColor=\"false\" transparent=\"false\"/>" + NEW_LINE)
	      .append("          <y:BorderStyle hasColor=\"false\" type=\"line\" width=\"1.0\"/>" + NEW_LINE)
	      .append("          <y:NodeLabel alignment=\"left\" autoSizePolicy=\"content\" borderDistance=\"0.0\" "
	    		  + "fontFamily=\"Dialog\" fontSize=\"16\" fontStyle=\"bold\" hasBackgroundColor=\"false\" "
	    		  + "hasLineColor=\"false\" height=\"90.53\" horizontalTextPosition=\"center\" iconTextGap=\"4\" "
	    		  + "modelName=\"internal\" modelPosition=\"c\" textColor=\"#000000\" verticalTextPosition=\"bottom\" "
	    		  + "visible=\"true\" width=\"140.44\" x=\"-5.04\" y=\"-19.145\">")
	      .append(text1 + CLOSE_NODE_LABEL_XML + NEW_LINE)
		  .append(SHAPE_RECTANGLE_XML + NEW_LINE)
		  .append("        </y:ShapeNode>" + NEW_LINE)
		  .append("    " + SPACES_CLOSE_DATA_XML + NEW_LINE)
		  .append("    </node>" + NEW_LINE);
		
		if (!text2.isEmpty()) {
			sb.append("    <node id=\"" + id + "::n1\">" + NEW_LINE)
			  .append("    " + DATA_KEY_XML + NEW_LINE)
	          .append("        <y:ShapeNode>" + NEW_LINE)
	          .append("          <y:Geometry height=\"25.0\" width=\"430.0\" x=\"148.09\" y=\"934.20\"/>" + NEW_LINE)
	          .append("          <y:Fill hasColor=\"false\" transparent=\"false\"/>" + NEW_LINE)
	          .append("          <y:BorderStyle hasColor=\"false\" type=\"line\" width=\"1.0\"/>" + NEW_LINE)
	          .append("          <y:NodeLabel alignment=\"left\" autoSizePolicy=\"content\" borderDistance=\"0.0\" "
	    		  + "fontFamily=\"Dialog\" fontSize=\"16\" fontStyle=\"plain\" hasBackgroundColor=\"false\" "
	    		  + "hasLineColor=\"false\" height=\"90.53\" horizontalTextPosition=\"center\" iconTextGap=\"4\" "
	    		  + "modelName=\"internal\" modelPosition=\"c\" textColor=\"#000000\" verticalTextPosition=\"bottom\" "
	    		  + "visible=\"true\" width=\"140.44\" x=\"-66.3\" y=\"-19.145\">")
	          .append(text2 + CLOSE_NODE_LABEL_XML + NEW_LINE)
	      .append(SHAPE_RECTANGLE_XML + NEW_LINE)
	      .append("        </y:ShapeNode>" + NEW_LINE)
	      .append("    " + SPACES_CLOSE_DATA_XML + NEW_LINE)
	      .append("    </node>" + NEW_LINE);
		}
		
	    sb.append("  </graph>" + NEW_LINE)
	      .append(CLOSE_NODE_XML + NEW_LINE);
	    
	    return sb.toString();
	}

	/**
	 * Add a node in GraphML format.
	 * 
	 * @param  nodeDetails NodeDetailsModel with info such as nodeShape, fillColor, visualization, ...
	 * @param  className String specifying the class (which is used as the node's id)
	 * @param  label String specifying the node's label
	 * @return String GraphML output
	 * 
	 */
    public static String addNode(NodeDetailsModel nodeDetails, final String className, 
    		final String label) {
    
    	StringBuilder sb = new StringBuilder();

        sb.append(NODE_ID_XML + className + "\">" + NEW_LINE)
          .append(DATA_KEY_XML + NEW_LINE)
          .append("    <y:ShapeNode>" + NEW_LINE)
          .append(GEOMETRY_HEIGHT_XML + nodeDetails.getHeight() + WIDTH_XML 
            		+ nodeDetails.getWidth() + "\" x=\"385.3\" y=\"187.0\"/>" + NEW_LINE)
          .append("      <y:Fill color=\"" + nodeDetails.getFillColor() + "\" transparent=\"false\"/>" 
            		+ NEW_LINE)
          .append("      <y:BorderStyle color=\"" + nodeDetails.getBorderColor() + "\" type=\"" 
        		  + lineTypes.get(nodeDetails.getBorderType()) + WIDTH_XML + nodeDetails.getBorderWidth()
                  + "\"/>" + NEW_LINE)
          .append(NODE_LABEL_CENTER_XML + "fontSize=\"16\" fontStyle=\"plain\" hasBackgroundColor=\"false\" "
                  + "hasLineColor=\"false\" height=\"22.84\" modelName=\"" + nodeDetails.getModelName()
                  + "\" modelPosition=\"" + nodeDetails.getModelPosition() + "\" textColor=\"" 
                  + nodeDetails.getTextColor() + "\" visible=\"true\" width=\"44.84\" x=\"18.56\" y=\"10.58\">"
                  + label + CLOSE_NODE_LABEL_XML + NEW_LINE)
          .append("      <y:Shape type=\"" + nodeShapes.get(nodeDetails.getNodeShape()) + "\"/>" + NEW_LINE)
          .append("    </y:ShapeNode>" + NEW_LINE)
          .append(SPACES_CLOSE_DATA_XML + NEW_LINE)
          .append(CLOSE_NODE_XML + NEW_LINE);
        
        return sb.toString();
    }

    /**
	 * Add a note in GraphML format.
	 * 
	 * @param  visualization defining the visualization type
	 * @param  noteDetails NoteDetailsModel holding the line type, height and width
	 * @param  noteName String
	 * @param  noteText String
	 * @return String GraphMLString output
	 * 
	 */
	public static String addNote(final String visualization, NoteDetailsModel noteDetails, 
			final String noteName, final String noteText) {
		
		StringBuilder sb = new StringBuilder();
	
	    sb.append(NODE_ID_XML + noteName + "\">" + NEW_LINE)
	      .append("  <data key=\"d4\"/>" + NEW_LINE)
	      .append("  <data key=\"d5\"><![CDATA[UMLNote]]></data>" + NEW_LINE)
	      .append(DATA_KEY_XML + NEW_LINE)
	      .append("    <y:UMLNoteNode>" + NEW_LINE)
	      .append(GEOMETRY_HEIGHT_XML + Double.toString(noteDetails.getHeight()) 
	      						+ WIDTH_XML + Double.toString(noteDetails.getWidth()) 
	      						+ "\" x=\"572.71\" y=\"764.98\"/>" + NEW_LINE)
	      .append("      <y:Fill color=\"#FFFFFF\" transparent=\"false\"/>" + NEW_LINE)
	      .append("      <y:BorderStyle color=\"#000000\" type=\"" + lineTypes.get(noteDetails.getLineType())
	      						+ "\" width=\"1.0\"/>" + NEW_LINE)
	      .append(NODE_LABEL_LEFT_XML + "fontSize=\"" + getFontSize(visualization) + "\" "
	    		  				+ "fontStyle=\"plain\" hasBackgroundColor=\"false\" "
	    		  				+ "hasLineColor=\"false\" height=\"" + Integer.toString(noteDetails.getHeight())
	    		  				+ ".0\" width=\"" + Integer.toString(noteDetails.getWidth()) + ".0\" "
	    		  				+ "horizontalTextPosition=\"center\" iconTextGap=\"4\" "
	    		  				+ "modelName=\"internal\" modelPosition=\"c\" textColor=\"#000000\" "
	    		  				+ "verticalTextPosition=\"bottom\" visible=\"true\" x=\"19.156\" "
	    		  				+ "y=\"16.5085\">" + noteText + "  ")
	      .append(CLOSE_NODE_LABEL_XML + NEW_LINE)
	      .append("    </y:UMLNoteNode>" + NEW_LINE)
	      .append(CLOSE_DATA_XML + NEW_LINE)
	      .append(CLOSE_NODE_XML + NEW_LINE);
	    
		return sb.toString();
	}

	/**
	 * Add a specific edge in GraphML format.
	 * 
	 * @param  edgeDetails EdgeDetailsModel with info such as sourceArrow, edgeLabel, ...
	 * @param  source String specifying the source class name
	 * @param  target String specifying the target class name
	 * @param  idPrefix String
	 * @param  edgeFlags EdgeFlagsModel indicating whether the property is defined as a functional/
	 *               inverse functional, asymmetric/symmetric, reflexive/irreflexive or transitive property
	 * @return String GraphMLString output
	 * 
	 */
    public static String addEdge(EdgeDetailsModel edgeDetails, final String source, 
    		final String target, final String idPrefix, EdgeFlagsModel edgeFlags) {

    	String visualization = edgeDetails.getVisualization();
	    StringBuilder sb = new StringBuilder();
	    
	    // For collapsed properties, the distinguishing prefix is actually the source + target strings
	    // So, there is no need to combine them again
	    String propName = idPrefix;
	    if (!propName.contains(source) || !propName.contains(target)) {
	    	propName = idPrefix + source + target;
	    }
	    
	    // VOWL properties are shown as sitting above their edge line, but this is not how the
	    //    implementation in the spec is drawn (which is correct?)
	    sb.append("<edge id=\"" + propName + "\" source=\"" + source + "\" target=\"" + target + "\">" + NEW_LINE)
	        .append("  <data key=\"d10\">" + NEW_LINE)
	        .append("    <y:PolyLineEdge>" + NEW_LINE)
	        .append("      <y:Path sx=\"0.0\" sy=\"0.0\" tx=\"0.0\" ty=\"0.0\"/>" + NEW_LINE)
	        .append("      <y:LineStyle color=\"" + edgeDetails.getLineColor() + "\" type=\"" 
	        		+ lineTypes.get(edgeDetails.getLineType()) + WIDTH_XML + edgeDetails.getLineWidth() + "\"/>" 
	                + NEW_LINE)
	        .append("      <y:Arrows source=\"" + arrowShapes.get(edgeDetails.getSourceArrow()) + "\" target=\"" 
	                + arrowShapes.get(edgeDetails.getTargetArrow()) + "\"/>" + NEW_LINE)
	        .append("      <y:EdgeLabel alignment=\"center\" backgroundColor=\"" 
	                + edgeDetails.getEdgeLabelBackground() + "\" distance=\"2.0\" fontFamily=\"Dialog\" "
	                + "fontSize=\"" + getFontSize(visualization) + "\" fontStyle=\"plain\" hasLineColor=\"false\" "
	                + "height=\"22.84\" modelName=\"centered\" modelPosition=\"center\" preferredPlacement"
	                + "=\"anywhere\" ratio=\"0.5\" textColor=\"" + edgeDetails.getEdgeLabelColor() 
	                + "\" width=\"76.16\" x=\"102.14\" y=\"-11.42\" ");
	 
	    if (edgeDetails.getEdgeLabel().isEmpty()) {
	    	sb.append("visible=\"false\"> ");
	    } else {
	    	sb.append("visible=\"true\">"); 
	    	String edgeLabel = edgeDetails.getEdgeLabel();
	    	String flagsText = getEdgeFlagsText(edgeFlags);
	    	if (!flagsText.isEmpty()) {
	    		edgeLabel += NEW_LINE + "(" + flagsText.replaceAll(" ", ", ");
	    		edgeLabel = edgeLabel.substring(0, edgeLabel.length() - 2) + ")";
	    	}
	        sb.append(edgeLabel);
	    }
	    
	    sb.append("</y:EdgeLabel>" + NEW_LINE)
	        .append("      <y:BendStyle smoothed=\"false\"/>" + NEW_LINE)
	        .append("    </y:PolyLineEdge>" + NEW_LINE)
	        .append(SPACES_CLOSE_DATA_XML + NEW_LINE)
	        .append("</edge>" + NEW_LINE);
	    
	    return sb.toString();
    }
    
    /**
     * Add an image with a specific node/image name.
     * 
     * @param  imageName String
     * @param  imageType String indicating that either a "disjoint", "union", "intersection" or "complement"
     * 					image is added
     * @return graphML String
     * 
     */
    public static String addImage(final String imageName, final String imageType) {
    	
    	StringBuilder sb = new StringBuilder();
    	
    	sb.append(NODE_ID_XML + imageName + "\">" + NEW_LINE)
          .append(DATA_KEY_XML + NEW_LINE)
          .append("    <y:ImageNode>" + NEW_LINE)
          .append("      <y:Geometry height=\"72.53\" width=\"76.0\" x=\"-163.76\" y=\"557.0\"/>" + NEW_LINE)
          .append("      <y:Fill hasColor=\"false\" transparent=\"false\"/>" + NEW_LINE)
          .append("      <y:BorderStyle hasColor=\"false\" type=\"line\" width=\"1.0\"/>" + NEW_LINE)
          .append(NODE_LABEL_CENTER_XML + FONT_XML 
        		  + "hasText=\"false\" height=\"4.0\" horizontalTextPosition=\"center\" iconTextGap=\"4\" "
        		  + "modelName=\"sandwich\" modelPosition=\"s\" textColor=\"#000000\" verticalTextPosition=\"bottom\" "
        		  + "visible=\"false\" width=\"4.0\" x=\"36.0\" y=\"76.53\"/>" + NEW_LINE)
          .append("      <y:Image alphaImage=\"true\" refid=\"" + imageType + "\"/>" + NEW_LINE)
          .append("    </y:ImageNode>" + NEW_LINE)
          .append(SPACES_CLOSE_DATA_XML + NEW_LINE)
          .append(CLOSE_NODE_XML + NEW_LINE);
          
        return sb.toString();  
    }
	
	/**
	 * Add a UML node in graphML format.
	 * 
	 * @param  entityName String specifying the class or individual id
	 * @param  entityLabel String
	 * @param  attributes List<String> datatype properties where the class is the domain
	 * @param  width double specifying width of box
	 * @param  height double specifying height of box
	 * @return GraphML String
	 * 
	 */
	public static String addUMLNode(final String entityName, final String entityLabel, 
			List<String> attributes, double width, double height) {
		
	    StringBuilder sb = new StringBuilder();
        
        sb.append(NODE_ID_XML + entityName + "\">" + NEW_LINE)
          .append("  <data key=\"d5\"/>" + NEW_LINE)
          .append(DATA_KEY_XML + NEW_LINE)
          .append("    <y:GenericNode configuration=\"com.yworks.entityRelationship.big_entity\">" + NEW_LINE)
          .append(GEOMETRY_HEIGHT_XML + height + "\" width=\"" + width + "\" x=\"385.30\" y=\"187.01\"/>" + NEW_LINE)
          .append("      <y:Fill color=\"#FFFFFF\" transparent=\"false\"/>" + NEW_LINE)
          .append("      <y:BorderStyle color=\"#000000\" type=\"line\" " + "width=\"1.0\"/>" + NEW_LINE)
          .append("      <y:NodeLabel alignment=\"center\" autoSizePolicy=\"content\" backgroundColor=\"#FFFFFF\" " 
                  + "configuration=\"com.yworks.entityRelationship.label.name\" fontFamily=\"Dialog\" fontSize=\"12\" "
                  + "fontStyle=\"plain\" hasLineColor=\"false\" height=\"18.13\" modelName=\"internal\" modelPosition"
                  + "=\"t\" textColor=\"#000000\" visible=\"true\" width=\"36.67\" x=\"21.67\" y=\"4.0\">")
          .append(entityLabel + CLOSE_NODE_LABEL_XML + NEW_LINE)
          .append("      <y:NodeLabel alignment=\"left\" autoSizePolicy=\"content\" configuration=\"com.yworks"
                  + ".entityRelationship.label.attributes\" fontFamily=\"Dialog\" fontSize=\"12\" fontStyle=\"plain\" "
                  + "hasBackgroundColor=\"false\" hasLineColor=\"false\" height=\"46.40\" modelName=\"custom\" "
                  + "textColor=\"#000000\" visible=\"true\" width=\"65.54\" x=\"2.0\" y=\"30.13\">");
        
        if (attributes != null && !attributes.isEmpty()) {
            for (String attr : attributes) {
                sb.append(attr + NEW_LINE);
            }
        } else {
        	sb.append(NEW_LINE);
        }
        
        sb.append("        <y:LabelModel>" + NEW_LINE)
          .append("          <y:ErdAttributesNodeLabelModel/>" + NEW_LINE)
          .append("        </y:LabelModel>" + NEW_LINE)
          .append("        <y:ModelParameter>" + NEW_LINE)
          .append("          <y:ErdAttributesNodeLabelModelParameter/>" + NEW_LINE)
          .append("        </y:ModelParameter>" + NEW_LINE)
          .append("      " + CLOSE_NODE_LABEL_XML + NEW_LINE)
          .append("      <y:StyleProperties>" + NEW_LINE)
          .append("          <y:Property class=\"java.lang.Boolean\" name=\"y.view.ShadowNodePainter.SHADOW_PAINTING"
                  + "\" value=\"false\"/>" + NEW_LINE)
          .append("      </y:StyleProperties>" + NEW_LINE)
          .append("    </y:GenericNode>" + NEW_LINE)
          .append(SPACES_CLOSE_DATA_XML + NEW_LINE)
          .append("</node>" + NEW_LINE);
        
        return sb.toString();
	}

	/**
     * Adds finishing brackets for the GraphML file.
     *
     * @return String GraphML output
     * 
     */
    public static String closeGraph() {
    	
        StringBuilder output = new StringBuilder();
        output.append("</graph>" + NEW_LINE)
            .append("<data key=\"d7\">" + NEW_LINE)
            .append("   <y:Resources/>" + NEW_LINE)
            .append(CLOSE_DATA_XML + NEW_LINE)
            .append("</graphml>");
        
        return output.toString();
    }
    
	/**
     * Adds finishing brackets and the necessary image (right-pointing arrow) for a UML GraphML file.
     *
     * @return String GraphML output
     * 
     */
    public static String closeVOWLGraph() throws IOException {
    	
        StringBuilder output = new StringBuilder();
        output.append("</graph>" + NEW_LINE)
            .append("<data key=\"d7\">" + NEW_LINE)
            .append("   <y:Resources>" + NEW_LINE)
            .append(addImageResource("complement"))
            .append(addImageResource("intersection"))
            .append(addImageResource("union"))
            .append(addImageResource(DISJOINT))
            .append("   </y:Resources>" + NEW_LINE)
            .append(CLOSE_DATA_XML + NEW_LINE)
            .append("</graphml>");
        
        return output.toString();
    }
    
    /**
     * Creates the two text lists for any prefixes and their corresponding URIs.
     * 
     * @param  prefixes List<PrefixModel>
     * @return GraphML String
     * 
     */
    public static String createPrefixesBox(List<PrefixModel> prefixes) {
    	
    	// Create a list of prefixes
    	StringBuilder sb1 = new StringBuilder();
    	// Create a list of the URIs associated with each prefix
    	StringBuilder sb2 = new StringBuilder();

		// Add prefixes 
	    for (PrefixModel pm : prefixes) {
	        sb1.append(pm.getPrefixName() + NEW_LINE);
	    }
		// Add prefix URLs
	    for (PrefixModel pm : prefixes) {
	        sb2.append(pm.getUrl() + NEW_LINE);
	    }
	    
    	return addBox(sb1.toString(), sb2.toString());
    }
    
    /**
     * Creates the text for the title/graph info box.
     * 
     * @param  title String
     * @param  ontologyURI String
     * @return GraphML String
     * 
     */
    public static String createTitleBox(final String title, final String ontologyURI) {
    	
    	// Create the contents of the title box (the graph info)
    	StringBuilder sb = new StringBuilder();
    	sb.append("Title:  " + title + NEW_LINE + NEW_LINE);
    	sb.append("Ontology URI:  " + ontologyURI + NEW_LINE + NEW_LINE);
    	sb.append("Generated:  " + new java.util.Date().toString());
    	
    	return addBox(sb.toString(), "");
    }
    
    /**
	 * Returns the text identifying if the property is asymmetric, functional, inverse functional,
	 * symmetric, reflexive, irreflexive and/or transitive.
	 * 
	 * @param  edgeFlags EdgeFlagsModel
	 * @return String text outlining the applicable flags, separated by spaces
	 * 
	 */
	public static String getEdgeFlagsText(EdgeFlagsModel edgeFlags) {

    	StringBuilder sb = new StringBuilder();
    	if (edgeFlags.isFunctional()) {
    		sb.append("functional ");
    	}
    	if (edgeFlags.isInverseFunctional()) {
    		sb.append("inverseFunctional ");
    	}
    	if (edgeFlags.isReflexive()) {
    		sb.append("reflexive ");
    	}
    	if (edgeFlags.isIrreflexive()) {
    		sb.append("irreflexive ");
    	}
    	if (edgeFlags.isAsymmetric()) {
    		sb.append("asymmetric ");
    	}
    	if (edgeFlags.isSymmetric()) {
    		sb.append("symmetric ");
    	}
    	if (edgeFlags.isTransitive()) {
    		sb.append("transitive ");
    	}
    	
    	return sb.toString();
	}
	
	/**
	 * Gets an edge/line width as appropriate for the visualization
	 * 
	 * @param  visualization String
	 * @return lineWidth String
	 * 
	 */
	public static String getLineWidth(final String visualization) {

		String lineWidth = "1.0";
	    if (VOWL.equals(visualization)) {
	    	lineWidth = "2.0";
	    }
	    
	    return lineWidth;
	}
	
	/**
	 * Gets the length of the longest string in an array as a double.
	 * 
	 * @param  list Array of Strings
	 * @param  openingLine String defining any introductory text for the list (it may be an empty string)
	 * @return largest length as integer
	 * 
	 */
	public static int getMaxLength(List<String> list, final String openingLine) {
	    
	    int max = 0;
	    for (String s : list) {
	        if (s.length() > max) {
	            max = s.length();
	        }
	    }
	    
	    if (openingLine.length() > max) {
	    	return openingLine.length();
	    } else {
	    	return max;
	    }
	}

	/**
	 * Returns the appropriate modelName, modelPosition, border width, width, height, ... for a node,
	 * which is based on a visualization
	 * 
	 * @param  nodeDetails NodeDetailsModel with info such as nodeShape, fillColor, visualization, ... 
	 *               Note that some of the values have defaults (such as nodeShape), and may be overwritten. 
	 *               The model is returned with the appropriate values based on the visualization. 
	 * @param  nodeName String defining the class/datatype name (beginning with a prefix and ":")
	 * @param  label String providing the class/datatype label 
	 * @param  ontologyPrefix String that is the prefix of the owl:Ontology URI\
	 * @return NodeDetailsModel
	 *                         
	 */
	public static NodeDetailsModel getNodeDetails(final String ontologyPrefix,
			NodeDetailsModel nodeDetails, final String nodeName, final String label) {
		
		// Defaults
		nodeDetails.setModelName(INTERNAL);
		nodeDetails.setModelPosition("c");
	    
	    if (VOWL.equals(nodeDetails.getVisualization())) {
	        // Bigger borders, equally sized shapes
	    	nodeDetails.setNodeShape("ellipse");
	    	nodeDetails.setWidth("150.0");
	        nodeDetails.setHeight("150.0");
	        
	        // Different formatting for owl:Thing
	        if (nodeName.startsWith(OWL_THING) || "Thing".equals(nodeName)
	        		|| nodeName.startsWith("Thing*")) {
	            nodeDetails.setTextColor(BLACK);
	            nodeDetails.setFillColor(WHITE);
	            nodeDetails.setBorderType(DASHED);
	            nodeDetails.setWidth("90.0");
	            nodeDetails.setHeight("90.0");
	        } else {
	            // Different color for classes that are external to the owl:Ontology 
	        	//    and not owl:Thing. Note that blank nodes are NOT external.
	            if (nodeName.contains(":") && (EMPTY_STRING.equals(ontologyPrefix) || 
	            		!nodeName.startsWith(ontologyPrefix))) {
	                nodeDetails.setFillColor("#3366CC");
	                nodeDetails.setTextColor(WHITE);
	            } else {
	                nodeDetails.setFillColor("#AACCFF");
	                nodeDetails.setBorderType(SOLID);
	                nodeDetails.setTextColor(BLACK);
	            }
	        }
	    } else {
	    	// If not VOWL ...
	        modifyNodeDetailsForNodeShape(nodeDetails, label);
	    }
	    
	    return nodeDetails;
	}

	/**
	 * Modifies the display settings for a node, taking into account node shapes that are unique to OntoGraph
	 * (such as "smallCircle" or "none"), the length of the label, and adjusting the height/width for circles.
	 * 
	 * @param  nodeDetails NodeDetailsModel with info such as nodeShape, fillColor, visualization, ...
	 *                  The model may be updated by the processing in this method.
	 * @param label String
	 * 
	 */
	public static void modifyNodeDetailsForNodeShape(NodeDetailsModel nodeDetails, 
			final String label) {

    	String nodeShape = nodeDetails.getNodeShape();
        // Change position and size of text if the nodeShape is "smallCircle"
        if (SMALL_CIRCLE.equals(nodeShape)) {
        	nodeDetails.setHeight("20.0");
        	nodeDetails.setWidth("20.0");
            nodeDetails.setModelName("eight_pos");
            nodeDetails.setModelPosition("e");
            nodeDetails.setNodeShape(ELLIPSE);
        } else {
        	int width = label.length() * 13;
			nodeDetails.setWidth(Integer.toString(width) + ".0");
        	// Height is tied to width if the nodeType is a circle
        	if (CIRCLE.equals(nodeShape)) {
        		nodeDetails.setNodeShape(ELLIPSE);
        		if (width <= 50) {
        			nodeDetails.setWidth("50.0");
        			nodeDetails.setHeight("50.0");
        		} else {
        			nodeDetails.setHeight(Integer.toString(width) + ".0");
        		}
        	} else if ("none".equals(nodeShape)) {
            		nodeDetails.setNodeShape("squareRectangle");
            		nodeDetails.setFillColor(WHITE);
            		nodeDetails.setBorderColor(WHITE);
        			nodeDetails.setHeight("20.0");
        	} else {
        		nodeDetails.setHeight("50.0");
        	} 
        }
	}

	/**
	 * Builds initial XML of a GraphML file.
	 *
	 * @return String GraphML output 
	 * 
	 */
	public static String setUpGraph() {
		
	    StringBuilder output = new StringBuilder();
	    // Create graphml header
	    output.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>" + NEW_LINE
	                + "<graphml xmlns=\"http://graphml.graphdrawing.org/xmlns\" "
	                + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
	                + "xmlns:sys=\"http://www.yworks.com/xml/yfiles-common/markup/primitives/2.0\" "
	                + "xmlns:x=\"http://www.yworks.com/xml/yfiles-common/markup/2.0\" "
	                + "xmlns:y=\"http://www.yworks.com/xml/graphml\" "
	                + "xmlns:yed=\"http://www.yworks.com/xml/yed/3\" "
	                + "xsi:schemaLocation=\"http://graphml.graphdrawing.org/xmlns "
	                + "http://www.yworks.com/xml/schema/graphml/1.1/ygraphml.xsd\"> " + NEW_LINE)
	        .append(NEW_LINE)
	        .append("<key attr.name=\"Description\" attr.type=\"string\" for=\"graph\" id=\"d0\"/>"
	                + "<key for=\"port\" id=\"d1\" yfiles.type=\"portgraphics\"/>" + NEW_LINE
	                + "<key for=\"port\" id=\"d2\" yfiles.type=\"portgeometry\"/>" + NEW_LINE
	                + "<key for=\"port\" id=\"d3\" yfiles.type=\"portuserdata\"/>" + NEW_LINE
	                + "<key attr.name=\"url\" attr.type=\"string\" for=\"node\" id=\"d4\"/>" + NEW_LINE
	                + "<key attr.name=\"description\" attr.type=\"string\" for=\"node\" id=\"d5\"/>" + NEW_LINE
	                + "<key for=\"node\" id=\"d6\" yfiles.type=\"nodegraphics\"/>" + NEW_LINE
	        		+ "<key for=\"graphml\" id=\"d7\" yfiles.type=\"resources\"/>" + NEW_LINE
	                + "<key attr.name=\"url\" attr.type=\"string\" for=\"edge\" id=\"d8\"/>" + NEW_LINE
	                + "<key attr.name=\"description\" attr.type=\"string\" for=\"edge\" id=\"d9\"/>" + NEW_LINE
	                + "<key for=\"edge\" id=\"d10\" yfiles.type=\"edgegraphics\"/>" + NEW_LINE)
	        .append(NEW_LINE)
	        .append("<graph edgedefault=\"directed\" id=\"G\">" + NEW_LINE
	                + "  <data key=\"d0\"/>" + NEW_LINE);
	                    
	    return output.toString();
	}

	/** 
     * Add a node for a Graffoo individual or equivalent or disjoint node class
     * 
     * @param  isClassRestriction boolean indicating that a note is displayed (this is not an individual
     *              node) and that it is a class restriction (versus a datatype restriction)
     * @param  nodeName String
     * @param  nodeText String which is the Restriction text or the individual's name
     * @param  width String (is an empty string if this is for an individual)
     * @param  height String (is an empty string if this is for an individual)
     * @return GraphML String
     * 
     */
    protected static String addGraffooIndividualOrNode(final boolean isClassRestriction, 
    		final String nodeName, final String nodeText,
    		final String width, final String height) {

    	String text = nodeText;
    	
        NodeDetailsModel nodeDetails = NodeDetailsModel.builder()
        		.nodeShape(ELLIPSE)
        		.fillColor("#FF7FC1")
        		.borderType(SOLID)
        		.modelName("eight_pos")
        		.modelPosition("e")
        		.width("20.0")
        		.height("20.0")
        		.textColor(BLACK)
        		.borderColor(BLACK)
        		.borderWidth(getLineWidth(GRAFFOO))
        		.build();
        		
        if (!width.isEmpty()) {
        	// Is a restriction
            if (isClassRestriction) {
            	nodeDetails.setNodeShape(ROUND_RECTANGLE);
                nodeDetails.setFillColor("#FFFFAA");
            } else {
            	// Datatype restriction
            	nodeDetails.setNodeShape("parallelogramRight");
                nodeDetails.setFillColor("#ECFFEC");
            }
            nodeDetails.setBorderType(DASHED);
            nodeDetails.setModelName(INTERNAL);
            nodeDetails.setModelPosition("c");
            nodeDetails.setWidth(width);
            nodeDetails.setHeight(height);
        } else {
        	// Is an individual and need to make sure that there is no label
            text = GraphMLUtils.getLabelForDisplay("", GRAFFOO, nodeName, 
            		nodeText, true);
        }
        
        nodeDetails.setTextColor(BLACK);
        nodeDetails.setBorderColor(BLACK);
        nodeDetails.setBorderWidth(getLineWidth(GRAFFOO));
        
        return addNode(nodeDetails, nodeName, text);
    }
	
	/**
	 * Adds an image resource to the graphML
	 * 
	 * @param  resourceType String
	 * @return graphML String
	 * @throws IOException
	 * 
	 */
	protected static String addImageResource(String resourceType) throws IOException {
	
	    return "      <y:Resource id=\"" + resourceType + "\" type=\"java.awt.image.BufferedImage\">"
	    		+ readFile("graphmlimages/" + resourceType + ".txt")
	    		+ "</y:Resource>" + NEW_LINE;
	}
	
	/**
	 * Adds a VOWL datatype (likely rdfs:Literal) that is specific to a property. This supports
	 * VOWL's splitting by property requirements.
	 * 
	 * @param  isDatatype boolean indicating whether the name is a datatype or a unique id for another
	 *                 owl:Thing node 
	 * @param  name String defining the datatype or the owl:Thing node id in support of property splitting
	 * @return GraphML String for the datatype or new owl:Thing node
	 * 
	 */
	protected static String addVOWLDatatypeOrThing(boolean isDatatype, final String name) {

		String label;
		
        // Get input from model for the node details
        NodeDetailsModel nodeDetails = NodeDetailsModel.builder()
        	    .nodeShape(ELLIPSE)
        	    .fillColor("#FFFFFF")
        	    .width("90.0")
        	    .height("90.0")
        	    .textColor(BLACK)
        	    .borderColor(BLACK)
        	    .borderType(DASHED)
        	    .borderWidth(getLineWidth("vowl"))
        	    .modelName(INTERNAL)
        	    .modelPosition("c")
        	    .build();
        		
        if (isDatatype) {
        	nodeDetails.setNodeShape(ROUND_RECTANGLE);
        	nodeDetails.setFillColor("#FFCC33");
            nodeDetails.setWidth("80.0");
            nodeDetails.setHeight("30.0");
            label = name.substring(name.lastIndexOf(':') + 1);
        } else {
    	    label = "Thing";
        }
        
        return addNode(nodeDetails, name, label);
	}

    /**
	 * Gets the appropriate font size for notes and edges, given the visualization type.
	 * 
	 * @param  visualization String
	 * @return font size as a String
	 * 
	 */
	protected static String getFontSize(final String visualization) {
		
		if (UML.equals(visualization)) {
			return "12";
		} else {
			return "16";
		}
	}
	
	/**
	 * Creates unique rdfs:Literals or owl:Things as required for VOWL splitting by property
	 * or class (respectively).
	 * 
	 * @param  propertyName String
	 * @param  domainName String
	 * @param  rangeName String
	 * @param  propertyType String indicating whether the property identified by the propertyName 
	 *            is an "o" (object), "d" (datatype) or "a" (annotation) property
 	 * @return newDetails List<String> array of 3 entries returning the domainName and rangeName 
 	 * 	          at indices 0 and 1, one of which will be updated due to VOWL splitting,
 	 *            and a string defining the new node that was added ("split" node) 
	 * 
	 */
	protected static List<String> handleVOWLSplitting(final String propertyName, final String domainName,
	        final String rangeName, final String propertyType) {
		
		List<String> newDetails = new ArrayList<>();
		if (!"o".equals(propertyType)) {
			// Datatype or annotation property
			// Need to duplicate the datatype for each property
			String newDatatype = propertyName + rangeName;
			newDetails.add(domainName);
			newDetails.add(newDatatype);
			newDetails.add(addVOWLDatatypeOrThing(true, newDatatype));
	    } else {
	    	// Object property
	    	// Need a different owl:Thing node for each set of properties
	    	if (OWL_THING.equals(domainName) && !OWL_THING.equals(rangeName)) {
	    		String newThing = OWL_THING + propertyName + rangeName;
	    		newDetails.add(newThing);
	    		newDetails.add(rangeName);
	    		newDetails.add(addVOWLDatatypeOrThing(false, newThing));
	    	} else if (OWL_THING.equals(rangeName) && !OWL_THING.equals(domainName)) {
	    		String newThing = OWL_THING + propertyName + domainName ;
	    		newDetails.add(domainName);
	    		newDetails.add(newThing);
	    		newDetails.add(addVOWLDatatypeOrThing(false, newThing));
	    	} else {
	    		newDetails.add(domainName);
	    		newDetails.add(rangeName);
	    		newDetails.add(EMPTY_STRING);
	    	}
	    }
		
		return newDetails;
	}
    
    /**
     * Reads a file and returns the contents as a string.
     * 
     * @param path String (absolute or relative)
     * @throws IOException
     * 
     */
    protected static String readFile(String path) throws IOException {
    	
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, Charset.defaultCharset());
    }
	
	/** 
	 * Update edgeDetails for an edge connecting two blank nodes representing either union, complement
	 * or intersection.
	 * 
	 * @param edgeDetails EdgeDetailsModel with info such as sourceArrow, edgeLabel, ...
	 *             The model is updated by this method.
	 * 
	 */
	protected static void resetEdgeDetails(EdgeDetailsModel edgeDetails) {
		
		edgeDetails.setEdgeLabel(EMPTY_STRING);
		if (!UML.equals(edgeDetails.getVisualization())) {
		    edgeDetails.setLineType(DASHED);
		}
	}
}
