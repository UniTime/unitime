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
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.unitime.org/tags-localization" prefix="loc" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<loc:bundle name="CourseMessages">
		
	<logic:notEmpty name="instructorList" scope="request">
		<table width="100%" border="0" cellspacing="0" cellpadding="3"><tr>
			<bean:write name="instructorList" scope="request" filter="false"/>
		</tr></table>
		
		<table width="100%" border="0" cellspacing="0" cellpadding="3">
		<tr><td align="right">
				<TABLE border="0" cellspacing="3" cellpadding="0" align="right"><TR>
					<sec:authorize access="hasPermission(#deptUniqueId, 'Department', 'InstructorsExportPdf')">
					<TD>
					<html:form action="instructorList" styleClass="FormWithNoPadding">			
						<html:submit property="op" styleClass="btn" 
							accesskey="<%=MSG.accessExportPdf() %>" 
							title="<%=MSG.titleExportPdf(MSG.accessExportPdf()) %>">
							<loc:message name="actionExportPdf" />
						</html:submit>
					</html:form>
					</TD>
				</sec:authorize>
				<sec:authorize access="hasPermission(#deptUniqueId, 'Department', 'ManageInstructors')">
					<TD>
					<html:form action="instructorListUpdate" styleClass="FormWithNoPadding">
						<html:submit onclick="displayLoading();" styleClass="btn"
							accesskey="<%=MSG.accessManageInstructorList() %>" 
							title="<%=MSG.titleManageInstructorList(MSG.accessManageInstructorList()) %>">
							<loc:message name="actionManageInstructorList" />
						</html:submit>
					</html:form>
					</TD>
				</sec:authorize>
				<sec:authorize access="hasPermission(#deptUniqueId, 'Department', 'InstructorAdd')">
					<TD>
					<html:form action="instructorAdd" styleClass="FormWithNoPadding">
						<html:submit onclick="displayLoading();" styleClass="btn"
							accesskey="<%=MSG.accessAddNewInstructor() %>" 
							title="<%=MSG.titleAddNewInstructor(MSG.accessAddNewInstructor()) %>">
							<loc:message name="actionAddNewInstructor" />
						</html:submit>
					</html:form>
					</TD>
				</sec:authorize>
			</TR></TABLE>
		</td></tr>
		</table>
	</logic:notEmpty>
</loc:bundle>

