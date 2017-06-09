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
<%@ page import="org.unitime.timetable.defaults.UserProperty"%>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.model.DepartmentalInstructor" %>
<%@ page import="org.unitime.timetable.form.ClassInstructorAssignmentForm" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ page import="org.unitime.timetable.defaults.SessionAttribute"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.unitime.org/tags-localization" prefix="loc" %>
<tiles:importAttribute />

<loc:bundle name="CourseMessages">

<tt:session-context/>
<% 
	String frmName = "classInstructorAssignmentForm";
	ClassInstructorAssignmentForm frm = (ClassInstructorAssignmentForm)request.getAttribute(frmName);
	String crsNbr = (String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
%>

<html:form action="/classInstructorAssignment">
<html:hidden name="<%=frmName%>" property="instrOffrConfigId"/>
<html:hidden property="instrOfferingId"/>	
<html:hidden property="displayExternalId"/>
<html:hidden property="defaultTeachingResponsibilityId"/>
<INPUT type="hidden" name="deletedInstrRowNum" value = "">
<INPUT type="hidden" name="addInstructorId" value = "">
<INPUT type="hidden" name="hdnOp" value = "">

<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(sessionContext) %>
		
		function confirmUnassignAll() {
			if (jsConfirm!=null && !jsConfirm)
				return true;

			if (!confirm('<%=MSG.confirmUnassignAllInstructors() %>')) {
				return false;
			}

			return true;
		}

	// -->
</SCRIPT>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2" valign="middle">
				 <tt:section-header>
					<tt:section-title>
							<A  title="<%=MSG.titleBackToIOList(MSG.accessBackToIOList()) %>" 
								accesskey="<%=MSG.accessBackToIOList() %>"
								class="l8" 
								href="instructionalOfferingShowSearch.do?doit=Search&subjectAreaId=<bean:write name="<%=frmName%>" 
										property="subjectAreaId" />&courseNbr=<%=crsNbr%>#A<bean:write name="<%=frmName%>" property="instrOfferingId" />"
							><bean:write name="<%=frmName%>" property="instrOfferingName" /></A>
							<html:hidden property="instrOfferingId"/>
							<html:hidden property="instrOfferingName"/>
					</tt:section-title>
				
				<!-- dummy submit button to make sure Update button is the first (a.k.a. default) submit button -->
				<html:submit property="op" style="position: absolute; left: -100%;"><loc:message name="actionUpdateClassInstructorsAssignment" /></html:submit>						

				<html:submit property="op"
					onclick="return confirmUnassignAll();"
					styleClass="btn" 
					title="<%=MSG.titleUnassignAllInstructorsFromConfig() %>">
					<loc:message name="actionUnassignAllInstructorsFromConfig" />
				</html:submit>
				 
				&nbsp;
				<html:submit property="op"
					styleClass="btn" 
					accesskey="<%=MSG.accessUpdateClassInstructorsAssignment() %>" 
					title="<%=MSG.titleUpdateClassInstructorsAssignment(MSG.accessUpdateClassInstructorsAssignment()) %>">
					<loc:message name="actionUpdateClassInstructorsAssignment" />
				</html:submit>
			
				<bean:define id="instrOfferingId">
					<bean:write name="<%=frmName%>" property="instrOfferingId" />				
				</bean:define>

				<logic:notEmpty name="<%=frmName%>" property="previousId">
					<html:hidden name="<%=frmName%>" property="previousId"/>
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" 
						accesskey="<%=MSG.accessPreviousIO() %>" 
						title="<%=MSG.titlePreviousIOWithUpdate(MSG.accessPreviousIO()) %>">
						<loc:message name="actionPreviousIO" />
					</html:submit> 
				</logic:notEmpty>
				<logic:notEmpty name="<%=frmName%>" property="nextId">
					<html:hidden name="<%=frmName%>" property="nextId"/>
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" 
						accesskey="<%=MSG.accessNextIO() %>" 
						title="<%=MSG.titleNextIOWithUpdate(MSG.accessNextIO()) %>">
						<loc:message name="actionNextIO" />
					</html:submit> 
				</logic:notEmpty>
				 
				&nbsp;
				<html:button property="op" 
					styleClass="btn" 
					accesskey="<%=MSG.accessBackToIODetail() %>" 
					title="<%=MSG.titleBackToIODetail(MSG.accessBackToIODetail()) %>" 
					onclick="document.location.href='instructionalOfferingDetail.do?op=view&io=${instrOfferingId}';">
					<loc:message name="actionBackToIODetail" />
				</html:button>		
				</tt:section-header>					
			</TD>
		</TR>

		<logic:messagesPresent>
		<TR>
			<TD colspan="2" align="left" class="errorCell">
					<B><U><loc:message name="errors"/></U></B><BR>
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
			<TR>
				<TD valign="top"><loc:message name="propertyCoordinators"/></TD>
				<TD>
					<bean:write name="classInstructorAssignmentForm" property="coordinators" filter="false"/>
				</TD>
			</TR>
		</TR>
		<TR>
			<TD colspan="2" align="left">
				<TABLE align="left" border="0" cellspacing="0" cellpadding="1">
					<TR>
						<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'> &nbsp;</TD>
						<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'> &nbsp;</TD>
						<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'> &nbsp;</TD>
						<logic:equal name="<%=frmName%>" property="displayExternalId" value="true" >
							<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
							<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'><loc:message name="columnExternalId"/></TD>
						</logic:equal>
						<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
						<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
						<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
						<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
						<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'><loc:message name="columnInstructorName"/></TD>
						<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'><loc:message name="columnInstructorShare"/></TD>
						<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'><loc:message name="columnInstructorCheckConflictsBr"/>&nbsp;&nbsp;</TD>
						<logic:notEmpty name="responsibilities" scope="request">
							<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'><loc:message name="columnTeachingResponsibility"/></TD>
						</logic:notEmpty>
						<TD align="center" valign="bottom" class='WebTableHeaderFirstRow'><loc:message name="columnDisplay"/>&nbsp;</TD>
						<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
						<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'><loc:message name="columnAssignedTime"/></TD>
						<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
						<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'><loc:message name="columnAssignedRoom"/></TD>
						<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
					</TR>
					<TR>
						<TD align="left" valign="bottom" class='WebTableHeaderSecondRow'>
							(<loc:message name="propertyAll"/>
							<input type='checkbox' checked='checked' 
									onclick='resetAllDisplayFlags(this.checked);' value='test'>)
						</TD>
					</TR>

					<logic:iterate name="<%=frmName%>" property="classIds" id="c" indexId="ctr">
						<TR onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='default';" onmouseout="this.style.backgroundColor='transparent';">
							<TD nowrap valign="top">
								<html:hidden property='<%= "classIds[" + ctr + "]" %>'/>
								<html:hidden property='<%= "classLabels[" + ctr + "]" %>'/>
								<html:hidden property='<%= "classLabelIndents[" + ctr + "]" %>'/>
								<html:hidden property='<%= "rooms[" + ctr + "]" %>'/>
								<html:hidden property='<%= "times[" + ctr + "]" %>'/>
								<html:hidden property='<%= "allowDeletes[" + ctr + "]" %>'/>
								<html:hidden property='<%= "readOnlyClasses[" + ctr + "]" %>'/>
								<html:hidden property='<%= "classHasErrors[" + ctr + "]" %>'/>
								<html:hidden name="<%=frmName%>" property='<%= "showDisplay[" + ctr + "]" %>' />
								&nbsp;
							</TD>
							<TD nowrap valign="top">
								<logic:equal name="<%=frmName%>" property='<%= "classHasErrors[" + ctr + "]" %>' value="true" >
									<IMG src="images/cancel.png">
								</logic:equal>
								<logic:equal name="<%=frmName%>" property='<%= "classHasErrors[" + ctr + "]" %>' value="false" >
									&nbsp;
								</logic:equal>
							</TD>
							<TD nowrap valign="top">
								<%=frm.getClassLabelIndents().get(ctr.intValue()).toString()%>
								<bean:write name="<%=frmName%>" property='<%= "classLabels[" + ctr + "]" %>'/> 
								&nbsp;
							</TD>
	
							<logic:equal name="<%=frmName%>" property="displayExternalId" value="true" >
								<TD>&nbsp;</TD>
								<TD align="left" valign="top" nowrap><%= frm.getExternalIds().get(ctr)%></TD>
							</logic:equal>
							<TD>&nbsp;</TD>
							<TD align="center" valign="top" nowrap>
								<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" >
									<logic:equal name="<%=frmName%>" property='<%= "allowDeletes[" + ctr + "]" %>' value="true" >
										<IMG border="0" src="images/action_delete.png" title="<%=MSG.titleRemoveInstructorFromClass() %>"
											onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
											onclick="document.forms[0].elements['hdnOp'].value='Delete';
													document.forms[0].elements['deletedInstrRowNum'].value='<%= ctr.toString() %>';
													document.forms[0].submit();">
										</logic:equal>
								</logic:equal>
							</TD>
							<TD align="center" valign="top" nowrap> &nbsp;
								<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" >
									<IMG border="0" src="images/action_add.png" title="<%=MSG.titleAddInstructorToClass() %>"
										onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
										onclick="document.forms[0].elements['hdnOp'].value='Add Instructor';
												document.forms[0].elements['addInstructorId'].value='<%= ctr.toString() %>';
												document.forms[0].submit();">
								</logic:equal>
							</TD>
							<TD>&nbsp;<html:hidden property='<%= "externalIds[" + ctr + "]" %>'/></TD>
							<TD align="left" valign="top" nowrap>
								<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" >
									<html:select style="width:200px;" property='<%= "instructorUids[" + ctr + "]" %>' tabindex="<%=java.lang.Integer.toString(10000 + ctr.intValue())%>">
										<loc:bundle name="ConstantsMessages" id="CONST">	
											<html:option value="<%= Constants.BLANK_OPTION_VALUE%>"><loc:message name="select" id="CONST"/></html:option>
										</loc:bundle>
										<html:options collection="<%=DepartmentalInstructor.INSTR_LIST_ATTR_NAME%>" property="uniqueId" labelProperty="nameLastFirst" />
									</html:select>
								</logic:equal>
								<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" >
									<% String nameFormat = UserProperty.NameFormat.get(sessionContext.getUser()); %>
									<logic:iterate scope="request" name="<%=DepartmentalInstructor.INSTR_LIST_ATTR_NAME%>" id="instr">
										<logic:equal name="<%=frmName%>" property='<%= "instructorUids[" + ctr + "]" %>' value="<%=((DepartmentalInstructor)instr).getUniqueId().toString()%>">
											<%=((DepartmentalInstructor)instr).getName(nameFormat)%>
										</logic:equal>
									</logic:iterate>
									<html:hidden property='<%= "instructorUids[" + ctr + "]" %>'/>
								</logic:equal>
							</TD>
							
							<TD align="left" valign="top" nowrap>
								<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" >
									<html:text name="<%=frmName%>" property='<%= "percentShares[" + ctr + "]" %>' 
											tabindex="<%=java.lang.Integer.toString(4000 + ctr.intValue())%>" maxlength="5" size="5"/>
								</logic:equal>
								<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" >
									<html:hidden property='<%= "percentShares[" + ctr + "]" %>'/>
									<logic:notEmpty name="<%=frmName%>" property='<%= "instructorUids[" + ctr + "]" %>'>
										<bean:write name="<%=frmName%>" property='<%= "percentShares[" + ctr + "]" %>'/>
									</logic:notEmpty>
								</logic:equal>
							</TD>
							<TD align="center" valign="top" nowrap>
								<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" >
									<html:checkbox name="<%=frmName%>" property='<%= "leadFlags[" + ctr + "]" %>' 
											tabindex="<%=java.lang.Integer.toString(6000 + ctr.intValue())%>"/>
								</logic:equal>
								<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" >
									<html:hidden property='<%= "leadFlags[" + ctr + "]" %>'/>
									<logic:notEmpty name="<%=frmName%>" property='<%= "instructorUids[" + ctr + "]" %>'>
										<bean:write name="<%=frmName%>" property='<%= "leadFlags[" + ctr + "]" %>'/>
									</logic:notEmpty>
								</logic:equal>
							</TD>
							<logic:notEmpty name="responsibilities" scope="request">
								<TD align="center" valign="top" nowrap>
									<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" >
										<html:select tabindex="<%=java.lang.Integer.toString(8000 + ctr.intValue())%>"
											property='<%= "responsibilities[" + ctr + "]" %>'>
											<logic:equal name="<%=frmName%>" property='<%= "responsibilities[" + ctr + "]" %>' value="">
												<html:option value="">-</html:option>
											</logic:equal>
											<logic:notEqual name="<%=frmName%>" property='<%= "responsibilities[" + ctr + "]" %>' value="">
												<logic:empty name="<%=frmName%>" property='defaultTeachingResponsibilityId'>
													<html:option value="">-</html:option>
												</logic:empty>
											</logic:notEqual>
											<html:options collection="responsibilities" property="uniqueId" labelProperty="label" />
										</html:select>
									</logic:equal>
									<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" >
										<html:hidden property='<%= "responsibilities[" + ctr + "]" %>'/>
										<bean:define name="<%=frmName%>" property='<%= "responsibilities[" + ctr + "]" %>' id="r" type="java.lang.String"/>
										<logic:iterate id="responsibility" name="responsibilities" scope="request">
											<logic:equal name="responsibility" property="uniqueId" value="<%=(String)r%>"><bean:write name="responsibility" property="label"/></logic:equal>
										</logic:iterate>
									</logic:equal>
								</TD>
							</logic:notEmpty>
							<logic:empty name="responsibilities" scope="request">
								<html:hidden property='<%= "responsibilities[" + ctr + "]" %>'/>
							</logic:empty>
							<TD align="center" valign="top" nowrap>
								<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" >
									<logic:equal name="<%=frmName%>" property='<%= "showDisplay[" + ctr + "]" %>' value="true" >
										<html:checkbox name="<%=frmName%>" property='<%= "displayFlags[" + ctr + "]" %>' 
												tabindex="<%=java.lang.Integer.toString(10000 + ctr.intValue())%>"/>
									</logic:equal>
								</logic:equal>
								<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" >
									<html:hidden property='<%= "displayFlags[" + ctr + "]" %>'/>
										<bean:write name="<%=frmName%>" property='<%= "displayFlags[" + ctr + "]" %>'/>
								</logic:equal></TD>
							<TD>&nbsp;</TD>
							<TD align="left" valign="top" nowrap>
								&nbsp;
								<bean:write name="<%=frmName%>" property='<%= "times[" + ctr + "]" %>'/>
							</TD>
							<TD>&nbsp;</TD>
							<TD align="left" valign="top" nowrap><%= frm.getRooms().get(ctr)%></TD>
							<TD>&nbsp;</TD>
						</TR>
					</logic:iterate>
				</TABLE>
			</TD>
		</TR>

<!-- Buttons -->
		<TR>
			<TD colspan="2" valign="middle">
				<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="right">
				<html:submit property="op"
					onclick="return confirmUnassignAll();"
					styleClass="btn" 
					title="<%=MSG.titleUnassignAllInstructorsFromConfig() %>">
					<loc:message name="actionUnassignAllInstructorsFromConfig" />
				</html:submit>
			 
				&nbsp;
				<html:submit property="op"
					styleClass="btn" 
					accesskey="<%=MSG.accessUpdateClassInstructorsAssignment() %>" 
					title="<%=MSG.titleUpdateClassInstructorsAssignment(MSG.accessUpdateClassInstructorsAssignment()) %>">
					<loc:message name="actionUpdateClassInstructorsAssignment" />
				</html:submit>
			
				<bean:define id="instrOfferingId">
					<bean:write name="<%=frmName%>" property="instrOfferingId" />				
				</bean:define>

				<logic:notEmpty name="<%=frmName%>" property="previousId">
					<html:hidden name="<%=frmName%>" property="previousId"/>
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" 
						accesskey="<%=MSG.accessPreviousIO() %>" 
						title="<%=MSG.titlePreviousIOWithUpdate(MSG.accessPreviousIO()) %>">
						<loc:message name="actionPreviousIO" />
					</html:submit> 
				</logic:notEmpty>
				<logic:notEmpty name="<%=frmName%>" property="nextId">
					<html:hidden name="<%=frmName%>" property="nextId"/>
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" 
						accesskey="<%=MSG.accessNextIO() %>" 
						title="<%=MSG.titleNextIOWithUpdate(MSG.accessNextIO()) %>">
						<loc:message name="actionNextIO" />
					</html:submit> 
				</logic:notEmpty>

				&nbsp;
				<html:button property="op" 
					styleClass="btn" 
					accesskey="<%=MSG.accessBackToIODetail() %>" 
					title="<%=MSG.titleBackToIODetail(MSG.accessBackToIODetail()) %>" 
					onclick="document.location.href='instructionalOfferingDetail.do?op=view&io=${instrOfferingId}';">
					<loc:message name="actionBackToIODetail" />
				</html:button>		
					
			</TD>
		</TR>

	</TABLE>

</html:form>

<script language='JavaScript'>
      function resetAllDisplayFlags(value) {
            for (var i=0;i<<%=frm.getClassIds().size()%>;i++) {
                  var chbox = document.getElementsByName('displayFlags['+i+']');
                  if (chbox!=null && chbox.length>0)
                        chbox[0].checked = value;
            }
      }
</script>

</loc:bundle>