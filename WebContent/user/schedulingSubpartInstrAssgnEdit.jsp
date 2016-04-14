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
<%@ page import="org.unitime.timetable.util.IdValue"%>
<%@ page import="org.unitime.timetable.model.DatePattern"%>
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.action.SchedulingSubpartEditAction" %>
<%@ page import="org.unitime.timetable.form.SchedulingSubpartEditForm" %>
<%@ page import="org.unitime.timetable.model.ItypeDesc"%>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ page import="org.unitime.timetable.model.CourseCreditType" %>
<%@ page import="org.unitime.timetable.model.CourseCreditUnitType" %>
<%@ page import="org.unitime.timetable.model.FixedCreditUnitConfig" %>
<%@ page import="org.unitime.timetable.model.ArrangeCreditUnitConfig" %>
<%@ page import="org.unitime.timetable.model.VariableFixedCreditUnitConfig" %>
<%@ page import="org.unitime.timetable.model.VariableRangeCreditUnitConfig" %>
<%@ page import="org.unitime.timetable.defaults.SessionAttribute"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.unitime.org/tags-localization" prefix="loc" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<tt:session-context/>
<%
	// Get Form 
	String frmName = "SchedulingSubpartEditForm";
	SchedulingSubpartEditForm frm = (SchedulingSubpartEditForm) request.getAttribute(frmName);
	String crsNbr = (String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
%>
<loc:bundle name="CourseMessages">
<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(sessionContext) %>
	
	function instructorAssignmentChanged(){			
		var op2Obj = document.getElementById('op2');
		if (op2Obj!=null) {
			op2Obj.value='updateInstructorAssignment';
			document.forms[0].submit();
		}			
	}

	// -->
</SCRIPT>
		
<tiles:importAttribute />
<html:form action="/schedulingSubpartInstrAssgnEdit" focus="instructorAssignment" >
	<html:hidden property="schedulingSubpartId"/>
	<html:hidden property="op2" value="" styleId="op2"/>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<bean:write name="<%=frmName%>" property="subjectArea" />&nbsp;
						<bean:write name="<%=frmName%>" property="courseNbr" /> : 
						<bean:write name="<%=frmName%>" property="parentSubpart" />
						<B><bean:write name="<%=frmName%>" property="instructionalTypeLabel" /></B>
					</tt:section-title>
					<html:submit property="op" 
						styleClass="btn" 
						accesskey='<%=MSG.accessUpdatePreferences()%>' 
						title='<%=MSG.titleUpdatePreferences(MSG.accessUpdatePreferences()) %>' >
						<loc:message name="actionUpdatePreferences"/>
					</html:submit>
					<sec:authorize access="hasPermission(#SchedulingSubpartEditForm.controllingDept, 'Department', 'InstructorClearAssignmentPreferences')"> 
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" 
						accesskey='<%=MSG.accessClearSubpartPreferences() %>' 
						title='<%=MSG.titleClearSubpartInstructorAssignmentPreferences(MSG.accessClearSubpartPreferences()) %>'>
						<loc:message name="actionClearSubpartPreferences" />
					</html:submit> 
					</sec:authorize>
					<logic:notEmpty name="<%=frmName%>" property="previousId">
						&nbsp;
						<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessPreviousSubpart() %>" 
							title="<%=MSG.titlePreviousSubpartWithUpdate(MSG.accessPreviousSubpart()) %>">
							<loc:message name="actionPreviousSubpart" />
						</html:submit> 
					</logic:notEmpty>
					<logic:notEmpty name="<%=frmName%>" property="nextId">
						&nbsp;
						<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessNextSubpart() %>" 
							title="<%=MSG.titleNextSubpartWithUpdate(MSG.accessNextSubpart()) %>">
							<loc:message name="actionNextSubpart" />
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
					<B><U><loc:message name="errorsSubpartEdit"/></U></B><BR>
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

		<logic:notEmpty name="<%=frmName%>" property="managingDeptName">
			<TR>
				<TD><loc:message name="filterManager"/></TD>
				<TD>
					<bean:write name="<%=frmName%>" property="managingDeptName" />
				</TD>
			</TR>
		</logic:notEmpty>
		<logic:notEmpty name="<%=frmName%>" property="parentSubpartId">
			<TR>
				<TD><loc:message name="propertyParentSchedulingSubpart"/></TD>
				<TD>
					<bean:write name="<%=frmName%>" property="parentSubpartLabel" />
				</TD>
			</TR>
		</logic:notEmpty>
		<TR>
			<TD> <loc:message name="filterInstructionalType"/> </TD>
			<TD>
				<logic:iterate scope="request" name="<%=ItypeDesc.ITYPE_ATTR_NAME%>" id="itp">
					<logic:equal name="<%=frmName%>" property="instructionalType" value="<%=((ItypeDesc)itp).getItype().toString()%>">
						<bean:write name="itp" property="desc"/>
					</logic:equal>
				</logic:iterate>
			</TD>
		</TR>
		<logic:equal name="<%=frmName%>" property="sameItypeAsParent" value="false">
			<logic:notEmpty name="<%=frmName%>" property="creditText">
				<TR>
					<TD><loc:message name="propertySubpartCredit"/></TD>
					<TD>
						<bean:write name="<%=frmName%>" property="creditText" />
					</TD>
				</TR>
			</logic:notEmpty>
		</logic:equal>
		<TR>
			<TD><loc:message name="propertyNeedInstructorAssignment"/></TD>
			<TD>
				<html:checkbox property="instructorAssignment" onchange="instructorAssignmentChanged();"/> <i><loc:message name="descriptionSubpartNeedInstructorAssignment"/></i>
			</TD>
		</TR>
		<logic:equal name="<%=frmName%>" property="instructorAssignment" value="true">
		<TR>
			<TD><loc:message name="propertyNbrInstructors"/></TD>
			<TD>
				<html:text property="nbrInstructors" size="10" style="text-align: right;"/>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="propertyTeachingLoad"/></TD>
			<TD>
				<html:text property="teachingLoad" size="10" style="text-align: right;"/> <loc:message name="teachingLoadUnits"/>
			</TD>
		</TR>
		</logic:equal>

<!-- Preferences -->
		<jsp:include page="preferencesEdit.jspf">
			<jsp:param name="frmName" value="<%=frmName%>"/>
			<jsp:param name="periodPref" value="false"/>
			<jsp:param name="datePatternPref" value="false"/>
			<jsp:param name="timePref" value="false"/>
			<jsp:param name="roomPref" value="false"/>
			<jsp:param name="roomGroupPref" value="false"/>
			<jsp:param name="roomFeaturePref" value="false"/>
			<jsp:param name="bldgPref" value="false"/>
			<jsp:param name="distPref" value="false"/>
			<jsp:param name="attributePref" value="${SchedulingSubpartEditForm.instructorAssignment}"/>
			<jsp:param name="instructorPref" value="${SchedulingSubpartEditForm.instructorAssignment}"/>
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
						accesskey='<%=MSG.accessUpdatePreferences()%>' 
						title='<%=MSG.titleUpdatePreferences(MSG.accessUpdatePreferences()) %>' >
						<loc:message name="actionUpdatePreferences"/>
					</html:submit>
					<sec:authorize access="hasPermission(#SchedulingSubpartEditForm.controllingDept, 'Department', 'InstructorClearAssignmentPreferences')"> 
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" 
						accesskey='<%=MSG.accessClearSubpartPreferences() %>' 
						title='<%=MSG.titleClearSubpartInstructorAssignmentPreferences(MSG.accessClearSubpartPreferences()) %>'>
						<loc:message name="actionClearSubpartPreferences" />
					</html:submit> 
					</sec:authorize>
					<logic:notEmpty name="<%=frmName%>" property="previousId">
						&nbsp;
						<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessPreviousSubpart() %>" 
							title="<%=MSG.titlePreviousSubpartWithUpdate(MSG.accessPreviousSubpart()) %>">
							<loc:message name="actionPreviousSubpart" />
						</html:submit> 
					</logic:notEmpty>
					<logic:notEmpty name="<%=frmName%>" property="nextId">
						&nbsp;
						<html:submit property="op" 
							styleClass="btn" 
							accesskey="<%=MSG.accessNextSubpart() %>" 
							title="<%=MSG.titleNextSubpartWithUpdate(MSG.accessNextSubpart()) %>">
							<loc:message name="actionNextSubpart" />
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
    <% if (request.getAttribute(SchedulingSubpartEditAction.HASH_ATTR) != null) { %>
  		location.hash = "<%=request.getAttribute(SchedulingSubpartEditAction.HASH_ATTR)%>";
	<% } %>
	    self.focus();
  	}
</SCRIPT>
</loc:bundle>