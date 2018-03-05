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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import graph.OntoGraphException;
import graph.models.AttributeLinesAndLength;
import graph.models.EntityAndRelatedNodesModel;
import graph.models.ClassModel;
import graph.models.EdgeDetailsModel;
import graph.models.EdgeFlagsModel;
import graph.models.GraphRequestModel;
import graph.models.IndividualModel;
import graph.models.PropertyModel;
import graph.models.RelatedAndRestrictionModel;
import graph.models.TypeAndValueModel;
import graph.models.UMLClassModel;

/**
 * TitleAndPrefixCreation transforms an ontology, producing a UML visualization.
 *
 */
public final class UMLGraphCreation {
    
    // Not meant to be instantiated
    private UMLGraphCreation() {
      throw new IllegalAccessError("UMLGraphCreation is a utility class and should not be instantiated.");
    }
   
    
    /**
     * Create a UML graph (class hierarchy, data properties as attributes and object
     * properties as associations) in GraphML.
     *
     * @param  requestModel GraphRequestModel defining visualization settings
     * @param  origClasses List<ClassModel> containing class details
     * @param  classes List<UMLClassModel> containing the additional UML details for all classes
     *              from origClasses
     * @param  properties List<PropertyModel> needed for the datatype and object properties
     * @param  collProperties List<PropertyModel> needed for the object properties if the
     *              requestModel indicates that edges should be collapsed 
     * @param  relatedsAndRestrictions RelatedAndRestrictionModel containing lists of models 
     *             of "related" classes (equivalent, disjoints, and oneOfs), of connectives
     *             (unions, intersections and complementOfs) and restrictions (allValuesFrom,
     *             someValuesFrom, min/maxInclusive, ...)
     * @return String GraphML output 
     * @throws OntoGraphException 
     * 
     */
    public static String processUMLClassGraph(GraphRequestModel requestModel, 
    		List<ClassModel> origClasses, List<UMLClassModel> classes, List<PropertyModel> properties, 
    		List<PropertyModel> collProperties, RelatedAndRestrictionModel relatedsAndRestrictions) 
    				throws OntoGraphException {
        
    	StringBuilder sb = new StringBuilder();
    	
        // The TypeAndValueModel is used here where the "type" is the class name and the 
        //   "value" is the superclass
        Set<TypeAndValueModel> blankNodeSuperClasses = new HashSet<>();  
        // Set up an EdgeDetailsModel in case there are any datatype restrictions to be drawn
        EdgeDetailsModel edgeDetails = setupUMLEdgeDetailsModel(requestModel);

        // Keep track of any blank nodes that are attribute ranges (datatype restrictions)
        Set<String> referencedBlankNodes = new HashSet<>();
        
        for (UMLClassModel umlModel : classes) {
        	String className = umlModel.getClassName();
            List<String> superClasses = umlModel.getSuperClasses();
            char classType = umlModel.getClassType();
            
            // Fix up the attributes to include their range(s) in the property label
            Set<String> specificBlankNodes = new HashSet<>();
	        List<String> attributeList = getAttributesWithPropDetails(umlModel.getAttributes(),
	            	properties, specificBlankNodes);
	        
            // If there are any referenced blank nodes as attribute types, then add an edge to them, 
            //    which is necessary since the type will be an arbitrary identifier and have no 
    		//    inherent meaning (the details are added by the blankNodeProcessing below)
            for (String rbn : specificBlankNodes) {
            		edgeDetails.setEdgeLabel("Attribute type: " + getBlankNodeId(rbn));
	                sb.append(GraphMLOutputDetails.addEdge(edgeDetails, className, rbn, "typeOf", 
	                		EdgeFlagsModel.createEdgeFlagsFalse()));
            }
            // Add the specific blank nodes to the overall list
            referencedBlankNodes.addAll(specificBlankNodes);
            
	        // Add each class with its attributes (datatype properties) 
	        sb.append(addClassOrInstance(className, umlModel.getClassLabel(), attributeList, 
	            		(classType == 'd' ? requestModel.getUmlDataNodeColor() : requestModel.getUmlNodeColor())));
	        
            // Add edges to the superclasses
            sb.append(GraphMLUtils.addSubclassOfEdges(requestModel, className, superClasses));
        
            // Find any superclasses that are blank nodes
            for (String sc : superClasses) {
            	if (!sc.contains(":")) {
            		blankNodeSuperClasses.add(TypeAndValueModel.createTypeAndValueModel(className, sc));
            	}
            }
        }

        Set<String> referencedClasses = new HashSet<>();
        // Add any blank node superclasses
        if (!blankNodeSuperClasses.isEmpty()) {
        	sb.append(GraphMLUtils.addBlankNodeSuperclasses(requestModel, origClasses, 
        			blankNodeSuperClasses, relatedsAndRestrictions, referencedClasses));
        }

        // Output the details for blank nodes as attribute types (datatype restrictions)  
        for (String node : referencedBlankNodes) {
    		// Add the node details
			sb.append(GraphMLUtils.blankNodeProcessing(requestModel, origClasses, 
					EntityAndRelatedNodesModel.createEntityAndRelatedNodesModel(node, ""),
					"", relatedsAndRestrictions, referencedClasses));
        }
        
        // Add object properties
        // Functional and inverse functional mean cardinality of [0..1] at one of the arrows of the 
        //     property edge, but this can't be done with the base yEd definitions. Similarly, cardinalities
        //     are not supported. Instead the phrase, "(functional)" or "(inverseFunctional)", is appended 
        //     to the property name.
        // This also allows the support of transitive, reflexive, symmetric, ... declarations.
        // TODO Determine how to address lack of compliance with UML, and how to support cardinalities in general
        if ("collapseTrue".equals(requestModel.getCollapseEdges())) {
        	sb.append(addProperties(requestModel, collProperties, origClasses, relatedsAndRestrictions));
        } else {
        	sb.append(addProperties(requestModel, properties, origClasses, relatedsAndRestrictions));
        }

        // Add any equivalent, disjoint, ... classes and oneOf individuals
        for (Map.Entry<String, List<TypeAndValueModel>> entry : 
        		relatedsAndRestrictions.getEquivalentsDisjointsOneOfs().entrySet()) {
            String className = entry.getKey();
            List<TypeAndValueModel> relatedList = entry.getValue();
            sb.append(GraphMLUtils.addRelated(requestModel, origClasses, className,
            		relatedList, relatedsAndRestrictions, referencedClasses));
        }
	
	    // Need to add any classes referenced in complement, union or intersectionOf declarations that are
	    //   NOT found - since they will be missing from the graph
        List<String> attributeList = new ArrayList<>();
        String nodeColor = requestModel.getUmlNodeColor();
	    for (String refClass : referencedClasses) {
	    	if (!sb.toString().contains("<node id=\"" + refClass + "\"")) {
	    		sb.append(addClassOrInstance(refClass, refClass, attributeList, nodeColor));
	    	}
	    }
        
        return sb.toString();
    }
    
