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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import graph.models.GraphRequestModel;

/**
 * Tests a variety of erroneous inputs for the GraphRequestModel. Since a final error string is built containing all
 * errors, each input must be tested separately.
 *
 */
@RunWith(SpringRunner.class)
@ContextConfiguration("classpath:test-context.xml")
@TestPropertySource("classpath:test.properties")
@SpringBootTest()
public class ErrorTest {

	@Autowired private GraphController controller;
	
    // Frequently used strings
    private static final String BLACK = "#000000";
    private static final String NONE = "none";
    private static final String SOLID = "solid";
    private static final String SPACE = " ";
    private static final String WHITE = "#FFFFFF";
	
	/**
	 * Tests error handling for an empty request model
	 * @throws IllegalArgumentException if successful
	 */
    @Test(expected = IllegalArgumentException.class)
    public void testEmptyGraphRequest() throws IllegalArgumentException {
        GraphRequestModel requestModel = new GraphRequestModel("", "reasoningFalse", "", "", "", "", false);
        GraphRequestValidator.validateRequest(requestModel);
    }
	
	/**
	 * Tests error handling for an empty ontology file
	 * @throws IllegalArgumentException if successful
	 */
    @Test(expected = OntoGraphException.class)
    public void testEmptyOntology() throws OntoGraphException {
        GraphRequestModel requestModel = createCustomGraphRequestModelWithDiffOntologyErrors("test.rdf", " ");
        controller.graph(requestModel);
    }
	
	/**
	 * Tests error handling for an ontology file with bad contents
	 * @throws IllegalArgumentException if successful
	 */
    @Test(expected = OntoGraphException.class)
    public void testInvalidOntology() throws OntoGraphException {
        GraphRequestModel requestModel = createCustomGraphRequestModelWithDiffOntologyErrors("test.rdf", 
        		"test file contents");
        controller.graph(requestModel);
    }
	
	/**
	 * Tests error handling for an ontology file with a bad suffix
	 * @throws IllegalArgumentException if successful
	 */
    @Test(expected = OntoGraphException.class)
    public void testBadOntologySuffix() throws OntoGraphException {
        GraphRequestModel requestModel = createCustomGraphRequestModelWithDiffOntologyErrors("test.foo", 
        		"test file contents");
        controller.graph(requestModel);
    }
    
    /**
	 * Tests error handling for an incorrect annotation property edge color (property graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousAnnPropEdgeColor() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomPropertyGraphRequestModel();
	    requestModel.setAnnPropEdgeColor(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an undefined annotation property edge type (property graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousAnnPropEdgeType() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomPropertyGraphRequestModel();
	    requestModel.setAnnPropEdgeType(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an undefined annotation property edge source arrow (property graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousAnnPropSourceShape() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomPropertyGraphRequestModel();
	    requestModel.setAnnPropSourceShape(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an undefined annotation property edge target arrow (property graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousAnnPropTargetShape() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomPropertyGraphRequestModel();
	    requestModel.setAnnPropTargetShape(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an incorrect class node border color (class graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousClassBorderColor() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomClassGraphRequestModel();
	    requestModel.setClassBorderColor(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an undefined class node border type (class graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousClassBorderType() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomClassGraphRequestModel();
	    requestModel.setClassBorderType(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an incorrect class node fill color (class graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousClassFillColor() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomClassGraphRequestModel();
	    requestModel.setClassFillColor(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
     * Tests error handling for an undefined class node shape (class graph)
     * @throws IllegalArgumentException if successful
     */
    @Test(expected = IllegalArgumentException.class)
    public void testErroneousClassNodeShape() throws IllegalArgumentException {
        GraphRequestModel requestModel = createCustomClassGraphRequestModel();
        requestModel.setClassNodeShape(SPACE);
        GraphRequestValidator.validateRequest(requestModel);
    }
    
