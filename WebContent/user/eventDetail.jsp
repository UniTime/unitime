<%--
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org
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
* Font of table headers for meetings (should be <font size = -1 ></font>)
*    - how to make a style for the header???
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
	<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title><bean:write name="eventDetailForm" property="eventName"/></tt:section-title>
					<html:submit property="op" styleClass="btn">Edit Event</html:submit>
					<html:submit property="op" styleClass="btn">Previous</html:submit>
					<html:submit property="op" styleClass="btn">Next</html:submit>
					<html:submit property="op" styleClass="btn">Back</html:submit>
				</tt:section-header>
			</TD>
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
		<TR>
			<TD nowrap>Sponsoring Organization:&nbsp;</TD>
			<TD width='100%'>
				<font color="gray">
				<bean:write name="eventDetailForm" property="sponsoringOrg"/>
				</font>
			</TD>
		</TR>
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

		<TR>
			<TD colspan='2'>&nbsp;</TD>
		</TR>

		<TR>
			<TD colspan='2'>
				<tt:section-header>
				<tt:section-title>Meetings</tt:section-title>
				<html:submit property="op" styleClass="btn">Add Meeting</html:submit>
				</tt:section-header>
			</TD>
		</TR>

	<tr><td colspan='2'>
	<Table width="100%" border="0" cellspacing="0" cellpadding="1">
		<tr align="left">
			<th>Date</th><th>Time</th><th>Location</th><th>Approved</th>
		</tr>
		<html:hidden property="selected"/>
		<logic:iterate name="eventDetailForm" property="meetings" id="meeting">
			<bean:define name="meeting" property="id" id="meetingId"/>
			<tr>
				<td>
					<bean:write name="meeting" property="date"/>
				</td>
				<td>
					<bean:write name="meeting" property="startTime"/> - <bean:write name="meeting" property="endTime"/>
				</td>
				<td>
					<bean:write name="meeting" property="location"/>
				</td>	
				<td>
					<bean:write name="meeting" property="approvedDate"/>
				</td>			
				<td>
					<html:submit property="op" styleClass="btn" onclick="<%="selected.value='"+meetingId+"';"%>">Delete</html:submit>
				</td>
			</tr>	
		</logic:iterate>
	</Table>
	</td></tr>

		<TR>
			<TD colspan='2'>&nbsp;</TD>
		</TR>

		<TR>
			<TD colspan='2'>
				<tt:section-header>
				<tt:section-title>Contacts</tt:section-title>
				</tt:section-header>
			</TD>
		</TR>
		<tr>
			<td colspan='2'>
				<Table width="100%" border="0" cellspacing="0" cellpadding="1">
				<tr align="left">
					<th>Name</th><th>E-mail</th><th>Phone</th>
				</tr>
				<logic:notEmpty name="eventDetailForm" property="mainContact">
				<bean:define name="eventDetailForm" property="mainContact" id="mc"/>
				<TR>
					<TD>
						<bean:write name="mc" property="firstName"/>
						<bean:write name="mc" property="middleName"/> 
						<bean:write name="mc" property="lastName"/> <i>(main contact)</i>
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
		</tr>
	</TABLE>

</html:form>



