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
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>

<tiles:importAttribute />

<tt:confirm name="confirmDelete">The solver parameter group will be deleted. Continue?</tt:confirm>

<html:form action="/solverParamGroups">
<input type="hidden" name="op2" value="">
<html:hidden property="uniqueId"/><html:errors property="uniqueId"/>
<logic:notEqual name="solverParamGroupsForm" property="op" value="List">
	<html:hidden property="order"/><html:errors property="order"/>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<logic:equal name="solverParamGroupsForm" property="op" value="Save">
							Add
						</logic:equal>
						<logic:notEqual name="solverParamGroupsForm" property="op" value="Save">
							Edit
						</logic:notEqual>
						Sovler Parameter Group
					</tt:section-title>
					<logic:equal name="solverParamGroupsForm" property="op" value="Save">
						<html:submit property="op" value="Save" accesskey="S" title="Save Solver Parameter Group (Alt+S)"/>
					</logic:equal>
					<logic:notEqual name="solverParamGroupsForm" property="op" value="Save">
						<html:submit property="op" value="Update" accesskey="U" title="Update Solver Parameter Group (Alt+U)"/>
						<html:submit property="op" value="Delete" onclick="return confirmDelete();" accesskey="D" title="Delete Solver Parameter Group (Alt+D)"/> 
					</logic:notEqual>
					<html:submit property="op" value="Back" title="Return to Solver Parameter Groups (Alt+B)" accesskey="B"/>
				</tt:section-header>
			</TD>
		</TR>

		<TR>
			<TD>Name:</TD>
			<TD>
				<html:text property="name" size="50" maxlength="100"/>
				&nbsp;<html:errors property="name"/>
			</TD>
		</TR>

		<TR>
			<TD>Type:</TD>
			<TD>
				<html:select property="type">
					<html:optionsCollection name="solverParamGroupsForm" property="types" value="id" label="value"/>
				</html:select>
				&nbsp;<html:errors property="type"/>
			</TD>
		</TR>

		<TR>
			<TD>Description:</TD>
			<TD>
				<html:text property="description" size="50" maxlength="1000"/>
				&nbsp;<html:errors property="description"/>
			</TD>
		</TR>
		<TR>
			<TD colspan="2">
				<tt:section-title/>
			</TD>
		</TR>
		<TR>
			<TD align="right" colspan="2">
				<logic:equal name="solverParamGroupsForm" property="op" value="Save">
					<html:submit property="op" value="Save" accesskey="S" title="Save Solver Parameter Group (Alt+S)"/>
				</logic:equal>
				<logic:notEqual name="solverParamGroupsForm" property="op" value="Save">
					<html:submit property="op" value="Update" accesskey="U" title="Update Solver Parameter Group (Alt+U)"/>
					<html:submit property="op" value="Delete" onclick="return confirmDelete();" accesskey="D" title="Delete Solver Parameter Group (Alt+D)"/> 
				</logic:notEqual>
				<html:submit property="op" value="Back" title="Return to Solver Parameter Groups (Alt+B)" accesskey="B"/>
			</TD>
		</TR>
	</TABLE>

</logic:notEqual>
<logic:equal name="solverParamGroupsForm" property="op" value="List">
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<tr>
			<td colspan='4'>
				<tt:section-header>
					<tt:section-title>Solver Groups</tt:section-title>
					<html:submit property="op" value="Add Solver Parameter Group" accesskey="A" title="Create New Solver Group (Alt+A)"/>
				</tt:section-header>
			</td>
		</tr>
		<%= request.getAttribute("SolverParameterGroup.table") %> 
		<tr>
			<td colspan='4'>
				<tt:section-title/>
			</td>
		</tr>
		<tr>
			<td colspan='4' align="right">
				<html:submit property="op" value="Add Solver Parameter Group" accesskey="A" title="Create New Solver Group (Alt+A)"/>
			</td>
		</tr>
	</TABLE>
</logic:equal>
</html:form>
