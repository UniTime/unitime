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
