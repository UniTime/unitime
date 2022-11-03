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
<s:form action="lastChanges">
 
<script type="text/javascript" src="scripts/block.js"></script>
 	<script type="text/javascript">blToggleHeader('<loc:message name="filter"/>','dispFilter');blStart('dispFilter');</script>
	<table class="unitime-MainTable">
		<tr>	
		<th nowrap="nowrap" align="right"><loc:message name="filterDepartment"/></th>
		<td>
			<s:select name="form.departmentId" list="%{#request.departments}" listKey="uniqueId" listValue="label"/>
		</td>
		</tr>
		<tr>	
		<th nowrap="nowrap" align="right"><loc:message name="filterSubjectArea"/></th>
		<td>
			<s:select name="form.subjAreaId" list="%{#request.subjAreas}" listKey="uniqueId" listValue="subjectAreaAbbreviation"/>
		</td>
		</tr>

		<tr>	
		<th nowrap="nowrap" align="right"><loc:message name="filterManager"/></th>
		<td>
			<s:select name="form.managerId" list="%{#request.managers}" listKey="uniqueId" listValue="name"/>
		</td>
		</tr>

		<tr>	
		<th nowrap="nowrap" align="right"><loc:message name="filterNumberOfChanges"/></th>
		<td>
			<s:textfield name="form.n" size="8" maxlength="5" type="number" min="1" max="99999" title="%{#msg.filterNumberOfChanges()}"/>
		</td>
		</tr>

		<tr>
			<td colspan="2" align="right" style="padding-left: 20px;" width="100%;">
				<s:submit name='form.op' value="%{#msg.buttonApply()}"/>
				<s:submit name='form.op' value="%{#msg.buttonRefresh()}"/>
				<s:submit name='form.op' value="%{#msg.buttonExportPDF()}"/>
			</td>
		</tr>
	</TABLE>
	<script type="text/javascript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
	<table class="unitime-MainTable">
		<tr>
			<td align="right" style="padding-left: 20px;" width="100%;">
				<s:submit name='form.op' value="%{#msg.buttonRefresh()}"/>
				<s:submit name='form.op' value="%{#msg.buttonExportPDF()}"/>
			</td>
		</tr>
		</TABLE>
	<script type="text/javascript">blEndCollapsed('dispFilter');</script>

	<BR><BR>

	<s:if test="#request.table != null">
		<table class="unitime-MainTable" style="padding-top: 20px;">
			<s:property value="%{#request.table}" escapeHtml="false"/>
		</table>
	</s:if>
	
 </s:form></loc:bundle>
