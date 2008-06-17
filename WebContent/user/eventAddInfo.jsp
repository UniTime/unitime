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
					<html:submit property="op" styleClass="btn" accesskey="S"
						title="Submit Reservation (Alt+S)" value="Submit"/>
					<html:submit property="op" styleClass="btn" accesskey="C"
						title="Cancel Event (Alt+C)" value="Cancel Event"/>
					<html:submit property="op" styleClass="btn" accesskey="B"
						title="Back To Event Room Availability (Alt+B)" value="Back"/>
				</tt:section-header>
			</TD>
		</TR>

<!-- A list of selected dates & times & locations -->
		<TR><TD colspan='2'>
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="1">
			<TR align="left">
				<th><font size = -1 >Date</font></th><th><font size = -1 >Time</font></th><th><font size = -1 >Location</font></th>
			</TR>
			<logic:iterate name="eventAddInfoForm" property="dateLocations" id="dl">
				<TR>
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


<!-- Data to be filled in about the event -->
		<TR>
			<TD> &nbsp; </TD>
			<TD> &nbsp; </TD>
		</TR>
		<TR>
			<TD nowrap> Event Name<font color='red'>*</font>: </TD>
			<TD> 
				<html:text property="eventName" maxlength="50" size="50" /> 
			</TD>
		</TR>
		<TR>
  		<TD valign='top'>Contact: </TD> 
			<TD colspan ='2'>
				<Table width='100%'>
					<TR>
						<TD nowrap> First Name: </TD>
						<TD>
							<html:text property="mainContactFirstName" maxlength="50" size="50" /> 
						</TD>
					</TR>
					<TR>
						<TD nowrap> Last Name: </TD>
						<TD>
							<html:text property="mainContactLastName" maxlength="50" size="50" /> 
						</TD>
					</TR>
					<TR>
						<TD nowrap>Email<font color='red'>*</font>: </TD>
						<TD>
							<html:text property="mainContactEmail" maxlength="50" size="50" /> 
						</TD>
					</TR>
					<TR>
						<TD nowrap>Phone<font color='red'>*</font>: </TD>
						<TD>
							<html:text property="mainContactPhone" maxlength="50" size="50" /> 
						</TD>
					</TR>
				</Table>
			</TD>
		</TR>
		<TR>
			<TD nowrap>Additional Information: </TD>
			<TD>
				<html:textarea property="additionalInfo" rows="2" cols="50"></html:textarea>
			</TD>
		</TR>
		<TR>
			<TD colspan='2'>
				<font color='red'>*</font> <i> Fields marked with red asterix are mandatory.</i> 
			</TD>
		</TR>


<!--  Footer - another set of buttons -->
		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>

		<TR>
			<TD align='right' colspan='2'>
					<html:submit property="op" styleClass="btn" accesskey="S"
						title="Submit Reservation (Alt+S)" value="Submit"/>
					<html:submit property="op" styleClass="btn" accesskey="C"
						title="Cancel Event (Alt+C)" value="Cancel Event"/>
					<html:submit property="op" styleClass="btn" accesskey="B"
						title="Back To Event Room Availability (Alt+B)" value="Back"/>
			</TD>
		</TR>
		
</TABLE>
</html:form>
