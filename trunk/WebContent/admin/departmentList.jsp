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
