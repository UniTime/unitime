/*HM_ScriptDOM.js
* by Peter Belesis. v4.0.7 010323
* Copyright (c) 2001 Peter Belesis. All Rights Reserved.
* Originally published and documented at http://www.dhtmlab.com/
* You may use this code on a public Web site only if this entire
* copyright notice appears unchanged and you publicly display
* a link to http://www.dhtmlab.com/.
*
* Contact peter.belesis@btclick.com for all other uses.
*/

HM_IE5M = HM_IE && HM_Mac;
HM_IE5W = HM_IE && !HM_Mac;
HM_NS6 = (navigator.vendor == ("Netscape6") || navigator.product == ("Gecko"));

HM_a_Parameters = [
	["MenuWidth",          150],
	["FontFamily",         "Arial,sans-serif"],
	["FontSize",           10],
	["FontBold",           false],
	["FontItalic",         false],
	["FontColor",          "black"],
	["FontColorOver",      "white"],
	["BGColor",            "white"],
	["BGColorOver",        "black"],
	["ItemPadding",        3],
	["BorderWidth",        2],
	["BorderColor",        "red"],
	["BorderStyle",        "solid"],
	["SeparatorSize",      1],
	["SeparatorColor",     "yellow"],
	["ImageSrc",           "tri.gif"],
	["ImageSrcLeft",       "triL.gif"],
	["ImageSize",          5],
	["ImageHorizSpace",    0],
	["ImageVertSpace",     0],
	["KeepHilite",         false],
	["ClickStart",         false],
	["ClickKill",          true],
	["ChildOverlap",       20],
	["ChildOffset",        10],
	["ChildPerCentOver",   null],
	["TopSecondsVisible",  .5],
	["StatusDisplayBuild", 1],
	["StatusDisplayLink",  1],
	["UponDisplay",        null],
	["UponHide",           null],
	["RightToLeft",        false],
	["CreateTopOnly",      0],
	["ShowLinkCursor",     false]
]

HM_MenuIDPrefix = "HM_Menu";
HM_ItemIDPrefix = "HM_Item";
HM_ArrayIDPrefix = "HM_Array";

function HM_f_StringTrim(){
	var TestString = this;
	var SpaceChar  = " ";
	while (TestString.charAt(0) == SpaceChar) {TestString = TestString.substr(1)};
	while (TestString.charAt(TestString.length-1) == SpaceChar) {TestString = TestString.substr(0,TestString.length-1)};
	return TestString.toString();
}

HM_a_BadChars = [".","/"," ",",","-"];

function HM_f_StringStrip(){
	var TestString = this;
	var BadChar;
	for(var i=0;i<HM_a_BadChars.length;i++) {
		BadChar = HM_a_BadChars[i];
		BadCharIndex = TestString.lastIndexOf(BadChar);
		if(BadCharIndex!=-1) TestString = TestString.substr(BadCharIndex + 1);
	}
	return TestString.toString();
}

String.prototype.trim = HM_f_StringTrim;
String.prototype.strip = HM_f_StringStrip;

function HM_f_AssignParameters(paramname,defaultvalue){
	var FullParamName = "HM_" + paramname;
	if (typeof eval("window.HM_PG_" + paramname) == "undefined") {
		if (typeof eval("window.HM_GL_" + paramname) == "undefined") {
			eval(FullParamName + "= defaultvalue");
		}
		else {
			eval(FullParamName + "= HM_GL_" + paramname);
		}
	}
	else {
		eval(FullParamName + "= HM_PG_" + paramname);
	}

	var TestString = eval(FullParamName);
	if(eval("typeof(TestString)") == "string") {
		TestString = TestString.trim();
		if(TestString.length == 0) {
			eval(FullParamName + "= null");
			return;
		}
		if(TestString.charAt(0)=="#")return;
		TestString = TestString.strip();
	}

	if (eval("typeof(" + TestString +")") != 'undefined') {
		eval(FullParamName + "= eval("+ FullParamName +")");
	}
}

for (i=0;i<HM_a_Parameters.length;i++) {
	HM_f_AssignParameters(HM_a_Parameters[i][0],HM_a_Parameters[i][1]);
}

HM_ChildPerCentOver = (isNaN(parseFloat(HM_ChildPerCentOver))) ? null : parseFloat(HM_ChildPerCentOver)/100;

function HM_f_ValidateArray(arrayname){
	return ((typeof eval("window." + arrayname) == "object") && (eval(arrayname).length > 1))
}

