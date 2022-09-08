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
<tt:back-mark back="true" clear="true" title="${MSG.sectRoomAvailability()}" uri="roomAvailability.action"/>
<s:form action="roomAvailability">
	<s:hidden name="form.showSections"/>
	<s:hidden name="form.subjectArea"/>
	<table class="unitime-MainTable">
	<TR>
		<TD colspan='2'>
			<tt:section-header>
				<tt:section-title><loc:message name="filter"/></tt:section-title>
				<s:submit name='form.op' value="%{#msg.buttonApply()}" accesskey="%{#msg.accessApply()}" title="%{#msg.titleApply()}"/>
				<s:if test="form.table != null">
					<s:submit name='form.op' value="%{#msg.buttonExportPDF()}"/>
					<s:submit name='form.op' value="%{#msg.buttonExportCSV()}"/>
				</s:if>
				<s:submit name='form.op' value="%{#msg.buttonRefresh()}" accesskey="%{#msg.accessRefresh()}" title="%{#msg.titleRefresh()}"/>
			</tt:section-header>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap><loc:message name="filterExaminationProblem"/></TD>
		<TD>
			<s:select name="form.examType"
				list="#request.examTypes" listKey="uniqueId" listValue="label"/>
		</TD>
	</TR>
	<TR>
		<TD width="10%" nowrap><loc:message name="filterRoomFilter"/></TD>
		<TD>
			<s:textfield name="form.filter" size="80"/>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap><loc:message name="filterDisplayExaminations"/></TD>
		<TD>
			<s:checkbox name="form.includeExams"/>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap><loc:message name="filterCompareExaminations"/></TD>
		<TD>
			<s:checkbox name="form.compare"/>
		</TD>
	</TR>
	<s:if test="#request.timestamp != null">
		<TR>
  			<TD width="10%" nowrap><loc:message name="propLastUpdate"/></TD>
			<TD>
				<s:property value="#request.timestamp"/>
			</TD>
		</TR>
	</s:if>
	</TABLE>

	<BR><BR>
	<s:if test="form.table == null">
		<table class="unitime-MainTable">
			<tr><td><i>
				<s:if test="form.examType == -1">
					<loc:message name="messageNoExaminationProblemSelected"/>
				</s:if><s:else>
					<loc:message name="messageNothingToDisplay"/>
				</s:else>
			</i></td></tr>
		</table>
	</s:if>
	<s:else>
		<table class="unitime-MainTable">
			<s:property value="form.table" escapeHtml="false"/>
		</table>
	</s:else>
	<s:if test="#request.hash != null">
		<SCRIPT type="text/javascript">
			location.hash = '<%=request.getAttribute("hash")%>';
		</SCRIPT>
	</s:if>
	<table class="unitime-MainTable">
	<TR>
		<TD>
			<tt:section-title/>
		</TD>
	</TR>
	<TR>
		<TD align="right">
			<s:submit name='form.op' value="%{#msg.buttonApply()}" accesskey="%{#msg.accessApply()}" title="%{#msg.titleApply()}"/>
			<s:if test="form.table != null">
				<s:submit name='form.op' value="%{#msg.buttonExportPDF()}"/>
				<s:submit name='form.op' value="%{#msg.buttonExportCSV()}"/>
			</s:if>
			<s:submit name='form.op' value="%{#msg.buttonRefresh()}" accesskey="%{#msg.accessRefresh()}" title="%{#msg.titleRefresh()}"/>
		</TD>
	</TR>
	</TABLE>
</s:form>
</loc:bundle>