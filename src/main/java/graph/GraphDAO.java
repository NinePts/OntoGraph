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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.openrdf.model.Namespace;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import com.complexible.common.rdf.model.Namespaces;
import com.complexible.common.rdf.model.Values;
import com.complexible.stardog.StardogException;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.api.admin.AdminConnection;
import com.complexible.stardog.api.admin.AdminConnectionConfiguration;
import com.complexible.stardog.ext.spring.DataSource;
import com.complexible.stardog.ext.spring.RowMapper;
import com.complexible.stardog.ext.spring.SnarlTemplate;
import com.complexible.stardog.ext.spring.mapper.SimpleRowMapper;
import com.complexible.stardog.ext.spring.mapper.SingleMapper;

import graph.models.ClassModel;
import graph.models.EdgeFlagsModel;
import graph.models.IndividualModel;
import graph.models.PrefixModel;
import graph.models.PropertyModel;
import graph.models.RelatedAndRestrictionModel;
import graph.models.RestrictionModel;
import graph.models.TypeAndValueModel;
import graph.models.UMLClassModel;

/** 
 * GraphDAO acts as a data access object to interact with the
 * Stardog server. It contains a variety of methods that query Stardog 
 * to return data to build a graph from a user's OWL ontology.
 *
 */
@Repository
@Component
public class GraphDAO {
	
    // Stardog configuration and temp file location from application.properties
    @Value("${stardog.server}")
    private String stardogServer;
    @Value("${stardog.user}")
    private String stardogUser;
    @Value("${stardog.password}")
    private String stardogPassword;
    @Value("${graph.tempDir}")
    private String tempDir;
    
    private AdminConnection adminConnection = null;

    // Frequently used strings
    private static final String CLASS = "class";
    private static final String LABEL = "label";
    private static final String MEMBER = "member";
    private static final String OWL_NOTHING_FULL_URI = "http://www.w3.org/2002/07/owl#Nothing";
    private static final String OWL_RESTRICTION_FULL_URI = "http://www.w3.org/2002/07/owl#Restriction";
    private static final String OWL_THING = "owl:Thing";
    private static final String OWL_THING_FULL_URI = "http://www.w3.org/2002/07/owl#Thing";
    private static final String RDFS_CLASS_FULL_URI = "http://www.w3.org/2000/01/rdf-schema#Class";
    private static final String RDFS_DATATYPE_FULL_URI = "http://www.w3.org/2000/01/rdf-schema#Datatype";

    // Partial query strings
    private static final String LIST_QUERY = "?list rdf:rest*/rdf:first ?member }";
    private static final String OPTIONAL_LABEL = "OPTIONAL { ?name rdfs:label ?label } }";
    private static final String SELECT_DISTINCT_NAME = "SELECT DISTINCT ?name WHERE { ";
    