    /**
	 * Tests error handling for an incorrect class node text color (class graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousClassTextColor() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomClassGraphRequestModel();
	    requestModel.setClassTextColor(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an undefined collapse edge request (property graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousCollapseEdges() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomPropertyGraphRequestModel();
	    requestModel.setCollapseEdges(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an incorrect datatype node border color (property graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousDataBorderColor() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomPropertyGraphRequestModel();
	    requestModel.setDataBorderColor(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an undefined datatype node border type (property graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousDataBorderType() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomPropertyGraphRequestModel();
	    requestModel.setDataBorderType(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an incorrect datatype node fill color (property graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousDataFillColor() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomPropertyGraphRequestModel();
	    requestModel.setDataFillColor(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an undefined datatype node shape (property graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousDataNodeShape() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomPropertyGraphRequestModel();
	    requestModel.setDataNodeShape(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an incorrect datatype node text color (property graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousDataTextColor() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomPropertyGraphRequestModel();
	    requestModel.setDataTextColor(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an incorrect datatype property edge color (property graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousDataPropEdgeColor() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomPropertyGraphRequestModel();
	    requestModel.setDataPropEdgeColor(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an undefined datatype property edge type (property graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousDataPropEdgeType() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomPropertyGraphRequestModel();
	    requestModel.setDataPropEdgeType(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an undefined datatype property edge source arrow (property graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousDataPropSourceShape() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomPropertyGraphRequestModel();
	    requestModel.setDataPropSourceShape(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an undefined datatype property edge target arrow (property graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousDataPropTargetShape() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomPropertyGraphRequestModel();
	    requestModel.setDataPropTargetShape(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an incorrect datatype node border color (individual graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousIndDataBorderColor() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomIndividualGraphRequestModel();
	    requestModel.setDataBorderColor(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an undefined datatype node border type (individual graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousIndDataBorderType() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomIndividualGraphRequestModel();
	    requestModel.setDataBorderType(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an incorrect datatype node border color (individual graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousIndDataFillColor() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomIndividualGraphRequestModel();
	    requestModel.setDataFillColor(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an undefined datatype node shape (individual graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousIndDataNodeShape() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomIndividualGraphRequestModel();
	    requestModel.setDataNodeShape(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an incorrect datatype node text color (individual graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousIndDataTextColor() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomIndividualGraphRequestModel();
	    requestModel.setDataTextColor(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an incorrect individual node border color (individual graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousIndividualBorderColor() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomIndividualGraphRequestModel();
	    requestModel.setIndividualBorderColor(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an undefined individual node border type (individual graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousIndividualBorderType() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomIndividualGraphRequestModel();
	    requestModel.setIndividualBorderType(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an incorrect individual node fill color (individual graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousIndividualFillColor() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomIndividualGraphRequestModel();
	    requestModel.setIndividualFillColor(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an undefined individual node shape (individual graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousIndividualNodeShape() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomIndividualGraphRequestModel();
	    requestModel.setIndividualNodeShape(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an incorrect individual node text color (individual graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousIndividualTextColor() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomIndividualGraphRequestModel();
	    requestModel.setIndividualTextColor(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an incorrect object node border color (property graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousObjBorderColor() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomPropertyGraphRequestModel();
	    requestModel.setObjBorderColor(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an undefined object node border type (property graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousObjBorderType() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomPropertyGraphRequestModel();
	    requestModel.setObjBorderType(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an incorrect object node fill color (property graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousObjFillColor() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomPropertyGraphRequestModel();
	    requestModel.setObjFillColor(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an undefined object node shape (property graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousObjNodeShape() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomPropertyGraphRequestModel();
	    requestModel.setObjNodeShape(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an incorrect object node text color (property graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousObjTextColor() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomPropertyGraphRequestModel();
	    requestModel.setObjTextColor(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an incorrect object property edge color (property graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousObjPropEdgeColor() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomPropertyGraphRequestModel();
	    requestModel.setObjPropEdgeColor(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an undefined object property edge type (property graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousObjPropEdgeType() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomPropertyGraphRequestModel();
	    requestModel.setObjPropEdgeType(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an undefined object property edge source arrow (property graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousObjPropSourceShape() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomPropertyGraphRequestModel();
	    requestModel.setObjPropSourceShape(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an undefined object property edge target arrow (property graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousObjPropTargetShape() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomPropertyGraphRequestModel();
	    requestModel.setObjPropTargetShape(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an undefined subclassOf edge line type (class graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousSubclassOfLineType() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomClassGraphRequestModel();
	    requestModel.setSubclassOfLineType(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
     * Tests error handling for an undefined subclassOf edge source shape (class graph)
     * @throws IllegalArgumentException if successful
     */
    @Test(expected = IllegalArgumentException.class)
    public void testErroneousSubclassOfSourceShape() throws IllegalArgumentException {
        GraphRequestModel requestModel = createCustomClassGraphRequestModel();
        requestModel.setSubclassOfSourceShape(SPACE);
        GraphRequestValidator.validateRequest(requestModel);
    }
    
    /**
     * Tests error handling for an undefined subclassOf edge target shape (class graph)
     * @throws IllegalArgumentException if successful
     */
    @Test(expected = IllegalArgumentException.class)
    public void testErroneousSubclassOfTargetShape() throws IllegalArgumentException {
        GraphRequestModel requestModel = createCustomClassGraphRequestModel();
        requestModel.setSubclassOfTargetShape(SPACE);
        GraphRequestValidator.validateRequest(requestModel);
    }
    
