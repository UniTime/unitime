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
<%@ page import="org.unitime.timetable.ApplicationProperties" %>
<%@ page import="org.unitime.timetable.util.Constants"%>
<%@ page import="org.unitime.commons.web.Web"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<HTML>
	<HEAD>
		<% if (!Web.isLoggedIn(session)) {%>
		<META http-equiv="Refresh" content="1; url=login.do">	
		<% } else { %>	
		<META http-equiv="Refresh" content="1; url=selectPrimaryRole.do">	
		<% } %>
		<LINK rel="stylesheet" type="text/css" href="styles/timetabling.css">
		<link rel="shortcut icon" href="images/timetabling.ico" />
		<TITLE>UniTime <%=Constants.VERSION%>| University Timetabling Application</TITLE>
    <meta http-equiv="X-UA-Compatible" content="IE=8,chrome=1">
	</HEAD>
	<BODY>
		<% if (ApplicationProperties.getProperty("tmtbl.header.external", "").trim().length()>0) { %>
			<jsp:include flush="true" page='<%=ApplicationProperties.getProperty("tmtbl.header.external")%>' />
		<% } %>
		<BR>
	</BODY>
</HTML>
