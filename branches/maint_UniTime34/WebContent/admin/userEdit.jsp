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
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<tiles:importAttribute />

<tt:confirm name="confirmDelete">The user will be deleted. Continue?</tt:confirm>

<html:form action="/userEdit">
<logic:notEqual name="userEditForm" property="op" value="List">
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<logic:equal name="userEditForm" property="op" value="Save">
							Add
						</logic:equal>
						<logic:notEqual name="userEditForm" property="op" value="Save">
							Edit
						</logic:notEqual>
						User
					</tt:section-title>
					<logic:equal name="userEditForm" property="op" value="Save">
						<html:submit property="op" value="Save" accesskey="S" title="Save User (Alt+S)"/>
					</logic:equal>
					<logic:notEqual name="userEditForm" property="op" value="Save">
						<html:submit property="op" value="Update" accesskey="U" title="Update User (Alt+U)"/>
						<html:submit property="op" value="Delete" onclick="return confirmDelete();" accesskey="D" title="Delete User (Alt+D)"/> 
					</logic:notEqual>
					<html:submit property="op" value="Back" title="Return to Users (Alt+B)" accesskey="B"/> 
				</tt:section-header>
			</TD>
		</TR>

		<TR>
			<TD>External ID:</TD>
			<TD>
				<logic:equal name="userEditForm" property="op" value="Save">
					<html:text property="externalId" size="15" maxlength="15"/>
				</logic:equal>
				<logic:notEqual name="userEditForm" property="op" value="Save">
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
			<TD colspan="2">
				<tt:section-title/>
			</TD>
		</TR>
		
		<TR>
			<TD align="right" colspan="2">
				<logic:equal name="userEditForm" property="op" value="Save">
					<html:submit property="op" value="Save" accesskey="S" title="Save User (Alt+S)"/>
				</logic:equal>
				<logic:notEqual name="userEditForm" property="op" value="Save">
					<html:submit property="op" value="Update" accesskey="U" title="Update User (Alt+U)"/>
					<html:submit property="op" value="Delete" onclick="return confirmDelete();" accesskey="D" title="Delete User (Alt+D)"/> 
				</logic:notEqual>
				<html:submit property="op" value="Back" title="Return to Users (Alt+B)" accesskey="B"/> 
			</TD>
		</TR>
	</TABLE>

</logic:notEqual>
<logic:equal name="userEditForm" property="op" value="List">
<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<tr>
			<td colspan='3'>
				<tt:section-header>
					<tt:section-title>Users</tt:section-title>
					<html:submit property="op" value="Request Password Change" accesskey="R" title="Request password change (Alt+R)"/>
					<html:submit property="op" value="Add User" accesskey="A" title="Create New User (Alt+A)"/>
				</tt:section-header>
			</td>
		</tr>
		<%= request.getAttribute("Users.table") %> 
		<tr>
			<td colspan='3'>
				<tt:section-title/>
			</td>
		</tr>
		<tr>
			<td colspan='3' align="right">
				<html:submit property="op" value="Request Password Change" accesskey="R" title="Request password change (Alt+R)"/>
				<html:submit property="op" value="Add User" accesskey="A" title="Create New User (Alt+A)"/>
			</td>
		</tr>
</TABLE>
</logic:equal>

</html:form>
