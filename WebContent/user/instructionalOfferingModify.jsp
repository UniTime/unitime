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
<%@page import="org.unitime.timetable.webutil.JavascriptFunctions"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="tt" uri="http://www.unitime.org/tags-custom" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="loc" uri="http://www.unitime.org/tags-localization" %>
<loc:bundle name="CourseMessages"><s:set var="msg" value="#attr.MSG"/>
<s:form action="instructionalOfferingModify">
<script type="text/javascript">
function doClick(op, id) {
	document.forms[0].elements["hdnOp"].value=op;
	document.forms[0].elements["id"].value=id;
	document.forms[0].submit();
}
function updateSubpartTotal(subpartIndex) {
	displayInstructors=${form.displayDisplayInstructors};
	displayEnabledForStudentScheduling=${form.displayEnabledForStudentScheduling};
	subtotalName='form.subtotalIndexes['+subpartIndex+']';
	origLimitName='form.origMinLimit['+subpartIndex+']';
	minLimitName='form.minClassLimits['+subpartIndex+']';
	totalIndex=document.getElementsByName(subtotalName)[0].value;
	subtotalValueName='form.subtotalValues['+totalIndex+']';
	subtotalValueName1='subtotal1Values'+totalIndex;
	if (displayInstructors != 'false' || displayEnabledForStudentScheduling != 'false') {
		subtotalValueName2='subtotal2Values' + totalIndex;
	}
	origTotal=document.getElementsByName(subtotalValueName)[0].value;
	origSubpartLimit=document.getElementsByName(origLimitName)[0].value;
	newSubpartLimit=document.getElementsByName(minLimitName)[0].value;
	if(newSubpartLimit.length == 0 || (newSubpartLimit.search("[^0-9]")) >= 0) {
		newSubpartLimit=0;
	}
	newTotal=origTotal-origSubpartLimit+(newSubpartLimit-0);
	document.getElementsByName(subtotalValueName)[0].value=newTotal;
	document.getElementById(subtotalValueName1).innerHTML=newTotal;
	if (displayInstructors != 'false' || displayEnabledForStudentScheduling != 'false') {
		document.getElementById(subtotalValueName2).innerHTML=newTotal; 
	}
	document.getElementsByName(origLimitName)[0].value=newSubpartLimit;
}
function updateSnapshotTotal(subpartIndex) {
	displayInstructors=${form.displayDisplayInstructors};
	displayEnabledForStudentScheduling=${form.displayEnabledForStudentScheduling};
	subtotalName='form.subtotalIndexes['+subpartIndex+']';
	origLimitName='form.origSnapLimit['+subpartIndex+']';
	minLimitName='form.snapshotLimits['+subpartIndex+']';
	totalIndex=document.getElementsByName(subtotalName)[0].value;
	subtotalValueName='form.subtotalSnapValues['+totalIndex+']';
	subtotalValueName1='subtotal1SnapValues'+totalIndex;
	if (displayInstructors || displayEnabledForStudentScheduling) {
		subtotalValueName2='subtotal2SnapValues' + totalIndex;
	}
	origTotal=document.getElementsByName(subtotalValueName)[0].value;
	origSubpartLimit=document.getElementsByName(origLimitName)[0].value;
	newSubpartLimit=document.getElementsByName(minLimitName)[0].value;
	if(newSubpartLimit.length == 0 || (newSubpartLimit.search("[^0-9]")) >= 0) { newSubpartLimit=0;}
	newTotal=origTotal-origSubpartLimit+(newSubpartLimit-0);
	document.getElementsByName(subtotalValueName)[0].value=newTotal;
	document.getElementById(subtotalValueName1).innerHTML=newTotal;
	if (displayInstructors || displayEnabledForStudentScheduling) {
		document.getElementById(subtotalValueName2).innerHTML=newTotal;
	}
	document.getElementsByName(origLimitName)[0].value=newSubpartLimit;
}
function updateSubpartFlags(value, baseName, subpartIndex, flagName) {
	for (var i=0;i<${form.classIds.size()};i++) {
		var chbox=document.getElementsByName('form.' + baseName+'['+i+']');
		var subtotalIndexName='form.subtotalIndexes['+i+']';
		var subpartIndexValue=document.getElementsByName(subtotalIndexName)[0].value;
		if ((subpartIndexValue * 1) == (subpartIndex * 1) && chbox!=null && chbox.length>0){
			chbox[0].checked=value;
		}
	}
	var subpartFlag=document.getElementsByName('form.'+flagName+'['+subpartIndex+']');
	subpartFlag[0].checked=value;
	subpartFlag[1].checked=value;
}
function resetAllDisplayFlags(value, baseName) {
	for (var i=0;i<${form.classIds.size()};i++) {
	var chbox=document.getElementsByName('form.'+baseName+'['+i+']');
	if (chbox!=null && chbox.length>0)
	chbox[0].checked=value;
	}
}
</script>
<s:hidden name="form.instrOfferingId"/>
<s:hidden name="form.instrOfferingName"/>
<s:hidden name="form.instrOffrConfigId"/>
<s:hidden name="form.origSubparts"/>
<s:hidden name="form.displayMaxLimit"/>
<s:hidden name="form.displayOptionForMaxLimit"/>
<s:hidden name="form.displayEnrollment"/>
<s:hidden name="form.displaySnapshotLimit"/>
<s:hidden name="form.displayExternalId"/>
<s:hidden name="form.editExternalId"/>
<s:hidden name="form.editSnapshotLimits"/>
<s:hidden name="form.displayDisplayInstructors"/>
<s:hidden name="form.displayEnabledForStudentScheduling"/>
<s:hidden name="form.displayLms"/>
<input type="hidden" name="hdnOp" value="">
<input type="hidden" name="id" value="">
<table class="unitime-MainTable">
<TR><TD valign="middle" colspan="2"><tt:section-header>
	<tt:section-title><s:property value="form.instrOfferingName"/></tt:section-title>
	<s:submit name='op' value='%{#msg.actionUpdateMultipleClassSetup()}'
		accesskey='%{#msg.accessUpdateMultipleClassSetup()}' title='%{#msg.titleUpdateMultipleClassSetup(#msg.accessUpdateMultipleClassSetup())}'/>
	<s:submit name='op' value='%{#msg.actionBackToIODetail()}'
		accesskey='%{#msg.accessBackToIODetail()}' title='%{#msg.titleBackToIODetail(#msg.accessBackToIODetail())}'/>
