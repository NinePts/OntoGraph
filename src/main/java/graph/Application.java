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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.ScopedProxyMode;


@EnableAutoConfiguration
@SpringBootApplication
@ImportResource("classpath:root-context.xml")
@ComponentScan(
	basePackages = {"graph", "graph.graphmloutputs"},
    scopedProxy=ScopedProxyMode.TARGET_CLASS
)

/**
 * Main executable, running as a Spring Boot application
 * with an embedded Tomcat servlet container as the HTTP runtime
 *
 */
public class Application { 

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
