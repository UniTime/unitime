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
<tt:session-context/>
<script type="text/javascript" src="scripts/block.js"></script>
<script type="text/javascript">
	function enrollmentInformationChecked(checkbox) {
		if (checkbox.checked){
			document.getElementById('instructionalOfferingSearch_form_demand').checked = true;
			document.getElementById('instructionalOfferingSearch_form_projectedDemand').checked = true;
			document.getElementById('instructionalOfferingSearch_form_limit').checked = true;
			document.getElementById('instructionalOfferingSearch_form_snapshotLimit').checked = true;
			document.getElementById('instructionalOfferingSearch_form_roomLimit').checked = true;
		} else {
			document.getElementById('instructionalOfferingSearch_form_demand').checked = false;
			document.getElementById('instructionalOfferingSearch_form_projectedDemand').checked = false;
			document.getElementById('instructionalOfferingSearch_form_limit').checked = false;
			document.getElementById('instructionalOfferingSearch_form_snapshotLimit').checked = false;
			document.getElementById('instructionalOfferingSearch_form_roomLimit').checked = false;
		};
	}
	function dateTimeInformationChecked(checkbox) {
		if (checkbox.checked){
			document.getElementById('instructionalOfferingSearch_form_datePattern').checked = true;
			document.getElementById('instructionalOfferingSearch_form_minPerWk').checked = true;
			document.getElementById('instructionalOfferingSearch_form_timePattern').checked = true;
		} else {
			document.getElementById('instructionalOfferingSearch_form_datePattern').checked = false;
			document.getElementById('instructionalOfferingSearch_form_minPerWk').checked = false;
			document.getElementById('instructionalOfferingSearch_form_timePattern').checked = false;
		};
	}
	function catalogInformationChecked(checkbox) {
		if (checkbox.checked){
			document.getElementById('instructionalOfferingSearch_form_title').checked = true;
			document.getElementById('instructionalOfferingSearch_form_credit').checked = true;
			document.getElementById('instructionalOfferingSearch_form_subpartCredit').checked = true;
			document.getElementById('instructionalOfferingSearch_form_consent').checked = true;
			document.getElementById('instructionalOfferingSearch_form_schedulePrintNote').checked = true;
		} else {
			document.getElementById('instructionalOfferingSearch_form_title').checked = false;
			document.getElementById('instructionalOfferingSearch_form_credit').checked = false;
			document.getElementById('instructionalOfferingSearch_form_subpartCredit').checked = false;
			document.getElementById('instructionalOfferingSearch_form_consent').checked = false;
			document.getElementById('instructionalOfferingSearch_form_schedulePrintNote').checked = false;
		};
	}
