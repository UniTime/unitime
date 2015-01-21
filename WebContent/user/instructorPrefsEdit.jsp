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
<%@ page import="org.unitime.timetable.action.InstructorPrefEditAction" %>
<%@ page import="org.unitime.timetable.form.InstructorEditForm" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.unitime.org/tags-localization" prefix="loc" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<%
	// Get Form 
	String frmName = "instructorEditForm";	
	InstructorEditForm frm = (InstructorEditForm) request.getAttribute(frmName);	
%>	

<loc:bundle name="CourseMessages">
<tt:session-context/>
<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(sessionContext) %>
	// -->
</SCRIPT>

<html:form action="instructorPrefEdit">
	<html:hidden property="instructorId"/>
	<html:hidden property="nextId"/>
	<html:hidden property="previousId"/>
	<html:hidden property="deptCode"/>
	<bean:define name='<%=frmName%>' property="instructorId" id="instructorId"/>
	
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<bean:write name='<%=frmName%>' property='name'/>
					</tt:section-title>

					<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessUpdatePreferences() %>" 
							title="<%=MSG.titleUpdatePreferences(MSG.accessUpdatePreferences()) %>" >
						<loc:message name="actionUpdatePreferences" />
					</html:submit> 
					
					<sec:authorize access="hasPermission(#instructorId, 'DepartmentalInstructor', 'InstructorEditClearPreferences')">
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" 
						accesskey="<%=MSG.accessClearInstructorPreferences() %>" 
						title="<%=MSG.titleClearInstructorPreferences(MSG.accessClearInstructorPreferences()) %>">
						<loc:message name="actionClearInstructorPreferences" />
					</html:submit> 
					</sec:authorize>
									
					<logic:notEmpty name="<%=frmName%>" property="previousId">
						&nbsp;
						<html:submit property="op" 
								styleClass="btn" 
								accesskey='<%=MSG.accessPreviousInstructor() %>' 
								title='<%=MSG.titlePreviousInstructorWithUpdate(MSG.accessPreviousInstructor())%>'>
							<loc:message name="actionPreviousInstructor" />
						</html:submit> 
					</logic:notEmpty>
					<logic:notEmpty name="<%=frmName%>" property="nextId">
						&nbsp;
						<html:submit property="op" 
								styleClass="btn" 
								accesskey='<%=MSG.accessNextInstructor() %>' 
								title='<%=MSG.titleNextInstructorWithUpdate(MSG.accessNextInstructor()) %>'>
							<loc:message name="actionNextInstructor" />						
						</html:submit> 
					</logic:notEmpty>
					&nbsp;
					<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessBackToDetail()%>" 
							title="<%=MSG.titleBackToDetail(MSG.accessBackToDetail()) %>">
						<loc:message name="actionBackToDetail"/>
					</html:submit>
				</tt:section-header>
			</TD>
		</TR>
		
		<logic:messagesPresent>
		<TR>
			<TD colspan="2" align="left" class="errorCell">
					<B><U>ERRORS</U></B><BR>
				<BLOCKQUOTE>
				<UL>
				    <html:messages id="error">
				      <LI>
						${error}
				      </LI>
				    </html:messages>
			    </UL>
			    </BLOCKQUOTE>
			</TD>
		</TR>
		</logic:messagesPresent>

<!-- Preferences -->
		<jsp:include page="preferencesEdit.jspf">
			<jsp:param name="frmName" value="<%=frmName%>"/>
			<jsp:param name="periodPref" value="false"/>
			<jsp:param name="datePatternPref" value="false"/>
			<jsp:param name="timePref" value="false"/>
			<jsp:param name="timeAvail" value="true"/>
		</jsp:include>
		
<!-- buttons -->
		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>
		<TR>
			<TD colspan="2" align="right">
						<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessUpdatePreferences() %>" 
							title="<%=MSG.titleUpdatePreferences(MSG.accessUpdatePreferences()) %>" >
						<loc:message name="actionUpdatePreferences" />
					</html:submit> 
					
					<sec:authorize access="hasPermission(#instructorId, 'DepartmentalInstructor', 'InstructorEditClearPreferences')">
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" 
						accesskey="<%=MSG.accessClearInstructorPreferences() %>" 
						title="<%=MSG.titleClearInstructorPreferences(MSG.accessClearInstructorPreferences()) %>">
						<loc:message name="actionClearInstructorPreferences" />
					</html:submit> 
					</sec:authorize>
									
					<logic:notEmpty name="<%=frmName%>" property="previousId">
						&nbsp;
						<html:submit property="op" 
								styleClass="btn" 
								accesskey='<%=MSG.accessPreviousInstructor() %>' 
								title='<%=MSG.titlePreviousInstructorWithUpdate(MSG.accessPreviousInstructor())%>'>
							<loc:message name="actionPreviousInstructor" />
						</html:submit> 
					</logic:notEmpty>
					<logic:notEmpty name="<%=frmName%>" property="nextId">
						&nbsp;
						<html:submit property="op" 
								styleClass="btn" 
								accesskey='<%=MSG.accessNextInstructor() %>' 
								title='<%=MSG.titleNextInstructorWithUpdate(MSG.accessNextInstructor()) %>'>
							<loc:message name="actionNextInstructor" />						
						</html:submit> 
					</logic:notEmpty>
					&nbsp;
					<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessBackToDetail()%>" 
							title="<%=MSG.titleBackToDetail(MSG.accessBackToDetail()) %>">
						<loc:message name="actionBackToDetail"/>
					</html:submit>
			</TD>
		</TR>

	</TABLE>
	
	
</html:form>

<SCRIPT type="text/javascript" language="javascript">
	function jumpToAnchor() {
    <% if (request.getAttribute(InstructorPrefEditAction.HASH_ATTR) != null) { %>
  		location.hash = "<%=request.getAttribute(InstructorPrefEditAction.HASH_ATTR)%>";
	<% } %>
		self.focus();
  	}
	
</SCRIPT>
</loc:bundle>	
