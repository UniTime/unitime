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
<loc:bundle name="ConstantsMessages" id="CONST"><s:set var="const" value="#attr.CONST"/>
<SCRIPT type="text/javascript">
	function datePatternChanged(){			
		var op2Obj = document.getElementById('op2');
		if (op2Obj!=null) {
			op2Obj.value='updateDatePattern';
			document.forms[0].submit();
		}			
	}
	function creditFormatChanged(creditFormat) {
		if (creditFormat.value == 'fixedUnit') {
			document.getElementById('schedulingSubpartEdit_form_creditType').disabled = false;
			document.getElementById('schedulingSubpartEdit_form_creditUnitType').disabled = false;
			document.getElementById('schedulingSubpartEdit_form_units').disabled = false;
			document.getElementById('schedulingSubpartEdit_form_maxUnits').disabled = true;
			document.getElementById('schedulingSubpartEdit_form_fractionalIncrementsAllowed').disabled = true
		} else if (creditFormat.value == 'arrangeHours') {
			document.getElementById('schedulingSubpartEdit_form_creditType').disabled = false;
			document.getElementById('schedulingSubpartEdit_form_creditUnitType').disabled = false;
			document.getElementById('schedulingSubpartEdit_form_units').disabled = true;
			document.getElementById('schedulingSubpartEdit_form_maxUnits').disabled = true;
			document.getElementById('schedulingSubpartEdit_form_fractionalIncrementsAllowed').disabled = true
		} else if (creditFormat.value == 'variableMinMax') {
			document.getElementById('schedulingSubpartEdit_form_creditType').disabled = false;
			document.getElementById('schedulingSubpartEdit_form_creditUnitType').disabled = false;
			document.getElementById('schedulingSubpartEdit_form_units').disabled = false;
			document.getElementById('schedulingSubpartEdit_form_maxUnits').disabled = false;
			document.getElementById('schedulingSubpartEdit_form_fractionalIncrementsAllowed').disabled = true
		} else if (creditFormat.value == 'variableRange') {
			document.getElementById('schedulingSubpartEdit_form_creditType').disabled = false;
			document.getElementById('schedulingSubpartEdit_form_creditUnitType').disabled = false;
			document.getElementById('schedulingSubpartEdit_form_units').disabled = false;
			document.getElementById('schedulingSubpartEdit_form_maxUnits').disabled = false;
			document.getElementById('schedulingSubpartEdit_form_fractionalIncrementsAllowed').disabled = false
		} else {
			document.getElementById('schedulingSubpartEdit_form_creditType').disabled = true;
			document.getElementById('schedulingSubpartEdit_form_creditUnitType').disabled = true;
			document.getElementById('schedulingSubpartEdit_form_units').disabled = true;
			document.getElementById('schedulingSubpartEdit_form_maxUnits').disabled = true;
			document.getElementById('schedulingSubpartEdit_form_fractionalIncrementsAllowed').disabled = true
		}
	}
