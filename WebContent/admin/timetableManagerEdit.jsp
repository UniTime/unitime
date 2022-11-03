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
<tt:confirm name="confirmDelete"><loc:message name="confirmDeleteManager"/></tt:confirm>
<s:form action="timetableManagerEdit">
	<s:hidden name="form.uniqueId"/>
	<s:hidden name="form.op"/>
	<s:hidden name="deleteId" value="" id="deleteId"/>
	<s:hidden name="deleteType" value="" id="deleteType"/>

	
	<table class="unitime-MainTable">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<s:property value="form.firstName"/>
						<s:property value="form.middleName"/>
						<s:property value="form.lastName"/>
					</tt:section-title>
					<s:if test="form.uniqueId == null">
						<s:submit name="op" value="%{#msg.actionSaveManager()}"
							accesskey="%{#msg.acessSaveManager()}" title="%{#msg.titleSaveManager(#msg.acessSaveManager())}"/>
					</s:if><s:else>
						<s:submit name="op" value="%{#msg.actionUpdateManager()}"
							accesskey="%{#msg.acessUpdateManager()}" title="%{#msg.titleUpdateManager(#msg.acessUpdateManager())}"/>
						<sec:authorize access="hasPermission(#form.uniqueId, 'TimetableManager', 'TimetableManagerDelete')">
							<s:submit name="op" value="%{#msg.actionDeleteManager()}"
							accesskey="%{#msg.acessDeleteManager()}" title="%{#msg.titleDeleteManager(#msg.acessDeleteManager())}"
							onclick="return confirmDelete();"/>
						</sec:authorize>
					</s:else>
					<s:submit name="op" value="%{#msg.actionBackToManagers()}"
							accesskey="%{#msg.acessBackToManagers()}" title="%{#msg.titleBackToManagers(#msg.acessBackToManagers())}"/>
				</tt:section-header>
			</TD>
		</TR>

		<s:if test="!fieldErrors.isEmpty()">
			<TR><TD colspan="2" align="left" class="errorTable">
				<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror escape="false"/>
			</TD></TR>
		</s:if>

		<TR>
			<TD><loc:message name="propAcademicSession"/></TD>
			<TD style="color: gray;"><s:property value="session"/></TD>
		</TR>
		
			<TR>
				<TD><loc:message name="fieldFirstName"/>:</TD>
				<TD>
					<s:textfield name="form.firstName" size="50" maxlength="100" id="fname"/>
					<input type='button' value='Lookup' onclick="lookup();" style="btn">
				</TD>
			</TR>
			<TR>
				<TD><loc:message name="fieldMiddleName"/>:</TD>
				<TD>
					<s:textfield name="form.middleName" size="50" maxlength="100" id="mname"/>
				</TD>
			</TR>
			<TR>
				<TD><loc:message name="fieldLastName"/>:</TD>
				<TD>
					<s:textfield name="form.lastName" size="50" maxlength="100" id="lname"/>
				</TD>
			</TR>
			<TR>
				<TD><loc:message name="fieldAcademicTitle"/>:</TD>
				<TD>
					<s:textfield name="form.title" size="25" maxlength="50" id="title"/>
				</TD>
			</TR>

		<TR>
			<TD><loc:message name="propertyExternalId"/></TD>
			<TD>
				<s:textfield name="form.externalId" size="40" maxlength="40" id="uid"/>
				<s:hidden name="form.lookupEnabled"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="columnEmailAddress"/>:</TD>
			<TD>
				<s:textfield name="form.email" size="30" maxlength="100" id="email"/>
			</TD>
		</TR>

<!-- Departments -->
		<TR>
			<TD colspan="2">
				<tt:section-title>&nbsp;<br><loc:message name="fieldDepartments"/></tt:section-title>
			</TD>
		</TR>

		<TR>
			<TD>&nbsp;</TD>
			<TD>
				<s:select name="form.dept" style="min-width: 300px;"
					list="#request.deptsList" listKey="uniqueId" listValue="label"
					headerKey="-1" headerValue="%{#msg.itemSelect()}"/>
				&nbsp;
				<s:submit name="op" value="%{#msg.actionAddDepartment()}"/>
			</TD>
		</TR>

		<TR>
			<TD>&nbsp;</TD>
			<TD>
				<table class="unitime-Table" style="width:100%">
				<s:iterator value="form.depts" var="dept" status="stat"><s:set var="ctr" value="#stat.index"/>
					<TR>
						<TD>
							<s:hidden name="form.depts[%{#ctr}]"/>
							<s:hidden name="form.deptLabels[%{#ctr}]"/>
							<s:property value="form.deptLabels[#ctr]"/>
						</TD>
						<TD align="right">							
							&nbsp;
							<s:submit name="op" value="%{#msg.actionDelete()}" onclick="doDel('dept', '%{#ctr}');"/> 
						</TD>
					</TR>
				</s:iterator>
				</table>
			</TD>
		</TR>

