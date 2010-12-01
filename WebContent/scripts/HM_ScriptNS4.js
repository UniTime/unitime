/*HM_ScriptNS4.js
* by Peter Belesis. v4.0.7 010323
* Copyright (c) 2001 Peter Belesis. All Rights Reserved.
* Originally published and documented at http://www.dhtmlab.com/
* You may use this code on a public Web site only if this entire
* copyright notice appears unchanged and you publicly display
* a link to http://www.dhtmlab.com/.
*
* Contact peter.belesis@btclick.com for all other uses.
*/

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
	["SeparatorSize",      1],
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
	["CreateTopOnly",      true],
	["ShowLinkCursor",     false],
	["NSFontOver",		   true]
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

function HM_f_Initialize() {
    if(HM_AreCreated) {
		for(var i=0; i<HM_TotalTrees; i++) {
			var TopMenu = HM_a_TopMenus[i];
			clearTimeout(TopMenu.hideTimer);
			TopMenu.hideTimer = null;
        }
        clearTimeout(HM_HideAllTimer);
    }
	HM_AreCreated = false;
	HM_BeingCreated = false;
	HM_UserOverMenu = false;
	HM_CurrentMenu = null;
	HM_HideAllTimer = null;
	HM_TotalTrees = 0;
	HM_a_TopMenus = [];
}

Layer.prototype.showIt = HM_f_ShowIt;
Layer.prototype.keepInWindow = HM_f_KeepInWindow;
Layer.prototype.hideTree = HM_f_HideTree
Layer.prototype.hideParents = HM_f_HideParents;
Layer.prototype.hideChildren = HM_f_HideChildren;
Layer.prototype.hideTop = HM_f_HideTop;
Layer.prototype.hideSelf = HM_f_HideSelf;
Layer.prototype.hasChildVisible = false;
Layer.prototype.isOn = false;
Layer.prototype.hideTimer = null;
Layer.prototype.currentItem = null;
Layer.prototype.itemSetup = HM_f_ItemSetup;
Layer.prototype.itemCount = 0;
Layer.prototype.child = null;
Layer.prototype.isWritten = false;

HM_NS_OrigWidth  = window.innerWidth;
HM_NS_OrigHeight = window.innerHeight;

window.onresize = function (){
    if (window.innerWidth == HM_NS_OrigWidth && window.innerHeight == HM_NS_OrigHeight) return;
    HM_f_Initialize();
    window.location.reload();
}

function HM_f_StartIt() {
	HM_AreLoaded = true;
	if (HM_ClickKill) {
		HM_f_OtherMouseDown = (document.onmousedown) ? document.onmousedown :  new Function;
		document.captureEvents(Event.MOUSEDOWN);
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
			HorizOffsetRight : HorizOffsetRight = (parseInt((HM_ChildPerCentOver != null) ? (HM_ChildPerCentOver  * ItemWidth) : HM_ChildOverlap)) - HM_ItemPadding,
			HorizOffsetLeft  : (MenuWidth - HorizOffsetRight) - (HM_BorderWidth*2),
			FontColor        : FontColor = HM_f_AssignTreeParameters(TreeParams[3],HM_FontColor),
			FontColorOver    : FontColorOver = HM_f_AssignTreeParameters(TreeParams[4],HM_FontColorOver),
			BGColor          : HM_f_AssignTreeParameters(TreeParams[5],HM_BGColor),
			BGColorOver      : HM_f_AssignTreeParameters(TreeParams[6],HM_BGColorOver),
			BorderColor      : HM_f_AssignTreeParameters(TreeParams[7],HM_BorderColor),
			TopIsPermanent   : ((MenuLeft == null) || (MenuTop == null)) ? false : HM_f_AssignTreeParameters(TreeParams[9],false),
			TopIsHorizontal  : TopIsHorizontal = HM_f_AssignTreeParameters(TreeParams[10],false),
			TreeIsHorizontal : TreeHasChildren ? HM_f_AssignTreeParameters(TreeParams[11],false) : false,
			PositionUnder    : (!TopIsHorizontal || !TreeHasChildren) ? false : HM_f_AssignTreeParameters(TreeParams[12],false),
			TopImageShow     : TreeHasChildren ? HM_f_AssignTreeParameters(TreeParams[13],true)  : false,
			TreeImageShow    : TreeHasChildren ? HM_f_AssignTreeParameters(TreeParams[14],true)  : false,
			UponDisplay      : HM_f_AssignTreeParameters(TreeParams[15],HM_UponDisplay),
			UponHide         : HM_f_AssignTreeParameters(TreeParams[16],HM_UponHide),
			RightToLeft      : HM_f_AssignTreeParameters(TreeParams[17],HM_RightToLeft),
			NSFontOver		 : HM_NSFontOver ? (FontColor != FontColorOver) : false
		}

		HM_CurrentMenu = null;
		HM_f_MakeMenu(HM_a_TreesToBuild[t]);
		HM_a_TopMenus[HM_TotalTrees] = HM_CurrentTree.treeParent;
		HM_TotalTrees++;
		if(HM_CurrentTree.TopIsPermanent){
			with(HM_CurrentTree.treeParent) {
				moveTo(HM_CurrentTree.MenuLeft,HM_CurrentTree.MenuTop);
				visibility = "show";
			}
		}
    }

	if(HM_StatusDisplayBuild) status = HM_TotalTrees + " Hierarchical Menu Trees Created";
    HM_AreCreated = true;
    HM_BeingCreated = false;
}

