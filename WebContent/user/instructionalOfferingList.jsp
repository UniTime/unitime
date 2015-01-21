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
<%@ page language="java"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ page import="org.unitime.timetable.webutil.WebInstructionalOfferingTableBuilder"%>
<%@ page import="org.unitime.timetable.form.InstructionalOfferingListForm"%>
<%@ page import="org.unitime.timetable.solver.WebSolver"%>
<html:form action="/instructionalOfferingSearch">
<bean:define id="instructionalOfferings" name="instructionalOfferingListForm" property="instructionalOfferings"></bean:define>
<tt:session-context/>
<%
		

	String subjectAreaIds = (request.getParameter("subjectAreaIds")!=null)
							? request.getParameter("subjectAreaIds")
							: (String) request.getAttribute("subjectAreaIds");
	session.setAttribute("subjArea", subjectAreaIds);
	session.setAttribute("callingPage", "instructionalOfferingSearch");

	// Get Form 
	String frmName = "instructionalOfferingListForm";
	InstructionalOfferingListForm frm = (InstructionalOfferingListForm) request.getAttribute(frmName);
	if (frm.getInstructionalOfferings() != null && frm.getInstructionalOfferings().size() > 0){
		new WebInstructionalOfferingTableBuilder()
				    		.htmlTableForInstructionalOfferings(
				    				sessionContext,
				    		        WebSolver.getClassAssignmentProxy(session),
				    		        WebSolver.getExamSolver(session),
				    		        frm, 
				    		        frm.getSubjectAreaIds(), 
				    		        true, 
				    		        frm.getCourseNbr() == null || frm.getCourseNbr().isEmpty(),
				    		        out,
				    		        request.getParameter("backType"),
				    		        request.getParameter("backId"));
	}
%>
</html:form>
 
