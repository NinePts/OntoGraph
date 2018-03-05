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

import graph.models.GraphRequestModel;

/**
 * Each RDF test case generates a basic graph using Graffoo, UML, VOWL and default custom settings.
 * The generated graph is then compared to a master.
 * 
 */
@RunWith(Parameterized.class)
@ContextConfiguration("classpath:test-context.xml")
@TestPropertySource("classpath:test.properties")
@SpringBootTest()
public class RDFGraphTest {
    
    @Autowired private GraphController controller;
    private TestContextManager testContextManager;
    
    @Parameters
    public static Object[] params() {
        // return new Object[] { "custom", "graffoo", "uml", "vowl" };
        return new Object[] { "custom", "vowl" };
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
	 * Tests individuals with a container of a Bag, Alt or Seq (each container has 2 strings)
	 * @throws Exception (IOException, OntoGraphException, SAXException)
	 * 
	 */
    /*
	@Test
	public void testContainers() throws Exception {
	    
		if (!"vowl".equals(vis)) {
	        List<String> prefixes = Arrays.asList("dc", "dcterms", "owl", "rdf", "rdfs", "xsd");
	        List<String> titleEntries = Arrays.asList("Title:  Test RDF", 
	                "Ontology URI:  None defined", "Generated: ");
	    	
	        GraphRequestModel requestModel = createRDFGraphRequestModel(vis, "individual", 
	        		"DCore-Magazine-Containers.ttl");
	        createGraphMLAndCompareToMaster(requestModel, "TestContainers", prefixes, titleEntries);
		}
	}
	*/

	/**
     * Tests individuals based on the Dublin Core
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
     */
    @Test
    public void testDCIndividuals() throws Exception {
        
    	if (!"vowl".equals(vis)) {
	        List<String> prefixes = Arrays.asList("dc", "dcterms", "owl", "rdf", "rdfs", "xsd");
	        List<String> titleEntries = Arrays.asList("Title:  Test RDF", 
	                "Ontology URI:  None defined", "Generated: ");
	    	
	        GraphRequestModel requestModel = createRDFGraphRequestModel(vis, "individual", 
	        		"DCore-Example.rdf");
	        createGraphMLAndCompareToMaster(requestModel, "TestDCIndividuals",
	        		 prefixes, titleEntries);
    	}
    }

	/**
     * Tests class and datatype definitions of Dublin Core
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
     */
    @Test
    public void testDublinCoreClasses() throws Exception {

    	List<String> prefixes = Arrays.asList("dcam", "dcterms", "owl", "rdf", "rdfs", "skos");
    	List<String> titleEntries = Arrays.asList("Title:  Test RDF", 
    			"Ontology URI:  None defined", "Generated: ");
    	
        createGraphMLAndCompareToMaster(createRDFGraphRequestModel(vis, "class", "dcterms-1.ttl"),
        		"TestDublinCoreClasses", prefixes, titleEntries);
    }
    
    /**
     * Tests property definitions of Dublin Core
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
     */
    @Test
    public void testDublinCoreProperties() throws Exception {

    	List<String> prefixes = Arrays.asList("dcam", "dcterms", "owl", "rdf", "rdfs", "skos");
    	List<String> titleEntries = Arrays.asList("Title:  Test RDF", 
    			"Ontology URI:  None defined", "Generated: ");
    	
        createGraphMLAndCompareToMaster(createRDFGraphRequestModel(vis, "property", "dcterms-1.ttl"),
        		"TestDublinCoreProperties", prefixes, titleEntries);
    }
    
    /**
     * Tests property definitions of Dublin Core using collapsed properties
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
     */
    @Test
    public void testDublinCorePropertiesCollapsed() throws Exception {

    	if (!"vowl".equals(vis)) {
	    	List<String> prefixes = Arrays.asList("dcam", "dcterms", "owl", "rdf", "rdfs", "skos");
	    	List<String> titleEntries = Arrays.asList("Title:  Test RDF", 
	    			"Ontology URI:  None defined", "Generated: ");
	
	        GraphRequestModel requestModel = createRDFGraphRequestModel(vis, "property", "dcterms-1.ttl");
	        requestModel.setCollapseEdges("collapseTrue");
	        createGraphMLAndCompareToMaster(requestModel, "TestDCPropertiesCollapsed", prefixes, titleEntries);
    	}
    }
    
    /**
	 * Tests individuals from the Gene Ontology
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
	 */
	@Test
	public void testGOIndividuals() throws Exception {
	    
		if (!"vowl".equals(vis)) {
	        List<String> prefixes = Arrays.asList("go", "owl", "rdf", "rdfs", "xsd");
	        List<String> titleEntries = Arrays.asList("Title:  Test RDF", 
	                "Ontology URI:  None defined", "Generated: ");
	        
	        createGraphMLAndCompareToMaster(createRDFGraphRequestModel(vis, "individual", "GeneOntology.rdf"),
	                "TestGO", prefixes, titleEntries);
		}
	}

	/**
	 * Tests individuals from the RDF Rossetti Archives example where rdfs:label and value are used
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
	 */
	/*
	@Test
	public void testIndividualsRossettiArchives() throws Exception {
	    
		if (!"vowl".equals(vis)) {
	        List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "test", "xsd");
	        List<String> titleEntries = Arrays.asList("Title:  Test RDF", 
	                "Ontology URI:  None defined", "Generated: ");
	        
	        createGraphMLAndCompareToMaster(createRDFGraphRequestModel(vis, "individual", "RossettiArchives.rdf"),
	                "TestRossettiArchives", prefixes, titleEntries);
		}
	}
	*/

	/**
     * Tests individuals with a list of members defined by rdf:Description elements
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
     */
    @Test
    public void testList() throws Exception {
        
    	if (!"vowl".equals(vis)) {
	        List<String> prefixes = Arrays.asList("cd", "owl", "rdf", "rdfs", "xsd");
	        List<String> titleEntries = Arrays.asList("Title:  Test RDF", 
	                "Ontology URI:  None defined", "Generated: ");
	        
	        createGraphMLAndCompareToMaster(createRDFGraphRequestModel(vis, "individual", "RdfList.rdf"),
	                "TestRDFList", prefixes, titleEntries);
    	}
    }
    
    /**
     * Tests individuals with lists (rdf:first/:rest) and an empty list
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
     */
    @Test
    public void testLists() throws Exception {
        
    	if (!"vowl".equals(vis)) {
	        List<String> prefixes = Arrays.asList("owl", "rdf", "rdfs", "xsd");
	        List<String> titleEntries = Arrays.asList("Title:  Test RDF", 
	                "Ontology URI:  None defined", "Generated: ");
	        
	        createGraphMLAndCompareToMaster(createRDFGraphRequestModel(vis, "individual", "RdfLists.ttl"),
	                "TestRDFLists", prefixes, titleEntries);
    	}
    }
    
    /**
     * Tests reified statements (rdf:Statements) 
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
     */
    @Test
    public void testReifiedStatements() throws Exception {
        
    	if (!"vowl".equals(vis)) {
    		List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "xsd");
	        List<String> titleEntries = Arrays.asList("Title:  Test RDF", 
	                "Ontology URI:  None defined", "Generated: ");
	        
	        createGraphMLAndCompareToMaster(createRDFGraphRequestModel(vis, "individual", "ReifiedStatement.ttl"),
	                "TestReified", prefixes, titleEntries);
    	}
    }
    
