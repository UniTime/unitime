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
<tt:confirm name="confirmDelete"><loc:message name="confirmDepartmentDelete"/></tt:confirm>
<s:form action="departmentEdit">
	<table class="unitime-MainTable">
 	<s:hidden name="form.id"/>
	<s:hidden name="form.sessionId"/>
	<s:hidden name="form.fullyEditable"/>

		<TR>
			<TD colspan="2">
			
				<tt:section-header>
					<tt:section-title>
						<s:if test="form.id == null">
							<loc:message name="sectAddDepartment"/>
						</s:if><s:else>
							<loc:message name="sectEditDepartment"/>
						</s:else>
					</tt:section-title>
					
					<s:if test="form.id == null">
						<tt:button value="%{#msg.buttonSave()}"/>
					</s:if><s:else>
						<tt:button value="%{#msg.buttonUpdate()}"/>
						<sec:authorize access="hasPermission(#form.id, 'Department', 'DepartmentDelete')">
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
	
	<s:if test="form.fullyEditable == false">
		<TR>
			<TD width='10%'><loc:message name="propDeptCode"/></TD>
			<TD>
				<s:hidden name="form.deptCode"/>
				<s:property value="form.deptCode"/>
			</TD>
		</TR>
		
		<TR>
			<TD><loc:message name="propAbbreviation"/></TD>
			<TD>
				<s:hidden name="form.abbv"/>
				<s:property value="form.abbv"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propName"/></TD>
			<TD>
				<s:hidden name="form.name"/>
				<s:property value="form.name"/>
			</TD>
		</TR>

		<TR>
			<TD nowrap><loc:message name="propDepartmentStatus"/></TD>
			<TD>
				<s:hidden name="form.statusType"/>
				<s:if test="form.statusType == null || form.statusType.isEmpty()">
					<i><loc:message name="propDepartmentStatusDefault"/></i>
				</s:if><s:else>
					<s:iterator value="form.statusOptions" var="statusOption">
						<s:if test="#statusOption.reference == form.statusType">
							<s:property value="#statusOption.label"/>
						</s:if>
					</s:iterator>
				</s:else>
			</TD>
		</TR>
		
		<s:hidden name="form.externalId"/>
		<s:if test="form.externalId != null && !form.externalId.isEmpty()">
		<TR>
			<TD><loc:message name="propExternalId"/></TD>
			<TD>
				<s:property value="form.externalId"/>
			</TD>
		</TR>
		</s:if>
		
		<s:hidden name="form.isExternal"/>
		<s:hidden name="form.extAbbv"/>
		<s:hidden name="form.extName"/>
		<s:if test="form.isExternal == true">
			<TR>
				<TD nowrap><loc:message name="propExternalManagerAbbreviation"/></TD>
				<TD><s:property value="form.extAbbv"/></TD>
			</TR>
			<TR>
				<TD nowrap><loc:message name="propExternalManagerName"/></TD>
				<TD><s:property value="form.extName"/></TD>
			</TR>		
		</s:if>
		
		<TR>
			<TD nowrap><loc:message name="propPrefPriority"/></TD>
			<TD>
				<s:hidden name="form.distPrefPriority"/>
				<s:property value="form.distPrefPriority"/>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap><loc:message name="propAllowReqTime"/></TD>
			<TD>
				<s:hidden name="form.allowReqTime"/>
				<s:if test="form.allowReqTime == true"><img src="images/accept.png" border="0"></s:if>
				<s:else><img src="images/cross.png" border="0"></s:else>
			</TD>
		</TR>

		<TR>
			<TD nowrap><loc:message name="propAllowReqRoom"/></TD>
			<TD>
				<s:hidden name="form.allowReqRoom"/>
				<s:if test="form.allowReqRoom == true"><img src="images/accept.png" border="0"></s:if>
				<s:else><img src="images/cross.png" border="0"></s:else>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap><loc:message name="propAllowReqDist"/></TD>
			<TD>
				<s:hidden name="form.allowReqDist"/>
				<s:if test="form.allowReqDist == true"><img src="images/accept.png" border="0"></s:if>
				<s:else><img src="images/cross.png" border="0"></s:else>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap><loc:message name="propInheritInstructorPref"/></TD>
			<TD>
				<s:hidden name="form.inheritInstructorPreferences"/>
				<s:if test="form.inheritInstructorPreferences == true"><img src="images/accept.png" border="0"></s:if>
				<s:else><img src="images/cross.png" border="0"></s:else>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap><loc:message name="propAllowEvents"/></TD>
			<TD>
				<s:hidden name="form.allowEvents"/>
				<s:if test="form.allowEvents == true"><img src="images/accept.png" border="0"></s:if>
				<s:else><img src="images/cross.png" border="0"></s:else>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap><loc:message name="propAllowStudentScheduling"/></TD>
			<TD>
				<s:hidden name="form.allowStudentScheduling"/>
				<s:if test="form.allowStudentScheduling == true"><img src="images/accept.png" border="0"></s:if>
				<s:else><img src="images/cross.png" border="0"></s:else>
			</TD>
		</TR>
	</s:if><s:else>
		<TR>
			<TD width='10%'><loc:message name="propDeptCode"/></TD>
			<TD>
				<s:textfield name="form.deptCode" size="50" maxlength="50"/>
				<s:fielderror fieldName="form.deptCode" escape="false"/>
			</TD>
		</TR>
		
		<TR>
			<TD><loc:message name="propAbbreviation"/></TD>
			<TD>
				<s:textfield name="form.abbv" size="20" maxlength="20"/>
				<s:fielderror fieldName="form.abbv" escape="false"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propName"/></TD>
			<TD>
				<s:textfield name="form.name" size="100" maxlength="100"/>
				<s:fielderror fieldName="form.name" escape="false"/>
			</TD>
		</TR>

		<TR>
			<TD nowrap><loc:message name="propDepartmentStatus"/></TD>
			<TD>
				<s:select name="form.statusType"
					list="form.statusOptions" listKey="reference" listValue="label"
					headerKey="-1" headerValue="%{#msg.propDepartmentStatusDefault()}"/>
				<s:fielderror fieldName="form.statusType" escape="false"/>
			</TD>
		</TR>
		
		<TR>
			<TD><loc:message name="propExternalId"/></TD>
			<TD>
				<s:textfield name="form.externalId" size="40" maxlength="40"/>
				<s:fielderror fieldName="form.externalId" escape="false"/>
			</TD>
		</TR>
		<TR>
			<TD nowrap><loc:message name="propExternalManager"/></TD>
			<TD>
				<s:if test="form.id == null">
					<s:checkbox name="form.isExternal"/>
				</s:if><s:else>
					<sec:authorize access="hasPermission(#form.id, 'Department', 'DepartmentEditChangeExternalManager')">
						<s:checkbox name="form.isExternal"/>
					</sec:authorize>
					<sec:authorize access="!hasPermission(#form.id, 'Department', 'DepartmentEditChangeExternalManager')">
						<s:checkbox name="form.isExternal" disabled="true"/>
						<s:hidden name="form.isExternal"/>
					</sec:authorize>
				</s:else>
				<s:fielderror fieldName="form.isExternal" escape="false"/>
			</TD>
		</TR>

		<TR>
			<TD nowrap><loc:message name="propExternalManagerAbbreviation"/></TD>
			<TD>
				<s:textfield name="form.extAbbv" size="10" maxlength="10"/>
				<s:fielderror fieldName="form.extAbbv" escape="false"/>
			</TD>
		</TR>

		<TR>
			<TD nowrap><loc:message name="propExternalManagerName"/></TD>
			<TD>
				<s:textfield name="form.extName" size="30" maxlength="30"/>
				<s:fielderror fieldName="form.extName" escape="false"/>
			</TD>
		</TR>

		<TR>
			<TD nowrap><loc:message name="propPrefPriority"/></TD>
			<TD>
				<s:textfield name="form.distPrefPriority" size="10" maxlength="5"/>
				<s:fielderror fieldName="form.distPrefPriority" escape="false"/>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap><loc:message name="propAllowReqTime"/></TD>
			<TD>
				<s:checkbox name="form.allowReqTime"/>
				<s:fielderror fieldName="form.allowReqTime" escape="false"/>
			</TD>
		</TR>

		<TR>
			<TD nowrap><loc:message name="propAllowReqRoom"/></TD>
			<TD>
				<s:checkbox name="form.allowReqRoom"/>
				<s:fielderror fieldName="form.allowReqRoom" escape="false"/>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap><loc:message name="propAllowReqDist"/></TD>
			<TD>
				<s:checkbox name="form.allowReqDist"/>
				<s:fielderror fieldName="form.allowReqDist" escape="false"/>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap><loc:message name="propInheritInstructorPref"/></TD>
			<TD>
				<s:checkbox name="form.inheritInstructorPreferences"/>
				<s:fielderror fieldName="form.inheritInstructorPreferences" escape="false"/>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap><loc:message name="propAllowEvents"/></TD>
			<TD>
				<s:checkbox name="form.allowEvents"/>
				<s:fielderror fieldName="form.allowEvents" escape="false"/>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap><loc:message name="propAllowStudentScheduling"/></TD>
			<TD>
				<s:checkbox name="form.allowStudentScheduling"/>
				<s:fielderror fieldName="form.allowStudentScheduling" escape="false"/>
			</TD>
		</TR>
	</s:else>
		
		<s:if test="!form.dependentDepartments.isEmpty()">
			<TR><TD colspan='2'>&nbsp;</TD></TR>
			<TR>
				<TD valign="middle" colspan='2'>
					<A id='ExternalDepartmentStatuses'></A>
					<tt:section-header title="${MSG.sectControllingDepartmentStatuses()}">
						<tt:button value="%{#msg.buttonDependentAddStatus()}"/>
						<tt:button value="%{#msg.buttonDependentDeleteAll()}"/>
					</tt:section-header>
				</TD>
			</TR>
			<s:hidden name="deleteId" value="" id="deleteId"/>
			<TR>
				<TD colspan="2" align="left">
					<TABLE class="unitime-Table">
						<TR>
							<TD><I><loc:message name="colControllingDepartment"/></I></TD>
							<TD colspan='2'><I><loc:message name="propStatusManagedBy">
								<s:if test="form.id == null"><loc:message name="thisDepartment"/></s:if>
								<s:else><s:property value="form.deptCode"/> - <s:property value="form.name"/></s:else>
							</loc:message>
							</I></TD>
						</TR>
						<s:iterator value="form.dependentDepartments" var="dept" status="stat"><s:set var="ctr" value="#stat.index"/>
							<TR>
								<TD align="left" nowrap>
									<s:select name="form.dependentDepartments[%{#ctr}]" style="min-width:300px;"
										list="#request.deptsList" listKey="uniqueId" listValue="label"
										headerKey="-1" headerValue="-"/>
								</TD>
								<TD>
									<s:select name="form.dependentStatuses[%{#ctr}]" style="min-width:300px;"
										list="form.statusOptions" listKey="reference" listValue="label"
										headerKey="" headerValue="%{#msg.propDefaultDependentStatus()}"/>
								</TD>
								<TD>
									<tt:button value="%{#msg.buttonDeleteLine()}" onclick="document.getElementById('deleteId').value='%{#ctr}';"/>
								</TD>
							</TR>
						</s:iterator>
					</TABLE>
				</TD>
			</TR>
		</s:if>

		<TR>
			<TD colspan="2">
			<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
			</TD>
		</TR>
		
		<TR>
			<TD align="right" colspan='2'>
				<s:if test="form.id == null">
					<tt:button value="%{#msg.buttonSave()}"/>
				</s:if><s:else>
					<tt:button value="%{#msg.buttonUpdate()}"/>
					<sec:authorize access="hasPermission(#form.id, 'Department', 'DepartmentDelete')">
						<tt:button value="%{#msg.buttonDelete()}" onclick="return confirmDelete();"/>	
					</sec:authorize>
				</s:else>
				<tt:button value="%{#msg.buttonBack()}"/>
			</TD>
		</TR>
	</table>
</s:form>
</loc:bundle>