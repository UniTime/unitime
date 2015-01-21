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


<html:form action="/solverParamDef">
<tt:confirm name="confirmDelete">The solver parameter will be deleted. Continue?</tt:confirm>
<html:hidden property="uniqueId"/><html:errors property="uniqueId"/>
<input type="hidden" name="op2" value="">
<logic:notEqual name="solverParamDefForm" property="op" value="List">
	<html:hidden property="order"/><html:errors property="order"/>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<logic:equal name="solverParamDefForm" property="op" value="Save">
							Add
						</logic:equal>
						<logic:notEqual name="solverParamDefForm" property="op" value="Save">
							Edit
						</logic:notEqual>
						Solver Parameter
					</tt:section-title>
					<logic:equal name="solverParamDefForm" property="op" value="Save">
						<html:submit property="op" value="Save" accesskey="S" title="Save Solver Parameter (Alt+S)"/>
					</logic:equal>
					<logic:notEqual name="solverParamDefForm" property="op" value="Save">
						<html:submit property="op" value="Update" accesskey="U" title="Update Solver Parameter (Alt+U)"/>
						<html:submit property="op" value="Delete" onclick="return confirmDelete();" accesskey="D" title="Delete Solver Parameter (Alt+D)"/> 
					</logic:notEqual>
					<html:submit property="op" value="Back" title="Return to Solver Parameters (Alt+B)" accesskey="B"/>
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
			<TD>Group:</TD>
			<TD>
				<html:select property="group">
					<html:options name="solverParamDefForm" property="groups"/>
				</html:select>
				&nbsp;<html:errors property="group"/>
			</TD>
		</TR>

		<TR>
			<TD>Description:</TD>
			<TD>
				<html:text property="description" size="100" maxlength="1000"/>
				&nbsp;<html:errors property="description"/>
			</TD>
		</TR>

		<TR>
			<TD>Type:</TD>
			<TD>
				<html:text property="type" size="50" maxlength="250"/>
				&nbsp;<html:errors property="type"/>
			</TD>
		</TR>

		<TR>
			<TD>Default:</TD>
			<TD>
				<html:text property="default" size="100" maxlength="2048"/>
				&nbsp;<html:errors property="default"/>
			</TD>
		</TR>

		<TR>
			<TD>Visible:</TD>
			<TD>
				<html:checkbox property="visible"/>
				&nbsp;<html:errors property="visible"/>
			</TD>
		</TR>

		<TR>
			<TD colspan="2">
				<tt:section-title/>
			</TD>
		</TR>

		<TR>
			<TD align="right" colspan="2">
				<logic:equal name="solverParamDefForm" property="op" value="Save">
					<html:submit property="op" value="Save" accesskey="S" title="Save Solver Parameter (Alt+S)"/>
				</logic:equal>
				<logic:notEqual name="solverParamDefForm" property="op" value="Save">
					<html:submit property="op" value="Update" accesskey="U" title="Update Solver Parameter (Alt+U)"/>
					<html:submit property="op" value="Delete" onclick="return confirmDelete();" accesskey="D" title="Delete Solver Parameter (Alt+D)"/> 
				</logic:notEqual>
				<html:submit property="op" value="Back" title="Return to Solver Parameters (Alt+B)" accesskey="B"/>
			</TD>
		</TR>
	</TABLE>
</logic:notEqual>
<logic:equal name="solverParamDefForm" property="op" value="List">
	<input type="hidden" name="group" value="">
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<bean:write scope="request" name="SolverParameterDef.table" filter="false"/>
	</TABLE>
	<% if (request.getAttribute("hash") != null) { %>
		<SCRIPT type="text/javascript" language="javascript">
			location.hash = '<%=request.getAttribute("hash")%>';
		</SCRIPT>
	<% } %>
</logic:equal>
</html:form>
