<%--
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2009 - 2010, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
--%>
<%@ page language="java" autoFlush="true"%>
<%@ page import="org.unitime.timetable.form.ClassInfoForm"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>
<tiles:importAttribute />
<%
	// Get Form 
	String frmName = "classInfoForm";
	ClassInfoForm frm = (ClassInfoForm) request.getAttribute(frmName);

%>
<html:form action="/classInfo">
	<html:submit onclick="displayLoading();" property="op" value="Apply" style="display:none;"/>
	<input type='hidden' name='op2' value=''>
	<bean:define id="model" name="classInfoForm" property="model"/>
	<bean:define id="clazz" name="model" property="clazz"/>
	<bean:define id="classId" name="clazz" property="classId"/>
	<bean:define id="className" name="clazz" property="className"/>
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
				<tt:section-title>Class <a href='classDetail.do?cid=<%=classId%>' target='_blank' class='l8' title='Open Class Detail for <%=className%> in a new window.'><bean:write name="clazz" property="className"/></a></tt:section-title>
			</tt:section-header>
		</td></tr>
		<tr><td>Manager:</td><td><bean:write name="clazz" property="manager"/></td></tr>
		<logic:notEmpty name="clazz" property="classDivSec">
		<tr><td>Class Division-Section:</td><td><bean:write name="clazz" property="classDivSec"/></td></tr>
		</logic:notEmpty>
		<logic:notEqual name="clazz" property="enrollment" value="0">
			<tr><td>Enrollment:</td><td><bean:write name="clazz" property="enrollment"/></td></tr>
		</logic:notEqual>
		<tr><td>Class Limit:</td><td><bean:write name="clazz" property="classLimit"/></td></tr>
		<tr><td>Number of Rooms:</td><td><bean:write name="clazz" property="numberOfRooms"/></td></tr>
		<tr><td>Room Ratio:</td><td><bean:write name="clazz" property="roomRatio"/> ( Minimum Room Capacity: <bean:write name="clazz" property="minRoomCapacity"/> )</td></tr>
		<logic:notEmpty name="clazz" property="instructors">
			<tr><td valign="top">Conflict Checked Instructor(s):</td><td>
			<%= frm.getModel().getClazz().getLeadingInstructorNames("<br>") %> 
		</td></tr> 
 		</logic:notEmpty>
		<logic:equal name="model" property="hasChange" value="true">
			<logic:notEmpty name="model" property="classOldAssignment">
				<bean:define id="assignment" name="model" property="classOldAssignment"/>
				<tr><td>Assigned Dates:</td><td><bean:write name="assignment" property="dateLongNameHtml" filter="false"/></td></tr>
				<tr><td>Assigned Time:</td><td><bean:write name="assignment" property="timeLongNameHtml" filter="false"/></td></tr>
				<logic:notEmpty name="assignment" property="rooms">
					<tr><td>Assigned Room:</td><td><bean:write name="assignment" property="roomNamesHtml(, )" filter="false"/></td></tr>
				</logic:notEmpty>
			</logic:notEmpty>
			<logic:notEmpty name="model" property="selectedAssignment">
				<bean:define id="assignment" name="model" property="selectedAssignment"/>
				<tr><td>Selected Dates:</td><td><bean:write name="assignment" property="dateLongNameHtml" filter="false"/></td></tr>
				<tr><td>Selected Time:</td><td><bean:write name="assignment" property="timeLongNameHtml" filter="false"/></td></tr>
				<logic:notEmpty name="assignment" property="rooms">
					<tr><td>Selected Room:</td><td><bean:write name="assignment" property="roomNamesHtml(, )" filter="false"/></td></tr>
				</logic:notEmpty>
			</logic:notEmpty>
			<tr><td colspan='2'><tt:section-title><br>New Assignment(s)</tt:section-title></td></tr>
			<tr><td colspan='2'><bean:write name="model" property="changeHtmlTable" filter="false"/></td></tr>
			<tr><td colspan='2'>Do not unassign conflicting classes: <html:checkbox property="keepConflictingAssignments" onchange="op2.value='Apply'; submit();"/></td></tr>
			<logic:equal name="model" property="canAssign" value="true">
				<tr><td colspan='2' align="right">
					<html:submit onclick="return confirmAssign();" property="op" value="Assign" />
				</td></tr>
			</logic:equal>
		</logic:equal>
		<logic:notEqual name="model" property="hasChange" value="true">
			<logic:notEmpty name="model" property="classAssignment">
				<bean:define id="assignment" name="model" property="classAssignment"/>
				<tr><td>Date:</td><td><bean:write name="assignment" property="dateLongNameHtml" filter="false"/></td></tr>
				<tr><td>Time:</td><td><bean:write name="assignment" property="timeLongNameHtml" filter="false"/></td></tr>
				<logic:notEmpty name="assignment" property="rooms">
					<tr><td>Room:</td><td><bean:write name="assignment" property="roomNamesHtml(, )" filter="false"/></td></tr>
				</logic:notEmpty>
			</logic:notEmpty>
		</logic:notEqual>
		<tr><td colspan='2'><tt:section-title><br>Student Conflicts</tt:section-title></td></tr>
		<tr><td colspan='2'><bean:write name="model" property="studentConflictTable" filter="false"/></td></tr>
		<logic:equal name="model" property="showDates" value="true">
			<tr><td colspan='2'><tt:section-title>
				<br>Available Dates for <bean:write name="clazz" property="className"/> &nbsp;&nbsp;
			</tt:section-title></td></tr>
			<tr><td colspan='2'>
			<bean:write name="model" property="datesTable" filter="false"/>
			</td></tr>
		</logic:equal>
		<tr><td colspan='2'><tt:section-title>
			<br>Available Times for <bean:write name="clazz" property="className"/> &nbsp;&nbsp;
		</tt:section-title></td></tr>
		<logic:notEmpty name="model" property="times">
			<tr><td colspan='2'>
			<bean:write name="model" property="timesTable" filter="false"/>
			</td></tr>
		</logic:notEmpty>
		<logic:empty name="model" property="times">
			<tr><td colspan='2'><i>No times available.</i></td></tr>
		</logic:empty>
		<logic:notEmpty name="model" property="selectedAssignment">
		<logic:greaterThan name="clazz" property="numberOfRooms" value="0">
			<tr><td colspan='2'><tt:section-title>
				<bean:define id="classLimit" name="clazz" property="classLimit"/>
				<br>Available Rooms for <bean:write name="clazz" property="className"/> &nbsp;&nbsp;
				( selected size: <span id='roomCapacityCounter'>
					<logic:lessThan name="model" property="roomSize" value="<%=String.valueOf(classLimit)%>">
						<font color='red'><bean:write name="model" property="roomSize"/></font>
					</logic:lessThan>
					<logic:greaterEqual name="model" property="roomSize" value="<%=String.valueOf(classLimit)%>">
						<bean:write name="model" property="roomSize"/>
					</logic:greaterEqual>
					</span> of <bean:write name="clazz" property="minRoomCapacity"/> ) 
			</tt:section-title></td></tr>
			<tr><td colspan='2'>
				<table border='0' width='100%'>
					<tr><td valign="top" nowrap>
						Size:
							<html:text property="minRoomSize" size="5" maxlength="5"/> - <html:text property="maxRoomSize" size="5" maxlength="5"/>
					</td><td valign="top" nowrap>
						Filter:
							<html:text property="roomFilter" size="15" maxlength="100"/>
					</td><td valign="top" nowrap>
						Allow conflicts:
							<html:checkbox property="allowRoomConflict"/>
					</td><td valign="top" nowrap>
						Rooms:
							<html:select property="roomBase">
								<html:optionsCollection property="roomBases" label="label" value="value"/>
							</html:select>
					</td><td valign="top" nowrap>
						Order:
							<html:select property="roomOrder">
								<html:options property="roomOrders"/>
							</html:select>
					</td><td align="right" valign="top" nowrap>
						<html:submit onclick="displayLoading();" property="op" value="Apply"/>
					</td></tr>
				</table>
			</td></tr>
			<tr><td colspan='2'>
				<table border='0' width="100%"><tr>
					<td nowrap>Room Types:</td>
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
					<td nowrap>Room Groups:</td>
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
				<tr><td colspan='2'><i>No room matching the above criteria was found.</i></td></tr>
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
</html:form>
