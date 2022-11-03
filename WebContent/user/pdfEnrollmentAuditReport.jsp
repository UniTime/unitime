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
<script type="text/javascript" src="scripts/block.js"></script>
<loc:bundle name="ExaminationMessages"><s:set var="msg" value="#attr.MSG"/> 
<s:form action="enrollmentAuditPdfReport">
<table class="unitime-MainTable">
	<s:if test="!fieldErrors.isEmpty()">
		<TR>
			<TD colspan='2'>
				<tt:section-header>
					<tt:section-title><loc:message name="sectErrors"/></tt:section-title>
					<s:if test="form.report != null && !form.report.isEmpty()">
						<s:submit accesskey='%{#msg.accesBack()}' name='op' value='%{#msg.actionBack()}' title='%{#msg.titleBack()}'/>
					</s:if><s:else>
						<s:submit accesskey='%{#msg.accessGenerateReport()}' name='op' value='%{#msg.actionGenerateReport()}' title='%{#msg.titleGenerateReport()}'/>
					</s:else>
				</tt:section-header>
			</TD>
		</TR>
		<TR>
			<TD colspan="2" align="left" class="errorTable">
				<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror/>
			</TD>
		</TR>
		<TR><TD>&nbsp;</TD></TR>
	</s:if>
	<s:if test="form.report != null && !form.report.isEmpty()">
		<TR>
			<TD colspan='2'>
				<tt:section-header>
					<tt:section-title><loc:message name="sectLog"/></tt:section-title>
					<s:if test="fieldErrors.isEmpty()">
						<s:submit accesskey='%{#msg.accesBack()}' name='op' value='%{#msg.actionBack()}' title='%{#msg.titleBack()}'/>
					</s:if>
				</tt:section-header>
			</TD>
		</TR>
		<TR>
  			<TD colspan='2'>
  				<blockquote>
  					<s:property value="form.report" escapeHtml="false"/>
  				</blockquote>
  			</TD>
		</TR>
		<TR>
			<TD colspan='2'>
				<tt:section-title>&nbsp;</tt:section-title>
			</TD>
		</TR>
		<TR>
			<TD colspan='2' align='right'>
				<s:submit accesskey='%{#msg.accesBack()}' name='op' value='%{#msg.actionBack()}' title='%{#msg.titleBack()}'/>
			</TD>
		</TR>
	</s:if><s:else>
	<TR>
		<TD colspan='2'>
			<tt:section-header>
				<tt:section-title><loc:message name="sectInputData"/></tt:section-title>
				<s:if test="fieldErrors.isEmpty()">
					<s:submit accesskey='%{#msg.accessGenerateReport()}' name='op' value='%{#msg.actionGenerateReport()}' title='%{#msg.titleGenerateReport()}'/>
				</s:if>
			</tt:section-header>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap valign='top'><loc:message name="filterSubjectAreas"/></TD>
		<TD>
			<s:checkbox name="form.all" onclick="selectionChanged();"/><loc:message name="checkReportAllSubjectAreas"/><br>
			<s:select name="form.subjects" list="form.subjectAreas" listKey="uniqueId" listValue="subjectAreaAbbreviation" multiple="true"/>
		</TD>
	</TR>
	<TR>
		<TD colspan='2'>
			<tt:section-title><br><loc:message name="sectReport"/></tt:section-title>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap valign='top'><loc:message name="filterReport"/></TD>
		<TD>
			<s:iterator value="form.allReports" var="report">
				<s:checkboxlist name="form.reports" list="#{#report.value:''}" onchange="selectionChanged();"/>
				<s:property value="#report.label"/>
				<br>
			</s:iterator>
		</TD>
	</TR>
	<TR>
		<TD colspan='2'>
			<tt:section-title><br><loc:message name="sectParameters"/></tt:section-title>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap valign='top'><loc:message name="propAllReports"/></TD>
		<TD><s:checkbox name="form.externalId"/><loc:message name="checkDisplayStudentId"/><br>
			<s:checkbox name="form.studentName"/><loc:message name="checkDisplayStudentName"/></TD>
	</TR>
	<TR>
		<TD colspan='2' valign='top'>
			<tt:section-title><br><loc:message name="sectOutput"/></tt:section-title>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap><loc:message name="propReportFormat"/></TD>
		<TD>
			<s:select name="form.mode" list="form.modes" listKey="value" listValue="label"/>
		</TD>
	</TR>
	<s:if test="form.canEmail == false">
		<s:hidden name="form.email"/>
	</s:if><s:else>
	<TR>
		<TD rowspan='1' valign='top'><loc:message name="propReportDelivery"/></TD>
		<TD>
			<s:checkbox name="form.email" onclick="document.getElementById('eml').style.display=(this.checked?'block':'none');"/> <loc:message name="checkReportDeliveryEmail"/>
			<s:set var="email" value="form.email"/>
			<table id='eml' style='display:none;'>
				<tr>
					<td valign='top'><loc:message name="propEmailAddress"/></td>
					<td><s:textarea name="form.address" rows="3" cols="70"/></td>
				</tr>
				<tr><td valign='top'><loc:message name="propEmailCC"/></td><td>
					<s:textarea name="form.cc" rows="2" cols="70"/>
				</td></tr>
				<tr><td valign='top'><loc:message name="propEmailBCC"/></td><td>
					<s:textarea name="form.bcc" rows="2" cols="70"/>
				</td></tr>
				<tr><td valign='top' style='border-top: black 1px dashed;'><loc:message name="propEmailSubject"/></td><td style='border-top: black 1px dashed;'>
					<s:textfield name="form.subject" size="70" style="margin-top:2px;"/>
				</td></tr>
				<tr><td valign='top'><loc:message name="propEmailMessage"/></td><td>
					<s:textarea name="form.message" rows="10" cols="70"/>
				</td></tr>
			</table>
			<s:if test="form.email == true">
				<script type="text/javascript">
					document.getElementById('eml').style.display = 'block';
				</script>
			</s:if>
		</TD>
	</TR>
	</s:else>
	<TR>
		<TD colspan='2'>
			<tt:section-title><br>&nbsp;</tt:section-title>
		</TD>
	</TR>
	<TR>
		<TD colspan='2' align='right'>
			<s:submit accesskey='%{#msg.accessGenerateReport()}' name='op' value='%{#msg.actionGenerateReport()}' title='%{#msg.titleGenerateReport()}'/>
		</TD>
	</TR>
	</s:else>
	</TABLE>
<script type="text/javascript">
	function selectionChanged() {
		if (document.getElementsByName('form.all')==null || document.getElementsByName('form.all').length==0) return;
		var allSubjects = document.getElementsByName('form.all')[0].checked;
		var objSubjects = document.getElementsByName('form.subjects')[0];
		var objReports = document.getElementsByName('form.reports');
		var objSince = document.getElementsByName('form.since')[0];
		var studentSchedule = false;
		var instructorSchedule = false;
		objSubjects.disabled=allSubjects;
	}
</script>
</s:form>
</loc:bundle>
