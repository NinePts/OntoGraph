OntoGraph, Current Version 1.0.0
======
Licensed under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0). 

[Overview](#overview)<br>
[Setup](#setup)<br>
[Using the GUI](#graphical-interface)<br>
[Using the REST API](#rest-interface)<br>
[Modifying the Code](#code-details)<br>
[Testing and Testcases](#testing-and-testcases)<br>
[Issues](#known-issues)
<br><br>

### Overview
OntoGraph is a Spring Boot application for graphing OWL ontologies. The code consists of a Java-based REST API that creates various GraphML outputs of a user-provided OWL ontology file (or zip file of a set of ontology files) that can be input programmatically or using a simple GUI. The program stores the ontologies in a triple store ([Stardog](http://www.stardog.com/)), then runs a series of queries to return the necessary information (classes, properties, individuals...) to be diagrammed. For more information on OntoGraph and why it was created, click 'Download' for the paper, [Ontology Development by Domain Experts (Without Using the "O" Word)](https://github.com/NinePts/OntoGraph/blob/master/OntologyDevelopmentByDomainExperts.pdf). 

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

To check your Java version, go to the command line in Linux, OSX or Windows, and type `java -version`. If your version is not "build 1.8.x", please update it. To update Java, go to the [Oracle Free Java Download page](https://www.java.com/en/download/), and follow the directions there.

To download Stardog, go to [Stardog Knowledge Graph](http://stardog.com/) and click the 'DOWNLOAD' button.  Then, complete the form with your information, select either the Stardog community or enterprise trial version, and click download. (Note that the community edition of Stardog is free and can be used with OntoGraph. The enterprise trial version can also be used, but the license expires 30 days from download.) After receiving an email with your download details (which is sent very quickly after clicking 'download'), get the Stardog zip file and unpack it. Details on installing Stardog for Linux, OSX or Windows can be found in the [Stardog Quick Start Guide](http://www.stardog.com/docs/#_quick_start_guide).

OntoGraph can be run directly from its jar file, which is downloaded (along with a few other files) from a [zip file](https://github.com/NinePts/OntoGraph/blob/master/release/ontograph.zip) in the Nine Points Solutions' OntoGraph release directory in GitHub. After downloading ontograph.zip, unzip it in your desired directory. Then, execute `java -jar OntoGraph.jar` from the command line (in the directory where you unzipped the file). Please note that Stardog MUST have been started (`./stardog-admin server start` or on Windows, `stardog-admin.bat server start`) before requesting any graph output from OntoGraph.

After starting Ontograph, access its GUI from your web browser at the URI, `http://localhost:8181`. Using the GUI, you can specify the ontology file to be diagrammed, a graph title, the visualization (custom, Graffoo, UML-like or VOWL) and the type of graph (class, property or individual). The output file will be available from a dialog box allowing you to open or save the file (it is saved in your 'Downloads' directory). For more details about the GUI interface, see the [Graphical Inteface](#graphical-interface) section below.

As opposed to executing the jar file, OntoGraph's complete source can be downloaded from [Nine Points Solutions' GitHub repository](https://github.com/NinePts/OntoGraph/). OntoGraph is built using [Gradle](http://gradle.org). It can be run as a Java application or in debug (bootrun) mode from the (for example) Gradle Tasks view of Eclipse. 

OntoGraph is built with the current release of Gradle (V4.0.1), but has also been tested with Gradle V3.5. (If you want to use an earlier version than 4.0.1, change the version number specified in the build.gradle file, line 105.) You can also navigate to the (downloaded) OntoGraph directory and enter `./gradlew tasks` on the command line to download Gradle v4.0.1 manually.
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
* graphML:	<?xml version="1.0" encoding="..esources/> </data> </graphml>
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
In-progress.
<br><br>

### Known issues
All issues and improvements for OntoGraph are listed in the project's [Issues]().

Please create Github issues for any bugs or improvement suggestions, or feel free to fork the repository, make changes and create pull requests.
<br><br>

Thanks for using OntoGraph!  
The Nine Points Solutions Dev Team
