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

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import graph.OntoGraphException;
import graph.models.ClassModel;
import graph.models.EdgeDetailsModel;
import graph.models.EdgeFlagsModel;
import graph.models.GraphRequestModel;
import graph.models.IndividualModel;
import graph.models.NodeDetailsModel;
import graph.models.RelatedAndRestrictionModel;
import graph.models.TypeAndValueModel;

/**
 * IndividualsGraphCreation transforms instances/named individuals from an ontology 
 * into a specific GraphML output as defined by the visualization.
 *
 */
public final class IndividualsGraphCreation {
    
	// Frequently used strings
    protected static final String BLACK = "#000000";
    protected static final String WHITE = "#FFFFFF";
	
    // Not meant to be instantiated
    private IndividualsGraphCreation() {
      throw new IllegalAccessError("IndividualsGraphCreation is a utility class and should not be instantiated.");
    }
    
    /**
     * Create an instance/individual graph (individuals and their types) in GraphML.
     * 
	 * @param  requestModel GraphRequestModel
     * @param  ontologyPrefix String that is the prefix of the owl:Ontology URI
	 * @param  classes List<ClassModel> defining the naming details of any classes included due
	 *              to equivalencies, disjoints, ... and connectives
     * @param  relatedsAndRestrictions RelatedAndRestrictionModel containing lists of models 
     *             of "related" classes (equivalent, disjoints, and oneOfs), of connectives
     *             (unions, intersections and complementOfs) and restrictions (allValuesFrom,
     *             someValuesFrom, min/maxInclusive, ...)
     * @param  individuals List of IndividualModels
     * @return GraphML String
     * @throws OntoGraphException 
     * 
     */
    public static String processIndividualGraph(GraphRequestModel requestModel,
    		final String ontologyPrefix, List<ClassModel> classes, List<IndividualModel> individuals,
    		RelatedAndRestrictionModel relatedsAndRestrictions) throws OntoGraphException { 
        
        StringBuilder sb = new StringBuilder();
        
        Set<String> uniqueTypes = new HashSet<>();
        Set<String> uniqueIndividuals = new HashSet<>();
        Set<String> referencedClasses = new HashSet<>(); 
        
        // For each individual ...
        for (IndividualModel indiv : individuals) {
            String individualName = indiv.getIndividualName();
            String individualLabel = indiv.getIndividualLabel();
            // And add the edge from an individual to its type(s)
        	List<String> types = indiv.getTypeLabels();
            sb.append(addTypeOfEdges(requestModel, types, classes, individualName, relatedsAndRestrictions,
                    referencedClasses));
            
            // Keep track of all the individuals and types
            // (We add the individuals and the types at the end of this method)
            uniqueIndividuals.add(individualLabel);
        	uniqueTypes.addAll(types);

        	// Add the datatype properties and values
        	// These properties use the TypeAndValueModel where the "type" is the property name
        	//    and the "value" is the property value
        	List<TypeAndValueModel> datatypeProperties = indiv.getDatatypeProperties();
        	List<TypeAndValueModel> objectProperties = indiv.getObjectProperties();
        	for (TypeAndValueModel propAndVal : datatypeProperties) {
        		String dataValue = propAndVal.getValue();
        	    sb.append(addDatatypePropertyValue(requestModel, ontologyPrefix, dataValue));
        	    sb.append(addDatatypePropertyEdge(requestModel, individualName, dataValue.replaceAll("\"", ""), 
        	    		propAndVal.getType()));
        	}
        	
        	// Add the object properties and values
        	for (TypeAndValueModel propAndVal : objectProperties) {
        		String objValue = propAndVal.getValue();
        	    sb.append(addObjectPropertyEdges(requestModel, individualName, objValue, 
        	    		propAndVal.getType()));
        	    updateUniqueIndividuals(objValue, uniqueIndividuals);
        	}
        }	
        
        // Add the (unique) individuals and types
        sb.append(addIndividuals(requestModel, ontologyPrefix, uniqueIndividuals));
        sb.append(addTypes(requestModel, ontologyPrefix, uniqueTypes));

        // Need to add any classes referenced in complement, union or intersectionOf declarations 
        // The display details are set to handle nodes
        NodeDetailsModel nodeDetails = NodeDetailsModel.createNodeDetailsModel(requestModel, "class");
        for (String refClass : referencedClasses) {
    		sb.append(GraphMLUtils.addReferencedClass(ontologyPrefix, nodeDetails, refClass));
        }
        
        return sb.toString();
    }
    
