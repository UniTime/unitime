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
<%@ page language="java" pageEncoding="ISO-8859-1"%>
<%@ page import="org.unitime.timetable.form.RollForwardSessionForm"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean"%> 
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
 
<html> 
	<head>
		<title>Roll Forward Session</title>
	</head>
	<body>
<script language="javascript">displayLoading();</script>
	<%// Get Form 
			String frmName = "rollForwardSessionForm";
			RollForwardSessionForm frm = (RollForwardSessionForm) request
					.getAttribute(frmName);
%>
<% if (frm.isAdmin()) {  %>
		<html:form action="/rollForwardSession">
		<TABLE border="0" cellspacing="5" cellpadding="5">
		<logic:messagesPresent>
		<TR>
			<TD align="left" class="errorCell">
					<B><U>ERRORS</U></B><BR>
				<BLOCKQUOTE>
				<UL>
				    <html:messages id="error">
				      <LI>
						${error}
				      </LI>
				    </html:messages>
			    </UL>
			    </BLOCKQUOTE>
			</TD>
		</TR>
		</logic:messagesPresent>
		
		<tr>
			<td valign="top" nowrap ><b>Session To Roll Foward To: </b>
			<html:select style="width:200;" property="sessionToRollForwardTo" onchange="displayElement('loading', true);submit();">
			<html:optionsCollection property="toSessions" value="uniqueId" label="label"  /></html:select>
			</td>			
		</tr>
		<tr>
		<td>&nbsp;
		</td>
		</tr>
		<tr>
			<td valign="top" nowrap ><html:checkbox name="<%=frmName%>" property="rollForwardDatePatterns"/> Roll Date Pattern Data Forward From Session: 
			<html:select style="width:200;" property="sessionToRollDatePatternsForwardFrom">
			<html:optionsCollection property="fromSessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
			<td valign="top" nowrap ><html:checkbox name="<%=frmName%>" property="rollForwardTimePatterns"/> Roll Time Pattern Data Forward From Session: 
			<html:select style="width:200;" property="sessionToRollTimePatternsForwardFrom">
			<html:optionsCollection property="fromSessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
			<td valign="top" nowrap ><html:checkbox name="<%=frmName%>" property="rollForwardDepartments"/> Roll Departments Forward From Session: 
			<html:select style="width:200;" property="sessionToRollDeptsFowardFrom">
			<html:optionsCollection property="fromSessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
			<td valign="top" nowrap ><html:checkbox name="<%=frmName%>" property="rollForwardManagers"/> Roll Manager Data Forward From Session: 
			<html:select style="width:200;" property="sessionToRollManagersForwardFrom">
			<html:optionsCollection property="fromSessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
			<td valign="top" nowrap ><html:checkbox name="<%=frmName%>" property="rollForwardRoomData"/> Roll Building and Room Data Forward From Session: 
			<html:select style="width:200;" property="sessionToRollRoomDataForwardFrom">
			<html:optionsCollection property="fromSessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
			<td valign="top" nowrap><html:checkbox name="<%=frmName%>" property="rollForwardSubjectAreas"/> Roll Subject Areas Forward From Session: 
			<html:select style="width:200;" property="sessionToRollSubjectAreasForwardFrom">
			<html:optionsCollection property="fromSessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
			<td valign="top" nowrap><html:checkbox name="<%=frmName%>" property="rollForwardInstructorData"/> Roll Instructor Data Forward From Session: 
			<html:select style="width:200;" property="sessionToRollInstructorDataForwardFrom">
			<html:optionsCollection property="fromSessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
			<td valign="top" nowrap><html:checkbox name="<%=frmName%>" property="rollForwardCourseOfferings"/> Roll Course Offerings Forward From Session: 
			<html:select style="width:200;" property="sessionToRollCourseOfferingsForwardFrom">
			<html:optionsCollection property="fromSessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<TR>
			<TD valign="top">
			<table><tr><td valign="top">
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;For Subject Areas:
				</td><td>
				<html:select size="<%=String.valueOf(Math.min(7,frm.getSubjectAreas().size()))%>" name="<%=frmName%>" styleClass="cmb" property="rollForwardSubjectAreaIds" multiple="true" onfocus="setUp();" onkeypress="return selectSearch(event, this);" onkeydown="return checkKey(event, this);">
					<html:optionsCollection property="subjectAreas" label="subjectAreaAbbreviation" value="uniqueId" />
				</html:select>
			</td></tr></table>
			</td>
		</tr>
		<tr>
			<td valign="top">
			<table><tr>	<td valign="top" nowrap><html:checkbox name="<%=frmName%>" property="rollForwardClassPreferences"/> Roll Forward Preferences to the Class Level For Subject Areas: 
				</td><td>
				<html:select size="<%=String.valueOf(Math.min(7,frm.getSubjectAreas().size()))%>" name="<%=frmName%>" styleClass="cmb" property="rollForwardClassPrefsSubjectIds" multiple="true" onfocus="setUp();" onkeypress="return selectSearch(event, this);" onkeydown="return checkKey(event, this);">
					<html:optionsCollection property="subjectAreas" label="subjectAreaAbbreviation" value="uniqueId" />
				</html:select>
			</td></tr></table>
			</td>
		</tr>
		<tr>
			<td valign="top">
			<table><tr>	<td valign="top" nowrap><html:checkbox name="<%=frmName%>" property="rollForwardClassInstructors"/> Roll Forward Class Instructors For Subject Areas: 
				</td><td>
				<html:select size="<%=String.valueOf(Math.min(7,frm.getSubjectAreas().size()))%>" name="<%=frmName%>" styleClass="cmb" property="rollForwardClassInstrSubjectIds" multiple="true" onfocus="setUp();" onkeypress="return selectSearch(event, this);" onkeydown="return checkKey(event, this);">
					<html:optionsCollection property="subjectAreas" label="subjectAreaAbbreviation" value="uniqueId" />
				</html:select>
			</td></tr></table>
			</td>
		</tr>
		<tr>
			<td valign="top">
			<table><tr>	<td valign="top" nowrap><html:checkbox name="<%=frmName%>" property="addNewCourseOfferings"/> Add New Course Offerings For Subject Areas:<br><i>Note: Only use this after all existing course<br> offerings have been rolled forward to avoid<br> errors with cross lists.</i>
				</td><td>
				<html:select size="<%=String.valueOf(Math.min(7,frm.getSubjectAreas().size()))%>" name="<%=frmName%>" styleClass="cmb" property="addNewCourseOfferingsSubjectIds" multiple="true" onfocus="setUp();" onkeypress="return selectSearch(event, this);" onkeydown="return checkKey(event, this);">
					<html:optionsCollection property="subjectAreas" label="subjectAreaAbbreviation" value="uniqueId" />
				</html:select>
			</td></tr></table>
			</td>
		</tr>
		<tr>
			<td valign="top" nowrap><html:checkbox name="<%=frmName%>" property="rollForwardExamConfiguration"/> Roll Exam Configuration Data Forward From Session: 
			<html:select style="width:200;" property="sessionToRollExamConfigurationForwardFrom">
			<html:optionsCollection property="fromSessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
			<td valign="top" nowrap><html:checkbox name="<%=frmName%>" property="rollForwardMidtermExams"/> Roll Midterm Exams Forward From Session
			</td>	
		</tr>
		<tr>
			<td valign="top" nowrap><html:checkbox name="<%=frmName%>" property="rollForwardFinalExams"/> Roll Final Exams Forward From Session
			</td>		
		</tr>
		<tr><td>&nbsp;<br>&nbsp;<br></td></tr>
		<tr>
			<td>
				&nbsp;&nbsp;&nbsp;
				<logic:equal name="<%=frmName%>" property="admin" value="true">
					&nbsp;&nbsp;&nbsp;
					<html:submit property="op" accesskey="M" styleClass="btn" onclick="displayElement('loading', true);">
						<bean:message key="button.rollForward" />
					</html:submit>
				</logic:equal>
			</TD>
		</TR>
		</TABLE>
		</html:form>
		<% } else { %>
		<b>User must be an administrator to roll foward to a session.</b>
		<% } %>
	<script language="javascript">displayElement('loading', false);</script>
	</body>
</html>

