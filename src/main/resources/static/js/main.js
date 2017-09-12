//
// Copyright (c) Nine Points Solutions, LLC
// 
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// 

// ====================
// Local variables
// ====================

var AppRouter = Backbone.Router.extend({	
});
var app_router = new AppRouter;
var model = new GraphRequestModel(); 
var downloadComplete = false;

var borderTypeOptions = '<option>Solid</option> '
	+ '<option>Dashed</option> '
	+ '<option>Dashed and Dotted</option> '
	+ '<option>Dotted</option>'
	+ '<option>None</option>';

var nodeShapeOptions = '<option>Circle</option> '
					    + '<option>Circle, Small</option> '
					    + '<option>Diamond</option> '
					    + '<option>Ellipse</option> '
					    + '<option>Hexagon</option> '
					    + '<option>Parallelogram, Skewed Left</option> '
				 	    + '<option>Parallelogram, Skewed Right</option> '
					    + '<option>Rectangle, Rounded Corners</option> '
					    + '<option>Rectangle, Square Corners</option> '
					    + '<option>None (Text Only)</option>';

var lineTypeOptions = '<option>Solid</option> '
						+ '<option>Dashed</option> '
						+ '<option>Dashed and Dotted</option> '
						+ '<option>Dotted</option>';

var arrowTypeOptions = '<option>Angle Bracket</option> '
						+ '<option>Backslash</option> '
						+ '<option>Circle</option> '
						+ '<option>Circle Outline</option> '
						+ '<option>Diamond</option> '
						+ '<option>Diamond Outline</option> '
						+ '<option>Triangle</option> '
						+ '<option>Triangle Outline</option> '
						+ '<option>None</option>';

//====================
// Functions
//====================
function adjustArrowValue(value) {
	var newValue = value.toLowerCase();
	if (value.indexOf('Bracket') > -1) {
		newValue = 'angleBracket';
	} else if (value === 'Circle') {
		newValue = 'circleSolid';
	} else if (value === 'Circle Outline') {
		newValue = 'circleEmpty';
	} else if (value === 'Diamond') {
		newValue = 'diamondSolid';
	} else if (value === 'Diamond Outline') {
		newValue = 'diamondEmpty';
	} else if (value === 'Triangle') {
		newValue = 'triangleSolid';
	} else if (value === 'Triangle Outline') {
		newValue = 'triangleEmpty';
	}
	return newValue;
}

function adjustColorValue(value) {
	var newValue = value;
	if (!value.startsWith('#')) {
		newValue = '#' + value;
	}
	return newValue;
}

function adjustGraphType(value) {
	var newValue = 'class';
	if (value.startsWith('Object')) {
		newValue = 'property';
	} else if (value.startsWith('Individuals')) {
		newValue = 'individual';
	} else if (value.startsWith('Both')) {
		newValue = 'both';
	}
	return newValue;
}

function adjustLineValue(value) {
	var newValue = value.toLowerCase();
	if (value.indexOf(' and ') > -1) {
		newValue = 'dashedDotted';
	}
	return newValue;
}

function adjustNodeValue(value) {
	var newValue = value.toLowerCase();
	if (value.indexOf('Small') > -1) {
		newValue = 'smallCircle';
	} else if (value.indexOf('Skewed Left') > -1) {
		newValue = 'parallelogramLeft';
	} else if (value.indexOf('Skewed Right') > -1) {
		newValue = 'parallelogramRight';
	} else if (value.indexOf('Rounded') > -1) {
		newValue = 'roundRectangle';
	} else if (value.indexOf('Square') > -1) {
		newValue = 'squareRectangle';
	} else if (value.indexOf('None') > -1) {
		newValue = 'none';
	}
    return newValue;
}

function adjustVisualization(value) {
	return value.toLowerCase();
}

function cancelCustomization(customizationType) {
	document.getElementById('mainContainer').style.display = 'block';
	document.getElementById(customizationType).style.display = 'none';
	if (customizationType === 'propertyCustomization') {
		document.getElementById('classAndPropertyCustomization').style.display = 'none';
		var propTitle = $('#propertyCustomizationTitle').val();
		$('#propertyCustomizationTitle').val(propTitle.replace('Class and Property', 'XXX'));
		$('#propertyCustomizationTitle').val(propTitle.replace('Property', 'XXX'));
	} else if (customizationType === 'stdCustomization') {
		var stdTitle = $('#stdCustomizationTitle').val();
		$('#stdCustomizationTitle').val(stdTitle.replace('Graffoo', 'XXX'));
		$('#stdCustomizationTitle').val(stdTitle.replace('UML', 'XXX'));
	} 
}

