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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.complexible.stardog.ext.spring.SnarlTemplate;
import com.complexible.stardog.ext.spring.DataSource;

import org.apache.commons.codec.binary.Base64;

import graph.models.GraphRequestModel;
import graph.models.GraphResponseModel;
import graph.models.IndividualModel;
import graph.models.ClassModel;
import graph.models.EdgeFlagsModel;
import graph.models.PrefixModel;
import graph.models.PropertyModel;
import graph.models.RelatedAndRestrictionModel;
import graph.models.UMLClassModel;
import graph.graphmloutputs.ClassesGraphCreation;
import graph.graphmloutputs.GraphMLOutputDetails;
import graph.graphmloutputs.GraphMLUtils;
import graph.graphmloutputs.IndividualsGraphCreation;
import graph.graphmloutputs.PropertiesGraphCreation;
import graph.graphmloutputs.TitleAndPrefixCreation;
import graph.graphmloutputs.UMLGraphCreation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * GraphController handles an HTTP POST request for a graph to be 
 * created, based on the user's (input) OWL ontology
 *
 */
@RestController
@Component
public class GraphController extends RestExceptionHandler {
	
	// TODO OWL concepts not yet supported:
	//   deprecated, disjoint union (pairwise disjoint), negative datatype/object property assertion, 
	//   individual with a negative property assertion, inverse object property, equivalent and 
	//   disjoint property, sub-property and sub-annotation-property, ontology details (versionInfo, 
	//   imports, backwardCompatibleWith, incompatibleWith, priorVersion), differentFrom, AllDifferent, 
	//   distinct members, annotatedSource, annotatedTarget, annotatedProperty
	// Other concepts not yet supported: SWRL rules
    
    @Autowired private GraphDAO dao;
    
    // Frequently used strings
    private static final String BLACK = "#000000";
    private static final String CLASS = "class";
    private static final String EMPTY_STRING = "";
    private static final String GRAFFOO = "graffoo";
    private static final String GRAFFOO_CLASS = "Graffoo Class";
    private static final String INDIVIDUAL = "individual";
    private static final String NONE = "none";
    private static final String PROPERTY = "property";
    private static final String ROUND_RECTANGLE = "roundRectangle";
    private static final String SOLID = "solid";
    private static final String TRIANGLE_ARROW = "triangleSolid";
    private static final String TRIANGLE_ARROW_EMPTY = "triangleEmpty";
    private static final String UML = "uml";
    private static final String VOWL = "vowl";
    private static final String VOWL_CLASS = "VOWL Class";
    private static final String VOWL_EDGE = "VOWL Edge";
    private static final String WHITE = "#FFFFFF";
    
    // Mapping of standard Graffoo and VOWL colors to RGB values
    private static final Map<String, String> colors = createColorMap();
    private static Map<String, String> createColorMap() {
    	Map<String, String> colorMap = new HashMap<>();
        // From the various visualization types
    	colorMap.put(GRAFFOO_CLASS, "#FFFF00");
    	colorMap.put("Graffoo Individual", "#FF7FC1");
    	colorMap.put("Graffoo Datatype", "#CCFFCC");
    	colorMap.put("Graffoo Annotation Edge", "#993300");
    	colorMap.put("Graffoo Datatype Edge", "#008000");
    	colorMap.put("Graffoo Object Edge", "#000080");
    	colorMap.put(VOWL_CLASS, "#AACCFF");
    	colorMap.put("VOWL RDFS Class", "#CC99CC");
    	colorMap.put("VOWL Deprecated Class", "#CCCCCC");
    	colorMap.put("VOWL External Class", "#3366CC");
    	colorMap.put("VOWL Object Label Fill", "#AACCFF");
    	colorMap.put("VOWL Datatype Label Fill", "#99CC66");
    	colorMap.put("VOWL RDFS Datatype", "#FFCC33");
    	colorMap.put("VOWL Connectives", "#6699CC");
    	colorMap.put(VOWL_EDGE, "#000000");
    	return colorMap;
    }
    
    // TODO Add further customization options such as:
    //   * Whether to include subclasses, equivalencies, disjoints, restrictions, etc. in a class diagram
    //     and thereby segment/reduce what is shown in a diagram
    //   * Using a node or note for equivalencies/disjoints, the color of the note, note border, etc.
    //   * Displaying a property's label and/or prefixed name
    //   * What to display for "subclassOf" and "typeOf" labels (simpler, friendlier text?)
    //   * Whether the full class hierarchy is shown, only the top x levels, or the hierarchy starting at
    //     a particular class
    //   * Different format for datatype vs object restrictions
    
