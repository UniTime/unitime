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

<tiles:importAttribute />

<html:form action="/solverInfoDef" focus="name">
<html:hidden property="uniqueId"/><html:errors property="uniqueId"/>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<DIV class="WelcomeRowHead">
				<logic:equal name="solverInfoDefForm" property="op" value="Add New">
				Add
				</logic:equal>
				<logic:notEqual name="solverInfoDefForm" property="op" value="Add New">
				Edit
				</logic:notEqual>
				Solution Info Defition
				</DIV>
			</TD>
		</TR>

		<TR>
			<TD>Name:</TD>
			<TD>
				<html:text property="name" size="50" maxlength="100"/>
				&nbsp;<html:errors property="name"/>
			</TD>
		</TR>

		<TR>
			<TD>Description:</TD>
			<TD>
				<html:text property="description" size="50" maxlength="1000"/>
				&nbsp;<html:errors property="description"/>
			</TD>
		</TR>

		<TR>
			<TD>Implementation:</TD>
			<TD>
				<html:text property="implementation" size="50" maxlength="250"/>
				&nbsp;<html:errors property="implementation"/>
			</TD>
		</TR>

		<TR>
			<TD align="right" colspan="2">
				<html:submit property="op">
					<bean:write name="solverInfoDefForm" property="op" />
				</html:submit> 
				<logic:notEqual name="solverInfoDefForm" property="op" value="Add New">
					<html:submit property="op" value="Delete"/> 
				</logic:notEqual>
				<html:submit property="op" value="Clear" /> 
			</TD>
		</TR>
	</TABLE>

<BR>&nbsp;<BR>

<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<%= request.getAttribute("SolverInfoDef.table") %> 
</TABLE>


</html:form>
