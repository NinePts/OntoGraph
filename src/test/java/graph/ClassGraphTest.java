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
 * Each class test case generates a basic graph using Graffoo, VOWL, UML and default custom settings.
 * The generated graph is then compared to the expected output. Since the output is static, this is
 * easily supported.
 * 
 */
@RunWith(Parameterized.class)
@ContextConfiguration("classpath:test-context.xml")
@TestPropertySource("classpath:test.properties")
@SpringBootTest()
public class ClassGraphTest {
	
	@Autowired private GraphController controller;
	private TestContextManager testContextManager;
	
	@Parameters
	public static Object[] params() {
	    return new Object[] { "custom", "graffoo", "uml", "vowl" };
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
     * Tests combinations of uses of blank nodes that have resulted in errors
	 * @throws Exception (IOException, OntoGraphException, SAXException)
	 * 
     */
    /*
    @Test
    public void testBlankNodeIssues() throws Exception {
        
        List<String> prefixes = Arrays.asList("fabio", "ninepts", "owl", "rdf", "rdfs", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Classes", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(createClassGraphRequestModel(vis, "TestBlankNodeIssues.ttl"),
                "TestBlankNodeIssues", prefixes, titleEntries);
    }
    */
    
    /**
     * Tests five nodes, with multiple levels of inheritance and multiple inheritance
     * Also, there are two namespaces for nodes, both are defined as prefixes
     * 
	 * @throws Exception (IOException, OntoGraphException, SAXException)
	 * 
     */
    @Test
    public void testClassesA() throws Exception {
        
        List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "test", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Classes", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(createClassGraphRequestModel(vis, "TestClassesA.ttl"),
                "TestClassesA", prefixes, titleEntries);
    }

