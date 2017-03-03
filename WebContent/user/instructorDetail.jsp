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
<tt:session-context/>
<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(sessionContext) %>
	// -->
</SCRIPT>

<html:form action="instructorDetail">
  	<loc:bundle name="CourseMessages"> 
	<logic:empty name="<%=frmName%>" property="departments"><html:hidden property="instructorId"/></logic:empty>
	<html:hidden property="nextId"/>
	<html:hidden property="previousId"/>
	<html:hidden property="deptCode"/>
	<html:hidden property="op2" value=""/>
	<bean:define name='<%=frmName%>' property="instructorId" id="instructorId"/>
	
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<bean:write name='<%=frmName%>' property='name'/>
					</tt:section-title>
				<sec:authorize access="hasPermission(#instructorId, 'DepartmentalInstructor', 'InstructorEdit')">
					<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessEditInstructor() %>" 
							title="<%=MSG.titleEditInstructor(MSG.accessEditInstructor()) %>" >
							<loc:message name="actionEditInstructor" />
					</html:submit>
				</sec:authorize>
				<sec:authorize access="hasPermission(#deptCode, 'Department', 'InstructorAssignmentPreferences')">
					<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessEditInstructorAssignmentPreferences() %>" 
							title="<%=MSG.titleEditInstructorAssignmentPreferences(MSG.accessEditInstructorAssignmentPreferences()) %>" >
							<loc:message name="actionEditInstructorAssignmentPreferences" />
					</html:submit>
				</sec:authorize> 
				<sec:authorize access="hasPermission(#instructorId, 'DepartmentalInstructor', 'InstructorPreferences')">
					<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessEditInstructorPreferences() %>" 
							title="<%=MSG.titleEditInstructorPreferences(MSG.accessEditInstructorPreferences()) %>" >
							<loc:message name="actionEditInstructorPreferences" />
					</html:submit>
				</sec:authorize> 
				<logic:notEmpty name="<%=frmName%>" property="previousId">
					<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessPreviousInstructor() %>" 
							title="<%=MSG.titlePreviousInstructor(MSG.accessPreviousInstructor()) %>" >
							<loc:message name="actionPreviousInstructor" />
					</html:submit>
				</logic:notEmpty>
				<logic:notEmpty name="<%=frmName%>" property="nextId">
					<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessNextInstructor() %>" 
							title="<%=MSG.titleNextInstructor(MSG.accessNextInstructor()) %>" >
							<loc:message name="actionNextInstructor" />
					</html:submit> 
				</logic:notEmpty>
				<tt:back styleClass="btn" 
					name="<%=MSG.actionBackInstructorDetail()%>" 
					title="<%=MSG.titleBackInstructorDetail(MSG.accessBackInstructorDetail())%>" 
					accesskey="<%=MSG.accessBackInstructorDetail() %>" 
					type="PreferenceGroup">
					<bean:write name="<%=frmName%>" property="instructorId"/>
				</tt:back>
				</tt:section-header>
			</TD>
		</TR>
		
		<logic:messagesPresent>
		<TR>
			<TD colspan="2" align="left" class="errorCell">
					<B><U><loc:message name="errorsInstructorDetail"/></U></B><BR>
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
		
		<TR>
			<TD><loc:message name="propertyExternalId"></loc:message></TD>
			<TD> <bean:write name="<%=frmName%>" property="puId" /></TD>
		</TR>
		<logic:notEmpty name="<%=frmName%>" property="departments">
			<TR>
				<TD><loc:message name="propertyDepartment"></loc:message></TD>
				<TD>
					<html:select property='instructorId' onchange="submit();">
						<html:optionsCollection name="<%=frmName%>" property="departments" value="id" label="value"/>
					</html:select>
				</TD>
			</TR>
		</logic:notEmpty>
		<logic:empty name="<%=frmName%>" property="departments"><logic:notEmpty name="<%=frmName%>" property="deptName">
			<TR>
				<TD><loc:message name="propertyDepartment"></loc:message></TD>
				<TD>
					<bean:write name="<%=frmName%>" property="deptName" />
				</TD>
			</TR>
		</logic:notEmpty></logic:empty>
		
		<logic:notEmpty name="<%=frmName%>" property="careerAcct">
			<TR>
				<TD><loc:message name="propertyAccountName"></loc:message></TD>
				<TD> <bean:write name="<%=frmName%>" property="careerAcct" /></TD>
			</TR>
		</logic:notEmpty>
		<logic:notEmpty name="<%=frmName%>" property="email">
			<TR>
				<TD><loc:message name="propertyEmail"></loc:message></TD>
				<TD> <bean:write name="<%=frmName%>" property="email" /></TD>
			</TR>
		</logic:notEmpty>
		<TR>
			<TD><loc:message name="propertyInstructorPosition"></loc:message></TD>
			<TD> <bean:write name="<%=frmName%>" property="posType" /></TD>
		</TR>
		<logic:notEmpty name="<%=frmName%>" property="note">
			<TR>
				<TD valign="top"><loc:message name="propertyNote"></loc:message></TD>
				<TD> <bean:write name="<%=frmName%>" property="note" filter="false"/></TD>
			</TR>
		</logic:notEmpty>
		<logic:equal name="<%=frmName%>" property="ignoreDist" value="true">
			<TR>
				<TD><loc:message name="propertyIgnoreTooFar"></loc:message></TD>
				<TD><font color='red'>ENABLED</font>&nbsp;&nbsp; -- 
					<i><loc:message name="descriptionInstructorIgnoreTooFar"></loc:message>
				</i></TD>
			</TR>
		</logic:equal>
		<logic:notEmpty name="<%=frmName%>" property="teachingPreference">
		<TR>
			<TD><loc:message name="propertyTeachingPreference"/></TD>
			<TD>
				<logic:iterate scope="request" name="<%=org.unitime.timetable.model.PreferenceLevel.PREF_LEVEL_ATTR_NAME%>" id="pr" type="org.unitime.timetable.model.PreferenceLevel" >
					<logic:equal name="<%=frmName%>" property='teachingPreference' value="<%=pr.getPrefProlog()%>">
						<logic:equal name="pr" property="prefProlog" value="0">
							<b><bean:write name="pr" property="prefName"/></b>
						</logic:equal>
						<logic:notEqual name="pr" property="prefProlog" value="0">
							<div style='color:<%=pr.prefcolor()%>'><b><bean:write name="pr" property="prefName"/></b></div>
						</logic:notEqual>
					</logic:equal>
				</logic:iterate>
			</TD>
		</TR>
		</logic:notEmpty>
		<logic:notEmpty name="<%=frmName%>" property="maxLoad">
			<TR>
				<TD><loc:message name="propertyMaxLoad"/></TD>
				<TD><bean:write name="<%=frmName%>" property="maxLoad"/></TD>
			</TR>
		</logic:notEmpty>
		<logic:iterate scope="request" name="<%=org.unitime.timetable.model.InstructorAttributeType.ATTRIBUTE_TYPES_LIST_ATTR_NAME%>" id="type" type="org.unitime.timetable.model.InstructorAttributeType" >
			<% boolean hasType = false; %>
			<logic:iterate scope="request" name="<%=org.unitime.timetable.model.InstructorAttribute.ATTRIBUTES_LIST_ATTR_NAME%>" id="attribute" type="org.unitime.timetable.model.InstructorAttribute" >
				<logic:equal name="attribute" property="type.uniqueId" value="<%=type.getUniqueId().toString()%>">
					<logic:equal name="<%=frmName%>" property='<%="attribute(" + attribute.getUniqueId() + ")"%>' value="true">
						<% hasType = true; %>
					</logic:equal>
				</logic:equal>
			</logic:iterate>
			<% if (hasType) { %>
			<TR><TD style="vertical-align: top;"><bean:write name="type" property="label"/>:</TD><TD>
			<logic:iterate scope="request" name="<%=org.unitime.timetable.model.InstructorAttribute.ATTRIBUTES_LIST_ATTR_NAME%>" id="attribute" type="org.unitime.timetable.model.InstructorAttribute" >
				<logic:equal name="attribute" property="type.uniqueId" value="<%=type.getUniqueId().toString()%>">
					<logic:equal name="<%=frmName%>" property='<%="attribute(" + attribute.getUniqueId() + ")"%>' value="true">
						<div class='unitime-InstructorAttribute'><bean:write name="attribute" property="name"/></div>
					</logic:equal>
				</logic:equal>
			</logic:iterate>
			</TD></TR>
			<% } %>
		</logic:iterate>