    // Query strings
    private static final String CHECK_DB_LOAD = "SELECT (count(?s) as ?count) WHERE { ?s ?p ?o }";
    private static final String GET_ANNOTATION_PROPERTIES = "SELECT DISTINCT * WHERE { "
            + "?name a owl:AnnotationProperty . " + OPTIONAL_LABEL;
	private static final String GET_ASYMMETRIC_PROPERTIES = SELECT_DISTINCT_NAME + "?name a rdf:AsymmetricProperty }";
    private static final String GET_CLASSES = "SELECT DISTINCT * WHERE { ?name a owl:Class . " + OPTIONAL_LABEL;
    private static final String GET_COMPLEMENTS = "SELECT DISTINCT * WHERE { ?node owl:complementOf ?class }";
	private static final String GET_DATATYPE_PROPERTIES = "SELECT DISTINCT * WHERE { ?attr a owl:DatatypeProperty . "
	        + "OPTIONAL { ?attr rdfs:label ?label } }";
	private static final String GET_DATATYPE_PROPERTIES_FOR_THING = "SELECT DISTINCT ?attr WHERE { "
			+ "{ ?attr a owl:DatatypeProperty . MINUS { ?attr a owl:DatatypeProperty . ?attr rdfs:domain ?name } } "
			+ "UNION { ?attr a owl:DatatypeProperty . ?attr rdfs:domain owl:Thing } }";
	private static final String GET_DATATYPE_PROPERTIES_WITH_DOMAINS = "SELECT DISTINCT ?attr WHERE { "
	        + "?attr a owl:DatatypeProperty . ?attr rdfs:domain ?name }";
	private static final String GET_DATATYPE_PROPERTY_RANGES = "SELECT DISTINCT ?range WHERE { "
	        + "?name rdfs:range ?range . ?name a owl:DatatypeProperty }";
	private static final String GET_DISJOINTS = "SELECT DISTINCT * WHERE { ?class owl:disjointWith ?disClass }";
    private static final String GET_EQUIVALENTS = "SELECT DISTINCT ?class ?eqClass WHERE { "
            + "?class owl:equivalentClass ?eqClass . MINUS { ?eqClass owl:oneOf ?indiv } }";
    private static final String GET_EXTERNALLY_DEFINED_CLASSES = "SELECT DISTINCT ?name ?label WHERE { " 
    		+ "{ { ?someClass rdfs:subClassOf ?name } UNION { ?someClass owl:equivalentClass ?name } "
    		+ "UNION { ?someClass owl:disjointWith ?name } } . "
    		+ "FILTER isIRI(?name) . MINUS { ?name a owl:Class } . " + OPTIONAL_LABEL;
    private static final String GET_FUNCTIONAL_PROPERTIES = SELECT_DISTINCT_NAME + "?name a owl:FunctionalProperty }";
	private static final String GET_INDIVIDUAL_PROPERTIES = "SELECT ?prop ?val WHERE { "
	+ "?name ?prop ?val . FILTER ( !strstarts(str(?prop), 'http://www.w3.org/2002/07/owl') "
	+ "&& !strstarts(str(?prop), 'http://www.w3.org/2006/12/owl2') "
	+ "&& !strstarts(str(?prop), 'http://www.w3.org/ns/owl2') "
	+ "&& !strstarts(str(?prop), 'http://www.w3.org/1999/02/22-rdf-syntax-ns') "
	+ "&& !strstarts(str(?prop), 'http://www.w3.org/2000/01/rdf-schema') ) }";
	private static final String GET_INDIVIDUAL_TYPES = "SELECT ?type ?label WHERE "
	+ "{ ?name a ?type . FILTER ( !strstarts(str(?type), 'http://www.w3.org/2002/07/owl') "
	+ "&& !strstarts(str(?type), 'http://www.w3.org/2006/12/owl2') "
	+ "&& !strstarts(str(?type), 'http://www.w3.org/ns/owl2') "
	+ "&& !strstarts(str(?type), 'http://www.w3.org/1999/02/22-rdf-syntax-ns') "
	+ "&& !strstarts(str(?type), 'http://www.w3.org/2000/01/rdf-schema') ) . " 
	+ "OPTIONAL { ?type rdfs:label ?label } }";
	private static final String GET_INDIVIDUALS = "SELECT ?name ?label WHERE "
			+ "{ ?name a ?type . FILTER ( !strstarts(str(?type), 'http://www.w3.org/2002/07/owl') "
	        // The following two namespaces are for OWL 2 - the first is the temporary working group 
			//    namespace, which may still be used. The second is the recommended namespace. 
	        + "&& !strstarts(str(?type), 'http://www.w3.org/2006/12/owl2') "
	        + "&& !strstarts(str(?type), 'http://www.w3.org/ns/owl2') "
			+ "&& !strstarts(str(?type), 'http://www.w3.org/1999/02/22-rdf-syntax-ns') "
			+ "&& !strstarts(str(?type), 'http://www.w3.org/2000/01/rdf-schema') ) . " + OPTIONAL_LABEL; 
	private static final String GET_INDIVIDUALS_WITH_REASONING = "SELECT ?name ?label WHERE "
			+ "{ ?name a ?type . " + OPTIONAL_LABEL;  
	private static final String GET_INTERSECTIONS = "SELECT DISTINCT ?node ?member WHERE { "
	        + "?node owl:intersectionOf ?list . " + LIST_QUERY;
	private static final String GET_INVERSE_FUNCTIONAL_PROPERTIES = SELECT_DISTINCT_NAME + "?name a owl:InverseFunctionalProperty }";
	private static final String GET_IRREFLEXIVE_PROPERTIES = SELECT_DISTINCT_NAME + "?name a owl:IrreflexiveProperty }";
	private static final String GET_OBJECT_PROPERTIES = "SELECT DISTINCT * WHERE { ?name a owl:ObjectProperty . " 
    		+ OPTIONAL_LABEL;
	private static final String GET_OBJECT_PROPERTY_RANGES = "SELECT DISTINCT ?range ?label WHERE { "
	        + "?name rdfs:range ?range . ?name a owl:ObjectProperty . OPTIONAL { ?range rdfs:label ?label } }";
	private static final String GET_ONEOFS = "SELECT DISTINCT ?node ?member WHERE { "
	        + "?node owl:equivalentClass ?eqClass . ?eqClass owl:oneOf ?list . " + LIST_QUERY;
	private static final String GET_ONTOLOGY_URI = "SELECT ?uri WHERE { ?uri a owl:Ontology }";
	private static final String GET_PROPERTIES_MULTIPLE_DOMAINS = "SELECT DISTINCT ?name WHERE { ?name rdfs:domain ?domain } "
	+ "GROUP BY ?name HAVING ( COUNT(?domain) > 1 )";
	private static final String GET_PROPERTIES_MULTIPLE_RANGES = "SELECT DISTINCT ?name WHERE { ?name rdfs:range ?range } "
	+ "GROUP BY ?name HAVING ( COUNT(?range) > 1 )";
	private static final String GET_PROPERTY_DOMAINS = "SELECT DISTINCT ?domain ?label WHERE { "
	        + "?name rdfs:domain ?domain . OPTIONAL { ?domain rdfs:label ?label } }";
	private static final String GET_REFLEXIVE_PROPERTIES = SELECT_DISTINCT_NAME + "?name a owl:ReflexiveProperty }";
	private static final String GET_RESTRICTION_DETAILS = "SELECT DISTINCT * WHERE { "
			+ "{ { ?name a owl:Restriction } UNION { ?name a rdfs:Datatype } } . ?name ?p ?o }";
	private static final String GET_STANDALONE_BLANK_NODE_ONEOFS = "SELECT DISTINCT ?name ?node ?member "
	+ "WHERE { ?node owl:oneOf ?list . MINUS { ?class owl:equivalentClass ?node } . "
	+ "BIND (?node AS ?name) . " + LIST_QUERY;
	private static final String GET_STANDALONE_BLANK_NODES = "SELECT DISTINCT ?name ?node ?member WHERE { "
	+ "?node owl:oneOf ?list . MINUS { ?class owl:equivalentClass ?node } . "
	+ "BIND (?node AS ?name) } ";
	private static final String GET_SYMMETRIC_PROPERTIES = SELECT_DISTINCT_NAME + "?name a owl:SymmetricProperty }";
	private static final String GET_SUPERCLASSES = "SELECT DISTINCT ?class WHERE { ?name rdfs:subClassOf ?class }";
    private static final String GET_TRANSITIVE_PROPERTIES = SELECT_DISTINCT_NAME + "?name a owl:TransitiveProperty }";
	private static final String GET_UNIONS = "SELECT DISTINCT ?node ?member WHERE { ?node owl:unionOf ?list . "
            + LIST_QUERY;
	private static final String GET_WITHRESTRICTIONS = "SELECT DISTINCT ?p ?o WHERE { "
			+ "?list rdf:rest*/rdf:first ?member . ?member ?p ?o . FILTER (regex(str(?list), ?bnode)) }";

	// RowMapper for SPARQL results for individual properties
	private final RowMapper<List<org.openrdf.model.Value>> indivPropertyMapper = 
			bs -> Arrays.asList(bs.getValue("prop"), bs.getValue("val"));
			
    /**
     * Check if Stardog loaded the ontology - if there are no triples, then drop the db
     * 
     * @param  snarlTemplate SnarlTemplate with server details
     * @param  databaseName graph title (name of data store)
     * @throws OntoGraphException 
     * 
     */
    public void checkDBLoad(final SnarlTemplate snarlTemplate, String databaseName) 
    		throws OntoGraphException {
    	
    	checkAdminConnection();
        
        if (Integer.parseInt(snarlTemplate.queryForObject(CHECK_DB_LOAD, new SingleMapper("count"))) == 0) {
        	adminConnection.drop(databaseName);
    		throw new OntoGraphException("No triples were loaded to the database. Please validate that the input file "
    				+ "is a supported format and has no errors. Then, try again.");  
        }
    }

	/**
	 * Removes a database from Stardog, but first checks that the db exists
	 * 
	 * @param  databaseName String 
	 * 
	 */    
	public void dropDatabase(String databaseName) {

    	checkAdminConnection();
    	
    	Collection<String> dbs = adminConnection.list();
    	if (dbs.contains(databaseName)) {
    		adminConnection.drop(databaseName);
    	} 
	}

