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
<TABLE style="width:100%;">
	<TR>
		<TD colspan='7'>
			<tt:section-header>
				<tt:section-title><loc:message name="sectionInstructionalTypes"/></tt:section-title>
				<sec:authorize access="hasPermission(null, null, 'InstructionalTypeAdd')">
					<s:form action="itypeDescEdit" style="display: inline-block;">
						<s:submit accesskey='%{#msg.accessAddIType()}' name='op' value='%{#msg.actionAddIType()}' title='%{#msg.titleAddIType(#msg.accessAddIType())}'/>
					</s:form>
				</sec:authorize>
				<s:form action="itypeDescList" style="display: inline-block;">
					<s:submit accesskey='%{#msg.accessExportPdf()}' name='op' value='%{#msg.actionExportPdf()}' title='%{#msg.titleExportPdf(#msg.accessExportPdf())}'/>
				</s:form>
			</tt:section-header>
		</TD>
	</TR>
	<s:property value="%{#request.itypeDescList}" escapeHtml="false"/>
	<TR><TD colspan='7'><tt:section-title/></TD></TR>
	<TR>
		<TD colspan='7' align="right">
			<sec:authorize access="hasPermission(null, null, 'InstructionalTypeAdd')">
				<s:form action="itypeDescEdit" style="display: inline-block;">
					<s:submit accesskey='%{#msg.accessAddIType()}' name='op' value='%{#msg.actionAddIType()}' title='%{#msg.titleAddIType(#msg.accessAddIType())}'/>
				</s:form>
			</sec:authorize>
			<s:form action="itypeDescList" style="display: inline-block;">
				<s:submit accesskey='%{#msg.accessExportPdf()}' name='op' value='%{#msg.actionExportPdf()}' title='%{#msg.titleExportPdf(#msg.accessExportPdf())}'/>
			</s:form>
		</TD>
	</TR>
</TABLE>
</loc:bundle>