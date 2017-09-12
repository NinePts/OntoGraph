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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import graph.OntoGraphException;
import graph.models.ClassModel;
import graph.models.GraphRequestModel;
import graph.models.NodeDetailsModel;
import graph.models.RelatedAndRestrictionModel;
import graph.models.TypeAndValueModel;

/**
 * ClassesGraphCreation visualizes OWL classes using a GraphML format, 
 * based on the requested visualization details.
 *
 */
public final class ClassesGraphCreation {
    
    // Not meant to be instantiated
    private ClassesGraphCreation() {
      throw new IllegalAccessError("ClassesGraphCreation is a utility class and should not be instantiated.");
    }

    
    /**
     * Create a class hierarchy graph (classes, superclasses, enumerations, equivalences, ...) in GraphML.
     * 
     * @param  requestModel GraphRequestModel defining visualization settings
     * @param  ontologyPrefix String holding the URI of the loaded ontology
     * @param  classes List of ClassModels
     * @param  relatedsAndRestrictions RelatedAndRestrictionModel containing lists of models 
     *             of "related" classes (equivalent, disjoints, and oneOfs), of connectives
     *             (unions, intersections and complementOfs) and restrictions (allValuesFrom,
     *             someValuesFrom, min/maxInclusive, ...)
     * @return String GraphMLString
     * @throws OntoGraphException 
     * 
     */
    public static String processClassHierarchy(GraphRequestModel requestModel, 
    		final String ontologyPrefix, List<ClassModel> classes,
    		RelatedAndRestrictionModel relatedsAndRestrictions)
    			throws OntoGraphException {
        
        StringBuilder sb = new StringBuilder();
        // The TypeAndValueModel is used here where the "type" is the class name and the 
        //   "value" is the superclass
        Set<TypeAndValueModel> blankNodeSuperClasses = new HashSet<>();  
        
        for (ClassModel cl : classes) {
            String className = cl.getClassName();
            // Add the classes to the graph
            sb.append(addClass(requestModel, ontologyPrefix, className, cl.getClassLabel()));
            
            List<String> superClasses = cl.getSuperClasses();
            if (!superClasses.isEmpty()) {
            	// Add subclassOf edges
            	sb.append(GraphMLUtils.addSubclassOfEdges(requestModel, className, superClasses));
            }
        
            // Find any superclasses that are blank nodes
            for (String sc : superClasses) {
            	if (!sc.contains(":")) {
            		blankNodeSuperClasses.add(TypeAndValueModel.createTypeAndValueModel(className, sc));
            	}
            }
        }
        
        Set<String> referencedClasses = new HashSet<>();  // Not needed for class definitions, but used in
                      								      // the method calls below
        // Add any blank node superclasses
        sb.append(GraphMLUtils.addBlankNodeSuperclasses(requestModel, classes, blankNodeSuperClasses,
        		relatedsAndRestrictions, referencedClasses));
        
        // Add any equivalent, disjoint, ... classes and oneOf individuals
        for (Map.Entry<String, List<TypeAndValueModel>> entry : 
        		relatedsAndRestrictions.getEquivalentsDisjointsOneOfs().entrySet()) {
            String className = entry.getKey();
            List<TypeAndValueModel> relatedList = entry.getValue();
            sb.append(GraphMLUtils.addRelated(requestModel, classes, className, relatedList, 
            		relatedsAndRestrictions, referencedClasses));
        }
        
        return sb.toString();
    }
    
    /**
     * Add all classes defined in the ontology.
     * 
     * @param  requestModel GraphRequestModel defining visualization settings
     * @param  ontologyPrefix String that is the prefix of the owl:Ontology URI
     * @param  className String of the class name (beginning with a prefix and ":")
     * @param  classLabel String 
     * @return String GraphMLString
     * 
     */
    private static String addClass(GraphRequestModel requestModel, final String ontologyPrefix,
    		final String className, final String classLabel) {
        
    	String visualization = requestModel.getVisualization();
    	
        // Defaults
        NodeDetailsModel nodeDetails = NodeDetailsModel.createNodeDetailsModel(requestModel, "class");
        		
        // Get display changes based on the class name and visualization
        // TODO: Allow selection between label, label (prefixed name), prefixed name or 
        //     local name for a custom display
        String label = GraphMLUtils.getLabelForDisplay(ontologyPrefix, visualization, className, 
        		classLabel, true);
        GraphMLOutputDetails.getNodeDetails(ontologyPrefix, nodeDetails, className, label);
        
        // Generate node
        return GraphMLOutputDetails.addNode(nodeDetails, className, label);
    }
}
