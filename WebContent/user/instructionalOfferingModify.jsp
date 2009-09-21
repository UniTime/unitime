<%--
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
--%>
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.util.IdValue" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.timetable.model.DatePattern" %>
<%@ page import="org.unitime.timetable.form.InstructionalOfferingModifyForm" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

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
		subtotalName='subtotalIndexes['+subpartIndex+']';
		origLimitName='origMinLimit['+subpartIndex+']';
		minLimitName='minClassLimits['+subpartIndex+']'
		totalIndex=document.getElementsByName(subtotalName)[0].value;
		subtotalValueName='subtotalValues['+totalIndex+']';
		subtotalValueName1='subtotal1Values'+totalIndex;
		subtotalValueName2='subtotal2Values' + totalIndex;
		origTotal=document.getElementsByName(subtotalValueName)[0].value;
		origSubpartLimit=document.getElementsByName(origLimitName)[0].value;
		newSubpartLimit=document.getElementsByName(minLimitName)[0].value;
		if(newSubpartLimit.length == 0 || (newSubpartLimit.search("[^0-9]")) >= 0) 
              { newSubpartLimit = 0;}
		newTotal=origTotal-origSubpartLimit+(newSubpartLimit-0);
		document.getElementsByName(subtotalValueName)[0].value=newTotal;
		document.getElementById(subtotalValueName1).innerHTML=newTotal;
		document.getElementById(subtotalValueName2).innerHTML=newTotal; 
		document.getElementsByName(origLimitName)[0].value=newSubpartLimit;
	}
	
	// -->
</SCRIPT>


<tiles:importAttribute />
<% 
	String frmName = "instructionalOfferingModifyForm";
	InstructionalOfferingModifyForm frm = (InstructionalOfferingModifyForm)request.getAttribute(frmName);
	String crsNbr = "";
	if (session.getAttribute(Constants.CRS_NBR_ATTR_NAME)!=null )
		crsNbr = session.getAttribute(Constants.CRS_NBR_ATTR_NAME).toString();
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
	<html:hidden property="displayDisplayInSchedule"/>
	<INPUT type="hidden" name="hdnOp" value = "">
	<INPUT type="hidden" name="id" value = "">
	<INPUT type="hidden" name="click" value = "">
	<INPUT type="hidden" name="deletedClassId" value = "">
	<INPUT type="hidden" name="addTemplateClassId" value = "">
	<INPUT type="hidden" name="moveUpClassId" value = "">
	<INPUT type="hidden" name="moveDownClassId" value = "">

	<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">