    /**
     * Adds datatype property edges between an individual and the property values (for a given property).
     * 
     * @param  requestModel GraphRequestModel with visualization details
     * @param  individualName String source
     * @param  value String target
     * @param  property String edge label
     * @return GraphML String
     */
    private static String addDatatypePropertyEdge(GraphRequestModel requestModel,
    		final String individualName, final String value, final String property) {
    	
    	EdgeDetailsModel edgeDetails = EdgeDetailsModel.createEdgeDetailsModelForType(requestModel, "data");
    	edgeDetails.setEdgeLabel(property);
    	
        return GraphMLOutputDetails.addEdge(edgeDetails, individualName, value, property, 
        		EdgeFlagsModel.createEdgeFlagsFalse());
    }

    /**
     * Adds datatype value nodes for a given property.
     * 
     * @param  requestModel GraphRequestModel holding visualization details
     * @param  ontologyPrefix String that is the prefix of the owl:Ontology URI
     * @param  value String node contents
     * @return GraphML String
     */
    private static String addDatatypePropertyValue(GraphRequestModel requestModel,
    		final String ontologyPrefix, final String value) {
        
        NodeDetailsModel nodeDetails = NodeDetailsModel.createNodeDetailsModel(requestModel, "data");
        
        String nodeName = value.replaceAll("\"", "");
        GraphMLOutputDetails.getNodeDetails(ontologyPrefix, nodeDetails, nodeName, value);
        
        return GraphMLOutputDetails.addNode(nodeDetails, nodeName, value);
    }

    /**
     * Add individual nodes for all unique individuals.
     * 
     * @param  requestModel GraphRequestModel with individual visualization details
     * @param  ontologyPrefix String that is the prefix of the owl:Ontology URI
     * @param  individuals Set<String> defining the individuals' names/labels 
     * @return GraphML String
     * 
     */
    private static String addIndividuals(GraphRequestModel requestModel, final String ontologyPrefix,
    		Set<String> individuals) {
        
    	StringBuilder sb = new StringBuilder();
    	String visualization = requestModel.getVisualization();
    	
        // Get input from model for the node details
        NodeDetailsModel nodeDetails = NodeDetailsModel.createNodeDetailsModel(requestModel, "individual");

        for (String indiv : individuals) {
        	String indivName = GraphMLUtils.getPrefixedNameFromLabel(indiv);
        	// Get display changes based on the individual's name/label and visualization
        	String label = GraphMLUtils.getLabelForDisplay(ontologyPrefix, visualization,
        			indivName, indiv, true);
        	// Need to set/reset the node shape since it might be changed from "smallCircle" or
        	//   "none", to "ellipse" or "rectangle", when setting the display details 
        	//   (since the former values are specific to OntoGraph and are not yEd values)
        	nodeDetails.setNodeShape(requestModel.getIndividualNodeShape());
        	GraphMLOutputDetails.getNodeDetails(ontologyPrefix, nodeDetails, indivName, label);
        
        	sb.append(GraphMLOutputDetails.addNode(nodeDetails, indivName, label));
        }
        
        return sb.toString();
    }
    
    /**
     * Adds object property edges between an individual and the property values (for a given property).
     * If the value of an object property is an external individual, a new node will be created.
     * 
     * @param  requestModel GraphRequestModel with visualization details
     * @param  individualName String source
     * @param  value String targets
     * @param  property String edge label
     * @return GraphML String
     * 
     */
    private static String addObjectPropertyEdges(GraphRequestModel requestModel,
    		final String individualName, final String value, final String property) {

    	EdgeDetailsModel edgeDetails = EdgeDetailsModel.createEdgeDetailsModelForType(requestModel, "object");
    	edgeDetails.setEdgeLabel(property);
    	
        return GraphMLOutputDetails.addEdge(edgeDetails, individualName, value, property, 
        		EdgeFlagsModel.createEdgeFlagsFalse());
    }
    
