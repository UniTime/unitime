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
<%@ page language="java" autoFlush="true"%>
<%@ page import="org.unitime.commons.Debug" %>
<%@ page import="org.unitime.timetable.solver.SolverProxy" %>
<%@ page import="org.unitime.timetable.solver.WebSolver" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>
<tiles:importAttribute />
<html:form action="/assignedClasses">
<%
try {
%>
	<script language="JavaScript">blToggleHeader('Filter','dispFilter');blStart('dispFilter');</script>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD>Simplified mode:</TD>
		<TD>
			<html:checkbox property="simpleMode"/>
		</TD>
	</TR>
	</TABLE>
	<script language="JavaScript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
	<script language="JavaScript">blEndCollapsed('dispFilter');</script>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD width="10%" nowrap>Subject Area:</TD>
			<TD>
				<html:select name="assignedClassesForm" property="subjectArea">
					<html:option value="">Select...</html:option>
					<html:option value="-1">All</html:option>
					<html:optionsCollection property="subjectAreas"	label="subjectAreaAbbreviation" value="uniqueId" />
				</html:select>
			</TD>
		</TR>
		<TR>
			<TD colspan='2' align='right'>
				<html:submit onclick="displayLoading();" property="op" value="Apply"/>
				<html:submit property="op" value="Export PDF"/>
				<html:submit property="op" value="Export CSV"/>
				<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh"/>
			</TD>
		</TR>
	</TABLE>

	<BR><BR>
<%
	String assigned = (String)request.getAttribute("AssignedClasses.table");
	SolverProxy solver = WebSolver.getSolver(session);
	if (assigned!=null) {
%>
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
			<tr>
				<td colspan='<%=request.getAttribute("AssignedClasses.table.colspan")%>'>
					<i>
					NOTE: Only classes that are <%=solver==null?"included in the selected solution":"loaded into the solver"%> 
					are displayed in the below list. This means that classes that are assigned to other<br/>timetabling managers 
					(e.g., LLR or LAB) as well as classes that <%=solver==null?"were":"are"%> not loaded into the solver 
					(e.g., Arrange Hours classes) are excluded.<br/>
					For the full list of classes see <a href='classShowSearch.do'>Classes</a> or 
					<a href='classAssignmentsReportShowSearch.do'>Class Assignments</a> page.<br>
					</i>
				</td>
			</tr>
			<tr>
				<td colspan='<%=request.getAttribute("AssignedClasses.table.colspan")%>'>
					&nbsp;
				</td>
			</tr>
			<%=assigned%>
			<tr>
				<td colspan='<%=request.getAttribute("AssignedClasses.table.colspan")%>'>
					<tt:displayPrefLevelLegend/>
					<br>
					<i>
					NOTE: Only classes that are <%=solver==null?"included in the selected solution":"loaded into the solver"%> 
					are displayed in the below list. This means that classes that are assigned to other<br/>timetabling managers 
					(e.g., LLR or LAB) as well as classes that <%=solver==null?"were":"are"%> not loaded into the solver 
					(e.g., Arrange Hours classes) are excluded.<br/>
					For the full list of classes see <a href='classShowSearch.do'>Classes</a> or 
					<a href='classAssignmentsReportShowSearch.do'>Class Assignments</a> page.<br>
					</i>
				</td>
			</tr>
		</TABLE>
<%
	} else {
%>
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
			<TR>
				<TD colspan="2">
					<DIV class="WelcomeRowHead">
					Assigned Classes
					</DIV>
				</TD>
			</TR>
			<TR>
				<TD>
					<I><%=request.getAttribute("AssignedClasses.message")%></I>
				</TD>
			</TR>
		</TABLE>
<%
	}
%>
<%
} catch (Exception e) {
	Debug.error(e);
%>		
		<font color='red'><B>ERROR:<%=e.getMessage()%></B></font>
<%
}
%>
</html:form>
