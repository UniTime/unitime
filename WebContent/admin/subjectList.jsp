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
<%@ taglib uri="http://struts.apache.org/tags-logic"	prefix="logic"%>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<tt:session-context/>
<html:form action="subjectAreaEdit.do">

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">

		<TR>
			<TD>

				<tt:section-header>
					<tt:section-title>
					Subject Area List - <%= sessionContext.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel() %>
					</tt:section-title>

					<sec:authorize access="hasPermission(null, 'Session', 'SubjectAreaAdd')">
						<html:submit property="op" styleClass="btn" accesskey="S" titleKey="title.addSubjectArea">
							<bean:message key="button.addSubjectArea" />
						</html:submit>
					</sec:authorize>

					<input type='button' onclick="document.location='subjectList.do?op=Export%20PDF';" title='Export PDF (Alt+P)' accesskey="P" class="btn" value="Export PDF">
				</tt:section-header>
			</TD>
		</TR>

	</TABLE>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<bean:write name="table" scope="request" filter="false"/>
	</TABLE>
	
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">

		<TR>
			<TD class="WelcomeRowHead">
				&nbsp;
			</TD>
		</TR>
		<TR>
			<TD align="right">
				<sec:authorize access="hasPermission(null, 'Session', 'SubjectAreaAdd')">
					<html:submit property="op" styleClass="btn" accesskey="S" titleKey="title.addSubjectArea">
						<bean:message key="button.addSubjectArea" />
					</html:submit>
				</sec:authorize>

				<input type='button' onclick="document.location='subjectList.do?op=Export%20PDF';" title='Export PDF (Alt+P)' accesskey="P" class="btn" value="Export PDF">
			</TD>
		</TR>

	</TABLE>

	
</html:form>

<SCRIPT type="text/javascript" language="javascript">
	function jumpToAnchor() {
    <% if (request.getAttribute(Constants.JUMP_TO_ATTR_NAME) != null) { %>
  		location.hash = "<%=request.getAttribute(Constants.JUMP_TO_ATTR_NAME)%>";
	<% } %>
	    self.focus();
  	}
</SCRIPT>	