    /**
	 * Draw typeOf relationships between types/classes and individuals.
	 * 
	 * @param  requestModel GraphRequestModel with individual visualization details
	 * @param  types List<String> of type(s) for the individual where the strings have
	 *              the format, "label (prefixed name)"
     * @param  classes List<ClassModel> holding the naming details for any owl:Classes referenced by
     *              the blank nodes
	 * @param  individualName String 
     * @param  relatedsAndRestrictions RelatedAndRestrictionModel containing lists of models 
     *             of "related" classes (equivalent, disjoints, and oneOfs), of connectives
     *             (unions, intersections and complementOfs) and restrictions (allValuesFrom,
     *             someValuesFrom, min/maxInclusive, ...)
	 * @return GraphML String
     * @throws OntoGraphException 
	 * 
	 */
	private static String addTypeOfEdges(GraphRequestModel requestModel, List<String> types, 
			List<ClassModel> classes, String individualName, 
			RelatedAndRestrictionModel relatedsAndRestrictions, Set<String> referencedClasses)
    				throws OntoGraphException {
		
	    StringBuilder sb = new StringBuilder();

        EdgeDetailsModel edgeDetails = EdgeDetailsModel.createEdgeDetailsModelForType(requestModel, "type");
        
        for (String type : types) {
        	if (!type.isEmpty()) {
	    		String typeName = GraphMLUtils.getPrefixedNameFromLabel(type);
	    		// The type may not be known (need to account for this)
	    		if (!typeName.contains(":")) {
	    			// Process a blank node
	    			sb.append(GraphMLUtils.addRelated(requestModel, classes, "", 
	    					Arrays.asList(TypeAndValueModel.builder()
	    						.type("eq")
	    						.value(typeName)
	    						.build()), 
	    					relatedsAndRestrictions, referencedClasses));
	    		}
	    		// Add specific source-target edge 
	    		sb.append(GraphMLOutputDetails.addEdge(edgeDetails, individualName, typeName, "typeOf", 
	    				EdgeFlagsModel.createEdgeFlagsFalse()));
        	}
    	
        }
	    
	    return sb.toString();
	}

	/**
	 * Add type nodes (classes) for all individuals
	 * 
	 * @param  requestModel GraphRequestModel with visualization details
     * @param  ontologyPrefix String that is the prefix of the owl:Ontology URI
	 * @param  types Set<String> of classes that are rdf:types of an individual. The
	 *            types are strings using the format, "label (prefixed name)"
	 * @return GraphML String
	 * 
	 */
	private static String addTypes(GraphRequestModel requestModel, final String ontologyPrefix, 
			Set<String> types) {
	    
		String visualization = requestModel.getVisualization();
		
        // Get input from model for the node details
        NodeDetailsModel nodeDetails = NodeDetailsModel.createNodeDetailsModel(requestModel, "class");
	    
        StringBuilder sb = new StringBuilder();
        for (String type : types) {
        	// Don't add a blank node type, since it was already processed
        	if (type.contains(":")) {
	        	String className = GraphMLUtils.getPrefixedNameFromLabel(type);
	        	// Get display changes based on the class name and visualization
	        	String typeLabel = GraphMLUtils.getLabelForDisplay(ontologyPrefix, visualization, 
	        			className, type, true);
	        	GraphMLOutputDetails.getNodeDetails(ontologyPrefix, nodeDetails, className, typeLabel);
	        	sb.append(GraphMLOutputDetails.addNode(nodeDetails, className, typeLabel));
        	}
        }
	    
	    return sb.toString();
	}
	
	/**
	 * Updates the set of strings defining all the unique individuals, IF the value string
	 * is truly unique. Note that a check for equality is not correct ... the check must be for
	 * "contains" since the value is a prefixed name, but the uniqueIndividuals string may use
	 * the format, "label (prefixed name)", if a label is defined.
	 * 
	 * @param value String
	 * @param uniqueIndividuals Set<String> which may be updated
	 * 
	 */
	private static void updateUniqueIndividuals(final String value, Set<String> uniqueIndividuals) {

	    boolean foundMatch = false;
	    for (String uniqueIndiv : uniqueIndividuals) {
	    	if (uniqueIndiv.contains(value)) {
	    		foundMatch = true;
	    		break;
	    	}
	    }
	    if (!foundMatch) {
	    	uniqueIndividuals.add(value);
	    }
	}
}
