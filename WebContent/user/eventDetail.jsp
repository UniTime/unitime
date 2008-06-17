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
		<logic:equal name="eventDetailForm" property="canEdit" value="true">
			<TR>
				<TD nowrap valign="top">Contact:&nbsp;</TD>
				<td>
				<Table width="100%" border="0" cellspacing="0" cellpadding="1">
					<tr align="left">
						<td><i>Name</i></td><td><i>E-mail</i></td><td><i>Phone</i></td>
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
			</TR>		
		</logic:equal>



		<TR>
			<TD colspan='2'>&nbsp;</TD>
		</TR>

		<TR>
			<TD colspan='2'>
				<tt:section-header>
				<tt:section-title>Meetings</tt:section-title>
				<logic:equal name="eventDetailForm" property="canEdit" value="true">
					<html:submit property="op" styleClass="btn">Add Meeting</html:submit>
				</logic:equal>
				</tt:section-header>
			</TD>
		</TR>

	<tr><td colspan='2'>
	<Table width="100%" border="0" cellspacing="0" cellpadding="1">
		<tr align="left">
			<th><font size = -1 >Date</font></th><th><font size = -1 >Time</font></th><th><font size = -1 >Location</font></th><th><font size = -1 >Approved</font></th>
		</tr>
		<html:hidden property="selected"/>
		<logic:iterate name="eventDetailForm" property="meetings" id="meeting">
			<bean:define name="meeting" property="id" id="meetingId"/>
			<tr>
				<td>
					<bean:write name="meeting" property="date" filter="false"/> 
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
					<logic:equal name="eventDetailForm" property="canEdit" value="true">
						<html:submit property="op" styleClass="btn" onclick="<%="selected.value='"+meetingId+"';"%>">Delete</html:submit>
					</logic:equal>
				</td>
			</tr>	
		</logic:iterate>
	</Table>
	</td></tr>

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



