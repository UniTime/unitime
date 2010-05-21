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
<%@ page import="org.unitime.timetable.solver.remote.SolverRegisterService" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>

<tiles:importAttribute />

<html:form action="/manageSolvers">

<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
	<%= request.getAttribute("ManageSolvers.table") %> 
</TABLE>
<BR>
<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
	<%= request.getAttribute("ManageSolvers.xtable") %> 
</TABLE>
<BR>
<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
	<%= request.getAttribute("ManageSolvers.stable") %> 
</TABLE>
<BR>
<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
	<%= request.getAttribute("ManageSolvers.table2") %> 
</TABLE>
<BR>
<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD colspan='2'><DIV class="WelcomeRowHeadBlank">&nbsp;</DIV></TD>
	</TR>
	<TR>
		<TD align='left'>
			<i>Solver register service is running at <%=request.getServerName()%>:<%=SolverRegisterService.getPort()%>.</i>
		</TD>
		<TD align='right'>
			<% if (session.getAttribute("ManageSolver.puid")!=null || session.getAttribute("ManageSolver.examPuid")!=null || session.getAttribute("ManageSolver.sectionPuid")!=null) { %>
				<html:submit onclick="displayLoading();" property="op" value="Deselect"/>
			<% } %>
			<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh"/>
		</TD>
	</TR>
</TABLE>
</html:form>
