function openStrutsLayoutPopup(styleId) {
	var divElement = document.getElementById(styleId);
	var size = getStrutsLayoutPopupWindowSize();
	
	divElement.style.left = (size[0]/2 - divElement.clientWidth/2) + 'px';
	divElement.style.top = (size[1]/2 - divElement.clientHeight/2) + 'px';
	divElement.style.visibility = "visible";
	
	// Lock controls.
     document.getElementById("slpdiv").style.display = "block";
     document.getElementById("slpdiv").style.width = size[0];
     document.getElementById("slpdiv").style.height = size[1];
     document.getElementById("slpdiv").style.backgroundColor = "gray";
     document.getElementById("slpdiv").style.opacity=0.3;
     document.getElementById("slpdiv").style.filter = "alpha(opacity=30)";
     
     // Hide IE select.
     if (document.all) {
     	var elements = document.all.tags("SELECT");
     	for (i = 0; i < elements.length;i++) {
	     	elements[i].statusVisibility = elements[i].style.visibility;
	     	if (!isLayoutPopupPart(elements[i], divElement)) {
	     		elements[i].style.visibility = "hidden";
	     	}
     	}
     	
     }
}

function closeStrutsLayoutPopup(styleId) {
var divElement = document.getElementById(styleId);
	// Unlock controls.
     document.getElementById("slpdiv").style.display = "none";
     
     // Show IE select.
     if (document.all) {
     	var elements = document.all.tags("SELECT");
     	for (i = 0; i < elements.length;i++) {
     		elements[i].style.visibility = elements[i].statusVisibility;
     	}
     	
     }

	divElement.style.visibility = "hidden";
}

function startStrutsLayoutPopupMove(titleElement, e) {
	 if(!e) e=window.event;
	 	 
     // Store the position diff.
     var divElement = titleElement;
     while (divElement.nodeName.toUpperCase()!="DIV") {
     	divElement = divElement.parentNode;
     }
     divElement.ty = e.clientY -parseInt(divElement.style.top);
     divElement.tx = e.clientX -parseInt(divElement.style.left);
     
     // Update the cursoe.
     divElement.tw = divElement.style.cursor;
     titleElement.style.cursor = "move";
     
     // Prepare for move
     divElement.tz = document.onmousemove;
     document.dd = divElement;
     document.onmousemove = moveStrutsLayoutPopup;         
}

function moveStrutsLayoutPopup(e){
     if(!e) e=window.event;
         
     // compute new position.
     var divElement = document.dd;
     if (divElement!=null) {
	     doMoveLayoutPopup(divElement, e);
	 }
}    

function stopStrutsLayoutPopupMove(titleElement, e){
     if(!e) e=window.event;
     // stop.
     var divElement = titleElement;
     while (divElement.nodeName.toUpperCase()!="DIV") {
     	divElement = divElement.parentNode;
     }
     document.dd = null;
     document.onmousemove = divElement.tz;
     
     titleElement.style.cursor = divElement.tw;    
}    

function isLayoutPopupPart(childElement, mainElement) {
	if (childElement==null) {
		return false;
	} else if (childElement.parentNode == mainElement) {
		return true;
	} else {
		return isLayoutPopupPart(childElement.parentNode, mainElement);
	}
}

function doMoveLayoutPopup(divElement, e) {	
     var top = e.clientY - divElement.ty;
     var left = e.clientX - divElement.tx;
     
     // set new position
     divElement.style.top=top + 'px';
     divElement.style.left=left + 'px';
}

function getStrutsLayoutPopupWindowSize() {
	var myWidth = 0, myHeight = 0;
  	if( typeof( window.innerWidth ) == 'number' ) {
	    //Non-IE
	    myWidth = window.innerWidth;
	    myHeight = window.innerHeight;
  	} else if( document.documentElement && ( document.documentElement.clientWidth || document.documentElement.clientHeight ) ) {
	    //IE 6+ in 'standards compliant mode'
	    myWidth = document.documentElement.clientWidth;
	    myHeight = document.documentElement.clientHeight;
	} else if( document.body && ( document.body.clientWidth || document.body.clientHeight ) ) {
	    //IE 4 compatible
	    myWidth = document.body.clientWidth;
	    myHeight = document.body.clientHeight;
	}
	return [myWidth, myHeight];  
}