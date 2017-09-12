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
 * TypeAndValueModel defines two String values, where the first, the "type" characterizes
 * the second, the "value". 
 * 
 * Lombok Builder allows instantiation with builder().
 * Lombok Data removes need for POJO boilerplate.
 *
 */
@Data
@Builder
public class TypeAndValueModel {
    
    @NonNull private String type;
    @NonNull private String value;
	
	/**
	 * Creates an instance of the class, TypeAndValueModel.
	 * 
	 * @param  type String
	 * @param  value String
	 * @return TypeAndValueModel
	 * 
	 */
	public static TypeAndValueModel createTypeAndValueModel(final String type,
			final String value) {
		
		return TypeAndValueModel.builder()
				.type(type)
				.value(value)
				.build();	
	}
}
