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
<tt:confirm name="confirmDelete"><loc:message name="confirmDeleteAppConfig"/></tt:confirm>
<s:form action="applicationConfig">
<s:hidden name="form.op"/>
<s:if test="form.op != 'list'">
	<table class="unitime-MainTable">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<s:if test="form.op == 'add'">
							<loc:message name="sectAddAppSetting"/>
						</s:if>
						<s:else>
							<loc:message name="sectEditAppSetting"/>
						</s:else>
					</tt:section-title>
					<s:if test="form.op == 'add'">
						<s:submit name='op' value='%{#msg.actionSaveSetting()}'
							accesskey='%{#msg.accessSaveSetting()}' title='%{#msg.titleSaveSetting(#msg.accessSaveSetting())}'/>
					</s:if><s:else>
						<s:submit name='op' value='%{#msg.actionUpdateSetting()}'
							accesskey='%{#msg.accessUpdateSetting()}' title='%{#msg.titleUpdateSetting(#msg.accessUpdateSetting())}'/>
						<s:submit name='op' value='%{#msg.actionDeleteSetting()}'
							accesskey='%{#msg.accessDeleteSetting()}' title='%{#msg.titleDeleteSetting(#msg.accessDeleteSetting())}'
							onclick="return confirmDelete();"/>
					</s:else>
					<s:submit name='op' value='%{#msg.actionCancelSetting()}'
						accesskey='%{#msg.accessCancelSetting()}' title='%{#msg.titleCancelSetting(#msg.accessCancelSetting())}'/>
				</tt:section-header>
			</TD>
		</TR>

		<TR>
			<TD valign="top"><loc:message name="propAppConfigKey"/></TD>
			<TD valign="top">
				<s:if test="form.op == 'add'">
					<s:textfield name="form.key" size="120" maxlength="1000"/>
				</s:if><s:else>
					<s:hidden name="form.key"/>
					<s:property value="form.key"/>
				</s:else>
				&nbsp;<s:fielderror fieldName="form.key"/>
			</TD>
		</TR>

		<TR>
			<TD valign="top" rowspan="2"><loc:message name="propAppConfigAppliesTo"/></TD>
			<TD valign="top">
				<s:checkbox name="form.allSessions" onchange="document.getElementById('sessionsCell').style.display = (this.checked ? 'none' : 'table-cell');"/> <loc:message name="checkAppConfigAppliesToAllSessions"/>
			</TD>
		</TR>
		
		<TR>
			<TD valign="top" id="sessionsCell" style="max-width: 700px;">
				<s:iterator value="form.listSessions" var="s">
					<div style="display: inline-block; width: 200px; white-space: nowrap; margin-left: 20px; overflow: hidden;">
						<s:checkboxlist name="form.sessions" list="#{#s.uniqueId:''}"/>
						<s:property value="#s.label"/>
					</div>
				</s:iterator>
			</TD>
			<s:if test="form.allSessions == true">
				<script>document.getElementById('sessionsCell').style.display = 'none';</script>
			</s:if>
		</TR>
		
		<s:if test="form.type != null && !form.type.isEmpty()">
			<TR>
				<TD valign="top"><loc:message name="propAppConfigType"/></TD>
				<TD valign="top">
					<s:property value="form.type"/>
				</TD>
			</TR>
		</s:if>
		
		<s:if test="form.values != null && !form.values.isEmpty()">
			<TR>
				<TD valign="top"><loc:message name="propAppConfigValues"/></TD>
				<TD valign="top">
					<s:property value="form.values"/>
				</TD>
			</TR>
		</s:if>

		<s:if test="form.default != null && !form.default.isEmpty()">
			<TR>
				<TD valign="top"><loc:message name="propAppConfigDefault"/></TD>
				<TD valign="top">
					<s:property value="form.default"/>
				</TD>
			</TR>
		</s:if>

		<TR>
			<TD valign="top"><loc:message name="propAppConfigValue"/></TD>
			<TD valign="top">
				<s:textarea name="form.value" rows="10" cols="120"/>
			</TD>
		</TR>

		<TR>
			<TD valign="top"><loc:message name="propAppConfigDescription"/></TD>
			<TD valign="top">
				<s:textarea name="form.description" rows="5" cols="120"/>
			</TD>
		</TR>

		<TR>
			<TD colspan="2">
				<tt:section-title/>
			</TD>
		</TR>

		<TR>
			<TD align="right" colspan="2">
				<s:if test="form.op == 'add'">
					<s:submit name='op' value='%{#msg.actionSaveSetting()}'
						accesskey='%{#msg.accessSaveSetting()}' title='%{#msg.titleSaveSetting(#msg.accessSaveSetting())}'/>
				</s:if><s:else>
					<s:submit name='op' value='%{#msg.actionUpdateSetting()}'
						accesskey='%{#msg.accessUpdateSetting()}' title='%{#msg.titleUpdateSetting(#msg.accessUpdateSetting())}'/>
					<s:submit name='op' value='%{#msg.actionDeleteSetting()}'
						accesskey='%{#msg.accessDeleteSetting()}' title='%{#msg.titleDeleteSetting(#msg.accessDeleteSetting())}'
						onclick="return confirmDelete();"/>
				</s:else>
				<s:submit name='op' value='%{#msg.actionCancelSetting()}'
					accesskey='%{#msg.accessCancelSetting()}' title='%{#msg.titleCancelSetting(#msg.accessCancelSetting())}'/>
			</TD>
		</TR>
	</TABLE>
	
</s:if>
<s:else>
<table class="unitime-MainTable unitime-ApplicationConfigTable">
	<TR>
		<TD colspan='3'>
			<tt:section-header>
				<tt:section-title><loc:message name="sectAppSettings"/></tt:section-title>
				<sec:authorize access="hasPermission(null, null, 'ApplicationConfigEdit')">
					<s:submit name='op' value='%{#msg.actionAddSetting()}'
						accesskey='%{#msg.accessAddSetting()}' title='%{#msg.titleAddSetting(#msg.accessAddSetting())}'/>
				</sec:authorize> 
			</tt:section-header>
		</TD>
	</TR>
	<s:property value="#request.appConfig" escapeHtml="false"/>
	<TR>
		<TD colspan='2' valign="top">
			<span class="unitime-Hint" style="vertical-align: top;"><loc:message name="descAppConfigAppliesToCurrentAcadSession"/></span>
		</TD>
		<TD align="right">
			<input type='hidden' name='apply' value=''/><s:checkbox name="form.showAll" onchange="apply.value='1'; submit();"/><loc:message name="checkShowAllAppSettings"/>&nbsp;&nbsp;&nbsp;&nbsp;			
			<sec:authorize access="hasPermission(null, null, 'ApplicationConfigEdit')">
				<s:submit name='op' value='%{#msg.actionAddSetting()}'
					accesskey='%{#msg.accessAddSetting()}' title='%{#msg.titleAddSetting(#msg.accessAddSetting())}'/>
			</sec:authorize> 
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
