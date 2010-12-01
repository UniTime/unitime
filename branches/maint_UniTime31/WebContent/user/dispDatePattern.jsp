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
<%@ page language="java" pageEncoding="UTF-8"%>
<%@ page import="org.unitime.timetable.model.dao.Class_DAO" %>
<%@ page import="org.unitime.timetable.model.Class_" %>
<%@ page import="org.unitime.timetable.model.DatePattern" %>
<%@ page import="org.unitime.timetable.model.dao.DatePatternDAO" %>
<%@ page import="org.unitime.commons.web.Web" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%
	Long datePatternId = Long.valueOf(request.getParameter("id"));
	DatePattern datePattern = null;
	if (datePatternId.intValue()<0 && request.getParameter("class")!=null) {
		Class_ clazz = (new Class_DAO()).get(Long.valueOf(request.getParameter("class")));
		if (clazz!=null) 
			datePattern = clazz.getSchedulingSubpart().effectiveDatePattern();
	} else if (datePatternId.intValue()<0) {
    	datePattern = org.unitime.timetable.model.Session.getCurrentAcadSession(Web.getUser(session)).getDefaultDatePatternNotNull();
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
		<td valign="middle" align='left'>
			<table width='100%' cellspacing='1' cellpadding='1' border='0' style='border-bottom:black 1px solid'>
				<tr><td align='center'>
					<tt:displayPrefLevelLegend prefs="false" dpOffered="true" dpBackgrounds="true" separator="none"/>
				</td></tr>
			</table>
		</td>
	</tr><tr valign='bottom'>
		<td>
			<table width='100%'><tr><td align='right'>
				<input type='button' value='Close' onclick='window.close();'>
			</td></tr></table>
		</td>
	</tr></table>
  </body>
</html>
<% if (datePattern==null) { %>
<script language="JavaScript">
	alert('No default date pattern selected.'); window.close();
</script>
<% } %>
