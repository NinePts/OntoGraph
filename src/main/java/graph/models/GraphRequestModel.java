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

package graph.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * GraphRequest defines the graph type, visualization, etc., 
 * as specified in a user's request.
 * 
 * Lombok Data removes need for POJO boilerplate.
 *
 */
@Data
public class GraphRequestModel {
	
    private final String graphTitle;
    private final String inputFile;		// Could be a zip file
    private final String fileData;
    
    // Graph visualization (Graffoo, VOWL, UML, ...)
    private final String visualization;
    
    // Graph type (classAndInheritance, property, individual)
    private String graphType;
    
    // Class and individual customization details
    // (Classes are referenced as individuals' "rdf:type"s)
	private String classNodeShape;
	private String classFillColor; 
	private String classTextColor;
	private String classBorderColor;
	private String classBorderType; 
	
	// Class customization details
	private String subclassOfSourceShape;
	private String subclassOfTargetShape;
	private String subclassOfLineColor;
	private String subclassOfLineType;
	private String subclassOfText;
	
	// Individual customization details
	private String individualNodeShape;
	private String individualFillColor; 
	private String individualTextColor;
	private String individualBorderColor;
	private String individualBorderType; 
	private String typeOfSourceShape;
	private String typeOfTargetShape;
	private String typeOfLineColor;
	private String typeOfLineType;
	private String typeOfText;
	
	// Individual and property customizations
	// (Datatypes are referenced as individuals' values in datatype properties)
    private String dataNodeShape;
    private String dataFillColor;
    private String dataTextColor;
    private String dataBorderColor;
    private String dataBorderType;
    private String dataPropSourceShape;
    private String dataPropTargetShape;
    private String dataPropEdgeColor;
    private String dataPropEdgeType;
    private String objPropSourceShape;
    private String objPropTargetShape;
    private String objPropEdgeColor;
    private String objPropEdgeType;
    
	// Properties customization details
    // Annotation properties can/should be distinguished from datatype properties 
    //   in property diagrams, but not for individual diagrams
    // For individual diagrams, annotations will be drawn as datatype properties 
	//   (since that is basically what they are - annotation values are rdfs:Literals)
	private String annPropSourceShape;
    private String annPropTargetShape;
    private String annPropEdgeColor;
    private String annPropEdgeType;
	private String collapseEdges;
    private String objNodeShape;
    private String objFillColor;
    private String objTextColor;
    private String objBorderColor;
    private String objBorderType; 
    
	// Used to set up the test cases
    public GraphRequestModel(String graphTitle, String inputFile, 
    		String fileData, String visualization, String graphType,
    		boolean collapseEdges) {
    	super();
    	this.graphTitle = graphTitle;
    	this.inputFile = inputFile;
    	this.fileData = fileData;
    	this.visualization = visualization;
    	this.graphType = graphType;
    	if (collapseEdges) {
    		this.collapseEdges = "collapseTrue";
    	} else {
    		this.collapseEdges = "collapseFalse";
    	}
    }
    
