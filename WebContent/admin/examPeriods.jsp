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
<tt:confirm name="confirmDelete"><loc:message name="confirmDeleteExamPerid"/></tt:confirm>
<s:form action="examPeriodEdit">
<s:hidden name="form.op"/>
<s:if test="form.op != 'List'">
	<s:hidden name="form.uniqueId"/><s:fielderror escape="false" fieldName="form.uniqueId"/>
	<s:hidden name="form.autoSetup"/>
	<s:hidden name="form.editable"/>
	<s:if test="form.autoSetup == true">
	<table class="unitime-MainTable">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<loc:message name="sectSetupExaminationPeriods"/>
					</tt:section-title>
					<s:submit name='op' value='%{form.op}'/>
					<s:submit name='op' value='%{#msg.actionBackToExaminationPeriods()}'/>
				</tt:section-header>
			</TD>
		</TR>
		
		<TR>
			<TD><loc:message name="propExamType"/></TD>
			<TD>
				<s:iterator value="#request.examTypes" var="type">
					<s:if test="#type.uniqueId == form.examType">
						<s:property value="#type.label"/>
					</s:if>
				</s:iterator>
				<s:hidden name="form.examType"/>
				<s:fielderror escape="false" fieldName="form.examType"/>
			</TD>
		</TR>
		

		<TR>
			<TD><loc:message name="prop1stPeriodStartTime"/></TD>
			<TD>
			<s:textfield name="form.start" size="4" maxlength="4"/> <loc:message name="noteTimeInMilitaryFormat"/>
			<s:fielderror escape="false" fieldName="form.start"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="prop1stPeriodLength"/> </TD>
			<TD>
			<s:textfield name="form.length" size="4" maxlength="4"/> <loc:message name="noteLengthInMinutes"/>
			<s:fielderror escape="false" fieldName="form.length"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="prop1stEventStartOffset"/></TD>
			<TD>
			<s:textfield name="form.startOffset" size="4" maxlength="4"/> <loc:message name="noteLengthInMinutes"/>
			<s:fielderror escape="false" fieldName="form.startOffset"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="prop1stEventStopOffset"/></TD>
			<TD>
			<s:textfield name="form.stopOffset" size="4" maxlength="4"/> <loc:message name="noteLengthInMinutes"/>
			<s:fielderror escape="false" fieldName="form.stopOffset"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="prop2ndPeriodStartTime"/></TD>
			<TD>
			<s:textfield name="form.start2" size="4" maxlength="4"/> <loc:message name="noteTimeInMilitaryFormat"/>
			<s:fielderror escape="false" fieldName="form.start2"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="prop2ndPeriodLength"/></TD>
			<TD>
			<s:textfield name="form.length2" size="4" maxlength="4"/> <loc:message name="noteLengthInMinutes"/>
			<s:fielderror escape="false" fieldName="form.length2"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="prop2ndEventStartOffset"/></TD>
			<TD>
			<s:textfield name="form.startOffset2" size="4" maxlength="4"/> <loc:message name="noteLengthInMinutes"/>
			<s:fielderror escape="false" fieldName="form.startOffset2"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="prop2ndEventStopOffset"/></TD>
			<TD>
			<s:textfield name="form.stopOffset2" size="4" maxlength="4"/> <loc:message name="noteLengthInMinutes"/>
			<s:fielderror escape="false" fieldName="form.stopOffset2"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="prop3rdPeriodStartTime"/></TD>
			<TD>
			<s:textfield name="form.start3" size="4" maxlength="4"/> <loc:message name="noteTimeInMilitaryFormat"/>
			<s:fielderror escape="false" fieldName="form.start3"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="prop3rdPeriodLength"/></TD>
			<TD>
			<s:textfield name="form.length3" size="4" maxlength="4"/> <loc:message name="noteLengthInMinutes"/>
			<s:fielderror escape="false" fieldName="form.length3"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="prop3rdEventStartOffset"/></TD>
			<TD>
			<s:textfield name="form.startOffset3" size="4" maxlength="4"/> <loc:message name="noteLengthInMinutes"/>
			<s:fielderror escape="false" fieldName="form.startOffset3"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="prop3rdEventStopOffset"/></TD>
			<TD>
			<s:textfield name="form.stopOffset3" size="4" maxlength="4"/> <loc:message name="noteLengthInMinutes"/>
			<s:fielderror escape="false" fieldName="form.stopOffset3"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="prop4thPeriodStartTime"/></TD>
			<TD>
			<s:textfield name="form.start4" size="4" maxlength="4"/> <loc:message name="noteTimeInMilitaryFormat"/>
			<s:fielderror escape="false" fieldName="form.start4"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="prop4thPeriodLength"/></TD>
			<TD>
			<s:textfield name="form.length4" size="4" maxlength="4"/> <loc:message name="noteLengthInMinutes"/>
			<s:fielderror escape="false" fieldName="form.length4"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="prop4thEventStartOffset"/></TD>
			<TD>
			<s:textfield name="form.startOffset4" size="4" maxlength="4"/> <loc:message name="noteLengthInMinutes"/>
			<s:fielderror escape="false" fieldName="form.startOffset4"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="prop4thEventStopOffset"/></TD>
			<TD>
			<s:textfield name="form.stopOffset4" size="4" maxlength="4"/> <loc:message name="noteLengthInMinutes"/>
			<s:fielderror escape="false" fieldName="form.stopOffset4"/>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="prop5thPeriodStartTime"/></TD>
			<TD>
			<s:textfield name="form.start5" size="4" maxlength="4"/> <loc:message name="noteTimeInMilitaryFormat"/>
			<s:fielderror escape="false" fieldName="form.start5"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="prop5thPeriodLength"/></TD>
			<TD>
			<s:textfield name="form.length5" size="4" maxlength="4"/> <loc:message name="noteLengthInMinutes"/>
			<s:fielderror escape="false" fieldName="form.length5"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="prop5thEventStartOffset"/></TD>
			<TD>
			<s:textfield name="form.startOffset5" size="4" maxlength="4"/> <loc:message name="noteLengthInMinutes"/>
			<s:fielderror escape="false" fieldName="form.startOffset5"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="prop5thEventStopOffset"/></TD>
			<TD>
			<s:textfield name="form.stopOffset5" size="4" maxlength="4"/> <loc:message name="noteLengthInMinutes"/>
			<s:fielderror escape="false" fieldName="form.stopOffset5"/>
			</TD>
		</TR>

		<TR>
			<TD colspan='2'><br><loc:message name="propExaminationDates"/></TD>
		</TR>
		
		<TR>
			<TD colspan='2'>
				<s:property value="form.patternHtml" escapeHtml="false"/>
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
					<s:submit name='op' value='%{#msg.actionBackToExaminationPeriods()}'/> 
			</TD>
		</TR>
	</TABLE>
	</s:if><s:else>
	<table class="unitime-MainTable">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<s:if test="form.op == #msg.actionSaveExaminationPeriod()">
							<loc:message name="sectAddExaminationPeriod"/>
						</s:if><s:else>
							<loc:message name="sectEditExaminationPeriod"/>
						</s:else>
					</tt:section-title>
					<s:submit name='op' value='%{form.op}'/>
					<s:if test="form.editable == true && form.op != #msg.actionSaveExaminationPeriod()">
						<s:submit name='op' value='%{#msg.actionDeleteExaminationPeriod()}' onclick="return confirmDelete();"/>
					</s:if>
					<s:submit name='op' value='%{#msg.actionBackToExaminationPeriods()}'/>
				</tt:section-header>
			</TD>
		</TR>
		
		<TR>
			<TD><loc:message name="propExamType"/></TD>
			<TD>
				<s:if test="form.op == #msg.actionSaveExaminationPeriod()">
					<s:hidden name="op2" value="" id="op2"/>
					<s:select name="form.examType"
						list="#request.examTypes" listKey="uniqueId" listValue="label"
						headerKey="" headerValue="%{#msg.itemSelect()}"
						onchange="document.getElementById('op2').value='Reload'; submit();" />
					<s:fielderror escape="false" fieldName="form.examType"/>
				</s:if><s:else>
					<s:iterator value="#request.examTypes" var="type">
						<s:if test="#type.uniqueId == form.examType">
							<s:property value="#type.label"/>
						</s:if>
					</s:iterator>
					<s:hidden name="form.examType"/>
					<s:fielderror escape="false" fieldName="form.examType"/>
				</s:else>
			</TD>
		</TR>
		

		<TR>
			<TD><loc:message name="propertyPeriodDate"/></TD>
			<TD>
				<s:if test="form.editable == true">
					<tt:calendar name="form.date"/>
				</s:if><s:else>
					<s:property value="form.date"/>
					<s:hidden name="form.date"/>
				</s:else>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propPeriodStartTime"/></TD>
			<TD>
				<s:if test="form.editable == true">
					<s:textfield name="form.start" size="4" maxlength="4"/> <loc:message name="noteTimeInMilitaryFormat"/>
					<s:fielderror escape="false" fieldName="form.start"/>
				</s:if><s:else>
					<s:property value="form.start"/> <loc:message name="noteTimeInMilitaryFormat"/>
					<s:hidden name="form.start"/>
				</s:else>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propPeriodLength"/></TD>
			<TD>
				<s:if test="form.editable == true">
					<s:textfield name="form.length" size="4" maxlength="4"/> <loc:message name="noteLengthInMinutes"/>
					<s:fielderror escape="false" fieldName="form.length"/>
				</s:if><s:else>
					<s:property value="form.length"/> <loc:message name="noteMinutes"/>
					<s:hidden name="form.length"/>
				</s:else>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propEventStartOffset"/></TD>
			<TD>
				<s:if test="form.editable == true">
					<s:textfield name="form.startOffset" size="4" maxlength="4"/> <loc:message name="noteLengthInMinutes"/>
					<s:fielderror escape="false" fieldName="form.startOffset"/>
				</s:if><s:else>
					<s:property value="form.startOffset"/> <loc:message name="noteMinutes"/>
					<s:hidden name="form.startOffset"/>
				</s:else>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propEventStopOffset"/></TD>
			<TD>
				<s:if test="form.editable == true">
					<s:textfield name="form.stopOffset" size="4" maxlength="4"/> <loc:message name="noteLengthInMinutes"/>
					<s:fielderror escape="false" fieldName="form.stopOffset"/>
				</s:if><s:else>
					<s:property value="form.stopOffset"/> <loc:message name="noteMinutes"/>
					<s:hidden name="form.stopOffset"/>
				</s:else>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propPeriodPreference"/></TD>
			<TD>
				<s:select name="form.prefLevel"
					list="form.prefLevels" listKey="uniqueId" listValue="prefName"/>
				<s:fielderror escape="false" fieldName="form.prefLevel"/>
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
				<s:if test="form.editable == true && form.op != #msg.actionSaveExaminationPeriod()">
					<s:submit name='op' value='%{#msg.actionDeleteExaminationPeriod()}' onclick="return confirmDelete();"/>
				</s:if>
				<s:submit name='op' value='%{#msg.actionBackToExaminationPeriods()}'/>
			</TD>
		</TR>
	</TABLE>
	</s:else>
