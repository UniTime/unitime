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
<%@ page import="org.unitime.timetable.util.Constants"%>
<%@ taglib uri="http://struts.apache.org/tags-bean"	prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html"	prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<tt:session-context/>
<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD align="right">
			<tt:section-header>
				<tt:section-title>
				 Department List - <%= sessionContext.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel() %>
				</tt:section-title>

				<TABLE align="right" cellspacing="0" cellpadding="2" class="FormWithNoPadding">
					<TR>
					<sec:authorize access="hasPermission(null, 'Session', 'DepartmentAdd')">
						<TD nowrap>
								<html:form action="departmentEdit" styleClass="FormWithNoPadding">
									<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="D" titleKey="title.addDepartment">
									<bean:message key="button.addDepartment" />
								</html:submit>
							</html:form>
						</TD>
					</sec:authorize>
					<TD nowrap>
						<input type='button' onclick="document.location='departmentList.do?op=Export%20PDF';" title='Export PDF (Alt+P)' accesskey="P" class="btn" value="Export PDF">
					</TD></TR>
				</TABLE>

			</tt:section-header>
		</TD>
	</TR>
</TABLE>

<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<bean:write name="table" scope="request" filter="false"/>
</TABLE>

<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD align="right" colspan="2">
			<tt:section-header/>
		</TD>
	</TR>
	<TR>
		<TD align="left">
			<html:form action="/departmentList">
				Show all departments (including departments with no manager and no subject area):
				<html:hidden property="op" value="Apply"/>
				<html:checkbox property="showUnusedDepts" onchange="submit()"/>
			</html:form>
		</TD>
		<TD align="right">
				<TABLE align="right" cellspacing="0" cellpadding="2" class="FormWithNoPadding">
					<TR>
					<sec:authorize access="hasPermission(null, 'Session', 'DepartmentAdd')">
						<TD nowrap>
							<html:form action="departmentEdit" styleClass="FormWithNoPadding">
								<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="D" titleKey="title.addDepartment">
								<bean:message key="button.addDepartment" />
								</html:submit>
							</html:form>
						</TD>
					</sec:authorize>
					<TD nowrap>
						<input type='button' onclick="document.location='departmentList.do?op=Export%20PDF';" title='Export PDF (Alt+P)' accesskey="P" class="btn" value="Export PDF">
					</TD></TR>
				</TABLE>
		</TD>
	</TR>
</TABLE>				

<SCRIPT type="text/javascript" language="javascript">
	function jumpToAnchor() {
    <% if (request.getAttribute(Constants.JUMP_TO_ATTR_NAME) != null) { %>
  		location.hash = "<%=request.getAttribute(Constants.JUMP_TO_ATTR_NAME)%>";
	<% } %>
	    self.focus();
  	}
</SCRIPT>