    /**
     * Same as testClassesA, but with labels
	 * @throws Exception (IOException, OntoGraphException, SAXException)
	 * 
     */
    @Test
    public void testClassesB() throws Exception {
        
        List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "test", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Classes", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(createClassGraphRequestModel(vis, "TestClassesB.ttl"),
                "TestClassesB", prefixes, titleEntries);
    }
    
    /**
	 * Tests five nodes, with multiple levels of inheritance and multiple inheritance
	 * No ontology defined (all "external" VOWL entities) and no prefixes defined
	 * 
	 * @throws Exception (IOException, OntoGraphException, SAXException)
	 * 
	 */
	@Test
	public void testClassesNoOntolNoPrefixes() throws Exception {
	    
	    List<String> prefixes = Arrays.asList("owl", "rdf", "rdfs", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Classes", 
                "Ontology URI:  None defined", "Generated: ");
        
        createGraphMLAndCompareToMaster(createClassGraphRequestModel(vis, "TestClassesNoOntolNoPrefixes.ttl"),
                "TestClassesNoOntolNoPrefixes", prefixes, titleEntries);
	}

	/**
     * Tests three nodes, where one is the complement of the union of the other two
	 * @throws Exception (IOException, OntoGraphException, SAXException)
	 * 
     */
    @Test
    public void testComplementUnionOf() throws Exception {

	    List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Classes", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(createClassGraphRequestModel(vis, "TestComplementUnionOf.ttl"), 
        		"TestComplementUnionOf", prefixes, titleEntries);
    }
    
    /**
	 * Tests a property with a datatype restriction (which only is relevant for a UML class/property diagram)
	 * @throws Exception (IOException, OntoGraphException, SAXException)
	 * 
	 */
	@Test
	public void testDatatypeRestrictions() throws Exception {
		
		if ("uml".equals(vis)) {
		    List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "xsd");
	        List<String> titleEntries = Arrays.asList("Title:  Test Classes", 
	                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
	        
	        createGraphMLAndCompareToMaster(createClassGraphRequestModel(vis, "TestDatatypeRestrictions.ttl"), 
	        		"TestDatatypeRestrictions", prefixes, titleEntries);
		}
	}

	/**
     * Tests two nodes, which are disjoint with each other
	 * @throws Exception (IOException, OntoGraphException, SAXException)
	 * 
     */
    @Test
    public void testDisjoint() throws Exception {
    	
	    List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Classes", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(createClassGraphRequestModel(vis, "TestDisjoint.ttl"), 
        		"TestDisjoint", prefixes, titleEntries);
    }
    
    /**
     * Tests three nodes, where one is disjoint with the union of the other two
	 * @throws Exception (IOException, OntoGraphException, SAXException)
	 * 
     */
    @Test
    public void testDisjointUnionOf() throws Exception {
    	
	    List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Classes", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(createClassGraphRequestModel(vis, "TestDisjointUnionOf.ttl"), 
        		"TestDisjointUnionOf", prefixes, titleEntries);
    }
    
    /**
     * Tests two nodes, which are equivalent to each other
	 * @throws Exception (IOException, OntoGraphException, SAXException)
	 * 
     */
    @Test
    public void testEquivalent() throws Exception {
    	
	    List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Classes", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(createClassGraphRequestModel(vis, "TestEquivalent.ttl"), 
        		"TestEquivalent", prefixes, titleEntries);
    }
    
    /**
	 * Tests two nodes, complements of each other
	 * @throws Exception (IOException, OntoGraphException, SAXException)
	 * 
	 */
	@Test
	public void testEquivalentComplementOf() throws Exception {
    	
	    List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Classes", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(createClassGraphRequestModel(vis, "TestEquivalentComplementOf.ttl"), 
        		"TestEquivalentComplementOf", prefixes, titleEntries);
	}

	/**
     * Tests three nodes, where one is equivalent to the intersection of the other two
	 * @throws Exception (IOException, OntoGraphException, SAXException)
	 * 
     */
    @Test
    public void testEquivalentIntersectionOf() throws Exception {
    	
	    List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Classes", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(createClassGraphRequestModel(vis, "TestEquivalentIntersectionOf.ttl"), 
        		"TestEquivalentIntersectionOf", prefixes, titleEntries);
    }
    
    /**
     * Tests one node that is equivalent to a list of two individuals
	 * @throws Exception (IOException, OntoGraphException, SAXException)
	 * 
	 */
    @Test
    public void testEquivalentOneOf() throws Exception {
    	
	    List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Classes", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(createClassGraphRequestModel(vis, "TestEquivalentOneOf.ttl"), 
        		"TestEquivalentOneOf", prefixes, titleEntries);
    }
    
    /**
     * Tests three nodes, where one is equivalent to the union of the other two
	 * @throws Exception (IOException, OntoGraphException, SAXException)
	 * 
     */
    @Test
    public void testEquivalentUnionOf() throws Exception {
    	
	    List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Classes", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(createClassGraphRequestModel(vis, "TestEquivalentUnionOf.ttl"), 
        		"TestEquivalentUnionOf", prefixes, titleEntries);
    }
    
    /**
     * Tests six nodes, where one is equivalent to the union of the intersection of two and the union of two
     * One class is "external" to the ontology namespace
     * 
	 * @throws Exception (IOException, OntoGraphException, SAXException)
	 * 
     */
    @Test
    public void testEquivalentUnionOfNested() throws Exception {
    	
	    List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "test", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Classes", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(createClassGraphRequestModel(vis, "TestEquivalentUnionOfNested.ttl"), 
        		"TestEquivalentUnionOfNested", prefixes, titleEntries);
    }
    
    /**
     * Tests FOAF
	 * @throws Exception (IOException, OntoGraphException, SAXException)
	 * 
     */
    @Test
    public void testFOAF() throws Exception {
    	
	    List<String> prefixes = Arrays.asList("dc", "foaf", "owl", "rdf", "rdfs", "vs", "wot", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Classes", 
                "Ontology URI:  http://xmlns.com/foaf/0.1/", "Generated: ");
    	
    	if ("uml".equals(vis)) {
    		GraphRequestModel requestModel = createClassGraphRequestModel(vis, "TestFOAF.rdf");
    		requestModel.setCollapseEdges("collapseFalse");
    		createGraphMLAndCompareToMaster(requestModel, "TestFOAF", prefixes, titleEntries);
    	} else {
            createGraphMLAndCompareToMaster(createClassGraphRequestModel(vis, "TestFOAF.rdf"), 
            		"TestFOAF", prefixes, titleEntries);
    	}
    }
    
    /**
     * Tests FOAF with collapsed edges for UML visualizations
	 * @throws Exception (IOException, OntoGraphException, SAXException)
	 * 
     */
    @Test
    public void testFOAFCollapsed() throws Exception {
    	
    	if ("uml".equals(vis)) {
		    List<String> prefixes = Arrays.asList("dc", "foaf", "owl", "rdf", "rdfs", "vs", "wot", "xsd");
	        List<String> titleEntries = Arrays.asList("Title:  Test Classes", 
	                "Ontology URI:  http://xmlns.com/foaf/0.1/", "Generated: ");
    	
    		GraphRequestModel requestModel = createClassGraphRequestModel(vis, "TestFOAF.rdf");
    		requestModel.setCollapseEdges("collapseTrue");
    		createGraphMLAndCompareToMaster(requestModel, "TestFOAFCollapsed", prefixes, titleEntries);
    	} 
    }
    
    /**
	 * Tests the W3C Turtle primer file
	 * @throws Exception (IOException, OntoGraphException, SAXException)
	 * 
	 */
	@Test
	public void testTurtlePrimer() throws Exception {
    	
	    List<String> prefixes = Arrays.asList("f", "g", "owl", "owl2", "rdf", "rdfs", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Classes", 
                "Ontology URI:  http://example.com/owl/families", "Generated: ");
    	
        createGraphMLAndCompareToMaster(createClassGraphRequestModel(vis, "TestTurtlePrimer.ttl"), 
            		"TestTurtlePrimer", prefixes, titleEntries);
	}

	/**
	 * Tests W3C Primer with collapsed edges for UML visualizations
	 * @throws Exception (IOException, OntoGraphException, SAXException)
	 * 
	 */
	@Test
	public void testTurtlePrimerCollapsed() throws Exception {
		
		if ("uml".equals(vis)) {
	    	
		    List<String> prefixes = Arrays.asList("f", "g", "owl", "owl2", "rdf", "rdfs", "xsd");
	        List<String> titleEntries = Arrays.asList("Title:  Test Classes", 
	                "Ontology URI:  http://example.com/owl/families", "Generated: ");
	        
			GraphRequestModel requestModel = createClassGraphRequestModel(vis, "TestTurtlePrimer.ttl");
			requestModel.setCollapseEdges("collapseTrue");
			createGraphMLAndCompareToMaster(requestModel, "TestTurtlePrimerCollapsed", prefixes, titleEntries);
		} 
	}

	/**
     * Tests a zip file including the files, TestClassesA and TestDisjoint
	 * @throws Exception (IOException, OntoGraphException, SAXException)
	 * 
     */
    @Test
    public void testZip() throws Exception {

	    List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Classes", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
		createGraphMLAndCompareToMaster(createClassGraphRequestModel(vis, "archive.zip"), 
				"TestZip", prefixes, titleEntries);
    }
    
    /**
	 * Sets up a graph request for the specified test input (fileName) and vis.
	 * 
	 * @param  visualization (String)
	 * @param  fileName (String) holding the RDF triples 
	 * @throws IOException
	 * 
	 */
	private static GraphRequestModel createClassGraphRequestModel(final String visualization, 
			final String fileName) throws IOException {

		GraphRequestModel requestModel = new GraphRequestModel("Test Classes", "reasoningFalse", fileName, 
				TestUtils.readFile("src/test/resources/classTestFiles/" + fileName), visualization,
				"class", false);
		requestModel.setCollapseEdges("collapseFalse");
		
		if ("custom".equals(visualization)) {
			// CHANGEME - Custom class definitions defaults
			requestModel.setClassNodeShape("roundRectangle");
			requestModel.setClassFillColor("#FFFF99");
			requestModel.setClassTextColor("#000000");
			requestModel.setClassBorderColor("#000000");
			requestModel.setClassBorderType("solid");
			requestModel.setDataNodeShape("none");
			requestModel.setDataFillColor("#FFFFFF");
			requestModel.setDataTextColor("#000000");
			requestModel.setDataBorderColor("#FFFFFF");
			requestModel.setDataBorderType("solid");
			requestModel.setSubclassOfSourceShape("none");
			requestModel.setSubclassOfTargetShape("angleBracket");
			requestModel.setSubclassOfLineColor("#000000");
			requestModel.setSubclassOfLineType("solid");
		} else if ("uml".equals(visualization)) {
			requestModel.setUmlNodeColor("#FFFFFF");
			requestModel.setUmlDataNodeColor("#FFFFFF");
		}
		
		return requestModel;
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
        
        String visualization = requestModel.getVisualization();
        String testXML = controller.graph(requestModel).getGraphML();
        TestUtils.testGraphMLOutput("class", visualization, fileName, expectedPrefixes, expectedTitle, testXML);
    }
}
