/**
 * struts-layout core javascript
 *
 * All rights reserved.
 */
 
// Dynamic result update
// for mathDataCollection on CollectionInputTab

function roundNum (number,X){
	return Math.round(number*Math.pow(10,X))/Math.pow(10,X);
}

function mathDataUpdate (operation, resultId, tableId, columnId, rowsNumber){
	var result = document.getElementById(resultId);
	newResult = 0.0;
	if  (operation=="sum"){
		for (i=0; i<rowsNumber; i++) {
			var cel = document.getElementById("mathData_t" + tableId + "l" + i +"c" + columnId);
			if (isNaN(parseFloat(cel.value))){
				alert("\"" + cel.value + "\"" + " is not a number");
				return false;
			}		
			newResult = parseFloat(newResult) + parseFloat(cel.value);
		}
	} else if  (operation=="max"){
		for (i=0; i<rowsNumber; i++) {
			var cel = document.getElementById("mathData_t" + tableId + "l" + i +"c" + columnId);
			if (isNaN(parseFloat(cel.value))){
				alert("\"" + cel.value + "\"" + " is not a number");
				return false;
			}
			if (i==0){
				newResult = cel.value;
			}
			if (newResult < cel.value){
				newResult = cel.value;
			}		
		}
	} else if  (operation=="min"){
		for (i=0; i<rowsNumber; i++) {
			var cel = document.getElementById("mathData_t" + tableId + "l" + i +"c" + columnId);
			if (isNaN(parseFloat(cel.value))){
				alert("\"" + cel.value + "\"" + " is not a number");
				return false;
			}
			if (i==0){
				newResult = cel.value;
			}
			if (newResult > cel.value){
				newResult = cel.value;
			}		
		}
	} else {
		alert("Sorry !!. \n\"" + operation + "\" operation is not supported yet. ");
	}
	result.innerHTML = roundNum(newResult,2);
}

// type checking functions

function checkValue(field, property, type, required) {
	if (document.images[property + "required"]!=null) {
		if (field.value!="") {		
			document.images[property + "required"].src= imgsrc + "clearpixel.gif";
			if (type=="NUMBER" && !isNumber(field.value)) document.images[property + "required"].src= imgsrc + "ast.gif";
			if (type=="DATE" && !isDate(field.value)) document.images[property + "required"].src = imgsrc + "ast.gif";
			if (type=="EMAIL" && !isEmail(field.value)) document.images[property + "required"].src= imgsrc + "ast.gif";		
		} else {	
			if (required) document.images[property + "required"].src= imgsrc + "ast.gif";
		}
	}
}

// Return true if value is an e-mail address
function isEmail(value) {
	invalidChars = " /:,;";
	if (value=="") return false;
	
	for (i=0; i<invalidChars.length;i++) {
	   badChar = invalidChars.charAt(i);
	   if (value.indexOf(badChar,0) != -1) return false;
	}
	
	atPos = value.indexOf("@", 1);
	if (atPos == -1) return false;
	if (value.indexOf("@", atPos + 1) != -1) return false;
	
	periodPos = value.indexOf(".", atPos);
	if (periodPos == -1) return false;
	
	if (periodPos+3 > value.length) return false;

	return true;
}



// Return true if value is a number
function isNumber(value) {
	if (value=="") return false;

	var d = parseInt(value);
	if (!isNaN(d)) return true; else return false;		

}

// return true if value is a date
// ie in the format XX/YY/ZZ where XX YY and ZZ are numbers
function isDate(value) {
	if (value=="") return false;
	
	var pos = value.indexOf("/");
	if (pos == -1) return false;
	var d = parseInt(value.substring(0,pos));
	value = value.substring(pos+1, 999);
	pos = value.indexOf("/");
	if (pos==-1) return false;
	var m = parseInt(value.substring(0,pos));
	value = value.substring(pos+1, 999);
	var y = parseInt(value);	
	if (isNaN(d)) return false;	
	if (isNaN(m)) return false;	
	if (isNaN(y)) return false;	
	
	var type=navigator.appName;
	if (type=="Netscape") var lang = navigator.language;
	else var lang = navigator.userLanguage;
	lang = lang.substr(0,2);

	if (lang == "fr") var date = new Date(y, m-1, d);
	else var date = new Date(d, m-1, y);
	if (isNaN(date)) return false;	
	return true;
 }
 
/**
 * Check if a number is being type in.
 */
function checkNumber(el, e) {
	// Get the key.
	var key = 0;
	if (e.charCode) {
		key = e.charCode;
	} else if (e.keyCode) {
		key = e.keyCode;
	}
	
	// Check key.
	if (key>=48 && key<=57) {
		// Digit : ok
		return true;
	} else if (checkSpecialKey(el, e)) {
		// Special letter : don't block.
		return true;
	}
	
	// Block everything else.
	return false;
}

/**
 *
 */
function checkSpecialKey(element, e) {
	// Get key code.
	var key = e.keyCode;
	switch (key) {
		case 8:
		case 9:
		case 35:
		case 16:
		case 36:
		case 37:
		case 38:
		case 39:
		case 40:
		case 144:
		case 145:
		case 17:
		case 18:
		case 19:
		case 20:
		case 45:
		case 46:
		case 91:
		case 92:
	return true;
		default:
			return false;
	}
 }

/**
 * Check if the current field is fill.
 * If yes, move focus to the next field.
 */ 
function checkAutoSkip(el, e){
	// keyCode :  8 backtab, 9 tab, 35 end, 16 shift
	//             35 to 40 cursor , 144 verrnum ,145 defil
	// 17 ctrl,18 alt,19 attn,20 capslock,36 home,45  ins,46 del, 91/92 fenetw,93 menuw
	
	// Get key code.
	var key = e.keyCode;
	
   if ((el.tagName=="INPUT" || el.tagName=="TEXTAREA")
       && !(key >=16  && key <=20)
       && !(key >=35  && key <=40)
       && !(key >=91  && key <=93)
       && key !=  8 && key !=  9
       && key != 45 && key != 46
       && key !=144 && key !=145){

      if (el.tagName=="INPUT") {
          var longueurChamp = el.maxLength ;
          if (longueurChamp == -1) {
              longueurChamp = 9999;
      	  }
          var taille = el.maxLength ;
      }
      if (el.tagName=="TEXTAREA") {
          var longueurChamp = el.rows * el.cols ;
          var taille =  el.rows * el.cols  ;
      }

      // Check if the user hit the maximum length
      if (el.value.length>= longueurChamp || el.value.length>= taille){
         var i;
        // Find the next control on the form.
         for(i=0; i < el.form.elements.length; i++){
            if (el==el.form.elements[i]){
               break;
            }
         }
         if (i != el.form.elements.length){
            for(j=i+1; j < el.form.elements.length; j++){

              if (el.form.elements[j].disabled == false && !el.form.elements[j].readOnly) {
				  // look for the next editable field.
                 if (el.form.elements[j].type=="text" || el.form.elements[j].type=="password" || el.form.elements[j].type=="checkbox" || el.form.elements[j].type=="CHECKBOX" || el.form.elements[j].tagName=="TEXTAREA" || el.form.elements[j].tagName=="select" || el.form.elements[j].tagName=="SELECT") {
         			el.form.elements[j].focus();
                    break;
                  }
               }
            }          
		 }
         else {
            // Go to the first control if at the last control
            i=0;
            el.form.elements[i].focus();
         }
      }
   }
}

