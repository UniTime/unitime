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
<tt:confirm name="confirmMakeOffered"><loc:message name="confirmMakeOffered"/></tt:confirm>
<tt:confirm name="confirmMakeNotOffered"><loc:message name="confirmMakeNotOffered"/></tt:confirm>
<tt:confirm name="confirmDelete"><loc:message name="confirmDeleteIO"/></tt:confirm>
	<table class="unitime-MainTable">
		<s:form action="instructionalOfferingDetail">
			<s:hidden name="form.instrOfferingId"/>
			<s:hidden name="form.ctrlCrsOfferingId"/>
			<s:hidden name="form.nextId"/>
			<s:hidden name="form.previousId"/>
			<s:hidden name="form.catalogLinkLabel"/>
			<s:hidden name="form.catalogLinkLocation"/>
			<s:hidden name="form.crsOfferingId" id="courseOfferingId"/>
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<A title="${MSG.titleBackToIOList(MSG.accessBackToIOList())}" 
							accesskey="${MSG.accessBackToIOList()}" class="l8"
							href="instructionalOfferingSearch.action?doit=Search&loadInstrFilter=1&subjectAreaIds=${form.subjectAreaId}&courseNbr=${crsNbr}#A${form.instrOfferingId}"><s:property value="form.instrOfferingName"/></A>
					</tt:section-title>						
					<sec:authorize access="hasPermission(#form.instrOfferingId, 'InstructionalOffering', 'OfferingCanLock')">
						<s:submit name='op' value='%{#msg.actionLockIO()}'
							accesskey='%{#msg.accessLockIO()}' title='%{#msg.titleLockIO(#msg.accessLockIO())}'
							onclick='%{#msg.jsSubmitLockIO(#form.instrOfferingName)}'/>
					</sec:authorize>
					 <sec:authorize access="hasPermission(#form.instrOfferingId, 'InstructionalOffering', 'OfferingCanUnlock')">
					 	<s:submit name='op' value='%{#msg.actionUnlockIO()}'
							accesskey='%{#msg.accessUnlockIO()}' title='%{#msg.titleUnlockIO(#msg.accessUnlockIO())}'
							onclick='%{#msg.jsSubmitUnlockIO(#form.instrOfferingName)}'/>
					</sec:authorize>
					<sec:authorize access="hasPermission(#form.instrOfferingId, 'InstructionalOffering', 'InstrOfferingConfigAdd')">
						<s:submit name='op' value='%{#msg.actionAddConfiguration()}'
							accesskey='%{#msg.accessAddConfiguration()}' title='%{#msg.titleAddConfiguration(#msg.accessAddConfiguration())}'/>
					</sec:authorize>
					<sec:authorize access="hasPermission(#form.instrOfferingId, 'InstructionalOffering', 'InstructionalOfferingCrossLists')">
						<s:submit name='op' value='%{#msg.actionCrossLists()}'
							accesskey='%{#msg.accessCrossLists()}' title='%{#msg.titleCrossLists(#msg.accessCrossLists())}'/>
					</sec:authorize>
					<sec:authorize access="hasPermission(#form.instrOfferingId, 'InstructionalOffering', 'OfferingMakeOffered')">
						<s:submit name='op' value='%{#msg.actionMakeOffered()}'
							accesskey='%{#msg.accessMakeOffered()}' title='%{#msg.titleMakeOffered(#msg.accessMakeOffered())}'
							onclick="return confirmMakeOffered();"/>
					</sec:authorize>
					<sec:authorize access="hasPermission(#form.instrOfferingId, 'InstructionalOffering', 'OfferingDelete')">
						<s:submit name='op' value='%{#msg.actionDeleteIO()}'
							accesskey='%{#msg.accessDeleteIO()}' title='%{#msg.titleDeleteIO(#msg.accessDeleteIO())}'
							onclick="return confirmDelete();"/>
					</sec:authorize>
					<sec:authorize access="hasPermission(#form.instrOfferingId, 'InstructionalOffering', 'OfferingMakeNotOffered')">
						<s:submit name='op' value='%{#msg.actionMakeNotOffered()}'
							accesskey='%{#msg.accessMakeNotOffered()}' title='%{#msg.titleMakeNotOffered(#msg.accessMakeNotOffered())}'
							onclick="return confirmMakeNotOffered();"/>
					</sec:authorize>
					<s:if test="form.previousId != null">
						<s:submit name='op' value='%{#msg.actionPreviousIO()}'
							accesskey='%{#msg.accessPreviousIO()}' title='%{#msg.titlePreviousIO(#msg.accessPreviousIO())}'/>
					</s:if>
					<s:if test="form.nextId != null">
						<s:submit name='op' value='%{#msg.actionNextIO()}'
							accesskey='%{#msg.accessNextIO()}' title='%{#msg.titleNextIO(#msg.accessNextIO())}'/>
					</s:if>
					<tt:back styleClass="btn" 
							name="${MSG.actionBackIODetail()}" 
							title="${MSG.titleBackIODetail(MSG.accessBackIODetail())}" 
							accesskey="${MSG.accessBackIODetail()}" 
							type="InstructionalOffering">
						<s:property value="form.instrOfferingId"/>
					</tt:back>
				</tt:section-header>					
			</TD>
		</TR>
		
		<s:if test="!fieldErrors.isEmpty()">
			<TR><TD colspan="2" align="left" class="errorTable">
				<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror/>
			</TD></TR>
		</s:if>

		<TR>
			<TD width="20%" valign="top"><loc:message name="propertyCourseOfferings"/></TD>
			<TD>
				<div class='unitime-ScrollTableCell'>
				<TABLE style="border-spacing:0px; width: 100%;">
					<TR>
						<TD align="center" class="WebTableHeader">&nbsp;</TD>
						<s:if test="form.hasCourseTypes == true">
							<TD align="left" class="WebTableHeader"><loc:message name="columnCourseType"/></TD>
						</s:if>
						<TD align="left" class="WebTableHeader"><loc:message name="columnTitle"/></TD>
						<s:if test="form.hasCourseExternalId == true">
							<TD align="left" class="WebTableHeader"><loc:message name="columnExternalId"/></TD>
						</s:if>
						<s:if test="form.hasCourseReservation == true">
							<TD align="left" class="WebTableHeader"><loc:message name="columnReserved"/></TD>
						</s:if>
						<s:if test="form.hasCredit == true">
							<TD align="left" class="WebTableHeader"><loc:message name="columnCredit"/></TD>
						</s:if>
						<s:if test="form.hasScheduleBookNote == true">
							<TD align="left" class="WebTableHeader"><loc:message name="columnScheduleOfClassesNote"/></TD>
						</s:if>
						<s:if test="form.hasDemandOfferings == true">
							<TD align="left" class="WebTableHeader"><loc:message name="columnDemandsFrom"/></TD>
						</s:if>
						<s:if test="form.hasAlternativeCourse == true">
							<TD align="left" class="WebTableHeader"><loc:message name="columnAlternativeCourse"/></TD>
						</s:if>
						<TD align="left" class="WebTableHeader"><loc:message name="columnConsent"/></TD>
						<s:if test="form.hasDisabledOverrides == true">
							<TD align="left" class="WebTableHeader"><loc:message name="columnDisabledOverrides"/></TD>
						</s:if>
						<tt:hasProperty name="unitime.custom.CourseUrlProvider">
						<TD align="left" class="WebTableHeader"><loc:message name="columnCourseCatalog"/></TD>
						</tt:hasProperty>
						<TD align="center" class="WebTableHeader">&nbsp;</TD>
					</TR>
				<s:iterator value="form.courseOfferings" var="cx">
					<TR>
						<TD align="center" class="BottomBorderGray">
							&nbsp;
							<s:if test="#cx.isControl == true">
								<IMG src="images/accept.png" alt="${MSG.altControllingCourse()}" title="${MSG.titleControllingCourse()}" border="0">
							</s:if>
							&nbsp;
						</TD>
						<s:if test="form.hasCourseTypes == true">
							<TD class="BottomBorderGray">
								<s:if test="#cx.courseType != null">
									<span title='${cx.courseType.label}'><s:property value="#cx.courseType.reference"/></span>
								</s:if>
							</TD>
						</s:if>
						<TD class="BottomBorderGray"><s:property value="#cx.courseNameWithTitle"/></TD>
						<s:if test="form.hasCourseExternalId == true">
							<TD class="BottomBorderGray">
								<s:if test="#cx.externalUniqueId != null">
									<s:property value="#cx.externalUniqueId"/>
								</s:if>
							</TD>
						</s:if>
						<s:if test="form.hasCourseReservation == true">
							<TD class="BottomBorderGray">
								<s:if test="#cx.reservation != null">
									<s:property value="#cx.reservation"/>
								</s:if>
							</TD>
						</s:if>
						<s:if test="form.hasCredit == true">
							<TD class="BottomBorderGray">
								<s:if test="#cx.credit != null">
									<span title='${cx.credit.creditText()}'><s:property value="#cx.credit.creditAbbv()"/></span>
								</s:if>
							</TD>
						</s:if>
						<s:if test="form.hasScheduleBookNote == true">
							<TD class="BottomBorderGray" style="white-space: pre-wrap;"><s:property value="#cx.scheduleBookNote" escapeHtml="false"/></TD>
						</s:if>
						<s:if test="form.hasDemandOfferings == true">
							<TD class="BottomBorderGray">&nbsp;
								<s:if test="#cx.demandOffering != null">
									<s:property value="#cx.demandOffering.courseName"/>
								</s:if>
							</TD>
						</s:if>
						<s:if test="form.hasAlternativeCourse == true">
							<TD class="BottomBorderGray">&nbsp;
								<s:if test="#cx.alternativeOffering != null">
									<s:property value="#cx.alternativeOffering.courseName"/>
								</s:if>
							</TD>
						</s:if>
						<TD class="BottomBorderGray">
							<s:if test="#cx.consentType == null">
								<loc:message name="noConsentRequired"/>
							</s:if>
							<s:else>
								<s:property value="#cx.consentType.abbv"/>
							</s:else>
						</TD>
						<s:if test="form.hasDisabledOverrides == true">
							<TD class="BottomBorderGray">
								<s:iterator value="#cx.disabledOverrides" var="override" status="stat">
									<span title='${override.label}'><s:property value="#override.reference"/></span><s:if test="!#stat.last">, </s:if>
								</s:iterator>
							</TD>
						</s:if>
						<tt:hasProperty name="unitime.custom.CourseUrlProvider">
							<TD class="BottomBorderGray">
								<span name='UniTimeGWT:CourseLink' style="display: none;"><s:property value="#cx.uniqueId"/></span>
							</TD>
						</tt:hasProperty>
						<TD align="right" class="BottomBorderGray">
							<sec:authorize access="hasPermission(#cx, 'EditCourseOffering') or hasPermission(#cx, 'EditCourseOfferingNote') or hasPermission(#cx, 'EditCourseOfferingCoordinators')">
								<s:submit name='op' value='%{#msg.actionEditCourseOffering()}' title='%{#msg.titleEditCourseOffering()}'
									onclick="document.getElementById('courseOfferingId').value = '%{#cx.uniqueId}'; return true;"
								/>
							</sec:authorize>
						</TD>
					</TR>
				</s:iterator>
				</TABLE>
				</div>
			</TD>
		</TR>
		
		<TR>
			<TD><loc:message name="propertyEnrollment"/> </TD>
			<TD>
				<s:property value="form.enrollment"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propertyLastEnrollment"/> </TD>
			<TD>
				<s:if test="form.demand == 0">-</s:if>
				<s:else><s:property value="form.demand"/></s:else>
			</TD>
		</TR>

		<s:if test="form.projectedDemand != 0">
			<TR>
				<TD><loc:message name="propertyProjectedDemand"/> </TD>
				<TD>
					<s:property value="form.projectedDemand"/>
				</TD>
			</TR>
		</s:if>

		<TR>
			<TD><loc:message name="propertyOfferingLimit"/> </TD>
			<TD>
				<s:if test="form.unlimited == false">
					<s:property value="form.limit"/>
					<s:if test="#request.limitsDoNotMatch != null">
						&nbsp;
						<img src='images/cancel.png' alt='${MSG.altLimitsDoNotMatch()}' title='${MSG.titleLimitsDoNotMatch()}' border='0' align='top'>
						<font color="#FF0000"><loc:message name="errorReservedSpacesForOfferingsTotal"><s:property value="#request.limitsDoNotMatch"/></loc:message></font>
					</s:if>
					<s:elseif test="#request.configsWithTooHighLimit != null">
						&nbsp;
						<img src='images/cancel.png' alt='${MSG.altLimitsDoNotMatch()}' title='${MSG.titleLimitsDoNotMatch()}' border='0' align='top'>
						<font color="#FF0000"><s:property value="#request.configsWithTooHighLimit"/></font>
					</s:elseif>
				</s:if>
				<s:else>
					<span title="${MSG.titleUnlimitedEnrollment()}"><font size="+1">&infin;</font></span>
				</s:else>
			</TD>
		</TR>
		<s:if test="form.unlimited == false && form.snapshotLimit != null">
		<TR>
			<TD><loc:message name="propertySnapshotLimit"/> </TD>
			<TD><s:property value="form.snapshotLimit"/></TD>
		</TR>
		</s:if>
		<s:if test="form.byReservationOnly == true">
			<TR>
				<TD><loc:message name="propertyByReservationOnly"/></TD>
				<TD>
					<IMG src="images/accept.png" alt="ENABLED" title="${MSG.descriptionByReservationOnly2()}" border="0">
					<i><loc:message name="descriptionByReservationOnly2"/></i>
				</TD>
			</TR>
		</s:if>
		<s:if test="form.coordinators != null && !form.coordinators.isEmpty()">
			<TR>
				<TD valign="top"><loc:message name="propertyCoordinators"/></TD>
				<TD>
					<s:property value="form.coordinators" escapeHtml="false"/>
				</TD>
			</TR>
		</s:if>
		
		<s:if test="form.wkEnroll != null && !form.wkEnroll.isEmpty()">
			<TR>
				<TD valign="top"><loc:message name="propertyLastWeekEnrollment"/></TD>
				<TD>
					<loc:message name="textLastWeekEnrollment"><s:property value="form.wkEnroll"/></loc:message>
				</TD>
			</TR>
		</s:if>
		
		<s:if test="form.wkChange != null && !form.wkChange.isEmpty()">
			<TR>
				<TD valign="top"><loc:message name="propertyLastWeekChange"/></TD>
				<TD>
					<loc:message name="textLastWeekChange"><s:property value="form.wkChange"/></loc:message>
				</TD>
			</TR>
		</s:if>

		<s:if test="form.wkDrop != null && !form.wkDrop.isEmpty()">
			<TR>
				<TD valign="top"><loc:message name="propertyLastWeekDrop"/></TD>
				<TD>
					<loc:message name="textLastWeekDrop"><s:property value="form.wkDrop"/></loc:message>
				</TD>
			</TR>
		</s:if>
		
		<s:if test="form.displayEnrollmentDeadlineNote == true">
			<TR>
				<TD valign="top">&nbsp;</TD>
				<TD>
					<i><loc:message name="descriptionEnrollmentDeadlines"><s:property value="form.weekStartDayOfWeek"/></loc:message></i>
				</TD>
			</TR>
		</s:if>
		
		<s:if test="form.waitList != null && !form.waitList.isEmpty()">
			<TR>
				<TD valign="top"><loc:message name="propertyWaitListing"/></TD>
				<TD>
					<s:if test="form.waitList == 'WaitList'">
						<IMG src="images/accept.png" alt="${MSG.waitListEnabled()}" title="${MSG.descWaitListEnabled()}" border="0" align="top">
						<loc:message name="descWaitListEnabled"/>
					</s:if>
					<s:elseif test="form.waitList == 'ReSchedule'">
						<IMG src="images/accept_gold.png" alt="${MSG.waitListReschedule()}" title="${MSG.descWaitListReschedule()}" border="0" align="top">
						<loc:message name="descWaitListReschedule"/>
					</s:elseif>
					<s:elseif test="form.waitList == 'Disabled'">
						<img src="images/cancel.png" alt="${MSG.waitListDisabled()}" title="${MSG.descWaitListDisabled()}" border="0" align="top">
						<loc:message name="descWaitListDisabled"/>
					</s:elseif>
					<s:if test="#request.waitlistProblem != null">
						<br>
						<s:set var="waitlistProblem" value="#request.waitlistProblem"/>
						<img src='images/warning.png' alt='WARNING' border='0' align='top' title="${waitlistProblem}">
						<font color="#FF0000"><s:property value="#request.waitlistProblem" escapeHtml="false"/></font>
					</s:if>
				</TD>
			</TR>
		</s:if>

		<s:if test="form.catalogLinkLabel != null">
		<TR>
			<TD><loc:message name="propertyCourseCatalog"/> </TD>
			<TD>
				<A href="${form.catalogLinkLocation}" target="_blank"><s:property value="form.catalogLinkLabel"/></A>
			</TD>
		</TR>
		</s:if>
		<s:if test="form.accommodation != null && !form.accommodation.isEmpty()">
			<TR>
				<TD valign="top"><loc:message name="propertyAccommodations"/></TD>
				<TD>
					<s:property value="form.accommodation" escapeHtml="false"/>
				</TD>
			</TR>
		</s:if>
		<s:if test="form.hasConflict == true">
			<TR>
				<TD></TD>
				<TD>
					<IMG src="images/warning.png" alt="WARNING" title="${MSG.warnOfferingHasConflictingClasses()}" border="0">
					<font color="#FF0000"><loc:message name="warnOfferingHasConflictingClasses"/></font>
				</TD>
			</TR>
		</s:if>
		<s:if test="form.notes != null && !form.notes.isEmpty()">
			<TR>
				<TD valign="top"><loc:message name="propertyRequestsNotes"/></TD>
				<TD>
					<div class='unitime-ScrollTableCell'>
						<span style='white-space: pre-wrap;'><s:property value="form.notes" escapeHtml="false"/></span>
					</div>
				</TD>
			</TR>
		</s:if>
		<tt:propertyEquals name="unitime.courses.funding_departments_enabled" value="true">
			<s:if test="form.fundingDepartment != null">
				<TR>
					<TD valign="top"><loc:message name="propertyFundingDepartment"/></TD>
					<TD>
						<s:property value="form.fundingDepartment"/>
					</TD>
				</TR>
			</s:if>
		</tt:propertyEquals>
		
		<s:if test="form.instructorSurvey == true">
			<TR><TD colspan="2">
				<div id='UniTimeGWT:InstructorSurveyOffering' style="display: none;"><s:property value="form.instrOfferingId"/></div>
			</TD></TR>
		</s:if>
		
		<sec:authorize access="hasPermission(null, 'Session', 'CurriculumView')">
		<TR>
			<TD colspan="2">
				<div id='UniTimeGWT:CourseCurricula' style="display: none;"><s:property value="form.instrOfferingId"/></div>
			</TD>
		</TR>
		</sec:authorize>
		
		<sec:authorize access="hasPermission(null, 'Department', 'Reservations')">
		<TR>
			<TD colspan="2">
				<a id="reservations"></a>
				<sec:authorize access="hasPermission(#form.instrOfferingId, 'InstructionalOffering', 'ReservationOffering') and hasPermission(null, null, 'ReservationAdd')">
					<div id='UniTimeGWT:OfferingReservations' style="display: none;"><s:property value="form.instrOfferingId"/></div>
				</sec:authorize>
				<sec:authorize access="not hasPermission(#form.instrOfferingId, 'InstructionalOffering', 'ReservationOffering') or not hasPermission(null, null, 'ReservationAdd')">
					<div id='UniTimeGWT:OfferingReservationsRO' style="display: none;"><s:property value="form.instrOfferingId"/></div>
				</sec:authorize>
			</TD>
		</TR>
		</sec:authorize>

		<TR>
			<TD colspan="2" >&nbsp;</TD>
		</TR>
		</s:form>

