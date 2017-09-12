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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

/**
 * GraphResponse returns some of the user's input request parameters (the 
 * graph title, type and visualization) and the GraphML output.
 * 
 * Lombok Data removes need for POJO boilerplate.
 *
 */
@Data
public class GraphResponseModel {
    
    private final String graphTitle;
    private final String visualization;
    // Graph type (classAndInheritance, property, individual)
    private final String graphType;
    private String graphML;

    @JsonCreator
    @JsonIgnoreProperties(ignoreUnknown = true)
    public GraphResponseModel(@JsonProperty("graphTitle") String graphTitle, 
    		@JsonProperty("visualization") String visualization, 
    		@JsonProperty("graphType") String graphType, 
    		@JsonProperty("graphML") String graphML) {
    	super();
    	this.graphTitle = graphTitle;
    	this.visualization = visualization;
    	this.graphType = graphType;
    	this.graphML = graphML;
    }
    
}
