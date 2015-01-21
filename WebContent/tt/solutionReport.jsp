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
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>

<tiles:importAttribute />
<html:form action="/solutionReport">
	<logic:notEmpty name="SolutionReport.message" scope="request">
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
			<TR>
				<TD>
					<I>
						<bean:write name="SolutionReport.message" scope="request" filter="false"/>
					</I>
				</TD>
			</TR>
		</TABLE>
	</logic:notEmpty>
<%	boolean atLeastOneRoomReport = false; %>
	<logic:iterate name="solutionReportForm" property="roomTypes" id="roomType">
		<bean:define name="roomType" property="reference" id="ref"/>
		<logic:notEmpty name='<%="SolutionReport.roomReportTable."+ref%>' scope="request">
			<% if (!atLeastOneRoomReport) { %>
				<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
			<%  atLeastOneRoomReport = true; } %>
			<bean:write name='<%="SolutionReport.roomReportTable."+ref%>' scope="request" filter="false"/>
		</logic:notEmpty>
	</logic:iterate>
	<logic:notEmpty name="SolutionReport.roomReportTable.nonUniv" scope="request">
		<% if (!atLeastOneRoomReport) { %>
			<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<%  atLeastOneRoomReport = true; } %>
		<bean:write name="SolutionReport.roomReportTable.nonUniv" scope="request" filter="false"/>
	</logic:notEmpty>
<%
	if (atLeastOneRoomReport) {
%>
		<tr><td><i>Group</i></td><td colspan='8'>group size &lt;minimum, maximum)</td></tr>
		<tr><td><i>Size</i></td><td colspan='8'>actual group size (size of the smallest and the biggest room in the group)</td></tr>
		<tr><td><i>NrRooms</i></td><td colspan='8'>number of rooms in the group</td></tr>
		<tr><td><i>ClUse</i></td><td colspan='8'>number of classes that are using a room from the group (actual solution)</td></tr>
		<tr><td><i>ClShould</i></td><td colspan='8'>number of classes that "should" use a room of the group (smallest available room of a class is in this group)</td></tr>
		<tr><td><i>ClMust</i></td><td colspan='8'>number of classes that must use a room of the group (all available rooms of a class are in this group)</td></tr>
		<tr><td><i>HrUse</i></td><td colspan='8'>average hours a room of the group is used (actual solution)</td></tr>
		<tr><td><i>HrShould</i></td><td colspan='8'>average hours a room of the group should be used (smallest available room of a class is in this group)</td></tr>
		<tr><td><i>HrMust</i></td><td colspan='8'>average hours a room of this group must be used (all available rooms of a class are in this group)</td></tr>
		<tr><td></td><td colspan='8'>
			<i>*) cumulative numbers (group minimum ... inf) are displayed in parentheses.</i>
		</td></tr>
	</TABLE><BR><BR><BR>
