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
<loc:bundle name="ExaminationMessages"><s:set var="msg" value="#attr.MSG"/> 
<s:form action="examList">
	<table class="unitime-MainTable">
		<tr>
			<th nowrap="nowrap"><loc:message name="propExamType"/></th><td>
				<s:select name="form.examType" list="%{#request.examTypes}" listKey="uniqueId" listValue="label"/>
			</td>
			<th nowrap="nowrap" style="padding-left: 10px;"><loc:message name="propExamSubject"/></th><td>
				<s:select name="form.subjectAreaId" id="subjectId" list="form.subjectAreas" listKey="id" listValue="value"/>
			</td>
			<th nowrap="nowrap" style="padding-left: 10px;"><loc:message name="propExamCourseNumber"/></th><td>
				<tt:course-number name="form.courseNbr" configuration="subjectId=\${subjectId};notOffered=exclude"
					size="15" title="%{#msg.titleCourseNumberSuggestBox()}"/>
			</td>
			<td align="right" style="padding-left: 20px;" width="100%;">
				<s:submit name='form.op' value="%{#msg.buttonSearch()}"/>
				<s:submit name='form.op' value="%{#msg.buttonExportPDF()}"/>
				<s:submit name='form.op' value="%{#msg.buttonExportCSV()}"/>
				<sec:authorize access="hasPermission(null, 'Session', 'ExaminationAdd')">
					<s:submit name='form.op' value="%{#msg.buttonAddExamination()}"/>
				</sec:authorize>
			</td>
		</tr>
		<tr>
			<td colspan="7" align="center">
				<s:actionerror/>
			</td>
		</tr>
	</table>
	
	<s:if test="#request.table != null">
		<table class="unitime-MainTable" style="padding-top: 20px;">
			<s:property value="%{#request.table}" escapeHtml="false"/>
		</table>
		<table class="unitime-MainTable">
			<tr>
				<td align="right" class="top-border-solid" style="padding-top: 3px;">
					<s:submit name='form.op' value="%{#msg.buttonSearch()}"/>
					<s:submit name='form.op' value="%{#msg.buttonExportPDF()}"/>
					<s:submit name='form.op' value="%{#msg.buttonExportCSV()}"/>
					<sec:authorize access="hasPermission(null, 'Session', 'ExaminationAdd')">
						<s:submit name='form.op' value="%{#msg.buttonAddExamination()}"/>
					</sec:authorize>
				</td>
			</tr>
		</table>
	</s:if>
	
	<s:if test="#request.hash != null">
		<SCRIPT type="text/javascript">
			location.hash = '<%=request.getAttribute("hash")%>';
		</SCRIPT>
	</s:if>
</s:form></loc:bundle>