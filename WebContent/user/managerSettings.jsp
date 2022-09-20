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
<s:form action="managerSettings">
<s:if test="form.op == 'Edit'">
	<s:hidden name="form.key"/>
	<s:hidden name="form.name"/>
	<s:hidden name="form.defaultValue"/>
	<table class="unitime-MainTable">
		<TR>
			<TD>
				<tt:section-header>
					<tt:section-title>
						<s:property value="form.name"/>
					</tt:section-title>
					<s:submit name='op' value='%{#msg.actionUpdateManagerSetting()}'
						accesskey='%{#msg.accessUpdateManagerSetting()}' title='%{#msg.titleUpdateManagerSetting(#msg.accessUpdateManagerSetting())}'/>
					<s:submit name='op' value='%{#msg.actionBackToManagerSettings()}'
						accesskey='%{#msg.accessBackToManagerSettings()}' title='%{#msg.titleBackToManagerSettings(#msg.accessBackToManagerSettings())}'/>
				</tt:section-header>
			</TD>
		</TR>

		<TR>
			<TD valign="middle" style="padding-left: 50px;">
				<s:iterator value="form.allowedValues" var="allowedValue">
					<s:radio name="form.value" list="#{#allowedValue:''}"/>
					<s:property value="%{form.getLabel(#allowedValue)}"/>
					<s:if test="form.defaultValue == #allowedValue"><i><loc:message name="userSettingDefaultIndicator"/></i></s:if>
					<br>
				</s:iterator>
				<s:fielderror fieldName="form.value"/>
			</TD>
		</TR>

		<TR>
			<TD align="right">
				<tt:section-title/>
			</TD>
		</TR>

		<TR>
			<TD align="right">
				<s:submit name='op' value='%{#msg.actionUpdateManagerSetting()}'
					accesskey='%{#msg.accessUpdateManagerSetting()}' title='%{#msg.titleUpdateManagerSetting(#msg.accessUpdateManagerSetting())}'/>
				<s:submit name='op' value='%{#msg.actionBackToManagerSettings()}'
					accesskey='%{#msg.accessBackToManagerSettings()}' title='%{#msg.titleBackToManagerSettings(#msg.accessBackToManagerSettings())}'/>
			</TD>
		</TR>
	</table>
</s:if>
<s:else>
	<table class="unitime-MainTable">
		<s:property value="#request.table" escapeHtml="false"/>
	</table>
</s:else>
</s:form>
</loc:bundle>