<!-- Configuration -->
		<TR>
			<TD colspan="2" valign="middle">
				<s:if test="form.instrOfferingId != null">
					<s:property value="%{printTable()}" escapeHtml="false"/>
				</s:if>
			</TD>
		</TR>

		<TR>
			<TD valign="middle" colspan='3' align='left'>
				<tt:displayPrefLevelLegend/>
			</TD>
		</TR>
		
		<s:if test="#request.distPrefs != null">
			<TR>
				<TD colspan="2" >&nbsp;</TD>
			</TR>
	
			<TR>
				<TD colspan="2">
					<TABLE style="border-spacing:0px; width: 100%;">
						<s:property value="#request.distPrefs" escapeHtml="false"/>
					</TABLE>
				</TD>
			</TR>
		</s:if>
		
		<s:form action="instructionalOfferingDetail">
			<s:hidden name="form.instrOfferingId"/>
			<s:hidden name="form.ctrlCrsOfferingId"/>
			<s:hidden name="form.nextId"/>
			<s:hidden name="form.previousId"/>
			<s:hidden name="form.catalogLinkLabel"/>
			<s:hidden name="form.catalogLinkLocation"/>
			<s:hidden name="form.crsOfferingId" id="courseOfferingId"/>
		
		<s:if test="form.notOffered == false && form.teachingRequests == true">
			<sec:authorize access="hasPermission(null, 'SolverGroup', 'InstructorScheduling') and hasPermission(null, 'Department', 'InstructorAssignmentPreferences')">
			<TR>
				<TD colspan="2">
					<a id="instructors"></a>
					<div id='UniTimeGWT:TeachingRequests' style="display: none;"><s:property value="form.instrOfferingId"/></div>
				</TD>
			</TR>
			</sec:authorize>
		</s:if>

		<s:if test="form.notOffered == false">
		<TR>
			<TD colspan="2">
				<tt:exams type='InstructionalOffering' add='true'>
					<s:property value="form.instrOfferingId"/>
				</tt:exams>
			</TD>
		</TR>
		</s:if>
		
		<tt:last-change type='InstructionalOffering'>
			<s:property value="form.instrOfferingId"/>
		</tt:last-change>		

		<s:if test="form.notOffered == false">
			<TR>
				<TD colspan="2">
					<div id='UniTimeGWT:OfferingEnrollments' style="display: none;"><s:property value="form.instrOfferingId"/></div>
				</TD>
			</TR>
		</s:if>

