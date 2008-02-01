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
<%@ page import="org.unitime.timetable.form.RoomDetailForm" %>
<%@ page import="org.unitime.timetable.model.RoomGroup" %>
<%@ page import="org.unitime.timetable.model.GlobalRoomFeature" %>
<%@ page import="org.unitime.timetable.model.DepartmentRoomFeature" %>
<%@ page import="org.unitime.timetable.model.PreferenceLevel" %> 
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.apache.struts.util.LabelValueBean" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<%
	boolean admin = Web.hasRole(request.getSession(), new String[] { Roles.ADMIN_ROLE});

	// Get Form 
	String frmName = "roomDetailForm";	
	RoomDetailForm frm = (RoomDetailForm) request.getAttribute(frmName);
%>	
<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(Web.getUser(session)) %>
		
		function confirmDelete() {
			if (jsConfirm!=null && !jsConfirm)
				return;

			if (!confirm('Do you really want to delete this room?')) {
				roomDetailForm.confirm.value='n';
			}
		}
	// -->
</SCRIPT>

<tiles:importAttribute />

<html:form action="/roomDetail">
	<html:hidden property="id"/>
	<input type='hidden' name='confirm' value='y'/>

	<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title><%=frm.getName()%></tt:section-title>
					<logic:equal name="<%=frmName%>" property="editable" value="true">
					<% if (admin || (frm.isNonUniv() && frm.isOwner())) { %>
						<html:submit property="doit" 
								accesskey="P" styleClass="btn" titleKey="title.modifyRoom"
								>
								<bean:message key="button.modifyRoom" />
						</html:submit>						
						&nbsp;
					<% } %>
					<html:submit property="doit" 
							accesskey="A" styleClass="btn" titleKey="title.modifyRoomDepts"
							>
						<bean:message key="button.modifyRoomDepts" />
					</html:submit>
					&nbsp;
					<html:submit property="doit" 
							accesskey="P" styleClass="btn" titleKey="title.modifyRoomPreference"
							>
							<bean:message key="button.modifyRoomPreference" />
					</html:submit>
					&nbsp;
					<html:submit property="doit" 
							accesskey="G" styleClass="btn" titleKey="title.modifyRoomGroups"
							>
						<bean:message key="button.modifyRoomGroups" />
					</html:submit>
					&nbsp;
					<html:submit property="doit" 
							accesskey="F" styleClass="btn" titleKey="title.modifyRoomFeatures"
							>
							<bean:message key="button.modifyRoomFeatures" />
					</html:submit>
					<logic:equal name="<%=frmName%>" property="examEnabled" value="true">
						&nbsp;
						<html:submit property="doit"  styleClass="btn" accesskey="X" titleKey="title.modifyRoomPeriodPreferences">
							<bean:message key="button.modifyRoomPeriodPreferences" />
						</html:submit>
					</logic:equal>
					<% if (admin || (frm.isDeleteFlag() && frm.isOwner())) {%>
						&nbsp;
						<html:submit property="doit"  styleClass="btn" accesskey="D" titleKey="title.removeRoom" onclick="confirmDelete();">
							<bean:message key="button.delete" />
						</html:submit>
					<%}%>
					&nbsp;
					</logic:equal>
					<html:submit property="doit"  styleClass="btn" accesskey="B" titleKey="title.returnToRoomList">
						<bean:message key="button.returnToRoomList" />
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
	
		<logic:notEmpty name="<%=frmName%>" property="externalId">
			<TR>
				<TD>External Id:</TD><TD><bean:write name="<%=frmName%>" property="externalId"/></TD>
			</TR>
		</logic:notEmpty>		

		<TR>
			<TD>Capacity:</TD><TD width='100%'><%=frm.getCapacity()%></TD>
		</TR>
		
		<logic:equal name="<%=frmName%>" property="examEnabled" value="true">
			<TR>
				<TD nowrap>Exam Seating Capacity:</TD><TD width='100%'>
					<bean:write name="<%=frmName%>" property="examCapacity"/>
				</TD>
			</TR>
		</logic:equal>
		
		<logic:notEmpty name="<%=frmName%>" property="control"> 
			<TR>
				<TD nowrap>Controlling Department:</TD>
				<TD>
					<logic:iterate scope="request" name="<%=Department.DEPT_ATTR_NAME%>" id="d">
						<logic:equal name="<%=frmName%>" property="control" value="<%=((LabelValueBean)d).getValue()%>">
							<bean:write name="d" property="label"/>
						</logic:equal>
					</logic:iterate>
				</TD>
			</TR>
		</logic:notEmpty>
			
		<% if (frm.getCoordinateX().intValue()>=0 && frm.getCoordinateY().intValue()>=0) { %>
			<TR>
				<TD>Coordinates:</TD><TD><%=frm.getCoordinateX()%>, <%=frm.getCoordinateY()%></TD>
			</TR>
		<% } else {%>
			<TR>
				<TD nowrap>Ignore Too Far Distances:</TD><TD><%=frm.getIgnoreTooFar()%></TD>
			</TR>
		<% } %>
			
		<TR>
			<TD nowrap>Ignore Room Checks:</TD><TD><bean:write name="<%=frmName%>" property="ignoreRoomCheck"/></TD>
		</TR>

		<TR>
			<TD>Type:</TD><TD><bean:write name="<%=frmName%>" property="typeName"/></TD>
		</TR>

		<logic:notEmpty name="<%=frmName%>" property="roomPrefs">
			<TR>
				<TD valign="top">Preference:</TD>
				<TD>
					<logic:iterate name="<%=frmName%>" property="depts" id="dept" indexId="idx">
						<% Department d = (Department) dept; PreferenceLevel preference = (PreferenceLevel)frm.getRoomPrefs().get(((Integer)idx).intValue()); %>
						<logic:greaterThan name="idx" value="0">
							<BR>
						</logic:greaterThan>
						<span style='color:<%=preference.prefcolor()%>;font-weight:bold;'><%=preference.getPrefName()%></span>
						(<%=d.getDeptCode()%> - <%=d.getName()%>)
				   	</logic:iterate>
				</TD>
			</TR>
		</logic:notEmpty>
		
		<logic:notEmpty name="<%=frmName%>" property="groups">
			<TR>
				<TD valign="top">Groups:</TD>
				<TD>
					<logic:iterate name="<%=frmName%>" property="groups" id="group" indexId="idx">
						<% RoomGroup rg = (RoomGroup) group; %>
						<logic:greaterThan name="idx" value="0">
							<BR>
						</logic:greaterThan>
						<%=rg.getName()%>
						<%=rg.isGlobal().booleanValue()?"":rg.getDepartment().isExternalManager().booleanValue()?"("+rg.getDepartment().getExternalMgrLabel()+")":"("+rg.getDepartment().getDeptCode()+" - "+rg.getDepartment().getName()+")"%>
					</logic:iterate>
				</TD>
			</TR>
		</logic:notEmpty>

		<logic:notEmpty name="<%=frmName%>" property="globalFeatures">
			<TR>
				<TD nowrap valign="top">Global Features:</TD>
				<TD>
					<logic:iterate name="<%=frmName%>" property="globalFeatures" id="globalFeature" indexId="idx">
						<logic:greaterThan name="idx" value="0">
							<BR>
						</logic:greaterThan>
						<% GlobalRoomFeature grf = (GlobalRoomFeature) globalFeature; %>
						<%=grf.getLabel()%>
					</logic:iterate>
				</TD>
			</TR>
		</logic:notEmpty>
		
		<logic:notEmpty name="<%=frmName%>" property="departmentFeatures">
			<TR>
				<TD nowrap valign="top">Department Features:</TD>
				<TD>
					<logic:iterate name="<%=frmName%>" property="departmentFeatures" id="departmentFeature" indexId="idx">
						<logic:greaterThan name="idx" value="0">
							<BR>
						</logic:greaterThan>
						<% DepartmentRoomFeature drf = (DepartmentRoomFeature) departmentFeature; %>
						<%=drf.getLabel()%>
						(<%=drf.getDepartment().isExternalManager().booleanValue()?drf.getDepartment().getExternalMgrLabel():drf.getDepartment().getDeptCode()+" - "+drf.getDepartment().getName()%>)
					</logic:iterate>
				</TD>
			</TR>
		</logic:notEmpty>
		
		<logic:equal name="<%=frmName%>" property="examEnabled" value="true">
			<logic:notEmpty name="<%=frmName%>" property="examPref">
				<TR>
					<TD nowrap valign="top">Period Preferences:</TD>
					<TD>
						<bean:write name="<%=frmName%>" property="examPref" filter="false"/>
					</TD>
				</TR>
			</logic:notEmpty>
		</logic:equal>

		<TR>
			<TD colspan='2'>&nbsp;</TD>
		</TR>
		
		<TR>
			<TD colspan='2'>
				<tt:section-title>Room Availability</tt:section-title>
			</TD>
		</TR>

		<TR>
			<TD colspan="2">
				<%=frm.getSharingTable()%>
			</TD>
		</TR>

		<logic:equal name="<%=frmName%>" property="editable" value="true">
			<TR>
				<TD colspan="2" align='middle' style='border-top:black 1px dashed'>
					<font size='-1'><i>
						Room Availability table is read-only. To edit this table, please click Edit Room Availability button.
					</i></font>
				</TD>
			</TR>
		</logic:equal>

		<tt:last-change type='Location'>
			<bean:write name="<%=frmName%>" property="id"/>
		</tt:last-change>		

		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>
		
		<TR>
			<TD colspan='2' align='right'>
				<logic:equal name="<%=frmName%>" property="editable" value="true">
					<% if (admin || (frm.isNonUniv() && frm.isOwner())) { %>
						<html:submit property="doit" 
								accesskey="P" styleClass="btn" titleKey="title.modifyRoom"
								>
								<bean:message key="button.modifyRoom" />
						</html:submit>				
						&nbsp;
					<% } %>
					<html:submit property="doit" 
							accesskey="A" styleClass="btn" titleKey="title.modifyRoomDepts"
							>
						<bean:message key="button.modifyRoomDepts" />
					</html:submit>
					&nbsp;
					<html:submit property="doit" 
							accesskey="P" styleClass="btn" titleKey="title.modifyRoomPreference"
							>
							<bean:message key="button.modifyRoomPreference" />
					</html:submit>
					&nbsp;
					<html:submit property="doit" 
							accesskey="G" styleClass="btn" titleKey="title.modifyRoomGroups"
							>
						<bean:message key="button.modifyRoomGroups" />
					</html:submit>
					&nbsp;
					<html:submit property="doit" 
							accesskey="F" styleClass="btn" titleKey="title.modifyRoomFeatures"
							>
							<bean:message key="button.modifyRoomFeatures" />
					</html:submit>
					<logic:equal name="<%=frmName%>" property="examEnabled" value="true">
						&nbsp;
						<html:submit property="doit"  styleClass="btn" accesskey="X" titleKey="title.modifyRoomPeriodPreferences">
							<bean:message key="button.modifyRoomPeriodPreferences" />
						</html:submit>
					</logic:equal>
					<% if (admin || (frm.isDeleteFlag() && frm.isOwner())) {%>
						&nbsp;
						<html:submit property="doit"  styleClass="btn" accesskey="D" titleKey="title.removeRoom" onclick="confirmDelete();">
							<bean:message key="button.delete" />
						</html:submit>
					<%}%>
					&nbsp;
				</logic:equal>
				<html:submit property="doit"  styleClass="btn" accesskey="B" titleKey="title.returnToRoomList">
					<bean:message key="button.returnToRoomList" />
				</html:submit>
			</TD>
		</TR>

	</TABLE>
			
</html:form>