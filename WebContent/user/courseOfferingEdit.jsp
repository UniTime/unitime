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
<s:form action="courseOfferingEdit">
	<s:hidden name="form.instrOfferingId"/>
	<s:hidden name="form.courseOfferingId"/>
	<s:hidden name="form.add"/>
	<s:hidden name="form.isControl"/>
	<s:hidden name="form.courseName"/>
	<s:hidden name="form.ioNotOffered"/>
	<s:hidden name="form.defaultTeachingResponsibilityId"/>
	
	<TABLE class="unitime-MainTable">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<s:if test="form.add != true">
							<A  title="${MSG.titleBackToIOList(MSG.accessBackToIOList())}"
								accesskey="${MSG.accessBackToIOList()}"
								class="l8"
								href="instructionalOfferingSearch.action?doit=Search&loadInstrFilter=1&subjectAreaIds=${form.subjectAreaId}&courseNbr=${crsNbr}#A${form.instrOfferingId}"
								><s:property value="form.courseName" /></A>
						</s:if>
					</tt:section-title>
					<s:if test="form.add == true">
						<s:submit accesskey='%{#msg.accessSaveCourseOffering()}' name='op' value='%{#msg.actionSaveCourseOffering()}'
							title='%{#msg.titleSaveCourseOffering(#msg.accessSaveCourseOffering())}'/>
						<s:submit accesskey='%{#msg.accessBackToIOList()}' name='op' value='%{#msg.actionBackToIOList()}'
							title='%{#msg.titleBackToIOList(#msg.accessBackToIOList())}'/>
					</s:if>
					<s:else>
						<s:submit accesskey='%{#msg.accessUpdateCourseOffering()}' name='op' value='%{#msg.actionUpdateCourseOffering()}'
							title='%{#msg.titleUpdateCourseOffering(#msg.accessUpdateCourseOffering())}'/>
						<s:submit accesskey='%{#msg.accessBackToIODetail()}' name='op' value='%{#msg.actionBackToIODetail()}'
							title='%{#msg.titleBackToIODetail(#msg.accessBackToIODetail())}'/>
					</s:else>
				</tt:section-header>
			</TD>
		</TR>
		<s:if test="!fieldErrors.isEmpty()">
			<TR><TD colspan="2" align="left" class="errorTable">
				<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror/>
			</TD></TR>
		</s:if>
		
	<s:if test="form.add == true">
		<TR>
			<TD><loc:message name="filterSubject"/> </TD>
			<TD>
				<s:select name="form.subjectAreaId"
					list="#request.subjects" listKey="uniqueId" listValue="subjectAreaAbbreviation"
					onchange="submit();" id="subjectId"/>
			</TD>
		</TR>
	</s:if><s:else>
		<s:hidden name="form.subjectAreaId" id="subjectId"/>
	</s:else>
	
		
	<sec:authorize access="(not #form.add and hasPermission(#form.courseOfferingId, 'CourseOffering', 'EditCourseOffering')) or 
						(#form.add and hasPermission(#form.subjectAreaId, 'SubjectArea', 'AddCourseOffering'))">
		<TR>
			<TD><loc:message name="filterCourseNumber"/> </TD>
			<TD>
				<s:textfield name="form.courseNbr" size="40" maxlength="40" id="course" />
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="propertyCourseTitle"/> </TD>
			<TD>
				<s:textfield name="form.title" size="100" maxlength="200" />
			</TD>
		</TR>
		<tt:propertyEquals name="unitime.course.editExternalIds" value="true">
			<TR>
				<TD><loc:message name="propertyExternalId"/> </TD>
				<TD>
					<s:textfield name="form.externalId" maxlength="40" size="20"/>
				</TD>
			</TR>
		</tt:propertyEquals>
		<tt:propertyNotEquals name="unitime.course.editExternalIds" value="true">
			<s:if test="form.externalId != null && !form.externalId.isEmpty()">
				<TR>
					<TD><loc:message name="propertyExternalId"/> </TD>
					<TD>
						<s:property value="form.externalId"/>
					</TD>
				</TR>
			</s:if>
			<s:hidden name="form.externalId"/>
		</tt:propertyNotEquals>
	</sec:authorize>
	<sec:authorize access="!(not #form.add and hasPermission(#form.courseOfferingId, 'CourseOffering', 'EditCourseOffering')) and
							!(#form.add and hasPermission(#form.subjectAreaId, 'SubjectArea', 'AddCourseOffering'))">
		<s:hidden name="form.courseNbr" id="course"/>
		<s:if test="form.title != null && !form.title.isEmpty()">
			<TR>
				<TD><loc:message name="propertyCourseTitle"/> </TD>
				<TD>
					<s:property value="form.title"/>				
				</TD>
			</TR>
		</s:if>
		<s:hidden name="form.title"/>
		<s:if test="form.externalId != null && !form.externalId.isEmpty()">
			<TR>
				<TD><loc:message name="propertyExternalId"/> </TD>
				<TD>
					<s:property value="form.externalId"/>
				</TD>
			</TR>
		</s:if>
		<s:hidden name="form.externalId"/>
	</sec:authorize>
	
	<s:if test="#request.courseTypes != null && !#request.courseTypes.isEmpty()">
		<sec:authorize access="(not #form.add and hasPermission(#form.courseOfferingId, 'CourseOffering', 'EditCourseOffering')) or 
							(#form.add and hasPermission(#form.subjectAreaId, 'SubjectArea', 'AddCourseOffering'))">
			<TR>
				<TD><loc:message name="propertyCourseType"/></TD>
				<TD>
					<s:select name="form.courseTypeId"
						 list="#request.courseTypes" listKey="uniqueId" listValue="label"
						 headerKey="" headerValue=""/>
				</TD>
			</TR>
		</sec:authorize>
		<sec:authorize access="!(not #form.add and hasPermission(#form.courseOfferingId, 'CourseOffering', 'EditCourseOffering')) and
								!(#form.add and hasPermission(#form.subjectAreaId, 'SubjectArea', 'AddCourseOffering'))">
			<s:hidden name="form.courseTypeId"/>
			<s:if test="form.courseTypeId != null">
				<s:iterator value="#request.courseTypes" var="type">
					<s:if test="#type.uniqueId == form.courseTypeId">
						<TR>
							<TD><loc:message name="propertyCourseType"/></TD>
							<TD><s:property value="#type.label"/></TD>
						</TR>
					</s:if>
				</s:iterator>
			</s:if>
		</sec:authorize>
	</s:if>

	<sec:authorize access="(not #form.add and (hasPermission(#form.courseOfferingId, 'CourseOffering', 'EditCourseOffering') or
							 hasPermission(#form.courseOfferingId, 'CourseOffering', 'EditCourseOfferingNote')))
						or (#form.add and hasPermission(#form.subjectAreaId, 'SubjectArea', 'AddCourseOffering'))">
		<TR>
			<TD valign="top"><loc:message name="propertyScheduleOfClassesNote"/> </TD>
			<TD>
				<s:textarea name="form.scheduleBookNote" rows="4" cols="57" />
			</TD>
		</TR>
	</sec:authorize>
	<sec:authorize access="!(not #form.add and (hasPermission(#form.courseOfferingId, 'CourseOffering', 'EditCourseOffering') or
							 hasPermission(#form.courseOfferingId, 'CourseOffering', 'EditCourseOfferingNote')))
						and !(#form.add and hasPermission(#form.subjectAreaId, 'SubjectArea', 'AddCourseOffering'))">
		<s:if test="form.scheduleBookNote != null && !form.scheduleBookNote.isEmpty()">
			<TR>
				<TD valign="top"><loc:message name="propertyScheduleOfClassesNote"/> </TD>
				<TD>
					<s:property value="form.scheduleBookNote" escapeHtml="false"/>
				</TD>
			</TR>
		</s:if>
		<s:hidden name="form.scheduleBookNote"/>
	</sec:authorize>
	
		
	<sec:authorize access="(not #form.add and hasPermission(#form.courseOfferingId, 'CourseOffering', 'EditCourseOffering')) or 
						(#form.add and hasPermission(#form.subjectAreaId, 'SubjectArea', 'AddCourseOffering'))">
		<TR>
			<TD valign="top"><loc:message name="propertyConsent" /></TD>
			<TD>
				<s:select name="form.consent" 
					list="#request.consentTypeList" listKey="uniqueId" listValue="label"
					headerKey="-1" headerValue="%{#msg.noConsentRequired()}"/>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="propertyCredit"/></TD>
			<TD>
				<s:select name="form.creditFormat"
					list="#request.courseCreditFormatList" listKey="reference" listValue="label"
					headerKey="" headerValue="%{#msg.itemSelect()}"
					style="width:200px;" onchange="creditFormatChanged(this);"
					/>
			</TD>
		</TR>
		<TR>
			<TD> &nbsp;</TD>
			<TD>
				<table>
				<tr>
				<td nowrap><loc:message name="propertyCreditType"/> </td>
				<td>
					<s:select name="form.creditType"
						list="#request.courseCreditTypeList" listKey="uniqueId" listValue="label"
						style="width:200px;" disabled="%{form.creditFormat == null || form.creditFormat.isEmpty()}"/>
				</td>
				</tr>
				<tr>
				<td nowrap><loc:message name="propertyCreditUnitType"/></td>
				<td>
				<s:select name="form.creditUnitType"
					list="#request.courseCreditUnitTypeList" listKey="uniqueId" listValue="label" 
					style="width:200px;" disabled="%{form.creditFormat == null || form.creditFormat.isEmpty()}"/>
				</td>
				</tr>
				<tr>
				<td nowrap><loc:message name="propertyUnits"/> </td>
				<td>
				<s:textfield name="form.units" maxlength="4" size="4"
					disabled="%{form.creditFormat == null || form.creditFormat.isEmpty() || form.creditFormat == 'arrangeHours'}"/>
				</td>
				</tr>
				<tr>
				<td nowrap><loc:message name="propertyMaxUnits"/></td>
				<td>
				<s:textfield name="form.maxUnits" maxlength="4" size="4"
					disabled="%{form.creditFormat != 'variableRange'}"/>
				</td>
				</tr>
				<tr>
				<td nowrap><loc:message name="propertyFractionalIncrementsAllowed"/></td>
				<td>
				<s:checkbox name="form.fractionalIncrementsAllowed"
					disabled="%{form.creditFormat != 'variableRange'}"/>
				</td>
				</tr>
				</table>
			</TD>
		</TR>

		<s:if test="form.allowDemandCourseOfferings == true && #request.crsOfferingList != null && !#request.crsOfferingList.isEmpty()">
			<TR>
				<TD><loc:message name="propertyTakeCourseDemandsFromOffering"/> </TD>
				<TD>
					<s:select name="form.demandCourseOfferingId"
						list="#request.crsOfferingList" listKey="uniqueId" listValue="courseNameWithTitle"
						headerKey="" headerValue=""/>
				</TD>
			</TR>
		</s:if>

		<s:if test="form.allowAlternativeCourseOfferings == true && #request.altOfferingList != null && !#request.altOfferingList.isEmpty()">
			<TR>
				<TD><loc:message name="propertyAlternativeCourseOffering"/> </TD>
				<TD>
					<s:select name="form.alternativeCourseOfferingId"
						list="#request.altOfferingList" listKey="uniqueId" listValue="courseNameWithTitle"
						headerKey="" headerValue=""/>
				</TD>
			</TR>
		</s:if>		
	</sec:authorize>
	
	<sec:authorize access="!(not #form.add and hasPermission(#form.courseOfferingId, 'CourseOffering', 'EditCourseOffering')) and
						!(#form.add and hasPermission(#form.subjectAreaId, 'SubjectArea', 'AddCourseOffering'))">
		<s:if test="form.consent != -1">
			<TR>
				<TD valign="top"><loc:message name="propertyConsent" /></TD>
				<TD>
					<s:iterator value="#request.consentTypeList" var="consent">
						<s:if test="#consent.uniqueId == form.consent">
							<s:property value="#consent.label"/>
						</s:if>
					</s:iterator>
				</TD>
			</TR>
		</s:if>
		<s:hidden name="form.consent"/>

		<s:if test="form.isControl == true">
			<s:if test="form.creditText != null && !form.creditText.isEmpty()">
				<TR>
					<TD><loc:message name="propertyCredit"/></TD>
					<TD>
						<s:property value="form.creditText"/>
					</TD>
				</TR>
			</s:if>
			<s:hidden name="form.creditFormat"/>
			<s:hidden name="form.creditType"/>
			<s:hidden name="form.creditUnitType"/>
			<s:hidden name="form.units"/>
			<s:hidden name="form.maxUnits"/>
			<s:hidden name="form.creditText"/>
		</s:if>
		
		<s:if test="form.allowDemandCourseOfferings == true && #request.crsOfferingList != null && !#request.crsOfferingList.isEmpty()">
			<s:iterator value="#request.crsOfferingList" var="course">
				<s:if test="#course.uniqueId == form.demandCourseOfferingId">
					<TR>
						<TD><loc:message name="propertyTakeCourseDemandsFromOffering"/> </TD>
						<TD>
							<s:property value="#course.courseNameWithTitle"/>
						</TD>
					</TR>				
				</s:if>
			</s:iterator>
		</s:if>
		<s:hidden name="form.demandCourseOfferingId"/>
	</sec:authorize>

	<s:if test="form.catalogLinkLabel != null && !form.catalogLinkLabel.isEmpty()">
		<TR>
			<TD><loc:message name="propertyCourseCatalog"/> </TD>
			<TD>
				<A href="<s:property value="form.catalogLinkLocation" />" target="_blank"><s:property value="form.catalogLinkLabel" /></A>
			</TD>
		</TR>
	</s:if>
	<tt:hasProperty name="unitime.custom.CourseUrlProvider">
		<TR>
			<TD><loc:message name="propertyCourseCatalog"/> </TD>
			<TD>
				<span id='UniTimeGWT:CourseLink' style="display: none;">subjectId,course</span>
			</TD>
		</TR>
	</tt:hasProperty>

	<s:if test="form.isControl == true">
		<sec:authorize access="(not #form.add and (hasPermission(#form.courseOfferingId, 'CourseOffering', 'EditCourseOffering')
								or hasPermission(#form.courseOfferingId, 'CourseOffering', 'EditCourseOfferingCoordinators')))
								or (#form.add and hasPermission(#form.subjectAreaId, 'SubjectArea', 'AddCourseOffering'))">
			<s:if test="#request.instructorsList != null && !#request.instructorsList.isEmpty()">
			<TR>
				<TD valign="top"><loc:message name="propertyCoordinators"/> </TD>
				<TD nowrap>
				<table class="unitime-Table">
					<tr>
						<td>&nbsp;<i><loc:message name="columnInstructorName"/></i>&nbsp;</td>
						<td>&nbsp;<i><loc:message name="columnInstructorShare"/></i>&nbsp;</td>
						<td>
						<s:if test="#request.responsibilities != null && !#request.responsibilities.isEmpty()">
							&nbsp;<i><loc:message name="columnTeachingResponsibility"/></i>&nbsp;
						</s:if>
						</td><td></td>
					</tr>
				<s:iterator value="form.instructors" var="instructor" status="stat"><s:set var="ctr" value="#stat.index"/>
					<tr><td>
					<s:select name="form.instructors[%{#ctr}]"
						list="#request.instructorsList" listKey="value" listValue="label"
						headerKey="-" headerValue="-"
						style="width:200px;" />
					</td><td align="center">
						<s:textfield name="form.percentShares[%{#ctr}]" size="3" maxlength="3" style="text-align: right;"/>
					</td><td>
					<s:if test="#request.responsibilities != null && !#request.responsibilities.isEmpty()">
						<s:if test="form.responsibilities[#ctr] == '' || form.defaultTeachingResponsibilityId == ''">
							<s:select name="form.responsibilities[%{#ctr}]"
								list="#request.responsibilities" listKey="uniqueId" listValue="label"
								headerKey="-" headerValue="-"/>
						</s:if>
						<s:else>
							<s:select name="form.responsibilities[%{#ctr}]"
								list="#request.responsibilities" listKey="uniqueId" listValue="label"
								/>
						</s:else>
					</s:if>
					<s:else>
						<s:hidden name="form.responsibilities[%{#ctr}]"/>
					</s:else>
					</td><td>
						<s:submit name='op' value='%{#msg.actionRemoveCoordinator()}' onclick="doDel('coordinator', '%{#ctr}');"/>
					</td></tr>
   				</s:iterator>
   				<tr><td colspan='4'>
					<s:submit accesskey='%{#msg.accessAddCoordinator()}' name='op' value='%{#msg.actionAddCoordinator()}'
						title='%{#msg.titleAddCoordinator(#msg.accessAddCoordinator())}'/>
				</td></tr>
				</table>
				</TD>
			</TR>
			</s:if>
		</sec:authorize>
		<sec:authorize access="!(not #form.add and (hasPermission(#form.courseOfferingId, 'CourseOffering', 'EditCourseOffering')
								or hasPermission(#form.courseOfferingId, 'CourseOffering', 'EditCourseOfferingCoordinators')))
								and !(#form.add and hasPermission(#form.subjectAreaId, 'SubjectArea', 'AddCourseOffering'))">
			<s:if test="form.instructors != null && !form.instructors.isEmpty()">
				<TD valign="top"><loc:message name="propertyCoordinators"/> </TD>
				<TD nowrap>
					<s:iterator value="form.instructors" var="instructor" status="stat"><s:set var="ctr" value="#stat.index"/>
						<s:iterator value="#request.instructorsList" var="lookup">
							<s:if test="#lookup.value == #instructor">
								<s:if test="#ctr > 0"><br></s:if>
								<s:property value="#lookup.label"/>
								<s:iterator value="#request.responsibilities" var="responsibility">
									<s:if test="#responsibility.uniqueId == form.responsibilities[#ctr]">(<s:property value="#responsibility.label"/>)</s:if>
								</s:iterator>
							</s:if>
						</s:iterator>
					</s:iterator>
				</TD>
			</s:if>
			<s:iterator value="form.instructors" var="instructor" status="stat"><s:set var="ctr" value="#stat.index"/>
				<s:hidden name="form.instructors[%{#ctr}]"/>
				<s:hidden name="form.percentShares[%{#ctr}]"/>
				<s:hidden name="form.responsibilities[%{#ctr}]"/>
			</s:iterator>
		</sec:authorize>

		<sec:authorize access="(not #form.add and hasPermission(#form.courseOfferingId, 'CourseOffering', 'EditCourseOffering')) or 
							(#form.add and hasPermission(#form.subjectAreaId, 'SubjectArea', 'AddCourseOffering'))">
			<TR>
				<TD valign="top"><loc:message name="propertyByReservationOnly"/> </TD>
				<TD>
					<s:checkbox name="form.byReservationOnly" />
					<i><loc:message name="descriptionByReservationOnly"/></i>
				</TD>
			</TR>
			<TR>
				<TD valign="top"><loc:message name="propertyLastWeekEnrollment"/></TD>
				<TD valign="top">
					<s:textfield name="form.wkEnroll" maxlength="4" size="4"/>
					<i><loc:message name="descriptionLastWeekEnrollment"><s:property value="form.wkEnrollDefault" /></loc:message></i>
					<s:hidden name="form.wkEnrollDefault"/>
				</TD>
			</TR>
			<TR>
				<TD valign="top"><loc:message name="propertyLastWeekChange"/></TD>
				<TD valign="top">
					<s:textfield name="form.wkChange" maxlength="4" size="4"/>
					<i><loc:message name="descriptionLastWeekChange"><s:property value="form.wkChangeDefault" /></loc:message></i>
					<s:hidden name="form.wkChangeDefault"/>
				</TD>
			</TR>
			<TR>
				<TD valign="top"><loc:message name="propertyLastWeekDrop"/></TD>
				<TD valign="top">
					<s:textfield name="form.wkDrop" maxlength="4" size="4"/>
					<i><loc:message name="descriptionLastWeekDrop"><s:property value="form.wkDropDefault" /></loc:message></i>
					<s:hidden name="form.wkDropDefault"/>
					<br><i><loc:message name="descriptionEnrollmentDeadlines"><s:property value="form.weekStartDayOfWeek" /></loc:message></i>
					<s:hidden name="form.weekStartDayOfWeek"/>
				</TD>
			</TR>
			
			<!-- Requests / Notes -->
			<TR>
				<TD valign="top"><loc:message name="propertyRequestsNotes"/></TD>
				<TD align="left">
				<s:textarea name="form.notes" rows="4" cols="57"/>
				</TD>
			</TR>
		</sec:authorize>
		<sec:authorize access="!(not #form.add and hasPermission(#form.courseOfferingId, 'CourseOffering', 'EditCourseOffering')) and 
							!(#form.add and hasPermission(#form.subjectAreaId, 'SubjectArea', 'AddCourseOffering'))">
			<s:if test="form.byReservationOnly== true">
				<TR>
					<TD><loc:message name="propertyByReservationOnly"/></TD>
					<TD>
						<IMG src="images/accept.png" alt="ENABLED" title="${MSG.descriptionByReservationOnly2()}" border="0">
						<i><loc:message name="descriptionByReservationOnly2"/></i>
					</TD>
				</TR>
			</s:if>
			<s:hidden name="form.byReservationOnly"/>
			<s:if test="form.wkEnroll != null && !form.wkEnroll.isEmpty()">
				<TR>
					<TD valign="top"><loc:message name="propertyLastWeekEnrollment"/></TD>
					<TD>
						<loc:message name="textLastWeekEnrollment"><s:property value="form.wkEnroll" /></loc:message>
						<s:if test="(form.wkChange == null || form.wkChange.isEmpty()) && (form.wkDrop == null || form.wkDrop.isEmpty())">
							<br><i><loc:message name="descriptionEnrollmentDeadlines"><s:property value="form.weekStartDayOfWeek" /></loc:message></i>
						</s:if>
					</TD>
				</TR>
			</s:if>
			<s:if test="form.wkChange != null && !form.wkChange.isEmpty()">
				<TR>
					<TD valign="top"><loc:message name="propertyLastWeekChange"/></TD>
					<TD>
						<loc:message name="textLastWeekChange"><s:property value="form.wkChange" /></loc:message>
						<s:if test="form.wkDrop == null || form.wkDrop.isEmpty()">
							<br><i><loc:message name="descriptionEnrollmentDeadlines"><s:property value="form.weekStartDayOfWeek" /></loc:message></i>
						</s:if>
					</TD>
				</TR>
			</s:if>
			<s:if test="form.wkDrop != null && !form.wkDrop.isEmpty()">
				<TR>
					<TD valign="top"><loc:message name="propertyLastWeekDrop"/></TD>
					<TD>
						<loc:message name="textLastWeekDrop"><s:property value="form.wkDrop" /></loc:message>
						<br><i><loc:message name="descriptionEnrollmentDeadlines"><s:property value="form.weekStartDayOfWeek" /></loc:message></i>
					</TD>
				</TR>
			</s:if>
			<s:hidden name="form.wkEnroll"/>
			<s:hidden name="form.wkEnrollDefault"/>
			<s:hidden name="form.wkChange"/>
			<s:hidden name="form.wkChangeDefault"/>
			<s:hidden name="form.wkDrop"/>
			<s:hidden name="form.wkDropDefault"/>
			<s:hidden name="form.weekStartDayOfWeek"/>
			<s:hidden name="form.notes"/>
		</sec:authorize>		
	</s:if>
	
	<s:if test="form.isControl == true">
		<TR>
			<TD valign="top"><loc:message name="propertyWaitListing"/></TD>
			<TD valign="top">
				<s:select name="form.waitList" 
					list="form.waitListOptions" listKey="value" listValue="label"/>
			</TD>
		</TR>
	</s:if>
	<s:else>
		<TR>
			<TD valign="top"><loc:message name="propertyWaitListing"/></TD>
			<TD valign="top">
				<s:if test="form.waitList == null || form.waitList.isEmpty()">
					<tt:propertyEquals name="unitime.offering.waitListDefault" value="true">
						<img src="images/accept.png" alt="${MSG.waitListDefaultEnabled()}" title="${MSG.descWaitListEnabled()}" border="0" align="top">
						<loc:message name="waitListDefaultEnabled"/>
					</tt:propertyEquals>
					<tt:propertyEquals name="unitime.offering.waitListDefault" value="WaitList">
						<img src="images/accept.png" alt="${MSG.waitListDefaultEnabled()}" title="${MSG.descWaitListEnabled()}" border="0" align="top">
						<loc:message name="waitListDefaultEnabled"/>
					</tt:propertyEquals>
					<tt:propertyEquals name="unitime.offering.waitListDefault" value="ReSchedule">
						<img src="images/accept_gold.png" alt="${MSG.waitListDefaultReschedule()}" title="${MSG.descWaitListReschedule()}" border="0" align="top">
						<loc:message name="waitListDefaultReschedule"/>
					</tt:propertyEquals>
					<tt:propertyEquals name="unitime.offering.waitListDefault" value="false">
						<img src="images/cancel.png" alt="${MSG.waitListDisabled()}" title="${MSG.descWaitListDisabled()}" border="0" align="top">
						<loc:message name="waitListDefaultDisabled"/>
					</tt:propertyEquals>
					<tt:propertyEquals name="unitime.offering.waitListDefault" value="Disabled">
						<img src="images/cancel.png" alt="${MSG.waitListDisabled()}" title="${MSG.descWaitListDisabled()}" border="0" align="top">
						<loc:message name="waitListDefaultDisabled"/>
					</tt:propertyEquals>
				</s:if>
				<s:if test="form.waitList == 'WaitList' || form.waitList == 'true'">
					<img src="images/accept.png" alt="${MSG.waitListEnabled()}" title="${MSG.descWaitListEnabled()}" border="0" align="top">
					<loc:message name="waitListEnabled"/>
				</s:if>
				<s:if test="form.waitList == 'ReSchedule'">
					<img src="images/accept_gold.png" alt="${MSG.waitListReschedule()}" title="${MSG.descWaitListReschedule()}" border="0" align="top">
					<loc:message name="waitListReschedule"/>
				</s:if>
				<s:if test="form.waitList == 'false' || form.waitList == 'Disabled'">
					<img src="images/cancel.png" alt="${MSG.waitListDisabled()}" title="${MSG.descWaitListDisabled()}" border="0" align="top">
					<loc:message name="waitListDisabled"/>
				</s:if>
			</TD>
		</TR>
		<s:hidden name="form.waitList"/>
	</s:else>
	
	<s:if test="form.overrideTypes != null && !form.overrideTypes.isEmpty()">
		<TR>
			<TD valign="top"><loc:message name="propertyDisabledOverrides"/> </TD>
			<TD>
				<s:iterator value="form.overrideTypes" var="type" status="stat"><s:set var="idx" value="#stat.index"/>
					<s:if test="#idx > 0"><br></s:if>
					<s:checkbox name="form.courseOverride[%{#type.uniqueId}]"/>
					<s:property value="#type.reference"/>: <s:property value="#type.label"/>
				</s:iterator>
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
				<s:if test="form.add == true">
					<s:submit accesskey='%{#msg.accessSaveCourseOffering()}' name='op' value='%{#msg.actionSaveCourseOffering()}'
						title='%{#msg.titleSaveCourseOffering(#msg.accessSaveCourseOffering())}'/>
					<s:submit accesskey='%{#msg.accessBackToIOList()}' name='op' value='%{#msg.actionBackToIOList()}'
						title='%{#msg.titleBackToIOList(#msg.accessBackToIOList())}'/>
				</s:if>
				<s:else>
					<s:submit accesskey='%{#msg.accessUpdateCourseOffering()}' name='op' value='%{#msg.actionUpdateCourseOffering()}'
						title='%{#msg.titleUpdateCourseOffering(#msg.accessUpdateCourseOffering())}'/>
					<s:submit accesskey='%{#msg.accessBackToIODetail()}' name='op' value='%{#msg.actionBackToIODetail()}'
						title='%{#msg.titleBackToIODetail(#msg.accessBackToIODetail())}'/>
				</s:else>
			</TD>
		</TR>

	</TABLE>
	
	<INPUT type="hidden" name="deleteType" id="deleteType" value="">
	<INPUT type="hidden" name="deleteId" id="deleteId" value="">
	<SCRIPT type="text/javascript" >
		function doDel(type, id) {
			document.getElementById('deleteType').value = type;
			document.getElementById('deleteId').value = id;
		}
		function creditFormatChanged(creditFormat) {
			if (creditFormat.value == 'fixedUnit') {
				document.getElementById('courseOfferingEdit_form_creditType').disabled = false;
				document.getElementById('courseOfferingEdit_form_creditUnitType').disabled = false;
				document.getElementById('courseOfferingEdit_form_units').disabled = false;
				document.getElementById('courseOfferingEdit_form_maxUnits').disabled = true;
				document.getElementById('courseOfferingEdit_form_fractionalIncrementsAllowed').disabled = true
			} else if (creditFormat.value == 'arrangeHours') {
				document.getElementById('courseOfferingEdit_form_creditType').disabled = false;
				document.getElementById('courseOfferingEdit_form_creditUnitType').disabled = false;
				document.getElementById('courseOfferingEdit_form_units').disabled = true;
				document.getElementById('courseOfferingEdit_form_maxUnits').disabled = true;
				document.getElementById('courseOfferingEdit_form_fractionalIncrementsAllowed').disabled = true
			} else if (creditFormat.value == 'variableMinMax') {
				document.getElementById('courseOfferingEdit_form_creditType').disabled = false;
				document.getElementById('courseOfferingEdit_form_creditUnitType').disabled = false;
				document.getElementById('courseOfferingEdit_form_units').disabled = false;
				document.getElementById('courseOfferingEdit_form_maxUnits').disabled = false;
				document.getElementById('courseOfferingEdit_form_fractionalIncrementsAllowed').disabled = true
			} else if (creditFormat.value == 'variableRange') {
				document.getElementById('courseOfferingEdit_form_creditType').disabled = false;
				document.getElementById('courseOfferingEdit_form_creditUnitType').disabled = false;
				document.getElementById('courseOfferingEdit_form_units').disabled = false;
				document.getElementById('courseOfferingEdit_form_maxUnits').disabled = false;
				document.getElementById('courseOfferingEdit_form_fractionalIncrementsAllowed').disabled = false
			} else {
				document.getElementById('courseOfferingEdit_form_creditType').disabled = true;
				document.getElementById('courseOfferingEdit_form_creditUnitType').disabled = true;
				document.getElementById('courseOfferingEdit_form_units').disabled = true;
				document.getElementById('courseOfferingEdit_form_maxUnits').disabled = true;
				document.getElementById('courseOfferingEdit_form_fractionalIncrementsAllowed').disabled = true
			}
		}
	</SCRIPT>
</s:form>
</loc:bundle>