function displayCustomization(customizationType) {
	document.getElementById('mainContainer').style.display = 'none';
	document.getElementById(customizationType).style.display = 'block';
}

function downloadGraph(strData, strFileName, strMimeType) {
	var D = document,
    	A = arguments,
    	a = D.createElement('a'),
    	n = A[1];

	// Build download link
	a.href = 'data:' + strMimeType + 'charset=utf-8,' + escape(strData);
	if (window.MSBlobBuilder) { // IE10 download
		var bb = new MSBlobBuilder();
		bb.append(strData);
		navigator.msSaveBlob(bb, strFileName);
		return true;
	} // end if IE10
	if ('download' in a) { // FF20 or CH19 download
		a.setAttribute('download', n);
		a.innerHTML = 'downloading...';
		D.body.appendChild(a);
		setTimeout(function() {
			var e = D.createEvent('MouseEvents');
			e.initMouseEvent("click", true, false, window, 0, 0, 0, 0, 0, 
					false, false, false, false, 0, null);
			a.dispatchEvent(e);
			D.body.removeChild(a);
		}, 66);
		return true;
	} // end if FF20 or CH19
	// iframe dataURL download
	var f = D.createElement('iframe');
	D.body.appendChild(f);
	f.src = "data:" + (A[2] ? A[2] : "application/xml") + (window.btoa ? ";base64" : "") + "," 
		+ (window.btoa ? window.btoa : escape)(strData);
	setTimeout(function() {
		D.body.removeChild(f);
	}, 333);
	return true;
}

