<%--
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
--%>

<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@page import="org.unitime.timetable.form.MeetingListForm"%>
<%@page import="org.unitime.timetable.webutil.WebEventTableBuilder"%>

<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>

<tiles:importAttribute />
<tt:session-context/>
<html:form action="/meetingList">
<script language="JavaScript">blToggleHeader('Filter','dispFilter');blStart('dispFilter');</script>
<TABLE border="0" cellspacing="0" cellpadding="3" width='100%'>
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
			</TD>
		</TR>
		<sec:authorize access="hasPermission(null, null, 'HasRole')">
		<TR>
			<TD>Requested By: </TD>
			<TD>
				<html:text property="eventMainContactSubstring" maxlength="50" size="50" /> 
			</TD> 
		</TR>
		</sec:authorize>
		<TR>
			<TD>Location: </TD>
			<TD>
				<html:text property="location" maxlength="50" size="50" /> 
			</TD> 
		</TR>
		<TR>
			<TD>Sponsoring Organization: </TD>
			<TD>
				<html:select property="sponsoringOrganization">
					<html:option value="-1">All Organizations</html:option>
					<html:optionsCollection property="sponsoringOrganizations" label="name" value="uniqueId"/>
				</html:select>
			</TD> 
		</TR>
		
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
		<TR>
			<TD nowrap>Time: </TD>
			<TD>
				<html:checkbox property="dayMon"/>
				Mon&nbsp;
				<html:checkbox property="dayTue"/>
				Tue&nbsp;
				<html:checkbox property="dayWed"/>
				Wed&nbsp;
				<html:checkbox property="dayThu"/>
				Thu&nbsp;
				<html:checkbox property="dayFri"/>
				Fri&nbsp;
				<html:checkbox property="daySat"/>
				Sat&nbsp;
				<html:checkbox property="daySun"/>
				Sun
				<br/>
				From:&nbsp;
				<html:select name="meetingListForm" property="startTime"
					onfocus="setUp();" 
    				onkeypress="return selectSearchTime(event, this);" 
					onkeydown="return checkKey(event, this);">
					<html:optionsCollection name="meetingListForm" property="times"/>
				</html:select>
				
				&nbsp;&nbsp;
				To: 
				<html:select name="meetingListForm" property="stopTime"
					onfocus="setUp();" 
    				onkeypress="return selectSearchTime(event, this);" 
					onkeydown="return checkKey(event, this);">
					<html:optionsCollection name="meetingListForm" property="stopTimes"/>
				</html:select> 
		</TR>
		<TR>
			<TD valign="top">Event Type:</TD>
			<TD>
				<logic:iterate name="meetingListForm" property="allEventTypes" id="type" indexId="typeIdx">
					<html:multibox property="eventTypes">
						<bean:write name="typeIdx"/>
					</html:multibox>
					<bean:write name="type"/><br>
				</logic:iterate>
			</TD>
		</TR>
		<TR>
			<TD valign="top">Mode:</TD>
			<TD>
				<bean:define id="modes" name="modes" scope="request"/>
				<html:select property="mode">
					<html:optionsCollection name="modes" label="label" value="value"/>
				</html:select>
				<sec:authorize access="hasPermission(null, null, 'HasRole')">
					&nbsp;&nbsp;&nbsp;<html:checkbox property="dispConflicts"/> Display Conflicts
				</sec:authorize>
			</TD>
		</TR>
		<TR>
			<TD>Sort By: </TD>
			<TD>
				<html:select property="orderBy">
					<html:options property="orderBys"/>
				</html:select>
			</TD> 
		</TR>
		
		<TR>
			<TD colspan='2' align='right'>
				<html:submit property="op" value="Search" onclick="displayLoading();" accesskey="S" title="Search (Alt+S)"/> 
				&nbsp; 
				<html:submit property="op" value="Export PDF" onclick="displayLoading();" accesskey="P" title="Export to PDF (Alt+P)"/> 
				&nbsp; 
				<html:submit property="op" value="Export CSV" onclick="displayLoading();" accesskey="C" title="Export to CSV (Alt+C)"/> 
				&nbsp; 
				<html:submit property="op" value="iCalendar" onclick="displayLoading();" accesskey="I" title="Export iCalendar (Alt+I)"/> 
				&nbsp; 
				<html:submit property="op" value="Add Event" accesskey="A" title="Add Event (Alt+A)"/>
			</TD>
		</TR>
</TABLE>
<script language="JavaScript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
<TABLE border="0" cellspacing="0" cellpadding="3" width='100%'>	
	<TR>
		<TD align='right'>
				<html:submit property="op" value="Export PDF" onclick="displayLoading();" accesskey="P" title="Export to PDF (Alt+P)"/> 
				&nbsp; 
				<html:submit property="op" value="Export CSV" onclick="displayLoading();" accesskey="C" title="Export to CSV (Alt+C)"/> 
				&nbsp; 
				<html:submit property="op" value="iCalendar" onclick="displayLoading();" accesskey="I" title="Export iCalendar (Alt+I)"/> 
				&nbsp; 
				<html:submit property="op" value="Add Event" accesskey="A" title="Add Event (Alt+A)"/>
		</TD>
	</TR>
</TABLE>
<script language="JavaScript">blEndCollapsed('dispFilter');</script>
<br>

	<% 
		MeetingListForm form = (MeetingListForm)request.getAttribute("meetingListForm");
		new WebEventTableBuilder().htmlTableForMeetings(sessionContext,form,out);
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
  date		: <%=request.getAttribute("eventDateFrom") != null && ((String)request.getAttribute("eventDateFrom")).length() >= 10 ? (String)request.getAttribute("eventDateFrom") : null%>,
  button     : "show_event_date_from" // ID of the button
 } );

 Calendar.setup( {
  cache      : true,      // Single object used for all calendars
  electric   : false,     // Changes date only when calendar is closed
  inputField : "event_date_to",  // ID of the input field
     ifFormat   : "%m/%d/%Y",    // Format of the input field
     showOthers : true,     // Show overlap of dates from other months     
  date		: <%=request.getAttribute("eventDateTo") != null && ((String)request.getAttribute("eventDateTo")).length() >= 10 ? (String)request.getAttribute("eventDateTo") : null%>,
  button     : "show_event_date_to" // ID of the button
 } );
 
 
 
 
 </script>