<!-- Class Assignments -->
		<TR>
			<TD colspan="2" align="left">
				&nbsp;<BR>
				<tt:section-title><loc:message name="sectionTitleClassAssignments"/></tt:section-title>
			</TD>
		</TR>
		<TR>
			<TD colspan="2">
				<table width="100%" border="0" cellspacing="0" cellpadding="3">
					<%if (request.getAttribute("classTable") != null ) {%>
						<%=request.getAttribute("classTable")%>
					<%} else { %>
					<TR><TD>&nbsp;</TD></TR>
					<%} %>					
				</table>
			</TD>
		</TR>
		
	<TR>
		<TD colspan="2">
			<tt:exams type='DepartmentalInstructor' add='false'>
				<bean:write name="<%=frmName%>" property="instructorId"/>
			</tt:exams>
		</TD>
	</TR>
	
	<logic:notEmpty scope="request" name="eventTable">
		<TR>
			<TD colspan="2">
				<br>
				<table width="100%" border="0" cellspacing="0" cellpadding="3">
					<bean:write name="eventTable" scope="request" filter="false"/>
				</table>
			</TD>
		</TR>
	</logic:notEmpty>
		
<!-- Preferences -->		
	<% if (frm.isDisplayPrefs()) { %>
		<TR>
			<TD colspan="2" valign="middle">
				<br>
				<tt:section-title>
					<a style="border:0;background:0" 
						accesskey="<%=MSG.accessHideInstructorPreferences()%>" 
						title="<%=MSG.titleHideInstructorPreferences(MSG.accessHideInstructorPreferences()) %>"
						href="javascript:instructorEditForm.op2.value='<%=MSG.actionHideInstructorPreferences() %>'; instructorEditForm.submit();">
					<img border='0' src='images/collapse_node_btn.gif' /></a>
					<loc:message name="sectionTitlePreferences"/>
				</tt:section-title>
			</TD>
		</TR>
		<jsp:include page="preferencesDetail.jspf">
			<jsp:param name="frmName" value="<%=frmName%>"/>
			<jsp:param name="coursePref" value="true"/>
		</jsp:include>
	<% } else { %>
		<TR>
			<TD colspan="2" valign="middle">
				<br>
				<tt:section-title>
					<a style="border:0;background:0" 
						accesskey="<%=MSG.accessShowInstructorPreferences()%>" 
						title="<%=MSG.titleShowInstructorPreferences(MSG.accessShowInstructorPreferences()) %>"
						href="javascript:instructorEditForm.op2.value='<%=MSG.actionDisplayInstructorPreferences() %>'; instructorEditForm.submit();">
						<img border='0' src='images/expand_node_btn.gif' /></a>
					Preferences
					<!--  op2.value='Show Instructor Preferences';submit(); -->
				</tt:section-title>
			</TD>
		</TR>
		<TR>
			<TD colspan="2">
				<br>
			</TD>
		</TR>
	<% } %>
	
		<logic:notEmpty name="<%=frmName%>" property="puId">
			<TR>
				<TD colspan="2">
					<div id='UniTimeGWT:StudentEnrollments' style="display: none;"><bean:write name="<%=frmName%>" property="puId"/></div>
				</TD>
			</TR>
		</logic:notEmpty>
		
		<sec:authorize access="hasPermission(null, 'SolverGroup', 'InstructorScheduling') and hasPermission(#deptCode, 'Department', 'InstructorAssignmentPreferences')">
		<TR>
			<TD colspan="2">
				<a name="assignments"></a>
				<div id='UniTimeGWT:TeachingAssignments' style="display: none;"><bean:write name="<%=frmName%>" property="instructorId" /></div>
			</TD>
		</TR>
		</sec:authorize>

		<tt:last-change type='DepartmentalInstructor'>
			<bean:write name="<%=frmName%>" property="instructorId"/>
		</tt:last-change>		
	
	
		<TR>
			<TD colspan="2" class="WelcomeRowHead">
				&nbsp;
			</TD>
		</TR>

		<TR align="right">
			<TD valign="middle" colspan='2'>