if(!window.HM_a_TreesToBuild) {
	HM_a_TreesToBuild = [];
	for(i=1; i<100; i++){
		if(HM_f_ValidateArray(HM_ArrayIDPrefix + i)) HM_a_TreesToBuild[HM_a_TreesToBuild.length] = i;
	}
}

HM_CurrentArray = null;
HM_CurrentTree  = null;
HM_CurrentMenu  = null;
HM_CurrentItem  = null;
HM_a_TopMenus = [];
HM_AreLoaded = false;
HM_AreCreated = false;
HM_BeingCreated = false;
HM_UserOverMenu = false;
HM_HideAllTimer = null;
HM_TotalTrees = 0; 
HM_ZIndex = 5000;

function HM_f_StartIt() {
	if((typeof(document.body) == "undefined") || (document.body == null)) return;
	HM_AreLoaded = true;
	if (HM_ClickKill) {
		HM_f_OtherMouseDown = (document.onmousedown) ? document.onmousedown :  new Function;
    	document.onmousedown = function(){HM_f_PageClick();HM_f_OtherMouseDown()}
    }
	else {
		HM_TopMilliSecondsVisible = HM_TopSecondsVisible * 1000;
	}
    HM_f_MakeTrees();
	HM_f_OtherOnLoad();
}

function HM_f_AssignTreeParameters(arrayvalue,defaultvalue){
	var ValueIsString = (typeof arrayvalue == "string");
	if (ValueIsString) arrayvalue = arrayvalue.trim();
	var ValueIsNull = ((arrayvalue == null) || (typeof arrayvalue == "undefined") || (ValueIsString && arrayvalue.length == 0));
	if(ValueIsNull) return defaultvalue;
	var TestString = arrayvalue;
	if(eval("typeof(TestString)") == "string") {
		if(TestString.charAt(0)=="#")return arrayvalue;
		TestString = TestString.strip()
	}
	if (eval("typeof("+ TestString+" )") != 'undefined') {
		eval("arrayvalue = eval(arrayvalue)");
	}
	return arrayvalue;
}

function HM_f_MakeTrees(){
    HM_BeingCreated = true;
	var TreeParams = null;
	var TreeHasChildren = false;
	var ItemArray = null;

	for(var t=0; t<HM_a_TreesToBuild.length; t++) {
		if(!HM_f_ValidateArray(HM_ArrayIDPrefix + HM_a_TreesToBuild[t])) continue;
		HM_CurrentArray = eval(HM_ArrayIDPrefix + HM_a_TreesToBuild[t]);

		TreeParams = HM_CurrentArray[0];
		TreeHasChildren = false;

		for(var i=1; i<HM_CurrentArray.length; i++) {
			ItemArray = HM_CurrentArray[i];
			if(ItemArray[ItemArray.length-1]) {TreeHasChildren = true; break}
		}

		HM_CurrentTree = {
			MenuWidth        : MenuWidth = HM_f_AssignTreeParameters(TreeParams[0],HM_MenuWidth),
			MenuLeft         : MenuLeft = HM_f_AssignTreeParameters(TreeParams[1],null),
			MenuTop          : MenuTop = HM_f_AssignTreeParameters(TreeParams[2],null),
			ItemWidth        : ItemWidth = MenuWidth - (HM_BorderWidth*2),
			ItemTextWidth    : TreeHasChildren ? (ItemWidth - (HM_ImageSize + HM_ImageHorizSpace + HM_ItemPadding)) : ItemWidth,
			HorizOffsetRight : HorizOffsetRight = (parseInt((HM_ChildPerCentOver != null) ? (HM_ChildPerCentOver  * ItemWidth) : HM_ChildOverlap)),
			HorizOffsetLeft  : (MenuWidth - HorizOffsetRight),
			FontColor        : HM_f_AssignTreeParameters(TreeParams[3],HM_FontColor),
			FontColorOver    : HM_f_AssignTreeParameters(TreeParams[4],HM_FontColorOver),
			BGColor          : HM_f_AssignTreeParameters(TreeParams[5],HM_BGColor),
			BGColorOver      : HM_f_AssignTreeParameters(TreeParams[6],HM_BGColorOver),
			BorderColor      : HM_f_AssignTreeParameters(TreeParams[7],HM_BorderColor),
			SeparatorColor   : HM_f_AssignTreeParameters(TreeParams[8],HM_SeparatorColor),
			TopIsPermanent   : ((MenuLeft == null) || (MenuTop == null)) ? false : HM_f_AssignTreeParameters(TreeParams[9],false),
			TopIsHorizontal  : TopIsHorizontal = HM_f_AssignTreeParameters(TreeParams[10],false),
			TreeIsHorizontal : TreeHasChildren ? HM_f_AssignTreeParameters(TreeParams[11],false) : false,
			PositionUnder    : (!TopIsHorizontal || !TreeHasChildren) ? false : HM_f_AssignTreeParameters(TreeParams[12],false),
			TopImageShow     : TreeHasChildren ? HM_f_AssignTreeParameters(TreeParams[13],true)  : false,
			TreeImageShow    : TreeHasChildren ? HM_f_AssignTreeParameters(TreeParams[14],true)  : false,
			UponDisplay      : HM_f_AssignTreeParameters(TreeParams[15],HM_UponDisplay),
			UponHide         : HM_f_AssignTreeParameters(TreeParams[16],HM_UponHide),
			RightToLeft      : HM_f_AssignTreeParameters(TreeParams[17],HM_RightToLeft)
		}

		HM_CurrentMenu = null;
		HM_f_MakeMenu(HM_a_TreesToBuild[t]);
		HM_a_TopMenus[HM_TotalTrees] = HM_CurrentTree.treeParent;
		HM_TotalTrees++;
		if(HM_CurrentTree.TopIsPermanent){
			with(HM_CurrentTree.treeParent) {
				moveTo(HM_CurrentTree.MenuLeft,HM_CurrentTree.MenuTop);
				style.zIndex = 5000;
			}
			if(HM_IE5M) setTimeout(HM_CurrentTree.treeParent.id + ".fixSize(true)",10);	
			else HM_CurrentTree.treeParent.style.visibility = "visible";
		}
    }

	if(HM_StatusDisplayBuild) status = HM_TotalTrees + " Hierarchical Menu Trees Created";
    HM_AreCreated = true;
    HM_BeingCreated = false;

}

