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

	// <!-- Hide script from old browsers

	function getBrowser() {
		var browser = "";  
		if (navigator.userAgent.indexOf("Opera")!=-1 && document.getElementById) browser="OP"; 
		if (document.all) browser="IE"; 
		if (document.layers) browser="NN"; 
		if (!document.all && document.getElementById) browser="MO"; 
		return browser;
	}
	      
	function displayLoading() {
		var browser = getBrowser();  
	  
	    // Microsoft Internet Explorer
	    if ( browser =="IE" && document.all.loading) {
	      document.all.loading.style.visibility="visible";
	      document.all.loading.style.display="block";
	    }
	    
	    // Netscape Navigator 4-
	    if ( browser =="NN" && document.loading) {
	      document.loading.visibility="visible";
	      document.loading.display="block";
	    }
	    
	    // Mozilla / Netscape Navigator 4+
	   	if ( browser =="MO" || browser =="OP" ) {
	      	loadingId = document.getElementById("loading");
	      	if (loadingId) {
		      	loadingId.style.visibility="visible";
		      	loadingId.style.display="block";
		    }
	    }
	}
      
    /**
     * Toggles display of a block (usually enclosed within DIV tags)
     * elementId - id of the block
     * display - true / false 
     */  
	function displayElement(elementId, display) {
		var browser = getBrowser();  
		var visible = "visible";
		var block = "block";
	  
	  	if(!display) {
	  		visible = "hidden";
	  		block = "none";
	  	}
	  	
	    // Microsoft Internet Explorer
	    if ( browser =="IE" ) {
	      loadingId = document.getElementById(elementId);
	      if (loadingId) {
	          loadingId.style.visibility=visible;
	          loadingId.style.display=block;
		  }
	    }
	    
	    // Netscape Navigator 4-
	    if ( browser =="NN" && document.elementId) {
	      document.elementId.visibility=visible;
	      document.elementId.display=block;
	    }
	    
	    // Mozilla / Netscape Navigator 4+
	   	if ( browser =="MO" || browser =="OP" ) {
	      	loadingId = document.getElementById(elementId);
   			if (loadingId) {
   			    loadingId.style.visibility=visible;
	    	  	loadingId.style.display=block;
   			}
	    }
	}
	
	function isWideTable(element) {
		if (element) {
			if ("unitime-Page" == element.id)
				return true;
			if ("TABLE" == element.tagName)
				return false;
			return isWideTable(element.parentNode);
		}
		return false;
	}
	
	function resizeWideTables() {
		var clientWidth = 12 + (document.compatMode=='CSS1Compat'? document.documentElement.clientWidth : document.body.clientWidth);
		var scrollWidth = (document.compatMode=='CSS1Compat'? document.documentElement.scrollWidth : document.body.scrollWidth);
		var mainTable = document.getElementById("unitime-Page");
		var header = document.getElementById("unitime-Header");
		var footer = document.getElementById("unitime-Footer");
		if (mainTable && clientWidth < scrollWidth) {
			var border = 10 + scrollWidth - mainTable.clientWidth;
			var newWidth = (clientWidth - border) + "px";
			var tables = mainTable.getElementsByTagName("table");
			for (var i = 0; i < tables.length; i++)
				if ("100%" == tables[i].width && isWideTable(tables[i].parentNode)) // && tables[i].clientWidth > clientWidth - border)
					tables[i].style.width = newWidth;
			if (header)
				header.style.width = newWidth;
			if (footer)
				footer.style.width = newWidth;
		}
	}
	
	window.onresize = resizeWideTables;
	window.onload = resizeWideTables;
	
    // -->
    