    /**
	 * Gets user input (including the ontology details) and uses it to create GraphML output
	 * 
	 * @param requestModel GraphRequestModel holding all details of the request from the browser
	 * @return GraphResponseModel 
     * @throws OntoGraphException
	 * 
	 */
	@RequestMapping(value="/graph", method= RequestMethod.POST)
	public GraphResponseModel graph(@RequestBody GraphRequestModel requestModel) 
			throws OntoGraphException {
	    
		// Validate inputs in case the REST processing is used independently from the browser
	    GraphRequestValidator.validateRequest(requestModel);

	    // Create the result/response
	    GraphResponseModel graphResponseModel =  new GraphResponseModel(
	    		requestModel.getGraphTitle(), requestModel.getVisualization(), 
	    		requestModel.getGraphType(), EMPTY_STRING);
	    
	    // Either the graph output is returned or an exception is thrown
	    graphResponseModel.setGraphML(createGraph(requestModel));
	    return graphResponseModel;
	}

	/**
	 * Defines the graphing conventions for a class/subclass hierarchy diagram
	 * 
	 * @param  requestModel GraphRequestModel holding all the details of the request from the browser. 
	 *              It is modified to set the correct values if the graphType is Graffoo or VOWL.
	 */
	private static void createClassConventions(GraphRequestModel requestModel) {
		
		String visualization = requestModel.getVisualization();
		
	    if (GRAFFOO.equals(visualization)) {
	        // Set based on graffoo specs
	    	requestModel.setClassNodeShape(ROUND_RECTANGLE);
	    	requestModel.setClassFillColor(colors.get(GRAFFOO_CLASS));
	    	requestModel.setClassTextColor(BLACK);
	    	requestModel.setClassBorderColor(BLACK);
	    	requestModel.setClassBorderType(SOLID);
	    	requestModel.setSubclassOfSourceShape(NONE);
	    	requestModel.setSubclassOfTargetShape(TRIANGLE_ARROW);
	    	requestModel.setSubclassOfLineColor(BLACK);
	    	requestModel.setSubclassOfLineType(SOLID);
	    	requestModel.setSubclassOfText("rdfs:subClassOf");
	        
	    } else if (VOWL.equals(visualization)) {
	    	// Set based on VOWL specs
	    	requestModel.setClassNodeShape("circle");
	    	requestModel.setClassFillColor(colors.get(VOWL_CLASS));
	    	requestModel.setClassTextColor(BLACK);
	    	requestModel.setClassBorderColor(BLACK);
	    	requestModel.setClassBorderType(SOLID);
	    	requestModel.setSubclassOfSourceShape(NONE);
	    	requestModel.setSubclassOfTargetShape(TRIANGLE_ARROW_EMPTY);
	    	requestModel.setSubclassOfLineColor(BLACK);
	    	requestModel.setSubclassOfLineType("dotted");
	    	requestModel.setSubclassOfText("Subclass of");
	        
	    } else {
	    	if ("both".equals(requestModel.getGraphType())) {
	    		requestModel.setClassNodeShape(requestModel.getObjNodeShape());
	    		requestModel.setClassFillColor(requestModel.getObjFillColor());
	    		requestModel.setClassTextColor(requestModel.getObjTextColor());
	    		requestModel.setClassBorderColor(requestModel.getObjBorderColor());
	    		requestModel.setClassBorderType(requestModel.getObjBorderType());
	    	}
	    	requestModel.setSubclassOfText("rdfs:subClassOf");
	    }
	}
	
	/**
	 * Processing to get the class details from an ontology and generate the GraphML output.
	 * 
	 * @param  requestModel GraphRequestModel
	 * @param  ontologyPrefix String defining the ontology namespace
	 * @param  classes List<ClassModel> defining all classes from the ontology
     * @param  relatedsAndRestrictions RelatedAndRestrictionModel containing lists of models 
     *             of "related" classes (equivalent, disjoints, and oneOfs), of connectives
     *             (unions, intersections and complementOfs) and restrictions (allValuesFrom,
     *             someValuesFrom, min/maxInclusive, ...)
	 * @return String with the GraphML output
	 * @throws OntoGraphException
	 * 
	 */
	private String createClassGraph(GraphRequestModel requestModel, final String ontologyPrefix,
	        List<ClassModel> classes, RelatedAndRestrictionModel relatedsAndRestrictions)
			        throws OntoGraphException {
        
        // Set the visualization conventions as needed
        createClassConventions(requestModel);
        
        // Generate the graph
        return ClassesGraphCreation.processClassHierarchy(requestModel, ontologyPrefix, classes, 
                relatedsAndRestrictions);
	}