    /**
     * Tests error handling for an incorrect subclassOf line color (class graph)
     * @throws IllegalArgumentException if successful
     */
    @Test(expected = IllegalArgumentException.class)
    public void testErroneousSubclassOfLineColor() throws IllegalArgumentException {
        GraphRequestModel requestModel = createCustomClassGraphRequestModel();
        requestModel.setSubclassOfLineColor(SPACE);
        GraphRequestValidator.validateRequest(requestModel);
    }
    
    /**
	 * Tests error handling for an incorrect type node border color (individual graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousTypeBorderColor() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomIndividualGraphRequestModel();
	    requestModel.setClassBorderColor(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an undefined type node border type (individual graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousTypeBorderType() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomIndividualGraphRequestModel();
	    requestModel.setClassBorderType(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an incorrect type node fill color (individual graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousTypeFillColor() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomIndividualGraphRequestModel();
	    requestModel.setClassFillColor(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an undefined type node shape (individual graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousTypeNodeShape() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomIndividualGraphRequestModel();
	    requestModel.setClassNodeShape(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an incorrect type node text color (individual graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousTypeTextColor() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomIndividualGraphRequestModel();
	    requestModel.setClassTextColor(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an incorrect typeOf line color (individual graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousTypeOfLineColor() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomIndividualGraphRequestModel();
	    requestModel.setTypeOfLineColor(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Tests error handling for an undefined typeOf edge type (individual graph)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testErroneousTypeOfLineType() throws IllegalArgumentException {
	    GraphRequestModel requestModel = createCustomIndividualGraphRequestModel();
	    requestModel.setTypeOfLineType(SPACE);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
     * Tests error handling for an undefined typeOf edge source arrow (individual graph)
     * @throws IllegalArgumentException if successful
     */
    @Test(expected = IllegalArgumentException.class)
    public void testErroneousTypeOfSourceShape() throws IllegalArgumentException {
        GraphRequestModel requestModel = createCustomIndividualGraphRequestModel();
        requestModel.setTypeOfSourceShape(SPACE);
        GraphRequestValidator.validateRequest(requestModel);
    }
    
    /**
     * Tests error handling for an undefined typeOf edge target arrow (individual graph)
     * @throws IllegalArgumentException if successful
     */
    @Test(expected = IllegalArgumentException.class)
    public void testErroneousTypeOfTargetShape() throws IllegalArgumentException {
        GraphRequestModel requestModel = createCustomIndividualGraphRequestModel();
        requestModel.setTypeOfTargetShape(SPACE);
        GraphRequestValidator.validateRequest(requestModel);
    }
    
    /**
	 * Tests error handling for a VOWL graph request with collapsed edges (not supported)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testVowlCollapsed() throws IllegalArgumentException {
	    GraphRequestModel requestModel = new GraphRequestModel("test", "reasoningFalse", "test", 
	    		"test file contents", "vowl", "class", false);
	    requestModel.setCollapseEdges("collapseTrue");
	    GraphRequestValidator.validateRequest(requestModel);
	}
    
    /**
	 * Tests error handling for a VOWL graph request of individual graph (not supported)
	 * @throws IllegalArgumentException if successful
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testVowlIndividual() throws IllegalArgumentException {
	    GraphRequestModel requestModel = new GraphRequestModel("test", "reasoningFalse", "test", 
	    		"test file contents", "vowl", "individual", false);
	    GraphRequestValidator.validateRequest(requestModel);
	}

	/**
	 * Creates a custom class GraphRequestModel with acceptable input
	 * @return GraphRequestModel
	 */
	private GraphRequestModel createCustomClassGraphRequestModel() {
	    GraphRequestModel requestModel = new GraphRequestModel("test", "reasoningFalse", "test.rdf", 
	    		"test file contents", "custom", "class", false);
	    
	    // Set acceptable custom input
	    requestModel.setClassNodeShape(NONE);
	    requestModel.setClassBorderType(SOLID);
	    requestModel.setSubclassOfSourceShape(NONE);
	    requestModel.setSubclassOfTargetShape(NONE);
	    requestModel.setSubclassOfLineType(SOLID);
	    requestModel.setClassFillColor(WHITE);
	    requestModel.setClassTextColor(BLACK);
	    requestModel.setClassBorderColor(BLACK);
	    requestModel.setSubclassOfLineColor(BLACK);
	    
	    return requestModel;
	}

