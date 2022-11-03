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
<script type="text/javascript" src="scripts/block.js"></script>
<loc:bundle name="ExaminationMessages"><s:set var="msg" value="#attr.MSG"/> 
<s:form action="examPdfReport">
	<table class="unitime-MainTable">
	<s:if test="!fieldErrors.isEmpty()">
		<TR>
			<TD colspan='2'>
				<tt:section-header>
					<tt:section-title><loc:message name="sectErrors"/></tt:section-title>
					<s:submit accesskey='%{#msg.accessGenerateReport()}' name='op' value='%{#msg.actionGenerateReport()}' title='%{#msg.titleGenerateReport()}'/>
				</tt:section-header>
			</TD>
		</TR>
		<TR>
			<TD colspan="2" align="left" class="errorTable">
				<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror/>
			</TD>
		</TR>
		<TR><TD>&nbsp;</TD></TR>
	</s:if>
	<s:if test="#request.table != null">
		<TR><TD colspan="2">
			<table style="width:100%;">
				<s:property value="#request.table" escapeHtml="false"/>
			</table>
		</TD></TR>
		<TR><TD colspan='2'>&nbsp;</TD></TR>
	</s:if>
	<s:if test="#request.log != null">
		<TR>
			<TD colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<loc:message name="sectLogOfTask"><s:property value="#request.logname"/></loc:message>
					</tt:section-title>
					<s:hidden name="log" value="#request.logid"/>
					<s:submit accesskey='%{#msg.accessRegreshLog()}' name='op' value='%{#msg.actionRegreshLog()}' title='%{#msg.titleRegreshLog()}'/>
				</tt:section-header>
			</TD>
		</TR>
		<TR>
  			<TD colspan='2'>
  				<blockquote>
  					<s:property value="#request.log" escapeHtml="false"/>
  				</blockquote>
  			</TD>
		</TR>
	</s:if>
	<TR>
		<TD colspan='2'>
			<tt:section-header>
				<tt:section-title><loc:message name="sectInputData"/></tt:section-title>
				<s:if test="fieldErrors.isEmpty()">
					<s:submit accesskey='%{#msg.accessGenerateReport()}' name='op' value='%{#msg.actionGenerateReport()}' title='%{#msg.titleGenerateReport()}'/>
				</s:if>
			</tt:section-header>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap><loc:message name="filterExaminationProblem"/></TD>
		<TD>
			<s:select name="form.examType" 
				list="#request.examTypes" listKey="uniqueId" listValue="label"/>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap valign='top'><loc:message name="filterSubjectAreas"/></TD>
		<TD>
			<s:checkbox name="form.all" onclick="selectionChanged();"/><loc:message name="checkReportAllSubjectAreas"/><br>
			<s:select name="form.subjects" list="form.subjectAreas" listKey="id" listValue="value" multiple="true"/>
		</TD>
	</TR>
	<TR>
		<TD colspan='2'>
			<tt:section-title><br><loc:message name="sectReport"/></tt:section-title>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap valign='top'><loc:message name="filterReport"/></TD>
		<TD>
			<s:iterator value="form.allReports" var="report">
				<s:checkboxlist name="form.reports" list="#{#report.value:''}" onchange="selectionChanged();"/>
				<s:property value="#report.label"/>
				<br>
			</s:iterator>
		</TD>
	</TR>
	<TR>
		<TD colspan='2'>
			<tt:section-title><br><loc:message name="sectParameters"/></tt:section-title>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap valign='top'><loc:message name="propAllReports"/></TD>
		<TD><s:checkbox name="form.itype"/><loc:message name="checkDisplayInstructionalType"/><br>
			<s:checkbox name="form.ignoreEmptyExams"/><loc:message name="checkSkipExamsWithNoEnrollment"/><br>
			<s:checkbox name="form.roomDispNames"/><loc:message name="checkUseRoomDisplayNames"/></TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap valign='top'><loc:message name="propConflictReports"/></TD>
		<TD>
			<s:checkbox name="form.direct"/><loc:message name="checkDisplayDirectConflicts"/><br>
			<s:checkbox name="form.m2d"/><loc:message name="checkDisplayMoreThan2ExamsADayConflicts"/><br>
			<s:checkbox name="form.btb"/><loc:message name="checkDisplayBackToBackConflicts"/>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap valign='top' rowspan='2'><loc:message name="propReportsWithRooms"/></TD>
		<TD><s:checkbox name="form.dispRooms"/><loc:message name="checkDisplayRooms"/></TD>
	</TR>
	<TR>
		<TD><loc:message name="propNoRoomLabel"/> <s:textfield name="form.noRoom" size="11" maxlength="11"/></TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap valign='top' rowspan='4'><loc:message name="propPeriodChartReport"/></TD>
		<TD><s:checkbox name="form.totals"/><loc:message name="checkDisplayTotals"/></TD>
	</TR>
	<TR>
		<TD><loc:message name="propExamLimit"/> <s:textfield name="form.limit" size="4" maxlength="4"/></TD>
	</TR>
	<TR>
		<TD><loc:message name="propRoomCodes"/> <s:textfield name="form.roomCodes" size="70" maxlength="200"/></TD>
	</TR>
	<TR>
		<TD><s:checkbox name="form.compact"/><loc:message name="checkReportCompactSize"/></TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap valign='top' rowspan="2"><loc:message name="propVerificationReport"/></TD>
		<TD><s:checkbox name="form.dispLimit"/><loc:message name="checkDisplayLimitsAndEnrollments"/></TD>
	</TR>
	<TR>
		<TD><s:checkbox name="form.dispNote"/><loc:message name="checkDisplayClassScheduleNotes"/></TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap valign='top' rowspan='2'><loc:message name="propIndividualReports"/></TD>
  		<TD><s:checkbox name="form.classSchedule"/><loc:message name="checkIncludeClassSchedule"/></TD>
  	</TR>
  	<TR>
		<TD>
			<span style="display: table;">
				<span style="display: table-row;">
					<span style="display: table-cell; vertical-align: middle; padding-right: 5px;"><loc:message name="propReportStartDate"/> </span>
					<tt:calendar name="form.since" outerStyle="display: table-cell;"/>
					<span style="display: table-cell; font-style: italic; padding-left: 5px; vertical-align: middle;"><loc:message name="hintReportStartDate"/></span>
				</span>
			</span>
		</TD>
	</TR>
	<TR>
		<TD colspan='2' valign='top'>
			<tt:section-title><br><loc:message name="sectOutput"/></tt:section-title>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap><loc:message name="propReportFormat"/></TD>
		<TD>
			<s:select name="form.mode" list="form.modes" listKey="value" listValue="label"/>
		</TD>
	</TR>
	<s:if test="form.canEmail == false">
		<s:hidden name="form.email"/>
	</s:if>
	<s:else>
	<TR>
		<TD rowspan='1' valign='top'><loc:message name="propReportDelivery"/></TD>
		<TD>
			<s:checkbox name="form.email" onclick="document.getElementById('eml').style.display=(this.checked?'block':'none');"/> <loc:message name="checkReportDeliveryEmail"/>
			<s:set var="email" value="form.email"/>
			<table id='eml' style='display:none;'>
				<sec:authorize access="hasPermission(null, null, 'DepartmentIndependent')">
					<tr>
						<td rowspan='4' valign='top'><loc:message name="propEmailAddress"/></td>
						<td><s:textarea name="form.address" rows="3" cols="70"/></td>
					</tr>
					<tr><td>
						<s:checkbox name="form.emailDeputies"/> <loc:message name="checkAllInvolvedDepartmentScheduleManagers"/>
					</td></tr>
					<tr><td>
						<s:checkbox name="form.emailInstructors"/> <loc:message name="checkSendIndividualInstructorReportsToInstructors"/>
					</td></tr>
					<tr><td>
						<s:checkbox name="form.emailStudents"/> <loc:message name="checkSendIndividualStudentReportsToStudents"/>
					</td></tr>
				</sec:authorize>
				<sec:authorize access="!hasPermission(null, null, 'DepartmentIndependent')">
					<tr>
						<td valign='top'><loc:message name="propEmailAddress"/></td>
						<td><s:textarea name="form.address" rows="3" cols="70"/></td>
					</tr>
				</sec:authorize>
				<tr><td valign='top'><loc:message name="propEmailCC"/></td><td>
					<s:textarea name="form.cc" rows="2" cols="70"/>
				</td></tr>
				<tr><td valign='top'><loc:message name="propEmailBCC"/></td><td>
					<s:textarea name="form.bcc" rows="2" cols="70"/>
				</td></tr>
				<tr><td valign='top' style='border-top: black 1px dashed;'><loc:message name="propEmailSubject"/></td><td style='border-top: black 1px dashed;'>
					<s:textfield name="form.subject" size="70" style="margin-top:2px;"/>
				</td></tr>
				<tr><td valign='top'><loc:message name="propEmailMessage"/></td><td>
					<s:textarea name="form.message" rows="10" cols="70"/>
				</td></tr>
			</table>
			<s:if test="form.email == true">
				<script type="text/javascript">
					document.getElementById('eml').style.display = 'block';
				</script>
			</s:if>
		</TD>
	</TR>
	</s:else>
	<TR>
		<TD colspan='2'>
			<tt:section-title><br>&nbsp;</tt:section-title>
		</TD>
	</TR>
	<TR>
		<TD colspan='2' align='right'>
			<s:submit accesskey='%{#msg.accessGenerateReport()}' name='op' value='%{#msg.actionGenerateReport()}' title='%{#msg.titleGenerateReport()}'/>
		</TD>
	</TR>
	</TABLE>
