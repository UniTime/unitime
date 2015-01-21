<!DOCTYPE html>
<%--
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
--%>
<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8" errorPage="/error.jsp"%>
<%@ page import="org.unitime.timetable.ApplicationProperties" %>
<%@ page import="org.unitime.timetable.util.Constants"%>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<tt:session-context/>
<HTML>
	<HEAD>
	    <meta charset="UTF-8"/>
	    <meta http-equiv="X-UA-Compatible" content="IE=Edge">
		<% if (!sessionContext.isAuthenticated()) {%>
		<META http-equiv="Refresh" content="1; url=login.do">	
		<% } else { %>	
		<META http-equiv="Refresh" content="1; url=selectPrimaryRole.do">	
		<% } %>
		<LINK rel="stylesheet" type="text/css" href="styles/timetabling.css">
		<link rel="shortcut icon" href="images/timetabling.ico" />
		<TITLE>UniTime <%=Constants.VERSION%>| University Timetabling Application</TITLE>
	</HEAD>
	<BODY>
		<% if (ApplicationProperties.getProperty("tmtbl.header.external", "").trim().length()>0) { %>
			<jsp:include flush="true" page='<%=ApplicationProperties.getProperty("tmtbl.header.external")%>' />
		<% } %>
		<BR>
	</BODY>
</HTML>
