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
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.timetable.form.RoomGroupListForm" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<%
	// Get Form 
	String frmName = "roomGroupListForm";	
	RoomGroupListForm frm = (RoomGroupListForm) request.getAttribute(frmName);
%>
	
<tiles:importAttribute />
<html:form action="roomGroupList">
	<TABLE border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD>
				<B>Department: </B>
				<html:select property="deptCodeX"
					onchange="displayLoading(); submit()">
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:option value="<%=Constants.ALL_OPTION_VALUE%>">All Managed</html:option>
					<tt:canSeeExams>
						<logic:iterate scope="request" name="examTypes" id="type" type="org.unitime.timetable.model.ExamType">
							<html:option value='<%="Exam" + type.getUniqueId() %>'>All <bean:write name="type" property="label"/> Examination Rooms</html:option>
						</logic:iterate>
					</tt:canSeeExams>
					<html:options collection="<%=Department.DEPT_ATTR_NAME%>" 
						property="value" labelProperty="label"/>
					</html:select>
			</TD>
		
			<TD align="right" nowrap>			
				&nbsp;&nbsp;&nbsp;
				<html:submit property="op" value="Search" onclick="displayLoading();" accesskey="S" styleClass="btn"/>
				<sec:authorize access="hasPermission(null, 'Session', 'RoomGroupsExportPdf')">
					&nbsp;&nbsp;
					<html:submit property="op" value="Export PDF" accesskey="P" styleClass="btn"/>
				</sec:authorize>
			</TD>
		</TR>
		
		<TR>
			<TD colspan="2" valign="top" align="center">
				<html:errors />			
			</TD>
		</TR>

	</TABLE>
</html:form>

<logic:notEmpty name="body2">
	<script language="javascript">displayLoading();</script>
	<tiles:insert attribute="body2" />
	<script language="javascript">displayElement('loading', false);</script>
</logic:notEmpty>
