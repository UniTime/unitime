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

<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@page import="org.unitime.timetable.form.EventListForm"%>
<%@page import="org.unitime.timetable.webutil.WebEventTableBuilder"%>

<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>

<tiles:importAttribute />
<html:form action="/eventList">
<script language="JavaScript">blToggleHeader('Filter','dispFilter');blStart('dispFilter');</script>
<TABLE border="0" cellspacing="0" cellpadding="3" width='90%'>
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
			<TD>Event Name:</TD>
			<TD>
				<html:text property="eventNameSubstring" maxlength="50" size="50" /> 
			<TD>
		</TR>
		<TR>
			<TD>Requested By: </TD>
			<TD>
				<html:text property="eventMainContactSubstring" maxlength="50" size="50" /> 
			</TD> 
		</TR>
		<%--
		<TR>
			<TD>Sponsoring Organization: </TD>
			<TD>
				<html:text property="eventSponsoringOrganization" maxlength="50" size="50" /> 
			</TD> 
		</TR>
		--%>
		
		<TR>
			<TD>Date: </TD>
			<TD>
				From: <html:text property="eventDateFrom" maxlength="10" size="10" styleId="event_date_from"/> 
				<img style="cursor: pointer;" src="scripts/jscalendar/calendar_1.gif" 
				border="0" id="show_event_date_from">
				To: <html:text property="eventDateTo" maxlength="10" size="10" styleId="event_date_to"/>
				<img style="cursor: pointer;" src="scripts/jscalendar/calendar_1.gif" 
				border="0" id="show_event_date_to">  
			</TD> 
		</TR>
		<%--
		<TR>
			<TD>Time: </TD>
			<TD>
				From: <html:text property="eventTimeFrom" maxlength="10" size="10" /> 
				To: <html:text property="eventTimeTo" maxlength="10" size="10" />  
			</TD> 
		</TR>
		--%>
		<TR>
			<TD valign="top">Event Type:</TD>
			<TD>
				<logic:iterate name="eventListForm" property="allEventTypes" id="type">
					<html:multibox property="eventTypes">
						<bean:write name="type" property="reference"/>
					</html:multibox>
					<bean:write name="type" property="label" /><br>
				</logic:iterate>
			<TD>
		</TR>
		<TR>
			<TD colspan='2' align='right'>
				<html:submit property="op" value="Search" onclick="displayLoading();"/>
			</TD>
		</TR>
</TABLE>
<script language="JavaScript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>

<script language="JavaScript">blEndCollapsed('dispFilter');</script>
<br>

	<% 
		EventListForm form = (EventListForm)request.getAttribute("eventListForm");
		new WebEventTableBuilder().htmlTableForEvents(session,form,out);
	%>

	<logic:notEmpty scope="request" name="hash">
		<SCRIPT type="text/javascript" language="javascript">
			location.hash = '<%=request.getAttribute("hash")%>';
		</SCRIPT>
	</logic:notEmpty>
</html:form>


<script type="text/javascript" language="javascript">
 
 Calendar.setup( {
  cache      : true,      // Single object used for all calendars
  electric   : false,     // Changes date only when calendar is closed
  inputField : "event_date_from",  // ID of the input field
     ifFormat   : "%m/%d/%Y",    // Format of the input field
     showOthers : true,     // Show overlap of dates from other months     
     <% if (request.getParameter("eventDateFrom")!=null && request.getParameter("eventDateFrom").length()>=10) { %>
     date  : <%=request.getParameter("eventDateFrom")%>,
     <% }%>
  button     : "show_event_date_from" // ID of the button
 } );

 Calendar.setup( {
  cache      : true,      // Single object used for all calendars
  electric   : false,     // Changes date only when calendar is closed
  inputField : "event_date_to",  // ID of the input field
     ifFormat   : "%m/%d/%Y",    // Format of the input field
     showOthers : true,     // Show overlap of dates from other months     
     <% if (request.getParameter("eventDateTo")!=null && request.getParameter("eventDateTo").length()>=10) { %>
     date  : <%=request.getParameter("eventDateTo")%>,
     <% }%>
  button     : "show_event_date_to" // ID of the button
 } );
 
 
 
 
 </script>