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
<loc:bundle name="ConstantsMessages" id="CONST"><s:set var="const" value="#attr.CONST"/>
<s:form action="instructorSearch">
<table class="unitime-MainTable">
	<tr><td>
		<tt:section-header>
			<tt:section-title>
				<div style="padding-right: 3px; display: block; margin-bottom: 3px; vertical-align: bottom; line-height: 25px;">
				<b><loc:message name="propertyDepartment"/></b>
				<s:select name="deptId" list="%{#request.deptsList}" listKey="id" listValue="value"
					headerKey="" headerValue="%{#const.select()}"
				/>
				<s:submit name='op' value="%{#msg.actionSearchInstructors()}"
					title="%{#msg.titleSearchInstructors(#msg.accessSearchInstructors())}"
					accesskey="%{#msg.accessSearchInstructors()}"/>
				</div>
			</tt:section-title>
			<s:if test="#request.instructorList != null">
				<sec:authorize access="hasPermission(#deptId, 'Department', 'InstructorsExportPdf')">
					<s:submit name='op' value="%{#msg.actionExportPdf()}"
						title="%{#msg.titleExportPdf(#msg.accessExportPdf())}"
						accesskey="%{#msg.accessExportPdf()}"/>
					<s:submit name='op' value="%{#msg.actionExportCsv()}"
						title="%{#msg.titleExportCsv(#msg.accessExportCsv())}"
						accesskey="%{#msg.accessExportCsv()}"/>
				</sec:authorize>
				<sec:authorize access="hasPermission(#deptId, 'Department', 'InstructorSurveyAdmin')">
					<s:if test="hasSurveys == true">
						<s:submit name='op' value="%{#msg.actionExportSurveysXLS()}"/>
					</s:if>
				</sec:authorize>
			</s:if>
			<s:if test="deptId != null">
				<sec:authorize access="hasPermission(#deptId, 'Department', 'ManageInstructors')">
					<s:submit name='op' value="%{#msg.actionManageInstructorList()}"
						title="%{#msg.titleManageInstructorList(#msg.accessManageInstructorList())}"
						accesskey="%{#msg.accessManageInstructorList()}"/>
				</sec:authorize>
				<sec:authorize access="hasPermission(#deptId, 'Department', 'InstructorAdd')">
					<s:submit name='op' value="%{#msg.actionAddNewInstructor()}"
						title="%{#msg.titleAddNewInstructor(#msg.accessAddNewInstructor())}"
						accesskey="%{#msg.accessAddNewInstructor()}"/>
				</sec:authorize>
			</s:if>
		</tt:section-header>
		</td></tr>
		<tr><td colspan="2" align="center"><s:actionerror/></td></tr>
	</table>
	<s:if test="#request.instructorList != null">
		<table class="unitime-MainTable" style="padding-top: 20px;">
			<s:property value="%{#request.instructorList}" escapeHtml="false"/>
		</table>
		<table class="unitime-MainTable">
			<tr><td align="right" class="top-border-solid" style="padding-top: 3px;">
				<s:if test="#request.instructorList != null">
					<sec:authorize access="hasPermission(#deptId, 'Department', 'InstructorsExportPdf')">
						<s:submit name='op' value="%{#msg.actionExportPdf()}"
							title="%{#msg.titleExportPdf(#msg.accessExportPdf())}"
							accesskey="%{#msg.accessExportPdf()}"/>
						<s:submit name='op' value="%{#msg.actionExportCsv()}"
							title="%{#msg.titleExportCsv(#msg.accessExportCsv())}"
							accesskey="%{#msg.accessExportCsv()}"/>
					</sec:authorize>
					<sec:authorize access="hasPermission(#deptId, 'Department', 'InstructorSurveyAdmin')">
						<s:if test="hasSurveys == true">
							<s:submit name='op' value="%{#msg.actionExportSurveysXLS()}"/>
						</s:if>
					</sec:authorize>
				</s:if>
				<sec:authorize access="hasPermission(#deptId, 'Department', 'ManageInstructors')">
					<s:submit name='op' value="%{#msg.actionManageInstructorList()}"
						title="%{#msg.titleManageInstructorList(#msg.accessManageInstructorList())}"
						accesskey="%{#msg.accessManageInstructorList()}"/>
				</sec:authorize>
				<sec:authorize access="hasPermission(#deptId, 'Department', 'InstructorAdd')">
					<s:submit name='op' value="%{#msg.actionAddNewInstructor()}"
						title="%{#msg.titleAddNewInstructor(#msg.accessAddNewInstructor())}"
						accesskey="%{#msg.accessAddNewInstructor()}"/>
				</sec:authorize>
			</td></tr>
		</table>
	</s:if>
</s:form>
</loc:bundle>
</loc:bundle>