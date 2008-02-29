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
<%@ page language="java" autoFlush="true"%>
<%@ page import="org.unitime.timetable.solver.SolverProxy" %>
<%@ page import="org.unitime.timetable.solver.WebSolver" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<tiles:importAttribute />

<html:form action="/unassigned">
	<% 	SolverProxy solver = WebSolver.getSolver(session); %>
	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<td colspan='4'>
				<i>
					NOTE: Only classes that are <%=solver==null?"included in the selected solution":"loaded into the solver"%> 
					are displayed in the below list. This means that classes that are assigned to other timetabling managers 
					(e.g., LLR or LAB) as well as classes that <%=solver==null?"were":"are"%> not loaded into the solver 
					(e.g., Arrange Hours classes) are excluded. 
					For the full list of classes see <a href='classShowSearch.do'>Classes</a> or 
					<a href='classAssignmentsReportShowSearch.do'>Class Assignments</a> page.<br>
				</i>
			</TD>
		</TR>
		<TR>
			<td colspan='4'>
				&nbsp;
			</TD>
		</TR>
		<TR>
			<td colspan='4'>
				<tt:section-header>
					<tt:section-title>
						Not-assigned Classes
					</tt:section-title>
					<html:submit onclick="displayLoading();" property="op" value="Export PDF" /> 
					<html:submit onclick="displayLoading();" property="op" accesskey="R" value="Refresh" /> 
				</tt:section-header>
			</td>
		</TR>
		<%= request.getAttribute("Unassigned.table") %> 
		<TR>
			<td colspan='4'>
				<tt:displayPrefLevelLegend/>
				<br>
				<i>
					NOTE: Only classes that are <%=solver==null?"included in the selected solution":"loaded into the solver"%> 
					are displayed in the above list. This means that classes that are assigned to other timetabling managers 
					(e.g., LLR or LAB) as well as classes that <%=solver==null?"were":"are"%> not loaded into the solver 
					(e.g., Arrange Hours classes) are excluded. 
					For the full list of classes see <a href='classShowSearch.do'>Classes</a> or 
					<a href='classAssignmentsReportShowSearch.do'>Class Assignments</a> page.
				</i>
			</TD>
		</TR>
	</TABLE>
</html:form>