	/**
	 * Get all classes and their superclasses in the loaded ontology
	 * 
	 * @param  reasoningType String ("reasoningTrue" or "reasoningFalse")
	 * @param  snarlTemplate SnarlTemplate with server details
	 * @param  reasoningTemplate SnarlTemplate with server details, and reasoning enabled
	 * @param  prefixes List of prefixes (PrefixModels)
	 * @return List<ClassModel> of classes with details (ClassModel)
	 * @throws OntoGraphException
	 * 
	 */
	public List<ClassModel> getClasses(final String reasoningType, final SnarlTemplate snarlTemplate, 
			final SnarlTemplate reasoningTemplate, List<PrefixModel> prefixes) throws OntoGraphException {

    	checkAdminConnection();
    	
	    List<ClassModel> models = new ArrayList<>();
	    List<Map<String, String>> classInfo;
	    if (reasoningType.contains("True")) {
	    	classInfo = reasoningTemplate.query(GET_CLASSES, new SimpleRowMapper());
	    } else {
	    	classInfo = snarlTemplate.query(GET_CLASSES, new SimpleRowMapper());
	    }
       
        for (Map<String, String> classDetails : classInfo) {
        	String fullClassName = classDetails.get("name");
        	if (!OWL_THING_FULL_URI.equals(fullClassName) 
        			&& !OWL_NOTHING_FULL_URI.equals(fullClassName) 
        			&& !RDFS_CLASS_FULL_URI.equals(fullClassName) 
        			&& fullClassName.contains(":")) {  // This clause removes blank nodes
        		// Clean up the className to turn it into a prefix ":" name format
        		String className = processURIName(prefixes, fullClassName);
	            // Add class and superclasses to the model
	            models.add(ClassModel.builder()
	            		.className(className)
	            		.classLabel(getLabel(className, classDetails.get(LABEL)))
	            		.fullClassName(fullClassName)
	                    .superClasses(processURIList(prefixes, 
	            				getSuperClasses(snarlTemplate, prefixes, fullClassName)))
	                    .build());
	        }
	    }
	    
	    return models;
	}

	/**
     * Get all classes and properties for UML output
     * 
     * @param  snarlTemplate SnarlTemplate with server details
     * @param  prefixes List of PrefixModels
     * @param  classes List<ClassModel> defining the classes in the ontology (without the additional
     *             details needed for a UML diagram)
     * @return List<UMLClassModel> of classes with details for UML (UMLClassModel)
     * @throws OntoGraphException
     * 
     */
    public List<UMLClassModel> getClassesForUML(final SnarlTemplate snarlTemplate, 
    		List<PrefixModel> prefixes, List<ClassModel> classes) throws OntoGraphException {

        checkAdminConnection();
        
        List<UMLClassModel> models = new ArrayList<>();
        // Query for any attributes where domain is not specified, and therefore automatically Thing,
        //    or where the domain is actually set to owl:Thing
        List<String> thingAttributes = snarlTemplate.query(GET_DATATYPE_PROPERTIES_FOR_THING, 
        		new SingleMapper("attr"));
        
        boolean addedOwlThing = false;
        for (ClassModel classInfo : classes) {
        	String fullClassName = classInfo.getFullClassName();
            List<String> attributes = new ArrayList<>();  
            if (OWL_THING_FULL_URI.equals(fullClassName)) {
            	addedOwlThing = true;
            	attributes.addAll(thingAttributes);
            } else {
            	if (fullClassName.contains(":")) {  // Skips blank nodes
	            	// Query for attributes where the class is the domain of a datatype property
	            	attributes = snarlTemplate.query(GET_DATATYPE_PROPERTIES_WITH_DOMAINS, 
	            			createMap("name", Values.iri(fullClassName)), 
	            			new SingleMapper("attr"));
            	}
            }
            
        	// Add to model
        	models.add(UMLClassModel.builder()
        		  .className(classInfo.getClassName())
        		  .classLabel(classInfo.getClassLabel())
        		  .fullClassName(fullClassName)
                  .superClasses(classInfo.getSuperClasses())
                  .attributes(processURIList(prefixes, attributes))
                  .build());
        }
        
        // Did we already add owl:Thing, and if not, do we need to?
        if (!addedOwlThing && !thingAttributes.isEmpty()) {
            // Only add owl:Thing if necessary
        	classes.add(ClassModel.builder()
                    .className(OWL_THING)
                    .classLabel(OWL_THING)
                    .fullClassName(OWL_THING_FULL_URI)
                    .superClasses(new ArrayList<>())
                    .build());
            models.add(UMLClassModel.builder()
                    .className(OWL_THING)
                    .classLabel(OWL_THING)
                    .fullClassName(OWL_THING_FULL_URI)
                    .superClasses(new ArrayList<>())
                    .attributes(processURIList(prefixes, thingAttributes))
                    .build());
        }
        	
        return models;
    }
    
    /**
     * Get all equivalent and disjoint classes in the loaded ontology, also get
     * all enumerations/oneOfs
     * 
     * @param  snarlTemplate SnarlTemplate with server details
     * @param  prefixes List of PrefixModels
     * @param  relatedsAndRestrictions RelatedAndRestrictionModel containing lists of models 
     *             of "related" classes (equivalent, disjoints, and oneOfs), of connectives
     *             (unions, intersections and complementOfs) and restrictions (allValuesFrom,
     *             someValuesFrom, min/maxInclusive, ...)
     *             
     */
    public void getClassRelationships(final SnarlTemplate snarlTemplate, 
    		List<PrefixModel> prefixes, RelatedAndRestrictionModel relatedsAndRestrictions) {

    	checkAdminConnection();
    	
    	// Get the related classes from the model (may be empty maps)
    	Map<String, List<TypeAndValueModel>> equivalentsDisjointsOneOfs = 
    			relatedsAndRestrictions.getEquivalentsDisjointsOneOfs();
    	Map<String, List<TypeAndValueModel>> connectives = relatedsAndRestrictions.getConnectives();
    	
        // Get all classes which are defined with 1+ equivalentClasses
        List<Map<String, String>> equivalentList = snarlTemplate.query(GET_EQUIVALENTS, new SimpleRowMapper());
        
        // Get all classes which are defined with 1+ disjointWith classes
        List<Map<String, String>> disjointList = snarlTemplate.query(GET_DISJOINTS, new SimpleRowMapper());
        
        // Get all complementOfs, intersectionOfs, oneOfs and unionOfs
        List<Map<String, String>> complementOfList = snarlTemplate.query(GET_COMPLEMENTS, new SimpleRowMapper());
        List<Map<String, String>> intersectionOfList = snarlTemplate.query(GET_INTERSECTIONS, new SimpleRowMapper());
        List<Map<String, String>> oneOfList = snarlTemplate.query(GET_ONEOFS, new SimpleRowMapper());
        oneOfList.addAll(snarlTemplate.query(GET_STANDALONE_BLANK_NODE_ONEOFS, new SimpleRowMapper()));
        List<Map<String, String>> unionOfList = snarlTemplate.query(GET_UNIONS, new SimpleRowMapper());
        
        // Get the details for the equivalences
        for (Map<String, String> equiv : equivalentList) {
        	// Class might be equivalent to another class, or might be equivalent to a blank
        	//   node that is then a complementOf, intersectionOf, oneOf or unionOf other classes
        	// Blank nodes could reference other blank nodes, arbitrarily deep
        	updateRelationMap(prefixes, "eq", equivalentsDisjointsOneOfs, equiv.get(CLASS), equiv.get("eqClass"));
        }
        
        // Get the details for the disjoints
        for (Map<String, String> dis : disjointList) {
        	// Class might be disjoint to another class, or might be disjoint to a blank node
        	updateRelationMap(prefixes, "dis", equivalentsDisjointsOneOfs, dis.get(CLASS), dis.get("disClass"));
        }
        
        // An ontology might define the equivalencies or disjoints for two classes in "both" directions 
        //    (i.e., class1 disjointWith class2, and class2 disjointWith class1). Remove this duplicated logic.
        Map<String, List<TypeAndValueModel>> newMap = removeDupsFromRelationMap(equivalentsDisjointsOneOfs);
        equivalentsDisjointsOneOfs.clear();
        equivalentsDisjointsOneOfs.putAll(newMap);
        
        // Get the details for the complementOfs, intersectionOfs, oneOfs, and unionOfs...
        for (Map<String, String> comp : complementOfList) {
        	updateRelationMap(prefixes, "comp", connectives, comp.get("node"), comp.get(CLASS));
        }
        for (Map<String, String> inter : intersectionOfList) {
        	updateRelationMap(prefixes, "inter", connectives, inter.get("node"),  inter.get(MEMBER));
        }
        for (Map<String, String> one : oneOfList) {
        	updateRelationMap(prefixes, "one", equivalentsDisjointsOneOfs, one.get("node"),  one.get(MEMBER));
        }
        for (Map<String, String> union : unionOfList) {
        	updateRelationMap(prefixes, "un", connectives, union.get("node"),  union.get(MEMBER));
        }
    }

