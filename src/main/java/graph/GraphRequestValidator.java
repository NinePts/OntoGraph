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

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import graph.models.GraphRequestModel;

/**
 * GraphRequestValidator contains methods to validate the input from a GraphRequestModel.
 * 
 */
public class GraphRequestValidator {
    
    // Frequently used strings
	private static final String CLASS = "class";
	private static final String DATATYPE_NODE_SHAPE = "datatype node shape";
	private static final String DATATYPE_NODE_FILL_COLOR = "datatype node fill color";
	private static final String DATATYPE_NODE_TEXT_COLOR = "datatype node text color";
	private static final String DATATYPE_NODE_BORDER_COLOR = "datatype node border color";
	private static final String EMPTY_STRING = "";
	private static final String INDIVIDUAL = "individual";
	
	// Hex colors
    private static final Pattern colorPattern = Pattern.compile("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
    
    // Strings for validation
    private static final List<String> reasoningTypes = Arrays.asList("reasoningTrue", "reasoningFalse");
    private static final List<String> arrowTypes = Arrays.asList("angleBracket", "backslash", "circleSolid", 
    		"circleEmpty", "diamondSolid", "diamondEmpty", "triangleSolid", "triangleEmpty", "none");
    private static final List<String> collapseTypes = Arrays.asList("collapseTrue", "collapseFalse");
    private static final List<String> graphTypes = Arrays.asList(CLASS, "property", INDIVIDUAL, "both"); 
    private static final List<String> lineTypes = Arrays.asList("solid", "dashed", "dotted", "dashedDotted", "none");
    private static final List<String> nodeShapes = Arrays.asList("circle", "smallCircle", "diamond", "ellipse",
    		"hexagon", "parallelogramRight", "parallelogramLeft", "roundRectangle", "squareRectangle",
    		"none");
    private static final List<String> visualizationTypes = Arrays.asList("custom", "graffoo", "vowl", "uml");
    
    // Not meant to be instantiated
    private GraphRequestValidator() {
      throw new IllegalAccessError("GraphRequestValidator is a utility class and should not be instantiated.");
    }
    
    /**
     * Validates that the user has provided correct input information.
     * Mainly used to catch errors if REST processing is used independently from the browser.
     * 
     * @param GraphRequestModel holding all details of the user request
     * 
     */
    public static void validateRequest(GraphRequestModel requestModel) {
    	
    	String graphType = requestModel.getGraphType();
        String visualization = requestModel.getVisualization();
        
        // Check inputs
        String errorString = EMPTY_STRING;
        errorString = validateString(requestModel.getGraphTitle(), null, "graph title", errorString);
	    errorString = validateString(requestModel.getReasoning(), reasoningTypes, 
	    		"use reasoning T/F", errorString);
        errorString = validateString(requestModel.getInputFile(), null, "input file", errorString);
        errorString = validateString(visualization, visualizationTypes, "visualization type", errorString);
        errorString = validateString(graphType, graphTypes, "graph type", errorString);
        
        if ("uml".equals(visualization)) {
        	errorString = validateColor(requestModel.getUmlNodeColor(), "UML node fill color", errorString);
        	errorString = validateColor(requestModel.getUmlDataNodeColor(), "UML datatype node fill color", errorString);
        	if (!INDIVIDUAL.equals(graphType)) {
        		// Class, property or both class and property all translate to a class diagram for UML
        		requestModel.setGraphType(CLASS);
        	}
        }
        
        if ("vowl".equals(visualization)) {
        	if (INDIVIDUAL.equals(graphType)) {
	        	// Cannot have an individual graph type for VOWL, since instances/individuals are not defined 
	        	//   in VOWL 2
	    	    errorString = updateErrorString(errorString, 
	    	    		"A selection of an 'Individual' graph type is not valid for a VOWL visualization.");
        	}
        	if ("collapseTrue".equals(requestModel.getCollapseEdges())) {
        		// Collapsing edges is not supported by VOWL, it uses class and property splitting instead
	    	    errorString = updateErrorString(errorString, 
	    	    		"Defining collapsed edges is not valid for a VOWL visualization since it mandates "
	        			+ "the use of class and property splitting.");
        	}
        }
        
        if ("custom".equals(visualization)) {
            if (CLASS.equals(graphType)) {
                errorString = validateCustomClassGraph(requestModel, errorString);
            } else if (INDIVIDUAL.equals(graphType)) {
                errorString = validateCustomIndividualGraph(requestModel, errorString);
            } else if ("property".equals(graphType)) {
                errorString = validateCustomPropertyGraph(requestModel, true, errorString);
            } else {
                errorString = validateCustomPropertyGraph(requestModel, false, errorString);
            }
        }
        
        if (!EMPTY_STRING.equals(errorString)) {
        	throw new IllegalArgumentException(errorString);
        }
    }
    
    /**
     * Updates the error string with the new error on a new line. If the error string is empty, the new string is
     * returned.
     * 
     * @param currentError (String) the current error string
     * @param newError (String) the new error string
     * @return errorString updated by the new error message (if any)
     * 
     */
    private static String updateErrorString(final String currentError, final String newError) {
    	
    	if (!EMPTY_STRING.equals(newError)) {
    		if (!EMPTY_STRING.equals(currentError)) {
    			return currentError + System.getProperty("line.separator") + newError;
    		} else {
    			return newError;
    		}
    	} else {
    		return currentError;
    	}
    }
    
    /**
     * Validates that each color input is in hex color code format.
     * 
     * @param  color (String) being checked
	 * @param  description (String) indicating the type of value being check
	 * @param  currentError which is the current error message
	 * @return errorString updated by any error messages added by the checks in this method
	 * 
     */
    private static String validateColor(final String color, final String description, 
    		final String currentError) {
    	
    	String error = EMPTY_STRING;
        Matcher m = colorPattern.matcher(color);
        if (!m.matches()) {
            error = "Please provide the 3- or 6-digit hex color code, beginning with # for " + description + ".";  
        }
        
        return updateErrorString(currentError, error);
    }
    
    /**
	 * Validates the user input for a custom class graph. Checks that all required customization options are provided
	 * and that they are one of the expected values.
	 * 
	 * @param requestModel holding all details of the user request
	 * @param  currentError which is the current error message
	 * @return errorString updated by any error messages added by the checks in this method
	 * 
	 */
	private static String validateCustomClassGraph(GraphRequestModel requestModel, 
			final String currentError)  {
	    
	    String errorString = currentError;
	    errorString = validateString(requestModel.getClassNodeShape(), 
	    		nodeShapes, "class node shape", errorString);
	    errorString = validateString(requestModel.getClassBorderType(),
	    		lineTypes, "class node border type", errorString);
	    errorString = validateString(requestModel.getDataNodeShape(), 
	    		nodeShapes, DATATYPE_NODE_SHAPE, errorString);
	    errorString = validateString(requestModel.getDataBorderType(),
	    		lineTypes, "datatype node border type", errorString);
	    
	    errorString = validateString(requestModel.getSubclassOfSourceShape(),
	    		arrowTypes, "subclassOf source arrow shape", errorString);
	    errorString = validateString(requestModel.getSubclassOfTargetShape(),
	    		arrowTypes, "subclassOf target arrow shape", errorString);
	    errorString = validateString(requestModel.getSubclassOfLineType(),
	    		lineTypes, "subclassOf line type", errorString);
	    
	    errorString = validateColor(requestModel.getClassFillColor(), "class node fill color", errorString);
	    errorString = validateColor(requestModel.getClassTextColor(), "class node text color", errorString);
	    errorString = validateColor(requestModel.getClassBorderColor(), "class node border color", errorString);
	    errorString = validateColor(requestModel.getSubclassOfLineColor(), "subclassOf line color", errorString);

	    errorString = validateColor(requestModel.getDataFillColor(), DATATYPE_NODE_FILL_COLOR, errorString);
	    errorString = validateColor(requestModel.getDataTextColor(), DATATYPE_NODE_TEXT_COLOR, errorString);
	    return validateColor(requestModel.getDataBorderColor(), DATATYPE_NODE_BORDER_COLOR, errorString);
	}

	/**
	 * Validates the user input for a custom individual graph. Checks that all required customization options are
	 * provided and that they are one of the expected values.
	 * 
	 * @param  requestModel holding all details of the user request
	 * @param  currentError which is the current error message
	 * @return errorString updated by any error messages added by the checks in this method
	 * 
	 */
	private static String validateCustomIndividualGraph(GraphRequestModel requestModel,
	        final String currentError) {

	    String errorString = currentError;
	    errorString = validateString(requestModel.getDataNodeShape(), 
	    		nodeShapes, DATATYPE_NODE_SHAPE, errorString);
	    errorString = validateString(requestModel.getIndividualNodeShape(), 
	    		nodeShapes, "individual node shape", errorString);
	    errorString = validateString(requestModel.getClassNodeShape(), 
	    		nodeShapes, "type node shape", errorString);
	    errorString = validateString(requestModel.getDataBorderType(),
	    		lineTypes, "datatype node border type", errorString);
	    errorString = validateString(requestModel.getIndividualBorderType(),
	    		lineTypes, "individual node border type", errorString);
	    errorString = validateString(requestModel.getClassBorderType(),
	    		lineTypes, "type node border type", errorString);
	    
	    errorString = validateString(requestModel.getTypeOfSourceShape(),
	    		arrowTypes, "typeOf source arrow shape", errorString);
	    errorString = validateString(requestModel.getTypeOfTargetShape(),
	    		arrowTypes, "typeOf target arrow shape", errorString);
	    errorString = validateString(requestModel.getTypeOfLineType(),
	    		lineTypes, "typeOf line type", errorString);
		
	    errorString = validateString(requestModel.getDataPropSourceShape(),
	    		arrowTypes, "datatype property source arrow shape", errorString);
	    errorString = validateString(requestModel.getDataPropTargetShape(),
	    		arrowTypes, "datatype property target arrow shape", errorString);
	    errorString = validateString(requestModel.getDataPropEdgeType(),
	    		lineTypes, "datatype property line type", errorString);
	    errorString = validateString(requestModel.getObjPropSourceShape(),
	    		arrowTypes, "object property source arrow shape", errorString);
	    errorString = validateString(requestModel.getObjPropTargetShape(),
	    		arrowTypes, "object property target arrow shape", errorString);
	    errorString = validateString(requestModel.getObjPropEdgeType(),
	    		lineTypes, "object property line type", errorString);

	    errorString = validateColor(requestModel.getDataFillColor(),
	            DATATYPE_NODE_FILL_COLOR, errorString);
	    errorString = validateColor(requestModel.getDataTextColor(),
	            DATATYPE_NODE_TEXT_COLOR, errorString);
	    errorString = validateColor(requestModel.getDataBorderColor(),
	            DATATYPE_NODE_BORDER_COLOR, errorString);
	    errorString = validateColor(requestModel.getIndividualFillColor(),
	            "individual node fill color", errorString);
	    errorString = validateColor(requestModel.getIndividualTextColor(),
	            "individual node text color", errorString);
	    errorString = validateColor(requestModel.getIndividualBorderColor(),
	            "individual node border color", errorString);
	    errorString = validateColor(requestModel.getClassFillColor(), "type node fill color", errorString);
	    errorString = validateColor(requestModel.getClassTextColor(), "type node text color", errorString);
	    errorString = validateColor(requestModel.getClassBorderColor(), "type node border color", errorString);

	    errorString = validateColor(requestModel.getDataPropEdgeColor(), 
	    		"datatype property edge color", errorString);
	    errorString = validateColor(requestModel.getObjPropEdgeColor(), 
	    		"object property edge color", errorString);
	    return validateColor(requestModel.getTypeOfLineColor(), "typeOf line color", errorString);
	}

	/**
	 * Validates the user input for a custom property graph. Checks that all required customization options are provided
	 * and that they are one of the expected values.
	 * 
	 * @param  requestModel holding all details of the user request
	 * @param  isOnlyProperty boolean indicating whether this is for a property graph validation (if true) or  
	 *                 both a class and property validation (if false)
	 * @param  currentError which is the current error message
	 * @return errorString updated by any error messages added by the checks in this method
	 * 
	 */
	private static String validateCustomPropertyGraph(GraphRequestModel requestModel, 
			boolean isOnlyProperty, final String currentError) {

	    String errorString = currentError;
	    errorString = validateString(requestModel.getCollapseEdges(), collapseTypes, 
	    		"collapse edges T/F", errorString);

	    errorString = validateString(requestModel.getDataNodeShape(), 
	    		nodeShapes, DATATYPE_NODE_SHAPE, errorString);
	    errorString = validateString(requestModel.getObjNodeShape(), 
	    		nodeShapes, "object node shape", errorString);

	    errorString = validateString(requestModel.getAnnPropSourceShape(),
	    		arrowTypes, "annotataion property source arrow shape", errorString);
	    errorString = validateString(requestModel.getAnnPropTargetShape(),
	    		arrowTypes, "annotation property target arrow shape", errorString);
	    errorString = validateString(requestModel.getAnnPropEdgeType(),
	    		lineTypes, "annotation property line type", errorString);
	    errorString = validateString(requestModel.getDataPropSourceShape(),
	    		arrowTypes, "datatype property source arrow shape", errorString);
	    errorString = validateString(requestModel.getDataPropTargetShape(),
	    		arrowTypes, "datatype property target arrow shape", errorString);
	    errorString = validateString(requestModel.getDataPropEdgeType(),
	    		lineTypes, "datatype property line type", errorString);
	    errorString = validateString(requestModel.getObjPropSourceShape(),
	    		arrowTypes, "object property source arrow shape", errorString);
	    errorString = validateString(requestModel.getObjPropTargetShape(),
	    		arrowTypes, "object property target arrow shape", errorString);
	    errorString = validateString(requestModel.getObjPropEdgeType(),
	    		lineTypes, "object property line type", errorString);
	    errorString = validateString(requestModel.getRdfPropSourceShape(),
	    		arrowTypes, "RDF property source arrow shape", errorString);
	    errorString = validateString(requestModel.getRdfPropTargetShape(),
	    		arrowTypes, "RDF property target arrow shape", errorString);
	    errorString = validateString(requestModel.getRdfPropEdgeType(),
	    		lineTypes, "RDF property line type", errorString);
	    if (!isOnlyProperty) {
		    errorString = validateString(requestModel.getSubclassOfSourceShape(),
		    		arrowTypes, "subclassOf source arrow shape", errorString);
		    errorString = validateString(requestModel.getSubclassOfTargetShape(),
		    		arrowTypes, "subclassOf target arrow shape", errorString);
		    errorString = validateString(requestModel.getSubclassOfLineType(),
		    		lineTypes, "subclassOf line type", errorString);
		    errorString = validateColor(requestModel.getSubclassOfLineColor(), "subclassOf line color", errorString);
	    }

	    errorString = validateColor(requestModel.getDataFillColor(), DATATYPE_NODE_FILL_COLOR, errorString);
	    errorString = validateColor(requestModel.getDataTextColor(), DATATYPE_NODE_TEXT_COLOR, errorString);
	    errorString = validateColor(requestModel.getObjFillColor(), "object node fill color", errorString);
	    errorString = validateColor(requestModel.getObjTextColor(), "object node text color", errorString);
	    errorString = validateColor(requestModel.getDataBorderColor(), DATATYPE_NODE_BORDER_COLOR, errorString);
	    errorString = validateColor(requestModel.getObjBorderColor(), "object node border color", errorString);
	    errorString = validateColor(requestModel.getAnnPropEdgeColor(),
	            "annotation property line color", errorString);
	    errorString = validateColor(requestModel.getDataPropEdgeColor(), "data property line color", errorString);
	    errorString = validateColor(requestModel.getObjPropEdgeColor(), "object property line color", errorString);
	    return validateColor(requestModel.getRdfPropEdgeColor(), "RDF property line color", errorString);
	}

	/**
	 * Checks that a string is not null or empty, and matches one of a list of possible values
	 * 
	 * @param  checkString String to validate
	 * @param  possibleValues (List<String>) which may be null if any value is allowed
	 * @param  description (String) indicating the type of value being check
	 * @param  currentError which is the current error message
	 * @return errorString updated by any error messages added by the checks in this method
	 * 
	 */
	private static String validateString(String checkString, List<String> possibleValues, 
			final String description, final String currentError) {
		
		String error = EMPTY_STRING;
	    if (checkString == null || EMPTY_STRING.equals(checkString)) {
	        error = "The string, " + checkString + ", defining the " + description + ", is null or empty.";
	    } else if (possibleValues != null && !possibleValues.contains(checkString)) {
	    	error = "The string, " + checkString + ", defining the " + description 
	    	        + ", is not one of the expected values.";
	    }
	    
	    // Get final error string to return
	    return updateErrorString(currentError, error);
	}
}