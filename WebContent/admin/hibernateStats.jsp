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
<%@ page language="java" %>
<%@ page errorPage="../error.jsp" %>
<%@ page import="org.hibernate.SessionFactory" %>
<%@ page import="org.unitime.timetable.model.dao._RootDAO" %>
<%@ page import="org.unitime.commons.hibernate.stats.StatsProvider" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<sec:authorize access="hasPermission(null, null, 'HibernateStatistics')">
<%
    boolean details = "true".equals(request.getParameter("details"));
	if (request.getParameter("enable")!=null) {
		new _RootDAO().getSession().getSessionFactory().getStatistics().setStatisticsEnabled("true".equals(request.getParameter("enable")));
	}
	boolean enabled = new _RootDAO().getSession().getSessionFactory().getStatistics().isStatisticsEnabled();
%>
<TABLE width="100%">
		<TR>
			<TD>
				<tt:section-header>
					<tt:section-title>
						<%=details?"Detailed Statistics":"Summary Statistics"%>
					</tt:section-title>
					<input type='button' onclick="document.location='hibernateStats.do?details=<%=details%>&enable=<%=!enabled%>';" value='<%=enabled?"Disable":"Enable"%> Statistics'>
					<input type='button' onclick="document.location='hibernateStats.do?details=<%=!details%>';" value='<%=details?"Hide Details":"Show Details"%>'>
				</tt:section-header>
			</TD>
		</TR>
		
		<TR>
			<TD>
				<%=StatsProvider.getStatsHtml(!details)%>
			</TD>
		</TR>
		<TR>
			<TD>
				<tt:section-title/>
			</TD>
		</TR>
		<TR>
			<TD align='right'>
				<input type='button' onclick="document.location='hibernateStats.do?details=<%=details%>&enable=<%=!enabled%>';" value='<%=enabled?"Disable":"Enable"%> Statistics'>
				<input type='button' onclick="document.location='hibernateStats.do?details=<%=!details%>';" value='<%=details?"Hide Details":"Show Details"%>'>
			</TD>
		</TR>
</TABLE>
</sec:authorize>
<sec:authorize access="!hasPermission(null, null, 'HibernateStatistics')">
Access denied.
</sec:authorize>