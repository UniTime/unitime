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
<s:form action="classDetail">
	<s:hidden name="form.classId"/>
	<s:hidden name="form.nextId"/>
	<s:hidden name="form.previousId"/>
	<table class="unitime-MainTable">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<A title="${MSG.titleInstructionalOfferingDetail(MSG.accessInstructionalOfferingDetail())}" 
							accesskey="${MSG.accessInstructionalOfferingDetail()}" class="l8"
							href="instructionalOfferingDetail.action?op=view&io=${form.instrOfferingId}"><s:property value="form.courseName"/> - <s:property value="form.courseTitle"/></A>:
						<s:if test="form.subpart == null"><s:property value="form.itypeDesc"/></s:if>
						<s:else> 
							<A title="${MSG.titleSchedulingSubpartDetail(MSG.accessSchedulingSubpartDetail())}" 
								accesskey="${MSG.accessSchedulingSubpartDetail()}" class="l8"
								href="schedulingSubpartDetail.action?ssuid=${form.subpart}"><s:property value="form.itypeDesc"/></A>
						</s:else>
						<s:property value="form.section"/>
					</tt:section-title>
					<sec:authorize access="hasPermission(#form.classId, 'Class_', 'ClassEdit')">
						<s:submit accesskey='%{#msg.accessEditClass()}' name='op' value='%{#msg.actionEditClass()}'
							title='%{#msg.titleEditClass(#msg.accessEditClass())}'/>
					</sec:authorize>
					<sec:authorize access="hasPermission(#form.classId, 'Class_', 'DistributionPreferenceClass')">
						<s:submit accesskey='%{#msg.accessAddDistributionPreference()}' name='op' value='%{#msg.actionAddDistributionPreference()}'
							title='%{#msg.titleAddDistributionPreference(#msg.accessAddDistributionPreference())}'/>
					</sec:authorize>
					<sec:authorize access="hasPermission(#form.classId, 'Class_', 'ClassAssignment')">
						<input type="button" value="${MSG.actionOpenClassAssignmentDialog()}" 
								title="${MSG.titleOpenClassAssignmentDialog(MSG.accessOpenClassAssignmentDialog())}" 
								class="btn" 
								accesskey="${MSG.accessOpenClassAssignmentDialog()}"
								onClick="showGwtDialog('${MSG.dialogClassAssignment()}', 'classInfo.action?classId=${form.classId}','900','90%');"
						/>
					</sec:authorize>
					<s:if test="form.previousId != null">
						<s:submit accesskey='%{#msg.accessPreviousClass()}' name='op' value='%{#msg.actionPreviousClass()}'
							title='%{#msg.titlePreviousClass(#msg.accessPreviousClass())}'/>
					</s:if>
					<s:if test="form.nextId != null">
						<s:submit accesskey='%{#msg.accessNextClass()}' name='op' value='%{#msg.actionNextClass()}'
							title='%{#msg.titleNextClass(#msg.accessNextClass())}'/>
					</s:if>
					<tt:back styleClass="btn" 
						name="${MSG.actionBackClassDetail()}" 
						title="${MSG.titleBackClassDetail(MSG.accessBackClassDetail())}" 
						accesskey="${MSG.accessBackClassDetail()}" 
						type="PreferenceGroup">
						<s:property value="form.classId"/>
					</tt:back>
				</tt:section-header>
			</TD>
		</TR>
		
		<s:if test="!fieldErrors.isEmpty()">
			<TR><TD colspan="2" align="left" class="errorTable">
				<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror/>
			</TD></TR>
		</s:if>
		
		<s:if test="form.isCancelled == true">
			<TR>
				<TD></TD>
				<TD style="color:red; font-weight: bold;">
					<loc:message name="classNoteCancelled"><s:property value="form.courseName"/> <s:property value="form.itypeDesc"/> <s:property value="form.section"/></loc:message>
				</TD>
			</TR>
		</s:if>

		<TR>
			<TD><loc:message name="filterManager"/></TD>
			<TD>
				<s:property value="form.managingDeptLabel"/>
			</TD>
		</TR>
		
		<s:if test="form.parentClassName != \"-\"">
			<TR>
				<TD><loc:message name="propertyParentClass"/> </TD>
				<TD>
					<s:if test="form.parentClassId == null">
						<s:property value="form.parentClassName"/>
					</s:if>
					<s:else>
						<A href="classDetail.action?cid=${form.parentClassId}"><s:property value="form.parentClassName"/></A>
					</s:else>
				</TD>
			</TR>
		</s:if>
		
		<s:if test="form.classSuffix != null">
			<TR>
				<TD><loc:message name="propertyExternalId"/></TD>
				<TD>
					<s:property value="form.classSuffix"/>
				</TD>
			</TR>
		</s:if>
		<TR>
			<TD><loc:message name="propertyEnrollment"></loc:message> </TD>
			<TD>
				<s:property value="form.enrollment"/>
			</TD>
		</TR>
		<s:if test="form.nbrRooms > 0 && form.expectedCapacity == form.maxExpectedCapacity">
			<TR>
				<TD><loc:message name="propertyClassLimit"/></TD>
				<TD>
					<s:property value="form.expectedCapacity"/>
				</TD>
			</TR>
		</s:if>
		<s:if test="form.nbrRooms > 0 && form.expectedCapacity != form.maxExpectedCapacity">
			<TR>
				<TD><loc:message name="propertyMinimumClassLimit"/></TD>
				<TD>
					<s:property value="form.expectedCapacity"/>
				</TD>
			</TR>
			<TR>
				<TD><loc:message name="propertyMaximumClassLimit"/></TD>
				<TD>
					<s:property value="form.maxExpectedCapacity"/>
				</TD>
			</TR>
		</s:if>
		
		<s:if test="form.nbrRooms != 0 && form.snapshotLimit != null">
			<TR>
				<TD><loc:message name="propertySnapshotLimit"/></TD>
				<TD>
					<s:property value="form.snapshotLimit"/>
				</TD>
			</TR>
		</s:if>

		<TR>
			<TD><loc:message name="propertyNumberOfRooms"/></TD>
			<TD>
				<s:property value="form.nbrRooms"/>
			</TD>
		</TR>
		
		<s:if test="form.nbrRooms != 0">
			<TR>
				<TD><loc:message name="propertyRoomRatio"/></TD>
				<TD>
					<s:property value="form.roomRatio"/>
					&nbsp;&nbsp;&nbsp;&nbsp; ( <loc:message name="propertyMinimumRoomCapacity"/>
					<s:property value="form.minRoomLimit"/> )
				</TD>
			</TR>
		</s:if>

		<s:if test="form.lms != null && !form.lms.isEmpty()">
			<TR>
				<TD valign="top"><loc:message name="propertyLms"/></TD>
				<TD>
					<s:property value="form.lms" escapeHtml="false"/>
				</TD>
			</TR>
		</s:if>
		
		<s:if test="form.fundingDept != null">
			<TR>
				<TD valign="top"><loc:message name="propertyFundingDept"/></TD>
				<TD>
					<s:property value="form.fundingDept" escapeHtml="false"/>
				</TD>
			</TR>
		</s:if>

		<TR>
			<TD><loc:message name="propertyDatePattern"/></TD>
			<TD>
				<s:iterator value="#request.datePatternList" var="dp">
					<s:if test="#dp.id == form.datePattern">
						<s:property value="#dp.value"/>
						<img style="cursor: pointer;" src="images/calendar.png" border="0" onclick="showGwtDialog('${MSG.sectPreviewOfDatePattern(dp.value)}', 'dispDatePattern.action?id=${dp.id}&classId=${form.classId}','840','520');">
					</s:if>
				</s:iterator>
			</TD>
		</TR>
		
		<TR>
			<TD><loc:message name="propertyDisplayInstructors"/></TD>
			<TD>
				<s:if test="form.displayInstructor == true">
					<IMG src="images/accept.png" border="0" alt="${MSG.titleInstructorDisplayed()}" title="${MSG.titleInstructorDisplayed()}">
				</s:if>
				<s:else>
					<IMG src="images/cross.png" border="0" alt="${MSG.titleInstructorNotDisplayed()}" title="${MSG.titleInstructorNotDisplayed()}">
				</s:else>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propertyEnabledForStudentScheduling"/> </TD>
			<TD>
				<s:if test="form.enabledForStudentScheduling == true">
					<IMG src="images/accept.png" border="0" alt="${MSG.titleEnabledForStudentScheduling()}" title="${MSG.titleEnabledForStudentScheduling()}">
				</s:if>
				<s:else>
					<IMG src="images/cross.png" border="0" alt="${MSG.titleNotEnabledForStudentScheduling()}" title="${MSG.titleNotEnabledForStudentScheduling()}">
				</s:else>
			</TD>
		</TR>

		<s:if test="form.schedulePrintNote != null && !form.schedulePrintNote.isEmpty()">
			<TR>
				<TD valign="top"><loc:message name="propertyStudentScheduleNote"/></TD>
				<TD style="white-space: pre-wrap;"><s:property value="form.schedulePrintNote" escapeHtml="false"/></TD>
			</TR>
		</s:if>

		<s:if test="form.notes != null && !form.notes.isEmpty()">
			<TR>
				<TD valign="top"><loc:message name="propertyRequestsNotes"/></TD>
				<TD style="white-space: pre-wrap;"><s:property value="form.notes" escapeHtml="false"/></TD>
			</TR>
		</s:if>
				
		<s:if test="form.instructors != null && !form.instructors.isEmpty()">
			<TR>
				<TD valign="top"><loc:message name="propertyInstructors"/></TD>
				<TD>
					<table style="border-spacing: 0px;">
						<tr><td width='250'><i><loc:message name="columnInstructorName"/></i></td><td width='80'><i><loc:message name="columnInstructorShare"/></i></td><td width='100'><i><loc:message name="columnInstructorCheckConflicts"/></i></td>
						<s:if test="#request.responsibilities != null && !#request.responsibilities.isEmpty()">
							<td width='200'><i><loc:message name="columnTeachingResponsibility"/></i></td>
						</s:if>
						</tr>
						<s:iterator value="form.instructors" var="instructor" status="stat">
							<tr onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='hand';this.style.cursor='pointer';" 
								onmouseout="this.style.backgroundColor='transparent';"
								onClick="document.location='instructorDetail.action?instructorId=${instructor}';"
							>
								<td>
								<s:iterator value="#request.instructorsList" var="instr">
									<s:if test="#instr.value == #instructor">
										<s:property value="#instr.label"/>
									</s:if>
								</s:iterator>
								</td>
								<td> 
									<s:property value="form.instrPctShare[#stat.index]"/>%
								</td>
								<td>
									<s:if test="form.instrLead[#stat.index] == 'true'">
										<IMG border='0' alt='true' align="middle" src='images/accept.png'>
									</s:if>
								</td>
								<s:if test="#request.responsibilities != null">
									<td>
										<s:iterator value="#request.responsibilities" var="responsibility">
											<s:if test="#responsibility.uniqueId == form.instrResponsibility[#stat.index]">
												<s:property value="#responsibility.label"/>
											</s:if>
										</s:iterator>
									</td>
								</s:if>
							</tr>
						</s:iterator>
					</table>
				</TD>
			</TR>
		</s:if>

		<s:if test="form.accommodation != null">
			<TR>
				<TD valign="top"><loc:message name="propertyAccommodations"/></TD>
				<TD>
					<s:property value="form.accommodation" escapeHtml="false"/>
				</TD>
			</TR>
		</s:if>

		<tt:last-change type='Class_'><s:property value="form.classId"/></tt:last-change>
		
		<s:if test="#request.assignmentInfo != null">
			<TR>
				<TD colspan="2" align="left" style="padding-top: 20px;">
					<tt:section-title><loc:message name="sectionTitleTimetable"/></tt:section-title>
				</TD>
			</TR>
			<s:property value="#request.assignmentInfo" escapeHtml="false"/>
		</s:if>

		<s:if test="#request.classConflicts != null">
			<TR>
				<TD colspan="2" style="padding-top: 20px;">
					<table style="width:100%;">
						<s:property value="#request.classConflicts" escapeHtml="false"/>
					</table>
				</TD>
			</TR>
		</s:if>

		<s:if test="#request.eventConflicts != null">
			<TR>
				<TD colspan="2" style="padding-top: 20px;">
					<table style="width:100%;">
						<s:property value="#request.eventConflicts" escapeHtml="false"/>
					</table>
				</TD>
			</TR>
		</s:if>

