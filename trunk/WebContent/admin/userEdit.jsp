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

<html:form action="/userEdit">

	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<DIV class="WelcomeRowHead">
				<logic:equal name="userEditForm" property="op" value="Add New">
				Add
				</logic:equal>
				<logic:notEqual name="userEditForm" property="op" value="Add New">
				Edit
				</logic:notEqual>
				User
				</DIV>
			</TD>
		</TR>

		<TR>
			<TD>External ID:</TD>
			<TD>
				<logic:equal name="userEditForm" property="op" value="Add New">
					<html:text property="externalId" size="15" maxlength="15"/>
				</logic:equal>
				<logic:notEqual name="userEditForm" property="op" value="Add New">
					<bean:write name="userEditForm" property="externalId"/>
					<html:hidden property="externalId"/>
				</logic:notEqual>
				&nbsp;<html:errors property="externalId"/>
			</TD>
		</TR>

		<TR>
			<TD>User Name:</TD>
			<TD>
				<html:text property="name" size="25" maxlength="25"/>
				&nbsp;<html:errors property="name"/>
			</TD>
		</TR>

		<TR>
			<TD>Password:</TD>
			<TD>
				<html:password property="password" size="25" maxlength="40"/>
				&nbsp;<html:errors property="password"/>
			</TD>
		</TR>
		
		<TR>
			<TD align="right" colspan="2">
				<html:submit property="op">
					<bean:write name="userEditForm" property="op" />
				</html:submit> 
				<logic:notEqual name="userEditForm" property="op" value="Add New">
					<html:submit property="op" value="Delete"/> 
				</logic:notEqual>
				<html:submit property="op" value="Clear" /> 
			</TD>
		</TR>
	</TABLE>

<BR>&nbsp;<BR>

<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
	<%= request.getAttribute("Users.table") %> 
</TABLE>


</html:form>