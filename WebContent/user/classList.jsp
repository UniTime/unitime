<%--
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org
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
<%@ page import="org.unitime.timetable.webutil.WebClassListTableBuilder"%>
<%@ page import="org.unitime.timetable.form.ClassListForm"%>
<%@ page import="org.unitime.commons.User"%>
<%@ page import="org.unitime.commons.web.Web"%>
<%@ page import="org.unitime.timetable.solver.WebSolver"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>

<%
	// Get Form 
	String frmName = "classListForm";
	ClassListForm frm = (ClassListForm) request.getAttribute(frmName);

%>
<html:form action="/classSearch">
	<%
//	String subjectAreaIds = (request.getParameter("subjectAreaIds")!=null)
//							? request.getParameter("subjectAreaIds")
//							: (String) request.getAttribute("subjectAreaIds");
//	session.setAttribute("subjArea", subjectAreaIds);
	User user = Web.getUser(session);
	session.setAttribute("callingPage", "classSearch");
%>
	<%new WebClassListTableBuilder().htmlTableForClasses(session, WebSolver.getClassAssignmentProxy(session),WebSolver.getExamSolver(session),frm, user, out, request.getParameter("backType"), request.getParameter("backId"));%>
</html:form>

