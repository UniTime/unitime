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
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>

<tiles:importAttribute name="showNavigation" scope="request"/>	

	<div id="loading" class="unitime-PageLoading" style="visibility:hidden;display:none">
		<img align="middle" vspace="5" border="0" src="images/loading.gif">
	</div>
	
	<logic:equal name="showNavigation" value="true"> 
		<% if (!"hide".equals(request.getParameter("menu"))) { %>
		<tt:has-back>
			<tt:form-factor value="mobile">
				<tt:back styleClass="btn" name="[&larr;]" title="Return to %%"/>
				<tt:back-tree/>
				<tt:gwt-back/>
			</tt:form-factor>
			<tt:form-factor value="desktop">
				<span class="unitime-Navigation">
					<tt:back styleClass="btn" name="[&larr;]" title="Return to %%"/>
					<tt:back-tree/>
					<tt:gwt-back/>
				</span>
			</tt:form-factor>
		</tt:has-back>
		<% } %>
	</logic:equal>
