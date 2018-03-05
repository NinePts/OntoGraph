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
 * PropertyModel defines a property and its details such as domain and range,
 * and whether it is a functional property.
 * 
 * Lombok Builder allows instantiation with builder().
 * Lombok Data removes need for POJO boilerplate.
 *
 */
@Data
@Builder
public class PropertyModel {
    
    @NonNull private String propertyName;
    @NonNull private String propertyLabel;
    @NonNull private String fullPropertyName;
    // Value is 'a' for annotation properties, 'd' for datatype properties, 
    //   'o' for object properties and 'r' for RDF properties
    private char propertyType;
    // Flags indicating whether the property is functional/inverse functional/symmetric/
    //   asymmetric/transitive/reflexive/irreflexive
    private EdgeFlagsModel edgeFlags;
    // Domains and object ranges are strings of the form, "label (class name with prefix)"
    //    or simply the class name if there is no label defined
    private List<String> domains;
    private List<String> ranges;

}
