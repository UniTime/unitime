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
<%@ page import="org.unitime.timetable.util.IdValue" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.timetable.model.DatePattern" %>
<%@ page import="org.unitime.timetable.form.InstructionalOfferingModifyForm" %>
<%@ page import="org.unitime.timetable.defaults.SessionAttribute"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.unitime.org/tags-localization" prefix="loc" %>


<loc:bundle name="CourseMessages">

<SCRIPT language="javascript">
	<!--

		function doClick(op, id) {
			document.forms[0].elements["hdnOp"].value=op;
			document.forms[0].elements["id"].value=id;
			document.forms[0].elements["click"].value="y";
			document.forms[0].submit();
		}
		
	// -->
</SCRIPT>
<SCRIPT language="javascript">
	<!--
	
	function updateSubpartTotal(subpartIndex) {
	    displayInstructors = document.getElementsByName('displayDisplayInstructors')[0].value;
	    displayEnabledForStudentScheduling = document.getElementsByName('displayEnabledForStudentScheduling')[0].value;

		subtotalName='subtotalIndexes['+subpartIndex+']';
		origLimitName='origMinLimit['+subpartIndex+']';
		minLimitName='minClassLimits['+subpartIndex+']';
		totalIndex=document.getElementsByName(subtotalName)[0].value;
		subtotalValueName='subtotalValues['+totalIndex+']';
		subtotalValueName1='subtotal1Values'+totalIndex;
		if (displayInstructors != 'false' || displayEnabledForStudentScheduling != 'false') {
			subtotalValueName2='subtotal2Values' + totalIndex;
		}
		origTotal=document.getElementsByName(subtotalValueName)[0].value;
		origSubpartLimit=document.getElementsByName(origLimitName)[0].value;
		newSubpartLimit=document.getElementsByName(minLimitName)[0].value;
		if(newSubpartLimit.length == 0 || (newSubpartLimit.search("[^0-9]")) >= 0) 
              { newSubpartLimit = 0;}
		newTotal=origTotal-origSubpartLimit+(newSubpartLimit-0);
		document.getElementsByName(subtotalValueName)[0].value=newTotal;
		document.getElementById(subtotalValueName1).innerHTML=newTotal;
		if (displayInstructors != 'false' || displayEnabledForStudentScheduling != 'false') {
			document.getElementById(subtotalValueName2).innerHTML=newTotal; 
		}
		document.getElementsByName(origLimitName)[0].value=newSubpartLimit;
	}
	
	// -->
</SCRIPT>


