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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp"%>
<%@ page import="org.unitime.timetable.form.InstructorListUpdateForm" %>
<%@ page import="org.unitime.timetable.model.DepartmentalInstructor" %>
<%@ page import="org.unitime.timetable.model.PositionType" %>
<%@ page import="org.unitime.timetable.model.Staff" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="java.text.NumberFormat" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>

<%
	// Get Form 
	String frmName = "instructorListUpdateForm";	
	InstructorListUpdateForm frm = (InstructorListUpdateForm) request.getAttribute(frmName);	
	NumberFormat percentFormatter = NumberFormat.getPercentInstance();	
%>	
<script type="text/javascript" language="javascript">
<!--
	function doSelectAll(elem, styleId, checked) {
		var i = 0;
		
		for (;;) {
			var idName = styleId + '_' + i;
			var idVal = document.getElementById(idName);
			if (idVal!=null && idVal.value!=null) {
				if (!idVal.disabled)
					idVal.checked = checked;
			}
			else {
				break;
			}
			++i;
		}
	}
//-->
</script>

<html:form action="instructorListUpdate">

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>				
					<tt:section-title>
						<bean:write name='<%=frmName%>' property='deptName'/>
					</tt:section-title>
					<html:submit property="op" 
						styleClass="btn" accesskey="M" titleKey="title.updateInstructor" >
						<bean:message key="button.update" />
					</html:submit> 
					<html:submit property="op" 
						styleClass="btn" accesskey="B" titleKey="title.returnToDetail">
						<bean:message key="button.returnToDetail" />
					</html:submit>
				</tt:section-header>
			</TD>
		</TR>
		
		<TR>
			<TD colspan='2'>
				<script language="JavaScript" type="text/javascript">blToggleHeader('Filter','dispFilter');blStart('dispFilter');</script>
				<TABLE border="0" cellspacing="0" cellpadding="3">
					<TR>
						<TD valign="top">
							<B>Display:</B> &nbsp;
						</TD>
						<TD>
							 &nbsp;<html:radio property="displayListType" value="assigned" /> Department Assigned Instructors Only&nbsp; <BR>
							 &nbsp;<html:radio property="displayListType" value="available" /> Available Instructors Only&nbsp; <BR> 
							 &nbsp;<html:radio property="displayListType" value="both" /> Both &nbsp; <BR>
						</TD>
					</TR>
					<TR>
						<TD valign="top">
							<B>Ignore Positions:</B> * &nbsp;
						</TD>
						<TD>
							<TABLE border="0" align="left" cellspacing="1" cellpadding="2">
							<% int ctr22 = 0; %>
							 <logic:iterate indexId="ctr" id="filterPosType" name="<%=PositionType.POSTYPE_ATTR_NAME%>" type="org.unitime.timetable.model.PositionType" >
							 	<% 	ctr22 = ctr.intValue();
							 		if ((ctr22+1)%3==1) out.println("<TR>"); %>
							 	<TD>
							 		&nbsp;<html:multibox property="displayPosType"><%=((PositionType) filterPosType).getReference()%></html:multibox> <%=((PositionType) filterPosType).getLabel()%>&nbsp;
							 	</TD>
							 	<% if ((ctr22+1)%3==0) out.println("</TR>"); %>
							 </logic:iterate>
							 <% if ((ctr22+1)%3!=0) out.println("</TR>"); %>
							 </TABLE>
						</TD>
					</TR>
					<TR>
						<TD colspan='2' class="WelcomeRowHead">
							&nbsp;<font class="normal">* applies only to Instructors not in department list</font>							
						</TD>
					</TR>
					<TR>
						<TD colspan='2' align="right">
							<html:submit property="op" 
								styleClass="btn" accesskey="A" titleKey="title.applyFilter" >
								<bean:message key="button.applyFilter" />
							</html:submit> 
						</TD>
					</TR>

					<TR>
						<TD colspan='2'>&nbsp;</TD>
					</TR>
				</TABLE>
				<script language="JavaScript" type="text/javascript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
				<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
					<TR>
						<TD colspan='2' align='right'>
							<br>
						</TD>
					</TR>
				</TABLE>
				<script language="JavaScript" type="text/javascript">blEnd('dispFilter');</script>
			</TD>
		</TR>

