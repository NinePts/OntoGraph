//
// Copyright (c) Nine Points Solutions, LLC
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// 

var GraphRequestModel = Backbone.Model.extend({
	// The defaults listed below define the initial settings for the customization fields
	//   of the browser
	// CHANGEME
	defaults: {
		"graphTitle": "",
		"inputFile": "",
		"fileData": "",
		"visualization": "VOWL",
		"graphType": "Class Definitions",
		
		// Class customizations
		"classNodeShape": "Rectangle, Rounded Corners",
		"classFillColor": "#FFFF99",
		"classTextColor" : "#000000",
		"classBorderColor" : "#000000",
		"classBorderType": "Solid",
		"subclassOfSourceShape": "None",
		"subclassOfTargetShape": "Triangle Outline",
		"subclassOfLineColor": "#000000",
		"subclassOfLineType": "Solid",
		
		// Individual customizations
		// Where "type" is the formatting for the individual's type definition
		"typeNodeShape": "Rectangle, Rounded Corners",
		"typeFillColor": "#FFFF99",
		"typeTextColor" : "#000000",
		"typeBorderColor" : "#000000",
		"typeBorderType": "Solid",
		"individualNodeShape": "Circle, Small",
		"individualFillColor": "#AACCFF",
		"individualTextColor" : "#000000",
		"individualBorderColor" : "#000000",
		"individualBorderType": "Solid",
		"typeOfSourceShape": "None",
		"typeOfTargetShape": "Triangle Outline",
		"typeOfLineColor": "#000000",
		"typeOfLineType": "Solid",
		// Formatting for data values related to the individual by datatype properties
		"indDataNodeShape": "None (Text Only)",
		"indDataFillColor": "#FFFFFF",
		"indDataTextColor": "#000000",
		"indDataBorderColor": "#FFFFFF",
		"indDataBorderType": "Solid",
		
		// Individual customizations - Formatting for object and datatype property edges
		// Note that annotations will be drawn similar to datatype edges (since that
		//    is basically what they are; annotation values are rdfs:Literals)
		"indObjPropSourceShape": "None",
		"indObjPropTargetShape": "Triangle",
		"indObjPropEdgeColor": "#000000",
		"indObjPropEdgeType": "Solid",
		"indDataPropSourceShape": "None",
		"indDataPropTargetShape": "Triangle",
		"indDataPropEdgeColor": "#000000",
		"indDataPropEdgeType": "Solid",

		// All property customizations, and "both" class and property customizations 
		//    for all visualizations but VOWL
		"collapseEdges": "collapseFalse",

		// Properties customization
		"objNodeShape": "Rectangle, Rounded Corners",
		"objFillColor": "#FFFF99",
		"objTextColor" : "#000000",
		"objBorderColor" : "#000000",
		"objBorderType": "Solid",
		"dataNodeShape": "None (Text Only)",
		"dataFillColor": "#FFFFFF",
		"dataTextColor" : "#000000",
		"dataBorderColor" : "#FFFFFF",
		"dataBorderType": "Solid",

		"annPropSourceShape": "None",
		"annPropTargetShape": "Angle Bracket",
		"annPropEdgeColor": "#000000",
		"annPropEdgeType": "Solid",
		"dataPropSourceShape": "None",
		"dataPropTargetShape": "Angle Bracket",
		"dataPropEdgeColor": "#000000",
		"dataPropEdgeType": "Solid",
		"objPropSourceShape": "Circle Outline",
		"objPropTargetShape": "Triangle Outline",
		"objPropEdgeColor": "#000000",
		"objPropEdgeType": "Solid"
	},
	
	urlRoot: "graph"
});