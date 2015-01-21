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
