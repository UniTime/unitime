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

<tt:confirm name="confirmDelete">The status will be deleted. Continue?</tt:confirm>

<html:form action="/deptStatusTypeEdit">
<html:hidden property="uniqueId"/><html:errors property="uniqueId"/>
<input type='hidden' name='op2' value=''>
<logic:notEqual name="deptStatusTypeEditForm" property="op" value="List">
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="3">
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
			<TD colspan='2'>
				<html:text property="reference" size="20" maxlength="20"/>
				&nbsp;<html:errors property="reference"/>
			</TD>
		</TR>

		<TR>
			<TD>Label:</TD>
			<TD colspan='2'>
				<html:text property="label" size="60" maxlength="60"/>
				&nbsp;<html:errors property="label"/>
			</TD>
		</TR>

		<TR>
			<TD>Apply:</TD>
			<TD colspan='2'>
			<html:select property="apply">
				<html:optionsCollection name="deptStatusTypeEditForm" property="applyOptions" label="value" value="id"/>
			</html:select>
			&nbsp;<html:errors property="apply"/>
		</TR>
		
		<TR><TD colspan='3'><tt:section-title><br>Course Timetabling</tt:section-title></TD></TR>
		<TR><TD>Owner View:</TD><TD><html:checkbox property="canOwnerView"/></TD>
			<TD><i>The manager of the department of the subject area of a class can view class data but cannot change it.</i></TD></TR>
		<TR><TD>Owner Limited Edit:</TD><TD><html:checkbox property="canOwnerLimitedEdit"/></TD>
			<TD><i>The manager of the department of the subject area of a class can assign instructors.</i></TD></TR>
		<TR><TD>Owner Edit:</TD><TD><html:checkbox property="canOwnerEdit"/></TD>
			<TD><i>The manager of the department of the subject area of a class can edit the class data.</i></TD></TR>
		<TR><TD>Manager View:</TD><TD><html:checkbox property="canManagerView"/></TD>
			<TD><i>The schedule manager of the class (manager of the department that is managing the class, if externally managed) can view class data but cannot change it.</i></TD></TR>
		<TR><TD nowrap>Manager Limited Edit:</TD><TD><html:checkbox property="canManagerLimitedEdit"/></TD>
			<TD><i>The schedule manager of the class can assign instructors.</i></TD></TR>
		<TR><TD>Manager Edit:</TD><TD><html:checkbox property="canManagerEdit"/></TD>
			<TD><i>The schedule manager of the class can edit class data.</i></TD></TR>
		<TR><TD>Audit:</TD><TD><html:checkbox property="canAudit"/></TD>
			<TD><i>The schedule manager can run the solver, but cannot save timetables.</i></TD></TR>
		<TR><TD>Timetable:</TD><TD><html:checkbox property="canTimetable"/></TD>
			<TD><i>The schedule manager can create timetables and save them, but cannot commit.</i></TD></TR>
		<TR><TD>Commit:</TD><TD><html:checkbox property="canCommit"/></TD>
			<TD><i>The schedule manager can commit a timetable.</i></TD></TR>
		
		<TR><TD colspan='3'><tt:section-title><br>Examination Timetabling</tt:section-title></TD></TR>
		<TR><TD>Exam View:</TD><TD><html:checkbox property="canExamView"/></TD>
			<TD><i>The schedule manager of the department can view examination data of the appropriate subject areas.</i></TD></TR>
		<TR><TD>Exam Edit:</TD><TD><html:checkbox property="canExamEdit"/></TD>
			<TD><i>The schedule manager of the department can edit examination data of the appropriate subject areas.</i></TD></TR>
		<TR><TD>Exam Timetable:</TD><TD><html:checkbox property="canExamTimetable"/></TD>
			<TD><i>The examination timetabling manager can edit all examination data and create examination schedule.</i></TD></TR>
		
		<TR><TD colspan='3'><tt:section-title><br>Student Sectioning</tt:section-title></TD></TR>
		<TR><TD nowrap>Registration:</TD><TD><html:checkbox property="canPreRegisterStudents"/></TD>
			<TD><i>Students are able to use the Student Course Requests page to fill in their course and free time requests.</i></TD></TR>
		<TR><TD nowrap>Assistant:</TD><TD><html:checkbox property="canSectioningStudents"/></TD>
			<TD><i>Unauthenticated users and students are able to use the Student Scheduling Assistant.</i></TD></TR>
		<TR><TD nowrap>Online Sectioning:</TD><TD><html:checkbox property="canOnlineSectionStudents"/></TD>
			<TD><i>Online student sectioning is fully enabled (e.g., courses must be locked before a change, conflicting students are automatically re-sectioned).</i></TD></TR>

		<TR><TD colspan='3'><tt:section-title><br>No-Role Reports</tt:section-title></TD></TR>
		<TR><TD>Class Schedule:</TD><TD><html:checkbox property="canNoRoleReportClass"/></TD>
			<TD><i>Class schedule can be presented to unauthenticated users or authenticated users without a role.</i></TD></TR>
		<TR><TD>Final Examination Schedule:</TD><TD><html:checkbox property="canNoRoleReportExamFin"/></TD>
			<TD><i>Final examination schedule can be presented to unauthenticated users or authenticated users without a role.</i></TD></TR>
		<TR><TD nowrap>Midterm Examination Schedule:</TD><TD><html:checkbox property="canNoRoleReportExamMid"/></TD>
			<TD><i>Midterm examination schedule can be presented to unauthenticated users or authenticated users without a role.</i></TD></TR>

		<TR><TD colspan='3'><tt:section-title><br>Other</tt:section-title></TD></TR>
		<TR><TD nowrap>Test Session:</TD><TD><html:checkbox property="testSession"/></TD>
			<TD><i>Academic session is marked as a test session. Such a session is not available to no-role users. Also, classes and examinations do not generate any events.</i></TD></TR>
		
		<TR>
			<TD colspan='3'>
				<tt:section-title/>
			</TD>
		</TR>
		<TR>
			<TD align="right" colspan="3">
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
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
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
