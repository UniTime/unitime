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
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<tiles:importAttribute />

<html:form action="/eventEdit">
	<html:hidden property = "id"/>
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
					<tt:section-title><bean:write name="eventEditForm" property="eventName"/></tt:section-title>
					<html:submit property="op" styleClass="btn" accesskey="U"
						title="Update Event (Alt+U)" value="Update"/>
					<html:submit property="op" styleClass="btn" accesskey="B"
						title="Back to Event Detail (Alt+B)" value="Back"/>
				</tt:section-header>
			</TD>
		</TR>


<!-- Data to be edited about the event -->
		<TR>
			<TD nowrap> Event Name<font color='red'>*</font>: </TD>
			<TD> 
				<html:text property="eventName" maxlength="100" size="50" /> 
			</TD>
		</TR>
		<logic:equal name="eventEditForm" property="eventType" value="Special Event">
		<TR>
			<TD nowrap> Sponsoring Organization: </TD>
			<TD>
				<html:select name="eventEditForm" property="sponsoringOrgId"
					onfocus="setUp();" 
    				onkeypress="return selectSearch(event, this);" 
					onkeydown="return checkKey(event, this);">
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:optionsCollection name="eventEditForm" property="sponsoringOrgs" label="name" value="uniqueId"/>
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
							<logic:equal name="eventEditForm" property="mainContactLookup" value="true">
								<input type='button' value='Lookup' onclick="window.open('user/peopleLookup.jsp?query='+mainContactFirstName.value+' '+mainContactLastName.value,'peopleLookup','width=800,height=600,resizable=no,scrollbars=yes,toolbar=no,location=no,directories=no,status=no,menubar=no,copyhistory=no').focus();" style="btn"> 
							</logic:equal>
						</TD>
					</TR>
					<TR>
						<TD nowrap> Middle Name: </TD>
						<TD>
							<html:text property="mainContactMiddleName" maxlength="20" size="30" styleId="mname" />
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
							<html:text property="mainContactPhone" maxlength="25" size="30" styleId="phone"/>
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

<!-- A list of event meetings -->
		<TR>
			<TD colspan="2" valign="middle">
				<br>
				<tt:section-title>
					Event Meetings
				</tt:section-title>
			</TD>
		</TR>
		<TR><TD colspan='2'>
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="1">
			<TR align="left">
				<td><font color="gray"><i>Date</i></font></td><td><font color="gray"><i>Time</i></font></td><td><font color="gray"><i>Location</i></font></td><td><font color="gray"><i>Capacity</i></font></td>
			</TR>
			<logic:iterate name="eventEditForm" property="existingMeetings" id="meeting">
				<TR onmouseover="this.style.backgroundColor='rgb(223,231,242)';" onmouseout="this.style.backgroundColor='transparent';">
					<TD>
						<bean:write name="meeting" property="date" filter="false"/> 
					</TD>
					<TD>
						<bean:write name="meeting" property="time"/>
					</TD>
					<TD>
						<bean:write name="meeting" property="location"/>
					</TD>	
					<TD>
						<bean:write name="meeting" property="locationCapacity"/>
					</TD>	
				</TR>	
			</logic:iterate>
		</TABLE>
		</TD>
		</TR>

<!-- Courses/Classes if this is a course event -->
		<logic:equal name="eventEditForm" property="eventType" value="Course Event">
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
					<bean:write scope="request" name="EventDetail.table" filter="false"/>
				</Table>
			</TD>
		</TR>
		<logic:equal name="eventEditForm" property="attendanceRequired" value="true">
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
					<html:submit property="op" styleClass="btn" accesskey="U"
						title="Update Event (Alt+U)" value="Update"/>
					<html:submit property="op" styleClass="btn" accesskey="B"
						title="Back to Event Detail (Alt+B)" value="Back"/>
			</TD>
		</TR>
		
</TABLE>
</html:form>
