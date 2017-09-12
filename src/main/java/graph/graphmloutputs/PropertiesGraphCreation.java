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
 */

package graph.graphmloutputs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import graph.OntoGraphException;
import graph.models.BlankAndRelatedNodesModel;
import graph.models.ClassModel;
import graph.models.GraphRequestModel;
import graph.models.NodeDetailsModel;
import graph.models.PropertyModel;
import graph.models.RelatedAndRestrictionModel;
import graph.models.TypeAndValueModel;

/**
 * PropertiesGraphCreation transforms properties from an ontology 
 * into a specific GraphML output as defined by the visualization.
 *
 */
public final class PropertiesGraphCreation {
    
    // Not meant to be instantiated
    private PropertiesGraphCreation() {
      throw new IllegalAccessError("PropertiesGraphCreation is a utility class and should not be instantiated.");
    }
    
    /**
     * Create a property graph (property name, domains, ranges, etc.) in GraphML.
     * 
     * @param  requestModel GraphRequestModel with property visualization details
     * @param  ontPrefixAndCurrGraphML List<String> holding the URI of the loaded ontology as the first value,
     *              and the current GraphML output as the second. Note that the second value will be an
     *              empty string (will not be used) unless the requestModel's graphType is "both" (class and 
     *              property).
     * @param  classes List<ClassModel> used as domains and object ranges
     * @param  properties List<PropertyModel>
     * @param  relatedsAndRestrictions RelatedAndRestrictionModel containing lists of models 
     *             of "related" classes (equivalent, disjoints, and oneOfs), of connectives
     *             (unions, intersections and complementOfs) and restrictions (allValuesFrom,
     *             someValuesFrom, min/maxInclusive, ...)
     * @return GraphML String
     * @throws OntoGraphException 
     * 
     */
    public static String processPropertyGraph(GraphRequestModel requestModel, 
    		List<String> ontPrefixAndCurrGraphML, List<ClassModel> classes, List<PropertyModel> properties,  
    		RelatedAndRestrictionModel relatedsAndRestrictions) throws OntoGraphException { 
        
        StringBuilder sb = new StringBuilder();
        String ontologyPrefix = ontPrefixAndCurrGraphML.get(0);

        Set<String> domainOrRangeClasses = new HashSet<>();
        Set<String> datatypes = new HashSet<>();
        
        // TODO Multiple domains or ranges mean "intersection"; Currently shown as 2+ edges with the same property name
        for (PropertyModel propModel : properties) {
            // Get a unique list of all the domains and ranges across all the properties
        	domainOrRangeClasses.addAll(propModel.getDomains());
        	if ("o".equals(propModel.getPropertyType())) {
        	    domainOrRangeClasses.addAll(propModel.getRanges());
        	} else {
        		datatypes.addAll(propModel.getRanges());
        	}
        	
        	// Add the property edges for the current PropertyModel
            sb.append(GraphMLUtils.addPropertyEdges(requestModel, ontologyPrefix, propModel));
        }
            
        // Add the domain and object property range classes
        sb.append(addClassNodes(requestModel, ontPrefixAndCurrGraphML, classes, 
        		domainOrRangeClasses, relatedsAndRestrictions));
        
        // Add the datatype ranges 
        // Need to handle datatype restrictions differently than datatype (i.e., xsd:xxx) values
        Set<String> xsdDatatypes = new HashSet<>();
        for (String datatype : datatypes) {
        	if (!datatype.contains(":")) {
        		// Blank Node => datatype restriction
        		sb.append(GraphMLUtils.blankNodeProcessing(requestModel, classes, 
        				BlankAndRelatedNodesModel.createBlankAndRelatedNodesModel(datatype, ""),
        				"", relatedsAndRestrictions, new HashSet<String>()));
        	} else {
        		xsdDatatypes.add(datatype);
        	}
        }
        
        // Already added the datatypes as part of VOWL property splitting, need to add the datatypes
        //    for other visualizations
        if (!"vowl".equals(requestModel.getVisualization()) && !xsdDatatypes.isEmpty()) {
        	sb.append(addDatatypeNodes(requestModel, datatypes));
        }
        
        return sb.toString();
    }  
    
