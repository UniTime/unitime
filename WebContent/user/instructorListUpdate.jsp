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

<script type="text/javascript">
	function doSelectAll(elem, styleId, checked) {
		var i = 0;
		
		for (;;) {
			var idName = styleId + '-' + i + '-1';
			var idVal = document.getElementById(idName);
			if (idVal!=null && idVal.value!=null) {
				if (!idVal.disabled)
					idVal.checked = checked;
			}
			else {
				break;
			}
			++i;
		}
	}
</script>

<loc:bundle name="CourseMessages"><s:set var="msg" value="#attr.MSG"/>
<s:form action="instructorListUpdate">

	<table class="unitime-MainTable">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<s:property value="form.deptName"/>
					</tt:section-title>
					<s:submit accesskey='%{#msg.accessUpdateInstructorsList()}' name='op' value='%{#msg.actionUpdateInstructorsList()}'
						title='%{#msg.titleUpdateInstructorsList(#msg.accessUpdateInstructorsList())}'/>
					<s:submit accesskey='%{#msg.accessBackToInstructors()}' name='op' value='%{#msg.actionBackToInstructors()}'
						title='%{#msg.titleBackToInstructors(#msg.accessBackToInstructors())}'/>
				</tt:section-header>
			</TD>
		</TR>
		
		<TR>
			<TD colspan='2'>
				<script type="text/javascript">blToggleHeader('<loc:message name="filter"/>','dispFilter');blStart('dispFilter');</script>
				<table style="width: 100%;">
					<TR>
						<TD valign="top">
							<B><loc:message name="propertyFilterDisplay"/></B>
						</TD>
						<TD>
							<s:radio name="form.displayListType" list="#{'assigned':''}"/><loc:message name="filterDisplayDepartmentAssignedInstructorsOnly"/><br>
							<s:radio name="form.displayListType" list="#{'available':''}"/><loc:message name="filterDisplayAvailableInstructorsOnly"/><br>
							<s:radio name="form.displayListType" list="#{'both':''}"/><loc:message name="filterDisplayBothDepartmentAssignedAndAvailableInstructors"/><br>
						</TD>
					</TR>
					<TR>
						<TD valign="top" style="white-space: nowrap;">
							<B><loc:message name="propertyFilterIgorePositions"/></B> *
						</TD>
						<TD>
							<table>
								<s:iterator value="#request.posTypeList" var="type">
									<span style="width:25%; display:inline-block;">
										<s:checkboxlist name="form.displayPosType" list="#{#type.reference:''}"/>
										<s:property value="#type.label"/>
									</span>
								</s:iterator>
							 </table>
						</TD>
					</TR>
					<TR>
						<TD colspan='2'><i><loc:message name="descriptionIgnorePosition"/></i></TD>
					</TR>
					<TR>
						<TD colspan='2' align="right">
							<s:submit accesskey='%{#msg.accessApplyInstructorFilter()}' name='op' value='%{#msg.actionApplyInstructorFilter()}'
								title='%{#msg.titleApplyInstructorFilter(#msg.accessApplyInstructorFilter())}'/>
						</TD>
					</TR>
					<TR>
						<TD colspan='2'>&nbsp;</TD>
					</TR>
				</table>
				<script type="text/javascript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
					<table style="width: 100%;">
						<TR>
							<TD colspan='2' align='right'>
								<br>
							</TD>
						</TR>
					</table>
				<script type="text/javascript">blEnd('dispFilter');</script>
			</TD>
		</TR>

<!-- Department Instructors -->
	<s:if test="form.displayListType == null || form.displayListType == 'both' || form.displayListType == 'assigned'">
		<TR>
			<TD colspan='2'>
				<tt:section-title><A id="DeptInstr"><loc:message name="sectionDepartmentInstructors"/></A></tt:section-title>
			</TD>
		</TR>
		<s:if test="form.assignedInstr == null || form.assignedInstr.isEmpty()">
			<TR>
				<TD colspan='2'><loc:message name="messageNoDepartmentalInstructors"/></TD>
			</TR>
		</s:if>
		<s:else>
			<TR>
				<TD colspan='2'>
					<table>
						<TR align="center">
							<TD> &nbsp;</TD>
							<TD align="left"><I><loc:message name="columnExternalId"/></I></TD>
							<TD align="left"><I><loc:message name="columnInstructorName"/></I></TD>
						</TR>
						<s:set var="prevPosType" value="''"/>
						<s:set var="idx" value="0"/>
						<s:iterator value="form.assignedInstr" var="instr" status="stat">
							<s:set var="posType" value="%{#instr.positionType}"/>
							<s:set var="canDelete" value="%{#instr.classes.isEmpty() && #instr.exams.isEmpty()}"/>
							<s:if test="#posType == null">
								<s:set var="posId" value="-1"/>
								<s:set var="currPosType" value="%{#msg.positionNotSet}"/>
							</s:if>
							<s:else>
								<s:set var="posId" value="%{#posType.uniqueId}"/>
								<s:set var="currPosType" value="%{#posType.label}"/>
							</s:else>
							<s:if test="#currPosType != #prevPosType">
								<s:set var="prevPosType" value="%{#currPosType}"/>
								<s:set var="idx" value="0"/>
								<TR>
									<TD colspan="4" align="left">
										<s:if test="#stat.index > 0">&nbsp;<br></s:if>
										<span style="font-weight: bold; text-decoration: underline;"><s:property value="#currPosType"/></span></TD>
									<TD colspan="2" align="right">
										<s:if test="#stat.index > 0">&nbsp;<br></s:if>
										<IMG src="images/check_all.gif" alt="Select all ${currPosType}" title="Select All ${currPosType}" align="middle" onclick="doSelectAll(this, 'as-${posId}', true);" onmouseover="this.style.cursor='hand';this.style.cursor='pointer';">
										<IMG src="images/clear_all.gif" alt="Clear all ${currPosType}" title="Clear All ${currPosType}" align="middle" onclick="doSelectAll(this, 'as-${posId}', false);" onmouseover="this.style.cursor='hand';this.style.cursor='pointer';">
									</TD>
								</TR>
							</s:if>
							<TR align="center">
								<TD class="BottomBorderGray">
									<s:checkboxlist name="form.assignedSelected" list="#{#instr.uniqueId:''}" id="as-%{#posId}-%{#idx}" disabled="%{!#canDelete}"/>
								</TD>
								<TD align="left" class="BottomBorderGray">
									<s:if test="#instr.externalUniqueId != null">
										<s:property value="#instr.externalUniqueId"/>
									</s:if>
								</TD>
								<TD align="left" class="BottomBorderGray">
									<s:property value="#instr.getName(form.nameFormat)"/>
								</TD>
							</TR>
							<s:set var="idx" value="%{#idx + 1}"/>
						</s:iterator>
					</table>
				</TD>
			</TR>
			<TR>
				<TD colspan='2'>&nbsp;</TD>
			</TR>
		</s:else>
	</s:if>

