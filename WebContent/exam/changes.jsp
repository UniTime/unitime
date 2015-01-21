<%--
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
--%>
<%@ page language="java" autoFlush="true"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
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
			<html:select name="examChangesForm" property="subjectArea">
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
