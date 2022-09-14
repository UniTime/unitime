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
<s:form action="examInfo">
	<s:submit name='form.op' value="%{#msg.buttonApply()}" style="display:none;" onclick="displayLoading();"/>
	<s:hidden name="form.depth"/>
	<s:hidden name="form.timeout"/>
	<s:set var="model" value="form.model"/>
	<s:set var="exam" value="#model.exam"/>
	<s:set var="examId" value="#exam.examId"/>
	<s:set var="examName" value="#exam.examName"/>
	<s:if test="form.op == 'Close'">
		<script type="text/javascript">
			parent.hideGwtDialog();
			parent.refreshPage();
		</script>
	</s:if>
	<script type="text/javascript">
		if (parent) parent.hideGwtHint();
	</script>
	<tt:confirm name="confirmAssign"><s:property value="#model.assignConfirm"/></tt:confirm>
	<table class="unitime-MainTable">
		<tr><td colspan='2'>
			<tt:section-header>
				<tt:section-title><loc:message name="sectExamination"><a href='examDetail.action?examId=${examId}' target='_blank' class='l8' title='${MSG.hintOpenExaminationDetail(examName)}'><s:property value="#examName"/></a></loc:message></tt:section-title>
			</tt:section-header>
		</td></tr>
		<tr><td><loc:message name="propOwners"/></td><td><s:property value="%{#exam.getSectionName('<br>')}" escapeHtml="false"/></td></tr>
		<tr><td><loc:message name="propExamType"/></td><td><s:property value="#exam.examTypeLabel"/></td></tr>
		<tr><td><loc:message name="propExamLength"></loc:message> </td><td><loc:message name="examLengthInMinutes"><s:property value="#exam.length"/></loc:message></td></tr>
		<tr><td><loc:message name="propExamSize"/></td><td><s:property value="#exam.nrStudents"/></td></tr>
		<tr><td><loc:message name="propExamSeatingType"/></td><td><s:property value="#exam.seatingTypeLabel"/></td></tr>
		<tr><td><loc:message name="propExamMaxRooms"/></td><td><s:property value="#exam.maxRooms"/></td></tr>
		<s:if test="#exam.instructors != null && !#exam.instructors.isEmpty()">
			<tr><td valign="top"><loc:message name="propExamInstructors"/></td><td><s:property value="%{#exam.getInstructorName('<br>')}" escapeHtml="false"/></td></tr>
		</s:if>
		<s:if test="#model.change != null && !#model.change.isEmpty()">
			<s:if test="#model.examOldAssignment != null && #model.examOldAssignment.period != null">
				<s:set var="assignment" value="#model.examOldAssignment"/>
				<tr><td><loc:message name="propAssignedPeriod"/></td><td><s:property value="#assignment.periodNameWithPref" escapeHtml="false"/></td></tr>
				<s:if test="#assignment.rooms != null && !#assignment.rooms.isEmpty()">
					<tr><td><loc:message name="propAssignedRoom"/></td><td><s:property value="%{#assignment.getRoomsNameWithPref(', ')}" escapeHtml="false"/></td></tr>
				</s:if>
			</s:if>
			<s:if test="#model.selectedAssignment != null">
				<s:set var="assignment" value="#model.selectedAssignment"/>
				<tr><td><loc:message name="propSelectedPeriod"/></td><td><s:property value="#assignment.periodNameWithPref" escapeHtml="false"/></td></tr>
				<s:if test="#assignment.rooms != null && !#assignment.rooms.isEmpty()">
					<tr><td><loc:message name="propSelectedRoom"/></td><td><s:property value="%{#assignment.getRoomsNameWithPref(', ')}" escapeHtml="false"/></td></tr>
				</s:if>
			</s:if>
			<s:set var="change" value="#model.change"/>
			<tr><td colspan='2'><tt:section-title><br><loc:message name="sectNewAssignments"/></tt:section-title></td></tr>
			<tr><td colspan='2'><s:property value="#change.htmlTable" escapeHtml="false"/></td></tr>
			<s:if test="#model.canAssign == true">
				<tr><td colspan='2' align="right">
					<s:submit name='form.op' value="%{#msg.buttonAssign()}" onclick="return confirmAssign();"/>
				</td></tr>
			</s:if>
			<s:if test="#model.selectedAssignment != null">
				<s:if test="#assignment.nrDistributionConflicts > 0 ">
					<tr><td colspan='2'><tt:section-title><br><loc:message name="sectViolatedDistributionPreferencesForExam"><s:property value="#exam.examName"/> (<s:property value="#assignment.periodAbbreviation" escapeHtml="false"/>)</loc:message></tt:section-title></td></tr>
					<tr><td colspan='2'><s:property value="#assignment.distributionInfoConflictTable" escapeHtml="false"/></td></tr>
				</s:if>
				<s:if test="#assignment.hasConflicts == true">
					<tr><td colspan='2'><tt:section-title><br><loc:message name="sectStudentConflictsForExam"><s:property value="#exam.examName"/> (<s:property value="#assignment.periodAbbreviation" escapeHtml="false"/>)</loc:message></tt:section-title></td></tr>
					<tr><td colspan='2'><s:property value="#assignment.conflictInfoTable" escapeHtml="false"/></td></tr>
				</s:if>
				<s:if test="#assignment.hasInstructorConflicts == true">
					<tr><td colspan='2'><tt:section-title><br><loc:message name="sectInstructorConflictsForExam"><s:property value="#exam.examName"/> (<s:property value="#assignment.periodAbbreviation" escapeHtml="false"/>)</loc:message></tt:section-title></td></tr>
					<tr><td colspan='2'><s:property value="#assignment.instructorConflictInfoTable" escapeHtml="false"/></td></tr>
				</s:if>
			</s:if>
		</s:if>
		<s:if test="#model.change == null && #model.examAssignment != null">
			<s:set var="assignment" value="#model.examAssignment"/>
				<tr><td><loc:message name="propPeriod"/></td><td><s:property value="#assignment.periodNameWithPref" escapeHtml="false"/></td></tr>
				<s:if test="#assignment.rooms != null && !#assignment.rooms.isEmpty()">
					<tr><td><loc:message name="propRoom"/></td><td><s:property value="%{#assignment.getRoomsNameWithPref(', ')}" escapeHtml="false"/></td></tr>
				</s:if>
				<s:if test="#assignment.nrDistributionConflicts > 0">
					<tr><td colspan='2'><tt:section-title><br><loc:message name="sectViolatedDistributionPreferences"/></tt:section-title></td></tr>
					<tr><td colspan='2'><s:property value="#assignment.distributionConflictTable" escapeHtml="false"/></td></tr>
				</s:if>
				<s:if test="#assignment.hasConflicts == true">
					<tr><td colspan='2'><tt:section-title><br><loc:message name="sectStudentConflicts"/></tt:section-title></td></tr>
					<tr><td colspan='2'><s:property value="#assignment.conflictTable" escapeHtml="false"/></td></tr>
				</s:if>
				<s:if test="#assignment.hasInstructorConflicts == true">
					<tr><td colspan='2'><tt:section-title><br><loc:message name="sectInstructorConflicts"/></tt:section-title></td></tr>
					<tr><td colspan='2'><s:property value="#assignment.instructorConflictTable" escapeHtml="false"/></td></tr>
				</s:if>
		</s:if>
		<s:if test="#model.periods != null && !#model.periods.isEmpty()">
			<tr><td colspan='2'><br><table style="width: 100%;">
				<s:property value="#model.periodsTable" escapeHtml="false"/>
			</table></td></tr>
		</s:if>
		<s:if test="#model.selectedAssignment != null && #exam.maxRooms > 0">
			<tr><td colspan='2'><tt:section-title>
				<s:set var="nrStudents" value="#exam.nrStudents"/>
				<br><loc:message name="sectAvailableRoomsForExam"><s:property value="#exam.examName"/></loc:message> &nbsp;&nbsp;
				( <loc:message name="hintSelectedSize"/> <span id='roomCapacityCounter'>
					<s:if test="#model.roomSize < #nrStudents">
						<font color='red'><s:property value="#model.roomSize"/></font>
					</s:if>
					<s:else>
						<s:property value="#model.roomSize"/>
					</s:else>
					</span> <loc:message name="hintRoomSizeOfNbrStudents"/> <s:property value="#exam.nrStudents"/> ) 
			</tt:section-title></td></tr>
			<tr><td colspan='2'>
				<table style="width:100%;">
					<tr><td>
						<loc:message name="filterRoomSize"/>
							<s:textfield name="form.minRoomSize" size="5" maxlength="5"/> - <s:textfield name="form.maxRoomSize" size="5" maxlength="5"/>
					</td><td>
						<loc:message name="filterRoomTextFilter"/>
							<s:textfield name="form.roomFilter" size="25" maxlength="100"/>
					</td><td>
						<loc:message name="filterAllowForConflicts"/>
							<s:checkbox name="form.allowRoomConflict"/>
					</td><td>
						<loc:message name="filterRoomOrder"/>
							<s:select name="form.roomOrder" list="form.roomOrders" listKey="value" listValue="label"/>
					</td><td align="right">
						<s:submit name='form.op' value="%{#msg.buttonApply()}" onclick="displayLoading();"/>
					</td></tr>
				</table>
			</td></tr>
			<tr><td colspan='2'>
				<table style="width:100%;"><tr>
					<td nowrap><loc:message name="filterRoomTypes"/></td>
					<s:iterator value="form.allRoomTypes" var="rf" status="rfIdx">
						<td nowrap>
							<s:checkboxlist name="form.roomTypes" list="#{#rf.uniqueId:''}"/>
							<s:property value="#rf.label"/>&nbsp;&nbsp;&nbsp;
						</td>
						<s:if test="(#rfIdx.index % 3) == 2">
							<s:property value="'</tr><tr><td></td>'" escapeHtml="false"/>
						</s:if>
					</s:iterator>
				</tr><tr>
					<td nowrap><loc:message name="filterRoomGroups"/></td>
					<s:iterator value="form.allRoomGroups" var="rf" status="rfIdx">
						<td nowrap>
							<s:checkboxlist name="form.roomGroups" list="#{#rf.uniqueId:''}"/>
							<s:property value="#rf.name"/>&nbsp;&nbsp;&nbsp;
						</td>
						<s:if test="(#rfIdx.index % 3) == 2">
							<s:property value="'</tr><tr><td></td>'" escapeHtml="false"/>
						</s:if>
					</s:iterator>
				</tr>
				<s:iterator value="form.roomFeatureTypes" var="ft">
					<tr>
						<td nowrap><s:property value="#ft.label"/>:</td>
						<s:iterator value="%{form.getAllRoomFeatures(#ft.uniqueId)}" var="rf" status="rfIdx">
							<td nowrap>
								<s:checkboxlist name="form.roomFeatures" list="#{#rf.uniqueId:''}"/>
								<s:property value="#rf.label"/>&nbsp;&nbsp;&nbsp;
							</td>
							<s:if test="(#rfIdx.index % 3) == 2">
								<s:property value="'</tr><tr><td></td>'" escapeHtml="false"/>
							</s:if>
						</s:iterator>
					</tr>
				</s:iterator>
			</table></td></tr>
			<s:if test="#model.roomTable == null">
				<tr><td colspan='2'><i><loc:message name="infoNoMatchingRoom"/></i></td></tr>
			</s:if>
			<s:else>
				<tr><td colspan='2'>
					<s:property value="%{#model.getRoomTable()}" escapeHtml="false"/>
				</td></tr>
			</s:else>
		</s:if>
		<s:if test="#model.canComputeSuggestions == true">
			<tr><td colspan='2'><tt:section-title><br><s:checkbox name="form.computeSuggestions" onclick="displayLoading();submit();"/> <loc:message name="sectSuggestions"/></tt:section-title></td></tr>
			<s:if test="form.computeSuggestions == true">
				<tr><td colspan='2'>
					<table style="width:100%;">
						<tr><td>
							<loc:message name="filterTextFilter"/>
								<s:textfield name="form.filter" size="50" maxlength="100"/>
						</td><td>
							<loc:message name="filterMaxNumberOfSuggestions"/>
								<s:textfield name="form.limit" size="5" maxlength="5"/>
						</td><td align="right">
						</td><td align="right">
							<s:submit name='form.op' value="%{#msg.buttonApply()}" onclick="displayLoading();"/>
							<s:submit name='form.op' value="%{#msg.buttonSearchDeeper()}" onclick="displayLoading();"/>
							<s:if test="#model.suggestionsTimeoutReached == true">
								<s:submit name='form.op' value="%{#msg.buttonSearchLonger()}" onclick="displayLoading();"/>
							</s:if>
						</td></tr>
					</table>
				</td></tr>
				<s:if test="#model.suggestions != null">
					<tr><td colspan='2'>
						<s:property value="%{#model.getSuggestionTable()}" escapeHtml="false"/>
					</td></tr>
				</s:if>
			</s:if>
		</s:if>
		<s:if test="#model.cbs != null">
			<s:property value="%{printCbsHeader()}" escapeHtml="false"/>
			<tr><td colspan='2'><tt:section-title><br><loc:message name="sectConflictBasedStatistics"/></tt:section-title></td></tr>
			<tr><td colspan='2'>
				<font size='2'>
					<s:property value="%{printCbs()}" escapeHtml="false"/>
				</font>
			</td></tr>
		</s:if>
	</table>
	<s:if test="form.message != null && !form.message.isEmpty()">
		<script type="text/javascript">
			alert('${form.message}');
		</script>
	</s:if>
</s:form>
</loc:bundle>
