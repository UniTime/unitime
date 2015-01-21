<!DOCTYPE html>
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
	<A class="l7" href="<%=request.getContextPath()%>/login.do" target="_top">LOG IN</A>
	<BR><BR>
</DIV>

<%@ include file="/initializationError.jspf"%>
