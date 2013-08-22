/**
 * struts-layout item swapper javascript functions
 */

/**
 * Remove empty options from a select object.
 */
function BumpUp(box)  {
	for(var i=0; i<box.options.length; i++) {
		box.options[i].selected = true;
		if(box.options[i].value == "")  {
			for(var j=i; j<box.options.length-1; j++)  {
				box.options[j].value = box.options[j+1].value;
				box.options[j].text = box.options[j+1].text;				
				box.options[j].selected = true;				
			}
			box.options.length -= 1;
			i--;
		}
	}
}

/**
 * When swapping lines between two tables, select and unselect the given tr.
 */
function prepareSwap(line, aClass) {
	if (line.selectedForSwap && line.selectedForSwap==true) {
		line.selectedForSwap = false;
	} else {
		line.selectedForSwap = true;
	}

	var oTD = line.getElementsByTagName("TD");
	for (i=0;i<oTD.length;i++) {
		var className = oTD[i].className;
		var oldClassName = oTD[i].oldClassName ? oTD[i].oldClassName : aClass;
		
		oTD[i].className = oldClassName;
		oTD[i].oldClassName = className;	
	}
}

/**
 * Move the selected rows from the sSelectFrom table to the sSelectTo table.
 */
function swap(sSelectFrom, from, sSelectTo, to) {
	var selectFrom = document.getElementById(sSelectFrom).getElementsByTagName("TBODY").item(0);
	var selectTo = document.getElementById(sSelectTo).getElementsByTagName("TBODY").item(0); 
	var oTRsFrom = selectFrom.getElementsByTagName("TR");
	var aSelectedTRs = new Array();
	var h=0;
	
	// look for selected lines.
	for (var y=0;y<oTRsFrom.length;y++) {
		thisTR = oTRsFrom.item(y);
		if (thisTR.selectedForSwap && thisTR.selectedForSwap==true) {
  			aSelectedTRs[h]=y;
			h++;
		}
	}
	
	// move selected lines.
	for (var y=aSelectedTRs.length-1;y>=0;y--) {
		thisTR = oTRsFrom.item(aSelectedTRs[y]);
		if (thisTR.selectedForSwap && thisTR.selectedForSwap==true) {
			// move the line.
			selectTo.insertBefore(thisTR,selectTo.getElementsByTagName("TR").item(1));
			
			// move the option on the hidden selects.
			var nameAZ = aSelectedTRs[y]-1;
			var selectAZ = document.getElementById(from);
			
			var selectToAZ = document.getElementById(to);
			for (var zz=selectToAZ.options.length; zz>0; zz--) {
				selectToAZ.options[zz] = new Option();
				selectToAZ.options[zz].value = selectToAZ.options[zz-1].value;
				selectToAZ.options[zz].text = selectToAZ.options[zz-1].text;				
				selectToAZ.options[zz].selected = true;
			}
			selectToAZ.options[0] = new Option();
			selectToAZ.options[0].value = selectAZ.options[nameAZ].value;
			selectToAZ.options[0].text = selectAZ.options[nameAZ].text;			
			selectToAZ.options[0].selected = true;
			
			selectAZ.options[nameAZ].value = "";
			selectAZ.options[nameAZ].text = "";
			
			// deselect the line.
			prepareSwap(thisTR);
		}
	}
	BumpUp(document.getElementById(from));
}