    /**
     * Creates a UML instance graph containing all individuals, their attributes with values,
     * and the relationships between individuals in GraphML.
     * 
     * @param  requestModel GraphRequestModel defining visualization settings
	 * @param  classes List<ClassModel> holding the naming conventions for referenced entities that are
	 *              not blank nodes
	 * @param  relatedsAndRestrictions RelatedAndRestrictionModel containing lists of models 
	 *             of "related" classes (equivalent, disjoints, and oneOfs), of connectives
	 *             (unions, intersections and complementOfs) and restrictions (allValuesFrom,
	 *             someValuesFrom, min/maxInclusive, ...)
     * @param  individuals List<IndividualModel> 
     * @return GraphML String
     * @throws OntoGraphException 
     * 
     */
    public static String processUMLIndividualGraph(GraphRequestModel requestModel,
    		List<ClassModel> classes, RelatedAndRestrictionModel relatedsAndRestrictions,
    		List<IndividualModel> individuals) throws OntoGraphException {
        
        StringBuilder sb = new StringBuilder();
        String nodeColor = requestModel.getUmlNodeColor();

        // Set up an EdgeDetailsModel in case there are any blank nodes as the "type" of 1+ individuals
        EdgeDetailsModel edgeDetails = setupUMLEdgeDetailsModel(requestModel);
        
        // Keep track of known/graphed instances and those that are referenced in object properties
        //   since the latter may not be (fully) defined in the ontology
        Set<String> createdIndividuals = new HashSet<>();
        Set<String> referencedIndividuals = new HashSet<>();
        // Keep track of blank nodes that are a "type" of one or more individuals
        Set<String> referencedBlankNodes = new HashSet<>();
        
        for (IndividualModel indiv : individuals) {
            String individualName = indiv.getIndividualName();
            createdIndividuals.add(individualName);
            
            List<String> attributeList = getAttributesWithValues(indiv);
            
            // Add the individual node
            sb.append(addClassOrInstance(individualName, processIndividualLabel(indiv, referencedBlankNodes), 
            		attributeList, nodeColor));
            // Check the individual's types for a blank node
            for (String type : indiv.getTypeLabels()) {
            	if (!"".equals(type) && !type.contains(":")) {
            		// If the type is a blank node, then add an edge to it, which is necessary
            		//    since the type will be an arbitrary identifier and will have no inherent 
            		//    meaning (the details are added by the blankNodeProcessing below)
            		referencedBlankNodes.add(type);
            		edgeDetails.setEdgeLabel("Type: " + getBlankNodeId(type));
	                sb.append(GraphMLOutputDetails.addEdge(edgeDetails, individualName, type, "typeOf", 
	                		EdgeFlagsModel.createEdgeFlagsFalse()));
            	}
            }
           
            // Create property models for relationships and build edges
            // Don't need to worry about RDF properties since we have already separated 
            //    class-class relationships from class-datatype relationships
            // In UML, only the class-class relationships are graphed
            for (TypeAndValueModel r : indiv.getObjectProperties()) {
                String propName = r.getType();
                String propVal = r.getValue();
                PropertyModel p = PropertyModel.builder()
                        .propertyName(propName)
                        .propertyLabel(propName)
                        .fullPropertyName(propName)
                        .propertyType('o')
                        .domains(Arrays.asList(individualName))
                        .ranges(Arrays.asList(propVal))
                        .edgeFlags(EdgeFlagsModel.createEdgeFlagsFalse())
                        .build();
                sb.append(GraphMLUtils.addPropertyEdges(requestModel, "", p));
                referencedIndividuals.add(propVal);
            }
        }
        
        // Create nodes for any instances that are referenced in an object property, but are
        //    not defined with a "type"
        for (String ref : referencedIndividuals) {
            if (!createdIndividuals.contains(ref)) {
                String refLabel = ref + " : Unknown";
                sb.append(addClassOrInstance(ref, refLabel, Arrays.asList(""), nodeColor));
            }
        }
        
        // Define the details for blank nodes since their meaning is not obvious
        Set<String> referencedClasses = new HashSet<>();    
        for (String node : referencedBlankNodes) {
    		// Add the node details
			sb.append(GraphMLUtils.blankNodeProcessing(requestModel, classes, 
					EntityAndRelatedNodesModel.createEntityAndRelatedNodesModel(node, ""),
					"", relatedsAndRestrictions, referencedClasses));
        }
        
        // Now add any referenced classes that are not already present since these will be related
        //    to the blank nodes. Note that no "types" are included in the graph by default, except for
        //    the blank nodes that were added above, since this is an instance/individual graph.
        List<String> attributes = new ArrayList<>();	// Not needed
        for (String refClass : referencedClasses) {
        	if (!referencedBlankNodes.contains(refClass)) {
        		sb.append(addClassOrInstance(GraphMLUtils.getPrefixedNameFromLabel(refClass), 
        				refClass, attributes, nodeColor));
        	}
        }
        
        return sb.toString();
    }
    
