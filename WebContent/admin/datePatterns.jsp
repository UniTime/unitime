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
<tt:confirm name="confirmDelete"><loc:message name="confirmDeleteDatePattern"/></tt:confirm>
<s:form action="datePatternEdit">
<s:hidden name="form.uniqueId"/><s:fielderror fieldName="form.uniqueId"/>
<s:if test="form.op != 'List'">
	<s:hidden name="form.isUsed"/>
	<s:hidden name="form.sessionId"/>
	<s:hidden name="form.nextId"/>
	<s:hidden name="form.previousId"/>
	<table class="unitime-MainTable">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<s:if test="form.op == #msg.actionSaveDatePattern()">
							<loc:message name="sectAddDatePattern"/>
						</s:if><s:else>
							<loc:message name="sectEditDatePattern"/>
						</s:else>
					</tt:section-title>
					<s:submit name='op' value='%{form.op}'/>
					<s:if test="form.hasPrevious == true">
						<s:submit name='op' value='%{#msg.actionPreviousDatePattern()}'/>
					</s:if>
					<s:if test="form.hasNext == true">
						<s:submit name='op' value='%{#msg.actionNextDatePattern()}'/>
					</s:if>
					<s:if test="form.op == #msg.actionUpdateDatePattern() && form.isUsed == false && form.isDefault == false">
						<s:submit name='op' value='%{#msg.actionDeleteDatePattern()}' onclick="return confirmDelete();"/>
					</s:if>
					<s:if test="form.isDefault == false && form.typeInt <= 2">
						<s:submit name='op' value='%{#msg.actionMakeDatePatternDefaulf()}'/>
					</s:if>
					<s:submit name='op' value='%{#msg.actionBackToDatePatterns()}'/>
				</tt:section-header>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propDatePatternName"/></TD>
			<TD>
				<s:textfield name="form.name" size="50" maxlength="100"/>
				<s:fielderror fieldName="form.name"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propDatePatternType"/></TD>
			<TD>
				<s:select name="form.type" id="__type" onchange="hideParentsIfNeeded();"
					list="form.types" listKey="value" listValue="label"/>
				<s:fielderror fieldName="form.type"/>
			</TD>
		</TR>
		
		<TR>
			<TD><loc:message name="propDatePatternNbrWeeks"/></TD>
			<TD>
				<s:textfield name="form.numberOfWeeks" size="10" maxlength="10"/>
				<s:fielderror fieldName="form.numberOfWeeks"/>
				<i><loc:message name="infoDatePatternNbrWeeks"/></i>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propDatePatternVisible"/></TD>
			<TD>
				<s:checkbox name="form.visible"/>
				<s:fielderror fieldName="form.visible"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propDatePatternDefault"/></TD>
			<TD>
				<s:checkbox name="form.isDefault" disabled="true"/>
				<s:fielderror fieldName="form.isDefault"/>
			</TD>
		</TR>
		
		<TR id="__departments">
			<TD valign="top"><loc:message name="propDatePatternDepartments"/></TD>
			<TD>
				<s:iterator value="form.departmentIds" var="deptId" status="stat"><s:set var="ctr" value="#stat.index"/>
					<s:iterator value="#request.deptsList" var="dept">
						<s:if test="#dept.id == #deptId">
							<s:property value="#dept.value"/>
							<s:hidden name="form.departmentIds[%{#ctr}]"/>
							<br>
						</s:if>
					</s:iterator>
				</s:iterator>
				<s:select name="form.departmentId"
					list="#request.deptsList" listKey="id" listValue="value"
					headerKey="-1" headerValue="%{#msg.itemSelect()}"/>
				&nbsp;
				<s:submit name='op' value='%{#msg.actionAddDepartment()}'/>
				&nbsp;
				<s:submit name='op' value='%{#msg.actionRemoveDepartment()}'/>
				&nbsp;
				<s:fielderror fieldName="form.department"/>
			</TD>
		</TR>
		
		<s:if test="#request.datePatternParentsList != null && !#request.datePatternParentsList.isEmpty()">
		<TR id="__parents">		
		    <TD valign="top"><loc:message name="propDatePatternAltPatternSets"/></TD>
			<TD>
				<s:iterator value="form.parentIds" var="parentId" status="stat"><s:set var="ctr" value="#stat.index"/>
					<s:iterator value="#request.datePatternParentsList" var="parent">
						<s:if test="#parent.uniqueId == #parentId">
							<s:property value="#parent.name"/>
							<s:hidden name="form.parentIds[%{#ctr}]"/>
							<br>
						</s:if>
					</s:iterator>
				</s:iterator>
				<s:select name="form.parentId"
					list="#request.datePatternParentsList" listKey="uniqueId" listValue="name"
					headerKey="-1" headerValue="%{#msg.itemSelect()}"/>
				&nbsp;
				<s:submit name='op' value='%{#msg.actionAddAltPatternSet()}'/>
				&nbsp;
				<s:submit name='op' value='%{#msg.actionRemovePatternSet()}'/>
				&nbsp;
				<s:fielderror fieldName="form.parent"/>
			</TD>
		</TR>
		</s:if>
		
		<s:if test="#request.datePatternChildrenList != null && !#request.datePatternChildrenList.isEmpty()">
		<TR id="__children">		
		    <TD valign="top"><loc:message name="propDatePatternChildren"/></TD>
			<TD>
				<s:iterator value="form.childrenIds" var="childId" status="stat"><s:set var="ctr" value="#stat.index"/>
					<s:iterator value="#request.datePatternChildrenList" var="child">
						<s:if test="#child.uniqueId == #childId">
							<s:property value="#child.name"/>
							<s:hidden name="form.childrenIds[%{#ctr}]"/>
							<br>
						</s:if>
					</s:iterator>
				</s:iterator>
				<s:select name="form.childId"
					list="#request.datePatternChildrenList" listKey="uniqueId" listValue="name"
					headerKey="-1" headerValue="%{#msg.itemSelect()}"/>
				&nbsp;
				<s:submit name='op' value='%{#msg.actionAddDatePattern()}'/>
				&nbsp;
				<s:submit name='op' value='%{#msg.actionRemoveDatePattern()}'/>
				&nbsp;
				<s:fielderror fieldName="form.child"/>
			</TD>
		</TR>
		</s:if>
		

		<TR id="__pattern">
			<TD><loc:message name="propDatePatternPattern"/></TD><TD><s:fielderror fieldName="form.pattern"/></TD>
		</TR>
		<TR id="__patternTable">
			<TD colspan='2'>
				<s:property value="#request.pattern" escapeHtml="false"/>
			</TD>
		</TR>

		<TR>
			<TD align="right" colspan="2">
				<tt:section-title/>
			</TD>
		</TR>
		
		<TR>
			<TD align="right" colspan="2">
				<s:submit name='op' value='%{form.op}'/>
				<s:if test="form.hasPrevious == true">
					<s:submit name='op' value='%{#msg.actionPreviousDatePattern()}'/>
				</s:if>
				<s:if test="form.hasNext == true">
					<s:submit name='op' value='%{#msg.actionNextDatePattern()}'/>
				</s:if>
				<s:if test="form.op == #msg.actionUpdateDatePattern() && form.isUsed == false && form.isDefault == false">
					<s:submit name='op' value='%{#msg.actionDeleteDatePattern()}' onclick="return confirmDelete();"/>
				</s:if>
				<s:if test="form.isDefault == false && form.typeInt <= 2">
					<s:submit name='op' value='%{#msg.actionMakeDatePatternDefaulf()}'/>
				</s:if>
				<s:submit name='op' value='%{#msg.actionBackToDatePatterns()}'/>
			</TD>
		</TR>
	</TABLE>

