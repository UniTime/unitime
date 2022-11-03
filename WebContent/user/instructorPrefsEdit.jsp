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
<s:form action="instructorPrefEdit">
	<s:hidden name="form.instructorId"/>
	<s:hidden name="form.nextId"/>
	<s:hidden name="form.previousId"/>
	<s:hidden name="form.deptCode"/>
	
	<table class="unitime-MainTable">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<s:property value="form.name"/>
					</tt:section-title>
					<s:submit accesskey='%{#msg.accessUpdatePreferences()}' name='op' value='%{#msg.actionUpdatePreferences()}'
						title='%{#msg.titleUpdatePreferences(#msg.accessUpdatePreferences())}'/>
					<sec:authorize access="hasPermission(#form.instructorId, 'DepartmentalInstructor', 'InstructorEditClearPreferences')">
						<s:submit accesskey='%{#msg.accessClearInstructorPreferences()}' name='op' value='%{#msg.actionClearInstructorPreferences()}'
							title='%{#msg.titleClearInstructorPreferences(#msg.accessClearInstructorPreferences())}'/>
					</sec:authorize>
					<s:if test="form.previousId != null">
						<s:submit accesskey='%{#msg.accessPreviousInstructor()}' name='op' value='%{#msg.actionPreviousInstructor()}'
							title='%{#msg.titlePreviousInstructorWithUpdate(#msg.accessPreviousInstructor())}'/>
					</s:if>
					<s:if test="form.nextId != null">
						<s:submit accesskey='%{#msg.accessNextInstructor()}' name='op' value='%{#msg.actionNextInstructor()}'
							title='%{#msg.titleNextInstructorWithUpdate(#msg.accessNextInstructor())}'/>
					</s:if>
					<s:submit accesskey='%{#msg.accessBackToDetail()}' name='op' value='%{#msg.actionBackToDetail()}'
						title='%{#msg.titleBackToDetail(#msg.accessBackToDetail())}'/>
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
		
<!-- Preferences -->
		<s:include value="preferencesEdit2.jspf">
			<s:param name="frmName" value="'instructorPrefsEdit'"/>
			<s:param name="periodPref" value="false"/>
			<s:param name="datePatternPref" value="false"/>
			<s:param name="timePref" value="false"/>
			<s:param name="timeAvail" value="true"/>
			<s:param name="dateAvail" value="true"/>
		</s:include>
<!-- buttons -->
		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>
		<TR>
			<TD colspan="2" align="right">
					<s:submit accesskey='%{#msg.accessUpdatePreferences()}' name='op' value='%{#msg.actionUpdatePreferences()}'
						title='%{#msg.titleUpdatePreferences(#msg.accessUpdatePreferences())}'/>
					<sec:authorize access="hasPermission(#form.instructorId, 'DepartmentalInstructor', 'InstructorEditClearPreferences')">
						<s:submit accesskey='%{#msg.accessClearInstructorPreferences()}' name='op' value='%{#msg.actionClearInstructorPreferences()}'
							title='%{#msg.titleClearInstructorPreferences(#msg.accessClearInstructorPreferences())}'/>
					</sec:authorize>
					<s:if test="form.previousId != null">
						<s:submit accesskey='%{#msg.accessPreviousInstructor()}' name='op' value='%{#msg.actionPreviousInstructor()}'
							title='%{#msg.titlePreviousInstructorWithUpdate(#msg.accessPreviousInstructor())}'/>
					</s:if>
					<s:if test="form.nextId != null">
						<s:submit accesskey='%{#msg.accessNextInstructor()}' name='op' value='%{#msg.actionNextInstructor()}'
							title='%{#msg.titleNextInstructorWithUpdate(#msg.accessNextInstructor())}'/>
					</s:if>
					<s:submit accesskey='%{#msg.accessBackToDetail()}' name='op' value='%{#msg.actionBackToDetail()}'
						title='%{#msg.titleBackToDetail(#msg.accessBackToDetail())}'/>
			</TD>
		</TR>
	</TABLE>
	
	<s:if test="#request.hash != null">
		<SCRIPT type="text/javascript">
			location.hash = '<%=request.getAttribute("hash")%>';
		</SCRIPT>
	</s:if>	
</s:form>
</loc:bundle>	
