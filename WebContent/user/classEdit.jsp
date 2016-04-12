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
		
		function confirmRoomSizeChange() {
			if (jsConfirm!=null && !jsConfirm)
				return true;

			return ( confirm('<%=MSG.confirmRoomSizeDifferentFromCapacity()%>'));
		}
		
		function instructorChanged(idx, source) {
			var hadPreferences = false;
			var instrHasPrefObj = document.getElementById('instrHasPref'+idx);
			if (instrHasPrefObj!=null && instrHasPrefObj.value=='true')
				hadPreferences = true;
			var instructorId = '-';
			var instructorsObj = document.getElementById('instructors'+idx);
			if (instructorsObj!=null && instructorsObj.selectedIndex>=0)
				instructorId = instructorsObj.options[instructorsObj.selectedIndex].value;
			var hasPreferences = false;
			<%
				Vector instIdWithPrefs = (Vector)request.getAttribute(DepartmentalInstructor.INSTR_HAS_PREF_ATTR_NAME);
				if (instIdWithPrefs!=null)
					for (Enumeration e=instIdWithPrefs.elements();e.hasMoreElements();) {
						Long instrId = (Long)e.nextElement();
						out.println("if (instructorId=='"+instrId+"') hasPreferences=true;");
					}
			%>
			var instrLeadObj = document.getElementById('instrLead'+idx);
			var op2Obj = document.getElementById('op2');
			var isLead = false;
			if (instrLeadObj!=null)
				isLead = instrLeadObj.checked;
			if (instructorId=='-' && instrLeadObj!=null) {
				instrLeadObj.checked=false; isLead=false;
				if (source.id=='instrLead'+idx) {
					alert('<%=MSG.alertSelectAnInstructor()%>');
					if (instructorsObj!=null) instructorsObj.focus();
				}
			}
			if (isLead && hasPreferences) {
				if (op2Obj!=null && <%=JavascriptFunctions.getInheritInstructorPreferencesCondition(sessionContext)%>) {
					op2Obj.value='updatePref';
					document.forms[0].submit();
				}
			} else if (hadPreferences) {
				if (op2Obj!=null && <%=JavascriptFunctions.getCancelInheritInstructorPreferencesCondition(sessionContext)%>) {
					op2Obj.value='updatePref';
					document.forms[0].submit();
				}
			}
		}
		
		function datePatternChanged(){			
			var op2Obj = document.getElementById('op2');
			if (op2Obj!=null) {
				op2Obj.value='updateDatePattern';
				document.forms[0].submit();
			}			
		}
		
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
<html:form action="/classEdit" focus="expectedCapacity">
	<html:hidden property="classId"/>	
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
					<sec:authorize access="hasPermission(#ClassEditForm.classId, 'Class_', 'ClassEditClearPreferences')"> 
						&nbsp;
						<html:submit property="op" 
							styleClass="btn" 
							accesskey='<%=MSG.accessClearClassPreferences() %>' 
							title='<%=MSG.titleClearClassPreferences(MSG.accessClearClassPreferences()) %>'>
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

		<%--
		<TR>
			<TD>Class Division-Secion:</TD>
			<TD>
				<html:text property="classSuffix" maxlength="6" size="6" />
			<TD>
		</TR>
		--%>
		<html:hidden property="classSuffix"/>
		<logic:notEmpty name="<%=frmName%>" property="classSuffix">
			<TR>
				<TD><loc:message name="propertyExternalId"/></TD>
				<TD>
					<bean:write name="<%=frmName%>" property="classSuffix" />
				</TD>
			</TR>
		</logic:notEmpty>

		<%--
		<TR>
			<TD>Minimum Class Limit:</TD>
			<TD>
				<html:text styleId="expectedCapacity" property="expectedCapacity" maxlength="4" size="4" onchange="getMinRoomLimit();"/>
			<TD>
		</TR>

		<TR>
			<TD>Maximum Class Limit:</TD>
			<TD>
				<html:text property="maxExpectedCapacity" maxlength="4" size="4" />
			<TD>
		</TR>

		<TR>
			<TD>Number of Rooms:</TD>
			<TD>
				<html:text styleId="nbrRooms" property="nbrRooms" maxlength="4" size="4" />
			<TD>
		</TR>

		<TR>
			<TD>Room Ratio:</TD>
			<TD>
				<script language="javascript" type="text/javascript">
					function isNumeric(sText) {
					   	var validChars = "0123456789.";
					   	var c;
					
					   	for (i = 0; i < sText.length; i++) { 
					      c = sText.charAt(i); 
					      if (validChars.indexOf(c) == -1) 
					      	return false;
				      	}
					   	return true;				   
				   	}
				   	
					function getMinRoomLimit() {
						var rr = document.getElementById('roomRatio').value;
						var nr = document.getElementById('nbrRooms').value;
						var mec = document.getElementById('expectedCapacity').value;
						var mrl = document.getElementById('minRoomLimit');
						
						if (nr!=null && isNumeric(nr) && nr>0 &&
							mec!=null && isNumeric(mec) && mec>=0 &&
							rr!=null && isNumeric(rr) && rr>0 ) {
							if (mec==0)
								mrl.value = Math.ceil(rr);
							else
								mrl.value = Math.ceil(mec*rr);
						} else {
							mrl.value = '0';
						}
					}
					
				</script>
				<html:text styleId="roomRatio" property="roomRatio" maxlength="4" size="4" onchange="getMinRoomLimit();" />
				&nbsp;&nbsp;&nbsp;&nbsp; Minimum Room Capacity: <html:text readonly="true" tabindex="500" styleId="minRoomLimit" property="minRoomLimit" maxlength="4" size="4" disabled="true"/>
			<TD>
		</TR>
		--%>
		
		<html:hidden property="nbrRooms"/>
		<html:hidden property="enrollment"/>
		<html:hidden property="expectedCapacity"/>
		<html:hidden property="maxExpectedCapacity"/>
		<html:hidden property="roomRatio"/>
		<TR>
			<TD><loc:message name="propertyEnrollment"/></TD>
			<TD>
				<bean:write name="<%=frmName%>" property="enrollment" />
			</TD>
		</TR>
		
		<logic:notEqual name="<%=frmName%>" property="nbrRooms" value="0">
			<% if (frm.getExpectedCapacity().intValue()==frm.getMaxExpectedCapacity().intValue()) { %>
				<TR>
					<TD><loc:message name="propertyClassLimit"/></TD>
					<TD>
						<bean:write name="<%=frmName%>" property="expectedCapacity" />
					</TD>
				</TR>
			<% } else { %>
				<TR>
					<TD><loc:message name="propertyMinimumClassLimit"/></TD>
					<TD>
						<bean:write name="<%=frmName%>" property="expectedCapacity" />
					</TD>
				</TR>
				<TR>
					<TD><loc:message name="propertyMaximumClassLimit"/></TD>
					<TD>
						<bean:write name="<%=frmName%>" property="maxExpectedCapacity" />
					</TD>
				</TR>
			<% } %>
		</logic:notEqual>

		<TR>
			<TD><loc:message name="propertyNumberOfRooms"/></TD>
			<TD>
				<bean:write name="<%=frmName%>" property="nbrRooms" />
			</TD>
		</TR>
		
		<logic:notEqual name="<%=frmName%>" property="nbrRooms" value="0">
			<TR>
				<TD><loc:message name="propertyRoomRatio"/></TD>
				<TD>
					<bean:write name="<%=frmName%>" property="roomRatio" />
					&nbsp;&nbsp;&nbsp;&nbsp; ( <loc:message name="propertyMinimumRoomCapacity"/> <bean:write name="<%=frmName%>" property="minRoomLimit" /> )
				</TD>
			</TR>
		</logic:notEqual>
		

		<TR>
			<TD><loc:message name="propertyDatePattern"/></TD>
			<TD>
				<html:select style="width:200px;" property="datePattern" onchange='<%= "datePatternChanged();"%>'>
					<html:options collection="<%=org.unitime.timetable.model.DatePattern.DATE_PATTERN_LIST_ATTR%>" property="id" labelProperty="value" />
				</html:select>
				<img style="cursor: pointer;" src="images/calendar.png" border="0" onclick="showGwtDialog('Preview of '+ClassEditForm.datePattern.options[ClassEditForm.datePattern.selectedIndex].text, 'user/dispDatePattern.jsp?id='+ClassEditForm.datePattern.value+'&class='+ClassEditForm.classId.value,'840','520');">
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propertyDisplayInstructors"/></TD>
			<TD>
				<html:checkbox property="displayInstructor" />
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="propertyEnabledForStudentScheduling"/></TD>
			<TD>
				<html:checkbox property="enabledForStudentScheduling" />
			</TD>
		</TR>

		<TR>
			<TD valign="top"><loc:message name="propertyStudentScheduleNote"/></TD>
			<TD>
				<html:textarea property="schedulePrintNote" cols="70" rows="4"  />
			</TD>
		</TR>
		
		<TR>
			<TD><loc:message name="propertyNeedInstructorAssignment"/></TD>
			<TD>
				<html:checkbox property="instructorAssignment" onchange="instructorAssignmentChanged();"/> <i><loc:message name="descriptionNeedInstructorAssignment"/></i>
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
<!-- Requests / Notes -->
		<TR>
			<TD colspan="2" align="left">
				&nbsp;<BR>
				<tt:section-title><loc:message name="sectionTitleNotesToScheduleManager"/></tt:section-title>
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="left">
			<html:textarea property="notes" rows="3" cols="80"></html:textarea>
			</TD>
		</TR>
		</logic:equal>
