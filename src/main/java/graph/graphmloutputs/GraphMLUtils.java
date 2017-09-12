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
import graph.models.BlankAndRelatedNodesModel;
import graph.models.ClassModel;
import graph.models.EdgeDetailsModel;
import graph.models.EdgeFlagsModel;
import graph.models.GraphRequestModel;
import graph.models.NodeDetailsModel;
import graph.models.NoteDetailsModel;
import graph.models.PropertyModel;
import graph.models.RelatedAndRestrictionModel;
import graph.models.RestrictionModel;
import graph.models.TypeAndValueModel;

/**
 * GraphMLUtils extends GraphMLOutputDetails and uses its routines to create GraphML 
 * text and boxes, nodes, images, edges and notes. 
 *
 */
public class GraphMLUtils extends GraphMLOutputDetails {
	
	// Frequently used string
	private static final String ONE_OF = "oneOf";
	
	/**
	 * Adds any superclasses that are blank nodes.
	 * 
	 * @param  requestModel GraphRequestModel
     * @param  classes List of ClassModels
	 * @param  blankNodeSuperClasses Set<TypeAndValueModel>
     * @param  relatedsAndRestrictions RelatedAndRestrictionModel containing lists of models 
     *             of "related" classes (equivalent, disjoints, and oneOfs), of connectives
     *             (unions, intersections and complementOfs) and restrictions (allValuesFrom,
     *             someValuesFrom, min/maxInclusive, ...)
     * @param  referencedClasses Set<String> defining any classes where an edge is added due to complement,
     *             union or intersection reference. This information is needed when doing a property diagram
     *             since the class may not be referenced in a domain or range declaration - and therefore will
     *             not be found in the property diagram without special handling.
	 * @return GraphML String
	 * @throws OntoGraphException 
	 * 
	 */
	public static String addBlankNodeSuperclasses(GraphRequestModel requestModel, 
			List<ClassModel> classes, Set<TypeAndValueModel> blankNodeSuperClasses,
			RelatedAndRestrictionModel relatedsAndRestrictions, Set<String> referencedClasses) 
					throws OntoGraphException {
		
		StringBuilder sb = new StringBuilder();
		
        // Add blank node superclasses
        if (!blankNodeSuperClasses.isEmpty()) { 
        	for (TypeAndValueModel bnsc : blankNodeSuperClasses) {
        		sb.append(GraphMLUtils.addRelated(requestModel, classes, bnsc.getType(), 
        				Arrays.asList(TypeAndValueModel.createTypeAndValueModel("super", bnsc.getValue())),
        				relatedsAndRestrictions, referencedClasses));
        	}
        }
		
        return sb.toString();
	}
	