    /**
     * Calculate the box sizes for a class or individual with its datatype properties/attributes, and
     * return the GraphML details for it.
     * 
     * @param  entityName String
     * @param  entityLabel String
     * @param  attributes List of (datatype) properties as Strings
     * @param  nodeColor String
     * @return GraphML String
     * 
     */
    private static String addClassOrInstance(final String entityName, final String entityLabel,
    		List<String> attributes, final String nodeColor) {
        
        // Get box sizes
        double len = entityLabel.length();
        int attribLines = 0;
        for (String attribute : attributes) {
        	// Check for line feeds and determine the max length based on the text with line feeds
        	// The first value in the List is the number of lines > 1, the second value is the max length
        	AttributeLinesAndLength attribDetails = GraphMLUtils.getAttributeDetails(attribute);
        	attribLines += attribDetails.getNumberOfLines();
        	if (attribDetails.getMaxLength() > len) {
        		len = attribDetails.getMaxLength();
        	}
        }
        
        int size = attributes.size() + attribLines;
        double width = len * 9;
        double height;
        
        // Set height based on number of attributes
        int[] sizes = {40, 60, 80, 100, 120, 140, 150, 160, 170, 180};
        if (size <= 9) {
        	height = sizes[size];
        } else {
        	height = (double) 180 + (10 * (size - 9));
        }
        
        // Add new node
        return GraphMLOutputDetails.addUMLNode(entityName, entityLabel, attributes, nodeColor, width, height);
    }
    
