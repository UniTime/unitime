// ***************************************************************
//
//		GLOBAL VARS
//
// ***************************************************************

var KEY_BACKSPACE = 8;
var KEY_DELETE = 46;
var KEY_DOWN = 40;
var KEY_UP = 38;
var KEY_ENTER = 13;
var KEY_ESCAPE = 27;



// ***************************************************************
//
//		AUX FUNCTIONS
//
// ***************************************************************

function getPositionLeft( elem )
{
	var offsetLeft = elem.offsetLeft;
	
	var offsetParent = elem.offsetParent;
	while ( offsetParent != null ) 
	{
		offsetLeft += offsetParent.offsetLeft;
		offsetParent = offsetParent.offsetParent;
	}
	
	return offsetLeft;
}


function getPositionTop( elem )
{
	var offsetTop = elem.offsetTop;
	
	var offsetParent = elem.offsetParent;
	while ( offsetParent != null ) 
	{
		offsetTop += offsetParent.offsetTop;
		offsetParent = offsetParent.offsetParent;
	}
	
	return offsetTop;
}


function setTextFieldValue( textFieldId, value )
{
	var textField = document.getElementById(textFieldId);
	textField.value = value;
}


function selectTextFieldText( textFieldId, startPos, endPos ) 
{
	if (isIE())
	{
		var oSelection = document.getElementById(textFieldId).createTextRange();
		oSelection.collapse(true);
		oSelection.moveStart('character', startPos);
		oSelection.moveEnd('character', endPos);
		oSelection.select();					
	}
	else				
		document.getElementById(textFieldId).setSelectionRange(startPos, endPos);
}

function getSelectedSuggestionIndex(textFieldId)
{
	return parseInt(document.getElementById(textFieldId + "SuggestionList_selectedSuggestionIndex").value);
}

function setSelectedSuggestionIndex(textFieldId, value)
{
	document.getElementById(textFieldId + "SuggestionList_selectedSuggestionIndex").value = value;
}

function getTypedWord(textFieldId)
{
	return document.getElementById(textFieldId + "SuggestionList_typedWord").value;
}

function setTypedWord(textFieldId, value)
{
	document.getElementById(textFieldId + "SuggestionList_typedWord").value = value;
}


function getKey( keyCode, which )
{
	if (keyCode)	// IE
		return keyCode;
	else	// Reste
		return which;
}

/*
	Tab			  		  9
	Shift				 16
	Ctrl				 17
	Alt					 18
	Pause/Break			 19
	CapsLock			 20
	Page Up		 		 33
	Page Down		 	 34
	End			 		 35
	Home			 	 36
	LeftArrow		 	 37
	RightArrow		 	 39
	PrintScreen / Syst	 44
	Insert		 		 45
	F1					112
	F2					113
	F3					114
	F4					115
	F5					116
	F6					117
	F7					118
	F8					119
	F9					120
	F10					121
	F11					122
	F12					123
	NumLock				144
	ScrollLock			145	
*/
function isSpecialKey(keyCode)
{
	var pattern = "|9|16|17|18|19|20|33|34|35|36|37|39|44|45|112|113|114|115|116|117|118|119|120|121|122|123|144|145|";
	
	return pattern.indexOf("|" + keyCode + "|") > -1;
}


function isIE() 
{
	if ( window.ActiveXObject ) 
		return true;
	else
		return false;
}