	/**
	 * Performs all the processing to load the ontology into Stardog and then query it for the
	 * appropriate details to produce the graph requested in GraphRequestModel
	 * 
	 * @param  requestModel GraphRequestModel details
	 * @return GraphML String
	 * @throws OntoGraphException
	 * 
	 */
	private String createGraph(GraphRequestModel requestModel) throws OntoGraphException {  
	    
	    // Get user input from the GraphRequestModel object
	    String graphTitle = requestModel.getGraphTitle();
	    String cleanGraphTitle = graphTitle.replaceAll("\\s+", EMPTY_STRING);
	    String visualization = requestModel.getVisualization();
	
	    // Create a StringBuilder to hold the GraphML output
	    StringBuilder sb = new StringBuilder();
	    
	    // Create the SnarlTemplate to access the DB, and the array to hold the namespace prefix info
	    SnarlTemplate snarlTemplate = new SnarlTemplate();
	    List<PrefixModel> prefixes = new ArrayList<>();
	    
	    // Instantiate an empty model for the equivalents, disjoints, oneOfs, connectives, and restrictions
        RelatedAndRestrictionModel relatedsAndRestrictions = RelatedAndRestrictionModel.builder()
                .restrictions(new ArrayList<>())
                .connectives(new HashMap<>())
                .equivalentsDisjointsOneOfs(new HashMap<>())
                .build();
	    
	    try {
	        // Get the details for the arrays and maps, and begin generating the GraphML output
	        sb.append(getGraphDetails(requestModel, snarlTemplate, prefixes, relatedsAndRestrictions));

	        // Get the classes, which are needed in almost all graphs
	        List<ClassModel> classes = dao.getClasses(snarlTemplate, prefixes);
	        // Get any classes that are defined as equivalents or superclasses that are NOT 
	        //   defined as owl:Class in the ontology
	        classes.addAll(dao.getExternallyDefinedClasses(snarlTemplate, prefixes));
	        // Add any blank node oneOfs that are not defined as an equivalentClass
	        classes.addAll(dao.getStandaloneBlankNodes(snarlTemplate));
	        
	        // Determine the ontology's prefix (needed for VOWL to distinguish "external" classes)
        	List<String> ontPrefixAndCurrGraphML = new ArrayList<>(
        			Arrays.asList(EMPTY_STRING, EMPTY_STRING));
	        if (VOWL.equals(visualization)) {
	        	// Need to know the ontology's URI (only need it for VOWL to distinguish "external" classes)
	        	ontPrefixAndCurrGraphML.set(0, getOntologyPrefix(snarlTemplate, prefixes));
	        }
	       
	        // Generate the graph based on user's selection
	        sb.append(generateGraph(requestModel, ontPrefixAndCurrGraphML, snarlTemplate, prefixes, classes, 
	        		relatedsAndRestrictions));
	        
	    } catch (Exception e) {   //NOSONAR - Logged as part of OntoGraphException handling
			throw new OntoGraphException("Error creating the graph. Exception details: " + e.getMessage());
		} finally {
	        // Drop the database
	        dao.dropDatabase(cleanGraphTitle);
		}  
	    
	    // Close the GraphML XML and return the output
	    if (VOWL.equals(visualization)) {
	    	try {
				sb.append(GraphMLOutputDetails.closeVOWLGraph());
			} catch (IOException e) {	//NOSONAR - Logged as part of OntoGraphException handling
				throw new OntoGraphException("Error reading buffered image files to close a VOWL graph. "
				        + "IO Exception details: " + e.getMessage());
			}
	    } else {
	    	sb.append(GraphMLOutputDetails.closeGraph());
	    }
	    
	    return sb.toString();
	}
	