<!-- Solver Groups -->
		<TR>
			<TD colspan="2">
				<tt:section-title>&nbsp;<br><loc:message name="sectSolverGroups"/></tt:section-title>
			</TD>
		</TR>

		<TR>
			<TD>&nbsp;</TD>
			<TD>
				<s:select name="form.solverGr" style="min-width: 300px;"
					list="#request.solverGroupList" listKey="uniqueId" listValue="name"
					headerKey="-1" headerValue="%{#msg.itemSelect()}"/>
				&nbsp;
				<s:submit name="op" value="%{#msg.actionAddSolverGroup()}"/>
			</TD>
		</TR>

		<TR>
			<TD>&nbsp;</TD>
			<TD>
				<table class="unitime-Table" style="width:100%">
				<s:iterator value="form.solverGrs" var="solverGr" status="stat"><s:set var="ctr" value="#stat.index"/>
					<TR>
						<TD>
							<s:hidden name="form.solverGrs[%{#ctr}]"/>
							<s:hidden name="form.solverGrLabels[%{#ctr}]"/>
							<s:property value="form.solverGrLabels[#ctr]"/>
						</TD>
						<TD align="right">							
							&nbsp;
							<s:submit name="op" value="%{#msg.actionDelete()}" onclick="doDel('solverGr', '%{#ctr}');"/> 
						</TD>
					</TR>
				</s:iterator>
				</table>
			</TD>
		</TR>

<!-- Roles -->
		<TR>
			<TD colspan="2">
				<tt:section-title>&nbsp;<br><loc:message name="columnRoles"/></tt:section-title>
			</TD>
		</TR>

		<TR>
			<TD>&nbsp;</TD>
			<TD>
				<s:select name="form.role" style="min-width: 300px;"
					list="#request.rolesList" listKey="roleId" listValue="abbv"
					headerKey="-1" headerValue="%{#msg.itemSelect()}"/>
				&nbsp;
				<s:submit name="op" value="%{#msg.actionAddRole()}"/>
			</TD>
		</TR>

		<TR>
			<TD>&nbsp;</TD>
			<TD>
				<table class="unitime-Table" style="width:100%">
					<TR>
						<TD align="left" style="width: 100px;">
							<I><loc:message name="columnPrimaryRole"/></I>
						</TD>
						<TD>
							<I><loc:message name="fieldRole"/></I>
						</TD>
						<TD align="center" nowrap="nowrap" style="width: 100px;">
							<i><loc:message name="columnReceiveEmails"/></i>
						</TD>
						<TD>
							&nbsp;
						</TD>
					</TR>				
				<s:iterator value="form.roles" var="role" status="stat"><s:set var="ctr" value="#stat.index"/>
					<TR>
						<TD align="left" style="width: 100px;">
							<s:radio name="form.primaryRole" list="#{#role:''}"/>
						</TD>
						<TD>
							<s:hidden name="form.roles[%{#ctr}]"/>
							<s:hidden name="form.roleRefs[%{#ctr}]"/>
							<s:property value="form.roleRefs[#ctr]"/>
						</TD>
						<TD align="center">
							<s:checkbox name="form.roleReceiveEmailFlags[%{#ctr}]"/>
						</TD>
						<TD align="right">							
							&nbsp;
							<s:submit name="op" value="%{#msg.actionDelete()}" onclick="doDel('role', '%{#ctr}');"/> 
						</TD>
					</TR>
				</s:iterator>
				</table>
			</TD>
		</TR>

		<TR>
			<TD colspan="2">
				<tt:section-title/>
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="right">
					<s:if test="form.uniqueId == null">
						<s:submit name="op" value="%{#msg.actionSaveManager()}"
							accesskey="%{#msg.acessSaveManager()}" title="%{#msg.titleSaveManager(#msg.acessSaveManager())}"/>
					</s:if><s:else>
						<s:submit name="op" value="%{#msg.actionUpdateManager()}"
							accesskey="%{#msg.acessUpdateManager()}" title="%{#msg.titleUpdateManager(#msg.acessUpdateManager())}"/>
						<sec:authorize access="hasPermission(#form.uniqueId, 'TimetableManager', 'TimetableManagerDelete')">
							<s:submit name="op" value="%{#msg.actionDeleteManager()}"
							accesskey="%{#msg.acessDeleteManager()}" title="%{#msg.titleDeleteManager(#msg.acessDeleteManager())}"
							onclick="return confirmDelete();"/>
						</sec:authorize>
					</s:else>
					<s:submit name="op" value="%{#msg.actionBackToManagers()}"
							accesskey="%{#msg.acessBackToManagers()}" title="%{#msg.titleBackToManagers(#msg.acessBackToManagers())}"/>			
			</TD>
		</TR>
	</TABLE>
<script type="text/javascript">
	function doDel(type, id) {
		var delType = document.getElementById('deleteType');
		delType.value = type;
		var delId = document.getElementById('deleteId');
		delId.value = id;
	}
	function lookup() {
		peopleLookup((document.getElementById('fname').value + ' ' + document.getElementById('lname').value).trim(), function(person) {
			if (person) {
				document.getElementById('uid').value = (person[0] == null ? '' : person[0]);
				document.getElementById('fname').value = (person[1] == null ? '' : person[1]);
				document.getElementById('mname').value = (person[2] == null ? '' : person[2]);
				document.getElementById('lname').value = (person[3] == null ? '' : person[3]);
				document.getElementById('email').value = (person[4] == null ? '' : person[4]);
				document.getElementById('title').value = (person[6] == null ? '' : person[6]);
			}
		}, "mustHaveExternalId");
	}
</script>
</s:form>
</loc:bundle>
