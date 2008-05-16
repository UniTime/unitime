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
<%@page import="org.unitime.timetable.webutil.JavascriptFunctions"%>
<%@page import="org.unitime.commons.web.Web"%>
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
		<TR>
			<TD valign="middle">
				<tt:section-header>
					<tt:section-title>Add New Event</tt:section-title>
				</tt:section-header>
			</TD>
		</TR>
		<TR>
			<TD nowrap>Event Type:
				<html:select property="eventType">
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
			<TD nowrap>Academic Session:
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
			<TD>&nbsp;</TD>
		</TR>
		<TR>
			<TD>
				<tt:displayPrefLevelLegend prefs="false" dpBackgrounds="true" separator=""/>
			</TD>
		</TR>
		<TR>
			<TD>
				<bean:write name="dates" scope="request" filter="false"/>
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
			<TD nowrap colspan="4">Start Time: &nbsp;<html:text property="startTime" maxlength="10" size="10"/> &nbsp; &nbsp; 
			Stop Time:&nbsp; <html:text property="stopTime" maxlength="10" size="10"/></TD> 
		</TR>
<!--		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>Locations</tt:section-title>
				</tt:section-header>
			</TD>
		</TR>
-->		<TR>
			<TD>Location Type:&nbsp;
					<html:select property = "locationType">
					<html:optionsCollection name="eventAddForm" property = "locationTypes"  label="label" value="value"/>
				</html:select>				
			</TD>
		</TR>
		<TR>
			<TD>Building: &nbsp; <html:text property="startTime" maxlength="10" size="10"/>&nbsp;&nbsp;
			Room Number:&nbsp; <html:text property="startTime" maxlength="10" size="10"/></TD>
		</TR>


	<TR>
		<TD>
			<tt:section-title/>
		</TD>
	</TR>

	<TR>
		<TD>
			<html:submit property="op" styleClass="btn" accesskey="S" 
				title="Show Scheduled Events (Alt+S)" value="Show Scheduled Events"/>
			<html:submit property="op" styleClass="btn" accesskey="A" 
				title="Show Room Availability (Alt+A)" value="Show Room Availability"/>
		</TD>
	</TR>


</TABLE>
</html:form>
