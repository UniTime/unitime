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
 --%>
<%@ page language="java" autoFlush="true"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<tiles:importAttribute />

<html:form action="/examPeriodEdit">

<logic:notEqual name="examPeriodEditForm" property="op" value="List">
	<html:hidden property="uniqueId"/><html:errors property="uniqueId"/>
	<html:hidden property="autoSetup"/>
	<logic:equal name="examPeriodEditForm" property="autoSetup" value="true">
	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						Setup Midterm Examination Periods
					</tt:section-title>
					<html:submit property="op">
						<bean:write name="examPeriodEditForm" property="op" />
					</html:submit> 
					<html:submit property="op" value="Back" /> 
				</tt:section-header>
			</TD>
		</TR>
		
		<TR>
			<TD>Type:</TD>
			<TD>
				<html:select property="examType" disabled="true">
					<html:options property="examTypes"/>
				</html:select>
				<html:hidden property="examType"/>
			</TD>
		</TR>
		

		<TR>
			<TD>1st Period Start Time:</TD>
			<TD>
			<html:text property="start" size="4" maxlength="4"/> (in military format)
			&nbsp;<html:errors property="start"/>
			</TD>
		</TR>

		<TR>
			<TD>1st Period Length:</TD>
			<TD>
			<html:text property="length" size="4" maxlength="4"/> (in minutes)
			&nbsp;<html:errors property="length"/>
			</TD>
		</TR>

		<TR>
			<TD>2nd Period Start Time:</TD>
			<TD>
			<html:text property="start2" size="4" maxlength="4"/> (in military format)
			&nbsp;<html:errors property="start2"/>
			</TD>
		</TR>

		<TR>
			<TD>2nd Period Length:</TD>
			<TD>
			<html:text property="length2" size="4" maxlength="4"/> (in minutes)
			&nbsp;<html:errors property="length2"/>
			</TD>
		</TR>

		<TR>
			<TD>3rd Period Start Time:</TD>
			<TD>
			<html:text property="start3" size="4" maxlength="4"/> (in military format)
			&nbsp;<html:errors property="start3"/>
			</TD>
		</TR>

		<TR>
			<TD>3rd Period Length:</TD>
			<TD>
			<html:text property="length3" size="4" maxlength="4"/> (in minutes)
			&nbsp;<html:errors property="length3"/>
			</TD>
		</TR>

		<TR>
			<TD>4th Period Start Time:</TD>
			<TD>
			<html:text property="start4" size="4" maxlength="4"/> (in military format)
			&nbsp;<html:errors property="start4"/>
			</TD>
		</TR>

		<TR>
			<TD>4th Period Length:</TD>
			<TD>
			<html:text property="length4" size="4" maxlength="4"/> (in minutes)
			&nbsp;<html:errors property="length4"/>
			</TD>
		</TR>

		<TR>
			<TD>5th Period Start Time:</TD>
			<TD>
			<html:text property="start5" size="4" maxlength="4"/> (in military format)
			&nbsp;<html:errors property="start5"/>
			</TD>
		</TR>

		<TR>
			<TD>5th Period Length:</TD>
			<TD>
			<html:text property="length5" size="4" maxlength="4"/> (in minutes)
			&nbsp;<html:errors property="length5"/>
			</TD>
		</TR>

		<TR>
			<TD colspan='2'><br>Examination Dates:</TD>
		</TR>
		
		<TR>
			<TD colspan='2'>
				<bean:write name="examPeriodEditForm" property="patternHtml" filter="false"/>
			</TD>
		</TR>

		<TR>
			<TD align="right" colspan="2">
				<tt:section-title/>
			</TD>
		</TR>
		
		<TR>
			<TD align="right" colspan="2">
				<html:submit property="op">
					<bean:write name="examPeriodEditForm" property="op" />
				</html:submit> 
				<html:submit property="op" value="Back" /> 
			</TD>
		</TR>
	</TABLE>
	</logic:equal>
	<logic:equal name="examPeriodEditForm" property="autoSetup" value="false">
	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						<logic:equal name="examPeriodEditForm" property="op" value="Save">
							Add
						</logic:equal>
						<logic:notEqual name="examPeriodEditForm" property="op" value="Save">
							Edit
						</logic:notEqual>
						Examination Period
					</tt:section-title>
					<html:submit property="op">
						<bean:write name="examPeriodEditForm" property="op" />
					</html:submit> 
					<logic:notEqual name="examPeriodEditForm" property="op" value="Save">
						<html:submit property="op" value="Delete"/> 
					</logic:notEqual>
					<html:submit property="op" value="Back" /> 
				</tt:section-header>
			</TD>
		</TR>
		
		<TR>
			<TD>Type:</TD>
			<TD>
				<html:select property="examType">
					<html:options property="examTypes"/>
				</html:select>
			</TD>
		</TR>
		

		<TR>
			<TD>Date:</TD>
			<TD>
				<html:text property="date" size="10" maxlength="10" styleId="date"/>
				<img style="cursor: pointer;" src="scripts/jscalendar/calendar_1.gif" border="0" id="show_date">
				&nbsp;<html:errors property="date"/>
			</TD>
		</TR>

		<TR>
			<TD>Start Time:</TD>
			<TD>
			<html:text property="start" size="4" maxlength="4"/> (in military format)
			&nbsp;<html:errors property="start"/>
			</TD>
		</TR>

		<TR>
			<TD>Length:</TD>
			<TD>
			<html:text property="length" size="4" maxlength="4"/> (in minutes)
			&nbsp;<html:errors property="length"/>
			</TD>
		</TR>

		<TR>
			<TD>Preference:</TD>
			<TD>
			<html:select property="prefLevel">
				<html:optionsCollection property="prefLevels" label="prefName" value="uniqueId"/>
			</html:select>
			&nbsp;<html:errors property="prefLevel"/>
			</TD>
		</TR>

		<TR>
			<TD align="right" colspan="2">
				<tt:section-title/>
			</TD>
		</TR>
		
		<TR>
			<TD align="right" colspan="2">
				<html:submit property="op">
					<bean:write name="examPeriodEditForm" property="op" />
				</html:submit> 
				<logic:notEqual name="examPeriodEditForm" property="op" value="Save">
					<html:submit property="op" value="Delete"/> 
				</logic:notEqual>
				<html:submit property="op" value="Back" /> 
			</TD>
		</TR>
	</TABLE>
	<script type="text/javascript" language="javascript">
	
	Calendar.setup( {
		cache      : true, 					// Single object used for all calendars
		electric   : false, 				// Changes date only when calendar is closed
		inputField : "date",		// ID of the input field
	    ifFormat   : "%m/%d/%Y", 			// Format of the input field
	    showOthers : true,					// Show overlap of dates from other months	    
	    <% if (request.getParameter("date")!=null && request.getParameter("date").length()>=10) { %>
	    date		: <%=request.getParameter("date")%>,
	    <% }%>
		button     : "show_date"	// ID of the button
	} );

	</script>
	</logic:equal>
