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
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>
<tt:back-mark back="true" clear="true" title="Exam Changes" uri="examChanges.do"/>
<tiles:importAttribute />
<html:form action="/examChanges">
	<script language="JavaScript">blToggleHeader('Filter','dispFilter');blStart('dispFilter');</script>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD width="10%" nowrap>Show classes/courses:</TD>
			<TD>
				<html:checkbox property="showSections"/>
			</TD>
		</TR>
		<TR>
			<TD width="10%" nowrap>Reversed mode:</TD>
			<TD>
				<html:checkbox property="reverse"/> (current &rarr; compared solution)
			</TD>
		</TR>
	</TABLE>
	<script language="JavaScript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
	<script language="JavaScript">blEndCollapsed('dispFilter');</script>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD width="10%" nowrap>Subject Areas:</TD>
		<TD>
			<html:select name="examChangesForm" property="subjectArea"
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
		<TD width="10%" nowrap>Compare with:</TD>
		<TD>
			<html:select property="changeType">
				<html:options property="changeTypes"/>
			</html:select>
		</TD>
	</TR>
	<TR>
		<TD colspan='2' align='right'>
			<html:submit onclick="displayLoading();" accesskey="A" property="op" value="Apply"/>
			<logic:notEmpty name="examChangesForm" property="table">
				<html:submit property="op" value="Export PDF"/>
			</logic:notEmpty>
			<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh"/>
		</TD>
	</TR>
	</TABLE>

	<BR><BR>
	<logic:empty name="examChangesForm" property="table">
		<table width='100%' border='0' cellspacing='0' cellpadding='3'>
			<tr><td><i>
				<logic:equal name="examChangesForm" property="noSolver" value="true">
					No examination timetable is loaded into the solver.
				</logic:equal>
				<logic:empty name="examChangesForm" property="subjectArea">
					No subject area selected.
				</logic:empty>
				<logic:equal name="examChangesForm" property="subjectArea" value="0">
					No subject area selected.
				</logic:equal>
				<logic:lessThan name="examChangesForm" property="subjectArea" value="0">
					Solutions are identical.
				</logic:lessThan>
				<logic:greaterThan name="examChangesForm" property="subjectArea" value="0">
					There are no assignment changes between exams of <bean:write name="examChangesForm" property="subjectAreaAbbv"/> subject area.
				</logic:greaterThan>
			</i></td></tr>
		</table>
	</logic:empty>
	<logic:notEmpty name="examChangesForm" property="table">
		<table width='100%' border='0' cellspacing='0' cellpadding='3'>
			<bean:define id="colspan" name="examChangesForm" property="nrColumns"/>
			<bean:write name="examChangesForm" property="table" filter="false"/>
			<tr><td colspan='<%=colspan%>'><tt:displayPrefLevelLegend/></td></tr>
		</table>
	</logic:notEmpty>
	<logic:notEmpty scope="request" name="hash">
		<SCRIPT type="text/javascript" language="javascript">
			location.hash = '<%=request.getAttribute("hash")%>';
		</SCRIPT>
	</logic:notEmpty>
</html:form>
