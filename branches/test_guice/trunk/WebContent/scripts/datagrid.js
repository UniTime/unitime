/*
 * This functions allow the user to interact with a datagrid object.
 */
 
// Global object holding the data for the different grids.
var strutsLayoutDatagridData = new Object();

StrutsLayout = new Object();

// definition of a column
StrutsLayout.Column = function (in_property, in_styleClass, in_type, in_values) {
	this.property = in_property;
	this.styleClass = in_styleClass;
	this.type = in_type;
	this.values = in_values;
};

// definition of an option
StrutsLayout.Option = function (in_label, in_value) {
	this.label = in_label;
	this.value = in_value;
};

// definition of the Struts-Layout datagrid js object.
StrutsLayout.Datagrid = function Datagrid(in_property, in_styleId, in_styleClass, in_styleClass2, in_allowSelection, in_allowMultipleSelection) {
	// form property holding the data.
	this.property = in_property;
	
	// id of the table element.
	this.styleId = in_styleId;
	
	// definition of the columns of the table
	this.columns = new Array();
	
	// definition of the styleClass of the rows.
	this.styleClass = in_styleClass;
	this.styleClass2 = in_styleClass2;
	this.rowStyleClassMap = new Object();
	
	// allow selection
	this.allowSelection = in_allowSelection;
	this.allowMultipleSelection = in_allowMultipleSelection;
		
	// add a column
	StrutsLayout.Datagrid.prototype.addColumn = function addColumn(in_property, in_styleClass, in_type, in_values) {
		this.columns[this.columns.length] = new StrutsLayout.Column(in_property, in_styleClass, in_type, in_values);
	};
		
	// add a styleClass
	StrutsLayout.Datagrid.prototype.addStyleClass = function addStyleClass(in_styleName, in_styleClass) {
		this.rowStyleClassMap[in_styleName] = in_styleClass;
	};
	
	// set the state
	StrutsLayout.Datagrid.prototype.initState = function initState(in_index, in_state) {
		var table = document.getElementById(this.styleId);
		var row = table.rows[in_index+1]; // row 0 is header.
		
		if (in_state!=null && in_state!="") {
			row.className = this.rowStyleClassMap[in_state];
		}
		var hidden = this.createStateElement(in_index, in_state);
		table.parentNode.appendChild(hidden);
		
	};
				
	this.getDatagridRowStateField = getDatagridRowStateField;		
	this.addDatagridCell = addDatagridCell;	
	this.getDatagridLinesWithState = getDatagridLinesWithState;
	this.createStateElement = createStateElement;
	this.unselectRows = unselectRows;
	this.selectDatagridLine = selectDatagridLine;
	
	// return the hidden field holding the state of the specified line.
	function getDatagridRowStateField(rowIndex) {
		var element = document.forms[0].elements[this.property + ".dataState[" + (rowIndex-1) + "]"];
		if (element==null) {
			// IE bug
			element = document.getElementById(this.property + ".dataState[" + (rowIndex-1) + "]");
		}
		return element;
	}
	
	// select a datagrid line
	function selectDatagridLine(row) {
		var table;
		var index;				
		
		if (row.parentNode) {
			// Get the parent table.
			table = row.parentNode.parentNode;
			
			// Get the row index.		
			for (index = 0; index < table.rows.length; index++) {
				if (row==table.rows[index]) {
					break;
				}
			}	
		} else {
			index = parseInt(row) - 1;
			table = document.getElementById(this.styleId);
			row = table.rows[index];
		}						
					
		// Get the row status.
		var hidden = this.getDatagridRowStateField(index);		
		
		var status = hidden.value;
		if (status=="selected") {
			status = "";
		} else {
			status = "selected";
		}
		
		// Set the new row status.
		hidden.value = status;
		if (status=="") {
			if (index % 2) {
				row.className = this.styleClass;
			} else {
				row.className = this.styleClass2;
			}
		} else {
			row.className = this.rowStyleClassMap["selected"];
		}
		
		// If single selection is used, unselect all other rows.
		if (!this.allowMultipleSelection) {
			this.unselectRows(table, index);
		}
	}
	
	// unselect all rows other than the one specified.
	function unselectRows(table, index) {
		var i;
		var rows;
		var hidden;
		for (i = 1; i < table.rows.length; i++) {
			if (i!=index) {
				hidden = this.getDatagridRowStateField(i);
				if (hidden.value=="selected") {
					row = table.rows[i];
					if (i % 2) {
						row.className = this.styleClass;
					} else {
						row.className = this.styleClass2;
					}
					hidden.value = "";
				}
			}
		}
	}	
	
	function createStateElement(index, value) {
		var hidden = document.createElement("INPUT");
		hidden.setAttribute('type', 'hidden');		
		hidden.setAttribute('name', this.property + ".dataState[" + (index) + "]");
		hidden.setAttribute('value', value);
		hidden.id = this.property + ".dataState[" + (index) + "]"; // ie bug :(
		return hidden;
	}	
	
	function addDatagridCell(row, index, property, styleClass, type, values) {
		var newCell = row.insertCell(row.cells.length);
		newCell.className = styleClass;
		
		if (type=="empty") {
			if (values!=null && values!="") {
				eval(values + "(newCell,index)");
			}
		} else {
			var inputElementName = type=="select"? "SELECT" : "INPUT";
			var input = document.createElement(inputElementName);
			if (type!="select") {
				input.setAttribute('type', type==null ? "text" : type);
			}
		    input.setAttribute('name', property + "[" + index + "]");
		    if (type=="checkbox") {	   
			    input.setAttribute('value', "true");
			} else if (type=="select") {
				for (i=0;i<values.length; i++) {
					var option = document.createElement("OPTION");
					option.setAttribute("value",  values[i].value);
					option.innerHTML = values[i].label;
					input.appendChild(option);
				}
			} else {
				input.setAttribute('value', "");
			}
		    newCell.appendChild(input);
		}
	    return newCell;
	}
		
	// return the lines having the specified state.
	function getDatagridLinesWithState(state) {
		var i = 0;
		var hidden = document.forms[0].elements[this.property + ".dataState[" + i + "]"];
		var array = new Array();
		while (hidden!=null) {
			if (hidden.value==state) {
				array[i] = true;
			}
			i++;
			hidden = document.forms[0].elements[this.property + ".dataState[" + i + "]"];
		}
		return array;
	}	
};

