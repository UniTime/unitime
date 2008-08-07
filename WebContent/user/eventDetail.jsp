<%--
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC
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
 * <bean:write name="eventDetailForm" property="additionalInfo"/> 
--%>

<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>


<tiles:importAttribute />

<html:form action="/eventDetail">
	<html:hidden property="id"/>
	<html:hidden property="nextId"/>
	<html:hidden property="previousId"/>	
	<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">
		<logic:messagesPresent>
		<TR>
			<TD colspan="2" align="left" class="errorCell">
					<B><U>ERRORS</U></B><BR>
				<BLOCKQUOTE>
				<UL>
				    <html:messages id="error">
				      <LI>
						${error}
				      </LI>
				    </html:messages>
			    </UL>
			    </BLOCKQUOTE>
			</TD>
		</TR>
		</logic:messagesPresent>
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title><bean:write name="eventDetailForm" property="eventName"/></tt:section-title>
					<logic:equal name="eventDetailForm" property="canEdit" value="true">
						<html:submit property="op" styleClass="btn" accesskey="E" 
							title="Edit Event (Alt+E)" value="Edit Event"/>
						&nbsp;
					</logic:equal>
					<logic:greaterEqual name="eventDetailForm" property="previousId" value="0">
						<html:submit property="op" styleClass="btn" accesskey="P" 
							title="Previous Event (Alt+P)" value="Previous"/>
						&nbsp;
					</logic:greaterEqual>
					<logic:greaterEqual name="eventDetailForm" property="nextId" value="0">
						<html:submit property="op" styleClass="btn" accesskey="N"
							title="Next Event (Alt+N)" value="Next"/>
						&nbsp;
					</logic:greaterEqual>
					<tt:back styleClass="btn" name="Back" title="Return to %% (Alt+B)" accesskey="B" type="PreferenceGroup">
						A<bean:write name="eventDetailForm" property="id"/>
					</tt:back>
				</tt:section-header>
			</TD>
		</TR>
		<TR>
			<TD nowrap> Event Type:&nbsp;</TD>
			<TD> <bean:write name="eventDetailForm" property="eventType"/>
		</TR>
		<logic:notEmpty name="eventDetailForm" property="minCapacity">
		<TR>
			<logic:equal name="eventDetailForm" property="eventType" value="Special Event">
				<TD nowrap>Expected Size:&nbsp;</TD>
			</logic:equal>
			<logic:notEqual name="eventDetailForm" property="eventType" value="Special Event">
				<TD nowrap>Event Capacity:&nbsp;</TD>
			</logic:notEqual>
			<TD width='100%'>
				<bean:define name="eventDetailForm" property="minCapacity" id="min"/>
 				<logic:equal name="eventDetailForm" property="maxCapacity" value="<%=min.toString()%>">
					<bean:write name="eventDetailForm" property="minCapacity"/>
				</logic:equal> 
				<logic:notEqual name="eventDetailForm" property="maxCapacity" value="<%=min.toString()%>">
					<logic:equal name="eventDetailForm" property="maxCapacity" value="">
						<bean:write name="eventDetailForm" property="minCapacity"/>
					</logic:equal>
					<logic:notEqual name="eventDetailForm" property="maxCapacity" value="">
						<bean:write name="eventDetailForm" property="minCapacity"/> - <bean:write name="eventDetailForm" property="maxCapacity"/>
					</logic:notEqual>
				</logic:notEqual>
 			</TD>
		</TR>
		</logic:notEmpty>
		<logic:equal name="eventDetailForm" property="eventType" value="Special Event">
		<logic:notEmpty name="eventDetailForm" property="sponsoringOrgName">
		<TR>
			<TD nowrap>Sponsoring Organization:&nbsp;</TD>
			<TD width='100%'>
				<bean:write name="eventDetailForm" property="sponsoringOrgName"/>
			</TD>
		</TR>
		</logic:notEmpty>
		</logic:equal>
		<logic:equal name="eventDetailForm" property="isManager" value="true">
			<logic:notEmpty name="eventDetailForm" property="mainContact">
			<TR>
				<TD nowrap valign="top">Main Contact:&nbsp;</TD>
				<td>
				<Table width="100%" border="0" cellspacing="0" cellpadding="1">
					<tr align="left">
						<td><font color="gray"><i>Name</i></font></td>
						<td><font color="gray"><i>E-mail</i></font></td>
						<td><font color="gray"><i>Phone</i></font></td>
					</tr>
					<bean:define name="eventDetailForm" property="mainContact" id="mc"/>
					<TR>
						<TD>
							<bean:write name="mc" property="firstName"/>
							<bean:write name="mc" property="middleName"/> 
							<bean:write name="mc" property="lastName"/>
						</TD>
						<td>
							<bean:write name="mc" property="email"/>						
						</td>
						<td>
							<bean:write name="mc" property="phone"/>						
						</td>
					</TR>		
					<logic:iterate name="eventDetailForm" property="additionalContacts" id="additionalContact">
						<tr>
							<td>
								<bean:write name="additionalContact" property="firstName"/> 
								<bean:write name="additionalContact" property="middleName"/>
								<bean:write name="additionalContact" property="lastName"/>							
							</td>
							<td>
								<bean:write name="additionalContact" property="email"/>
							</td>
							<td>
								<bean:write name="additionalContact" property="phone"/>
							</td>						
						</tr>
					</logic:iterate>
					
				</table>
				</td>
			</TR>
			</logic:notEmpty>
			<logic:notEmpty name="eventDetailForm" property="additionalEmails">
			<TR>
				<TD nowrap valign="top">Additional E-mails:&nbsp;</TD>
				<TD>
					<bean:write name="eventDetailForm" property="additionalEmails"/>
				</TD>
			</TR>		
			</logic:notEmpty>
		</logic:equal>
		<logic:equal name="eventDetailForm" property="canEdit" value="true">
			<tt:last-change type='Event'>
				<bean:write name="eventDetailForm" property="id"/>
			</tt:last-change>		
		</logic:equal>


		<TR>
			<TD colspan='2'>&nbsp;</TD>
		</TR>

		<TR>
			<TD colspan='2'>
				<tt:section-header>
				<tt:section-title>Meetings</tt:section-title>
				<logic:equal name="eventDetailForm" property="canEdit" value="true">
					<html:submit property="op" styleClass="btn" accesskey="A"
						title="Add Meetings (Alt+A)" value="Add Meetings"/>
				</logic:equal>
				</tt:section-header>
			</TD>
		</TR>

		<TR><TD colspan='2'>
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="1">
			<TR align="left">
				<td>
					<logic:equal name="eventDetailForm" property="canSelectAll" value="true">
						<input type='checkbox' onclick='selectAll(this.checked);' title="Select All">
					</logic:equal>
				</td><td><font color="gray"><i>Date</i></font></td><td><font color="gray"><i>Time</i></font></td><td><font color="gray"><i>Location</i></font></td><td><font color="gray"><i>Capacity</i></font></td><td><font color="gray"><i>Approved</i></font></td>
			</TR>
			<html:hidden property="selected"/>
			<logic:iterate name="eventDetailForm" property="meetings" id="meeting">
				<bean:define name="meeting" property="uniqueId" id="meetingId"/>
				<bean:define id="bg" value="transparent"/>
				<bean:define id="color" value="black"/>
				<bean:define id="fs" value="normal"/>
				<logic:equal name="meeting" property="isPast" value="true">
					<bean:define id="fs" value="italic"/>
					<bean:define id="color" value="gray"/>
				</logic:equal>
				<logic:notEmpty name="meeting" property="overlaps">
					<bean:define id="color" value="red"/>
				</logic:notEmpty>
				<logic:notEqual name="meeting" property="isPast" value="true">
					<logic:empty name="meeting" property="approvedDate">
						<bean:define id="bg" value="#FFFFDD"/>
					</logic:empty>
					<logic:notEmpty name="meeting" property="approvedDate">
						<bean:define id="bg" value="#DDFFDD"/>
					</logic:notEmpty>
				</logic:notEqual>
				<TR onmouseover="style.backgroundColor='rgb(223,231,242)';" onmouseout="style.backgroundColor='<%=bg%>';" style="color:<%=color%>;background-color:<%=bg%>;font-style:<%=fs%>;">
					<TD>
						<logic:equal name="meeting" property="canSelect" value="true">
							<bean:define name="meeting" property="date" id="meetingDate"/>
							<bean:define name="meeting" property="startTime" id="meetingStartTime"/>
							<bean:define name="meeting" property="endTime" id="meetingEndTime"/>
							<bean:define name="meeting" property="location" id="meetingLocation"/>
							<html:multibox property="selectedMeetings">
								<bean:write name="meetingId"/>
							</html:multibox>
						</logic:equal>
					</TD>
					<TD>
						<bean:write name="meeting" property="date" filter="false"/> 
					</TD>
					<TD>
						<bean:write name="meeting" property="startTime"/> - <bean:write name="meeting" property="endTime"/>
					</TD>
					<TD>
						<bean:write name="meeting" property="location"/>
					</TD>	
					<TD>
						&nbsp; <bean:write name="meeting" property="locationCapacity"/>
					</TD>	
					<TD>
						<bean:write name="meeting" property="approvedDate"/>
					</TD>			
				</TR>	
				<logic:iterate name="meeting" property="overlaps" id="overlap">
					<bean:define name="overlap" property="eventId" id="overlapEventId"/>
					<TR onmouseover="this.style.cursor='hand';this.style.cursor='pointer';" onclick="<%="document.location='eventDetail.do?id="+overlapEventId+"';\""%>">
						<TD></TD>
						<TD style="background-color:#FFD7D7;">&nbsp;&nbsp;&nbsp;Conflicts with <bean:write name="overlap" property="name"/> (<bean:write name="overlap" property="type"/>)</TD>
						<TD style="background-color:#FFD7D7;"><bean:write name="overlap" property="startTime"/> - <bean:write name="overlap" property="endTime"/></TD>
						<TD style="background-color:#FFD7D7;" colspan='2'></TD>
						<TD style="background-color:#FFD7D7;">
							<logic:empty name="overlap" property="approvedDate">
								<i>Not Approved</i>
							</logic:empty>
							<logic:notEmpty name="overlap" property="approvedDate">
								<i>Approved</i>
							</logic:notEmpty>
						</TD>
					</TR>
				</logic:iterate>
			</logic:iterate>
		</Table>
		</TD></TR>
		<logic:equal name="eventDetailForm" property="canApprove" value="true">
			<TR>
				<TD colspan="2">
					<tt:section-title/>
				</TD>
			</TR>
			<TR>
				<TD colspan="2">
				<font color="gray"><i>Attach notes to your approval/rejection of above selected meetings. Double click on a standard note to add it to notes below. Only the Notes: section will be sent to the requester.</i></font>
				</TD>
			</TR>
			<TR>
				<TD valign="top">
				Standard Notes:
				</TD>
				<TD>
					<html:select size="3"
								name="eventDetailForm" 
								styleClass="cmb" 
								property="selectedStandardNote" 
								onfocus="setUp();" 
								onkeypress="return selectSearch(event, this);"
								onkeydown="return checkKey(event, this);"
								ondblclick="if (newEventNote.value.length>0) newEventNote.value+='\n'; newEventNote.value+=this.options[this.selectedIndex].text; newEventNote.focus();"
								>
						<html:optionsCollection property="standardNotes" 
								label="note" 
								value="uniqueId" />
					</html:select>
				</TD>			
			</TR>
			<TR>
				<TD valign="top">
				Notes:
				</TD>
				<TD colspan='2'>
					<html:textarea rows="3" cols="50"  
								name="eventDetailForm"  
								property="newEventNote">
					</html:textarea>
				</TD>			
			</TR>
			</logic:equal>
			<TR>
				<TD colspan='6'>
				<logic:equal name="eventDetailForm" property="canDelete" value="true">
					<html:submit property="op" styleClass="btn" accesskey="D"
						title="Delete Selected Meetings (Alt+D)" value="Delete"/>
				</logic:equal>
				<logic:equal name="eventDetailForm" property="canApprove" value="true">
					<html:submit property="op" styleClass="btn" accesskey="P"
						title="Approve Selected Meetings (Alt+P)" value="Approve"/>
					<html:submit property="op" styleClass="btn" accesskey="R"
						title="Reject Selected Meetings (Alt+R)" value="Reject"/>
				</logic:equal>
				</TD>
			</TR>
