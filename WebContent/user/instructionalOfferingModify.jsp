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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp"%>
<%@ page import="org.unitime.timetable.util.IdValue"%>
<%@ page import="org.unitime.timetable.model.Department"%>
<%@ page import="org.unitime.timetable.model.DatePattern"%>
<%@ page import="org.unitime.timetable.model.LearningManagementSystemInfo"%>
<%@ page import="org.unitime.timetable.form.InstructionalOfferingModifyForm"%>
<%@ page import="org.unitime.timetable.defaults.SessionAttribute"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt"%>
<%@ taglib uri="http://www.unitime.org/tags-localization" prefix="loc"%>

<loc:bundle name="CourseMessages">
<SCRIPT language="javascript">
<!--
function doClick(op, id) {
	document.forms[0].elements["hdnOp"].value=op;
	document.forms[0].elements["id"].value=id;
	document.forms[0].elements["click"].value="y";
	document.forms[0].submit();
}
function updateSubpartTotal(subpartIndex) {
	displayInstructors=document.getElementsByName('displayDisplayInstructors')[0].value;
	displayEnabledForStudentScheduling=document.getElementsByName('displayEnabledForStudentScheduling')[0].value;
	subtotalName='subtotalIndexes['+subpartIndex+']';
	origLimitName='origMinLimit['+subpartIndex+']';
	minLimitName='minClassLimits['+subpartIndex+']';
	totalIndex=document.getElementsByName(subtotalName)[0].value;
	subtotalValueName='subtotalValues['+totalIndex+']';
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
	displayInstructors=document.getElementsByName('displayDisplayInstructors')[0].value;
	displayEnabledForStudentScheduling=document.getElementsByName('displayEnabledForStudentScheduling')[0].value;
	subtotalName='subtotalIndexes['+subpartIndex+']';
	origLimitName='origSnapLimit['+subpartIndex+']';
	minLimitName='snapshotLimits['+subpartIndex+']';
	totalIndex=document.getElementsByName(subtotalName)[0].value;
	subtotalValueName='subtotalSnapValues['+totalIndex+']';
	subtotalValueName1='subtotal1SnapValues'+totalIndex;
	if (displayInstructors != 'false' || displayEnabledForStudentScheduling != 'false') {
		subtotalValueName2='subtotal2SnapValues' + totalIndex;
	}
	origTotal=document.getElementsByName(subtotalValueName)[0].value;
	origSubpartLimit=document.getElementsByName(origLimitName)[0].value;
	newSubpartLimit=document.getElementsByName(minLimitName)[0].value;
	if(newSubpartLimit.length == 0 || (newSubpartLimit.search("[^0-9]")) >= 0) { newSubpartLimit=0;}
	newTotal=origTotal-origSubpartLimit+(newSubpartLimit-0);
	document.getElementsByName(subtotalValueName)[0].value=newTotal;
	document.getElementById(subtotalValueName1).innerHTML=newTotal;
	if (displayInstructors != 'false' || displayEnabledForStudentScheduling != 'false') {
		document.getElementById(subtotalValueName2).innerHTML=newTotal;
	}
	document.getElementsByName(origLimitName)[0].value=newSubpartLimit;
}
</SCRIPT>

