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

package graph.graphmloutputs;

import java.util.List;

import graph.models.PrefixModel;

/**
 * TitleAndPrefixCreation builds the graph title and prefixes boxes
 * for the GraphML output.
 *
 */
public final class TitleAndPrefixCreation {
    
    // Not meant to be instantiated
    private TitleAndPrefixCreation() {
      throw new IllegalAccessError("TitleAndPrefixCreation is a utility class and should not be instantiated.");
    }
    
    /**
     * Adds the GraphML for the title and prefixes boxes.
     * 
     * @param  graphTitle String
     * @param  ontologyURI String
     * @param  prefixes List<PrefixModel>
     * @return String GraphML output (String)
     * 
     */
    public static String addTitleAndPrefixes(final String graphTitle, final String ontologyURI,
    		List<PrefixModel> prefixes) {
        
    	StringBuilder sb = new StringBuilder();
        
        sb.append(GraphMLOutputDetails.createTitleBox(graphTitle, ontologyURI));
        if (prefixes != null) {
        	sb.append(GraphMLOutputDetails.createPrefixesBox(prefixes));
        }
        
        return sb.toString();
    }
}
