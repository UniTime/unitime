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
<%@page import="org.unitime.timetable.model.Department"%>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%> 
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<tt:session-context/>
<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(sessionContext) %>
		
		function confirmDelete() {
			if (jsConfirm!=null && !jsConfirm)
				return true;

			if(confirm('The department and all associated data will be deleted. Continue?')) {
				return true;
			}
			return false;
		}

	// -->
</SCRIPT>

<html:form action="/departmentEdit">
	<html:hidden property="id"/>
	<html:hidden property="sessionId"/>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">

		<TR>
			<TD colspan="2">
			
				<tt:section-header>
					<tt:section-title>
						<logic:notEmpty name="departmentEditForm" property="id">
							<bean:write name="departmentEditForm" property="deptCode"/> 
							- 
							<bean:write name="departmentEditForm" property="name"/>
						</logic:notEmpty>
					</tt:section-title>
						
					<logic:empty name="departmentEditForm" property="id">
						<html:submit property="op" styleClass="btn" accesskey="S" titleKey="title.saveDepartment">
							<bean:message key="button.saveDepartment"/>
						</html:submit>
					</logic:empty>

					<logic:notEmpty name="departmentEditForm" property="id">
						<html:submit property="op" styleClass="btn" accesskey="U" titleKey="title.updateDepartment">
							<bean:message key="button.updateDepartment"/>
						</html:submit>
						<sec:authorize access="hasPermission(#departmentEditForm.id, 'Department', 'DepartmentDelete')">
							<html:submit property="op" onclick="return confirmDelete();" styleClass="btn" accesskey="D" titleKey="title.deleteDepartment">
								<bean:message key="button.deleteDepartment"/>
							</html:submit>
						</sec:authorize>
					</logic:notEmpty>

					<html:submit property="op" styleClass="btn" accesskey="B" titleKey="title.backToPrevious">
						<bean:message key="button.backToPrevious"/>
					</html:submit>
				</tt:section-header>				
			</TD>
		</TR>
		
		<logic:messagesPresent>
			<TR>
				<TD colspan="2" align="left" class="errorCell">
						<B><U>ERRORS</U></B><BR>
					<BLOCKQUOTE>
					<UL>
					    <html:messages id="error">
					      <LI>
							${error}
					      </LI>
					    </html:messages>
				    </UL>
				    </BLOCKQUOTE>
				</TD>
			</TR>
		</logic:messagesPresent>

		<TR>
			<TD>Academic Session: </TD>
			<TD><%= sessionContext.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel() %></TD>
		</TR>

		<TR>
			<TD width='10%'>Code:</TD>
			<TD>
				<html:text property="deptCode" size="50" maxlength="50"/>
			</TD>
		</TR>
		
		<TR>
			<TD>Abbreviation:</TD>
			<TD>
				<html:text property="abbv" size="20" maxlength="20"/>
			</TD>
		</TR>

		<TR>
			<TD>Name:</TD>
			<TD>
				<html:text property="name" size="100" maxlength="100"/>
			</TD>
		</TR>

		<TR>
			<TD nowrap>Department Status:</TD>
			<TD>
				<html:select property="statusType">
					<html:option value="">Session Default</html:option>
					<html:optionsCollection property="statusOptions" value="reference" label="label" />
				</html:select>
			</TD>
		</TR>
		
		<TR>
			<TD>External ID:</TD>
			<TD>
				<html:text property="externalId" size="40" maxlength="40"/>
			</TD>
		</TR>
		<TR>
			<TD nowrap>External Manager:</TD>
			<TD>
				<logic:empty name="departmentEditForm" property="id">
					<html:checkbox property="isExternal"/>
				</logic:empty>
				<logic:notEmpty name="departmentEditForm" property="id">
					<sec:authorize access="hasPermission(#departmentEditForm.id, 'Department', 'DepartmentEditChangeExternalManager')">
						<html:checkbox property="isExternal"/>
					</sec:authorize>
					<sec:authorize access="!hasPermission(#departmentEditForm.id, 'Department', 'DepartmentEditChangeExternalManager')">
						<html:checkbox property="isExternal" disabled="true"/>
						<html:hidden property="isExternal"/>
					</sec:authorize>
				</logic:notEmpty>
			</TD>
		</TR>

		<TR>
			<TD nowrap>External Manager Abbreviation:</TD>
			<TD>
				<html:text property="extAbbv" size="10" maxlength="10"/>
			</TD>
		</TR>

		<TR>
			<TD nowrap>External Manager Name:</TD>
			<TD>
				<html:text property="extName" size="30" maxlength="30"/>
			</TD>
		</TR>

		<TR>
			<TD nowrap>Distribution Preference Priority:</TD>
			<TD>
				<html:text property="distPrefPriority" size="10" maxlength="5"/>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap>Allow Required Time:</TD>
			<TD>
				<html:checkbox property="allowReqTime"/>
			</TD>
		</TR>

		<TR>
			<TD nowrap>Allow Required Room:</TD>
			<TD>
				<html:checkbox property="allowReqRoom"/>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap>Allow Required Distribution:</TD>
			<TD>
				<html:checkbox property="allowReqDist"/>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap>Inherit Instructor Preferences:</TD>
			<TD>
				<html:checkbox property="inheritInstructorPreferences"/>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap>Event Management:</TD>
			<TD>
				<html:checkbox property="allowEvents"/>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap>Student Scheduling:</TD>
			<TD>
				<html:checkbox property="allowStudentScheduling"/>
			</TD>
		</TR>
		
		<logic:notEmpty name="departmentEditForm" property="dependentDepartments">
			<TR><TD colspan='2'>&nbsp;</TD></TR>
			<TR>
				<TD valign="middle" colspan='2'>
					<A name='ExternalDepartmentStatuses'></A>
					<tt:section-header title="Controlling Department Statuses">
						<html:submit property="op" styleClass="btn" accesskey="A" title="Add Controlling Department Status (Alt+A)">Add Status</html:submit> 			
					</tt:section-header>
				</TD>
			</TR>
			<INPUT type="hidden" name="deleteId" id="deleteId" value="">
			<TR>
				<TD colspan="2" align="left">
					<TABLE align="left" cellspacing="0" cellpadding="2" border="0">
						<TR>
							<TD><I>Controlling Department</I></TD>
							<TD colspan='2'><I>Status for classes managed by
							<logic:empty name="departmentEditForm" property="id">this department</logic:empty>
							<logic:notEmpty name="departmentEditForm" property="id"><bean:write name="departmentEditForm" property="deptCode"/> - <bean:write name="departmentEditForm" property="name"/></logic:notEmpty>
							</I></TD>
						</TR>
						<logic:iterate name="departmentEditForm" property="dependentDepartments" id="dept" indexId="ctr">
							<TR>
								<TD align="left" nowrap>
									<html:select property='<%= "dependentDepartments[" + ctr + "]"%>'>
										<html:option value="-1">-</html:option>
										<html:options collection="<%=Department.DEPT_ATTR_NAME%>" property="uniqueId" labelProperty="label" />
									</html:select>
								</TD>
								<TD>
									<html:select property='<%= "dependentStatuses[" + ctr + "]"%>'>
										<html:option value="">Department/Session Default</html:option>
										<html:optionsCollection property="statusOptions" value="reference" label="label" />
									</html:select>
								</TD>
								<TD>
									<html:submit property="op" styleClass="btn" title="Remove controlling department" onclick="<%=\"javascript: document.getElementById('deleteId').value = \" + ctr + \";\"%>">Delete</html:submit>
								</TD>
							</TR>
						</logic:iterate>
					</TABLE>
				</TD>
			</TR>
		</logic:notEmpty>

		<TR>
			<TD colspan="2">
			<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
			</TD>
		</TR>
		
		<TR>
			<TD align="right" colspan='2'>
				<logic:empty name="departmentEditForm" property="id">
					<html:submit property="op" styleClass="btn" accesskey="S" titleKey="title.saveDepartment">
						<bean:message key="button.saveDepartment"/>
					</html:submit>
				</logic:empty>

				<logic:notEmpty name="departmentEditForm" property="id">
					<html:submit property="op" styleClass="btn" accesskey="U" titleKey="title.updateDepartment">
						<bean:message key="button.updateDepartment"/>
					</html:submit>
					<sec:authorize access="hasPermission(#departmentEditForm.id, 'Department', 'DepartmentDelete')">
						<html:submit property="op" onclick="return confirmDelete();" styleClass="btn" accesskey="D" titleKey="title.deleteDepartment">
							<bean:message key="button.deleteDepartment"/>
						</html:submit>
					</sec:authorize>
				</logic:notEmpty>	
					
				<html:submit property="op" styleClass="btn" accesskey="B" titleKey="title.backToPrevious">
					<bean:message key="button.backToPrevious"/>
				</html:submit>
			</TD>
		</TR>
		
	</TABLE>
</html:form>
