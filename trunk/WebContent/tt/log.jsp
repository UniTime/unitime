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
<%@ page import="org.unitime.timetable.solver.SolverProxy" %>
<%@ page import="org.unitime.timetable.solver.WebSolver" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<tt:back-mark back="true" clear="true" title="Solver Log" uri="solverLog.do"/>

<tiles:importAttribute />

<html:form action="/solverLog">
	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<DIV class="WelcomeRowHead">
				Solver Log
				</DIV>
			</TD>
		</TR>
		<TR>
			<TD>Debug Level:</TD>
			<TD>
				<html:select property="level">
					<html:options name="solverLogForm" property="levels"/>
				</html:select>
				<html:submit onclick="displayLoading();" property="op" value="Change"/>
				<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh"/>
			</TD>
		</TR>
	</TABLE>
<%
	SolverProxy solver = WebSolver.getSolver(session);
	if (solver!=null) {
%>
	<font size='2'>
<%=solver.getLog()%>
	</font>
<%		
	} else {
%>
	<logic:notEqual name="solverLogForm" property="nrLogs" value="0">
		<logic:iterate name="solverLogForm" property="ownerNames" id="ownerName" indexId="idx">
			<BR>
			<logic:greaterThan name="solverLogForm" property="nrLogs" value="1">
				<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
					<TR>
						<TD colspan="2">
							<DIV class="WelcomeRowHead">
								<bean:write name="ownerName"/>
							</DIV>
						</TD>
					</TR>
				</TABLE>
			</logic:greaterThan>
			<bean:write name="solverLogForm" property='<%="log["+idx+"]"%>' filter="false"/>
		</logic:iterate>
	</logic:notEqual>
	
	<logic:equal name="solverLogForm" property="nrLogs" value="0">
		<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
			<TR>
				<TD colspan="2">	
					<i>Neither a solver is started nor solution is selected.</i>
				</TD>
			</TR>
		</TABLE>
	</logic:equal>
<%
	}
%>
</html:form>