// add a line, PUBLIC
StrutsLayout.addDatagridLine = function(property) {				
	// Get the datagrid.
	var datagrid = strutsLayoutDatagridData[property];

	// Get the table object.
	var table = document.getElementById(datagrid.styleId);

	// Create a new line.
	var newRow = table.insertRow(table.rows.length);
	var odd = table.rows.length % 2;		
	newRow.className = !odd ? datagrid.styleClass : datagrid.styleClass2;
	if (datagrid.allowSelection) {
		newRow.onclick = new Function("strutsLayoutDatagridData['" + property + "'].selectDatagridLine(" + table.rows.length + ");");
		if (document.all) {
			// Does not work on Gecko
			newRow.style.cursor = "hand";
		} else {
			// Break on IE5.x
			newRow.style.cursor = "pointer";
		}
	}
	var newCell;
	for (i in datagrid.columns) {
		var column = datagrid.columns[i];
		var cellStyle = column.styleClass;
		var type = column.type;
		var values = column.values;
		newCell = datagrid.addDatagridCell(newRow, table.rows.length-2, property + "." + column.property, cellStyle, type, values);
	}		
	
	var hidden = datagrid.createStateElement(table.rows.length-2, "");
	table.parentNode.appendChild(hidden);		
};

// set the state of the selected lines.
StrutsLayout.setDatagridLineState = function(property, state) {
	// Get the datagrid.
	var datagrid = strutsLayoutDatagridData[property];

	// Get the table object.
	var table = document.getElementById(datagrid.styleId);
	
	// Get the selected items.
	var selectedLines = datagrid.getDatagridLinesWithState("selected");
	
	for (i in selectedLines) {
		// Set the state of the line to "removed".
		document.forms[0].elements[property + ".dataState[" + i + "]"].value = state;

		// Hide the line
		table.rows[parseInt(i)+1].className = datagrid.rowStyleClassMap[state];
	}
};
	