	/**
	 * Get all classes that are referenced as superclasses or equivalentClasses but that are defined
	 * external to the ontology.
	 * 
	 * @param  snarlTemplate SnarlTemplate with server details
	 * @param  prefixes List of prefixes (PrefixModels)
	 * @return List<ClassModel> of classes with details (ClassModel)
	 * 
	 */
	public List<ClassModel> getExternallyDefinedClasses(final SnarlTemplate snarlTemplate, 
			List<PrefixModel> prefixes) {

    	checkAdminConnection();
    	
	    List<ClassModel> models = new ArrayList<>();
        List<Map<String, String>> classInfo = snarlTemplate.query(GET_EXTERNALLY_DEFINED_CLASSES,
                new SimpleRowMapper());
        
        for (Map<String, String> classDetails : classInfo) {
        	String fullClassName = classDetails.get("name");
        	if (!OWL_THING_FULL_URI.equals(fullClassName) && !RDFS_CLASS_FULL_URI.equals(fullClassName)) {
        		// Clean up the className to turn it into a prefix ":" name format
        		String className = processURIName(prefixes, fullClassName);
	            // Add the class to the model
	            models.add(ClassModel.builder()
	            		.className(className)
	            		.classLabel(getLabel(className, classDetails.get(LABEL)))
	            		.fullClassName(fullClassName)
	                    .superClasses(new ArrayList<>())
	                    .build());
        	}
        }
        
        return models;
	}

	/**
     * Get all individual, their types and property values from the loaded ontology
     * 
	 * @param  reasoningType String ("reasoningTrue" or "reasoningFalse")
     * @param  snarlTemplate SnarlTemplate with server details
	 * @param  reasoningTemplate SnarlTemplate with server details, and reasoning enabled
     * @param  prefixes List of PrefixModels
     * @return List<IndividualModel> of individuals with details (IndividualModel)
     * @throws OntoGraphException
     * 
     */
    public List<IndividualModel> getIndividuals(final String reasoningType, final SnarlTemplate snarlTemplate, 
    		final SnarlTemplate reasoningTemplate, List<PrefixModel> prefixes) throws OntoGraphException {

    	checkAdminConnection();
    	
        List<IndividualModel> models = new ArrayList<>();
        
        Set<String> definedIndividuals = new HashSet<>();
        Set<String> referencedIndividuals = new HashSet<>();
        
        // Find all individuals and their types
        List<Map<String, String>> individualInfo;
	    if (reasoningType.contains("True")) {
	    	individualInfo = reasoningTemplate.query(GET_INDIVIDUALS_WITH_REASONING, new SimpleRowMapper());
	    } else {
	    	individualInfo = snarlTemplate.query(GET_INDIVIDUALS, new SimpleRowMapper());
	    }
	    
        for (Map<String, String> indiv : individualInfo) {
        	String fullIndivName = indiv.get("name");
        	String indivName = processURIName(prefixes, fullIndivName);
        	definedIndividuals.add(indivName);
        	
        	// A list of the individual's types using a format, "label (type name with prefix)"
        	List<String> types = new ArrayList<>();
        	
        	// Get the type names and their labels
            List<Map<String, String>> typeList;
    	    if (reasoningType.contains("True")) {
    	    	typeList = reasoningTemplate.query(GET_INDIVIDUAL_TYPES, 
            		createMap("name", Values.iri(fullIndivName)), new SimpleRowMapper());
    	    } else {
    	    	typeList = snarlTemplate.query(GET_INDIVIDUAL_TYPES, 
                		createMap("name", Values.iri(fullIndivName)), new SimpleRowMapper());
    	    }	
            		
            if (!typeList.isEmpty()) {
            	for (Map<String, String> typeDetails : typeList) {
            		String typeName = processURIName(prefixes, typeDetails.get("type"));
            		types.add(getLabel(typeName, typeDetails.get(LABEL)));
            	}
            }
            
            // Get the individual's properties
            List<TypeAndValueModel> datatypeProperties = new ArrayList<>();
            List<TypeAndValueModel> objectProperties = new ArrayList<>();
            createIndividualPropertyLists(snarlTemplate, prefixes, fullIndivName,
            		datatypeProperties, objectProperties);
            
            // Add individuals referenced in the domains and ranges of the properties
            for (TypeAndValueModel objPropDetails : objectProperties) {
                referencedIndividuals.add(objPropDetails.getValue());
            }
            
            // Add individual names, types and properties to models
            models.add(IndividualModel.builder()
                                    .individualName(indivName)
                                    .individualLabel(getLabel(indivName, indiv.get(LABEL)))
                                    .fullIndividualName(fullIndivName)
                                    .typeLabels(types)
                                    .datatypeProperties(datatypeProperties)
                                    .objectProperties(objectProperties)
                                    .build());
        }
        
        // Add individuals that are referenced as domain or object property ranges but do not have an rdf:type
        models.addAll(getUntypedIndividuals(snarlTemplate, referencedIndividuals, definedIndividuals, prefixes));
        return models;
    }
    
    /** 
     * Get the URI for the owl:Ontology
     * 
     * @param  snarlTemplate SnarlTemplate with server details
     * @return String holding the URI of the ontology
     * 
     */
    public String getOntologyURI(SnarlTemplate snarlTemplate) {

    	checkAdminConnection();
    	return snarlTemplate.queryForObject(GET_ONTOLOGY_URI, new SingleMapper("uri"));
    }
    
