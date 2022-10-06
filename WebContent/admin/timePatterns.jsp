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
<tt:confirm name="confirmDelete"><loc:message name="confirmTimeDatePattern"/></tt:confirm>
<s:form action="timePatternEdit">
<s:hidden name="form.uniqueId"/><s:fielderror fieldName="form.uniqueId"/>
<s:if test="form.op != 'List'">
 	<s:hidden name="form.editable"/>
	<s:hidden name="form.nextId"/>
	<s:hidden name="form.previousId"/>
	<table class="unitime-MainTable">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<s:if test="form.op == #msg.actionSaveTimePattern()">
							<loc:message name="sectAddTimePattern"/>
						</s:if><s:else>
							<loc:message name="sectEditTimePattern"/>
						</s:else>
					</tt:section-title>
					<s:submit name='op' value='%{form.op}'/>
					<s:if test="form.hasPrevious == true">
						<s:submit name='op' value='%{#msg.actionPreviousTimePattern()}'/>
					</s:if>
					<s:if test="form.hasNext == true">
						<s:submit name='op' value='%{#msg.actionNextTimePattern()}'/>
					</s:if>
					<s:if test="form.op == #msg.actionUpdateTimePattern() && form.editable == true">
						<s:submit name='op' value='%{#msg.actionDeleteTimePattern()}' onclick="return confirmDelete();"/>
					</s:if>
					<s:submit name='op' value='%{#msg.actionBackToTimePatterns()}'/>
				</tt:section-header>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propTimePatternName"/></TD>
			<TD>
				<s:textfield name="form.name" size="50" maxlength="100"/>
				<s:fielderror fieldName="form.name"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propTimePatternType"/></TD>
			<TD>
				<s:if test="form.editable == true">
					<s:textfield name="form.nrMtgs" size="2" maxlength="2"/>
					&times;
					<s:textfield name="form.minPerMtg" size="3" maxlength="3"/>
					&nbsp;
					<s:select name="form.type" list="form.types" listKey="value" listValue="label"/>
					<s:fielderror fieldName="form.nrMtgs"/>
					<s:fielderror fieldName="form.minPerMtg"/>
					<s:fielderror fieldName="form.type"/>
				</s:if><s:else>
					<s:property value="form.nrMtgs"/><s:hidden name="form.nrMtgs"/>
					&times;
					<s:property value="form.minPerMtg"/><s:hidden name="form.minPerMtg"/>
					&nbsp;
					<s:select name="form.type" list="form.types" listKey="value" listValue="label"/>
					<s:fielderror fieldName="form.type"/>
				</s:else>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propTimePatternVisible"/></TD>
			<TD>
				<s:checkbox name="form.visible"/>
				<s:fielderror fieldName="form.visible"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propTimePatternSlotsPerMeeting"/></TD>
			<TD>
				<s:if test="form.editable == true">
					<s:textfield name="form.slotsPerMtg" size="3" maxlength="3"/>
					<loc:message name="hintTimePatternSlotsPerMeeting"/>
					<s:fielderror fieldName="form.slotsPerMtg"/>
				</s:if><s:else>
					<s:property value="form.slotsPerMtg"/><s:hidden name="form.slotsPerMtg"/>
					<loc:message name="hintTimePatternSlotsPerMeeting"/>
				</s:else>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propTimePatternBreakTime"/></TD>
			<TD>
				<s:textfield name="form.breakTime" size="3" maxlength="3"/>
				<s:fielderror fieldName="form.breakTime"/>
			</TD>
		</TR>

		<TR>
			<TD style="vertical-align: top;"><loc:message name="propTimePatternDays"/></TD>
			<TD>
				<s:if test="form.editable == true">
					<s:textarea name="form.dayCodes" rows="5" cols="10"/>
				</s:if><s:else>
					<s:property value="form.dayCodes"/><s:hidden name="form.dayCodes"/>
				</s:else>
				<s:fielderror fieldName="form.dayCodes"/>
			</TD>
		</TR>

		<TR>
			<TD style="vertical-align: top;"><loc:message name="propTimePatternStartTimes"/></TD>
			<TD>
				<s:if test="form.editable == true">
					<s:textarea name="form.startTimes" rows="5" cols="10"/>
				</s:if><s:else>
					<s:property value="form.startTimes"/><s:hidden name="form.startTimes"/>
				</s:else>
				<s:fielderror fieldName="form.startTimes"/>
			</TD>
		</TR>

		<TR>
			<TD valign="top"><loc:message name="propTimePatternDepartments"/></TD>
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
		
		<s:if test="#request.example != null">
		<TR>
			<TD valign='top'><loc:message name="propTimePatternExample"/></TD>
			<TD>
				<s:property value="#request.example" escapeHtml="false"/>
			</TD>
		</TR>
		</s:if>
		<TR>
			<TD align="right" colspan="2">
				<tt:section-title/>
			</TD>
		</TR>
		
		<TR>
			<TD align="right" colspan="2">
				<s:submit name='op' value='%{form.op}'/>
				<s:if test="form.hasPrevious == true">
					<s:submit name='op' value='%{#msg.actionPreviousTimePattern()}'/>
				</s:if>
				<s:if test="form.hasNext == true">
					<s:submit name='op' value='%{#msg.actionNextTimePattern()}'/>
				</s:if>
				<s:if test="form.op == #msg.actionUpdateTimePattern() && form.editable == true">
					<s:submit name='op' value='%{#msg.actionDeleteTimePattern()}' onclick="return confirmDelete();"/>
				</s:if>
				<s:submit name='op' value='%{#msg.actionBackToTimePatterns()}'/>			
			</TD>
		</TR>
	</TABLE>

<BR>&nbsp;<BR>
</s:if><s:else>
<table class="unitime-MainTable">
	<TR>
		<TD colspan='10'>
			<tt:section-header>
				<tt:section-title><loc:message name="sectTimePatterns"/></tt:section-title>
				<s:submit name='op' value='%{#msg.actionAddTimePattern()}' title='%{#msg.titleAddTimePattern()}'/>
				<s:submit name='op' value='%{#msg.actionAssingDepartmentsToTimePatterns()}' title='%{#msg.titleAssingDepartmentsToTimePatterns()}'/>
				<s:submit name='op' value='%{#msg.actionExactTimesCSV()}' title='%{#msg.titleExactTimesCSV()}'/>
				<s:submit name='op' value='%{#msg.actionExportCsv()}' title='%{#msg.titleExportTimePatternsCSV()}'/>
			</tt:section-header>
		</TD>
	</TR>
	<s:property value="#request.table" escapeHtml="false"/>
	<TR>
		<TD colspan='10'>
			<tt:section-title/>
		</TD>
	</TR>
	<TR>
		<TD colspan='10' align="right">
			<s:submit name='op' value='%{#msg.actionAddTimePattern()}' title='%{#msg.titleAddTimePattern()}'/>
			<s:submit name='op' value='%{#msg.actionAssingDepartmentsToTimePatterns()}' title='%{#msg.titleAssingDepartmentsToTimePatterns()}'/>
			<s:submit name='op' value='%{#msg.actionExactTimesCSV()}' title='%{#msg.titleExactTimesCSV()}'/>
			<s:submit name='op' value='%{#msg.actionExportCsv()}' title='%{#msg.titleExportTimePatternsCSV()}'/>
		</TD>
	</TR>
</TABLE>
<s:if test="#request.hash != null">
	<SCRIPT type="text/javascript">
		location.hash = '<%=request.getAttribute("hash")%>';
	</SCRIPT>
</s:if>
</s:else>
</s:form>
</loc:bundle>