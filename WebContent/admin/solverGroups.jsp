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
<%@ page import="org.unitime.timetable.util.Constants"%>
<%@ page import="org.unitime.commons.web.Web"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<tiles:importAttribute />

<tt:confirm name="confirmDelete">The solver group will be deleted. Continue?</tt:confirm>
<tt:confirm name="confirmDeleteAll">All solver groups will be deleted. Continue?</tt:confirm>
<tt:confirm name="confirmAutoSetup">New solver groups may be created. Continue?</tt:confirm>

<html:form action="/solverGroupEdit">
<html:hidden property="uniqueId"/><html:errors property="uniqueId"/>
<html:hidden property="departmentsEditable"/><html:errors property="departmentsEditable"/>

<logic:notEqual name="solverGroupEditForm" property="op" value="List">
	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<logic:equal name="solverGroupEditForm" property="op" value="Save">
						Add
						</logic:equal>
						<logic:notEqual name="solverGroupEditForm" property="op" value="Save">
						Edit
						</logic:notEqual>
						Solver Group
					</tt:section-title>
					<logic:equal name="solverGroupEditForm" property="op" value="Save">
						<html:submit property="op" value="Save" accesskey="S" title="Save Solver Group (Alt+S)"/>
					</logic:equal>
					<logic:notEqual name="solverGroupEditForm" property="op" value="Save">
						<html:submit property="op" value="Update" accesskey="U" title="Update Solver Group (Alt+U)"/>
						<logic:equal name="solverGroupEditForm" property="departmentsEditable" value="true">
							<html:submit property="op" value="Delete" onclick="return confirmDelete();" accesskey="D" title="Delete Solver Group (Alt+D)"/>
						</logic:equal> 
					</logic:notEqual>
					<html:submit property="op" value="Back" title="Return to Solver Groups (Alt+B)" accesskey="B"/>
				</tt:section-header>
			</TD>
		</TR>

		<TR>
			<TD>Abbreviation:</TD>
			<TD>
				<html:text property="abbv" size="10" maxlength="100"/>
				&nbsp;<html:errors property="abbv"/>
			</TD>
		</TR>

		<TR>
			<TD>Name:</TD>
			<TD>
				<html:text property="name" size="50" maxlength="100"/>
				&nbsp;<html:errors property="name"/>
			</TD>
		</TR>
		
		<logic:equal name="solverGroupEditForm" property="departmentsEditable" value="false">
			<TR><TD colspan='2'>&nbsp;</TD></TR>
			<TR><TD colspan='2'><tt:section-header title="Departments"/></TD></TR>
			<logic:iterate name="solverGroupEditForm" property="departmentIds" id="departmentId" indexId="ctr">
				<logic:equal name="solverGroupEditForm" property="<%="assignedDepartments["+ctr+"]"%>" value="true">
					<TR><TD colspan='2'>&nbsp;&nbsp;&nbsp;&nbsp;
							<html:hidden property="<%="departmentIds["+ctr+"]"%>"/>
							<html:hidden property="<%="departmentNames["+ctr+"]"%>"/>
							<html:hidden property="<%="assignedDepartments["+ctr+"]"%>"/>
							<bean:write name="solverGroupEditForm" property="<%="departmentNames["+ctr+"]"%>"/>
					</TD></TR>
				</logic:equal>
			</logic:iterate>
		</logic:equal>
		
		<logic:equal name="solverGroupEditForm" property="departmentsEditable" value="true">
			<logic:notEqual name="solverGroupEditForm" property="op" value="Save">
				<TR><TD colspan='2'>&nbsp;</TD></TR>
				<TR><TD colspan='2'><tt:section-header title="Assigned Departments"/></TD></TR>
				<logic:iterate name="solverGroupEditForm" property="departmentIds" id="departmentId" indexId="ctr">
					<logic:equal name="solverGroupEditForm" property="<%="assignedDepartments["+ctr+"]"%>" value="true">
						<TR><TD colspan='2'>&nbsp;&nbsp;&nbsp;&nbsp;
							<html:hidden property="<%="departmentIds["+ctr+"]"%>"/>
							<html:hidden property="<%="departmentNames["+ctr+"]"%>"/>
							<html:checkbox property="<%="assignedDepartments["+ctr+"]"%>"/>
							<bean:write name="solverGroupEditForm" property="<%="departmentNames["+ctr+"]"%>"/>
						</TD></TR>
					</logic:equal>
				</logic:iterate>
			</logic:notEqual>
		</logic:equal>

		<logic:notEqual name="solverGroupEditForm" property="op" value="Save">
			<TR><TD colspan='2'>&nbsp;</TD></TR>
			<TR><TD colspan='2'><tt:section-header title="Assigned Managers"/></TD></TR>
			<logic:iterate name="solverGroupEditForm" property="managerIds" id="managerId" indexId="ctr">
				<logic:equal name="solverGroupEditForm" property="<%="assignedManagers["+ctr+"]"%>" value="true">
					<TR><TD colspan='2'>&nbsp;&nbsp;&nbsp;&nbsp;
						<html:hidden property="<%="managerIds["+ctr+"]"%>"/>
						<html:hidden property="<%="managerNames["+ctr+"]"%>"/>
						<html:checkbox property="<%="assignedManagers["+ctr+"]"%>"/>
						<bean:write name="solverGroupEditForm" property="<%="managerNames["+ctr+"]"%>" filter="false"/>
					</TD></TR>
				</logic:equal>
			</logic:iterate>
		</logic:notEqual>

		<logic:equal name="solverGroupEditForm" property="departmentsEditable" value="true">
			<TR><TD colspan='2'>&nbsp;</TD></TR>
			<TR><TD colspan='2'><tt:section-header title="Not Assigned Departments"/></TD></TR>
			<logic:iterate name="solverGroupEditForm" property="departmentIds" id="departmentId" indexId="ctr">
				<logic:equal name="solverGroupEditForm" property="<%="assignedDepartments["+ctr+"]"%>" value="false">
					<TR><TD colspan='2'>&nbsp;&nbsp;&nbsp;&nbsp;
						<html:hidden property="<%="departmentIds["+ctr+"]"%>"/>
						<html:hidden property="<%="departmentNames["+ctr+"]"%>"/>
						<html:checkbox property="<%="assignedDepartments["+ctr+"]"%>"/>
						<bean:write name="solverGroupEditForm" property="<%="departmentNames["+ctr+"]"%>"/>
					</TD></TR>
				</logic:equal>
			</logic:iterate>
		</logic:equal>
		
		<TR><TD colspan='2'>&nbsp;</TD></TR>
		<TR><TD colspan='2'><tt:section-header title="Not Assigned Managers"/></TD></TR>
		<logic:iterate name="solverGroupEditForm" property="managerIds" id="managerId" indexId="ctr">
			<logic:equal name="solverGroupEditForm" property="<%="assignedManagers["+ctr+"]"%>" value="false">
				<TR><TD colspan='2'>&nbsp;&nbsp;&nbsp;&nbsp;
					<html:hidden property="<%="managerIds["+ctr+"]"%>"/>
					<html:hidden property="<%="managerNames["+ctr+"]"%>"/>
					<html:checkbox property="<%="assignedManagers["+ctr+"]"%>"/>
					<bean:write name="solverGroupEditForm" property="<%="managerNames["+ctr+"]"%>" filter="false"/>
				</TD></TR>
			</logic:equal>
		</logic:iterate>

		<TR><TD colspan='2'><tt:section-header/></TD></TR>
		<TR>
			<TD align="right" colspan="2">
				<logic:equal name="solverGroupEditForm" property="op" value="Save">
					<html:submit property="op" value="Save" accesskey="S" title="Save Solver Group (Alt+S)"/>
				</logic:equal>
				<logic:notEqual name="solverGroupEditForm" property="op" value="Save">
					<html:submit property="op" value="Update" accesskey="U" title="Update Solver Group (Alt+U)"/>
					<logic:equal name="solverGroupEditForm" property="departmentsEditable" value="true">
						<html:submit property="op" value="Delete" onclick="return confirmDelete();" accesskey="D" title="Delete Solver Group (Alt+D)"/>
					</logic:equal> 
				</logic:notEqual>
				<html:submit property="op" value="Back" title="Return to Solver Groups (Alt+B)" accesskey="B"/>
			</TD>
		</TR>
	</TABLE>