<!-- Department Instructors -->
		<% if (frm.getDisplayListType()==null 
				|| ( frm.getDisplayListType()!=null 
						&& ( frm.getDisplayListType().equals("both") 
								|| frm.getDisplayListType().equals("assigned")
							)
					) ) { %>

		
		<TR>
			<TD colspan='2'>
				<tt:section-title><A name="DeptInstr">Department Instructors</A></tt:section-title>
			</TD>
		</TR>
		
		<logic:empty name="<%=frmName%>" property="assignedInstr">
			<TR>
				<TD colspan='2'>
				There are no instructors assigned to this department
				</TD>
			</TR>
		</logic:empty>
				
		<logic:notEmpty name="<%=frmName%>" property="assignedInstr">
			<TR>
				<TD colspan='2'>
					<TABLE cellpadding="2" cellspacing="3" border="0">
				
						<logic:notEmpty name="<%=frmName%>" property="assignedInstr">
						<TR align="center">
							<TD> &nbsp;</TD>
							<TD align="left"><I>External Id</I></TD>
							<TD align="left"><I>Name</I></TD>
						</TR>
							
						<% 
							String prevPosType = "";
							String currPosType = "";
							String posId = "";
							String posId2 = "";
							int ctr2 = 0;
						%>

						<logic:iterate name="<%=frmName%>" property="assignedInstr" id="instr" indexId="ctr">
						<%	
							DepartmentalInstructor inst = (DepartmentalInstructor) instr;
							PositionType posType = inst.getPositionType();
							boolean canDelete = inst.getClasses().isEmpty() && inst.getExams().isEmpty();
							
							if (posType == null) {
								currPosType = "Position Type Not Set";
								posId = "assigned_notSet";
							}
							else {
								currPosType = posType.getLabel().trim();
								posId = "assigned_" + posType.getUniqueId().toString();
							}
								
							
							
							if (!prevPosType.equalsIgnoreCase(currPosType)) {
								prevPosType = currPosType;
								ctr2 = 0;
						%>
						<TR>
							<TD colspan="4" align="left">
								&nbsp;<br>
								<B><U><%=currPosType%></U></B>								
							</TD>
							<TD colspan="2" align="right">
								&nbsp;<br>
								<IMG src="images/check_all.gif" alt="Select all <%=currPosType%>" title="Select All <%=currPosType%>" align="middle" onclick="doSelectAll(this, '<%=posId%>', true);" onmouseover="this.style.cursor='hand';this.style.cursor='pointer';">
								<IMG src="images/clear_all.gif" alt="Clear all <%=currPosType%>" title="Clear All <%=currPosType%>" align="middle" onclick="doSelectAll(this, '<%=posId%>', false);" onmouseover="this.style.cursor='hand';this.style.cursor='pointer';">
							</TD>
						</TR>							
						<%  } 
						
							posId2 = posId + "_" + (ctr2++);
						%>
						
						<TR align="center">
							<TD class="BottomBorderGray">
								<html:multibox property="assignedSelected" styleId="<%=posId2%>" disabled="<%=!canDelete%>">
									<%=inst.getUniqueId()%>
								</html:multibox>
							</TD>
							<TD align="left" class="BottomBorderGray">
								<% if (inst.getExternalUniqueId() != null) {
										out.println(inst.getExternalUniqueId());
									}		
								%>&nbsp;
							</TD>
							<TD align="left" class="BottomBorderGray">
								<%= Constants.toInitialCase(inst.nameLastNameFirst(), "'-".toCharArray()) %>
							</TD>
						</TR>
						</logic:iterate>
								
						</logic:notEmpty>
					</TABLE>
				</TD>
			</TR>
			<TR>
				<TD colspan='2'>&nbsp;</TD>
			</TR>
		</logic:notEmpty>
		<% } %>