function HM_f_SetItemProperties(itemidsuffix) {
	this.tree        = HM_CurrentTree;
	this.index       = HM_CurrentMenu.itemCount - 1;
	this.tree        = HM_CurrentTree;
	this.isLastItem  = (HM_CurrentMenu.itemCount == HM_CurrentMenu.maxItems);
	this.array	     = HM_CurrentMenu.array[HM_CurrentMenu.itemCount];
	this.dispText    = this.array[0];
	this.linkText    = this.array[1];
	this.permHilite  = eval(this.array[3]);
	this.hasRollover = (!this.permHilite && eval(this.array[2]));
	this.hasMore	 = eval(this.array[4]) && HM_f_ValidateArray(HM_ArrayIDPrefix + itemidsuffix);

	this.childID	 = this.hasMore ? (HM_MenuIDPrefix + itemidsuffix) : null;
	this.child	     = null;
    this.onmouseover = HM_f_ItemOver;
    this.onmouseout  = HM_f_ItemOut;
	this.setItemStyle = HM_f_SetItemStyle;
}

function HM_f_MakeElement(menuid) {
//	if (menuid=="HM_Menu1" || menuid=="HM_Menu1" || menuid=="HM_Menu1") {
//		var MenuObject = document.getElementById("DynMenu1");
//		MenuObject.id = menuid;
//		MenuObject.style.position="relative";
//		MenuObject.style.width = (HM_NS6 ? HM_CurrentTree.ItemWidth : HM_CurrentTree.MenuWidth) + "px";
//		return MenuObject;
//	}
	var MenuObject;
	MenuObject = document.createElement("DIV");
	with(MenuObject){
		id = menuid;
		with(style) {
			position = "absolute";
			visibility = "hidden";
			left = "-500px";
			top = "0px";
			width = (HM_NS6 ? HM_CurrentTree.ItemWidth : HM_CurrentTree.MenuWidth) + "px";
		}
	}
//	var last_ = menuid.lastIndexOf("_");
//	alert(menuid.substring(0,last_));
	document.body.appendChild(MenuObject);
	return MenuObject;
}