<BR>
</s:if><s:else>
<table class="unitime-MainTable">
	<TR>
		<TD colspan='6'>
			<tt:section-header>
				<tt:section-title><loc:message name="sectDatePatterns"/></tt:section-title>
				<s:submit name='op' value='%{#msg.actionAddDatePattern()}' title='%{#msg.titleAddDatePattern()}'/>
				<s:submit name='op' value='%{#msg.actionAssingDepartmentsToDatePatterns()}' title='%{#msg.titleAssingDepartmentsToDatePatterns()}'/>
				<s:submit name='op' value='%{#msg.actionPushUpDatePatterns()}' title='%{#msg.titlePushUpDatePatterns()}'/>
				<s:submit name='op' value='%{#msg.actionExportCsv()}' title='%{#msg.titleExportDatePatternsCSV()}'/>
			</tt:section-header>
		</TD>
	</TR>
	<s:property value="#request.table" escapeHtml="false"/>
	<TR>
		<TD colspan='6'>
			<tt:section-title/>
		</TD>
	</TR>
	<TR>
		<TD colspan='6' align="right">
			<s:submit name='op' value='%{#msg.actionAddDatePattern()}' title='%{#msg.titleAddDatePattern()}'/>
			<s:submit name='op' value='%{#msg.actionAssingDepartmentsToDatePatterns()}' title='%{#msg.titleAssingDepartmentsToDatePatterns()}'/>
			<s:submit name='op' value='%{#msg.actionPushUpDatePatterns()}' title='%{#msg.titlePushUpDatePatterns()}'/>
			<s:submit name='op' value='%{#msg.actionExportCsv()}' title='%{#msg.titleExportDatePatternsCSV()}'/>
		</TD>
	</TR>
</TABLE>
<s:if test="#request.hash != null">
	<SCRIPT type="text/javascript">
		location.hash = '<%=request.getAttribute("hash")%>';
	</SCRIPT>
</s:if>
</s:else>
<script>
function hideParentsIfNeeded() {
	var type = document.getElementById('__type');
	if (type == null) return;
	var selected = type.selectedIndex >= 0 && type.options[type.selectedIndex].value == 'PatternSet';
	var parents = document.getElementById('__parents');
	if (parents != null) parents.style.display = ( selected ? 'none' : 'table-row');
	var children = document.getElementById('__children');
	if (children != null) children.style.display = ( selected ? 'table-row' : 'none');
	var pattern = document.getElementById('__pattern');
	if (pattern != null) pattern.style.display = ( selected ? 'none' : 'table-row');
	var patternTable = document.getElementById('__patternTable');
	if (patternTable != null) patternTable.style.display = ( selected ? 'none' : 'table-row');
	var showDepts = type.selectedIndex >= 0 && (type.options[type.selectedIndex].value == 'PatternSet' || type.options[type.selectedIndex].value == 'Extended');
	var departments = document.getElementById('__departments');
	if (departments != null) departments.style.display = (showDepts ? 'table-row' : 'none');
}
hideParentsIfNeeded();
</script>
</s:form>
</loc:bundle>