<% 
	}
	
	String violatedDistrPreferencesReportTable =  (String)request.getAttribute("SolutionReport.violatedDistrPreferencesReportTable");
	if (violatedDistrPreferencesReportTable!=null) {
%>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<%=violatedDistrPreferencesReportTable%>
		<tr><td colspan='5'><tt:displayPrefLevelLegend/></td></tr>
	</TABLE><BR><BR><BR>
<% 
	}	
	
	
	String discouragedInstructorBtbReportReportTable = (String)request.getAttribute("SolutionReport.discouragedInstructorBtbReportReportTable");
	if (discouragedInstructorBtbReportReportTable!=null) {
%>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<%=discouragedInstructorBtbReportReportTable%>
		<tr><td colspan='6'><tt:displayPrefLevelLegend/></td></tr>
	</TABLE><BR><BR><BR>
<% 
	}
	
	String studentConflictsReportTable = (String)request.getAttribute("SolutionReport.studentConflictsReportTable");
	if (studentConflictsReportTable!=null) {
%>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<%=studentConflictsReportTable%>
		<tr><td colspan='10'><tt:displayPrefLevelLegend/></td></tr>
	</TABLE><BR><BR><BR>
<% 
	}
	
	String sameSubpartBalancingReportTable = (String)request.getAttribute("SolutionReport.sameSubpartBalancingReportTable");
	if (sameSubpartBalancingReportTable!=null) {
%>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<%=sameSubpartBalancingReportTable%>
		<tr><td colspan='<%=2+Constants.SLOTS_PER_DAY_NO_EVENINGS/6%>'><tt:displayPrefLevelLegend/></td></tr>
	</TABLE><BR><BR><BR>
<% 
	}
	
	String deptBalancingReportTable = (String)request.getAttribute("SolutionReport.deptBalancingReportTable");
	if (deptBalancingReportTable!=null) {
%>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<%=deptBalancingReportTable%>
		<tr><td colspan='<%=2+Constants.SLOTS_PER_DAY_NO_EVENINGS/6%>'><tt:displayPrefLevelLegend/></td></tr>
	</TABLE><BR><BR><BR>
<% 
	}
	
	String perturbationReportTable = (String)request.getAttribute("SolutionReport.perturbationReportTable");
	if (perturbationReportTable!=null) {
%>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<%=perturbationReportTable%>
		<tr><td><i>Class</i></td><td colspan='23'>Class name</td></tr>
		<tr><td><i>Time</i></td><td colspan='23'>Time (initial &rarr; assigned)</td></tr>
		<tr><td><i>Room</i></td><td colspan='23'>Room (initial &rarr; assigned)</td></tr>
		<tr><td><i>Dist</i></td><td colspan='23'>Distance between assignments (if different are used buildings)</td></tr>
		<tr><td><i>St</i></td><td colspan='23'>Number of affected students</td></tr>
		<tr><td><i>StT</i></td><td colspan='23'>Number of affected students by time change</td></tr>
		<tr><td><i>StR</i></td><td colspan='23'>Number of affected students by room change</td></tr>
		<tr><td><i>StB</i></td><td colspan='23'>Number of affected students by building change</td></tr>
		<tr><td><i>Ins</i></td><td colspan='23'>Number of affected instructors</td></tr>
		<tr><td><i>InsT</i></td><td colspan='23'>Number of affected instructors by time change</td></tr>
		<tr><td><i>InsR</i></td><td colspan='23'>Number of affected instructors by room change</td></tr>
		<tr><td><i>InsB</i></td><td colspan='23'>Number of affected instructors by building change</td></tr>
		<tr><td><i>Rm</i></td><td colspan='23'>Number of rooms changed</td></tr>
		<tr><td><i>Bld</i></td><td colspan='23'>Number of buildings changed</td></tr>
		<tr><td><i>Tm</i></td><td colspan='23'>Number of times changed</td></tr>
		<tr><td><i>Day</i></td><td colspan='23'>Number of days changed</td></tr>
		<tr><td><i>Hr</i></td><td colspan='23'>Number of hours changed</td></tr>
		<tr><td><i>TFSt</i></td><td colspan='23'>Assigned building too far for instructor (from the initial one)</td></tr>
		<tr><td><i>TFIns</i></td><td colspan='23'>Assigned building too far for students (from the initial one)</td></tr>
		<tr><td><i>DStC</i></td><td colspan='23'>Difference in student conflicts</td></tr>
		<tr><td><i>NStC</i></td><td colspan='23'>Number of new student conflicts</td></tr>
		<tr><td><i>DTPr</i></td><td colspan='23'>Difference in time preferences</td></tr>
		<tr><td><i>DRPr</i></td><td colspan='23'>Difference in room preferences</td></tr>
		<tr><td><i>DInsB</i></td><td colspan='23'>Difference in back-to-back instructor preferences</td></tr>
	</TABLE>
<% 
	}
%>
	<BR>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD align="right">
				<html:submit property="op" value="Export PDF" /> 
				<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh" /> 
			</TD>
		</TR>
	</TABLE>
</html:form>
