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
<%@ page language="java" autoFlush="true"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>

<tiles:importAttribute />

<html:form action="/managerSettings">

<logic:equal name="mgrSettingsForm" property="op" value="Edit">

	<html:hidden property="key"/>
	<html:hidden property="name"/>
	<html:hidden property="defaultValue"/>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD>
				<tt:section-header>
					<tt:section-title>
						<bean:write name="mgrSettingsForm" property="name"/>
					</tt:section-title>
					<html:submit styleClass="btn" property="op" accesskey="U" titleKey="title.updateSetting">
						<bean:message key="button.updateSetting" />
					</html:submit>
					<html:submit styleClass="btn" property="op" accesskey="B" titleKey="title.cancelUpdateSetting"> 
						<bean:message key="button.cancelUpdateSetting" />
					</html:submit>
				</tt:section-header>
			</TD>
		</TR>

		<TR>
			<TD valign="middle">
				<!-- html :select property="value">
					<html :options name="mgrSettingsForm" property="allowedValues"/>
				</html :select -->
				<logic:iterate id="allowedValue" name="mgrSettingsForm" property="allowedValues">
					&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
					<html:radio property="value" value="${allowedValue}"> &nbsp; ${allowedValue}</html:radio>
					<logic:equal property="defaultValue" name="mgrSettingsForm" value="${allowedValue}"><i>(default)</i></logic:equal>
					<BR>
				</logic:iterate>				
				<html:errors property="value"/>
			</TD>
		</TR>

		<TR>
			<TD align="right">
				<tt:section-title/>
			</TD>
		</TR>

		<TR>
			<TD align="right">
				<html:submit styleClass="btn" property="op" accesskey="U" titleKey="title.updateSetting">
					<bean:message key="button.updateSetting" />
				</html:submit>
				<html:submit styleClass="btn" property="op" accesskey="B" titleKey="title.cancelUpdateSetting"> 
					<bean:message key="button.cancelUpdateSetting" />
				</html:submit>
			</TD>
		</TR>
	</TABLE>

</logic:equal>
<logic:notEqual name="mgrSettingsForm" property="op" value="Edit">
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<bean:write name="table" scope="request" filter="false"/>
	</TABLE>
</logic:notEqual>

</html:form>
