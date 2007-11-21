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
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<tiles:importAttribute />

<tt:confirm name="confirmDelete">The status will be deleted. Continue?</tt:confirm>

<html:form action="/deptStatusTypeEdit">
<html:hidden property="uniqueId"/><html:errors property="uniqueId"/>
<input type='hidden' name='op2' value=''>
<logic:notEqual name="deptStatusTypeEditForm" property="op" value="List">
	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<logic:equal name="deptStatusTypeEditForm" property="op" value="Save">
						Add
						</logic:equal>
						<logic:notEqual name="deptStatusTypeEditForm" property="op" value="Save">
							Edit
						</logic:notEqual>
						Status Type
					</tt:section-title>
					<logic:equal name="deptStatusTypeEditForm" property="op" value="Save">
						<html:submit property="op" value="Save" accesskey="S" title="Save Status (Alt+S)"/>
					</logic:equal>
					<logic:notEqual name="deptStatusTypeEditForm" property="op" value="Save">
						<html:submit property="op" value="Update" accesskey="U" title="Update Status (Alt+U)"/>
						<html:submit property="op" value="Delete" onclick="return confirmDelete();" accesskey="D" title="Delete Status (Alt+D)"/> 
					</logic:notEqual>
					<html:submit property="op" value="Back" title="Return to Status Types (Alt+B)" accesskey="B"/>
				</tt:section-header>
			</TD>
		</TR>

		<TR>
			<TD>Reference:</TD>
			<TD>
				<html:text property="reference" size="20" maxlength="20"/>
				&nbsp;<html:errors property="reference"/>
			</TD>
		</TR>

		<TR>
			<TD>Label:</TD>
			<TD>
				<html:text property="label" size="60" maxlength="60"/>
				&nbsp;<html:errors property="label"/>
			</TD>
		</TR>

		<TR>
			<TD>Apply:</TD>
			<TD>
			<html:select property="apply">
				<html:optionsCollection name="deptStatusTypeEditForm" property="applyOptions" label="value" value="id"/>
			</html:select>
			&nbsp;<html:errors property="apply"/>
		</TR>
		
		<TR><TD>Owner View:</TD><TD><html:checkbox property="canOwnerView"/></TD></TR>
		<TR><TD>Owner Limited Edit:</TD><TD><html:checkbox property="canOwnerLimitedEdit"/></TD></TR>
		<TR><TD>Owner Edit:</TD><TD><html:checkbox property="canOwnerEdit"/></TD></TR>
		<TR><TD>Manager View:</TD><TD><html:checkbox property="canManagerView"/></TD></TR>
		<TR><TD>Manager Limited Edit:</TD><TD><html:checkbox property="canManagerLimitedEdit"/></TD></TR>
		<TR><TD>Manager Edit:</TD><TD><html:checkbox property="canManagerEdit"/></TD></TR>
		<TR><TD>Audit:</TD><TD><html:checkbox property="canAudit"/></TD></TR>
		<TR><TD>Timetable:</TD><TD><html:checkbox property="canTimetable"/></TD></TR>
		<TR><TD>Commit:</TD><TD><html:checkbox property="canCommit"/></TD></TR>
		
		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>
		<TR>
			<TD align="right" colspan="2">
				<logic:equal name="deptStatusTypeEditForm" property="op" value="Save">
					<html:submit property="op" value="Save" accesskey="S" title="Save Status (Alt+S)"/>
				</logic:equal>
				<logic:notEqual name="deptStatusTypeEditForm" property="op" value="Save">
					<html:submit property="op" value="Update" accesskey="U" title="Update Status (Alt+U)"/>
					<html:submit property="op" value="Delete" onclick="return confirmDelete();" accesskey="D" title="Delete Status (Alt+D)"/> 
				</logic:notEqual>
				<html:submit property="op" value="Back" title="Return to Status Types (Alt+B)" accesskey="B"/>
			</TD>
		</TR>
	</TABLE>
</logic:notEqual>
<logic:equal name="deptStatusTypeEditForm" property="op" value="List">
	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
		<tr>
			<td colspan='5'>
				<tt:section-header>
					<tt:section-title>Status Types</tt:section-title>
					<html:submit property="op" value="Add Status Type" accesskey="A" title="Create New Status Type (Alt+A)"/>
				</tt:section-header>
			</td>
		</tr>
		<%= request.getAttribute("DeptStatusType.table") %> 
		<tr>
			<td colspan='5'>
				<tt:section-title/>
			</td>
		</tr>
		<tr>
			<td colspan='5' align="right">
				<html:submit property="op" value="Add Status Type" accesskey="A" title="Create New Status Type (Alt+A)"/>
			</td>
		</tr>
	</TABLE>
</logic:equal>
</html:form>