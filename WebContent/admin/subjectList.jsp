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
