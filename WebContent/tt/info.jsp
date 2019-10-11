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
<%@ page language="java" autoFlush="true"%>
<%@ page import="org.unitime.timetable.form.ClassInfoForm"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.unitime.org/tags-localization" prefix="loc" %>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>
<tiles:importAttribute />
<%
	// Get Form 
	String frmName = "classInfoForm";
	ClassInfoForm frm = (ClassInfoForm) request.getAttribute(frmName);

%>
<html:form action="/classInfo">
	<loc:bundle name="CourseMessages">
	<html:submit onclick="displayLoading();" property="op" style="display:none;"><loc:message name="actionFilterApply"/></html:submit>
	<input type='hidden' name='op2' value=''>
	<bean:define id="model" name="classInfoForm" property="model"/>
	<bean:define id="clazz" name="model" property="clazz"/>
	<bean:define id="classId" name="clazz" property="classId"/>
	<bean:define id="className" name="clazz" property="className" type="String"/>
	<logic:equal name="classInfoForm" property="op" value="Close">
		<script language="JavaScript" type="text/javascript">
			parent.hideGwtDialog();
			parent.refreshPage();
		</script>
	</logic:equal>
	<script language="JavaScript" type="text/javascript">
		if (parent) parent.hideGwtHint();
	</script>
	<tt:confirm name="confirmAssign"><bean:write name="model" property="assignConfirm"/></tt:confirm>
	<table border='0' width='100%'>
		<tr><td colspan='2'>
			<tt:section-header>
				<tt:section-title>Class <a href='classDetail.do?cid=<%=classId%>' target='_blank' class='l8' title="<%=MSG.titleOpenClassDetail(className)%>"><bean:write name="clazz" property="className"/></a></tt:section-title>
			</tt:section-header>
		</td></tr>
		<tr><td><loc:message name="filterManager"/></td><td><bean:write name="clazz" property="manager"/></td></tr>
		<logic:notEmpty name="clazz" property="classDivSec">
		<tr><td><loc:message name="propertyExternalId"/></td><td><bean:write name="clazz" property="classDivSec"/></td></tr>
		</logic:notEmpty>
		<logic:notEqual name="clazz" property="enrollment" value="0">
			<tr><td><loc:message name="propertyEnrollment"/></td><td><bean:write name="clazz" property="enrollment"/></td></tr>
		</logic:notEqual>
		<tr><td><loc:message name="propertyClassLimit"/></td><td><bean:write name="clazz" property="classLimit"/></td></tr>
		<tr><td><loc:message name="propertyNumberOfRooms"/></td><td><bean:write name="clazz" property="numberOfRooms"/></td></tr>
		<tr><td><loc:message name="propertyRoomRatio"/></td><td><bean:write name="clazz" property="roomRatio"/> ( <loc:message name="propertyMinimumRoomCapacity"/> <bean:write name="clazz" property="minRoomCapacity"/> )</td></tr>
		<logic:notEmpty name="clazz" property="instructors">
			<tr><td valign="top"><loc:message name="properyConflictCheckedInstructors"/></td><td>
			<%= frm.getModel().getClazz().getLeadingInstructorNames("<br>") %> 
		</td></tr> 
 		</logic:notEmpty>
		<logic:equal name="model" property="hasChange" value="true">
			<logic:notEmpty name="model" property="classOldAssignment">
				<bean:define id="assignment" name="model" property="classOldAssignment"/>
				<tr><td><loc:message name="properyAssignedDates"/></td><td><bean:write name="assignment" property="dateLongNameHtml" filter="false"/></td></tr>
				<tr><td><loc:message name="filterAssignedTime"/></td><td><bean:write name="assignment" property="timeLongNameHtml" filter="false"/></td></tr>
				<logic:notEmpty name="assignment" property="rooms">
					<tr><td><loc:message name="filterAssignedRoom"/></td><td><bean:write name="assignment" property="roomNamesHtml(, )" filter="false"/></td></tr>
				</logic:notEmpty>
			</logic:notEmpty>
			<logic:notEmpty name="model" property="selectedAssignment">
				<bean:define id="assignment" name="model" property="selectedAssignment"/>
				<tr><td><loc:message name="properySelectedDates"/></td><td><bean:write name="assignment" property="dateLongNameHtml" filter="false"/></td></tr>
				<tr><td><loc:message name="properySelectedTime"/></td><td><bean:write name="assignment" property="timeLongNameHtml" filter="false"/></td></tr>
				<logic:notEmpty name="assignment" property="rooms">
					<tr><td><loc:message name="properySelectedRoom"/></td><td><bean:write name="assignment" property="roomNamesHtml(, )" filter="false"/></td></tr>
				</logic:notEmpty>
			</logic:notEmpty>
			<tr><td colspan='2'><tt:section-title><br><loc:message name="sectionTitleNewAssignments"/></tt:section-title></td></tr>
			<tr><td colspan='2'><bean:write name="model" property="changeHtmlTable" filter="false"/></td></tr>
			<tr><td colspan='2'><loc:message name="toggleDoNotUnassignConflictingClasses"/> <html:checkbox property="keepConflictingAssignments" onchange="op2.value='Apply'; submit();"/></td></tr>
			<logic:equal name="model" property="canAssign" value="true">
				<tr><td colspan='2' align="right">
					<html:submit onclick="return confirmAssign();" property="op"><loc:message name="actionClassAssign"/></html:submit>
				</td></tr>
			</logic:equal>
		</logic:equal>
		<logic:notEqual name="model" property="hasChange" value="true">
			<logic:notEmpty name="model" property="classAssignment">
				<bean:define id="assignment" name="model" property="classAssignment"/>
				<tr><td><loc:message name="propertyDate"/></td><td><bean:write name="assignment" property="dateLongNameHtml" filter="false"/></td></tr>
				<tr><td><loc:message name="propertyTime"/></td><td><bean:write name="assignment" property="timeLongNameHtml" filter="false"/></td></tr>
				<logic:notEmpty name="assignment" property="rooms">
					<tr><td><loc:message name="propertyRoom"/></td><td><bean:write name="assignment" property="roomNamesHtml(, )" filter="false"/></td></tr>
				</logic:notEmpty>
			</logic:notEmpty>
		</logic:notEqual>
		<tr><td colspan='2'><tt:section-title><br><loc:message name="sectionTitleStudentConflicts"/></tt:section-title></td></tr>
		<tr><td colspan='2'><bean:write name="model" property="studentConflictTable" filter="false"/></td></tr>
		<logic:notEqual name="model" property="useRealStudents" value="true">
			<tr><td colspan='2' align="center" onClick="displayLoading(); document.location='classInfo.do?op=Type&type=actual';" align='center' style='cursor:pointer;'><i><loc:message name="studentConflictsShowingSolutionConflicts"/></i></td></tr>
		</logic:notEqual>
		<logic:equal name="model" property="useRealStudents" value="true">
			<tr><td colspan='2' align="center" onClick="displayLoading(); document.location='classInfo.do?op=Type&type=solution';" align='center' style='cursor:pointer;'><i><loc:message name="studentConflictsShowingActualConflicts"/></i></td></tr>
		</logic:equal>
		<logic:equal name="model" property="showDates" value="true">
			<tr><td colspan='2'><tt:section-title>
				<br><loc:message name="sectionTitleAvailableDatesForClass"><bean:write name="clazz" property="className"/></loc:message> &nbsp;&nbsp;
			</tt:section-title></td></tr>
			<tr><td colspan='2'>
			<bean:write name="model" property="datesTable" filter="false"/>
			</td></tr>
		</logic:equal>
		<tr><td colspan='2'><tt:section-title>
			<br><loc:message name="sectionTitleAvailableTimesForClass"><bean:write name="clazz" property="className"/></loc:message> &nbsp;&nbsp;
		</tt:section-title></td></tr>
		<logic:notEmpty name="model" property="times">
			<tr><td colspan='2'>
			<bean:write name="model" property="timesTable" filter="false"/>
			</td></tr>
		</logic:notEmpty>
		<logic:empty name="model" property="times">
			<tr><td colspan='2'><i><loc:message name="messageNoTimesAvailable"/></i></td></tr>
		</logic:empty>
		<logic:notEmpty name="model" property="selectedAssignment">
		<logic:greaterThan name="clazz" property="numberOfRooms" value="0">
			<tr><td colspan='2'><tt:section-title>
				<bean:define id="classLimit" name="clazz" property="classLimit"/>
				<br><loc:message name="sectionTitleAvailableRoomsForClass"><bean:write name="clazz" property="className"/></loc:message> &nbsp;&nbsp;
				( <loc:message name="messageSelectedSize"></loc:message> <span id='roomCapacityCounter'>
					<logic:lessThan name="model" property="roomSize" value="<%=String.valueOf(classLimit)%>">
						<font color='red'><bean:write name="model" property="roomSize"/></font>
					</logic:lessThan>
					<logic:greaterEqual name="model" property="roomSize" value="<%=String.valueOf(classLimit)%>">
						<bean:write name="model" property="roomSize"/>
					</logic:greaterEqual>
					</span> <loc:message name="messageSelectedSizeOf"/> <bean:write name="clazz" property="minRoomCapacity"/> ) 
			</tt:section-title></td></tr>
			<tr><td colspan='2'>
				<table border='0' width='100%'>
					<tr><td valign="top" nowrap>
						<loc:message name="properyRoomSize"/>
							<html:text property="minRoomSize" size="5" maxlength="5"/> - <html:text property="maxRoomSize" size="5" maxlength="5"/>
					</td><td valign="top" nowrap>
						<loc:message name="properyRoomFilter"/>
							<html:text property="roomFilter" size="15" maxlength="100"/>
					</td><td valign="top" nowrap>
						<loc:message name="properyRoomAllowConflicts"/>
							<html:checkbox property="allowRoomConflict"/>
					</td><td valign="top" nowrap>
						<loc:message name="propertyRooms"/>
							<html:select property="roomBase">
								<html:optionsCollection property="roomBases" label="label" value="value"/>
							</html:select>
					</td><td valign="top" nowrap>
						<loc:message name="propertyRoomOrder"/>
							<html:select property="roomOrder">
								<html:options property="roomOrders"/>
							</html:select>
					</td><td align="right" valign="top" nowrap>
						<html:submit onclick="displayLoading();" property="op"><loc:message name="actionFilterApply"/></html:submit>
					</td></tr>
				</table>
			</td></tr>
			<tr><td colspan='2'>
				<table border='0' width="100%"><tr>
					<td nowrap><loc:message name="propertyRoomTypes"/></td>
					<logic:iterate name="classInfoForm" property="allRoomTypes" id="rf" indexId="rfIdx">
						<td nowrap>
							<html:multibox property="roomTypes">
								<bean:write name="rf" property="uniqueId"/>
							</html:multibox>
							<bean:write name="rf" property="label"/>&nbsp;&nbsp;&nbsp;
						</td>
						<% if (rfIdx%3==2) { %>
							</tr><tr><td></td>
						<% } %>
					</logic:iterate>
				</tr><tr>
					<td nowrap><loc:message name="propertyRoomGroups"/></td>
					<logic:iterate name="classInfoForm" property="allRoomGroups" id="rf" indexId="rfIdx">
						<td nowrap>
							<html:multibox property="roomGroups">
								<bean:write name="rf" property="uniqueId"/>
							</html:multibox>
							<bean:write name="rf" property="name"/>&nbsp;&nbsp;&nbsp;
						</td>
						<% if (rfIdx%3==2) { %>
							</tr><tr><td></td>
						<% } %>
					</logic:iterate>
				</tr>
				<logic:iterate name="classInfoForm" property="roomFeatureTypes" id="ft" type="org.unitime.timetable.model.RoomFeatureType">
					<tr>
						<td nowrap><bean:write name="ft" property="label"/>:</td>
						<logic:iterate name="classInfoForm" property='<%="allRoomFeatures("+ft.getUniqueId()+")"%>' id="rf" indexId="rfIdx">
							<td nowrap>
								<html:multibox property="roomFeatures">
									<bean:write name="rf" property="uniqueId"/>
								</html:multibox>
								<bean:write name="rf" property="label"/>&nbsp;&nbsp;&nbsp;
							</td>
							<% if (rfIdx%3==2) { %>
								</tr><tr><td></td>
							<% } %>
						</logic:iterate>
					</tr>
				</logic:iterate>
			</table></td></tr>
			<logic:empty name="model" property="roomTable">
				<tr><td colspan='2'><i><loc:message name="messageNoMatchingRoomFound"/></i></td></tr>
			</logic:empty>
			<logic:notEmpty name="model" property="roomTable">
				<tr><td colspan='2'>
					<bean:write name="model" property="roomTable" filter="false"/>
				</td></tr>
			</logic:notEmpty>
		</logic:greaterThan>
		</logic:notEmpty>
		<tr><td colspan='2'><tt:section-title><br></tt:section-title></td></tr>
	</table>
	<logic:notEmpty name="classInfoForm" property="message">
		<bean:define id="message" name="classInfoForm" property="message"/>
		<script language="JavaScript" type="text/javascript">
			alert('<%=message%>');
		</script>
	</logic:notEmpty>
	</loc:bundle>	
</html:form>
