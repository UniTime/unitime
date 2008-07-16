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

<%-- 
TO DO: 
* Trash cans instead of Delete
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
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title><bean:write name="eventDetailForm" property="eventName"/></tt:section-title>
					<logic:notEmpty name="eventDetailForm" property="previousId">
						<html:submit property="op" styleClass="btn" accesskey="P" 
							title="Previous Event (Alt+P)" value="Previous"/>
					</logic:notEmpty>
					&nbsp;
					<logic:notEmpty name="eventDetailForm" property="nextId">
						<html:submit property="op" styleClass="btn" accesskey="N"
							title="Next Event (Alt+N)" value="Next"/>
					</logic:notEmpty>
					&nbsp;
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
		<TR>
			<TD nowrap>Event Capacity:&nbsp;</TD>
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
		<logic:equal name="eventDetailForm" property="eventType" value="Special Event">
		<TR>
			<TD nowrap>Sponsoring Organization:&nbsp;</TD>
			<TD width='100%'>
				<bean:write name="eventDetailForm" property="sponsoringOrgName"/>
			</TD>
		</TR>
		</logic:equal>
		<TR>
			<TD nowrap valign="top">Additional Information:&nbsp;</TD>
			<td>
			<table>
				<logic:iterate name="eventDetailForm" property="notes" id="note">
				<tr><td><bean:write name="note" property="textNote"/></td></tr>
				</logic:iterate>
				<logic:iterate name="eventDetailForm" property="notes" id="note">
				<tr><td><bean:write name="note" property="standardNote"/></td></tr>
				</logic:iterate>
			</table>
			</td>
		</TR>
		<logic:equal name="eventDetailForm" property="canEdit" value="true">
			<TR>
				<TD nowrap valign="top">Main Contact:&nbsp;</TD>
				<td>
				<Table width="100%" border="0" cellspacing="0" cellpadding="1">
					<tr align="left">
						<td><font color="gray"><i>Name</i></font></td>
						<td><font color="gray"><i>E-mail</i></font></td>
						<td><font color="gray"><i>Phone</i></font></td>
					</tr>
					<logic:notEmpty name="eventDetailForm" property="mainContact">
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
					</logic:notEmpty>
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
			<TR>
				<TD nowrap valign="top">Additional E-mails:&nbsp;</TD>
				<TD>
					<bean:write name="eventDetailForm" property="additionalEmails"/>
				</TD>
			</TR>		
		</logic:equal>
		<tt:last-change type='Event'>
			<bean:write name="eventDetailForm" property="id"/>
		</tt:last-change>		


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
				<td><font color="gray"><i>Date</i></font></td><td><font color="gray"><i>Time</i></font></td><td><font color="gray"><i>Location</i></font></td><td><font color="gray"><i>Approved</i></font></td>
			</TR>
			<html:hidden property="selected"/>
			<logic:iterate name="eventDetailForm" property="meetings" id="meeting">
				<bean:define name="meeting" property="uniqueId" id="meetingId"/>
				<TR onmouseover="style.backgroundColor='rgb(223,231,242)';" onmouseout="style.backgroundColor='transparent';">
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
						<bean:write name="meeting" property="approvedDate"/>
					</TD>			
					<TD>
						<logic:equal name="eventDetailForm" property="canEdit" value="true">
							<bean:define name="meeting" property="date" id="meetingDate"/>
							<bean:define name="meeting" property="startTime" id="meetingStartTime"/>
							<bean:define name="meeting" property="endTime" id="meetingEndTime"/>
							<bean:define name="meeting" property="location" id="meetingLocation"/>
							<html:submit property="op" styleClass="btn" title="Delete Meeting" value="Delete" 
								onclick="<%="selected.value='"+meetingId+"'; return confirm('The "+meetingDate+" "+meetingStartTime+" - "+meetingEndTime+" "+meetingLocation+" meeting will be deleted. Continue?');"%>"/>
						</logic:equal>
					</TD>
				</TR>	
			</logic:iterate>
		</Table>
		</TD></TR>

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
			

<!-- Buttons -->
	<TR>
		<TD colspan="2" align="right">
			<tt:section-title/>
		</TD>
	</TR>
	<TR>
		<TD colspan="2" align="right">
				<logic:notEmpty name="eventDetailForm" property="previousId">
					<html:submit property="op" styleClass="btn" accesskey="P" 
						title="Go To Previous Event (Alt+P)" value="Previous"/>
				</logic:notEmpty>
				&nbsp;
				<logic:notEmpty name="eventDetailForm" property="nextId">
					<html:submit property="op" styleClass="btn" accesskey="N"
						title="Next Event (Alt+N)" value="Next"/>
				</logic:notEmpty>
				&nbsp;
				<tt:back styleClass="btn" name="Back" title="Return to %% (Alt+B)" accesskey="B" type="PreferenceGroup">
					A<bean:write name="eventDetailForm" property="id"/>
				</tt:back>
		</TD>
	</TR>


	</TABLE>

</html:form>