function HM_f_MakeMenu(menucount) {
	if(!HM_f_ValidateArray(HM_ArrayIDPrefix + menucount)) return false;
	HM_CurrentArray = eval(HM_ArrayIDPrefix + menucount);

	NewMenu = document.getElementById(HM_MenuIDPrefix + menucount);
	if(!NewMenu){
		NewMenu = HM_f_MakeElement(HM_MenuIDPrefix + menucount);

		if(HM_CurrentMenu) {
			NewMenu.parentMenu = HM_CurrentMenu;
			NewMenu.parentItem = HM_CurrentMenu.itemElement;
			NewMenu.parentItem.child = NewMenu;
			NewMenu.hasParent = true;
			NewMenu.isHorizontal = HM_CurrentTree.TreeIsHorizontal;
			NewMenu.showImage = HM_CurrentTree.TreeImageShow;
		}
		else {
			NewMenu.isHorizontal = HM_CurrentTree.TopIsHorizontal;
			NewMenu.showImage = HM_CurrentTree.TopImageShow;
		}

		HM_CurrentMenu = NewMenu;
		HM_CurrentMenu.array = HM_CurrentArray;
		HM_CurrentMenu.tree  = HM_CurrentTree;
		HM_CurrentMenu.itemCount = 0;
		HM_CurrentMenu.maxItems = HM_CurrentMenu.array.length - 1;
		HM_CurrentMenu.zIndex = ++HM_ZIndex;
		HM_CurrentMenu.showIt = HM_f_ShowIt;
		HM_CurrentMenu.count = menucount;
		HM_CurrentMenu.keepInWindow = HM_f_KeepInWindow;
	    HM_CurrentMenu.onmouseover = HM_f_MenuOver;
	    HM_CurrentMenu.onmouseout = HM_f_MenuOut;
	    HM_CurrentMenu.hideTree = HM_f_HideTree
	    HM_CurrentMenu.hideParents = HM_f_HideParents;
	    HM_CurrentMenu.hideChildren = HM_f_HideChildren;
	    HM_CurrentMenu.hideTop = HM_f_HideTop;
	    HM_CurrentMenu.hideSelf = HM_f_HideSelf;
	    HM_CurrentMenu.hasChildVisible = false;
	    HM_CurrentMenu.isOn = false;
	    HM_CurrentMenu.hideTimer = null;
	    HM_CurrentMenu.currentItem = null;
		HM_CurrentMenu.setMenuStyle = HM_f_SetMenuStyle;
		HM_CurrentMenu.sizeFixed = false;
		HM_CurrentMenu.fixSize = HM_f_FixSize;

		if(HM_IE) HM_CurrentMenu.onselectstart = HM_f_CancelSelect;
	    HM_CurrentMenu.moveTo = HM_f_MoveTo;
		HM_CurrentMenu.setMenuStyle();
	}

	while (HM_CurrentMenu.itemCount < HM_CurrentMenu.maxItems) {
		HM_CurrentMenu.itemCount++;
		HM_CurrentMenu.itemElement = document.getElementById(HM_ItemIDPrefix + menucount + "_" + HM_CurrentMenu.itemCount);
		if(!HM_CurrentMenu.itemElement){
			if(HM_StatusDisplayBuild) status = "Creating Hierarchical Menus: " + menucount + " / " + HM_CurrentMenu.itemCount;
			HM_CurrentMenu.itemElement = HM_f_MakeItemElement(menucount);
		}
		if(HM_CurrentMenu.itemElement.hasMore && (!HM_CreateTopOnly || HM_AreCreated && HM_CreateTopOnly)) {
		    MenuCreated = HM_f_MakeMenu(menucount + "_" + HM_CurrentMenu.itemCount);
    	    if(MenuCreated) {
				HM_CurrentMenu = HM_CurrentMenu.parentMenu;
			}
		}
    }
	if(!HM_IE5M)HM_CurrentMenu.fixSize();
	HM_CurrentTree.treeParent = HM_CurrentTree.startChild = HM_CurrentMenu;
	return HM_CurrentMenu;
}

function HM_f_SetMenuStyle(){
	with(this.style) {
		borderWidth = HM_BorderWidth + "px";
		borderColor = HM_CurrentTree.BorderColor;
		borderStyle = HM_BorderStyle;
		zIndex      = --HM_ZIndex;
		overflow    = "hidden";
		cursor      = "default";
	}
}