<!-- Instructors not in the Department List -->
		<% if (frm.getDisplayListType()==null 
				|| ( frm.getDisplayListType()!=null 
						&& ( frm.getDisplayListType().equals("both") 
								|| frm.getDisplayListType().equals("available")
							)
					) ) { %>

		<TR>
			<TD colspan='2'>
				<tt:section-title><A name="NonDeptInstr">Instructors not in the Department List</A></tt:section-title>
			</TD>
		</TR>

		<logic:empty name="<%=frmName%>" property="availableInstr">
			<TR>
				<TD colspan='2'>
				There are no additional instructors that can be assigned to this department
				</TD>
			</TR>
		</logic:empty>
				
		<logic:notEmpty name="<%=frmName%>" property="availableInstr">
			<TR>
				<TD colspan='2'>
					<TABLE cellpadding="2" cellspacing="3" border="0">
				
						<logic:notEmpty name="<%=frmName%>" property="availableInstr">
						<TR align="center">
							<TD> &nbsp;</TD>
							<TD align="left"><I>External Id</I></TD>
							<TD align="left"><I>Name</I></TD>
						</TR>
							
						<% 
							String prevPosType2 = "";
							String currPosType2 = "";
							String prevPosRef2 = "";
							String currPosRef2 = "";
							String posIdA = "";
							String posIdA2 = "";
							int ctrA2 = 0;
						%>

						<logic:iterate name="<%=frmName%>" property="availableInstr" id="staff" indexId="ctr">
						<%	
							Staff s = (Staff) staff;
							PositionType posType2 = null;
							if (s.getPositionType()!=null) 
								posType2 = s.getPositionType();
							
							if (posType2 == null) {
								currPosType2 = "Position Type Not Set";
								currPosRef2 = "NOT_SET";
								posIdA = "available_notSet";
							}
							else {
								currPosType2 = posType2.getLabel().trim();
								currPosRef2 = posType2.getReference().trim();
								posIdA = "available_" + posType2.getUniqueId().toString();
							}
							
							String[] hpt = frm.getDisplayPosType();
							boolean found = false;
							for (int ctrP=0; ctrP<hpt.length; ctrP++) {
								if (currPosRef2.equalsIgnoreCase(hpt[ctrP])) {
									found = true;
									break;
								}
							}
							if (!found) {
								
								if (!prevPosType2.equalsIgnoreCase(currPosType2)) {
									prevPosType2 = currPosType2;
									prevPosRef2 = currPosRef2;
									ctrA2=0;
						%>
						<TR>
							<TD colspan="4" align="left">
								&nbsp;<br>
								<B><U><%=currPosType2%></U></B>								
							</TD>
							<TD colspan="2" align="right">
								&nbsp;<br>
								<IMG src="images/check_all.gif" alt="Select all <%=currPosType2%>" title="Select All <%=currPosType2%>" align="middle" onclick="doSelectAll(this, '<%=posIdA%>', true);" onmouseover="this.style.cursor='hand';this.style.cursor='pointer';">
								<IMG src="images/clear_all.gif" alt="Clear all <%=currPosType2%>" title="Clear All <%=currPosType2%>" align="middle" onclick="doSelectAll(this, '<%=posIdA%>', false);" onmouseover="this.style.cursor='hand';this.style.cursor='pointer';">
							</TD>
						</TR>							
						<%  	} 
						
								posIdA2 = posIdA + "_" + (ctrA2++);
						%>
						
						<TR align="center">
							<TD class="BottomBorderGray">
								<html:multibox property="availableSelected" styleId="<%=posIdA2%>">
									<%=s.getUniqueId()%>
								</html:multibox>	
							</TD>
							<TD align="left" class="BottomBorderGray">
								<% if (s.getExternalUniqueId() != null) {
										out.println(s.getExternalUniqueId());
									}		
								%>&nbsp;
							</TD>
							<TD align="left" class="BottomBorderGray">
								<%= Constants.toInitialCase(s.nameLastNameFirst(), "'-".toCharArray()) %>
							</TD>							
						</TR>
						<% } %>
						</logic:iterate>
						
						</logic:notEmpty>
					
					</TABLE>
				</TD>
			</TR>
		</logic:notEmpty>
		<% } %>

	
		<TR>
			<TD valign="middle" colspan='2' class='WelcomeRowHead'>
				&nbsp;
			</TD>
		</TR>
		
		<TR>
			<TD valign="middle" colspan='2' align="right">
				<html:submit property="op" 
					styleClass="btn" accesskey="M" titleKey="title.updateInstructor" >
					<bean:message key="button.update" />
				</html:submit> 
				<html:submit property="op" 
					styleClass="btn" accesskey="B" titleKey="title.returnToDetail">
					<bean:message key="button.returnToDetail" />
				</html:submit>
			</TD>
		</TR>
		
	</TABLE>
</html:form>
