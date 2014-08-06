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
<%@ page language="java" %>
<%@ page errorPage="../error.jsp" %>
<%@ page import="org.hibernate.SessionFactory" %>
<%@ page import="org.unitime.timetable.model.dao._RootDAO" %>
<%@ page import="org.unitime.commons.hibernate.stats.StatsProvider" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
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