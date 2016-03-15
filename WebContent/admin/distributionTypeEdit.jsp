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
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%> 
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>

<html:form action="/distributionTypeEdit">
	<html:hidden property="uniqueId"/>
	<table width="100%" border="0" cellspacing="0" cellpadding="3">	
		<TR>
			<TD colspan='2'>
			<tt:section-header>
				<tt:section-title>
					<bean:write name="distributionTypeEditForm" property="reference"/>
					<html:hidden property="reference"/>
				</tt:section-title>
				<html:submit property="op" value="Save"/>
				<html:submit property="op" value="Back"/>
			</tt:section-header>
			</TD>
		</TR>

		<tr>
			<td>Id:</td>
			<td>
				<bean:write name="distributionTypeEditForm" property="requirementId"/>
				<html:hidden property="requirementId"/>
				<html:errors property="requirementId"/>
			</td>
		</tr>
		<tr>
			<td>Abbreviation:</td>
			<td>
				<html:text property="abbreviation" size="60"/>
				<html:errors property="abbreviation"/>
			</td>
		</tr>
		<tr>
			<td>Name:</td>
			<td>
				<html:text property="label" size="60"/>
				<html:errors property="label"/>
			</td>
		</tr>
		<tr>
			<td>Type:</td>
			<td>
				<logic:equal name="distributionTypeEditForm" property="examPref" value="false">
					Course
				</logic:equal>
				<logic:equal name="distributionTypeEditForm" property="examPref" value="true">
					Examination
				</logic:equal>
				<html:hidden property="examPref"/>
				<html:errors property="examPref"/>
			</td>
		</tr>
		<tr>
			<td>Visible:</td>
			<td>
				<html:checkbox property="visible"/>
				<html:errors property="visible"/>
			</td>
		</tr>
		<logic:equal name="distributionTypeEditForm" property="examPref" value="false">
			<tr>
				<td>Allow Instructor Preference:</td>
				<td>
					<html:checkbox property="instructorPref"/>
					<html:errors property="instructorPref"/>
				</td>
			</tr>
		</logic:equal>
		<logic:equal name="distributionTypeEditForm" property="examPref" value="true">
			<html:hidden property="instructorPref"/>			
		</logic:equal>
		<tr>
			<td>Sequencing Required:</td>
			<td>
				<logic:equal name="distributionTypeEditForm" property="sequencingRequired" value="true">
					Yes
				</logic:equal>
				<logic:equal name="distributionTypeEditForm" property="sequencingRequired" value="false">
					No
				</logic:equal>
				<html:hidden property="sequencingRequired"/>
				<html:errors property="sequencingRequired"/>
			</td>
		</tr>
		<tr>
			<td>Allow Preferences:</td>
			<td>
				<html:text property="allowedPref" size="10"/>
				<html:errors property="allowedPref"/>
			</td>
		</tr>
		<tr>
			<td>Description:</td>
			<td>
				<html:textarea property="description" rows="4" cols="160"/>
				<html:errors property="description"/>
			</td>
		</tr>
		
		<TR>
			<TD colspan='2'>
				&nbsp;
			</TD>
		</TR>

		<TR>
			<TD colspan='2'>
			<tt:section-title>Restrict Access</tt:section-title>
			</TD>
		</TR>

		<TR>
			<TD valign="top">Departments:</TD>
			<TD>
				<logic:iterate name="distributionTypeEditForm" property="departmentIds" id="deptId">
					<logic:iterate scope="request" name="<%=Department.DEPT_ATTR_NAME%>" id="dept">
						<logic:equal name="dept" property="value" value="<%=deptId.toString()%>">
							<bean:write name="dept" property="label"/>
							<input type="hidden" name="depts" value="<%=deptId%>">
							<BR>
						</logic:equal>
					</logic:iterate>
				</logic:iterate>
				<html:select property="departmentId">
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:options collection="<%=Department.DEPT_ATTR_NAME%>" property="value" labelProperty="label"/>
				</html:select>
				&nbsp;
				<html:submit property="op" value="Add Department"/>
				&nbsp;
				<html:submit property="op" value="Remove Department"/>
				&nbsp;
				<html:errors property="department"/>
			</TD>
		</TR>
		
		<tr>
			<td valign="middle" colspan="2">
				<tt:section-title/>
			</td>
		</tr>
		
		<TR>
			<TD colspan='2' align='right'>
				<html:submit property="op" value="Save"/>
				<html:submit property="op" value="Back"/>
			</TD>
		</TR>
	</table>	
</html:form>