    /**
     * Add a property edge in graphML.
     * 
     * @param  requestModel GraphRequestModel defining visualization settings
     * @param  ontologyPrefix String
     * @param  propModel PropertyModel with all info for a property
     * @return graphML String
     * 
     */
    public static String addPropertyEdges(GraphRequestModel requestModel,
    		final String ontologyPrefix, PropertyModel propModel) {   
        
        String visualization = requestModel.getVisualization();
        StringBuilder sb = new StringBuilder();
        
        EdgeDetailsModel edgeDetails = EdgeDetailsModel.createBasicEdgeDetailsModel(visualization,
                // TODO: Currently uses the format, "prefixed name" (except VOWL) but the property model's label  
                //    uses the format, "label (prefixed name)" if a label is defined. Allow selection of label,
                //    label (prefixed name), prefixed name or local name for a custom display.  
        		GraphMLUtils.getPrefixedNameFromLabel(propModel.getPropertyLabel()), WHITE, BLACK);
        
        // Note that the processing with "collapse edges" has created a list with the prefixed name and
        //    indicators of functional/inverse functional/transitive/... properties.
        if ("collapseTrue".equals(requestModel.getCollapseEdges())) {
        	// Take the label as-is since it has all the details about which obj props are functional, ...
        	edgeDetails.setEdgeLabel(propModel.getPropertyLabel());
        } 
        
        String propFullName = getPropertyEdgeDetails(requestModel, ontologyPrefix, edgeDetails, 
        		propModel);

        for (String domain : propModel.getDomains()) {
            String adjustedDomainName = getPrefixedNameFromLabel(domain);
            for (String range : propModel.getRanges()) {
                String adjustedRangeName = getPrefixedNameFromLabel(range);
                // Deal with class and property splitting for VOWL
                String newNode = EMPTY_STRING;
                if (range.contains(":") && "vowl".equals(visualization)) {  // VOWL processing for non-blank nodes
                	List<String> newDetails = handleVOWLSplitting(propModel.getPropertyName(), 
                			adjustedDomainName, adjustedRangeName, propModel.getPropertyType());
                	adjustedDomainName = newDetails.get(0);
                	adjustedRangeName = newDetails.get(1);
                	newNode = newDetails.get(2);
                }
                
                // If a new node was added, append that to the StringBuilder
                if (!newNode.isEmpty()) {
                	sb.append(newNode);
                }
                
                // Add the property edge between the domain and range
                sb.append(addEdge(edgeDetails, adjustedDomainName, adjustedRangeName, propFullName,
                        propModel.getEdgeFlags()));
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Returns the GraphML output for a class that is referenced as part of blank node processing in
     * the property diagram.
     * 
     * @param  ontologyPrefix String
	 * @param  nodeDetails NodeDetailsModel with info such as nodeShape, fillColor, visualization, ...
     * @param  refClass String defining the class which is referenced and which should be added to 
     *                         the GraphML output
     * @return GraphML String
     * 
     */
    public static String addReferencedClass(final String ontologyPrefix, 
    		NodeDetailsModel nodeDetails, final String refClass) {

    	String visualization = nodeDetails.getVisualization();

    	String className = refClass;
    	String label = refClass;
    	if (refClass.contains("(")) {
    		className = refClass.substring(refClass.lastIndexOf('(') + 1, 
    				refClass.lastIndexOf(')'));
    	}
    	
        label = getLabelForDisplay(ontologyPrefix, visualization, className, label, true);
		getNodeDetails(ontologyPrefix, nodeDetails, className, label);
		return addNode(nodeDetails, className, label);
    }
    
    /** 
     * Add the equivalentClasses, disjointWiths and oneOfs definitions to the graph.
     * 
     * @param  requestModel GraphRequestModel
     * @param  classes List<ClassModel> holding the naming conventions for any non-blank node entity
     *             referenced by a blank node
     * @param  className String defining the class to which the listed classes are related (may be empty
     * 						if the processing is part of property or individual graph)
     * @param  relatedList List<TypeAndValueModel> defining all the superclass for, equivalent, disjoint 
     *             and enums/oneOfs for a className. This is a list of models where the first entry is the
     *             "type" ("super", "eq", "dis", or "one") and the second entry is the "value" (the 
     *             related entity - class name or blank node ID).
     * @param  relatedsAndRestrictions RelatedAndRestrictionModel containing lists of models 
     *             of "related" classes (equivalent, disjoints, and oneOfs), of connectives
     *             (unions, intersections and complementOfs) and restrictions (allValuesFrom,
     *             someValuesFrom, min/maxInclusive, ...)
     * @param  referencedClasses Set<String> defining any classes where an edge is added due to complement,
     *             union or intersection reference. This information is needed when doing a property diagram
     *             since the class may not be referenced in a domain or range declaration - and therefore will
     *             not be found in the property diagram without special handling.
     * @return GraphML String
     * @throws OntoGraphException 
     * 
     */
    public static String addRelated(GraphRequestModel requestModel, List<ClassModel> classes, 
    		final String className, List<TypeAndValueModel> relatedList, 
    		RelatedAndRestrictionModel relatedsAndRestrictions, Set<String> referencedClasses) 
    				throws OntoGraphException {
 
    	StringBuilder sb = new StringBuilder();
    	List<String> enumIndividuals = new ArrayList<>();
    	
    	List<RestrictionModel> restrictions = relatedsAndRestrictions.getRestrictions();
    	
    	for (TypeAndValueModel related : relatedList) {
    		String typeOfRelationship = related.getType();
    		String relatedEntity = related.getValue();
    		if (!"one".equals(typeOfRelationship)) {
    			// Process a superclassOf, disjointWith or an equivalentClass 
    			//   (oneOf individuals are handled differently even though "oneOf" is also defined 
    			//   as an equivalentClass)
    			if (!relatedEntity.contains(":")) {
    				sb.append(blankNodeProcessing(requestModel, classes, 
    						BlankAndRelatedNodesModel.createBlankAndRelatedNodesModel(relatedEntity, className), 
    						typeOfRelationship, relatedsAndRestrictions, referencedClasses));
    			} else {
    				sb.append(addEquivalentDisjoint(requestModel, classes, className, relatedEntity, 
    						typeOfRelationship));
    			}
    		} else {
    			// Process an enumeration individual
    			enumIndividuals.add(relatedEntity);
    		}
    	}
    	
    	// Add a note defining the enum individuals, if there are any
    	if (!enumIndividuals.isEmpty()) {
    		// Determine type of className
    		boolean isClassRestriction = true;
    		for (RestrictionModel rm : restrictions) {
    			if (className.equals(rm.getRestrictionName())) {
    				isClassRestriction = rm.isClassRestriction();
    				break;
    			}
    		}
    		sb.append(addEnumerationIndividuals(requestModel, isClassRestriction,
    				BlankAndRelatedNodesModel.createBlankAndRelatedNodesModel(className + "OneOf", className), 
    				enumIndividuals, "eq"));
    	}
    	
    	return sb.toString();
    }
    
    /**
     * Draw edges from a class to its superclasses.
     * 
     * @param  className String of the class name (with a prefix)
     * @param  superClasses List<String> of superclasses 
     * @param  requestModel GraphRequestModel defining visualization settings
     * @return String GraphMLString
     * 
     */
    public static String addSubclassOfEdges(GraphRequestModel requestModel,
    		final String className, List<String> superClasses) {
        
        StringBuilder sb = new StringBuilder();
        
        EdgeDetailsModel edgeDetails = EdgeDetailsModel.createEdgeDetailsModelForType(requestModel, "subclass");
        EdgeFlagsModel edgeFlags = EdgeFlagsModel.createEdgeFlagsFalse();
        
        for (String superClass : superClasses) {
        	if (superClass.contains(":") && !OWL_THING.equals(superClass)) {
        		// Add specific source-target edges 
        		sb.append(addEdge(edgeDetails, className, superClass, "subClassOf", edgeFlags));
        	}
        }
        
        return sb.toString();
    }
    
    /**
	 * Processing to display the semantics of a blank node (a union, intersection or complement of
	 * other classes/blank nodes).
	 * 
	 * @param  requestModel GraphRequestModel
	 * @param  classes List<ClassModel> holding the naming conventions for referenced entities that are
	 *              not blank nodes
	 * @param  blankAndRelated BlankAndRelatedNodesModel defining the blank node and a className/related node
     *               (which may be empty if the processing is part of property or individual definition 
	 *               graphing, or may also be a blank node if there is nesting of the equivalent classes and 
	 *               connectives) 
	 * @param  typeOfRelationship String indicating whether the class is related to the blank node as a
	 *               its super class, via an equivalent to or disjoint with declaration, or via a  
	 *               connective ("super", "eq" or "dis", or "comp", "un" or "inter" respectively)
	 * @param  relatedsAndRestrictions RelatedAndRestrictionModel containing lists of models 
	 *             of "related" classes (equivalent, disjoints, and oneOfs), of connectives
	 *             (unions, intersections and complementOfs) and restrictions (allValuesFrom,
	 *             someValuesFrom, min/maxInclusive, ...)
	 * @param  referencedClasses Set<String> defining any classes where an edge is added due to complement,
	 *             union or intersection reference. This information is needed when doing a property or 
	 *             individual diagram since the class may not be referenced in a domain or range declaration 
	 *             (for properties) or listed at all (for individuals), but needs to be found in the diagram.
	 * @return GraphML String
	 * @throws OntoGraphException
	 * 
	 */
	public static String blankNodeProcessing(GraphRequestModel requestModel, List<ClassModel> classes, 
			BlankAndRelatedNodesModel blankAndRelated, final String typeOfRelationship, 
			RelatedAndRestrictionModel relatedsAndRestrictions, Set<String> referencedClasses) 
					throws OntoGraphException {
	
		StringBuilder sb = new StringBuilder();
		
		String blankNode = blankAndRelated.getBlankNode();
		String className = blankAndRelated.getRelatedNode();
		
		// Get all equivalent, disjoint and one of details
		Map<String, List<TypeAndValueModel>> equivalentsDisjointsOneOfs = 
				relatedsAndRestrictions.getEquivalentsDisjointsOneOfs();
		// Get all union, intersection and complement details
		Map<String, List<TypeAndValueModel>> connectives = 
				relatedsAndRestrictions.getConnectives();
		
		// Determine what the blank node represents - a restriction, oneOf, union, intersection or complement
		if (connectives.get(blankNode) == null && equivalentsDisjointsOneOfs.get(blankNode) == null) {
			// The blank node must represent a restriction
			return handleRestriction(requestModel, 
					BlankAndRelatedNodesModel.createBlankAndRelatedNodesModel(blankNode, className),
					relatedsAndRestrictions);
		} 
		
		// The blank node is a oneOf, union, intersection or complement declaration
		// Get the specific details
		Set<String> unions = new HashSet<>();
		Set<String> intersections = new HashSet<>();
		List<String> individuals = new ArrayList<>();
		String complement = getEquivalentDetails(blankNode, relatedsAndRestrictions, 
				unions, intersections, individuals);
	
		EdgeDetailsModel edgeDetails = EdgeDetailsModel.createEdgeDetailsModelForRelationship(
				requestModel, typeOfRelationship);
		// The createEdgeDetails sets up most of the properties correctly for both regular classes and blank nodes, 
		//    but in the case of blank nodes a few things need to be reset
		if (!className.contains(":")) {
			resetEdgeDetails(edgeDetails);
		}
		
		if (!complement.isEmpty()) {
			sb.append(processComplementUnionOrIntersection(edgeDetails, className, blankNode, "complement"));
			// Draw an edge from the image/note to the referenced complement class 
			sb.append(handleReferencedEquivalent(requestModel, classes, edgeDetails, 
					BlankAndRelatedNodesModel.createBlankAndRelatedNodesModel(blankNode, complement), 
					"comp", relatedsAndRestrictions, referencedClasses));
		}
		
		if (!unions.isEmpty()) {
			sb.append(processComplementUnionOrIntersection(edgeDetails, className, blankNode, "union"));
			for (String unionClass : unions) {
				// Draw an edge from the image/note to the referenced union classes
				sb.append(handleReferencedEquivalent(requestModel, classes, edgeDetails, 
						BlankAndRelatedNodesModel.createBlankAndRelatedNodesModel(blankNode, unionClass), 
						UN, relatedsAndRestrictions, referencedClasses));
			}
		}
	
		if (!intersections.isEmpty()) {
			sb.append(processComplementUnionOrIntersection(edgeDetails, className, blankNode, "intersection"));
			for (String interClass : intersections) {
				// Draw an edge from the image/note to the referenced intersection classes 
				sb.append(handleReferencedEquivalent(requestModel, classes, edgeDetails, 
						BlankAndRelatedNodesModel.createBlankAndRelatedNodesModel(blankNode, interClass), 
						INTER, relatedsAndRestrictions, referencedClasses));
			}
		}
		
		if (!individuals.isEmpty()) {
			sb.append(addEnumerationIndividuals(requestModel, true, 
					BlankAndRelatedNodesModel.createBlankAndRelatedNodesModel(blankNode, EMPTY_STRING), 
					individuals, "eq"));
		}
		
		return sb.toString();
	}
	
	/**
	 * Returns the entity name, correctly formatted for the visualization.
	 * This is important since VOWL does not display/distinguish different prefix names,
	 * only distinguishing between entities in the ontology and those external to it.
	 * 
     * @param  ontologyPrefix String that is the prefix of the owl:Ontology URI
     * @param  visualization String defining visualization type (e.g., "graffoo")
     * @param  entityName String of the class/property name with its prefix
     * @param  entityLabel String
     * @param  forNode boolean indicating that the processing is for a node (either a class or individual)
	 * @return String of the entity name as appropriate for the visualization
	 * 
	 */
	public static String getLabelForDisplay(final String ontologyPrefix,  //NOSONAR - Acknowledging complexity
			final String visualization, final String entityName, final String entityLabel,  
			boolean forNode)  {
		
		String label = entityLabel;
		if (GRAFFOO.equals(visualization) && entityLabel.contains("(")) {
			// Is a label with the prefixed name in parentheses ... 
			// For Graffoo, the label needs to be removed and the prefixed name returned
			//    (remembering to remove the closing parentheses)
			label = entityLabel.substring(entityLabel.lastIndexOf('(') + 1, entityLabel.length() - 1);
		} else if (VOWL.equals(visualization)) {
			if (entityLabel.contains("(")) {
				// Is a label with the prefixed name in parentheses ... which needs to be removed
				// Take care to remove the prefixed name which is in the LAST occurrence of "(...)"
				//    just in case the label uses parentheses also (the URI would not use parentheses)
				label = entityLabel.substring(0, entityLabel.lastIndexOf(" ("));
			} else if (entityLabel.startsWith("http") || entityLabel.startsWith("urn")) {
				// URI should either use "#' or '/' before the entity name
				if (entityLabel.contains("#")) {
					label = entityLabel.substring(entityLabel.indexOf('#') + 1);
				} else { 
					label = entityLabel.substring(entityLabel.lastIndexOf('/') + 1);
				}
			} else if (entityLabel.contains(":")) {
				// There is no label but the name is a prefixed class name ... and the prefix needs to be removed
				label = entityLabel.substring(entityLabel.indexOf(':') + 1);
			} 

			// Adjust the length of the label
			if (label.length() > 15) {
                label = label.substring(0, 12) + "...";
            } 
			// Make sure that we don't label blank nodes as "external" (the second check)
			if (forNode && entityName.contains(":") && !entityName.startsWith("owl")  //NOSONAR - Acknowledging 4 conditional ops
					&& (EMPTY_STRING.equals(ontologyPrefix) || !entityName.startsWith(ontologyPrefix))) {
            	label += NEW_LINE + "(external)";
            }
		} 

		return label;
	}
	
	/**
	 * Gets the prefixed name from a label that is either in the form, prefixedName or
	 *              label (prefixedName)
	 *              
	 * @param  label String
	 * @return prefixedName String
	 * 
	 */
	public static String getPrefixedNameFromLabel(final String label) {
		
		String prefixedName = label;
        if (label.contains("(")) {
        	prefixedName = label.substring(label.lastIndexOf('(') + 1, label.lastIndexOf(')'));
        }
		
		return prefixedName;
	}

    /**
	 * Add enumeration details to the graph and draw edge to parent class.
	 * 
	 * @param  requestModel GraphRequestModel
	 * @param  isClassRestriction boolean indicating that this is a request for a owl:Class OneOf definition 
	 *                 (if true) or an rdfs:Datatype OneOf definition (if false)
	 * @param  blankAndRelated BlankAndRelatedNodesModel defining the blank node and the class name that holds 
	 *                 the equivalent class definition (which may also be a blank node)
	 * @param  individualList List<String> of individuals in the enumeration 
	 * @param  type String defining the label on the edge
	 * @return String GraphMLString
	 * 
	 */
	private static String addEnumerationIndividuals(GraphRequestModel requestModel, //NOSONAR - Acknowledging complexity
			boolean isClassRestriction, BlankAndRelatedNodesModel blankAndRelated, 
			List<String> individualList, final String type) {
		
		StringBuilder sb = new StringBuilder();
		
		String visualization = requestModel.getVisualization();
		String graphType = requestModel.getGraphType();
		
		String openingLine = "Enumeration Individuals (OneOf)";
		String blankNode = blankAndRelated.getBlankNode();
		String enumClassName = blankAndRelated.getRelatedNode();

		// Calculate size of the note
	    int height = (individualList.size() + 3) * 13;
	    int width = getMaxLength(individualList, openingLine) * 9;
	    
	    // Set the note name and text
	    StringBuilder sbText = new StringBuilder();
	    sbText.append(NEW_LINE + openingLine + ":" + NEW_LINE);
        for (String indiv : individualList) {
            sbText.append("  " + indiv + NEW_LINE);
        }

        EdgeDetailsModel edgeDetails = EdgeDetailsModel.createEdgeDetailsModelForRelationship(
        		requestModel, "eq");  
        EdgeFlagsModel edgeFlags = EdgeFlagsModel.createEdgeFlagsFalse();
		
        // For Graffoo class and property diagrams, create a node indicating that this is an 
        //   enumeration and then draw each of the individuals
        if (GRAFFOO.equals(visualization)) {
        	if ("class".equals(graphType) || "property".equals(graphType)) {
	        	sb.append(addGraffooIndividualOrNode(isClassRestriction, blankNode, openingLine, 
	        			Integer.toString(width) + ".0", "50.0"));
	        	// Need to retain the edge label for use below, but for the edge to the individuals, 
	        	//   don't want any label
	        	String currEdgeLabel = edgeDetails.getEdgeLabel();
	        	edgeDetails.setEdgeLabel(EMPTY_STRING);
	        	for (String indiv : individualList) {
	        		// Draw the individual and an edge from it, to the node
	        		sb.append(addGraffooIndividualOrNode(false, indiv, indiv, EMPTY_STRING, EMPTY_STRING));
	        		sb.append(addEdge(edgeDetails, blankNode, indiv, ONE_OF, edgeFlags));
	        	}
	        	edgeDetails.setEdgeLabel(currEdgeLabel);
	        	
        	} else {
        		// If an individual diagram, already have a node for the "OneOf Blank Node", 
        		//    just draw the individuals and finish
	        	edgeDetails.setEdgeLabel(EMPTY_STRING);
	        	for (String indiv : individualList) {
	        		// Draw the individual and an edge from it, to the enumeration class name
	        		sb.append(addGraffooIndividualOrNode(false, indiv, indiv, EMPTY_STRING, EMPTY_STRING));
	        		sb.append(addEdge(edgeDetails, enumClassName, indiv, ONE_OF, edgeFlags));
	        	}
	        	
	        	return sb.toString();
        	}
        	
        } else {
        	if (type.contains("ValuesFrom")) {
        		edgeDetails.setEdgeLabel(type);
        	}
        	// Create a note with the enumeration details for all visualizations but Graffoo
    	    NoteDetailsModel noteDetails = NoteDetailsModel.createNoteDetailsModel(SOLID, height, width);
        	if (VOWL.equals(visualization)) {
        		noteDetails.setLineType("dashed");
        	}
        	sb.append(addNote(visualization, noteDetails, blankNode, sbText.toString()));
        }

        // Add an edge from the class to its enum individuals node or note (this is needed for a 
        //   Graffoo class diagram, which drops through to here, and for non-Graffoo visualizations)
        // Don't need this edge, if this is for a property graph - in which case, the enumClassName 
    	//   is empty
        if (!enumClassName.isEmpty()) {
        	sb.append(addEdge(edgeDetails, enumClassName, blankNode, ONE_OF, edgeFlags));
        }
        
	    // Return the note and edge details
	    return sb.toString();
	}

	/**
	 * Add equivalence and disjoint details for a class to another class.
	 * 
	 * @param  requestModel GraphRequestModel
	 * @param  classes List<ClassModel> to determine node labeling
	 * @param  className String which has the equivalent or disjoint definition
	 * @param  related String which is the equivalent or disjoint class
	 * @param  type String indicating whether this is an "eq" (equivalent) or "dis"
	 *             (disjoint) relationship
	 * @return GraphML String
	 * 
	 */
	private static String addEquivalentDisjoint(GraphRequestModel requestModel, List<ClassModel> classes,
			final String className, final String related, final String type) { 
	    
		StringBuilder sb = new StringBuilder();
		
		// Create the equivalent/disjoint edge
	    EdgeDetailsModel edgeDetails = EdgeDetailsModel.createEdgeDetailsModelForRelationship(
	    		requestModel, type);
	    EdgeFlagsModel edgeFlags = EdgeFlagsModel.createEdgeFlagsFalse();
	    // Get the prefixed names (just in case, since this can be called from referenced
	    //    classes in blank node processing)
	    String prefixedName = getPrefixedClassName(classes, className);
	    String prefixedRelated = getPrefixedClassName(classes, related);
			
		if ("dis".equals(type) && VOWL.equals(requestModel.getVisualization())) {
			// VOWL disjoint definition uses an image to connect the two classes
			// Add image but don't need text label
			// Also remove arrows
		    edgeDetails.setEdgeLabel(EMPTY_STRING);
		    edgeDetails.setTargetArrow("none");
			String imageName = prefixedName + prefixedRelated + "dis";
			sb.append(addImage(imageName, DISJOINT));
			// Draw edges to the classes from the image
		    sb.append(addEdge(edgeDetails, prefixedName, imageName, DISJOINT, edgeFlags));
		    sb.append(addEdge(edgeDetails, prefixedRelated, imageName, DISJOINT, edgeFlags));
		} else {
			if ("dis".equals(type)) {
				sb.append(addEdge(edgeDetails, prefixedName, prefixedRelated, DISJOINT, edgeFlags));
			} else {
				sb.append(addEdge(edgeDetails, prefixedName, prefixedRelated, "equiv", edgeFlags));
			}
		}
		
		return sb.toString();
	}
    
    /**
     * For a union, intersection or complement, add either an image (if the visualization is VOWL), 
     * a node (if the visualization is Graffoo) or a note to indicate the type.
     * 
     * @param  visualization String
     * @param  entityName String defining the name of the node which will be an image or a note
     * @param  type String indicating that either a union, intersection or complement is being defined
     * @return GraphML String
     * 
     */
    private static String addImageNodeOrNote(final String visualization,
    		final String entityName, final String type) {
    	
    	if (VOWL.equals(visualization)) {
    		return addImage(entityName, type);
    		
    	} else {
    	    String noteText = " Complement of ";
    	    if (type.startsWith(INTER)) {
    	    	noteText = " Intersection of ";
    	    } else if (type.startsWith(UN)) {
    	    	noteText = " Union of ";
    	    }
    	    
    	    int width = noteText.length() * 9;
    	    NoteDetailsModel noteDetails = NoteDetailsModel.createNoteDetailsModel(SOLID, 50, width);
    	    
            if (GRAFFOO.equals(visualization)) {
            	return addGraffooIndividualOrNode(true, entityName, noteText, 
            			Integer.toString(width) + ".0", "50.0");
            } else {
            	return addNote(visualization, noteDetails, entityName, noteText);
            }
    	}
    }
	
	/**
	 * Gets the prefixed class name for the referenced entity from the List<ClassModel>
	 * 
	 * @param  classes List<ClassModel> holding the various names/URIs for the entity
	 * @param  referencedEntity String
	 * @return String that is the prefixed name of the entity
	 * 
	 */
	private static String getPrefixedClassName(List<ClassModel> classes, 
			final String referencedEntity) {

		String refClassName = referencedEntity;
		if (referencedEntity.startsWith("http://") || referencedEntity.startsWith("urn:")) {
			for (ClassModel classDetails : classes) {
				if (referencedEntity.equals(classDetails.getClassName()) 
						|| referencedEntity.equals(classDetails.getFullClassName())) {	
					refClassName = classDetails.getClassName();
					break;
				}
			}
		}
		
		return refClassName;
	}
	
	/**
	 * For a blank node, get the details of the referenced classes if it is a complementOf, unionOf, 
	 * intersectionOf or oneOf declaration.
	 *  
	 * @param  blankNode String
     * @param  relatedsAndRestrictions RelatedAndRestrictionModel containing lists of models 
     *             of "related" classes (equivalent, disjoints, and oneOfs), of connectives
     *             (unions, intersections and complementOfs) and restrictions (allValuesFrom,
     *             someValuesFrom, min/maxInclusive, ...)
	 * @param  unions Set<String> of the unioned classes if the blank node is a unionOf declaration
	 * @param  intersections Set<String> of the intersected classes if the blank node is an intersectionOf
	 *              declaration
	 * @param  individuals List<String> of any individuals declared in the blank node's oneOf statement
	 * @return complement String if the blank node is a complementOf declaration
	 * @throws OntoGraphException 
	 * 
	 */
	private static String getEquivalentDetails(String blankNode, 
			RelatedAndRestrictionModel relatedsAndRestrictions, Set<String> unions, 
			Set<String> intersections, List<String> individuals) throws OntoGraphException {
		
		List<TypeAndValueModel> equivalents = new ArrayList<>();
		
		String complement = EMPTY_STRING;
		Map<String, List<TypeAndValueModel>> equivalentsDisjointsOneOfs =
				relatedsAndRestrictions.getEquivalentsDisjointsOneOfs();
		Map<String, List<TypeAndValueModel>> connectives = relatedsAndRestrictions.getConnectives();
		
		if (connectives.get(blankNode) != null) {
			equivalents.addAll(connectives.get(blankNode));
		}
		if (equivalentsDisjointsOneOfs.get(blankNode) != null) {
			equivalents.addAll(equivalentsDisjointsOneOfs.get(blankNode));
		}
	
		for (TypeAndValueModel equiv : equivalents) {
		    String relation = equiv.getType();
		    String entity = equiv.getValue();
			if ("comp".equals(relation)) {
				complement = entity;
			} else if (INTER.equals(relation)) {
				intersections.add(entity);
			} else if (UN.equals(relation)) {
				unions.add(entity);
				
			} else if ("one".equals(relation)) {
				// For the blank node to represent an enum is rare, but occurs in property and
				//    individual definition graphing if a domain or range references a oneOf blank node
				individuals.add(entity);
			} else {
				throw new OntoGraphException("Unknown connective, " + equiv + ", in the blank node processing for " 
						+ blankNode + ".");
			}
		}
		
		return complement;
	}
	
	/**
	 * Get the height and width of the restriction note/node.
	 * 
	 * @param  restrictionDetails List<String> with the text of the restriction
	 * @param  valuesFrom String specifying the class name or blank node of someValueFrom or
	 *              allValuesFrom restriction
	 * @return NoteDetailsModel holding the line type, height and width
	 * 
	 */
	private static NoteDetailsModel getRestrictionNoteDetails(final String visualization,
			List<String> restrictionDetails, final String valuesFrom) {

    	int height = (restrictionDetails.size() + 3) * 15;
	    if (!valuesFrom.isEmpty()) {
	    	// One line of text shorter
	    	height -= 15;
	    } 
	    String lineType = SOLID;
	    if (VOWL.equals(visualization)) {
	    	lineType = "dashed";
	    }
	    
	    int width = (getMaxLength(restrictionDetails, "Restricton:") + 2) * 10;	
	    return NoteDetailsModel.createNoteDetailsModel(lineType, height, width);
	}
	
	/**
	 * Get the correct arrow shapes, label, edge and label colors, etc. for the property edge.
	 * 
     * @param  requestModel GraphRequestModel defining visualization settings
     * @param  ontologyPrefix String
	 * @param  edgeDetails EdgeDetailsModel with info such as sourceArrow, edgeLabel, ...
	 * @param  PropertyModel with all info for a property
	 * @return propFullName String specifying the full property name with its namespace
	 * 
	 */
	private static String getPropertyEdgeDetails(GraphRequestModel requestModel, //NOSONAR - Acknowledging complexity
			final String ontologyPrefix, EdgeDetailsModel edgeDetails, PropertyModel propModel) {
        
		String visualization = requestModel.getVisualization();
        String propType = propModel.getPropertyType();
			
        if ("o".equals(propModel.getPropertyType())) {
            // Object property
            edgeDetails.setSourceArrow(requestModel.getObjPropSourceShape());
            edgeDetails.setTargetArrow(requestModel.getObjPropTargetShape());
            edgeDetails.setLineType(requestModel.getObjPropEdgeType());
            edgeDetails.setLineColor(requestModel.getObjPropEdgeColor());
            if (VOWL.equals(visualization)) {
                edgeDetails.setEdgeLabelBackground("#AACCFF");
            }
        } else if ("d".equals(propModel.getPropertyType())) {
            // Datatype property
            edgeDetails.setSourceArrow(requestModel.getDataPropSourceShape());
            edgeDetails.setTargetArrow(requestModel.getDataPropTargetShape());
            edgeDetails.setLineType(requestModel.getDataPropEdgeType());
            edgeDetails.setLineColor(requestModel.getDataPropEdgeColor());
            if (VOWL.equals(visualization)) {
                edgeDetails.setEdgeLabelBackground("#99CC66");
            }
        } else {
            // Annotation property
            edgeDetails.setSourceArrow(requestModel.getAnnPropSourceShape());
            edgeDetails.setTargetArrow(requestModel.getAnnPropTargetShape());
            edgeDetails.setLineType(requestModel.getAnnPropEdgeType());
            edgeDetails.setLineColor(requestModel.getAnnPropEdgeColor());
            if (VOWL.equals(visualization)) {
                edgeDetails.setEdgeLabelBackground("#99CC66");
            }
        }
        
        // Get the propertyName with its prefix
        String propPrefixedName = propType + propModel.getPropertyName();
        
        // Determine display changes based on the property name for VOWL
        if (VOWL.equals(visualization)) {
        	if (!propPrefixedName.startsWith(propType + ontologyPrefix)) {
                edgeDetails.setEdgeLabelBackground("#3366CC");
        	}
        	
        	String label = getLabelForDisplay(ontologyPrefix, VOWL, propPrefixedName, 
        			propModel.getPropertyLabel(), false); 
        	// Cut long labels
        	if (label.length() > 15) {
        	    label = label.substring(0, 12) + "...";
        	}
        	if ("a".equals(propModel.getPropertyType())) {
        		label = label + NEW_LINE + "(annotation)";
        	}
        	
        	edgeDetails.setEdgeLabel(label);
        }
        
        return propPrefixedName;
	}
	
	/**
	 * Either draw an edge to the referenced entity (if not a blank node) or continue drawing the complement,
	 * union or intersection details.
	 * 
	 * @param  requestModel GraphRequestModel
     * @param  classes List<ClassModel> holding the naming conventions of any referenced entities
     *             that are not blank nodes
	 * @param  edgeDetails EdgeDetailsModel with info such as sourceArrow, edgeLabel, ...
	 * @param  blankAndRelated BlankAndRelatedNodesModel defining the blank node and either a specific class/node 
	 *               name that is referenced in a connective declaration, or another blank node (meaning that further 
	 *               connectives are referenced)
	 * @param  typeOfConnective indicating whether the blank node is related to the referenced entity
	 *               in a complement, union or intersection declaration ("comp", "un" or "inter", respectively)
     * @param  relatedsAndRestrictions RelatedAndRestrictionModel containing lists of models 
     *             of "related" classes (equivalent, disjoints, and oneOfs), of connectives
     *             (unions, intersections and complementOfs) and restrictions (allValuesFrom,
     *             someValuesFrom, min/maxInclusive, ...)
     * @param  referencedClasses Set<String> defining any classes where an edge is added due to complement,
     *             union or intersection reference. This information is needed when doing a property diagram
     *             since the class may not be referenced in a domain or range declaration - and therefore will
     *             not be found in the property diagram without special handling.
     * @return GraphML String
	 * @throws OntoGraphException 
	 * 
	 */
	private static String handleReferencedEquivalent(GraphRequestModel requestModel, 
			List<ClassModel> classes, EdgeDetailsModel edgeDetails, BlankAndRelatedNodesModel blankAndRelated,  
			final String typeOfConnective, RelatedAndRestrictionModel relatedsAndRestrictions, 
			Set<String> referencedClasses) throws OntoGraphException {
		
		StringBuilder sb = new StringBuilder();
		
		String blankNode = blankAndRelated.getBlankNode();
		String referencedEntity = blankAndRelated.getRelatedNode();	
		
		if (!referencedEntity.contains(":")) {
			sb.append(blankNodeProcessing(requestModel, classes, 
					BlankAndRelatedNodesModel.createBlankAndRelatedNodesModel(referencedEntity, blankNode), 
					typeOfConnective, relatedsAndRestrictions, referencedClasses));
		} else {
			String refClassName = getPrefixedClassName(classes, referencedEntity);
			String refClassLabel = "";
			for (ClassModel classDetails : classes) {
				if (referencedEntity.equals(classDetails.getClassName()) 
						|| referencedEntity.equals(classDetails.getFullClassName())) {	
					refClassLabel = classDetails.getClassLabel();
					break;
				}
			}
			referencedClasses.add(refClassLabel);
			sb.append(addEdge(edgeDetails, blankNode, refClassName, typeOfConnective, 
					EdgeFlagsModel.createEdgeFlagsFalse()));
		}
		
		return sb.toString();
	}
	
	/**
	 * Add a node or note which represents a blank node that is a restriction, then add an edge 
	 * from the restriction details to the entityName.
	 * 
	 * @param  requestModel GraphRequestModel
	 * @param  blankAndRelated BlankAndRelatedNodesModel defining the blank node and the related entity name
	 *             (which may be empty if the processing is part of property definition graphing)
     * @param  relatedsAndRestrictions RelatedAndRestrictionModel containing lists of models 
     *             of "related" classes (equivalent, disjoints, and oneOfs), of connectives
     *             (unions, intersections and complementOfs) and restrictions (allValuesF
	 * @return GraphML String
	 * @throws OntoGraphException 
	 * 
	 */
	private static String handleRestriction(GraphRequestModel requestModel,  //NOSONAR - Acknowledging complexity
			BlankAndRelatedNodesModel blankAndRelated, RelatedAndRestrictionModel relatedsAndRestrictions) 
					throws OntoGraphException {
		
		StringBuilder sb = new StringBuilder();
		
		String visualization = requestModel.getVisualization();
		String blankNode = blankAndRelated.getBlankNode();
		String entityName = blankAndRelated.getRelatedNode();
		
		List<RestrictionModel> restrictions = relatedsAndRestrictions.getRestrictions();
		
		// Get the restriction details and format them into a single String with line feeds
		// TODO The restriction details are just written as predicate-object pairs - They should be parsed
		//    into human-readable text. For now, this will have to be done manually, during graph layout.
 		List<String> restrictionDetails = new ArrayList<>();
 		boolean isClassRestriction = false;
		for (RestrictionModel rm : restrictions) {
			if (blankNode.equals(rm.getRestrictionName())) {
				restrictionDetails.addAll(rm.getRestrictionDetails());
				isClassRestriction = rm.isClassRestriction();
				break;
			}
		}
		
	    StringBuilder restriction = new StringBuilder();
	    // Need to know if the restriction references other classes using the predicates, some/allValuesFrom
	    String valuesFrom = EMPTY_STRING;
	    String typeOfValuesFrom = "someValuesFrom";
		restriction.append("Restriction:");
	    for (String rd : restrictionDetails) {
	    	if (rd.contains("someValuesFrom ") || rd.contains("allValuesFrom ")) {
	    		// Track the referenced class in order to define an edge later
	    		valuesFrom = rd.substring(rd.indexOf("ValuesFrom ") + 11);
	    		if (rd.contains("allValuesFrom ")) {
	    			typeOfValuesFrom = "allValuesFrom";
	    		}
	    	} else {
	    		// Just add the text for display in the node or note
	    		restriction.append(NEW_LINE + rd);
	    	}
	    }
		
		// Get the height and width of the node/note
	    NoteDetailsModel noteDetails = getRestrictionNoteDetails(visualization, restrictionDetails, valuesFrom);
	    
	    // Add the node/note
        if (GRAFFOO.equals(visualization)) {
        	sb.append(addGraffooIndividualOrNode(isClassRestriction, blankNode, restriction.toString(), 
        			Integer.toString(noteDetails.getWidth()) + ".0", 
        			Integer.toString(noteDetails.getHeight()) + ".0"));
        } else {
        	sb.append(addNote(visualization, noteDetails, blankNode, restriction.toString()));
        }
        
        // Add an edge from the entityName to the node/note IF the entityName != blankNode
        // Otherwise, only the node definition was needed
    	EdgeDetailsModel edgeDetails = EdgeDetailsModel.createEdgeDetailsModelForRelationship(
    			requestModel, "eq");
    	resetEdgeDetails(edgeDetails);
        if (!entityName.equals(blankNode) && !entityName.isEmpty()) {
        	sb.append(addEdge(edgeDetails, entityName, blankNode, "restriction", 
        			EdgeFlagsModel.createEdgeFlagsFalse()));
        }
        
    	// Is this a someValuesFrom, allValuesFrom?  
    	if (!valuesFrom.isEmpty()) {
    		sb.append(processValuesFrom(requestModel, edgeDetails, blankNode, valuesFrom, typeOfValuesFrom,
    				relatedsAndRestrictions));
    	} 
    	
    	return sb.toString();
	}

	/**
	 * Adds an image, node or note about a complementOf, unionOf or intersectionOf definition
	 * and then adds an edge from a class to that image/node/note
	 * 
	 * @param  edgeDetails EdgeDetailsModel with info such as sourceArrow, edgeLabel, ...
	 * @param  className String (may be empty if the processing is part of property definition graphing)
	 * @param  entityName String identifying the name of the image, node or note
	 * @param  type String indicating that this is either a "complement", "union" or "intersection" 
	 * @return GraphML String
	 * 
	 */
	private static String processComplementUnionOrIntersection(EdgeDetailsModel edgeDetails, 
			final String className, final String entityName, final String type) {
		
		StringBuilder sb = new StringBuilder();
		
		// Add complement/union/intersection image or note
		sb.append(addImageNodeOrNote(edgeDetails.getVisualization(), entityName, type));
		// Drawn an edge from the image to the original class to which the blank node is associated
		if (!className.isEmpty()) {
			sb.append(addEdge(edgeDetails, className, entityName, type + "Of", 
					EdgeFlagsModel.createEdgeFlagsFalse()));
		}
		
		// Reset edgeDetails
		resetEdgeDetails(edgeDetails);
		
		return sb.toString();
	}

	/**
	 * Provides the details for a some/allValuesFrom note.
	 * 
	 * @param  requestModel GraphRequestModel
	 * @param  edgeDetails EdgeDetailsModel with info such as sourceArrow, edgeLabel, ...
	 * @param  blankNode String that is the original restriction
	 * @param  valuesFrom String defining the blank node that holds the details of the valuesFrom restriction
	 * @param  typeOfValuesFrom String defining the details as for "someValuesFrom" or "allValuesFrom"
     * @param  relatedsAndRestrictions RelatedAndRestrictionModel containing lists of models 
     *             of "related" classes (equivalent, disjoints, and oneOfs), of connectives
     *             (unions, intersections and complementOfs) and restrictions (allValuesF
	 * @return GraphML String
	 * @throws OntoGraphException 
	 * 
	 */
	private static String processValuesFrom(GraphRequestModel requestModel, EdgeDetailsModel edgeDetails,
			final String blankNode, final String valuesFrom, final String typeOfValuesFrom, 
			RelatedAndRestrictionModel relatedsAndRestrictions) throws OntoGraphException {
		
		StringBuilder sb = new StringBuilder();
		
		List<RestrictionModel> restrictions = relatedsAndRestrictions.getRestrictions();
        
        // Will not have a node/note for the blank node IF it is a type of rdfs:Datatype
        // So, add it 
        for (RestrictionModel rm2 : restrictions) {
            if (valuesFrom.equals(rm2.getRestrictionName()) && !rm2.isClassRestriction()) {
            	String rd = rm2.getRestrictionDetails().get(0);
            	if (!rd.contains(ONE_OF)) {
            		// Is another restriction, so process it
            		sb.append(handleRestriction(requestModel, 
            				BlankAndRelatedNodesModel.createBlankAndRelatedNodesModel(valuesFrom, valuesFrom), 
            				relatedsAndRestrictions));
            		break;
            	}
            		
            	// Is a oneOf definition and the details are in equivalentsDisjointsOneOfs
            	List<TypeAndValueModel> tvmList = 
            			relatedsAndRestrictions.getEquivalentsDisjointsOneOfs().get(valuesFrom);
            	List<String> individuals = new ArrayList<>();
            	for (TypeAndValueModel tvm : tvmList) {
            			individuals.add(tvm.getValue());	
            	}
            	sb.append(addEnumerationIndividuals(requestModel, false, 
            			BlankAndRelatedNodesModel.createBlankAndRelatedNodesModel(valuesFrom, blankNode), 
            			individuals, typeOfValuesFrom));
            	return sb.toString();
            }
        }

    	// Draw an edge from the restriction to the referenced class
        edgeDetails.setEdgeLabel(typeOfValuesFrom);
        sb.append(addEdge(edgeDetails, blankNode, valuesFrom, typeOfValuesFrom, 
        		EdgeFlagsModel.createEdgeFlagsFalse()));
        
        return sb.toString();
	}
}
