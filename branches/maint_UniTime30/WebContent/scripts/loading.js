/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org
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

	<!-- Hide script from old browsers

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

    // -->
    