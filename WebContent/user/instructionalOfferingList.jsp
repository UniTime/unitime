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
<%@ page language="java"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ page import="org.unitime.timetable.webutil.WebInstructionalOfferingTableBuilder"%>
<%@ page import="org.unitime.timetable.form.InstructionalOfferingListForm"%>
<%@ page import="org.unitime.timetable.solver.WebSolver"%>
<%@ page import="org.unitime.commons.web.Web"%>
<html:form action="/instructionalOfferingSearch">
<bean:define id="instructionalOfferings" name="instructionalOfferingListForm" property="instructionalOfferings"></bean:define>
<%	
	String subjectAreaId = (request.getParameter("subjectAreaId")!=null)
							? request.getParameter("subjectAreaId")
							: (String) request.getAttribute("subjectAreaId");
	session.setAttribute("subjArea", subjectAreaId);
	session.setAttribute("callingPage", "instructionalOfferingSearch");

	// Get Form 
	String frmName = "instructionalOfferingListForm";
	InstructionalOfferingListForm frm = (InstructionalOfferingListForm) request.getAttribute(frmName);
	if (frm.getInstructionalOfferings() != null && frm.getInstructionalOfferings().size() > 0){
		new WebInstructionalOfferingTableBuilder()
				    		.htmlTableForInstructionalOfferings(
				    				session,
				    		        WebSolver.getClassAssignmentProxy(session),
				    		        WebSolver.getExamSolver(session),
				    		        frm, 
				    		        new Long(frm.getSubjectAreaId()), 
				    		        Web.getUser(session),	
				    		        true, 
				    		        frm.getCourseNbr()==null || frm.getCourseNbr().length()==0,
				    		        out,
				    		        request.getParameter("backType"),
				    		        request.getParameter("backId"));
	}
%>
</html:form>
 
