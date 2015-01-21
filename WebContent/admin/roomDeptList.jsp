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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp"%>
<%@ page import="org.unitime.timetable.form.RoomDeptListForm" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%		
	// Get Form 
	String frmName = "roomDeptListForm";	
	RoomDeptListForm frm = (RoomDeptListForm) request.getAttribute(frmName);
%>

<!-- Buttons -->
<TABLE>
	<%--
	<TR>
		<TD align="right">
			<html:form action="roomList">
				<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="R" title="Return to Room List">
					<bean:message key="button.returnToRoomList" />
				</html:submit>
			</html:form>
		</TD>
	</TR>
	--%>
	<TR>
		<TD colspan="2" valign="top" align="center">
			<html:errors/>
		</TD>
	</TR>
</TABLE>

<!-- room departments list -->
<TABLE width="100%" border="0" cellspacing="0" cellpadding="5">
	<% if (request.getAttribute("roomDepts") != null) {%>
		<%=request.getAttribute("roomDepts")%>
	<%}%>
</TABLE>

<!-- Buttons -->
<%--
<TABLE>
	<TR>
		<TD align="right">
			<html:form action="roomList">
				<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="R" title="Return to Room List">
					<bean:message key="button.returnToRoomList" />
				</html:submit>
			</html:form>
		</TD>
	</TR>
</TABLE>
--%>