</tt:section-header></TD></TR>
<s:if test="!fieldErrors.isEmpty()"><TR><TD colspan="2" align="left" class="errorTable">
	<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror escape="false"/>
</TD></TR></s:if>
<s:hidden name="form.instructionalMethodEditable"/>
<s:if test="form.instructionalMethods != null && !form.instructionalMethods.isEmpty()">
	<TR><TD><loc:message name="propertyInstructionalMethod"/></TD>
	<TD><s:if test="form.instructionalMethodEditable == true">
		<s:if test="form.instructionalMethodDefault == null">
			<s:select name="form.instructionalMethod" list="form.instructionalMethods" listKey="id" listValue="value" headerKey="-1" headerValue="%{#msg.selectNoInstructionalMethod()}"/>
		</s:if><s:if test="form.instructionalMethodDefault != null">
			<s:select name="form.instructionalMethod" list="form.instructionalMethods" listKey="id" listValue="value" headerKey="-1" headerValue="%{#msg.defaultInstructionalMethod(form.instructionalMethodDefault)}"/>
		</s:if></s:if>
		<s:else><s:hidden name="form.instructionalMethod"/><s:property value="form.instructionalMethodLabel"/></s:else>
	</TD></TR>
</s:if>
<s:hidden name="form.instrOffrConfigUnlimitedReadOnly"/>
<s:if test="form.instrOffrConfigUnlimitedReadOnly == true"><s:hidden name="form.instrOffrConfigUnlimited"/>
	<s:if test="form.instrOffrConfigUnlimited == true">
		<TR><TD align="left"><loc:message name="propertyUnlimitedEnrollment"/></TD>
		<TD><IMG border='0' title='${MSG.titleUnlimitedEnrollment()}' alt='true' align='middle' src='images/accept.png'></TD></TR>
	</s:if>
</s:if><s:else>
	<TR><TD><loc:message name="propertyUnlimitedEnrollment"/></TD>
	<TD><s:checkbox name="form.instrOffrConfigUnlimited" onclick="doClick('unlimited',0);"/></TD></TR>
</s:else>
<s:if test="form.instrOffrConfigUnlimited"><s:hidden name="form.instrOffrConfigLimit"/></s:if>
<s:else>
	<TR><TD><loc:message name="propertyConfigurationLimit"/></TD>
	<TD><s:textfield name="form.instrOffrConfigLimit" maxlength="5" size="5"/></TD></TR>