function HM_f_MakeItemElement(menucount) {
	var ItemElement = document.createElement("DIV");
	ItemElement.id = HM_ItemIDPrefix + menucount + "_" + HM_CurrentMenu.itemCount;

	ItemElement.style.position = "absolute";
	ItemElement.style.visibility = "inherit";
	HM_CurrentMenu.appendChild(ItemElement);
	ItemElement.setItemProperties = HM_f_SetItemProperties;
	ItemElement.setItemProperties(menucount + "_" + HM_CurrentMenu.itemCount);
	ItemElement.siblingBelow = ItemElement.previousSibling;
	if(ItemElement.linkText) {
		ItemElement.onclick = HM_f_LinkIt;
		if(HM_ShowLinkCursor)ItemElement.style.cursor = HM_NS6 ? "pointer" : "hand";
	}
	ItemElement.menu = HM_CurrentMenu;
	var FullPadding  = (HM_ItemPadding*2) + HM_ImageSize + HM_ImageHorizSpace;
    if(ItemElement.hasMore && HM_CurrentMenu.showImage) {
		var ImageElement = document.createElement("IMG");
		with(ImageElement){
			src = HM_CurrentTree.RightToLeft ? HM_ImageSrcLeft : HM_ImageSrc;
			removeAttribute("height");
			hspace = (!HM_CurrentTree.RightToLeft && HM_IE5W) ? HM_ItemPadding : 0;

			vspace = 0;
			width = HM_ImageSize;
			with(style) {
				if(HM_CurrentTree.RightToLeft) {
					position = "absolute";
					top = (HM_ItemPadding + HM_ImageVertSpace) + "px";
					left = (HM_ItemPadding + HM_ImageHorizSpace) + "px";
				}
				else {
					position = "relative";
					marginTop = HM_ImageVertSpace + "px";
					if(HM_IE5W) {
						marginRight = -FullPadding + "px";
					}
					else marginRight = -(HM_ImageSize + HM_ItemPadding) +"px";
					if(HM_NS6) cssFloat = "right";
					else styleFloat = "right";
				}	
			}
		}
		ItemElement.imgLyr = ImageElement;
	}
	ItemElement.innerHTML = ItemElement.dispText;
	if(ImageElement) ItemElement.insertBefore(ImageElement,ItemElement.firstChild);
	ItemElement.setItemStyle();
	return ItemElement;
}

function HM_f_SetItemStyle() {
	with(this.style){
		backgroundColor = (this.permHilite) ? HM_CurrentTree.BGColorOver : HM_CurrentTree.BGColor;
		color = (this.permHilite) ? HM_CurrentTree.FontColorOver : HM_CurrentTree.FontColor;
		padding = HM_ItemPadding +"px";
		font = ((HM_FontBold) ? "bold " : "normal ") + HM_FontSize + "pt " + HM_FontFamily;
		fontStyle = (HM_FontItalic) ? "italic" : "normal";
		if(HM_IE) overflow = "hidden";

		if(HM_CurrentMenu.showImage)	{
			var FullPadding  = (HM_ItemPadding*2) + HM_ImageSize + HM_ImageHorizSpace;
			if (HM_CurrentTree.RightToLeft) paddingLeft = FullPadding + "px";
			else paddingRight = FullPadding + "px";
		}
		if(!this.isLastItem) {
			var SeparatorString = HM_SeparatorSize + "px solid " + this.tree.SeparatorColor;
			if (this.menu.isHorizontal) borderRight = SeparatorString;
			else borderBottom = SeparatorString;
		}

		if(HM_IE) width = HM_CurrentTree.ItemWidth + "px";
		else width = (HM_CurrentTree.ItemWidth - (parseInt(paddingLeft) + parseInt(paddingRight))) + "px";

		if(this.menu.isHorizontal){
			if(HM_IE){
				if(this.isLastItem) width = (HM_CurrentTree.MenuWidth - HM_BorderWidth - HM_SeparatorSize) + "px"
				else width = (HM_CurrentTree.MenuWidth - HM_BorderWidth) + "px"
			}
			if(HM_NS6) width = (HM_CurrentTree.MenuWidth - HM_BorderWidth - parseInt(paddingLeft) - parseInt(paddingRight) - HM_SeparatorSize) + "px";
			top = "0px";
			if(HM_IE) left = (this.index * (HM_CurrentTree.MenuWidth - HM_BorderWidth)) + "px";
			if(HM_NS6) left = ((this.index * parseInt(width)) + ((HM_SeparatorSize * this.index)))  + ((parseInt(paddingLeft) + parseInt(paddingRight)) * this.index) + "px";
			var LeftAndWidth = parseInt(left) + parseInt(width);
			this.menu.style.width = LeftAndWidth + (HM_IE ? (HM_BorderWidth * 2) : (parseInt(paddingLeft) + parseInt(paddingRight))) + "px"
		}
		else {
			left = "0px";
		}
	}
}

