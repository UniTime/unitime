/* Copyright 2005 Improve SA
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
 
/********************************************************************************
 * TODO : see the problem when up and down key presses -> selection not updated *
 * TODO : move all div if IE (near the cursor)                                  *
 ********************************************************************************/
 
StrutsLayoutExpert = new Object();

StrutsLayoutExpert.errorMessage = 'Impossible de prendre en compte tous les labels'+'\r\n\r\n'+'Tag expert non fonctionnel';
StrutsLayoutExpert.joker = ';';

StrutsLayoutExpert.KEY_BACKSPACE = 8;
StrutsLayoutExpert.KEY_DELETE = 46;
StrutsLayoutExpert.KEY_DOWN = 40;
StrutsLayoutExpert.KEY_UP = 38;
StrutsLayoutExpert.KEY_ENTER = 13;
StrutsLayoutExpert.KEY_ESCAPE = 27;

StrutsLayoutExpert.arrayExpertLetter;
StrutsLayoutExpert.arrayExpertTag;

StrutsLayoutExpert.lastWrittenText;
StrutsLayoutExpert.cursorPosition;
StrutsLayoutExpert.elementSuggested = "";
StrutsLayoutExpert.lenElementToSuggest = 0;
StrutsLayoutExpert.expertLetterNumber = 2;
StrutsLayoutExpert.isSelectMultiple;
StrutsLayoutExpert.textareaFieldId = "expertSuggest";

/*****************
 * AUX FUNCTIONS *
 *****************/
 
StrutsLayoutExpert.getPositionLeft = function( elem ) {
	var offsetLeft = elem.offsetLeft;
	
	var offsetParent = elem.offsetParent;
	while ( offsetParent != null ) {
		offsetLeft += offsetParent.offsetLeft;
		offsetParent = offsetParent.offsetParent;
	}
	
	return offsetLeft;
};

StrutsLayoutExpert.getPositionTop = function( elem ) {
	var offsetTop = elem.offsetTop;
	
	var offsetParent = elem.offsetParent;
	while ( offsetParent != null ) {
		offsetTop += offsetParent.offsetTop;
		offsetParent = offsetParent.offsetParent;
	}
	
	return offsetTop;
};

StrutsLayoutExpert.setTextareaFieldValue = function(node, value, cursorPosition) {
	// On the textarea
	if((node.nodeName).toLowerCase()=="textarea")
		node.value = node.value.substring(0,cursorPosition) + value + node.value.substring(cursorPosition);
	else {
		// Click on a value (so from a suggest hidden div)
		var textAreaNode = node.parentNode.previousSibling.previousSibling.previousSibling.previousSibling;
		textAreaNode.value = textAreaNode.value.substring(0,cursorPosition) + textAreaNode.value.substring(parseInt(cursorPosition)+parseInt(StrutsLayoutExpert.elementSuggested.length));
		textAreaNode.value = textAreaNode.value.substring(0,cursorPosition) + value.substring(StrutsLayoutExpert.lenElementToSuggest) + textAreaNode.value.substring(cursorPosition);
		StrutsLayoutExpert.elementSuggested = value.substring(StrutsLayoutExpert.lenElementToSuggest);
		textAreaNode.focus();
	}
};

StrutsLayoutExpert.selectTextareaFieldText = function(expertNode, startPos, endPos) {
	if (StrutsLayoutExpert.isIE()) {
		var oSelection = expertNode.createTextRange();
		oSelection.collapse(true);
		oSelection.moveStart('character', startPos);
		oSelection.moveEnd('character', parseInt(endPos)-parseInt(startPos));
		oSelection.select();
	}
	else {			
		expertNode.setSelectionRange(startPos, endPos);
	}
};

StrutsLayoutExpert.getSelectedSuggestionIndex = function(textareaFieldId) {
	return parseInt(document.getElementById(textareaFieldId + "SuggestionList_selectedSuggestionIndex").value);
};

StrutsLayoutExpert.setSelectedSuggestionIndex = function(textareaFieldId, value) {
	document.getElementById(textareaFieldId + "SuggestionList_selectedSuggestionIndex").value = value;
};

StrutsLayoutExpert.getTypedWord = function(textareaFieldId) {
	return document.getElementById(textareaFieldId + "SuggestionList_typedWord").value;
};

StrutsLayoutExpert.setTypedWord = function(textareaFieldId, value) {
	document.getElementById(textareaFieldId + "SuggestionList_typedWord").value = value;
};

