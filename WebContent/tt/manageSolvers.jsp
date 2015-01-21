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

<html:form action="/manageSolvers">

<logic:notEmpty name="ManageSolvers.table" scope="request">
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<bean:write name="ManageSolvers.table" scope="request" filter="false"/> 
	</TABLE>
	<BR>
</logic:notEmpty>
<logic:notEmpty name="ManageSolvers.xtable" scope="request">
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<bean:write name="ManageSolvers.xtable" scope="request" filter="false"/>
	</TABLE>
	<BR>
</logic:notEmpty>
<logic:notEmpty name="ManageSolvers.stable" scope="request">
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<bean:write name="ManageSolvers.stable" scope="request" filter="false"/>
	</TABLE>
	<BR>
</logic:notEmpty>
<logic:notEmpty name="ManageSolvers.otable" scope="request">
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<bean:write name="ManageSolvers.otable" scope="request" filter="false"/>
	</TABLE>
	<BR>
</logic:notEmpty>
<logic:notEmpty name="ManageSolvers.table2" scope="request">
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<bean:write name="ManageSolvers.table2" scope="request" filter="false"/>
	</TABLE>
<BR>
</logic:notEmpty>
<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD colspan='2'><DIV class="WelcomeRowHeadBlank">&nbsp;</DIV></TD>
	</TR>
	<TR>
		<TD align='right'>
			<% if (session.getAttribute("ManageSolver.puid")!=null || session.getAttribute("ManageSolver.examPuid")!=null || session.getAttribute("ManageSolver.sectionPuid")!=null) { %>
				<html:submit onclick="displayLoading();" property="op" value="Deselect"/>
			<% } %>
			<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh"/>
		</TD>
	</TR>
</TABLE>
</html:form>
