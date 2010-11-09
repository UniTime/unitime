/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/

var defaultMenuItems = "";

function displayLoading() {
	if (parent.__idContentFrame && parent.__idContentFrame.displayElement) {
		parent.__idContentFrame.displayElement('contentMain', false);
		parent.__idContentFrame.displayElement('loadingMain', true);
	}
}

function toggle(item) {
	var obj=document.getElementById("__idMenuDiv"+item);
	var img=document.getElementById("__idMenuImg"+item);
	var visible=(obj.style.display!="none");
	setToggle(item,!visible);
	if (visible) {
		obj.style.display="none";
		img.src="images/expand_node_btn.gif";
		img.title='Expand '+img.name;
	} else {
		obj.style.display="block";
		img.src="images/collapse_node_btn.gif";
		img.title='Collapse '+img.name;
	}
}

function menu_item(id,name,title,page,type) {
	menu_item(id,name,title,page,type,'__idContentFrame');
}

function menu_item(id,name,title,page,type,target) {
	if (hasMenuCookie())
		type = (getToggle(id)?'collapse':'expand');
	else if (type=='collapse') defaultMenuItems += '<'+id+'>';
	document.write('<div style="margin:5px;margin-right:0;">');
	document.write('<img border="0" src="images/'+type+'_node_btn.gif" align="absmiddle" '+
		'id="__idMenuImg'+id+'" title="Expand '+name+'" name="'+name+'" '+
		'onMouseOver="javascript:this.style.cursor=\'hand\';this.style.cursor=\'pointer\';" '+
		'onclick="javascript:toggle(\''+id+'\')">');
	makelink(page,name,title,target);
	document.write('</div>');
	document.write('<div ID="__idMenuDiv'+id+'" style="display:'+(type=='collapse'?'block':'none')+';position:relative;margin-left:18px;">');
}

function leaf_item(name,title,page) {
	leaf_item(name,title,page, '');
}

function leaf_item(name,title,page,target) {
	document.write('<div style="margin:5px;margin-right:0;">');
	document.write('<img border="0" src="images/end_node_btn.gif" align="absmiddle">');
	makelink(page,name,title,target);
	document.write('</div>');
}

function enditem() {
	document.write('</div>');
}

function makelink(page, name, title, target) {
	if (page=='') {
		document.write('&nbsp;<span title="'+title+'">'+name+'</span><br>');
	} else { 
		var onClick = '';
		if (target==null || target=='') target='__idContentFrame';
		if (target=='__idContentFrame') onClick='onClick=" if (!event.ctrlKey) displayLoading();"';
		/*
		if (page.indexOf('?') >= 0)
			page += '&gwt.codesvr=127.0.0.1:9997';
		else
			page += '?gwt.codesvr=127.0.0.1:9997';
			*/
		document.write('&nbsp;<A ' + onClick + ' target="' + target + '" href="'+page+'" title="'+title+'" >'+name+'</A>');
	}
}

function setDefaultCookie(items) {
	
}

function setMenuCookie(value) {
	var expires = new Date();
  	expires.setDate(expires.getDate()+30);
	document.cookie = "menu="+value+"; expires="+expires.toGMTString();
}

function hasMenuCookie() {
	return (document.cookie.length>0?document.cookie.indexOf("menu=")>=0:false);
}

function getMenuCookie() {
	if (document.cookie.length>0) {
		var c_start=document.cookie.indexOf("menu=");
		if (c_start!=-1) {
			c_start=c_start + 5;
			var c_end=document.cookie.indexOf(";",c_start);
    		if (c_end==-1) c_end=document.cookie.length;
    		return unescape(document.cookie.substring(c_start,c_end));
    	}
    }
	return "";
}

function setToggle(item, visible) {
	var mc = getMenuCookie();
	if (!hasMenuCookie()) mc = defaultMenuItems;
	var ix = '<'+item+'>';
	if (visible) {
		mc += ix;
	} else {
		var idx = mc.indexOf(ix);
		if (idx>=0) mc = mc.substring(0,idx) + mc.substring(idx+ix.length);
	}
	setMenuCookie(mc);
}

function getToggle(item) {
	return (getMenuCookie().indexOf('<'+item+'>')>=0);
}