function HM_f_FixSize(makevis){
	if(this.isHorizontal) {
		var MaxItemHeight = 0;
	    for(i=0; i<this.childNodes.length; i++) {
	        var TempItem = this.childNodes[i];
		    if (TempItem.index) {
				var SiblingHeight = TempItem.siblingBelow.offsetHeight - (HM_NS6 ? HM_ItemPadding * 2 : 0);
				MaxItemHeight = Math.max(MaxItemHeight,SiblingHeight);
			}
	       	else{
				MaxItemHeight = TempItem.offsetHeight;
			}
		}
	    for(i=0; i<this.childNodes.length; i++) {
			this.childNodes[i].style.height = MaxItemHeight +"px";
		}
		this.style.height = MaxItemHeight + (HM_NS6 ? 0 : HM_BorderWidth * 2) + "px";
	}
	else {
	    for(i=0; i<this.childNodes.length; i++) {
	        var TempItem = this.childNodes[i];
		    if (TempItem.index) {
				var SiblingHeight =(TempItem.siblingBelow.offsetHeight);
				TempItem.style.top = parseInt(TempItem.siblingBelow.style.top) + SiblingHeight + "px";
			}
			else TempItem.style.top = "0px";
		}
		this.style.height = parseInt(TempItem.style.top) + (HM_IE5W ? TempItem.scrollHeight : TempItem.offsetHeight) + (HM_NS6 ? 0 : (HM_BorderWidth * 2)) + "px";
	}

	this.sizeFixed = true;
	if(makevis)this.style.visibility = "visible";
}

function HM_f_PopUp(menuname,e){
	if(HM_IE) e = event;
    if (!HM_AreLoaded) return;
	menuname = menuname.replace("elMenu",HM_MenuIDPrefix);
	var TempMenu = document.getElementById(menuname);
	if(!TempMenu)return;
	HM_CurrentMenu = TempMenu;
	if (HM_ClickStart) {
		var ClickElement = (HM_IE) ? e.srcElement : e.target;
		if(HM_NS6) {
			while(ClickElement.tagName==null){
				ClickElement = ClickElement.parentNode;
			}
		}
		ClickElement.onclick = HM_f_PopMenu;
    }
	else HM_f_PopMenu(e);
}

function HM_f_PopMenu(e){
	if(HM_IE) e = event;
    if (!HM_AreLoaded || !HM_AreCreated) return true;
    if (HM_ClickStart && e.type != "click") return true;
    HM_f_HideAll();
    HM_CurrentMenu.hasParent = false;
	HM_CurrentMenu.tree.startChild = HM_CurrentMenu;
	var EventX = (HM_IE) ? (e.clientX + document.body.scrollLeft) : e.pageX;
	var EventY = (HM_IE) ? (e.clientY + document.body.scrollTop)  : e.pageY;
	HM_CurrentMenu.xPos = (HM_CurrentMenu.tree.MenuLeft!=null) ? HM_CurrentMenu.tree.MenuLeft : EventX;
   	HM_CurrentMenu.yPos = (HM_CurrentMenu.tree.MenuTop!=null)  ? HM_CurrentMenu.tree.MenuTop  : EventY;
    HM_CurrentMenu.keepInWindow();
    HM_CurrentMenu.moveTo(HM_CurrentMenu.xPos,HM_CurrentMenu.yPos);
    HM_CurrentMenu.isOn = true;
    HM_CurrentMenu.showIt(true);
    return false;
}

function HM_f_MenuOver() {
	if(!this.tree.startChild){this.tree.startChild = this}
	if(this.tree.startChild == this) HM_f_HideAll(this)
    this.isOn = true;
    HM_UserOverMenu = true;
    HM_CurrentMenu = this;
    if (this.hideTimer) clearTimeout(this.hideTimer);
}

function HM_f_MenuOut() {
	if(HM_IE && event.srcElement.contains(event.toElement)) return;
    this.isOn = false;
    HM_UserOverMenu = false;
    if(HM_StatusDisplayLink) status = "";
    if(!HM_ClickKill) HM_HideAllTimer = setTimeout("HM_CurrentMenu.hideTree()",10);  
}

