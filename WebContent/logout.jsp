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
<%@ page language="java" %>
<%@ page errorPage="error.jsp" %>
<%@ page import="java.util.Enumeration" %>
<%
	Cookie cookie = new Cookie("loggedOn", "false" );    	
	response.addCookie( cookie );

	Enumeration e = session.getAttributeNames();
	while (e.hasMoreElements()) {
		String key = e.nextElement().toString();
		session.setAttribute(key, null);
	}	
	session.invalidate(); 	
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<HTML>
	<HEAD>
		<META http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
		<TITLE>Timetabling - Logout</TITLE>
		<SCRIPT language="javascript">
			function closeWin() {
				top.window.close();				
				top.location.href='<%=request.getContextPath()%>/login.do';
			}
		</SCRIPT>
	</HEAD>
	<BODY onload="closeWin();">
		<BLOCKQUOTE>
			<DIV class="normal">Logging off ...</DIV>
		</BLOCKQUOTE>
	</BODY>
</HTML>
	