<tiles:importAttribute />
<tt:session-context />
<% 
String fN="instructionalOfferingModifyForm";
InstructionalOfferingModifyForm frm=(InstructionalOfferingModifyForm)request.getAttribute(fN);
String crsNbr=(String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
%>
<SCRIPT language="javascript">
<!--
function updateSubpartFlags(value, baseName, subpartIndex, flagName) {
	for (var i=0;i<<%=frm.getClassIds().size()%>;i++) {
		var chbox=document.getElementsByName(baseName+'['+i+']');
		var subtotalIndexName='subtotalIndexes['+i+']';
		var subpartIndexValue=document.getElementsByName(subtotalIndexName)[0].value;
		if ((subpartIndexValue * 1) == (subpartIndex * 1) && chbox!=null && chbox.length>0){
			chbox[0].checked=value;
		}
	}
	var subpartFlag=document.getElementsByName(flagName+'['+subpartIndex+']');
	subpartFlag[0].checked=value;
	subpartFlag[1].checked=value;
}
function resetAllDisplayFlags(value, baseName) {
	for (var i=0;i<<%=frm.getClassIds().size()%>;i++) {
	var chbox=document.getElementsByName(baseName+'['+i+']');
	if (chbox!=null && chbox.length>0)
	chbox[0].checked=value;
	}
}
// -->
</SCRIPT>
<script language="javascript">displayLoading();</script>

<html:form action="/instructionalOfferingModify">
<html:hidden property="instrOfferingId"/>
<html:hidden property="instrOfferingName"/>
<html:hidden property="instrOffrConfigId"/>
<html:hidden property="origSubparts"/>
<html:hidden property="displayMaxLimit"/>
<html:hidden property="displayOptionForMaxLimit"/>
<html:hidden property="displayEnrollment"/>
<html:hidden property="displaySnapshotLimit"/>
<html:hidden property="displayExternalId"/>
<html:hidden property="editExternalId"/>
<html:hidden property="editSnapshotLimits"/>
<html:hidden property="displayDisplayInstructors"/>
<html:hidden property="displayEnabledForStudentScheduling"/>
<html:hidden property="displayLms"/>
<INPUT type="hidden" name="hdnOp" value="">
<INPUT type="hidden" name="id" value="">
<INPUT type="hidden" name="click" value="">
<INPUT type="hidden" name="deletedClassId" value="">
<INPUT type="hidden" name="addTemplateClassId" value="">
<INPUT type="hidden" name="moveUpClassId" value="">
<INPUT type="hidden" name="moveDownClassId" value="">
<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
<!-- Buttons -->
<TR><TD valign="middle" colspan="2"><tt:section-header>
	<tt:section-title><bean:write name="<%=fN%>" property="instrOfferingName"/></tt:section-title>
	<html:submit property="op" disabled="true" styleClass="btn" accesskey="<%=MSG.accessUpdateMultipleClassSetup()%>"
		title="<%=MSG.titleUpdateMultipleClassSetup(MSG.accessUpdateMultipleClassSetup())%>">
		<loc:message name="actionUpdateMultipleClassSetup"/>
	</html:submit>
	<bean:define id="instrOfferingId"><bean:write name="<%=fN%>" property="instrOfferingId"/></bean:define>
	<html:button property="op" styleClass="btn" accesskey="<%=MSG.accessBackToIODetail()%>"
		title="<%=MSG.titleBackToIODetail(MSG.accessBackToIODetail())%>"
		onclick="document.location.href='instructionalOfferingDetail.do?op=view&io=${instrOfferingId}';">
		<loc:message name="actionBackToIODetail"/> 
	</html:button>
</tt:section-header></TD></TR>
<logic:messagesPresent><TR>
	<TD colspan="2" align="left" class="errorCell"><B><U><loc:message name="errorsMultipleClassSetup"/></U></B><BR>
	<BLOCKQUOTE><UL><html:messages id="error"><LI>${error}</LI></html:messages></UL></BLOCKQUOTE></TD>
</TR></logic:messagesPresent>
<html:hidden property="instructionalMethodEditable"/>
<logic:notEmpty name="instructionalOfferingModifyForm" property="instructionalMethods">
	<TR><TD><loc:message name="propertyInstructionalMethod"/></TD>
	<TD><logic:equal name="instructionalOfferingModifyForm" property="instructionalMethodEditable" value="true">
		<html:select property="instructionalMethod">
			<logic:empty name="instructionalOfferingModifyForm" property="instructionalMethodDefault"><html:option value="-1"><loc:message name="selectNoInstructionalMethod"/></html:option></logic:empty>
			<logic:notEmpty name="instructionalOfferingModifyForm" property="instructionalMethodDefault"><html:option value="-1"><loc:message name="defaultInstructionalMethod"><bean:write name="instructionalOfferingModifyForm" property="instructionalMethodDefault"/></loc:message></html:option></logic:notEmpty>
			<html:optionsCollection property="instructionalMethods" value="id" label="value"/>
		</html:select></logic:equal>
		<logic:notEqual name="instructionalOfferingModifyForm" property="instructionalMethodEditable" value="true"><html:hidden property="instructionalMethod"/><bean:write name="instructionalOfferingModifyForm" property="instructionalMethodLabel"/></logic:notEqual>
	</TD></TR>
</logic:notEmpty>
<html:hidden property="instrOffrConfigUnlimitedReadOnly"/>
<logic:equal name="<%=fN%>" property="instrOffrConfigUnlimitedReadOnly" value="true">
	<logic:equal name="<%=fN%>" property="instrOffrConfigUnlimited" value="true">
		<TR><TD align="left"><loc:message name="propertyUnlimitedEnrollment"/></TD>
		<TD><IMG border='0' title='<%=MSG.titleUnlimitedEnrollment()%>' alt='true' align='middle' src='images/accept.png'></TD></TR>
	</logic:equal>
<html:hidden property="instrOffrConfigUnlimited"/></logic:equal>
<logic:notEqual name="<%=fN%>" property="instrOffrConfigUnlimitedReadOnly" value="true">
	<TR><TD><loc:message name="propertyUnlimitedEnrollment"/></TD>
	<TD><html:checkbox property="instrOffrConfigUnlimited" onclick="document.forms[0].elements['hdnOp'].value='unlimited';document.forms[0].submit();"/></TD></TR>
</logic:notEqual>
<logic:equal name="<%=fN%>" property="instrOffrConfigUnlimited" value="true"><html:hidden property="instrOffrConfigLimit"/></logic:equal>
<logic:notEqual name="<%=fN%>" property="instrOffrConfigUnlimited" value="true">
	<TR><TD><loc:message name="propertyConfigurationLimit"/></TD>
	<TD><html:text property="instrOffrConfigLimit" maxlength="5" size="5"/></TD></TR>
</logic:notEqual>
<TR><TD valign="top" style="white-space: nowrap; width: 165px;"><loc:message name="propertySchedulingSubpartLimits"/></TD>
	<TD><table align="left" border="0" cellspacing="0" cellpadding="0">
		<logic:iterate name="<%=fN%>" property="subtotalValues" id="v" indexId="ctr">
			<tr onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='default';"
				onmouseout="this.style.backgroundColor='transparent';">
				<td valign="top" align="right" nowrap>
					<html:hidden property='<%="subtotalLabels["+ctr+"]"%>'/>
					<html:hidden property='<%="subtotalValues["+ctr+"]"%>'/>
					<b><%=((String) frm.getSubtotalLabels().get(ctr)).trim()%>:</b>&nbsp;</td>
				<td align="right" nowrap><div id='<%="subtotal1Values" + ctr%>'>
					<logic:equal name="<%=fN%>" property="instrOffrConfigUnlimited" value="true">&infin;</logic:equal>
					<logic:notEqual name="<%=fN%>" property="instrOffrConfigUnlimited" value="true">
						<bean:write name="<%=fN%>" property='<%="subtotalValues["+ctr+"]"%>'/>
					</logic:notEqual>
				</div></td>
				<logic:equal name="<%=fN%>" property="displayDisplayInstructors" value="true">
				<TD align="center" nowrap>&nbsp; &nbsp; <loc:message name="propertyDisplayInstructors"/>
					<logic:equal name="<%=fN%>" property='<%="readOnlySubparts["+ctr+"]"%>' value="false">
						<html:checkbox name="<%=fN%>" property='<%="displayAllClassesInstructorsForSubpart["+ctr+"]"%>'
							onclick="<%=\"updateSubpartFlags(this.checked, 'displayInstructors',\" + ctr +\", 'displayAllClassesInstructorsForSubpart');\"%>"/>
					</logic:equal>
					<logic:equal name="<%=fN%>" property='<%="readOnlySubparts["+ctr+"]"%>' value="true">
						<logic:equal name="<%=fN%>" property='<%="displayAllClassesInstructorsForSubpart["+ctr+"]"%>' value="true">
							<IMG border='0' title='<%=MSG.titleDisplayAllInstrForSubpartInSchedBook()%>' alt='true' align='middle' src='images/accept.png'>
						</logic:equal>
						<html:hidden property='<%="displayAllClassesInstructorsForSubpart["+ctr+"]"%>'/>
				</logic:equal></TD></logic:equal>
				<logic:equal name="<%=fN%>" property="displayEnabledForStudentScheduling" value="true">
					<TD align="center" nowrap>&nbsp; &nbsp; <loc:message name="propertyEnabledForStudentScheduling"/>
						<logic:equal name="<%=fN%>" property='<%="readOnlySubparts["+ctr+"]"%>' value="false">
							<html:checkbox name="<%=fN%>"
								property='<%="enableAllClassesForStudentSchedulingForSubpart["+ctr+"]"%>'
								onclick="<%=\"updateSubpartFlags(this.checked, 'enabledForStudentScheduling',\" + ctr +\", 'enableAllClassesForStudentSchedulingForSubpart');\"%>"/>
						</logic:equal>
						<logic:equal name="<%=fN%>" property='<%="readOnlySubparts["+ctr+"]"%>' value="true">
							<logic:equal name="<%=fN%>" property='<%="enableAllClassesForStudentSchedulingForSubpart["+ctr+"]"%>' value="true">
								<IMG border='0' title='<%=MSG.titleEnableAllClassesOfSubpartForStudentScheduling()%>' alt='true' align='middle' src='images/accept.png'>
							</logic:equal>
							<html:hidden property='<%="enableAllClassesForStudentSchedulingForSubpart["+ctr+"]"%>'/>
						</logic:equal></TD>
				</logic:equal>
</tr></logic:iterate></table></TD></TR>
<logic:equal name="<%=fN%>" property="editSnapshotLimits" value="true">
	<logic:notEqual name="<%=fN%>" property="instrOffrConfigUnlimited" value="true">
		<TR><TD valign="top" style="white-space: nowrap; width: 165px;"><loc:message name="propertySchedulingSubpartSnapshotLimits"/></TD>
			<TD><table align="left" border="0" cellspacing="0" cellpadding="0">
				<logic:iterate name="<%=fN%>" property="subtotalValues" id="v" indexId="ctr">
					<tr onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='default';"
						onmouseout="this.style.backgroundColor='transparent';">
						<td valign="top" align="right" nowrap>
							<html:hidden property='<%="subtotalSnapValues["+ctr+"]"%>'/> 
							<b><%=((String) frm.getSubtotalLabels().get(ctr)).trim()%>:</b> &nbsp;</td>
						<td align="right" nowrap><div id='<%="subtotal1SnapValues" + ctr%>'>
							<bean:write name="<%=fN%>" property='<%="subtotalSnapValues["+ctr+"]"%>'/>
						</div></td>
</tr></logic:iterate></table></TD></TR></logic:notEqual></logic:equal>
<TR><TD colspan="2" align="left">
	<TABLE align="left" border="0" cellspacing="0" cellpadding="1">
		<TR>
			<logic:notEqual name="<%=fN%>" property="instrOffrConfigUnlimited" value="true">
				<logic:equal name="<%=fN%>" property="displayOptionForMaxLimit" value="true">
					<TD align="left" valign="bottom" rowSpan="2" colspan="2" class='WebTableHeader'>
						<html:checkbox name="<%=fN%>" property="displayMaxLimit" onclick="doClick('multipleLimits', 0);"/>
						<small><loc:message name="columnAllowVariableLimits"/></small></TD>
				</logic:equal><logic:equal name="<%=fN%>" property="displayOptionForMaxLimit" value="false">
					<TD align="center" valign="bottom" rowSpan="2" colspan="2" class='WebTableHeader'>&nbsp;</TD>
			</logic:equal></logic:notEqual>
			<logic:equal name="<%=fN%>" property="instrOffrConfigUnlimited" value="true">
				<TD align="center" valign="bottom" rowSpan="2" colspan="2" class='WebTableHeader'>&nbsp;</TD>
			</logic:equal><logic:equal name="<%=fN%>" property="displayExternalId" value="true">
				<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
				<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'><loc:message name="columnExternalId"/></TD>
			</logic:equal><logic:equal name="<%=fN%>" property="editExternalId" value="true">
				<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
				<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'><loc:message name="columnExternalId"/></TD>
			</logic:equal>
			<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
			<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
			<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
			<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
			<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
			<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
			<logic:equal name="<%=fN%>" property="displayEnrollment" value="true">
				<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnEnroll"/></TD>
				<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
			</logic:equal>
			<logic:notEqual name="<%=fN%>" property="instrOffrConfigUnlimited" value="true">
				<logic:equal name="<%=fN%>" property="displayMaxLimit" value="true">
					<TD align="center" valign="bottom" colSpan="2" class='WebTableHeaderFirstRow'><loc:message name="columnLimit"/></TD>
				</logic:equal><logic:equal name="<%=fN%>" property="displayMaxLimit" value="false">
					<TD align="center" valign="bottom" colSpan="2" rowspan="2" class='WebTableHeader'><loc:message name="columnLimit"/></TD>
				</logic:equal><logic:equal name="<%=fN%>" property="displaySnapshotLimit" value="true">
					<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnSnapshotLimitBr"/></TD>
				</logic:equal>
				<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnRoomRatioBr"/></TD>
				<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnNbrRms"/></TD>
			</logic:notEqual>
			<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnManagingDepartment"/></TD>
			<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnDatePattern"/></TD>
			<logic:equal name="<%=fN%>" property="displayLms" value="true">
				<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnLms"/></TD>
			</logic:equal>
			<logic:equal name="<%=fN%>" property="displayDisplayInstructors" value="true">
				<TD align="center" valign="bottom" rowSpan="1" class='WebTableHeaderFirstRow'><loc:message name="columnDisplayInstr"/></TD>
			</logic:equal>
			<logic:equal name="<%=fN%>" property="displayEnabledForStudentScheduling" value="true">
				<TD align="center" valign="bottom" rowSpan="1" class='WebTableHeaderFirstRow'><loc:message name="columnStudentScheduling"/></TD>
			</logic:equal>
			<TD align="center" valign="bottom" rowSpan="1" colspan="2" class='WebTableHeaderFirstRow'>---&nbsp;<loc:message name="columnTimetable"/>&nbsp;---</TD>
			<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
			<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnInstructors"/></TD>
			<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
		</TR>
		<TR>
			<logic:notEqual name="<%=fN%>" property="instrOffrConfigUnlimited" value="true">
				<logic:equal name="<%=fN%>" property="displayMaxLimit" value="true">
					<TD align="center" valign="bottom" class='WebTableHeaderSecondRow'><loc:message name="columnMin"/></TD>
					<TD align="center" valign="bottom" class='WebTableHeaderSecondRow'><loc:message name="columnMax"/></TD>
				</logic:equal>
			</logic:notEqual>
			<logic:equal name="<%=fN%>" property="displayDisplayInstructors" value="true">
				<td align="center" valign="bottom" class='WebTableHeaderSecondRow'>
					(<loc:message name="propertyAll"/> <html:checkbox name="<%=fN%>" property="displayAllClassesInstructors"
						onclick="resetAllDisplayFlags(this.checked, 'displayInstructors')"/>)
				</td>
			</logic:equal>
			<logic:equal name="<%=fN%>" property="displayEnabledForStudentScheduling" value="true">
				<td align="center" valign="bottom" class='WebTableHeaderSecondRow'>
					(<loc:message name="propertyAll"/> <html:checkbox name="<%=fN%>" property="enableAllClassesForStudentScheduling"
						onclick="resetAllDisplayFlags(this.checked, 'enabledForStudentScheduling')"/>)
				</td>
			</logic:equal>
			<TD align="center" valign="bottom" rowSpan="1" class='WebTableHeaderSecondRow'><loc:message name="columnAssignedTime"/></TD>
			<TD align="center" valign="bottom" rowSpan="1" class='WebTableHeaderSecondRow'><loc:message name="columnAssignedRoom"/></TD>
		</TR>
		<logic:iterate name="<%=fN%>" property="classIds" id="c" indexId="ctr">
			<logic:equal name="<%=fN%>" property='<%="isCancelled["+ctr+"]"%>' value="true">
				<TR onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='default';"
					onmouseout="this.style.backgroundColor='transparent';" style="color: gray;">
			</logic:equal>
			<logic:notEqual name="<%=fN%>" property='<%="isCancelled["+ctr+"]"%>' value="true">
				<TR onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='default';"
					onmouseout="this.style.backgroundColor='transparent';">
			</logic:notEqual>
			<TD nowrap valign="top">
				<logic:equal name="<%=fN%>" property='<%="classHasErrors["+ctr+"]"%>' value="true"><IMG src="images/cancel.png"></logic:equal>
				<logic:equal name="<%=fN%>" property='<%="classHasErrors["+ctr+"]"%>' value="false">&nbsp;</logic:equal></TD>
			<TD nowrap valign="top">
				<logic:notEqual name="<%=fN%>" property="editExternalId" value="true">
					<html:hidden property='<%="externalIds["+ctr+"]"%>'/>
				</logic:notEqual>
				<html:hidden property='<%="classIds["+ctr+"]"%>'/>
				<html:hidden property='<%="subpartIds["+ctr+"]"%>'/>
				<html:hidden property='<%="itypes["+ctr+"]"%>'/>
				<html:hidden property='<%="mustHaveChildClasses["+ctr+"]"%>'/>
				<html:hidden property='<%="parentClassIds["+ctr+"]"%>'/>
				<html:hidden property='<%="readOnlyClasses["+ctr+"]"%>'/>
				<html:hidden property='<%="readOnlyDatePatterns["+ctr+"]"%>'/>
				<html:hidden property='<%="enrollments["+ctr+"]"%>'/>
				<html:hidden property='<%="classCanMoveUp["+ctr+"]"%>'/>
				<html:hidden property='<%="classCanMoveDown["+ctr+"]"%>'/>
				<html:hidden property='<%="subtotalIndexes["+ctr+"]"%>'/>
				<html:hidden property='<%="classHasErrors["+ctr+"]"%>'/>
				<html:hidden property='<%="classLabels["+ctr+"]"%>'/>
				<html:hidden property='<%="classLabelIndents["+ctr+"]"%>'/>
				<html:hidden property='<%="canDelete["+ctr+"]"%>'/>
				<html:hidden property='<%="canCancel["+ctr+"]"%>'/>
				<html:hidden property='<%="isCancelled["+ctr+"]"%>'/>
				<%=frm.getClassLabelIndents().get(ctr.intValue()).toString()%>
				<bean:write name="<%=fN%>" property='<%="classLabels["+ctr+"]"%>'/> &nbsp;</TD>
			<logic:equal name="<%=fN%>" property="displayExternalId" value="true">
				<TD>&nbsp;</TD>
				<TD align="left" valign="top" nowrap><%=frm.getExternalIds().get(ctr)%></TD>
			</logic:equal>
			<logic:equal name="<%=fN%>" property="editExternalId" value="true">
				<TD>&nbsp;</TD><TD align="left" valign="top" nowrap>
				<logic:equal name="<%=fN%>" property='<%="readOnlyClasses["+ctr+"]"%>' value="false">
					<html:text name="<%=fN%>" property='<%="externalIds["+ctr+"]"%>' tabindex="<%=java.lang.Integer.toString(1000 + ctr.intValue())%>" maxlength="40" size="20"/>
				</logic:equal><logic:equal name="<%=fN%>" property='<%="readOnlyClasses["+ctr+"]"%>' value="true">
					<bean:write name="<%=fN%>" property='<%="externalIds["+ctr+"]"%>'/>
					<html:hidden property='<%="externalIds["+ctr+"]"%>'/>
				</logic:equal></TD>
			</logic:equal>
			<TD>&nbsp;</TD>
			<TD align="center" valign="top" nowrap>
				<logic:equal name="<%=fN%>" property='<%="readOnlyClasses["+ctr+"]"%>' value="false">
					<logic:equal name="<%=fN%>" property='<%="classCanMoveUp["+ctr+"]"%>' value="true">
						<IMG border="0" src="images/arrow_up.png" align='absmiddle' title="<%=MSG.titleMoveClassUp()%>"
							onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
							onclick="document.forms[0].elements['hdnOp'].value='moveUp';document.forms[0].elements['moveUpClassId'].value='<%=c.toString()%>';document.forms[0].submit();">
			</logic:equal></logic:equal></TD>
			<TD align="center" valign="top" nowrap>
				<logic:equal name="<%=fN%>" property='<%="readOnlyClasses["+ctr+"]"%>' value="false">
					<logic:equal name="<%=fN%>" property='<%="classCanMoveDown["+ctr+"]"%>' value="true">
						<IMG border="0" src="images/arrow_down.png" align='absmiddle' title="<%=MSG.titleMoveClassDown()%>"
							onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
							onclick="document.forms[0].elements['hdnOp'].value='moveDown';document.forms[0].elements['moveDownClassId'].value='<%=c.toString()%>';document.forms[0].submit();">
			</logic:equal></logic:equal></TD>
			<TD align="center" valign="top" nowrap>
				<logic:equal name="<%=fN%>" property='<%="readOnlyClasses["+ctr+"]"%>' value="false">
					<logic:equal name="<%=fN%>" property='<%="canDelete["+ctr+"]"%>' value="true">
						<IMG border="0" src="images/action_delete.png" align='absmiddle' title="<%=MSG.titleRemoveClassFromIO()%>"
							onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
							onclick="document.forms[0].elements['hdnOp'].value='delete';document.forms[0].elements['deletedClassId'].value='<%=c.toString()%>';document.forms[0].submit();">
			</logic:equal></logic:equal></TD>
			<TD align="center" valign="top" nowrap>
				<logic:notEqual name="<%=fN%>" property='<%="isCancelled["+ctr+"]"%>' value="true">
					<IMG border="0" src="images/action_add.png" align='absmiddle' title="<%=MSG.titleAddClassToIO()%>"
						onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
						onclick="document.forms[0].elements['hdnOp'].value='add';document.forms[0].elements['addTemplateClassId'].value='<%=c.toString()%>';document.forms[0].submit();">
			</logic:notEqual></TD>
			<TD align="center" valign="top" nowrap>
				<logic:equal name="<%=fN%>" property='<%="canCancel["+ctr+"]"%>' value="true">
					<logic:equal name="<%=fN%>" property='<%="isCancelled["+ctr+"]"%>' value="true">
						<IMG border="0" src="images/reopen.png" align='absmiddle' title="<%=MSG.titleReopenClass()%>"
							onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
							onclick="document.forms[0].elements['hdnOp'].value='reopen';document.forms[0].elements['deletedClassId'].value='<%=c.toString()%>';document.forms[0].submit();">
					</logic:equal><logic:notEqual name="<%=fN%>" property='<%="isCancelled["+ctr+"]"%>' value="true">
						<IMG border="0" src="images/cancel.png" align='absmiddle' title="<%=MSG.titleCancelClass()%>"
							onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
							onclick="document.forms[0].elements['hdnOp'].value='cancel';document.forms[0].elements['deletedClassId'].value='<%=c.toString()%>';document.forms[0].submit();">
			</logic:notEqual></logic:equal></TD>
			<logic:equal name="<%=fN%>" property="displayEnrollment" value="true">
				<TD align="right" valign="top" nowrap><bean:write name="<%=fN%>" property='<%="enrollments["+ctr+"]"%>'/></TD><TD>&nbsp;</TD>
			</logic:equal>
			<logic:equal name="<%=fN%>" property='<%="isCancelled["+ctr+"]"%>' value="true">
				<html:hidden property='<%="minClassLimits["+ctr+"]"%>'/>
				<html:hidden property='<%="maxClassLimits["+ctr+"]"%>'/>
				<html:hidden property='<%="roomRatios["+ctr+"]"%>'/>
				<html:hidden property='<%="numberOfRooms["+ctr+"]"%>'/>
				<html:hidden property='<%="departments["+ctr+"]"%>'/>
				<html:hidden property='<%="datePatterns["+ctr+"]"%>'/>
				<html:hidden property='<%="displayInstructors["+ctr+"]"%>'/>
				<html:hidden property='<%="enabledForStudentScheduling["+ctr+"]"%>'/>
				<html:hidden property='<%="snapshotLimits["+ctr+"]"%>'/>
				<html:hidden property='<%="lms["+ctr+"]"%>'/>
				<TD colspan="<%=String.valueOf(6 + (frm.getDisplaySnapshotLimit() ? 1 : 0) + (frm.getDisplayLms() ? 1 : 0) + (frm.getDisplayDisplayInstructors() ? 1 : 0) + (frm.getDisplayEnabledForStudentScheduling() ? 1 : 0))%>" style="font-style: italic;">
					<loc:message name="classNoteCancelled"><bean:write name="<%=fN%>" property='<%="classLabels["+ctr+"]"%>'/></loc:message></TD>
			</logic:equal>
			<logic:notEqual name="<%=fN%>" property='<%="isCancelled["+ctr+"]"%>' value="true">
			<logic:equal name="<%=fN%>" property="instrOffrConfigUnlimited" value="true">
				<html:hidden property='<%="minClassLimits["+ctr+"]"%>'/>
				<html:hidden property='<%="maxClassLimits["+ctr+"]"%>'/>
				<html:hidden property='<%="roomRatios["+ctr+"]"%>'/>
				<html:hidden property='<%="numberOfRooms["+ctr+"]"%>'/>
				<html:hidden property='<%="snapshotLimits["+ctr+"]"%>'/>
			</logic:equal>
			<logic:notEqual name="<%=fN%>" property="instrOffrConfigUnlimited" value="true">
				<TD align="left" nowrap valign="top">
					<logic:equal name="<%=fN%>" property='<%="readOnlyClasses["+ctr+"]"%>' value="false">
						<html:hidden property='<%="origMinLimit["+ctr+"]"%>' value="<%=(String) frm.getMinClassLimits().get(ctr)%>"/>
						<html:text name="<%=fN%>" property='<%="minClassLimits["+ctr+"]"%>'
							tabindex="<%=java.lang.Integer.toString(2000 + ctr.intValue())%>" maxlength="5" size="4"
							onchange="<%=\"updateSubpartTotal(\" + ctr +\");document.getElementsByName('maxClassLimits[\" + ctr +\"]')[0].value=this.value\"%>"/>
					</logic:equal>
					<logic:equal name="<%=fN%>" property='<%="readOnlyClasses["+ctr+"]"%>' value="true">
						<html:hidden property='<%="minClassLimits["+ctr+"]"%>'/>
						<bean:write name="<%=fN%>" property='<%="minClassLimits["+ctr+"]"%>'/>
					</logic:equal>
				</TD>
				<logic:equal name="<%=fN%>" property="displayMaxLimit" value="true">
					<TD align="left" nowrap valign="top">
						<logic:equal name="<%=fN%>" property='<%="readOnlyClasses["+ctr+"]"%>' value="false">
							<html:text name="<%=fN%>" property='<%="maxClassLimits["+ctr+"]"%>'
								tabindex="<%=java.lang.Integer.toString(4000 + ctr.intValue())%>" maxlength="5" size="4"/>
						</logic:equal>
						<logic:equal name="<%=fN%>" property='<%="readOnlyClasses["+ctr+"]"%>' value="true">
							<html:hidden property='<%="maxClassLimits["+ctr+"]"%>'/>
							<bean:write name="<%=fN%>" property='<%="maxClassLimits["+ctr+"]"%>'/>
					</logic:equal></TD>
				</logic:equal>
				<logic:equal name="<%=fN%>" property="displayMaxLimit" value="false">
					<TD align="left" valign="top" nowrap><html:hidden property='<%="maxClassLimits["+ctr+"]"%>'/></TD>
				</logic:equal>
				<logic:equal name="<%=fN%>" property="displaySnapshotLimit" value="true">
					<logic:notEqual name="<%=fN%>" property="editSnapshotLimits" value="true">
						<html:hidden property='<%="snapshotLimits["+ctr+"]"%>'/>
						<TD align="right" valign="top" nowrap><bean:write name="<%=fN%>" property='<%="snapshotLimits["+ctr+"]"%>'/>&nbsp;&nbsp;&nbsp;</TD>
					</logic:notEqual>
					<logic:equal name="<%=fN%>" property="editSnapshotLimits" value="true">
						<TD align="center" valign="top" nowrap>
							<logic:equal name="<%=fN%>" property='<%="readOnlyClasses["+ctr+"]"%>' value="false">
								<html:hidden property='<%="origSnapLimit["+ctr+"]"%>' value="<%=(String) frm.getSnapshotLimits().get(ctr)%>"/>
								<html:text name="<%=fN%>" property='<%="snapshotLimits["+ctr+"]"%>'
									tabindex="<%=java.lang.Integer.toString(5000 + ctr.intValue())%>" maxlength="5" size="4"
									onchange="<%=\"updateSnapshotTotal(\" + ctr +\");\"%>"/>
							</logic:equal>
							<logic:equal name="<%=fN%>" property='<%="readOnlyClasses["+ctr+"]"%>' value="true">
								<html:hidden property='<%="snapshotLimits["+ctr+"]"%>'/>
								<bean:write name="<%=fN%>" property='<%="snapshotLimits["+ctr+"]"%>'/>
				</logic:equal></TD></logic:equal></logic:equal>
				<logic:notEqual name="<%=fN%>" property="displaySnapshotLimit" value="true">
					<html:hidden property='<%="snapshotLimits["+ctr+"]"%>'/>
				</logic:notEqual>
				<TD align="left" valign="top" nowrap>
					<logic:equal name="<%=fN%>" property='<%="readOnlyClasses["+ctr+"]"%>' value="false">
						<html:text name="<%=fN%>" property='<%="roomRatios["+ctr+"]"%>'
							tabindex="<%=java.lang.Integer.toString(6000 + ctr.intValue())%>" maxlength="6" size="3"/>
					</logic:equal>
					<logic:equal name="<%=fN%>" property='<%="readOnlyClasses["+ctr+"]"%>' value="true">
						<bean:write name="<%=fN%>" property='<%="roomRatios["+ctr+"]"%>'/>
						<html:hidden property='<%="roomRatios["+ctr+"]"%>'/>
				</logic:equal></TD>
				<TD align="left" valign="top" nowrap>
					<logic:equal name="<%=fN%>" property='<%="readOnlyClasses["+ctr+"]"%>' value="false">
						<html:text name="<%=fN%>" property='<%="numberOfRooms["+ctr+"]"%>'
							tabindex="<%=java.lang.Integer.toString(8000 + ctr.intValue())%>" maxlength="5" size="3"/>
					</logic:equal>
					<logic:equal name="<%=fN%>" property='<%="readOnlyClasses["+ctr+"]"%>' value="true">
						<bean:write name="<%=fN%>" property='<%="numberOfRooms["+ctr+"]"%>'/>
						<html:hidden property='<%="numberOfRooms["+ctr+"]"%>'/>
					</logic:equal>
				</TD>
			</logic:notEqual>
			<TD align="left" valign="top" nowrap>
				<logic:equal name="<%=fN%>" property='<%="readOnlyClasses["+ctr+"]"%>' value="false">
					<html:select style="width:200px;" property='<%="departments["+ctr+"]"%>' tabindex="<%=java.lang.Integer.toString(10000 + ctr.intValue())%>">
						<html:option value="-1"><loc:message name="dropDeptDepartment"/></html:option>
						<html:options collection='<%=Department.EXTERNAL_DEPT_ATTR_NAME + "list"%>' property="uniqueId" labelProperty="managingDeptLabel"/>
				</html:select></logic:equal>
				<logic:equal name="<%=fN%>" property='<%="readOnlyClasses["+ctr+"]"%>' value="true">
					<logic:iterate scope="request" name="<%=Department.EXTERNAL_DEPT_ATTR_NAME%>" id="dept">
						<logic:equal name="<%=fN%>" property='<%="departments["+ctr+"]"%>' value="<%=((Department) dept).getUniqueId().toString()%>">
							<bean:write name="dept" property="managingDeptLabel"/>
					</logic:equal></logic:iterate>
					<html:hidden property='<%="departments["+ctr+"]"%>'/>
			</logic:equal></TD>
			<TD align="left" valign="top" nowrap>
				<logic:equal name="<%=fN%>" property='<%="readOnlyDatePatterns["+ctr+"]"%>' value="false">
					<html:select style="width:100px;" property='<%="datePatterns["+ctr+"]"%>' tabindex="<%=java.lang.Integer.toString(12000 + ctr.intValue())%>">
						<html:options collection="<%=DatePattern.DATE_PATTERN_LIST_ATTR%>" property="id" labelProperty="value"/>
				</html:select></logic:equal>
				<logic:equal name="<%=fN%>" property='<%="readOnlyDatePatterns["+ctr+"]"%>' value="true">
					<logic:equal name="<%=fN%>" property='<%="datePatterns["+ctr+"]"%>' value=""><loc:message name="dropDefaultDatePattern"/></logic:equal>
					<logic:iterate scope="request" name="<%=DatePattern.DATE_PATTERN_LIST_ATTR%>" id="dp">
						<logic:notEqual name="<%=fN%>" property='<%="datePatterns["+ctr+"]"%>' value="">
							<logic:equal name="<%=fN%>" property='<%="datePatterns["+ctr+"]"%>' value="<%=((IdValue) dp).getId().toString()%>">
								<bean:write name="dp" property="value"/>
					</logic:equal></logic:notEqual></logic:iterate>
					<html:hidden property='<%="datePatterns["+ctr+"]"%>'/>
			</logic:equal></TD>
			<logic:equal name="<%=fN%>" property="displayLms" value="true">
				<TD align="left" valign="top" nowrap>
					<logic:equal name="<%=fN%>" property='<%="readOnlyClasses["+ctr+"]"%>' value="false">
						<html:select style="width:100px;" property='<%="lms["+ctr+"]"%>' tabindex="<%=java.lang.Integer.toString(14000 + ctr.intValue())%>">
							<html:options collection="<%=LearningManagementSystemInfo.LEARNING_MANAGEMENT_SYSTEM_LIST_ATTR%>" property="id" labelProperty="value"/>
					</html:select></logic:equal>
					<logic:equal name="<%=fN%>" property='<%="readOnlyClasses["+ctr+"]"%>' value="true">
						<logic:equal name="<%=fN%>" property='<%="lms["+ctr+"]"%>' value=""><loc:message name="dropDefaultLearningManagementSystem"/></logic:equal>
						<logic:iterate scope="request" name="<%=LearningManagementSystemInfo.LEARNING_MANAGEMENT_SYSTEM_LIST_ATTR%>" id="lmsInfo">
							<logic:notEqual name="<%=fN%>" property='<%="lms["+ctr+"]"%>' value="">
								<logic:equal name="<%=fN%>" property='<%="lms["+ctr+"]"%>' value="<%=((IdValue) lmsInfo).getId().toString()%>">
									<bean:write name="lmsInfo" property="value"/>
						</logic:equal></logic:notEqual></logic:iterate>
						<html:hidden property='<%="lms["+ctr+"]"%>'/>
				</logic:equal></TD>
			</logic:equal>
			<logic:notEqual name="<%=fN%>" property="displayLms" value="true"><html:hidden property='<%="lms["+ctr+"]"%>'/></logic:notEqual>
			<logic:equal name="<%=fN%>" property="displayDisplayInstructors" value="true">
				<TD align="center" valign="top" nowrap>
					<logic:equal name="<%=fN%>" property='<%="readOnlyClasses["+ctr+"]"%>' value="false">
						<html:checkbox name="<%=fN%>" property='<%="displayInstructors["+ctr+"]"%>' tabindex="<%=java.lang.Integer.toString(16000 + ctr.intValue())%>"/>
					</logic:equal>
					<logic:equal name="<%=fN%>" property='<%="readOnlyClasses["+ctr+"]"%>' value="true">
						<logic:equal name="<%=fN%>" property='<%="displayInstructors["+ctr+"]"%>' value="true">
							<IMG border='0' title='<%=MSG.titleDisplayAllInstrForSubpartInSchedBook()%>' alt='true' align='middle' src='images/accept.png'>
						</logic:equal>
						<html:hidden property='<%="displayInstructors["+ctr+"]"%>'/>
				</logic:equal>
			</TD></logic:equal>
			<logic:equal name="<%=fN%>" property="displayDisplayInstructors" value="false">
				<html:hidden property='<%="displayInstructors["+ctr+"]"%>'/>
			</logic:equal>
			<TD align="center" valign="top" nowrap>
				<logic:equal name="<%=fN%>" property="displayEnabledForStudentScheduling" value="true">
					<logic:equal name="<%=fN%>" property='<%="readOnlyClasses["+ctr+"]"%>' value="false">
						<html:checkbox name="<%=fN%>" property='<%="enabledForStudentScheduling["+ctr+"]"%>' tabindex="<%=java.lang.Integer.toString(18000 + ctr.intValue())%>"/>
					</logic:equal>
					<logic:equal name="<%=fN%>" property='<%="readOnlyClasses["+ctr+"]"%>' value="true">
						<logic:equal name="<%=fN%>" property='<%="enabledForStudentScheduling["+ctr+"]"%>' value="true">
							<IMG border='0' title='<%=MSG.titleEnableTheseClassesForStudentScheduling()%>' alt='true' align='middle' src='images/accept.png'>
						</logic:equal>
						<html:hidden property='<%="enabledForStudentScheduling["+ctr+"]"%>'/>
					</logic:equal>
				</logic:equal>
				<logic:equal name="<%=fN%>" property="displayEnabledForStudentScheduling" value="false">
					<html:hidden property='<%="enabledForStudentScheduling["+ctr+"]"%>'/>
			</logic:equal></TD>
			</logic:notEqual>
			<TD align="left" valign="top" nowrap><%=frm.getTimes().get(ctr)%>&nbsp;&nbsp; <html:hidden property='<%="times["+ctr+"]"%>'/></TD>
			<TD align="left" valign="top" nowrap><%=frm.getRooms().get(ctr)%> <html:hidden property='<%="rooms["+ctr+"]"%>'/></TD>
			<TD>&nbsp;</TD>
			<TD align="left" valign="top" nowrap><%=frm.getInstructors().get(ctr)%><html:hidden property='<%="instructors["+ctr+"]"%>'/></TD>
			<TD>&nbsp;</TD>
		</TR></logic:iterate>
	</TABLE></TD></TR>
<%
	if (frm.getDisplayDisplayInstructors().booleanValue() || frm.getDisplayEnabledForStudentScheduling().booleanValue()) {
%>
	<TR><TD valign="top"><loc:message name="propertySchedulingSubpartLimits"/></TD>
	<TD>
		<table align="left" border="0" cellspacing="0" cellpadding="0">
			<logic:iterate name="<%=fN%>" property="subtotalValues" id="v" indexId="ctr">
			<tr onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='default';"
				onmouseout="this.style.backgroundColor='transparent';">
				<td valign="top" align="right" nowrap><b><%=((String) frm.getSubtotalLabels().get(ctr)).trim()%>:</b>&nbsp;</td>
				<td nowrap align="right"><div id='<%="subtotal2Values" + ctr%>'>
					<logic:equal name="<%=fN%>" property="instrOffrConfigUnlimited" value="true">&infin;</logic:equal>
					<logic:notEqual name="<%=fN%>" property="instrOffrConfigUnlimited" value="true">
						<bean:write name="<%=fN%>" property='<%="subtotalValues["+ctr+"]"%>'/>
					</logic:notEqual>
				</div></td>
				<logic:equal name="<%=fN%>" property="displayDisplayInstructors" value="true">
					<TD align="center" nowrap>&nbsp; &nbsp; <loc:message name="propertyDisplayInstructors"/>
					<logic:equal name="<%=fN%>" property='<%="readOnlySubparts["+ctr+"]"%>' value="false">
						<html:checkbox name="<%=fN%>" property='<%="displayAllClassesInstructorsForSubpart["+ctr+"]"%>'
							onclick="<%=\"updateSubpartFlags(this.checked, 'displayInstructors',\" + ctr +\", 'displayAllClassesInstructorsForSubpart');\"%>"/>
					</logic:equal>
					<logic:equal name="<%=fN%>" property='<%="readOnlySubparts["+ctr+"]"%>' value="true">
						<logic:equal name="<%=fN%>" property='<%="displayAllClassesInstructorsForSubpart["+ctr+"]"%>' value="true">
							<IMG border='0' title='<%=MSG.titleDisplayAllInstrForSubpartInSchedBook()%>'
								alt='true' align="middle" src='images/accept.png'>
						</logic:equal>
						<html:hidden property='<%="displayAllClassesInstructorsForSubpart["+ctr+"]"%>'/>
					</logic:equal></TD>
				</logic:equal>
				<logic:equal name="<%=fN%>" property="displayEnabledForStudentScheduling" value="true">
					<TD align="center" nowrap>&nbsp; &nbsp; <loc:message name="propertyEnabledForStudentScheduling"/>
						<logic:equal name="<%=fN%>" property='<%="readOnlySubparts["+ctr+"]"%>' value="false">
							<html:checkbox name="<%=fN%>" property='<%="enableAllClassesForStudentSchedulingForSubpart["+ctr+"]"%>'
								onclick="<%=\"updateSubpartFlags(this.checked, 'enabledForStudentScheduling',\" + ctr +\", 'enableAllClassesForStudentSchedulingForSubpart');\"%>"/>
						</logic:equal><logic:equal name="<%=fN%>" property='<%="readOnlySubparts["+ctr+"]"%>' value="true">
							<logic:equal name="<%=fN%>" property='<%="enableAllClassesForStudentSchedulingForSubpart["+ctr+"]"%>' value="true">
								<IMG border='0' title='<%=MSG.titleEnableAllClassesOfSubpartForStudentScheduling()%>' alt='true' align='middle' src='images/accept.png'>
							</logic:equal>
							<html:hidden property='<%="enableAllClassesForStudentSchedulingForSubpart["+ctr+"]"%>'/>
						</logic:equal>
			</TD></logic:equal></tr>
	</logic:iterate></table></td></tr>
	<logic:equal name="<%=fN%>" property="editSnapshotLimits" value="true">
		<logic:notEqual name="<%=fN%>" property="instrOffrConfigUnlimited" value="true">
			<TR><TD valign="top" style="white-space: nowrap; width: 165px;"><loc:message name="propertySchedulingSubpartSnapshotLimits"/></TD>
				<TD><table align="left" border="0" cellspacing="0" cellpadding="0">
					<logic:iterate name="<%=fN%>" property="subtotalValues" id="v" indexId="ctr">
						<tr onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='default';"
							onmouseout="this.style.backgroundColor='transparent';">
								<td valign="top" align="right" nowrap><b><%=((String) frm.getSubtotalLabels().get(ctr)).trim()%>:</b>&nbsp;</td>
								<td align="right" nowrap><div id='<%="subtotal2SnapValues" + ctr%>'>
									<bean:write name="<%=fN%>" property='<%="subtotalSnapValues["+ctr+"]"%>'/>
								</div></td>
					</tr></logic:iterate>
			</table></TD></TR>
	</logic:notEqual></logic:equal>
<%	} %>

<SCRIPT language="javascript">
<!--
document.forms[0].elements["op"][0].disabled="";
// -->
</SCRIPT>
<!-- Buttons -->
<TR><TD colspan="2" valign="middle"><DIV class="WelcomeRowHeadBlank">&nbsp;</DIV></TD></TR>
<TR><TD colspan="2" align="right">
	<html:submit property="op" styleClass="btn" accesskey="<%=MSG.accessUpdateMultipleClassSetup()%>"
		title="<%=MSG.titleUpdateMultipleClassSetup(MSG.accessUpdateMultipleClassSetup())%>">
		<loc:message name="actionUpdateMultipleClassSetup"/>
	</html:submit>
	<bean:define id="instrOfferingId"><bean:write name="<%=fN%>" property="instrOfferingId"/></bean:define>
	<html:button property="op" styleClass="btn" accesskey="<%=MSG.accessBackToIODetail() %>"
		title="<%=MSG.titleBackToIODetail(MSG.accessBackToIODetail()) %>"
		onclick="document.location.href='instructionalOfferingDetail.do?op=view&io=${instrOfferingId}';">
		<loc:message name="actionBackToIODetail"/>
	</html:button>
</TD></TR></TABLE>
</html:form>
<script language="javascript">hideLoading();</script>
</loc:bundle>