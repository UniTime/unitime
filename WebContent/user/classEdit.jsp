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
<%@ page import="java.util.Enumeration"%>
<%@ page import="java.util.Vector"%>
<%@ page import="org.unitime.timetable.model.DepartmentalInstructor"%>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="tt" uri="http://www.unitime.org/tags-custom" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="loc" uri="http://www.unitime.org/tags-localization" %>
<loc:bundle name="CourseMessages"><s:set var="msg" value="#attr.MSG"/>
<s:form action="classEdit">
<tt:session-context/>
<script type="text/javascript">
	function instructorChanged(idx, source) {
		var hadPreferences = false;
		var instrHasPrefObj = document.getElementById('instrHasPref'+idx);
		if (instrHasPrefObj!=null && instrHasPrefObj.value=='true')
			hadPreferences = true;
		var instructorId = '-';
		var instructorsObj = document.getElementById('instructors'+idx);
		if (instructorsObj!=null && instructorsObj.selectedIndex>=0)
			instructorId = instructorsObj.options[instructorsObj.selectedIndex].value;
		var hasPreferences = false;
		<%
		Vector instIdWithPrefs = (Vector)request.getAttribute(DepartmentalInstructor.INSTR_HAS_PREF_ATTR_NAME);
		if (instIdWithPrefs!=null)
			for (Enumeration e=instIdWithPrefs.elements();e.hasMoreElements();) {
				Long instrId = (Long)e.nextElement();
				out.println("if (instructorId=='"+instrId+"') hasPreferences=true;");
			}
		%>
		var instrLeadObj = document.getElementById('instrLead'+idx);
		var op2Obj = document.getElementById('op2');
		var isLead = false;
		if (instrLeadObj!=null)
			isLead = instrLeadObj.checked;
		if (instructorId=='-' && instrLeadObj!=null) {
			instrLeadObj.checked=false; isLead=false;
			if (source.id=='instrLead'+idx) {
				alert('${MSG.alertSelectAnInstructor()}');
				if (instructorsObj!=null) instructorsObj.focus();
			}
		}
		if (isLead && hasPreferences) {
			if (op2Obj!=null && <%=JavascriptFunctions.getInheritInstructorPreferencesCondition(sessionContext)%>) {
				op2Obj.value='updatePref';
				document.forms[0].submit();
			}
		} else if (hadPreferences) {
			if (op2Obj!=null && <%=JavascriptFunctions.getCancelInheritInstructorPreferencesCondition(sessionContext)%>) {
				op2Obj.value='updatePref';
				document.forms[0].submit();
			}
		}
	}
	function datePatternChanged(){			
		var op2Obj = document.getElementById('op2');
		if (op2Obj!=null) {
			op2Obj.value='updateDatePattern';
			document.forms[0].submit();
		}			
	}