<!-- Preferences -->
		<TR>
			<TD colspan="2" valign="middle" style="padding-top: 20px;">
				<tt:section-title><loc:message name="sectionTitlePreferences"/></tt:section-title>
			</TD>
		</TR>
		<s:if test="form.nbrRooms == 0">
			<s:include value="preferencesDetail2.jspf">
				<s:param name="bldgPref" value="false"/>
				<s:param name="roomFeaturePref" value="false"/>
				<s:param name="roomGroupPref" value="false"/>
			</s:include>
		</s:if>
		<s:if test="form.nbrRooms != 0">
			<s:if test="form.unlimitedEnroll == true">
				<s:include value="preferencesDetail2.jspf">
					<s:param name="bldgPref" value="false"/>
					<s:param name="roomFeaturePref" value="false"/>
					<s:param name="roomGroupPref" value="false"/>
				</s:include>
			</s:if>
			<s:if test="form.unlimitedEnroll != true">
				<s:include value="preferencesDetail2.jspf"/>
			</s:if>
		</s:if>
		
		<TR>
			<TD colspan="2">
				<tt:exams type='Class_' add='true'><s:property value="form.classId"/></tt:exams>
			</TD>
		</TR>

		<TR>
			<TD colspan="2">
				<div id='UniTimeGWT:OfferingEnrollments' style="display: none;">-<s:property value="form.classId"/></div>
			</TD>
		</TR>
		

