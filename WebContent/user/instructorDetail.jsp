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
<s:form action="instructorDetail">
	<s:if test="form.departments == null">
		<s:hidden name="form.instructorId"/>
	</s:if>
	<s:hidden name="form.nextId"/>
	<s:hidden name="form.previousId"/>
	<s:hidden name="form.deptCode"/>
	<s:hidden name="op2" value=""/>
	<table class="unitime-MainTable">
		<TR><TD valign="middle" colspan='2'>
			<tt:section-header>
				<tt:section-title>
					<s:property value="form.name"/>
				</tt:section-title>
			<sec:authorize access="hasPermission(#form.instructorId, 'DepartmentalInstructor', 'InstructorEdit')">
				<s:submit accesskey='%{#msg.accessEditInstructor()}' name='op' value='%{#msg.actionEditInstructor()}'
					title='%{#msg.titleEditInstructor(#msg.accessEditInstructor())}'/>
			</sec:authorize>
			<sec:authorize access="hasPermission(#form.deptCode, 'Department', 'InstructorAssignmentPreferences')">
				<s:submit accesskey='%{#msg.accessEditInstructorAssignmentPreferences()}' name='op' value='%{#msg.actionEditInstructorAssignmentPreferences()}'
					title='%{#msg.titleEditInstructorAssignmentPreferences(#msg.accessEditInstructorAssignmentPreferences())}'/>
			</sec:authorize> 
			<sec:authorize access="hasPermission(#form.instructorId, 'DepartmentalInstructor', 'InstructorPreferences')">
				<s:submit accesskey='%{#msg.accessEditInstructorPreferences()}' name='op' value='%{#msg.actionEditInstructorPreferences()}'
					title='%{#msg.titleEditInstructorPreferences(#msg.accessEditInstructorPreferences())}'/>
			</sec:authorize>
			<s:if test="form.showInstructorSurvey == true">
				<input type="button" value="${MSG.actionInstructorSurvey()}" 
								title="${MSG.titleInstructorSurvey(MSG.accessInstructorSurvey())}" 
								class="btn" 
								accesskey="${MSG.accessInstructorSurvey()}"
								onClick="showGwtDialog('${MSG.actionInstructorSurvey()}', 'gwt.jsp?page=instructorSurvey&menu=hide&id=${form.puId}','900','90%');"
						/>
			</s:if>
			<s:if test="form.previousId != null">
				<s:submit accesskey='%{#msg.accessPreviousInstructor()}' name='op' value='%{#msg.actionPreviousInstructor()}'
					title='%{#msg.titlePreviousInstructor(#msg.accessPreviousInstructor())}'/>
			</s:if>
			<s:if test="form.nextId != null">
				<s:submit accesskey='%{#msg.accessNextInstructor()}' name='op' value='%{#msg.actionNextInstructor()}'
					title='%{#msg.titleNextInstructor(#msg.accessNextInstructor())}'/>
			</s:if>
			<tt:back styleClass="btn" 
				name="${MSG.actionBackInstructorDetail()}" 
				title="${MSG.titleBackInstructorDetail(MSG.accessBackInstructorDetail())}" 
				accesskey="${MSG.accessBackInstructorDetail()}" 
				type="PreferenceGroup">
				<s:property value="form.instructorId"/>
			</tt:back>
			</tt:section-header>
		</TD></TR>
		
		<s:if test="!fieldErrors.isEmpty()">
			<TR><TD colspan="2" align="left" class="errorTable">
				<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror/>
			</TD></TR>
		</s:if>
		<s:if test="form.puId != null">
			<TR>
				<TD><loc:message name="propertyExternalId"/></TD>
				<TD><s:property value="form.puId"/></TD>
			</TR>
		</s:if>
		<s:if test="form.departments != null">
			<TR>
				<TD><loc:message name="propertyDepartment"/></TD>
				<TD>
					<s:select name="form.instructorId" list="form.departments" listKey="id" listValue="value" onchange="submit();"/>
				</TD>
			</TR>
		</s:if>
		<s:if test="form.departments == null && form.deptName != null">
			<TR>
				<TD><loc:message name="propertyDepartment"/></TD>
				<TD><s:property value="form.deptName"/></TD>
			</TR>
		</s:if>
		
		<s:if test="form.careerAcct != null">
			<TR>
				<TD><loc:message name="propertyAccountName"/></TD>
				<TD><s:property value="form.careerAcct"/></TD>
			</TR>
		</s:if>
		
		<s:if test="form.email != null && !form.email.isEmpty()">
			<TR>
				<TD><loc:message name="propertyEmail"/></TD>
				<TD><s:property value="form.email"/></TD>
			</TR>
		</s:if>

		<s:if test="form.posType != null">
			<TR>
				<TD><loc:message name="propertyInstructorPosition"/></TD>
				<TD><s:property value="form.posType"/></TD>
			</TR>
		</s:if>

		<s:if test="form.note != null">
			<TR>
				<TD><loc:message name="propertyNote"/></TD>
				<TD style="white-space: pre-wrap;"><s:property value="form.note" escapeHtml="false"/></TD>
			</TR>
		</s:if>
		
		<s:if test="form.ignoreDist == true">
			<TR>
				<TD><loc:message name="propertyIgnoreTooFar"/></TD>
				<TD><font color='red'><loc:message name="enabled"/></font>&nbsp;&nbsp; -- <i><loc:message name="descriptionInstructorIgnoreTooFar"/>
				</i></TD>
			</TR>
		</s:if>
		
		<s:if test="form.teachingPreference != null">
		<TR>
			<TD><loc:message name="propertyTeachingPreference"/></TD>
			<TD>
				<s:iterator value="#request.prefLevelsList" var="prLevel">
					<s:if test="form.teachingPreference == #prLevel.prefProlog">
						<s:if test="#prLevel.prefProlog.equalsIgnoreCase('0')">
							<b><s:property value="#prLevel.prefName"/></b>
						</s:if>
						<s:else>
							<div style='color:${prLevel.prefcolor()}'><b><s:property value="#prLevel.prefName"/></b></div>
						</s:else>
					</s:if>
				</s:iterator>
			</TD>
		</TR>
		</s:if>
		
		<s:if test="form.maxLoad != null">
			<TR>
				<TD><loc:message name="propertyMaxLoad"/></TD>
				<TD><s:property value="form.maxLoad"/></TD>
			</TR>
		</s:if>
		
		<s:iterator value="#request.attributeTypesList" var="type">
			<s:set var="hasType" value="false"/>
			<s:iterator value="#request.attributesList" var="attribute">
				<s:if test="#attribute.type == #type && form.getAttribute(#attribute.uniqueId)">
					<s:set var="hasType" value="true"/>
				</s:if>
			</s:iterator>
			<s:if test="#hasType">
				<TR><TD style="vertical-align: top;"><s:property value="#type.label"/>:</TD><TD>
					<s:iterator value="#request.attributesList" var="attribute">
						<s:if test="#attribute.type == #type && form.getAttribute(#attribute.uniqueId)">
							<div class='unitime-InstructorAttribute'><s:property value="#attribute.name"/></div>
						</s:if>
					</s:iterator>
				</TD></TR>
			</s:if>
		</s:iterator>
		
		<s:if test="form.hasInstructorSurvey == true">
			<TR><TD colspan="2">
				<div id='UniTimeGWT:InstructorSurvey' style="display: none;"><s:property value="form.instructorId"/></div>
			</TD></TR>
		</s:if>

