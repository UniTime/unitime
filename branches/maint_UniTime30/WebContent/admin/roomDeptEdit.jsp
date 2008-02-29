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
<%@ page import="org.unitime.timetable.form.RoomDeptEditForm" %>
<%@ page import="org.unitime.timetable.model.Room" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<%
	// Get Form 
	String frmName = "roomDeptEditForm";	
	RoomDeptEditForm frm = (RoomDeptEditForm) request.getAttribute(frmName);
%>	

<tiles:importAttribute />
<html:form action="/roomDeptEdit" focus="name">
	<html:hidden property="id"/>
	
	<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan='2'>
				<tt:section-header>
					<tt:section-title><A name="top"><bean:write name="<%=frmName%>" property="deptCode"/> <bean:write name="<%=frmName%>" property="deptAbbv"/></A>
					</tt:section-title>
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="U" 
							title="Update Room Department (Alt+U)">
						<bean:message key="button.update" />
					</html:submit>
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="B" 
						title="Back to Room Department List (Alt+B)">
						<bean:message key="button.returnToRoomDeptList"/>
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
			<TD valign="middle" colspan="2" align="right">
				<A href="#notAssigned">View Rooms - Currently Not Assigned</A>
			</TD>
		</TR>

		<TR>
			<TD colspan='2'>
				&nbsp;<font  class="WelcomeRowHead"><A name="assigned">Rooms - Currently Assigned</A></font>
			</TD>
		</TR>
		<TR>
			<TD colspan='2'>
				<TABLE cellpadding="5" cellspacing="0">
					<% String oldType=""; %>
					<logic:iterate name="<%=frmName%>" property="assignedRooms" id="room" indexId="ctr">
						<%
							String type = Room.getSchedulingRoomTypeName(((Room) room).getSchedulingRoomTypeInteger());
							if (!type.equals(oldType)) {
						%>
						<TR>
							<TD colspan="3">&nbsp;</TD>
						</TR>	
						<TR>
							<TD colspan="3">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<font class="WelcomeRowHead"><%=type%></font></TD>
						</TR>	
						<%	
								oldType = type;
							}
						%>
						<TR align="center" onmouseover="this.style.backgroundColor='rgb(223,231,242)';" onmouseout="this.style.backgroundColor='transparent';">
							<TD>
								&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
								<html:multibox property="assignedSelected">
								<bean:write name="room" property="uniqueId"/>
								</html:multibox>	
							</TD>
				
							<TD align="left">
								<bean:write name="room" property="label"/>
							</TD>

							<TD align="right">
								<bean:write name="room" property="capacity"/>
							</TD>
						</TR>
					</logic:iterate>
				</TABLE>
			</TD>
		</TR>
		
		<TR>
			<TD valign="middle" colspan="2" align="right">
				<A href="#top">Top</A>
			</TD>
		</TR>

		<TR>
			<TD colspan='2'>
				&nbsp;<font class="WelcomeRowHead"><A name="notAssigned">Rooms - Currently Not Assigned</A></font>
			</TD>
		</TR>
		<TR>
			<TD colspan='2'>
				<TABLE cellpadding="5" cellspacing="0">
					<% oldType=""; %>
					<logic:iterate name="<%=frmName%>" property="notAssignedRooms" id="room" indexId="ctr">
						<%
							String type = Room.getSchedulingRoomTypeName(((Room) room).getSchedulingRoomTypeInteger());
							if (!type.equals(oldType)) {
						%>
						<TR>
							<TD colspan="3">&nbsp;</TD>
						</TR>	
						<TR>
							<TD colspan="3">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<font class="WelcomeRowHead"><%=type%></font></TD>
						</TR>	
						<%	
								oldType = type;
							}
						%>
						<TR align="center" onmouseover="this.style.backgroundColor='rgb(223,231,242)';" onmouseout="this.style.backgroundColor='transparent';">
							<TD>
								&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
								<html:multibox property="notAssignedSelected">
								<bean:write name="room" property="uniqueId"/>
								</html:multibox>	
							</TD>
				
							<TD align="left">
								<bean:write name="room" property="label"/>
							</TD>

							<TD align="right">
								<bean:write name="room" property="capacity"/>
							</TD>
						</TR>
					</logic:iterate>
				</TABLE>
			</TD>
		</TR>

		<TR>
			<TD valign="middle" colspan="2" align="right">
				<A href="#top">Top</A>
			</TD>
		<TR>
			
		<TR>
			<TD valign="middle" colspan="2" align="right" class="WelcomeRowHead">
				&nbsp;
			</TD>
		<TR>
			
		<TR>
			<TD colspan='2' align='right'>
				<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="U" 
						title="Update Room Department (Alt+U)">
					<bean:message key="button.update" />
				</html:submit>
				<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="B" 
					title="Back to Room Department List (Alt+B)">
					<bean:message key="button.returnToRoomDeptList"/>
				</html:submit>
			</TD>
		</TR>
	</TABLE>
</html:form>