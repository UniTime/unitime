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
<loc:bundle name="CourseMessages"><s:set var="msg" value="#attr.MSG"/>
<s:form action="classSearch">
	<table class="unitime-MainTable">
		<TR>
			<TD colspan="6">
				<script type="text/javascript">blToggleHeader('<loc:message name="filter"/>','dispFilter');blStart('dispFilter');</script>
				<TABLE>
					<TR>
						<TD>
							<B><loc:message name="filterOptionalColumns" /></B>
						</TD>
						<TD>
							<s:checkbox name="form.divSec" />
							<loc:message name="columnExternalId"/>
						</TD>
					</TR>
					<s:if test="form.demandIsVisible == true">
						<TR>
							<TD></TD>
							<TD>
								<s:checkbox name="form.demand" />
								<loc:message name="columnDemand"/>
							</TD>
						</TR>
					</s:if><s:else>
						<s:hidden name="form.demand"/>
					</s:else>
					<s:hidden name="form.demandIsVisible"/>
					<TR>
						<TD></TD>
						<TD>
							<s:checkbox name="form.limit" />
							<loc:message name="columnLimit"/>
						</TD>
					</TR>
					<s:if test="form.snapshotLimit != null">
						<TR>
							<TD></TD>
							<TD colspan="2">
								<s:checkbox name="form.snapshotLimit" />
								<loc:message name="columnSnapshotLimit"/>
							</TD>
						</TR>
					</s:if>
					<TR>
						<TD></TD>
						<TD>
							<s:checkbox name="form.roomLimit" />
							<loc:message name="columnRoomRatio"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD>
							<s:checkbox name="form.manager" />
							<loc:message name="columnManager"/>
						</TD>
					</TR>
					<tt:propertyEquals name="unitime.courses.funding_departments_enabled" value="true">
						<TR>
							<TD></TD>
							<TD>
								<s:checkbox name="form.fundingDepartment" />
								<loc:message name="columnFundingDepartment"/>
							</TD>
						</TR>
					</tt:propertyEquals><tt:propertyNotEquals name="unitime.courses.funding_departments_enabled" value="true">
						<s:hidden name="form.fundingDepartment"/>
					</tt:propertyNotEquals>
					<TR>
						<TD></TD>
						<TD>
							<s:checkbox name="form.datePattern" />
							<loc:message name="columnDatePattern"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD>
							<s:checkbox name="form.timePattern" />
							<loc:message name="columnTimePattern"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD>
							<s:checkbox name="form.preferences" />
							<loc:message name="columnPreferences"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD>
							<s:checkbox name="form.instructorAssignment" />
							<loc:message name="includeInstructorScheduling"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD>
							<s:checkbox name="form.instructor" />
							<loc:message name="columnInstructor"/>
						</TD>
					</TR>
					<s:if test="form.timetable != null">
						<TR>
							<TD></TD>
							<TD>
								<s:checkbox name="form.timetable" />
								<loc:message name="columnTimetable"/>
							</TD>
						</TR>
					</s:if>
					<TR>
						<TD></TD>
						<TD>
							<s:checkbox name="form.schedulePrintNote" />
							<loc:message name="columnSchedulePrintNote"/>
						</TD>
					</TR>
					<s:if test="form.lms != null">
						<TR>
							<TD></TD>
							<TD>
								<s:checkbox name="form.lms" />
								<loc:message name="columnLms"/>
							</TD>
						</TR>
					</s:if>
					<TR>
						<TD></TD>
						<TD>
							<s:checkbox name="form.note" />
							<loc:message name="columnNote"/>
						</TD>
					</TR>
					<sec:authorize access="hasPermission(null, 'Session', 'Examinations')">
						<TR>
							<TD></TD>
							<TD>
								<s:checkbox name="form.exams" />
								<loc:message name="columnExams"/>
							</TD>
						</TR>
					</sec:authorize>
					
					<TR>
						<TD>
							<B><loc:message name="filterManager" /></B>
						</TD>
						<TD>
							<s:select name="form.filterManager" list="managers" listKey="id" listValue="value"/>
						</TD>
					</TR>
					<TR>
						<TD>
							<B><loc:message name="filterInstructionalType"/></B>
						</TD>
						<TD>
							<s:select name="form.filterIType"
								list="#request.itypesList" listKey="itype" listValue="desc"
								headerKey="" headerValue="%{#msg.dropITypeAll()}"/>
						</TD>
					</TR>
					<TR>
						<TD>
							<B><loc:message name="filterInstructor"/></B>
						</TD>
						<TD>
							<s:textfield name="form.filterInstructor" size="25"/>
						</TD>
					</TR>
					<s:if test="form.timetable != null">
						<TR>
							<TD>
								<B><loc:message name="filterAssignedTime"/></B>
							</TD>
							<TD>
								<loc:bundle name="ConstantsMessages" id="CONST">
								<s:checkbox name="form.filterAssignedTimeMon"/>
								<loc:message name="mon" id="CONST"/>&nbsp;
								<s:checkbox name="form.filterAssignedTimeTue"/>
								<loc:message name="tue" id="CONST"/>&nbsp;
								<s:checkbox name="form.filterAssignedTimeWed"/>
								<loc:message name="wed" id="CONST"/>&nbsp;
								<s:checkbox name="form.filterAssignedTimeThu"/>
								<loc:message name="thu" id="CONST"/>&nbsp;
								<s:checkbox name="form.filterAssignedTimeFri"/>
								<loc:message name="fri" id="CONST"/>&nbsp;
								<s:checkbox name="form.filterAssignedTimeSat"/>
								<loc:message name="sat" id="CONST"/>&nbsp;
								<s:checkbox name="form.filterAssignedTimeSun"/>
								<loc:message name="sun" id="CONST"/>&nbsp;
								</loc:bundle>
							</TD>
						</TR>
						<TR>
							<TD></TD>
							<TD>
								<loc:message name="filterTimeFrom"/>
								<s:select name="form.filterAssignedTimeHour" list="form.filterAssignedTimeHours"/>
								:
								<s:select name="form.filterAssignedTimeMin" list="form.filterAssignedTimeMins"/>
								<s:select name="form.filterAssignedTimeAmPm" list="form.filterAssignedTimeAmPms"/>
								&nbsp;&nbsp;
								<loc:message name="filterTimeFor"/>
								<s:textfield name="form.filterAssignedTimeLength" size="3" maxlength="4"/>
								<loc:message name="filterTimeMinutes"/>
							</TD>
						</TR>
						<TR>
							<TD>
								<B><loc:message name="filterAssignedRoom"/></B>
							</TD>
							<TD colspan='2'>
								<s:textfield name="form.filterAssignedRoom" size="25"/>
							</TD>
						</TR>
					</s:if>
					<TR>
						<TD>
							<B><loc:message name="filterSortBy"/></B>
						</TD>
						<TD>
							<s:select name="form.sortBy" list="form.sortByOptions" style="min-width: 200px;"/>
							<BR>
							<s:checkbox name="form.sortByKeepSubparts"/>
							<loc:message name="checkSortWithinSubparts"/>
						</TD>
					</TR>
					<TR>
						<TD>
							<B><loc:message name="filterCrossList"/></B>
						</TD>
						<TD>
							<s:checkbox name="form.showCrossListedClasses"/>
							<loc:message name="showCrossListedClasses"/>
						</TD>
					</TR>
					<TR>
						<TD>
							<B><loc:message name="filterCancelledClasses"/></B>
						</TD>
						<TD>
							<s:checkbox name="form.includeCancelledClasses"/>
							<loc:message name="showCancelledClasses"/>
						</TD>
					</TR>
					<TR>
						<TD>
							<B><loc:message name="filterNeedInstructorAssignment"/></B>
						</TD>
						<TD>
							<s:checkbox name="form.filterNeedInstructor"/>
							<loc:message name="showNeedInstructorClasses"/>
						</TD>
					</TR>
					<TR>
						<TD colspan='2' align='right'>
							<br>
						</TD>
					</TR>
				</TABLE>
				<script type="text/javascript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
				<TABLE>
					<TR>
						<TD colspan='2' align='right'>
							<br>
						</TD>
					</TR>
				</TABLE>
				<script type="text/javascript">blEnd('dispFilter');</script>
			</TD>
		</TR>
		<TR>
			<TD valign="top">
				<b><A id="Search"></A><loc:message name="filterSubject"/></b>
			</TD>
			<TD>
				<s:if test="form.subjectAreas.size == 1">
					<s:select name="form.subjectAreaIds" id="subjectAreaIds"
						list="form.subjectAreas" listKey="uniqueId" listValue="subjectAreaAbbreviation"/>
				</s:if>
				<s:else>
					<s:select name="form.subjectAreaIds" size="%{form.getSubjectAreaListSize()}" id="subjectAreaIds" multiple="true"
						list="form.subjectAreas" listKey="uniqueId" listValue="subjectAreaAbbreviation"/>
				</s:else>
			</TD>
			<TD valign="top" nowrap>
				<B><loc:message name="filterCourseNumber"/></B>
			</TD>
			<TD valign="top">
				<tt:course-number name="form.courseNbr" configuration="subjectId=\${subjectAreaIds};notOffered=exclude"
					title="%{#msg.tooltipCourseNumber()}" size="15"/>
			</TD>
			<TD valign="top" style="padding-left: 10px;" nowrap>
				<s:submit name='doit' value="%{#msg.actionSearchClasses()}"
						title="%{#msg.titleSearchClasses(#msg.accessSearchClasses())}"
						accesskey="%{#msg.accessSearchClasses()}"/>
				<sec:authorize access="hasPermission(null, 'Department', 'ClassesExportPDF')">
					<s:submit name='doit' value="%{#msg.actionExportPdf()}"
							title="%{#msg.titleExportPdf(#msg.accessExportPdf())}"
							accesskey="%{#msg.accessExportPdf()}"/>
					<s:submit name='doit' value="%{#msg.actionExportCsv()}"
							title="%{#msg.titleExportCsv(#msg.accessExportCsv())}"
							accesskey="%{#msg.accessExportCsv()}"/>
				</sec:authorize>
			</TD>
			<TD width='100%'></TD>
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