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

package graph;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Utility method to set up the Spring ApplicationContext.
 * The ApplicationContext "represents the Spring IoC container and is 
 * responsible for instantiating, configuring, and assembling ... beans."
 *
 */
public class AppContextUtil implements ApplicationContextAware {
    private ApplicationContext applicationContext;     

    @Override
    public void setApplicationContext(ApplicationContext context) {
        applicationContext = context;
    }
    
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
