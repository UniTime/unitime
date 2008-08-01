<!-- 
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
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
-->

<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<tiles:importAttribute />

<html:form action="/eventAddInfo">
	<html:hidden property = "isAddMeetings"/>
	<html:hidden property = "eventId"/>
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
					<tt:section-title>Complete the Request</tt:section-title>
					<logic:equal name="eventAddInfoForm" property="isAddMeetings" value="true">
					<html:submit property="op" styleClass="btn" accesskey="U"
						title="Update Event With New Meetings (Alt+U)" value="Update"/>
					<html:submit property="op" styleClass="btn" accesskey="C"
						title="Cancel Request For More Meetings (Alt+C)" value="Cancel"/>
					</logic:equal>
					<logic:notEqual name="eventAddInfoForm" property="isAddMeetings" value="true">				
					<html:submit property="op" styleClass="btn" accesskey="S"
						title="Submit Reservation (Alt+S)" value="Submit"/>
					<html:submit property="op" styleClass="btn" accesskey="C"
						title="Cancel Event (Alt+C)" value="Cancel Event"/>
					</logic:notEqual>
					<logic:equal name="eventAddInfoForm" property="canChangeSelection" value="true">
					<html:submit property="op" styleClass="btn" accesskey="A"
						title="Change Selection (Alt+A)" value="Change Selection"/>
					</logic:equal>
				</tt:section-header>
			</TD>
		</TR>

<!--  Data about an existing event if just adding meetings -->
	<logic:equal name="eventAddInfoForm" property="isAddMeetings" value="true">
		<TR>
			<TD nowrap> Event Name:&nbsp;</TD>
			<TD> 
				<bean:write name="eventAddInfoForm" property="eventName"/> 
			</TD>
		</TR>
		<TR>
			<TD nowrap> Event Type:&nbsp;</TD>
			<TD> 
				<bean:write name="eventAddInfoForm" property="eventType"/> 
			</TD>
		</TR>
		<TR>
			<logic:equal name="eventAddInfoForm" property="eventType" value="Special Event">
				<TD nowrap>Expected Size:&nbsp;</TD>
			</logic:equal>
			<logic:notEqual name="eventAddInfoForm" property="eventType" value="Special Event">
				<TD nowrap>Event Capacity:&nbsp;</TD>
			</logic:notEqual>
			<TD>
				<bean:write name="eventAddInfoForm" property="capacity"/>
				<html:hidden property="capacity"/>
			</TD>
		</TR>
		<logic:equal name="eventAddInfoForm" property="eventType" value="Special Event">
		<TR>
			<TD nowrap> Sponsoring Organization: </TD>
			<TD>
				<bean:write name="eventAddInfoForm" property="sponsoringOrgName"/>
			</TD>
		</TR>
		</logic:equal>
		<TR>
			<TD nowrap valign="top">Contact:&nbsp;</TD>
			<td>
			<Table width="100%" border="0" cellspacing="0" cellpadding="1">
				<tr align="left">
					<td><i>Name</i></td><td><i>E-mail</i></td><td><i>Phone</i></td>
				</tr>
				<TR>
					<TD>
						<bean:write name="eventAddInfoForm" property="mainContactFirstName"/>
						<bean:write name="eventAddInfoForm" property="mainContactLastName"/>
					</TD>
					<td>
						<bean:write name="eventAddInfoForm" property="mainContactEmail"/>						
					</td>
					<td>
						<bean:write name="eventAddInfoForm" property="mainContactPhone"/>						
					</td>
				</TR>		
			</Table>
			</td>
		</TR>
		<TR>
			<TD nowrap valign='top'>Additional Information: </TD>
			<TD>
				<html:textarea property="additionalInfo" rows="2" cols="50"></html:textarea>
			</TD>
		</TR>
		<TR>
			<TD colspan="2" valign="middle">
				<br>
				<tt:section-title>
					Existing Meetings (meetings set up earlier)
				</tt:section-title>
			</TD>
		</TR>
		<TR><TD colspan='2'>
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="1">
			<TR align="left">
				<td><font color="gray"><i>Date</i></font></td><td><font color="gray"><i>Time</i></font></td><td><font color="gray"><i>Location</i></font></td><td><font color="gray"><i>Capacity</i></font></td>
			</TR>
			<logic:iterate name="eventAddInfoForm" property="existingMeetings" id="meeting">
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
						<bean:write name="meeting" property="date" filter="false"/> 
					</TD>
					<TD>
						<bean:write name="meeting" property="startTime"/> - <bean:write name="meeting" property="endTime"/>
					</TD>
					<TD>
						<bean:write name="meeting" property="location"/>
					</TD>	
					<TD>
						<bean:write name="meeting" property="locationCapacity"/>
					</TD>	
				</TR>	
				<logic:iterate name="meeting" property="overlaps" id="overlap">
					<TR style="background-color:#FFD7D7;">
						<TD>&nbsp;&nbsp;&nbsp;Conflicts with <bean:write name="overlap" property="name"/> (<bean:write name="overlap" property="type"/>)</TD>
						<TD><bean:write name="overlap" property="startTime"/> - <bean:write name="overlap" property="endTime"/></TD>
						<TD></TD>
						<TD>
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
		
		
		
		
		
		
		</logic:equal>

