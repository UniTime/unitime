<%--
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime.org
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
--%>
<%@ page language="java" autoFlush="true"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>
<tiles:importAttribute />
<html:form action="/examInfo">
	<bean:define id="model" name="examInfoForm" property="model"/>
	<bean:define id="exam" name="model" property="exam"/>
	<bean:define id="examId" name="exam" property="examId"/>
	<logic:notEmpty name="examInfoForm" property="message">
		<bean:define id="message" name="examInfoForm" property="message"/>
		<script language="JavaScript" type="text/javascript">
			alert('<%=message%>');
		</script>
	</logic:notEmpty>
	<logic:equal name="examInfoForm" property="op" value="Close">
		<script language="JavaScript" type="text/javascript">
			if (document.parentWindow && document.parentWindow.frames[5]) { 
				document.parentWindow.frames[5].location = document.parentWindow.frames[5].location+'?backId=<%=examId%>&backType=Exam';
			}
			window.close();
		</script>
	</logic:equal>
	<tt:confirm name="confirmAssign"><bean:write name="model" property="assignConfirm"/></tt:confirm>
	<table border='0' width='95%'>
		<tr><td colspan='2'>
			<tt:section-header>
				<tt:section-title>Examination <bean:write name="exam" property="examName"/></tt:section-title>
			</tt:section-header>
		</td></tr>
		<tr><td>Courses / Classes:</td><td><bean:write name="exam" property="sectionName(<br>)" filter="false"/></td></tr>
		<tr><td>Type:</td><td><bean:write name="exam" property="examTypeLabel"/></td></tr>
		<tr><td>Length:</td><td><bean:write name="exam" property="length"/> minutes</td></tr>
		<tr><td>Students:</td><td><bean:write name="exam" property="nrStudents"/></td></tr>
		<tr><td>Seating Type:</td><td><bean:write name="exam" property="seatingTypeLabel"/></td></tr>
		<tr><td>Maximum Number of Rooms:</td><td><bean:write name="exam" property="maxRooms"/></td></tr>
		<logic:notEmpty name="exam" property="instructors">
			<tr><td>Instructor(s):</td><td><bean:write name="exam" property="instructorName(<br>)"/></td></tr>
		</logic:notEmpty>
		<logic:notEmpty name="model" property="selectedAssignment">
			<logic:notEmpty name="model" property="examAssignment">
				<bean:define id="assignment" name="model" property="examAssignment"/>
				<tr><td>Assigned Period:</td><td><bean:write name="assignment" property="periodNameWithPref" filter="false"/></td></tr>
				<logic:notEmpty name="assignment" property="rooms">
					<tr><td>Assigned Room:</td><td><bean:write name="assignment" property="roomsNameWithPref(, )" filter="false"/></td></tr>
				</logic:notEmpty>
			</logic:notEmpty>
			<bean:define id="assignment" name="model" property="selectedAssignment"/>
			<tr><td>Selected Period:</td><td><bean:write name="assignment" property="periodNameWithPref" filter="false"/></td></tr>
			<logic:notEmpty name="assignment" property="rooms">
				<tr><td>Selected Room:</td><td><bean:write name="assignment" property="roomsNameWithPref(, )" filter="false"/></td></tr>
			</logic:notEmpty>
			<logic:equal name="assignment" property="valid" value="true">
				<tr><td colspan='2' align="right">
					<html:submit property="op" value="Assign" onclick="return confirmAssign();"/>
				</td></tr>
			</logic:equal>
			<logic:greaterThan name="assignment" property="nrDistributionConflicts" value="0">
				<tr><td colspan='2'><tt:section-title><br>Violated Distribution Preferences (<bean:write name="assignment" property="periodAbbreviation" filter="false"/>)</tt:section-title></td></tr>
				<tr><td colspan='2'><bean:write name="assignment" property="distributionConflictTable" filter="false"/></td></tr>
			</logic:greaterThan>
			<logic:equal name="assignment" property="hasConflicts" value="true">
				<tr><td colspan='2'><tt:section-title><br>Student Conflicts (<bean:write name="assignment" property="periodAbbreviation" filter="false"/>)</tt:section-title></td></tr>
				<tr><td colspan='2'><bean:write name="assignment" property="conflictTable" filter="false"/></td></tr>
			</logic:equal>
			<logic:equal name="assignment" property="hasInstructorConflicts" value="true">
				<tr><td colspan='2'><tt:section-title><br>Instructor Conflicts (<bean:write name="assignment" property="periodAbbreviation" filter="false"/>)</tt:section-title></td></tr>
				<tr><td colspan='2'><bean:write name="assignment" property="instructorConflictTable" filter="false"/></td></tr>
			</logic:equal>
		</logic:notEmpty>
		<logic:empty name="model" property="selectedAssignment">
			<logic:notEmpty name="model" property="examAssignment">
				<bean:define id="assignment" name="model" property="examAssignment"/>
				<tr><td>Period:</td><td><bean:write name="assignment" property="periodNameWithPref" filter="false"/></td></tr>
				<logic:notEmpty name="assignment" property="rooms">
					<tr><td>Room:</td><td><bean:write name="assignment" property="roomsNameWithPref(, )" filter="false"/></td></tr>
				</logic:notEmpty>
				<logic:greaterThan name="assignment" property="nrDistributionConflicts" value="0">
					<tr><td colspan='2'><tt:section-title><br>Violated Distribution Preferences</tt:section-title></td></tr>
					<tr><td colspan='2'><bean:write name="assignment" property="distributionConflictTable" filter="false"/></td></tr>
				</logic:greaterThan>
				<logic:equal name="assignment" property="hasConflicts" value="true">
					<tr><td colspan='2'><tt:section-title><br>Student Conflicts</tt:section-title></td></tr>
					<tr><td colspan='2'><bean:write name="assignment" property="conflictTable" filter="false"/></td></tr>
				</logic:equal>
				<logic:equal name="assignment" property="hasInstructorConflicts" value="true">
					<tr><td colspan='2'><tt:section-title><br>Instructor Conflicts</tt:section-title></td></tr>
					<tr><td colspan='2'><bean:write name="assignment" property="instructorConflictTable" filter="false"/></td></tr>
				</logic:equal>
			</logic:notEmpty>
		</logic:empty>
		<logic:notEmpty name="model" property="periods">
			<tr><td colspan='2'><br><table border='0' width='100%' cellspacing='0' cellpadding='3'>
				<bean:write name="model" property="periodsTable" filter="false"/>
			</table></td></tr>
		</logic:notEmpty>
		<logic:notEmpty name="model" property="rooms">
			<tr><td colspan='2'><tt:section-title>
				<bean:define id="nrStudents" name="assignment" property="nrStudents"/>
				<br>Available Rooms &nbsp;&nbsp;
				( selected size: <span id='roomCapacityCounter'>
					<logic:lessThan name="model" property="roomSize" value="<%=String.valueOf(nrStudents)%>">
						<font color='red'><bean:write name="model" property="roomSize"/></font>
					</logic:lessThan>
					<logic:greaterEqual name="model" property="roomSize" value="<%=String.valueOf(nrStudents)%>">
						<bean:write name="model" property="roomSize"/>
					</logic:greaterEqual>
					</span> ) 
			</tt:section-title></td></tr>
			<tr><td colspan='2'>
				<bean:write name="model" property="roomTable" filter="false"/>
			</td></tr>
		</logic:notEmpty>
		<tr><td colspan='2'><tt:section-title><br></tt:section-title></td></tr>
	</table>
</html:form>