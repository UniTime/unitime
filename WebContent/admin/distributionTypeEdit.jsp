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
<s:form action="distributionTypeEdit">
	<s:hidden name="form.uniqueId"/>
	<table class="unitime-MainTable">	
		<TR>
			<TD colspan='2'>
			<tt:section-header>
				<tt:section-title>
					<s:property value="form.reference"/>
					<s:hidden name="form.reference"/>
				</tt:section-title>
				<s:submit name='op' value='%{#msg.actionUpdateDistributionType()}'/>
				<s:submit name='op' value='%{#msg.actionBackToDistributionTypes()}'/>
			</tt:section-header>
			</TD>
		</TR>
		<tr>
			<td><loc:message name="fieldId"/>:</td>
			<td>
				<s:property value="form.requirementId"/>
				<s:hidden name="form.requirementId"/>
				<s:fielderror fieldName="form.requirementId"/>
			</td>
		</tr>
		<tr>
			<td><loc:message name="fieldAbbreviation"/>:</td>
			<td>
				<s:textfield name="form.abbreviation" size="60"/>
				<s:fielderror fieldName="form.abbreviation"/>
			</td>
		</tr>
		<tr>
			<td><loc:message name="fieldName"/>:</td>
			<td>
				<s:textfield name="form.label" size="60"/>
				<s:fielderror fieldName="form.label"/>
			</td>
		</tr>
		<tr>
			<td><loc:message name="fieldType"/>:</td>
			<td>
				<s:if test="form.examPref == true">
					<loc:message name="itemDistTypeExams"/>
				</s:if><s:else>
					<loc:message name="itemDistTypeCourses"/>
				</s:else>
				<s:hidden name="form.examPref"/>
				<s:fielderror fieldName="form.examPref"/>
			</td>
		</tr>
		<tr>
			<td><loc:message name="fieldVisible"/>:</td>
			<td>
				<s:checkbox name="form.visible"/>
				<s:fielderror fieldName="form.visible"/>
			</td>
		</tr>
		<s:if test="form.examPref == false">
			<tr>
				<td><loc:message name="fieldAllowInstructorPreference"/>:</td>
				<td>
					<s:checkbox name="form.instructorPref"/>
					<s:fielderror fieldName="form.instructorPref"/>
				</td>
			</tr>
		</s:if><s:else>
			<s:hidden name="form.instructorPref"/>			
		</s:else>
		<s:if test="form.examPref == false">
			<tr>
				<td><loc:message name="fieldAllowInstructorSurvey"/>:</td>
				<td>
					<s:checkbox name="form.survey"/>
					<s:fielderror fieldName="form.survey"/>
				</td>
			</tr>
		</s:if><s:else>
			<s:hidden name="form.survey"/>			
		</s:else>
		<tr>
			<td><loc:message name="fieldSequencingRequired"/>:</td>
			<td>
				<s:if test="form.sequencingRequired == true">
					<loc:message name="yes"/>
				</s:if><s:else>
					<loc:message name="no"/>
				</s:else>
				<s:hidden name="form.sequencingRequired"/>
				<s:fielderror fieldName="form.sequencingRequired"/>
			</td>
		</tr>
		<tr>
			<td><loc:message name="fieldAllowPreferences"/>:</td>
			<td>
				<s:iterator value="#request.prefLevelsList" var="p" status="stat"><s:set var="idx" value="%{#stat.index}"/>
					<s:if test="#idx > 0"><br></s:if>
					<s:checkbox name="form.allowedPreference[%{#p.prefId}]"/><s:property value="#p.prefName"/>
				</s:iterator>
				<s:fielderror fieldName="form.allowedPref"/>
			</td>
		</tr>
		<tr>
			<td><loc:message name="fieldDescription"/>:</td>
			<td>
				<s:textarea name="form.description" rows="4" cols="160"/>
				<s:fielderror fieldName="form.description"/>
			</td>
		</tr>
		
		<TR>
			<TD colspan='2'>
				&nbsp;
			</TD>
		</TR>

		<TR>
			<TD colspan='2'>
			<tt:section-title><loc:message name="sectRestrictAccess"/></tt:section-title>
			</TD>
		</TR>

		<TR>
			<TD valign="top"><loc:message name="propDepartments"/></TD>
			<TD>
				<s:iterator value="form.departmentIds" var="deptId" status="stat"><s:set var="idx" value="%{#stat.index}"/>
					<s:hidden name="form.departmentIds[%{#idx}]"/>
					<s:iterator value="#request.deptsList" var="dept">
						<s:if test="#dept.value == #deptId">
							<s:property value="#dept.label"/><br>
						</s:if>
					</s:iterator>
				</s:iterator>
				<s:select name="form.departmentId"
					list="#request.deptsList" listKey="value" listValue="label"
					headerKey="-1" headerValue="%{#msg.itemSelect()}"/>
				<s:submit name='op' value='%{#msg.actionAddDepartment()}'/>
				<s:submit name='op' value='%{#msg.actionRemoveDepartment()}'/>
				<s:fielderror fieldName="form.department"/>
			</TD>
		</TR>
		
		<tr>
			<td valign="middle" colspan="2">
				<tt:section-title/>
			</td>
		</tr>
		
		<TR>
			<TD colspan='2' align='right'>
				<s:submit name='op' value='%{#msg.actionUpdateDistributionType()}'/>
				<s:submit name='op' value='%{#msg.actionBackToDistributionTypes()}'/>
			</TD>
		</TR>
	</table>	
</s:form>
</loc:bundle>