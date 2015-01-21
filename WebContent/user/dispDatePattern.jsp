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
<%@page import="org.unitime.timetable.model.dao.SessionDAO"%>
<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="org.unitime.timetable.model.dao.Class_DAO" %>
<%@ page import="org.unitime.timetable.model.Class_" %>
<%@ page import="org.unitime.timetable.model.DatePattern" %>
<%@ page import="org.unitime.timetable.model.dao.DatePatternDAO" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<tt:session-context/>
<%
	Long datePatternId = Long.valueOf(request.getParameter("id"));
	DatePattern datePattern = null;
	if (datePatternId < 0 && request.getParameter("class")!=null) {
		Class_ clazz = (new Class_DAO()).get(Long.valueOf(request.getParameter("class")));
		if (clazz!=null) 
			datePattern = clazz.getSchedulingSubpart().effectiveDatePattern();
	} else if (datePatternId < 0) {
    	datePattern = SessionDAO.getInstance().get(sessionContext.getUser().getCurrentAcademicSessionId()).getDefaultDatePatternNotNull();
	} else {
		datePattern = (new DatePatternDAO()).get(datePatternId);
	}
%>
<html>
  <head>
    <title>Preview of <%=datePattern==null?"":datePattern.getName()%></title>
    <link rel="stylesheet" type="text/css" href="<%=request.getContextPath()%>/styles/timetabling.css">
    <script language='JavaScript' type='text/javascript' src='<%=request.getContextPath()%>/scripts/datepatt.js'></script>
  </head>
  <body class="bodyStyle">
  	<table border='0' width='100%' height='100%'><tr>
  		<% if (datePattern!=null) { %>
  			<td align='center'>
			    <%=datePattern.getPatternHtml(false,null,false)%>
			</td>
		<% } else { %>
  			<td align='left'>
				<br><i>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;No default date pattern selected.</i>
			</td>
		<% } %>
	</tr><tr>
		<td align='center'>
			<tt:displayPrefLevelLegend prefs="false" dpOffered="true" dpBackgrounds="true" separator="top"/>
		</td>
	</tr></table>
  </body>
</html>