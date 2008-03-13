<%-- 
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org
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
<%@ page import="org.unitime.commons.web.Web" %>
<%@ page import="org.unitime.timetable.model.Roles" %>
<%@ page import="org.unitime.timetable.model.Room" %>
<%@ page import="org.unitime.timetable.model.RoomFeature" %>
<%@ page import="org.unitime.timetable.form.RoomGroupEditForm" %>
<%@ page import="org.unitime.timetable.model.Location" %>
<%@page import="org.unitime.timetable.model.Exam"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<%
	boolean flag = true;
	if(Web.hasRole(request.getSession(), new String[] { Roles.ADMIN_ROLE})) 
		flag = false;
		
	// Get Form 
	String frmName = "roomGroupEditForm";	
	RoomGroupEditForm frm = (RoomGroupEditForm) request.getAttribute(frmName);
%>	

<tiles:importAttribute />
<html:form action="/roomGroupEdit" focus="name">
	<html:hidden property="id"/>

	<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan='2'>
				<tt:section-header>
					<logic:empty name="roomGroupEditForm" property="id">
						<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="A" 
								title="Add New Room Group (Alt+A)">
							<bean:message key="button.addNew" />
						</html:submit>
					</logic:empty>
					<logic:notEmpty name="roomGroupEditForm" property="id">
						<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="U" 
							title="Update Room Group (Alt+U)">
							<bean:message key="button.update" />
						</html:submit>
						&nbsp;
						<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="D" 
								title="Deletes Room Group (Alt+D)">
							<bean:message key="button.delete" />
						</html:submit>
					</logic:notEmpty>
					&nbsp;
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="B" 
						title="Back to Room Groups (Alt+B)">
						<bean:message key="button.returnToRoomGroupList"/>
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
			<TD>Name: <font class="reqField">*</font></TD>
			<TD>
				<html:text property="name" maxlength="20" size="20" />
			<TD>
		</TR>
			
		<TR>
			<TD>Abbreviation: <font class="reqField">*</font></TD>
			<TD>
				<html:text property="abbv" maxlength="20" size="20" />
			<TD>
		</TR>

		<TR>
			<TD>Global:</TD>
			<TD>
				<html:checkbox property="global" disabled="true"/>
			</TD>
		</TR>
			
		<% if (!flag) {%>		
		<TR>
			<TD>Default:</TD>
			<TD>
				<html:checkbox property="deft" disabled="<%=flag || !frm.isGlobal()%>"/>
			</TD>
		</TR>
		<%}%>
			
		<TR>
			<TD>Description:</TD>
			<TD>
				<html:textarea property="desc" rows="4" cols="50" />
			<TD>
		</TR>
		
		<logic:equal name="<%=frmName%>" property="global" value="false">
			<TR>
				<TD>Department:</TD>
				<TD><%=frm.getDeptName(frm.getDeptCode(), request)%><html:hidden property="deptCode" />
				</TD>
			</TR>
		</logic:equal>
		
		<logic:notEmpty name="<%=frmName%>" property="assignedRooms">
			<TR>
				<TD colspan='2'>&nbsp;</TD>
			</TR>
			<TR>
				<TD colspan='2'>
					<tt:section-title>Currently Assigned Rooms</tt:section-title>
				</TD>
			</TR>
			<TR>
				<TD colspan='2'>
					<TABLE cellpadding="5" cellspacing="0">
						<TR align="center" valign="top" >
						<TD> &nbsp;</TD>
						<logic:iterate name="<%=frmName%>" property="heading" id="heading">
						<%String h1 = (String) heading; %>	
							<TD align="left"><I><%=h1%></I></TD>
						</logic:iterate>
						</TR>
							
						<logic:iterate name="<%=frmName%>" property="assignedRooms" id="room" indexId="ctr">
						<% Location r1 = (Location) room;%>
						
						<TR align="center" onmouseover="this.style.backgroundColor='rgb(223,231,242)';" onmouseout="this.style.backgroundColor='transparent';">
							<TD>
								<html:multibox property="assignedSelected">
									<%=r1.getUniqueId()%>
								</html:multibox>	
							</TD>
				
							<TD align="left">
								<%=r1.getLabel()%>
							</TD>
							
							<TD align="left">
								<% if (r1 instanceof Room) { %>
									<%=((Room)r1).getScheduledRoomType()%>
								<% } else { %>
									Non University
								<% } %>
							</TD>
							
							<TD align="center">
								<%=r1.getCapacity()%>
							</TD>
							
							<% if (r1.isExamEnabled(Exam.sExamTypeEvening) || r1.isExamEnabled(Exam.sExamTypeFinal)) { %>
								<TD align="center"><%=r1.getExamCapacity()%></TD>
							<% } else { %>
								<TD></TD>
							<% } %>
							
							<logic:iterate name="<%=frmName%>" property="roomFeatures" id="roomFeature">
							<% 
								RoomFeature rf1 = (RoomFeature) roomFeature; 
								frm.setFeature(r1.hasFeature(rf1));
							%>
							<TD align="center">
								<% if (r1.hasFeature(rf1)) { %>
									<IMG border='0' align='absmiddle' src='images/tick.gif' title='<%=rf1.getLabel()%>'/>
								<% } %>
								<!-- 
								<html:checkbox disabled="true" property="feature" />
								-->
							</TD>
							</logic:iterate>
						
						</TR>
						
						</logic:iterate>
					</TABLE>
				</TD>
			</TR>
		</logic:notEmpty>

		<logic:notEmpty name="<%=frmName%>" property="notAssignedRooms">
			<TR>
				<TD colspan='2'>&nbsp;</TD>
			</TR>
			<TR>
				<TD colspan='2'>
					<tt:section-title>Currently Not Assigned Rooms</tt:section-title>
				</TD>
			</TR>
			<TR>
				<TD colspan='2'>
					<TABLE cellpadding="5" cellspacing="0">
						<TR align="center" valign="top">
						<TD> &nbsp;</TD>
						<logic:iterate name="<%=frmName%>" property="heading" id="heading">
						<%String h2 = (String) heading; %>	
							<TD align="left"><I><%=h2%></I></TD>
						</logic:iterate>
						</TR>
							
						<logic:iterate name="<%=frmName%>" property="notAssignedRooms" id="room" indexId="ctr">
						<% Location r2 = (Location) room;%>
						
						<TR align="center" onmouseover="this.style.backgroundColor='rgb(223,231,242)';" onmouseout="this.style.backgroundColor='transparent';">
				
							<TD>
								<html:multibox property="notAssignedSelected">
									<%=r2.getUniqueId()%>
								</html:multibox>	
							</TD>
				
							<TD align="left">
								<%=r2.getLabel()%>
							</TD>
							
							<TD align="left">
								<% if (r2 instanceof Room) { %>
									<%=((Room)r2).getScheduledRoomType()%>
								<% } else { %>
									Non University
								<% } %>
							</TD>

							<TD align="center">
								<%=r2.getCapacity()%>
							</TD>
							
							<% if (r2.isExamEnabled(Exam.sExamTypeEvening) || r2.isExamEnabled(Exam.sExamTypeFinal)) { %>
								<TD align="center"><%=r2.getExamCapacity()%></TD>
							<% } else { %>
								<TD></TD>
							<% } %>

							<logic:iterate name="<%=frmName%>" property="roomFeatures" id="roomFeature">
							<% 
								RoomFeature rf2 = (RoomFeature) roomFeature; 
								frm.setFeature(r2.hasFeature(rf2));
							%>
							<TD align="center">
								<% if (r2.hasFeature(rf2)) { %>
									<IMG border='0' align='absmiddle' src='images/tick.gif' title='<%=rf2.getLabel()%>'/>
								<% } %>
								<!-- 
								<html:checkbox disabled="true" property="feature" />
								-->
							</TD>
							</logic:iterate>
						
						</TR>
						
						</logic:iterate>
					</TABLE>
				</TD>
			</TR>
		</logic:notEmpty>
		
		<TR>
			<TD valign="middle" colspan="2">
				<tt:section-title/>
			</TD>
		<TR>
			
		<TR>
			<TD colspan='2' align='right'>
				<logic:empty name="roomGroupEditForm" property="id">
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="A" 
							title="Add New Room Group (Alt+A)">
						<bean:message key="button.addNew" />
					</html:submit>
				</logic:empty>
				<logic:notEmpty name="roomGroupEditForm" property="id">
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="U" 
						title="Update Room Group (Alt+U)">
						<bean:message key="button.update" />
					</html:submit>
					&nbsp;
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="D" 
							title="Deletes Room Group (Alt+D)">
						<bean:message key="button.delete" />
					</html:submit>
				</logic:notEmpty>
				&nbsp;
				<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="B" 
					title="Back to Room Groups (Alt+B)">
					<bean:message key="button.returnToRoomGroupList"/>
				</html:submit>
			</TD>
		</TR>
	</TABLE>

</html:form>