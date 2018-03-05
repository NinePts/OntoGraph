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

import java.util.Comparator;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

/**
 * PrefixModel defines namespace details.
 * 
 * Lombok Builder allows instantiation with builder().
 * Lombok Data removes need for POJO boilerplate.
 *
 */
@Data
@Builder
public class PrefixModel {
    
    @NonNull private String prefixName;
    @NonNull private String url;
    
    // Comparator for sorting the prefixes alphabetically, by prefix name 
    public static final Comparator<PrefixModel> prefixSort = 
    		(PrefixModel pm1, PrefixModel pm2)->pm1.getPrefixName().compareTo(pm2.getPrefixName());
    
}