</SCRIPT>
<s:form action="schedulingSubpartEdit">
	<s:hidden name="form.schedulingSubpartId"/>
	<s:hidden name="form.creditText"/>
	<s:hidden name="form.subpartCreditEditAllowed"/>

	<table class="unitime-MainTable">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<s:property value="form.subjectArea"/>&nbsp;
						<s:property value="form.courseNbr"/> :
						<s:property value="form.parentSubpart"/>
						<B><s:property value="form.instructionalTypeLabel"/></B>
					</tt:section-title>
					<s:submit accesskey='%{#msg.accessUpdatePreferences()}' name='op' value='%{#msg.actionUpdatePreferences()}'
						title='%{#msg.titleUpdatePreferences(#msg.accessUpdatePreferences())}'/>
					<sec:authorize access="hasPermission(#form.schedulingSubpartId, 'SchedulingSubpart', 'SchedulingSubpartEditClearPreferences')"> 
						<s:submit accesskey='%{#msg.accessClearSubpartPreferences()}' name='op' value='%{#msg.actionClearSubpartPreferences()}'
							title='%{#msg.titleClearSubpartPreferences(#msg.accessClearSubpartPreferences())}'/>
					</sec:authorize>
					<s:if test="form.previousId != null">
						<s:submit accesskey='%{#msg.accessPreviousSubpart()}' name='op' value='%{#msg.actionPreviousSubpart()}'
							title='%{#msg.titlePreviousSubpartWithUpdate(#msg.accessPreviousSubpart())}'/>
					</s:if>
					<s:if test="form.nextId != null">
						<s:submit accesskey='%{#msg.accessNextSubpart()}' name='op' value='%{#msg.actionNextSubpart()}'
							title='%{#msg.titleNextSubpartWithUpdate(#msg.accessNextSubpart())}'/>
					</s:if>
					<s:submit accesskey='%{#msg.accessBackToDetail()}' name='op' value='%{#msg.actionBackToDetail()}'
							title='%{#msg.titleBackToDetail(#msg.accessBackToDetail())}'/>
				</tt:section-header>
			</TD>
		</TR>
		
		<s:if test="!fieldErrors.isEmpty()">
			<TR><TD colspan="2" align="left" class="errorTable">
				<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror/>
			</TD></TR>
		</s:if>

		<s:if test="form.managingDeptName != null">
			<TR>
				<TD><loc:message name="filterManager"/></TD>
				<TD><s:property value="form.managingDeptName"/></TD>
			</TR>
		</s:if>
		<s:if test="form.parentSubpartLabel != null">
			<TR>
				<TD><loc:message name="propertyParentSchedulingSubpart"/></TD>
				<TD><s:property value="form.parentSubpartLabel"/></TD>
			</TR>
		</s:if>
		<TR>
			<TD><loc:message name="filterInstructionalType"/></TD>
			<TD>
				<s:select name="form.instructionalType"
					list="#request.itypes" listKey="value" listValue="label"
					onchange="itypeChanged(this);" style="min-width:200px;"
					/>				 
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="propertyDatePattern"/></TD>
			<TD>
				<s:hidden name="op2" value="" id="op2"/>
				<s:hidden name="form.datePatternEditable"/>
				<s:if test="form.datePatternEditable == true">
					<s:select name="form.datePattern" list="#request.datePatternList" listKey="id" listValue="value"
						style="min-width:200px;" onchange="datePatternChanged();"/>
					<img style="cursor: pointer;" src="images/calendar.png" border="0" onclick="showGwtDialog('Preview of '+schedulingSubpartEdit_form_datePattern.options[schedulingSubpartEdit_form_datePattern.selectedIndex].text, 'dispDatePattern.action?id='+schedulingSubpartEdit_form_datePattern.value+'&subpartId='+schedulingSubpartEdit_form_schedulingSubpartId.value,'840','520');">
				</s:if>
				<s:else>
					<s:hidden name="form.datePattern"/>
					<s:iterator value="#request.datePatternList" var="dp">
						<s:if test="#dp.id == form.datePattern">
							<s:property value="#dp.value"/>
							<img style="cursor: pointer;" src="images/calendar.png" border="0" onclick="showGwtDialog('${MSG.sectPreviewOfDatePattern(dp.value)}', 'dispDatePattern.action?id=${dp.id}&subpartId=${form.schedulingSubpartId}','840','520');">
						</s:if>
					</s:iterator>
				</s:else>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="propertyAutomaticSpreadInTime"/></TD>
			<TD>
				<s:checkbox name="form.autoSpreadInTime"/> <i><loc:message name="descriptionAutomaticSpreadInTime"/></i>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="propertyStudentOverlaps"/></TD>
			<TD>
				<s:checkbox name="form.studentAllowOverlap"/> <i><loc:message name="descriptionStudentOverlaps"/></i>
			</TD>
		</TR>
		<s:if test="form.sameItypeAsParent == false">
		<s:if test="form.subpartCreditEditAllowed == true">
		<TR>
			<TD><loc:message name="propertySubpartCredit"/></TD>
			<TD>
				<s:select name="form.creditFormat"
					list="#request.courseCreditFormatList" listKey="reference" listValue="label"
					style="width:200px;" onchange="creditFormatChanged(this);"
					headerKey="" headerValue="%{#const.select()}"/>
			</TD>
		</TR>
		<TR>
			<TD> &nbsp;</TD>
			<TD>
				<table>
				<tr>
				<td nowrap><loc:message name="propertyCreditType"/></td>
				<td>
					<s:select name="form.creditType"
						list="#request.courseCreditTypeList" listKey="uniqueId" listValue="label"
						style="width:200px;" disabled="%{form.creditFormat == null || form.creditFormat.isEmpty()}"/>
				</td>
				</tr>
				<tr>
				<td nowrap><loc:message name="propertyCreditUnitType"/></td>
				<td>
				<s:select name="form.creditUnitType"
					list="#request.courseCreditUnitTypeList" listKey="uniqueId" listValue="label" 
					style="width:200px;" disabled="%{form.creditFormat == null || form.creditFormat.isEmpty()}"/>
				</td>
				</tr>
				<tr>
				<td nowrap><loc:message name="propertyUnits"/></td>
				<td>
				<s:textfield name="form.units" maxlength="4" size="4"
					disabled="%{form.creditFormat == null || form.creditFormat.isEmpty() || form.creditFormat == 'arrangeHours'}"/>
				</td>
				</tr>
				<tr>
				<td nowrap><loc:message name="propertyMaxUnits"/></td>
				<td>
				<s:textfield name="form.maxUnits" maxlength="4" size="4"
					disabled="%{form.creditFormat != 'variableRange'}"/>
				</td>
				</tr>
				<tr>
				<td nowrap><loc:message name="propertyFractionalIncrementsAllowed"/></td>
				<td>
				<s:checkbox name="form.fractionalIncrementsAllowed"
					disabled="%{form.creditFormat != 'variableRange'}"/>
				</td>
				</tr>
				</table>
			</TD>
		</TR>
		</s:if>
		<s:if test="form.subpartCreditEditAllowed == false">
			<s:if test="form.creditText != null && !form.creditText.isEmpty()">
				<TR>
					<TD><loc:message name="propertyCredit"/></TD>
					<TD><s:property value="form.creditText"/></TD>
				</TR>
			</s:if>
			<s:hidden name="form.creditFormat"/>
			<s:hidden name="form.creditType"/>
			<s:hidden name="form.creditUnitType"/>
			<s:hidden name="form.units"/>
			<s:hidden name="form.maxUnits"/>				
			<s:hidden name="form.fractionalIncrementsAllowed"/>				
		</s:if>
		</s:if>