StrutsLayoutExpert.displaySuggestionList = function(expertNode, textareaFieldId) {
	var suggestionList = document.getElementById( textareaFieldId + "SuggestionList" );
	
	suggestionList.style.top = StrutsLayoutExpert.getPositionTop(expertNode) + 25;
	suggestionList.style.left = StrutsLayoutExpert.getPositionLeft(expertNode);
	suggestionList.style.visibility = "visible";	
};

StrutsLayoutExpert.hideSuggestionList = function(textareaFieldId) {
	var suggestionList = document.getElementById( textareaFieldId + "SuggestionList" );
	
	// SuggestionList may not exist
	if (suggestionList) {
		suggestionList.style.top = 0;
		suggestionList.style.left = 0;
		suggestionList.style.visibility = "hidden";				
	}
};

StrutsLayoutExpert.displayCursor = function(textareaFieldId, suggestionIndex) {
	var suggestionList = document.getElementById( textareaFieldId + "SuggestionList" );
	var suggestions = suggestionList.getElementsByTagName("div");

	var selectedItem = StrutsLayoutExpert.getSelectedSuggestionIndex(textareaFieldId);
	if (selectedItem != -1) {
		var currentSelection = suggestions.item(selectedItem);
		currentSelection.className="suggestionList_element_off";
	}
	
	StrutsLayoutExpert.setSelectedSuggestionIndex(textareaFieldId, suggestionIndex);
	
	var newSelection = suggestions.item(suggestionIndex);
	newSelection.className="suggestionList_element_on";
};

StrutsLayoutExpert.hideCursor = function(textareaFieldId, suggestionIndex) {
	var suggestionList = document.getElementById( textareaFieldId + "SuggestionList" );
	var suggestions = suggestionList.getElementsByTagName("div");
		
	var newSelection = suggestions.item(suggestionIndex);
	newSelection.className="suggestionList_element_off";
};

StrutsLayoutExpert.getSelectionCrossBrowser = function() {
	var txt;
	if (window.getSelection)
		txt = window.getSelection();
	else if (document.getSelection)
		txt = document.getSelection();
	else if (document.selection)
		txt = document.selection.createRange().text;
	else
		txt = 'Navigateur non compatible';
	return txt;
};

StrutsLayoutExpert.getKey = function( keyCode, which ) {
	if (keyCode)	// IE
		return keyCode;
	else	// Reste
		return which;
};

StrutsLayoutExpert.isSpecialKey = function(keyCode) {
	var pattern = "|9|16|17|18|19|20|33|34|35|36|37|39|44|45|112|113|114|115|116|117|118|119|120|121|122|123|144|145|";
	return pattern.indexOf("|" + keyCode + "|") > -1;
};

StrutsLayoutExpert.isIE = function() {
	if (navigator.appName=="Microsoft Internet Explorer") {
		return true;
	}
	else {
		return false;
	}
};

StrutsLayoutExpert.expertLastWrittenText = function(value) {
	StrutsLayoutExpert.lastWrittenText = value;
};

StrutsLayoutExpert.getLastCharWrittenPosition = function(lastText, currentText) {
	var i, maxSize, find, lastCharPosition;
	if(lastText.length==currentText.length)
		return StrutsLayoutExpert.cursorPosition;
	else if(lastText.length<currentText.length)
		maxSize = lastText.length;
	else
		maxSize = currentText.length;
	find = false;
	for(i=0; i<maxSize; i++) {
		if(lastText.charAt(i)!=currentText.charAt(i)) {
			lastCharPosition = i+1;
			find=true;
			break;
		}
	}
	if(find==false)
		lastCharPosition = currentText.length;
	return lastCharPosition;
};

StrutsLayoutExpert.getCurrentExpertText = function(value, cursorPosition, leftCharacter, rightCharacter) {
	var i;
	var find, start, end;
	var currentPosition, currentExpertChar;
	
	// Beginning of the expert text
	currentExpertChar = value.charAt(cursorPosition);
	currentPosition = cursorPosition;
	// NECESSARY JOKER " "
	while (currentExpertChar!=leftCharacter && currentExpertChar!=StrutsLayoutExpert.joker) {
		currentPosition--;
		if(currentPosition<0)
			break;
		currentExpertChar = value.charAt(currentPosition);
	}
	if(currentPosition==cursorPosition)
		start = currentPosition;
	else
		start = currentPosition+1;
	
	// Ending of the expert text
	currentExpertChar = value.charAt(cursorPosition);
	currentPosition = cursorPosition;
	while (currentExpertChar!=rightCharacter && currentExpertChar!=StrutsLayoutExpert.joker) {
		currentPosition++;
		if(currentPosition>value.length)
			break;
		currentExpertChar = value.charAt(currentPosition);
	}
	if(currentPosition>value.length)
		end = currentPosition-1;
	else
		end = currentPosition;
	
	return value.substring(start, end);
};

