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