function getAllFormTag() {
	var i, j, arrayTemp;
	var arrayFormTags = new Array();
	var arrayTagAndValue = new Array();
	var currentArrayTagAndValuePosition = 0;
	
	arrayTemp = document.getElementsByTagName("select");
	for(i=0; i<arrayTemp.length; i++)
		arrayFormTags.push(arrayTemp[i]);
	arrayTemp = document.getElementsByTagName("textaraea");
	for(i=0; i<arrayTemp.length; i++)
		arrayFormTags.push(arrayTemp[i]);
	arrayTemp = document.getElementsByTagName("input");
	for(i=0; i<arrayTemp.length; i++)
		arrayFormTags.push(arrayTemp[i]);
	
	for(i=0; i<arrayFormTags.length; i++) {
		switch(arrayFormTags[i].tagName.toLowerCase()) {
			case "select" : 	if(arrayFormTags[i].multiple==false) {
									arrayTagAndValue.push(arrayFormTags[i].name);
									if(arrayFormTags[i].selectedIndex>=0)
										arrayTagAndValue.push(arrayFormTags[i].options[arrayFormTags[i].selectedIndex].value);
									else
										arrayTagAndValue.push("");
									currentArrayTagAndValuePosition += 2;
								}
								else {
									// TODO : MONSTRUOUS !!!
									// If none or one value selected, the String[] is convert
									// to a String so we lose the information that this is 
									// a multi-select : String can be a text, a select or a textarea
									// but String[] is necessary a multi-select -> the convertion
									// causes the type tag loss
									for(j=0; j<arrayFormTags[i].options.length; j++) {
										if(arrayFormTags[i].options[j].selected==true) {
											arrayTagAndValue.push(arrayFormTags[i].name);
											arrayTagAndValue.push(arrayFormTags[i].options[j].value);
											currentArrayTagAndValuePosition += 2;
										}
									}
								}
								break;
			case "textarea" : 	arrayTagAndValue.push(arrayFormTags[i].name);
								if(arrayFormTags[i].value==null)
									arrayTagAndValue.push("");
								else
									arrayTagAndValue.push(arrayFormTags[i].value);
								currentArrayTagAndValuePosition += 2;
								break;
			case "input" : 		if(arrayFormTags[i].type.toLowerCase()=="text") {
									// Avoid empty paramaters
									if (arrayFormTags[i].name=="") break;
									arrayTagAndValue.push(arrayFormTags[i].name);
									if(arrayFormTags[i].value==null)
										arrayTagAndValue.push("");
									else
										arrayTagAndValue.push(arrayFormTags[i].value);
									currentArrayTagAndValuePosition += 2;
								}
								else if(arrayFormTags[i].type.toLowerCase()=="checkbox") {
									if(arrayFormTags[i].checked==true) {
										if(arrayFormTags[i].name.charAt(arrayFormTags[i].name.length-1)=="]") {
											j=arrayFormTags[i].name.length-1;
											while(arrayFormTags[i].name.charAt(j)!="[")
												j--;
											arrayTagAndValue.push(arrayFormTags[i].name.substr(0,j));
											arrayTagAndValue.push(arrayFormTags[i].value);
										}
										else {
											arrayTagAndValue.push(arrayFormTags[i].name);
											arrayTagAndValue.push(arrayFormTags[i].value);
										}
										currentArrayTagAndValuePosition += 2;
									}
								}
								else if(arrayFormTags[i].type.toLowerCase()=="radio") {
									if(arrayFormTags[i].checked==true) {
										arrayTagAndValue.push(arrayFormTags[i].name);
										arrayTagAndValue.push(arrayFormTags[i].value);
										currentArrayTagAndValuePosition += 2;
									}
								}
								break;
		}
	}
	
	return arrayTagAndValue;
}


// ***************************************************************
//
//		MAIN FUNCTIONS
//
// ***************************************************************