<!-- Buttons -->
		<TR>
			<TD colspan="2" align="right">
				<tt:section-title/>
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="right">
					<sec:authorize access="hasPermission(#form.classId, 'Class_', 'ClassEdit')">
						<s:submit accesskey='%{#msg.accessEditClass()}' name='op' value='%{#msg.actionEditClass()}'
							title='%{#msg.titleEditClass(#msg.accessEditClass())}'/>
					</sec:authorize>
					<sec:authorize access="hasPermission(#form.classId, 'Class_', 'DistributionPreferenceClass')">
						<s:submit accesskey='%{#msg.accessAddDistributionPreference()}' name='op' value='%{#msg.actionAddDistributionPreference()}'
							title='%{#msg.titleAddDistributionPreference(#msg.accessAddDistributionPreference())}'/>
					</sec:authorize>
					<sec:authorize access="hasPermission(#form.classId, 'Class_', 'ClassAssignment')">
						<input type="button" value="${MSG.actionOpenClassAssignmentDialog()}" 
								title="${MSG.titleOpenClassAssignmentDialog(MSG.accessOpenClassAssignmentDialog())}" 
								class="btn" 
								accesskey="${MSG.accessOpenClassAssignmentDialog()}"
								onClick="showGwtDialog('${MSG.dialogClassAssignment()}', 'classInfo.action?classId=${form.classId}','900','90%');"
						/>
					</sec:authorize>
					<s:if test="form.previousId != null">
						<s:submit accesskey='%{#msg.accessPreviousClass()}' name='op' value='%{#msg.actionPreviousClass()}'
							title='%{#msg.titlePreviousClass(#msg.accessPreviousClass())}'/>
					</s:if>
					<s:if test="form.nextId != null">
						<s:submit accesskey='%{#msg.accessNextClass()}' name='op' value='%{#msg.actionNextClass()}'
							title='%{#msg.titleNextClass(#msg.accessNextClass())}'/>
					</s:if>
					<tt:back styleClass="btn" 
						name="${MSG.actionBackClassDetail()}" 
						title="${MSG.titleBackClassDetail(MSG.accessBackClassDetail())}" 
						accesskey="${MSG.accessBackClassDetail()}" 
						type="PreferenceGroup">
						<s:property value="form.classId"/>
					</tt:back>
				</TD>
		</TR>
	</TABLE>
</s:form>
</loc:bundle>
