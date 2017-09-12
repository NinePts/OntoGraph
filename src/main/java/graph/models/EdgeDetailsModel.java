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

import graph.graphmloutputs.GraphMLOutputDetails;
import graph.graphmloutputs.GraphMLUtils;
import lombok.Builder;
import lombok.Data;

/**
 * EdgeDetailsModel defines the necessary information to create a GraphML edge (including source/target
 * arrow shape, edge label, ... details).  
 * 
 * Lombok Builder allows instantiation with builder().
 * Lombok Data removes need for POJO boilerplate.
 *
 */
@Data
@Builder
public class EdgeDetailsModel {
	
	// Frequently used strings
	private static final String VOWL = "vowl";
	private static final String BLACK = "#000000";

	private String edgeLabel;
	private String edgeLabelBackground;
	private String edgeLabelColor;
	private String lineColor;
	private String lineType;
	private String lineWidth;
	private String sourceArrow;
	private String targetArrow;
	private String visualization;

	/**
	 * Create an instance of an EdgeDetailsModel.
	 * 
	 * @param  visualization String
	 * @param  label String
	 * @param  labelBackground String
	 * @param  labelColor String
	 * @return EdgeDetailsModel
	 * 
	 */
	public static EdgeDetailsModel createBasicEdgeDetailsModel(final String visualization,
			final String label, final String labelBackground, final String labelColor) {
		
		return EdgeDetailsModel.builder()
			.edgeLabel(label)
			.edgeLabelBackground(labelBackground)
			.edgeLabelColor(labelColor)
			.lineWidth(GraphMLUtils.getLineWidth(visualization))
			.visualization(visualization)
			.build();
	}

	/**
	 * Create the edge-specific details for an equivalentClass or disjointWith edge.
	 * 
	 * @param  visualization String
	 * @param  edgeType String (either "eq" or "dis" for equivalentClass or disjointWith edges,
	 *                respectively)
	 * @return EdgeDetailsModel 
	 * 
	 */
	public static EdgeDetailsModel createEdgeDetailsModelForRelationship(GraphRequestModel requestModel,
			final String edgeType) {
	
		String visualization = requestModel.getVisualization();
		
	    String targetArrow = "triangleSolid";
	    String lineType = "solid";
		String edgeLabel = "owl:equivalentClass";
	
		// Graffoo equivalent and disjoint classes are connected by a single edge with
		//    the label, owl:equivalentClass or owl:disjointWith. This is supported in OntoGraph  
		//    between two non-blank nodes. But when equivalence or disjointness involves a blank node,
		//    then the blank node should be drawn as a class restriction using Manchester OWL syntax.
		//    Manchester OWL is not currently output and it is confusing. So, a note is added instead.
		// TODO Determine how to address lack of compliance with Graffoo
		if ("uml".equals(visualization)) {
		    targetArrow = "angleBracket";
		}
		if (VOWL.equals(visualization)) {
			// VOWL equivalent class has double circle border around a single class (that is defined in 
			//    the ontology namespace and then other equivalents are listed in the next line), 
			//    but this is not natively supported in yEd. For now, this is drawn similar to VOWL's
			//    "Subclass of" relationship. This also easily supports the equivalent/disjoint class 
			//    being a blank node.
			// TODO Determine how to address lack of compliance with VOWL
			targetArrow = "triangleEmpty";
			lineType = "dashed";
			edgeLabel = "Equivalent to";
		}
		if ("dis".equals(edgeType)) {
	    	if (VOWL.equals(visualization)) {
	    		edgeLabel = "Disjoint with";
	    	} else {
	    		edgeLabel = "owl:disjointWith";
	    	}
		}
		if ("super".equals(edgeType)) {
			targetArrow = requestModel.getSubclassOfTargetShape();
			lineType = requestModel.getSubclassOfLineType();
	    	if (VOWL.equals(visualization)) {
	    		edgeLabel = "Subclass of";
	    	} else if ("uml".equals(visualization)) {
	    		edgeLabel = "";
	    	} else {
	    		edgeLabel = "rdfs:subClassOf";
	    	}
		}
	    
	    return EdgeDetailsModel.builder()
	    		.sourceArrow("none")
	    		.targetArrow(targetArrow)
	    		.lineType(lineType)
	    		.lineColor(BLACK)  
	    		.lineWidth(GraphMLOutputDetails.getLineWidth(visualization))
	    		.edgeLabel(edgeLabel)
	    		.edgeLabelBackground("#FFFFFF")  // White
	    		.edgeLabelColor(BLACK)
	    		.visualization(visualization)
	    		.build();
	}
	
	/**
	 * Create an instance of an EdgeDetailsModel based on the type of edge.
	 * 
	 * @param  requestModel GraphRequestModel
	 * @param  typeOfEdge String defining either a "subClass" edge, "type" edge, "data" 
	 *               (datatype) property edge, or "object" property edge
	 * @param  labelBackground String
	 * @param  labelColor String
	 * @return EdgeDetailsModel
	 * 
	 */
	public static EdgeDetailsModel createEdgeDetailsModelForType(GraphRequestModel requestModel,
			final String typeOfEdge) {
		
		String visualization = requestModel.getVisualization();
		
		EdgeDetailsModel edgeDetails = EdgeDetailsModel.builder()
				.edgeLabel("")
        		.lineWidth(GraphMLUtils.getLineWidth(visualization))
        		.edgeLabelBackground("#FFFFFF") 	// White
         		.edgeLabelColor(BLACK)		
        		.visualization(visualization)
        		.build();
				
		if ("subclass".equals(typeOfEdge)) {
    		edgeDetails.setSourceArrow(requestModel.getSubclassOfSourceShape());
    		edgeDetails.setTargetArrow(requestModel.getSubclassOfTargetShape());
    		edgeDetails.setLineColor(requestModel.getSubclassOfLineColor());
    		edgeDetails.setLineType(requestModel.getSubclassOfLineType());
    		edgeDetails.setEdgeLabel(requestModel.getSubclassOfText());
		
		} else if ("data".equals(typeOfEdge)) {
			edgeDetails.setSourceArrow(requestModel.getDataPropSourceShape());
			edgeDetails.setTargetArrow(requestModel.getDataPropTargetShape());
			edgeDetails.setLineColor(requestModel.getDataPropEdgeColor());
			edgeDetails.setLineType(requestModel.getDataPropEdgeType());
			
		} else if ("object".equals(typeOfEdge)) {
			edgeDetails.setSourceArrow(requestModel.getObjPropSourceShape());
			edgeDetails.setTargetArrow(requestModel.getObjPropTargetShape());
			edgeDetails.setLineColor(requestModel.getObjPropEdgeColor());
			edgeDetails.setLineType(requestModel.getObjPropEdgeType());
			
		} else if ("type".equals(typeOfEdge)) {
			edgeDetails.setSourceArrow(requestModel.getTypeOfSourceShape());
			edgeDetails.setTargetArrow(requestModel.getTypeOfTargetShape());
			edgeDetails.setLineColor(requestModel.getTypeOfLineColor());
			edgeDetails.setLineType(requestModel.getTypeOfLineType());
			edgeDetails.setEdgeLabel(requestModel.getTypeOfText());
		}
		
		return edgeDetails;
	}
	
}