	/**
     * Defines the graphing conventions for an individuals graph
     * 
     * @param  requestModel GraphRequestModel holding all the details of the request from the browser. 
     *              It is modified to set the correct values if the graphType is Graffoo (no VOWL).
     *              
     */
	private static void createIndividualsConventions(GraphRequestModel requestModel) {
	    String visualization = requestModel.getVisualization();
	    
	    // No VOWL for individuals, so only set Graffoo
	    if (GRAFFOO.equals(visualization)) {
	        // Type (class) conventions
	        requestModel.setClassNodeShape(ROUND_RECTANGLE);
	        requestModel.setClassFillColor(colors.get(GRAFFOO_CLASS));
	        requestModel.setClassTextColor(BLACK);
	        requestModel.setClassBorderColor(BLACK);
	        requestModel.setClassBorderType(SOLID);
	        
	        // Individual conventions
	        requestModel.setIndividualNodeShape("smallCircle");
	        requestModel.setIndividualFillColor(colors.get("Graffoo Individual"));
	        requestModel.setIndividualTextColor(BLACK);
	        requestModel.setIndividualBorderColor(BLACK);
	        requestModel.setIndividualBorderType(SOLID);
	        
	        // Datatype conventions
	        requestModel.setDataNodeShape(NONE);
	        requestModel.setDataFillColor(WHITE);
	        requestModel.setDataTextColor(BLACK);
	        requestModel.setDataBorderColor(WHITE);
	        requestModel.setDataBorderType(SOLID);
	        
	        // Type edge
	        requestModel.setTypeOfSourceShape(NONE);
	        requestModel.setTypeOfTargetShape(TRIANGLE_ARROW);
	        requestModel.setTypeOfLineColor(BLACK);
	        requestModel.setTypeOfLineType(SOLID);
	        requestModel.setTypeOfText("rdf:type");
	        
	        // Property conventions
	        requestModel.setDataPropSourceShape(NONE);
	        requestModel.setDataPropTargetShape(TRIANGLE_ARROW);
	        requestModel.setDataPropEdgeColor(BLACK);
	        requestModel.setDataPropEdgeType(SOLID);
	        requestModel.setObjPropSourceShape(NONE);
	        requestModel.setObjPropTargetShape(TRIANGLE_ARROW);
	        requestModel.setObjPropEdgeColor(BLACK);
	        requestModel.setObjPropEdgeType(SOLID);
	    } else {
	    	requestModel.setTypeOfText("rdf:type");
	    }
	}

	/**
	 * Process to get individuals and their attributes to generate the GraphML output.
	 * 
	 * @param  requestModel GraphRequestModel details
	 * @param  ontologyPrefix String defining the ontology namespace
	 * @param  snarlTemplate SnarlTemplate connection details
	 * @param  prefixes List<PrefixModel> defining all known prefixes
	 * @param  classes List<ClassModel> defining all classes which may be referenced as an
	 *              individual type, or in the equivalentsDisjointsOneOfs or connectives maps
     * @param  relatedsAndRestrictions RelatedAndRestrictionModel containing lists of models 
     *             of "related" classes (equivalent, disjoints, and oneOfs), of connectives
     *             (unions, intersections and complementOfs) and restrictions (allValuesFrom,
     *             someValuesFrom, min/maxInclusive, ...)
	 * @return String with GraphML output
	 * @throws OntoGraphException
	 * 
	 */
	private String createIndividualsGraph(GraphRequestModel requestModel, final String ontologyPrefix, 
			SnarlTemplate snarlTemplate, List<PrefixModel> prefixes, List<ClassModel> classes, 
			RelatedAndRestrictionModel relatedsAndRestrictions) throws OntoGraphException {
		
	    // Get the individuals
	    List<IndividualModel> individuals = dao.getIndividuals(snarlTemplate, prefixes);
	    
	    // Set the visualization conventions as needed
	    createIndividualsConventions(requestModel);
	    
	    // Generate the graph
	    return IndividualsGraphCreation.processIndividualGraph(requestModel, ontologyPrefix,
	    		classes, individuals, relatedsAndRestrictions);
	}
	