// menu functions

function initMenu(menu) {
	if (getMenuCookie(menu)=="hide") {
		document.getElementById(menu).style.display="none";
	} else {
		document.getElementById(menu).style.display="";
	}
}

function changeMenu(menu) {
if (document.getElementById(menu).style.display=="none") {
	document.getElementById(menu).style.display="";
	element = document.getElementById(menu+"b");
	if (element != null) {
		document.getElementById(element).style.display="none";
	}
	setMenuCookie(menu,"show");
} else {
	document.getElementById(menu).style.display="none";
	element = document.getElementById(menu+"b");
	if (element != null) {	
		var width = document.getElementById(menu).offsetWidth;	
		if (navigator.vendor == ("Netscape6") || navigator.product == ("Gecko"))
			document.getElementById(menu+"b").style.width = width;	
		else 
			document.getElementById(menu+"b").width = width;
		document.getElementById(menu+"b").style.display="";
	}
	setMenuCookie(menu,"hide");
}
return false;
}

function changeDisplayState(elementId, imageId, visibleImageSrc, hiddenImageSrc) {
	var display;
	if (document.getElementById(elementId).style.display=="none") {
		document.getElementById(elementId).style.display="";
		display = true;
	} else {
		document.getElementById(elementId).style.display="none";
		display = false;
	}
	if (imageId) {
		document.getElementById(imageId).src = imgsrc + (display ? visibleImageSrc : hiddenImageSrc);
	}
}

function setMenuCookie(name, state) {	
	if (name.indexOf("treeView")!=-1) {
		if (state=="show") {
			var cookie = getMenuCookie("treeView", "");
			if (cookie=="???") cookie = "_";
			cookie = cookie + name + "_";
			document.cookie = "treeView=" + escape(cookie);

		} else {
			var cookie = getMenuCookie("treeView", "");
			var begin = cookie.indexOf("_" + name + "_");
			if (cookie.length > begin + name.length + 2) {
				cookie = cookie.substring(0, begin+1) + cookie.substring(begin + 2 + name.length);
			} else {
				cookie = cookie.substring(0, begin+1);
			}
			document.cookie = "treeView=" + escape(cookie);
		}
		return;
	} 
	if (name.indexOf("selectedTab")!=-1) {
		document.cookie = "selectedTab=" + escape(state) + getCookieContextPath();
	} else {
		var cookie = name + "STRUTSMENU=" + escape(state);
		document.cookie = cookie;	
	}
}

function getCookieContextPath() {
	if (window.contextPath) {
		return "; path=" + window.contextPath;
	} else {
		return "";
	}
}


function setTabCookie(name, value) {
	var cookie = getMenuCookie("selectedTab", "");	
	var start;
	var end;
	if (cookie=="undefined") cookie = "";
	if (cookie==null) cookie = "";
	if (cookie=="???") cookie = "";	
	start = cookie.indexOf(name + "=");
	if (start==-1) {
		cookie = cookie + name + "=" + value + ";";
	} else {
		end = cookie.substring(start).indexOf(";");
		cookie = cookie.substring(0, start) + name + "=" + value + cookie.substring(start+end);
	}
	setMenuCookie("selectedTab", cookie);
}

function getMenuCookie(name, suffix) {
	if (suffix==null) {
		suffix = "STRUTSMENU";
	}
	var prefix = name + suffix + "=";
	var cookieStartIndex = document.cookie.indexOf(prefix);
	if (cookieStartIndex == -1) return "???";
	var cookieEndIndex = document.cookie.indexOf(";", cookieStartIndex + prefix.length);
	if (cookieEndIndex == -1) cookieEndIndex = document.cookie.length;
	return unescape(document.cookie.substring(cookieStartIndex + prefix.length, cookieEndIndex));
}

// sort functions
function arrayCompare(e1,e2) {
	return e1[0] < e2[0] ? -1 : (e1[0] == e2[0] ? 0 : 1);

}

var tables = new Array();
function arraySort(tableName, column, lineNumber, columnNumber) {
	var aTable = tables[tableName];
	var arrayToSort;
	var array;
	var reverse = 0;
	if (aTable) {
		array = aTable[0];
		arrayToSort = new Array(lineNumber);
		for (i=0;i<lineNumber;i++) {
			arrayToSort[i] = new Array(2);
			arrayToSort[i][0] = array[i][column];
			arrayToSort[i][1] = i;				
		}
		reverse = 1 - aTable[1];
		aTable[1] = reverse;
	} else {
		array = new Array(lineNumber);
		arrayToSort = new Array(lineNumber);
		for (i=0;i<lineNumber;i++) {	
			array[i] = new Array(columnNumber);
			for (j=0;j<columnNumber;j++) {
				obj = document.getElementById("t" + tableName + "l" + (i+1) +"c" + j);		
				array[i][j] = obj.innerHTML;
			}
			array[i][columnNumber] = obj.parentNode.parentNode.onmouseover;
			array[i][columnNumber+1] = obj.parentNode.parentNode.onmouseout;			
			
			arrayToSort[i] = new Array(2);
			arrayToSort[i][0] = array[i][column];
			arrayToSort[i][1] = i;		
	
			aTable = new Array(2);
			aTable[0] = array;
			aTable[1] = 0;
			tables[tableName] = aTable;
		}
	}

	arrayToSort.sort(arrayCompare);
	if (reverse) {
		arrayToSort.reverse();
	}

	for (i=0;i<lineNumber;i++) {
		goodLine = arrayToSort[i][1];
		for (j=0;j<columnNumber;j++) {
			document.getElementById("t" + tableName + "l" + (i+1) +"c" + j).innerHTML = array[goodLine][j];
		}
		document.getElementById("t" + tableName + "l" + (i+1) +"c" + 0).parentNode.parentNode.onmouseover = array[goodLine][columnNumber];
		document.getElementById("t" + tableName + "l" + (i+1) +"c" + 0).parentNode.parentNode.onmouseout = array[goodLine][columnNumber+1];
	}
}

