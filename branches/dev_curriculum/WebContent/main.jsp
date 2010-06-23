<%--
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC
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
--%>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ page errorPage="error.jsp" %>
<%@ include file="/checkLogin.jspf" %>
<%@ include file="/checkAccessLevel.jspf" %>
<html>
<head>
	<tt:hasProperty name="tmtbl.title">
		<title>
			<tt:property name="tmtbl.title"/>
		</title>
	</tt:hasProperty>
	<tt:notHasProperty name="tmtbl.title">
		<title>UniTime 3.2| University Timetabling Application</title>
	</tt:notHasProperty>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<link rel="stylesheet" type="text/css" href="styles/timetabling.css" />
	<link rel="shortcut icon" href="images/timetabling.ico" />
	<script language="javascript" type="text/javascript">
		var strFrameCols = "";
		var bMenuFrameCollapsed = false;

    	function onreadystatechange() {
    		if (document.readyState != "complete") return;
		}

		function onbeforeunload() {
			// set original frame width in case frame has been collapsed
			if (strFrameCols != "0px,200px,*") strOrigFrameCols = strFrameCols;
			else strOrigFrameCols = "0px,200px,*";
			// get get the current frame width; this may be zero
			var x = document.getElementById('__idMenuFrameset');
			strFrameCols = x.cols;
    	}

		function onMenuFrameResize() {
			var x = document.getElementById('__idMenuFrameset');
			if (x.cols== "28px,0px,*") {
				if (!bMenuFrameCollapsed) {
					document.getElementById("__idMenuNavFrame").tabIndex = "-1";
					document.getElementById("__idMenuFrame").tabIndex = "-1";
					bMenuFrameCollapsed = true;
				}
			} else {
				if (bMenuFrameCollapsed) {
					document.getElementById("__idMenuNavFrame").tabIndex = "0";
					document.getElementById("__idMenuFrame").tabIndex = "0";
					bMenuFrameCollapsed = false;
				}
			}
		}

	    if(parent.frames.length!=0)
	       top.location.href = 'main.jsp';
	</script>
</head>

<frameset cols="0,200,*" id="__idMenuFrameset" name="__idMenuFrameset" border="0" onresize="onMenuFrameResize();">
    <frameset id="__idMenuFramesetMin" rows="22,*">
        <frame class="MenuFrameMin" id="__idMenuFrameMin" name="__idMenuFrameMin" src="menumin.jsp" marginwidth="0" marginheight="0" framespacing="0" frameborder="0" scrolling="no"/>
        <frame id="__blankFrame" name="__idBlankFrame" src="empty.html" marginwidth="0" marginheight="0" framespacing="0" frameborder="0" scrolling="no"/>
    </frameset>
    <frameset class="MenuFrameLeftBorder" id="__idMenuFramesetLeft" name="__idMenuFramesetLeft" rows="22,*,177">
         <frame class="MenuNavFrame" id="__idMenuNavFrame" name="__idMenuNavFrame" src="menunav.jsp" marginwidth="0" marginheight="0" framespacing="0" frameborder="0" scrolling="no"/>
         <frame id="__idMenuFrame" name="__idMenuFrame" src="menu.jsp" marginwidth="0" marginheight="0" framespacing="0" frameborder="0" scrolling="auto"/>
         <frame class="MenuUserFrame" id="__userFrame" name="__idUserFrame" src="admin/userinfo.jsp" marginwidth="0" marginheight="0" framespacing="0" frameborder="0" scrolling="no"/>
    </frameset>
    <frame name="__idContentFrame" id="__idContentFrame" src="blank.jsp" marginwidth="0" marginheight="0" framespacing="0"/>
</frameset>

</html>
