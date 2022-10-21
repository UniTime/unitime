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
<tt:confirm name="confirmUnassignAll"><loc:message name="confirmUnassignAllInstructors"/></tt:confirm>
<s:form action="classInstructorAssignment" id="form">
<SCRIPT type="text/javascript" >
function resetAllDisplayFlags(value) {
	for (var i=0;i<${form.getClassIds().size()};i++) {
		var chbox = document.getElementsByName('form.displayFlags['+i+']');
		if (chbox!=null && chbox.length>0)
			chbox[0].checked = value;
	}
}
function doDelete(idx) {
	document.getElementById('hdnOp').value = 'Delete';
	document.getElementById('deletedInstrRowNum').value = idx;
	document.getElementById('form').submit();
}
function doAddInstructor(idx) {
	document.getElementById('hdnOp').value = 'Add Instructor';
	document.getElementById('addInstructorId').value = idx;
	document.getElementById('form').submit();
}
</SCRIPT>
	<s:hidden name="form.instrOffrConfigId"/>
	<s:hidden name="form.instrOfferingId"/>	
	<s:hidden name="form.displayExternalId"/>
	<s:hidden name="form.defaultTeachingResponsibilityId"/>
	<s:hidden name="form.deletedInstrRowNum" value = "" id="deletedInstrRowNum"/>
	<s:hidden name="form.addInstructorId" value = "" id="addInstructorId"/>
	<s:hidden name="hdnOp" value = "" id="hdnOp"/>
	<TABLE class="unitime-MainTable">
		<TR>
			<TD colspan="2" valign="middle">
				 <tt:section-header>
					<tt:section-title>
							<A  title="${MSG.titleBackToIOList(MSG.accessBackToIOList())}" 
								accesskey="${MSG.accessBackToIOList()}"
								class="l8" 
								href="instructionalOfferingSearch.action?doit=Search&loadInstrFilter=1&subjectAreaIds=${form.subjectAreaId}&courseNbr=${crsNbr}#A${form.instrOfferingId}"
							><s:property value="form.instrOfferingName"/></A>
							<s:hidden name="form.instrOfferingName"/>
					</tt:section-title>

				<!-- dummy submit button to make sure Update button is the first (a.k.a. default) submit button -->
				<s:submit name='op' value='%{#msg.actionUpdateClassInstructorsAssignment()}' style="position: absolute; left: -100%;"/>

				<s:submit name='op' value='%{#msg.actionUnassignAllInstructorsFromConfig()}' title='%{#msg.titleUnassignAllInstructorsFromConfig()}'
					onclick="return confirmUnassignAll();"/>

				<s:submit accesskey='%{#msg.accessUpdateClassInstructorsAssignment()}' name='op' value='%{#msg.actionUpdateClassInstructorsAssignment()}'
							title='%{#msg.titleUpdateClassInstructorsAssignment(#msg.accessUpdateClassInstructorsAssignment())}'/>

				<s:hidden name="form.previousId"/>
				<s:if test="form.previousId != null">
					<s:submit accesskey='%{#msg.accessPreviousIO()}' name='op' value='%{#msg.actionPreviousIO()}'
							title='%{#msg.titlePreviousIOWithUpdate(#msg.accessPreviousIO())}'/>
				</s:if>
				<s:hidden name="form.nextId"/>
				<s:if test="form.nextId != null">
					<s:submit accesskey='%{#msg.accessNextIO()}' name='op' value='%{#msg.actionNextIO()}'
							title='%{#msg.titleNextIOWithUpdate(#msg.accessNextIO())}'/>
				</s:if>

				<s:submit accesskey='%{#msg.accessBackToIODetail()}' name='op' value='%{#msg.actionBackToIODetail()}'
							title='%{#msg.titleBackToIODetail(#msg.accessBackToIODetail())}'/>
				</tt:section-header>					
			</TD>
		</TR>

		<s:if test="!fieldErrors.isEmpty()">
			<TR><TD colspan="2" align="left" class="errorTable">
				<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror/>
			</TD></TR>
		</s:if>


		<TR>
			<TD valign="top"><loc:message name="propertyCoordinators"/></TD>
			<TD>
				<s:property value="form.coordinators" escapeHtml="false"/>
				<s:hidden name="form.coordinators"/>
			</TD>
		</TR>
		<TR>
			<TD colspan="2" align="left">
				<TABLE class="unitime-Table" style="width:100%;">
					<TR>
						<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'> &nbsp;</TD>
						<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'> &nbsp;</TD>
						<s:if test="form.displayExternalId == true">
							<TD align="left" valign="bottom" rowspan="2" class='WebTableHeader'><loc:message name="columnExternalId"/></TD>
						</s:if>
						<TD class='WebTableHeader' rowspan="2">&nbsp;</TD>
						<TD class='WebTableHeader' rowspan="2">&nbsp;</TD>
						<TD align="left" valign="bottom" rowspan="2" class='WebTableHeader'><loc:message name="columnInstructorName"/></TD>
						<TD align="right" valign="bottom" rowspan="2" class='WebTableHeader'><loc:message name="columnInstructorShare"/></TD>
						<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'><loc:message name="columnInstructorCheckConflictsBr"/></TD>
						<s:if test="#request.responsibilities != null && !#request.responsibilities.isEmpty()">
							<TD align="left" valign="bottom" rowspan="2" class='WebTableHeader'><loc:message name="columnTeachingResponsibility"/></TD>
						</s:if>
						<TD align="center" valign="bottom" class='WebTableHeaderFirstRow'><loc:message name="columnDisplay"/></TD>
						<TD align="left" valign="bottom" rowspan="2" class='WebTableHeader'><loc:message name="columnAssignedTime"/></TD>
						<TD align="left" valign="bottom" rowspan="2" class='WebTableHeader'><loc:message name="columnAssignedRoom"/></TD>
					</TR>
					<TR>
						<TD align="left" valign="bottom" class='WebTableHeaderSecondRow'>
							(<loc:message name="propertyAll"/>
							<input type='checkbox' checked='checked' 
									onclick='resetAllDisplayFlags(this.checked);' value='test'>)
						</TD>
					</TR>

					<s:iterator value="form.classIds" var="c" status="stat"><s:set var="ctr" value="#stat.index"/>
						<TR onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='default';" onmouseout="this.style.backgroundColor='transparent';">
							<TD nowrap valign="top">
								<s:hidden name="form.classIds[%{#ctr}]"/>
								<s:hidden name="form.classLabels[%{#ctr}]"/>
								<s:hidden name="form.classLabelIndents[%{#ctr}]"/>
								<s:hidden name="form.rooms[%{#ctr}]"/>
								<s:hidden name="form.times[%{#ctr}]"/>
								<s:hidden name="form.allowDeletes[%{#ctr}]"/>
								<s:hidden name="form.readOnlyClasses[%{#ctr}]"/>
								<s:hidden name="form.classHasErrors[%{#ctr}]" value="false"/>
								<s:hidden name="form.showDisplay[%{#ctr}]"/>
								<s:if test="form.classHasErrors[#ctr] == true">
									<IMG src="images/cancel.png">
								</s:if>
							</TD>
							<TD nowrap valign="top">
								<s:property value="form.classLabelIndents[#ctr]" escapeHtml="false"/>
								<s:property value="form.classLabels[#ctr]"/> 
								&nbsp;
							</TD>
							<s:if test="form.displayExternalId == true">
								<TD align="left" valign="top" nowrap><s:property value="form.externalIds[#ctr]"/></TD>
							</s:if>
							<TD align="center" valign="top" nowrap>
								<s:if test="form.readOnlyClasses[#ctr] == 'false' && form.allowDeletes[#ctr] == true">
									<IMG border="0" src="images/action_delete.png" title="${MSG.titleRemoveInstructorFromClass()}"
										onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
										onclick="doDelete('${ctr}');">
								</s:if>
							</TD>
							<TD align="center" valign="top" nowrap> &nbsp;
								<s:if test="form.readOnlyClasses[#ctr] == 'false'">
									<IMG border="0" src="images/action_add.png" title="${MSG.titleAddInstructorToClass()}"
										onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
										onclick="doAddInstructor('${ctr}');">
								</s:if>
							</TD>
							<TD align="left" valign="top" nowrap>
								<s:hidden name="form.externalIds[%{#ctr}]"/>
								<s:if test="form.readOnlyClasses[#ctr] == 'false'">
									<s:select name="form.instructorUids[%{#ctr}]"
										list="#request.instructorsList" listKey="uniqueId" listValue="getName(nameFormat)"
										headerKey="" headerValue="%{#msg.itemSelect()}"
										style="width:200px;"/>
								</s:if><s:else>
									<s:iterator value="#request.instructorsList" var="instr">
										<s:if test="form.instructorUids[#ctr] == #instr.uniqueId">
											<s:property value="#instr.getName(nameFormat)"/>
										</s:if>
									</s:iterator>
									<s:hidden name="form.instructorUids[%{#ctr}]"/>
								</s:else>
							</TD>
							
							<TD align="right" valign="top" nowrap>
								<s:if test="form.readOnlyClasses[#ctr] == 'false'">
									<s:textfield name="form.percentShares[%{#ctr}]" maxlength="5" size="5" style="text-align:right;"/>
								</s:if><s:else>
									<s:hidden name="form.percentShares[%{#ctr}]"/>
									<s:property value="form.percentShares[#ctr]"/>
								</s:else>
							</TD>
							<TD align="center" valign="top" nowrap>
								<s:if test="form.readOnlyClasses[#ctr] == 'false'">
									<s:checkbox name="form.leadFlags[%{#ctr}]"/>
								</s:if><s:else>
									<s:hidden name="form.leadFlags[%{#ctr}]"/>
									<s:if test="form.leadFlags[#ctr] == true">
										<IMG src="images/accept.png" border="0" alt="true">
									</s:if><s:else>
										<IMG src="images/cross.png" border="0" alt="false">
									</s:else>
								</s:else>
							</TD>
							<s:if test="#request.responsibilities != null && !#request.responsibilities.isEmpty()">
								<TD align="left" valign="top" nowrap>
									<s:if test="form.readOnlyClasses[#ctr] == 'false'">
										<s:if test="form.responsibilities[#ctr] == '' || form.defaultTeachingResponsibilityId == ''">
											<s:select name="form.responsibilities[%{#ctr}]"
										 		list="#request.responsibilities" listKey="uniqueId" listValue="label"
										 		headerKey="-" headerValue="-"/>
										 </s:if><s:else>
											<s:select name="form.responsibilities[%{#ctr}]"
										 		list="#request.responsibilities" listKey="uniqueId" listValue="label"
										 		/>
										 </s:else>
									</s:if><s:else>
										<s:hidden name="form.responsibilities[%{#ctr}]"/>
										<s:iterator value="#request.responsibilities" var="responsibility">
											<s:if test="#responsibility.uniqueId == form.responsibilities[#ctr]"><s:property value="#responsibility.label"/></s:if>
										</s:iterator>
									</s:else>
								</TD>
							</s:if><s:else>
								<s:hidden name="form.responsibilities[%{#ctr}]"/>
							</s:else>
							<TD align="center" valign="top" nowrap>
								<s:if test="form.readOnlyClasses[#ctr] == 'false' && form.showDisplay[#ctr]">
									<s:checkbox name="form.displayFlags[%{#ctr}]"/>
								</s:if>
								<s:else>
									<s:hidden name="form.displayFlags[%{#ctr}]"/>
									<s:if test="form.showDisplay[#ctr]">
										<s:if test="form.showDisplay[#ctr] == true">
											<IMG src="images/accept.png" border="0" alt="${MSG.titleInstructorDisplayed()}" title="${MSG.titleInstructorDisplayed()}">
										</s:if><s:else>
											<IMG src="images/cross.png" border="0" alt="${MSG.titleInstructorNotDisplayed()}" title="${MSG.titleInstructorNotDisplayed()}">
										</s:else>
									</s:if>
								</s:else>
							</TD>
							<TD align="left" valign="top" nowrap><s:property value="form.times[#ctr]"/></TD>
							<TD align="left" valign="top" nowrap><s:property value="form.rooms[#ctr]" escapeHtml="false"/></TD>
						</TR>
					</s:iterator>
				</TABLE>
			</TD>
		</TR>