    /**
	 * Draw edges representing object properties between classes.
	 * 
	 * @param  requestModel GraphRequestModel defining visualization settings
	 * @param  properties List<PropertyModel>
	 * @param  classes List<ClassModel> 
     * @param  relatedsAndRestrictions RelatedAndRestrictionModel containing lists of models 
     *             of "related" classes (equivalent, disjoints, and oneOfs), of connectives
     *             (unions, intersections and complementOfs) and restrictions (allValuesFrom,
     *             someValuesFrom, min/maxInclusive, ...)
	 * @return GraphML String
	 * @throws OntoGraphException 
	 * 
	 */
	private static String addProperties(GraphRequestModel requestModel, List<PropertyModel> properties,
	        List<ClassModel> classes, RelatedAndRestrictionModel relatedsAndRestrictions) 
	        		throws OntoGraphException {
	
	    StringBuilder sb = new StringBuilder();
        List<String> attributes = new ArrayList<>();  // There are no attributes for added classes
        Set<String> addedClasses = new HashSet<>();
        
        // Get all the labels
        List<String> classLabels = new ArrayList<>();
        for (ClassModel c : classes) {
            classLabels.add(c.getClassLabel());
        }
	    
	    for (PropertyModel propModel : properties) {
	        // Skip if it's a datatype or annotation property (only object properties are graphed for OWL classes, 
	    	//   since datatype and annotation properties are shown "inside" the box for the class)
	    	char propType = propModel.getPropertyType();
	        if (propType == 'd' || propType == 'a') {
	        	continue;
	        }
	        
	        // Know that the property is an OWL object or an RDF property
	    	// Need to check the ranges for RDF properties to determine which are class-class
	    	//   relationships and which are class-datatype relationships
	        
	        // Get classes used as domains and ranges from model
	        List<String> domainsAndRanges = new ArrayList<>();
	        if (propType == 'o') {
	        	// For object properties, both the domain and range are classes
		        domainsAndRanges.addAll(propModel.getDomains());
		        domainsAndRanges.addAll(propModel.getRanges());
	        } else {
	        	// Is an RDF property - check the range to determine if it is a class and therefore,
	        	//    an edge is needed
	        	List<String> classRanges = new ArrayList<>();
	        	// If the range does not start with the RDFS, RDF or XSD prefix, then it is a class
	        	for (String range : propModel.getRanges()) {
	        		if (!range.equals("rdfs:Class") && !range.equals("rdfs:Resource")
	        				&& (range.contains("rdfs:") || range.contains("rdf:") || range.contains("xsd:"))) {
	        			continue;
	        		}
	        		classRanges.add(range);
	        	}
	        	if (!classRanges.isEmpty()) {
	        		domainsAndRanges.addAll(propModel.getDomains());
	        		domainsAndRanges.addAll(classRanges);
	        	}
	        }
        
	        // Only add an edge if there are actually domains/ranges defined
	        // Also add any classes used as domains or ranges, that are not defined in the owlClasses 
	        //   or not already added
	        if (!domainsAndRanges.isEmpty()) {
		        for (String dar : domainsAndRanges) {
		        	String prefixedName = GraphMLUtils.getPrefixedNameFromLabel(dar);
		        	// If the class name list does not have the class in it ... 
		        	// And the class has not already been added ...
		        	if (!classLabels.contains(dar) && !classLabels.contains(prefixedName)   
		        			&& !addedClasses.contains(dar)) {
		        		addedClasses.add(dar);
		        		if (!dar.contains(":")) {   
			                // Is a blank node ... 
			                // Passing in an empty string for the "related" class name since this is not a class only
			                //   diagram (we want to reuse all the logic, but not make an edge to the related class)
			                sb.append(GraphMLUtils.addRelated(requestModel, classes, "", 
			                        Arrays.asList(TypeAndValueModel.createTypeAndValueModel("eq", dar)),	
			                        relatedsAndRestrictions, new HashSet<>()));
		                } else {
		                	sb.append(addClassOrInstance(GraphMLUtils.getPrefixedNameFromLabel(dar), 
		                			dar, attributes, requestModel.getUmlNodeColor()));
		                }
		        	}
		        }
		        
		        // Second parameter is empty since a UML graph does not need the ontologyPrefix
		        sb.append(GraphMLUtils.addPropertyEdges(requestModel, "", propModel));
	        }
	    }
	    
	    return sb.toString();
	}