</script>
	<s:hidden name="form.classId"/>
	<s:hidden name="form.defaultTeachingResponsibilityId"/>	
	<table class="unitime-MainTable">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<s:property value="form.className"/>
					</tt:section-title>
					<s:submit accesskey='%{#msg.accessUpdatePreferences()}' name='op' value='%{#msg.actionUpdatePreferences()}'
						title='%{#msg.titleUpdatePreferences(#msg.accessUpdatePreferences())}'/>
					<sec:authorize access="hasPermission(#form.classId, 'Class_', 'ClassEditClearPreferences')">
						<s:submit accesskey='%{#msg.accessClearClassPreferences()}' name='op' value='%{#msg.actionClearClassPreferences()}'
							title='%{#msg.titleClearClassPreferences(#msg.accessClearClassPreferences())}'/>
					</sec:authorize>
					<s:if test="form.previousId != null">
						<s:submit accesskey='%{#msg.accessPreviousClass()}' name='op' value='%{#msg.actionPreviousClass()}'
							title='%{#msg.titlePreviousClassWithUpdate(#msg.accessPreviousClass())}'/>
					</s:if>
					<s:if test="form.nextId != null">
						<s:submit accesskey='%{#msg.accessNextClass()}' name='op' value='%{#msg.actionNextClass()}'
							title='%{#msg.titleNextClassWithUpdate(#msg.accessNextClass())}'/>
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

		<TR>
			<TD><loc:message name="filterManager"/></TD>
			<TD><s:property value="form.managingDeptLabel"/></TD>
		</TR>

		<s:if test="form.parentClassName != \"-\"">
			<TR>
				<TD><loc:message name="propertyParentClass"/></TD>
				<TD><s:property value="form.parentClassName"/></TD>
			</TR>
		</s:if>
		
		<s:hidden name="form.classSuffix"/>
		<s:if test="form.classSuffix != null && !form.classSuffix.isEmpty()">
			<TR>
				<TD><loc:message name="propertyExternalId"/></TD>
				<TD><s:property value="form.classSuffix"/></TD>
			</TR>
		</s:if>
		
		<s:hidden name="form.nbrRooms"/>
		<s:hidden name="form.enrollment"/>
		<s:hidden name="form.snapshotLimit"/>
		<s:hidden name="form.expectedCapacity"/>
		<s:hidden name="form.maxExpectedCapacity"/>
		<s:hidden name="form.roomRatio"/>
		<s:hidden name="form.lms"/>
		<s:hidden name="form.fundingDept"/>
		<TR>
			<TD><loc:message name="propertyEnrollment"/></TD>
			<TD><s:property value="form.enrollment"/></TD>
		</TR>		
		<s:if test="form.nbrRooms > 0 && form.expectedCapacity == form.maxExpectedCapacity">
			<TR>
				<TD><loc:message name="propertyClassLimit"/></TD>
				<TD><s:property value="form.expectedCapacity"/></TD>
			</TR>
		</s:if>
		<s:if test="form.nbrRooms > 0 && form.expectedCapacity != form.maxExpectedCapacity">
			<TR>
				<TD><loc:message name="propertyMinimumClassLimit"/></TD>
				<TD><s:property value="form.expectedCapacity"/></TD>
			</TR>
			<TR>
				<TD><loc:message name="propertyMaximumClassLimit"/></TD>
				<TD><s:property value="form.maxExpectedCapacity"/></TD>
			</TR>
		</s:if>
		<s:if test="form.nbrRooms != 0 && form.snapshotLimit != null">
			<TR>
				<TD><loc:message name="propertySnapshotLimit"/></TD>
				<TD><s:property value="form.snapshotLimit"/></TD>
			</TR>
		</s:if>

		<TR>
			<TD><loc:message name="propertyNumberOfRooms"/></TD>
			<TD><s:property value="form.nbrRooms"/></TD>
		</TR>
		
		<s:if test="form.nbrRooms != 0"> 
			<TR>
				<TD><loc:message name="propertyRoomRatio"/></TD>
				<TD>
					<s:property value="form.roomRatio"/>
					&nbsp;&nbsp;&nbsp;&nbsp; ( <loc:message name="propertyMinimumRoomCapacity"/> <s:property value="form.minRoomLimit"/> )
				</TD>
			</TR>
		</s:if>
		
		<s:if test="form.lms != null && !form.lms.isEmpty()">
			<TR>
				<TD valign="top"><loc:message name="propertyLms"/></TD>
				<TD>
					<s:property value="form.lms" escapeHtml="false"/>
				</TD>
			</TR>
		</s:if>
		
		<s:if test="form.fundingDept != null">
			<TR>
				<TD valign="top"><loc:message name="propertyFundingDept"/></TD>
				<TD>
					<s:property value="form.fundingDept" escapeHtml="false"/>
				</TD>
			</TR>
		</s:if>
		

		<TR>
			<TD><loc:message name="propertyDatePattern"/></TD>
			<TD>
				<s:hidden name="form.datePatternEditable"/>
				<s:if test="form.datePatternEditable == true">
					<s:select name="form.datePattern" list="#request.datePatternList" listKey="id" listValue="value"
						style="min-width:200px;" onchange="datePatternChanged();"/>
					<img style="cursor: pointer;" src="images/calendar.png" border="0" onclick="showGwtDialog('Preview of '+classEdit_form_datePattern.options[classEdit_form_datePattern.selectedIndex].text, 'dispDatePattern.action?id='+classEdit_form_datePattern.value+'&classId='+classEdit_form_classId.value,'840','520');">
				</s:if>
				<s:else>
					<s:hidden name="form.datePattern"/>
					<s:iterator value="#request.datePatternList" var="dp">
						<s:if test="#dp.id == form.datePattern">
							<s:property value="#dp.value"/>
							<img style="cursor: pointer;" src="images/calendar.png" border="0" onclick="showGwtDialog('${MSG.sectPreviewOfDatePattern(dp.value)}', 'dispDatePattern.action?id=${dp.id}&classId=${form.classId}','840','520');">
						</s:if>
					</s:iterator>
				</s:else>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propertyDisplayInstructors"/></TD>
			<TD><s:checkbox name="form.displayInstructor"/></TD>
		</TR>

		<TR>
			<TD><loc:message name="propertyEnabledForStudentScheduling"/></TD>
			<TD><s:checkbox name="form.enabledForStudentScheduling"/></TD>
		</TR>

		<TR>
			<TD valign="top"><loc:message name="propertyStudentScheduleNote"/></TD>
			<TD><s:textarea name="form.schedulePrintNote" cols="70" rows="4"/></TD>
		</TR>
		
		
		<s:if test="form.accommodation != null">
			<TR>
				<TD valign="top"><loc:message name="propertyAccommodations"/></TD>
				<TD><s:property value="form.accommodation" escapeHtml="false"/></TD>
			</TR>
		</s:if>

		<s:if test="#request.assignmentInfo != null">
			<TR>
				<TD colspan="2" align="left" style="padding-top: 20px;">
					<tt:section-title><loc:message name="sectionTitleTimetable"/></tt:section-title>
				</TD>
			</TR>
			<s:property value="#request.assignmentInfo" escapeHtml="false"/>
		</s:if>