function updateSuggestionList(textFieldId, actionURL, selectFirstWord, maxSuggestionsCount, suggestionsEncoding, minWordLength, timeout, all)
{
	if (!document.getElementById(textFieldId) || document.getElementById(textFieldId).value.length < minWordLength) 
	{
		hideSuggestionList(textFieldId);
		return;
	}


	// Request initialization
	
	if ( window.XMLHttpRequest ) 
		req = new XMLHttpRequest();
	
	else if ( window.ActiveXObject ) 
		req = new ActiveXObject( "Microsoft.XMLHTTP" );
		
	
	// Listener creation
	
	req.onreadystatechange = function () 
	{
			
		if (req.readyState == 4) 
		{
		
			if (req.status == 200) 
			{
		
				// Response
				
				var xmlDoc = req.responseXML;
				
			
				if (xmlDoc && xmlDoc.documentElement && xmlDoc.documentElement.childNodes && xmlDoc.documentElement.childNodes.length > 0) 
				{
					// Suggestion list creation
					
					var elementsMenu = new Array;
					
					var count = xmlDoc.documentElement.childNodes.length;
					
					for(i=0; i<count && i<maxSuggestionsCount; i++)
					{
						elementsMenu[i] = xmlDoc.documentElement.childNodes[i].getAttribute("value");
					}
					
					
					// Suggestion list rendering
					
					var divMenu = document.getElementById( textFieldId + "SuggestionList" ) ;
					
					divMenu.innerHTML = '';
					
					for( j=0; j<elementsMenu.length; j++)
					{
						var fixedApostropheString = elementsMenu[j].split("'").join("\\'");
						divMenu.innerHTML += '<div class="suggestionList_element_off" onClick="setTextFieldValue(\'' + textFieldId + '\', \'' + fixedApostropheString + '\');hideSuggestionList(\'' + textFieldId + '\')" onMouseOver="displayCursor(\'' + textFieldId + '\', ' + j + ')" onMouseOut="hideCursor(\'' + textFieldId + '\', ' + j + ')">' + elementsMenu[j] + '</div>';
					}
					
					
					// Selection of the first suggestion
					
					if (elementsMenu.length > 0)
					{
						displaySuggestionList(textFieldId);
						
						// Selection of the first item
						var suggestionList = document.getElementById( textFieldId + "SuggestionList" );
						var newSelection = suggestionList.getElementsByTagName("div").item(0);
						setSelectedSuggestionIndex(textFieldId, 0);
						
						// Saving the typed word
						setTypedWord(textFieldId, document.getElementById(textFieldId).value);	
						newSelection.className="suggestionList_element_on";
						
						if (selectFirstWord)
						{
							var currentWord = document.getElementById(textFieldId).value;
							
							document.getElementById(textFieldId).value = currentWord + elementsMenu[0].substr(currentWord.length);
							
							// Selection of the end of the suggestion
							selectTextFieldText(textFieldId, currentWord.length, elementsMenu[0].length);
							document.getElementById(textFieldId + "SuggestionList_selectedFieldText").value = 1;						
						}
					}
					
					else
						hideSuggestionList(textFieldId);		
				}
				
				else
					hideSuggestionList(textFieldId);
				
			}
		}
	};
	
	
	// Request construction
	if(all=='true') {
		var arrayTagAndValue = new Array();
		var i, vars;
		vars = '';
		arrayTagAndValue = getAllFormTag();
		for(i=0;i<arrayTagAndValue.length; i+=2) {
			vars += arrayTagAndValue[i] + '=' + escape(arrayTagAndValue[i+1]) + '&';
		}
		vars += 'enc=' + escape(suggestionsEncoding);
	}
	else
		var vars = 'word=' + escape(document.getElementById(textFieldId).value) + '&enc=' + escape(suggestionsEncoding);
	req.open( "POST", actionURL, true );
	req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
	// req.setRequestHeader("Content-Length", vars.length);
	
	
	// Request send
	
	setTimeout("try { req.send('" + vars + "') } catch(e) {}", timeout);
	
}


function computeFocus(textFieldId, actionURL, maxSuggestionsCount, suggestionsEncoding, minWordLength, timeout, all)
{
	updateSuggestionList(textFieldId, actionURL, true, maxSuggestionsCount, suggestionsEncoding, minWordLength, timeout, all);
	return;
}


function computeBlur(textFieldId, actionURL, maxSuggestionsCount, suggestionsEncoding, minWordLength, timeout, all)
{
	hideSuggestionList(textFieldId);
	return;
}

function computeKeyUp(textFieldId, key, actionURL, maxSuggestionsCount, suggestionsEncoding, minWordLength, timeout, all)
{
	if (key != KEY_DOWN && key != KEY_UP && key != KEY_ENTER && key != KEY_ESCAPE && !isSpecialKey(key))
	{
		if ( key == KEY_BACKSPACE || key == KEY_DELETE )
		{
			if (document.getElementById(textFieldId + "SuggestionList_selectedFieldText").value == 0)
			{
				updateSuggestionList(textFieldId, actionURL, false, maxSuggestionsCount, suggestionsEncoding, minWordLength, timeout, all);
			}					
			else
			{
				document.getElementById(textFieldId + "SuggestionList_selectedFieldText").value = 0;
				updateSuggestionList(textFieldId, actionURL, false, maxSuggestionsCount, suggestionsEncoding, minWordLength, timeout, all);
			}
		}
		else 
			updateSuggestionList(textFieldId, actionURL, true, maxSuggestionsCount, suggestionsEncoding, minWordLength, timeout, all);
	}
	
}