<script type="text/javascript">
	function selectionChanged() {
		if (document.getElementById('examPdfReport_form_all')==null || document.getElementById('examPdfReport_form_all').length==0) return;
		var allSubjects = document.getElementById('examPdfReport_form_all').checked;
		var objSubjects = document.getElementById('examPdfReport_form_subjects');
		var objEmailDeputies = document.getElementById('examPdfReport_form_emailDeputies');
		var objEmailInstructors = document.getElementById('examPdfReport_form_emailInstructors');
		var objEmailStudents = document.getElementById('examPdfReport_form_emailStudents');
		var objReports = document.getElementsByName('form.reports');
		var objSince = document.getElementById('examPdfReport_form_since');
		var studentSchedule = false;
		var instructorSchedule = false;
		for (var i=0;i<objReports.length;i++) {
			if ('StudentExamReport'==objReports[i].value) studentSchedule = objReports[i].checked;
			if ('InstructorExamReport'==objReports[i].value) instructorSchedule = objReports[i].checked;
		}
		objSubjects.disabled=allSubjects;
		if (objEmailDeputies) objEmailDeputies.disabled=allSubjects; 
		if (objEmailInstructors) objEmailInstructors.disabled=!instructorSchedule;
		if (objEmailStudents) objEmailStudents.disabled=!studentSchedule;
		if (allSubjects) {
			objEmailDeputies.checked=false;
		}
		if (!studentSchedule) objEmailStudents.checked=false;
		if (!instructorSchedule) objEmailInstructors.checked=false;
		objSince.disabled=objEmailInstructors.disabled && objEmailStudents.disabled;
	}
	selectionChanged();
</script>
</s:form>
</loc:bundle>
