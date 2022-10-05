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
<tt:confirm name="confirmDelete"><loc:message name="confirmUserDelete"/></tt:confirm>
<s:form action="userEdit">
<s:hidden name="form.op"/>
<s:if test="form.op != 'List'">
	<table class="unitime-MainTable">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<s:if test="form.op == #msg.actionSaveUser()">
							<loc:message name="sectAddUser"/>
						</s:if><s:else>
							<loc:message name="sectEditUser"/>
						</s:else>
					</tt:section-title>
					<s:if test="form.op == #msg.actionSaveUser()">
						<s:submit name='op' value='%{#msg.actionSaveUser()}'
							accesskey='%{#msg.accessSaveUser()}' title='%{#msg.titleSaveUser(#msg.accessSaveUser())}'/>
					</s:if><s:else>
						<s:submit name='op' value='%{#msg.actionUpdateUser()}'
							accesskey='%{#msg.accessUpdateUser()}' title='%{#msg.titleUpdateUser(#msg.accessUpdateUser())}'/>
						<s:submit name='op' value='%{#msg.actionDeleteUser()}'
							accesskey='%{#msg.accessDeleteUser()}' title='%{#msg.titleDeleteUser(#msg.accessDeleteUser())}'
							onclick="return confirmDelete();"/>
					</s:else>
					<s:submit name='op' value='%{#msg.actionBackToUsers()}'
						accesskey='%{#msg.accessBackToUsers()}' title='%{#msg.titleBackToUsers(#msg.accessBackToUsers())}'/>
				</tt:section-header>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propertyExternalId"/></TD>
			<TD>
				<s:if test="form.op == #msg.actionSaveUser()">
					<s:textfield name="form.externalId" size="40" maxlength="40"/>
				</s:if><s:else>
					<s:property value="form.externalId"/>
					<s:hidden name="form.externalId"/>
				</s:else>
				&nbsp;<s:fielderror fieldName="form.externalId"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propertyUsername"/></TD>
			<TD>
				<s:textfield name="form.name" size="15" maxlength="15"/>
				&nbsp;<s:fielderror fieldName="form.name"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propUserPassword"/></TD>
			<TD>
				<s:password name="form.password" size="25" maxlength="40"/>
				&nbsp;<s:fielderror fieldName="form.password"/>
			</TD>
		</TR>
		
		<s:if test="form.token != null && !form.token.isEmpty()">
			<TR>
				<TD><loc:message name="propAPIKey"/></TD>
				<TD>
					<s:property value="form.token"/>
					<s:hidden value="form.token"/>
				</TD>
			</TR>
		</s:if>
		
		<TR>
			<TD colspan="2">
				<tt:section-title/>
			</TD>
		</TR>
		
		<TR>
			<TD align="right" colspan="2">
				<s:if test="form.op == #msg.actionSaveUser()">
					<s:submit name='op' value='%{#msg.actionSaveUser()}'
						accesskey='%{#msg.accessSaveUser()}' title='%{#msg.titleSaveUser(#msg.accessSaveUser())}'/>
				</s:if><s:else>
					<s:submit name='op' value='%{#msg.actionUpdateUser()}'
						accesskey='%{#msg.accessUpdateUser()}' title='%{#msg.titleUpdateUser(#msg.accessUpdateUser())}'/>
					<s:submit name='op' value='%{#msg.actionDeleteUser()}'
						accesskey='%{#msg.accessDeleteUser()}' title='%{#msg.titleDeleteUser(#msg.accessDeleteUser())}'
						onclick="return confirmDelete();"/>
				</s:else>
				<s:submit name='op' value='%{#msg.actionBackToUsers()}'
					accesskey='%{#msg.accessBackToUsers()}' title='%{#msg.titleBackToUsers(#msg.accessBackToUsers())}'/> 
			</TD>
		</TR>
	</TABLE>

</s:if><s:else>
<table class="unitime-MainTable">
		<tr>
			<td colspan='3'>
				<tt:section-header>
					<tt:section-title><loc:message name="sectUsers"/></tt:section-title>
					<s:submit name='op' value='%{#msg.actionRequestPasswordChange()}'
						accesskey='%{#msg.accessRequestPasswordChange()}' title='%{#msg.titleRequestPasswordChange(#msg.accessRequestPasswordChange())}'/>
					<s:submit name='op' value='%{#msg.actionAddUser()}'
						accesskey='%{#msg.accessAddUser()}' title='%{#msg.titleAddUser(#msg.accessAddUser())}'/>
				</tt:section-header>
			</td>
		</tr>
		<s:property value="usersTable" escapeHtml="false"/>
		<tr>
			<td colspan='3'>
				<tt:section-title/>
			</td>
		</tr>
		<tr>
			<td colspan='3' align="right">
				<s:submit name='op' value='%{#msg.actionRequestPasswordChange()}'
					accesskey='%{#msg.accessRequestPasswordChange()}' title='%{#msg.titleRequestPasswordChange(#msg.accessRequestPasswordChange())}'/>
				<s:submit name='op' value='%{#msg.actionAddUser()}'
					accesskey='%{#msg.accessAddUser()}' title='%{#msg.titleAddUser(#msg.accessAddUser())}'/>
			</td>
		</tr>
</table>
</s:else>
</s:form>
</loc:bundle>