function HM_f_ItemOver(){
    if (HM_KeepHilite) {
        if (this.menu.currentItem && this.menu.currentItem != this && this.menu.currentItem.hasRollover) {
			with(this.menu.currentItem.style){
				backgroundColor = this.tree.BGColor;
            	color = this.tree.FontColor
			}
        }
    }
	if(HM_IE && event.srcElement.id == "HM_ImMore") return;
	if(this.hasRollover) {
	    this.style.backgroundColor = this.tree.BGColorOver;
	    this.style.color = this.tree.FontColorOver;
	}

    if(HM_StatusDisplayLink) status = this.linkText;
    this.menu.currentItem = this;

	if (this.menu.hasChildVisible) {
		if(this.menu.visibleChild == this.child && this.menu.visibleChild.hasChildVisible) this.menu.visibleChild.hideChildren(this);
		else this.menu.hideChildren(this);
    }

    if (this.hasMore) { 
		if(!this.child) {
			HM_CurrentTree = this.tree;
			HM_CurrentMenu = this.menu;
			HM_CurrentItem = this;
			HM_CurrentMenu.itemElement = this;
			this.child = HM_f_MakeMenu(this.menu.count + "_"+(this.index+1));
			this.tree.treeParent = this.menu;
			this.tree.startChild = this.menu;
		}
		if (this.tree.PositionUnder && (this.menu == this.tree.treeParent)) {
			this.child.xPos = parseInt(this.menu.style.left) + parseInt(this.style.left);
			this.child.yPos = parseInt(this.menu.style.top) + this.menu.offsetHeight - (HM_BorderWidth);
		}
		else {
			if(HM_IE5M) {
				this.oL = parseInt(this.menu.style.left) - HM_BorderWidth;
				this.oL += this.offsetLeft;
				this.oT =  parseInt(this.menu.style.top)  -HM_BorderWidth;
				this.oT += this.offsetTop;
			} else if (HM_NS6) {
				// Fix for NS6.
				this.oL = parseInt(this.menu.style.left) - HM_BorderWidth;
				this.oL += this.offsetLeft;
				this.oT =  parseInt(this.menu.style.top)  -HM_BorderWidth;
				this.oT += this.offsetTop;
			} else {
				this.oL = (HM_IE) ? parseInt(this.menu.style.left) : -HM_BorderWidth;
				this.oL += this.offsetLeft;
				this.oT = (HM_IE) ? parseInt(this.menu.style.top) : -HM_BorderWidth;
				this.oT += this.offsetTop;
			}
			if(this.tree.RightToLeft) {
				this.child.xPos = this.oL + (this.tree.HorizOffsetRight - this.child.offsetWidth);
			}
			else {		
				this.child.xPos = this.oL + this.tree.HorizOffsetLeft;
			}
			this.child.yPos = this.oT + HM_ChildOffset + HM_BorderWidth;
		}
        if(!this.tree.PositionUnder || this.menu!=this.tree.treeParent)	this.child.keepInWindow();
	this.child.moveTo(this.child.xPos,this.child.yPos);

//	alert(this.menu.style.left + " " + this.style.left + "\n" + this.menu.style.top + this.style.top);
//	this.child.moveTo(0,0);

        this.menu.hasChildVisible = true;
        this.menu.visibleChild = this.child;
        this.child.showIt(true);
    }
}

function HM_f_ItemOut() {
	if (HM_IE && (event.srcElement.contains(event.toElement)
	  || (event.fromElement.tagName=="IMG" && event.toElement.contains(event.fromElement))))
		  return;
    if ( (!HM_KeepHilite || ((this.tree.TopIsPermanent && (this.tree.treeParent==this)) && !this.menu.hasChildVisible)) && this.hasRollover) {
        with(this.style) {
			backgroundColor = this.tree.BGColor;
        	color = this.tree.FontColor
		}
    }
}

function HM_f_MoveTo(xPos,yPos) {
	this.style.left = xPos + "px";
	this.style.top = yPos + "px";
}

function HM_f_ShowIt(on) {
	if (!(this.tree.TopIsPermanent && (this.tree.treeParent==this))) {
		if(!this.hasParent || (this.hasParent && this.tree.TopIsPermanent)) {
			var IsVisible = (this.style.visibility == "visible");
			if ((on && !IsVisible) || (!on && IsVisible))
				eval(on ? this.tree.UponDisplay : this.tree.UponHide)
		}
		if(HM_IE5M && on && !this.sizeFixed) this.fixSize(false); 
		this.style.visibility = (on) ? "visible" : "hidden";
	}
    if (HM_KeepHilite && this.currentItem && this.currentItem.hasRollover) {
		with(this.currentItem.style){
			backgroundColor = this.tree.BGColor;
			color = this.tree.FontColor;
		}
    }
    this.currentItem = null;
}