<!-- Instructors -->
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
									<html:options collection="<%=DepartmentalInstructor.INSTR_LIST_ATTR_NAME + ctr%>" property="value" labelProperty="label" />
								</html:select>
							</TD>
							<html:hidden property='<%="instrHasPref["+ctr+"]" %>' styleId='<%="instrHasPref"+ctr%>'/>
							<TD nowrap align="center">
								<html:text property='<%= "instrPctShare[" + ctr + "]" %>' size="3" maxlength="3" />
							</TD>
							<TD nowrap align="center">
								<html:checkbox property='<%="instrLead[" + ctr + "]"%>' 
									styleId='<%= "instrLead" + ctr %>' 
									onclick='<%= "instructorChanged("+ctr+", this);"%>'
								/>
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

<!-- Preferences -->
		<logic:equal value="0" name="<%=frmName%>" property="nbrRooms">
			<jsp:include page="preferencesEdit.jspf">
				<jsp:param name="frmName" value="<%=frmName%>"/>
				<jsp:param name="distPref" value="false"/>
				<jsp:param name="periodPref" value="false"/>
				<jsp:param name="bldgPref" value="false"/>
				<jsp:param name="roomFeaturePref" value="false"/>
				<jsp:param name="roomGroupPref" value="false"/>
				<jsp:param name="attributePref" value="${ClassEditForm.instructorAssignment}"/>
				<jsp:param name="instructorPref" value="${ClassEditForm.instructorAssignment}"/>
			</jsp:include>
		</logic:equal>
		<logic:notEqual value="0" name="<%=frmName%>" property="nbrRooms">
			<logic:equal value="true" name="<%=frmName%>" property="unlimitedEnroll">
				<jsp:include page="preferencesEdit.jspf">
					<jsp:param name="frmName" value="<%=frmName%>"/>
					<jsp:param name="distPref" value="false"/>
					<jsp:param name="periodPref" value="false"/>
					<jsp:param name="bldgPref" value="false"/>
					<jsp:param name="roomFeaturePref" value="false"/>
					<jsp:param name="roomGroupPref" value="false"/>
					<jsp:param name="attributePref" value="${ClassEditForm.instructorAssignment}"/>
					<jsp:param name="instructorPref" value="${ClassEditForm.instructorAssignment}"/>
				</jsp:include>
			</logic:equal>
			<logic:notEqual value="true" name="<%=frmName%>" property="unlimitedEnroll">
				<jsp:include page="preferencesEdit.jspf">
					<jsp:param name="frmName" value="<%=frmName%>"/>
					<jsp:param name="distPref" value="false"/>
					<jsp:param name="periodPref" value="false"/>
					<jsp:param name="attributePref" value="${ClassEditForm.instructorAssignment}"/>
					<jsp:param name="instructorPref" value="${ClassEditForm.instructorAssignment}"/>
				</jsp:include>
			</logic:notEqual>
		</logic:notEqual>
		
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
					<sec:authorize access="hasPermission(#ClassEditForm.classId, 'Class_', 'ClassEditClearPreferences')"> 
						&nbsp;
						<html:submit property="op" 
							styleClass="btn" 
							accesskey='<%=MSG.accessClearClassPreferences() %>' 
							title='<%=MSG.titleClearClassPreferences(MSG.accessClearClassPreferences()) %>'>
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