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
<loc:bundle name="CourseMessages"><s:set var="msg" value="#attr.MSG"/> 
<s:form action="classInfo">
	<s:submit name='form.op' value="%{#msg.actionFilterApply()}" style="display:none;" onclick="displayLoading();"/>
	<s:hidden name='op2' value='' id="op2"/>
	<s:set var="model" value="form.model"/>
	<s:set var="clazz" value="#model.clazz"/>
	<s:set var="classId" value="#clazz.classId"/>
	<s:set var="className" value="#clazz.className"/>
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
				<tt:section-title><loc:message name="sectClass"><a href='classDetail.action?cid=${classId}' target='_blank' class='l8' title="${MSG.titleOpenClassDetail(className)}"><s:property value="#className"/></a></loc:message></tt:section-title>
			</tt:section-header>
		</td></tr>
		<tr><td><loc:message name="filterManager"/></td><td><s:property value="#clazz.manager"/></td></tr>
		<s:if test="#clazz.classDivSec != null && !#clazz.classDivSec.isEmpty()">
		<tr><td><loc:message name="propertyExternalId"/></td><td><s:property value="#clazz.classDivSec"/></td></tr>
		</s:if>
		<s:if test="#clazz.enrollment != 0">
			<tr><td><loc:message name="propertyEnrollment"/></td><td><s:property value="#clazz.enrollment"/></td></tr>
		</s:if>
		<tr><td><loc:message name="propertyClassLimit"/></td><td><s:property value="#clazz.classLimit"/></td></tr>
		<tr><td><loc:message name="propertyNumberOfRooms"/></td><td><s:property value="#clazz.numberOfRooms"/></td></tr>
		<tr><td><loc:message name="propertyRoomRatio"/></td><td><s:property value="#clazz.roomRatio"/> ( <loc:message name="propertyMinimumRoomCapacity"/> <s:property value="#clazz.minRoomCapacity"/> )</td></tr>
		<s:if test="#clazz.instructors != null && !#clazz.instructors.isEmpty()">
			<tr><td valign="top"><loc:message name="properyConflictCheckedInstructors"/></td><td>
			<s:property value="%{#clazz.getLeadingInstructorNames('<br>')}" escapeHtml="false"/>
		</td></tr> 
 		</s:if>
 		<s:if test="#model.hasChange == true">
 			<s:if test="#model.classOldAssignment != null">
 				<s:set var="assignment" value="#model.classOldAssignment"/>
				<tr><td><loc:message name="properyAssignedDates"/></td><td><s:property value="#assignment.dateLongNameHtml" escapeHtml="false"/></td></tr>
				<tr><td><loc:message name="filterAssignedTime"/></td><td><s:property value="#assignment.timeLongNameHtml" escapeHtml="false"/></td></tr>
				<s:if test="#assignment.rooms != null && !#assignment.rooms.isEmpty()">
					<tr><td><loc:message name="filterAssignedRoom"/></td><td><s:property value="%{#assignment.getRoomNamesHtml(', ')}" escapeHtml="false"/></td></tr>
				</s:if>
			</s:if>
			<s:if test="#model.selectedAssignment != null">
				<s:set var="assignment" value="#model.selectedAssignment"/>
				<tr><td><loc:message name="properySelectedDates"/></td><td><s:property value="#assignment.dateLongNameHtml" escapeHtml="false"/></td></tr>
				<tr><td><loc:message name="properySelectedTime"/></td><td><s:property value="#assignment.timeLongNameHtml" escapeHtml="false"/></td></tr>
				<s:if test="#assignment.rooms != null && !#assignment.rooms.isEmpty()">
					<tr><td><loc:message name="properySelectedRoom"/></td><td><s:property value="%{#assignment.getRoomNamesHtml(', ')}" escapeHtml="false"/></td></tr>
				</s:if>
			</s:if>
			<tr><td colspan='2'><tt:section-title><br><loc:message name="sectionTitleNewAssignments"/></tt:section-title></td></tr>
			<tr><td colspan='2'><s:property value="#model.changeHtmlTable" escapeHtml="false"/></td></tr>
			<tr><td colspan='2'><loc:message name="toggleDoNotUnassignConflictingClasses"/> <s:checkbox name="form.keepConflictingAssignments" onchange="document.getElementById('op2').value='Apply'; submit();"/></td></tr>
			<s:if test="#model.canAssign == true">
				<tr><td colspan='2' align="right">
					<s:submit name='form.op' value="%{#msg.actionClassAssign()}" onclick="return confirmAssign();"/>
				</td></tr>
			</s:if>
		</s:if>
		<s:if test="#model.hasChange == false">
			<s:if test="#model.classAssignment != null">
				<s:set var="assignment" value="#model.classAssignment"/>
				<tr><td><loc:message name="propertyDate"/></td><td><s:property value="#assignment.dateLongNameHtml" escapeHtml="false"/></td></tr>
				<tr><td><loc:message name="propertyTime"/></td><td><s:property value="#assignment.timeLongNameHtml" escapeHtml="false"/></td></tr>
				<s:if test="#assignment.rooms != null && !#assignment.rooms.isEmpty()">
					<tr><td><loc:message name="propertyRoom"/></td><td><s:property value="%{#assignment.getRoomNamesHtml(', ')}" escapeHtml="false"/></td></tr>
				</s:if>
			</s:if>
		</s:if>
		<tr><td colspan='2'><tt:section-title><br><loc:message name="sectionTitleStudentConflicts"/></tt:section-title></td></tr>
		<tr><td colspan='2'><s:property value="#model.studentConflictTable" escapeHtml="false"/></td></tr>
		<s:if test="#model.useRealStudents != true">
			<tr><td colspan='2' align="center" onClick="displayLoading(); document.location='classInfo.action?op=Type&type=actual';" align='center' class='unitime-ClassAssignmentLink'><loc:message name="studentConflictsShowingSolutionConflicts"/></td></tr>
		</s:if>
		<s:if test="#model.useRealStudents == true">
			<tr><td colspan='2' align="center" onClick="displayLoading(); document.location='classInfo.action?op=Type&type=solution';" align='center' class='unitime-ClassAssignmentLink'><loc:message name="studentConflictsShowingActualConflicts"/></td></tr>
		</s:if>
		<s:if test="#model.showDates == true">
			<tr><td colspan='2'><tt:section-title>
				<br><loc:message name="sectionTitleAvailableDatesForClass"><s:property value="#clazz.className"/></loc:message> &nbsp;&nbsp;
			</tt:section-title></td></tr>
			<tr><td colspan='2'>
			<s:property value="#model.datesTable" escapeHtml="false"/>
			</td></tr>
		</s:if>
		<tr><td colspan='2'><tt:section-title>
			<br><loc:message name="sectionTitleAvailableTimesForClass"><s:property value="#clazz.className"/></loc:message> &nbsp;&nbsp;
		</tt:section-title></td></tr>
		<s:if test="#model.times != null && !#model.times.isEmpty()">
			<tr><td colspan='2'>
			<s:property value="#model.timesTable" escapeHtml="false"/>
			</td></tr>
		</s:if>
		<s:else>
			<tr><td colspan='2'><i><loc:message name="messageNoTimesAvailable"/></i></td></tr>
		</s:else>
		<s:if test="#model.selectedAssignment != null && #clazz.numberOfRooms > 0">
			<tr><td colspan='2'><tt:section-title>
				<s:set var="classLimit" value="#clazz.classLimit"/>
				<br><loc:message name="sectionTitleAvailableRoomsForClass"><s:property value="#clazz.className"/></loc:message> &nbsp;&nbsp;
				( <loc:message name="messageSelectedSize"></loc:message> <span id='roomCapacityCounter'>
					<s:if test="#model.roomSize < #classLimit">
						<font color='red'><s:property value="#model.roomSize"/></font>
					</s:if>
					<s:else>
						<s:property value="#model.roomSize"/>
					</s:else>
					</span> <loc:message name="messageSelectedSizeOf"/> <s:property value="#clazz.minRoomCapacity"/> ) 
			</tt:section-title></td></tr>
			<tr><td colspan='2'>
				<table class='unitime-Table' style='width:100%;'>
					<tr><td valign="top" nowrap>
						<loc:message name="properyRoomSize"/>
							<s:textfield name="form.minRoomSize" size="5" maxlength="5"/> - <s:textfield name="form.maxRoomSize" size="5" maxlength="5"/>
					</td><td valign="top" nowrap>
						<loc:message name="properyRoomFilter"/>
							<s:textfield name="form.roomFilter" size="15" maxlength="100"/>
					</td><td valign="top" nowrap>
						<loc:message name="properyRoomAllowConflicts"/>
							<s:checkbox name="form.allowRoomConflict"/>
					</td><td valign="top" nowrap>
						<loc:message name="propertyRooms"/>
							<s:select name="form.roomBase" list="form.roomBases" listKey="value" listValue="label"/>
					</td><td valign="top" nowrap>
						<loc:message name="propertyRoomOrder"/>
							<s:select name="form.roomOrder" list="form.roomOrders"/>
					</td><td align="right" valign="top" nowrap>
						<s:submit name='form.op' value="%{#msg.actionFilterApply()}" onclick="displayLoading();"/>
					</td></tr>
				</table>
			</td></tr>
			<tr><td colspan='2'>
				<table class='unitime-Table' style='width:100%;'><tr>
					<td nowrap><loc:message name="propertyRoomTypes"/></td>
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
					<td nowrap><loc:message name="propertyRoomGroups"/></td>
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
				<tr><td colspan='2'><i><loc:message name="messageNoMatchingRoomFound"/></i></td></tr>
			</s:if><s:else>
				<tr><td colspan='2'>
					<s:property value="%{#model.getRoomTable()}" escapeHtml="false"/>
				</td></tr>
			</s:else>
		</s:if>
		<tr><td colspan='2'><tt:section-title><br></tt:section-title></td></tr>
	</table>
	<s:if test="form.message != null && !form.message.isEmpty()">
		<script type="text/javascript">
			alert('${form.message}');
		</script>
	</s:if>
</s:form>
</loc:bundle>