<!-- Exam -->
	<logic:equal name="eventDetailForm" property="isManager" value="true">
	<logic:equal name="eventDetailForm" property="eventType" value="Final Examination Event">
		<TR>
			<TD colspan='2'>
				<tt:exams type="ExamEvent" add="false"><bean:write name="eventDetailForm" property="id"/></tt:exams>
			</TD>
		</TR>
	</logic:equal>
	<logic:equal name="eventDetailForm" property="eventType" value="Midterm Examination Event">
		<TR>
			<TD colspan='2'>
				<tt:exams type="ExamEvent" add="false"><bean:write name="eventDetailForm" property="id"/></tt:exams>
			</TD>
		</TR>
	</logic:equal>
	</logic:equal>
<!-- Courses/Classes if this is a course event -->
	<logic:equal name="eventDetailForm" property="eventType" value="Course Event">
		<TR>
			<TD colspan='2'>
				&nbsp;
			</TD>
		</TR>
		<TR>
			<TD colspan="2" valign="middle">
				<br>
				<tt:section-title>
					Related Classes / Courses
				</tt:section-title>
			</TD>
		</TR>
		<TR>
			<TD colspan='2'>
				<logic:empty scope="request" name="EventDetail.table">
					<i>No relation defined for this event.</i>
				</logic:empty>
				<logic:notEmpty scope="request" name="EventDetail.table">
					<table border='0' cellspacing="0" cellpadding="3" width='99%'>
					<bean:write scope="request" name="EventDetail.table" filter="false"/>
					</table>
				</logic:notEmpty>
			</TD>
		</TR>
		<logic:equal name="eventDetailForm" property="attendanceRequired" value="true">
		<TR>
			<TD colspan='2'>
				<i>Students of the listed courses/classes are required to attend this event.</i>
			</TD>
		</TR>
		</logic:equal>
	</logic:equal>
	<logic:equal name="eventDetailForm" property="isManager" value="true">
	<logic:equal name="eventDetailForm" property="eventType" value="Class Event">
		<logic:notEmpty scope="request" name="EventDetail.table">
		<TR>
			<TD colspan='2'>
				&nbsp;
			</TD>
		</TR>
		<TR>
			<TD colspan="2" valign="middle">
				<br>
				<tt:section-title>
					Class
				</tt:section-title>
			</TD>
		</TR>
		<TR>
			<TD colspan='2'>
				<table border='0' cellspacing="0" cellpadding="3" width='99%'>
				<bean:write scope="request" name="EventDetail.table" filter="false"/>
				</table>
			</TD>
		</TR>
		</logic:notEmpty>
	</logic:equal>
	</logic:equal>

