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
import lombok.Builder;
import lombok.Data;

/**
 * NodeDetailsModel defines the necessary information to create a GraphML node (including shape, color,
 * border, ... details). All the values (even numerics such as height and width) are strings, since 
 * these values are used in an XML "string" definition.  
 * 
 * Lombok Builder allows instantiation with builder().
 * Lombok Data removes need for POJO boilerplate.
 *
 */
@Data
@Builder
public class NodeDetailsModel {

	private String borderColor;
	private String borderType;
	private String borderWidth;
	private String fillColor;
	private String height;
	private String modelName;
	private String modelPosition;
	private String nodeShape;
	private String textColor;
	private String width;
	private String visualization;

	/**
	 * Creates an instance of the class, NodeDetailsModel.
	 * 
	 * @param  requestModel GraphRequestModel
	 * @param  typeOfNode String defining a "class" node, "individual" node, "data" (datatype) 
	 * 				property node, or "object" property node
	 * @return NodeDetailsModel
	 * 
	 */
	public static NodeDetailsModel createNodeDetailsModel(GraphRequestModel requestModel, 
			final String typeOfNode) {
		
		String visualization = requestModel.getVisualization();
		
		NodeDetailsModel nodeDetails = NodeDetailsModel.builder()
				.borderWidth(GraphMLOutputDetails.getLineWidth(visualization))
				.visualization(visualization)
				.modelName("internal")
				.modelPosition("c")
				.build();
		
		if ("class".equals(typeOfNode)) {
			nodeDetails.setNodeShape(requestModel.getClassNodeShape());
			nodeDetails.setFillColor(requestModel.getClassFillColor());
			nodeDetails.setTextColor(requestModel.getClassTextColor());
			nodeDetails.setBorderType(requestModel.getClassBorderType());
			nodeDetails.setBorderColor(requestModel.getClassBorderColor());
			
		} else if ("data".equals(typeOfNode)) {
	        // This is not really intended for UML, but it works out - Need to fill in undefined values
			if ("uml".equals(requestModel.getVisualization())) {
				nodeDetails.setNodeShape("squareRectangle");
				nodeDetails.setFillColor(requestModel.getUmlDataNodeColor());
				nodeDetails.setTextColor("#000000");
				nodeDetails.setBorderColor("#FFFFFF");
				nodeDetails.setBorderType("solid");
			} else {
				nodeDetails.setNodeShape(requestModel.getDataNodeShape());
				nodeDetails.setFillColor(requestModel.getDataFillColor());
				nodeDetails.setTextColor(requestModel.getDataTextColor());
				nodeDetails.setBorderType(requestModel.getDataBorderType());
				nodeDetails.setBorderColor(requestModel.getDataBorderColor());
			}

		} else if ("individual".equals(typeOfNode)) {
			nodeDetails.setNodeShape(requestModel.getIndividualNodeShape());
			nodeDetails.setFillColor(requestModel.getIndividualFillColor());
			nodeDetails.setTextColor(requestModel.getIndividualTextColor());
			nodeDetails.setBorderType(requestModel.getIndividualBorderType());
			nodeDetails.setBorderColor(requestModel.getIndividualBorderColor());
		
		} else if ("object".equals(typeOfNode)) {
			nodeDetails.setNodeShape(requestModel.getObjNodeShape());
			nodeDetails.setFillColor(requestModel.getObjFillColor());
			nodeDetails.setTextColor(requestModel.getObjTextColor());
			nodeDetails.setBorderType(requestModel.getObjBorderType());
			nodeDetails.setBorderColor(requestModel.getObjBorderColor());
		}
		
		return nodeDetails;
	}
}