StrutsLayoutExpert.expertLastWrittenText = function(value) {
	StrutsLayoutExpert.lastWrittenText = value;
};

StrutsLayoutExpert.getFormTag = function(tagToUpdate) {
	var nodeName, hasNode, currentNodePosition, find, next;
	var element, currentElement;
	// First element of the tree
	element = document.getElementById(tagToUpdate);
	// Current element = First Element
	currentElement = element;
	find = false;
	// The element is necessary in the tree because referenced on arrayExpertLetter
	// SO : INFINITE LOOP NOT CONSIDERED HERE
	// (var find NECESSARY EQUAL TRUE BEFORE THE END OF THE TREE)
	// To re-use this fonction -> implement infinite case
	do {
		nodeName = (currentElement.nodeName).toLowerCase();
		if(nodeName=="select" || nodeName=="input") {
			find = true;
		} 
		else {
			// Go next node if next node not null and current node not first node (because 
			// first node can have a next node but we want to go forward the tree)
			if(currentElement.nextSibling!=null && currentElement.innerHTML!=element.innerHTML) {
				currentElement = currentElement.nextSibling;
			}
			// Next node is null : go forward or go back on the tree ?
			else {
				next = false;
				do {
					// The current node has no children
					if(currentElement.childNodes.length==0) {
						// Next Node is null
						if(currentElement.nextSibling==null) {
							// Go back on the node tree until the next node of the parent node is null
							do {
								currentElement = currentElement.parentNode;
							} while (currentElement.nextSibling==null);
							currentElement = currentElement.nextSibling;
							next = true;
						}
						else {
							// Next Node
							currentElement = currentElement.nextSibling;
						}
					}
					else {
						// Current node has children : go forward on the node tree
						currentElement = currentElement.childNodes.item(0);
						next = true;
					}
				} while(next==false);
			}
		}
	} while (find==false);
	
	return currentElement;
};


/******************
 * MAIN FUNCTIONS *
 ******************/

/**
 * @author Romain Maton
 * Underline all the value that expert tag have to manage
 */
StrutsLayoutExpert.init = function (arrayId) {
	var i, j, k;
	var letterUnavailable = new Array();
	var equivalentTag = new Array();
	var isPresent, error;
	var textContent, innerHTML;
	var currentLetter, currentPosition;
	var beginning, ending;
	for(i=0; i<arrayId.length; i++) {
		if(StrutsLayoutExpert.isIE())
			textContent = document.getElementById(arrayId[i]+"L").innerText;
		else
			textContent = document.getElementById(arrayId[i]+"L").textContent;
		textContentLength = textContent.length;
		currentPosition=0;
		error=false;
		do {
			isPresent = false;
			if(currentPosition!=textContentLength) {
				currentLetter = (textContent.substring(currentPosition, parseInt(currentPosition)+1)).toLowerCase();
				if(currentLetter.match(/[a-zA-Z1-9]/)) {
					for(j=0; j<letterUnavailable.length; j++) {
						if(currentLetter==letterUnavailable[j]) {
							isPresent=true;
							break;
						}
					}
					if(isPresent==false) {
						textContent = textContent.substring(0, currentPosition) + "<u>" + textContent.substring(currentPosition, parseInt(currentPosition)+1) + "</u>" + textContent.substring(parseInt(currentPosition)+1);
						letterUnavailable.push(currentLetter);
						equivalentTag.push(arrayId[i]);
					}
					else {
						currentPosition++;
					}
				}
				else {
					isPresent=true;
					currentPosition++;
				}
			}
			else {
				error=true;
				alert(StrutsLayoutExpert.errorMessage);
				letterUnavailable = new Array();
				equivalentTag = new Array();
				break;
			}
		} while (error==false && isPresent==true);
		innerHTML = document.getElementById(arrayId[i]+"L").innerHTML;
		beginning = innerHTML.indexOf(">");
		ending = innerHTML.indexOf("<", beginning);
		document.getElementById(arrayId[i]+"L").innerHTML = (innerHTML.substr(0,parseInt(beginning)+1) + textContent + innerHTML.substr(ending)).replace(/\s/g,"&nbsp;");
	}
	StrutsLayoutExpert.arrayExpertLetter = letterUnavailable;
	StrutsLayoutExpert.arrayExpertTag = equivalentTag;
};


/**
 * @author Romain Maton
 * Fill the appropriate tag with the letter and the value written
 * NB : check ALL the text area, not only the last expert tag
 */
