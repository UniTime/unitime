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
<%@ page language="java" pageEncoding="ISO-8859-1"%>
<%@ page import="org.unitime.timetable.form.RollForwardSessionForm"%>
<%@ page import="org.unitime.timetable.util.SessionRollForward"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
 
<html> 
	<head>
		<title>Roll Forward Session</title>
	</head>
	<body>
<script language="javascript">displayLoading();</script>
	<%// Get Form 
			String frmName = "rollForwardSessionForm";
			RollForwardSessionForm frm = (RollForwardSessionForm) request.getAttribute(frmName);
	%>
		<html:form action="/rollForwardSession">
		<table width="100%" cellspacing="0" cellpadding="3">
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
	<logic:notEmpty name="table" scope="request">
		<TR><TD>
			<tt:section-header>
				<tt:section-title>Roll Forward(s) In Progress</tt:section-title>
				<%--
				<html:submit property="op" accesskey="R" styleClass="btn" onclick="displayLoading();">Refresh</html:submit>
				--%>
			</tt:section-header>
		</TD></TR>
		<TR><TD>
			<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
				<bean:write name="table" scope="request" filter="false"/>
			</TABLE>
		</TD></TR>
		<TR><TD>&nbsp;</TD></TR>
	</logic:notEmpty>
	<logic:notEmpty name="log" scope="request">
		<TR>
			<TD colspan='2'>
				<tt:section-header>
					<tt:section-title>
						Log of <bean:write name="logname" scope="request" filter="false"/>
					</tt:section-title>
					<bean:define id="logid" name="logid" scope="request"/>
					<input type="hidden" name="log" value="<%=logid%>">
					<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh" title="Refresh Log (Alt+R)"/>
				</tt:section-header>
			</TD>
		</TR>
		<TR>
  			<TD colspan='2'>
  				<blockquote>
	  				<bean:write name="log" scope="request" filter="false"/>
  				</blockquote>
  			</TD>
		</TR>
	</logic:notEmpty>			
	<TR><TD>
		<tt:section-header>
			<tt:section-title>Roll Forward Actions</tt:section-title>
					<html:submit property="op" accesskey="M" styleClass="btn" onclick="displayLoading();">
					<bean:message key="button.rollForward" />
				</html:submit>
		</tt:section-header>
	</TD></TR>
		<tr>
			<td valign="middle" nowrap ><b>Session To Roll Forward To: </b>
			<html:select style="width:200px;" property="sessionToRollForwardTo" onchange="displayLoading();submit();">
			<html:optionsCollection property="toSessions" value="uniqueId" label="label"  /></html:select>
			</td>			
		</tr>
		<tr>
		<td>&nbsp;
		</td>
		</tr>
		<tr>
			<td valign="middle" nowrap ><html:checkbox name="<%=frmName%>" property="rollForwardDepartments"/> Roll Departments Forward From Session: 
			<html:select style="width:200px;" property="sessionToRollDeptsFowardFrom">
			<html:optionsCollection property="fromSessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
			<td valign="middle" nowrap><html:checkbox name="<%=frmName%>" property="rollForwardSessionConfig" onclick="document.getElementById('configNote').style.display = (this.checked ? 'table-row' : 'none');"/> Roll Session Configuration Forward From Session: 
			<html:select style="width:200px;" property="sessionToRollSessionConfigForwardFrom">
			<html:optionsCollection property="fromSessions" value="uniqueId" label="label" /></html:select>
		</tr>
		<tr style="display:none;" id="configNote">
			<td valign="middle" style="white-space: wrap; max-width: 600px; padding-left: 20px;">
				<i>Session configuration contains application configuration, standard notes, event room notes, and break times that are applied directly to the session and/or its department(s).
				Individual room notes and break times are rolled forward with the rooms.</i>
			</td>
		</tr>
		<logic:equal name="<%=frmName%>" property="rollForwardSessionConfig" value="true">
			<script>document.getElementById('configNote').style.display = 'table-row';</script>
		</logic:equal>
		<tr>
			<td valign="middle" nowrap ><html:checkbox name="<%=frmName%>" property="rollForwardManagers"/> Roll Manager Data Forward From Session: 
			<html:select style="width:200px;" property="sessionToRollManagersForwardFrom">
			<html:optionsCollection property="fromSessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
			<td valign="middle" nowrap ><html:checkbox name="<%=frmName%>" property="rollForwardRoomData"/> Roll Building and Room Data Forward From Session: 
			<html:select style="width:200px;" property="sessionToRollRoomDataForwardFrom">
			<html:optionsCollection property="fromSessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
			<td valign="middle" nowrap ><html:checkbox name="<%=frmName%>" property="rollForwardDatePatterns"/> Roll Date Pattern Data Forward From Session: 
			<html:select style="width:200px;" property="sessionToRollDatePatternsForwardFrom">
			<html:optionsCollection property="fromSessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
			<td valign="middle" nowrap ><html:checkbox name="<%=frmName%>" property="rollForwardTimePatterns"/> Roll Time Pattern Data Forward From Session: 
			<html:select style="width:200px;" property="sessionToRollTimePatternsForwardFrom">
			<html:optionsCollection property="fromSessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
			<td valign="middle" nowrap><html:checkbox name="<%=frmName%>" property="rollForwardSubjectAreas"/> Roll Subject Areas Forward From Session: 
			<html:select style="width:200px;" property="sessionToRollSubjectAreasForwardFrom">
			<html:optionsCollection property="fromSessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
			<td valign="middle" nowrap><html:checkbox name="<%=frmName%>" property="rollForwardInstructorData"/> Roll Instructor Data Forward From Session: 
			<html:select style="width:200px;" property="sessionToRollInstructorDataForwardFrom">
			<html:optionsCollection property="fromSessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
			<td valign="middle">
			<table style="margin-left: 50px;"><tr>
			    <td valign="top">For Departments:</td>
			    <td><html:select size="<%=String.valueOf(Math.min(7,frm.getDepartments().size()))%>" name="<%=frmName%>" styleClass="cmb" property="rollForwardDepartmentIds" multiple="true">
					<html:optionsCollection property="departments" label="label" value="uniqueId" />
				</html:select>
			    </td></tr>
			</table></td>
		</tr>
		<tr>
			<td valign="middle" nowrap><html:checkbox name="<%=frmName%>" property="rollForwardCourseOfferings"/> Roll Course Offerings Forward From Session: 
			<html:select style="width:200px;" property="sessionToRollCourseOfferingsForwardFrom">
			<html:optionsCollection property="fromSessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
			<td valign="middle">
			<table style="margin-left: 50px;"><tr>
			    <td valign="top">For Subject Areas:</td>
			    <td><html:select size="<%=String.valueOf(Math.min(7,frm.getSubjectAreas().size()))%>" name="<%=frmName%>" styleClass="cmb" property="rollForwardSubjectAreaIds" multiple="true">
					<html:optionsCollection property="subjectAreas" label="subjectAreaAbbreviation" value="uniqueId" />
				</html:select>
			    </td></tr>
			<tr><td style="padding-top: 20px;" rowspan="2" valign="top">Scheduling Subpart Level Time Preference Options:</td><td style="padding-top: 20px;"><html:radio property="subpartTimePrefsAction" value="<%= SessionRollForward.ROLL_PREFS_ACTION %>"> Roll forward scheduling subpart time preferences</html:radio></td></tr>
			<tr><td><html:radio property="subpartTimePrefsAction" value="<%= SessionRollForward.DO_NOT_ROLL_ACTION %>"> Do not roll forward scheduling subpart time preferences</html:radio></td></tr>
			<tr><td style="padding-top: 20px;" rowspan="2" valign="top">Scheduling Subpart Level Location Preference Options:</td><td style="padding-top: 20px;"><html:radio property="subpartLocationPrefsAction" value="<%= SessionRollForward.ROLL_PREFS_ACTION %>"> Roll forward scheduling subpart location preferences</html:radio></td></tr>
			<tr><td><html:radio property="subpartLocationPrefsAction" value="<%= SessionRollForward.DO_NOT_ROLL_ACTION %>"> Do not roll forward scheduling subpart location preferences</html:radio></td></tr>
			<tr><td style="padding-top: 20px;" rowspan="2" valign="top">Class Level Preference Options:</td><td style="padding-top: 20px;"><html:radio property="classPrefsAction" value="<%= SessionRollForward.DO_NOT_ROLL_ACTION %>"> Ignore all class level preferences</html:radio></td></tr>
			<tr><td><html:radio property="classPrefsAction" value="<%= SessionRollForward.PUSH_UP_ACTION %>"> Promote appropriate class level preferences to subparts</html:radio></td></tr>
			<tt:propertyEquals name="unitime.rollforward.allowClassPrefs" value="true">
				<tr><td></td><td><html:radio property="classPrefsAction" value="<%= SessionRollForward.ROLL_PREFS_ACTION %>"> Roll forward class level preferences</html:radio></td></tr>
			</tt:propertyEquals>
			<tr><td style="padding-top: 20px;" rowspan="4" valign="top">Distribution Preferences:</td>
				<td style="padding-top: 20px;"><html:radio property="rollForwardDistributions" value="<%= SessionRollForward.DistributionMode.ALL.name() %>"> Roll forward all distribution preferences</html:radio></td></tr>
			</td></tr>
			<tr><td><html:radio property="rollForwardDistributions" value="<%= SessionRollForward.DistributionMode.MIXED.name() %>"> Roll forward all distribution preferences, except those that are put solely on classes</html:radio></td></tr>
			<tr><td><html:radio property="rollForwardDistributions" value="<%= SessionRollForward.DistributionMode.SUBPART.name() %>"> Roll forward only distribution preferences that are put solely on subparts</html:radio></td></tr>
			<tr><td><html:radio property="rollForwardDistributions" value="<%= SessionRollForward.DistributionMode.NONE.name() %>"> Do not roll forward distribution preferences</html:radio></td></tr>
			<tr><td style="padding-top: 20px;" rowspan="3" valign="top">Cancelled Classes:</td>
				<td style="padding-top: 20px;"><html:radio property="cancelledClassAction" value="<%= SessionRollForward.CancelledClassAction.KEEP.name() %>"> Roll forward cancelled classes as they are (keep)</html:radio></td></tr>
			<tr><td><html:radio property="cancelledClassAction" value="<%= SessionRollForward.CancelledClassAction.REOPEN.name() %>"> Roll forward cancelled classes as offered (reopen)</html:radio></td></tr>
			<tr><td><html:radio property="cancelledClassAction" value="<%= SessionRollForward.CancelledClassAction.SKIP.name() %>"> Do not roll forward cancelled classes (skip)</html:radio></td></tr>
			</table></td>
		</tr>
		<tr>
			<td valign="middle">
			<table><tr>	<td valign="middle" nowrap><html:checkbox name="<%=frmName%>" property="rollForwardClassInstructors"/> Roll Forward Class Instructors For Subject Areas: 
				</td><td>
				<html:select size="<%=String.valueOf(Math.min(7,frm.getSubjectAreas().size()))%>" name="<%=frmName%>" styleClass="cmb" property="rollForwardClassInstrSubjectIds" multiple="true">
					<html:optionsCollection property="subjectAreas" label="subjectAreaAbbreviation" value="uniqueId" />
				</html:select>
			</td></tr></table>
			</td>
		</tr>
		<tr>
			<td valign="middle">
			<table><tr>	<td valign="middle" nowrap><html:checkbox name="<%=frmName%>" property="rollForwardOfferingCoordinators"/> Roll Forward Offering Coordinators For Subject Areas: 
				</td><td>
				<html:select size="<%=String.valueOf(Math.min(7,frm.getSubjectAreas().size()))%>" name="<%=frmName%>" styleClass="cmb" property="rollForwardOfferingCoordinatorsSubjectIds" multiple="true">
					<html:optionsCollection property="subjectAreas" label="subjectAreaAbbreviation" value="uniqueId" />
				</html:select>
			</td></tr></table>
			</td>
		</tr>
		<tr>
			<td valign="middle">
			<table><tr>	<td valign="middle" nowrap><html:checkbox name="<%=frmName%>" property="rollForwardTeachingRequests"/> Roll Forward Teaching Request For Subject Areas: 
				</td><td>
				<html:select size="<%=String.valueOf(Math.min(7,frm.getSubjectAreas().size()))%>" name="<%=frmName%>" styleClass="cmb" property="rollForwardTeachingRequestsSubjectIds" multiple="true">
					<html:optionsCollection property="subjectAreas" label="subjectAreaAbbreviation" value="uniqueId" />
				</html:select>
			</td></tr></table>
			</td>
		</tr>
		
		<tr>
			<td valign="middle">
			<table>
				<tr>
					<td valign="middle" nowrap><html:checkbox name="<%=frmName%>" property="addNewCourseOfferings"/>Add New Course Offerings For Subject Areas:
					<div style="margin-left: 20px;"><i>Note: Only use this after all existing course<br> offerings have been rolled forward to avoid<br> errors with cross lists.</i></div>
				</td><td>
				<html:select size="<%=String.valueOf(Math.min(7,frm.getSubjectAreas().size()))%>" name="<%=frmName%>" styleClass="cmb" property="addNewCourseOfferingsSubjectIds" multiple="true">
					<html:optionsCollection property="subjectAreas" label="subjectAreaAbbreviation" value="uniqueId" />
				</html:select>
			</td></tr></table>
			</td>
		</tr>
		<tr>
			<td valign="middle" nowrap><html:checkbox name="<%=frmName%>" property="rollForwardExamConfiguration"/> Roll Exam Configuration Data Forward From Session: 
			<html:select style="width:200px;" property="sessionToRollExamConfigurationForwardFrom">
			<html:optionsCollection property="fromSessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr>
			<td valign="middle" nowrap><html:checkbox name="<%=frmName%>" property="rollForwardMidtermExams"/> Roll Midterm Exams Forward
			<table style="margin-left: 50px;"><tr>
			    <tr><td rowspan="3" valign="top">Preferences:</td>
			    <td><html:radio property="midtermExamsPrefsAction" value="<%= SessionRollForward.EXAMS_ALL_PREF %>">Roll forward all midterm examination preferences</html:radio></td></tr>
			    <tr><td><html:radio property="midtermExamsPrefsAction" value="<%= SessionRollForward.EXAMS_ROOM_PREFS %>">Roll forward building, room feature and room group preferences (exclude period and individual room preferences)</html:radio></td></tr>
			    <tr><td><html:radio property="midtermExamsPrefsAction" value="<%= SessionRollForward.EXAMS_NO_PREF %>">Do not roll forward any midterm examination preferences</html:radio></td></tr>
			</table>
			</td>	
		</tr>
		<tr>
			<td valign="middle" nowrap><html:checkbox name="<%=frmName%>" property="rollForwardFinalExams"/> Roll Final Exams Forward
			<table style="margin-left: 50px;"><tr>
			    <tr><td rowspan="3" valign="top">Preferences:</td>
			    <td><html:radio property="finalExamsPrefsAction" value="<%= SessionRollForward.EXAMS_ALL_PREF %>">Roll forward all final examination preferences</html:radio></td></tr>
			    <tr><td><html:radio property="finalExamsPrefsAction" value="<%= SessionRollForward.EXAMS_ROOM_PREFS %>">Roll forward building, room feature and room group preferences (exclude period and individual room preferences)</html:radio></td></tr>
			    <tr><td><html:radio property="finalExamsPrefsAction" value="<%= SessionRollForward.EXAMS_NO_PREF %>">Do not roll forward any final examination preferences</html:radio></td></tr>
			</table>
			</td>		
		</tr>
		<tr>
			<td valign="middle" nowrap><html:checkbox name="<%=frmName%>" property="rollForwardStudents"/> Import Last-Like Course Demands
			<table style="margin-left: 50px;">
				<tr><td><html:radio property="rollForwardStudentsMode" value="<%= SessionRollForward.StudentEnrollmentMode.LAST_LIKE.name() %>"> Copy Last-like Course Demands From Previous Session</html:radio></td></tr>
				<tr><td><html:radio property="rollForwardStudentsMode" value="<%= SessionRollForward.StudentEnrollmentMode.STUDENT_CLASS_ENROLLMENTS.name() %>"> Import Last-like Course Demands From Student Class Enrollments Of Previous Session</html:radio></td></tr>
				<tr><td><html:radio property="rollForwardStudentsMode" value="<%= SessionRollForward.StudentEnrollmentMode.STUDENT_COURSE_REQUESTS.name() %>"> Import Last-like Course Demands From Course Requests Of Previous Session</html:radio></td></tr>
				<tr><td><html:radio property="rollForwardStudentsMode" value="<%= SessionRollForward.StudentEnrollmentMode.POINT_IN_TIME_CLASS_ENROLLMENTS.name() %>"> Import Last-like Course Demands From a Point In Time Snapshot of Student Class Enrollments Of Previous Session</html:radio>
				<table style="margin-left: 50px;"><tr>
				    <tr>
				    	<td valign="top">Point In Time Data Snapshot To Use:
							<html:select property="pointInTimeSnapshotToRollCourseEnrollmentsForwardFrom">
								<html:optionsCollection property="fromPointInTimeDataSnapshots" value="uniqueId" label="name" />
							</html:select>
						</td>
					</tr>
				</table>
				</td></tr>
			</table>
			</td>		

			</td>		
		</tr>
		<tr>
			<td valign="middle" nowrap><html:checkbox name="<%=frmName%>" property="rollForwardCurricula"/> Roll Curricula Forward From Session: 
			<html:select style="width:200px;" property="sessionToRollCurriculaForwardFrom">
			<html:optionsCollection property="fromSessions" value="uniqueId" label="label" /></html:select>
			<br/><i>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;This will also roll academic areas, classifications, majors, minors, and projection rules forward (if these are not already present in the target academic session).</i>
			</td>
		</tr>
		<tr>
			<td valign="middle" nowrap><html:checkbox name="<%=frmName%>" property="rollForwardReservations" onclick="document.getElementById('reservationDetail').style.display = (this.checked ? 'table-row' : 'none');"/> Roll Reservations Forward From Session: 
			<html:select style="width:200px;" property="sessionToRollReservationsForwardFrom">
			<html:optionsCollection property="fromSessions" value="uniqueId" label="label" /></html:select>
			</td>
		</tr>
		<tr style="display:none;" id="reservationDetail">
			<td valign="middle">
			<table style="margin-left: 50px;"><tr>
			    <td valign="top" nowrap width="20%">For Subject Areas:</td>
			    <td><html:select size="<%=String.valueOf(Math.min(7,frm.getSubjectAreas().size()))%>" name="<%=frmName%>" styleClass="cmb" property="rollForwardReservationsSubjectIds" multiple="true">
					<html:optionsCollection property="subjectAreas" label="subjectAreaAbbreviation" value="uniqueId" />
				</html:select>
			    </td></tr>
			<tr><td valign="top" colspan="2">
				<html:checkbox name="<%=frmName%>" property="rollForwardCourseReservations" onclick="document.getElementById('courseReservationDetail').style.display = (this.checked ? 'table-row' : 'none');"/> Include Course Reservations
			</td></tr>
			<tr style="display:none;" id="courseReservationDetail"><td style="padding-left: 50px; vertical-align: middle;" valign="top" colspan="2">
				<span style="display: table;">
					<span style="display: table-row;">
						<span style="display: table-cell; vertical-align: middle;">New Expiration Date: </span>
						<tt:calendar property="expirationCourseReservations" outerStyle="display: table-cell;"/>
						<span style="display: table-cell; font-style: italic; padding-left: 20px; vertical-align: middle;">Applies to course reservations with an expiration date filled in.</span>
					</span>
				</span>
			</td></tr>
			<tr><td valign="top" colspan="2">
				<html:checkbox name="<%=frmName%>" property="rollForwardCurriculumReservations" onclick="document.getElementById('curriculumReservationDetail').style.display = (this.checked ? 'table-row' : 'none');"/> Include Curriculum Reservations
			<tr style="display:none;" id="curriculumReservationDetail"><td style="padding-left: 50px;" colspan="2">
				<span style="display: table;">
					<span style="display: table-row;">
						<span style="display: table-cell; vertical-align: middle;">New Expiration Date: </span>
						<tt:calendar property="expirationCurriculumReservations" outerStyle="display: table-cell;"/>
						<span style="display: table-cell; font-style: italic; padding-left: 20px; vertical-align: middle;">Applies to curriculum reservations with an expiration date filled in.</span>
					</span>
				</span>
			</td></tr>
			<tr><td valign="top" colspan="2">
				<html:checkbox name="<%=frmName%>" property="rollForwardGroupReservations" onclick="document.getElementById('groupReservationDetail').style.display = (this.checked ? 'table-row' : 'none');"/> Include Student Group Reservations
			<tr style="display:none;" id="groupReservationDetail"><td style="padding-left: 50px;" colspan="2">
				<div>
					<span style="display: table;">
						<span style="display: table-row;">
							<span style="display: table-cell; vertical-align: middle;">New Expiration Date: </span>
							<tt:calendar property="expirationGroupReservations" outerStyle="display: table-cell;"/>
							<span style="display: table-cell; font-style: italic; padding-left: 20px; vertical-align: middle;">Applies to student group reservations with an expiration date filled in.
						</span>
					</span>
				</div>
				<html:checkbox name="<%=frmName%>" property="createStudentGroupsIfNeeded"/> Create student groups that do not exist (with no students). Ignore group reservations that do not match otherwise.
			</td></tr>
			</table>
			</td>
		</tr>
		<logic:equal name="<%=frmName%>" property="rollForwardReservations" value="true">
			<script>document.getElementById('reservationDetail').style.display = 'table-row';</script>
		</logic:equal>
		<logic:equal name="<%=frmName%>" property="rollForwardCourseReservations" value="true">
			<script>document.getElementById('courseReservationDetail').style.display = 'table-row';</script>
		</logic:equal>
		<logic:equal name="<%=frmName%>" property="rollForwardCurriculumReservations" value="true">
			<script>document.getElementById('curriculumReservationDetail').style.display = 'table-row';</script>
		</logic:equal>
		<logic:equal name="<%=frmName%>" property="rollForwardGroupReservations" value="true">
			<script>document.getElementById('groupReservationDetail').style.display = 'table-row';</script>
		</logic:equal>
		<tr>
			<td class="WelcomeRowHead">
			&nbsp;
			</td>
		</tr>
		<tr>
			<td align="right">
					<html:submit property="op" accesskey="M" styleClass="btn" onclick="displayLoading();">
						<bean:message key="button.rollForward" />
					</html:submit>
			</TD>
		</TR>
		</TABLE>
		</html:form>
	<script language="javascript">hideLoading();</script>
	</body>
</html>