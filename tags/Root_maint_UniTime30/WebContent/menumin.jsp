<%--
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
--%>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<link rel="stylesheet" type="text/css" href="styles/timetabling.css" />
	<tt:hasProperty name="tmtbl.custom.css">
		<LINK rel="stylesheet" type="text/css" href="%tmtbl.custom.css%" />
	</tt:hasProperty>
	<script language="javascript" type="text/javascript">
		function menuopen() {
			var parentFrame = window.parent.document.getElementById('__idMenuFrameset');
		
			if (parent.strFrameCols=="28px,0px,*") 
				parentFrame.cols = "0px,190px,*";
			else 
				parentFrame.cols = parent.strFrameCols;
		}
		
		function mouseover() {
			document.getElementById('idOpenMenu').src = "images/openMenu_RO.gif";
		}
		
		function mouseout() {
			document.getElementById('idOpenMenu').src = "images/openMenu.gif";
		}
	</script>
</head>

<body class="HelpBody">
<table width="100%" cellpadding="0" cellspacing="0" border="0">
	<tr>
		<td align="center">
		<table cellpadding="0" cellspacing="0" border="0">
			<tr>
				<td height="20">
					<a onclick="menuopen();" onmouseover="mouseover();" onmouseout="mouseout();"><img id="idOpenMenu" src="images/openMenu.gif" title="Maximize Menu" border="0" width="14" height="14" tabindex="2" alt=""></a>
				</td>
			</tr>
		</table>
		</td>
	</tr>
</table>
</body>
</html>
