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
<%@ page language="java" autoFlush="true" errorPage="error.jsp" %>
<%@ page import="org.unitime.timetable.model.Roles"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%> 
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<html:form action="selectPrimaryRole.do" target="_top">
	<html:hidden property="sessionId"/>
	<html:hidden property="roleId"/>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<% if (!"Y".equals(request.getParameter("list"))) { %>
			<tr><td colspan='4'>
				<i>A default user role and/or academic session could not be assigned. Please select one of the user role and academic session combinations below to proceed.<br><br></i>
			</td></tr>
		<% } %>
		<%=request.getAttribute(Roles.USER_ROLES_ATTR_NAME)%>
	</TABLE>
</html:form>
