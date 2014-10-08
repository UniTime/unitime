<!DOCTYPE html>
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
<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8" errorPage="/error.jsp"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<DIV align="center" class="H1">
	<BR>
	<BR>
	<logic:notEmpty name="message" scope="request">
		<bean:write name="message" scope="request" filter="false"/>
		<br><br>
	</logic:notEmpty>
	<% if (request.getParameter("message")!=null && !"null".equals(request.getParameter("message"))) { %> 
		<%=request.getParameter("message")%>
		<BR>
		<BR>
	<% } %>
	<A class="l7" href="javascript:self.history.back();">BACK</A>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
	<A class="l7" href="<%=request.getContextPath()%>/login.jsp" target="_top">LOG IN</A>
	<BR><BR>
</DIV>

<%@ include file="/initializationError.jspf"%>
