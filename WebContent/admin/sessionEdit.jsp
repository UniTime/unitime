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
<s:form action="sessionEdit">
<tt:confirm name="confirmDelete"><loc:message name="confirmDeleteAcademicSession"/></tt:confirm>
<SCRIPT type="text/javascript">
	<!--
		function trim(str) {
			return str.replace(/^\s*(\S*(\s+\S+)*)\s*$/, "$1");		
		}
		function doRefresh() {
			var ss = document.getElementsByName('form.sessionStart')[0].value;
			var se = document.getElementsByName('form.sessionEnd')[0].value;
			var ce = document.getElementsByName('form.classesEnd')[0].value;
			var es = document.getElementsByName('form.examStart')[0].value;
			var evs = document.getElementsByName('form.eventStart')[0].value;
			var eve = document.getElementsByName('form.eventEnd')[0].value;
			var year = document.getElementsByName('form.academicYear')[0].value;
			
			if (ss!=null && trim(ss)!=''
				 && se!=null && trim(se)!=''
				 && ce!=null && trim(ce)!=''
				 && es!=null && trim(es)!=''
				 && evs!=null && trim(evs)!=''
				 && eve!=null && trim(eve)!=''
				 && year!=null && trim(year)!='' && !isNaN(year)) {
				document.getElementById('refresh').value='true';
				var btn = document.getElementById('save');
				btn.click();
			}
		}
	// -->