	/**
	 * Add the nodes that represent the domain and object range classes
	 * 
	 * @param  requestModel GraphRequestModel holding all details of user request
     * @param  ontPrefixAndCurrGraphML List<String> holding the URI of the loaded ontology as the first value,
     *              and the current GraphML output as the second. Note that the second value will be an
     *              empty string (will not be used) unless the requestModel's graphType is "both" (class and 
     *              property).
	 * @param  classes Set<String> holding all domain or object ranges references in the format,
	 *                "label (class name)"
	 * @param  domainOrRangeClasses Set<String> defining each class that is referenced as a domain or range
     * @param  relatedsAndRestrictions RelatedAndRestrictionModel containing lists of models 
     *             of "related" classes (equivalent, disjoints, and oneOfs), of connectives
     *             (unions, intersections and complementOfs) and restrictions (allValuesFrom,
     *             someValuesFrom, min/maxInclusive, ...)
     * @param  currentGraphMLOutput String (will be an empty string if the requestModel's graphType is not "both"
     *               class and property)
	 * @return GraphML String
	 * @throws OntoGraphException 
	 * 
	 */
    private static String addClassNodes(GraphRequestModel requestModel,  //NOSONAR - Acknowledging complexity
    		List<String> ontPrefixAndCurrGraphML, List<ClassModel> classes,   
    		Set<String> domainOrRangeClasses, RelatedAndRestrictionModel relatedsAndRestrictions)
    				throws OntoGraphException {

        StringBuilder sb = new StringBuilder();
        
    	String visualization = requestModel.getVisualization();
    	String ontologyPrefix = ontPrefixAndCurrGraphML.get(0);
    	String currentGraphML = ontPrefixAndCurrGraphML.get(1);
        Set<String> referencedClasses = new HashSet<>();  
    	
        // Get input from the request model for the node details
        NodeDetailsModel nodeDetails = NodeDetailsModel.createNodeDetailsModel(requestModel, "object");
	    
        // Get all the classes referenced in domain or range declarations, and any classes that are 
        //    further referenced if the domain or range is a blank node
        for (String drClass : domainOrRangeClasses) {
        	String className = drClass;
        	String label = drClass;
        	if (drClass.contains("(")) {
        		className = drClass.substring(drClass.lastIndexOf('(') + 1, drClass.lastIndexOf(')'));
        		if ("vowl".equals(visualization)) {
        		    label = drClass.substring(0, drClass.lastIndexOf('(') - 1);
        		}
        	}
	
			if (!className.contains(":")) {
				// Is a blank node ... 
				// Passing in an empty string for the "related" class name since this is not a class diagram
				//   (we want to reuse all the logic, but not make an edge to a related class)
				sb.append(GraphMLUtils.addRelated(requestModel, classes, "", 
						Arrays.asList(TypeAndValueModel.createTypeAndValueModel("eq", className)),
						relatedsAndRestrictions, referencedClasses));
			} else {
				// Not a blank node ...
				// Reset the node shape in case it was manipulated (for example, "smallCircle" is reset
				//    to "ellipse" with specific width and height)
		        nodeDetails.setNodeShape(requestModel.getObjNodeShape());
		        
		        label = GraphMLUtils.getLabelForDisplay(ontologyPrefix, visualization, className, label, true);
		        GraphMLOutputDetails.getNodeDetails(ontologyPrefix, nodeDetails, className, label);
				sb.append(GraphMLOutputDetails.addNode(nodeDetails, className, label));
			}
        }
	    
        if ("both".equals(requestModel.getGraphType())) {
        	String missingNodesAndEdges = addMissingNodesAndEdges(sb.toString(), 
        			ontPrefixAndCurrGraphML.get(1));
        	// Clear out the current StringBuilder, since the property graph details for domains and ranges
        	//    has been checked 
        	sb.setLength(0);
        	// Append any missing elements to the StringBuilder
        	if (!missingNodesAndEdges.isEmpty()) {
        		sb.append(missingNodesAndEdges);
        	}
        }
        
        // Need to add any classes referenced in complement, union or intersectionOf declarations that are
        //   NOT found in a domain/range declaration - since they will be missing from the graph
        for (String refClass : referencedClasses) {
        	boolean foundRefClass = false;
        	for (String drClass : domainOrRangeClasses) {
        		if (drClass.contains(refClass)) {
        			foundRefClass = true;
        			break;
        		}
        	}
        	if (!foundRefClass && !currentGraphML.contains("<node id=\"" + refClass + "\" ")) {
				// Reset the node shape in case it was manipulated
		        nodeDetails.setNodeShape(requestModel.getObjNodeShape());
        		sb.append(GraphMLUtils.addReferencedClass(ontologyPrefix, nodeDetails, refClass));
        	}
        }
        
	    return sb.toString();
    }

