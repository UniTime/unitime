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
<loc:bundle name="ExaminationMessages"><s:set var="msg" value="#attr.MSG"/> 
<tt:confirm name="confirmDelete"><loc:message name="confirmExamDelete"/></tt:confirm>
<s:form action="examDetail">
	<s:hidden name="form.examId"/>
	<s:hidden name="form.nextId"/>
	<s:hidden name="form.previousId"/>
	<s:hidden name="op2" value=""/>
	<table class="unitime-MainTable">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<s:property value="form.label"/>
					</tt:section-title>
				<sec:authorize access="hasPermission(#form.examId, 'Exam', 'ExaminationEdit')">
					<s:submit accesskey='%{#msg.accessExamEdit()}' name='op' value='%{#msg.actionExamEdit()}' title='%{#msg.titleExamEdit()}'/>
				</sec:authorize>
				<sec:authorize access="hasPermission(#form.examId, 'Exam', 'ExaminationClone')">
					<s:submit accesskey='%{#msg.accessExamClone()}' name='op' value='%{#msg.actionExamClone()}' title='%{#msg.titleExamClone()}'/>
				</sec:authorize>
				<sec:authorize access="hasPermission(#form.examId, 'Exam', 'DistributionPreferenceExam')">
					<s:submit accesskey='%{#msg.accessExamAddDistributionPref()}' name='op' value='%{#msg.actionExamAddDistributionPref()}' title='%{#msg.titleExamAddDistributionPref()}'/>
				</sec:authorize>
				<sec:authorize access="hasPermission(#form.examId, 'Exam', 'ExaminationAssignment')">
					<input type="button" value="${MSG.actionExamAssign()}" title="${MSG.titleExamAssign()}" class="btn" accesskey="${MSG.accessExamAssign()}"
							onClick="showGwtDialog('${MSG.dialogExamAssign()}', 'examInfo.action?examId=${examId}','900','90%');"
					/>
				</sec:authorize>
				<sec:authorize access="hasPermission(#form.examId, 'Exam', 'ExaminationDelete')">
					<s:submit accesskey='%{#msg.accessExamDelete()}' name='op' value='%{#msg.actionExamDelete()}' title='%{#msg.titleExamDelete()}' onclick="return confirmDelete();"/>
				</sec:authorize>
				<s:if test="form.previousId > 0">
					<s:submit accesskey='%{#msg.accessExamPrevious()}' name='op' value='%{#msg.actionExamPrevious()}' title='%{#msg.titleExamPrevious()}'/>
				</s:if>
				<s:if test="form.nextId > 0">
					<s:submit accesskey='%{#msg.accessExamNext()}' name='op' value='%{#msg.actionExamNext()}' title='%{#msg.titleExamNext()}'/>
				</s:if>
				<tt:back styleClass="btn" name="${MSG.actionExamBack()}" title="${MSG.titleExamBack()}" accesskey="${MSG.accessExamBack()}" type="PreferenceGroup">
					<s:property value="form.examId"/>
				</tt:back>
				</tt:section-header>
			</TD>
		</TR>
		
		<s:if test="!fieldErrors.isEmpty()">
		<TR>
			<TD colspan="2" align="left" class="errorTable">
				<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror/>
			</TD>
		</TR>
		</s:if>
		
		<TR>
			<TD><loc:message name="propExamName"/></TD><TD>
				<s:if test="form.name != null && !form.name.isEmpty()">
					<s:property value="form.name"/>
				</s:if>
				<s:else>
					<i><s:property value="form.label"/></i>
				</s:else>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="propExamType"/></TD><TD>
				<s:iterator value="#request.examTypes" var="et">
					<s:if test="#et.uniqueId == form.examType">
						<s:property value="label"/>
					</s:if>
				</s:iterator>
			 </TD>
		</TR>
		<TR>
			<TD><loc:message name="propExamLength"/></TD><TD><s:property value="form.length"/></TD>
		</TR>
		<TR>
			<TD><loc:message name="propExamSeatingType"/></TD><TD><s:property value="form.seatingType"/></TD>
		</TR>
		<TR>
			<TD nowrap><loc:message name="propExamMaxRooms"/></TD><TD><s:property value="form.maxNbrRooms"/></TD>
		</TR>
		<TR>
			<TD><loc:message name="propExamSize"/></TD><TD><s:property value="form.size"/></TD>
		</TR>
		<s:if test="form.printOffset != null">
			<TR>
				<TD><loc:message name="propExamPrintOffset"/></TD><TD><s:property value="form.printOffset"/> <loc:message name="offsetUnitMinutes"/></TD>
			</TR>
		</s:if>
		<s:if test="form.instructors != null && !form.instructors.isEmpty()">
			<TR>
				<TD valign="top"><loc:message name="propExamInstructors"/></TD>
				<TD>
					<table>
					<s:iterator value="form.instructors" var="instructor">
						<s:iterator value="#request.instructorsList" var="instr">
							<s:if test="#instr.value == #instructor">
								<tr onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='hand';this.style.cursor='pointer';" 
									onmouseout="this.style.backgroundColor='transparent';"
									onClick="document.location='instructorDetail.action?instructorId=${instructor}';"
								>
									<td style="padding-right: 20px;">
										<s:property value="#instr.label"/>
									</td>
									<td>
										<s:set var="email" value="form.getEmail(#instructor)"/>
										<s:if test="#email != null">
											<a href="mailto:${email}" onclick="event.cancelBubble=true;"><s:property value="email"/></a>
										</s:if>
									</td>
								</tr>
							</s:if>
						</s:iterator>
					</s:iterator>
	   				</table>
			   	</TD>
		   	</TR>
		</s:if>
		
		<s:if test="form.avgPeriod != null">
			<TR>
				<TD><loc:message name="propExamAvgPeriod"/></TD><TD><s:property value="form.avgPeriod"/></TD>
			</TR>
		</s:if>
		
		<tt:last-change type='Exam'>
			<s:property value="form.examId"/>
		</tt:last-change>
		
		<s:if test="form.note != null && !form.note.isEmpty()">
			<TR>
				<TD colspan="2" valign="middle">
					<br>
					<tt:section-title>
						<loc:message name="sectExamNotes"/>
					</tt:section-title>
				</TD>
			</TR>
			<TR>
				<TD colspan='2'><s:property value="form.note" escapeHtml="false"/></TD>
			</TR>
		</s:if>
		
		<s:if test="form.accommodation != null">
			<TR>
				<TD valign="top"><loc:message name="propExamStudentAccommodations"/></TD><TD><s:property value="form.accommodation" escapeHtml="false"/></TD>
			</TR>
		</s:if>
		<TR>
			<TD colspan="2" valign="middle">
				<br>
				<tt:section-title>
					<loc:message name="sectExamOwners"/>
				</tt:section-title>
			</TD>
		</TR>
		<TR>
			<TD colspan='2'>
				<s:if test="#request.table == null">
					<i><loc:message name="warnNoExamOwners"/></i>
				</s:if>
				<s:else>
					<table style='width: 100%;'>
						<s:property value="#request.table" escapeHtml="false"/>
					</table>
				</s:else>
			</TD>
		</TR>
		
		<s:if test="#request.assignment != null">
			<TR>
				<TD colspan="2" valign="middle">
					<br>
					<tt:section-title>
						<loc:message name="sectExamAssignment"/>
					</tt:section-title>
				</TD>
			</TR>
			<TR>
				<TD colspan='2'>
					<s:property value="#request.assignment" escapeHtml="false"/>
				</TD>
			</TR>
		</s:if>
		