<tiles:importAttribute />
<tt:session-context/>
<% 
	String frmName = "instructionalOfferingModifyForm";
	InstructionalOfferingModifyForm frm = (InstructionalOfferingModifyForm)request.getAttribute(frmName);
	String crsNbr = (String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
%>
<SCRIPT language="javascript">
	<!--
	
	function updateSubpartFlags(value, baseName, subpartIndex, flagName) {
		for (var i=0;i<<%=frm.getClassIds().size()%>;i++) {
                  var chbox = document.getElementsByName(baseName+'['+i+']');
                  var subtotalIndexName = 'subtotalIndexes['+i+']';
                  var subpartIndexValue = document.getElementsByName(subtotalIndexName)[0].value;
                  if ((subpartIndexValue * 1) == (subpartIndex * 1)
                   && chbox!=null && chbox.length>0)
                   {     chbox[0].checked = value;}
                  	
            }
        var subpartFlag = document.getElementsByName(flagName+'['+subpartIndex+']');
        subpartFlag[0].checked = value;
        subpartFlag[1].checked = value;
	}
	
	// -->
</SCRIPT>

<script language='JavaScript'>
      function resetAllDisplayFlags(value, baseName) {
            for (var i=0;i<<%=frm.getClassIds().size()%>;i++) {
                  var chbox = document.getElementsByName(baseName+'['+i+']');
                  if (chbox!=null && chbox.length>0)
                        chbox[0].checked = value;
            }
      }
</script>

<script language="javascript">displayLoading();</script>

<html:form action="/instructionalOfferingModify">
	<html:hidden property="instrOfferingId"/>	
	<html:hidden property="instrOfferingName"/>	
	<html:hidden property="instrOffrConfigId"/>	
	<html:hidden property="origSubparts"/>
	<html:hidden property="displayMaxLimit"/>
	<html:hidden property="displayOptionForMaxLimit"/>
	<html:hidden property="displayEnrollment"/>
	<html:hidden property="displayExternalId"/>
	<html:hidden property="displayDisplayInstructors"/>
	<html:hidden property="displayEnabledForStudentScheduling"/>
	<INPUT type="hidden" name="hdnOp" value = "">
	<INPUT type="hidden" name="id" value = "">
	<INPUT type="hidden" name="click" value = "">
	<INPUT type="hidden" name="deletedClassId" value = "">
	<INPUT type="hidden" name="addTemplateClassId" value = "">
	<INPUT type="hidden" name="moveUpClassId" value = "">
	<INPUT type="hidden" name="moveDownClassId" value = "">

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
<!-- Buttons -->
		<TR>
			<TD valign="middle" colspan="2">
				 <tt:section-header>
					<tt:section-title>
					<bean:write name="<%=frmName%>" property="instrOfferingName" />
					</tt:section-title>						
				<html:submit property="op" disabled="true"
					styleClass="btn" 
					accesskey="<%=MSG.accessUpdateMultipleClassSetup() %>" 
					title="<%=MSG.titleUpdateMultipleClassSetup(MSG.accessUpdateMultipleClassSetup()) %>" >
					<loc:message name="actionUpdateMultipleClassSetup" />
				</html:submit>
				<bean:define id="instrOfferingId">
					<bean:write name="<%=frmName%>" property="instrOfferingId" />				
				</bean:define>
				 
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
					<B><U><loc:message name="errorsMultipleClassSetup"/></U></B><BR>
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
		
		<html:hidden property="instrOffrConfigUnlimitedReadOnly"/>
		<logic:equal name="<%=frmName%>" property="instrOffrConfigUnlimitedReadOnly" value="true">
			<logic:equal name="<%=frmName%>" property="instrOffrConfigUnlimited" value="true">
				<TR><TD align="left" colspan="2">
					<loc:message name="propertyUnlimitedEnrollment"/>&nbsp;&nbsp;&nbsp;<IMG border='0' title='<%=MSG.titleUnlimitedEnrollment()%>' alt='true' align='middle' src='images/accept.png'>
				</TD></TR>
			</logic:equal>
			<html:hidden property="instrOffrConfigUnlimited"/>
		</logic:equal>
		<logic:notEqual name="<%=frmName%>" property="instrOffrConfigUnlimitedReadOnly" value="true">
			<TR>
				<TD align="left" colspan="2">
						<loc:message name="propertyUnlimitedEnrollment"/>&nbsp;&nbsp;&nbsp;<html:checkbox property="instrOffrConfigUnlimited"
							onclick="document.forms[0].elements['hdnOp'].value='unlimited';document.forms[0].submit();"/>
				</TD>
			</TR>
		</logic:notEqual>
		<logic:equal name="<%=frmName%>" property="instrOffrConfigUnlimited" value="true">
			<html:hidden property="instrOffrConfigLimit"/>
		</logic:equal>
		<logic:notEqual name="<%=frmName%>" property="instrOffrConfigUnlimited" value="true">
			<TR>
				<TD align="left" colspan="2">
					<loc:message name="propertyConfigurationLimit"/>&nbsp;&nbsp;&nbsp;<html:text property="instrOffrConfigLimit" maxlength="5" size="5"/>
				</TD>
			</TR>
		</logic:notEqual>
		<TR>
			<TD align="left" colspan="2">
			<table align="left" border="0" cellspacing="0" cellpadding="1">
				<tr>
					<td valign="top">
						<loc:message name="propertySchedulingSubpartLimits"/>
					</td>
					<td> &nbsp;&nbsp;&nbsp;</td>
					<td valign="top">
						<table align="left" border="0" cellspacing="0" cellpadding="0">
							<logic:iterate name="<%=frmName%>" property="subtotalValues" id="v" indexId="ctr">
								<tr onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='default';" onmouseout="this.style.backgroundColor='transparent';"> 
									<td valign="top" align="right" nowrap>
										<html:hidden property='<%= "subtotalLabels[" + ctr + "]" %>'/>
										<html:hidden property='<%= "subtotalValues[" + ctr + "]" %>'/>
										<b><%=((String)frm.getSubtotalLabels().get(ctr)).trim()%>:</b> &nbsp; 
									</td> 
									<td align="right" nowrap>
										<div id='<%= "subtotal1Values" + ctr %>'>
											<logic:equal name="<%=frmName%>" property="instrOffrConfigUnlimited" value="true">
												&infin;										
											</logic:equal>
											<logic:notEqual name="<%=frmName%>" property="instrOffrConfigUnlimited" value="true">
												<bean:write name="<%=frmName%>" property='<%= "subtotalValues[" + ctr + "]" %>'/>
											</logic:notEqual>
										</div>
									</td>
									<logic:equal name="<%=frmName%>" property="displayDisplayInstructors" value="true" >
										<TD align="center" nowrap>
											&nbsp; &nbsp; <loc:message name="propertyDisplayInstructors"/> 
											<logic:equal name="<%=frmName%>" property='<%= "readOnlySubparts[" + ctr + "]" %>' value="false" >
												<html:checkbox name="<%=frmName%>" property='<%= "displayAllClassesInstructorsForSubpart[" + ctr + "]" %>' 
													onclick="<%= \"updateSubpartFlags(this.checked, 'displayInstructors', \"+ctr+\", 'displayAllClassesInstructorsForSubpart');\"%>"/>
											</logic:equal>
											<logic:equal name="<%=frmName%>" property='<%= "readOnlySubparts[" + ctr + "]" %>' value="true" >
												<logic:equal name="<%=frmName%>" property='<%= "displayAllClassesInstructorsForSubpart[" + ctr + "]" %>' value="true" >
													<IMG border='0' title='<%=MSG.titleDisplayAllInstrForSubpartInSchedBook()%>' alt='true' align='middle' src='images/accept.png'>
												</logic:equal>
												<html:hidden property='<%= "displayAllClassesInstructorsForSubpart[" + ctr + "]" %>'/>
											</logic:equal>
										</TD>
									</logic:equal>
									<logic:equal name="<%=frmName%>" property="displayEnabledForStudentScheduling" value="true" >	
										<TD align="center" nowrap>
											&nbsp; &nbsp; <loc:message name="propertyEnabledForStudentScheduling"/> 
											<logic:equal name="<%=frmName%>" property='<%= "readOnlySubparts[" + ctr + "]" %>' value="false" >
												<html:checkbox name="<%=frmName%>" property='<%= "enableAllClassesForStudentSchedulingForSubpart[" + ctr + "]" %>' 
													onclick="<%= \"updateSubpartFlags(this.checked, 'enabledForStudentScheduling', \"+ctr+\", 'enableAllClassesForStudentSchedulingForSubpart');\"%>"/>
											</logic:equal>
											<logic:equal name="<%=frmName%>" property='<%= "readOnlySubparts[" + ctr + "]" %>' value="true" >
												<logic:equal name="<%=frmName%>" property='<%= "enableAllClassesForStudentSchedulingForSubpart[" + ctr + "]" %>' value="true" >
													<IMG border='0' title='<%=MSG.titleEnableAllClassesOfSubpartForStudentScheduling()%>' alt='true' align='middle' src='images/accept.png'>
												</logic:equal>
												<html:hidden property='<%= "enableAllClassesForStudentSchedulingForSubpart[" + ctr + "]" %>'/>
											</logic:equal>
										</TD>	
									</logic:equal>	
								</tr>
							</logic:iterate>			
			<!-- </tr> -->	
						</table>
					</td>
				</tr>
			</table>
			</TD>
		</TR>
		<TR>
			<TD colspan="2" align="left">
				<TABLE align="left" border="0" cellspacing="0" cellpadding="1">
					<TR>
						<logic:notEqual name="<%=frmName%>" property="instrOffrConfigUnlimited" value="true">
							<logic:equal name="<%=frmName%>" property="displayOptionForMaxLimit" value="true" >
								<TD align="left" valign="bottom" rowSpan="2" colspan="2" class='WebTableHeader'>
									<html:checkbox name="<%=frmName%>" property="displayMaxLimit" onclick="doClick('multipleLimits', 0);"/> 
									<small><loc:message name="columnAllowVariableLimits"/></small>
								</TD>
							</logic:equal>
							<logic:equal name="<%=frmName%>" property="displayOptionForMaxLimit" value="false" >
								<TD align="center" valign="bottom" rowSpan="2" colspan="2" class='WebTableHeader'> &nbsp;</TD>
							</logic:equal>
						</logic:notEqual>
						<logic:equal name="<%=frmName%>" property="instrOffrConfigUnlimited" value="true">
							<TD align="center" valign="bottom" rowSpan="2" colspan="2" class='WebTableHeader'> &nbsp;</TD>
						</logic:equal>
						<logic:equal name="<%=frmName%>" property="displayExternalId" value="true" >
							<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
							<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'><loc:message name="columnExternalId"/></TD>
						</logic:equal>
						<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
						<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
						<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
						<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
						<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
						<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
						<logic:equal name="<%=frmName%>" property="displayEnrollment" value="true" >
							<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnEnroll"/></TD>
							<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
						</logic:equal>
						<logic:notEqual name="<%=frmName%>" property="instrOffrConfigUnlimited" value="true">
							<logic:equal name="<%=frmName%>" property="displayMaxLimit" value="true" >
								<TD align="center" valign="bottom" colSpan="2" class='WebTableHeaderFirstRow'><loc:message name="columnLimit"/></TD>
							</logic:equal>
							<logic:equal name="<%=frmName%>" property="displayMaxLimit" value="false" >
								<TD align="center" valign="bottom" colSpan="2" rowspan="2" class='WebTableHeader'><loc:message name="columnLimit"/></TD>
							</logic:equal>
							<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnRoomRatioBr"/></TD>
							<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnNbrRms"/></TD>
						</logic:notEqual>
						<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnManagingDepartment"/></TD>
						<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnDatePattern"/></TD>
						<TD align="center" valign="bottom" rowSpan="1" class='WebTableHeaderFirstRow'>
							<logic:equal name="<%=frmName%>" property="displayDisplayInstructors" value="true" >
								<loc:message name="columnDisplayInstr"/>
							</logic:equal>
						</TD>
						<TD align="center" valign="bottom" rowSpan="1" class='WebTableHeaderFirstRow'>
							<logic:equal name="<%=frmName%>" property="displayEnabledForStudentScheduling" value="true" >
								<loc:message name="columnStudentScheduling"/>
							</logic:equal>
						</TD>
						<TD align="center" valign="bottom" rowSpan="1" colspan="2" class='WebTableHeaderFirstRow'>---&nbsp;<loc:message name="columnTimetable"/>&nbsp;---</TD>
						<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
						<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnInstructors"/></TD>
						<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
					</TR>
					<TR>
						<logic:notEqual name="<%=frmName%>" property="instrOffrConfigUnlimited" value="true">
							<logic:equal name="<%=frmName%>" property="displayMaxLimit" value="true" >
								<TD align="center" valign="bottom" class='WebTableHeaderSecondRow'><loc:message name="columnMin"/></TD>
								<TD align="center" valign="bottom" class='WebTableHeaderSecondRow'><loc:message name="columnMax"/></TD>
							</logic:equal>
						</logic:notEqual>			
						<td align="center" valign="bottom" class='WebTableHeaderSecondRow'>
							<logic:equal name="<%=frmName%>" property="displayDisplayInstructors" value="true" >
								(<loc:message name="propertyAll"/>
								 <html:checkbox name="<%=frmName%>" property="displayAllClassesInstructors" onclick="resetAllDisplayFlags(this.checked, 'displayInstructors')" />)
							</logic:equal>
						</td>
						<td align="center" valign="bottom" class='WebTableHeaderSecondRow'>
							<logic:equal name="<%=frmName%>" property="displayEnabledForStudentScheduling" value="true" >
								(<loc:message name="propertyAll"/>
								 <html:checkbox name="<%=frmName%>" property="enableAllClassesForStudentScheduling" onclick="resetAllDisplayFlags(this.checked, 'enabledForStudentScheduling')"/>)
							</logic:equal>
						</td>						
						<TD align="center" valign="bottom" rowSpan="1" class='WebTableHeaderSecondRow'><loc:message name="columnAssignedTime"/></TD>
						<TD align="center" valign="bottom" rowSpan="1" class='WebTableHeaderSecondRow'><loc:message name="columnAssignedRoom"/></TD>
					</TR>					
					<logic:iterate name="<%=frmName%>" property="classIds" id="c" indexId="ctr">
						<TR onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='default';" onmouseout="this.style.backgroundColor='transparent';">
							<TD nowrap valign="top">
								<logic:equal name="<%=frmName%>" property='<%= "classHasErrors[" + ctr + "]" %>' value="true" >
									<IMG src="images/cancel.png">
								</logic:equal>
								<logic:equal name="<%=frmName%>" property='<%= "classHasErrors[" + ctr + "]" %>' value="false" >
									&nbsp;
								</logic:equal></TD>
							<TD nowrap valign="top">
								<html:hidden property='<%= "externalIds[" + ctr + "]" %>'/>
								<html:hidden property='<%= "classIds[" + ctr + "]" %>'/>
								<html:hidden property='<%= "subpartIds[" + ctr + "]" %>'/>
								<html:hidden property='<%= "itypes[" + ctr + "]" %>'/>
								<html:hidden property='<%= "mustHaveChildClasses[" + ctr + "]" %>'/>
								<html:hidden property='<%= "parentClassIds[" + ctr + "]" %>'/>
								<html:hidden property='<%= "readOnlyClasses[" + ctr + "]" %>'/>
								<html:hidden property='<%= "enrollments[" + ctr + "]" %>'/>
								<html:hidden property='<%= "classCanMoveUp[" + ctr + "]" %>'/>
								<html:hidden property='<%= "classCanMoveDown[" + ctr + "]" %>'/>
								<html:hidden property='<%= "subtotalIndexes[" + ctr + "]" %>'/>
								<html:hidden property='<%= "classHasErrors[" + ctr + "]" %>'/>
								<html:hidden property='<%= "classLabels[" + ctr + "]" %>'/>
								<html:hidden property='<%= "classLabelIndents[" + ctr + "]" %>'/>
								<%=frm.getClassLabelIndents().get(ctr.intValue()).toString()%>
								<bean:write name="<%=frmName%>" property='<%= "classLabels[" + ctr + "]" %>'/> 
								&nbsp;
							</TD>
							<logic:equal name="<%=frmName%>" property="displayExternalId" value="true" >
								<TD>&nbsp;</TD>
								<TD align="left" valign="top" nowrap><%=frm.getExternalIds().get(ctr)%></TD>
							</logic:equal>
						
							<TD>&nbsp;</TD>
							<TD align="center" valign="top" nowrap>
								<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" >
									<logic:equal name="<%=frmName%>" property='<%= "classCanMoveUp[" + ctr + "]" %>' value="true" >
										<IMG border="0" src="images/arrow_u.gif" title="<%=MSG.titleMoveClassUp()%>"
											onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
											onclick="document.forms[0].elements['hdnOp'].value='moveUp';document.forms[0].elements['moveUpClassId'].value='<%=c.toString()%>';document.forms[0].submit();">
									</logic:equal>
								</logic:equal>
							</TD>
							<TD align="center" valign="top" nowrap>
								<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" >
									<logic:equal name="<%=frmName%>" property='<%= "classCanMoveDown[" + ctr + "]" %>' value="true" >
										<IMG border="0" src="images/arrow_d.gif" title="<%=MSG.titleMoveClassDown()%>"
											onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
											onclick="document.forms[0].elements['hdnOp'].value='moveDown';document.forms[0].elements['moveDownClassId'].value='<%=c.toString()%>';document.forms[0].submit();">
									</logic:equal>
								</logic:equal>
							</TD>
							<TD align="center" valign="top" nowrap>
								<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" >
									<IMG border="0" src="images/action_delete.png" title="<%=MSG.titleRemoveClassFromIO()%>"
										onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
										onclick="document.forms[0].elements['hdnOp'].value='delete';document.forms[0].elements['deletedClassId'].value='<%=c.toString()%>';document.forms[0].submit();">
								</logic:equal>
							</TD>
							<TD align="center" valign="top" nowrap>
								<IMG border="0" src="images/action_add.png" title="<%=MSG.titleAddClassToIO()%>"
									onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
									onclick="document.forms[0].elements['hdnOp'].value='add';document.forms[0].elements['addTemplateClassId'].value='<%=c.toString()%>';document.forms[0].submit();">
							</TD>
							<TD>&nbsp;</TD>
							<logic:equal name="<%=frmName%>" property="displayEnrollment" value="true" >
								<TD align="right" valign="top" nowrap><bean:write name="<%=frmName%>" property='<%= "enrollments[" + ctr + "]" %>'/></TD>
								<TD>&nbsp;</TD>
							</logic:equal>
							<logic:equal name="<%=frmName%>" property="instrOffrConfigUnlimited" value="true">
								<html:hidden property='<%= "minClassLimits[" + ctr + "]" %>'/>
								<html:hidden property='<%= "maxClassLimits[" + ctr + "]" %>'/>
								<html:hidden property='<%= "roomRatios[" + ctr + "]" %>'/>
								<html:hidden property='<%= "numberOfRooms[" + ctr + "]" %>'/>
							</logic:equal>
							<logic:notEqual name="<%=frmName%>" property="instrOffrConfigUnlimited" value="true">							
							<TD align="left" nowrap valign="top">
								<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" >
									<html:hidden property='<%= "origMinLimit[" + ctr + "]" %>' value="<%= (String)frm.getMinClassLimits().get(ctr) %>"/>
									<html:text name="<%=frmName%>" property='<%= "minClassLimits[" + ctr + "]" %>' tabindex="<%=java.lang.Integer.toString(2000 + ctr.intValue())%>" 
										maxlength="5" size="4" onchange="<%= \"updateSubpartTotal(\" + ctr + \");document.getElementsByName('maxClassLimits[\" + ctr + \"]')[0].value=this.value\"%>"/>
								</logic:equal>
								<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" >
									<html:hidden property='<%= "minClassLimits[" + ctr + "]" %>'/>
									<bean:write name="<%=frmName%>" property='<%= "minClassLimits[" + ctr + "]" %>'/>
								</logic:equal>
							</TD>
							<logic:equal name="<%=frmName%>" property="displayMaxLimit" value="true" >
								<TD align="left" nowrap valign="top">
									<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" >
										<html:text name="<%=frmName%>" property='<%= "maxClassLimits[" + ctr + "]" %>' tabindex="<%=java.lang.Integer.toString(4000 + ctr.intValue())%>" 
											maxlength="5" size="4"/>
									</logic:equal>
									<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" >
										<html:hidden property='<%= "maxClassLimits[" + ctr + "]" %>'/>
										<bean:write name="<%=frmName%>" property='<%= "maxClassLimits[" + ctr + "]" %>'/>
									</logic:equal>
								</TD>
							</logic:equal>
							<logic:equal name="<%=frmName%>" property="displayMaxLimit" value="false" >
								<TD align="left" valign="top" nowrap>
									<html:hidden property='<%= "maxClassLimits[" + ctr + "]" %>'/>
								</TD>
							</logic:equal>
							<TD align="left" valign="top" nowrap>
								<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" >
									<html:text name="<%=frmName%>" property='<%= "roomRatios[" + ctr + "]" %>' tabindex="<%=java.lang.Integer.toString(6000 + ctr.intValue())%>" 
										maxlength="6" size="3"/>
								</logic:equal>
								<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" >
									<bean:write name="<%=frmName%>" property='<%= "roomRatios[" + ctr + "]" %>'/>
									<html:hidden property='<%= "roomRatios[" + ctr + "]" %>'/>
								</logic:equal>
							</TD>
							<TD align="left" valign="top" nowrap>
								<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" >
									<html:text name="<%=frmName%>" property='<%= "numberOfRooms[" + ctr + "]" %>' tabindex="<%=java.lang.Integer.toString(8000 + ctr.intValue())%>" 
										maxlength="5" size="3"/>
								</logic:equal>
								<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" >
									<bean:write name="<%=frmName%>" property='<%= "numberOfRooms[" + ctr + "]" %>'/>
									<html:hidden property='<%= "numberOfRooms[" + ctr + "]" %>'/>
								</logic:equal>
							</TD>
							</logic:notEqual>
							<TD align="left" valign="top" nowrap>
								<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" >
									<html:select style="width:200px;" property='<%= "departments[" + ctr + "]" %>' 
												 tabindex="<%=java.lang.Integer.toString(10000 + ctr.intValue())%>">
										<html:option value="-1"><loc:message name="dropDeptDepartment"/></html:option>
										<html:options collection='<%=Department.EXTERNAL_DEPT_ATTR_NAME + "list"%>' property="uniqueId" labelProperty="managingDeptLabel" />
									</html:select>
								</logic:equal>
								<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" >
									<logic:iterate scope="request" name="<%=Department.EXTERNAL_DEPT_ATTR_NAME%>" id="dept">
										<logic:equal name="<%=frmName%>" property='<%= "departments[" + ctr + "]" %>' value="<%=((Department)dept).getUniqueId().toString()%>">
											<bean:write name="dept" property="managingDeptLabel" />
										</logic:equal>
									</logic:iterate>
									<html:hidden property='<%= "departments[" + ctr + "]" %>'/>
								</logic:equal>
							</TD>
							<TD align="left" valign="top" nowrap>
								<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" >
									<html:select style="width:100px;" property='<%= "datePatterns[" + ctr + "]" %>' 
											     tabindex="<%=java.lang.Integer.toString(12000 + ctr.intValue())%>">
										<html:options collection="<%=DatePattern.DATE_PATTERN_LIST_ATTR%>" property="id" labelProperty="value" />
									</html:select>
								</logic:equal>
								<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" >
									<logic:equal name="<%=frmName%>" property='<%= "datePatterns[" + ctr + "]"%>' value="">
										<loc:message name="dropDefaultDatePattern"/>
									</logic:equal>
									<logic:iterate scope="request" name="<%=DatePattern.DATE_PATTERN_LIST_ATTR%>" id="dp">
										<logic:notEqual name="<%=frmName%>" property='<%= "datePatterns[" + ctr + "]" %>' value="">
											<logic:equal name="<%=frmName%>" property='<%= "datePatterns[" + ctr + "]" %>' value="<%=((IdValue)dp).getId().toString()%>">
												<bean:write name="dp" property="value" />
											</logic:equal>
										</logic:notEqual>
									</logic:iterate>
									<html:hidden property='<%= "datePatterns[" + ctr + "]" %>'/>
								</logic:equal>
							</TD>
							<TD align="center" valign="top" nowrap>
								<logic:equal name="<%=frmName%>" property="displayDisplayInstructors" value="true" >
									<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" >
										<html:checkbox name="<%=frmName%>" property='<%= "displayInstructors[" + ctr + "]" %>' tabindex="<%=java.lang.Integer.toString(14000 + ctr.intValue())%>"/>
									</logic:equal>
										<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" >					
											<logic:equal name="<%=frmName%>" property='<%= "displayInstructors[" + ctr + "]" %>' value="true" >
												<IMG border='0' title='<%=MSG.titleDisplayAllInstrForSubpartInSchedBook()%>' alt='true' align='middle' src='images/accept.png'>
											</logic:equal>
											<html:hidden property='<%= "displayInstructors[" + ctr + "]" %>'/>
										</logic:equal>
									</logic:equal>
								<logic:equal name="<%=frmName%>" property="displayDisplayInstructors" value="false" >
									<html:hidden property='<%= "displayInstructors[" + ctr + "]" %>'/>
								</logic:equal>
							</TD>
							<TD align="center" valign="top" nowrap>
								<logic:equal name="<%=frmName%>" property="displayEnabledForStudentScheduling" value="true" >
									<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" >
										<html:checkbox name="<%=frmName%>" property='<%= "enabledForStudentScheduling[" + ctr + "]" %>' tabindex="<%=java.lang.Integer.toString(16000 + ctr.intValue())%>"/>
									</logic:equal>
									<logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" >
										<logic:equal name="<%=frmName%>" property='<%= "enabledForStudentScheduling[" + ctr + "]" %>' value="true" >
											<IMG border='0' title='<%=MSG.titleEnableTheseClassesForStudentScheduling()%>' alt='true' align='middle' src='images/accept.png'>
										</logic:equal><html:hidden property='<%= "enabledForStudentScheduling[" + ctr + "]" %>'/>
									</logic:equal>
								</logic:equal>
								<logic:equal name="<%=frmName%>" property="displayEnabledForStudentScheduling" value="false" >
									<html:hidden property='<%= "enabledForStudentScheduling[" + ctr + "]" %>'/>
								</logic:equal>
							</TD>
							<TD align="left" valign="top" nowrap>
								<%=frm.getTimes().get(ctr)%>&nbsp;&nbsp;
								<html:hidden property='<%= "times[" + ctr + "]" %>'/>
							</TD>
							<TD align="left" valign="top" nowrap>
								<%=frm.getRooms().get(ctr)%>
								<html:hidden property='<%= "rooms[" + ctr + "]" %>'/>
							</TD>
							<TD>&nbsp;</TD>
							<TD align="left" valign="top" nowrap>
								<%=frm.getInstructors().get(ctr)%>
								<html:hidden property='<%= "instructors[" + ctr + "]" %>'/>
							</TD>
							<TD>&nbsp;</TD>
						</TR>
					</logic:iterate>
				</TABLE>
			</TD>
		</TR>
		<%
			if (frm.getDisplayDisplayInstructors().booleanValue() || frm.getDisplayEnabledForStudentScheduling().booleanValue()){
		%>
		<TR>
			<TD align="left" colspan="2">
				<table align="left" border="0" cellspacing="0" cellpadding="0">
					<tr> 
						<td valign="top">
							<loc:message name="propertySchedulingSubpartLimits"/>
						</td> 
						<td> &nbsp;&nbsp;&nbsp;</td>
						<td valign="middle">
							<table align="left" border="0" cellspacing="0" cellpadding="0">
								<logic:iterate name="<%=frmName%>" property="subtotalValues" id="v" indexId="ctr">				
									<tr onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='default';" onmouseout="this.style.backgroundColor='transparent';">
										<td valign="top" align="right" nowrap>
											<b><%=((String)frm.getSubtotalLabels().get(ctr)).trim()%>:</b> &nbsp; 
										</td> 
										<td nowrap align="right">
											<div id='<%="subtotal2Values" + ctr%>'>
												<logic:equal name="<%=frmName%>" property="instrOffrConfigUnlimited" value="true">
													&infin;										
												</logic:equal>
												<logic:notEqual name="<%=frmName%>" property="instrOffrConfigUnlimited" value="true">
													<bean:write name="<%=frmName%>" property='<%= "subtotalValues[" + ctr + "]" %>'/>
												</logic:notEqual>
											</div>
										</td>
										<logic:equal name="<%=frmName%>" property="displayDisplayInstructors" value="true" >
											<TD align="center" nowrap>
												&nbsp; &nbsp; <loc:message name="propertyDisplayInstructors"/> 
												<logic:equal name="<%=frmName%>" property='<%= "readOnlySubparts[" + ctr + "]" %>' value="false" >
													<html:checkbox name="<%=frmName%>" property='<%= "displayAllClassesInstructorsForSubpart[" + ctr + "]" %>' 
														onclick="<%= \"updateSubpartFlags(this.checked, 'displayInstructors', \"+ctr+\", 'displayAllClassesInstructorsForSubpart');\"%>"/>
												</logic:equal>
												<logic:equal name="<%=frmName%>" property='<%= "readOnlySubparts[" + ctr + "]" %>' value="true" >
													<logic:equal name="<%=frmName%>" property='<%= "displayAllClassesInstructorsForSubpart[" + ctr + "]" %>' value="true" >
														<IMG border='0' title='<%=MSG.titleDisplayAllInstrForSubpartInSchedBook()%>' alt='true' align="middle" src='images/accept.png'>
													</logic:equal>
													<html:hidden property='<%= "displayAllClassesInstructorsForSubpart[" + ctr + "]" %>'/>
												</logic:equal>
											</TD>
										</logic:equal>
										<logic:equal name="<%=frmName%>" property="displayEnabledForStudentScheduling" value="true" >	
											<TD align="center" nowrap>
												&nbsp; &nbsp; <loc:message name="propertyEnabledForStudentScheduling"/> 
												<logic:equal name="<%=frmName%>" property='<%= "readOnlySubparts[" + ctr + "]" %>' value="false" >
													<html:checkbox name="<%=frmName%>" property='<%= "enableAllClassesForStudentSchedulingForSubpart[" + ctr + "]" %>' 
														onclick="<%= \"updateSubpartFlags(this.checked, 'enabledForStudentScheduling', \"+ctr+\", 'enableAllClassesForStudentSchedulingForSubpart');\"%>"/>
												</logic:equal>
												<logic:equal name="<%=frmName%>" property='<%= "readOnlySubparts[" + ctr + "]" %>' value="true" >
													<logic:equal name="<%=frmName%>" property='<%= "enableAllClassesForStudentSchedulingForSubpart[" + ctr + "]" %>' value="true" >
														<IMG border='0' title='<%=MSG.titleEnableAllClassesOfSubpartForStudentScheduling()%>' alt='true' align='middle' src='images/accept.png'>
													</logic:equal>
													<html:hidden property='<%= "enableAllClassesForStudentSchedulingForSubpart[" + ctr + "]" %>'/>
												</logic:equal>
											</TD>
										</logic:equal>	
									</tr>
								</logic:iterate>
							</table>
						</td>
					</tr>
				</table>
			</td>
		</tr>
	<% } %>
		
		
<SCRIPT language="javascript">
	<!--		
			document.forms[0].elements["op"][0].disabled="";	
	// -->
	</SCRIPT>

<!-- Buttons -->
		<TR>
			<TD colspan="2" valign="middle">
				<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="right">
				<html:submit property="op" 
					styleClass="btn" 
					accesskey="<%=MSG.accessUpdateMultipleClassSetup() %>" 
					title="<%=MSG.titleUpdateMultipleClassSetup(MSG.accessUpdateMultipleClassSetup()) %>" >
					<loc:message name="actionUpdateMultipleClassSetup" />
				</html:submit>
			
				<bean:define id="instrOfferingId">
					<bean:write name="<%=frmName%>" property="instrOfferingId" />				
				</bean:define>
				 
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
	<script language="javascript">displayElement('loading', false);</script>

</loc:bundle>