<%--			
			<% if (frm.isDisplayPrefs()) { %>
				<html:submit property="op" 
					styleClass="btn" accesskey="H" titleKey="title.hidePrefs" >
					<bean:message key="button.hidePrefs" />
				</html:submit> 
			<% } else {%>
				<html:submit property="op" 
					styleClass="btn" accesskey="S" titleKey="title.displayPrefs" >
					<bean:message key="button.displayPrefs" />
				</html:submit> 
			<% } %>
--%>
<%--			<sec:authorize access="hasPermission(#instructorId, 'DepartmentalInstructor', 'InstructorEdit')">
					<html:submit property="op" 
						styleClass="btn" accesskey="I" titleKey="title.editInstructorInfo" >
						<bean:message key="button.editInstructorInfo" />
					</html:submit>
				</sec:authorize>
				<sec:authorize access="hasPermission(#instructorId, 'DepartmentalInstructor', 'InstructorPreferences')">
					<html:submit property="op" 
						styleClass="btn" accesskey="P" titleKey="title.editInstructorPref" >
						<bean:message key="button.editInstructorPref" />
					</html:submit>
				</sec:authorize> 
				<logic:notEmpty name="<%=frmName%>" property="previousId">
					<html:submit property="op" 
							styleClass="btn" accesskey="P" titleKey="title.previousInstructor">
						<bean:message key="button.previousInstructor" />
					</html:submit> 
				</logic:notEmpty>
				<logic:notEmpty name="<%=frmName%>" property="nextId">
					<html:submit property="op" 
						styleClass="btn" accesskey="N" titleKey="title.nextInstructor">
						<bean:message key="button.nextInstructor" />
					</html:submit> 
				</logic:notEmpty>
				<tt:back styleClass="btn" name="Back" title="Return to %% (Alt+B)" accesskey="B" type="PreferenceGroup">
					<bean:write name="<%=frmName%>" property="instructorId"/>
				</tt:back>