	/**
	 * Defines the graphing conventions for an object and datatype property diagram
	 * 
	 * @param requestModel GraphRequestModel holding all the details of the request from the browser
	 *              It is modified to set the correct values if the graphType is Graffoo or VOWL
	 * 
	 */
	private static void createPropertyConventions(GraphRequestModel requestModel) {
	    
		String visualization = requestModel.getVisualization();
		
	    if (GRAFFOO.equals(visualization)) {
	        // Set based on Graffo specs
	    	requestModel.setDataNodeShape("parallelogramRight");
	    	requestModel.setDataFillColor(colors.get("Graffoo Datatype"));
	    	requestModel.setDataTextColor(BLACK);
	    	requestModel.setDataBorderColor(BLACK);
	    	requestModel.setDataBorderType(SOLID);
	    	requestModel.setDataPropSourceShape("circleEmpty");
	    	requestModel.setDataPropTargetShape(TRIANGLE_ARROW_EMPTY);
	    	requestModel.setDataPropEdgeColor(colors.get("Graffoo Datatype Edge"));
	    	requestModel.setDataPropEdgeType(SOLID);
	    	requestModel.setObjNodeShape(ROUND_RECTANGLE);
	    	requestModel.setObjFillColor(colors.get(GRAFFOO_CLASS));
	    	requestModel.setObjTextColor(BLACK);
	    	requestModel.setObjBorderColor(BLACK);
	    	requestModel.setObjBorderType(SOLID);
	    	requestModel.setObjPropSourceShape("circleSolid");
	    	requestModel.setObjPropTargetShape(TRIANGLE_ARROW);
	    	requestModel.setObjPropEdgeColor(colors.get("Graffoo Object Edge"));
	    	requestModel.setObjPropEdgeType(SOLID);
	    	requestModel.setAnnPropSourceShape("backslash");
	    	requestModel.setAnnPropTargetShape("angleBracket");
	    	requestModel.setAnnPropEdgeColor(colors.get("Graffoo Annotation Edge"));
	    	requestModel.setAnnPropEdgeType(SOLID);
	        
	    } else if (VOWL.equals(visualization)) {
	        // Set based on VOWL specs
	    	requestModel.setDataNodeShape("squareRectangle");
	    	requestModel.setDataFillColor(colors.get("VOWL RDFS Datatype"));
	    	requestModel.setDataTextColor(BLACK);
	    	requestModel.setDataBorderColor(BLACK);
	    	requestModel.setDataBorderType(SOLID);
	    	requestModel.setDataPropSourceShape(NONE);
	    	requestModel.setDataPropTargetShape(TRIANGLE_ARROW);
	    	requestModel.setDataPropEdgeColor(colors.get(VOWL_EDGE));
	    	requestModel.setDataPropEdgeType(SOLID);
	    	requestModel.setObjNodeShape("ellipse");
	    	requestModel.setObjFillColor(colors.get(VOWL_CLASS));
	    	requestModel.setObjTextColor(BLACK);
	    	requestModel.setObjBorderColor(BLACK);
	    	requestModel.setObjBorderType(SOLID);
	    	requestModel.setObjPropSourceShape(NONE);
	    	requestModel.setObjPropTargetShape(TRIANGLE_ARROW);
	    	requestModel.setObjPropEdgeColor(colors.get(VOWL_EDGE));
	    	requestModel.setObjPropEdgeType(SOLID);
	    	requestModel.setAnnPropSourceShape(NONE);
	    	requestModel.setAnnPropTargetShape(TRIANGLE_ARROW);
	    	requestModel.setAnnPropEdgeColor(colors.get(VOWL_EDGE));
	    	requestModel.setAnnPropEdgeType(SOLID);
	    	requestModel.setCollapseEdges("collapseFalse");
	    } 
	}

	/**
	 * Processing to get the details on datatype and object properties from an ontology,
	 * and generate the GraphML output.
	 * 
	 * @param  requestModel GraphRequestModel
     * @param  ontPrefixAndCurrGraphML List<String> holding the URI of the loaded ontology as the first value,
     *              and the current GraphML output as the second. Note that the second value will be an
     *              empty string (will not be used) unless the requestModel's graphType is "both" (class and
     *              property).
	 * @param  snarlTemplate SnarlTemplate 
	 * @param  prefixes List<PrefixModel> defining all known prefixes
	 * @param  classes List<ClassModel> defining all classes which may be referenced in property
	 *              domains or ranges, or by the equivalentsDisjointsOneOfs or connectives maps
     * @param  relatedsAndRestrictions RelatedAndRestrictionModel containing lists of models 
     *             of "related" classes (equivalent, disjoints, and oneOfs), of connectives
     *             (unions, intersections and complementOfs) and restrictions (allValuesFrom,
     *             someValuesFrom, min/maxInclusive, ...)
	 * @return String with the GraphML output
	 * @throws OntoGraphException
	 * 
	 */
	private String createPropertiesGraph(GraphRequestModel requestModel, List<String> ontPrefixAndCurrGraphML,
	        SnarlTemplate snarlTemplate, List<PrefixModel> prefixes, List<ClassModel> classes,
	        RelatedAndRestrictionModel relatedsAndRestrictions) throws OntoGraphException {
        
        // Get the domains, ranges, and linking properties
        List<PropertyModel> properties = dao.getProperties(snarlTemplate, prefixes);
        // If "collapse edges", then process the list of properties (VOWL does not allow collapsed edges)
        if (requestModel.getCollapseEdges().contains("True") 
        		&& !"vowl".equals(requestModel.getVisualization())) {
            properties = getPropertiesCollapsed(properties);
        }
        // Set the visualization conventions as needed
        createPropertyConventions(requestModel);
        
        // Generate the graph
        return PropertiesGraphCreation.processPropertyGraph(requestModel, ontPrefixAndCurrGraphML,
        		classes, properties, relatedsAndRestrictions);
	}

