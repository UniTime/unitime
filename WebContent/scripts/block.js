/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/

function blToggle(divName) {
	if (blIsColapsed(divName)) {
		document.writeln("<img onmouseover=\"this.style.cursor='hand';\" src='images/expand_node_btn.gif' border='0' onclick=\"blOnClick(this,'"+divName+"');\" >");
	} else {
		document.writeln("<img onmouseover=\"this.style.cursor='hand';\" src='images/collapse_node_btn.gif' border='0' onclick=\"blOnClick(this,'"+divName+"');\" >");
	}
}

function blToggleHeader(name, divName) {
	document.writeln('<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">');
	document.writeln('<TR><TD colspan="2"><DIV class="WelcomeRowHead">');
	blToggle(divName);
	document.writeln(name);
	document.writeln('</DIV></TD></TR></TABLE>');
}

function blStart(divName) {
	document.writeln("<DIV id='"+divName+"' style='display:"+(blIsColapsed(divName)?"none":"block")+";'>");
}

function blEnd(divName) {
	document.writeln("</DIV>");
}

function blStartCollapsed(divName) {
	document.writeln("<DIV id='"+divName+"Col' style='display:"+(blIsColapsed(divName)?"block":"none")+";'>");
}

function blEndCollapsed(divName) {
	document.writeln("</DIV>");
}

function blSet(divName, colapsed) {
  	expires = new Date();
  	expires.setDate(expires.getDate()+30);
	document.cookie = "bl_"+divName+"="+(colapsed?"1":"0")+
		"; expires="+expires.toGMTString();
//		"; domain=smas.purdue.edu";
}

function blGet(divName) {
	var dc = document.cookie;
	var prefix = "bl_"+divName+"=";
	var begin = dc.indexOf("; "+prefix);
	if (begin == -1) {
		begin = dc.indexOf(prefix);
		if (begin != 0) return '1'; //default is colapsed
	} else begin += 2;
	var end = dc.indexOf(";", begin);
	if (end == -1) end = dc.length;
	return dc.substring(begin + prefix.length, end);
}

function blOnClick(source, divName) {
	expand = (source.src.indexOf('expand')>=0);
	if (expand) {
		source.src='images/collapse_node_btn.gif';
		document.getElementById(divName).style.display='block';
		if (document.getElementById(divName+'Col')!=null)
			document.getElementById(divName+'Col').style.display='none';
	} else {
		source.src='images/expand_node_btn.gif';
		document.getElementById(divName).style.display='none';
		if (document.getElementById(divName+'Col')!=null)
			document.getElementById(divName+'Col').style.display='block';
	}
	blSet(divName,!expand);
}

function blIsColapsed(divName) {
	return ('1'==blGet(divName));
}