</script>
<loc:bundle name="CourseMessages"><s:set var="msg" value="#attr.MSG"/>
<s:form action="instructionalOfferingSearch">
	<table class="unitime-MainTable">
		<TR>
			<TD colspan="6">
				<script type="text/javascript">blToggleHeader('<loc:message name="filter"/>','dispFilter');blStart('dispFilter');</script>
				<TABLE>
					<TR>
						<TD style="min-width: 120px;">
							<B><loc:message name="filterOptionalColumns" /></B>
						</TD>
						<TD colspan="2">
							<s:checkbox name="form.divSec" />
							<loc:message name="columnExternalId"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD colspan="2">
							<s:checkbox name="form.enrollmentInformation" value="1" onclick="enrollmentInformationChecked(this);"/>
							<loc:message name="columnEnrollmentInformation"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD width="10%"></TD>						
						<TD>
							<s:checkbox name="form.demand"  />
							<loc:message name="columnDemand"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>						
						<TD>
							<s:checkbox name="form.projectedDemand" />
							<loc:message name="columnProjectedDemand"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>						
						<TD>
							<s:checkbox name="form.limit" />
							<loc:message name="columnLimit"/>
						</TD>
					</TR>
					<s:if test="form.snapshotLimit != null">
						<TR>
							<TD></TD>
							<TD></TD>
							<TD colspan="2">
								<s:checkbox name="form.snapshotLimit" />
								<loc:message name="columnSnapshotLimit"/>
							</TD>
						</TR>
					</s:if>
					<TR>
						<TD></TD>						
						<TD></TD>
						<TD>
							<s:checkbox name="form.roomLimit" />
							<loc:message name="columnRoomRatio"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD colspan="2">
							<s:checkbox name="form.manager" />
							<loc:message name="columnManager"/>
						</TD>
					</TR>
					<tt:propertyEquals name="unitime.courses.funding_departments_enabled" value="true">
						<TR>
							<TD></TD>
							<TD colspan="2">
								<s:checkbox name="form.fundingDepartment" />
								<loc:message name="columnFundingDepartment"/>
							</TD>
						</TR>
					</tt:propertyEquals><tt:propertyNotEquals name="unitime.courses.funding_departments_enabled" value="true">
						<s:hidden name="form.fundingDepartment"/>
					</tt:propertyNotEquals>
					<TR>
						<TD></TD>
						<TD colspan="2">
							<s:checkbox name="form.dateTimeInformation" value="1" onclick="dateTimeInformationChecked(this);"/>
							<loc:message name="columnDateTimeInformation"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<s:checkbox name="form.datePattern" />
							<loc:message name="columnDatePattern"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<s:checkbox name="form.minPerWk" />
							<loc:message name="columnMinPerWk"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<s:checkbox name="form.timePattern" />
							<loc:message name="columnTimePattern"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD colspan="2">
							<s:checkbox name="form.preferences" />
							<loc:message name="columnPreferences"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD colspan="2">
							<s:checkbox name="form.instructorAssignment" />
							<loc:message name="includeInstructorScheduling"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD colspan="2">
							<s:checkbox name="form.instructor" />
							<loc:message name="columnInstructor"/>
						</TD>
					</TR>
					<s:if test="form.timetable != null">
						<TR>
							<TD></TD>
							<TD colspan="2">
								<s:checkbox name="form.timetable" />
								<loc:message name="columnTimetable"/>
							</TD>
						</TR>
					</s:if>
					<TR>
						<TD></TD>
						<TD colspan="2">
							<s:checkbox name="form.catalogInformation" value="1" onclick="catalogInformationChecked(this);"/>
							<loc:message name="columnCatalogInformation"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<s:checkbox name="form.title" />
							<loc:message name="columnTitle"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<s:checkbox name="form.credit" />
							<loc:message name="columnOfferingCredit"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<s:checkbox name="form.subpartCredit" />
							<loc:message name="columnSubpartCredit"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<s:checkbox name="form.consent" />
							<loc:message name="columnConsent"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<s:checkbox name="form.schedulePrintNote" />
							<loc:message name="columnSchedulePrintNote"/>
						</TD>
					</TR>
					<s:if test="form.lms != null">
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<s:checkbox name="form.lms" />
							<loc:message name="columnLms"/>
						</TD>
					</TR>
					</s:if>
					<TR>
						<TD></TD>
						<TD colspan="2">
							<s:checkbox name="form.note" />
							<loc:message name="columnNote"/>
						</TD>
					</TR>
					<sec:authorize access="hasPermission(null, 'Session', 'Examinations')">
						<TR>
							<TD></TD>
							<TD colspan="2">
								<s:checkbox name="form.exams" />
								<loc:message name="columnExams"/>
							</TD>
						</TR>
					</sec:authorize>
					<s:if test="form.waitlistMode != null">
					<TR>
						<TD></TD>
						<TD colspan="2">
							<s:checkbox name="form.waitlistMode" />
							<loc:message name="columnWaitlistMode"/>
						</TD>
					</TR>
					</s:if>
					<TR>
						<TD>
							<B><loc:message name="filterSortBy"/></B>
						</TD>
						<TD colspan="2">
							<s:select name="form.sortBy" list="form.sortByOptions" style="min-width: 200px;"/>
						</TD>
					</TR>
				</TABLE>
				<script type="text/javascript">blEnd('dispFilter');blStartCollapsed('dispFilter');blEnd('dispFilter');</script>
			</TD>
		</TR>
		<s:if test="form.waitlist != null">
			<TR><TD colspan='6' style='padding-top: 0px;'>
			<span style='min-width: 120px; display: inline-block; padding: 3px;'><B><loc:message name="filterWaitlist"/></B></span>
			<s:select name="form.waitlist" list="#{'A':#msg.itemWaitListAllCourses(), 'W':#msg.itemWaitListWaitListed(), 'N':#msg.itemWaitListNotWaitListed(), 'R':#msg.itemWaitListReschedule(), 'Z':#msg.itemWaitListNotWaitListedReschedule(), 'X':#msg.itemWaitListNotReschedule()}" style="min-width: 200px;" id="waitlistFilter"/>
			</TD></TR>
		</s:if>
		<TR>
			<TH valign="top" nowrap style='padding-top: 12px;'><loc:message name="filterSubject"/></TH>
			<TD valign="top" nowrap style='padding-top: 10px;'>
				<s:if test="form.subjectAreas.size == 1">
					<s:select name="form.subjectAreaIds" id="subjectAreaIds"
						list="form.subjectAreas" listKey="uniqueId" listValue="subjectAreaAbbreviation"/>
				</s:if>
				<s:else>
					<s:select name="form.subjectAreaIds" size="%{form.getSubjectAreaListSize()}" id="subjectAreaIds" multiple="true"
						list="form.subjectAreas" listKey="uniqueId" listValue="subjectAreaAbbreviation"/>
				</s:else>
			</TD>
			<TH valign="top" nowrap style='padding-top: 12px;'><loc:message name="filterCourseNumber"/></TH>
			<TD valign="top" nowrap style='padding-top: 10px;'>
				<tt:course-number name="form.courseNbr" configuration="subjectId=\${subjectAreaIds};notOffered=include;waitlist=\${waitlistFilter}"
					title="%{#msg.tooltipCourseNumber()}" size="15"/>
			</TD>
			<TD valign="top" nowrap style='padding-top: 10px; padding-left: 10px;'>
				<s:submit name='doit' value="%{#msg.actionSearchInstructionalOfferings()}"
						title="%{#msg.titleSearchInstructionalOfferings(#msg.accessSearchInstructionalOfferings())}"
						accesskey="%{#msg.accessSearchInstructionalOfferings()}"/>
				<sec:authorize access="hasPermission(null, 'Department', 'InstructionalOfferingsExportPDF')">
					<s:submit name='doit' value="%{#msg.actionExportPdf()}"
							title="%{#msg.titleExportPdf(#msg.accessExportPdf())}"
							accesskey="%{#msg.accessExportPdf()}"/>
					<s:submit name='doit' value="%{#msg.actionExportCsv()}"
							title="%{#msg.titleExportCsv(#msg.accessExportCsv())}"
							accesskey="%{#msg.accessExportCsv()}"/>
				</sec:authorize>
				<sec:authorize access="hasPermission(null, 'Department', 'InstructionalOfferingsWorksheetPDF')">
					<tt:propertyEquals name="tmtbl.pdf.worksheet" value="true">
						<s:submit name='doit' value="%{#msg.actionWorksheetPdf()}"
								title="%{#msg.titleWorksheetPdf(#msg.accessWorksheetPdf())}"
								accesskey="%{#msg.accessWorksheetPdf()}"/>
					</tt:propertyEquals>
				</sec:authorize>

				<sec:authorize access="hasPermission(null, 'SubjectArea', 'AddCourseOffering')">
					<s:submit name='doit' value="%{#msg.actionAddNewInstructionalOffering()}"
							title="%{#msg.titleAddNewInstructionalOffering(#msg.accessAddNewInstructionalOffering())}"
							accesskey="%{#msg.accessAddNewInstructionalOffering()}"/>
				</sec:authorize>
				
			</TD>
			<TD width="100%" style='padding-top: 10px;'></TD>
		</TR>
		<s:if test="!fieldErrors.isEmpty()">
			<TR><TD colspan="6" align="left" class="errorTable">
				<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror/>
			</TD></TR>
		</s:if>
	</TABLE>
	<s:if test="showTable == true">
		<s:property value="%{printTable()}" escapeHtml="false"/>
	</s:if>
	<s:if test="#request.hash != null">
		<SCRIPT type="text/javascript">
			location.hash = '<%=request.getAttribute("hash")%>';
		</SCRIPT>
	</s:if>
</s:form>
</loc:bundle>