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
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>

<tiles:importAttribute />

<html:form action="/solverInfoDef" focus="name">
<html:hidden property="uniqueId"/><html:errors property="uniqueId"/>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<DIV class="WelcomeRowHead">
				<logic:equal name="solverInfoDefForm" property="op" value="Add New">
				Add
				</logic:equal>
				<logic:notEqual name="solverInfoDefForm" property="op" value="Add New">
				Edit
				</logic:notEqual>
				Solution Info Defition
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

		<TR>
			<TD>Implementation:</TD>
			<TD>
				<html:text property="implementation" size="50" maxlength="250"/>
				&nbsp;<html:errors property="implementation"/>
			</TD>
		</TR>

		<TR>
			<TD align="right" colspan="2">
				<html:submit property="op">
					<bean:write name="solverInfoDefForm" property="op" />
				</html:submit> 
				<logic:notEqual name="solverInfoDefForm" property="op" value="Add New">
					<html:submit property="op" value="Delete"/> 
				</logic:notEqual>
				<html:submit property="op" value="Clear" /> 
			</TD>
		</TR>
	</TABLE>

<BR>&nbsp;<BR>

<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<%= request.getAttribute("SolverInfoDef.table") %> 
</TABLE>


</html:form>
