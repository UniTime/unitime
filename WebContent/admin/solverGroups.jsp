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
<s:form action="solverGroupEdit">
<tt:confirm name="confirmDelete"><loc:message name="confirmDeleteSolverGroup"/></tt:confirm>
<tt:confirm name="confirmDeleteAll"><loc:message name="confirmDeleteAllSolverGroups"/></tt:confirm>
<tt:confirm name="confirmAutoSetup"><loc:message name="confirmCreateNewSolverGroups"/></tt:confirm>
<s:hidden name="form.uniqueId"/><s:fielderror fieldName="form.uniqueId"/>
<s:hidden name="form.departmentsEditable"/><s:fielderror fieldName="form.departmentsEditable"/>
<s:if test="form.op != 'List'">
	<table class="unitime-MainTable">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<s:if test="form.op == #msg.actionSaveSolverGroup()">
							<loc:message name="sectAddSolverGroup"/>
						</s:if><s:else>
							<loc:message name="sectEditSolverGroup"/>
						</s:else>
					</tt:section-title>
					<s:if test="form.op == #msg.actionSaveSolverGroup()">
						<s:submit name='op' value='%{#msg.actionSaveSolverGroup()}'/>
					</s:if><s:else>
						<s:submit name='op' value='%{#msg.actionUpdateSolverGroup()}'/>
						<s:if test="form.departmentsEditable == true">
							<s:submit name='op' value='%{#msg.actionDeleteSolverGroup()}' onclick="return confirmDelete();"/>
						</s:if>
					</s:else>
					<s:submit name='op' value='%{#msg.actionBackToSolverGroups()}'/>
				</tt:section-header>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="fieldAbbreviation"/>:</TD>
			<TD>
				<s:textfield name="form.abbv" size="50" maxlength="50"/>
				&nbsp;<s:fielderror fieldName="form.abbv"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="fieldName"/>:</TD>
			<TD>
				<s:textfield name="form.name" size="50" maxlength="50"/>
				&nbsp;<s:fielderror fieldName="form.name"/>
			</TD>
		</TR>
		
		<s:if test="form.departmentsEditable == false">
			<TR><TD colspan='2'>&nbsp;</TD></TR>
			<TR><TD colspan='2'><tt:section-header title="${MSG.fieldDepartments()}"/></TD></TR>
			<s:iterator value="form.departmentIds" var="departmentId" status="stat"><s:set var="ctr" value="#stat.index"/>
				<s:hidden name="form.departmentIds[%{#ctr}]"/>
				<s:hidden name="form.departmentNames[%{#ctr}]"/>
				<s:hidden name="form.assignedDepartments[%{#ctr}]"/>
				<s:if test="form.assignedDepartments[#ctr] == true">
					<TR><TD colspan='2' style="padding-left: 20px;">
						<s:property value="form.departmentNames[#ctr]"/>
					</TD></TR>
				</s:if>
			</s:iterator>
		</s:if><s:else>
			<s:if test="form.op != #msg.actionSaveSolverGroup()">
				<TR><TD colspan='2'>&nbsp;</TD></TR>
				<TR><TD colspan='2'><tt:section-header title="${MSG.sectAssignedDepartments()}"/></TD></TR>
				<s:iterator value="form.departmentIds" var="departmentId" status="stat"><s:set var="ctr" value="#stat.index"/>
					<s:if test="form.assignedDepartments[#ctr] == true">
						<TR><TD colspan='2' style="padding-left: 20px;">
							<s:hidden name="form.departmentIds[%{#ctr}]"/>
							<s:hidden name="form.departmentNames[%{#ctr}]"/>
							<s:checkbox name="form.assignedDepartments[%{#ctr}]"/>
							<s:property value="form.departmentNames[#ctr]"/>
						</TD></TR>
					</s:if>
				</s:iterator>
			</s:if>
		</s:else>
		
		<s:if test="form.op != #msg.actionSaveSolverGroup()">
			<TR><TD colspan='2'>&nbsp;</TD></TR>
			<TR><TD colspan='2'><tt:section-header title="${MSG.sectAssignedManagers()}"/></TD></TR>
			<s:iterator value="form.managerIds" var="managerId" status="stat"><s:set var="ctr" value="#stat.index"/>
				<s:if test="form.assignedManagers[#ctr] == true">
					<TR><TD colspan='2' style="padding-left: 20px;">
						<s:hidden name="form.managerIds[%{#ctr}]"/>
						<s:hidden name="form.managerNames[%{#ctr}]"/>
						<s:checkbox name="form.assignedManagers[%{#ctr}]"/>
						<s:property value="form.managerNames[#ctr]" escapeHtml="false"/>
					</TD></TR>
				</s:if>
			</s:iterator>
		</s:if>
		
		<s:if test="form.departmentsEditable == true">
			<TR><TD colspan='2'>&nbsp;</TD></TR>
			<TR><TD colspan='2'><tt:section-header title="${MSG.sectNotAssignedDepartments()}"/></TD></TR>
			<s:iterator value="form.departmentIds" var="departmentId" status="stat"><s:set var="ctr" value="#stat.index"/>
				<s:if test="form.assignedDepartments[#ctr] == false">
					<TR><TD colspan='2' style="padding-left: 20px;">
						<s:hidden name="form.departmentIds[%{#ctr}]"/>
						<s:hidden name="form.departmentNames[%{#ctr}]"/>
						<s:checkbox name="form.assignedDepartments[%{#ctr}]"/>
						<s:property value="form.departmentNames[#ctr]"/>
					</TD></TR>
				</s:if>
			</s:iterator>
		</s:if>
		
		<TR><TD colspan='2'>&nbsp;</TD></TR>
		<TR><TD colspan='2'><tt:section-header title="${MSG.sectNotAssignedManagers()}"/></TD></TR>
		<s:iterator value="form.managerIds" var="managerId" status="stat"><s:set var="ctr" value="#stat.index"/>
			<s:if test="form.assignedManagers[#ctr] == false">
				<TR><TD colspan='2' style="padding-left: 20px;">
					<s:hidden name="form.managerIds[%{#ctr}]"/>
					<s:hidden name="form.managerNames[%{#ctr}]"/>
					<s:checkbox name="form.assignedManagers[%{#ctr}]"/>
					<s:property value="form.managerNames[#ctr]" escapeHtml="false"/>
				</TD></TR>
			</s:if>
		</s:iterator>

		<TR><TD colspan='2'><tt:section-header/></TD></TR>
		<TR>
			<TD align="right" colspan="2">
				<s:if test="form.op == #msg.actionSaveSolverGroup()">
					<s:submit name='op' value='%{#msg.actionSaveSolverGroup()}'/>
				</s:if><s:else>
					<s:submit name='op' value='%{#msg.actionUpdateSolverGroup()}'/>
					<s:if test="form.departmentsEditable == true">
						<s:submit name='op' value='%{#msg.actionDeleteSolverGroup()}' onclick="return confirmDelete();"/>
					</s:if>
				</s:else>
				<s:submit name='op' value='%{#msg.actionBackToSolverGroups()}'/>			
			</TD>
		</TR>
	</TABLE>