	/**
	 * Returns a unique, user-friendly name for a blank node.
	 * 
	 * @param  blankNode String
	 * @return String that identifies the node as a "blank node" and adds a unique suffix
	 *             (either from Stardog generated bnode id or the id assigned in the ontology)
	 *             
	 */
	private static String getBlankNodeId(final String blankNode) {

		if (blankNode.startsWith("bnode")) {
			return "blankNode" + blankNode.substring(blankNode.lastIndexOf('_'));
		} else {  // Blank node labeled within the ontology
			return "blankNode_" + blankNode;
		}
	}
	
	/** 
	 * Gets the type(s) for the attribute/datatype property and whether the property is functional.
	 * 
	 * @param  attributes List<String> of the applicable datatype properties
	 * @param  propertyDetails List<PropertyModel> defining all of the properties in the ontology
	 *               with their details
	 * @param  referencedBlankNodes Set<String> of any blank nodes/datatype restrictions 
	 *               encountered in the attributes
	 * @return List<String> of the form, "attribute_name : datatype" with an optional "[0..1]" tag
	 *               if the property is functional
	 *               
	 */
	private static List<String> getAttributesWithPropDetails(List<String> attributes, 
			List<PropertyModel> propertyDetails, Set<String> referencedBlankNodes) {
		
		List<String> attributeList = new ArrayList<>();
		
		for (String attr : attributes) {
			for (PropertyModel pm : propertyDetails) {
				if (pm.getPropertyName().contains(attr)) {
					attributeList.add(attr + " : " + getPropDetails(pm, referencedBlankNodes));
					break;
				}
			}
		}
		
		return attributeList;
	}
	