</s:else>
<TR><TD valign="top" style="white-space: nowrap; width: 165px;"><loc:message name="propertySchedulingSubpartLimits"/></TD>
	<TD><table class="unitime-Table">
		<s:iterator value="form.subtotalValues" var="v" status="stat"><s:set var="ctr" value="%{#stat.index}"/>
			<tr onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='default';" onmouseout="this.style.backgroundColor='transparent';">
				<td valign="top" align="right" nowrap>
					<s:hidden name="form.subtotalLabels[%{#ctr}]"/>
					<s:hidden name="form.subtotalValues[%{#ctr}]"/>
					<b><s:property value="form.subtotalLabels[#ctr].trim()"/>:</b>&nbsp;</td>
				<td align="right" nowrap><div id='subtotal1Values${ctr}'>
					<s:if test="form.instrOffrConfigUnlimited == true">&infin;</s:if>
					<s:else><s:property value="form.subtotalValues[#ctr]"/></s:else>
				</div></td>
				<s:if test="form.displayDisplayInstructors == true">
				<TD align="center" nowrap>&nbsp; &nbsp; <loc:message name="propertyDisplayInstructors"/>
					<s:if test="form.readOnlySubparts[#ctr] == false">
						<s:checkbox name='form.displayAllClassesInstructorsForSubpart[%{#ctr}]' onclick="updateSubpartFlags(this.checked, 'displayInstructors', %{#ctr}, 'displayAllClassesInstructorsForSubpart');"/>
					</s:if><s:else>
						<s:if test="form.displayAllClassesInstructorsForSubpart[#ctr] == true">
							<IMG border='0' title='${MSG.titleDisplayAllInstrForSubpartInSchedBook()}' alt='true' align='middle' src='images/accept.png'>
						</s:if>
						<s:hidden value="form.displayAllClassesInstructorsForSubpart[%{#ctr}]"/>
					</s:else></TD></s:if>
				<s:if test="form.displayEnabledForStudentScheduling == true">
					<TD align="center" nowrap>&nbsp; &nbsp; <loc:message name="propertyEnabledForStudentScheduling"/>
						<s:if test="form.readOnlySubparts[#ctr] == false">
							<s:checkbox name="form.enableAllClassesForStudentSchedulingForSubpart[%{#ctr}]"
								onclick="updateSubpartFlags(this.checked, 'enabledForStudentScheduling', %{#ctr}, 'enableAllClassesForStudentSchedulingForSubpart');"/>
						</s:if><s:else>
							<s:if test="form.enableAllClassesForStudentSchedulingForSubpart[#ctr] == true">
								<IMG border='0' title='${MSG.titleEnableAllClassesOfSubpartForStudentScheduling()}' alt='true' align='middle' src='images/accept.png'>
							</s:if>
							<s:hidden name="form.enableAllClassesForStudentSchedulingForSubpart[%{#ctr}]"/>
					</s:else></TD>
				</s:if>
</tr></s:iterator></table></TD></TR>
<s:if test="form.editSnapshotLimits == true && form.instrOffrConfigUnlimited == false">
	<TR><TD valign="top" style="white-space: nowrap; width: 165px;"><loc:message name="propertySchedulingSubpartSnapshotLimits"/></TD>
		<TD><table class="unitime-Table">
			<s:iterator value="form.subtotalValues" var="v" status="stat"><s:set var="ctr" value="%{#stat.index}"/>
				<tr onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='default';" onmouseout="this.style.backgroundColor='transparent';">
					<td valign="top" align="right" nowrap>
						<s:hidden name="form.subtotalSnapValues[%{#ctr}]"/>
							<b><s:property value="form.subtotalLabels[#ctr].trim()"/>:</b> &nbsp;</td>
						<td align="right" nowrap><div id='subtotal1SnapValues${ctr}'>
							<s:property value="form.subtotalSnapValues[#ctr]"/>
						</div></td>