<!-- Notes exchanged between requester and approver; visible only by those who can edit -->
   		<logic:equal name="eventDetailForm" property="canEdit" value="true">
   		<logic:notEmpty name="eventDetailForm" property="notes">
		<TR>
			<TD colspan='2'>&nbsp;</TD>
		</TR>
		<TR>
			<TD colspan='2'>
				<tt:section-header>
					<tt:section-title>Notes</tt:section-title>
				</tt:section-header>
			</TD>
		</TR>
		<TR>
			<td colspan='2'>
				<table width="100%" border="0" cellspacing="0" cellpadding="3">
					<tr style='color:gray;font-style:italic;'>
						<td>Date</td>
						<logic:equal name="eventDetailForm" property="isManager" value="true">
							<td>User</td>
						</logic:equal>
						<td>Action</td>
						<td>Meetings</td>
						<td>Note</td>
					</tr>
					<logic:iterate name="eventDetailForm" property="notes" id="note">
						<bean:write name="note" filter="false"/>
					</logic:iterate>
				</table>
			</td>
		</TR>
		</logic:notEmpty>
		</logic:equal>

<!-- Buttons -->
	<TR>
		<TD colspan="2" align="right">
			<tt:section-title/>
		</TD>
	</TR>
	<TR>
		<TD colspan="2" align="right">
				<logic:equal name="eventDetailForm" property="canEdit" value="true">
					<html:submit property="op" styleClass="btn" accesskey="E" 
						title="Edit Event (Alt+E)" value="Edit Event"/>
						&nbsp;
				</logic:equal>
				<logic:greaterEqual name="eventDetailForm" property="previousId" value="0">
					<html:submit property="op" styleClass="btn" accesskey="P" 
						title="Go To Previous Event (Alt+P)" value="Previous"/>
					&nbsp;
				</logic:greaterEqual>
				<logic:greaterEqual name="eventDetailForm" property="nextId" value="0">
					<html:submit property="op" styleClass="btn" accesskey="N"
						title="Next Event (Alt+N)" value="Next"/>
					&nbsp;
				</logic:greaterEqual>
				<tt:back styleClass="btn" name="Back" title="Return to %% (Alt+B)" accesskey="B" type="PreferenceGroup">
					A<bean:write name="eventDetailForm" property="id"/>
				</tt:back>
		</TD>
	</TR>


	</TABLE>

	<script language='JavaScript'>
		function selectAll(checked) {
			var selected = document.getElementsByName('selectedMeetings');
			for (var i=0;i<selected.length;i++) selected[i].checked = checked;
		}
	</script>

</html:form>



