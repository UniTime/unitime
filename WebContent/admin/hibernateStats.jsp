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
<%@ page errorPage="../error.jsp" %>
<%@ page import="org.hibernate.SessionFactory" %>
<%@ page import="org.unitime.timetable.model.dao._RootDAO" %>
<%@ page import="org.unitime.commons.hibernate.stats.StatsProvider" %>

<%
	boolean summaryOnly = false;
	String so = request.getParameter("so");
	String linkStr = "View Summary Statistics Only";
	String title = "Detailed Statistics";
	
	if(so!=null && so.equals("true")) {
		summaryOnly = true;
		linkStr = "View Detailed Statistics";
		title = "Summary Statistics";
	}
%>
	<TABLE align="left" width="95%">
		<TR>
			<TD align="center" class="WelcomeRowHead">
				<%=title%>
			</TD>
			<TD align="right">
				 <A class="l7" href="hibernateStats.do?so=<%=!summaryOnly%>"><%=linkStr%></A>
				 &nbsp; &nbsp; &nbsp; &nbsp; 
			</TD>
		</TR>
		
		<TR>
			<TD align="center" colspan="2">
<%		
	// Get Session Factory
	_RootDAO brd = new _RootDAO();
	SessionFactory sessionFactory = brd.getSession().getSessionFactory();
		
	// Instantiate Stats Provider	
	StatsProvider stats = new StatsProvider();
	String statsStr = stats.getStatsHtml(sessionFactory, summaryOnly);
	
	// Display stats
	out.println(statsStr);		
%>
			</TD>
		</TR>
	</TABLE>

