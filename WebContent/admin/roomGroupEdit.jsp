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
<%@ page import="org.unitime.timetable.model.Room" %>
<%@ page import="org.unitime.timetable.model.RoomFeature" %>
<%@ page import="org.unitime.timetable.form.RoomGroupEditForm" %>
<%@ page import="org.unitime.timetable.model.Location" %>
<%@page import="org.unitime.timetable.model.Exam"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<%
	// Get Form 
	String frmName = "roomGroupEditForm";	
	RoomGroupEditForm frm = (RoomGroupEditForm) request.getAttribute(frmName);
%>	

<tiles:importAttribute />
<html:form action="/roomGroupEdit" focus="name">
	<html:hidden property="id"/>
	<html:hidden property="sessionId"/>
	<html:hidden property="deptCode" />

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
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
						<logic:equal name="roomGroupEditForm" property="global" value="true">
							<sec:authorize access="hasPermission(#roomGroupEditForm.id, 'RoomGroup', 'GlobalRoomGroupDelete')">
								&nbsp;
								<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="D" 
										title="Deletes Room Group (Alt+D)">
									<bean:message key="button.delete" />
								</html:submit>
							</sec:authorize>
						</logic:equal>
						<logic:notEqual name="roomGroupEditForm" property="global" value="true">
							<sec:authorize access="hasPermission(#roomGroupEditForm.id, 'RoomGroup', 'DepartmenalRoomGroupDelete')">
								&nbsp;
								<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="D" 
										title="Deletes Room Group (Alt+D)">
									<bean:message key="button.delete" />
								</html:submit>
							</sec:authorize>
						</logic:notEqual>
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
				<html:text property="name" maxlength="60" size="60" />
			</TD>
		</TR>
			
		<TR>
			<TD>Abbreviation: <font class="reqField">*</font></TD>
			<TD>
				<html:text property="abbv" maxlength="60" size="60" />
			</TD>
		</TR>

		<TR>
			<TD>Global:</TD>
			<TD>
				<html:checkbox property="global" disabled="true"/>
				<html:hidden property="global"/>
			</TD>
		</TR>
		
		<logic:equal name="<%=frmName%>" property="global" value="true">
			<sec:authorize access="hasPermission(#roomGroupEditForm.id, 'RoomGroup', 'GlobalRoomGroupEditSetDefault')">
				<TR>
					<TD>Default:</TD>
					<TD>
						<html:checkbox property="deft"/>
					</TD>
				</TR>
			</sec:authorize>
			<sec:authorize access="!hasPermission(#roomGroupEditForm.id, 'RoomGroup', 'GlobalRoomGroupEditSetDefault')">
				<TR>
					<TD>Default:</TD>
					<TD>
						<html:checkbox property="deft" disabled="true"/>
						<html:hidden property="deft"/>
					</TD>
				</TR>
			</sec:authorize>
		</logic:equal>
			
		<TR>
			<TD>Description:</TD>
			<TD>
				<html:textarea property="desc" rows="4" cols="50" />
			</TD>
		</TR>
		
		<logic:equal name="<%=frmName%>" property="global" value="false">
			<TR>
				<TD>Department:</TD>
				<TD><bean:write name="<%=frmName%>" property="deptName" /></TD>
			</TR>
		</logic:equal>
		<logic:equal name="<%=frmName%>" property="global" value="true">
			<logic:notEmpty name="<%=frmName%>" property="deptName">
				<TR>
					<TD>Rooms:</TD>
					<TD><bean:write name="<%=frmName%>" property="deptName" /></TD>
				</TR>
			</logic:notEmpty>
		</logic:equal>
		</table>
		
		<table width="100%" border="0" cellspacing="0" cellpadding="3">
		<logic:notEmpty name="<%=frmName%>" property="assignedRooms">
			<tr>
				<td colspan='6'>&nbsp;</td>
			</tr>
			<tr>
				<td colspan='6'>
					<tt:section-title>Currently Assigned Rooms</tt:section-title>
				</td>
			</tr>
			<tr valign="top">
				<td class="WebTableHeader">&nbsp;</td>
				<td class="WebTableHeader">Room</td>
				<td class="WebTableHeader">Type</td>
				<td class="WebTableHeader">Capacity</td>
				<td class="WebTableHeader">Exam<br>Capacity</td>
				<td class="WebTableHeader">Room Features</td>
			</tr>
			<logic:iterate name="<%=frmName%>" property="assignedRooms" id="room" indexId="ctr" type="org.unitime.timetable.model.Location">
				<tr valign="top" onmouseover="this.style.backgroundColor='rgb(223,231,242)';" onmouseout="this.style.backgroundColor='transparent';">
					<td nowrap>
						<html:multibox property="assignedSelected">
							<bean:write name="room" property="uniqueId"/>
						</html:multibox>
					</td>
					<td align="left" nowrap>
						<bean:write name="room" property="label"/>
					</td>
					<td align="left" nowrap>
						<bean:write name="room" property="roomTypeLabel"/>
					</td>
					<td align="center" nowrap>
						<bean:write name="room" property="capacity"/>
					</td>
					<td align="center" nowrap>
						<logic:notEmpty name="room" property="examTypes">
							<bean:write name="room" property="examCapacity"/>
						</logic:notEmpty>
						<logic:empty name="room" property="examTypes">
							<i>N/A</i>
						</logic:empty>
					</td>
					
					<td align="left">
						<bean:write name="<%=frmName%>" property='<%="features(" + room.getUniqueId() + ")"%>' filter="false"/>
					</td>
				</tr>
			</logic:iterate>
		</logic:notEmpty>

		<logic:notEmpty name="<%=frmName%>" property="notAssignedRooms">
			<tr>
				<td colspan='6'>&nbsp;</td>
			</tr>
			<tr>
				<td colspan='6'>
					<tt:section-title>Currently Not Assigned Rooms</tt:section-title>
				</td>
			</tr>
			<tr valign="top">
				<td class="WebTableHeader">&nbsp;</td>
				<td class="WebTableHeader">Room</td>
				<td class="WebTableHeader">Type</td>
				<td class="WebTableHeader">Capacity</td>
				<td class="WebTableHeader">Exam<br>Capacity</td>
				<td class="WebTableHeader">Room Features</td>
			</tr>
			<logic:iterate name="<%=frmName%>" property="notAssignedRooms" id="room" indexId="ctr" type="org.unitime.timetable.model.Location">
				<tr valign="top" onmouseover="this.style.backgroundColor='rgb(223,231,242)';" onmouseout="this.style.backgroundColor='transparent';">
					<td nowrap>
						<html:multibox property="notAssignedSelected">
							<bean:write name="room" property="uniqueId"/>
						</html:multibox>
					</td>
					<td align="left" nowrap>
						<bean:write name="room" property="label"/>
					</td>
					<td align="left" nowrap>
						<bean:write name="room" property="roomTypeLabel"/>
					</td>
					<td align="center" nowrap>
						<bean:write name="room" property="capacity"/>
					</td>
					<td align="center" nowrap>
						<logic:notEmpty name="room" property="examTypes">
							<bean:write name="room" property="examCapacity"/>
						</logic:notEmpty>
						<logic:empty name="room" property="examTypes">
							<i>N/A</i>
						</logic:empty>
					</td>
					<td align="left">
						<bean:write name="<%=frmName%>" property='<%="features(" + room.getUniqueId() + ")"%>' filter="false"/>
					</td>
				</tr>
			</logic:iterate>
		</logic:notEmpty>
		</table>
		
		<table width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan="2">
				<tt:section-title/>
			</TD>
		</TR>
			
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
					<logic:equal name="roomGroupEditForm" property="global" value="true">
						<sec:authorize access="hasPermission(#roomGroupEditForm.id, 'RoomGroup', 'GlobalRoomGroupDelete')">
							&nbsp;
							<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="D" 
									title="Deletes Room Group (Alt+D)">
								<bean:message key="button.delete" />
							</html:submit>
						</sec:authorize>
					</logic:equal>
					<logic:notEqual name="roomGroupEditForm" property="global" value="true">
						<sec:authorize access="hasPermission(#roomGroupEditForm.id, 'RoomGroup', 'DepartmenalRoomGroupDelete')">
							&nbsp;
							<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="D" 
									title="Deletes Room Group (Alt+D)">
								<bean:message key="button.delete" />
							</html:submit>
						</sec:authorize>
					</logic:notEqual>
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