function generateGraph() {
	Pace.start();
	downloadComplete = false;
	var graphType = model.get('graphType');
	if (graphType === 'both') {
		cancelCustomization('propertyCustomization');
	} else {
		cancelCustomization(graphType + 'Customization');
	}
	// Always cancel the standard customization (just in case)
	cancelCustomization('stdCustomization');
	var title = $('#graphTitle').val();
	model.save({}, {
		error: function (model, response, xhr) { 
			downloadComplete = true;
			var json = JSON.stringify(xhr.xhr.responseText);
			json = json.replace(/"/g, '');
			alert(json);   //NOSONAR - Not sensitive info
		},
		success: function (model, response) { 
			var fileName = title.replace(/ /g, '');
			// Set arguments and run download
			var strMimeType = 'application/xml';
			var strData = response.graphML;
			var strFileName = fileName + '.graphml';
			downloadComplete = downloadGraph(strData, strFileName, strMimeType);
		}
	});

	// Wait for the download to complete
	var waitOnDownload = function() {
		if (!downloadComplete) {
			setTimeout(waitOnDownload, 200);
		} else {
			Pace.stop();
		}
	};
	waitOnDownload();
}

function isValidInput(value) {
	return value !== null && value !== undefined && value.length > 0;
}

function setClassCustomization() {
	$('#classNodeShape').val(model.get('classNodeShape'));
	$('#classFillColor').val(model.get('classFillColor'));
	$('#classTextColor').val(model.get('classTextColor'));
	$('#classBorderColor').val(model.get('classBorderColor'));
	$('#classBorderType').val(model.get('classBorderType'));
	$('#subclassOfSourceShape').val(model.get('subclassOfSourceShape'));
	$('#subclassOfTargetShape').val(model.get('subclassOfTargetShape'));
	$('#subclassOfLineColor').val(model.get('subclassOfLineColor'));
	$('#subclassOfLineType').val(model.get('subclassOfLineType'));
}

function setDefaultsAndDisplayPage() {
	$('#visualization').val(model.get('visualization'));
	$('#graphType').val(model.get('graphType'));

	// Add the various option blocks
	$('#classNodeShape').append(nodeShapeOptions);
	$('#dataNodeShape').append(nodeShapeOptions);
	$('#indDataNodeShape').append(nodeShapeOptions);
	$('#individualNodeShape').append(nodeShapeOptions);
	$('#objNodeShape').append(nodeShapeOptions);
	$('#typeNodeShape').append(nodeShapeOptions);

	$('#classBorderType').append(borderTypeOptions);
	$('#dataBorderType').append(borderTypeOptions);
	$('#indDataBorderType').append(borderTypeOptions);
	$('#individualBorderType').append(borderTypeOptions);
	$('#objBorderType').append(borderTypeOptions);
	$('#typeBorderType').append(borderTypeOptions);
	
	$('#annPropEdgeType').append(lineTypeOptions);
	$('#dataPropEdgeType').append(lineTypeOptions);
	$('#indDataPropEdgeType').append(lineTypeOptions);
	$('#indObjPropEdgeType').append(lineTypeOptions);
	$('#objPropEdgeType').append(lineTypeOptions);
	$('#subclassOfLineType').append(lineTypeOptions);
	$('#subclassOfLineTypeBoth').append(lineTypeOptions);
	$('#typeOfLineType').append(lineTypeOptions);

	$('#annPropSourceShape').append(arrowTypeOptions);
	$('#annPropTargetShape').append(arrowTypeOptions);
	$('#dataPropSourceShape').append(arrowTypeOptions);
	$('#dataPropTargetShape').append(arrowTypeOptions);
	$('#indDataPropSourceShape').append(arrowTypeOptions);
	$('#indDataPropTargetShape').append(arrowTypeOptions);
	$('#indObjPropSourceShape').append(arrowTypeOptions);
	$('#indObjPropTargetShape').append(arrowTypeOptions);
	$('#objPropSourceShape').append(arrowTypeOptions);
	$('#objPropTargetShape').append(arrowTypeOptions);
	$('#subclassOfSourceShape').append(arrowTypeOptions);
	$('#subclassOfTargetShape').append(arrowTypeOptions);
	$('#subclassOfSourceShapeBoth').append(arrowTypeOptions);
	$('#subclassOfTargetShapeBoth').append(arrowTypeOptions);
	$('#typeOfSourceShape').append(arrowTypeOptions);
	$('#typeOfTargetShape').append(arrowTypeOptions);
	
	// Set the selected values
	setClassCustomization();
	setIndividualCustomization();
	setPropertyCustomization();
	setStdCustomization();
	
	document.getElementById('mainContainer').style.display = 'block';
}

function setIndividualCustomization() {
	$('#typeNodeShape').val(model.get('typeNodeShape'));
	$('#typeFillColor').val(model.get('typeFillColor'));
	$('#typeTextColor').val(model.get('typeTextColor'));
	$('#typeBorderColor').val(model.get('typeBorderColor'));
	$('#typeBorderType').val(model.get('typeBorderType'));
	$('#individualNodeShape').val(model.get('individualNodeShape'));
	$('#individualFillColor').val(model.get('individualFillColor'));
	$('#individualTextColor').val(model.get('individualTextColor'));
	$('#individualBorderColor').val(model.get('individualBorderColor'));
	$('#individualBorderType').val(model.get('individualBorderType'));
	$('#indDataNodeShape').val(model.get('indDataNodeShape'));
	$('#indDataFillColor').val(model.get('indDataFillColor'));
	$('#indDataTextColor').val(model.get('indDataTextColor'));
	$('#indDataBorderColor').val(model.get('indDataBorderColor'));
	$('#indDataBorderType').val(model.get('indDataBorderType'));
	
	$('#typeOfSourceShape').val(model.get('typeOfSourceShape'));
	$('#typeOfTargetShape').val(model.get('typeOfTargetShape'));
	$('#typeOfLineColor').val(model.get('typeOfLineColor'));
	$('#typeOfLineType').val(model.get('typeOfLineType'));
	
	$('#indObjPropSourceShape').val(model.get('indObjPropSourceShape'));
	$('#indObjPropTargetShape').val(model.get('indObjPropTargetShape'));
	$('#indObjPropEdgeColor').val(model.get('indObjPropEdgeColor'));
	$('#indObjPropEdgeType').val(model.get('indObjPropEdgeType'));
	$('#indDataPropSourceShape').val(model.get('indDataPropSourceShape'));
	$('#indDataPropTargetShape').val(model.get('indDataPropTargetShape'));
	$('#indDataPropEdgeColor').val(model.get('indDataPropEdgeColor'));
	$('#indDataPropEdgeType').val(model.get('indDataPropEdgeType'));
}

function setPropertyCustomization() {
	$('#subclassOfSourceShapeBoth').val(model.get('subclassOfSourceShape'));
	$('#subclassOfTargetShapeBoth').val(model.get('subclassOfTargetShape'));
	$('#subclassOfLineColorBoth').val(model.get('subclassOfLineColor'));
	$('#subclassOfLineTypeBoth').val(model.get('subclassOfLineType'));
	
	var collapse = model.get('collapseEdges');
	document.getElementById(collapse).checked = true;
	$('#collapseEdges').val(collapse);
	$('#objNodeShape').val(model.get('objNodeShape'));
	$('#objFillColor').val(model.get('objFillColor'));
	$('#objTextColor').val(model.get('objTextColor'));
	$('#objBorderColor').val(model.get('objBorderColor'));
	$('#objBorderType').val(model.get('objBorderType'));
	$('#dataNodeShape').val(model.get('dataNodeShape'));
	$('#dataFillColor').val(model.get('dataFillColor'));
	$('#dataTextColor').val(model.get('dataTextColor'));
	$('#dataBorderColor').val(model.get('dataBorderColor'));
	$('#dataBorderType').val(model.get('dataBorderType'));
	$('#objPropSourceShape').val(model.get('objPropSourceShape'));
	$('#objPropTargetShape').val(model.get('objPropTargetShape'));
	$('#objPropEdgeColor').val(model.get('objPropEdgeColor'));
	$('#objPropEdgeType').val(model.get('objPropEdgeType'));
	$('#dataPropSourceShape').val(model.get('dataPropSourceShape'));
	$('#dataPropTargetShape').val(model.get('dataPropTargetShape'));
	$('#dataPropEdgeColor').val(model.get('dataPropEdgeColor'));
	$('#dataPropEdgeType').val(model.get('dataPropEdgeType'));
	$('#annPropSourceShape').val(model.get('annPropSourceShape'));
	$('#annPropTargetShape').val(model.get('annPropTargetShape'));
	$('#annPropEdgeColor').val(model.get('annPropEdgeColor'));
	$('#annPropEdgeType').val(model.get('annPropEdgeType'));
}

function setStdCustomization() {
	var collapse = model.get("collapseEdges");
	if (collapse === 'collapseTrue') {
		document.getElementById('collapseStdTrue').checked = true;
	} else {
		document.getElementById('collapseStdFalse').checked = true;
	}
	$('#collapseEdgesStd').val(collapse);
}

//====================
// Main Processing
//====================
$(document).ready(function() {
	Backbone.history.start();

	$('#pop1').popover({ trigger: 'hover' });
	$('#pop2').popover({ trigger: 'hover' });
	$('#pop3').popover({ trigger: 'hover' });
	$('#pop4').popover({ trigger: 'hover' });
	$('#pop5').popover({ trigger: 'hover' });
	$('#pop6').popover({ trigger: 'hover' });

	// Set up the 'default' values for the various selections in the browser
	setDefaultsAndDisplayPage();

	$('#submit').on('click', function() {
		// Validate the data from the browser
		if (!isValidInput($('#graphTitle').val())) {
			alert('The graph must have a title.');		//NOSONAR - Not sensitive info
		} else if (!isValidInput($('#inputFile').val())) {
			alert('An ontology input file must be specified.');  //NOSONAR - Not sensitive info
		} else if ($('#visualization').val() === 'VOWL' 
			&& $('#graphType').val().indexOf('Individual') === 0) {
			alert('No graphical representation is defined for individuals in VOWL 2. '  //NOSONAR - Not sensitive info
					+ 'Either change the visualization or the graph type selection.');
		// TODO Other checks?  Perhaps checking that edge lines are not drawn using white?
		
		} else {
			
			// "Standardize" the graph type and visualization
			var graphType = adjustGraphType($('#graphType').val());
			var visualization = adjustVisualization($('#visualization').val());
		
			// Update the GraphRequestModel with the data from the browser
			model.set({
				graphTitle: $('#graphTitle').val(),
				inputFile: $('#inputFile').val(),
				fileData: '',
				visualization: visualization,
				graphType: graphType
				});
			// Check that the input file can be read
			var file = document.getElementById('inputFile').files[0];
			var reader = new FileReader();
			reader.onload = function() {
				var fileData = this.result;
				model.set('fileData', fileData);
				// Check if this is a custom visualization - If so, need to get specific options
				if (visualization === 'custom') {
					// Show correct customization options based on the graphType
					if (graphType === 'class') {
						var classTitle = document.getElementById('classCustomizationTitle').innerHTML;
						classTitle = classTitle.substring(0, classTitle.indexOf(',') + 2) + file.name;
						document.getElementById('classCustomizationTitle').innerHTML = classTitle;
						displayCustomization('classCustomization');
					} else if (graphType === 'individual') {
						var indivTitle = document.getElementById('individualCustomizationTitle').innerHTML;
						indivTitle = indivTitle.substring(0, indivTitle.indexOf(',') + 2) + file.name;
						document.getElementById('individualCustomizationTitle').innerHTML = indivTitle;
						displayCustomization('individualCustomization');
					} else if (graphType === 'property') {
						var propTitle = document.getElementById('propertyCustomizationTitle').innerHTML;
						propTitle = propTitle.replace('XXX', 'Property');
						propTitle = propTitle.substring(0, propTitle.indexOf(',') + 2) + file.name;
						document.getElementById('propertyCustomizationTitle').innerHTML = propTitle;
						displayCustomization('propertyCustomization');
					} else if (graphType === 'both') {
						var title = document.getElementById('propertyCustomizationTitle').innerHTML;
						title = title.replace('XXX', 'Class and Property');
						title = title.substring(0, title.indexOf(',') + 2) + file.name;
						document.getElementById('propertyCustomizationTitle').innerHTML = title;
						displayCustomization('propertyCustomization');
						document.getElementById('classAndPropertyCustomization').style.display = 'block';
					} else {
						alert('Invalid/Unknown Graph Type:' + graphType);  //NOSONAR - Not sensitive info
					}
				} else {
					// Not custom, just need to know whether edges are collapsed for Graffoo property 
					///   or UML graphs
					// Note that VOWL does not support collapsing edges - so, set "false" as the default
					$('#collapseEdges').val('collapseFalse');
					if (visualization === 'uml' || (visualization === 'graffoo' 
							&& graphType === 'property')) {
						var stdTitle = document.getElementById('stdCustomizationTitle').innerHTML;
						stdTitle = stdTitle.replace('XXX', 'Graffoo');
						stdTitle = stdTitle.substring(0, stdTitle.indexOf(',') + 2) + file.name;
						if (visualization === 'uml') {	//NOSONAR - 4 levels of nested if/else
							stdTitle = stdTitle.replace('Graffoo', 'UML');
						}
						document.getElementById('stdCustomizationTitle').innerHTML = stdTitle;
						displayCustomization('stdCustomization');
					} else {
						generateGraph();
					}
				}
			}
			reader.onerror = function() {
				alert('The file, ' + file.name + ', could not be read.');  //NOSONAR - Not sensitive info
			}
			reader.readAsDataURL(file);
		}
	});

	// Handle cancelling customization
	$('#cancelClassDiagram').on('click', function() {
		 cancelCustomization('classCustomization');
	});
	$('#cancelIndividualDiagram').on('click', function() {
		 cancelCustomization('individualCustomization');
	});
	$('#cancelPropertyDiagram').on('click', function() {
		 cancelCustomization('propertyCustomization');
	});
	$('#cancelStdDiagram').on('click', function() {
		 cancelCustomization('stdCustomization');
	});
	
	// Handle processing customization
	$('#classDiagram').on('click', function() {
		// Update the GraphRequestModel with the data from the browser
		model.set({
			classNodeShape: adjustNodeValue($('#classNodeShape').val()),
			classFillColor: adjustColorValue($('#classFillColor').val()),
			classTextColor: adjustColorValue($('#classTextColor').val()),
			classBorderColor: adjustColorValue($('#classBorderColor').val()),
			classBorderType: adjustLineValue($('#classBorderType').val()),
			subclassOfSourceShape: adjustArrowValue($('#subclassOfSourceShape').val()),
			subclassOfTargetShape: adjustArrowValue($('#subclassOfTargetShape').val()),
			subclassOfLineColor: adjustColorValue($('#subclassOfLineColor').val()),
			subclassOfLineType: adjustLineValue($('#subclassOfLineType').val())
		});
		generateGraph();
	});
	$('#individualDiagram').on('click', function() {
		// Update the GraphRequestModel with the data from the browser
		model.set({
			typeNodeShape: adjustNodeValue($('#typeNodeShape').val()),
			typeFillColor: adjustColorValue($('#typeFillColor').val()),
			typeTextColor: adjustColorValue($('#typeTextColor').val()),
			typeBorderColor: adjustColorValue($('#typeBorderColor').val()),
			typeBorderType: adjustLineValue($('#typeBorderType').val()),
			individualNodeShape: adjustNodeValue($('#individualNodeShape').val()),
			individualFillColor: adjustColorValue($('#individualFillColor').val()),
			individualTextColor: adjustColorValue($('#individualTextColor').val()),
			individualBorderColor: adjustColorValue($('#individualBorderColor').val()),
			individualBorderType: adjustLineValue($('#individualBorderType').val()),
			indDataNodeShape: adjustNodeValue($('#indDataNodeShape').val()),
			indDataFillColor: adjustColorValue($('#indDataFillColor').val()),
			indDataTextColor: adjustColorValue($('#indDataTextColor').val()),
			indDataBorderColor: adjustColorValue($('#indDataBorderColor').val()),
			indDataBorderType: adjustLineValue($('#indDataBorderType').val()),
			typeOfSourceShape: adjustArrowValue($('#typeOfSourceShape').val()),
			typeOfTargetShape: adjustArrowValue($('#typeOfTargetShape').val()),
			typeOfLineColor: adjustColorValue($('#typeOfLineColor').val()),
			typeOfLineType: adjustLineValue($('#typeOfLineType').val()),
			indObjPropSourceShape: adjustArrowValue($('#indObjPropSourceShape').val()),
			indObjPropTargetShape: adjustArrowValue($('#indObjPropTargetShape').val()),
			indObjPropEdgeColor: adjustColorValue($('#indObjPropEdgeColor').val()),
			indObjPropEdgeType: adjustLineValue($('#indObjPropEdgeType').val()),
			indDataPropSourceShape: adjustArrowValue($('#indDataPropSourceShape').val()),
			indDataPropTargetShape: adjustArrowValue($('#indDataPropTargetShape').val()),
			indDataPropEdgeColor: adjustColorValue($('#indDataPropEdgeColor').val()),
			indDataPropEdgeType: adjustLineValue($('#indDataPropEdgeType').val())
		});
		generateGraph();
	});
	$('#propertyDiagram').on('click', function() {
		// Update the GraphRequestModel with the data from the browser
		model.set({
			collapseEdges: $('input[name="collapseEdges"]:checked').val(),
			objNodeShape: adjustNodeValue($('#objNodeShape').val()),
			objFillColor: adjustColorValue($('#objFillColor').val()), 
			objTextColor: adjustColorValue($('#objTextColor').val()),
			objBorderColor: adjustColorValue($('#objBorderColor').val()),
			objBorderType: adjustLineValue($('#objBorderType').val()),
			dataNodeShape: adjustNodeValue($('#dataNodeShape').val()),
			dataFillColor: adjustColorValue($('#dataFillColor').val()),
			dataTextColor: adjustColorValue($('#dataTextColor').val()),
			dataBorderColor: adjustColorValue($('#dataBorderColor').val()),
			dataBorderType: adjustLineValue($('#dataBorderType').val()),
			objPropSourceShape: adjustArrowValue($('#objPropSourceShape').val()),
			objPropTargetShape: adjustArrowValue($('#objPropTargetShape').val()),
			objPropEdgeColor: adjustColorValue($('#objPropEdgeColor').val()),
			objPropEdgeType: adjustLineValue($('#objPropEdgeType').val()),
			dataPropSourceShape: adjustArrowValue($('#dataPropSourceShape').val()),
			dataPropTargetShape: adjustArrowValue($('#dataPropTargetShape').val()),
			dataPropEdgeColor: adjustColorValue($('#dataPropEdgeColor').val()),
			dataPropEdgeType: adjustLineValue($('#dataPropEdgeType').val()),
			annPropSourceShape: adjustArrowValue($('#annPropSourceShape').val()),
			annPropTargetShape: adjustArrowValue($('#annPropTargetShape').val()),
			annPropEdgeColor: adjustColorValue($('#annPropEdgeColor').val()),
			annPropEdgeType: adjustLineValue($('#annPropEdgeType').val()),
			
			subclassOfSourceShape: adjustArrowValue($('#subclassOfSourceShapeBoth').val()),
			subclassOfTargetShape: adjustArrowValue($('#subclassOfTargetShapeBoth').val()),
			subclassOfLineColor: adjustColorValue($('#subclassOfLineColorBoth').val()),
			subclassOfLineType: adjustLineValue($('#subclassOfLineTypeBoth').val())
		});
		generateGraph();
	});
	$('#stdDiagram').on('click', function() {
		// Update the GraphRequestModel with the data from the browser
		model.set({
			collapseEdges: $('input[name="collapseEdgesStd"]:checked').val()
		});
		generateGraph();
	});
});