<!-- Preferences -->
		<s:if test="form.unlimitedEnroll == true">
			<s:include value="preferencesEdit2.jspf">
				<s:param name="frmName" value="'schedulingSubpartEdit'"/>
				<s:param name="distPref" value="false"/>
				<s:param name="periodPref" value="false"/>
				<s:param name="bldgPref" value="false"/>
				<s:param name="roomFeaturePref" value="false"/>
				<s:param name="roomGroupPref" value="false"/>
			</s:include>
		</s:if>
		<s:if test="form.unlimitedEnroll != true">
			<s:include value="preferencesEdit2.jspf">
				<s:param name="frmName" value="'schedulingSubpartEdit'"/>
				<s:param name="distPref" value="false"/>
				<s:param name="periodPref" value="false"/>
			</s:include>
		</s:if>

<!-- buttons -->
		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>
		<TR>
			<TD colspan="2" align="right">
					<s:submit accesskey='%{#msg.accessUpdatePreferences()}' name='op' value='%{#msg.actionUpdatePreferences()}'
						title='%{#msg.titleUpdatePreferences(#msg.accessUpdatePreferences())}'/>
					<sec:authorize access="hasPermission(#form.schedulingSubpartId, 'SchedulingSubpart', 'SchedulingSubpartEditClearPreferences')"> 
						<s:submit accesskey='%{#msg.accessClearSubpartPreferences()}' name='op' value='%{#msg.actionClearSubpartPreferences()}'
							title='%{#msg.titleClearSubpartPreferences(#msg.accessClearSubpartPreferences())}'/>
					</sec:authorize>
					<s:if test="form.previousId != null">
						<s:submit accesskey='%{#msg.accessPreviousSubpart()}' name='op' value='%{#msg.actionPreviousSubpart()}'
							title='%{#msg.titlePreviousSubpartWithUpdate(#msg.accessPreviousSubpart())}'/>
					</s:if>
					<s:if test="form.nextId != null">
						<s:submit accesskey='%{#msg.accessNextSubpart()}' name='op' value='%{#msg.actionNextSubpart()}'
							title='%{#msg.titleNextSubpartWithUpdate(#msg.accessNextSubpart())}'/>
					</s:if>
					<s:submit accesskey='%{#msg.accessBackToDetail()}' name='op' value='%{#msg.actionBackToDetail()}'
							title='%{#msg.titleBackToDetail(#msg.accessBackToDetail())}'/>
			</TD>
		</TR>

	</TABLE>
	<s:if test="#request.hash != null">
		<SCRIPT type="text/javascript">
			location.hash = '<%=request.getAttribute("hash")%>';
		</SCRIPT>
	</s:if>
</s:form>

<SCRIPT type="text/javascript">
	function itypeChanged(itypeObj) {
		var options = itypeObj.options;
		var currentId = itypeObj.options[itypeObj.selectedIndex].value;
		var basic = true;
		if (currentId=='more') {
			basic = false;
		} else if (currentId=='less') {
			basic = true;
		} else return;
		
		// Request initialization
		if (window.XMLHttpRequest) req = new XMLHttpRequest();
		else if (window.ActiveXObject) req = new ActiveXObject( "Microsoft.XMLHTTP" );

		// Response
		req.onreadystatechange = function() {
			if (req.readyState == 4) {
				if (req.status == 200) {
					// Response
					var xmlDoc = req.responseXML;
					if (xmlDoc && xmlDoc.documentElement && xmlDoc.documentElement.childNodes && xmlDoc.documentElement.childNodes.length > 0) {
						options.length=1;
						var count = xmlDoc.documentElement.childNodes.length;
						for(i=0; i<count; i++) {
							var optId = xmlDoc.documentElement.childNodes[i].getAttribute("id");
							var optVal = xmlDoc.documentElement.childNodes[i].getAttribute("value");
							options[i+1] = new Option(optVal, optId, (currentId==optId));
						}
						if (basic)
							options[count+1] = new Option("${MSG.selectMoreOptions()}","more",false);
						else
							options[count+1] = new Option("${MSG.selectLessOptions()}","less",false);
						options[count+1].style.backgroundColor='rgb(223,231,242)';
					}
				}
			}
		};
	
		// Request
		var vars = "basic="+basic;
		req.open( "POST", "ajax/itypesAjax.action", true );
		req.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
		req.send(vars);
	}
</SCRIPT>
</loc:bundle>
</loc:bundle>