// calendar functions

var calformname;
var calformelement;
var calpattern;
var calweekstart;

/**
 * Static code included one time in the page.
 *
 * a {text-decoration: none; color: #000000;}");
 * TD.CALENDRIER {background-color: #C2C2C2; font-weight: bold; text-align: center; font-size: 10px; }");
 *
 * bgColor => #000000, #C9252C, 
 */
function printCalendar(day1, day2, day3, day4, day5, day6, day7, first, month1, month2, month3, month4, month5, month6, month7, month8, month9, month10, month11, month12, day, month, year) {
	document.write('<div id="caltitre" style="z-index:10;">');	
	document.write('<table cellpadding="0" cellspacing="0" border="0" width="267">');
//	document.write('<form>');
	document.write('<tr><td colspan="15" class="CALENDARBORDER"><img src="' + imgsrc + 'shim.gif" width=1 height=1></td></tr>');
	document.write('<tr>');
	document.write('	<td class="CALENDARBORDER" width="1"><img src="' + imgsrc + 'shim.gif" width=1 height=20></td>');
	document.write('	<td class="CALENDARTITLE" colspan="3" align="right"><img src="' + imgsrc + 'previous.gif" onclick="cal_before(' + day + ');"></td>');
	document.write('	<td colspan=7 align="center" class="CALENDARTITLE" nowrap>');
	
	// month
	document.write('<select id="calmois" name="calmois" onchange="cal_chg(' + day + ');"><option value=0>...</option>');	
	
	// use the good day for week start.
	// store the day the week start for later.
	calweekstart = first;	
	// compute an array of the days, starting from Sunday.
	caldays = new Array(7);
	caldays[0] = day1;
	caldays[1] = day2;
	caldays[2] = day3;
	caldays[3] = day4;
	caldays[4] = day5;
	caldays[5] = day6;
	caldays[6] = day7;
	// compute an array of the days, starting at the good day.
	computedcaldays = new Array(7);
	for (i=0; i<7; i++) {		
		computedcaldays[(i+1-calweekstart+7)%7] = caldays[i];
	}
			
	for(i=1;i<=12;i++) {
		var str='<option value=' + i + '>';
		monthIndex = i-1;
		switch (monthIndex) {
			case 0: str += month1; break;
			case 1: str += month2; break;
			case 2: str += month3; break;
			case 3: str += month4; break;
			case 4: str += month5; break;
			case 5: str += month6; break;
			case 6: str += month7; break;
			case 7: str += month8; break;
			case 8: str += month9; break;
			case 9: str += month10; break;
			case 10: str += month11; break;
			case 11: str += month12; break;
		}
		document.write(str);
	}	

	document.write('</select>');
	
	// year
	document.write('<select id="calyear" name="calyear" onchange="cal_chg('+ day + ');">');	
	document.write("</select>");
	
	document.write('	</td>');
	document.write('	<td class="CALENDARTITLE" align="left" colspan="3"><img src="' + imgsrc + 'next.gif" onclick="cal_after(' + day + ');">&nbsp;&nbsp;<img src="' + imgsrc + 'close.gif" onclick="hideCalendar()"></td>');
	document.write('	<td class="CALENDARBORDER" width=1><img src="' + imgsrc + 'shim.gif" width="1" height="1"></td>');
	document.write('</tr>');
	document.write('<tr><td colspan=15 class="CALENDARBORDER"><img src="' + imgsrc + 'shim.gif" width=1 height=1></td></tr>');
	document.write('<tr>');
	document.write('	<td class="CALENDARBORDER" width="1"><img src="' + imgsrc + 'shim.gif" width=1 height=1></td>');
	document.write('	<td class="CALENDRIER" width="38">' + computedcaldays[0] + '</td>');
	document.write('	<td class="CALENDRIER" width="1"><img src="' + imgsrc + 'shim.gif" width=1 height=1></td>');
	document.write('	<td class="CALENDRIER" width="38">' + computedcaldays[1] + '</td>');
	document.write('	<td class="CALENDRIER" width="1"><img src="' + imgsrc + 'shim.gif" width=1 height=1></td>');
	document.write('	<td class="CALENDRIER" width="38">' + computedcaldays[2] + '</td>');
	document.write('	<td class="CALENDRIER" width="1"><img src="' + imgsrc + 'shim.gif" width=1 height=1></td>');
	document.write('	<td class="CALENDRIER" width="38">' + computedcaldays[3] + '</td>');
	document.write('	<td class="CALENDRIER" width="1"><img src="' + imgsrc + 'shim.gif" width=1 height=1></td>');
	document.write('	<td class="CALENDRIER" width="38">' + computedcaldays[4] + '</td>');
	document.write('	<td class="CALENDRIER" width="1"><img src="' + imgsrc + 'shim.gif" width=1 height=1></td>');
	document.write('	<td class="CALENDRIER" width="38">' + computedcaldays[5] + '</td>');
	document.write('	<td class="CALENDRIER" width="1"><img src="' + imgsrc + 'shim.gif" width=1 height=1></td>');
	document.write('	<td class="CALENDRIER" width="38">' + computedcaldays[6] + '</td>');
	document.write('	<td class="CALENDARBORDER" width="1"><img src="' + imgsrc + 'shim.gif" width=1 height=1></td>');
	document.write('</tr>');
	document.write('<tr><td colspan=15 class="CALENDARBORDER"><img src="' + imgsrc + 'shim.gif" width=1 height=1></td></tr>');
//	document.write('</form>');
	document.write('</table>');
	document.write('</div>');
//	document.write('<div id="caljour" style="position:absolute; left:0px; top:45px; width:253; height:130; z-index:10;"></div>');
	document.write('<div id="caljour" style="z-index:10;"></div>');	
}

/**
 * Show the calendar
 */