<!-- Requests / Notes -->
		<TR>
			<TD colspan="2" align="left" style="padding-top:20px;">
				<tt:section-title><loc:message name="sectionTitleNotesToScheduleManager"/></tt:section-title>
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="left">
				<s:textarea name="form.notes" rows="3" cols="80"/>
			</TD>
		</TR>

<!-- Instructors -->
		<TR>
			<TD valign="middle" colspan='2' style="padding-top:20px;">
			<A id='Instructors'></A>
				<tt:section-header title="${MSG.sectionTitleInstructors()}">
					<s:submit accesskey='%{#msg.accessAddInstructor()}' name='op' value='%{#msg.actionAddInstructor()}'
							title='%{#msg.titleAddInstructor(#msg.accessAddInstructor())}'/>
				</tt:section-header>
			</TD>
		</TR>
		<TR>
			<TD colspan="2" align="left">
				<INPUT type="hidden" id="instrListTypeAction" name="instrListTypeAction" value="">				
				<TABLE>
					<TR>
						<TD><I><loc:message name="columnInstructorName"/></I></TD>
						<TD>&nbsp;<I><loc:message name="columnInstructorShare"/> </I>&nbsp;</TD>
						<TD>&nbsp;<I><loc:message name="columnInstructorCheckConflicts"/> </I>&nbsp;</TD>
						<s:if test="#request.responsibilities != null && !#request.responsibilities.isEmpty()">
							<TD>&nbsp;<I><loc:message name="columnTeachingResponsibility"/> </I>&nbsp;</TD>
						</s:if>
						<TD>&nbsp;</TD>
					</TR>
					
					<s:hidden name="op2" value="" id="op2"/>
					<s:iterator value="form.instructors" var="instructor" status="stat">
						<TR>
							<TD align="left" nowrap>
								<s:select name="form.instructors[%{#stat.index}]"
									list="#request.instructorsList" listKey="value" listValue="label"
									headerKey="-" headerValue="-"
									style="width:250px;" id="instructors%{#stat.index}" onchange="instructorChanged(%{#stat.index}, this);"
									/>
							</TD>
							<s:hidden name="form.instrHasPref[%{#stat.index}]" id="instrHasPref%{#stat.index}" value="%{form.getInstrHasPref(#stat.index)}"/>
							<TD nowrap align="center">
								<s:textfield name="form.instrPctShare[%{#stat.index}]" size="3" maxlength="3"/>
							</TD>
							<TD nowrap align="center">
								<s:checkbox name="form.instrLead[%{#stat.index}]" id="instrLead%{#stat.index}"
									onclick="instructorChanged(%{#stat.index}, this);"/>
							</TD>
							<s:if test="#request.responsibilities != null && !#request.responsibilities.isEmpty()">
								<TD>
									<s:if test="form.defaultTeachingResponsibilityId == '' || form.instrResponsibility[#stat.index] == ''">
										<s:select name="form.instrResponsibility[%{#stat.index}]"
											list="#request.responsibilities" listKey="uniqueId" listValue="label"
											headerKey="-" headerValue="-"/>
									</s:if>
									<s:else>
										<s:select name="form.instrResponsibility[%{#stat.index}]"
											list="#request.responsibilities" listKey="uniqueId" listValue="label"
										/>
									</s:else>
								</TD>
							</s:if>
							<s:else>
								<s:hidden name="form.instrResponsibility[%{#stat.index}]"/>
							</s:else>
							<TD nowrap>
								<s:submit name='op' value='%{#msg.actionRemoveInstructor()}'
									title='%{#msg.titleRemoveInstructor()}'
									onclick="javascript: doDel('instructor', %{#stat.index});"/>
							</TD>
						</TR>
				   	</s:iterator>
				</TABLE>
			</TD>
		</TR>

