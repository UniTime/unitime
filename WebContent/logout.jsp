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
				location.href='<%=request.getContextPath()%>/login.do';
			}
		</SCRIPT>
	</HEAD>
	<BODY onload="closeWin();">
		<BLOCKQUOTE>
			<DIV class="normal">Logging out ...</DIV>
		</BLOCKQUOTE>
	</BODY>
</HTML>
	
