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
<%@ page language="java" autoFlush="true"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<tt:back-mark back="true" clear="true" title="Room Availability" uri="roomAvailability.do"/>
<tiles:importAttribute />
<html:form action="/roomAvailability">
	<html:hidden property="showSections"/>
	<html:hidden property="subjectArea" />
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD colspan='2'>
			<tt:section-header>
				<tt:section-title>Filter</tt:section-title>
				<html:submit onclick="displayLoading();" accesskey="A" property="op" value="Apply"/>
				<logic:notEmpty name="roomAvailabilityForm" property="table">
					<html:submit property="op" value="Export PDF"/>
				</logic:notEmpty>
				<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh"/>
			</tt:section-header>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap>Examination Problem:</TD>
		<TD>
			<html:select property="examType">
				<html:option value="-1">Select...</html:option>
				<html:options collection="examTypes" property="uniqueId" labelProperty="label"/>
			</html:select>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap>Room Filter:</TD>
		<TD>
			<html:text property="filter" size="80"/>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap>Display Examinations:</TD>
		<TD>
			<html:checkbox property="includeExams"/>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap>Compare Examinations:</TD>
		<TD>
			<html:checkbox property="compare"/>
		</TD>
	</TR>
	<logic:notEmpty scope="request" name="timestamp">
		<TR>
  			<TD width="10%" nowrap>Last Update:</TD>
			<TD>
				<bean:write scope="request" name="timestamp"/>
			</TD>
		</TR>
	</logic:notEmpty>
	</TABLE>

	<BR><BR>
	<logic:empty name="roomAvailabilityForm" property="table">
		<table width='100%' border='0' cellspacing='0' cellpadding='3'>
			<tr><td><i>
				<logic:equal name="roomAvailabilityForm" property="examType" value="-1">
					Examination problem not selected.
				</logic:equal>
				<logic:notEqual name="roomAvailabilityForm" property="examType" value="-1">
					Nothing to display.
				</logic:notEqual>
			</i></td></tr>
		</table>
	</logic:empty>
	<logic:notEmpty name="roomAvailabilityForm" property="table">
		<table width='100%' border='0' cellspacing='0' cellpadding='3'>
			<bean:write name="roomAvailabilityForm" property="table" filter="false"/>
		</table>
	</logic:notEmpty>
	
	<logic:notEmpty scope="request" name="hash">
		<SCRIPT type="text/javascript" language="javascript">
			location.hash = '<%=request.getAttribute("hash")%>';
		</SCRIPT>
	</logic:notEmpty>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD>
			<tt:section-title/>
		</TD>
	</TR>
	<TR>
		<TD align="right">
			<html:submit onclick="displayLoading();" accesskey="A" property="op" value="Apply"/>
			<logic:notEmpty name="roomAvailabilityForm" property="table">
				<html:submit property="op" value="Export PDF"/>
			</logic:notEmpty>
			<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh"/>
		</TD>
	</TR>
	</TABLE>	
</html:form>