<!-- Buttons -->
		<TR>
			<TD colspan="2" valign="middle">
				<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="right">
					<sec:authorize access="hasPermission(#form.instrOfferingId, 'InstructionalOffering', 'OfferingCanLock')">
						<s:submit name='op' value='%{#msg.actionLockIO()}'
							accesskey='%{#msg.accessLockIO()}' title='%{#msg.titleLockIO(#msg.accessLockIO())}'
							onclick='%{#msg.jsSubmitLockIO(#form.instrOfferingName)}'/>
					</sec:authorize>
					 <sec:authorize access="hasPermission(#form.instrOfferingId, 'InstructionalOffering', 'OfferingCanUnlock')">
					 	<s:submit name='op' value='%{#msg.actionUnlockIO()}'
							accesskey='%{#msg.accessUnlockIO()}' title='%{#msg.titleUnlockIO(#msg.accessUnlockIO())}'
							onclick='%{#msg.jsSubmitUnlockIO(#form.instrOfferingName)}'/>
					</sec:authorize>
					<sec:authorize access="hasPermission(#form.instrOfferingId, 'InstructionalOffering', 'InstrOfferingConfigAdd')">
						<s:submit name='op' value='%{#msg.actionAddConfiguration()}'
							accesskey='%{#msg.accessAddConfiguration()}' title='%{#msg.titleAddConfiguration(#msg.accessAddConfiguration())}'/>
					</sec:authorize>
					<sec:authorize access="hasPermission(#form.instrOfferingId, 'InstructionalOffering', 'InstructionalOfferingCrossLists')">
						<s:submit name='op' value='%{#msg.actionCrossLists()}'
							accesskey='%{#msg.accessCrossLists()}' title='%{#msg.titleCrossLists(#msg.accessCrossLists())}'/>
					</sec:authorize>
					<sec:authorize access="hasPermission(#form.instrOfferingId, 'InstructionalOffering', 'OfferingMakeOffered')">
						<s:submit name='op' value='%{#msg.actionMakeOffered()}'
							accesskey='%{#msg.accessMakeOffered()}' title='%{#msg.titleMakeOffered(#msg.accessMakeOffered())}'
							onclick="return confirmMakeOffered();"/>
					</sec:authorize>
					<sec:authorize access="hasPermission(#form.instrOfferingId, 'InstructionalOffering', 'OfferingDelete')">
						<s:submit name='op' value='%{#msg.actionDeleteIO()}'
							accesskey='%{#msg.accessDeleteIO()}' title='%{#msg.titleDeleteIO(#msg.accessDeleteIO())}'
							onclick="return confirmDelete();"/>
					</sec:authorize>
					<sec:authorize access="hasPermission(#form.instrOfferingId, 'InstructionalOffering', 'OfferingMakeNotOffered')">
						<s:submit name='op' value='%{#msg.actionMakeNotOffered()}'
							accesskey='%{#msg.accessMakeNotOffered()}' title='%{#msg.titleMakeNotOffered(#msg.accessMakeNotOffered())}'
							onclick="return confirmMakeNotOffered();"/>
					</sec:authorize>
					<s:if test="form.previousId != null">
						<s:submit name='op' value='%{#msg.actionPreviousIO()}'
							accesskey='%{#msg.accessPreviousIO()}' title='%{#msg.titlePreviousIO(#msg.accessPreviousIO())}'/>
					</s:if>
					<s:if test="form.nextId != null">
						<s:submit name='op' value='%{#msg.actionNextIO()}'
							accesskey='%{#msg.accessNextIO()}' title='%{#msg.titleNextIO(#msg.accessNextIO())}'/>
					</s:if>
					<tt:back styleClass="btn" 
							name="${MSG.actionBackIODetail()}" 
							title="${MSG.titleBackIODetail(MSG.accessBackIODetail())}" 
							accesskey="${MSG.accessBackIODetail()}" 
							type="InstructionalOffering">
						<s:property value="form.instrOfferingId"/>
					</tt:back>			
			</TD>
		</TR>
		</s:form>
	</TABLE>
</loc:bundle>