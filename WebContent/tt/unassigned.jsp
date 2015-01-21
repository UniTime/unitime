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
<%@ page import="org.unitime.timetable.solver.SolverProxy" %>
<%@ page import="org.unitime.timetable.solver.WebSolver" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>

<tiles:importAttribute />

<html:form action="/unassigned">
	<% 	SolverProxy solver = WebSolver.getSolver(session); %>
	
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan='2'>
				<tt:section-title>Filter</tt:section-title>
			</TD>
		</TR>
		<TR>
			<TD width="10%" nowrap>Subject Area:</TD>
			<TD>
				<html:select name="unassignedForm" property="subjectArea">
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
	<br><br>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<td colspan='4'>
				<i>
					NOTE: Only classes that are <%=solver==null?"included in the selected solution":"loaded into the solver"%> 
					are displayed in the below list. This means that classes that are assigned to other<br/>timetabling managers 
					(e.g., LLR or LAB) as well as classes that <%=solver==null?"were":"are"%> not loaded into the solver 
					(e.g., Arrange Hours classes) are excluded.<br/>
					For the full list of classes see <a href='classShowSearch.do'>Classes</a> or 
					<a href='classAssignmentsReportShowSearch.do'>Class Assignments</a> page.<br><br>
				</i>
			</TD>
		</TR>
		<%= request.getAttribute("Unassigned.table") %> 
		<TR>
			<td colspan='4'>
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
			</TD>
		</TR>
	</TABLE>
</html:form>
