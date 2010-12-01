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
--%>
<%@ page language="java" autoFlush="true"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>
<tt:back-mark back="true" clear="true" title="Not-assigned Exams" uri="unassignedExams.do"/>
<tiles:importAttribute />
<html:form action="/unassignedExams">
	<script language="JavaScript">blToggleHeader('Filter','dispFilter');blStart('dispFilter');</script>
	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD width="10%" nowrap>Show classes/courses:</TD>
			<TD>
				<html:checkbox property="showSections"/>
			</TD>
		</TR>
	</TABLE>
	<script language="JavaScript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
	<script language="JavaScript">blEndCollapsed('dispFilter');</script>
	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
	<TR>
  		<TD width="10%" nowrap>Examination Problem:</TD>
		<TD>
			<html:select property="examType">
				<html:optionsCollection property="examTypes" label="label" value="value"/>
			</html:select>
		</TD>
	</TR>
	<TR>
		<TD width="10%" nowrap>Subject Areas:</TD>
		<TD>
			<html:select name="examReportForm" property="subjectArea"
				onfocus="setUp();" 
				onkeypress="return selectSearch(event, this);" 
				onkeydown="return checkKey(event, this);" >
				<html:option value="">Select...</html:option>
				<html:option value="-1">All</html:option>
				<html:optionsCollection property="subjectAreas"	label="subjectAreaAbbreviation" value="uniqueId" />
			</html:select>
		</TD>
	</TR>
	<TR>
		<TD colspan='2' align='right'>
			<html:submit onclick="displayLoading();" accesskey="A" property="op" value="Apply"/>
			<logic:notEmpty name="examReportForm" property="table">
				<html:submit onclick="displayLoading();" property="op" value="Export PDF"/>
			</logic:notEmpty>
			<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh"/>
		</TD>
	</TR>
	</TABLE>

	<BR><BR>
	<logic:empty name="examReportForm" property="table">
		<table width='95%' border='0' cellspacing='0' cellpadding='3'>
			<tr><td><i>
				<logic:empty name="examReportForm" property="subjectArea">
					No subject area selected.
				</logic:empty>
				<logic:equal name="examReportForm" property="subjectArea" value="0">
					No subject area selected.
				</logic:equal>
				<logic:lessThan name="examReportForm" property="subjectArea" value="0">
					All examinations are assigned.
				</logic:lessThan>
				<logic:greaterThan name="examReportForm" property="subjectArea" value="0">
					There are no examinations of <bean:write name="examReportForm" property="subjectAreaAbbv"/> subject area, or all of them are assigned.
				</logic:greaterThan>
			</i></td></tr>
		</table>
	</logic:empty>
	<logic:notEmpty name="examReportForm" property="table">
		<table width='95%' border='0' cellspacing='0' cellpadding='3'>
			<bean:define id="colspan" name="examReportForm" property="nrColumns"/>
			<!-- 
			<tr><td colspan='<%=colspan%>'><tt:displayPrefLevelLegend separator="none"/></td></tr>
			-->
			<bean:write name="examReportForm" property="table" filter="false"/>
			<tr><td colspan='<%=colspan%>'><tt:displayPrefLevelLegend/></td></tr>
		</table>
	</logic:notEmpty>
	<logic:notEmpty scope="request" name="hash">
		<SCRIPT type="text/javascript" language="javascript">
			location.hash = '<%=request.getAttribute("hash")%>';
		</SCRIPT>
	</logic:notEmpty>
</html:form>
