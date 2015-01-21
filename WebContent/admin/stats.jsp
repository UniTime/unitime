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
<%@ page language="java" autoFlush="true"%>
<%@ page import="org.unitime.timetable.model.QueryLog"%>
<%@ page import="org.unitime.commons.web.WebTable"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<tiles:importAttribute />

<sec:authorize access="hasPermission(null, null, 'PageStatistics')">
<tt:session-context/>
<table width="100%" cellpadding="10" cellspacing="0">
	<% for (QueryLog.ChartWindow ch: QueryLog.ChartWindow.values()) { %>
		<tr><td colspan="<%=QueryLog.ChartType.values().length%>">
			<tt:section-title><%=ch.getName()%></tt:section-title>
		</td></tr>
		<tr>
		<% for (QueryLog.ChartType t: QueryLog.ChartType.values()) { %>
			<td><img src="<%=QueryLog.getChart(ch, t)%>" border="0"/></td>
		<% } %>
		</tr>
	<% } %>
</table>
<table width="100%" cellpadding="2" cellspacing="0">
	<% WebTable.setOrder(sessionContext,"pageStats.ord",request.getParameter("ord"), 1); %>
	<%=QueryLog.getTopQueries(7).printTable(WebTable.getOrder(sessionContext, "pageStats.ord"))%>
</table>
</sec:authorize>
<sec:authorize access="!hasPermission(null, null, 'PageStatistics')">
Access denied.
</sec:authorize>