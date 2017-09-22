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
 */

package graph;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestPropertySource;
import org.xml.sax.SAXException;

import graph.GraphController;
import graph.models.GraphRequestModel;

import org.junit.runner.RunWith;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;


@RunWith(Parameterized.class)
@ContextConfiguration("classpath:test-context.xml")
@TestPropertySource("classpath:test.properties")
@SpringBootTest()
public class CustomizationTest {
	
	@Autowired private GraphController controller;
	private TestContextManager testContextManager;
	
	@Parameters
    public static Object[] params() {
        return new Object[] { customRequestModel1, customRequestModel2, customRequestModel3, customRequestModel4,
                customRequestModel5, customRequestModel6, customRequestModel7, customRequestModel8, customRequestModel9,
                customRequestModel10 };
    }
    
    @Parameter
    public GraphRequestModel requestModel;
    
    // Get custom request models - Testing each nodeShape, arrowShape and lineType setting at least once
    private final static GraphRequestModel customRequestModel1 = createCustomRequestModel(
            "TestCustom1", "circle", "solid", "angleBracket", "backslash", "dotted");
    private final static GraphRequestModel customRequestModel2 = createCustomRequestModel(
            "TestCustom2", "smallCircle", "dashed", "circleSolid", "circleEmpty", "dashedDotted");
    private final static GraphRequestModel customRequestModel3 = createCustomRequestModel(
            "TestCustom3", "diamond", "dotted", "diamondSolid", "diamondEmpty", "solid");
    private final static GraphRequestModel customRequestModel4 = createCustomRequestModel(
            "TestCustom4", "ellipse", "dashedDotted", "triangleSolid", "triangleEmpty", "dashed");
    private final static GraphRequestModel customRequestModel5 = createCustomRequestModel(
            "TestCustom5", "hexagon", "solid", "none", "none", "dotted");
    private final static GraphRequestModel customRequestModel6 = createCustomRequestModel(
            "TestCustom6", "parallelogramRight", "dashed", "backslash", "angleBracket", "dashedDotted");
    private final static GraphRequestModel customRequestModel7 = createCustomRequestModel(
            "TestCustom7", "parallelogramLeft", "dotted", "circleEmpty", "circleSolid", "solid");
    private final static GraphRequestModel customRequestModel8 = createCustomRequestModel(
            "TestCustom8", "roundRectangle", "dashedDotted", "diamondEmpty", "diamondSolid", "dashed");
    private final static GraphRequestModel customRequestModel9 = createCustomRequestModel(
    		"TestCustom9", "none", "solid", "triangleEmpty", "triangleSolid", "dotted");
	private final static GraphRequestModel customRequestModel10 = createCustomRequestModel(
            "TestCustom10", "squareRectangle", "dashed", "none", "none", "dashedDotted");
	
	private final static List<String> PREFIXES = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "test", "xsd");
	
    // Set up for Spring tests
    @Before
    public void setUp() throws Exception {
    	
         this.testContextManager = new TestContextManager(getClass());
         this.testContextManager.prepareTestInstance(this);
    }
	
	/**
	 * Test all customization combinations
	 * 
	 * @throws Exception 
	 * 
	 */
    @Test
    public void testCustomization() throws Exception {
        
        List<String> titleEntries = Arrays.asList("Title:  " + requestModel.getGraphTitle(), 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        createGraphMLAndCompareToMaster(requestModel, PREFIXES, titleEntries);
    }
    
    /**
     * Compares the test-generated GraphML output to the master reference for class graphs.
     * 
     * @param  GraphRequestModel GraphRequestModel 
     * @throws OntoGraphException 
     * @throws IOException
     * @throws SAXException 
     * 
     */
    private void createGraphMLAndCompareToMaster(GraphRequestModel requestModel, 
            List<String> expectedPrefixes, List<String> expectedTitle) throws Exception {
        
        String testXML = controller.graph(requestModel).getGraphML();
        TestUtils.testGraphMLOutput("custom", "custom", requestModel.getGraphTitle(), 
        		expectedPrefixes, expectedTitle, testXML);
    }
    
    /**
     * Sets up a request model for a class graph with custom visualization details. Uses 'TestClassesB.ttl', which
     * includes multiple nodes with multiple levels of inheritance.
     * 
     * @param graphTitle Name of file for output
     * @param nodeShape Class node shape as string
     * @param borderType Class border type as string
     * @param sourceShape Subclass of edge source shape as string
     * @param targetShape Subclass of edge target shape as string
     * @param lineType Subclass of edge line type
     * @return customized GraphRequestModel
     * 
     */
    private static GraphRequestModel createCustomRequestModel(String graphTitle, String nodeShape, String borderType,
            String sourceShape, String targetShape, String lineType) {
        
        String fileData = "";
        try {
            // Try to read the file
            fileData = TestUtils.readFile("src/test/resources/classTestFiles/TestClassesB.ttl");
        } catch (IOException e) {
            // Return stack trace on error
            e.printStackTrace();
        }
        
        GraphRequestModel requestModel = new GraphRequestModel(graphTitle, "TestClassesB.ttl", fileData, "custom",
                "class", false);
        
        requestModel.setClassNodeShape(nodeShape);
        requestModel.setClassFillColor("#FFFFFF"); 
        requestModel.setClassTextColor("#000000");
        requestModel.setClassBorderColor("#000000");
        requestModel.setClassBorderType(borderType); 
        requestModel.setSubclassOfSourceShape(sourceShape);
        requestModel.setSubclassOfTargetShape(targetShape);
        requestModel.setSubclassOfLineColor("#000000");
        requestModel.setSubclassOfLineType(lineType);
        
        return requestModel;
    }
}
