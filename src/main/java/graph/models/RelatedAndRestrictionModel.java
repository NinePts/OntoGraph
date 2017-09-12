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
import java.util.Map;

import lombok.Builder;
import lombok.Data;

/**
 * RelatedAndRestrictionsModel is an "uber" model containing lists of models 
 * of "related" classes (equivalent, disjoints, and oneOfs), of connectives
 * (unions, intersections and complementOfs) and restrictions (allValuesFrom,
 * someValuesFrom, min/maxInclusive, ...).
 * 
 * Lombok Builder allows instantiation with builder().
 * Lombok Data removes need for POJO boilerplate.
 *
 */
@Data
@Builder
public class RelatedAndRestrictionModel {
    
    private List<RestrictionModel> restrictions;
    
    // The key is a class name and the List<TypeAndValueModel> values are a list of models where the "type" 
    //     is first and the related entity is the "value". The relationship types are "com" for complementOf,
    //     "inter" for intersectionOf and "un" for unionOf.
    private Map<String, List<TypeAndValueModel>> connectives;
    
    // The key is a class name and the List<TypeAndValueModel> values are a list of models where the "type"  
    //     is first, and the related entity is the "value". The relationship types are "eq" for equivalentClass,
    //     "dis" for disjointWith and "one" for oneOf.
    private Map<String, List<TypeAndValueModel>> equivalentsDisjointsOneOfs;
    
}