
// ***************************************************************
//
//		MAIN FUNCTION
//
// ***************************************************************

function updateDropDownList(in_origField, in_formName, targetFieldId, actionURL){	
	
	// Request initialization	
	if ( window.XMLHttpRequest ) {
		req = new XMLHttpRequest();	
    } else if ( window.ActiveXObject ) {
		req = new ActiveXObject( "Microsoft.XMLHTTP" );		
	}
			
	// Listener creation 
	req.onreadystatechange = function () {			    
	if (req.readyState == 4) {     	 
			if (req.status == 200) { 		
				// Response				
			var xmlDoc = req.responseXML.documentElement;
			    	  
   		var xSel = xmlDoc.getElementsByTagName('selectElement')[0];   	
		var strFormName = xSel.childNodes[0].firstChild.nodeValue; // form name
		var strElementName = xSel.childNodes[1].firstChild.nodeValue; // name of the field to fill in
		var objDDL = document.forms[strFormName].elements[strElementName];
		objDDL.options.length =0;	
		// iterates on the elements used to fill in the list linked to the combo
		var xRows = xmlDoc.getElementsByTagName('entry');	
		for (i=0; i< xRows.length; i++) {		  
		 var theText = xRows[i].childNodes[0].firstChild.nodeValue;
		 var theValue  = xRows[i].childNodes[1].firstChild.nodeValue;		
		 // creates an option to add to the combo
		var option = new Option(theText, theValue);					  
		try{
			objDDL.add(option, null);					
		  }catch(e) {
			objDDL.add(option, -1);
		  }					  
		}			
	    }
	   }
	};
	
	// Request construction	
	var strValue = in_origField.options[in_origField.selectedIndex].value;
	var strParams = 'valueSelected='+ escape(strValue)+'&formName=' + escape(in_formName)+ '&elementName='+escape(targetFieldId);
		
	req.open( "POST", actionURL, true );
	req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	req.setRequestHeader("Content-Length", strParams.length);
		
	// Request send		 
	 setTimeout("try { req.send('" + strParams + "') } catch(e) {}", 0);
	
}


