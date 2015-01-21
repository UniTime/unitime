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

<tt:back-mark back="true" clear="true" title="Solver Log" uri="solverLog.do"/>

<tiles:importAttribute />

<html:form action="/solverLog">
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
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
	<logic:notEmpty name="log" scope="request">
		<font size='2'>
			<bean:write name="log" scope="request" filter="false"/>
		</font>
	</logic:notEmpty>
	<logic:empty name="log" scope="request">
	<logic:notEqual name="solverLogForm" property="nrLogs" value="0">
		<logic:iterate name="solverLogForm" property="ownerNames" id="ownerName" indexId="idx">
			<BR>
			<logic:greaterThan name="solverLogForm" property="nrLogs" value="1">
				<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
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
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
			<TR>
				<TD colspan="2">	
					<i>Neither a solver is started nor solution is selected.</i>
				</TD>
			</TR>
		</TABLE>
	</logic:equal>
	</logic:empty>
</html:form>