function HM_f_GetItemHtmlStr(arraystring){
	var TempString = arraystring;
	if (HM_FontBold) TempString = TempString.bold();
	if (HM_FontItalic) TempString = TempString.italics();
	TempString = "<FONT FACE='" + HM_FontFamily + "' POINT-SIZE=" + HM_FontSize + ">" + TempString + "</FONT>";
	var TempStringOver = TempString.fontcolor(HM_CurrentTree.FontColorOver);
	TempString = TempString.fontcolor(HM_CurrentTree.FontColor);
	return [TempString,TempStringOver];
}

function HM_f_MakeMenu(menucount) {
	if(!HM_f_ValidateArray(HM_ArrayIDPrefix + menucount)) return false;
	HM_CurrentArray = eval(HM_ArrayIDPrefix + menucount);

	NewMenu = eval("window." + HM_MenuIDPrefix + menucount);
	if(!NewMenu) {
		eval(HM_MenuIDPrefix + menucount + " = new Layer(HM_CurrentTree.MenuWidth,window)")
		NewMenu = eval(HM_MenuIDPrefix + menucount);
	
		if(HM_CurrentMenu) {
			NewMenu.parentMenu = HM_CurrentMenu;
			NewMenu.parentItem = HM_CurrentItem;
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
		HM_CurrentMenu.count = menucount;
		HM_CurrentMenu.tree  = HM_CurrentTree;
		HM_CurrentMenu.array = HM_CurrentArray;
		HM_CurrentMenu.maxItems = HM_CurrentArray.length - 1;
		HM_CurrentMenu.bgColor = HM_CurrentTree.BorderColor;
	    HM_CurrentMenu.onmouseover = HM_f_MenuOver;
	    HM_CurrentMenu.onmouseout = HM_f_MenuOut;
		HM_CurrentMenu.moveTo(0,0);
	}

	while (HM_CurrentMenu.itemCount < HM_CurrentMenu.maxItems) {
		HM_CurrentMenu.itemCount++;
		HM_CurrentItem = eval("window." + HM_ItemIDPrefix + menucount + "_" + HM_CurrentMenu.itemCount);
		if(!HM_CurrentItem) {
			eval(HM_ItemIDPrefix + menucount + "_" + HM_CurrentMenu.itemCount + " = new Layer(HM_CurrentTree.ItemWidth - (HM_ItemPadding*2),HM_CurrentMenu)")
			if(HM_StatusDisplayBuild) status = "Creating Hierarchical Menus: " + menucount + " / " + HM_CurrentMenu.itemCount;
			HM_CurrentItem = eval(HM_ItemIDPrefix + menucount + "_" + HM_CurrentMenu.itemCount);
			HM_CurrentItem.itemSetup(menucount + "_" + HM_CurrentMenu.itemCount);
		}
		if(HM_CurrentItem.hasMore && (!HM_CreateTopOnly || HM_AreCreated && HM_CreateTopOnly)) {
	       	MenuCreated = HM_f_MakeMenu(menucount + "_" + HM_CurrentMenu.itemCount);
           	if(MenuCreated) {
				HM_CurrentMenu =  HM_CurrentMenu.parentMenu;
				HM_CurrentArray = HM_CurrentMenu.array;
			}
		}
    }
	HM_CurrentMenu.itemCount = 0;
	if (HM_CurrentMenu.isHorizontal) {
	    HM_CurrentMenu.clip.right = HM_CurrentMenu.lastItem.left + HM_CurrentMenu.lastItem.clip.right + HM_BorderWidth;
	}
	else {
	    HM_CurrentMenu.clip.right = HM_CurrentTree.MenuWidth;
	}
    HM_CurrentMenu.clip.bottom = HM_CurrentMenu.lastItem.top + HM_CurrentMenu.lastItem.clip.bottom + HM_BorderWidth;
	HM_CurrentTree.treeParent = HM_CurrentTree.startChild = HM_CurrentMenu;
	return HM_CurrentMenu;
}

function HM_f_ItemSetup(itemidsuffix) {
	this.menu = HM_CurrentMenu;
	this.tree = HM_CurrentTree;
	this.index = HM_CurrentMenu.itemCount - 1;
	this.array = HM_CurrentArray[HM_CurrentMenu.itemCount];
	this.dispText = this.array[0];
	this.linkText = this.array[1];
	this.permHilite  = eval(this.array[3]);
	this.hasRollover = (!this.permHilite && eval(this.array[2]));
	this.hasMore	 = eval(this.array[4]) && HM_f_ValidateArray(HM_ArrayIDPrefix + itemidsuffix);
	var HtmlStrings = HM_f_GetItemHtmlStr(this.dispText);
	this.htmStr = HtmlStrings[0];
	this.htmStrOver = HtmlStrings[1];
	this.visibility = "inherit";
    this.onmouseover = HM_f_ItemOver;
   	this.onmouseout  = HM_f_ItemOut;
	this.menu.lastItem = this;

    if (this.linkText) {
		this.captureEvents(Event.MOUSEUP);
       	this.onmouseup = HM_f_LinkIt;
    }
	if (this.menu.isHorizontal) {
    	if (this.index) this.left = this.siblingBelow.left + this.siblingBelow.clip.width + HM_SeparatorSize;
		else this.left = (HM_BorderWidth + HM_ItemPadding);
		this.top = (HM_BorderWidth + HM_ItemPadding);
	}
	else {
		this.left = (HM_BorderWidth + HM_ItemPadding);
	    if (this.index) this.top = this.siblingBelow.top + this.siblingBelow.clip.height + HM_SeparatorSize;
    	else this.top = (HM_BorderWidth + HM_ItemPadding)
	}
    this.clip.top = this.clip.left = -HM_ItemPadding;
    this.clip.right = this.tree.ItemWidth - HM_ItemPadding;
	this.bgColor = this.permHilite ? this.tree.BGColorOver : this.tree.BGColor;

	this.txtLyrOff = new Layer(HM_CurrentTree.ItemTextWidth - (HM_ItemPadding*2),this);
	with(this.txtLyrOff) {
		document.write(this.permHilite ? this.htmStrOver : this.htmStr);
		document.close();
		if (HM_CurrentTree.RightToLeft && this.menu.showImage) left = HM_ItemPadding + HM_ImageSize + HM_ImageHorizSpace;
		visibility = "inherit";
	}

	if(this.tree.NSFontOver) {
		if(!this.permHilite){
			this.txtLyrOn = new Layer(HM_CurrentTree.ItemTextWidth - (HM_ItemPadding*2),this);
			with(this.txtLyrOn) {
				if (HM_CurrentTree.RightToLeft && this.menu.showImage) left = HM_ItemPadding + HM_ImageSize + HM_ImageHorizSpace;
				visibility = "hide";
			}
		}
	}

	this.fullClip = this.txtLyrOff.document.height + (HM_ItemPadding * 2);
	if(this.menu.isHorizontal) {
		if(this.index) {
			var SiblingHeight = this.siblingBelow.clip.height;
			this.fullClip = Math.max(SiblingHeight,this.fullClip);
			if(this.fullClip > SiblingHeight) {
				var SiblingPrevious = this.siblingBelow;
				while(SiblingPrevious != null) {
					SiblingPrevious.clip.height = this.fullClip;
					SiblingPrevious = SiblingPrevious.siblingBelow;
				}
			}
		}
	}
	this.clip.height = this.fullClip;

	this.dummyLyr = new Layer(100,this);
	with(this.dummyLyr) {
		left = top = -HM_ItemPadding;
		clip.width = this.clip.width;
		clip.height = this.clip.height;
		visibility = "inherit";
	}

	if(this.hasMore && HM_CurrentMenu.showImage) {
		this.imgLyr = new Layer(HM_ImageSize,this);
		with(this.imgLyr) {
			document.write("<IMG SRC='" + (HM_CurrentTree.RightToLeft ? HM_ImageSrcLeft : HM_ImageSrc) + "' WIDTH=" + HM_ImageSize + " VSPACE=0 HSPACE=0 BORDER=0>");
			document.close();
			moveBelow(this.txtLyrOff);
			left = (HM_CurrentTree.RightToLeft) ? HM_ImageHorizSpace : this.tree.ItemWidth - (HM_ItemPadding * 2) - HM_ImageSize - HM_ImageHorizSpace;
			top = HM_ImageVertSpace;
			visibility = "inherit";
		}
	}
}

function HM_f_PopUp(menuname,e){
    if (!HM_AreLoaded) return;
	menuname = menuname.replace("elMenu",HM_MenuIDPrefix);
	var TempMenu = eval("window."+menuname);
	if(!TempMenu)return;
	HM_CurrentMenu = TempMenu;
	if (HM_ClickStart) {
		var ClickElement = e.target;
		ClickElement.onclick = HM_f_PopMenu;
    }
	else HM_f_PopMenu(e);
}

function HM_f_PopMenu(e){
    if (!HM_AreLoaded || !HM_AreCreated) return true;
    if (HM_ClickStart && e.type != "click") return true;
    HM_f_HideAll();
    HM_CurrentMenu.hasParent = false;
	HM_CurrentMenu.tree.startChild = HM_CurrentMenu;
	HM_CurrentMenu.xPos = (HM_CurrentMenu.tree.MenuLeft!=null) ? HM_CurrentMenu.tree.MenuLeft : e.pageX;
   	HM_CurrentMenu.yPos = (HM_CurrentMenu.tree.MenuTop!=null)  ? HM_CurrentMenu.tree.MenuTop  : e.pageY;
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
    this.isOn = false;
    HM_UserOverMenu = false;
    if(HM_StatusDisplayLink) status = "";
    if(!HM_ClickKill) HM_HideAllTimer = setTimeout("HM_CurrentMenu.hideTree()",10);  
}

function HM_f_ItemOver(){
    if (HM_KeepHilite) {
        if (this.menu.currentItem && this.menu.currentItem != this && this.menu.currentItem.hasRollover) {
            with(this.menu.currentItem){
				bgColor = this.tree.BGColor;
				if(this.tree.NSFontOver) {
    	    	    txtLyrOff.visibility = "inherit";
					txtLyrOn.visibility = "hide";
				}
			}
        }
    }
	if(this.hasRollover) {
	    this.bgColor = this.tree.BGColorOver;
		if(this.tree.NSFontOver) {
			if(!this.txtLyrOn.isWritten){
				this.txtLyrOn.document.write(this.htmStrOver);
				this.txtLyrOn.document.close();
				this.txtLyrOn.isWritten = true;
			}
			this.txtLyrOff.visibility = "hide";
			this.txtLyrOn.visibility = "inherit";
		}
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
			this.child = HM_f_MakeMenu(this.menu.count + "_"+(this.index+1));
			this.tree.treeParent = this.menu;
			this.tree.startChild = this.menu;
		}
		if (this.tree.PositionUnder && (this.menu == this.tree.treeParent)) {
			this.child.xPos = this.pageX + this.clip.left - HM_BorderWidth;
			this.child.yPos = this.menu.top + this.menu.clip.height - HM_BorderWidth;
		}
		else {
			this.oL = this.pageX + this.clip.left;
			this.child.offsetWidth = this.child.clip.width;
			this.oT = this.pageY + this.clip.top - HM_BorderWidth;
			if(this.tree.RightToLeft) {
				this.child.xPos = this.oL + (this.tree.HorizOffsetRight - this.child.offsetWidth);
			}
			else {		
				this.child.xPos = this.oL + this.tree.HorizOffsetLeft;
			}
			this.child.yPos = this.oT + HM_ChildOffset + HM_BorderWidth;
		}
        if(!this.tree.PositionUnder || this.menu!=this.tree.treeParent) this.child.keepInWindow();
		this.child.moveTo(this.child.xPos,this.child.yPos);
        this.menu.hasChildVisible = true;
        this.menu.visibleChild = this.child;
        this.child.showIt(true);
    }
}