</logic:notEqual>
<logic:equal name="solverGroupEditForm" property="op" value="List">
	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD align="right" colspan="5">
				<tt:section-header>
					<tt:section-title>
						Solver Groups - <%= Web.getUser(session).getAttribute(Constants.ACAD_YRTERM_LABEL_ATTR_NAME) %>
					</tt:section-title>
				<html:submit property="op" value="Add Solver Group" title="Create New Solver Group (Alt+A)" accesskey="A"/> 
				<html:submit property="op" onclick="return confirmDeleteAll();" value="Delete All" title="Delete All Solver Groups"/> 
				<html:submit property="op" onclick="return confirmAutoSetup();" value="Auto Setup" title="Automatically Setup Solver Groups"/> 
				<html:submit property="op" value="Export PDF" title="Export PDF (Alt+P)" accesskey="P"/> 
				</tt:section-header>
			</TD>
		</TR>
		<%= request.getAttribute("SolverGroups.table") %> 
		<TR>
			<TD align="right" class="WelcomeRowHead" colspan="5">&nbsp;</TD>
		</TR>
		<TR>
			<TD align="right" colspan="5">
				<html:submit property="op" value="Add Solver Group" title="Create New Solver Group (Alt+A)" accesskey="A"/> 
				<html:submit property="op" onclick="return confirmDeleteAll();" value="Delete All" title="Delete All Solver Groups"/> 
				<html:submit property="op" onclick="return confirmAutoSetup();" value="Auto Setup" title="Automatically Setup Solver Groups"/> 
				<html:submit property="op" value="Export PDF" title="Export PDF (Alt+P)" accesskey="P"/> 
			</TD>
		</TR>
	</TABLE>
	<% if (request.getAttribute("hash") != null) { %>
		<SCRIPT type="text/javascript" language="javascript">
			location.hash = '<%=request.getAttribute("hash")%>';
		</SCRIPT>
	<% } %>
</logic:equal>

</html:form>