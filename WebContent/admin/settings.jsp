<%-- 
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org
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
<%@ page import="org.unitime.commons.web.Web" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>

<tiles:importAttribute />

<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(Web.getUser(session)) %>
		
		function confirmDelete() {
			if (jsConfirm!=null && !jsConfirm)
				return true;
			
			return confirm('If this setting is used by any user, it will be deleted as well. Continue?')
		}
	// -->
</SCRIPT>



<html:form action="/settings" focus="key">
<html:hidden property="uniqueId"/><html:errors property="uniqueId"/>

	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<DIV class="WelcomeRowHead">
				Add User Setting
				</DIV>
			</TD>
		</TR>

		<TR>
			<TD>Key:</TD>
			<TD>
				<html:text property="key" size="15" maxlength="30"/>
				&nbsp;<html:errors property="key"/>
			</TD>
		</TR>

		<TR>
			<TD>Default Value:</TD>
			<TD>
				<html:text property="defaultValue" size="15" maxlength="100"/>
				&nbsp;<html:errors property="defaultValue"/>
			</TD>
		</TR>

		<TR>
			<TD>Allowed Values:</TD>
			<TD>
				<html:text property="allowedValues" size="50" maxlength="500"/> Separate multiple values with a comma
				&nbsp;<html:errors property="allowedValues"/>
			</TD>
		</TR>

		<TR>
			<TD>Description:</TD>
			<TD>
				<html:text property="description" size="50" maxlength="100"/>
				&nbsp;<html:errors property="description"/>
			</TD>
		</TR>

		<TR>
			<TD align="right" colspan="2">
				<html:submit property="op">
					<bean:write name="settingsForm" property="op" />
				</html:submit> 
				<logic:notEqual name="settingsForm" property="op" value="Add New">
					<html:submit property="op" value="Delete" onclick="return(confirmDelete());" /> 
				</logic:notEqual>
				<html:submit property="op" value="Clear" /> 
				<html:reset />
			</TD>
		</TR>
	</TABLE>

<BR>&nbsp;<BR>

<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
	<%= request.getAttribute(org.unitime.timetable.model.Settings.SETTINGS_ATTR_NAME) %> 
</TABLE>


</html:form>