    /**
     * Tests schema.org class definitions
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
     */
    /*
    @Test
    public void testSchemaClasses() throws Exception {
    	
        List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "test", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test RDF", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(createRDFGraphRequestModel(vis, "class", "schema.ttl"),
                "TestSchemaClasses", prefixes, titleEntries);
    }
    */
    
    /**
     * Tests schema.org property definitions, including the use of meta-concepts, 
     * domainIncludes and rangeIncludes
     * 
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
     */
    /*
    @Test
    public void testSchemaProperties() throws Exception {
    
    List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "test", "xsd");
    List<String> titleEntries = Arrays.asList("Title:  Test RDF", 
            "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
    
    createGraphMLAndCompareToMaster(createRDFGraphRequestModel(vis, "property", "schema.ttl"),
            "TestSchemaProperties", prefixes, titleEntries);
    } 
    */
    
    /**
     * Tests class and property definitions including the definition of a "base URI".
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
     */
    @Test
    public void testVehicleClassAndProperty() throws Exception {
        
    	if (!"vowl".equals(vis)) {
    		List<String> prefixes = Arrays.asList("owl", "rdf", "rdfs", "xsd");
	        List<String> titleEntries = Arrays.asList("Title:  Test RDF", 
	                "Ontology URI:  None defined", "Generated: ");
	        
	        createGraphMLAndCompareToMaster(createRDFGraphRequestModel(vis, "both", "Vehicles.ttl"),
	                "TestVehicles", prefixes, titleEntries);
    	}
    }
    
