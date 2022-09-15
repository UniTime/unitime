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
<loc:bundle name="CourseMessages"><s:set var="msg" value="#attr.MSG"/> 
<s:form action="personalSchedule">
	<s:hidden name="form.sessionId"/>
	<table class="unitime-MainTable">
		<TR><TD align='right'>
			<s:if test="form.admin == true">
				<s:hidden name="form.uid" id="uid" onchange="submit();"/>
				<s:hidden name="form.fname" id="fname"/>
				<s:hidden name="form.mname" id="mname"/>
				<s:hidden name="form.lname" id="lname"/>
				<input type='button' value='${MSG.buttonLookup()}' onclick="lookup();" style="btn">
			</s:if>
			<s:if test="form.canExport == true">
				<s:submit name='form.op' value="%{#msg.actionExportPdf()}"
							title="%{#msg.titleExportPdf(#msg.accessExportPdf())}"
							accesskey="%{#msg.accessExportPdf()}"/>
				<s:submit name='form.op' value="%{#msg.actionExportIcal()}"
							title="%{#msg.titleExportIcal(#msg.accessExportIcal())}"
							accesskey="%{#msg.accessExportIcal()}"/>
			</s:if>
			<s:if test="form.logout == true">
				<s:submit name='form.op' value="%{#msg.buttonLogOut()}"
							title="%{#msg.titleLogOut(#msg.accessLogOut())}"
							accesskey="%{#msg.accessLogOut()}"/>
			</s:if>
		</TD></TR>
	</table>
	<s:if test="form.message != null">
		<BR>
		<div style='font-weight:bold;color:red;'>
			<s:property value="form.message"/>
		</div>
	</s:if>
	<s:if test="#request.message != null">
		<BR>
		<table class="unitime-MainTable">
			<s:property value="#request.message" escapeHtml="false"/>
		</table>
	</s:if>
	<s:if test="#request.sessions != null">
		<BR>
		<table class="unitime-MainTable">
			<s:property value="#request.sessions" escapeHtml="false"/>
		</table>
	</s:if>
	<s:if test="#request.iclsschd != null">
		<BR>
		<table class="unitime-MainTable">
			<s:property value="#request.iclsschd" escapeHtml="false"/>
		</table>
	</s:if>
	<s:if test="#request.ischedule != null">
		<BR>
		<table class="unitime-MainTable">
			<s:property value="#request.ischedule" escapeHtml="false"/>
		</table>
	</s:if>
	<s:if test="#request.iconf != null">
		<BR>
		<table class="unitime-MainTable">
			<s:property value="#request.iconf" escapeHtml="false"/>
		</table>
	</s:if>
	<s:if test="#request.sconf != null">
		<BR>
		<table class="unitime-MainTable">
			<s:property value="#request.sconf" escapeHtml="false"/>
		</table>
	</s:if>
	<s:if test="#request.clsschd != null">
		<BR>
		<table class="unitime-MainTable">
			<s:property value="#request.clsschd" escapeHtml="false"/>
		</table>
	</s:if>
	<s:if test="#request.schedule != null">
		<BR>
		<table class="unitime-MainTable">
			<s:property value="#request.schedule" escapeHtml="false"/>
		</table>
	</s:if>
	<s:if test="#request.conf != null">
		<BR>
		<table class="unitime-MainTable">
			<s:property value="#request.conf" escapeHtml="false"/>
		</table>
	</s:if>

	<table class="unitime-MainTable">
		<TR><TD><tt:section-title/></TD></TR>
		<TR><TD align='right'>
			<s:if test="form.admin == true">
				<input type='button' value='${MSG.buttonLookup()}' onclick="lookup();" style="btn">
			</s:if>
			<s:if test="form.canExport == true">
				<s:submit name='form.op' value="%{#msg.actionExportPdf()}"
							title="%{#msg.titleExportPdf(#msg.accessExportPdf())}"
							accesskey="%{#msg.accessExportPdf()}"/>
				<s:submit name='form.op' value="%{#msg.actionExportIcal()}"
							title="%{#msg.titleExportIcal(#msg.accessExportIcal())}"
							accesskey="%{#msg.accessExportIcal()}"/>
			</s:if>
			<s:if test="form.logout == true">
				<s:submit name='form.op' value="%{#msg.buttonLogOut()}"
							title="%{#msg.titleLogOut(#msg.accessLogOut())}"
							accesskey="%{#msg.accessLogOut()}"/>
			</s:if>
		</TD></TR>
	</table>
<script type="text/javascript">
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
</s:form>
</loc:bundle>