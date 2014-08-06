<%-- 
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 --%>
<%@ page language="java" autoFlush="true"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<tiles:importAttribute />

<tt:confirm name="confirmDelete">The manager setting parameter will be deleted. Continue?</tt:confirm>

<html:form action="/settings">
<html:hidden property="uniqueId"/><html:errors property="uniqueId"/>
<logic:notEqual name="settingsForm" property="op" value="List">
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<logic:equal name="settingsForm" property="op" value="Save">
							Add
						</logic:equal> 
						<logic:notEqual name="settingsForm" property="op" value="Save">
							Edit
						</logic:notEqual> 
						Manager Setting
					</tt:section-title>
					<logic:equal name="settingsForm" property="op" value="Save">
						<html:submit property="op" value="Save" title="Save Setting (Alt+S)" accesskey="S"/>
					</logic:equal>
					<logic:notEqual name="settingsForm" property="op" value="Save">
						<html:submit property="op" value="Update" title="Update Setting (Alt+U)" accesskey="U"/>
						<html:submit property="op" value="Delete" onclick="return confirmDelete();" title="Delete Setting (Alt+D)" accesskey="D"/> 
					</logic:notEqual>
					<html:submit property="op" value="Back" title="Back (Alt+B)" accesskey="B"/> 
				</tt:section-header>
			</TD>
		</TR>

		<TR>
			<TD>Reference:</TD>
			<TD>
				<html:text property="key" size="30" maxlength="30"/>
				&nbsp;<html:errors property="key"/>
			</TD>
		</TR>

		<TR>
			<TD>Default Value:</TD>
			<TD>
				<html:text property="defaultValue" size="30" maxlength="100"/>
				&nbsp;<html:errors property="defaultValue"/>
			</TD>
		</TR>

		<TR>
			<TD>Allowed Values:</TD>
			<TD>
				<html:text property="allowedValues" size="80" maxlength="500"/> <i>(Separate multiple values with a comma)</i>
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
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>

		<TR>
			<TD align="right" colspan="2">
				<logic:equal name="settingsForm" property="op" value="Save">
					<html:submit property="op" value="Save" title="Save Setting (Alt+S)" accesskey="S"/>
				</logic:equal>
				<logic:notEqual name="settingsForm" property="op" value="Save">
					<html:submit property="op" value="Update" title="Update Setting (Alt+U)" accesskey="U"/>
					<html:submit property="op" value="Delete" onclick="return confirmDelete();" title="Delete Setting (Alt+D)" accesskey="D"/> 
				</logic:notEqual>
				<html:submit property="op" value="Back" title="Back (Alt+B)" accesskey="B"/> 
			</TD>
		</TR>
	</TABLE>
</logic:notEqual>
<logic:equal name="settingsForm" property="op" value="List">

<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD colspan='4'>
			<tt:section-header>
				<tt:section-title>
					Manager Settings
				</tt:section-title>
				<html:submit property="op" value="Add Setting" title="Create New Manager Setting (Alt+A)" accesskey="A"/>
			</tt:section-header>
		</TD>
	</TR>
	<bean:write name="table" scope="request" filter="false"/>
	<TR>
		<TD colspan='4'>
			<tt:section-title/>
		</TD>
	</TR>
	<TR>
		<TD colspan='4' align="right">
			<html:submit property="op" value="Add Setting" title="Create New Manager Setting (Alt+A)" accesskey="A"/> 
		</TD>
	</TR>
	 
</TABLE>

</logic:equal>
</html:form>