</tr></s:iterator></table></TD></TR></s:if>
<TR><TD colspan="2" align="left"><TABLE class="unitime-Table">
	<TR>
		<s:if test="form.instrOffrConfigUnlimited == false">
			<s:if test="form.displayOptionForMaxLimit == true">
				<TD align="left" valign="bottom" rowSpan="2" colspan="2" class='WebTableHeader'>
					<s:checkbox name="form.displayMaxLimit" onclick="doClick('multipleLimits', 0);"/>
					<small><loc:message name="columnAllowVariableLimits"/></small>
				</TD>
			</s:if><s:else>
				<TD align="center" valign="bottom" rowSpan="2" colspan="2" class='WebTableHeader'>&nbsp;</TD>
			</s:else>
		</s:if><s:if test="form.instrOffrConfigUnlimited">
			<TD align="center" valign="bottom" rowSpan="2" colspan="2" class='WebTableHeader'>&nbsp;</TD>
		</s:if><s:if test="form.displayExternalId == true">
			<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
			<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'><loc:message name="columnExternalId"/></TD>
		</s:if><s:if test="form.editExternalId == true">
			<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
			<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'><loc:message name="columnExternalId"/></TD>
		</s:if>
		<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
		<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
		<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
		<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
		<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
		<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
		<s:if test="form.displayEnrollment == true">
			<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnEnroll"/></TD>
			<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
		</s:if>
		<s:if test="form.instrOffrConfigUnlimited == false">
			<s:if test="form.displayMaxLimit == true">
				<TD align="center" valign="bottom" colSpan="2" class='WebTableHeaderFirstRow'><loc:message name="columnLimit"/></TD>
			</s:if><s:if test="form.displayMaxLimit == false">
				<TD align="center" valign="bottom" colSpan="2" rowspan="2" class='WebTableHeader'><loc:message name="columnLimit"/></TD>
			</s:if>
			<s:if test="form.displaySnapshotLimit == true">
				<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnSnapshotLimitBr"/></TD>
			</s:if>
			<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnRoomRatioBr"/></TD>
			<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnNbrRms"/></TD>
		</s:if>
		<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnManagingDepartment"/></TD>
		<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnDatePattern"/></TD>
		<s:if test="form.displayLms == true">
			<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnLms"/></TD>
		</s:if>
		<s:if test="form.displayDisplayInstructors == true">
			<TD align="center" valign="bottom" rowSpan="1" class='WebTableHeaderFirstRow'><loc:message name="columnDisplayInstr"/></TD>
		</s:if>
		<s:if test="form.displayEnabledForStudentScheduling == true">
			<TD align="center" valign="bottom" rowSpan="1" class='WebTableHeaderFirstRow'><loc:message name="columnStudentScheduling"/></TD>
		</s:if>
		<TD align="center" valign="bottom" rowSpan="1" colspan="2" class='WebTableHeaderFirstRow'>---&nbsp;<loc:message name="columnTimetable"/>&nbsp;---</TD>
		<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
		<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnInstructors"/></TD>
		<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
	</TR>
	<TR>
		<s:if test="form.instrOffrConfigUnlimited == false">
			<s:if test="form.displayMaxLimit == true">
				<TD align="center" valign="bottom" class='WebTableHeaderSecondRow'><loc:message name="columnMin"/></TD>
				<TD align="center" valign="bottom" class='WebTableHeaderSecondRow'><loc:message name="columnMax"/></TD>
			</s:if>
		</s:if>
		<s:if test="form.displayDisplayInstructors == true">
			<td align="center" valign="bottom" class='WebTableHeaderSecondRow'>
				(<loc:message name="propertyAll"/> <s:checkbox name="form.displayAllClassesInstructors" onclick="resetAllDisplayFlags(this.checked, 'displayInstructors');"/>)
			</td>
		</s:if>
		<s:if test="form.displayEnabledForStudentScheduling">
			<td align="center" valign="bottom" class='WebTableHeaderSecondRow'>
				(<loc:message name="propertyAll"/> <s:checkbox name="form.enableAllClassesForStudentScheduling" onclick="resetAllDisplayFlags(this.checked, 'enabledForStudentScheduling');"/>)
			</td>
		</s:if>
		<TD align="center" valign="bottom" rowSpan="1" class='WebTableHeaderSecondRow'><loc:message name="columnAssignedTime"/></TD>
		<TD align="center" valign="bottom" rowSpan="1" class='WebTableHeaderSecondRow'><loc:message name="columnAssignedRoom"/></TD>
	</TR>
	<s:iterator value="form.classIds" var="c" status="stat"><s:set var="ctr" value="%{#stat.index}"/>
		<s:if test="form.isCancelled[#ctr] == true">
			<TR onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='default';" onmouseout="this.style.backgroundColor='transparent';" style="color: gray;">
		</s:if><s:else>
			<TR onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='default';" onmouseout="this.style.backgroundColor='transparent';">
		</s:else>
		<TD nowrap valign="top"><s:if test="form.classHasErrors[#ctr] == true"><IMG src="images/cancel.png"></s:if><s:else>&nbsp;</s:else></TD>
		<TD nowrap valign="top">
			<s:if test="form.editExternalId == false"><s:hidden name="form.externalIds[%{#ctr}]"/></s:if>
			<s:hidden name="form.classIds[%{#ctr}]"/>
			<s:hidden name="form.subpartIds[%{#ctr}]"/>
			<s:hidden name="form.itypes[%{#ctr}]"/>
			<s:hidden name="form.mustHaveChildClasses[%{#ctr}]"/>
			<s:hidden name="form.parentClassIds[%{#ctr}]"/>
			<s:hidden name="form.readOnlyClasses[%{#ctr}]"/>
			<s:hidden name="form.readOnlyDatePatterns[%{#ctr}]"/>
			<s:hidden name="form.enrollments[%{#ctr}]"/>
			<s:hidden name="form.classCanMoveUp[%{#ctr}]"/>
			<s:hidden name="form.classCanMoveDown[%{#ctr}]"/>
			<s:hidden name="form.subtotalIndexes[%{#ctr}]"/>
			<s:hidden name="form.classHasErrors[%{#ctr}]"/>
			<s:hidden name="form.classLabels[%{#ctr}]"/>
			<s:hidden name="form.classLabelIndents[%{#ctr}]"/>
			<s:hidden name="form.canDelete[%{#ctr}]"/>
			<s:hidden name="form.canCancel[%{#ctr}]"/>
			<s:hidden name="form.isCancelled[%{#ctr}]"/>
			<s:property value="form.classLabelIndents[#ctr]" escapeHtml="false"/>
			<s:property value="form.classLabels[#ctr]"/> &nbsp;</TD>
		<s:if test="form.displayExternalId == true">
			<TD>&nbsp;</TD>
			<TD align="left" valign="top" nowrap><s:property value="form.externalIds[#ctr]"/></TD>
		</s:if>
		<s:if test="form.editExternalId == true">
			<TD>&nbsp;</TD><TD align="left" valign="top" nowrap>
				<s:if test="form.readOnlyClasses[#ctr] == 'false'">
					<s:textfield name="form.externalIds[%{#ctr}]" maxlength="40" size="20" tabindex="%{(1000 + #ctr)}"/>
				</s:if><s:else>
					<s:property value="form.externalIds[#ctr]"/><s:hidden name="form.externalIds[%{#ctr}]"/>
				</s:else></TD>
		</s:if>
		<TD>&nbsp;</TD>
		<TD align="center" valign="top" nowrap>
			<s:if test="form.readOnlyClasses[#ctr] == 'false' && form.classCanMoveUp[#ctr] == true">
				<IMG border="0" src="images/arrow_up.png" align='middle' title="${MSG.titleMoveClassUp()}"
					onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
					onclick="doClick('moveUp', '${c}');">
		</s:if></TD>
		<TD align="center" valign="top" nowrap>
			<s:if test="form.readOnlyClasses[#ctr] == 'false' && form.classCanMoveDown[#ctr] == true">
				<IMG border="0" src="images/arrow_down.png" align='middle' title="${MSG.titleMoveClassDown()}"
					onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
					onclick="doClick('moveDown', '${c}');">
		</s:if></TD>
		<TD align="center" valign="top" nowrap>
			<s:if test="form.readOnlyClasses[#ctr] == 'false' && form.canDelete[#ctr] == true">
				<IMG border="0" src="images/action_delete.png" align='middle' title="${MSG.titleRemoveClassFromIO()}"
					onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
					onclick="doClick('delete', '${c}');">
		</s:if></TD>
		<TD align="center" valign="top" nowrap>
			<s:if test="form.isCancelled[#ctr] == false">
				<IMG border="0" src="images/action_add.png" align='middle' title="${MSG.titleAddClassToIO()}"
					onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
					onclick="doClick('add', '${c}');">
		</s:if></TD>
		<TD align="center" valign="top" nowrap>
			<s:if test="form.canCancel[#ctr] == true">
				<s:if test="form.isCancelled[#ctr] == true">
					<IMG border="0" src="images/reopen.png" align='middle' title="${MSG.titleReopenClass()}"
						onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
						onclick="doClick('reopen', '${c}');">
			</s:if><s:else>
					<IMG border="0" src="images/cancel.png" align='middle' title="${MSG.titleCancelClass()}"
						onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
						onclick="doClick('cancel', '${c}');">
		</s:else></s:if></TD>
		<s:if test="form.displayEnrollment == true">
			<TD align="right" valign="top" nowrap><s:property value="form.enrollments[#ctr]"/></TD><TD>&nbsp;</TD>
		</s:if>
		<s:if test="form.isCancelled[#ctr] == true">
			<s:hidden name="form.minClassLimits[%{#ctr}]"/>
			<s:hidden name="form.maxClassLimits[%{#ctr}]"/>
			<s:hidden name="form.roomRatios[%{#ctr}]"/>
			<s:hidden name="form.numberOfRooms[%{#ctr}]"/>
			<s:hidden name="form.departments[%{#ctr}]"/>
			<s:hidden name="form.datePatterns[%{#ctr}]"/>
			<s:hidden name="form.displayInstructors[%{#ctr}]"/>
			<s:hidden name="form.enabledForStudentScheduling[%{#ctr}]"/>
			<s:hidden name="form.snapshotLimits[%{#ctr}]"/>
			<s:hidden name="form.lms[%{#ctr}]"/>
			<TD colspan="${(6 + (form.displaySnapshotLimit ? 1 : 0) + (form.displayLms ? 1 : 0) + (form.displayDisplayInstructors ? 1 : 0) + (form.displayEnabledForStudentScheduling ? 1 : 0))}" style="font-style: italic;">
				<loc:message name="classNoteCancelled"><s:property value="form.classLabels[#ctr]"/></loc:message></TD>
		</s:if><s:else>
			<s:if test="form.instrOffrConfigUnlimited == true">
				<s:hidden name="form.minClassLimits[%{#ctr}]"/>
				<s:hidden name="form.maxClassLimits[%{#ctr}]"/>
				<s:hidden name="form.roomRatios[%{#ctr}]"/>
				<s:hidden name="form.numberOfRooms[%{#ctr}]"/>
				<s:hidden name="form.snapshotLimits[%{#ctr}]"/>
			</s:if>
			<s:if test="form.instrOffrConfigUnlimited == false">
				<TD align="left" nowrap valign="top">
					<s:if test="form.readOnlyClasses[#ctr] == 'false'">
						<s:hidden name="form.origMinLimit[%{#ctr}]" value="%{form.minClassLimits[#ctr]}"/>
						<s:textfield name="form.minClassLimits[%{#ctr}]"
							tabindex="%{(2000 + #ctr)}" maxlength="5" size="4"
							onchange="updateSubpartTotal(%{#ctr});document.getElementsByName('form.maxClassLimits[%{#ctr}]')[0].value=this.value;"/>
					</s:if>
					<s:if test="form.readOnlyClasses[#ctr] == 'true'">
						<s:hidden name="form.minClassLimits[%{#ctr}]"/>
						<s:property value="form.minClassLimits[#ctr]"/>
					</s:if>
				</TD>
				<s:if test="form.displayMaxLimit == true">
					<TD align="left" nowrap valign="top">
						<s:if test="form.readOnlyClasses[#ctr] == 'false'">
							<s:textfield name='form.maxClassLimits[%{#ctr}]' tabindex="%{(4000 + #ctr)}" maxlength="5" size="4"/>
						</s:if>
						<s:if test="form.readOnlyClasses[#ctr] == 'true'">
							<s:hidden name="form.maxClassLimits[%{#ctr}]"/>
							<s:property value="%{form.maxClassLimits[#ctr]}"/>
					</s:if></TD>
				</s:if>
				<s:if test="form.displayMaxLimit == false">
					<TD align="left" valign="top" nowrap><s:hidden name="form.maxClassLimits[%{#ctr}]"/></TD>
				</s:if>
				<s:if test="form.displaySnapshotLimit == true">
					<s:if test="form.editSnapshotLimits == false">
						<s:hidden name="form.snapshotLimits[%{#ctr}]"/>
						<TD align="right" valign="top" nowrap><s:property value="%{form.snapshotLimits[#ctr]}"/>&nbsp;&nbsp;&nbsp;</TD>
					</s:if>
					<s:if test="form.editSnapshotLimits == true">
						<TD align="center" valign="top" nowrap>
							<s:if test="form.readOnlyClasses[#ctr] == 'false'">
								<s:hidden name="form.origSnapLimit[%{#ctr}]" value="%{form.snapshotLimits[#ctr]}"/>
								<s:textfield name="form.snapshotLimits[%{#ctr}]" tabindex="%{(5000 + #ctr)}" maxlength="5" size="4" onchange="updateSnapshotTotal(%{#ctr});"/>
							</s:if>
							<s:if test="form.readOnlyClasses[#ctr] == 'true'">
								<s:hidden name="form.snapshotLimits[%{#ctr}]"/>
								<s:property value="%{form.snapshotLimits[#ctr]}"/>
				</s:if></TD></s:if></s:if>
				<s:if test="form.displaySnapshotLimit == false">
					<s:hidden name="form.snapshotLimits[%{#ctr}]"/>
				</s:if>
				<TD align="left" valign="top" nowrap>
					<s:if test="form.readOnlyClasses[#ctr] == 'false'">
						<s:textfield name="form.roomRatios[%{#ctr}]" tabindex="%{(6000 + #ctr)}" maxlength="6" size="3"/>
					</s:if>
					<s:if test="form.readOnlyClasses[#ctr] == 'true'">
						<s:property value="%{form.roomRatios[#ctr]}"/>
						<s:hidden name="form.roomRatios[%{#ctr}]"/>
				</s:if></TD>
				<TD align="left" valign="top" nowrap>
					<s:if test="form.readOnlyClasses[#ctr] == 'false'">
						<s:textfield name="form.numberOfRooms[%{#ctr}]" tabindex="%{(8000 + #ctr)}" maxlength="5" size="3"/>
					</s:if>
					<s:if test="form.readOnlyClasses[#ctr] == 'true'">
						<s:property value="%{form.numberOfRooms[#ctr]}"/>
						<s:hidden name="form.numberOfRooms[%{#ctr}]"/>
					</s:if>
				</TD>
			</s:if>
			<TD align="left" valign="top" nowrap>
				<s:if test="form.readOnlyClasses[#ctr] == 'false'">
					<s:select name="form.departments[%{#ctr}]" style="width:200px;" tabindex="%{(10000 + #ctr)}"
						list="#request.externalDepartmentslist" listKey="uniqueId" listValue="managingDeptLabel"
						headerKey="-1" headerValue="%{#msg.dropDeptDepartment()}"/>
				</s:if>
				<s:if test="form.readOnlyClasses[#ctr] == 'true'">
					<s:iterator value="#request.externalDepartments" var="dept">
						<s:if test="#dept.uniqueId == form.departments[#ctr]"><s:property value="#dept.managingDeptLabel"/></s:if>
					</s:iterator>
					<s:hidden name="form.departments[%{#ctr}]"/>
			</s:if></TD>
			<TD align="left" valign="top" nowrap>
				<s:if test="form.readOnlyDatePatterns[#ctr] == 'false'">
					<s:select name="form.datePatterns[%{#ctr}]" style="width:100px;" tabindex="%{(12000 + #ctr)}"
						list="#request.datePatternList" listKey="id" listValue="value"/>
				</s:if>
				<s:if test="form.readOnlyDatePatterns[#ctr] == 'true'">
					<s:if test="form.datePatterns[#ctr] == ''"><loc:message name="dropDefaultDatePattern"/></s:if>
					<s:else><s:iterator value="#request.datePatternList" var="dp">
						<s:if test="form.datePatterns[#ctr] == #dp.id"><s:property value="#dp.value"/></s:if>
					</s:iterator></s:else>
					<s:hidden name="form.datePatterns[%{#ctr}]"/>
			</s:if></TD>
			<s:if test="form.displayLms == true">
				<TD align="left" valign="top" nowrap>
					<s:if test="form.readOnlyClasses[#ctr] == 'false'">
						<s:select name="form.lms[%{#ctr}]" style="width:100px;" tabindex="%{(14000 + #ctr)}"
							list="#request.lmsList" listKey="id" listValue="value"/>
					</s:if>
					<s:if test="form.readOnlyClasses[#ctr] == 'true'">
						<s:if test="form.lms[#ctr] == ''"><loc:message name="dropDefaultLearningManagementSystem"/></s:if>
						<s:else><s:iterator value="#request.lmsList" var="lmsInfo">
							<s:if test="form.lms[#ctr] == #lmsInfo.id"><s:property value="#lmsInfo.value"/></s:if>
						</s:iterator></s:else>
						<s:hidden name="form.lms[%{#ctr}]"/>
				</s:if></TD>
			</s:if>
			<s:if test="form.displayLms == false"><s:hidden name="form.lms[%{#ctr}]"/></s:if>
			<s:if test="form.displayDisplayInstructors == true">
				<TD align="center" valign="top" nowrap>
					<s:if test="form.readOnlyClasses[#ctr] == 'false'">
						<s:checkbox name="form.displayInstructors[%{#ctr}]" tabindex="%{(16000 + #ctr)}"/>
					</s:if>
					<s:if test="form.readOnlyClasses[#ctr] == 'true'">
						<s:if test="form.displayInstructors[#ctr] == true">
							<IMG border='0' title='${MSG.titleDisplayAllInstrForSubpartInSchedBook()}' alt='true' align='middle' src='images/accept.png'>
						</s:if>
						<s:hidden name="form.displayInstructors[%{#ctr}]"/>
				</s:if>
			</TD></s:if>
			<s:if test="form.displayDisplayInstructors == false">
				<s:hidden name="form.displayInstructors[%{#ctr}]"/>
			</s:if>
			<s:if test="form.displayEnabledForStudentScheduling == true">
				<TD align="center" valign="top" nowrap>
					<s:if test="form.readOnlyClasses[#ctr] == 'false'">
						<s:checkbox name="form.enabledForStudentScheduling[%{#ctr}]" tabindex="%{(18000 + #ctr)}"/>
					</s:if>
					<s:if test="form.readOnlyClasses[#ctr] == 'true'">
						<s:if test="form.enabledForStudentScheduling[#ctr] == 'true'">
							<IMG border='0' title='${MSG.titleEnableTheseClassesForStudentScheduling()}' alt='true' align='middle' src='images/accept.png'>
						</s:if>
						<s:hidden name="form.enabledForStudentScheduling[%{#ctr}]"/>
					</s:if>
				</TD>
			</s:if>
			<s:if test="form.displayEnabledForStudentScheduling == false">
				<s:hidden name="form.enabledForStudentScheduling[%{#ctr}]"/>
			</s:if>
			</s:else>
			<TD align="left" valign="top" nowrap><s:property value="%{form.times[#ctr]}" escapeHtml="false"/>&nbsp;&nbsp; <s:hidden name="form.times[%{#ctr}]"/></TD>
			<TD align="left" valign="top" nowrap><s:property value="%{form.rooms[#ctr]}" escapeHtml="false"/> <s:hidden name="form.rooms[%{#ctr}]"/></TD>
			<TD>&nbsp;</TD>
			<TD align="left" valign="top" nowrap><s:property value="%{form.instructors[#ctr]}" escapeHtml="false"/><s:hidden name="form.instructors[%{#ctr}]"/></TD>
			<TD>&nbsp;</TD>
		</TR></s:iterator>
	</TABLE></TD></TR>
	<s:if test="form.displayDisplayInstructors == true || form.displayEnabledForStudentScheduling == true">
	<TR><TD valign="top"><loc:message name="propertySchedulingSubpartLimits"/></TD>
	<TD>
		<table class="unitime-Table">
			<s:iterator value="form.subtotalValues" var="v" status="stat"><s:set var="ctr" value="%{#stat.index}"/>
			<tr onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='default';" onmouseout="this.style.backgroundColor='transparent';">
				<td valign="top" align="right" nowrap><b><s:property value="%{form.subtotalLabels[#ctr].trim()}"/>:</b>&nbsp;</td>
				<td nowrap align="right"><div id='subtotal2Values${ctr}'>
					<s:if test="form.instrOffrConfigUnlimited == true">&infin;</s:if>
					<s:if test="form.instrOffrConfigUnlimited == false">
						<s:property value="%{form.subtotalValues[#ctr]}"/>
					</s:if>
				</div></td>
				<s:if test="form.displayDisplayInstructors == true">
					<TD align="center" nowrap>&nbsp; &nbsp; <loc:message name="propertyDisplayInstructors"/>
					<s:if test="form.readOnlySubparts[#ctr] == false">
						<s:checkbox name="form.displayAllClassesInstructorsForSubpart[%{#ctr}]" onclick="updateSubpartFlags(this.checked, 'displayInstructors', %{#ctr}, 'displayAllClassesInstructorsForSubpart');"/>
					</s:if>
					<s:if test="form.readOnlySubparts[#ctr] == true">
						<s:if test="form.displayAllClassesInstructorsForSubpart[#ctr] == true">
							<IMG border='0' title='${MSG.titleDisplayAllInstrForSubpartInSchedBook()}' alt='true' align="middle" src='images/accept.png'>
						</s:if>
						<s:hidden name="form.displayAllClassesInstructorsForSubpart[%{#ctr}]"/>
					</s:if></TD>
				</s:if>
				<s:if test="form.displayEnabledForStudentScheduling == true">
					<TD align="center" nowrap>&nbsp; &nbsp; <loc:message name="propertyEnabledForStudentScheduling"/>
						<s:if test="form.readOnlySubparts[#ctr] == false">
							<s:checkbox name="form.enableAllClassesForStudentSchedulingForSubpart[%{#ctr}]"
								onclick="updateSubpartFlags(this.checked, 'enabledForStudentScheduling', %{#ctr}, 'enableAllClassesForStudentSchedulingForSubpart');"/>
						</s:if><s:if test="form.readOnlySubparts[#ctr] == true">
							<s:if test="form.enableAllClassesForStudentSchedulingForSubpart[#ctr] == true">
								<IMG border='0' title='${MSG.titleEnableAllClassesOfSubpartForStudentScheduling()}' alt='true' align='middle' src='images/accept.png'>
							</s:if>
							<s:hidden name="form.enableAllClassesForStudentSchedulingForSubpart[%{#ctr}]"/>
						</s:if>
			</TD></s:if></tr>
	</s:iterator></table></td></tr>
	<s:if test="form.editSnapshotLimits == true">
		<s:if test="form.instrOffrConfigUnlimited == false">
			<TR><TD valign="top" style="white-space: nowrap; width: 165px;"><loc:message name="propertySchedulingSubpartSnapshotLimits"/></TD>
				<TD><table class="unitime-Table">
					<s:iterator value="form.subtotalValues" var="v" status="stat"><s:set var="ctr" value="%{#stat.index}"/>
						<tr onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='default';"
							onmouseout="this.style.backgroundColor='transparent';">
								<td valign="top" align="right" nowrap><b><s:property value="%{form.subtotalLabels[#ctr].trim()}"/>:</b>&nbsp;</td>
								<td align="right" nowrap><div id='subtotal2SnapValues${ctr}'>
									<s:property value="%{form.subtotalSnapValues[#ctr]}"/>
								</div></td>
					</tr></s:iterator>
			</table></TD></TR>
	</s:if></s:if>
	</s:if>
<!-- Buttons -->
<TR><TD colspan="2" valign="middle"><DIV class="WelcomeRowHeadBlank">&nbsp;</DIV></TD></TR>
<TR><TD colspan="2" align="right">
	<s:submit name='op' value='%{#msg.actionUpdateMultipleClassSetup()}'
		accesskey='%{#msg.accessUpdateMultipleClassSetup()}' title='%{#msg.titleUpdateMultipleClassSetup(#msg.accessUpdateMultipleClassSetup())}'/>
	<s:submit name='op' value='%{#msg.actionBackToIODetail()}'
		accesskey='%{#msg.accessBackToIODetail()}' title='%{#msg.titleBackToIODetail(#msg.accessBackToIODetail())}'/>
</TD></TR></TABLE>
</s:form>
</loc:bundle>