<!-- Data to be filled in about the event -->
		<logic:notEqual name="eventAddInfoForm" property="isAddMeetings" value="true">
		<TR>
			<TD nowrap> Event Name<font color='red'>*</font>: </TD>
			<TD> 
				<html:text property="eventName" maxlength="100" size="50" /> 
			</TD>
		</TR>
		<logic:equal name="eventAddInfoForm" property="eventType" value="Special Event">
		<TR>
			<TD nowrap> Sponsoring Organization: </TD>
			<TD>
				<html:select name="eventAddInfoForm" property="sponsoringOrgId"
					onfocus="setUp();" 
    				onkeypress="return selectSearch(event, this);" 
					onkeydown="return checkKey(event, this);">
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:optionsCollection name="eventAddInfoForm" property="sponsoringOrgs" label="name" value="uniqueId"/>
				</html:select>				
			</TD>
		</TR>
		</logic:equal>		
		<TR>
  		<TD valign='top'>Main Contact: </TD> 
			<TD colspan ='2'>
				<Table width='100%'>
					<html:hidden property="mainContactExternalId" styleId="uid"/>
					<TR>
						<TD nowrap> First Name: </TD>
						<TD>
							<html:text property="mainContactFirstName" maxlength="20" size="30" styleId="fname" />
							<logic:equal name="eventAddInfoForm" property="mainContactLookup" value="true">
								<input type='button' value='Lookup' onclick="window.open('user/peopleLookup.jsp?query='+mainContactFirstName.value+' '+mainContactLastName.value,'peopleLookup','width=800,height=600,resizable=no,scrollbars=yes,toolbar=no,location=no,directories=no,status=no,menubar=no,copyhistory=no').focus();" style="btn"> 
							</logic:equal>
						</TD>
					</TR>
					<TR>
						<TD nowrap> Last Name<font color='red'>*</font>: </TD>
						<TD>
							<html:text property="mainContactLastName" maxlength="30" size="30" styleId="lname"/> 
						</TD>
					</TR>
					<TR>
						<TD nowrap>Email<font color='red'>*</font>: </TD>
						<TD>
							<html:text property="mainContactEmail" maxlength="100" size="30" styleId="email"/>
						</TD>
					</TR>
					<TR>
						<TD nowrap>Phone: </TD>
						<TD>
							<html:text property="mainContactPhone" maxlength="10" size="30" styleId="phone"/> <i><font color="gray">&nbsp; Max. 10 digits </font></i>
						</TD>
					</TR>
				</Table>
			</TD>
		</TR>
		<TR>
			<TD valign='top'>Additional E-mails:<br>
			<font color="gray"><i>(will be notified about event changes)</i> </font></TD>
			<TD>
				<html:textarea property="additionalEmails" rows="2" cols="50"></html:textarea>
			</TD>
		</TR>
		<TR>
			<TD nowrap valign='top'>Additional Information: </TD>
			<TD>
				<html:textarea property="additionalInfo" rows="2" cols="50"></html:textarea>
			</TD>
		</TR>
		<TR>
			<TD colspan='2'>
				<font color='red'>*</font> <i> Fields marked with a red asterix are mandatory.</i> 
			</TD>
		</TR>
		</logic:notEqual>


