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
import lombok.NonNull;

/**
 * BlankAndRelatedNodesModel defines two String values, where the first is the id of a blank node, and
 * the second is the id of a related class/node, which may also be a blank node if the nodes are nested
 * or may be null if not needed for a particular graph type.
 * 
 * Lombok Builder allows instantiation with builder().
 * Lombok Data removes need for POJO boilerplate.
 *
 */
@Data
@Builder
public class BlankAndRelatedNodesModel {

	@NonNull private String blankNode;
	@NonNull private String relatedNode;

	/**
	 * Creates an instance of the class, BlankAndRelatedNodesModel.
	 * 
	 * @param  blankNode String
	 * @param  relatedNode String
	 * @return BlankAndRelatedNodesModel
	 * 
	 */
	public static BlankAndRelatedNodesModel createBlankAndRelatedNodesModel(final String blankNode,
			final String relatedNode) {
		
		return BlankAndRelatedNodesModel.builder()
				.blankNode(blankNode)
				.relatedNode(relatedNode)
				.build();	
	}
}
