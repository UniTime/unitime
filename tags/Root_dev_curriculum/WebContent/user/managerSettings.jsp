<%--
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
--%>
<%@ page language="java" autoFlush="true"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ page import="org.unitime.timetable.model.Settings" %>
<tiles:importAttribute />

<html:form action="/managerSettings">

<logic:equal name="mgrSettingsForm" property="op" value="Edit">

<html:hidden property="keyId"/><html:errors property="keyId"/>
<html:hidden property="settingId"/><html:errors property="settingId"/>

	<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD>
				<tt:section-header>
					<tt:section-title>
						<bean:write name="mgrSettingsForm" property="key"/>
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
					<html:radio property="value" value="${allowedValue}"> &nbsp; ${allowedValue}</html:radio><BR>
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
	<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">
		<%= request.getAttribute(Settings.SETTINGS_ATTR_NAME) %>
	</TABLE>
</logic:notEqual>

</html:form>
