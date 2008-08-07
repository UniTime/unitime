/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

// Select.js

// To use include the following in the <HEAD> of the HTML document:
//   <script language="JavaScript" src="Select.js"></script>

// Each <SELECT> that wishes to use this must include:
//    onactivate="setUp();"
//    onkeypress="return selectSearch(event, this);"
//    onkeydown="return checkKey(event, this);"


var matchString = "";
var wasModified = false;

function selectSearch(event, field) {
	var code = -1;
	
	if (event.keyCode) code = event.keyCode;
	else if (event.which) code = event.which;
	
	//displayStatus();

	// KeyCodes: 9=tab, 13=Enter, 46=Delete
	if (code == 9 || code == 13 || code == 46) {
		matchString = "";
		return true;
	}

	if(code > 31 && code < 127) {
		if ( (code<=33) || (code >=38 && code<=127) ) {
			keyVal = String.fromCharCode(code);
			matchString = matchString + keyVal;
			wasModified = true;
		}
		else {
			wasModified = true;
			return true;
		}
	}
	
	displayStatus();
	
	for (i = 0; i < field.options.length; i++) {
		selectText = field.options[i].text.toLowerCase();
		if (selectText.substr(0, matchString.length) == matchString.toLowerCase()) {
			field.options[i].selected = true;
			return false;
		}
	}
	
	return true;
}

function selectSearchTime(event, field) {
	var code = -1;
	
	if (event.keyCode) code = event.keyCode;
	else if (event.which) code = event.which;
	
	//displayStatus();

	// KeyCodes: 9=tab, 13=Enter, 46=Delete
	if (code == 9 || code == 13 || code == 46) {
		matchString = "";
		return true;
	}

	if(code > 31 && code < 127) {
		if ( (code<=33) || (code >=38 && code<=127) ) {
			keyVal = String.fromCharCode(code);
			matchString = matchString + keyVal;
			wasModified = true;
		}
		else {
			wasModified = true;
			return true;
		}
	}
	
	displayStatus();
	
	var pat1 = new RegExp("\\d?\\d","g");
	var pat2 = new RegExp("[ap]","g");

	var hour = pat1.exec(matchString);
	var am = pat2.exec(matchString);
	var min = pat1.exec(matchString);
	
	if (!hour) hour = 12;
	if (!am) am = (hour<7?'p':'a'); 
	if (!min) min = 0;
	if (hour>23) { min = (''+hour).substr(1,1) + min; hour = (''+hour).substr(0,1); }
	if (hour>12) { hour -= 12; am = 'p'; }
	
	var matchTime = hour+':'+((''+min).length<2?'0':'')+min+' '+am+'m';
	
	for (i = 0; i < field.options.length; i++) {
		if (field.options[i].text == matchTime) {
			field.options[i].selected = true;
			return false;
		}
	}
	
	return true;
}

function checkKey(event, field) {

	var code = -1;
	
	if (event.keyCode) code = event.keyCode;
	else if (event.which) code = event.which;
	
	// KeyCodes: 8=BackSpace, 46=Delete, 9==Tab
	if(code == 8) {
		if(matchString.length >= 1) {
			matchString = matchString.substr(0, (matchString.length - 1));
			wasModified = true;
		}
		return selectSearch(event, field);
	}
	
	if (code == 46) {
		matchString = "";
		wasModified = true;
		displayStatus();
		return false;
	}

	if (code == 9) {
		matchString = "";
		return true;
	}
}

function setUp() {

	matchString = "";
	wasModified = false;
}

function isModified() {

	return wasModified;
}

function displayStatus() {
	window.status = 'Searching for: "' + matchString.toLowerCase() + '" ( use BACKSPACE key to modify, DELETE key to clear the search string)';
}
