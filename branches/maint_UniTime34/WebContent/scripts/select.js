/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/

// Select.js

// To use include the following in the <HEAD> of the HTML document:
//   <script language="JavaScript" src="Select.js"></script>

// Each <SELECT> that wishes to use this must include:
//    onactivate="setUp();"
//    onkeypress="return selectSearch(event, this);"
//    onkeydown="return checkKey(event, this);"


function selectSearch(event, field) {
}

function selectSearchTime(event, field) {
}

function checkKey(event, field) {
}

function setUp() {
}

function isModified() {
	return false;
}

function displayStatus() {
}

function changeWidth(elementId, width){
	if (/MSIE (\d+\.\d+);/.test(navigator.userAgent)){
		document.getElementById(elementId).style.width = width;
	}
}