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
<html:form action="/personalSchedule">
	<html:hidden property="sessionId" />
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR><TD align='right'>
			<logic:equal name="personalizedExamReportForm" property="admin" value="true">
				<html:hidden property="uid" styleId="uid" onchange="submit();"/>
				<html:hidden property="fname" styleId="fname"/>
				<html:hidden property="mname" styleId="mname"/>
				<html:hidden property="lname" styleId="lname"/>
				<input type='button' value='Lookup' onclick="lookup();" style="btn">
			</logic:equal>
			<logic:equal name="personalizedExamReportForm" property="canExport" value="true">
				<html:submit accesskey="P" property="op" value="Export PDF" title="Export PDF (Alt+P)"/>
				<html:submit accesskey="I" property="op" value="iCalendar" title="Export iCalendar (Alt+I)"/>
			</logic:equal>
			<logic:equal name="personalizedExamReportForm" property="logout" value="true">
				<html:submit accesskey="L" property="op" value="Log Out" title="Log out (Alt+L)"/>
			</logic:equal>
		</TD></TR>
	</TABLE>
	<logic:notEmpty name="personalizedExamReportForm" property="message">
		<BR>
		<div style='font-weight:bold;color:red;'>
			<bean:write name="personalizedExamReportForm" property="message"/>
		</div>
	</logic:notEmpty>
	<logic:notEmpty scope="request" name="sessions">
		<BR>
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
			<bean:write scope="request" name="sessions" filter="false"/>
		</TABLE>
	</logic:notEmpty>
	<logic:notEmpty scope="request" name="iclsschd">
		<BR>
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
			<bean:write scope="request" name="iclsschd" filter="false"/>
		</TABLE>
	</logic:notEmpty>
	<logic:notEmpty scope="request" name="ischedule">
		<BR>
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
			<bean:write scope="request" name="ischedule" filter="false"/>
		</TABLE>
	</logic:notEmpty>
	<logic:notEmpty scope="request" name="iconf">
		<BR>
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
			<bean:write scope="request" name="iconf" filter="false"/>
		</TABLE>
	</logic:notEmpty>
	<logic:notEmpty scope="request" name="sconf">
		<BR>
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
			<bean:write scope="request" name="sconf" filter="false"/>
		</TABLE>
	</logic:notEmpty>
	<logic:notEmpty scope="request" name="clsschd">
		<BR>
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
			<bean:write scope="request" name="clsschd" filter="false"/>
		</TABLE>
	</logic:notEmpty>
	<logic:notEmpty scope="request" name="schedule">
		<BR>
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
			<bean:write scope="request" name="schedule" filter="false"/>
		</TABLE>
	</logic:notEmpty>
	<logic:notEmpty scope="request" name="conf">
		<BR>
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
			<bean:write scope="request" name="conf" filter="false"/>
		</TABLE>
	</logic:notEmpty>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR><TD><tt:section-title/></TD></TR>
		<TR><TD align='right'>
			<logic:equal name="personalizedExamReportForm" property="admin" value="true">
				<input type='button' value='Lookup' onclick="lookup();" style="btn">
			</logic:equal>
			<logic:equal name="personalizedExamReportForm" property="canExport" value="true">
				<html:submit accesskey="P" property="op" value="Export PDF" title="Export PDF (Alt+P)"/>
				<html:submit accesskey="I" property="op" value="iCalendar" title="Export iCalendar (Alt+I)"/>
			</logic:equal>
			<logic:equal name="personalizedExamReportForm" property="logout" value="true">
				<html:submit accesskey="L" property="op" value="Log Out" title="Log out (Alt+L)"/>
			</logic:equal>
		</TD></TR>
	</TABLE>
<script language="javascript">
	function lookup() {
		peopleLookup((document.getElementById('fname').value + ' ' + document.getElementById('lname').value).trim(), function(person) {
			if (person) {
				document.getElementById('uid').value = (person[0] == null ? '' : person[0]);
				document.getElementById('fname').value = (person[1] == null ? '' : person[1]);
				document.getElementById('mname').value = (person[2] == null ? '' : person[2]);
				document.getElementById('lname').value = (person[3] == null ? '' : person[3]);
				document.forms[0].submit();
			}
		}, "mustHaveExternalId");
	}
</script>
</html:form>