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

<%@ page language="java"%>
<%@ page import="org.unitime.timetable.webutil.WebClassAssignmentReportListTableBuilder" %>
<%@ page import="org.unitime.timetable.form.ClassAssignmentsReportForm" %>
<%@ page import="org.unitime.timetable.solver.WebSolver"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%
	// Get Form 
	String frmName = "classAssignmentsReportForm";
	ClassAssignmentsReportForm frm = (ClassAssignmentsReportForm) request.getAttribute(frmName);

%>		

<html:form action="/classAssignmentsReportSearch">
<tt:session-context/>
<% session.setAttribute("callingPage", ""); %>
<% new WebClassAssignmentReportListTableBuilder().htmlTableForClasses(sessionContext, WebSolver.getClassAssignmentProxy(session),WebSolver.getExamSolver(session),frm, out, request.getParameter("backType"), request.getParameter("backId"));%>
</html:form>
 