    /**
     * Get all prefixes and their corresponding URLs in the loaded ontology
     * 
     * @param  conn Stardog Connection
     * @return List<PrefixModel> of prefixes with details (PrefixModel)
     * 
     */
    public List<PrefixModel> getPrefixes(SnarlTemplate snarlTemplate) {

    	checkAdminConnection();
    	
        List<PrefixModel> models = new ArrayList<>();
        
        // Get the namespaces
        Namespaces ns = snarlTemplate.getDataSource().getConnection().namespaces();
        for (Namespace name : ns) {
            String url = name.getName();
            String prefixName = name.getPrefix();
            // Remove default stardog prefixes
            if (!prefixName.contains("stardog") && !prefixName.isEmpty()) {
                models.add(PrefixModel.builder()
                                       .prefixName(prefixName)
                                       .url(url)
                                       .build());
           }
        }
        
        return models;
    }
    
    /**
     * List of all properties and their domains/ranges in the loaded ontology
     * 
     * @param  snarlTemplate SnarlTemplate with server details
     * @param  prefixes List of PrefixModels>
     * @return List<PropertyModel> of properties with details (PropertyModel)
     * @throws OntoGraphException
     * 
     */
    public List<PropertyModel> getProperties(final SnarlTemplate snarlTemplate,   //NOSONAR - Acknowledging complexity
    		List<PrefixModel> prefixes) throws OntoGraphException {

    	checkAdminConnection();
    	
    	// Get all the properties
        List<PropertyModel> models = getAllProperties(snarlTemplate, prefixes);
        
        // Determine which object and datatype properties are functional/inverseFunctional,
        //    as well as transitive/symmetric/asymmetric/reflexive/irreflexive
        List<String> functionalNames = snarlTemplate.query(GET_FUNCTIONAL_PROPERTIES, new SingleMapper("name"));
        List<String> inverseFunctionalNames = snarlTemplate.query(GET_INVERSE_FUNCTIONAL_PROPERTIES,
                new SingleMapper("name"));
        List<String> transitiveNames = snarlTemplate.query(GET_TRANSITIVE_PROPERTIES, new SingleMapper("name"));
        List<String> symmetricNames = snarlTemplate.query(GET_SYMMETRIC_PROPERTIES, new SingleMapper("name"));
        List<String> asymmetricNames = snarlTemplate.query(GET_ASYMMETRIC_PROPERTIES, new SingleMapper("name"));
        List<String> reflexiveNames = snarlTemplate.query(GET_REFLEXIVE_PROPERTIES, new SingleMapper("name"));
        List<String> irreflexiveNames = snarlTemplate.query(GET_IRREFLEXIVE_PROPERTIES, new SingleMapper("name"));
        
        // Also determine if there are multiple domains or ranges specified, which have implications for
        //    reasoning the types of individuals (individuals are typed as intersections of the multiple
        //    domains or ranges)
        List<String> multipleDomainsNames = snarlTemplate.query(GET_PROPERTIES_MULTIPLE_DOMAINS, new SingleMapper("name"));
        List<String> multipleRangesNames = snarlTemplate.query(GET_PROPERTIES_MULTIPLE_RANGES, new SingleMapper("name"));
        
        // Get domains and ranges for all properties, and add the info to the PropertyModels
        // If no domain/range is defined, then it is automatically owl:Thing for all domains and the
        //   ranges of object properties, and rdfs:Literal for the ranges of datatype and annotation properties
        for (PropertyModel pm : models) {
            
        	String propName = pm.getFullPropertyName();
        	String propType = pm.getPropertyType();
            Map<String, Object> queryInput = createMap("name", Values.iri(propName));
            
	        List<Map<String, String>> domainList = snarlTemplate.query(GET_PROPERTY_DOMAINS, queryInput, 
	        		new SimpleRowMapper());
	        List<String> domains = processDomainOrRangeQueryResults(prefixes, domainList, "domain");

        	List<String> ranges;
	        if ("o".equals(propType)) {
	        	List<Map<String, String>> rangeList = snarlTemplate.query(GET_OBJECT_PROPERTY_RANGES, queryInput, 
	        			new SimpleRowMapper());
	        	ranges = processDomainOrRangeQueryResults(prefixes, rangeList, "range");
	        } else {
	        	// Datatype or annotation property
	        	ranges = snarlTemplate.query(GET_DATATYPE_PROPERTY_RANGES, queryInput, new SingleMapper("range"));
	        	if (!ranges.isEmpty()) {
	        		ranges = processURIList(prefixes, ranges);
	        	} else {
	        		ranges = new ArrayList<>(Arrays.asList("rdfs:Literal"));
	        	}
	        }
            
	        String fullName = pm.getFullPropertyName();
	        pm.setEdgeFlags(EdgeFlagsModel.builder()	//NOSONAR - Acknowledging use of conditional operators
	        		.asymmetric(asymmetricNames.contains(fullName) ? true : false)
	        		.functional(functionalNames.contains(fullName) ? true : false)
	        		.inverseFunctional(inverseFunctionalNames.contains(fullName) ? true : false)
	        		.irreflexive(irreflexiveNames.contains(fullName) ? true : false)
	        		.reflexive(reflexiveNames.contains(fullName) ? true : false)
	        		.symmetric(symmetricNames.contains(fullName) ? true : false)
	        		.transitive(transitiveNames.contains(fullName) ? true : false)
	        		.multipleDomains(multipleDomainsNames.contains(fullName) ? true : false)
	        		.multipleRanges(multipleRangesNames.contains(fullName) ? true : false)
	        		.build());
            pm.setDomains(domains);
            pm.setRanges(ranges);
        }
        
        return models;
    }

	/**
	 * Get the details for any owl:Restrictions or rdfs:Datatypes
	 * 
	 * @param  snarlTemplate SnarlTemplate with server details
	 * @return prefixes List<PrefixModel> 
	 * @throws OntoGraphException
	 * 
	 */
	public List<RestrictionModel> getRestrictions(final SnarlTemplate snarlTemplate, 
			List<PrefixModel> prefixes) throws OntoGraphException {

    	checkAdminConnection();
    	
	    List<RestrictionModel> models = new ArrayList<>();
        List<Map<String, String>> restrictionInfo = snarlTemplate.query(GET_RESTRICTION_DETAILS, 
        		new SimpleRowMapper());
        // Create map where the key is the restriction id/name and the value is a list of each of the 
        //    predicate-object pairs for that restriction
        Map<String, List<String>> restrictions = new HashMap<>();
        // Separately track which restriction ids/names are class and which are data range restrictions
        Set<String> classRestrictions = new HashSet<>();
        
        for (Map<String, String> resDetails : restrictionInfo) {  //NOSONAR - 2 continue statements in loop
        	String restrictionId = resDetails.get("name");
        	String obj = resDetails.get("o");
        	if (OWL_RESTRICTION_FULL_URI.equals(obj)) {
        		classRestrictions.add(restrictionId);
        		continue;		
        	} 
        	if (RDFS_DATATYPE_FULL_URI.equals(obj)) {  // Don't care about the rdfs:Datatype declaration
        		continue;		
        	}
			String details = processURIName(prefixes, resDetails.get("p")) + " " + processURIName(prefixes, obj);
			if (details.startsWith("owl:withRestrictions")) {
				// Need to get the blank nodes that are the "restrictions"
		        List<Map<String, String>> memberInfo = snarlTemplate.query(GET_WITHRESTRICTIONS, 
		        		createMap("bnode", obj), new SimpleRowMapper());
		        for (Map<String, String> memberDetails : memberInfo) { 
		        	details =  processURIName(prefixes, memberDetails.get("p")) + " " + memberDetails.get("o");
		        	updateMapValue(restrictions, restrictionId, details);
		        }
			} else {
				// Just add the "details" to the restrictions hashmap
				updateMapValue(restrictions, restrictionId, details);
			}
        }
        
        // Save all the restrictions and datatype restrictions
        for (Entry<String, List<String>> entry : restrictions.entrySet()) {
        	String restrictionName = entry.getKey();
			models.add(RestrictionModel.builder()
				.restrictionName(restrictionName)
				.classRestriction(classRestrictions.contains(restrictionName) ? true : false)
				.restrictionDetails(entry.getValue())
				.build());
        }
        
        return models;
	}
    