<!-- A list of selected dates & times & locations -->
		<TR>
			<TD colspan="2" valign="middle">
				<br>
				<tt:section-title>
					New Meetings (as selected in previous screens)
				</tt:section-title>
			</TD>
		</TR>
		<TR><TD colspan='2'>
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="1">
			<TR>
				<td><font color="gray"><i>Date</i></font></td><td><font color="gray"><i>Time</i></font></td><td><font color="gray"><i>Location</i></font></td><td><font color="gray"><i>Capacity</i></font></td>
			</TR>
			<logic:iterate name="eventAddInfoForm" property="newMeetings" id="meeting">
				<TR onmouseover="this.style.backgroundColor='rgb(223,231,242)';" onmouseout="this.style.backgroundColor='transparent';">
					<TD>
						<bean:write name="meeting" property="date"/> 
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
				</TR>	
				<logic:iterate name="meeting" property="overlaps" id="overlap">
					<TR style="background-color:#FFD7D7;">
						<TD>&nbsp;&nbsp;&nbsp;Conflicts with <bean:write name="overlap" property="name"/> (<bean:write name="overlap" property="type"/>)</TD>
						<TD><bean:write name="overlap" property="startTime"/> - <bean:write name="overlap" property="endTime"/></TD>
						<TD></TD>
						<TD>
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
		</TABLE>
		</TD></TR>

<!-- Courses/Classes if this is a course event -->
		<logic:equal name="eventAddInfoForm" property="eventType" value="Course Event">
		<logic:notEqual name="eventAddInfoForm" property="isAddMeetings" value="true">
		<TR>
			<TD colspan='2'>
				&nbsp;
			</TD>
		</TR>
		<TR>
			<TD colspan='2' valign="middle">
				<br>
				<tt:section-title>
					Related Classes / Courses
				</tt:section-title>
			</TD>
		</TR>
		<TR>
			<TD colspan='2'>
				<Table width='100%' cellspacing="0" cellpadding="3">
					<bean:write name="eventAddInfoForm" property="relatedCoursesTable" filter="false"/>
				</Table>
			</TD>
		</TR>
		<logic:equal name="eventAddInfoForm" property="attendanceRequired" value="true">
		<TR>
			<TD colspan='2'>
				<i>Students of the listed courses/classes are required to attend this event.</i>
			</TD>
		</TR>
		</logic:equal>
		</logic:notEqual>
		
		<logic:equal name="eventAddInfoForm" property="isAddMeetings" value="true">
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
		<logic:equal name="eventAddInfoForm" property="attendanceRequired" value="true">
		<TR>
			<TD colspan='2'>
				<i>Students of the listed courses/classes are required to attend this event.</i>
			</TD>
		</TR>
		</logic:equal>
		</logic:equal>		
		</logic:equal>
		

<!--  Footer - another set of buttons -->
		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>

		<TR>
			<TD align='right' colspan='2'>
					<logic:equal name="eventAddInfoForm" property="isAddMeetings" value="true">
					<html:submit property="op" styleClass="btn" accesskey="U"
						title="Update Event With New Meetings (Alt+U)" value="Update"/>
					<html:submit property="op" styleClass="btn" accesskey="C"
						title="Cancel Request For More Meetings (Alt+C)" value="Cancel"/>
					</logic:equal>
					<logic:notEqual name="eventAddInfoForm" property="isAddMeetings" value="true">				
					<html:submit property="op" styleClass="btn" accesskey="S"
						title="Submit Reservation (Alt+S)" value="Submit"/>
					<html:submit property="op" styleClass="btn" accesskey="C"
						title="Cancel Event (Alt+C)" value="Cancel Event"/>
					</logic:notEqual>
					<logic:equal name="eventAddInfoForm" property="canChangeSelection" value="true">
					<html:submit property="op" styleClass="btn" accesskey="A"
						title="Change Selection (Alt+A)" value="Change Selection"/>
					</logic:equal>
			</TD>
		</TR>
		
</TABLE>
</html:form>