function showCalendar(year, month, day, pattern, formName, formProperty, event, startYear, endYear) {
	if (document.forms[formName].elements[formProperty].disabled) {
			return;
	}
	if (startYear!=null) {
		var calyear = document.getElementById("calyear");
		for (i = startYear; i <= endYear; i++) {			
			calyear.options[i - startYear] = new Option(i,i);
		}
		calyear.options.length = endYear - startYear + 1;
	}

	// Update the calendar.
	if (document.layers) {
		document.slcalcod.document.caltitre.document.forms[0].calmois.selectedIndex=month;
	} else if (document.all) {
		document.all.calmois.selectedIndex= month;
	} else {
		document.getElementById("calmois").selectedIndex=month;
	}
	if (document.forms[formName].elements[formProperty].stlayout) {
		var lc_day = document.forms[formName].elements[formProperty].stlayout.day;
		var lc_month = document.forms[formName].elements[formProperty].stlayout.month;
		var lc_year = parseInt(document.forms[formName].elements[formProperty].stlayout.year);
		cal_chg(lc_day, lc_month, lc_year);	
	} else {
		cal_chg(day, month, year);	
	}

	if(document.all) {
		// IE.
		var position = cal_place(event);
		document.all.slcalcod.style.left = position[0];
		document.all.slcalcod.style.top = position[1];
		document.all.slcalcod.style.visibility="visible";
	} else if(document.layers) {
		// Netspace 4
		document.slcalcod.left = e.pageX+10;
		document.slcalcod.top = e.pageY+10;
		document.slcalcod.visibility="visible";
	} else {
		// Mozilla
		var calendrier = document.getElementById("slcalcod");
		var position = cal_place(event);
		calendrier.style.left = position[0];
		calendrier.style.top = position[1];				
		calendrier.style.visibility="visible";
	}	
	if (document.all) {
		hideElement("SELECT");
	}
	calformname = formName;
	calformelement = formProperty;
	calpattern = pattern;
}

/**
 * Compute the size of the window.
 */
