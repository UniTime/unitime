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
<tt:back-mark back="true" clear="true" title="${MSG.backAssignedExaminations()}" uri="assignedExams.action"/>
<s:form action="assignedExams">
	<script type="text/javascript">blToggleHeader('<loc:message name="filter"/>','dispFilter');blStart('dispFilter');</script>
	<table class="unitime-MainTable">
		<TR>
			<TD width="10%" nowrap><loc:message name="filterShowClassesCourses"/></TD>
			<TD>
				<s:checkbox name="form.showSections"/>
			</TD>
		</TR>
	</TABLE>
	<script type="text/javascript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
	<script type="text/javascript">blEndCollapsed('dispFilter');</script>
	<table class="unitime-MainTable">
	<TR>
  		<TD width="10%" nowrap><loc:message name="filterExaminationProblem"/></TD>
		<TD>
			<s:select name="form.examType"
				list="#request.examTypes" listKey="uniqueId" listValue="label"/>
		</TD>
	</TR>
	<TR>
		<TD width="10%" nowrap><loc:message name="filterSubjectAreas"/></TD>
		<TD>
			<s:select name="form.subjectArea" list="form.subjectAreas" listKey="id" listValue="value"/>
		</TD>
	</TR>
	<TR>
		<TD colspan='2' align='right'>
			<s:submit name='form.op' value="%{#msg.buttonApply()}" accesskey="%{#msg.accessApply()}" title="%{#msg.titleApply()}"/>
			<s:if test="form.table != null">
				<s:submit name='form.op' value="%{#msg.buttonExportPDF()}"/>
				<s:submit name='form.op' value="%{#msg.buttonExportCSV()}"/>
			</s:if>
			<s:submit name='form.op' value="%{#msg.buttonRefresh()}" accesskey="%{#msg.accessRefresh()}" title="%{#msg.titleRefresh()}"/>
		</TD>
	</TR>
	</TABLE>

	<BR><BR>
	<s:if test="form.table == null">
		<table class="unitime-MainTable">
			<tr><td><i>
				<s:if test="form.subjectArea == null || form.subjectArea == 0">
					<loc:message name="messageNoSubject"/>
				</s:if>
				<s:if test="form.subjectArea < 0">
					<loc:message name="messageAllExamsAreNotAssinged"/>
				</s:if>
				<s:if test="form.subjectArea > 0">
					<loc:message name="messageAllExamsOfASubjectAreNotAssinged"><s:property value="form.subjectAreaAbbv"/></loc:message>
				</s:if>
			</i></td></tr>
		</table>
	</s:if>
		<s:else>
		<table class="unitime-MainTable">
			<s:property value="form.table" escapeHtml="false"/>
			<tr><td colspan='%{form.nrColumns}'><tt:displayPrefLevelLegend/></td></tr>
		</table>
	</s:else>
	<s:if test="#request.hash != null">
		<SCRIPT type="text/javascript">
			location.hash = '<%=request.getAttribute("hash")%>';
		</SCRIPT>
	</s:if>
</s:form>
</loc:bundle>