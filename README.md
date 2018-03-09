OntoGraph, Current Version 1.1.0
======
Licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0). 

[Try OntoGraph](#try-ontograph)<br>
[Overview](#overview)<br>
[Setup](#setup)<br>
[Using the GUI](#graphical-interface)<br>
[Tips for Using yEd](#using-yed)<br>
[Using the REST API](#rest-interface)<br>
[Modifying the Code](#code-details)<br>
[Testing and Testcases](#testing-and-testcases)<br>
[Issues](#known-issues)
<br><br>

### Try OntoGraph
You can download OntoGraph's source from this page (and customize it for your environment, following the instructions below), or just download the executable jar (and Stardog) and run it yourself. All of the "how-to" details are below. Alternately, you can access OntoGraph directly from your browser (either Firefox or Chrome) using [our server](http://45.76.26.91:8181). The choice is yours!
<br><br>

### Overview
OntoGraph is a Spring Boot application for graphing OWL ontologies. It lets you go from (see the complete FOAF ontology at [http://xmlns.com/foaf/spec/index.rdf](http://xmlns.com/foaf/spec/index.rdf) ...

    ...
    <!-- FOAF classes (types) are listed first. -->
    
    <rdfs:Class rdf:about="http://xmlns.com/foaf/0.1/Person" rdfs:label="Person" rdfs:comment="A person." vs:term_status="stable">
      <rdf:type rdf:resource="http://www.w3.org/2002/07/owl#Class" />
      <owl:equivalentClass rdf:resource="http://schema.org/Person" />
      <owl:equivalentClass rdf:resource="http://www.w3.org/2000/10/swap/pim/contact#Person" />
      <!--    <rdfs:subClassOf><owl:Class rdf:about="http://xmlns.com/wordnet/1.6/Person"/></rdfs:subClassOf> -->
      <rdfs:subClassOf><owl:Class rdf:about="http://xmlns.com/foaf/0.1/Agent"/></rdfs:subClassOf>
      <!--    <rdfs:subClassOf><owl:Class rdf:about="http://xmlns.com/wordnet/1.6/Agent"/></rdfs:subClassOf> -->
      <rdfs:subClassOf><owl:Class rdf:about="http://www.w3.org/2003/01/geo/wgs84_pos#SpatialThing" rdfs:label="Spatial Thing"/></rdfs:subClassOf> 
      <rdfs:isDefinedBy rdf:resource="http://xmlns.com/foaf/0.1/"/>
      <owl:disjointWith rdf:resource="http://xmlns.com/foaf/0.1/Organization"/>
      <owl:disjointWith rdf:resource="http://xmlns.com/foaf/0.1/Project"/>
    </rdfs:Class>
    ...

To ...

![alt text](https://github.com/NinePts/OntoGraph/blob/master/foaf-graffoo-bothClassAndProperty.png "FOAF GraphML Output for Graffoo Visualization, Both Class And Property Graph Type")

The code consists of a Java-based REST API that creates various GraphML outputs of a user-provided OWL ontology file (or zip file of a set of ontology files) that can be input programmatically or using a simple GUI. The program stores the ontologies in a triple store ([Stardog](http://www.stardog.com/)), then runs a series of queries to return the necessary information (classes, properties, individuals...) to be diagrammed. For more information on OntoGraph and why it was created, click 'Download' for the paper, [Ontology Development by Domain Experts (Without Using the "O" Word)](https://github.com/NinePts/OntoGraph/blob/master/OntologyDevelopmentByDomainExperts.pdf). The paper has been accepted for publication in the next issue of the *Journal of Applied Ontology* from IOS Press.

Four visualizations of ontology data can be generated:

  * Custom format (defined to fit existing business or personal preferences)
  * [Graffoo](http://www.essepuntato.it/graffoo/)
  * [UML](http://www.uml-diagrams.org/class-reference.html)
  * [VOWL](http://vowl.visualdataweb.org/v2/#notation)
  
And, information can be segmented to display:

  * Class-related information (subclassing, equivalent and disjoint classes, class restrictions, ...)
  * Individual instances, their types, and their datatype and object property information
  * Property information (datatype and object properties, functional/symmetric/... properties, domain and range definitions, ...)
  * Both class and property information
  
After the GraphML outputs are created, a graphical editor (we recommend [yEd](http://www.yworks.com/products/yed)) is used to perform layout functions.
<br><br>

### Setup
Please note that OntoGraph uses Java 8 and Stardog 5.x. These must be installed in order for OntoGraph to execute.

To check your Java version, go to the command line in Linux, OSX or Windows, and type `java -version`. If your version is not "build 1.8.x", please update it. To update Java, go to the [Oracle Java Download page](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html), and follow the directions there. (For Windows, the default download is the 32-bit version. Please make sure to get the 64bit version of Java in order to have all the necessary files.)

To download Stardog, go to [Stardog Knowledge Graph](http://stardog.com/) and click the 'DOWNLOAD' button. Then, complete the form with your information, select either the Stardog community or enterprise trial version, and click download. (Note that the community edition of Stardog is free and can be used with OntoGraph. The enterprise trial version can also be used, but the license expires 30 days from download.) After receiving an email with your download details (which is sent very quickly after clicking 'download'), get the Stardog zip file, move it to your desired directory and unpack it. Details on installing Stardog for Linux, OSX or Windows can be found in the [Stardog Quick Start Guide](http://www.stardog.com/docs/#_quick_start_guide). Note that when the Stardog file is unzipped, the resulting files and directories are placed in a `stardog-<major.minor.release>` sub-directory.

OntoGraph can be run directly from its jar file, which is downloaded (along with a few other files) from the `OntoGraph-<major.minor.release>.zip` file in the master GitHub directory shown above. After downloading the OntoGraph-<version>.zip, move it to your desired directory and unzip it. (When unzipped, the resulting files and directories are placed in an `ontograph-<major.minor.release>` sub-directory.) Then, via the command line, move (`cd`) into the newly created sub-directory and execute `java -jar OntoGraph-<major.minor.release>.jar` (e.g., `java -jar OntoGraqph-1.0.0.jar`). Please note that Stardog MUST have been started (`./stardog-admin server start` or on Windows, `stardog-admin.bat server start`) before requesting any graph output from OntoGraph.

After starting Ontograph, access its GUI from your web browser at the URI, `http://localhost:8181`. Using the GUI, you can specify the ontology file to be diagrammed, a graph title, the visualization (custom, Graffoo, UML-like or VOWL) and the type of graph (class, property or individual). The output file will be available from a dialog box allowing you to open or save the file (it is saved in your 'Downloads' directory). For more details about the GUI interface, see the [Graphical Inteface](#graphical-interface) section below.

As opposed to executing the jar file, OntoGraph's complete source can be downloaded from [Nine Points Solutions' GitHub repository](https://github.com/NinePts/OntoGraph/). OntoGraph is built using [Gradle](http://gradle.org). It can be run as a Java application or in debug (bootrun) mode from the (for example) Gradle Tasks view of Eclipse. 

OntoGraph is built with the current release of Gradle (V4.2), but has also been tested with Gradle versions V3.5 and higher. You can also navigate to the (downloaded) OntoGraph directory and enter `./gradlew clean build` on the command line to download Gradle v4.2 manually and perform the build.
<br><br>

### Graphical interface
The following sub-sections describe the different inputs of the OntoGraph GUI.

##### Ontology file
This allows you to upload the file(s) to be diagrammed. The file formats supported by OntoGraph and Stardog are: NTRIPLES, RDF/XML, TURTLE, PRETTY_TURTLE, TRIG, TRIX, N3 and NQUADS. (For ontologies created in Protege, we recommend you save it in the RDF/XML serialization.) In addition, a zip file of related ontologies can be uploaded. 

Please note that OntoGraph will NOT import/download referenced ontologies. But, OntoGraph will correctly execute without the imported files - allowing very specific graphs to be created, without adding all the details of its imports. However, if imported ontologies should be graphed, these must be included with your ontology files in a zip file. 

##### Graph title
The 'Graph Title' input is displayed in a "Graph Information" box in the resulting GraphML output. It is also used as the returned GraphML file name (ending with the .graphml suffix). Because the graph title is used to generate a file name, we recommend that you do not use any special characters, though dashes and spaces are acceptable. Note that spaces will be removed in the file name. 

##### Visualization and graph type
The 'Visualization' option determines the format of the output. (Click on the links below to see the documentation and specifications for these formats.) Currently, the following formats are supported:

  * Custom
  * [Graffoo](http://www.essepuntato.it/graffoo/)
  * [VOWL](http://vowl.visualdataweb.org/v2/#notation)
  * [UML-like](http://www.uml-diagrams.org/class-reference.html) 

Finally, you have the option to choose from one of four graph types:

  * Class information
  * Individuals, types and their properties
  * Annotation, datatype and object property information
  * Both class and object and datatype property information

##### Generate the graph
After specifying the information above, click the 'GENERATE' button. If you selected a visualization of Graffoo or VOWL with a 'property' graph type, you will be prompted to select True/False to collapse property edges (for all visualizations but VOWL). Collapsing the edges reduces multiple property lines with the same domain and range to a single line. (This option is not available for VOWL graphs, as that is not allowed per the specification.)

If you have selected a 'custom' visualization, you will be prompted to define the specific shapes, colors and line types for the graph type. The default 'custom' visualization options that appear in the GUI can be changed in the file, `src/main/resources/static/js/GraphRequestModel.js`. More information about this is provided in the section, [Customizing Output](#customizing-output).

##### Graph layout
OntoGraph produces a ‘.graphml’ output which we recommend viewing and editing with the [yEd Graph Editor](http://www.yworks.com/products/yed). By default, there is no graph layout and all of the diagrammed entities are stacked on top of each other. This can be quickly and easily fixed by opening the graph in yEd, selecting 'Layout' from the menu bar and choosing any default layout. For optimal viewing, we recommend the following layouts:

  * Class: Circular
  * Property: Circular or Tree -> Balloon
  * Individual: Circular
  * UML: Orthogonal -> UML or Compact
<br><br>

### Using yEd
If you are a first-time user of yEd, then you might not know where or how to start making further layout changes to a diagram, after you have done the initial "Layout" task. Here are some tips:

  * If a node is too wide, since its text is too long ...  
    * You can resize the node by clicking on it and then dragging the corner edge. If you do this, you can make the node box larger or smaller, and/or thinner or wider.  
    * To edit the text inside the node box, click on the text and look to the right-hand side of the window. You should see a "Properties View" at the bottom, right of the window. A small portion of the text is displayed in the table under the "General" heading, to the right of the word, "Text". Click on the text and its ellipsis ("..."). A pop-up window will appear where you can shorten the text, add line feeds, etc. When finished modifying the text, click "OK" to close the pop-up window.
  * If there are too many lines running between two nodes ...  
    * This can be addressed by modifying and combining the text from mutliple lines, and then deleting extraneous lines.  
    * Or, when the GraphML is originally generated, choose to "Collapse Edges - "True".
  * If the labels of different edges are overlapping and hard to distinguish ...  
    * To change the location of a label on an edge, select the edge and look to the right-hand side of the window. You should see a "Properties View" at the bottom, right of the window. Under the "Label" heading, to the right of the word, "Placement", select the value (it is "Centered" by default). A pop-up window is shown. Click the drop-down for "Model" and select "SmartFree" to be able to place the text anywhere, or "Center Slider" to move the text anywhere along the edge.  
    * Alternately, you can bend the edge by clicking anywhere on the edge line and dragging. Or, change where an edge connects to a node by selecting the edge, and then moving the end point for the node.
  * If the title/graph information should be edited ...  
    * Under the "Structure" view in the lower, left hand side of the yEd window, expand the "Graph" tree and select "Graph Information". That will highlight where the title information is located in the diagram. Go to this element and select the sub-element, "Title:  ...", and then edit it as described for the node text above.
  * How do I correct the positioning of the prefixes and their full URIs ...  
    * Under the "Structure" view in the lower, left hand side of the yEd window, expand the "Graph" tree and select "Prefixes". That will highlight where the prefix information is located in the diagram. Select the sub-element that lists the prefixes, this will highlight the prefix list as a small rectangle with squares located along the border and the text surrounding it. Position your cursor inside the small rectangle and drag it to the left-hand side of the prefix box. Then, select the containing prefix box and resize it as described for resizing a node, above.
<br><br>

### REST interface
After the OntoGraph application has been started (along with Stardog), it is also possible to access it via a RESTful interface. The OntoGraph REST API accepts a POST request with a JSON payload consisting of the properties in the GraphRequestModel. The request is addressed to the URI, `http://<OntoGraph_address>:8181/graph`. For example, on OntoGraph's local machine, the address is `htt://localhost:8181/graph`. A JSON response is returned. The following inputs are required and validated for the various visualizations and graph types:
 
  * All: graphTitle, inputFile, fileData, visualization, graphType
  
  * Custom visualization, Class graph type:  
    * Class node characteristics => classNodeShape, classFillColor, classTextColor, classBorderColor, classBorderType  
    * SubclassOf edge characteristics => subclassOfSourceShapre, subclassOfTargetShape, subclassOfLineColor, subclassOfLineType  
    
  * Custom visualization, Individual graph type:  
     * Class/type node characteristics => classNodeShape, classFillColor, classTextColor, classBorderColor, classBorderType  
     * Datatype node (values such as "13"^^xsd:integer) characteristics => dataNodeShape, dataFillColor, dataTextColor, dataBorderColor, dataBorderType  
    * Individual node characteristics => individualNodeShape, individualFillColor, individualTextColor, individualBorderColor, individualBorderType  
    * TypeOf edge characteristics => typeOfSourceShape, typeOfTargetShape, typeOfLineColor, typeOfLineType  
    * Datatype property edge characteristics => dataPropSourceShape, dataPropTargetShape, dataPropEdgeColor, dataPropEdgeType  
    * Object property edge characteristics => objPropSourceShape, objPropTargetShape, objPropEdgeColor, objPropEdgeType  
    
  * Custom visualization, Property graph type:  
    * Collapse edges => collapseTrue or collapseFalse  
    * Class node characteristics (classes defined as domains or object property ranges) => objNodeShape, dataFillColor, dataTextColor, dataBorderColor, dataBorderType  
    * Datatype node characteristics (datatypes defined as datatype property ranges, such as rdfs:Literal) => dataNodeShape, dataFillColor, objTextColor, objBorderColor, objBorderType  
    * Annotation property edge characteristics => annPropSourceShape, annPropTargetShape, annPropEdgeColor, annPropEdgeType  
    * Datatype property edge characteristics => dataPropSourceShape, dataPropTargetShape, dataPropEdgeColor, dataPropEdgeType  
    * Object property edge characteristics => objPropSourceShape, objPropTargetShape, objPropEdgeColor, objPropEdgeType  
    
  * Custom visualization, Both class and property graph type:
    * Collapse edges => collapseEdges
    * Class node characteristics => objNodeShape, objFillColor, objTextColor, objBorderColor, objBorderType  
    * Datatype node characteristics => dataNodeShape, dataFillColor, dataTextColor, dataBorderColor, dataBorderType  
    * SubclassOf edge characteristics => subclassOfSourceShapre, subclassOfTargetShape, subclassOfLineColor, subclassOfLineType  
    * Annotation property edge characteristics => annPropSourceShape, annPropTargetShape, annPropEdgeColor, annPropEdgeType  
    * Datatype property edge characteristics => dataPropSourceShape, dataPropTargetShape, dataPropEdgeColor, dataPropEdgeType  
    * Object property edge characteristics => objPropSourceShape, objPropTargetShape, objPropEdgeColor, objPropEdgeType 
  
  * Graffoo visualization, Property or Both class and property graph type:  collapseEdges
  
  * UML visualization, Class, Property or Both class and property graph type:  collapseEdges

Note that most of the fields are limited to specific strings. These are:

  * CollapseEdges: collapseTrue or collapseFalse
  * Visualization: custom, graffoo, uml or vowl
  * GraphType: class, individual, property or both
  * Any node shape: circle, smallCircle (as for a Graffoo individual, text outside the circle), diamond, ellipse (sized according to the contained text), hexagon, parallelogramRight (parallelogram, skewed right), parallelogramLeft (parallelogram, skewed left), roundRectangle, squareRectangle or none (no border, white fill)
  * Any source/target (arrow) parameter: angleBracket, backslash (as for a Graffoo annotation property), circleSolid, circleEmpty, diamondSolid, diamondEmpty, triangleSolid, triangleEmpty or none
  * Any line type parameter: solid, dashed, dotted, dashedDotted or none
  * Any color parameter: hex color code format ('#' followed by 3 or 6 hexadecimals)
  
The GUI Javascript handles all of this via Backbone. Please refer to the code in `src/main/resources/static/js/main.js`. Alternately, review the test cases defined in `src/test/java/graph`. All of the possible inputs can also be seen in `src/main/java/graph/models/GraphRequestModel.java`.

##### REST example
Here is an example of the JSON parameters in the payload of a POST request for a custom, class definitions graph:
* graphTitle:	Test Graph
* inputFile:	C:\fakepath\TestFOAF.rdf
* fileData:	    data:application/rdf+xml;base6…vcGVydHk+Cgo8L3JkZjpSREY+Cgo=...
* visualization:	custom
* graphType:	class
* classNodeShape:	roundRectangle
* classFillColor:	#FFFF99
* classTextColor:	#000000
* classBorderColor:	#000000
* classBorderType:	solid
* subclassOfSourceShape:	none
* subclassOfTargetShape:	triangleEmpty
* subclassOfLineColor:	#000000
* subclassOfLineType:	solid

The input file name is only used to record the specific file and its suffix - in this example, TestFOAF.rdf. The GUI loads this file and passes it to the application in the fileData parameter.

The response corresponding to the above request is:
* graphTitle:	Test Graph
* visualization:	custom
* graphType:	class
* graphML:	`<?xml version="1.0" encoding="..esources/> </data> </graphml>`
<br><br>

### Code details
Information related to customizing the GUI, changing how OntoGraph and Stardog are accessed, and running OntoGraph in debug mode are discussed below.

##### Customizing output 
The GUI uses a set of default settings for the inputs when a 'custom' visualization is selected. The defaults are defined in the file, `src/main/resources/static/js/GraphRequestModel.js` (also easily found by searching the source for the string, 'CHANGEME'). For each graph type (class, individual, property or both class and property), change the corresponding values in the Javascript, and these will be displayed when generating a 'custom' graph. (Note that they can still be changed from the new defaults, if desired.) 

##### OntoGraph Tomcat server
OntoGraph is configured to run on localhost:8181, but this can easily be changed in `src/main/resources/application.properties`. This file can also be found by searching for 'CHANGEME' in the source. Line 10 specifies the port on which OntoGraph 'listens'.

##### Temporary directory
In the `src/main/java/resources/application.properties` file, you can edit the location of a temporary directory where OntoGraph stores the ontology file to be graphed. The file is uploaded and exists on disk for a brief period of time, in order to create and load a Stardog database with its contents (and take advantage of namespace prefix processing). After loading the ontology file, it is immediately deleted. 

The default location of the ontology file is the current directory where OntoGraph is executing. This is the directory from which you invoked the `java -jar` command, or the directory where the OntoGraph source is downloaded if you are running in your development environment. In order to change this, edit the file location in the `application.properties` file, line 13. Make sure that you include the ending '/', that the directory exists and that the OntoGraph application has write permissions.

##### Stardog
Depending on how you started the Stardog server (if you changed its default port or user names and passwords), its access information also needs to be updated in the `src/main/java/resources/application.properties` file (which also can be found by searching for the phrase 'CHANGEME' in the OntoGraph source, as above):

  * Line 5: the URL to your Stardog server, either local or remote (set to the default: `http://localhost:5820`)
  * Line 6: the Stardog server username (set to the default: `admin`)
  * Line 7: the Stardog server password (set to the default: `admin`)
  
Note that Stardog has many configurable parameters, but these do not need to be modified for use with OntoGraph. 
<br><br>

### Testing and testcases
The OntoGraph test directory (`src/test/java/graph`) contains a series of jUnit- and Spring-backed test methods that generate a graph given certain parameters, and compare it to a control file containing details about expected node and edge attributes and values. The graphs that are generated are based on various test ontologies found in `src/test/resources` and are organized based on graph type ('class', 'individual', 'property' or 'both' class and property). All control files can be found in `src/test/resources/control`. The tests have been designed to cover a large majority of graph and node combinations possible with OWL and OWL 2. To ensure the most complete coverage possible, we also include tests of the [Turtle RDF Primer](https://www.w3.org/2007/02/turtle/primer/) and [Friend of a Friend](http://www.foaf-project.org/).

The test cases vary with each graph type, but are the same for each visualization within the graph type. In the case of individual graphs, VOWL is not included as individuals are not represented in (not supported by) VOWL 2.0. The UML visualization tests are only defined for the 'class' and 'individual' graph types, because the UML visualization for 'class' is the same as for 'property' or 'both' class and property (given how UML class diagrams are rendered).

##### Writing test control files

There are two types of CSV control files for each test case - one defining the "expected" attributes and values for nodes, and the other for edges. The node files are defined for either UML customization nodes or for 'custom', 'graffoo' or 'vowl' visualization nodes, whereas the edge files are the same for all visualizations. Each file is designated by `TestName_Edges.txt` or `TestName_Nodes.txt`.

OntoGraph's tests validate the following:
  * The generated GraphML output is valid XML
  * All node and edge ids in the control files are present
  * There no other node or edge ids besides those that are described in the control files
  * No attribute values are set to "null" or an empty string (meaning that the attribute value was not set to a valid input)
  * Each node and edge described in the control files uses the specified element and attribute values

The 'Edge' files match against the following information for each "expected" edge in a generated graph:

* <edge> element, 'id' attribute
* <edge> element 'source' attribute
* <edge> element 'target' attribute
* <edge> element ... <y:Arrows> sub-element's 'source' attribute value 
* <edge> element ... <y:Arrows> sub-element's 'target' attribute value 
* <edge> element ... <y:LineStyle> sub-element's 'color' attribute value 
* <edge> element ... <y:LineStyle> sub-element's 'type' attribute value 
* <edge> element ... <y:EdgeLabel> sub-element's 'backgrounColor' attribute value
* <edge> element ... <y:EdgeLabel> sub-element's value 

These values are listed in order, on a single line, separated by the text, ", " (without the double quotes). Comments can be added to a control file by beginning the line with double forward slashes ("//").

For example, here is a edge check from a control file for a custom visualization of a 'class' graph type for FOAF:  
`subClassOffoaf:Personfoaf:Agent, foaf:Person, foaf:Agent, none, white_delta, #000000, line, #FFFFFF, rdfs:subClassOf`  

It validates the following GraphML output:  

    <edge id="subClassOffoaf:Personfoaf:Agent" source="foaf:Person" target="foaf:Agent">  
      <data key="d10">
        <y:PolyLineEdge>
          <y:Path sx="0.0" sy="0.0" tx="0.0" ty="0.0"/>
          <y:LineStyle color="#000000" type="line" width="1.0"/>
          <y:Arrows source="none" target="white_delta"/>
          <y:EdgeLabel alignment="center" backgroundColor="#FFFFFF" distance="2.0" fontFamily="Dialog" fontSize="16" fontStyle="plain" hasLineColor="false" height="22.84" modelName="centered" modelPosition="center" preferredPlacement="anywhere" ratio="0.5" textColor="#000000" width="76.16" x="102.14" y="-11.42" visible="true">rdfs:subClassOf</y:EdgeLabel>
          <y:BendStyle smoothed="false"/>
        </y:PolyLineEdge>
      </data>
    </edge>`

Note that the values of the arrow source and target types and the line style type are the values required by yEd. These are translated from the OntoGraph's validated inputs by the program, since the yEd values are not intuitive. The correspondences are provided in the section, [Mapping to yEd](#mapping-to-yed), below.

The 'custom', 'graffoo' or 'vowl' node files contain the following checks for each "expected" node:

* <node> element's 'id' attribute value
* <node> element ... <y:Fill> sub-element's 'fill' attribute value
* <node> element ... <y:BorderStyle> sub-element's 'color' attribute value
* <node> element ... <y:BorderStyle> sub-element's 'type' attribute value
* <node> element ... <y:NodeLabel> sub-element's value
* <node> element ... <y:Shape> sub-element's 'type' attribute value
	
As above, these values are listed in order, on a single line, separated by the text, ", " (without the double quotes). For example, here is a node check from a control file for a custom visualization of a 'class' graph type for FOAF:  
`foaf:Person, #FFFF99, #000000, line, Person (foaf:Person), roundrectangle`  

It validates the following GraphML output:  

    <node id="foaf:Person">
      <data key="d6">
        <y:ShapeNode>
          <y:Geometry height="50.0" width="260.0" x="385.3" y="187.0"/>
          <y:Fill color="#FFFF99" transparent="false"/>
          <y:BorderStyle color="#000000" type="line" width="1.0"/>
          <y:NodeLabel alignment="center" autoSizePolicy="content" fontFamily="Dialog" fontSize="16" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="22.84" modelName="internal" modelPosition="c" textColor="#000000" visible="true" width="44.84" x="18.56" y="10.58">Person (foaf:Person)</y:NodeLabel>
          <y:Shape type="roundrectangle"/>
        </y:ShapeNode>
      </data>
    </node>
    
Also as above, the values of the border style type are the values required by yEd. These are translated from the OntoGraph's validated values by the program. The correspondences are provided in the section, [Mapping to yEd](#mapping-to-yed), below.

The 'uml' node files contain the following checks for each "expected" node:

* <node> element's 'id' attribute value
* <node> element ... <y:Fill> sub-element's 'fill' attribute value
* <node> element ... <y:BorderStyle> sub-element's 'color' attribute value
* <node> element ... <y:BorderStyle> sub-element's 'type' attribute value
* <node> element ... <y:NodeLabel alignment="center"> sub-element's value (the title of the UML class or individual)
* <node> element ... <y:NodeLabel alignment="left"> sub-element's value (the attributes of the UML class or individual)
	
As above, these values are listed in order, on a single line, separated by the text, ", " (without the double quotes). For example, here is a node check from a control file for a custom visualization of a 'class' graph type for FOAF:  
`foaf:Person, #FFFFFF, #000000, line, Person (foaf:Person), foaf:geekcode : xsd:StringNEW_LINEfoaf:firstName : xsd:StringNEW_LINEfoaf:lastName : xsd:StringNEW_LINEfoaf:surname : xsd:StringNEW_LINEfoaf:family_name : xsd:StringNEW_LINEfoaf:familyName : xsd:StringNEW_LINEfoaf:plan : xsd:StringNEW_LINEfoaf:myersBriggs : xsd:String`  

It validates the following GraphML output:  

    <node id="foaf:Person">
      <data key="d5"/>
      <data key="d6">
        <y:GenericNode configuration="com.yworks.entityRelationship.big_entity">
          <y:Geometry height="170.0" width="261.0" x="385.30" y="187.01"/>
          <y:Fill color="#FFFFFF" transparent="false"/>
          <y:BorderStyle color="#000000" type="line" width="1.0"/>
          <y:NodeLabel alignment="center" autoSizePolicy="content" backgroundColor="#FFFFFF" configuration="com.yworks.entityRelationship.label.name" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasLineColor="false" height="18.13" modelName="internal" modelPosition="t" textColor="#000000" visible="true" width="36.67" x="21.67" y="4.0">Person (foaf:Person)</y:NodeLabel>
          <y:NodeLabel alignment="left" autoSizePolicy="content" configuration="com.yworks.entityRelationship.label.attributes" fontFamily="Dialog" fontSize="12" fontStyle="plain" hasBackgroundColor="false" hasLineColor="false" height="46.40" modelName="custom" textColor="#000000" visible="true" width="65.54" x="2.0" y="30.13">foaf:geekcode : xsd:String
    foaf:firstName : xsd:String
    foaf:lastName : xsd:String
    foaf:surname : xsd:String
    foaf:family_name : xsd:String
    foaf:familyName : xsd:String
    foaf:plan : xsd:String
    foaf:myersBriggs : xsd:String
            <y:LabelModel>
              <y:ErdAttributesNodeLabelModel/>
            </y:LabelModel>
            <y:ModelParameter>
              <y:ErdAttributesNodeLabelModelParameter/>
            </y:ModelParameter>
          </y:NodeLabel>
          <y:StyleProperties>
            <y:Property class="java.lang.Boolean" name="y.view.ShadowNodePainter.SHADOW_PAINTING" value="false"/>
          </y:StyleProperties>
        </y:GenericNode>
      </data>
    </node>

##### Special strings in the test attribute and value checks

As seen in the UML example above where NEW_LINE is added to the expected text (and also in many of the other control files), there are a few 'special' strings. In general, any entered text is compared as-is, but a simple replaceAlsl is done for the following strings:

  * `NULL` to ignore an attribute check  
    * For example, there is no Shape element for a UML 'note' (yEd's <y:UMLNoteNode> element)
  * `EMPTY_STRING` if a label is blank
  * `NEW_LINE` if the label contains a line break
  * `COMMA_SPACE` in place of a comma-space in a value, which would instead be interpreted as a separator when breaking apart the control file line

##### Running tests

All tests can be executed from the command line by entering `gradle test`. Over 200 tests are defined, and the results can be found in `build/reports/test/test/index.html`.

Additionally, each test in the `src/test/java/graph` directory can be individually executed in (for example) Eclipse, by right-clicking the test name and selecting "Run As" ... "JUnit Test".

##### Mapping to yEd

The following mapping applies to translate OntoGraph's inputs to yEd GraphML definitions:

  * Arrow types  
    * angleBracket => plain  
    * backslash => skewed_dash  
    * circleSolid => circle  
    * circleEmpty => transparent_circle  
    * diamondSolid => diamond  
    * diamondEmpty => white_diamond  
    * triangleSolid => delta  
    * triangleEmpty => white_delta  
    * none => none  
  * Line types  
    * solid => line  
    * dashed => dashed  
    * dotted => dotted  
    * dashedDotted => dashed_dotted  
    * none => none  
  * Node shape types  
    * circle => ellipse with width = height  
    * smallCircle => ellipse with width = height = 20  
    * diamond => diamond  
    * ellipse => ellipse  
    * hexagon => hexagon  
    * parallelogramRight => parallelogram  
    * parallelogramLeft => parallelogram2  
    * roundRectangle (rectangle with rounded corners) => roundrectangle  
    * squareRectangle (rectangle with square corners) => rectangle  
    * none => rectangle with fill color and border color of white
  
This information is defined in the file, `src/main/java/graph/graphmloutputs/GraphMLOutputDetails.java`, in the createNodeMap, createArrowMap and createLineMap methods.

### Known issues
All issues and improvements for OntoGraph are listed in the project's [Issues](https://github.com/NinePts/OntoGraph/issues). Please note that improvements are defined with the prefix, "(New Feature)".

Please create Github issues for any bugs or improvement suggestions, or feel free to fork the repository, make changes and create pull requests.
<br><br>

Thanks for using OntoGraph!  
The Nine Points Solutions Dev Team