	/**
	 * Defines the graphing conventions for a UML graph (either a class/property or individual graph)
	 * 
	 * @param requestModel GraphRequestModel holding all details of the request
	 * 
	 */
	private static void createUMLConventions(GraphRequestModel requestModel) {
	    
	    // Same conventions for class or individual diagrams
	    requestModel.setClassFillColor(WHITE);
	    requestModel.setClassBorderColor(BLACK);
	    requestModel.setSubclassOfSourceShape(NONE);
	    requestModel.setSubclassOfTargetShape("triangleEmpty");
	    requestModel.setSubclassOfText(EMPTY_STRING);
	    requestModel.setSubclassOfLineColor(BLACK);
	    requestModel.setSubclassOfLineType(SOLID);
	    requestModel.setObjPropEdgeColor(BLACK);
	    requestModel.setObjPropEdgeType(SOLID);
	    requestModel.setObjPropSourceShape(NONE);
	    requestModel.setObjPropTargetShape("angleBracket");
	}

	/**
	 * Processing to get the details on all classes, datatype and object properties OR all individuals, 
	 * and generate the GraphML output. 
	 * 
	 * @param  requestModel GraphRequestModel holding all details of the request
	 * @param  snarlTemplate SnarlTemplate 
	 * @param  prefixes List<PrefixModel> defining all known prefixes
	 * @param  origClasses List<ClassModel> holding original class details
     * @param  relatedsAndRestrictions RelatedAndRestrictionModel containing lists of models 
     *             of "related" classes (equivalent, disjoints, and oneOfs), of connectives
     *             (unions, intersections and complementOfs) and restrictions (allValuesFrom,
     *             someValuesFrom, min/maxInclusive, ...)
	 * @return String with the GraphML output
	 * @throws OntoGraphException
	 * 
	 */
	private String createUMLGraph(GraphRequestModel requestModel, SnarlTemplate snarlTemplate, 
	        List<PrefixModel> prefixes, List<ClassModel> origClasses,
	        RelatedAndRestrictionModel relatedsAndRestrictions) throws OntoGraphException {
	    
	    // Set the visualization conventions as needed
        createUMLConventions(requestModel);
        
	    if (INDIVIDUAL.equals(requestModel.getGraphType())) {
	        // Get the instances
	        List<IndividualModel> instances = dao.getIndividuals(snarlTemplate, prefixes);
	        return UMLGraphCreation.processUMLInstanceGraph(requestModel, origClasses, 
	        		relatedsAndRestrictions, instances);
	    } else {
	        // It doesn't matter if the graph type is class, property or both - the result is the same 
    	    // Get the UML entities
    	    List<UMLClassModel> classes = dao.getClassesForUML(snarlTemplate, prefixes, origClasses);
    	    List<PropertyModel> properties = dao.getProperties(snarlTemplate, prefixes);
    	    List<PropertyModel> collProperties = new ArrayList<>();
    	    // If "collapse edges", then process the list of properties
            if (requestModel.getCollapseEdges().contains("True")) {
                collProperties = getPropertiesCollapsed(properties);
            }
    	    // Generate the GraphML
    	    return UMLGraphCreation.processUMLClassGraph(requestModel, origClasses, classes, properties, 
    	    		collProperties, relatedsAndRestrictions); 
	    }
	}
	
