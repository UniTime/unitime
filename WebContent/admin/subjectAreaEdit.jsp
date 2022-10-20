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
<loc:bundle name="org.unitime.timetable.gwt.resources.GwtMessages"><s:set var="msg" value="#attr.MSG"/>
<tt:confirm name="confirmDelete"><loc:message name="confirmDeleteSubjectArea"/></tt:confirm>
<s:form action="subjectAreaEdit">
<s:hidden name="form.uniqueId" />
	<table class="unitime-MainTable">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<s:if test="form.uniqueId != null">
							<s:property value="form.abbv"/> - <s:property value="form.title"/>
						</s:if>
					</tt:section-title>
					<s:if test="form.uniqueId == null">
						<tt:button value="%{#msg.buttonSave()}"/>
					</s:if><s:else>
						<tt:button value="%{#msg.buttonUpdate()}"/>
						<sec:authorize access="hasPermission(#form.uniqueId, 'SubjectArea', 'SubjectAreaDelete')">
							<tt:button value="%{#msg.buttonDelete()}" onclick="return confirmDelete();"/>
						</sec:authorize>
					</s:else>
					<tt:button value="%{#msg.buttonBack()}"/>
				</tt:section-header>				
			</TD>
		</TR>
		
		<s:if test="!fieldErrors.isEmpty()">
			<TR><TD colspan="2" align="left" class="errorTable">
				<loc:bundle name="CourseMessages" id="X">
				<div class='errorHeader'><loc:message name="formValidationErrors" id="X"/></div><s:fielderror escape="false"/>
				</loc:bundle>
			</TD></TR>
		</s:if>

		<TR>
			<TD><loc:message name="propAcademicSession"/></TD>
			<TD style="color:gray;"><s:property value="session"/></TD>
		</TR>

		<TR>
			<TD><loc:message name="fieldAbbreviation"/>:</TD>
			<TD>
				<s:textfield name="form.abbv" size="20" maxlength="20"/>
				<s:fielderror fieldName="form.abbv" escape="false"/>
			</TD>
		</TR>
		
		<TR>
			<TD><loc:message name="fieldTitle"/>:</TD>
			<TD>
				<s:textfield name="form.title" size="40" maxlength="100"/>
				<s:fielderror fieldName="form.title" escape="false"/>
			</TD>
		</TR>
		
		<TR>
			<TD><loc:message name="fieldExternalId"/>:</TD>
			<TD>
				<s:textfield name="form.externalId" size="40" maxlength="40"/>
				<s:fielderror fieldName="form.externalId" escape="false"/>
			</TD>
		</TR>
		
		<TR>
			<TD><loc:message name="fieldDepartment"/>:</TD>
			<TD>
				<s:if test="form.uniqueId == null">
					<s:select name="form.department"
						list="#request.deptsList" listKey="uniqueId" listValue="label"
						headerKey="" headerValue="%{#msg.itemSelect()}"/>
				</s:if><s:else>
					<sec:authorize access="hasPermission(#form.uniqueId, 'SubjectArea', 'SubjectAreaChangeDepartment')">
						<s:select name="form.department"
							list="#request.deptsList" listKey="uniqueId" listValue="label"
							headerKey="" headerValue="%{#msg.itemSelect()}"/>
					</sec:authorize>
					<sec:authorize access="!hasPermission(#form.uniqueId, 'SubjectArea', 'SubjectAreaChangeDepartment')">
						<s:hidden name="form.department"/>
						<s:select name="form.department" disabled="true"
							list="#request.deptsList" listKey="uniqueId" listValue="label"
							headerKey="" headerValue="%{#msg.itemSelect()}"/>
					</sec:authorize>
				</s:else>
				<s:fielderror fieldName="form.department" escape="false"/>
			</TD>
		</TR>

		<TR>
			<TD colspan="2">
			<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
			</TD>
		</TR>
		
		<TR>
			<TD align="right" colspan='2'>
				<s:if test="form.uniqueId == null">
					<tt:button value="%{#msg.buttonSave()}"/>
				</s:if><s:else>
					<tt:button value="%{#msg.buttonUpdate()}"/>
					<sec:authorize access="hasPermission(#form.uniqueId, 'SubjectArea', 'SubjectAreaDelete')">
						<tt:button value="%{#msg.buttonDelete()}" onclick="return confirmDelete();"/>
					</sec:authorize>
				</s:else>
				<tt:button value="%{#msg.buttonBack()}"/>
			</TD>
		</TR>
	</table>
</s:form>
</loc:bundle>