<!-- Preferences -->		
		<TR>
			<TD colspan="2" valign="middle">
				<br>
				<tt:section-title>
					<loc:message name="sectExamPreferences"/>
				</tt:section-title>
			</TD>
		</TR>

		<s:include value="preferencesDetail2.jspf">
			<s:param name="timePref" value="false"/>
			<s:param name="examSeating" value="form.isExamSeating()"/>
		</s:include>
		
		<TR>
			<TD colspan="2">
				<div id='UniTimeGWT:ExamEnrollments' style="display: none;"><s:property value="form.examId"/></div>
			</TD>
		</TR>		
	
		<TR>
			<TD colspan="2" class="WelcomeRowHead">
				&nbsp;
			</TD>
		</TR>

		<TR align="right">
			<TD valign="middle" colspan='2'>
				<s:set var="msg" value="#attr.MSG"/>
				<sec:authorize access="hasPermission(#form.examId, 'Exam', 'ExaminationEdit')">
					<s:submit accesskey='%{#msg.accessExamEdit()}' name='op' value='%{#msg.actionExamEdit()}' title='%{#msg.titleExamEdit()}'/>
				</sec:authorize>
				<sec:authorize access="hasPermission(#form.examId, 'Exam', 'ExaminationClone')">
					<s:submit accesskey='%{#msg.accessExamClone()}' name='op' value='%{#msg.actionExamClone()}' title='%{#msg.titleExamClone()}'/>
				</sec:authorize>
				<sec:authorize access="hasPermission(#form.examId, 'Exam', 'DistributionPreferenceExam')">
					<s:submit accesskey='%{#msg.accessExamAddDistributionPref()}' name='op' value='%{#msg.actionExamAddDistributionPref()}' title='%{#msg.titleExamAddDistributionPref()}'/>
				</sec:authorize>
				<sec:authorize access="hasPermission(#form.examId, 'Exam', 'ExaminationAssignment')">
					<input type="button" value="${MSG.actionExamAssign()}" title="${MSG.titleExamAssign()}" class="btn" accesskey="${MSG.accessExamAssign()}"
							onClick="showGwtDialog('${MSG.dialogExamAssign()}', 'examInfo.action?examId=${examId}','900','90%');"
					/>
				</sec:authorize>
				<sec:authorize access="hasPermission(#form.examId, 'Exam', 'ExaminationDelete')">
					<s:submit accesskey='%{#msg.accessExamDelete()}' name='op' value='%{#msg.actionExamDelete()}' title='%{#msg.titleExamDelete()}' onclick="return confirmDelete();"/>
				</sec:authorize>
				<s:if test="form.previousId > 0">
					<s:submit accesskey='%{#msg.accessExamPrevious()}' name='op' value='%{#msg.actionExamPrevious()}' title='%{#msg.titleExamPrevious()}'/>
				</s:if>
				<s:if test="form.nextId > 0">
					<s:submit accesskey='%{#msg.accessExamNext()}' name='op' value='%{#msg.actionExamNext()}' title='%{#msg.titleExamNext()}'/>
				</s:if>
				<tt:back styleClass="btn" name="${MSG.actionExamBack()}" title="${MSG.titleExamBack()}" accesskey="${MSG.accessExamBack()}" type="PreferenceGroup">
					<s:property value="form.examId"/>
				</tt:back>
			</TD>
		</TR>
		
 	</TABLE>

</s:form></loc:bundle>

