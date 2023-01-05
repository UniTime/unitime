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
<s:form action="deptStatusTypeEdit">
<tt:confirm name="confirmDelete"><loc:message name="confirmStatusTypeDelete"/></tt:confirm>
<s:hidden name="form.uniqueId"/><s:fielderror fieldName="form.uniqueId"/>
<s:if test="form.op != 'List'">
	<table class="unitime-MainTable">
		<TR>
			<TD colspan="3">
				<tt:section-header>
					<tt:section-title>
						<s:if test="form.op == #msg.actionSaveStatusType()">
							<loc:message name="sectAddStatusType"/>
						</s:if><s:else>
							<loc:message name="sectEditStatusType"/>
						</s:else>
					</tt:section-title>
					<s:if test="form.op == #msg.actionSaveStatusType()">
						<s:submit name='op' value='%{#msg.actionSaveStatusType()}'/>
					</s:if><s:else>
						<s:submit name='op' value='%{#msg.actionUpdateStatusType()}'/>
						<s:submit name='op' value='%{#msg.actionDeleteStatusType()}' onclick="return confirmDelete();"/>
					</s:else>
					<s:submit name='op' value='%{#msg.actionBackToStatusTypes()}'/>
				</tt:section-header>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="fieldReference"/>:</TD>
			<TD colspan='2'>
				<s:textfield name="form.reference" size="20" maxlength="20"/>
				&nbsp;<s:fielderror fieldName="form.reference"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="fieldLabel"/>:</TD>
			<TD colspan='2'>
				<s:textfield name="form.label" size="60" maxlength="60"/>
				&nbsp;<s:fielderror fieldName="form.label"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="fieldApply"/>:</TD>
			<TD colspan='2'>
			<s:select name="form.apply"
				list="form.applyOptions" listKey="id" listValue="value"/>
			&nbsp;<s:fielderror fieldName="form.apply"/>
		</TR>
		
		<TR><TD colspan='3'><tt:section-title><br><loc:message name="sectCourseTimetabling"/></tt:section-title></TD></TR>
		<TR><TD><loc:message name="propInstructorSurvey"/></TD><TD><s:checkbox name="form.instructorSurvey"/></TD>
  			<TD><i><loc:message name="descInstructorSurvey"/></i></TD></TR>
		<TR><TD><loc:message name="propOwnerView"/></TD><TD><s:checkbox name="form.canOwnerView"/></TD>
			<TD><i><loc:message name="descOwnerView"/></i></TD></TR>
		<TR><TD><loc:message name="propOwnerLimitedEdit"/></TD><TD><s:checkbox name="form.canOwnerLimitedEdit"/></TD>
			<TD><i><loc:message name="descOwnerLimitedEdit"/></i></TD></TR>
		<TR><TD><loc:message name="propOwnerEdit"/></TD><TD><s:checkbox name="form.canOwnerEdit"/></TD>
			<TD><i><loc:message name="descOwnerEdit"/></i></TD></TR>
		<TR><TD><loc:message name="propManagerView"/></TD><TD><s:checkbox name="form.canManagerView"/></TD>
			<TD><i><loc:message name="descManagerView"/></i></TD></TR>
		<TR><TD nowrap><loc:message name="propManagerLimitedEdit"/></TD><TD><s:checkbox name="form.canManagerLimitedEdit"/></TD>
			<TD><i><loc:message name="descManagerLimitedEdit"/></i></TD></TR>
		<TR><TD><loc:message name="propManagerEdit"/></TD><TD><s:checkbox name="form.canManagerEdit"/></TD>
  			<TD><i><loc:message name="descManagerEdit"/></i></TD></TR>
		<TR><TD><loc:message name="propAudit"/></TD><TD><s:checkbox name="form.canAudit"/></TD>
  			<TD><i><loc:message name="descAudit"/></i></TD></TR>
		<TR><TD><loc:message name="propTimetable"/></TD><TD><s:checkbox name="form.canTimetable"/></TD>
  			<TD><i><loc:message name="descTimetable"/></i></TD></TR>
		<TR><TD><loc:message name="propCommit"/></TD><TD><s:checkbox name="form.canCommit"/></TD>
  			<TD><i><loc:message name="descCommit"/></i></TD></TR>

		<TR><TD colspan='3'><tt:section-title><br><loc:message name="sectExaminationTimetabling"/></tt:section-title></TD></TR>
		<TR><TD><loc:message name="propExamView"/></TD><TD><s:checkbox name="form.canExamView"/></TD>
  			<TD><i><loc:message name="descExamView"/></i></TD></TR>
		<TR><TD><loc:message name="propExamEdit"/></TD><TD><s:checkbox name="form.canExamEdit"/></TD>
  			<TD><i><loc:message name="descExamEdit"/></i></TD></TR>
		<TR><TD><loc:message name="propExamTimetable"/></TD><TD><s:checkbox name="form.canExamTimetable"/></TD>
  			<TD><i><loc:message name="descExamTimetable"/></i></TD></TR>

		<TR><TD colspan='3'><tt:section-title><br><loc:message name="sectStudentSectioning"/></tt:section-title></TD></TR>
		<TR><TD nowrap><loc:message name="propRegistration"/></TD><TD><s:checkbox name="form.canPreRegisterStudents"/></TD>
  			<TD><i><loc:message name="descRegistration"/></i></TD></TR>
		<TR><TD nowrap><loc:message name="propAssistant"/></TD><TD><s:checkbox name="form.canSectioningStudents"/></TD>
  			<TD><i><loc:message name="descAssistant"/></i></TD></TR>
		<TR><TD nowrap><loc:message name="propOnlineSectioning"/></TD><TD><s:checkbox name="form.canOnlineSectionStudents"/></TD>
  			<TD><i><loc:message name="descOnlineSectioning"/></i></TD></TR>

		<TR><TD colspan='3'><tt:section-title><br><loc:message name="sectEventManagement"/></tt:section-title></TD></TR>
		<TR><TD nowrap><loc:message name="propEvents"/></TD><TD><s:checkbox name="form.eventManagement"/></TD>
  			<TD><i><loc:message name="descEvents"/></i></TD></TR>
		<TR><TD><loc:message name="propClassSchedule"/></TD><TD><s:checkbox name="form.canNoRoleReportClass"/></TD>
  			<TD><i><loc:message name="descClassSchedule"/></i></TD></TR>
		<TR><TD><loc:message name="propFinalExaminationSchedule"/></TD><TD><s:checkbox name="form.canNoRoleReportExamFin"/></TD>
  			<TD><i><loc:message name="descFinalExaminationSchedule"/></i></TD></TR>
		<TR><TD nowrap><loc:message name="propMidtermExaminationSchedule"/></TD><TD><s:checkbox name="form.canNoRoleReportExamMid"/></TD>
  			<TD><i><loc:message name="descMidtermExaminationSchedule"/></i></TD></TR>

		<TR><TD colspan='3'><tt:section-title><br><loc:message name="sectOther"/></tt:section-title></TD></TR>
		<TR><TD><loc:message name="propAllowRollForward"/></TD><TD><s:checkbox name="form.allowRollForward"/></TD>
  			<TD><i><loc:message name="descAllowRollForward"/></i></TD></TR>
		<TR><TD><loc:message name="propAllowNoRole"/></TD><TD><s:checkbox name="form.allowNoRole"/></TD>
  			<TD><i><loc:message name="descAllowNoRole"/></i></TD></TR>
		<TR><TD nowrap><loc:message name="propTestSession"/></TD><TD><s:checkbox name="form.testSession"/></TD>
  			<TD><i><loc:message name="descTestSession"/></i></TD></TR>

		<TR>
			<TD colspan='3'>
				<tt:section-title/>
			</TD>
		</TR>
		<TR>
			<TD align="right" colspan="3">
				<s:if test="form.op == #msg.actionSaveStatusType()">
					<s:submit name='op' value='%{#msg.actionSaveStatusType()}'/>
				</s:if><s:else>
					<s:submit name='op' value='%{#msg.actionUpdateStatusType()}'/>
					<s:submit name='op' value='%{#msg.actionDeleteStatusType()}' onclick="return confirmDelete();"/>
				</s:else>
				<s:submit name='op' value='%{#msg.actionBackToStatusTypes()}'/>
			</TD>
		</TR>
	</TABLE>
</s:if><s:else>
	<s:hidden name="form.op" value="" id="op"/>
	<s:hidden name="id" value="" id="id"/>
	<table class="unitime-MainTable">
		<tr>
			<td colspan='5'>
				<tt:section-header>
					<tt:section-title><loc:message name="sectStatusTypes"/></tt:section-title>
					<s:submit name='op' value='%{#msg.actionAddStatusType()}'/>
				</tt:section-header>
			</td>
		</tr>
		<s:property value="table" escapeHtml="false"/>
		<tr>
			<td colspan='5'>
				<tt:section-title/>
			</td>
		</tr>
		<tr>
			<td colspan='5' align="right">
				<s:submit name='op' value='%{#msg.actionAddStatusType()}'/>
			</td>
		</tr>
	</TABLE>
</s:else>
</s:form>
</loc:bundle>
