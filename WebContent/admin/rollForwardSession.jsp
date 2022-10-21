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
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="tt" uri="http://www.unitime.org/tags-custom" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="loc" uri="http://www.unitime.org/tags-localization" %>
<loc:bundle name="CourseMessages"><s:set var="msg" value="#attr.MSG"/>
<s:form action="rollForwardSession">
<table class="unitime-MainTable">
	<s:if test="!fieldErrors.isEmpty()">
		<TR><TD align="left" class="errorTable">
			<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror escape="false"/>
		</TD></TR>
	</s:if>
		
		
	<s:if test="#request.table != null">
		<TR><TD>
			<tt:section-header>
				<tt:section-title><loc:message name="sectRollForwardsInProgress"/></tt:section-title>
			</tt:section-header>
		</TD></TR>
		<TR><TD>
			<table class='unitime-Table' style="width:100%;">
				<s:property value="#request.table" escapeHtml="false"/>
			</table>
		</TD></TR>
		<TR><TD>&nbsp;</TD></TR>
	</s:if>
	
	<s:hidden name="log" value="%{#request.logid}" id="log"/>
	<s:if test="#request.log != null">
		<TR>
			<TD>
				<tt:section-header>
					<tt:section-title>
						<loc:message name="sectionRollForwardLog"><s:property value="#request.logname"/></loc:message>
					</tt:section-title>
					<s:submit name="op" value="%{#msg.actionRefreshLog()}"
						accesskey="%{#msg.accessRefreshLog()}" title="%{#msg.titleRefreshLog(#msg.accessRefreshLog())}"/>
				</tt:section-header>
			</TD>
		</TR>
		<TR>
  			<TD>
  				<blockquote>
  					<s:property value="#request.log" escapeHtml="false"/>
  				</blockquote>
  			</TD>
		</TR>
	</s:if>
	
	<TR><TD>
		<tt:section-header>
			<tt:section-title><loc:message name="sectRollForwardActions"/></tt:section-title>
				<s:submit name="op" value="%{#msg.actionRollForward()}"
						accesskey="%{#msg.accessRollForward()}" title="%{#msg.titleRollForward(#msg.accessRollForward())}"/>
		</tt:section-header>
	</TD></TR>
		<tr>
			<td valign="middle" nowrap ><b><loc:message name="propSessionToRollForwardTo"/></b>
			<s:select name="form.sessionToRollForwardTo" style="min-width:200px;" onchange="document.getElementById('log').value = '';submit();"
				list="form.toSessions" listKey="uniqueId" listValue="label"/>
		</tr>
		<tr><td>&nbsp;</td></tr>
		<tr>
			<td valign="middle" nowrap ><s:checkbox name="form.rollForwardDepartments"/>
			<loc:message name="propRollDepartmentsForwardFromSession"/>
			<s:select name="form.sessionToRollDeptsFowardFrom" style="min-width:200px;"
				list="form.fromSessions" listKey="uniqueId" listValue="label"/>
			</td>			
		</tr>
		<tr>
			<td valign="middle" nowrap><s:checkbox name="form.rollForwardSessionConfig" onclick="document.getElementById('configNote').style.display = (this.checked ? 'table-row' : 'none');"/>
			<loc:message name="propRollSessionConfigFromSession"/>
			<s:select name="form.sessionToRollSessionConfigForwardFrom" style="min-width:200px;"
				list="form.fromSessions" listKey="uniqueId" listValue="label"/>
		</tr>
		<tr style="display:none;" id="configNote">
			<td valign="middle" style="white-space: wrap; max-width: 600px; padding-left: 20px;">
				<i><loc:message name="infoRollSessionConfigFromSession"/></i>
			</td>
		</tr>
		<s:if test="form.rollForwardSessionConfig == true">
			<script>document.getElementById('configNote').style.display = 'table-row';</script>
		</s:if>
		<tr>
			<td valign="middle" nowrap ><s:checkbox name="form.rollForwardManagers"/>
			<loc:message name="propRollManagersFromSession"/>
			<s:select name="form.sessionToRollManagersForwardFrom" style="min-width:200px;"
				list="form.fromSessions" listKey="uniqueId" listValue="label"/>
			</td>			
		</tr>
		<tr>
			<td valign="middle" nowrap ><s:checkbox name="form.rollForwardRoomData"/>
			<loc:message name="propRollRoomsFromSession"/>
			<s:select name="form.sessionToRollRoomDataForwardFrom" style="min-width:200px;"
				list="form.fromSessions" listKey="uniqueId" listValue="label"/>
			</td>			
		</tr>
		<tr>
			<td valign="middle" nowrap ><s:checkbox name="form.rollForwardDatePatterns"/>
			<loc:message name="propRollDatePatternsFromSession"/>
			<s:select name="form.sessionToRollDatePatternsForwardFrom" style="min-width:200px;"
				list="form.fromSessions" listKey="uniqueId" listValue="label"/>
			</td>			
		</tr>
		<tr>
			<td valign="middle" nowrap ><s:checkbox name="form.rollForwardTimePatterns"/>
			<loc:message name="propRollTimePatternsFromSession"/>
			<s:select name="form.sessionToRollTimePatternsForwardFrom" style="min-width:200px;"
				list="form.fromSessions" listKey="uniqueId" listValue="label"/>
			</td>			
		</tr>
		<tr>
			<td valign="middle" nowrap ><s:checkbox name="form.rollForwardLearningManagementSystems"/>
			<loc:message name="propRollLMSFromSession"/>
			<s:select name="form.sessionToRollLearningManagementSystemsForwardFrom" style="min-width:200px;"
				list="form.fromSessions" listKey="uniqueId" listValue="label"/>
			</td>			
		</tr>
		<tr>
			<td valign="middle" nowrap><s:checkbox name="form.rollForwardSubjectAreas"/>
			<loc:message name="propRollSubjectsFromSession"/>
			<s:select name="form.sessionToRollSubjectAreasForwardFrom" style="min-width:200px;"
				list="form.fromSessions" listKey="uniqueId" listValue="label"/>
			</td>			
		</tr>
		<tr>
			<td valign="middle" nowrap><s:checkbox name="form.rollForwardInstructorData"/>
			<loc:message name="propRollInstructorsFromSession"/>
			<s:select name="form.sessionToRollInstructorDataForwardFrom" style="min-width:200px;"
				list="form.fromSessions" listKey="uniqueId" listValue="label"/>
			</td>
		</tr>
		<tr>
			<td valign="middle">
			<table style="margin-left: 50px;"><tr>
			    <td valign="top"><loc:message name="propForDepartments"/></td>
			    <td>
				<s:select name="form.rollForwardDepartmentIds" style="min-width:200px;" multiple="true" size="%{form.departmentsListSize}"
					list="form.departments" listKey="uniqueId" listValue="label"/>
			</table></td>
		</tr>
		<tr>
			<td valign="middle" nowrap><s:checkbox name="form.rollForwardCourseOfferings"/>
			<loc:message name="propRollCoursesFormSession"/> 
			<s:select name="form.sessionToRollCourseOfferingsForwardFrom" style="min-width:200px;"
				list="form.fromSessions" listKey="uniqueId" listValue="label"/>
			</td>			
		</tr>
		<tr>
			<td valign="middle">
			<table style="margin-left: 50px;"><tr>
			    <td valign="top"><loc:message name="propForSubjectAreas"/></td>
			    <td>
			    <s:select name="form.rollForwardSubjectAreaIds" style="min-width:200px;" multiple="true" size="%{form.subjectAreasListSize}"
			    	list="form.subjectAreas" listKey="uniqueId" listValue="label"/>
			    </td></tr>
			<tr><td valign="top" colspan="2">
				<s:checkbox name="form.rollForwardWaitListsProhibitedOverrides"/>
				<loc:message name="checkIncludeWaitListAndOverrides"/>
			</td></tr>
			<tr><td style="padding-top: 20px;" rowspan="2" valign="top"><loc:message name="propSubpartLevelTimePrefs"/></td><td style="padding-top: 20px;">
				<s:radio name="form.subpartTimePrefsAction" list="#{'rollUnchanged':''}"/> <loc:message name="optRollSubpartTimePrefs"/></td></tr>
			<tr><td>
			<s:radio name="form.subpartTimePrefsAction" list="#{'doNotRoll':''}"/> <loc:message name="optNotRollSubpartTimePrefs"/></td></tr>
			<tr><td style="padding-top: 20px;" rowspan="2" valign="top"><loc:message name="propSubpartLevelRoomPrefs"/></td>
				<td style="padding-top: 20px;"><s:radio name="form.subpartLocationPrefsAction" list="#{'rollUnchanged':''}"/> <loc:message name="optRollSubpartRoomPrefs"/></td></tr>
			<tr><td><s:radio name="form.subpartLocationPrefsAction" list="#{'doNotRoll':''}"/> <loc:message name="optNotRollSubpartRoomPrefs"/></td></tr>
			<tr><td style="padding-top: 20px;" rowspan="2" valign="top"><loc:message name="propClassLevelPrefs"/></td><td style="padding-top: 20px;"><s:radio name="form.classPrefsAction" list="#{'doNotRoll':''}"/> <loc:message name="optNoRollClassPrefs"/></td></tr>
			<tr><td><s:radio name="form.classPrefsAction" list="#{'pushUp':''}"/> <loc:message name="optPushClassPrefsUp"/></td></tr>
			<tt:propertyEquals name="unitime.rollforward.allowClassPrefs" value="true">
				<tr><td></td><td><s:radio name="form.classPrefsAction" list="#{'rollUnchanged':''}"/> <loc:message name="optRollClassPrefs"/></td></tr>
			</tt:propertyEquals>
			<tr><td style="padding-top: 20px;" rowspan="4" valign="top"><loc:message name="propDistributionPrefs"/></td>
				<td style="padding-top: 20px;"><s:radio name="form.rollForwardDistributions" list="#{'ALL':''}"/> <loc:message name="optRollDistPrefsAll"/></td></tr>
			<tr><td><s:radio name="form.rollForwardDistributions" list="#{'MIXED':''}"/> <loc:message name="optRollDistPrefsMixed"/></td></tr>
			<tr><td><s:radio name="form.rollForwardDistributions" list="#{'SUBPART':''}"/> <loc:message name="optRollDistPrefsSubparts"/></td></tr>
			<tr><td><s:radio name="form.rollForwardDistributions" list="#{'NONE':''}"/> <loc:message name="optRollDistPrefsNone"/></td></tr>
			<tr><td style="padding-top: 20px;" rowspan="3" valign="top"><loc:message name="propCancelledClasses"/></td>
				<td style="padding-top: 20px;"><s:radio name="form.cancelledClassAction" list="#{'KEEP':''}"/> <loc:message name="optCancelledClassesKeep"/></td></tr>
			<tr><td><s:radio name="form.cancelledClassAction" list="#{'REOPEN':''}"/> <loc:message name="optCancelledClassesReopen"/></td></tr>
			<tr><td><s:radio name="form.cancelledClassAction" list="#{'SKIP':''}"/> <loc:message name="optCancelledClassesSkip"/></td></tr>
			</table></td>
		</tr>
		<tr>
			<td valign="middle">
			<table><tr>	<td valign="middle" nowrap><s:checkbox name="form.rollForwardClassInstructors"/> <loc:message name="propRollClassInstructorsForSubjects"/> 
				</td><td>
				<s:select name="form.rollForwardClassInstrSubjectIds" style="min-width:200px;" multiple="true" size="%{form.subjectAreasListSize}"
			    	list="form.subjectAreas" listKey="uniqueId" listValue="label"/>
			</td></tr></table>
			</td>
		</tr>
		<tr>
			<td valign="middle">
			<table><tr>	<td valign="middle" nowrap><s:checkbox name="form.rollForwardOfferingCoordinators"/> <loc:message name="propRollOfferingCoordinatorsForSubjects"/> 
				</td><td>
				<s:select name="form.rollForwardOfferingCoordinatorsSubjectIds" style="min-width:200px;" multiple="true" size="%{form.subjectAreasListSize}"
			    	list="form.subjectAreas" listKey="uniqueId" listValue="label"/>
			</td></tr></table>
			</td>
		</tr>
		<tr>
			<td valign="middle">
			<table><tr>	<td valign="middle" nowrap><s:checkbox name="form.rollForwardTeachingRequests"/> <loc:message name="propRollTeachingRequestsForSubjects"/> 
				</td><td>
				<s:select name="form.rollForwardTeachingRequestsSubjectIds" style="min-width:200px;" multiple="true" size="%{form.subjectAreasListSize}"
			    	list="form.subjectAreas" listKey="uniqueId" listValue="label"/>
			</td></tr></table>
			</td>
		</tr>
		
		<tr>
			<td valign="middle">
			<table>
				<tr>
					<td valign="middle" nowrap><s:checkbox name="form.addNewCourseOfferings"/> <loc:message name="propAddNewCoursesForSubjects"/>
					<div style="margin-left: 20px; white-space: pre;"><i><loc:message name="infoAddNewCoursesForSubjects"/></i></div>
				</td><td>
				<s:select name="form.addNewCourseOfferingsSubjectIds" style="min-width:200px;" multiple="true" size="%{form.subjectAreasListSize}"
			    	list="form.subjectAreas" listKey="uniqueId" listValue="label"/>
			</td></tr></table>
			</td>
		</tr>
		<tr>
			<td valign="middle" nowrap><s:checkbox name="form.rollForwardExamConfiguration"/> <loc:message name="propRollExamConfigFromSession"/>
			<s:select name="form.sessionToRollExamConfigurationForwardFrom" style="min-width:200px;"
				list="form.fromSessions" listKey="uniqueId" listValue="label"/>
			</td>			
		</tr>
		<tr>
			<td valign="middle" nowrap><s:checkbox name="form.rollForwardMidtermExams"/> <loc:message name="propRollMidtermExams"/>
			<table style="margin-left: 50px;"><tr>
			    <tr><td rowspan="3" valign="top"><loc:message name="propPreferences"/></td>
			    <td><s:radio name="form.midtermExamsPrefsAction" list="#{'rollAllPrefs':''}"/><loc:message name="prefMidtermExamsAll"/></td></tr>
			    <tr><td><s:radio name="form.midtermExamsPrefsAction" list="#{'rollRoomPrefs':''}"/><loc:message name="prefMidtermExamsRoom"/></td></tr>
			    <tr><td><s:radio name="form.midtermExamsPrefsAction" list="#{'doNotRoll':''}"/><loc:message name="prefMidtermExamsNone"/></td></tr>
			</table>
			</td>	
		</tr>
		<tr>
			<td valign="middle" nowrap><s:checkbox name="form.rollForwardFinalExams"/> <loc:message name="propRollFinalExams"/>
			<table style="margin-left: 50px;"><tr>
			    <tr><td rowspan="3" valign="top"><loc:message name="propPreferences"/></td>
			    <td><s:radio name="form.finalExamsPrefsAction" list="#{'rollAllPrefs':''}"/><loc:message name="prefFinalExamsAll"/></td></tr>
			    <tr><td><s:radio name="form.finalExamsPrefsAction" list="#{'rollRoomPrefs':''}"/><loc:message name="prefFinalExamsRoom"/></td></tr>
			    <tr><td><s:radio name="form.finalExamsPrefsAction" list="#{'doNotRoll':''}"/><loc:message name="prefFinalExamsNone"/></td></tr>
			</table>
			</td>		
		</tr>
		<tr>
			<td valign="middle" nowrap><s:checkbox name="form.rollForwardStudents"/> <loc:message name="propImportLastLikes"/>
			<table style="margin-left: 50px;">
				<tr><td><s:radio name="form.rollForwardStudentsMode" list="#{'LAST_LIKE':''}"/> <loc:message name="optLastLikeCopy"/></td></tr>
				<tr><td><s:radio name="form.rollForwardStudentsMode" list="#{'STUDENT_CLASS_ENROLLMENTS':''}"/> <loc:message name="optLastLikeEnrls"/></td></tr>
				<tr><td><s:radio name="form.rollForwardStudentsMode" list="#{'STUDENT_COURSE_REQUESTS':''}"/> <loc:message name="optLastLikeCourseReqs"/></td></tr>
				<tr><td><s:radio name="form.rollForwardStudentsMode" list="#{'POINT_IN_TIME_CLASS_ENROLLMENTS':''}"/> <loc:message name="optLastLikePIT"/>
				<table style="margin-left: 50px;"><tr>
				    <tr>
				    	<td valign="top"><loc:message name="propPointInTimeSnapshot"/>
							<s:select name="form.pointInTimeSnapshotToRollCourseEnrollmentsForwardFrom" style="min-width:200px;"
								list="form.fromPointInTimeDataSnapshots" listKey="uniqueId" listValue="name"/>
						</td>
					</tr>
				</table>
				</td></tr>
			</table>
			</td>		
		</tr>
		<tr>
			<td valign="middle" nowrap><s:checkbox name="form.rollForwardCurricula"/> <loc:message name="propRollCurriculaFromSession"/> 
			<s:select name="form.sessionToRollCurriculaForwardFrom" style="min-width:200px;"
				list="form.fromSessions" listKey="uniqueId" listValue="label"/>
			<br/><i>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<loc:message name="infoRollCurriculaFromSession"/></i>
			</td>
		</tr>
		<tr>
			<td valign="middle" nowrap><s:checkbox name="form.rollForwardReservations" onclick="document.getElementById('reservationDetail').style.display = (this.checked ? 'table-row' : 'none');"/> <loc:message name="propRollReservationsFromSession"/> 
			<s:select name="form.sessionToRollReservationsForwardFrom" style="min-width:200px;"
				list="form.fromSessions" listKey="uniqueId" listValue="label"/>
			</td>
		</tr>
		<tr style="display:none;" id="reservationDetail">
			<td valign="middle">
			<table style="margin-left: 50px;"><tr>
			    <td valign="top" nowrap width="20%"><loc:message name="propForSubjectAreas"/></td>
			    <td>
				<s:select name="form.rollForwardReservationsSubjectIds" style="min-width:200px;" multiple="true" size="%{form.subjectAreasListSize}"
			    	list="form.subjectAreas" listKey="uniqueId" listValue="label"/>
			    </td></tr>
			<tr><td valign="top" colspan="2">
				<s:checkbox name="form.rollForwardCourseReservations" onclick="document.getElementById('courseReservationDetail').style.display = (this.checked ? 'table-row' : 'none');"/> <loc:message name="optIncludeCourseReservations"/>
			</td></tr>
			<tr style="display:none;" id="courseReservationDetail"><td style="padding-left: 50px; vertical-align: middle;" valign="top" colspan="2">
				<span style="display: table;">
					<span style="display: table-row;">
						<span style="display: table-cell; vertical-align: middle;"><loc:message name="propNewStartDate"/> </span>
						<tt:calendar name="form.startDateCourseReservations" outerStyle="display: table-cell;"/>
						<span style="display: table-cell; font-style: italic; padding-left: 20px; vertical-align: middle;"><loc:message name="infoNewStartDateCourse"/></span>
					</span>
					<span style="display: table-row;">
						<span style="display: table-cell; vertical-align: middle;"><loc:message name="propNewExpirationDate"/> </span>
						<tt:calendar name="form.expirationCourseReservations" outerStyle="display: table-cell;"/>
						<span style="display: table-cell; font-style: italic; padding-left: 20px; vertical-align: middle;"><loc:message name="infoNewExpirationDateCourse"/></span>
					</span>
				</span>
			</td></tr>
			<tr><td valign="top" colspan="2">
				<s:checkbox name="form.rollForwardCurriculumReservations" onclick="document.getElementById('curriculumReservationDetail').style.display = (this.checked ? 'table-row' : 'none');"/> <loc:message name="optIncludeCurriculumReservations"/>
			<tr style="display:none;" id="curriculumReservationDetail"><td style="padding-left: 50px;" colspan="2">
				<span style="display: table;">
					<span style="display: table-row;">
						<span style="display: table-cell; vertical-align: middle;"><loc:message name="propNewStartDate"/> </span>
						<tt:calendar name="form.startDateCurriculumReservations" outerStyle="display: table-cell;"/>
						<span style="display: table-cell; font-style: italic; padding-left: 20px; vertical-align: middle;"><loc:message name="infoNewStartDateCurriculum"/></span>
					</span>
					<span style="display: table-row;">
						<span style="display: table-cell; vertical-align: middle;"><loc:message name="propNewExpirationDate"/> </span>
						<tt:calendar name="form.expirationCurriculumReservations" outerStyle="display: table-cell;"/>
						<span style="display: table-cell; font-style: italic; padding-left: 20px; vertical-align: middle;"><loc:message name="infoNewExpirationDateCurriculum"/></span>
					</span>
				</span>
			</td></tr>
			<tr><td valign="top" colspan="2">
				<s:checkbox name="form.rollForwardGroupReservations" onclick="document.getElementById('groupReservationDetail').style.display = (this.checked ? 'table-row' : 'none');"/> <loc:message name="optIncludeStudentGroupReservations"/>
			<tr style="display:none;" id="groupReservationDetail"><td style="padding-left: 50px;" colspan="2">
				<div>
					<span style="display: table;">
						<span style="display: table-row;">
							<span style="display: table-cell; vertical-align: middle;"><loc:message name="propNewStartDate"/> </span>
							<tt:calendar name="form.startDateGroupReservations" outerStyle="display: table-cell;"/>
							<span style="display: table-cell; font-style: italic; padding-left: 20px; vertical-align: middle;"><loc:message name="infoNewStartDateGroup"/></span>
						</span>
						<span style="display: table-row;">
							<span style="display: table-cell; vertical-align: middle;"><loc:message name="propNewExpirationDate"/> </span>
							<tt:calendar name="form.expirationGroupReservations" outerStyle="display: table-cell;"/>
							<span style="display: table-cell; font-style: italic; padding-left: 20px; vertical-align: middle;"><loc:message name="infoNewExpirationDateGroup"/></span>
						</span>
					</span>
				</div>
				<s:checkbox name="form.createStudentGroupsIfNeeded"/> <loc:message name="optCreateStudentGroupsForReservations"/>
			</td></tr>
			</table>
			</td>
		</tr>
		<s:if test="form.rollForwardReservations == true">
			<script>document.getElementById('reservationDetail').style.display = 'table-row';</script>
		</s:if>
		<s:if test="form.rollForwardCourseReservations == true">
			<script>document.getElementById('courseReservationDetail').style.display = 'table-row';</script>
		</s:if>
		<s:if test="form.rollForwardCurriculumReservations == true">
			<script>document.getElementById('curriculumReservationDetail').style.display = 'table-row';</script>
		</s:if>
		<s:if test="form.rollForwardGroupReservations == true">
			<script>document.getElementById('groupReservationDetail').style.display = 'table-row';</script>
		</s:if>
		<tr>
			<td valign="middle" nowrap ><s:checkbox name="form.rollForwardPeriodicTasks"/> <loc:message name="propRollScheduledTasksFromSession"/>
			<s:select name="form.sessionToRollPeriodicTasksFrom" style="min-width:200px;"
				list="form.fromSessions" listKey="uniqueId" listValue="label"/> 
			</td>
		</tr>
		<tr>
			<td class="WelcomeRowHead">
			&nbsp;
			</td>
		</tr>
		<tr>
			<td align="right" colspan="2">
				<s:submit name="op" value="%{#msg.actionRollForward()}"
					accesskey="%{#msg.accessRollForward()}" title="%{#msg.titleRollForward(#msg.accessRollForward())}"/>
			</TD>
		</TR>
	</table>
</s:form>
</loc:bundle>