    /**
	 * Get all blank nodes that are defined as oneOf enumerations, but are NOT defined as 
	 * an equivalenClass.
	 * 
	 * @param  snarlTemplate SnarlTemplate with server details
	 * @return List<ClassModel> of classes with details (ClassModel)
	 * 
	 */
	public List<ClassModel> getStandaloneBlankNodes(final SnarlTemplate snarlTemplate) {
	
		checkAdminConnection();
		
	    List<ClassModel> models = new ArrayList<>();
	    List<Map<String, String>> classInfo = snarlTemplate.query(GET_STANDALONE_BLANK_NODES,
	            new SimpleRowMapper());
	    
	    for (Map<String, String> classDetails : classInfo) {
	    	String fullClassName = classDetails.get("name");
	        // Add the class to the model
	        models.add(ClassModel.builder()
	            		.className(fullClassName)
	            		.classLabel("OneOf Definition")
	            		.fullClassName(fullClassName)
	                    .superClasses(new ArrayList<>())
	                    .build());
	    }
	    
	    return models;
	}
	
	/**
	 * Loads input file with given file format as named graph to Stardog database.
	 * 
	 * @param  snarlTemplate (returned, associated with the data source for the db)
	 * @param  reasoningTemplate (returned, associated with the data source for the db)
	 * @param  inputFile Full ontology as a byte array
	 * @param  graphTitle Title for database name
	 * @param  fileFormat File extension
	 * @throws OntoGraphException due to duplicate graph title (which indicates that the db already exists),
	 *                  or an IO error in creating the input file for loading to Stardog
	 * 
	 */   
	public void loadFileToDB(SnarlTemplate snarlTemplate, SnarlTemplate reasoningTemplate, byte[] fileData, 
	        String graphTitle, String fileFormat) throws OntoGraphException {

    	checkAdminConnection();
    	
	    // Get the list of existing databases and if the requested name is in use, then error
	    // TODO Check for and remove orphaned databases (usually caused in debug by terminating a run)
	    if (adminConnection.list().contains(graphTitle)) {
	        throw new OntoGraphException("Requested graph title (" + graphTitle + ") is already in use. Please choose "
	                + "another title.");  
	    }
		
	    String path = tempDir + graphTitle + "." + fileFormat;
	    
	    // Write input (byte[]) to file object
	    File ontolDefn = new File(path);
	    try {
			FileUtils.writeByteArrayToFile(ontolDefn, fileData);
		} catch (IOException e) {  //NOSONAR - Logged as part of OntoGraphException handling
	        ontolDefn.delete();  //NOSONAR - No need to use boolean returned 
	        throw new OntoGraphException("Failed to create db for the ontology file for the graph, " + graphTitle
	        		+ ". IO Exception details: " + e.getMessage());  
		} 
	    
	    ConnectionConfiguration connConfig = adminConnection.newDatabase(graphTitle).create(ontolDefn.toPath());
	    snarlTemplate.setDataSource(new DataSource(connConfig));
	    
	    ConnectionConfiguration reasoningConfig = ConnectionConfiguration.to(graphTitle).reasoning(true)
				.server(stardogServer)
				.credentials(stardogUser, stardogPassword);
	    reasoningTemplate.setDataSource(new DataSource(reasoningConfig));
	    
	    // Delete file (clean-up)
	    ontolDefn.delete();	//NOSONAR - No need to use boolean returned 
	}
	
	/**
	 * Gets all datatype, object or annotation properties and their details.
	 * 
	 * @param  snarlTemplate
	 * @param  prefixes List<PrefixModel> which may be updated if the property uses a new URI prefix
	 * @param  query String defining the specific query 
	 * @param  models List<PropertyModel> which is modified to add the requested properties
	 * @param  propertyType String holding a "d" for datatype properties, "o" for object properties or
	 *                an "a" for annotation properties
	 * 
	 */
	private void addPropertyModels(SnarlTemplate snarlTemplate, List<PrefixModel> prefixes, 
			final String query, List<PropertyModel> models, final String propertyType) {

        List<Map<String, String>> propertyInfo = snarlTemplate.query(query, new SimpleRowMapper());
        for (Map<String, String> prop : propertyInfo) {
        	String fullPropName;
        	if ("d".equals(propertyType)) {
        		fullPropName = prop.get("attr");
        	} else {
        		fullPropName = prop.get("name");
        	}
        	String propName = processURIName(prefixes, fullPropName);
        	models.add(PropertyModel.builder()
                .propertyName(propName)
        		.propertyLabel(getLabel(propName, prop.get(LABEL)))
                .fullPropertyName(fullPropName)
                .propertyType(propertyType)
                .build());
        }
	}
	
	/**
	 * Validates that the admin connection to Stardog is (still) in place.
	 * The connection may be lost if Stardog is restarted. 
	 * 
	 */
	private void checkAdminConnection() {
		
		try {
			if (adminConnection != null && adminConnection.isOpen()) {
				return;
			}
			if (adminConnection != null && !adminConnection.isOpen()) {
				adminConnection.close();
			}
			adminConnection = AdminConnectionConfiguration
					.toServer(stardogServer)
					.credentials(stardogUser, stardogPassword)
					.connect();
			
		} catch (StardogException e) {
			if (adminConnection != null) {
				adminConnection.close();
			}
			throw e;
		}
	}
				

