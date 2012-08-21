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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<tt:session-context/>
<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD align="right">
			<tt:section-header>
			
				<tt:section-title>
					Manager List - <%= sessionContext.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel() %>
				</tt:section-title>
				
				<TABLE align="right" cellspacing="0" cellpadding="2" class="FormWithNoPadding">
					<TR><TD nowrap>
						<sec:authorize access="hasPermission(null, null, 'TimetableManagerAdd')">
						<html:form action="timetableManagerEdit" styleClass="FormWithNoPadding">			
							<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="T" titleKey="title.addTimetableManager">
								<bean:message key="button.addTimetableManager" />
							</html:submit>
						</html:form>
						</sec:authorize>
					</TD><TD nowrap>
						<input type='button' onclick="document.location='timetableManagerList.do?op=Export%20PDF';" title='Export PDF (Alt+P)' accesskey="P" class="btn" value="Export PDF">
					</TD></TR>
				</TABLE>
				
			</tt:section-header>
		</TD>
	</TR>
</TABLE>				

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="1">
		<bean:write name="schedDeputyList" scope="request" filter="false"/>
	</TABLE>

	<table width="100%" border="0" cellspacing="0" cellpadding="3">
		<tr>
			<td align="center" class="WelcomeRowHead">
			&nbsp;
			</td>
		</tr>
		<tr>
			<td align="right">
				<TABLE align="right" cellspacing="0" cellpadding="2" class="FormWithNoPadding">
					<TR><TD nowrap>
						<sec:authorize access="hasPermission(null, null, 'TimetableManagerAdd')">
						<html:form action="timetableManagerEdit" styleClass="FormWithNoPadding">			
							<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="T" titleKey="title.addTimetableManager">
								<bean:message key="button.addTimetableManager" />
							</html:submit>
						</html:form>
						</sec:authorize>
					</TD><TD nowrap>
						<input type='button' onclick="document.location='timetableManagerList.do?op=Export%20PDF';" title='Export PDF (Alt+P)' accesskey="P" class="btn" value="Export PDF">
					</TD></TR>
				</TABLE>
			</td>
		</tr>
	</table>


<SCRIPT type="text/javascript" language="javascript">
	function jumpToAnchor() {
    <% if (request.getAttribute(Constants.JUMP_TO_ATTR_NAME) != null) { %>
  		location.hash = "<%=request.getAttribute(Constants.JUMP_TO_ATTR_NAME)%>";
	<% } %>
	    self.focus();
  	}
</SCRIPT>
