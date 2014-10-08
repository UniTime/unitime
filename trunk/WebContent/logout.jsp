<!DOCTYPE html>
<%--
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
--%>
<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8" errorPage="/error.jsp"%>
<%@ page import="java.util.Enumeration" %>
<%@ page import="org.unitime.timetable.util.Constants"%>
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
<HTML>
	<HEAD>
	    <meta charset="UTF-8"/>
	    <meta http-equiv="X-UA-Compatible" content="IE=Edge">
		<TITLE>UniTime <%=Constants.VERSION%>| Logging out ...</TITLE>
		<SCRIPT language="javascript">
			function closeWin() {
				// window.close();				
				location.href='<%=request.getContextPath()%>/login.jsp';
			}
		</SCRIPT>
	</HEAD>
	<BODY onload="closeWin();">
		<BLOCKQUOTE>
			<DIV class="normal">Logging out ...</DIV>
		</BLOCKQUOTE>
	</BODY>
</HTML>
	