function HM_f_ItemOut() {
    if ( (!HM_KeepHilite || ((this.tree.TopIsPermanent && (this.tree.treeParent==this)) && !this.menu.hasChildVisible)) && this.hasRollover) {
		with(this){
			bgColor = this.tree.BGColor;
			if(this.tree.NSFontOver) {
				txtLyrOff.visibility = "inherit";
				txtLyrOn.visibility = "hide";
			}
		}
    }
	if(!HM_ClickKill && !HM_UserOverMenu) {
        HM_HideAllTimer = setTimeout("HM_CurrentMenu.hideTree()",10);  
    }
}

function HM_f_ShowIt(on) {
	if (!(this.tree.TopIsPermanent && (this.tree.treeParent==this))) {
		if(!this.hasParent || (this.hasParent && this.tree.TopIsPermanent)) {
			if (on == this.hidden)
				eval(on ? this.tree.UponDisplay : this.tree.UponHide)
		}
		this.visibility = on ? "show" : "hide";
	}
    if (HM_KeepHilite && this.currentItem && this.currentItem.hasRollover) {
        with(this.currentItem){
			bgColor = this.tree.BGColor;
			if(this.tree.NSFontOver) {
				txtLyrOff.visibility = "inherit";
				txtLyrOn.visibility = "hide";
			}
		}
	}
    this.currentItem = null;
}

function HM_f_KeepInWindow() {
    var ExtraSpace     = 10;
	var WindowLeftEdge = window.pageXOffset;
	var WindowTopEdge  = window.pageYOffset;
	var WindowWidth    = window.innerWidth;
	var WindowHeight   = window.innerHeight;
	var WindowRightEdge  = (WindowLeftEdge + WindowWidth) - ExtraSpace;
	var WindowBottomEdge = (WindowTopEdge + WindowHeight) - ExtraSpace;

	var MenuLeftEdge = this.xPos;
	var MenuRightEdge = MenuLeftEdge + this.clip.width;
	var MenuBottomEdge = this.yPos + this.clip.height;

	if (this.hasParent) {
		var ParentLeftEdge = this.parentMenu.pageX;
		this.offsetWidth = this.clip.width;
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
    var MenuToHide = eval("window."+menuname);//document.layers[menuname];
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

function HM_f_PageClick() {
    if (!HM_UserOverMenu && HM_CurrentMenu!=null && !HM_CurrentMenu.isOn) HM_f_HideAll();
}

popUp = HM_f_PopUp;
popDown = HM_f_PopDown;

HM_f_OtherOnLoad = (window.onload) ? window.onload :  new Function;
window.onload = HM_f_StartIt;


//end