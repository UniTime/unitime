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
<%@ page import="org.unitime.timetable.action.InstructorAssignmentPrefAction"%>
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

<loc:bundle name="CourseMessages">
<tt:session-context/>
<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(sessionContext) %>
	// -->
</SCRIPT>

<html:form action="instructorAssignmentPref">
	<html:hidden property="instructorId"/>
	<html:hidden property="name"/>
	<html:hidden property="nextId"/>
	<html:hidden property="previousId"/>
	
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

		<TR>
			<TD><loc:message name="propertyTeachingPreference"/></TD>
			<TD>
				<html:select property="teachingPreference" size="1">
					<logic:iterate scope="request" name="<%=org.unitime.timetable.model.PreferenceLevel.PREF_LEVEL_ATTR_NAME%>" id="pr" type="org.unitime.timetable.model.PreferenceLevel" >
						<logic:notEqual name="pr" property="prefProlog" value="R">
							<html:option
								style='<%="background-color:" + pr.prefcolor() + ";"%>'
								value="<%=pr.getPrefProlog()%>" >
								<bean:write name="pr" property="prefName"/>
							</html:option>
						</logic:notEqual>
					</logic:iterate>
				</html:select>
				<i><loc:message name="descriptionTeachingPreference"/></i>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="propertyMaxLoad"/></TD>
			<TD>
				<html:text property="maxLoad" size="10" style="text-align: right;"/> <loc:message name="teachingLoadUnits"/>
			</TD>
		</TR>
		
		<logic:notEmpty scope="request" name="<%=org.unitime.timetable.model.InstructorAttributeType.ATTRIBUTE_TYPES_LIST_ATTR_NAME%>">
		<TR><TD colspan='2'>&nbsp;</TD></TR>
		<TR>
			<TD valign="middle" colspan='2' class='WelcomeRowHead'>
				<loc:message name="sectionAttributes"/>
			</TD>
		</TR>
		<logic:iterate scope="request" name="<%=org.unitime.timetable.model.InstructorAttributeType.ATTRIBUTE_TYPES_LIST_ATTR_NAME%>" id="type" type="org.unitime.timetable.model.InstructorAttributeType" >
			<TR><TD style="vertical-align: top;"><bean:write name="type" property="label"/>:</TD><TD>
			<div class='unitime-InstructorAttributes'>
			<logic:iterate scope="request" name="<%=org.unitime.timetable.model.InstructorAttribute.ATTRIBUTES_LIST_ATTR_NAME%>" id="attribute" type="org.unitime.timetable.model.InstructorAttribute" >
				<logic:equal name="attribute" property="type.uniqueId" value="<%=type.getUniqueId().toString()%>">
					<span class="attribute">
					<html:checkbox property='<%="attribute(" + attribute.getUniqueId() + ")"%>'><bean:write name="attribute" property="name"/></html:checkbox>
					</span>
				</logic:equal>
			</logic:iterate>
			</TD></TR>
			</div>
		</logic:iterate>
		</logic:notEmpty>
		
<!-- Preferences -->
		<jsp:include page="preferencesEdit.jspf">
			<jsp:param name="frmName" value="<%=frmName%>"/>
			<jsp:param name="periodPref" value="false"/>
			<jsp:param name="datePatternPref" value="false"/>
			<jsp:param name="timePref" value="false"/>
			<jsp:param name="timeAvail" value="true"/>
			<jsp:param name="roomPref" value="false"/>
			<jsp:param name="roomGroupPref" value="false"/>
			<jsp:param name="roomFeaturePref" value="false"/>
			<jsp:param name="bldgPref" value="false"/>
			<jsp:param name="coursePref" value="true"/>
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
    <% if (request.getAttribute(InstructorAssignmentPrefAction.HASH_ATTR) != null) { %>
  		location.hash = "<%=request.getAttribute(InstructorAssignmentPrefAction.HASH_ATTR)%>";
	<% } %>
		self.focus();
  	}
	
</SCRIPT>
</loc:bundle>	