    /**
     * Tests individuals with an integer property and two "object" properties
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
     */
    @Test
    public void testVehiclesIndividuals() throws Exception {
        
    	if (!"vowl".equals(vis)) {
	        List<String> prefixes = Arrays.asList("owl", "rdf", "rdfs", "xsd");
	        List<String> titleEntries = Arrays.asList("Title:  Test RDF", 
	                "Ontology URI:  None defined", "Generated: ");
	        
	        createGraphMLAndCompareToMaster(createRDFGraphRequestModel(vis, "individual", "Vehicles.ttl"),
	                "TestIndividualVehicle", prefixes, titleEntries);
    	}
    }
    
    /**
	 * Compares the test-generated GraphML output to the master reference for individual graphs.
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
	    TestUtils.writeFile("src/test/resources/control/rdfAspects/" + vis + fileName + ".graphml", testXML);
	    TestUtils.testGraphMLOutput("rdf", requestModel.getVisualization(), fileName, 
	    		expectedPrefixes, expectedTitle, testXML);
	}
	
	/**
     * Sets up a graph request for the specified test input (fileName) and vis.
     * 
     * @param  visualization (String)
     * @param  graphType (String)
     * @param  fileName (String) holding the RDF triples 
     * @throws IOException
     * 
     */
    private static GraphRequestModel createRDFGraphRequestModel(final String visualization, 
    		final String graphType, final String fileName) throws IOException {
        
        GraphRequestModel requestModel = new GraphRequestModel("Test RDF", "reasoningFalse", fileName, 
                TestUtils.readFile("src/test/resources/rdfTestFiles/" + fileName), visualization, 
                graphType, false);
        
        if ("custom".equals(visualization)) {
			// CHANGEME - Custom class definitions defaults
			requestModel.setClassNodeShape("roundRectangle");
			requestModel.setClassFillColor("#FFFF99");
			requestModel.setClassTextColor("#000000");
			requestModel.setClassBorderColor("#000000");
			requestModel.setClassBorderType("solid");
			requestModel.setSubclassOfSourceShape("none");
			requestModel.setSubclassOfTargetShape("angleBracket");
			requestModel.setSubclassOfLineColor("#000000");
			requestModel.setSubclassOfLineType("solid");
			
            // CHANGEME - Custom property definitions defaults
            requestModel.setCollapseEdges("collapseFalse");
            requestModel.setObjNodeShape("roundRectangle");
            requestModel.setObjFillColor("#FFFF99");
            requestModel.setObjTextColor("#000000");
            requestModel.setObjBorderColor("#000000");
            requestModel.setObjBorderType("solid");
            requestModel.setDataNodeShape("none");
            requestModel.setDataFillColor("#FFFFFF");
            requestModel.setDataTextColor("#000000");
            requestModel.setDataBorderColor("#FFFFFF");
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
	        	
            // CHANGEME - Custom class definitions defaults
            requestModel.setIndividualNodeShape("smallCircle");
            requestModel.setIndividualFillColor("#FF99CC");
            requestModel.setIndividualTextColor("#000000");
            requestModel.setIndividualBorderColor("#000000");
            requestModel.setIndividualBorderType("solid");
            requestModel.setTypeOfSourceShape("none");
            requestModel.setTypeOfTargetShape("triangleEmpty");
            requestModel.setTypeOfLineColor("#000000");
            requestModel.setTypeOfLineType("solid");
            
        } else if ("uml".equals(visualization)) {
        	requestModel.setUmlNodeColor("#FFFF99");
        	requestModel.setUmlDataNodeColor("#CCCC66");
        }
        
        return requestModel;
    }
}