StrutsLayoutExpert.expertUpdate = function(value, forceUpdate) {
	var i, j;
	var lastChar;
	var arrayContentText, arrayCurrentExpertText;
	var tagToUpdate, currentElement;
	var currentCharPosition, currentLetterPosition;
	var valueToSelect;
	var arrayValueToSelect = new Array();
	lastChar = value.substr(parseInt(StrutsLayoutExpert.getLastCharWrittenPosition(StrutsLayoutExpert.lastWrittenText, value))-1,1);
	if(lastChar==StrutsLayoutExpert.joker || forceUpdate==true || (StrutsLayoutExpert.isSelectMultiple==true && lastChar==',')) {
		if(lastChar==StrutsLayoutExpert.joker || forceUpdate==true)
			StrutsLayoutExpert.isSelectMultiple = false;
		StrutsLayoutExpert.hideSuggestionList(StrutsLayoutExpert.textareaFieldId);
		divMenu = document.getElementById(StrutsLayoutExpert.textareaFieldId+"SuggestionList");
		divMenu.innerHTML = '';
		arrayContentText = value.split(StrutsLayoutExpert.joker);
		currentCharPosition=0;
		do {
			if(arrayContentText[currentCharPosition]!=null && 
				arrayContentText[currentCharPosition]!="" &&
				arrayContentText[currentCharPosition]!=StrutsLayoutExpert.joker) {
				arrayCurrentExpertText = arrayContentText[currentCharPosition].split(':');
				if(arrayCurrentExpertText.length==2) {
					currentLetterPosition=0;
					do {
						if(arrayCurrentExpertText[0].toLowerCase()==StrutsLayoutExpert.arrayExpertLetter[currentLetterPosition]) {
							tagToUpdate = StrutsLayoutExpert.arrayExpertTag[currentLetterPosition] + 'F';
							currentElement = StrutsLayoutExpert.getFormTag(tagToUpdate);
							switch((currentElement.nodeName).toLowerCase()) {
								case "select" : if(currentElement.multiple==false) {
													valueToSelect = arrayCurrentExpertText[1].toLowerCase();
													for(i=0; i<currentElement.childNodes.length; i++) {
														if((currentElement.childNodes.item(i).nodeName).toLowerCase()=="option") {
															if(((currentElement.childNodes.item(i).value).replace(/(\s*)/g,"")).toLowerCase()==valueToSelect) {
																currentElement.selectedIndex = currentElement.childNodes.item(i).index;
																break;
															}
														}
													}
												}
												else {
													arrayValueToSelect = (arrayCurrentExpertText[1].toLowerCase()).split(',');
													for(i=0; i<currentElement.childNodes.length; i++) {
														if((currentElement.childNodes.item(i).nodeName).toLowerCase()=="option") {
															currentElement.childNodes.item(i).selected = false;
															for(j=0; j<arrayValueToSelect.length; j++) {
																if(((currentElement.childNodes.item(i).value).replace(/(\s*)/g,"")).toLowerCase()==arrayValueToSelect[j]) {
																	currentElement.childNodes.item(i).selected = true;
																	break;
																}
															}
														}
													}
												}
												break;
								case "input" :  if(currentElement.type=="text")
													currentElement.value = arrayCurrentExpertText[1];
												else if(currentElement.type=="checkbox") {
													if(arrayCurrentExpertText[1].toLowerCase()==currentElement.value)
														currentElement.checked = true;
													else
														currentElement.checked = false;
												}
												break;
							}
							break;
						}
						currentLetterPosition++;
					} while(currentLetterPosition!=StrutsLayoutExpert.arrayExpertLetter.length);
					if(currentLetterPosition==StrutsLayoutExpert.arrayExpertLetter.length) {
						// Error : letter, and so tag, not present in expertTag
					}
				}
				else {
					// Error : find no or more than one ':'
				}
			}
			else {
				// Error : more than one StrutsLayoutExpert.joker or other problem
			}
			currentCharPosition++;
		} while (currentCharPosition!=arrayContentText.length);
	}
};


/**
 * @author Romain Maton
 * Suggest with the select value and the checkbox
 */