<!-- Buttons -->
		<TR>
			<TD valign="middle" colspan="2">
				 <tt:section-header>
					<tt:section-title>
					<bean:write name="<%=frmName%>" property="instrOfferingName" />
					</tt:section-title>						
				<html:submit property="op" disabled="true"
					styleClass="btn" accesskey="U" titleKey="title.updateInstructionalOfferingConfig" >
					<bean:message key="button.updateInstructionalOfferingConfig" />
				</html:submit>
				<bean:define id="instrOfferingId">
					<bean:write name="<%=frmName%>" property="instrOfferingId" />				
				</bean:define>
				 
				<html:button property="op" 
					styleClass="btn" accesskey="B" titleKey="title.backToInstrOffrDetail" 
					onclick="document.location.href='instructionalOfferingDetail.do?op=view&io=${instrOfferingId}';">
					<bean:message key="button.backToInstrOffrDetail" />
				</html:button>		
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
		<TD align="left" colspan="2">
			Configuration Limit:<html:text property="instrOffrConfigLimit" maxlength="5" size="5"/>
		</TD>
		</TR>
		<TR>
		<TD align="left" colspan="2">
		<table align="left" border="0" cellspacing="0" cellpadding="1">
			<tr>
			<td valign="top">
			Scheduling Subpart Limits:
			</td>
			<td> &nbsp;&nbsp;&nbsp;</td>
			<td valign="center">
			<table align="left" valign="top" border="0" cellspacing="0" cellpadding="0">
				<logic:iterate name="<%=frmName%>" property="subtotalValues" id="v" indexId="ctr">
			<tr onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='default';" onmouseout="this.style.backgroundColor='transparent';"> 
				<td valign="top" align="right" nowrap>
				<html:hidden property='<%= "subtotalLabels[" + ctr + "]" %>'/>
				<html:hidden property='<%= "subtotalValues[" + ctr + "]" %>'/>
				<b><%=((String)frm.getSubtotalLabels().get(ctr)).trim()%>:</b> &nbsp; 
				</td> 
				<td align="right" nowrap><div id='<%= "subtotal1Values" + ctr %>'><bean:write name="<%=frmName%>" property='<%= "subtotalValues[" + ctr + "]" %>'/></div></td>
			<logic:equal name="<%=frmName%>" property="displayDisplayInstructors" value="true" >
				<TD align="center" nowrap>
				&nbsp; &nbsp; Display Instructors: 
				<logic:equal name="<%=frmName%>" property='<%= "readOnlySubparts[" + ctr + "]" %>' value="false" >
					<html:checkbox name="<%=frmName%>" property='<%= "displayAllClassesInstructorsForSubpart[" + ctr + "]" %>' onclick="<%= \"updateSubpartFlags(this.checked, 'displayInstructors', \"+ctr+\", 'displayAllClassesInstructorsForSubpart');\"%>"/>
				</logic:equal>
				<logic:equal name="<%=frmName%>" property='<%= "readOnlySubparts[" + ctr + "]" %>' value="true" >
					<logic:equal name="<%=frmName%>" property='<%= "displayAllClassesInstructorsForSubpart[" + ctr + "]" %>' value="true" >
						<IMG border='0' title='Display all instructors for this subpart in the schedule book.' alt='true' align='absmiddle' src='images/tick.gif'>
					</logic:equal>
					<html:hidden property='<%= "displayAllClassesInstructorsForSubpart[" + ctr + "]" %>'/>
				</logic:equal>
				</TD>
			</logic:equal>
			<logic:equal name="<%=frmName%>" property="displayDisplayInSchedule" value="true" >	
					<TD align="center" nowrap>
				&nbsp; &nbsp; Display Classes in Schedule: 
				<logic:equal name="<%=frmName%>" property='<%= "readOnlySubparts[" + ctr + "]" %>' value="false" >
					<html:checkbox name="<%=frmName%>" property='<%= "displayAllClassesInSchedBookForSubpart[" + ctr + "]" %>' onclick="<%= \"updateSubpartFlags(this.checked, 'displayInScheduleBooks', \"+ctr+\", 'displayAllClassesInSchedBookForSubpart');\"%>"/>
				</logic:equal>
				<logic:equal name="<%=frmName%>" property='<%= "readOnlySubparts[" + ctr + "]" %>' value="true" >
					<logic:equal name="<%=frmName%>" property='<%= "displayAllClassesInSchedBookForSubpart[" + ctr + "]" %>' value="true" >
						<IMG border='0' title='Display all classes for this subpart in the schedule book.' alt='true' align='absmiddle' src='images/tick.gif'>
					</logic:equal>
					<html:hidden property='<%= "displayAllClassesInSchedBookForSubpart[" + ctr + "]" %>'/>
				</logic:equal>
				</TD>	
			</logic:equal>	
				</tr>
				</logic:iterate>			
			</tr>
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
						<logic:equal name="<%=frmName%>" property="displayOptionForMaxLimit" value="true" >
						<TD align="left" valign="bottom" rowSpan="2" colspan="2"><i><html:checkbox name="<%=frmName%>" property="displayMaxLimit" onclick="doClick('multipleLimits', 0);"/> <small>Allow<br>variable limits</small></i></TD>
						</logic:equal>
						<logic:equal name="<%=frmName%>" property="displayOptionForMaxLimit" value="false" >
						<TD align="center" valign="bottom" rowSpan="2" colspan="2"> &nbsp;</TD>
						</logic:equal>
						<logic:equal name="<%=frmName%>" property="displayExternalId" value="true" >
						<TD rowspan="2">&nbsp;</TD>
						<TD align="center" valign="bottom" rowspan="2"><i>Extrnl&nbsp;Id</i></TD>
						</logic:equal>
						<TD rowspan="2">&nbsp;</TD>
						<TD rowspan="2">&nbsp;</TD>
						<TD rowspan="2">&nbsp;</TD>
						<TD rowspan="2">&nbsp;</TD>
						<TD rowspan="2">&nbsp;</TD>
						<TD rowspan="2">&nbsp;</TD>
						<logic:equal name="<%=frmName%>" property="displayEnrollment" value="true" >
						<TD align="center" valign="bottom" rowSpan="2"><I>Enroll</I></TD>
						<TD rowspan="2">&nbsp;</TD>
						</logic:equal>
						<logic:equal name="<%=frmName%>" property="displayMaxLimit" value="true" >
						<TD align="center" valign="bottom" colSpan="2"><I>Limit</I></TD>
						</logic:equal>
						<logic:equal name="<%=frmName%>" property="displayMaxLimit" value="false" >
						<TD align="center" valign="bottom" colSpan="2" rowspan="2"><I>Limit</I></TD>
						</logic:equal>
						<TD align="center" valign="bottom" rowSpan="2"><I>Room<br>Ratio</I></TD>
						<TD align="center" valign="bottom" rowSpan="2"><I>Nbr<br>Rms</I></TD>
						<TD align="center" valign="bottom" rowSpan="2"><I>Managing Department</I></TD>
						<TD align="center" valign="bottom" rowSpan="2"><I>Date Pattern</I></TD>
						<TD align="center" valign="bottom" rowSpan="1"><logic:equal name="<%=frmName%>" property="displayDisplayInstructors" value="true" ><I>Display&nbsp;<br>Instr</I></logic:equal></TD>
						<TD align="center" valign="bottom" rowSpan="1"><logic:equal name="<%=frmName%>" property="displayDisplayInSchedule" value="true" ><I>Display&nbsp;<br>Class</I></logic:equal></TD>
						<TD align="center" valign="bottom" rowSpan="1" colspan="2"><I>---&nbsp;Timetable&nbsp;---</I></TD>
						<TD rowspan="2">&nbsp;</TD>
						<TD align="center" valign="bottom" rowSpan="2"><I>Instructors</I></TD>
						<TD rowspan="2">&nbsp;</TD>
					</TR>
					<TR>
						<logic:equal name="<%=frmName%>" property="displayMaxLimit" value="true" >
						<TD align="center" valign="bottom"><I>Min</I></TD>
						<TD align="center" valign="bottom"><I>Max</I></TD>
						</logic:equal>			
						<td align="center" valign="bottom"><logic:equal name="<%=frmName%>" property="displayDisplayInstructors" value="true" >(All:<html:checkbox name="<%=frmName%>" property="displayAllClassesInstructors" onclick="resetAllDisplayFlags(this.checked, 'displayInstructors')" />)</logic:equal></td>
						<td align="center" valign="bottom"><logic:equal name="<%=frmName%>" property="displayDisplayInSchedule" value="true" >(All:<html:checkbox name="<%=frmName%>" property="displayAllClassesInSchedBook" onclick="resetAllDisplayFlags(this.checked, 'displayInScheduleBooks')"/>)</logic:equal></td>						
						<TD align="center" valign="bottom" rowSpan="1"><I>Time</I></TD>
						<TD align="center" valign="bottom" rowSpan="1"><I>Room</I></TD>
					</TR>					
					<logic:iterate name="<%=frmName%>" property="classIds" id="c" indexId="ctr">
					<TR onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='default';" onmouseout="this.style.backgroundColor='transparent';">
						<TD nowrap valign="top"><logic:equal name="<%=frmName%>" property='<%= "classHasErrors[" + ctr + "]" %>' value="true" ><IMG src="images/Error16.jpg"></logic:equal><logic:equal name="<%=frmName%>" property='<%= "classHasErrors[" + ctr + "]" %>' value="false" >&nbsp;</logic:equal></TD>
						<TD nowrap valign="top"><html:hidden property='<%= "externalIds[" + ctr + "]" %>'/><html:hidden property='<%= "classIds[" + ctr + "]" %>'/><html:hidden property='<%= "subpartIds[" + ctr + "]" %>'/><html:hidden property='<%= "itypes[" + ctr + "]" %>'/><html:hidden property='<%= "mustHaveChildClasses[" + ctr + "]" %>'/><html:hidden property='<%= "parentClassIds[" + ctr + "]" %>'/><html:hidden property='<%= "readOnlyClasses[" + ctr + "]" %>'/><html:hidden property='<%= "enrollments[" + ctr + "]" %>'/><html:hidden property='<%= "classCanMoveUp[" + ctr + "]" %>'/><html:hidden property='<%= "classCanMoveDown[" + ctr + "]" %>'/><html:hidden property='<%= "subtotalIndexes[" + ctr + "]" %>'/><html:hidden property='<%= "classHasErrors[" + ctr + "]" %>'/><html:hidden property='<%= "classLabels[" + ctr + "]" %>'/><html:hidden property='<%= "classLabelIndents[" + ctr + "]" %>'/><%=frm.getClassLabelIndents().get(ctr.intValue()).toString()%><bean:write name="<%=frmName%>" property='<%= "classLabels[" + ctr + "]" %>'/> &nbsp;</TD>
						<logic:equal name="<%=frmName%>" property="displayExternalId" value="true" >
						<TD>&nbsp;</TD>
						<TD align="left" valign="top"><%= frm.getExternalIds().get(ctr)%></TD>
						</logic:equal>
						
						<TD>&nbsp;</TD>
						<TD align="center" valign="top" nowrap><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" ><logic:equal name="<%=frmName%>" property='<%= "classCanMoveUp[" + ctr + "]" %>' value="true" ><A href="#null" onClick="document.forms[0].elements['hdnOp'].value='moveUp';document.forms[0].elements['moveUpClassId'].value='<%= c.toString() %>';document.forms[0].submit();"><IMG border="0" src="images/arrow_u.gif" title="Move Class Up"></A></logic:equal></logic:equal></TD>
						<TD align="center" valign="top" nowrap><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" ><logic:equal name="<%=frmName%>" property='<%= "classCanMoveDown[" + ctr + "]" %>' value="true" ><A href="#null" onClick="document.forms[0].elements['hdnOp'].value='moveDown';document.forms[0].elements['moveDownClassId'].value='<%= c.toString() %>';document.forms[0].submit();"><IMG border="0" src="images/arrow_d.gif" title="Move Class Down"></A></logic:equal></logic:equal></TD>
						<TD align="center" valign="top" nowrap><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" ><A href="#null" onClick="document.forms[0].elements['hdnOp'].value='delete';document.forms[0].elements['deletedClassId'].value='<%= c.toString() %>';document.forms[0].submit();"><IMG border="0" src="images/Delete16.gif" title="Remove Class from Instructional Offering"></A></logic:equal></TD>
						<TD align="center" valign="top" nowrap><A href="#null" onClick="document.forms[0].elements['hdnOp'].value='add';document.forms[0].elements['addTemplateClassId'].value='<%= c.toString() %>';document.forms[0].submit();"><IMG border="0" src="images/Add16.gif" title="Add Class to Instructional Offering"></A></TD>
						<TD>&nbsp;</TD>
						<logic:equal name="<%=frmName%>" property="displayEnrollment" value="true" ><TD align="right" valign="top" nowrap><bean:write name="<%=frmName%>" property='<%= "enrollments[" + ctr + "]" %>'/></TD><TD>&nbsp;</TD></logic:equal>
						<TD align="left" nowrap valign="top"><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" ><html:hidden property='<%= "origMinLimit[" + ctr + "]" %>' value="<%= (String)frm.getMinClassLimits().get(ctr) %>"/><html:text name="<%=frmName%>" property='<%= "minClassLimits[" + ctr + "]" %>' tabindex="<%=java.lang.Integer.toString(2000 + ctr.intValue())%>" maxlength="5" size="4" onchange="<%= \"updateSubpartTotal(\" + ctr + \");document.getElementsByName('maxClassLimits[\" + ctr + \"]')[0].value=this.value\"%>"/></logic:equal><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" ><html:hidden property='<%= "minClassLimits[" + ctr + "]" %>'/><bean:write name="<%=frmName%>" property='<%= "minClassLimits[" + ctr + "]" %>'/></logic:equal></TD>
						<logic:equal name="<%=frmName%>" property="displayMaxLimit" value="true" >
						<TD align="left" nowrap valign="top"><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" ><html:text name="<%=frmName%>" property='<%= "maxClassLimits[" + ctr + "]" %>' tabindex="<%=java.lang.Integer.toString(4000 + ctr.intValue())%>" maxlength="5" size="4"/></logic:equal><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" ><html:hidden property='<%= "maxClassLimits[" + ctr + "]" %>'/><bean:write name="<%=frmName%>" property='<%= "maxClassLimits[" + ctr + "]" %>'/></logic:equal></TD>
						</logic:equal>
						<logic:equal name="<%=frmName%>" property="displayMaxLimit" value="false" >
						<TD align="left" valign="top" nowrap><html:hidden property='<%= "maxClassLimits[" + ctr + "]" %>'/></TD>
						</logic:equal>
						<TD align="left" valign="top" nowrap><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" ><html:text name="<%=frmName%>" property='<%= "roomRatios[" + ctr + "]" %>' tabindex="<%=java.lang.Integer.toString(6000 + ctr.intValue())%>" maxlength="6" size="3"/></logic:equal><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" ><bean:write name="<%=frmName%>" property='<%= "roomRatios[" + ctr + "]" %>'/><html:hidden property='<%= "roomRatios[" + ctr + "]" %>'/></logic:equal></TD>
						<TD align="left" valign="top" nowrap><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" ><html:text name="<%=frmName%>" property='<%= "numberOfRooms[" + ctr + "]" %>' tabindex="<%=java.lang.Integer.toString(8000 + ctr.intValue())%>" maxlength="5" size="3"/></logic:equal><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" ><bean:write name="<%=frmName%>" property='<%= "numberOfRooms[" + ctr + "]" %>'/><html:hidden property='<%= "numberOfRooms[" + ctr + "]" %>'/></logic:equal></TD>
						<TD align="left" valign="top" nowrap><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" ><html:select style="width:200;" property='<%= "departments[" + ctr + "]" %>' tabindex="<%=java.lang.Integer.toString(10000 + ctr.intValue())%>"><html:option value="-1">Department</html:option><html:options collection='<%=Department.EXTERNAL_DEPT_ATTR_NAME + "list"%>' property="uniqueId" labelProperty="managingDeptLabel" /></html:select></logic:equal><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" ><logic:iterate scope="request" name="<%=Department.EXTERNAL_DEPT_ATTR_NAME%>" id="dept"><logic:equal name="<%=frmName%>" property='<%= "departments[" + ctr + "]" %>' value="<%=((Department)dept).getUniqueId().toString()%>"><bean:write name="dept" property="managingDeptLabel" /></logic:equal></logic:iterate><html:hidden property='<%= "departments[" + ctr + "]" %>'/></logic:equal></TD>
						<TD align="left" valign="top" nowrap><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" ><html:select style="width:100;" property='<%= "datePatterns[" + ctr + "]" %>' tabindex="<%=java.lang.Integer.toString(12000 + ctr.intValue())%>"><html:options collection="<%=DatePattern.DATE_PATTERN_LIST_ATTR%>" property="id" labelProperty="value" /></html:select></logic:equal><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" ><logic:equal name="<%=frmName%>" property='<%= "datePatterns[" + ctr + "]"%>' value="">Default</logic:equal><logic:iterate scope="request" name="<%=DatePattern.DATE_PATTERN_LIST_ATTR%>" id="dp"><logic:notEqual name="<%=frmName%>" property='<%= "datePatterns[" + ctr + "]" %>' value=""><logic:equal name="<%=frmName%>" property='<%= "datePatterns[" + ctr + "]" %>' value="<%=((IdValue)dp).getId().toString()%>"><bean:write name="dp" property="value" /></logic:equal></logic:notEqual></logic:iterate><html:hidden property='<%= "datePatterns[" + ctr + "]" %>'/></logic:equal></TD>
						<TD align="center" valign="top" nowrap><logic:equal name="<%=frmName%>" property="displayDisplayInstructors" value="true" ><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" ><html:checkbox name="<%=frmName%>" property='<%= "displayInstructors[" + ctr + "]" %>' tabindex="<%=java.lang.Integer.toString(14000 + ctr.intValue())%>"/></logic:equal><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" >					<logic:equal name="<%=frmName%>" property='<%= "displayInstructors[" + ctr + "]" %>' value="true" ><IMG border='0' title='Display all instructors for this class in the schedule book.' alt='true' align='absmiddle' src='images/tick.gif'></logic:equal><html:hidden property='<%= "displayInstructors[" + ctr + "]" %>'/></logic:equal></logic:equal><logic:equal name="<%=frmName%>" property="displayDisplayInstructors" value="false" ><html:hidden property='<%= "displayInstructors[" + ctr + "]" %>'/></logic:equal></TD>
						<TD align="center" valign="top" nowrap><logic:equal name="<%=frmName%>" property="displayDisplayInSchedule" value="true" ><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" ><html:checkbox name="<%=frmName%>" property='<%= "displayInScheduleBooks[" + ctr + "]" %>' tabindex="<%=java.lang.Integer.toString(16000 + ctr.intValue())%>"/></logic:equal><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" ><logic:equal name="<%=frmName%>" property='<%= "displayInScheduleBooks[" + ctr + "]" %>' value="true" ><IMG border='0' title='Display this classes in the schedule book.' alt='true' align='absmiddle' src='images/tick.gif'></logic:equal><html:hidden property='<%= "displayInScheduleBooks[" + ctr + "]" %>'/></logic:equal></logic:equal><logic:equal name="<%=frmName%>" property="displayDisplayInSchedule" value="false" ><html:hidden property='<%= "displayInScheduleBooks[" + ctr + "]" %>'/></logic:equal></TD>
						<TD align="left" valign="top" nowrap><%= frm.getTimes().get(ctr)%>&nbsp;&nbsp;<html:hidden property='<%= "times[" + ctr + "]" %>'/></TD>
						<TD align="left" valign="top" nowrap><%= frm.getRooms().get(ctr)%><html:hidden property='<%= "rooms[" + ctr + "]" %>'/></TD>
						<TD>&nbsp;</TD>
						<TD align="left" valign="top" nowrap><%= frm.getInstructors().get(ctr)%><html:hidden property='<%= "instructors[" + ctr + "]" %>'/></TD>
						<TD>&nbsp;</TD>
					</TR>
					</logic:iterate>
				</TABLE>
			</TD>
		</TR>
		<% if (frm.getDisplayDisplayInstructors().booleanValue() || frm.getDisplayDisplayInSchedule().booleanValue()){ %>
		<TR>
		<TD align="left" colspan="2">
			<table align="left" valign="top" border="0" cellspacing="0" cellpadding="0">
			<tr> 
			<td valign="top">
			Scheduling Subpart Limits:
			</td> 
			<td> &nbsp;&nbsp;&nbsp;</td>
			<td valign="center">
			<table align="left" valign="top" border="0" cellspacing="0" cellpadding="0">
				<logic:iterate name="<%=frmName%>" property="subtotalValues" id="v" indexId="ctr">				
				<tr onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='default';" onmouseout="this.style.backgroundColor='transparent';">
				<td valign="top" align="right" nowrap>
				<b><%=((String)frm.getSubtotalLabels().get(ctr)).trim()%>:</b> &nbsp; 
				</td> 
				<td nowrap align="right"><div id='<%= "subtotal2Values" + ctr %>'><bean:write name="<%=frmName%>" property='<%= "subtotalValues[" + ctr + "]" %>'/></div></td>
			<logic:equal name="<%=frmName%>" property="displayDisplayInstructors" value="true" >
				<TD align="center" nowrap>
				&nbsp; &nbsp; Display Instructors: 
				<logic:equal name="<%=frmName%>" property='<%= "readOnlySubparts[" + ctr + "]" %>' value="false" >
					<html:checkbox name="<%=frmName%>" property='<%= "displayAllClassesInstructorsForSubpart[" + ctr + "]" %>' onclick="<%= \"updateSubpartFlags(this.checked, 'displayInstructors', \"+ctr+\", 'displayAllClassesInstructorsForSubpart');\"%>"/>
				</logic:equal>
				<logic:equal name="<%=frmName%>" property='<%= "readOnlySubparts[" + ctr + "]" %>' value="true" >
					<logic:equal name="<%=frmName%>" property='<%= "displayAllClassesInstructorsForSubpart[" + ctr + "]" %>' value="true" >
						<IMG border='0' title='Display all instructors for this subpart in the schedule book.' alt='true' align='absmiddle' src='images/tick.gif'>
					</logic:equal>
					<html:hidden property='<%= "displayAllClassesInstructorsForSubpart[" + ctr + "]" %>'/>
				</logic:equal>
				</TD>
			</logic:equal>
			<logic:equal name="<%=frmName%>" property="displayDisplayInSchedule" value="true" >	
				<TD align="center" nowrap>
				&nbsp; &nbsp; Display Classes in Schedule: 
				<logic:equal name="<%=frmName%>" property='<%= "readOnlySubparts[" + ctr + "]" %>' value="false" >
					<html:checkbox name="<%=frmName%>" property='<%= "displayAllClassesInSchedBookForSubpart[" + ctr + "]" %>' onclick="<%= \"updateSubpartFlags(this.checked, 'displayInScheduleBooks', \"+ctr+\", 'displayAllClassesInSchedBookForSubpart');\"%>"/>
				</logic:equal>
				<logic:equal name="<%=frmName%>" property='<%= "readOnlySubparts[" + ctr + "]" %>' value="true" >
					<logic:equal name="<%=frmName%>" property='<%= "displayAllClassesInSchedBookForSubpart[" + ctr + "]" %>' value="true" >
						<IMG border='0' title='Display all classes for this subpart in the schedule book.' alt='true' align='absmiddle' src='images/tick.gif'>
					</logic:equal>
					<html:hidden property='<%= "displayAllClassesInSchedBookForSubpart[" + ctr + "]" %>'/>
				</logic:equal>
				</TD>
			</logic:equal>	
				</tr>
				</logic:iterate>
			</table>
			</td>
			</tr>
			</table></td>
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
					styleClass="btn" accesskey="U" titleKey="title.updateInstructionalOfferingConfig">
					<bean:message key="button.updateInstructionalOfferingConfig" />
				</html:submit>
			
				<bean:define id="instrOfferingId">
					<bean:write name="<%=frmName%>" property="instrOfferingId" />				
				</bean:define>
				 
				<html:button property="op" 
					styleClass="btn" accesskey="B" titleKey="title.backToInstrOffrDetail" 
					onclick="document.location.href='instructionalOfferingDetail.do?op=view&io=${instrOfferingId}';">
					<bean:message key="button.backToInstrOffrDetail" />
				</html:button>
					
			</TD>
		</TR>

	</TABLE>
</html:form>
	<script language="javascript">displayElement('loading', false);</script>