<!-- Buttons -->
		<TR>
			<TD colspan="2" valign="middle">
				<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="right">
			
				<s:submit name='op' value='%{#msg.actionUnassignAllInstructorsFromConfig()}' title='%{#msg.titleUnassignAllInstructorsFromConfig()}'
					onclick="return confirmUnassignAll();"/>
					
				<s:submit accesskey='%{#msg.accessUpdateClassInstructorsAssignment()}' name='op' value='%{#msg.actionUpdateClassInstructorsAssignment()}'
							title='%{#msg.titleUpdateClassInstructorsAssignment(#msg.accessUpdateClassInstructorsAssignment())}'/>
							
				<s:if test="form.previousId != null">
					<s:submit accesskey='%{#msg.accessPreviousIO()}' name='op' value='%{#msg.actionPreviousIO()}'
							title='%{#msg.titlePreviousIOWithUpdate(#msg.accessPreviousIO())}'/>
				</s:if>
				<s:if test="form.nextId != null">
					<s:submit accesskey='%{#msg.accessNextIO()}' name='op' value='%{#msg.actionNextIO()}'
							title='%{#msg.titleNextIOWithUpdate(#msg.accessNextIO())}'/>
				</s:if>

				<s:submit accesskey='%{#msg.accessBackToIODetail()}' name='op' value='%{#msg.actionBackToIODetail()}'
							title='%{#msg.titleBackToIODetail(#msg.accessBackToIODetail())}'/>
			</TD>
		</TR>

	</TABLE>
</s:form>
</loc:bundle>