<!-- Preferences -->
		<s:if test="form.nbrRooms == 0">
			<s:include value="preferencesEdit2.jspf">
				<s:param name="frmName" value="'classEdit'"/>
				<s:param name="distPref" value="false"/>
				<s:param name="periodPref" value="false"/>
				<s:param name="bldgPref" value="false"/>
				<s:param name="roomFeaturePref" value="false"/>
				<s:param name="roomGroupPref" value="false"/>
			</s:include>
		</s:if>
		<s:if test="form.nbrRooms != 0">
			<s:if test="form.unlimitedEnroll == true">
				<s:include value="preferencesEdit2.jspf">
					<s:param name="frmName" value="'classEdit'"/>
					<s:param name="distPref" value="false"/>
					<s:param name="periodPref" value="false"/>
					<s:param name="bldgPref" value="false"/>
					<s:param name="roomFeaturePref" value="false"/>
					<s:param name="roomGroupPref" value="false"/>
				</s:include>
			</s:if>
			<s:if test="form.unlimitedEnroll != true">
				<s:include value="preferencesEdit2.jspf">
					<s:param name="frmName" value="'classEdit'"/>
					<s:param name="distPref" value="false"/>
					<s:param name="periodPref" value="false"/>
				</s:include>
			</s:if>
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
					<sec:authorize access="hasPermission(#form.classId, 'Class_', 'ClassEditClearPreferences')">
						<s:submit accesskey='%{#msg.accessClearClassPreferences()}' name='op' value='%{#msg.actionClearClassPreferences()}'
							title='%{#msg.titleClearClassPreferences(#msg.accessClearClassPreferences())}'/>
					</sec:authorize>
					<s:if test="form.previousId != null">
						<s:submit accesskey='%{#msg.accessPreviousClass()}' name='op' value='%{#msg.actionPreviousClass()}'
							title='%{#msg.titlePreviousClassWithUpdate(#msg.accessPreviousClass())}'/>
					</s:if>
					<s:if test="form.nextId != null">
						<s:submit accesskey='%{#msg.accessNextClass()}' name='op' value='%{#msg.actionNextClass()}'
							title='%{#msg.titleNextClassWithUpdate(#msg.accessNextClass())}'/>
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
</loc:bundle>