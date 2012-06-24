function findDiv(tagName) {
	var p = document.getElementById(tagName);
	var elementLayer;
	while (elementLayer==null) {
		if (p.tagName.toUpperCase()=="DIV") {
			elementLayer = p;
		} else {
			p = p.parentNode;
		}
	}
	return elementLayer;
}

function initScrollbar(collectionTableId, collectionDivId) {
	var element = document.getElementById(collectionTableId);
	if (element==null) {
		// id was not specified ?
		return;
	}
	
	var layer = document.getElementById(collectionDivId);
	if (layer==null) {
		// id was not specified ?
		return;
	}
	
	// compute the position of the table.
	var x = 0;
	var y = 0;
	var p = element;
	while (p!=null && p.tagName.toUpperCase()!="BODY") {
		x += p.offsetLeft;
		y += p.offsetTop;
		p = p.offsetParent;
	}
	
	
	// find the div the table is in.
	p = element;
	var elementLayer = findDiv(collectionTableId);

	// ok, set the header div position
	layer.style.left = x;
	layer.style.top = y;
			
	// copy the table in it.
	var copy = element.cloneNode(true);
	layer.appendChild(copy);
	
	// don't show what is hidden
	layer.style.overflow = "hidden";
	
	// fix its size
	layer.style.width = elementLayer.offsetWidth -  16; //element.offsetWidth;
	layer.style.height = element.getElementsByTagName("tr")[0].offsetHeight + 4;	
	
		
	// create horizontal scroll synchronize handler.
	var scrollX = function() {
		layer.scrollLeft = elementLayer.scrollLeft;
	};
	
	//new Function("document.getElementById('" + collectionDivId + "').scrollLeft = findDiv('" + collectionTableId + "').scrollLeft;");

	// register synchronization handler.
	elementLayer.onscroll = scrollX;
}

function addLoadEvent(func) {
  var oldonload = window.onload;
  if (typeof window.onload != 'function') {
    window.onload = func;
  } else {
    window.onload = function() {
      oldonload();
      func();
    };
  }
}