function HM_f_KeepInWindow() {
    var ExtraSpace     = 10;
	var WindowLeftEdge = (HM_IE) ? document.body.scrollLeft   : window.pageXOffset;
	var WindowTopEdge  = (HM_IE) ? document.body.scrollTop    : window.pageYOffset;
	var WindowWidth    = (HM_IE) ? document.body.clientWidth  : window.innerWidth;
	var WindowHeight   = (HM_IE) ? document.body.clientHeight : window.innerHeight;
	var WindowRightEdge  = (WindowLeftEdge + WindowWidth) - ExtraSpace;
	var WindowBottomEdge = (WindowTopEdge + WindowHeight) - ExtraSpace;

	var MenuLeftEdge = this.xPos;
	var MenuRightEdge = MenuLeftEdge + this.offsetWidth;
	var MenuBottomEdge = this.yPos + this.offsetHeight;

	if (this.hasParent) {
		var ParentLeftEdge = parseInt(this.parentMenu.style.left);
	}
	if (MenuRightEdge > WindowRightEdge) {
		if (this.hasParent) {
			this.xPos = ParentLeftEdge + this.tree.HorizOffsetRight - this.offsetWidth;	
		}
		else {
			dif = MenuRightEdge - WindowRightEdge;
			this.xPos -= dif;
		}
	}

	if (MenuBottomEdge > WindowBottomEdge) {
		dif = MenuBottomEdge - WindowBottomEdge;
		this.yPos -= dif;
	}

	if (MenuLeftEdge < WindowLeftEdge) {
		if (this.hasParent) {
			this.xPos = ParentLeftEdge + this.tree.HorizOffsetLeft;
		}
		else {this.xPos = 5}
	}       
}

function HM_f_LinkIt() {
	HM_f_HideAll();
    if (this.linkText.indexOf("javascript:")!=-1) eval(this.linkText)
    else location.href = this.linkText;
}

function HM_f_PopDown(menuname){
    if (!HM_AreLoaded || !HM_AreCreated) return;
	menuname = menuname.replace("elMenu",HM_MenuIDPrefix);
    var MenuToHide = document.getElementById(menuname);
	if(!MenuToHide)return;
    MenuToHide.isOn = false;
    if (!HM_ClickKill) MenuToHide.hideTop();
}

function HM_f_HideAll(callingmenu) {
	for(var i=0; i<HM_TotalTrees; i++) {
        var TopMenu = HM_a_TopMenus[i].tree.startChild;
		if(TopMenu == callingmenu)continue
        TopMenu.isOn = false;
        if (TopMenu.hasChildVisible) TopMenu.hideChildren();
        TopMenu.showIt(false);
    }    
}

function HM_f_HideTree() { 
    HM_HideAllTimer = null;
    if (HM_UserOverMenu) return;
    if (this.hasChildVisible) this.hideChildren();
    this.hideParents();
}

function HM_f_HideTop() {
	TopMenuToHide = this;
    (HM_ClickKill) ? TopMenuToHide.hideSelf() : (this.hideTimer = setTimeout("TopMenuToHide.hideSelf()",HM_TopMilliSecondsVisible));
}

function HM_f_HideSelf() {
    this.hideTimer = null;
    if (!this.isOn && !HM_UserOverMenu) this.showIt(false);
}

function HM_f_HideParents() {
    var TempMenu = this;
    while(TempMenu.hasParent) {
        TempMenu.showIt(false);
        TempMenu.parentMenu.isOn = false;        
        TempMenu = TempMenu.parentMenu;
    }
    TempMenu.hideTop();
}

function HM_f_HideChildren(callingitem) {
    var TempMenu = this.visibleChild;
    while(TempMenu.hasChildVisible) {
        TempMenu.visibleChild.showIt(false);
        TempMenu.hasChildVisible = false;
        TempMenu = TempMenu.visibleChild;
    }

    if (!this.isOn || !callingitem.hasMore || this.visibleChild != this.child) {
        this.visibleChild.showIt(false);
        this.hasChildVisible = false;
    }
}

function HM_f_CancelSelect(){return false}

function HM_f_PageClick() {
    if (!HM_UserOverMenu && HM_CurrentMenu!=null && !HM_CurrentMenu.isOn) HM_f_HideAll();
}

popUp = HM_f_PopUp;
popDown = HM_f_PopDown;

HM_f_OtherOnLoad = (window.onload) ? window.onload :  new Function;
window.onload = function(){setTimeout("HM_f_StartIt()",10)};
//end