<BR>
</logic:notEqual>
<logic:equal name="examPeriodEditForm" property="op" value="List">
<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD colspan='6'>
			<tt:section-header>
				<tt:section-title>Examination Periods</tt:section-title>
				<html:submit property="op" value="Add Period" title="Create a new examination period"/>
				<logic:equal name="examPeriodEditForm" property="canAutoSetup" value="true">
					<html:submit property="op" value="Midterm Periods" title="Setup periods for midterm exams"/>
				</logic:equal>
			</tt:section-header>
		</TD>
	</TR>
	<%= request.getAttribute("ExamPeriods.table") %>
	<TR>
		<TD colspan='6'>
			<tt:section-title/>
		</TD>
	</TR>
	<TR>
		<TD colspan='6' align="right">
			<html:submit property="op" value="Add Period" title="Create a new examination period"/>
			<logic:equal name="examPeriodEditForm" property="canAutoSetup" value="true">
				<html:submit property="op" value="Midterm Periods" title="Setup periods for midterm exams"/>
			</logic:equal>
		</TD>
	</TR>
	<% if (request.getAttribute("hash") != null) { %>
		<SCRIPT type="text/javascript" language="javascript">
			location.hash = '<%=request.getAttribute("hash")%>';
		</SCRIPT>
	<% } %>
</TABLE>
</logic:equal>

</html:form>
