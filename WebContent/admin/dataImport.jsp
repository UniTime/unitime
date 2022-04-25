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
<s:form action="dataImport" enctype="multipart/form-data" method="POST">
	<table style="width:100%;">
		<s:if test="#request.table != null">
			<tr><td colspan='2'>
				<s:property value="%{#request.table}" escapeHtml="false"/>
			</td></tr>
		</s:if>
		<s:if test="#request.log != null && #request.log != ''">
			<tr><td colspan='2'>
			<tt:section-header>
				<tt:section-title><loc:message name="sectionDataExchangeLog"><s:property value="%{#request.logname}" escapeHtml="false"/></loc:message></tt:section-title>
				<s:hidden name="log" value="%{#request.logid}"/>
				<s:submit accesskey='%{#msg.accessRefreshLog()}' name='form.op' value='%{#msg.actionRefreshLog()}' title='%{#msg.titleRefreshLog(#msg.accessRefreshLog())}'/>
				</tt:section-header>
			</td></tr>
			<tr><td colspan='2'>
				<s:property value="%{#request.log}" escapeHtml="false"/>
			</td></tr>
		</s:if>
		<tr><td colspan='2'>
			<tt:section-header>
				<tt:section-title><loc:message name="sectioDateImport"/></tt:section-title>
				<s:submit name='form.op' value='%{#msg.actionImport()}'/>
			</tt:section-header>
		</td></tr>
		<tr>
			<td nowrap><loc:message name="fieldImportFile"/>:</td>
			<td><s:file name="form.file"/><s:fielderror fieldName="form.file"/></td>
		</tr>
		<tr><td colspan='2'>&nbsp;</td></tr>
		<tr><td colspan='2'>
			<tt:section-header>
				<tt:section-title><loc:message name="sectioDateExport"/></tt:section-title>
				<s:submit name='form.op' value='%{#msg.actionExport()}'/>
			</tt:section-header>
		</td></tr>
		<tr>
			<td nowrap><loc:message name="fieldExportType"/>:</td>
			<td>
				<s:select name="form.export" list="form.exportTypes" listKey="value" listValue="label"/>
				<s:fielderror fieldName="form.export"/>
			</td>
		</tr>
		<tr><td colspan='2'>&nbsp;</td></tr>
		<tr><td colspan='2'><tt:section-title><loc:message name="sectionDataExchangeOptions"/></tt:section-title></td></tr>
		<tr>
			<td nowrap><loc:message name="fieldDataExchangeEmail"/>:</td>
			<td>
				<s:checkbox name="form.email" onclick="document.getElementById('eml').style.display=(this.checked?'inline':'none');" id="emlChk"/>
				<s:textfield name="form.address" size="70" cssClass="eml" style="display:none;" id="eml"/>
				<script type="text/javascript">document.getElementById('eml').style.display=(document.getElementById('emlChk').checked?'inline':'none');</script>
			</td>
		</tr>
		<tr><td colspan='2'><tt:section-title/></td></tr>
		<tr>
			<td align="right" colspan='2'>
				<s:submit name='form.op' value='%{#msg.actionImport()}'/>
				<s:submit name='form.op' value='%{#msg.actionExport()}'/>
			</td>
		</tr>
	</table>
</s:form>
</loc:bundle>