	/**
     * Creates/updates the maps containing the datatype and object properties and 
     * their values for an individual.
     * 
     * @param  snarlTemplate SnarlTemplate connection details
     * @param  fullIndivName String full IRI
     * @param  prefixes List of PrefixModels
     * @param  datatypeProperties List<TypeAndValueModel> where each entry's "type" is the property name
     *                and the "value" is the property value. This list is empty at the start of the 
     *                method and holds the datatype properties on return.
     * @param  objectProperties List<TypeAndValueModel> where each entry's "type" is property name
     *                and the "value" is the property value. This list is empty at the start of the 
     *                method and holds the object properties on return.
     * @throws OntoGraphException
     */
    private void createIndividualPropertyLists(SnarlTemplate snarlTemplate, 
    		List<PrefixModel> prefixes, final String fullIndivName, 
    		List<TypeAndValueModel> datatypeProperties, List<TypeAndValueModel> objectProperties) 
    				throws OntoGraphException {
    	
        List<List<org.openrdf.model.Value>> propList = snarlTemplate.query(GET_INDIVIDUAL_PROPERTIES, 
        		createMap("name", Values.iri(fullIndivName)), indivPropertyMapper);
       
        // Go through the list of properties and values, and separate the object properties
        //    from the datatype properties
        for (List<org.openrdf.model.Value> propVal : propList) {
        	String value = propVal.get(1).toString();
        	if (value.contains("http://www.w3.org/2001/XMLSchema")) {
        		String newValue = value.substring(0, value.indexOf('<')) + "xsd:" 
        				+ value.substring(value.lastIndexOf('#') + 1, value.indexOf('>'));
        		datatypeProperties.add(TypeAndValueModel.createTypeAndValueModel(
        				processURIName(prefixes, propVal.get(0).toString()), newValue));
        	} else {
        		objectProperties.add(TypeAndValueModel.createTypeAndValueModel(
        				processURIName(prefixes, propVal.get(0).toString()), processURIName(prefixes, value)));
        	}
        }
    }

    /**
	 * Creates a hash map from String/Object pairs.
	 * 
	 * @param values Pairs of String key and Object values. The number
	 *        of arguments should be even otherwise an exception will be
	 *        thrown.
	 * @return Map created from the inputs.
	 * @throws OntoGraphException 
	 * 
	 */
	private Map<String, Object> createMap(final Object... values) throws OntoGraphException { 
		
	    final Map<String, Object> map = new HashMap<>();
	    for (int i = 0; values != null && i < values.length; i += 2) {
	        String key = Objects.toString(values[i], "");
	        
	        if (key.isEmpty() || i + 1 >= values.length) {
	            throw new OntoGraphException("Bad/missing input for map key, " + key 
	            		+ ", creating query input hash map. " + "Input was: " + Arrays.toString(values)); 
	        }
	        map.put(key, values[i+1]);
	    }
	    
	    return map;
	}

	/**
	 * Queries the database to get all annotation, datatype and object properties for an ontology.
	 * 
	 * @param snarlTemplate SnarlTemplate with server details
	 * @param prefixes List<PrefixModel> defining all known prefixes
	 * @return List<PropertyModel> returning all the property details
	 * 
	 */
	private List<PropertyModel> getAllProperties(SnarlTemplate snarlTemplate, 
			List<PrefixModel> prefixes) {
        
		List<PropertyModel> models = new ArrayList<>();
		
        addPropertyModels(snarlTemplate, prefixes, GET_DATATYPE_PROPERTIES, models, "d");
        addPropertyModels(snarlTemplate, prefixes, GET_OBJECT_PROPERTIES, models, "o");
        addPropertyModels(snarlTemplate, prefixes, GET_ANNOTATION_PROPERTIES, models, "a");
		
        return models;
	}

	/**
	 * Gets the label for a class or property.
	 * 
	 * @param  name String defining the class or property
	 * @param  label String indicating the current label, which may be null
	 * @return newLabel String
	 * 
	 */
	private String getLabel(final String name, final String label) {
		
		String newLabel = name;
		if (label != null && !label.isEmpty()) {
			newLabel = label + " (" + name + ")";
		} 
		
		return newLabel;
	}

	/**
	 * Gets all the superclasses for a class.
	 * 
	 * @param  snarlTemplate SnarlTemplate
	 * @param  prefixes List<String> defining the current prefixes
	 * @param  fullClassName String whose superclasses are needed
	 * @return List<String> holding the superclasses
	 * @throws OntoGraphException
	 * 
	 */
    private List<String> getSuperClasses(final SnarlTemplate snarlTemplate,
    		List<PrefixModel> prefixes, final String fullClassName) throws OntoGraphException {
    	
        // Set up parameterized query
        List<String> superClassList = snarlTemplate.query(GET_SUPERCLASSES, 
        		createMap("name", Values.iri(fullClassName)), 
                new SingleMapper(CLASS));
       
		if (superClassList.isEmpty()) {
			return new ArrayList<>();
		} else {
			// Change to displaying prefixes for the superclass names
			return processURIList(prefixes, superClassList);
		}
    }
    
    /**
     * Get object and datatype property details for individuals that are defined in the
     * ontology, but do not have an rdf:type
     * 
     * @param  snarlTemplate SnarlTemplate connection details
     * @param  referencedIndividuals Set of individuals referenced as object property values
     * @param  definedIndividuals Set of individuals that are defined with rdf:type
     * @param  prefixes List of PrefixModels
     * @return List of IndividualModels
     * @throws OntoGraphException
     * 
     */
    private List<IndividualModel> getUntypedIndividuals(final SnarlTemplate snarlTemplate, 
            Set<String> referencedIndividuals, Set<String> definedIndividuals, List<PrefixModel> prefixes) 
                    throws OntoGraphException {
        List<IndividualModel> models = new ArrayList<>();
        
        // Find individuals that are defined, but do not have an rdf:type
        for (String individual : referencedIndividuals) {
            
            // Skip any defined individuals
            if (definedIndividuals.contains(individual)) {
                continue;
            }
            
            // Plug full namespace back into the prefixed name to use as IRI
            String indName = individual.substring(individual.indexOf(':') + 1);
            String prefix = individual.substring(0, individual.indexOf(':'));
            String fullIndivName = "";
            for (PrefixModel pm : prefixes) {
                if (prefix.equals(pm.getPrefixName())) {
                    fullIndivName = pm.getUrl() + indName;
                    break;
                }
            }
            
            // Get property details and create model
            List<TypeAndValueModel> datatypeProperties = new ArrayList<>();
            List<TypeAndValueModel> objectProperties = new ArrayList<>();
            createIndividualPropertyLists(snarlTemplate, prefixes, fullIndivName, datatypeProperties,
                    objectProperties);
           
            models.add(IndividualModel.builder()
                    .individualName(individual)
                    .individualLabel(individual)
                    .fullIndividualName(fullIndivName)
                    .typeLabels(Arrays.asList(""))
                    .datatypeProperties(datatypeProperties)
                    .objectProperties(objectProperties)
                    .build());
        }
        
        return models;
    }

    /** 
     * Processes the results of a query for the domain or range classes of a property.
     * If there are no results, then the array is set to the value, "owl:Thing".
     * 
     * @param  prefixes List of PrefixModels>
     * @param  queryResults List<Map<String, String>> 
     * @param  variableName String
     * @return List<String> A list of strings of the form, "label (domain or range class name)"
     * 
     */
    private List<String> processDomainOrRangeQueryResults(List<PrefixModel> prefixes, 
    		List<Map<String, String>> queryResults, final String variableName) {
    	
    	List<String> domainOrRangeList = new ArrayList<>();
        for (Map<String, String> qr : queryResults) {
        	String name = qr.get(variableName);
        	if (OWL_THING_FULL_URI.equals(name)) {
        		// Have to worry about the ontology adding its own definition of owl:Thing (such as FOAF)
        		domainOrRangeList.add(OWL_THING);
        	} else {
        		name = processURIName(prefixes, name);
        		String label = getLabel(name, qr.get(LABEL));
        		domainOrRangeList.add(label);
        	}
        }
        
        if (domainOrRangeList.isEmpty()) {
        	domainOrRangeList = new ArrayList<>(Arrays.asList(OWL_THING));
        }
    	
        return domainOrRangeList;
    }
    