<BR>
</s:if><s:else>
<table class="unitime-MainTable">
	<TR>
		<TD colspan='8'>
			<tt:section-header>
				<tt:section-title><loc:message name="sectExaminationPeriods"/></tt:section-title>
				<s:submit name='op' value='%{#msg.actionAddExaminationPeriod()}'
					title="%{#msg.titleAddExaminationPeriod()}"/>
				<s:iterator value="#request.examTypes" var="type">
					<s:if test="form.getCanAutoSetup(#type.uniqueId) == true">
						<s:submit name='op' value='%{#msg.actionSetupExaminationPeriods(#type.label)}'
							title='%{#msg.titleSetupExaminationPeriods(#type.label)}'/>
					</s:if>
				</s:iterator>
			</tt:section-header>
		</TD>
	</TR>
	<s:property value="examPeriods" escapeHtml="false"/>
	<TR>
		<TD colspan='8'>
			<tt:section-title/>
		</TD>
	</TR>
	<TR>
		<TD colspan='8' align="right">
				<s:submit name='op' value='%{#msg.actionAddExaminationPeriod()}'
					title="%{#msg.titleAddExaminationPeriod()}"/>
				<s:iterator value="#request.examTypes" var="type">
					<s:if test="form.getCanAutoSetup(#type.uniqueId) == true">
						<s:submit name='op' value='%{#msg.actionSetupExaminationPeriods(#type.label)}'
							title='%{#msg.titleSetupExaminationPeriods(#type.label)}'/>
					</s:if>
				</s:iterator>
		</TD>
	</TR>
</TABLE>
</s:else>
<s:if test="#request.hash != null">
	<SCRIPT type="text/javascript">
		location.hash = '<%=request.getAttribute("hash")%>';
	</SCRIPT>
</s:if>
</s:form>
</loc:bundle>
