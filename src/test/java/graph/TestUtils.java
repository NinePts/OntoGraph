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

package graph;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.SAXException;
import graph.SAXErrorHandler;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import graph.models.EdgeTestDetailsModel;
import graph.models.NodeTestDetailsModel;

/**
 * Contains utility methods for test classes.
 *
 */
public class TestUtils {

	private static final String NEW_LINE = System.getProperty("line.separator");
	private static final int LENGTH_NEW_LINE = NEW_LINE.length();
	
	/**
	 * Reads a file's contents to a base64 encoded string.
	 * 
	 * @param  path String defining the (absolute or relative) path to the file
	 * @return String base-64 encoded contents of the file 
	 * @throws IOException
	 * 
	 */
	public static String readFile(String path) throws IOException {
		
		File file = new File(path);
	    byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(file));
	    String result = new String(encoded, StandardCharsets.US_ASCII);
	    return "data:application/octet-stream;base64," + result;
	}
    
    /**
     * Reads a file's contents to a String.
     * 
     * @param  path String defining the (absolute or relative) path to the file
     * @return String contents of the file
     * @throws IOException
     * 
     */
    public static String readGraphMLFile(String path) throws IOException {
    	
    	return new String(Files.readAllBytes(Paths.get(path)));
    }

    /**
     * Tests the GraphML output string for common problems and expected attribute and element values.
     * 
     * @param  directoryPrefix String (e.g., "class" or "custom") indicating the start of the directory name
     *                where the "expected" value files are found
     * @param  visualization String (e.g., "graffoo" or "custom")
     * @param  fileName String to use to retrieve the expected values
     * @param  expectedPrefixes List<String> identifying all of the prefixes to be included in the 
     *               "Prefixes" node
     * @param  expectedTitle List<String> holding the "Title: xxx" string as the first value, and the 
     *               "Ontology URI: xxx" string as the second value
     * @param  testXML String
     * @throws IOException
     * 
     */
	public static void testGraphMLOutput(final String directoryPrefix, final String visualization, final String fileName, 
    		List<String> expectedPrefixes, List<String> expectedTitle, final String testXML) 
    				throws IOException {

        String problems = testForCommonProblems(testXML);
        if (!problems.isEmpty()) {
            org.junit.Assert.assertTrue(problems, false);
        }

        List<NodeTestDetailsModel> controlNodes = readControlNodesFile(directoryPrefix, visualization, fileName);
	    List<EdgeTestDetailsModel> controlEdges = readControlEdgesFile(directoryPrefix, visualization, fileName);
        String documentDiff = compareTestToControl(visualization, testXML, expectedPrefixes, expectedTitle, 
    	    	controlNodes, controlEdges);
	
	    if (documentDiff.isEmpty()) {
	    	org.junit.Assert.assertTrue("No differences", true);
	    } else {
	    	org.junit.Assert.assertTrue("For the visualization, " + visualization + ", the XML differences are: " 
	    			+ documentDiff, false);
	    }
    }
	
    /**
     * Writes a string to the specified file. This is useful to use to review/debug test output files 
     * when one or more tests fail.
     * 
     * @param  path String defining the (absolute or relative) path to the file to be written
     * @param  input String defining the file contents
     * @throws IOException
     * 
     */
    public static void writeFile(String path, String input) throws IOException {
        
        File file = new File(path);
        FileWriter fw = new FileWriter(file);
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(input);
        bw.close();
        fw.close();
    }
    
    /** 
     * Checks the value of a particular attribute, or the value of the element (if the attributeName
     * parameter is empty).
     * 
     * @param  xmlDetails String holding all the XML elements from <node ...> or <edge ...> to 
     *            </node> or </edge>
     * @param  forNode boolean (true if this is for a node element check, or false if for an edge element)
     * @param  id String identifying the node or edge id
     * @param  element String identifying the specific sub-node to be checked
     * @param  attributeName String identifying the specific attribute of the sub-node to be checked
     * @param  compareValue String specifying the value of the value or sub-node
     * @return error String
     * 
     */
    private static String checkAttributeOrValue(final String xmlDetails, final boolean forNode, 
    		final String id, final String element, final String attributeName, final String compareValue) {
    	
    	StringBuilder error = new StringBuilder();
    	
    	// Get the element details <element ... /> or <element ...>value</element>
    	int indexOfElement = xmlDetails.indexOf("<" + element + " ");
    	String closeElement = "/>";
    	if (attributeName.isEmpty()) {
    		if (element.contains(" ")) {
    			closeElement = "</" + element.substring(0, element.indexOf(' '));
    		} else {
    			closeElement = "</" + element;
    		}
    	} 
    	
    	String elementDetails = xmlDetails.substring(indexOfElement, 
    			xmlDetails.indexOf(closeElement, indexOfElement));
    	
    	// Set up the error message
		if (forNode) {
			error.append("For the node, " + id);
		} else {
			error.append("For the edge, " + id);
		}
    	if (!attributeName.isEmpty()) {
    		if (!elementDetails.contains(attributeName + "=\"" + compareValue)) {
    			error.append(", the element - attribute, " + element + " - " + attributeName
    					+ ", does not have the expected value (" + compareValue + "). ");
    		} else {
    			error.setLength(0);
    		}
    		
    	} else {
    		// Is an element value check ...
        	// Fix up the string value if it contains the keywords: NEW_LINE, EMPTY_STRING, or COMMA_SPACE
    		String cv = compareValue.replaceAll("NEW_LINE", NEW_LINE)
    		                        .replaceAll("EMPTY_STRING", "")
    		                        .replaceAll("COMMA_SPACE", ", ");
    		if (cv.contains("##")) {
    			// This is a node or edge id with an arbitrary blank node id
    			 cv = cv.substring(0, cv.indexOf("##"));
    		}
    		if (!elementDetails.contains(">" + cv)) {
    			error.append(", the value of the element, " + element + " is not the expected value ("
    					+ compareValue + "). ");
    		} else {
    			error.setLength(0);
    		}
    	}
    	
    	return error.toString();
    }
    
    /**
     * Checks that the expected number of blank nodes are included in the output XML, and that they
     * have one of the specified NodeLabel values.
     * 
     * @param  visualization String
     * @param  nodeId String defining the specific text of the node id attribute
     * @param  testXML String
     * @param  nodeValues String holding the possible node labels (where the different values are 
     *            separated by the string, " || "
     * @param  numberOfBNodes int
     * @return error String or an empty string
     * 
     */
    private static String checkBlankNodes(final String visualization, final String nodeId, final String testXML, 
    		final String expectedValues, int numberOfExpectedNodes) {
    	
    	StringBuilder sb = new StringBuilder();
    	
    	String[] values = expectedValues.split("\\s\\|\\|\\s");

		// Check for the specified number of blank nodes
    	int bNodeCount = 0;
    	int bNodeIndex = 0;
    	Set<String> nodeLabels = new HashSet<>();
    	do {
    		bNodeIndex = testXML.indexOf("node id=\"" + nodeId.substring(0, nodeId.indexOf("##")), bNodeIndex);
    		if (bNodeIndex > 0) {
    			bNodeCount++;
    			if ("vowl".equals(visualization) && values[0].startsWith("IMAGE_REFID")) {
    				bNodeIndex = testXML.indexOf("<y:Image ", bNodeIndex);
    				nodeLabels.add("IMAGE_REFID:" + testXML.substring(testXML.indexOf("refid=\"", bNodeIndex) + 7, 
    						testXML.indexOf("\"/>", bNodeIndex)));
    			} else {
    				bNodeIndex = testXML.indexOf("<y:NodeLabel ", bNodeIndex);
    				nodeLabels.add(testXML.substring(testXML.indexOf(">", bNodeIndex) + 1, 
    						testXML.indexOf("</y:NodeLabel>", bNodeIndex)));
    			}
    		}
    	} while (bNodeIndex > 0);
    	if (bNodeCount != numberOfExpectedNodes) {
			sb.append(String.format(
					"The output GraphML does not contain the expected number of blank nodes, %d, " 
							+ ", instead it contains %d nodes. ", numberOfExpectedNodes, bNodeCount));
		}
    	
    	// Check that all the expected values are present
    	StringBuilder missingExpected = new StringBuilder();
    	for (String value : values) {
    		boolean foundValue = false;
    		for (String label : nodeLabels) {
    			if (label.startsWith(value)) {
    				foundValue = true;
    				break;
    			}
    		}
    		if (!foundValue) {
    			missingExpected.append(value + " ");
    		}
    	}
    	if (!missingExpected.toString().isEmpty()) {
    		sb.append(String.format("The output GraphML does not contain the expected node label(s): %s ", 
    				missingExpected.toString()));
    	}
    	
    	// Check that there are no additional labels
    	StringBuilder otherLabels = new StringBuilder();
    	for (String label : nodeLabels) {
    		boolean foundLabel = false;
    		for (String value : values) {
    			if (label.startsWith(value)) {
    				foundLabel = true;
    				break;
    			}
    		}
    		if (!foundLabel) {
    			otherLabels.append(label + " ");
    		}
    	}
    	if (!otherLabels.toString().isEmpty()) {
    		sb.append(String.format("The output GraphML contains additional node label(s): %s ", 
    				otherLabels.toString()));
    	}
    	
    	return sb.toString();
    }
 
    /**
     * Checks that the edgeText is valid per one of the control edge models.
     * 
     * @param  id String for error reporting
     * @param  edgeText String with the edge element details
     * @param  controlEdges List<EdgeTestDetailsModel> defining each edge and its expected element 
     *               and attribute values
     * @return error String
     * 
     */
    private static String checkEdgeDetails(final String id, final String edgeText, 
    		List<EdgeTestDetailsModel> controlEdges) {

    	StringBuilder sb = new StringBuilder();
    	
		boolean foundEdge = false;
		for (EdgeTestDetailsModel etdm : controlEdges) {
			if (determineEdgeMatch(id, edgeText, etdm)) {
				// Check the rest of the data for the edge
				// Already did the source and target check for a blank node ...
				if (!id.contains("bnode_")) {
					if (!edgeText.contains("source=\"" + etdm.getEdgeSource())) {
						sb.append("For the edge, " + id 
								+ ", the attribute, source, does not have the expected value (" 
								+ etdm.getEdgeSource() + "). ");
					}
					if (!edgeText.contains("target=\"" + etdm.getEdgeTarget())) {
						sb.append("For the edge, " + id 
								+ ", the attribute, target, does not have the expected value (" 
								+ etdm.getEdgeTarget() + "). ");
					}
				}
				
				sb.append(checkAttributeOrValue(edgeText, false, id, "y:Arrows", "source", 
						etdm.getArrowSource()));
				sb.append(checkAttributeOrValue(edgeText, false, id, "y:Arrows", "target", 
						etdm.getArrowTarget()));
				sb.append(checkAttributeOrValue(edgeText, false, id, "y:LineStyle", "color", 
						etdm.getLineStyleColor()));
				sb.append(checkAttributeOrValue(edgeText, false, id, "y:LineStyle", "type", 
						etdm.getLineStyleType()));
				sb.append(checkAttributeOrValue(edgeText, false, id, "y:EdgeLabel", 
						"backgroundColor", etdm.getEdgeLabelBackgroundColor()));
				sb.append(checkAttributeOrValue(edgeText, false, id, "y:EdgeLabel", "", 
						etdm.getEdgeLabelValue()));
				
				foundEdge = true;
				break;
			}
		}
		if (!foundEdge) {
			sb.append("Did not find the edge, " + id + ", in the list of expected edges. ");
		}
		
		return sb.toString();
    }
    
    /**
     * Performs a general check that each of the expected nodes and edges is present in the output 
     * GraphML.
     * 
     * @param  visualization String
     * @param  testXML String holding the generated XML
     * @param  controlNodes List<NodeTestDetailsModel>
     * @param  controlEdges List<EdgeTestDetailsModel>
     * @return error String
     * 
     */
    private static String checkExpected(final String visualization, final String testXML, 
    		List<NodeTestDetailsModel> controlNodes, List<EdgeTestDetailsModel> controlEdges) {

    	StringBuilder sb = new StringBuilder();    	
    	
    	// Check the nodes
    	for (NodeTestDetailsModel ntdm1: controlNodes) {
			String nodeId = ntdm1.getNodeId();
			if (nodeId.contains("##")) {
				// Check for the specified number of blank nodes
				// Should be only one of these blank node identifiers per file/ontology test
				String expectedValues = ntdm1.getNodeLabelValue();
				if ("uml".equals(visualization)) {
					// Expected values are defined in the node, <y:NodeLabel alignment="left" ...>
					expectedValues = ntdm1.getShapeTypeOrNodeLabel();
				}
				sb.append(checkBlankNodes(visualization, nodeId, testXML, 
						expectedValues.replaceAll("NEW_LINE", NEW_LINE).replaceAll("EMPTY_STRING", "")
									  .replaceAll("COMMA_SPACE", ", "),
						Integer.parseInt(nodeId.substring(nodeId.indexOf("##") + 2))));
			} else {
				if (!testXML.contains("<node id=\"" + nodeId)) {
					sb.append("The output GraphML does not contain the expected node, " + nodeId + ". ");
				}
			}
		}
    	
    	// Check the edges
		for (EdgeTestDetailsModel etdm : controlEdges) {
			String edgeId = etdm.getEdgeId();
			if (edgeId.contains("##")) {
				String[] bNodes = edgeId.split("##");
				if (bNodes.length > 1) {
					// At least one blank node, where the source reference is a blank node
					// This test is not exact, but a good first pass (complete checking is done in
					//    checkEdgeDetails)
					if (!(testXML.contains("<edge id=\"" + bNodes[0]) && testXML.contains(" target=\""
							+ bNodes[1]))) {
						sb.append("The output GraphML does not contain the expected edge that begins with the string, " 
								+ bNodes[0] + ", and includes the target node, " + bNodes[1] + ". ");
					}
					
				} else {
					// Blank node is the target reference
					if (!testXML.contains("<edge id=\"" + bNodes[0])) {
						sb.append("The output GraphML does not contain the expected edge that begins with the string, " 
								+ bNodes[0] + ". ");
					}
				}
			} else {
				if (!testXML.contains("<edge id=\"" + edgeId)) {
					sb.append("The output GraphML does not contain the expected edge, " + edgeId + ". ");
				}
			}
		}
		
		return sb.toString();
    }
 
    /**
     * Checks that the nodeText is valid per one of the control node models.
     * 
     * @param  id String for error reporting
     * @param  nodeText String with the node element details
     * @param  controlNodes List<NodeTestDetailsModel> defining each node and its expected element 
     *               and attribute values
     * @return error String
     * 
     */
    private static String checkNodeDetails(final String id, final String nodeText, 
    		List<NodeTestDetailsModel> controlNodes) {
        
    	StringBuilder sb = new StringBuilder();
		
		boolean foundNode = false;
		for (NodeTestDetailsModel ntdm : controlNodes) {
			if (id.equals(ntdm.getNodeId()) || 
					(id.contains("##") && ntdm.getNodeId().startsWith(id.substring(0, id.indexOf("##"))))) {
				boolean forUMLNode = ntdm.isForUMLNode();
				
				// Check the rest of the data for the node
			    if ("NULL".equals(ntdm.getFillColor())) {
			    	sb.append(checkAttributeOrValue(nodeText, true, id, "y:Fill", "hasColor", "false"));
			    } else {
			    	sb.append(checkAttributeOrValue(nodeText, true, id, "y:Fill", "color", 
			    			ntdm.getFillColor()));
			    }
			    
			    if ("NULL".equals(ntdm.getBorderStyleColor())) {
			    	sb.append(checkAttributeOrValue(nodeText, true, id, "y:BorderStyle", "hasColor", "false"));
			    } else {
			    	sb.append(checkAttributeOrValue(nodeText, true, id, "y:BorderStyle", "color", 
			    			ntdm.getBorderStyleColor()));
			    }
			    
				sb.append(checkAttributeOrValue(nodeText, true, id, "y:BorderStyle", "type", 
						ntdm.getBorderStyleType()));
				
				if (!"NULL".equals(ntdm.getNodeLabelValue())) {
					String nodeLabel = ntdm.getNodeLabelValue();
					if (forUMLNode) {
						sb.append(checkAttributeOrValue(nodeText, true, id, "y:NodeLabel alignment=\"center\"", "", 
								nodeLabel));
					} else if (nodeLabel.startsWith("IMAGE_REFID")) {
						sb.append(checkAttributeOrValue(nodeText, true, id, "y:Image", "refid", 
								nodeLabel.substring(nodeLabel.indexOf(':') + 1)));
					} else {
						sb.append(checkAttributeOrValue(nodeText, true, id, "y:NodeLabel", "", nodeLabel));
					}
				}
				
				if (!"NULL".equals(ntdm.getShapeTypeOrNodeLabel())) {
					if (forUMLNode) {
						sb.append(checkAttributeOrValue(nodeText, true, id, "y:NodeLabel alignment=\"left\"", "", 
								ntdm.getShapeTypeOrNodeLabel()));
					} else {
						sb.append(checkAttributeOrValue(nodeText, true, id, "y:Shape", "type", 
								ntdm.getShapeTypeOrNodeLabel()));
					}
				}
				
				foundNode = true;
				break;
			}
		}
		if (!foundNode) {
			sb.append("Did not find the node, " + id + ", in the list of expected nodes. ");
		}
		
		return sb.toString();
    }
    
    /**
	 * Compares the expected values for the title, prefixes, and all the nodes and edges with the
	 * values in the generated XML.
	 * 
	 * @param  visualization String
	 * @param  testXML String holding the generated XML
     * @param  expectedPrefixes List<String> identifying all of the prefixes to be included in the 
     *               "Prefixes" node
     * @param  expectedTitle List<String> holding the "Title: xxx" string as the first value, and the 
     *               "Ontology URI: xxx" string as the second value
	 * @param  controlNodes List<NodeTestDetailsModel> 
	 * @param  controlEdges List<EdgeTestDetailsModel>
	 * @return error String
	 * 
	 */
	private static String compareTestToControl(final String visualization, final String testXML, 
			List<String> expectedPrefixes, List<String> expectedTitle, List<NodeTestDetailsModel> controlNodes,
			List<EdgeTestDetailsModel> controlEdges) {
		
		StringBuilder sb = new StringBuilder();
		
		// Go through all the expected nodes and edges, making sure that each one is present
		sb.append(checkExpected(visualization, testXML, controlNodes, controlEdges));
	
		// Go through all the elements if we do not have any errors
		if (!sb.toString().isEmpty()) {
			return sb.toString();
		}
		int currIndex = 0;
		do {
			int nextIndex = testXML.indexOf(System.getProperty("line.separator"), currIndex);
			if (nextIndex > 0) {
				String nextLine = testXML.substring(currIndex, nextIndex + LENGTH_NEW_LINE).trim();
				if (nextLine.startsWith("<node id=\"title::n0\"")) {
					// Check for the title lines
					nextIndex = testXML.indexOf("</node>", currIndex) + 7;
					String titleText = testXML.substring(currIndex, nextIndex);
					for (String titleString : expectedTitle) {
						if (!titleString.isEmpty()) {
							if (!(titleText.contains(">" + titleString) 
									|| titleText.contains(NEW_LINE + titleString))) {
								sb.append("The title does not include the expected text (" + titleString + "). ");
							}
						}
					}
					
				} else if (nextLine.startsWith("<node id=\"prefixes::n0\"")) {
					// Check the prefixes
					nextIndex = testXML.indexOf("</node>", currIndex) + 7;
					String prefixesText = testXML.substring(currIndex, nextIndex);
					for (String prefix : expectedPrefixes) {
						if (!(prefixesText.contains(">" + prefix + NEW_LINE) || prefixesText.contains(">" + prefix + "</")
								|| prefixesText.contains(NEW_LINE + prefix + "</") 
								|| prefixesText.contains(NEW_LINE + prefix + NEW_LINE))) {
							sb.append("The list of prefixes does not include the expected value(" + prefix + "). ");
							break;
						}
					}
					
				} else if ((nextLine.startsWith("<node id") && !nextLine.contains("bnode_")  
							&& !nextLine.startsWith("<node id=\"title") 
							&& !nextLine.startsWith("<node id=\"prefixes"))
							|| nextLine.startsWith("<edge id")) {
					int indexOfIdName = nextLine.indexOf("id=") + 4;
					String id = nextLine.substring(indexOfIdName, nextLine.indexOf("\"", indexOfIdName));
					if (nextLine.startsWith("<node id")) {
						nextIndex = testXML.indexOf("</node>", currIndex) + 7;
						String nodeText = testXML.substring(currIndex, nextIndex);
						sb.append(checkNodeDetails(id, nodeText, controlNodes));
						
					} else {
						nextIndex = testXML.indexOf("</edge>", currIndex) + 7;
						sb.append(checkEdgeDetails(id, testXML.substring(currIndex, nextIndex), controlEdges));
					}
				}
				currIndex = nextIndex + LENGTH_NEW_LINE;
				
			} else {
				currIndex = testXML.length();
			}
			
		} while (currIndex < testXML.length());
		
		return sb.toString();
	}
	
	/**
	 * Determines if the EdgeTestDetailsModel is a match for/holds the comparison details for the edgeText.
	 * 
	 * @param id String holding the edge id
	 * @param edgeText String holding the complete <edge ...> ... </edge> GraphML text
	 * @param etdm EdgeTestDetailsModel 
	 * @return true/false boolean indicating if the EdgeTestDetailsModel is a match for the GraphML
	 *              input in edgeText
	 *              
	 */
	private static boolean determineEdgeMatch(final String id, final String edgeText, 
			EdgeTestDetailsModel etdm) {

		String edgeId = etdm.getEdgeId();
		String[] bNodes = edgeId.split("##");

		if (edgeId.contains("##")) {
			if (bNodes.length > 1) {
				// Source reference is a blank node (e.g., unbnode_##prefix:class)
				if (edgeText.startsWith("<edge id=\"" + bNodes[0])
						&& edgeText.contains(" source=\"bnode_") 
						&& edgeText.contains(" target=\"" + bNodes[1])) {
					return true;
				} else {
					return false;
				}
				
			} else {
				// Target reference is a blank node (e.g., complementOfprefix:classbnode_##)
				if (edgeText.startsWith("<edge id=\"" + bNodes[0]) && edgeText.contains(" target=\"bnode_")) {
					return true;
				} else {
					return false;
				}
			}
			
		} else {
			if (id.equals(edgeId)) {
				return true;
			} else {
				return false;
			}
		}
	}

	/**
	 * Reads an ontology's expected edge details from a file.
	 * 
	 * @param  visualization String
	 * @param  fileName String 
	 * @return List<NodeTestDetailsModel>
	 * @throws IOException
	 * 
	 */
	private static List<EdgeTestDetailsModel> readControlEdgesFile(final String graphType,
	        final String visualization, final String fileName) throws IOException {
		
		List<String> fileContents = Files.readAllLines(Paths.get(
				"src/test/resources/control/" + graphType + "Aspects/" + visualization + "/" 
						+ fileName + "_Edges.txt"));
		
		List<EdgeTestDetailsModel> edgeDetails = new ArrayList<>();
		
		for (String fileLine : fileContents) {
			if (!fileLine.startsWith("//")) {	// "//" identifies a comment line
				String[] values = fileLine.split(", ");
				edgeDetails.add(EdgeTestDetailsModel.builder()
						.edgeId(values[0])
						.edgeSource(values[1])
						.edgeTarget(values[2])
						.arrowSource(values[3])
						.arrowTarget(values[4])
						.lineStyleColor(values[5])
						.lineStyleType(values[6])
						.edgeLabelBackgroundColor(values[7])
						.edgeLabelValue(values[8])
						.build());
			}
		}
		
		return edgeDetails;
	}

	/**
     * Reads an ontology's expected node details from a file.
     * 
     * @param  visualization String
     * @param  fileName String 
     * @return List<NodeTestDetailsModel>
     * @throws IOException
     * 
     */
    private static List<NodeTestDetailsModel> readControlNodesFile(final String graphType, 
    		final String visualization, final String fileName) throws IOException {
    	
    	boolean forUMLNode = "uml".equals(visualization) ? true : false;
    	
    	List<String> fileContents = Files.readAllLines(Paths.get(
    			"src/test/resources/control/" + graphType + "Aspects/" + visualization + "/" 
    					+ fileName + "_Nodes.txt"));
    	
    	List<NodeTestDetailsModel> nodeDetails = new ArrayList<>();
    	
    	for (String fileLine : fileContents) {
    		if (!fileLine.startsWith("//")) {	// "//" identifies a comment line
	    		String[] values = fileLine.split(", ");
	    		nodeDetails.add(NodeTestDetailsModel.builder()
	    				.nodeId(values[0])
	    				.forUMLNode(forUMLNode)
	    				.fillColor(values[1])
	    				.borderStyleColor(values[2])
	    				.borderStyleType(values[3])
	    				.nodeLabelValue(values[4])
	    				.shapeTypeOrNodeLabel(values[5])
	    				.build());
    		}
    	}
    	
    	return nodeDetails;
    }
    
    /**
	 * Validates that the XML string does not contain the text, "null", does not duplicate any node
	 * or edge ids, and has a node for both the source and target references for an edge. Returns a 
	 * string describing each of the errors found.
	 * 
	 * @param testXML String to be checked
	 * 
	 */
	private static String testForCommonProblems(String testXML) {
	
		StringBuilder sb = new StringBuilder();
		
		SAXErrorHandler handler = new SAXErrorHandler();
		
		// Check that the document is well-formed
		try {
			SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
			InputStream stream = new ByteArrayInputStream(testXML.getBytes("UTF-8"));
			saxParser.parse(stream, handler);
		} catch (ParserConfigurationException | SAXException | IOException e) {
			sb.append("XML parsing error for the output GraphML: " + e.getMessage());
		}
  
	    // Don't continue if the XML is invalid
	    if (!sb.toString().isEmpty()) {
	    	return sb.toString();
	    }
		
		// Test for attributes set to "null"
	    if (testXML.contains("\"null\"")) {
	    	// One or more of the node or edge settings is invalid
	        sb.append("One or more of the node or edge settings is reporting as \"null\", "
	        		+ "which indicates that it is invalid. ");
	    } 
		// Test for attributes set to ""
	    if (testXML.contains("\"\"")) {
	    	// One or more of the node or edge settings is invalid
	        sb.append("One or more of the node or edge settings is reporting as \"\", "
	        		+ "which indicates that it is invalid. ");
	    } 
	    
	    List<String> nodeIds = new ArrayList<>();
	    List<String> edgeIds = new ArrayList<>();
	    Set<String> duplNodeIds = new HashSet<>();
	    Set<String> duplEdgeIds = new HashSet<>();
	    Set<String> sourceOrTargetIds = new HashSet<>();
	    List<String> missingIds = new ArrayList<>();
	    
	    // Get all the node/edge ids as well as the edge source and target ids
	    // Test for duplicate node or edge ids
		int currIndex = 0;
		do {
			int nextIndex = testXML.indexOf(System.getProperty("line.separator"), currIndex);
			if (nextIndex > 0) {
				String nextLine = testXML.substring(currIndex, nextIndex + LENGTH_NEW_LINE);
				if (nextLine.startsWith("<node id") || nextLine.startsWith("<edge id")) {
					int indexOfIdName = nextLine.indexOf("id=") + 4;
					String id = nextLine.substring(indexOfIdName, nextLine.indexOf("\"", 
							indexOfIdName));
					if (nextLine.startsWith("<node id")) {
						if (nodeIds.contains(id)) {
							duplNodeIds.add(id); 
						} else {
							nodeIds.add(id);
						}
						nextIndex = testXML.indexOf("</node>", currIndex) + 7;
					} else {
						if (edgeIds.contains(id)) {
							duplEdgeIds.add(id);
						} else {
							edgeIds.add(id);
						}
						// Also get the source and target ids for the edge
						sourceOrTargetIds.add(nextLine.substring(nextLine.indexOf("source=\"") + 8, 
								nextLine.indexOf("\" target"))); 
						sourceOrTargetIds.add(nextLine.substring(nextLine.indexOf("target=\"") + 8, 
								nextLine.indexOf("\">"))); 
						nextIndex = testXML.indexOf("</edge>", currIndex) + 7;
					}
				} 
				currIndex = nextIndex + LENGTH_NEW_LINE;
			} else {
				currIndex = testXML.length();
			}
		} while (currIndex < testXML.length());
		
		if (!duplNodeIds.isEmpty()) {
			sb.append("There are two or more occurrences of the following node ids: ");
			for (String duplNode : duplNodeIds) {
				sb.append(duplNode + " ");
			}
		}
	    if (!duplEdgeIds.isEmpty()) {
			sb.append("There are two or more occurrences of the following edge ids: ");
			for (String duplEdge : duplEdgeIds) {
				sb.append(duplEdge + " ");
			}
	    }
	    
	    // Test for edge source or target attribute ids with no corresponding node id
	    for (String srcOrTargetId : sourceOrTargetIds) {
	    	if (!nodeIds.contains(srcOrTargetId)) {
	    		missingIds.add(srcOrTargetId);
	    	}
	    }
	    
	    if (!missingIds.isEmpty()) {
			sb.append("The following ids are referenced as a source or target for an edge, "
					+ "but there is no corresponding node definition: ");
			for (String missing : missingIds) {
				sb.append(missing + " ");
			}
	    }
	    
	    return sb.toString();
	}
    
}