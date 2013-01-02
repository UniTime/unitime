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
<%@ page import="org.unitime.timetable.form.RoomFeatureEditForm" %>
<%@ page import="org.unitime.timetable.model.Room" %>
<%@ page import="org.unitime.timetable.model.Location" %>
<%@page import="org.unitime.timetable.model.Exam"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<%
	// Get Form 
	String frmName = "roomFeatureEditForm";		
	RoomFeatureEditForm frm = (RoomFeatureEditForm) request.getAttribute(frmName);
%>	

<tiles:importAttribute />
<html:form action="/roomFeatureEdit" focus="name">
	<html:hidden property="id"/>
	<html:hidden property="sessionId"/>
	<html:hidden property="deptCode"/>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan='2'>
			<tt:section-header>
				<logic:empty name="roomFeatureEditForm" property="id">
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="A" 
							title="Add New Room Feature (Alt+A)">
						<bean:message key="button.addNew" />
					</html:submit>
				</logic:empty>
				<logic:notEmpty name="roomFeatureEditForm" property="id">
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="U" 
							title="Update Room Feature (Alt+U)">
						<bean:message key="button.update" />
					</html:submit>
					<logic:equal name="roomFeatureEditForm" property="global" value="true">
						<sec:authorize access="hasPermission(#roomFeatureEditForm.id, 'GlobalRoomFeature', 'GlobalRoomFeatureDelete')">
							&nbsp;	
							<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="D" 
									title="Delete Room Feature (Alt+D)">
								<bean:message key="button.delete" />
							</html:submit>
						</sec:authorize>
					</logic:equal>
					<logic:notEqual name="roomFeatureEditForm" property="global" value="true">
						<sec:authorize access="hasPermission(#roomFeatureEditForm.id, 'DepartmentRoomFeature', 'DepartmenalRoomFeatureDelete')">
							&nbsp;	
							<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="D" 
									title="Delete Room Feature (Alt+D)">
								<bean:message key="button.delete" />
							</html:submit>
						</sec:authorize>
					</logic:notEqual>
				</logic:notEmpty>
				&nbsp;
				<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="R" 
					title="Back to Room Features (Alt+B)">
					<bean:message key="button.returnToRoomFeatureList"/>
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

		<logic:notEmpty scope="request" name="featureTypes">
			<TR>
				<TD>Type:</TD>
				<TD>
				<html:select property="featureTypeId">
					<html:option value="-1">No Type</html:option>
					<html:options collection="featureTypes" property="uniqueId" labelProperty="label"/>
				</html:select>
				</TD>
			</TR>
		</logic:notEmpty>
		
		<TR>
			<TD>Global:</TD>
			<TD>
				<html:checkbox property="global" disabled="true"/>
				<html:hidden property="global"/>
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
				<td class="WebTableHeader">Other Room Features</td>
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
				<td class="WebTableHeader">Other Room Features</td>
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
				<logic:empty name="roomFeatureEditForm" property="id">
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="A" 
							title="Add New Room Feature (Alt+A)">
						<bean:message key="button.addNew" />
					</html:submit>
				</logic:empty>
				<logic:notEmpty name="roomFeatureEditForm" property="id">
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="U" 
							title="Update Room Feature (Alt+U)">
						<bean:message key="button.update" />
					</html:submit>
					&nbsp;	
					<logic:equal name="roomFeatureEditForm" property="global" value="true">
						<sec:authorize access="hasPermission(#roomFeatureEditForm.id, 'GlobalRoomFeature', 'GlobalRoomFeatureDelete')">
							&nbsp;	
							<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="D" 
									title="Delete Room Feature (Alt+D)">
								<bean:message key="button.delete" />
							</html:submit>
						</sec:authorize>
					</logic:equal>
					<logic:notEqual name="roomFeatureEditForm" property="global" value="true">
						<sec:authorize access="hasPermission(#roomFeatureEditForm.id, 'DepartmentRoomFeature', 'DepartmenalRoomFeatureDelete')">
							&nbsp;	
							<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="D" 
									title="Delete Room Feature (Alt+D)">
								<bean:message key="button.delete" />
							</html:submit>
						</sec:authorize>
					</logic:notEqual>
				</logic:notEmpty>
				&nbsp;
				<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="R" 
					title="Back to Room Features (Alt+B)">
					<bean:message key="button.returnToRoomFeatureList"/>
				</html:submit>
			</TD>
		</TR>
	</TABLE>

</html:form>
