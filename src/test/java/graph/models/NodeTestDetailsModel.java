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

import lombok.Builder;
import lombok.Data;

/**
 * NodeTestDetailsModel defines the information to check in a GraphML output string for a node element.
 * 
 * Lombok Builder allows instantiation with builder().
 * Lombok Data removes need for POJO boilerplate.
 *
 */
@Data
@Builder
public class NodeTestDetailsModel {

	private String nodeId;
	private boolean forUMLNode;
	private String fillColor;
	private String borderStyleColor;
	private String borderStyleType;
	// The following is the NodeLabel value for a regular node, and is the NodeLabel alignment="left" value
	//    for a UML node
	private String nodeLabelValue;  
	// The following is the Shape type attribute for a regular node, and is the NodeLabel 
	//    alignment="center" value otherwise
	private String shapeTypeOrNodeLabel;  
	
}