    /**
	 * Executes the processUriName over all the URIs in a list
	 * 
	 * @param prefixes List of PrefixModels for namespaces 
	 * @param fullNames List<String> of URIs to format
	 * @return Formatted names as List<String>
	 * 
	 */    
	private List<String> processURIList(List<PrefixModel> prefixes, List<String> fullNames) {
		
	    List<String> names = new ArrayList<>();
	    for (String name : fullNames) {
	        // Skip if NamedIndividual 
	        if (name.contains("NamedIndividual")) {
	            break;
	        }
	        names.add(processURIName(prefixes, name));
	    }
	    
	    return names;
	}

	/**
	 * Cleans up URIs to use prefixes
	 * 
	 * @param prefixes List<PrefixModel>, all prefixes in ontology with corresponding URI/IRIs 
	 * @param fullName holding the full URI/IRI string
	 * @return String name formatted with a prefix
	 * 
	 */
	private String processURIName(List<PrefixModel> prefixes, String fullName) {

	    String name = fullName;
	    
	    // Don't process if this is a blank node (only process if the name contains a ":")
		if (fullName.contains(":")) {
		    String prefix = null;
		    for (PrefixModel pm : prefixes) {
		        if (fullName.contains(pm.getUrl())) {
		            prefix = pm.getPrefixName();
		            break;
		        }
		    }
	
		    if (prefix != null && (fullName.startsWith("http") || fullName.startsWith("urn"))) {
		    	// Use the prefix in the name
		    	// Name URIs either use "#" or "/", so need to account for both approaches
		    	if (fullName.contains("#")) {
		    		name = prefix + ":" + fullName.substring(fullName.lastIndexOf('#') + 1);
		    	} else {
		    		name = prefix + ":" + fullName.substring(fullName.lastIndexOf('/') + 1);
		    	}
		    }
		
		} 
		
		return name;
	}
	
	/**
	 * Removes duplicates from the equivalentsDisjointsOneOfs relation map. Duplicates occur when
	 * an ontology defines equivalencies or disjoints in both directions for a set of classes (i.e., class1 
	 * disjointWith class2, and class2 disjointWith class1). 
	 * 
	 * @param  holdingMap Map<String, List<TypeAndValueModel>> holding the original set of equivalencies, 
	 *            disjoints and oneOfs.  The key of the map is a class name and the List<TypeAndValueModel> 
	 *            values are a list of models where the relationship type is the first entry, and the
     *            referenced (equivalent, disjoint, oneOf) entity is the second. The relationship types 
     *            are "eq" for equivalentClass, "dis" for disjointWith or "one" for oneOf.
	 * @return revised Map<String, List<TypeAndValueModel>> where duplicates are removed
	 * 
	 */
	private static Map<String, List<TypeAndValueModel>> removeDupsFromRelationMap(
			Map<String, List<TypeAndValueModel>> holdingMap) {
		
		Map<String, List<TypeAndValueModel>> newMap = new HashMap<>();
		// Array of strings of the form, "relation type" + "!$" + "class name 1" + "!$"
		//    + "class name 2"
		List<String> allMapDetails = new ArrayList<>();
		// Array similar to above, but only one occurrence of the combination of relation type
		//    and the class names
		List<String> reducedMapDetails = new ArrayList<>();
		
		// Get all the relation map details
		for (Entry<String, List<TypeAndValueModel>> mapEntry : holdingMap.entrySet()) {
			String key = mapEntry.getKey();
			List<TypeAndValueModel> valueList = mapEntry.getValue();
			for (TypeAndValueModel value : valueList) {
				allMapDetails.add(value.getType() + "!$" + key + "!$" + value.getValue());  
			}
		}

		// Get only the unique combinations of relation type, class1 and class2
		for (String r12 : allMapDetails) {
			String type = r12.substring(0, r12.indexOf("!$"));
			String cl1 = r12.substring(r12.indexOf("!$") + 2, r12.lastIndexOf("!$"));
			String cl2 = r12.substring(r12.lastIndexOf("!$") + 2);
			if (!reducedMapDetails.contains(type + "!$" + cl1 + "!$" + cl2) 
					&& !reducedMapDetails.contains(type + "!$" + cl2 + "!$" + cl1)) {
				reducedMapDetails.add(type + "!$" + cl1 + "!$" + cl2);
			}
		}
		
		// Turn the unique combinations into a new relation map
		for (String relCl1Cl2 : reducedMapDetails) {
			String relType = relCl1Cl2.substring(0, relCl1Cl2.indexOf("!$"));
			String class1 = relCl1Cl2.substring(relCl1Cl2.indexOf("!$") + 2, relCl1Cl2.lastIndexOf("!$"));
			String class2 = relCl1Cl2.substring(relCl1Cl2.lastIndexOf("!$") + 2);
			updateMapValue(newMap, class1, TypeAndValueModel.createTypeAndValueModel(relType, class2));
		}
		
		return newMap;
	}

	/**
	 * Updates the hash map of equivalentsDisjointsOneOfs or connectives
	 * 
	 * @param prefixes List<PrefixModel> which may be updated if the key or value uses a new URI prefix
	 * @param relationType String which distinguishes the different types of relationships (equivalentClass,
	 *              disjointWith, oneOf or the propositional connectives - complementOf, intersectionOf 
	 *              or unionOf)
	 * @param holdingMap HashMap<String, List<TypeAndValueModel>> to be updated
	 * @param key String identifying the entry's key 
	 * @param value String identifying the entry's value
	 * 
	 */
	private void updateRelationMap(List<PrefixModel> prefixes, final String relationType,
			Map<String, List<TypeAndValueModel>> holdingMap, final String key, final String value) {
		
		String newKey = processURIName(prefixes, key);
		updateMapValue(holdingMap, newKey, TypeAndValueModel.createTypeAndValueModel(relationType, 
				processURIName(prefixes, value)));
	}

	/**
	 * Either creates a new entry in the hash map if the key does not exist, or adds
	 * the value to the existing value.
	 * 
	 * @param holdingMap HashMap<String, List<TypeAndValueModel>> to be updated
	 * @param key String identifying the entry's key 
	 * @param value String identifying the entry's value
	 * 
	 */
	private static <T> void updateMapValue(Map<String, List<T>> hashmap, final String key, final T value) {

		if (hashmap.containsKey(key)) {
			List<T> currDetails = new ArrayList<>(hashmap.get(key));
			currDetails.add(value);
			hashmap.put(key, currDetails);
		} else {
			hashmap.put(key, Arrays.asList(value));
		}
	}
}
