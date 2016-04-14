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
<%@page import="org.unitime.timetable.model.DatePattern"%>
<%@page import="org.unitime.timetable.util.IdValue"%>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.form.ClassEditForm" %>
<%@ page import="org.unitime.timetable.model.DepartmentalInstructor" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ page import="org.unitime.timetable.action.ClassEditAction" %>
<%@ page import="org.unitime.timetable.defaults.SessionAttribute"%>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.Vector" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.unitime.org/tags-localization" prefix="loc" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<loc:bundle name="CourseMessages">
<tt:session-context/>
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

<%
	// Get Form 
	String frmName = "ClassEditForm";
	ClassEditForm frm = (ClassEditForm) request.getAttribute(frmName);

	String crsNbr = (String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
%>		
<tiles:importAttribute />
<html:form action="/classInstrAssgnEdit" focus="instructorAssignment">
	<html:hidden property="classId"/>
	<html:hidden property="op2" value="" styleId="op2"/>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<bean:write name='<%=frmName%>' property='className'/>
					</tt:section-title>
					<html:submit property="op" 
						styleClass="btn" 
						accesskey='<%=MSG.accessUpdatePreferences()%>' 
						title='<%=MSG.titleUpdatePreferences(MSG.accessUpdatePreferences()) %>' >
						<loc:message name="actionUpdatePreferences"/>
					</html:submit>
					<sec:authorize access="hasPermission(#ClassEditForm.controllingDept, 'Department', 'InstructorClearAssignmentPreferences')"> 
						&nbsp;
						<html:submit property="op" 
							styleClass="btn" 
							accesskey='<%=MSG.accessClearClassPreferences() %>' 
							title='<%=MSG.titleClearClassInstructorAssignmentPreferences(MSG.accessClearClassPreferences()) %>'>
							<loc:message name="actionClearClassPreferences" />
						</html:submit>
					</sec:authorize> 
					<logic:notEmpty name="<%=frmName%>" property="previousId">
						&nbsp;
						<html:submit property="op" 
							styleClass="btn" 
							accesskey='<%=MSG.accessPreviousClass() %>' 
							title='<%=MSG.titlePreviousClassWithUpdate(MSG.accessPreviousClass())%>'>
							<loc:message name="actionPreviousClass" />
						</html:submit> 
					</logic:notEmpty>
					<logic:notEmpty name="<%=frmName%>" property="nextId">
						&nbsp;
						<html:submit property="op" 
							styleClass="btn" 
							accesskey='<%=MSG.accessNextClass() %>' 
							title='<%=MSG.titleNextClassWithUpdate(MSG.accessNextClass()) %>'>
							<loc:message name="actionNextClass" />
						</html:submit> 
					</logic:notEmpty>
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" 
						accesskey='<%=MSG.accessBackToDetail()%>' 
						title='<%=MSG.titleBackToDetail(MSG.accessBackToDetail()) %>'>
						<loc:message name="actionBackToDetail" />
					</html:submit>
				</tt:section-header>
			</TD>
		</TR>


		<logic:messagesPresent>
		<TR>
			<TD colspan="2" align="left" class="errorCell">
					<B><U><loc:message name="errorsClassEdit"/></U></B><BR>
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
			<TD><loc:message name="filterManager"/></TD>
			<TD>
				<bean:write name="<%=frmName%>" property="managingDeptLabel" />
			</TD>
		</TR>

		<logic:notEqual name="<%=frmName%>" property="parentClassName" value="-">
			<TR>
				<TD><loc:message name="propertyParentClass"/></TD>
				<TD>
					<bean:write name="<%=frmName%>" property="parentClassName" />
				</TD>
			</TR>
		</logic:notEqual>

		<html:hidden property="classSuffix"/>
		<logic:notEmpty name="<%=frmName%>" property="classSuffix">
			<TR>
				<TD><loc:message name="propertyExternalId"/></TD>
				<TD>
					<bean:write name="<%=frmName%>" property="classSuffix" />
				</TD>
			</TR>
		</logic:notEmpty>

		<TR>
			<TD><loc:message name="propertyEnrollment"/></TD>
			<TD>
				<bean:write name="<%=frmName%>" property="enrollment" />
			</TD>
		</TR>
		
		<TR>
			<TD><loc:message name="propertyDatePattern"/></TD>
			<TD>
				<logic:iterate scope="request" name="<%=DatePattern.DATE_PATTERN_LIST_ATTR%>" id="dp">
					<logic:equal name="<%=frmName%>" property="datePattern" value="<%=((IdValue)dp).getId().toString()%>">
						<bean:write name="dp" property="value" />
						<img style="cursor: pointer;" src="images/calendar.png" border="0" onclick="showGwtDialog('Preview of <%=((IdValue)dp).getValue()%>', 'user/dispDatePattern.jsp?id=<%=((IdValue)dp).getId()%>&class='+ClassEditForm.classId.value,'840','520');">
					</logic:equal>
				</logic:iterate>
			</TD>
		</TR>
		
		<TR>
			<TD><loc:message name="propertyDisplayInstructors"/></TD>
			<TD>
				<html:checkbox property="displayInstructor" />
			</TD>
		</TR>
		
		<logic:notEmpty name="<%=frmName%>" property="schedulePrintNote">
		<TR>
			<TD valign="top"><loc:message name="propertyStudentScheduleNote"/></TD>
			<TD>
				<bean:write name="<%=frmName%>" property="schedulePrintNote" />
			</TD>
		</TR>
		</logic:notEmpty>

		<logic:notEmpty name="<%=frmName%>" property="notes">
			<TR>
				<TD valign="top"><loc:message name="propertyRequestsNotes"/></TD>
				<TD>
					<bean:write name="<%=frmName%>" property="notes" filter="false"/>
				</TD>
			</TR>
		</logic:notEmpty>

		
		<TR>
			<TD><loc:message name="propertyNeedInstructorAssignment"/></TD>
			<TD>
				<html:checkbox property="instructorAssignment" onchange="instructorAssignmentChanged();"/> <i><loc:message name="descriptionClassNeedInstructorAssignment"/></i>
			</TD>
		</TR>
		<logic:equal name="<%=frmName%>" property="instructorAssignment" value="true">
		<TR>
			<TD><loc:message name="propertyNbrInstructors"/></TD>
			<TD>
				<html:text property="nbrInstructors" size="10" style="text-align: right;"/>
				<logic:equal name="<%=frmName%>" property="instructorAssignmentDefault" value="true">
					<loc:message name="classEditNbrRoomsDefault"><bean:write name="<%=frmName%>" property="nbrInstructorsDefault"/></loc:message>
				</logic:equal>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="propertyTeachingLoad"/></TD>
			<TD>
				<html:text property="teachingLoad" size="10" style="text-align: right;"/> <loc:message name="teachingLoadUnits"/>
				<logic:equal name="<%=frmName%>" property="instructorAssignmentDefault" value="true">
					<loc:message name="classEditTeachingLoadDefault"><bean:write name="<%=frmName%>" property="teachingLoadDefault"/></loc:message>
				</logic:equal>
			</TD>
		</TR>
		</logic:equal>

		<logic:notEmpty name="<%=frmName%>" property="accommodation">
			<TR>
				<TD valign="top"><loc:message name="propertyAccommodations"/></TD>
				<TD>
					<bean:write name="<%=frmName%>" property="accommodation" filter="false"/>
				</TD>
			</TR>
		</logic:notEmpty>

<%
	if (request.getAttribute("Suggestions.assignmentInfo")!=null) {
%>
		<TR>
			<TD colspan="2" align="left">
				&nbsp;<BR>
				<DIV class="WelcomeRowHead"><loc:message name="sectionTitleTimetable"/></DIV>
			</TD>
		</TR>
		<%=request.getAttribute("Suggestions.assignmentInfo")%>
<%
	}
%>

<!-- Instructors -->
	<sec:authorize access="hasPermission(#ClassEditForm.classId, 'Class_', 'AssignInstructorsClass')">
		<TR><TD colspan='2'>&nbsp;</TD></TR>
		<TR>
			<TD valign="middle" colspan='2'>
			<A name='Instructors'></A>
				<tt:section-header title="<%=MSG.sectionTitleInstructors() %>">
					<html:submit property="op" 
						styleId="addInstructor" 
						styleClass="btn" 
						accesskey="<%=MSG.accessAddInstructor() %>" 
						title="<%=MSG.titleAddInstructor(MSG.accessAddInstructor()) %>">
						<loc:message name="actionAddInstructor" />
					</html:submit> 			
				</tt:section-header>
			</TD>
		</TR>
		<TR>
			<TD colspan="2" align="left">
				<INPUT type="hidden" id="instrListTypeAction" name="instrListTypeAction" value="">				
				<TABLE align="left" cellspacing="0" cellpadding="2" border="0">
					<TR>
						<TD><I><loc:message name="columnInstructorName"/> </I></TD>
						<TD>&nbsp;<I><loc:message name="columnInstructorShare"/> </I>&nbsp;</TD>
						<TD>&nbsp;<I><loc:message name="columnInstructorCheckConflicts"/> </I>&nbsp;</TD>
						<TD>&nbsp;</TD>
					</TR>
					
					<html:hidden property="op2" value="" styleId="op2"/>
					<logic:iterate name="<%=frmName%>" property="instructors" id="instructor" indexId="ctr">
						<TR>
							<TD align="left" nowrap>	
								<html:select style="width:250px;" 
									styleId='<%= "instructors" + ctr %>' 
									property='<%= "instructors[" + ctr + "]" %>'
									onchange='<%= "instructorChanged("+ctr+", this);"%>'>														
									<html:option value="-">-</html:option>
									<html:options collection="<%=DepartmentalInstructor.INSTR_LIST_ATTR_NAME%>" property="value" labelProperty="label" />
								</html:select>
							</TD>
							<html:hidden property='<%="instrHasPref["+ctr+"]" %>' styleId='<%="instrHasPref"+ctr%>'/>
							<TD nowrap align="center">
								<html:text property='<%= "instrPctShare[" + ctr + "]" %>' size="3" maxlength="3" />
							</TD>
							<TD nowrap align="center">
								<html:checkbox property='<%="instrLead[" + ctr + "]"%>'/>
							</TD>
							<TD nowrap>
								<html:submit property="op" 
									styleClass="btn"
									title="<%=MSG.titleRemoveInstructor() %>"
									onclick="<%= \"javascript: doDel('instructor', '\" + ctr + \"');\"%>">
									<loc:message name="actionRemoveInstructor" />
								</html:submit> 			
							</TD>
						</TR>
				   	</logic:iterate>
				</TABLE>
			</TD>
		</TR>
	</sec:authorize>
	<sec:authorize access="not hasPermission(#ClassEditForm.classId, 'Class_', 'AssignInstructorsClass')">
		<logic:notEmpty name="<%=frmName%>" property="instructors">
			<TR>
				<TD colspan="2" align="left">
					&nbsp;<BR><DIV class="WelcomeRowHead"><loc:message name="sectionTitleInstructors"/></DIV>
				</TD>
			</TR>
			<TR>
				<TD colspan="2">
					<table cellspacing="0" cellpadding="3">
						<tr><td width='250'><i><loc:message name="columnInstructorName"/></i></td><td width='80'><i><loc:message name="columnInstructorShare"/></i></td><td width='100'><i><loc:message name="columnInstructorCheckConflicts"/></i></td></tr>
						<logic:iterate name="<%=frmName%>" property="instructors" id="instructor" indexId="ctr">
							<tr onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='hand';this.style.cursor='pointer';" 
								onmouseout="this.style.backgroundColor='transparent';"
								onClick="document.location='instructorDetail.do?instructorId=<%=instructor%>';"
							>
								<td>
								<logic:iterate scope="request" name="<%=DepartmentalInstructor.INSTR_LIST_ATTR_NAME%>" id="instr">
									<logic:equal name="instr" property="value" value="<%=(String)instructor%>">
										<bean:write name="instr" property="label"/>
									</logic:equal>
								</logic:iterate>
								</td>
								<td>
									<bean:write name="<%=frmName%>" property='<%= "instrPctShare[" + ctr + "]" %>' />%
								</td>
								<td>
									<logic:equal name="<%=frmName%>" property='<%="instrLead[" + ctr + "]"%>' value="true"> 
										<IMG border='0' alt='true' align="middle" src='images/accept.png'>
										<%-- <input type='checkbox' checked disabled> --%>
									</logic:equal>
									<%-- 
									<logic:notEqual name="<%=frmName%>" property='<%="instrLead[" + ctr + "]"%>' value="false"> 
										<input type='checkbox' disabled>
									</logic:notEqual>
									--%>
								</td>
								
							</tr>
						</logic:iterate>
					</table>
				</TD>
			</TR>
		</logic:notEmpty>
	</sec:authorize>

		<logic:notEmpty name="<%=frmName%>" property="accommodation">
			<TR>
				<TD valign="top"><loc:message name="propertyAccommodations"/></TD>
				<TD>
					<bean:write name="<%=frmName%>" property="accommodation" filter="false"/>
				</TD>
			</TR>
		</logic:notEmpty>

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
			<jsp:param name="attributePref" value="${ClassEditForm.instructorAssignment}"/>
			<jsp:param name="instructorPref" value="${ClassEditForm.instructorAssignment}"/>
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
					<sec:authorize access="hasPermission(#ClassEditForm.controllingDept, 'Department', 'InstructorClearAssignmentPreferences')"> 
						&nbsp;
						<html:submit property="op" 
							styleClass="btn" 
							accesskey='<%=MSG.accessClearClassPreferences() %>' 
							title='<%=MSG.titleClearClassInstructorAssignmentPreferences(MSG.accessClearClassPreferences()) %>'>
							<loc:message name="actionClearClassPreferences" />
						</html:submit>
					</sec:authorize> 
					<logic:notEmpty name="<%=frmName%>" property="previousId">
						&nbsp;
						<html:submit property="op" 
							styleClass="btn" 
							accesskey='<%=MSG.accessPreviousClass() %>' 
							title='<%=MSG.titlePreviousClassWithUpdate(MSG.accessPreviousClass())%>'>
							<loc:message name="actionPreviousClass" />
						</html:submit> 
					</logic:notEmpty>
					<logic:notEmpty name="<%=frmName%>" property="nextId">
						&nbsp;
						<html:submit property="op" 
							styleClass="btn" 
							accesskey='<%=MSG.accessNextClass() %>' 
							title='<%=MSG.titleNextClassWithUpdate(MSG.accessNextClass()) %>'>
							<loc:message name="actionNextClass" />
						</html:submit> 
					</logic:notEmpty>
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" 
						accesskey='<%=MSG.accessBackToDetail()%>' 
						title='<%=MSG.titleBackToDetail(MSG.accessBackToDetail()) %>'>
						<loc:message name="actionBackToDetail" />
					</html:submit>
				</TD>
		</TR>
		
		
		
	</TABLE>
</html:form>

<SCRIPT type="text/javascript" language="javascript">
	function jumpToAnchor() {
    <% if (request.getAttribute(ClassEditAction.HASH_ATTR) != null) { %>
  		location.hash = "<%=request.getAttribute(ClassEditAction.HASH_ATTR)%>";
	<% } %>
	    self.focus();
  	}
</SCRIPT>
</loc:bundle>