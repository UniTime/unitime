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
<%@ page import="org.unitime.commons.Debug" %>
<%@ page import="org.unitime.timetable.solver.SolverProxy" %>
<%@ page import="org.unitime.timetable.solver.WebSolver" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>
<tiles:importAttribute />
<html:form action="/assignedClasses">
<%
try {
%>
	<script language="JavaScript">blToggleHeader('Filter','dispFilter');blStart('dispFilter');</script>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD>Simplified mode:</TD>
		<TD>
			<html:checkbox property="simpleMode"/>
		</TD>
	</TR>
	</TABLE>
	<script language="JavaScript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
	<script language="JavaScript">blEndCollapsed('dispFilter');</script>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD width="10%" nowrap>Subject Area:</TD>
			<TD>
				<html:select name="assignedClassesForm" property="subjectArea">
					<html:option value="">Select...</html:option>
					<html:option value="-1">All</html:option>
					<html:optionsCollection property="subjectAreas"	label="subjectAreaAbbreviation" value="uniqueId" />
				</html:select>
			</TD>
		</TR>
		<TR>
			<TD colspan='2' align='right'>
				<html:submit onclick="displayLoading();" property="op" value="Apply"/>
				<html:submit property="op" value="Export PDF"/>
				<html:submit property="op" value="Export CSV"/>
				<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh"/>
			</TD>
		</TR>
	</TABLE>

	<BR><BR>
<%
	String assigned = (String)request.getAttribute("AssignedClasses.table");
	SolverProxy solver = WebSolver.getSolver(session);
	if (assigned!=null) {
%>
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
			<tr>
				<td colspan='<%=request.getAttribute("AssignedClasses.table.colspan")%>'>
					<i>
					NOTE: Only classes that are <%=solver==null?"included in the selected solution":"loaded into the solver"%> 
					are displayed in the below list. This means that classes that are assigned to other<br/>timetabling managers 
					(e.g., LLR or LAB) as well as classes that <%=solver==null?"were":"are"%> not loaded into the solver 
					(e.g., Arrange Hours classes) are excluded.<br/>
					For the full list of classes see <a href='classShowSearch.do'>Classes</a> or 
					<a href='classAssignmentsReportShowSearch.do'>Class Assignments</a> page.<br>
					</i>
				</td>
			</tr>
			<tr>
				<td colspan='<%=request.getAttribute("AssignedClasses.table.colspan")%>'>
					&nbsp;
				</td>
			</tr>
			<%=assigned%>
			<tr>
				<td colspan='<%=request.getAttribute("AssignedClasses.table.colspan")%>'>
					<tt:displayPrefLevelLegend/>
					<br>
					<i>
					NOTE: Only classes that are <%=solver==null?"included in the selected solution":"loaded into the solver"%> 
					are displayed in the below list. This means that classes that are assigned to other<br/>timetabling managers 
					(e.g., LLR or LAB) as well as classes that <%=solver==null?"were":"are"%> not loaded into the solver 
					(e.g., Arrange Hours classes) are excluded.<br/>
					For the full list of classes see <a href='classShowSearch.do'>Classes</a> or 
					<a href='classAssignmentsReportShowSearch.do'>Class Assignments</a> page.<br>
					</i>
				</td>
			</tr>
		</TABLE>
<%
	} else {
%>
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
			<TR>
				<TD colspan="2">
					<DIV class="WelcomeRowHead">
					Assigned Classes
					</DIV>
				</TD>
			</TR>
			<TR>
				<TD>
					<I><%=request.getAttribute("AssignedClasses.message")%></I>
				</TD>
			</TR>
		</TABLE>
<%
	}
%>
<%
} catch (Exception e) {
	Debug.error(e);
%>		
		<font color='red'><B>ERROR:<%=e.getMessage()%></B></font>
<%
}
%>
</html:form>