--%>

				<sec:authorize access="hasPermission(#instructorId, 'DepartmentalInstructor', 'InstructorEdit')">
					<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessEditInstructor() %>" 
							title="<%=MSG.titleEditInstructor(MSG.accessEditInstructor()) %>" >
							<loc:message name="actionEditInstructor" />
					</html:submit>
				</sec:authorize>
				<sec:authorize access="hasPermission(#deptCode, 'Department', 'InstructorAssignmentPreferences')">
					<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessEditInstructorAssignmentPreferences() %>" 
							title="<%=MSG.titleEditInstructorAssignmentPreferences(MSG.accessEditInstructorAssignmentPreferences()) %>" >
							<loc:message name="actionEditInstructorAssignmentPreferences" />
					</html:submit>
				</sec:authorize> 
				<sec:authorize access="hasPermission(#instructorId, 'DepartmentalInstructor', 'InstructorPreferences')">
					<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessEditInstructorPreferences() %>" 
							title="<%=MSG.titleEditInstructorPreferences(MSG.accessEditInstructorPreferences()) %>" >
							<loc:message name="actionEditInstructorPreferences" />
					</html:submit>
				</sec:authorize> 
				<logic:notEmpty name="<%=frmName%>" property="previousId">
					<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessPreviousInstructor() %>" 
							title="<%=MSG.titlePreviousInstructor(MSG.accessPreviousInstructor()) %>" >
							<loc:message name="actionPreviousInstructor" />
					</html:submit>
				</logic:notEmpty>
				<logic:notEmpty name="<%=frmName%>" property="nextId">
					<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessNextInstructor() %>" 
							title="<%=MSG.titleNextInstructor(MSG.accessNextInstructor()) %>" >
							<loc:message name="actionNextInstructor" />
					</html:submit> 
				</logic:notEmpty>
				<tt:back styleClass="btn" 
					name="<%=MSG.actionBackInstructorDetail()%>" 
					title="<%=MSG.titleBackInstructorDetail(MSG.accessBackInstructorDetail())%>" 
					accesskey="<%=MSG.accessBackInstructorDetail() %>" 
					type="PreferenceGroup">
					<bean:write name="<%=frmName%>" property="instructorId"/>
				</tt:back>


<%--
				<html:submit property="op" 
					styleClass="btn" accesskey="B" titleKey="title.returnToDetail">
					<bean:message key="button.returnToDetail" />
				</html:submit>
--%>
			</TD>
		</TR>
	
	</TABLE>
	</loc:bundle>
</html:form>