function computeKeyDown(textFieldId, key, actionURL)
{
	if ( key == KEY_DOWN )
	{
		var selectedItem = getSelectedSuggestionIndex(textFieldId);
	
		var suggestionList = document.getElementById( textFieldId + "SuggestionList" );
		var suggestions = suggestionList.getElementsByTagName("div");
	
		if (selectedItem < suggestions.length - 1)
		{
			if (selectedItem == -1) 
			{	
				// Saving the typed word
				setTypedWord(textFieldId, document.getElementById(textFieldId).value);
			}
			
			if (selectedItem > -1) 
			{
				var currentSelection = suggestions.item(selectedItem);
				currentSelection.className="suggestionList_element_off";
			}
			
			setSelectedSuggestionIndex(textFieldId, selectedItem + 1);	
			
			var newSelection = suggestions.item(selectedItem+1);
			newSelection.className="suggestionList_element_on";
		
			document.getElementById(textFieldId).value = newSelection.childNodes.item(0).data;
		}			
		
		// Move cursor to the end of the field
		var currentWord = document.getElementById(textFieldId).value;
		selectTextFieldText(textFieldId, currentWord.length, currentWord.length);
		document.getElementById(textFieldId + "SuggestionList_selectedFieldText").value = 0;

	}
	
	else if ( key == KEY_UP )
	{
		var selectedItem = getSelectedSuggestionIndex(textFieldId);
	
		var suggestionList = document.getElementById( textFieldId + "SuggestionList" );
		var suggestions = suggestionList.getElementsByTagName("div");
		
		if (selectedItem > -1)
		{
			if (selectedItem > 0)
			{
				var currentSelection = suggestions.item(selectedItem);
				currentSelection.className="suggestionList_element_off";
			
				setSelectedSuggestionIndex(textFieldId, selectedItem - 1);	
	
				var newSelection = suggestions.item(selectedItem-1);
				newSelection.className="suggestionList_element_on";
				
				document.getElementById(textFieldId).value = newSelection.childNodes.item(0).data;
			}
	
			else if (selectedItem == 0)
			{
				// Restore the typed word
				document.getElementById(textFieldId).value = getTypedWord(textFieldId);
			
				// Removing current selection
				var currentSelection = suggestions.item(selectedItem);
				currentSelection.className = "suggestionList_element_off";
				
				setSelectedSuggestionIndex(textFieldId, -1);
			}	
			
			// Move cursor to the end of the field
			var currentWord = document.getElementById(textFieldId).value;
			selectTextFieldText(textFieldId, currentWord.length, currentWord.length);
			document.getElementById(textFieldId + "SuggestionList_selectedFieldText").value = 0;
		}
	}
	
	else if ( key == KEY_ENTER || key == KEY_ESCAPE )
	{
		hideSuggestionList(textFieldId);
		var currentWord = document.getElementById(textFieldId).value;
		selectTextFieldText(textFieldId, currentWord.length, currentWord.length);
	}
}


function computeKeyPress(textFieldId, key)
{
	if ( key == KEY_ENTER )
	{
		return false;
	}
}
	
	
function displaySuggestionList(textFieldId)
{
	var textField = document.getElementById(textFieldId);
	var suggestionList = document.getElementById( textFieldId + "SuggestionList" );
	
	suggestionList.style.top = (getPositionTop( textField ) + 25) + "px";
	suggestionList.style.left = getPositionLeft( textField ) + "px";
	suggestionList.style.visibility = "visible";	
}

function hideSuggestionList(textFieldId)
{
	var suggestionList = document.getElementById( textFieldId + "SuggestionList" );
	
	if ( suggestionList )
	{
		suggestionList.style.top = "0px";
		suggestionList.style.left = "0px";
		suggestionList.style.visibility = "hidden";				
	}
}

function blurSuggestionList(textFieldId)
{
	setTimeout(function() { hideSuggestionList(textFieldId);}, 200);
}


function displayCursor(textFieldId, suggestionIndex)
{
	var suggestionList = document.getElementById( textFieldId + "SuggestionList" );
	var suggestions = suggestionList.getElementsByTagName("div");

	var selectedItem = getSelectedSuggestionIndex(textFieldId);
	if (selectedItem != -1)
	{
		var currentSelection = suggestions.item(selectedItem);
		currentSelection.className="suggestionList_element_off";
	}
	
	setSelectedSuggestionIndex(textFieldId, suggestionIndex);
	
	var newSelection = suggestions.item(suggestionIndex);
	newSelection.className="suggestionList_element_on";
}

function hideCursor(textFieldId, suggestionIndex)
{
	var suggestionList = document.getElementById( textFieldId + "SuggestionList" );
	var suggestions = suggestionList.getElementsByTagName("div");
		
	var newSelection = suggestions.item(suggestionIndex);
	newSelection.className="suggestionList_element_off";
}
		