	/**
	 * Generates the GraphML output given the details of the GraphRequestModel. 
	 * 
	 * @param  requestModel GraphRequestModel
	 * @param  ontPrefixAndCurrGraphML List<String> where the first value is the ontology prefix 
	 *              (which is used by VOWL to determine "external" classes) and the second value
	 *              is the current GraphML output (which is set in the "both" class and property
	 *              processing)
	 * @param  snarlTemplate SnarlTemplate
	 * @param  prefixes List<PrefixModel> defining all known prefixes
	 * @param  classes List<ClassModel> defining all classes which may be referenced as an
	 *              individual type, or in the equivalentsDisjointsOneOfs or connectives maps
     * @param  relatedsAndRestrictions RelatedAndRestrictionModel containing lists of models 
     *             of "related" classes (equivalent, disjoints, and oneOfs), of connectives
     *             (unions, intersections and complementOfs) and restrictions (allValuesFrom,
     *             someValuesFrom, min/maxInclusive, ...)
	 * @return GraphML String
	 * @throws OntoGraphException
	 */
	private String generateGraph(GraphRequestModel requestModel, List<String> ontPrefixAndCurrGraphML, 
			SnarlTemplate snarlTemplate, List<PrefixModel> prefixes, List<ClassModel> classes, 
    		RelatedAndRestrictionModel relatedsAndRestrictions) throws OntoGraphException {

		StringBuilder sb = new StringBuilder();
		String graphType = requestModel.getGraphType();
		String visualization = requestModel.getVisualization();
		
        if (UML.equals(visualization)) {
            sb.append(createUMLGraph(requestModel, snarlTemplate, prefixes, classes, relatedsAndRestrictions));
            
        // Graffoo, VOWL or Custom visualization
        } else if (CLASS.equals(graphType)) {
            sb.append(createClassGraph(requestModel, ontPrefixAndCurrGraphML.get(0), classes,
                    relatedsAndRestrictions));

        } else if (INDIVIDUAL.equals(graphType)) {
            sb.append(createIndividualsGraph(requestModel, ontPrefixAndCurrGraphML.get(0), snarlTemplate, prefixes,
                    classes, relatedsAndRestrictions));
            
        } else if (PROPERTY.equals(graphType)) {
            sb.append(createPropertiesGraph(requestModel, ontPrefixAndCurrGraphML, snarlTemplate, prefixes, classes,
                    relatedsAndRestrictions));
            
        } else if ("both".equals(graphType)) {
        	// Note that both subclassOf conventions and property edge conventions are needed
            sb.append(createClassGraph(requestModel, ontPrefixAndCurrGraphML.get(0), classes,
                    relatedsAndRestrictions));
            // Track what is already captured in the GraphML output to avoid duplicate defns
            ontPrefixAndCurrGraphML.set(1, sb.toString());
            sb.append(createPropertiesGraph(requestModel, ontPrefixAndCurrGraphML, snarlTemplate, prefixes, classes,
                    relatedsAndRestrictions));
        
        // Error - unknown graph type
        } else {
        	throw new IllegalArgumentException("Unknown graph type: " + graphType); 	
        }
        
        return sb.toString();
	}
	
	/**
	 * Gets the details for several arrays and hash maps, as well as starting the GraphML output.
	 * 
	 * @param  requestModel GraphRequestModel
	 * @param  ontologyURI String
	 * @param  snarlTemplate SnarlTemplate
	 * @param  prefixes List<PrefixModel> defining all known prefixes
     * @param  relatedsAndRestrictions RelatedAndRestrictionModel containing lists of models 
     *             of "related" classes (equivalent, disjoints, and oneOfs), of connectives
     *             (unions, intersections and complementOfs) and restrictions (allValuesFrom,
     *             someValuesFrom, min/maxInclusive, ...)
	 * @return String with the GraphML output
	 * @throws OntoGraphException 
	 * 
	 */
	private String getGraphDetails(GraphRequestModel requestModel,
			SnarlTemplate snarlTemplate, List<PrefixModel> prefixes,
			RelatedAndRestrictionModel relatedsAndRestrictions) throws OntoGraphException {
	    
	    // Get user input from the GraphRequestModel object
	    String graphTitle = requestModel.getGraphTitle();
	    String cleanGraphTitle = graphTitle.replaceAll("\\s+", EMPTY_STRING);
	    String inputFile = requestModel.getInputFile();
	    String fileFormat = inputFile.substring(inputFile.lastIndexOf('.') + 1);
	    String vis = requestModel.getVisualization();
	
	    // Create a StringBuilder to hold the GraphML output
	    StringBuilder sb = new StringBuilder();
	    
        // Load the ontology into its own db in order to take advantage of prefix processing in Stardog
        // Track the database's "data source" for connection management
        DataSource ds;
		try {
			ds = dao.loadFileToDB(Base64.decodeBase64(requestModel.getFileData().split(",")[1]), 
					cleanGraphTitle, fileFormat);
	        // Update the snarl template for accessing the db
			snarlTemplate.setDataSource(ds);
	        // Do a query to check that some triples were loaded (that the ontology file is valid)
	        dao.checkDBLoad(snarlTemplate, cleanGraphTitle);
        
	        // Start the GraphML file
	        sb.append(GraphMLOutputDetails.setUpGraph());

		    // Get the ontology URI for the title box
		    String ontologyURI = dao.getOntologyURI(snarlTemplate);
		    if (ontologyURI == null) {
		    	ontologyURI = "None defined";
		    }
	        
	        // Get prefixes defined in the ontology 
	        prefixes.addAll(dao.getPrefixes(ds));
	        
	        // Add the title, prefix box 
			Collections.sort(prefixes, PrefixModel.prefixSort);
	        if (VOWL.equals(vis)) {
	        	sb.append(TitleAndPrefixCreation.addTitleAndPrefixes(graphTitle, ontologyURI, null));
	        } else {
	        	sb.append(TitleAndPrefixCreation.addTitleAndPrefixes(graphTitle, ontologyURI, prefixes));
	        }
	        
	        // Get details on any equivalent classes, disjoints, propositional connectives, ... 
	        // This info is needed in case blank nodes are used as superclasses, rdf:types for individuals, 
	        //   in domain or range definitions, ...
	        dao.getClassRelationships(snarlTemplate, prefixes, relatedsAndRestrictions);  
	        // Also get details on restrictions
	        relatedsAndRestrictions.setRestrictions(dao.getRestrictions(snarlTemplate, prefixes));
	        
		} catch (Exception e) {  //NOSONAR - Logged as part of OntoGraphException handling
			throw new OntoGraphException("Error loading and querying the database. Exception details: "
			        + e.getMessage());
		}
		
	    return sb.toString();
	}