</SCRIPT>
	<s:hidden name="refresh" value="false" id="refresh"/>
	<s:hidden name="form.sessionId"/>
	<table class="unitime-MainTable">
		<TR>
			<TD colspan="3">
				<tt:section-header>
					<tt:section-title>
						<s:if test="form.sessionId == null">
							<loc:message name="sectAddAcademicSession"/>
						</s:if><s:else>
							<loc:message name="sectEditAcademicSession"/>
						</s:else>
					</tt:section-title>
					<s:if test="form.sessionId == null">
						<s:submit name='op' value='%{#msg.actionSaveAcademicSession()}' id="save"
							accesskey="%{#msg.accessSaveAcademicSession()}" title="%{#msg.titleSaveAcademicSession(#msg.accessSaveAcademicSession())}"/>
					</s:if>
					<s:else>
						<s:submit name='op' value='%{#msg.actionUpdateAcademicSession()}' id="save"
							accesskey="%{#msg.accessUpdateAcademicSession()}" title="%{#msg.titleUpdateAcademicSession(#msg.accessUpdateAcademicSession())}"/>
						<sec:authorize access="hasPermission(#form.sessionId, 'Session', 'AcademicSessionDelete')">
							<s:submit name='op' value='%{#msg.actionDeleteAcademicSession()}'
								accesskey="%{#msg.accessDeleteAcademicSession()}" title="%{#msg.titleDeleteAcademicSession(#msg.accessDeleteAcademicSession())}"
								onclick="return confirmDelete();"/>
						</sec:authorize>
					</s:else>
					<s:submit name='op' value='%{#msg.actionBackToAcademicSessions()}'
						accesskey="%{#msg.accessBackToAcademicSessions()}" title="%{#msg.titleBackToAcademicSessions(#msg.accessBackToAcademicSessions())}"/>
				</tt:section-header>			
			</TD>
		</TR>

		<s:if test="!fieldErrors.isEmpty()">
			<TR><TD colspan="3" align="left" class="errorTable">
				<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror/>
			</TD></TR>
		</s:if>

		<TR>
			<TD><loc:message name="columnAcademicInitiative"/>:</TD>
			<TD colspan='2'>
				<s:textfield name="form.academicInitiative" maxlength="20" size="20"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="columnAcademicTerm"/>:</TD>
			<TD colspan='2'>
				<s:textfield name="form.academicTerm" maxlength="20" size="20"/>
			</TD>
		</TR>
		
		<TR>
			<TD><loc:message name="columnAcademicYear"/>:</TD>
			<TD colspan='2'>
				<s:textfield name="form.academicYear" onchange="doRefresh();" maxlength="4" size="4"/>
			</TD>
		</TR>
		
		<s:if test="form.sessionId != null">
		<TR>
			<TD><loc:message name="columnDefaultDatePattern"/>:</TD>
			<TD colspan='2'>
				<s:if test="#request.datePatternList == null || #request.datePatternList.isEmpty()">
					<loc:message name="infoNoDatePatternsAvailable"/>					
					<s:hidden name="form.defaultDatePatternId"/>
				</s:if>
				<s:else>
					<s:select name="form.defaultDatePatternId" list="#request.datePatternList" listKey="id" listValue="value"/>
				</s:else>
			</TD>
		</TR>
		</s:if>
		
		<TR>
			<TD><loc:message name="columnSessionStartDate"/>:</TD>
			<TD colspan='2'>
				<tt:calendar name="form.sessionStart" cssStyle="border: #660000 2px solid;" onchange="$wnd.doRefresh();"/>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="columnClassesEndDate"/>:</TD>
			<TD colspan='2'>
				<tt:calendar name="form.classesEnd" cssStyle="border: #660000 2px solid;" onchange="$wnd.doRefresh();"/>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="columnExamStartDate"/>:</TD>
			<TD colspan='2'>
				<tt:calendar name="form.examStart" cssStyle="border: #999933 2px solid;" onchange="$wnd.doRefresh();"/>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="columnSessionEndDate"/>:</TD>
			<TD colspan='2'>
				<tt:calendar name="form.sessionEnd" cssStyle="border: #333399 2px solid;" onchange="$wnd.doRefresh();"/>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="columnEventStartDate"/>:</TD>
			<TD colspan='2'>
				<tt:calendar name="form.eventStart" cssStyle="border: 2px solid yellow;" onchange="$wnd.doRefresh();"/>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="columnEventEndDate"/>:</TD>
			<TD colspan='2'>
				<tt:calendar name="form.eventEnd" cssStyle="border: 2px solid red;" onchange="$wnd.doRefresh();"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="columnSessionStatus"/>:</TD>
			<TD colspan='2'>
				<s:select name="form.status"
					list="form.statusOptions" listKey="reference" listValue="label"
					headerKey="" headerValue="%{#msg.itemSelect()}"/>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="columnDefaultClassDuration"/>:</TD>
			<TD colspan="2">
				<s:select name="form.durationType"
					list="form.durationTypes" listKey="id" listValue="value"
					headerKey="-1" headerValue="%{#msg.itemDefaultClassDuration()}"/>
			</TD>
		</TR>

		<s:if test="form.instructionalMethods != null && !form.instructionalMethods.isEmpty()">
			<TR>
				<TD><loc:message name="columnDefailtInstructionalMethod"/>:</TD>
				<TD colspan="2">
					<s:select name="form.instructionalMethod"
					list="form.instructionalMethods" listKey="id" listValue="value"
					headerKey="-1" headerValue="%{#msg.itemNoDefault()}"/>
				</TD>
			</TR>
		</s:if><s:else>
			<s:hidden name="form.instructionalMethod"/>
		</s:else>

		<s:if test="#request.holidays != null">
			<TR>
				<TD><loc:message name="columnHolidays"/>:</TD><TD colspan='2'></TD>
			</TR><TR>
				<TD colspan='3'><s:property value="#request.holidays" escapeHtml="false"/></TD>
			</TR>
		</s:if>

		<TR>
			<TD colspan='3'>
				<tt:section-title><br><loc:message name="sectOnlineStudentSchedulingDefaultSettings"/></tt:section-title>
			</TD>
		</TR>
		
		<TR>
			<TD valign="top"><loc:message name="propNewEnrollmentDeadline"/></TD>
			<TD valign="top">
				<s:textfield name="form.wkEnroll" maxlength="4" size="4"/>
			</TD><TD style="white-space: pre-wrap; font-style: italic;"><loc:message name="descNewEnrollmentDeadline"/></TD>
		</TR>
		<TR>
			<TD valign="top"><loc:message name="propClassChangesDeadline"/></TD>
			<TD valign="top">
				<s:textfield name="form.wkChange" maxlength="4" size="4"/>
			</TD><TD style="white-space: pre-wrap; font-style: italic;"><loc:message name="descClassChangesDeadline"/></TD>
		</TR>
		<TR>
			<TD valign="top"><loc:message name="propCourseDropDeadline"/></TD>
			<TD valign="top">
				<s:textfield name="form.wkDrop" maxlength="4" size="4"/>
			</TD><TD style="white-space: pre-wrap; font-style: italic;"><loc:message name="descCourseDropDeadline"/></TD>
		</TR>
		<TR>
			<TD><loc:message name="propDefaultStudentStatus"/></TD>
			<TD colspan="2">
				<s:select name="form.sectStatus"
					list="form.sectStates" listKey="id" listValue="value"
					headerKey="-1" headerValue="%{#msg.itemDefaultStudentStatus()}"/>
			</TD>
		</TR>
		
		<TR>
			<TD colspan="3">
			<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
			</TD>
		</TR>
		
		<TR>
			<TD colspan="3" align="right">
					<s:if test="form.sessionId == null">
						<s:submit name='op' value='%{#msg.actionSaveAcademicSession()}' id="save"
							accesskey="%{#msg.accessSaveAcademicSession()}" title="%{#msg.titleSaveAcademicSession(#msg.accessSaveAcademicSession())}"/>
					</s:if>
					<s:else>
						<s:submit name='op' value='%{#msg.actionUpdateAcademicSession()}'
							accesskey="%{#msg.accessUpdateAcademicSession()}" title="%{#msg.titleUpdateAcademicSession(#msg.accessUpdateAcademicSession())}"/>
						<sec:authorize access="hasPermission(#form.sessionId, 'Session', 'AcademicSessionDelete')">
							<s:submit name='op' value='%{#msg.actionDeleteAcademicSession()}'
								accesskey="%{#msg.accessDeleteAcademicSession()}" title="%{#msg.titleDeleteAcademicSession(#msg.accessDeleteAcademicSession())}"
								onclick="return confirmDelete();"/>
						</sec:authorize>
					</s:else>
					<s:submit name='op' value='%{#msg.actionBackToAcademicSessions()}'
						accesskey="%{#msg.accessBackToAcademicSessions()}" title="%{#msg.titleBackToAcademicSessions(#msg.accessBackToAcademicSessions())}"/>
			</TD>
		</TR>
	</table>
</s:form>
</loc:bundle>