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
					<html:submit property="op" styleClass="btn" accesskey="B"
						title="Back To Event Room Availability (Alt+B)" value="Back"/>
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
			<TD nowrap valign="top">Contact:&nbsp;</TD>
			<td>
			<Table width="100%" border="0" cellspacing="0" cellpadding="1">
				<tr align="left">
					<td><i>Name</i></td><td><i>E-mail</i></td><td><i>Phone</i></td>
				</tr>
				<TR>
					<TD>
						<bean:write name="eventAddInfoForm" property="mainContactFirstName"/>
						<bean:write name="eventAddInfoForm" property="mainContactLastName"/> <i>(main contact)</i>
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
				<td><font color="gray"><i>Date</i></font></td><td><font color="gray"><i>Time</i></font></td><td><font color="gray"><i>Location</i></font></td>
			</TR>
			<logic:iterate name="eventAddInfoForm" property="existingMeetings" id="meeting">
				<TR>
					<TD>
						<bean:write name="meeting" property="date" filter="false"/> 
					</TD>
					<TD>
						<bean:write name="meeting" property="startTime"/> - <bean:write name="meeting" property="endTime"/>
					</TD>
					<TD>
						<bean:write name="meeting" property="location"/>
					</TD>	
				</TR>	
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
		<TR>
  		<TD valign='top'>Contact: </TD> 
			<TD colspan ='2'>
				<Table width='100%'>
					<TR>
						<TD nowrap> First Name: </TD>
						<TD>
							<html:text property="mainContactFirstName" maxlength="20" size="30" /> 
						</TD>
					</TR>
					<TR>
						<TD nowrap> Last Name: </TD>
						<TD>
							<html:text property="mainContactLastName" maxlength="30" size="30" /> 
						</TD>
					</TR>
					<TR>
						<TD nowrap>Email<font color='red'>*</font>: </TD>
						<TD>
							<html:text property="mainContactEmail" maxlength="100" size="30" />
						</TD>
					</TR>
					<TR>
						<TD nowrap>Phone<font color='red'>*</font>: </TD>
						<TD>
							<html:text property="mainContactPhone" maxlength="10" size="30" /> <i><font color="gray">&nbsp; Max. 10 digits </font></i>
						</TD>
					</TR>
				</Table>
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
				<td><font color="gray"><i>Date</i></font></td><td><font color="gray"><i>Time</i></font></td><td><font color="gray"><i>Location</i></font></td>
			</TR>
			<logic:iterate name="eventAddInfoForm" property="dateLocations" id="dl">
				<TR onmouseover="this.style.backgroundColor='rgb(223,231,242)';" onmouseout="this.style.backgroundColor='transparent';">
					<TD>
						<bean:write name="dl" property="dateLabel"/> 
					</TD>
					<TD>
						<bean:write name="eventAddInfoForm" property="startTimeString"/> - <bean:write name="eventAddInfoForm" property="stopTimeString"/>
					</TD>
					<TD>
						<bean:write name="dl" property="locationLabel"/>
					</TD>	
				</TR>	
			</logic:iterate>
		</TABLE>
		</TD></TR>

<!-- Courses/Classes if this is a course event -->
		<logic:equal name="eventAddInfoForm" property="eventType" value="Course Event">
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
					<html:submit property="op" styleClass="btn" accesskey="B"
						title="Back To Event Room Availability (Alt+B)" value="Back"/>
			</TD>
		</TR>
		
</TABLE>
</html:form>