	/**
	 * Creates a custom class GraphRequestModel with different ontology errors such as an invalid
	 * file suffix or invalid contents
	 * 
	 * @param  fileName
	 * @param  ontContents
	 * @return GraphRequestModel
	 */
	private GraphRequestModel createCustomGraphRequestModelWithDiffOntologyErrors(final String fileName, 
			final String ontContents) {
	    GraphRequestModel requestModel = new GraphRequestModel("test", "reasoningFalse", fileName, ontContents, 
	    		"custom", "class", false);
	    
	    // Set acceptable custom input
	    requestModel.setClassNodeShape(NONE);
	    requestModel.setClassBorderType(SOLID);
	    requestModel.setSubclassOfSourceShape(NONE);
	    requestModel.setSubclassOfTargetShape(NONE);
	    requestModel.setSubclassOfLineType(SOLID);
	    requestModel.setClassFillColor(WHITE);
	    requestModel.setClassTextColor(BLACK);
	    requestModel.setClassBorderColor(BLACK);
	    requestModel.setSubclassOfLineColor(BLACK);
	    
	    return requestModel;
	}

	/**
	 * Creates a custom individual GraphRequestModel (which should currently fail since "individual" 
	 * is not an allowed graph type)
	 *    
	 * @return GraphRequestModel
	 */
	private GraphRequestModel createCustomIndividualGraphRequestModel() {
	    GraphRequestModel requestModel = new GraphRequestModel("test", "reasoningFalse", "test.rdf", 
	    		"test file contents", "custom", "individual", false);
	    
	    // Set acceptable custom input
	    requestModel.setIndividualNodeShape(NONE);
	    requestModel.setClassNodeShape(NONE);
	    requestModel.setDataNodeShape(NONE);
	    requestModel.setIndividualBorderType(SOLID);
	    requestModel.setClassBorderType(SOLID);
	    requestModel.setDataBorderType(SOLID);
	    requestModel.setTypeOfSourceShape(NONE);
	    requestModel.setTypeOfTargetShape(NONE);
	    requestModel.setTypeOfLineType(SOLID);
	    requestModel.setDataPropSourceShape(NONE);
	    requestModel.setDataPropTargetShape(NONE);
	    requestModel.setDataPropEdgeType(SOLID);  
	    requestModel.setObjPropSourceShape(NONE);
	    requestModel.setObjPropTargetShape(NONE);
	    requestModel.setObjPropEdgeType(SOLID);  
	    requestModel.setIndividualFillColor(WHITE);
	    requestModel.setClassFillColor(WHITE);
	    requestModel.setDataFillColor(WHITE);
	    requestModel.setIndividualTextColor(BLACK);
	    requestModel.setClassTextColor(BLACK);
	    requestModel.setDataTextColor(BLACK);
	    requestModel.setIndividualBorderColor(BLACK);
	    requestModel.setClassBorderColor(BLACK);
	    requestModel.setDataBorderColor(BLACK);
	    requestModel.setTypeOfLineColor(BLACK);
	    requestModel.setDataPropEdgeColor(BLACK);
	    requestModel.setObjPropEdgeColor(BLACK);
	    
	    return requestModel;
	}

	/**
	 * Creates a custom property GraphRequestModel with acceptable input
	 * 
	 * @return GraphRequestModel
	 */
	private GraphRequestModel createCustomPropertyGraphRequestModel() {
	    GraphRequestModel requestModel = new GraphRequestModel("test", "reasoningFalse", "test", 
	    		"test file contents", "custom", "property", false);
	    
	    // Set acceptable custom input
	    requestModel.setCollapseEdges("false");
	    requestModel.setDataNodeShape(NONE);
	    requestModel.setObjNodeShape(NONE);
	    requestModel.setAnnPropSourceShape(NONE);
	    requestModel.setAnnPropTargetShape(NONE);
	    requestModel.setAnnPropEdgeType(SOLID);
	    requestModel.setDataPropSourceShape(NONE);
	    requestModel.setDataPropTargetShape(NONE);
	    requestModel.setDataPropEdgeType(SOLID);
	    requestModel.setObjPropSourceShape(NONE);
	    requestModel.setObjPropTargetShape(NONE);
	    requestModel.setObjPropEdgeType(SOLID);
	    requestModel.setDataFillColor(WHITE);
	    requestModel.setObjFillColor(WHITE);
	    requestModel.setDataTextColor(BLACK);
	    requestModel.setObjTextColor(BLACK);
	    requestModel.setDataBorderColor(BLACK);
	    requestModel.setObjBorderColor(BLACK);
	    requestModel.setAnnPropEdgeColor(BLACK);
	    requestModel.setDataPropEdgeColor(BLACK);
	    requestModel.setObjPropEdgeColor(BLACK);
	    
	    return requestModel;
	}
}
