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
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<script language='JavaScript' type='text/javascript' src='scripts/datepatt.js'></script>

<tiles:importAttribute />

<html:form action="/eventAdd">
	<input type="hidden" name="op2" value="">
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
					<tt:section-title>Add New Event</tt:section-title>
				</tt:section-header>
			</TD>
		</TR>
		<TR>
			<TD nowrap>Event Type: </TD>
			<TD>
				<html:select name="eventAddForm" property="eventType"
					onfocus="setUp();"
					onkeypress="return selectSearch(event, this);"
					onkeydown="return checkKey(event, this);"
					onchange="op2.value='EventTypeChanged'; submit();">
					<html:options name="eventAddForm" property="eventTypes"/>
				</html:select>
			</TD>
		</TR>
<!-- 		<TR>
			<TD>&nbsp;</TD>
		</TR>
		<TR>
 			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>Dates</tt:section-title>
				</tt:section-header>
			</TD>
		</TR>
 -->	<TR>
			<TD nowrap>Academic Session: </TD>
			<TD>
				<html:select name="eventAddForm" property="sessionId"  
					onfocus="setUp();" 
    				onkeypress="return selectSearch(event, this);" 
					onkeydown="return checkKey(event, this);"
					onchange="op2.value='SessionChanged'; submit();" > 
 					<html:optionsCollection property="academicSessions"	label="label" value="value" />
				</html:select>
			</TD>
		</TR>
		<TR>
			<TD colspan='2'>&nbsp;</TD>
		</TR>
		<TR>
			<TD colspan = '2'>
				<tt:displayPrefLevelLegend prefs="false" dpBackgrounds="true" separator=""/>
			</TD>
		</TR>
		<TR>
			<TD colspan = '2'>
				<bean:write name="eventAddForm" property="datesTable" filter="false"/>
			</TD>
		</TR>
		
<!--		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>Times</tt:section-title>
				</tt:section-header>
			</TD>
		</TR>
-->		<TR>
			<TD nowrap>Time: </TD>
			<TD> Start:&nbsp;
				<html:select name="eventAddForm" property="startTime"
					onfocus="setUp();" 
    				onkeypress="return selectSearch(event, this);" 
					onkeydown="return checkKey(event, this);">
					<html:optionsCollection name="eventAddForm" property="times"/>
				</html:select>
			
				&nbsp;&nbsp;
				Stop: 
				<html:select name="eventAddForm" property="stopTime"
					onfocus="setUp();" 
    				onkeypress="return selectSearch(event, this);" 
					onkeydown="return checkKey(event, this);">
					<html:optionsCollection name="eventAddForm" property="times"/>
				</html:select> 
		</TR>
<!--		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>Locations</tt:section-title>
				</tt:section-header>
			</TD>
		</TR>
-->		<TR>
			<TD>Location Type:&nbsp;</TD>
			<TD>			
				<html:select property = "locationType">
					<html:optionsCollection name="eventAddForm" property = "locationTypes"  label="label" value="value"/>
				</html:select>				
			</TD>
		</TR>
		<TR>
			<TD>Location: </TD>
			<TD>Building:
				<html:select name="eventAddForm" property="buildingId"
					onfocus="setUp();" 
    				onkeypress="return selectSearch(event, this);" 
					onkeydown="return checkKey(event, this);">
					<html:option value="-1">Select...</html:option>
					<html:optionsCollection name="eventAddForm" property="buildings" label="abbrName" value="uniqueId"/>
				</html:select> 			
			&nbsp; Room Number:&nbsp; <html:text property="roomNumber" maxlength="10" size="10"/></TD>
		</TR>
		<TR>
			<TD> 
				Room Capacity:
			</TD>
			<TD>
				Min: <html:text property="minCapacity" maxlength="5" size="5"/> &nbsp; Max: <html:text property="maxCapacity" maxlength="5" size="5"/>
			</TD>
		</TR>


	<TR>
		<TD colspan = '2'>
			<tt:section-title/>
		</TD>
	</TR>

	<TR>
		<TD colspan = '2'>
			<html:submit property="op" styleClass="btn" accesskey="S" 
				title="Show Scheduled Events (Alt+S)" value="Show Scheduled Events"/>
			<html:submit property="op" styleClass="btn" accesskey="A" 
				title="Show Location Availability (Alt+A)" value="Show Availability"/>
		</TD>
	</TR>


</TABLE>
</html:form>