StrutsLayoutExpert.expertSuggest = function(expertNode, selectFirstWord, maxSuggestionsCount, minWordLength) {
	var i, j;
	var arrayCurrentExpertText;
	var currentElement, currentExpertText;
	var tagWanted, valueToSelect, arrayValueToSelect;
	var currentSelectMultipleWord, currentArraySelectMultipleWord;
	var elementsMenu, currentSuggestPosition, count;
	var currentWord, find;
	var divMenu;
	
	StrutsLayoutExpert.cursorPosition = StrutsLayoutExpert.getLastCharWrittenPosition(StrutsLayoutExpert.lastWrittenText, expertNode.value);
	// cursorPosition-1 = position for substring
	currentExpertText = StrutsLayoutExpert.getCurrentExpertText(expertNode.value, StrutsLayoutExpert.cursorPosition-1, StrutsLayoutExpert.joker, StrutsLayoutExpert.joker);
	arrayCurrentExpertText = currentExpertText.split(':');
	if(arrayCurrentExpertText[1]==null)
		return;
	currentWord = arrayCurrentExpertText[1];
	StrutsLayoutExpert.lenElementToSuggest = arrayCurrentExpertText[1].length;
	// Case select Multiple (more than one value separate with ","
	if(StrutsLayoutExpert.isSelectMultiple==true) {
		currentSelectMultipleWord = StrutsLayoutExpert.getCurrentExpertText(expertNode.value, StrutsLayoutExpert.cursorPosition-1, ",", ",");
		if(currentSelectMultipleWord.length < parseInt(minWordLength)) {
			StrutsLayoutExpert.hideSuggestionList(StrutsLayoutExpert.textareaFieldId);
			divMenu = document.getElementById( StrutsLayoutExpert.textareaFieldId + "SuggestionList" );
			divMenu.innerHTML = '';
			return;
		}
		StrutsLayoutExpert.lenElementToSuggest = currentSelectMultipleWord.length;
		currentArraySelectMultipleWord = currentSelectMultipleWord.split(':');
		// Test if the multiple select value is the first
		if(currentArraySelectMultipleWord.length>1) {
			currentSelectMultipleWord = currentWord;
			StrutsLayoutExpert.lenElementToSuggest = currentWord.length;
		}
		// No suggest if word is not at the end
		var tempCursor=StrutsLayoutExpert.cursorPosition;
		find=false;
		do {
			if(currentExpertText.charAt(tempCursor)==",")
				find=true;
			if(currentExpertText.charAt(tempCursor)==StrutsLayoutExpert.joker)
				find="none";
			tempCursor++;
		} while (find==false && tempCursor<currentExpertText.length);
		if(find==true)
			return;
	}
	
	// Get suggest Results
	tagWanted=null;
	for(i=0; i<StrutsLayoutExpert.arrayExpertLetter.length; i++) {
		if(StrutsLayoutExpert.arrayExpertLetter[i]==arrayCurrentExpertText[0]) {
			tagWanted = StrutsLayoutExpert.arrayExpertTag[i];
			break;
		}
	}
	
	if(tagWanted!=null) {
		tagToUpdate = tagWanted + 'F';
		currentElement = StrutsLayoutExpert.getFormTag(tagToUpdate);
		
		// EVERYTHING FOR CHECKBOX
		if ((!expertNode || currentExpertText.length < parseInt(minWordLength)+StrutsLayoutExpert.expertLetterNumber) && !((currentElement.nodeName).toLowerCase()=="input" && (currentElement.type).toLowerCase()=="checkbox")) {
			StrutsLayoutExpert.hideSuggestionList(StrutsLayoutExpert.textareaFieldId);
			divMenu = document.getElementById( StrutsLayoutExpert.textareaFieldId + "SuggestionList" );
			divMenu.innerHTML = '';
			return;
		}
		
		if(StrutsLayoutExpert.isSelectMultiple==true) {
			// Don't count StrutsLayoutExpert.expertLetterNumber because only word here, not the tag letter and the ':'
			if(currentSelectMultipleWord.length < parseInt(minWordLength)) {
				StrutsLayoutExpert.hideSuggestionList(StrutsLayoutExpert.textareaFieldId);
				divMenu = document.getElementById( StrutsLayoutExpert.textareaFieldId + "SuggestionList" ) ;
				divMenu.innerHTML = '';
				return;
			}
		}
		
		elementsMenu = new Array;
		currentSuggestPosition = 0;
		switch((currentElement.nodeName).toLowerCase()) {
			case "select" : if(currentElement.multiple==false) {
								StrutsLayoutExpert.isSelectMultiple=false;
								valueToSelect = arrayCurrentExpertText[1].toLowerCase();
								for(i=0; i<currentElement.childNodes.length; i++) {
									if(currentSuggestPosition<maxSuggestionsCount) {
										if((currentElement.childNodes.item(i).nodeName).toLowerCase()=="option") {
											if((((currentElement.childNodes.item(i).value).replace(/(\s*)/g,"")).substr(0,valueToSelect.length)).toLowerCase()==valueToSelect) {
												elementsMenu[currentSuggestPosition] = (currentElement.childNodes.item(i).value).replace(/(\s*)/g,"");
												currentSuggestPosition++;
											}
										}
									}
									else
										break;
								}
							}
							else {
								StrutsLayoutExpert.isSelectMultiple=true;
								arrayValueToSelect = (arrayCurrentExpertText[1].toLowerCase()).split(',');
								if(arrayValueToSelect.length==1)
									currentSelectMultipleWord = arrayValueToSelect[0];
								for(i=0; i<currentElement.childNodes.length; i++) {
									if(currentSuggestPosition<maxSuggestionsCount) {
										if((currentElement.childNodes.item(i).nodeName).toLowerCase()=="option") {
											if((((currentElement.childNodes.item(i).value).replace(/(\s*)/g,"")).substr(0,arrayValueToSelect[arrayValueToSelect.length-1].length)).toLowerCase()==arrayValueToSelect[arrayValueToSelect.length-1]) {
												elementsMenu[currentSuggestPosition] = currentElement.childNodes.item(i).value;
												currentSuggestPosition++;
											}
										}
									}
									else
										break;
								}
							}
							break;
			case "input" :  StrutsLayoutExpert.isSelectMultiple=false;
							if((currentElement.type).toLowerCase()=="checkbox") {
								elementsMenu[0] = currentElement.value;
							}
							break;
		}
		
		// NO ELEMENT : STOP SUGGEST !
		if(elementsMenu.length==0)
			return;
		
		// Case multiple : only last item
		if(StrutsLayoutExpert.isSelectMultiple==true) {
			StrutsLayoutExpert.elementSuggested = elementsMenu[0].substr(currentSelectMultipleWord.length);
		}
		else {
			// Cas input text : no suggest
			if((currentElement.nodeName).toLowerCase()=="input" && (currentElement.type).toLowerCase()=="text") {
			}
			else {
				// Case other
				StrutsLayoutExpert.elementSuggested = elementsMenu[0].substr(currentWord.length);
			}
		}
		
		// Suggestion list rendering
		divMenu = document.getElementById(StrutsLayoutExpert.textareaFieldId + "SuggestionList") ;
		divMenu.innerHTML = '';
		var suggest;
		for( i=0; i<elementsMenu.length; i++) {
			suggest = parseInt(StrutsLayoutExpert.cursorPosition)+elementsMenu[i].length-1;
			divMenu.innerHTML += '<div class="suggestionList_element_off" onClick=";StrutsLayoutExpert.setTextareaFieldValue(this, \'' + elementsMenu[i] + '\', \'' + StrutsLayoutExpert.cursorPosition + '\');StrutsLayoutExpert.selectTextareaFieldText(this.parentNode.previousSibling.previousSibling.previousSibling.previousSibling, ' + suggest + ', ' + suggest +');StrutsLayoutExpert.hideSuggestionList(\'' + StrutsLayoutExpert.textareaFieldId + '\');StrutsLayoutExpert.expertUpdate(this.parentNode.previousSibling.previousSibling.previousSibling.previousSibling.value, true);" onMouseOver="StrutsLayoutExpert.displayCursor(\'' + StrutsLayoutExpert.textareaFieldId + '\', ' + i + ')" onMouseOut="StrutsLayoutExpert.hideCursor(\'' + StrutsLayoutExpert.textareaFieldId + '\', ' + i + ')">' + elementsMenu[i] + '</div>';
		}

		
		/*************************
		 * TODO : make this work *
		 *************************/
		
		// Move the suggest if IE		 
		/*if (StrutsLayoutExpert.isIE()) {
			expertNode.focus();
			range = document.selection.createRange();		
			document.getElementById(StrutsLayoutExpert.textareaFieldId + "SuggestionList").style.left = range.offsetLeft;
			document.getElementById(StrutsLayoutExpert.textareaFieldId + "SuggestionList").style.top = range.offsetTop;
			alert(range.offsetLeft + " " + document.getElementById(StrutsLayoutExpert.textareaFieldId + "SuggestionList").style.left);
		}*/
		
		
		// Selection of the first suggestion
		if (elementsMenu.length > 0) {
			StrutsLayoutExpert.displaySuggestionList(expertNode, StrutsLayoutExpert.textareaFieldId);
			
			// Selection of the first item
			var suggestionList = document.getElementById( StrutsLayoutExpert.textareaFieldId + "SuggestionList" );
			var newSelection = suggestionList.getElementsByTagName("div").item(0);
			StrutsLayoutExpert.setSelectedSuggestionIndex(StrutsLayoutExpert.textareaFieldId, 0);
			
			// Saving the typed word
			StrutsLayoutExpert.setTypedWord(StrutsLayoutExpert.textareaFieldId, expertNode.value);	
			newSelection.className="suggestionList_element_on";
			
			if (selectFirstWord) {
				StrutsLayoutExpert.setTextareaFieldValue(expertNode, StrutsLayoutExpert.elementSuggested, StrutsLayoutExpert.cursorPosition);
				StrutsLayoutExpert.selectTextareaFieldText(expertNode, parseInt(StrutsLayoutExpert.cursorPosition), parseInt(parseInt(StrutsLayoutExpert.cursorPosition)+parseInt(StrutsLayoutExpert.elementSuggested.length)));
				document.getElementById(StrutsLayoutExpert.textareaFieldId + "SuggestionList_selectedFieldText").value = 1;						
			}
		}
	}
	else {
		StrutsLayoutExpert.hideSuggestionList(StrutsLayoutExpert.textareaFieldId);
		divMenu = document.getElementById( StrutsLayoutExpert.textareaFieldId + "SuggestionList" ) ;
		divMenu.innerHTML = '';
	}
};


