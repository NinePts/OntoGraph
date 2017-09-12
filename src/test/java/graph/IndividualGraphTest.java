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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestPropertySource;

import org.xml.sax.SAXException;

import graph.models.GraphRequestModel;

@RunWith(Parameterized.class)
@ContextConfiguration("classpath:test-context.xml")
@TestPropertySource("classpath:test.properties")
@SpringBootTest()
public class IndividualGraphTest {
    
    @Autowired private GraphController controller;
    private TestContextManager testContextManager;
    
    @Parameters
    public static Object[] params() {
        return new Object[] { "custom", "graffoo", "uml" };
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
     * Tests two defined individuals of one type, with a third external individual.
     * Includes one object and one datatype property (the range of the datatype is a string).
     * 
     * @throws IOException
     * @throws OntoGraphException
     * @throws SAXException
     */
    @Test
    public void testIndividualsA() throws Exception {

    	List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "test", "xsd");
    	List<String> titleEntries = Arrays.asList("Title:  Test Individuals", 
    			"Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
    	
        createGraphMLAndCompareToMaster(createIndividualGraphRequestModel(vis, "TestIndividualsA.ttl"),
        		"TestIndividualsA", prefixes, titleEntries);
    }
    
    /**
     * Same as testIndividualsA, but with labels, and the range of the datatype is removed.
     * 
     * @throws IOException
     * @throws OntoGraphException
     * @throws SAXException
     */
    @Test
    public void testIndividualsB() throws Exception {
        
        List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "test", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Individuals", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(createIndividualGraphRequestModel(vis, "TestIndividualsB.ttl"),
                "TestIndividualsB", prefixes, titleEntries);
    }
    
    /**
     * Same as testIndividualsA, but the individuals are not types of owl:NamedIndividual
     * Also, the datatype property is an unsignedInt.
     * 
     * @throws IOException
     * @throws OntoGraphException
     * @throws SAXException
     */
    @Test
    public void testIndividualsC() throws Exception {
        
        List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "test", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Individuals", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(createIndividualGraphRequestModel(vis, "TestIndividualsC.ttl"),
                "TestIndividualsC", prefixes, titleEntries);
    }
    
    /**
     * Same as testIndividualsC, but the properties are not defined.
     * 
     * @throws IOException
     * @throws OntoGraphException
     * @throws SAXException
     */
    @Test
    public void testIndividualsD() throws Exception {
    	
        List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "test", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Individuals", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(createIndividualGraphRequestModel(vis, "TestIndividualsD.ttl"),
                "TestIndividualsD", prefixes, titleEntries);
    }
    
    /**
     * Pizza example with an undefined individual referenced as an object property.
     * 
     * @throws IOException
     * @throws OntoGraphException
     * @throws SAXException
     */
    @Test
    public void testPizzaTest() throws Exception {
        
        List<String> prefixes = Arrays.asList("ex", "owl", "pz", "rdf", "rdfs", "xml", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Individuals", 
                "Ontology URI:  None defined", "Generated: ");
        
        createGraphMLAndCompareToMaster(createIndividualGraphRequestModel(vis, "PizzaTest.ttl"),
                "PizzaTest", prefixes, titleEntries);
    }
    
    /**
     * Turtle primer with individuals with restrictions.
     * 
     * @throws IOException
     * @throws OntoGraphException
     * @throws SAXException
     */
    @Test
    public void testTurtlePrimer() throws Exception {
        
        List<String> prefixes = Arrays.asList("f", "g", "owl", "owl2", "rdf", "rdfs", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Individuals", 
                "Ontology URI:  http://example.com/owl/families", "Generated: ");
        
        createGraphMLAndCompareToMaster(createIndividualGraphRequestModel(vis, "TestTurtlePrimer.ttl"),
                "TestTurtlePrimer", prefixes, titleEntries);
    }
    
    /**
	 * Compares the test-generated GraphML output to the master reference for individual graphs.
	 * 
	 * @param  GraphRequestModel GraphRequestModel 
	 * @param  fileName String identifying the unique part of the file names that hold the 
	 *              expected values for nodes and edges
	 * @throws OntoGraphException 
	 * @throws IOException
	 * @throws SAXException 
	 * 
	 */
	private void createGraphMLAndCompareToMaster(GraphRequestModel requestModel, final String fileName, 
			List<String> expectedPrefixes, List<String> expectedTitle) throws Exception {
	    
	    String testXML = controller.graph(requestModel).getGraphML();
	    TestUtils.testGraphMLOutput("individual", requestModel.getVisualization(), fileName, 
	    		expectedPrefixes, expectedTitle, testXML);
	}
	
	/**
     * Sets up a graph request for the specified test input (fileName) and vis.
     * 
     * @param  visualization (String)
     * @param  fileName (String) holding the RDF triples 
     * @throws IOException
     * 
     */
    private static GraphRequestModel createIndividualGraphRequestModel(final String visualization, 
    		final String fileName) throws IOException {
        
        GraphRequestModel requestModel = new GraphRequestModel("Test Individuals", fileName, 
                TestUtils.readFile("src/test/resources/individualTestFiles/" + fileName), visualization, 
                "individual", false);
        
        if ("custom".equals(visualization)) {
            // CHANGEME - Custom class definitions defaults
            requestModel.setIndividualNodeShape("smallCircle");
            requestModel.setIndividualFillColor("#FF99CC");
            requestModel.setIndividualTextColor("#000000");
            requestModel.setIndividualBorderColor("#000000");
            requestModel.setIndividualBorderType("solid");
            requestModel.setClassNodeShape("roundRectangle");
            requestModel.setClassFillColor("#FFFF99");
            requestModel.setClassTextColor("#000000");
            requestModel.setClassBorderColor("#000000");
            requestModel.setClassBorderType("solid");
            requestModel.setTypeOfSourceShape("none");
            requestModel.setTypeOfTargetShape("triangleEmpty");
            requestModel.setTypeOfLineColor("#000000");
            requestModel.setTypeOfLineType("solid");
            requestModel.setDataNodeShape("none");
            requestModel.setDataFillColor("#FFFFFF");
            requestModel.setDataTextColor("#000000");
            requestModel.setDataBorderColor("#FFFFFF");
            requestModel.setDataBorderType("solid");
            requestModel.setDataPropSourceShape("none");
            requestModel.setDataPropTargetShape("triangleSolid");
            requestModel.setDataPropEdgeColor("#000000");
            requestModel.setDataPropEdgeType("solid");
            requestModel.setObjPropSourceShape("none");
            requestModel.setObjPropTargetShape("triangleSolid");
            requestModel.setObjPropEdgeColor("#000000");
            requestModel.setObjPropEdgeType("solid");
        }
        
        return requestModel;
    }
}