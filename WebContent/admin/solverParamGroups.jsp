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
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>

<tiles:importAttribute />

<html:form action="/solverParamGroups" focus="name">
<html:hidden property="uniqueId"/><html:errors property="uniqueId"/>
<html:hidden property="order"/><html:errors property="order"/>

	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<DIV class="WelcomeRowHead">
				<logic:equal name="solverParamGroupsForm" property="op" value="Add New">
				Add
				</logic:equal>
				<logic:notEqual name="solverParamGroupsForm" property="op" value="Add New">
				Edit
				</logic:notEqual>
				Sovler Parameter Group
				</DIV>
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
			<TD>Description:</TD>
			<TD>
				<html:text property="description" size="50" maxlength="1000"/>
				&nbsp;<html:errors property="description"/>
			</TD>
		</TR>
<!-- 
		<TR>
			<TD>Condition:</TD>
			<TD>
				<html:text property="condition" size="50" maxlength="250"/>
				&nbsp;<html:errors property="condition"/>
			</TD>
		</TR>
-->
		<TR>
			<TD align="right" colspan="2">
				<html:submit property="op">
					<bean:write name="solverParamGroupsForm" property="op" />
				</html:submit> 
				<logic:notEqual name="solverParamGroupsForm" property="op" value="Add New">
					<logic:greaterThan name="solverParamGroupsForm" property="order" value="0">
						<html:submit property="op" value="Move Up"/> 
					</logic:greaterThan>
					<logic:lessThan name="solverParamGroupsForm" property="order" value="<%=request.getAttribute("SolverParameterGroup.last").toString()%>">
						<html:submit property="op" value="Move Down"/> 
					</logic:lessThan>
					<html:submit property="op" value="Delete"/> 
				</logic:notEqual>
				<html:submit property="op" value="Clear" /> 
<!-- 				<html:reset /> -->
			</TD>
		</TR>
	</TABLE>

<BR>&nbsp;<BR>

<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
	<%= request.getAttribute("SolverParameterGroup.table") %> 
</TABLE>


</html:form>