	/**
	 * Get the values and datatypes for attributes (datatype properties) when processing an 
	 * instance graph.
	 * 
	 * @param instance IndividualModel
	 * @return String listing attributes and specific values
	 * 
	 */
	private static List<String> getAttributesWithValues(IndividualModel instance) {
	    
        List<TypeAndValueModel> attributes = instance.getDatatypeProperties();
        List<String> attributeList = new ArrayList<>();
        // Build a list of the attributes
        for (TypeAndValueModel attr : attributes) {
            // Process the value to separate val and datatype
            String fullVal = attr.getValue();
            String val = fullVal;
            String dataType = "";
            if (fullVal.contains("\"")) {
            	val = fullVal.substring(1, fullVal.lastIndexOf('"'));
            }
            if (fullVal.contains("xsd:")) {
            	dataType = fullVal.substring(fullVal.indexOf("xsd:") + 4);
            }
            
            attributeList.add(attr.getType() + " : " + dataType + " = " + val);
        }
        
        return attributeList;
	}

	
	/** 
	 * Returns the appropriate UML details for an attribute 
	 * 
	 * @param  attributes List<String> of the applicable datatype properties
	 * @param  propertyModel PropertyModel defining the details for the property
	 * @param  referencedBlankNodes List<String> of any blank nodes/datatype restrictions 
	 *               encountered in the attributes
	 * @return String with the data type(s) with an optional "[0..1]" tag
	 *               if the property is functional
	 *               
	 */
	private static String getPropDetails(PropertyModel propertyModel, Set<String> referencedBlankNodes) {
		
		StringBuilder sb = new StringBuilder();
		
		// Add the "type"/range of the datatype property
		List<String> ranges = propertyModel.getRanges();
		int count = 0;
		for (String range : ranges) {
			if (count++ > 0) {
				sb.append(", ");
			}
			if ("rdfs:Literal".equals(range)) {
				sb.append("xsd:String");
			} else {
				if (!range.contains(":")) {  
					referencedBlankNodes.add(range);
					sb.append(getBlankNodeId(range));
				} else {
					sb.append(range);
				}
			}
		}
	    
	    // Add the cardinality for a functional property (inverseFunctional, asymmetric,
		//    symmetric and transitive do not apply to datatype/annotation properties)
	    if (propertyModel.getEdgeFlags().isFunctional()) {
	    	sb.append(" [0..1]");
	    }
	    
	    return sb.toString();
	}
	
	/**
	 * Builds a single string label containing the instance name and the types in UML format
	 * ("instanceName : typeName1, typeName2 ...")
	 * 
	 * @param instance IndividualModel
	 * @param referencedBlankNodes Set<String> of all blank nodes referenced as types.
	 *              The set is updated as new nodes are encountered.
	 * @return String defining the instance label
	 * 
	 */
	private static String processIndividualLabel(IndividualModel individual, Set<String> referencedBlankNodes) {
		
	    StringBuilder sb = new StringBuilder();
	    
	    List<String> types = individual.getTypeLabels();
	    
        sb.append(individual.getIndividualLabel() + " : ");
        
        if (types.isEmpty() || "".equals(types.get(0))) {
        	// The "type" is not defined in the ontology - Listed as "Unknown"
        	sb.append("Unknown");
        	return sb.toString();
        	
        } else {
        	int count = 0;
	        for (String type : types) {
	        	if (++count > 1) {
        			sb.append(", ");
        		}
	        	if (type.contains(":")) {    // Not a blank node
	        		sb.append(type);
	        	} else {
	        		referencedBlankNodes.add(type);
	        		sb.append(getBlankNodeId(type));	// Append a more user-friendly name
	        	}
	        }
        }
        
        return sb.toString();
	}
	
	/**
	 * Sets up an EdgeDetailsModel with model defaults.
	 * 
	 * @param  requestModel GraphRequestModel
	 * @return EdgeDetailsModel
	 * 
	 */
	private static EdgeDetailsModel setupUMLEdgeDetailsModel(GraphRequestModel requestModel) {

       EdgeDetailsModel edgeDetails = EdgeDetailsModel.createBasicEdgeDetailsModel(
    		   requestModel.getVisualization(), "", "#FFFFFF", "#000000");
       edgeDetails.setLineColor(requestModel.getObjPropEdgeColor());
       edgeDetails.setLineType(requestModel.getObjPropEdgeType());
       edgeDetails.setSourceArrow(requestModel.getObjPropSourceShape());
       edgeDetails.setTargetArrow(requestModel.getObjPropTargetShape());
       
       return edgeDetails;
	}
}