</s:if><s:else>
	<table class="unitime-MainTable">
		<TR>
			<TD align="right" colspan="5">
				<tt:section-header>
					<tt:section-title>
						<s:property value="title"/>
					</tt:section-title>
				<s:submit name='op' value='%{#msg.actionAddSolverGroup()}'/>
				<s:submit name='op' onclick="return confirmDeleteAll();" value='%{#msg.actionDeleteAllSolverGroups()}' title="%{#msg.titleDeleteAllSolverGroups()}"/>
				<s:submit name='op' onclick="return confirmAutoSetup();" value='%{#msg.actionAutoSetupSolverGroups()}' title="%{#msg.titleAutoSetupSolverGroups()}"/>
				<s:submit name='op' value='%{#msg.actionExportPdf()}'/>
				<s:submit name='op' value='%{#msg.actionExportCsv()}'/>
				</tt:section-header>
			</TD>
		</TR>
		<s:property value="#request.table" escapeHtml="false"/>
		<TR>
			<TD align="right" class="WelcomeRowHead" colspan="5">&nbsp;</TD>
		</TR>
		<TR>
			<TD align="right" colspan="5">
				<s:submit name='op' value='%{#msg.actionAddSolverGroup()}'/>
				<s:submit name='op' onclick="return confirmDeleteAll();" value='%{#msg.actionDeleteAllSolverGroups()}' title="%{#msg.titleDeleteAllSolverGroups()}"/>
				<s:submit name='op' onclick="return confirmAutoSetup();" value='%{#msg.actionAutoSetupSolverGroups()}' title="%{#msg.titleAutoSetupSolverGroups()}"/>
				<s:submit name='op' value='%{#msg.actionExportPdf()}'/>
				<s:submit name='op' value='%{#msg.actionExportCsv()}'/>
			</TD>
		</TR>
	</TABLE>
	<s:if test="#request.hash != null">
		<SCRIPT type="text/javascript">
			location.hash = '<%=request.getAttribute("hash")%>';
		</SCRIPT>
	</s:if>
</s:else>

</s:form>
</loc:bundle>