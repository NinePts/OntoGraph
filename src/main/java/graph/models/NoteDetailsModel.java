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
 * NoteDetailsModel defines the necessary information to create a GraphML note (its height and width).
 * 
 * Lombok Builder allows instantiation with builder().
 * Lombok Data removes need for POJO boilerplate.
 *
 */
@Data
@Builder
public class NoteDetailsModel {

	private String lineType;
	private Integer height;
	private Integer width;
	
	/**
	 * Creates an instance of a NoteDetailsModel.
	 * 
	 * @param  lineType String
	 * @param  height Integer
	 * @param  width Integer
	 * @return NoteDetailsModel
	 * 
	 */
	public static NoteDetailsModel createNoteDetailsModel(final String lineType,
			final Integer height, final Integer width) {
		
		return NoteDetailsModel.builder()
			.lineType(lineType)
			.height(height)
			.width(width)
			.build();
	}
}