    @JsonCreator
    @JsonIgnoreProperties(ignoreUnknown = true)
    public GraphRequestModel(@JsonProperty("graphTitle") String graphTitle, 
    		@JsonProperty("inputFile") String inputFile, 
    		@JsonProperty("fileData") String fileData, 
    		@JsonProperty("visualization") String visualization, 
    		@JsonProperty("graphType") String graphType,
    		// For class customization
    		@JsonProperty("classNodeShape") String classNodeShape,
    		@JsonProperty("classFillColor") String classFillColor,
    		@JsonProperty("classTextColor") String classTextColor,
    		@JsonProperty("classBorderColor") String classBorderColor,
    		@JsonProperty("classBorderType") String classBorderType,
    		@JsonProperty("subclassOfSourceShape") String subclassOfSourceShape,
    		@JsonProperty("subclassOfTargetShape") String subclassOfTargetShape,
    		@JsonProperty("subclassOfLineColor") String subclassOfLineColor,
    		@JsonProperty("subclassOfLineType") String subclassOfLineType,
    		// For individual customization
    		@JsonProperty("typeNodeShape") String typeNodeShape,
    		@JsonProperty("typeFillColor") String typeFillColor,
    		@JsonProperty("typeTextColor") String typeTextColor,
    		@JsonProperty("typeBorderColor") String typeBorderColor,
    		@JsonProperty("typeBorderType") String typeBorderType,
    		@JsonProperty("individualNodeShape") String individualNodeShape,
    		@JsonProperty("individualFillColor") String individualFillColor,
    		@JsonProperty("individualTextColor") String individualTextColor,
    		@JsonProperty("individualBorderColor") String individualBorderColor,
    		@JsonProperty("individualBorderType") String individualBorderType,
    		@JsonProperty("indDataNodeShape") String indDataNodeShape,
    		@JsonProperty("indDataFillColor") String indDataFillColor,
    		@JsonProperty("indDataTextColor") String indDataTextColor,
    		@JsonProperty("indDataBorderColor") String indDataBorderColor,
    		@JsonProperty("indDataBorderType") String indDataBorderType,
    		@JsonProperty("typeOfSourceShape") String typeOfSourceShape,
    		@JsonProperty("typeOfTargetShape") String typeOfTargetShape,
    		@JsonProperty("typeOfLineColor") String typeOfLineColor,
    		@JsonProperty("typeOfLineType") String typeOfLineType,
    		@JsonProperty("indDataPropSourceShape") String indDataPropSourceShape,
    		@JsonProperty("indDataPropTargetShape") String indDataPropTargetShape,
    		@JsonProperty("indDataPropEdgeColor") String indDataPropEdgeColor,
    		@JsonProperty("indDataPropEdgeType") String indDataPropEdgeType,
    		@JsonProperty("indObjPropSourceShape") String indObjPropSourceShape,
    		@JsonProperty("indObjPropTargetShape") String indObjPropTargetShape,
    		@JsonProperty("indObjPropEdgeColor") String indObjPropEdgeColor,
    		@JsonProperty("indObjPropEdgeType") String indObjPropEdgeType,
    		// For property customization
    		@JsonProperty("collapseEdges") String collapseEdges,
    		@JsonProperty("dataNodeShape") String dataNodeShape,
    		@JsonProperty("dataFillColor") String dataFillColor,
    		@JsonProperty("dataTextColor") String dataTextColor,
    		@JsonProperty("dataBorderColor") String dataBorderColor,
    		@JsonProperty("dataBorderType") String dataBorderType,
    		@JsonProperty("objNodeShape") String objNodeShape,
    		@JsonProperty("objFillColor") String objFillColor,
    		@JsonProperty("objTextColor") String objTextColor,
    		@JsonProperty("objBorderColor") String objBorderColor,
    		@JsonProperty("objBorderType") String objBorderType,
    		@JsonProperty("annPropSourceShape") String annPropSourceShape,
    		@JsonProperty("annPropTargetShape") String annPropTargetShape,
    		@JsonProperty("annPropEdgeColor") String annPropEdgeColor,
    		@JsonProperty("annPropEdgeType") String annPropEdgeType,
    		@JsonProperty("dataPropSourceShape") String dataPropSourceShape,
    		@JsonProperty("dataPropTargetShape") String dataPropTargetShape,
    		@JsonProperty("dataPropEdgeColor") String dataPropEdgeColor,
    		@JsonProperty("dataPropEdgeType") String dataPropEdgeType,
    		@JsonProperty("objPropSourceShape") String objPropSourceShape,
    		@JsonProperty("objPropTargetShape") String objPropTargetShape,
    		@JsonProperty("objPropEdgeColor") String objPropEdgeColor,
    		@JsonProperty("objPropEdgeType") String objPropEdgeType) {
    	super();
    	this.graphTitle = graphTitle;
    	this.inputFile = inputFile;
    	this.fileData = fileData;
    	this.visualization = visualization;
    	this.graphType = graphType; 
    	
    	this.subclassOfSourceShape = subclassOfSourceShape;
    	this.subclassOfTargetShape = subclassOfTargetShape;
    	this.subclassOfLineColor = subclassOfLineColor;
    	this.subclassOfLineType = subclassOfLineType;
    	
    	this.individualNodeShape = individualNodeShape;
    	this.individualFillColor = individualFillColor;
    	this.individualTextColor = individualTextColor;
    	this.individualBorderColor = individualBorderColor;
    	this.individualBorderType = individualBorderType;
    	this.typeOfSourceShape = typeOfSourceShape;
    	this.typeOfTargetShape = typeOfTargetShape;
    	this.typeOfLineColor = typeOfLineColor;
    	this.typeOfLineType = typeOfLineType;

    	this.collapseEdges = collapseEdges;
    	this.objNodeShape = objNodeShape;
    	this.objFillColor = objFillColor;
    	this.objTextColor = objTextColor;
    	this.objBorderColor = objBorderColor;
    	this.objBorderType = objBorderType;
    	this.annPropSourceShape = annPropSourceShape;
    	this.annPropTargetShape = annPropTargetShape;
    	this.annPropEdgeColor = annPropEdgeColor;
    	this.annPropEdgeType = annPropEdgeType;
    	
    	if ("individual".equals(graphType)) {
        	this.classNodeShape = typeNodeShape;
        	this.classFillColor = typeFillColor;
        	this.classTextColor = typeTextColor;
        	this.classBorderColor = typeBorderColor;
        	this.classBorderType = typeBorderType;
            this.dataNodeShape = indDataNodeShape;
            this.dataFillColor = indDataFillColor;
            this.dataTextColor = indDataTextColor;
            this.dataBorderColor = indDataBorderColor;
            this.dataBorderType = indDataBorderType;
            this.dataPropSourceShape = indDataPropSourceShape;
            this.dataPropTargetShape = indDataPropTargetShape;
            this.dataPropEdgeColor = indDataPropEdgeColor;
            this.dataPropEdgeType = indDataPropEdgeType;
            this.objPropSourceShape = indObjPropSourceShape;
            this.objPropTargetShape = indObjPropTargetShape;
            this.objPropEdgeColor = indObjPropEdgeColor;
            this.objPropEdgeType = indObjPropEdgeType;
        } else if ("class".equals(graphType)) {
        	this.classNodeShape = classNodeShape;
        	this.classFillColor = classFillColor;
        	this.classTextColor = classTextColor;
        	this.classBorderColor = classBorderColor;
        	this.classBorderType = classBorderType;
        } else {
        	// graphType is "property"
            this.dataNodeShape = dataNodeShape;
            this.dataFillColor = dataFillColor;
            this.dataTextColor = dataTextColor;
            this.dataBorderColor = dataBorderColor;
            this.dataBorderType = dataBorderType;
            this.dataPropSourceShape = dataPropSourceShape;
            this.dataPropTargetShape = dataPropTargetShape;
            this.dataPropEdgeColor = dataPropEdgeColor;
            this.dataPropEdgeType = dataPropEdgeType;
            this.objPropSourceShape = objPropSourceShape;
            this.objPropTargetShape = objPropTargetShape;
            this.objPropEdgeColor = objPropEdgeColor;
            this.objPropEdgeType = objPropEdgeType;
        }
    }
}
