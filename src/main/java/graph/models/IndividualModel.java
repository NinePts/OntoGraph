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

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * IndividualModel defines instance/named individuals and their types.
 * 
 * Lombok Builder allows instantiation with builder().
 * Lombok Data removes need for POJO boilerplate.
 *
 */
@Data
@Builder
public class IndividualModel {
    
    @NonNull private String individualName;
    @NonNull private String individualLabel;
    @NonNull private String fullIndividualName;
    private List<String> typeLabels;
    // Array of datatype property names (the "types") and values
    private List<TypeAndValueModel> datatypeProperties;
    // Array of object property names (the "types") and the "related" individual (the "value")
    private List<TypeAndValueModel> objectProperties;
    
}
