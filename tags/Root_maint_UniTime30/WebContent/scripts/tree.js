/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

function displayLoading() {
	if (parent.__idContentFrame && parent.__idContentFrame.displayElement) {
		parent.__idContentFrame.displayElement('contentMain', false);
		parent.__idContentFrame.displayElement('loadingMain', true);
	}
}

function toggle(item) {
	obj=document.getElementById("__idMenuDiv"+item);
	visible=(obj.style.display!="none")
	img=document.getElementById("__idMenuImg" + item);
	menu=document.getElementById("__idMenu" + item);
	if (visible) {obj.style.display="none";img.src="images/expand_node_btn.gif";menu.title='Expand '+menu.name;}
	else {obj.style.display="block";img.src="images/collapse_node_btn.gif";menu.title='Collapse '+menu.name;}
}
function menu_item(id,name,title,page,type) {
	document.write('<div style="margin:5px;margin-right:0;">');
	document.write('<A style="border:0;background:0" id="__idMenu'+id+'" href="javascript:toggle(\''+id+'\')" name="'+name+'" title="Expand '+name+'">');
	document.write('<img id="__idMenuImg'+id+'" border="0" src="images/'+type+'_node_btn.gif" align="absmiddle"></A>');
	if (page=='')
		document.write('&nbsp;<span title="'+title+'">'+name+'</span><br>');
	else
		document.write('&nbsp;<A onClick="displayLoading();" target="__idContentFrame" '+(page==''?'':'href="'+page+'" ')+'title="'+title+'" >'+name+'</A><br>');
	document.write('</div>');
	document.write('<div ID="__idMenuDiv'+id+'" style="display:'+(type=='collapse'?'block':'none')+';position:relative;margin-left:18px;">');
}
function leaf_item(name,title,page) {
	leaf_item(name,title,page, '');
}
function leaf_item(name,title,page,target) {
	tgt = '__idContentFrame';
	onClick = 'onClick="displayLoading();"';
	if (target!='' && target!=null) {
		tgt = target;
		onClick = '';
	}
	document.write('<div style="margin:5px;margin-right:0;">');
	document.write('<img border="0" src="images/end_node_btn.gif" align="absmiddle">');
	if (page=='')
		document.write('&nbsp;<span title="'+title+'">'+name+'</span><br>');
	else
		document.write('&nbsp;<A ' + onClick + ' target="' + tgt + '" '+(page==''?'':'href="'+page+'" ')+'title="'+title+'" >'+name+'</A><br>');
	document.write('</div>');
}
function enditem() {
	document.write('</div>');
}