    /**
     * Add the nodes that represent each datatype
     * 
     * @param  requestModel GraphRequestModel
     * @param  datatypes Set<String> holding all datatype range references 
     * @return GraphML String
     * 
     */
    // TODO Add restrictions as the second parameter when cardinality restrictions are supported
    private static String addDatatypeNodes(GraphRequestModel requestModel, Set<String> datatypes) { 
    	
    	StringBuilder sb = new StringBuilder();
    	
        String nodeShape = requestModel.getDataNodeShape();
    	
        // Get input from model for the node details
        NodeDetailsModel nodeDetails = NodeDetailsModel.createNodeDetailsModel(requestModel, "data");
	    
        for (String data : datatypes) {
            String label = data;
        	// Get display changes based on the class name
            GraphMLOutputDetails.modifyNodeDetailsForNodeShape(nodeDetails, data);
	        // Restore the nodeShape in case it was changed in modifyNodeDetails
            nodeDetails.setNodeShape(nodeShape);
        	sb.append(GraphMLOutputDetails.addNode(nodeDetails, data, label));
        }
	    
	    return sb.toString();
    }
    
    /**
     * "Both" class and property diagrams are generated by processing a class graph and then a property graph.
     * But, the property graph will likely have overlapping node and edge definitions with the class graph.
     * We check all the class-related nodes and edges for a property graph (they are added to the graph because
     * they are referenced as property domains or ranges). If the node or edge is already defined in the
     * GraphML output from the class graph processing, it is discarded.  If it is new, then its details are
     * retained and returned.
     * 
     * @param  propertyGraphML holding the GraphML output for a property graph
     * @param  classGraphML holding the GraphML output for a class graph
     * @return String with any nodes or edges in the propertyGraphML String that are NOT in the 
     *              classGraphML String
     */
    private static String addMissingNodesAndEdges(String propertyGraphML, String classGraphML) {

    	StringBuilder missingElements = new StringBuilder();
    	
    	// Most likely have all the necessary classes and edges included in the class GraphML String  
    	//    (since "both class and property definitions" first generates the class graph). But, some
    	//    nodes and edges will be missing if they are only referenced in a domain or range declaration. 
    	//    If so, add them.
    	List<String> nodeEdgeList = new ArrayList<>();
    	nodeEdgeList.addAll(getGraphMLDetails(propertyGraphML, "node"));
    	nodeEdgeList.addAll(getGraphMLDetails(propertyGraphML, "edge"));
    	for (String element : nodeEdgeList) {
    		// Check the class GraphMLOutput to see if the node or edge is already included
    		if (!classGraphML.contains(element.substring(0, element.indexOf('"', 10) + 1))) {
    			// Not included, so add it to the "missing" list
    			missingElements.append(element);
    		}
    	}
    	
    	return missingElements.toString();
    }
    
    /**
     * Gets the details for a GraphML <elementName id= ...> ... </elementName> element 
     * 
     * @param  graphMLDetails String holding the defined GraphML elements ("node"s and "edge"s)
     * @param  elementName String, either "node" or "edge"
     * @return List<String> holding each <node> or <edge> definition in graphMLDetails 
     * 
     */
    private static List<String> getGraphMLDetails(String graphMLDetails, final String elementName) {
    	
    	List<String> xmlDetails = new ArrayList<>();

		int currIndex = 0;
		do {
			int nextIndex = graphMLDetails.indexOf("<" + elementName + " id=\"", currIndex);
			if (nextIndex > -1) {
				xmlDetails.add(graphMLDetails.substring(nextIndex, graphMLDetails.indexOf("</" + elementName + ">", 
						nextIndex) + 7) + System.getProperty("line.separator"));
				currIndex = nextIndex + 10;
			} else {
				currIndex = graphMLDetails.length();
			}
			
		} while (currIndex < graphMLDetails.length());
		
		return xmlDetails;
    }
}
