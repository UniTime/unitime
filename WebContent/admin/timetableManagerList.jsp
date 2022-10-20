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
<s:form action="timetableManagerList">
<table class="unitime-MainTable">
	<TR>
		<TD align="right" colspan="8">
			<tt:section-header>
				<tt:section-title>
					<s:property value="title"/>
				</tt:section-title>
				<sec:authorize access="hasPermission(null, null, 'TimetableManagerAdd')">
					<s:submit name="op" value="%{#msg.actionAddTimetableManager()}"
						accesskey="%{#msg.accessAddTimetableManager()}" title="%{#msg.titleAddTimetableManager(#msg.accessAddTimetableManager())}"/>
				</sec:authorize>
				<s:submit name="op" value="%{#msg.actionExportPdf()}"
						accesskey="%{#msg.accessExportPdf()}" title="%{#msg.titleExportPdf(#msg.accessExportPdf())}"/>
				<s:submit name="op" value="%{#msg.actionExportCsv()}"
						accesskey="%{#msg.accessExportCsv()}" title="%{#msg.titleExportCsv(#msg.accessExportCsv())}"/>
			</tt:section-header>
		</TD>
	</TR>
	<s:property value="table" escapeHtml="false"/>
	<tr>
		<td align="center" class="WelcomeRowHead" colspan="8">&nbsp;</td>
	</tr>
	<tr>
		<td align="left" colspan="4">
			<s:checkbox name="all" onclick="submit();"/>
			<loc:message name="checkShowAllManagers"/>
		</td>
		<td align="right" colspan="4">
			<sec:authorize access="hasPermission(null, null, 'TimetableManagerAdd')">
				<s:submit name="op" value="%{#msg.actionAddTimetableManager()}"
					accesskey="%{#msg.accessAddTimetableManager()}" title="%{#msg.titleAddTimetableManager(#msg.accessAddTimetableManager())}"/>
			</sec:authorize>
			<s:submit name="op" value="%{#msg.actionExportPdf()}"
					accesskey="%{#msg.accessExportPdf()}" title="%{#msg.titleExportPdf(#msg.accessExportPdf())}"/>
			<s:submit name="op" value="%{#msg.actionExportCsv()}"
					accesskey="%{#msg.accessExportCsv()}" title="%{#msg.titleExportCsv(#msg.accessExportCsv())}"/>
		</td>
	</tr>
</table>
</s:form>
</loc:bundle>