function cal_window_size() {
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

/**
 * Compute where the calendar popup should be placed
 */
function cal_place(event) {
	var calendrier = document.getElementById("slcalcod");
	var ofy=document.body.scrollTop;
	var ofx=document.body.scrollLeft;
	var size = cal_window_size();

	var endX = calendrier.clientWidth + event.clientX + ofx + 10;	
	var endY = calendrier.clientHeight + event.clientY + ofy + 10;
	
	var calX;
	var calY;

	if (endX>size[0]) {
		calX = event.clientX + ofx - 10 - calendrier.clientWidth;
	} else {
		calX = event.clientX + ofx + 10;
	}
	
	if (endY>size[1]) {
		calY = event.clientY + ofy - 10 - calendrier.clientHeight;
	} else {
		calY = event.clientY + ofy + 10;
	}
	
	return [calX, calY];
}

/**
 * Redraw the calendar for the current date and a selected month
 */
function cal_chg(day, month, year){
	var str='',j;	
	
	champMonth = document.getElementById("calmois");
	if (month==null) {		
		month = champMonth.options[champMonth.selectedIndex].value;
	} else {
		champMonth.selectedIndex = month;
	}
		
	
	champYear = document.getElementById("calyear");
	if (year==null) {		
		year = champYear.options[champYear.selectedIndex].value;
	} else {
		index = year - champYear.options[0].value;
		if (index >= 0 && index < champYear.options.length) {
			champYear.selectedIndex = index;
		} else {
			// the initial year is not in the calendar allowed years.
			year = champYear.options[0].value;
		}
	}
	
	
	if(month>0) {
	
		j=1;
		weekEnd1Pos = (1 - calweekstart + 7) % 7;
		weekEnd2Pos = (7 - calweekstart + 7) % 7;
				
		str+='<table cellpadding=0 cellspacing=0 border=0 width=267>\n';
		for(u=0;u<6;u++){
			str+='	<tr>\n';
			for(i=0;i<7;i++){
				ldt=new Date(year,month-1,j);				
				str+='		<td class="CALENDARBORDER" width=1><img src="' + imgsrc + 'shim.gif" width=1 height=20></td>\n';
				
				str+='		<td class="CALENDAR'; 
				if((ldt.getDay()+1-calweekstart+7)%7==i && ldt.getDate()==j && j==day /*&& newMonth==month && lc_annee==year*/) {
					str+='SELECTED'; 
				} else if(i==weekEnd1Pos || i==weekEnd2Pos) {
					str+='WEEKEND'; 
				} else {
					str+='WEEK'; 
				}
				str+='" width="38" align="center">';
				if ((ldt.getDay()+1-calweekstart+7)%7==i && ldt.getDate()==j) {
					str+='<a class="CALENDRIER" href="javascript://" class="CALENDRIER" onmousedown="dtemaj(\'' + j + '\',\'' + month + '\',\'' + year +'\');">'+j+'</a>'; 
					j++;
				} else {
					str+='&nbsp;';
				}
				str+='</td>\n';
			}
			str+='		<td class="CALENDARBORDER" width=1><img src="' + imgsrc + 'shim.gif" width=1 height=1></td>\n';
			str+='	</tr>\n';
			str+='	<tr><td colspan=15 class="CALENDARBORDER"><img src="' + imgsrc + 'shim.gif" width=1 height=1></td></tr>\n';
		}
		str+='</table>\n';
	
	}
	
	if(document.all) {
		document.all.caljour.innerHTML=str;
	}
	if(document.layers) {
		obj=document.calendrier.document.caljour; 
		obj.top=48; 
		obj.document.write(str); 
		obj.document.close();
	}
	if (!document.all && document.getElementById) {
		document.getElementById("caljour").innerHTML = str;
	}
}

/**
 * Display the previous month
 */
function cal_before(day, month, year) {
	var champMonth, champYear;
	champMonth = document.getElementById("calmois");
	champYear = document.getElementById("calyear");
			
	if (champMonth.selectedIndex>1) { 
		champMonth.selectedIndex--;
	} else if (champYear.selectedIndex>0) {
		champYear.selectedIndex--;
		champMonth.selectedIndex = champMonth.options.length - 1;
	}
	cal_chg(day, champMonth.options[champMonth.selectedIndex].value, champYear.options[champYear.selectedIndex].value);
}

/**
 * Display the next month
 */
function cal_after(day, month, year) {
	// get required objects
	var champMonth, champYear;
	champMonth = document.getElementById("calmois");
	champYear = document.getElementById("calyear");
	if (champMonth.selectedIndex < champMonth.options.length - 1) {
		champMonth.selectedIndex++;
	} else if (champYear.selectedIndex < champYear.options.length - 1) {
		champYear.selectedIndex++;	
		champMonth.selectedIndex = 1;
	}
	cal_chg(day, champMonth.options[champMonth.selectedIndex].value, champYear.options[champYear.selectedIndex].value);
}

/**
 * Update the date in the input field and hide the calendar.
 * PENDING: find a way to make the format customable.
 */
function dtemaj(jour, mois, annee){
	document.forms[calformname].elements[calformelement].value = formatDate(jour, mois, annee);
	document.forms[calformname].elements[calformelement].stlayout = new Object();
	document.forms[calformname].elements[calformelement].stlayout.day = jour;
	document.forms[calformname].elements[calformelement].stlayout.month = mois;
	document.forms[calformname].elements[calformelement].stlayout.year = annee;
	hideCalendar();
	if (document.forms[calformname].elements[calformelement].onchange) {
		document.forms[calformname].elements[calformelement].onchange();
	}
}

function formatDate(day, month, year) {
	var date = "";
	var pos = 0;
	var pattern;
	var previousPattern;
	var patternLength = 0;
	if (calpattern!=null && calpattern.length>0) {		
		previousPattern = calpattern.charAt(0);
		while (pos <= calpattern.length) {
			if (pos < calpattern.length) {
				pattern = calpattern.charAt(pos);
			}  else {
				pattern = "";
			}
			if (pattern != previousPattern) {			
				switch (previousPattern) {
					case 'y':
						date += padYear(year, patternLength);				
						break;
					case 'M':
						date += padNumber(month, patternLength);
						break;
					case 'd':
						date += padNumber(day, patternLength);
						break;
					case '\'':
						// PENDING
						break;
					default:
						date += previousPattern;
				}
				previousPattern = pattern;
				patternLength = 0;
			}
			patternLength++;
			pos++;
		}
	}
	return date;
}

function padYear(year, patternLength) {
	if (patternLength==2 && year.length==4) {
		return year.substring(2);
	} else {
		return year;
	}
}

function padNumber(number,length) {
    var str = '' + number;
    while (str.length < length)
        str = '0' + str;
    return str;
}

function hideCalendar() {
	if(document.all) {
		// IE.
		document.all.slcalcod.style.visibility="hidden";
		showElement("SELECT");
	} else if(document.layers) {
		// Netspace 4
		document.slcalcod.visibility="hidden";
	} else {
		// Mozilla
		var calendrier = document.getElementById("slcalcod");
		calendrier.style.visibility="hidden";
	}
}

/**
 * Fix IE bug
 */
function hideElement(elmID)
{
	if (!document.all) {
		return;
	}
	x = parseInt(document.all.slcalcod.style.left);
	y = parseInt(document.all.slcalcod.style.top);
	var node = event.srcElement;
    while(node.tagName != "DIV") {
     	node = node.parentNode;
    	if (node.tagName == 'HTML') break;
	}
    if(node.tagName == "DIV"){
     	x+= node.scrollLeft;
        y+=node.scrollTop;
    }
	//xxx = 253; // document.all.slcalcod.offsetWidth;	
	//yyy = 145; // document.all.slcalcod.offsetHeight;
	
	xxx = document.all.slcalcod.offsetWidth;
	yyy = document.all.slcalcod.offsetHeight;
		
	for (i = 0; i < document.all.tags(elmID).length; i++)
	{
		obj = document.all.tags(elmID)[i];
		if (! obj || ! obj.offsetParent || obj.id=="calmois" || obj.id=="calyear")
			continue;

		// Find the element's offsetTop and offsetLeft relative to the BODY tag.
		objLeft   = obj.offsetLeft;
		objTop    = obj.offsetTop;
		objParent = obj.offsetParent;
		if(obj.style.visibility != "hidden"){
		while (objParent.tagName.toUpperCase() != "BODY")
		{
			objLeft  += objParent.offsetLeft;
			objTop   += objParent.offsetTop;
			objParent = objParent.offsetParent;
		}
		}
		obj.statusVisibility = obj.style.visibility;
										
		// Adjust the element's offsetTop relative to the dropdown menu
		//objTop = objTop - y;
	
		if (x > (objLeft + obj.offsetWidth) || objLeft > (x + xxx))
			;
		else if (objTop > y + yyy)
			;
		else if (y > (objTop + obj.offsetHeight))
			;
		else
             if(obj.statusVisibility != "hidden"){
			obj.style.visibility = "hidden";
	}
}
}

/**
 * Fix IE bug
 */
function showElement(elmID)
{
	if (!document.all) {
		return;
	}
	for (i = 0; i < document.all.tags(elmID).length; i++)
	{
		obj = document.all.tags(elmID)[i];
		if (! obj || ! obj.offsetParent)
			continue;
			
		if(obj.statusVisibility != "hidden")
		obj.style.visibility = "";
	}
}

/**
 * Tabs code.
 *
 * @param tabVarName: name of the form variable that holds the id of the selected tab.
 */
function selectTab(tabGroupId, tabGroupSize, selectedTabId, enabledStyle, disabledStyle, errorStyle, tabKeyName, tabKeyValue, callback) {
	// first unselect all tab in the tag groups.
	for (i=0;i<tabGroupSize;i++) {
		var element = document.getElementById("tabs" + tabGroupId + "head" + i);
		
		var element_left = document.getElementById("tabs" + tabGroupId + "head" + i + "_left");
		var element_right = document.getElementById("tabs" + tabGroupId + "head" + i + "_right");
		var element_bottom = document.getElementById("tabs" + tabGroupId + "head" + i + "_bottom");
		
		var element_body = document.getElementById("tabs" + tabGroupId + "tab" + i);
		
		if (element.classNameErrorStdLayout) {
			element.className = errorStyle;
		
			if (element_right) element_right.className = errorStyle;
			if (element_left) element_left.className = errorStyle;
			if (element_bottom) element_bottom.className = errorStyle;
		
			element.style.color = "";			
		} else if (element.className == enabledStyle) {
			element.className = disabledStyle;
			
			if (element_right) element_right.className = disabledStyle+"_right";
			if (element_left) element_left.className = disabledStyle+"_left";
			if (element_bottom) element_bottom.className = disabledStyle+"_bottom";
			
			element.style.color = "";
		} else if (element.className == errorStyle) {
			// do nothing more
		}
		
		if (element_body) {
			element_body.style.display = "none";
		}
	}
	if (document.getElementById("tabs" + tabGroupId + "head" + selectedTabId).className==errorStyle) {
		document.getElementById("tabs" + tabGroupId + "head" + selectedTabId).classNameErrorStdLayout = new Object();
	}
	document.getElementById("tabs" + tabGroupId + "head" + selectedTabId).className = enabledStyle;
	
	if (document.getElementById("tabs" + tabGroupId + "head" + selectedTabId + "_left")) 
		document.getElementById("tabs" + tabGroupId + "head" + selectedTabId + "_left").className = enabledStyle+"_left";
	if (document.getElementById("tabs" + tabGroupId + "head" + selectedTabId + "_right"))
		document.getElementById("tabs" + tabGroupId + "head" + selectedTabId + "_right").className = enabledStyle+"_right";
	if (document.getElementById("tabs" + tabGroupId + "head" + selectedTabId + "_bottom"))
		document.getElementById("tabs" + tabGroupId + "head" + selectedTabId + "_bottom").className = enabledStyle+"_bottom";
	
	document.getElementById("tabs" + tabGroupId + "head" + selectedTabId).style.cursor = "default";
	document.getElementById("tabs" + tabGroupId + "tab" + selectedTabId).style.display = "";
		
	if (tabKeyName!=null) {
		if (callback!=null) {
			// cookie should not be used. Inform the server of the new seletec tab.
			var xhr = null; 
			if(window.XMLHttpRequest) {
				// Standard 
				xhr = new XMLHttpRequest(); 
			} else if(window.ActiveXObject) { 
				// Internet Explorer 
				xhr_object = new ActiveXObject("Microsoft.XMLHTTP");
			} else { 
				// Impossible to inform the server of the change.			    
			   return; 
			} 
			xhr.open("POST", callback, false);
			xhr.setRequestHeader("Content-type", "application/x-www-form-urlencoded");  
			xhr.send("tabKey=" + tabKeyName + "&tabValue=" + tabKeyValue); 
			if(xhr.readyState != 4) alert("Fail to update tab state on server");
		} else {
			// update a cookie holding the name of the selected tab.
			setTabCookie(tabKeyName, tabKeyValue);
		}
	}
}
function onTabHeaderOver(tabGroupId, selectedTabId, enabledStyle) {
	element = document.getElementById("tabs" + tabGroupId + "head" + selectedTabId);
	if (element.className == enabledStyle) {
		element.style.cursor = "default";
	} else {
		element.style.cursor = "hand";
	}
}

/**
 * Treeview code
 */
function loadTree(url, tree) {
	element = document.getElementById("treeView" + url);
	element.innerHTML = tree;	
	element.style.display = "";
	element = document.getElementById("treeViewNode" + url);
	element.href = "javascript://";
	setMenuCookie("treeView" + url, "show");
}

function changeTree(tree, image1, image2) {
	if (!isTreeviewLocked(tree)) {
		var image = document.getElementById("treeViewImage" + tree);
		if (image.src.indexOf(image1)!=-1) {
			image.src = image2;
		} else {
			image.src = image1;
		}
	
		if (document.getElementById("treeView" + tree).innerHTML == "") {
			return true;
		} else {
			changeMenu("treeView" + tree);
			return false;
		}
	} else {
		return false;
	}
}

function changeTreeAndSubtrees(tree) {
	var image = document.getElementById("treeViewImage" + tree);
	
	var link = image.parentNode;
	if (link.href.indexOf("javascript://") == -1) {
		// il s'agit d'un lien vers le treeview.do
		// => les sous-menus n'ont pas ?t? charg?s dans la page HTML
		// => on ne peut pas proc?der au changement
		return false;
	}
	
	if (image.src.indexOf("Close")!=-1) {
		reg=new RegExp("Close", "g");
		image.src = image.src.replace(reg, "Open");
	} else {
		reg=new RegExp("Open", "g");
		image.src = image.src.replace(reg, "Close");
	}
	
	if (document.getElementById("treeView" + tree).innerHTML == "") {
		return true;
	} else {
		// change the menu itself
		menu = "treeView" + tree;
		changeMenu(menu);
					
		toShow = true;
		if (document.getElementById(menu).style.display=="none") {
			// the "menu" menu has just been hidden : all its subtrees must collapse too
			toShow = false;
		}
		
		list = document.getElementsByTagName("td");
		for (i=0; i<list.length; i++) {
			currentElement = list[i];
			if (currentElement.id.indexOf(menu) != -1
				&& currentElement.id!=menu) {
				// we are at a submenu level
				subTreeName = currentElement.id.substring(8);
				
				if (currentElement.style.display=="none" && toShow
					|| currentElement.style.display=="" && !toShow) {	
					image = document.getElementById("treeViewImage" + subTreeName);
					
					link = image.parentNode;
					if (link.href.indexOf("javascript://") != -1) {
						// il s'agit d'un javascript
						// => les sous-menus ont ?t? charg?s dans la page HTML
						// => on peut proc?der au changement
						
						if (image.src.indexOf("Close")!=-1) {
							reg=new RegExp("Close", "g");
							image.src = image.src.replace(reg, "Open");
						} else {
							reg=new RegExp("Open", "g");
							image.src = image.src.replace(reg, "Close");
						}
					
						if (document.getElementById("treeView" + subTreeName).innerHTML == "") {
							//return true;
						} else {
							changeMenu("treeView" + subTreeName);
						}
					}
					
				}
			}
		}
		
		return false;
	}
}

function expandFirstLevels(treeviewId, numberOfLevelsToExpand) {
	menuId = "treeView" + treeviewId;
		
	list = document.getElementsByTagName("td");
	for (i=0; i<list.length; i++) {
		currentElement = list[i];
		if (currentElement.id.indexOf(menuId) != -1
			&& currentElement.id!=menuId) {
			// we are at a submenu level
			
			idSuffix = currentElement.id.substring(menuId.length);
			if (countStringOccurence(idSuffix, "*") <= numberOfLevelsToExpand) {
				
				subTreeName = currentElement.id.substring(8);
				
				image = document.getElementById("treeViewImage" + subTreeName);
				
				link = image.parentNode;
				if (link.href.indexOf("javascript://") != -1) {
					// il s'agit d'un javascript
					// => les sous-menus ont ?t? charg?s dans la page HTML
					// => on peut proc?der au changement
					
					if (image.src.indexOf("Close")!=-1) {
						reg=new RegExp("Close", "g");
						image.src = image.src.replace(reg, "Open");
					} else {
						reg=new RegExp("Open", "g");
						image.src = image.src.replace(reg, "Close");
					}
				
					if (document.getElementById("treeView" + subTreeName).innerHTML == "") {
						//return true;
					} else {
						changeMenu("treeView" + subTreeName);
					}
				}
			}
		}
	}
}

function openAll(treeviewId, numberOfLevelsToExpand) {
	if (!isTreeviewLocked(treeviewId)) {
	    menuId = "treeView" + treeviewId;
	    var doModify;
		list = document.getElementsByTagName("td");
		for (i=0; i<list.length; i++) {
			currentElement = list[i];
			if (currentElement.id.indexOf(menuId) != -1
				&& currentElement.id!=menuId) {
				// we are at a submenu level
	
				idSuffix = currentElement.id.substring(menuId.length);
				if (countStringOccurence(idSuffix, "*") <= numberOfLevelsToExpand) {
	
					subTreeName = currentElement.id.substring(8);
	
					image = document.getElementById("treeViewImage" + subTreeName);
	
					link = image.parentNode;
					if (link.href.indexOf("javascript://") != -1) {
	
						if (image.src.indexOf("Close")!=-1) {
							reg=new RegExp("Close", "g");
							image.src = image.src.replace(reg, "Open");
							doModify = true;
						} else {
						    // do nothing
	                        doModify = false;
						}
	
						if (document.getElementById("treeView" + subTreeName).innerHTML == "") {
							//return true;
						} else {
						    if(doModify) {
							    changeMenu("treeView" + subTreeName);
	                        }
						}
					}
				}
			}
		}
	}
}

function closeAll(treeviewId, numberOfLevelsToExpand) {
	if (!isTreeviewLocked(treeviewId)) {
	    menuId = "treeView" + treeviewId;
	    var doModify;
		list = document.getElementsByTagName("td");
		for (i=0; i<list.length; i++) {
			currentElement = list[i];
			if (currentElement.id.indexOf(menuId) != -1
				&& currentElement.id!=menuId) {
				// we are at a submenu level
	
				idSuffix = currentElement.id.substring(menuId.length);
				if (countStringOccurence(idSuffix, "*") <= numberOfLevelsToExpand) {
	
					subTreeName = currentElement.id.substring(8);
	
					image = document.getElementById("treeViewImage" + subTreeName);
	
					link = image.parentNode;
					if (link.href.indexOf("javascript://") != -1) {
	
						if (image.src.indexOf("Close")!=-1) {
						    // do nothing						
							doModify = false;
						} else {
							reg=new RegExp("Open", "g");
							image.src = image.src.replace(reg, "Close");
							doModify = true;
						}
	
						if (document.getElementById("treeView" + subTreeName).innerHTML == "") {
							//return true;
						} else {
						    if (doModify) {
							    changeMenu("treeView" + subTreeName);
	                        }
						}
					}
				}
			}
		}
	}
}

function countStringOccurence(stringToTest, occurenceToCount) {
	index = stringToTest.indexOf(occurenceToCount);
	if (stringToTest.indexOf(occurenceToCount) != -1) {
		/*document.write((index + occurenceToCount.length) + "<br/>");
		document.write(stringToTest.substring(index + occurenceToCount.length) + "<br/>");
		occ = countStringOccurence(stringToTest.substring(index + occurenceToCount.length) , occurenceToCount);
		document.write(occ + "<br/>");*/
		return 1 + countStringOccurence(stringToTest.substring(index + occurenceToCount.length) , occurenceToCount);
	} else {
		return 0;
	}
}

var lockedTrees = new Array();

function lockTreeview(in_name) {
	lockedTrees[in_name] = "locked";
	var item = document.getElementById("treeView" + in_name);
	var links = item.getElementsByTagName("a");
	var link;
	item.style.cursor = "wait";
	for (i=0; i < links.length; i++) {
		link = links.item(i);
		link.style.cursor = "wait";
	}
}

function unlockTreeview(in_name) {
	lockedTrees[in_name] = null;
	var item = document.getElementById("treeView" + in_name);
	var links = item.getElementsByTagName("a");
	var link;
	item.style.cursor = "default";
	for (i=0; i < links.length; i++) {
		link = links.item(i);
		link.style.cursor = "default";
	}
}

function isTreeviewLocked(in_name) {
	var pos = in_name.indexOf('*');
	var name = pos==-1 ? in_name : in_name.substring(0, pos);
	var value = lockedTrees[name];
	return "locked" == value;
}

/**
 * Popup code
 */
function openpopup(form, popup, width, height, e) {
	var xx, yy;
	xx = e.screenX;
	yy = e.screenY;
	window.open('about:blank', 'popup', 'directories=0, location=0, menubar=0, status=0, toolbar=0, width=' + width + ', height=' + height + ', top=' + yy + ', left=' + xx); 	
	var action = form.action;
	var target = form.target;
	if (popup == null || popup == "") {
		popup = action;
	}
	form.target='popup';
	form.action = popup;
	form.submit();
	form.target = target;
	form.action = action;
		
	return false;
}

function closepopup(form, openerField, popupField) {
	var inputField = form[popupField];
	var value;
	if (inputField.options) {
		value = inputField.options[form[popupField].selectedIndex].value;
	} else if (inputField.type == "file") {
		value = inputField.value;
	} else {
		for (i=0; i < form.elements.length; i++) {
			var element = form.elements[i];
			if (element.name == popupField && element.checked) {
				value = element.value;
				break;
			}
		}
	}
	window.opener.document.forms[0][openerField].value = value;
	window.close();
}

/**
 * form changes detect code
 */
function checkFormChange(link, text) {
  var ok = true;
  for (var form=0; form < document.forms.length; form++) {
    what = document.forms[form];
    for (var i=0, j=what.elements.length; i<j; i++) {

        if (what.elements[i].type == "checkbox" || what.elements[i].type == "radio") {
            if (what.elements[i].checked != what.elements[i].defaultChecked) {
				ok = false; break;
		    }
		} else if (what.elements[i].type == "text" || what.elements[i].type == "hidden" || what.elements[i].type == "password" || what.elements[i].type == "textarea") {
            if (what.elements[i].value != what.elements[i].defaultValue) {
				ok = false; break;
		    }
		} else if (what.elements[i].type == "select-one" || what.elements[i].type == "select-multiple") {
			var selectSet = false;
			for (var k=0, l=what.elements[i].options.length; k<l; k++) {
				if (what.elements[i].options[k].defaultSelected) {
					selectSet = true;
				}
			}
			for (var k=0, l=what.elements[i].options.length; k<l; k++) {
				if (what.elements[i].options[k].selected != what.elements[i].options[k].defaultSelected && (selectSet || k!=0)) {
					if ("calmois"!=what.elements[i].name && "calyear"!=what.elements[i].name) { 
						// don't check changes on the popup calendar						
						ok = false; break;
					}
				}
			}
		} else if (what.elements[i].type == "submit") {
			continue;	    
		} else if (what.elements[i].type == "button") {
			continue;		
		}  else if (what.elements[i].type == "file") {
			if (what.elements[i].value !=null && what.elements[i].value!="") {
				ok = false;
				break;			
			}	
		} else if (what.elements[i].type == "reset") {
			continue;		
		} else {
			alert(what.elements[i].type);
		}
    }
  }
    if (ok) {	
		window.location.href = link;
		return;
	    }
    if (confirm(text == null ? "Data will be lost. Continue ?" : text)) {
		window.location.href = link;
		return;
    }
}

/**
 * Shows the detail of the specified line.
 */
function showDetail(id, line) {
	// Get the object 
	var object = id[line];
	
	var fields = document.getElementsByTagName("input");
	var field;
	var value;
	
	// Update each property
	for (i in object) {
	
		// find the field tag.
		field = null;
		for (j in fields) {
			if (fields[j].type=="text" && fields[j].name==i) {
				field = fields[j];
				break;
			}
		}
		
		if (field) {
			value = object[i];
			field.value = value;
		}
	}
}

/**
 * Clear the details
 */
function clearDetail(id) {
	// Get the first object to check its properties
	var object = id[0];
	
	var fields = document.getElementsByTagName("input");
	var field;
	var value;
	
	// Update each property
	for (i in object) {
	
		// find the field tag.
		field = null;
		for (j in fields) {
			if (fields[j].type=="text" && fields[j].name==i) {
				field = fields[j];
				break;
			}
		}
		
		if (field) {
			value = object[i];
			field.value = "";
		}
	}
}

/**
 * Init dependent combo
 */
function initDependentComboHandler(masterSelectName, childSelectName, jsArrayName, jsChildArrayName, childSelectedValue) {
	// find the master select.
	var combo = findCombo(masterSelectName);
	var customFunction = new Function("updateCombo('" + masterSelectName + "', '" + childSelectName + "', " + jsArrayName + ", '" + jsChildArrayName + "');");
	if (combo.addEventListener) {
		combo.addEventListener("change", customFunction, false);
		customFunction();
	} else if (combo.attachEvent) {
		combo.attachEvent("onchange", customFunction);
		customFunction();
	} else {
		combo.onchange = customFunction;
		combo.onchange();
	}	
	
	// init child combo selected value.
	var childCombo = findCombo(childSelectName);
	for (i=0; i < childCombo.options.length;i++) {
		if (childCombo.options[i].value==childSelectedValue) {
			childCombo.selectedIndex = i;
			break;
		}
	}
}

function findCombo(comboName) {
	var elements = document.getElementsByTagName("SELECT");
	var combo;
	for (i in elements) {
		if (elements[i].name == comboName) {
			combo = elements[i];
		}
	}
	return combo;
}
 
/**
 * Update dependent combo.
 * @param masterCombo : the main select object.
 * @param comboName : the name of the child select object.
 * @param jsData : the name of the js data array holding the data.
 * @param jsCollectionProperty : the name of the nested collection property.
 */
function updateCombo(masterComboName, childComboName, jsData, jsCollectionProperty) {
	var masterCombo = findCombo(masterComboName);
	var combo = findCombo(childComboName);

	// get the option list.
	var value = masterCombo.options[masterCombo.selectedIndex].value;

	// get the selected bean
	var selectedValue = masterCombo.options[masterCombo.selectedIndex].value;
	var masterSelectedOption;
	for (i=0; i < jsData.length; i++) {		
		if (jsData[i].value == selectedValue) {
			masterSelectedOption = jsData[i];
			break;
		}
	}

	// remove old options
	while (combo.options.length!=0) {
		combo.remove(0);
	}

	// add new options
	if (masterSelectedOption!=null) {
		for (i = 0; i < masterSelectedOption[jsCollectionProperty].length; i++) {
			var option = new Option(masterSelectedOption[jsCollectionProperty][i].label, masterSelectedOption[jsCollectionProperty][i].value);
			if (document.all) {
				combo.add(option);
			} else {
				combo.add(option, null);
			}
		}
	}
}

/**
 * Get the key that was pressed
 */
function getKeyCode(e) {
	var code;
	if (!e) var e = window.event;
	if (e.keyCode) code = e.keyCode;
	else if (e.which) code = e.which;
	//var character = String.fromCharCode(code);
	return code;
}

/**
 * Go to the specified pager page.
 */
function pagerGoto(inputField, e, url, paramName, max) {	
	if (getKeyCode(e)!=13) {
		return;
	}	
	
	var value = inputField.value;
	var computedUrl = url;
	
	if (isNaN(parseInt(value, 10))) {
		return;
	}
	if (value<=0) {
		return;
	}
	if (value>max) {
		return;
	}
		
	if (url.indexOf("?")==-1) {
		computedUrl += "?";
	} else {
		computedUrl += "&";
	}
	computedUrl += paramName + "=" + (value-1);
	document.location = computedUrl;
}

function showRootMenu(td) {
	td.id = "css_hover";
	var element;
	var i;
	var length = td.childNodes.length;
	for (i=0; i < length; i++) {
		element = td.childNodes[i];	
		if (element.nodeName=="UL") {
			element.style.display = "block";		
		}
	}
}

function hideRootMenu(td) {
	td.id = null;
	var element;
	var i;
	var length = td.childNodes.length;
	for (i=0; i < length; i++) {
		element = td.childNodes[i];	
		if (element.nodeName=="UL") {
			element.style.display = "none";		
		}
	}
}

/**
 * Select / unselect all checkboxes for the collectionTag.
 */
function selectAllCollectionItems(checkbox,n,v,j) {
	var f = checkbox.form;
	var chk   = ( v == null ? true : v );
	for ( i = 0; i < j ; i++) {
		var name =  n + "[" + i + "]";
		if( f[name].type == "checkbox" ) {
			f[name].checked = checkbox.checked;
		}
	}
}

/**
 * For Layer tag
 */

var layerXOffset = 20;
var layerYOffset = 0;

function showLayoutLayer(ID,event) {
	var thisLayer = document.getElementById(ID);
	objLeft = 0;
	objTop = 0;
	objParent = thisLayer.offsetParent;
	while (objParent.tagName.toUpperCase() != "BODY") {
		objLeft  += objParent.offsetLeft;
		objTop   += objParent.offsetTop;
		objParent = objParent.offsetParent;
	}
	
	var layerPosY = event.clientY - objTop;
	var layerPosX = event.clientX - objLeft;	
   	var finalPosX = layerPosX - layerXOffset;   
   	if(finalPosX < 0) finalPosX = 0;
   	document.getElementById(ID).style.top = layerPosY + layerYOffset;
   	document.getElementById(ID).style.left = finalPosX;
//	document.getElementById(ID).style.zIndex= 3;
   	document.getElementById(ID).style.visibility = "visible";
}

function hideLayoutLayer(ID) {
    document.getElementById(ID).style.visibility = "hidden";
}
/**
 * Call by the default BasicImageRender
 */
function getParentForm(obj,reqCode){
 	var par=obj.parentNode;
 	while(par.nodeName!="FORM" && par.nodeName!="BODY"){
  		par=par.parentNode;
 	}
 	var form=null;
 	if(par.nodeName=='FORM'){
  		form=par;
  		form.elements['reqCode'].value = reqCode; 
  		form.submit();
 	} else {
  		alert("Javascript Error.No form linked to this button.");
 	}
}