StrutsLayoutExpert.computeKeyUp = function(expertNode, key, maxSuggestionsCount, minWordLength) {
	if (key != StrutsLayoutExpert.KEY_DOWN && key != StrutsLayoutExpert.KEY_UP && key != StrutsLayoutExpert.KEY_ENTER && key != StrutsLayoutExpert.KEY_ESCAPE && !StrutsLayoutExpert.isSpecialKey(key)) {
		if ( key == StrutsLayoutExpert.KEY_BACKSPACE || key == StrutsLayoutExpert.KEY_DELETE ) {
			if (document.getElementById(StrutsLayoutExpert.textareaFieldId + "SuggestionList_selectedFieldText").value == 0) {
				StrutsLayoutExpert.expertSuggest(expertNode, false, maxSuggestionsCount, minWordLength);
			}					
			else {
				document.getElementById(StrutsLayoutExpert.textareaFieldId + "SuggestionList_selectedFieldText").value = 0;
				StrutsLayoutExpert.expertSuggest(expertNode, false, maxSuggestionsCount, minWordLength);
			}
		}
		else {
			StrutsLayoutExpert.expertSuggest(expertNode, true, maxSuggestionsCount, minWordLength);
		}
	}
};


StrutsLayoutExpert.computeKeyDown = function(expertNode, key) {	
	if(StrutsLayoutExpert.isIE()) {
		if(document.selection.createRange().text==StrutsLayoutExpert.elementSuggested)
			StrutsLayoutExpert.expertLastWrittenText(expertNode.value.substring(0,StrutsLayoutExpert.cursorPosition) + expertNode.value.substring(parseInt(StrutsLayoutExpert.cursorPosition)+parseInt(StrutsLayoutExpert.elementSuggested.length)));
		else
			StrutsLayoutExpert.expertLastWrittenText(expertNode.value);
	}
	else {
		if(expertNode.value.substring(expertNode.selectionStart,expertNode.selectionEnd)==StrutsLayoutExpert.elementSuggested)
			StrutsLayoutExpert.expertLastWrittenText(expertNode.value.substring(0,StrutsLayoutExpert.cursorPosition) + expertNode.value.substring(parseInt(StrutsLayoutExpert.cursorPosition)+parseInt(StrutsLayoutExpert.elementSuggested.length)));
		else
			StrutsLayoutExpert.expertLastWrittenText(expertNode.value);
	}
	
	if ( key == StrutsLayoutExpert.KEY_DOWN ) {
		var selectedItem = StrutsLayoutExpert.getSelectedSuggestionIndex(StrutsLayoutExpert.textareaFieldId);
		
		var suggestionList = document.getElementById( StrutsLayoutExpert.textareaFieldId + "SuggestionList" );
		var suggestions = suggestionList.getElementsByTagName("div");
		
		if (selectedItem < suggestions.length - 1) {
			if (selectedItem == -1)  {	
				// Saving the typed word
				StrutsLayoutExpert.setTypedWord(StrutsLayoutExpert.textareaFieldId, expertNode.value);
			}
			
			if (selectedItem > -1)  {
				var currentSelection = suggestions.item(selectedItem);
				currentSelection.className="suggestionList_element_off";
			}
			
			StrutsLayoutExpert.setSelectedSuggestionIndex(StrutsLayoutExpert.textareaFieldId, selectedItem + 1);	
			
			var newSelection = suggestions.item(selectedItem+1);
			newSelection.className="suggestionList_element_on";
			
			expertNode.value = (expertNode.value).substring(0,StrutsLayoutExpert.cursorPosition) + (expertNode.value).substring(parseInt(StrutsLayoutExpert.cursorPosition)+parseInt(StrutsLayoutExpert.elementSuggested.length));
			StrutsLayoutExpert.elementSuggested = (newSelection.childNodes.item(0).data).substring(StrutsLayoutExpert.lenElementToSuggest);
			StrutsLayoutExpert.setTextareaFieldValue(expertNode, StrutsLayoutExpert.elementSuggested, StrutsLayoutExpert.cursorPosition);
		}			
		
		// Move cursor to the end of the field
		StrutsLayoutExpert.selectTextareaFieldText(expertNode, StrutsLayoutExpert.cursorPosition, parseInt(StrutsLayoutExpert.cursorPosition)+parseInt(StrutsLayoutExpert.elementSuggested.length));
		document.getElementById(StrutsLayoutExpert.textareaFieldId + "SuggestionList_selectedFieldText").value = 0;
	}
	
	else if ( key == StrutsLayoutExpert.KEY_UP ) {
		var selectedItem = StrutsLayoutExpert.getSelectedSuggestionIndex(StrutsLayoutExpert.textareaFieldId);
	
		var suggestionList = document.getElementById( StrutsLayoutExpert.textareaFieldId + "SuggestionList" );
		var suggestions = suggestionList.getElementsByTagName("div");
		
		if (selectedItem > -1) {
			if (selectedItem > 0) {
				var currentSelection = suggestions.item(selectedItem);
				currentSelection.className = "suggestionList_element_off";
			
				StrutsLayoutExpert.setSelectedSuggestionIndex(StrutsLayoutExpert.textareaFieldId, selectedItem - 1);	
	
				var newSelection = suggestions.item(selectedItem-1);
				newSelection.className="suggestionList_element_on";
				
				expertNode.value = (expertNode.value).substring(0,StrutsLayoutExpert.cursorPosition) + (expertNode.value).substring(parseInt(StrutsLayoutExpert.cursorPosition)+parseInt(StrutsLayoutExpert.elementSuggested.length));
				StrutsLayoutExpert.elementSuggested = (newSelection.childNodes.item(0).data).substring(StrutsLayoutExpert.lenElementToSuggest);
				StrutsLayoutExpert.setTextareaFieldValue(expertNode, StrutsLayoutExpert.elementSuggested, StrutsLayoutExpert.cursorPosition);
			}
	
			else if (selectedItem == 0) {
				// Restore the typed word
				expertNode.value = StrutsLayoutExpert.getTypedWord(StrutsLayoutExpert.textareaFieldId);
			
				var currentSelection = suggestions.item(selectedItem);
				currentSelection.className = "suggestionList_element_off";
				
				StrutsLayoutExpert.setSelectedSuggestionIndex(StrutsLayoutExpert.textareaFieldId, -1);
				StrutsLayoutExpert.hideSuggestionList(StrutsLayoutExpert.textareaFieldId);
				divMenu = document.getElementById( StrutsLayoutExpert.textareaFieldId + "SuggestionList" );
				divMenu.innerHTML = '';
			}	
			
			// Move cursor to the end of the field
			StrutsLayoutExpert.selectTextareaFieldText(expertNode, parseInt(StrutsLayoutExpert.cursorPosition), parseInt(parseInt(StrutsLayoutExpert.cursorPosition)+parseInt(StrutsLayoutExpert.elementSuggested.length)));
			document.getElementById(StrutsLayoutExpert.textareaFieldId + "SuggestionList_selectedFieldText").value = 0;
		}
	}
	
	else if ( key == StrutsLayoutExpert.KEY_ENTER || key == StrutsLayoutExpert.KEY_ESCAPE ) {
		StrutsLayoutExpert.hideSuggestionList(StrutsLayoutExpert.textareaFieldId);
		divMenu = document.getElementById(StrutsLayoutExpert.textareaFieldId+"SuggestionList");
		divMenu.innerHTML = '';
		/*StrutsLayoutExpert.setTextareaFieldValue(expertNode, StrutsLayoutExpert.joker, parseInt(StrutsLayoutExpert.cursorPosition)+StrutsLayoutExpert.elementSuggested.length);
		StrutsLayoutExpert.cursorPosition++;*/
		StrutsLayoutExpert.selectTextareaFieldText(expertNode, parseInt(StrutsLayoutExpert.cursorPosition)+parseInt(StrutsLayoutExpert.elementSuggested.length), parseInt(StrutsLayoutExpert.cursorPosition)+parseInt(StrutsLayoutExpert.elementSuggested.length));
	}
};


StrutsLayoutExpert.computeKeyPress = function(expertNode, key) {
	if (key == StrutsLayoutExpert.KEY_ENTER)
		return false;
};