<!-- Class Assignments -->
		<TR>
			<TD colspan="2" align="left" style="padding-top: 20px;">
				<tt:section-title><loc:message name="sectionTitleClassAssignments"/></tt:section-title>
			</TD>
		</TR>
		<TR>
			<TD colspan="2">
				<table style="width: 100%;">
					<s:if test="#request.classTable != null">
						<s:property value="#request.classTable" escapeHtml="false"/>
					</s:if>
					<s:else>
						<TR><TD>&nbsp;</TD></TR>
					</s:else>
				</table>
			</TD>
		</TR>
		
		<TR>
			<TD colspan="2">
				<tt:exams type='DepartmentalInstructor' add='false'>
					<s:property value="form.instructorId"/>
				</tt:exams>
			</TD>
		</TR>
		
		<s:if test="#request.eventTable != null">
			<TR>
				<TD colspan="2" style="padding-top: 20px;">
					<table style="width: 100%;">
						<s:property value="#request.eventTable" escapeHtml="false"/>
					</table>
				</TD>
			</TR>
		</s:if>
		
<!-- Preferences -->
	<s:if test="form.isDisplayPrefs()">
		<TR>
			<TD colspan="2" valign="middle">
				<br>
				<tt:section-title>
					<a style="border:0;background:0" 
						accesskey="${MSG.accessHideInstructorPreferences()}" 
						title="${MSG.titleHideInstructorPreferences(MSG.accessHideInstructorPreferences())}"
						href="javascript:instructorDetail.op2.value='${MSG.actionHideInstructorPreferences()}'; instructorDetail.submit();">
					<img border='0' src='images/collapse_node_btn.gif' /></a>
					<loc:message name="sectionTitlePreferences"/>
				</tt:section-title>
			</TD>
		</TR>
		<s:include value="preferencesDetail2.jspf">
			<s:param name="coursePref" value="true"/>
		</s:include>
	</s:if>
	<s:else>
		<TR>
			<TD colspan="2" valign="middle">
				<br>
				<tt:section-title>
					<a style="border:0;background:0" 
						accesskey="${MSG.accessShowInstructorPreferences()}" 
						title="${MSG.titleShowInstructorPreferences(MSG.accessShowInstructorPreferences())}"
						href="javascript:instructorDetail.op2.value='${MSG.actionDisplayInstructorPreferences()}'; instructorDetail.submit();">
						<img border='0' src='images/expand_node_btn.gif' /></a>
					<loc:message name="sectionTitlePreferences"/>
				</tt:section-title>
			</TD>
		</TR>
		<TR>
			<TD colspan="2">
				<br>
			</TD>
		</TR>
	</s:else>
	
		<s:if test="form.puId != null">
			<TR>
				<TD colspan="2">
					<div id='UniTimeGWT:StudentEnrollments' style="display: none;"><s:property value="form.puId"/></div>
				</TD>
			</TR>
		</s:if>
		
		<sec:authorize access="hasPermission(null, 'SolverGroup', 'InstructorScheduling') and hasPermission(#form.deptCode, 'Department', 'InstructorAssignmentPreferences')">
		<TR>
			<TD colspan="2">
				<a id="assignments"></a>
				<div id='UniTimeGWT:TeachingAssignments' style="display: none;"><s:property value="form.instructorId"/></div>
			</TD>
		</TR>
		</sec:authorize>

		<tt:last-change type='DepartmentalInstructor'>
			<s:property value="form.instructorId"/>
		</tt:last-change>		
	
	
		<TR align="right">
			<TD valign="middle" colspan='2' style='padding-top: 20px;'>
				<sec:authorize access="hasPermission(#form.instructorId, 'DepartmentalInstructor', 'InstructorEdit')">
					<s:submit accesskey='%{#msg.accessEditInstructor()}' name='op' value='%{#msg.actionEditInstructor()}'
						title='%{#msg.titleEditInstructor(#msg.accessEditInstructor())}'/>
				</sec:authorize>
				<sec:authorize access="hasPermission(#form.deptCode, 'Department', 'InstructorAssignmentPreferences')">
					<s:submit accesskey='%{#msg.accessEditInstructorAssignmentPreferences()}' name='op' value='%{#msg.actionEditInstructorAssignmentPreferences()}'
						title='%{#msg.titleEditInstructorAssignmentPreferences(#msg.accessEditInstructorAssignmentPreferences())}'/>
				</sec:authorize> 
				<sec:authorize access="hasPermission(#form.instructorId, 'DepartmentalInstructor', 'InstructorPreferences')">
					<s:submit accesskey='%{#msg.accessEditInstructorPreferences()}' name='op' value='%{#msg.actionEditInstructorPreferences()}'
						title='%{#msg.titleEditInstructorPreferences(#msg.accessEditInstructorPreferences())}'/>
				</sec:authorize>
				<s:if test="form.showInstructorSurvey == true">
					<input type="button" value="${MSG.actionInstructorSurvey()}" 
								title="${MSG.titleInstructorSurvey(MSG.accessInstructorSurvey())}" 
								class="btn" 
								accesskey="${MSG.accessInstructorSurvey()}"
								onClick="showGwtDialog('${MSG.actionInstructorSurvey()}', 'gwt.jsp?page=instructorSurvey&menu=hide&id=${form.puId}','900','90%');"
							/>
				</s:if>
				<s:if test="form.previousId != null">
					<s:submit accesskey='%{#msg.accessPreviousInstructor()}' name='op' value='%{#msg.actionPreviousInstructor()}'
						title='%{#msg.titlePreviousInstructor(#msg.accessPreviousInstructor())}'/>
				</s:if>
				<s:if test="form.nextId != null">
					<s:submit accesskey='%{#msg.accessNextInstructor()}' name='op' value='%{#msg.actionNextInstructor()}'
						title='%{#msg.titleNextInstructor(#msg.accessNextInstructor())}'/>
				</s:if>
				<tt:back styleClass="btn" 
					name="${MSG.actionBackInstructorDetail()}" 
					title="${MSG.titleBackInstructorDetail(MSG.accessBackInstructorDetail())}" 
					accesskey="${MSG.accessBackInstructorDetail()}" 
					type="PreferenceGroup">
					<s:property value="form.instructorId"/>
				</tt:back>
			</TD>
		</TR>
	</table>
</s:form>
</loc:bundle>
