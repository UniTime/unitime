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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
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
					<TR>
					<TD nowrap style="padding-right: 10px;">
						<logic:equal value="true" scope="request" name="showAllManagers">
							<input type="checkbox" checked onclick="document.location='timetableManagerList.do?all=false';">Show all managers
						</logic:equal>
						<logic:notEqual value="true" scope="request" name="showAllManagers">
							<input type="checkbox" onclick="document.location='timetableManagerList.do?all=true';">Show all managers
						</logic:notEqual>
					</TD>
					<TD nowrap>
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
