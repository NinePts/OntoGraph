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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestPropertySource;

import graph.GraphController;
import graph.models.GraphRequestModel;

import org.junit.runner.RunWith;
import org.junit.Before;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * Each class test case generates a basic graph using Graffoo, VOWL, and default custom settings.
 * The generated graph is then compared to the expected output. Since the output is static, this is
 * easily supported.
 * 
 */
@RunWith(Parameterized.class)
@ContextConfiguration("classpath:test-context.xml")
@TestPropertySource("classpath:test.properties")
@SpringBootTest()
public class ClassAndPropertyGraphTest {
	
	@Autowired private GraphController controller;
	private TestContextManager testContextManager;
	
	@Parameters
	public static Object[] params() {
	    return new Object[] { "custom", "graffoo", "vowl" };
	}
	
	@Parameter
	public String vis;
	
	// Set up for Spring tests
    @Before
    public void setUp() throws Exception {
         this.testContextManager = new TestContextManager(getClass());
         this.testContextManager.prepareTestInstance(this);
    }
    
    /**
	 * Tests a property with a datatype restriction
	 * @throws Exception (IOException, OntoGraphException, SAXException)
	 * 
	 */
	@Test
	public void testDatatypeRestrictions() throws Exception {

	    List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Classes And Properties", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(createGraphRequestModel(vis, "TestDatatypeRestrictions.ttl"), 
        		"TestDatatypeRestrictions", prefixes, titleEntries);
	}

	/**
     * Tests FOAF
	 * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
     */
    @Test
    public void testFOAF() throws Exception {
    	
	    List<String> prefixes = Arrays.asList("dc", "foaf", "owl", "rdf", "rdfs", "vs", "wot", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Classes And Properties", 
                "Ontology URI:  http://xmlns.com/foaf/0.1/", "Generated: ");
        
        createGraphMLAndCompareToMaster(createGraphRequestModel(vis, "TestFOAF.rdf"), 
        		"TestFOAF", prefixes, titleEntries);
    }
    
    /**
     * Tests FOAF with collapsed edges
	 * @throws Exception (IOException, OntoGraphException, SAXException)
	 * 
     */
    @Test
    public void testFOAFCollapsed() throws Exception {
    	
    	if (!"vowl".equals(vis)) {	// VOWL does not support collapsed edges
		    List<String> prefixes = Arrays.asList("dc", "foaf", "owl", "rdf", "rdfs", "vs", "wot", "xsd");
	        List<String> titleEntries = Arrays.asList("Title:  Test Classes And Properties", 
	                "Ontology URI:  http://xmlns.com/foaf/0.1/", "Generated: ");
	    	
	    	GraphRequestModel requestModel = createGraphRequestModel(vis, "TestFOAF.rdf");
	    	requestModel.setCollapseEdges("collapseTrue");
	        createGraphMLAndCompareToMaster(requestModel, "TestFOAFCollapsed", prefixes, titleEntries);
    	}
    }
    
    /**
     * Tests the Turtle Primer
	 * @throws Exception (IOException, OntoGraphException, SAXException)
	 * 
     */
    @Test
    public void testPrimer() throws Exception {
    	
	    List<String> prefixes = Arrays.asList("f", "g", "owl", "owl2", "rdf", "rdfs", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Classes And Properties", 
                "Ontology URI:  http://example.com/owl/families", "Generated: ");
    	
        createGraphMLAndCompareToMaster(createGraphRequestModel(vis, "TestTurtlePrimer.ttl"), 
            		"TestTurtlePrimer", prefixes, titleEntries);
    }
    
    /**
     * Tests FOAF with collapsed edges
	 * @throws Exception (IOException, OntoGraphException, SAXException)
	 * 
     */
    @Test
    public void testPrimerCollapsed() throws Exception {
    	
    	if (!"vowl".equals(vis)) {	// VOWL does not support collapsed edges
		    List<String> prefixes = Arrays.asList("f", "g", "owl", "owl2", "rdf", "rdfs", "xsd");
	        List<String> titleEntries = Arrays.asList("Title:  Test Classes And Properties",  
	                "Ontology URI:  http://example.com/owl/families", "Generated: ");
	        
	    	GraphRequestModel requestModel = createGraphRequestModel(vis, "TestTurtlePrimer.ttl");
	    	requestModel.setCollapseEdges("collapseTrue");
	        createGraphMLAndCompareToMaster(requestModel, "TestTurtlePrimerCollapsed", prefixes, titleEntries);
    	}
    }
    
    /**
	 * Sets up a graph request for the specified test input (fileName) and visualization.
	 * 
	 * @param  visualization (String)
	 * @param  fileName (String) holding the RDF triples 
	 * @throws IOException
	 * 
	 */
	private static GraphRequestModel createGraphRequestModel(final String visualization, final String fileName) 
			throws IOException {
		
		if ("custom".equals(visualization)) {
			GraphRequestModel requestModel = new GraphRequestModel("Test Classes And Properties", "reasoningFalse", 
					fileName, TestUtils.readFile("src/test/resources/bothClassAndPropertyTestFiles/" + fileName), 
					visualization, "both", false);
			
			// CHANGEME - Custom class definitions defaults
            requestModel.setCollapseEdges("collapseFalse");
            requestModel.setObjNodeShape("roundRectangle");
            requestModel.setObjFillColor("#FFFF99");
            requestModel.setObjTextColor("#000000");
            requestModel.setObjBorderColor("#000000");
            requestModel.setObjBorderType("solid");
            requestModel.setDataNodeShape("none");
            requestModel.setDataFillColor("#FFFFFF");
            requestModel.setDataTextColor("#000000");
            requestModel.setDataBorderColor("#000000");
            requestModel.setDataBorderType("solid");
            requestModel.setObjPropSourceShape("circleEmpty");
            requestModel.setObjPropTargetShape("triangleEmpty");
            requestModel.setObjPropEdgeColor("#000000");
            requestModel.setObjPropEdgeType("solid");
            requestModel.setDataPropSourceShape("none");
            requestModel.setDataPropTargetShape("angleBracket");
            requestModel.setDataPropEdgeColor("#000000");
            requestModel.setDataPropEdgeType("solid");
            requestModel.setAnnPropSourceShape("none");
            requestModel.setAnnPropTargetShape("angleBracket");
            requestModel.setAnnPropEdgeColor("#000000");
            requestModel.setAnnPropEdgeType("solid");
            requestModel.setRdfPropSourceShape("none");
            requestModel.setRdfPropTargetShape("angleBracket");
            requestModel.setRdfPropEdgeColor("#000000");
            requestModel.setRdfPropEdgeType("solid");

            requestModel.setSubclassOfSourceShape("none");
            requestModel.setSubclassOfTargetShape("triangleEmpty");
            requestModel.setSubclassOfLineColor("#000000");
            requestModel.setSubclassOfLineType("solid");
			
			return requestModel;
		
		} else {
			return new GraphRequestModel("Test Classes And Properties", "reasoningFalse", fileName, 
					TestUtils.readFile("src/test/resources/bothClassAndPropertyTestFiles/" + fileName), visualization,
					"both", false);
		}
	}
	/**
     * Compares the test-generated GraphML output to the master reference for class graphs.
     * 
     * @param  GraphRequestModel GraphRequestModel 
     * @param  fileName String identifying the unique part of the file names that hold the 
     *              expected values for nodes and edges
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
     */
    private void createGraphMLAndCompareToMaster(GraphRequestModel requestModel, final String fileName, 
            List<String> expectedPrefixes, List<String> expectedTitle) throws Exception {
        
        String testXML = controller.graph(requestModel).getGraphML();
        TestUtils.testGraphMLOutput("both", requestModel.getVisualization(), fileName, 
        		expectedPrefixes, expectedTitle, testXML);
    }
}