	/**
	 * Determines the URI of the ontology, which is needed to determine "external" classes for
	 * VOWL visualization.
	 * 
	 * @param snarlTemplate SnarlTemplate 
	 * @param prefixes List<PrefixModel> defining all known prefixes
	 * @return String identifying the prefix associated with the ontology's URI
	 * 
	 */
	private String getOntologyPrefix(SnarlTemplate snarlTemplate, List<PrefixModel> prefixes) {
		
		String ontologyPrefix = EMPTY_STRING;
    	String ontologyURI = dao.getOntologyURI(snarlTemplate);
    	// Determine what prefix is associated with the URI
    	if (ontologyURI != null) {
	    	for (PrefixModel prefix : prefixes) {
	    		if (prefix.getUrl().startsWith(ontologyURI)) {
	    			ontologyPrefix = prefix.getPrefixName();
	    			break;
	    		}
	    	}
    	}
    	
    	return ontologyPrefix;
	}

	/**
	 * Reduce individual domain-range property edges to a set of property edges
	 * that each have a unique domain and range pair. The label for these property 
	 * edges is all the property names with that domain-range pair.
	 * 
	 * @param  models List of all individual PropertyModels
	 * @return List<PropertyModel> holding only unique domain-range properties
	 * 
	 */
	private List<PropertyModel> getPropertiesCollapsed(List<PropertyModel> models) {
	    
	    List<PropertyModel> reducedPropModels = new ArrayList<>();
	    Map<String, String> domainRangePairs = new HashMap<>();
	    
	    // Find each unique domain-range pair
	    for (PropertyModel pm : models) {
	    	String propName = pm.getPropertyName();
	    	String flagsText = GraphMLOutputDetails.getEdgeFlagsText(pm.getEdgeFlags());
	    	if (!flagsText.isEmpty()) {
	    		propName += "(" + flagsText.replaceAll(" ", ", ");
	    		propName = propName.substring(0, propName.length() - 2) + ")";
	    	}
	    	
            // Create or update a map entry for each domain and range
            for (String domain : pm.getDomains()) {
            	for (String range : pm.getRanges()) {
            		String domainRange = pm.getPropertyType() + domain + "$$" + range;
            		updateMap(domainRangePairs, domainRange, propName);
            	}
            }
	    }
	    
	    // Turn the domainRangePairs hash map into PropertyModels
	    for (Map.Entry<String, String> entry : domainRangePairs.entrySet()) {
	    	String key = entry.getKey();
	    	String domain = key.substring(1, key.indexOf("$$"));
	    	String range = key.substring(key.indexOf("$$") + 2);
	    	reducedPropModels.add(PropertyModel.builder()
	    			.fullPropertyName(EMPTY_STRING)
	    			.propertyName(GraphMLUtils.getPrefixedNameFromLabel(domain) + 
	    					GraphMLUtils.getPrefixedNameFromLabel(range))
	    			.propertyLabel(entry.getValue()) 
	    			.propertyType(key.substring(0, 1))
	    			.edgeFlags(EdgeFlagsModel.createEdgeFlagsFalse())
	    			.domains(Arrays.asList(domain))
	    			.ranges(Arrays.asList(range))
	    			.build());
	    }
	
	    return reducedPropModels;
	}
	
	/** 
	 * Updates a Map<String, String>'s value if the input parameter, key, exists; Otherwise, adds
	 * a new key-value pair
	 * 
	 * @param stringMap Map<String, String> to be updated
	 * @param key String
	 * @param newValue String
	 * 
	 */
	private void updateMap(Map<String, String> stringMap, final String key, final String newValue) {
	
		if (stringMap.containsKey(key)) {  
			// Pair is already in the hash map, just update the value
			String currValue = stringMap.get(key);
			stringMap.put(key, currValue + "," + System.getProperty("line.separator") + newValue);
		} else {
			// Pair needs to be added to the hash map
			stringMap.put(key, newValue);
		}
	}
}
