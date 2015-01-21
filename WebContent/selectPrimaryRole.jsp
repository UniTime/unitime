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
<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8" errorPage="/error.jsp"%>
<%@ page import="org.unitime.timetable.model.Roles"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%> 
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<html:form action="selectPrimaryRole.do" target="_top">
	<html:hidden property="authority"/>
	<html:hidden property="target"/>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<% if (!"Y".equals(request.getParameter("list"))) { %>
			<tr><td colspan='4'>
				<i>A default user role and/or academic session could not be assigned. Please select one of the user role and academic session combinations below to proceed.<br><br></i>
			</td></tr>
		<% } %>
		<%=request.getAttribute(Roles.USER_ROLES_ATTR_NAME)%>
	</TABLE>
</html:form>
