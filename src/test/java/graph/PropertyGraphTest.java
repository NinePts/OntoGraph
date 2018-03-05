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
 * Each property test case generates a basic graph using Graffoo, VOWL and default custom settings.
 * The generated graph is then compared to a master.
 * 
 */
@RunWith(Parameterized.class)
@ContextConfiguration("classpath:test-context.xml")
@TestPropertySource("classpath:test.properties")
@SpringBootTest()
public class PropertyGraphTest {

	private static final String COLLAPSE_TRUE = "collapseTrue";
	private static final String COLLAPSE_FALSE = "collapseFalse";
    
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
	 * Tests an annotation  property 
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
	 */
    @Test
	public void testAnnotationProperties() throws Exception {
        
        List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Properties", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(
        		createPropertyGraphRequestModel(vis, "TestAnnotationProperty.ttl", COLLAPSE_FALSE),
                "TestAnnotationProperty", prefixes, titleEntries);
    }

	/**
	 * Tests a datatype property with an undefined domain
	 * The range of the datatype property is two datatypes
	 * 
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
	 */
	@Test
	public void testDataPropertyNoDomain() throws Exception {
	    
	    List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Properties", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(
        		createPropertyGraphRequestModel(vis, "TestDataPropertyNoDomain.ttl", COLLAPSE_FALSE),
                "TestDataPropertyNoDomain", prefixes, titleEntries);
	}

	/**
	 * Tests a datatype property with an undefined range
	 * The domain of the datatype property is an intersection-of declaration
	 * 
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
	 */
	@Test
	public void testDataPropertyNoRange() throws Exception {
	    
	    List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Properties", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(
        		createPropertyGraphRequestModel(vis, "TestDataPropertyNoRange.ttl", COLLAPSE_FALSE),
                "TestDataPropertyNoRange", prefixes, titleEntries);
	}

	/**
	 * Tests a datatype property defined with a standard domain and a range that is a
	 * datatype restriction
	 * 
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
	 */
	@Test
	public void testDatatypeRestrictions() throws Exception {
	    
	    List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Properties", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(
        		createPropertyGraphRequestModel(vis, "TestDatatypeRestrictions.ttl", COLLAPSE_FALSE),
                "TestDatatypeRestrictions", prefixes, titleEntries);
	}

	/**
	 * Tests the graphing of FOAF property definitions
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
	 */
	@Test
	public void testFOAF() throws Exception {
	    
	    List<String> prefixes = Arrays.asList("dc", "foaf", "owl", "rdf", "rdfs", "vs", "wot", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Properties", 
                "Ontology URI:  http://xmlns.com/foaf/0.1/", "Generated: ");
        
        createGraphMLAndCompareToMaster(
        		createPropertyGraphRequestModel(vis, "TestFOAF.rdf", COLLAPSE_FALSE),
                "TestFOAF", prefixes, titleEntries);
	}

	/**
	 * Tests the graphing of FOAF property definitions, with collapsed edges
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
	 */
	@Test
	public void testFOAFCollapsed() throws Exception {
		
		if (!"vowl".equals(vis)) {
		    List<String> prefixes = Arrays.asList("dc", "foaf", "owl", "rdf", "rdfs", "vs", "wot", "xsd");
	        List<String> titleEntries = Arrays.asList("Title:  Test Properties", 
	                "Ontology URI:  http://xmlns.com/foaf/0.1/", "Generated: ");
	        
	        createGraphMLAndCompareToMaster(
	        		createPropertyGraphRequestModel(vis, "TestFOAF.rdf", COLLAPSE_TRUE),
	                "TestFOAFCollapsed", prefixes, titleEntries);
		}
	}

	/**
	 * Tests both datatype and object properties where some are external to the ontology namespace
	 * No domains or ranges are defined
	 * 
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
	 */
	@Test
	public void testPropertiesMixed() throws Exception {
	    
	    List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "test", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Properties", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(
        		createPropertyGraphRequestModel(vis, "TestPropertiesMixed.ttl", COLLAPSE_FALSE),
                "TestPropertiesMixed", prefixes, titleEntries);
	}

	/**
	 * Tests both datatype and object properties where some are external to the ontology namespace,
	 *    with collapsed edges
	 * No domains or ranges are defined
	 * 
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
	 */
	@Test
	public void testPropertiesMixedCollapsed() throws Exception {
		
		if (!"vowl".equals(vis)) {
		    List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "test", "xsd");
	        List<String> titleEntries = Arrays.asList("Title:  Test Properties", 
	                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
	        
	        createGraphMLAndCompareToMaster(
	        		createPropertyGraphRequestModel(vis, "TestPropertiesMixed.ttl", COLLAPSE_TRUE),
	                "TestPropertiesMixedCollapsed", prefixes, titleEntries);
		}
	}

	/**
	 * Tests an object property with an undefined domain
	 * 
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
	 */
	@Test
	public void testObjPropertyNoDomain() throws Exception {
	    
	    List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Properties", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(
        		createPropertyGraphRequestModel(vis, "TestObjPropertyNoDomain.ttl", COLLAPSE_FALSE),
                "TestObjPropertyNoDomain", prefixes, titleEntries);
	}

	/**
     * Tests an object property with an undefined range
     * The domain of the object property is a one-of declaration
     * 
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
     */
    @Test
    public void testObjPropertyNoRange() throws Exception {
        
        List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Properties", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(
        		createPropertyGraphRequestModel(vis, "TestObjPropertyNoRange.ttl", COLLAPSE_FALSE),
                "TestObjPropertyNoRange", prefixes, titleEntries);
    }
    
    /**
	 * Tests a datatype and an object property that are functional properties and have 
	 * domains and ranges.  Other object properties are defined that test the inverse functional,
	 * asymmetric, symmetric, reflexive, irreflexive and transitive flags.
	 * 
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
	 */
	@Test
	public void testPropertiesAndFlags() throws Exception {
	    
	    List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Properties", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(
        		createPropertyGraphRequestModel(vis, "TestPropertiesAndFlags.ttl", COLLAPSE_FALSE),
                "TestPropertiesAndFlags", prefixes, titleEntries);
	}

	/**
	 * Tests a datatype and an object property that are functional properties and have 
	 * domains and ranges.  Other object properties are defined that test the inverse functional,
	 * asymmetric, symmetric, reflexive, irreflexive and transitive flags, with collapsed edges.
	 * 
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
	 */
	@Test
	public void testPropertiesAndFlagsCollapsed() throws Exception {
		
		if (!"vowl".equals(vis)) {
		    List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "xsd");
	        List<String> titleEntries = Arrays.asList("Title:  Test Properties", 
	                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
	        
	        createGraphMLAndCompareToMaster(
	        		createPropertyGraphRequestModel(vis, "TestPropertiesAndFlags.ttl", COLLAPSE_TRUE),
	                "TestPropertiesAndFlagsCollapsed", prefixes, titleEntries);
		}
	}

	/**
     * Tests two object properties - one with multiple domains and one with multiple ranges
     * (The test file also includes angle brackets in the Class1 label)
     * 
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
     */
    @Test
    public void testPropertiesImplicitIntersection() throws Exception {
        
        List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Properties", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(
        		createPropertyGraphRequestModel(vis, "TestPropertiesImplicitIntersection.ttl", COLLAPSE_FALSE),
                "TestPropertiesImplicitIntersection", prefixes, titleEntries);
    }

	/**
     * Tests a datatype and an object property with undefined domains and ranges
     * Both properties are functional properties
     * 
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
     */
    @Test
    public void testPropertiesNoDomainsNoRangesFunctional() throws Exception {
        
        List<String> prefixes = Arrays.asList("ninepts", "owl", "rdf", "rdfs", "xsd");
        List<String> titleEntries = Arrays.asList("Title:  Test Properties", 
                "Ontology URI:  http://purl.org/ninepts/test", "Generated: ");
        
        createGraphMLAndCompareToMaster(
        		createPropertyGraphRequestModel(vis, "TestPropertiesNoDomainsNoRangesFunctional.ttl", COLLAPSE_FALSE),
                "TestPropertiesNoDomainsNoRangesFunctional", prefixes, titleEntries);
    }
    
    /**
	 * Tests the W3C Turtle primer file
	 * 
     * @throws Exception (IOException, OntoGraphException, SAXException)
     * 
	 */
	@Test
	public void testTurtlePrimer() throws Exception {
	    
	    List<String> prefixes = Arrays.asList("f", "g", "owl", "owl2", "rdf", "rdfs", "xsd");
	    List<String> titleEntries = Arrays.asList("Title:  Test Properties", 
	            "Ontology URI:  http://example.com/owl/families", "Generated: ");
	    
	    createGraphMLAndCompareToMaster(
	    		createPropertyGraphRequestModel(vis, "TestTurtlePrimer.ttl", COLLAPSE_FALSE),
	            "TestTurtlePrimer", prefixes, titleEntries);
	}

	/**
     * Compares the test-generated GraphML output to the master reference for property graphs.
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
        TestUtils.testGraphMLOutput("property", requestModel.getVisualization(), fileName, 
        		expectedPrefixes, expectedTitle, testXML);
    }
    
    /**
     * Sets up a graph request for the specified test input (fileName) and vis.
     * 
     * @param  visualization (String) 
     * @param  fileName (String) holding the RDF triples 
     * @param  collapseEdges (String) set to either "collapseTrue" or "collapseFalse"
     * @throws IOException
     * 
     */
    private static GraphRequestModel createPropertyGraphRequestModel(final String visualization,
    		final String fileName, final String collapseEdges) throws IOException {
        
        GraphRequestModel requestModel = new GraphRequestModel("Test Properties", "reasoningFalse", fileName, 
                TestUtils.readFile("src/test/resources/propertyTestFiles/" + fileName), visualization,
                "property", false);
        requestModel.setCollapseEdges(collapseEdges);
        
        if ("custom".equals(visualization)) {
            // CHANGEME - Custom property definitions defaults
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
        }
        
        return requestModel;
    }
}