<!-- Instructors not in the Department List -->
	<s:if test="form.displayListType == null || form.displayListType == 'both' || form.displayListType == 'available'">
		<TR>
			<TD colspan='2'>
				<tt:section-title><A id="NonDeptInstr"><loc:message name="sectionAvailableInstructors"/></A></tt:section-title>
			</TD>
		</TR>
		<s:if test="form.availableInstr == null || form.availableInstr.isEmpty()">
			<TR>
				<TD colspan='2'><loc:message name="messageNoAvailableInstructors"/></TD>
			</TR>
		</s:if>
		<s:else>
			<TR>
				<TD colspan='2'>
					<table>
						<TR align="center">
							<TD> &nbsp;</TD>
							<TD align="left"><I><loc:message name="columnExternalId"/></I></TD>
							<TD align="left"><I><loc:message name="columnInstructorName"/></I></TD>
						</TR>
						<s:set var="prevPosType" value="''"/>
						<s:set var="idx" value="0"/>
						<s:iterator value="form.availableInstr" var="instr" status="stat">
							<s:set var="posType" value="%{#instr.positionType}"/>
							<s:if test="#posType == null">
								<s:set var="posId" value="-1"/>
								<s:set var="currPosType" value="%{#msg.positionNotSet}"/>
							</s:if>
							<s:else>
								<s:set var="posId" value="%{#posType.uniqueId}"/>
								<s:set var="currPosType" value="%{#posType.label}"/>
							</s:else>
							<s:if test="#currPosType != #prevPosType">
								<s:set var="prevPosType" value="%{#currPosType}"/>
								<s:set var="idx" value="0"/>
								<TR>
									<TD colspan="4" align="left">
										<s:if test="#stat.index > 0">&nbsp;<br></s:if>
										<span style="font-weight: bold; text-decoration: underline;"><s:property value="#currPosType"/></span>
									</TD>
									<TD colspan="2" align="right">
										<s:if test="#stat.index > 0">&nbsp;<br></s:if>
										<IMG src="images/check_all.gif" alt="Select all ${currPosType}" title="Select All ${currPosType}" align="middle" onclick="doSelectAll(this, 'na-${posId}', true);" onmouseover="this.style.cursor='hand';this.style.cursor='pointer';">
										<IMG src="images/clear_all.gif" alt="Clear all ${currPosType}" title="Clear All ${currPosType}" align="middle" onclick="doSelectAll(this, 'na-${posId}', false);" onmouseover="this.style.cursor='hand';this.style.cursor='pointer';">
									</TD>
								</TR>
							</s:if>
							<TR align="center">
								<TD class="BottomBorderGray">
									<s:checkboxlist name="form.availableSelected" list="#{#instr.uniqueId:''}" id="na-%{#posId}-%{#idx}"/>
								</TD>
								<TD align="left" class="BottomBorderGray">
									<s:if test="#instr.externalUniqueId != null">
										<s:property value="#instr.externalUniqueId"/>
									</s:if>
								</TD>
								<TD align="left" class="BottomBorderGray">
									<s:property value="#instr.getName(form.nameFormat)"/>
								</TD>
							</TR>
							<s:set var="idx" value="%{#idx + 1}"/>
						</s:iterator>
					</table>
				</TD>
			</TR>
			<TR>
				<TD colspan='2'>&nbsp;</TD>
			</TR>
		</s:else>
	</s:if>
	
		<TR>
			<TD valign="middle" colspan='2' class='WelcomeRowHead'>
				&nbsp;
			</TD>
		</TR>
		
		<TR>
			<TD valign="middle" colspan='2' align="right">
					<s:submit accesskey='%{#msg.accessUpdateInstructorsList()}' name='op' value='%{#msg.actionUpdateInstructorsList()}'
						title='%{#msg.titleUpdateInstructorsList(#msg.accessUpdateInstructorsList())}'/>
					<s:submit accesskey='%{#msg.accessBackToInstructors()}' name='op' value='%{#msg.actionBackToInstructors()}'
						title='%{#msg.titleBackToInstructors(#msg.accessBackToInstructors())}'/>
			</TD>
		</TR>
		
	</TABLE>
</s:form>
</loc:bundle>
