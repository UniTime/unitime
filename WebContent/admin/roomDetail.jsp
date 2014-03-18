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
<%@ page import="org.unitime.timetable.model.Roles" %>
<%@ page import="org.unitime.timetable.form.RoomDetailForm" %>
<%@ page import="org.unitime.timetable.model.RoomGroup" %>
<%@ page import="org.unitime.timetable.model.GlobalRoomFeature" %>
<%@ page import="org.unitime.timetable.model.DepartmentRoomFeature" %>
<%@ page import="org.unitime.timetable.model.PreferenceLevel" %> 
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.apache.struts.util.LabelValueBean" %>
<%@page import="org.cpsolver.ifs.util.DistanceMetric"%>
<%@page import="org.unitime.timetable.ApplicationProperties"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ taglib uri="/WEB-INF/tld/localization.tld" prefix="loc" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<%
	// Get Form 
	String frmName = "roomDetailForm";	
	RoomDetailForm frm = (RoomDetailForm) request.getAttribute(frmName);
%>	
<tt:session-context/>
<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(sessionContext) %>
		
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
	<loc:bundle name="CourseMessages">
	<html:hidden property="id"/>
	<html:hidden property="next"/>
	<html:hidden property="previous"/>
	<input type='hidden' name='confirm' value='y'/>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title><%=frm.getName()%></tt:section-title>
					<sec:authorize access="(#roomDetailForm.nonUniv == true and hasPermission(#roomDetailForm.id, 'NonUniversityLocation', 'NonUniversityLocationEdit')) or (#roomDetailForm.nonUniv == false and hasPermission(#roomDetailForm.id, 'Location', 'RoomEdit'))">
						<html:submit property="doit" 
								accesskey="R" styleClass="btn" titleKey="title.modifyRoom"
								>
								<bean:message key="button.modifyRoom" />
						</html:submit>						
					</sec:authorize>
					<sec:authorize access="hasPermission(#roomDetailForm.id, 'Location', 'RoomEditAvailability')">
						&nbsp;
						<html:submit property="doit" 
								accesskey="A" styleClass="btn" titleKey="title.modifyRoomDepts"
							>
							<bean:message key="button.modifyRoomDepts" />
						</html:submit>
					</sec:authorize>
					<sec:authorize access="hasPermission(#roomDetailForm.id, 'Location', 'RoomEditPreference')">
						&nbsp;
						<html:submit property="doit" 
								accesskey="M" styleClass="btn" titleKey="title.modifyRoomPreference"
								>
								<bean:message key="button.modifyRoomPreference" />
						</html:submit>
					</sec:authorize>
					<sec:authorize access="hasPermission(#roomDetailForm.id, 'Location', 'RoomEditGroups')">
						&nbsp;
						<html:submit property="doit" 
								accesskey="G" styleClass="btn" titleKey="title.modifyRoomGroups"
								>
							<bean:message key="button.modifyRoomGroups" />
						</html:submit>
					</sec:authorize>
					<sec:authorize access="hasPermission(#roomDetailForm.id, 'Location', 'RoomEditFeatures')">
						&nbsp;
						<html:submit property="doit" 
								accesskey="F" styleClass="btn" titleKey="title.modifyRoomFeatures"
								>
								<bean:message key="button.modifyRoomFeatures" />
						</html:submit>
					</sec:authorize>
					<sec:authorize access="hasPermission(#roomDetailForm.id, 'Location', 'RoomEditEventAvailability')">
						&nbsp;
						<html:submit property="doit" accesskey="E" styleClass="btn" title="Edit Event Availability (Alt+E)">Edit Event Availability</html:submit>
					</sec:authorize>
					<sec:authorize access="(#roomDetailForm.nonUniv == true and hasPermission(#roomDetailForm.id, 'NonUniversityLocation', 'NonUniversityLocationDelete')) or (#roomDetailForm.nonUniv == false and hasPermission(#roomDetailForm.id, 'Location', 'RoomDelete'))">
						&nbsp;
						<html:submit property="doit"  styleClass="btn" accesskey="D" titleKey="title.removeRoom" onclick="confirmDelete();">
							<bean:message key="button.delete" />
						</html:submit>
					</sec:authorize>
					<logic:notEmpty name="<%=frmName%>" property="previous">
						<logic:greaterEqual name="<%=frmName%>" property="previous" value="0">
							&nbsp;
							<html:submit property="doit"  styleClass="btn" accesskey="P" titleKey="title.previousRoom">
								<bean:message key="button.previousRoom" />
							</html:submit>
						</logic:greaterEqual>
					</logic:notEmpty>
					<logic:notEmpty name="<%=frmName%>" property="next">
						<logic:greaterEqual name="<%=frmName%>" property="next" value="0">
							&nbsp;
							<html:submit property="doit"  styleClass="btn" accesskey="N" titleKey="title.nextRoom">
								<bean:message key="button.nextRoom" />
							</html:submit>
						</logic:greaterEqual>
					</logic:notEmpty>
					&nbsp;
					<tt:back styleClass="btn" name="Back" title="Return to %% (Alt+B)" accesskey="B" type="Location">
						<bean:write name="<%=frmName%>" property="id"/>
					</tt:back>
					<%--
					<html:submit property="doit"  styleClass="btn" accesskey="B" titleKey="title.returnToRoomList">
						<bean:message key="button.returnToRoomList" />
					</html:submit>
					--%>
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
		
		<tt:hasProperty name="unitime.minimap.url">
			<% if (frm.getCoordinateX() != null && frm.getCoordinateY() != null) { %>
				</table>
				<table width="100%" border="0" cellspacing="0" cellpadding="0">
					<tr><td valign="top"> <!-- 450, 463 -->
						<table width="100%" border="0" cellspacing="0" cellpadding="3">
			<% } %>
		</tt:hasProperty>
		
		<TR>
			<TD>Type:</TD><TD><bean:write name="<%=frmName%>" property="typeName"/></TD>
		</TR>

		<logic:notEmpty name="<%=frmName%>" property="externalId">
			<TR>
				<TD>External Id:</TD><TD><bean:write name="<%=frmName%>" property="externalId"/></TD>
			</TR>
		</logic:notEmpty>		

		<TR>
			<TD>Capacity:</TD><TD width='100%'><%=frm.getCapacity()%></TD>
		</TR>
		
		<logic:notEmpty name="<%=frmName%>" property="examEnabledProblems">
			<TD nowrap>Exam Seating Capacity:</TD><TD width='100%'>
				<bean:write name="<%=frmName%>" property="examCapacity"/> (<bean:write name="<%=frmName%>" property="examEnabledProblems"/> Examinations)
			</TD>
		</logic:notEmpty>
		
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
		
		<% if (frm.getCoordinateX() != null && frm.getCoordinateY() != null) { %>
			<TR>
				<TD>Coordinates:</TD><TD><%=frm.getCoordinateX()%>, <%=frm.getCoordinateY()%>
					<% DistanceMetric.Ellipsoid ellipsoid = DistanceMetric.Ellipsoid.valueOf(ApplicationProperties.getProperty("unitime.distance.ellipsoid", DistanceMetric.Ellipsoid.LEGACY.name())); %>
					&nbsp;&nbsp;&nbsp;<i><%=ellipsoid.getEclipsoindName()%></i>
				</TD>
			</TR>
		<% } %>
		
		<logic:notEmpty name="<%=frmName%>" property="area">
			<TR>
				<TD><loc:message name="propertyRoomArea"/></TD><TD><bean:write name="<%=frmName%>" property="area"/> <loc:message name="roomAreaUnitsLong"/></TD>
			</TR>
		</logic:notEmpty>

		<TR>
			<TD nowrap>Ignore Too Far Distances:</TD><TD><%=frm.getIgnoreTooFar()%></TD>
		</TR>
			
		<TR>
			<TD nowrap>Ignore Room Checks:</TD><TD><bean:write name="<%=frmName%>" property="ignoreRoomCheck"/></TD>
		</TR>
		
		<logic:notEmpty name="<%=frmName%>" property="eventDepartment">
			<TR>
				<TD nowrap>Event Department:</TD>
				<TD>
					<logic:iterate scope="request" name="eventDepts" id="d">
						<logic:equal name="<%=frmName%>" property="eventDepartment" value="<%=((LabelValueBean)d).getValue()%>">
							<bean:write name="d" property="label"/>
						</logic:equal>
					</logic:iterate>
				</TD>
			</TR>
			
			<TR>
				<TD nowrap>Event Status:</TD>
				<TD>
					<bean:write name="<%=frmName%>" property="eventStatus" filter="false"/>
				</TD>
			</TR>
			<logic:notEmpty name="<%=frmName%>" property="breakTime">
				<TR>
					<TD nowrap>Break Time:</TD>
					<TD>
						<bean:write name="<%=frmName%>" property="breakTime" filter="false"/>
					</TD>
				</TR>
			</logic:notEmpty>
		</logic:notEmpty>

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
		
		<logic:notEmpty name="<%=frmName%>" property="note">
			<TR>
				<TD nowrap>Note:</TD>
				<TD>
					<bean:write name="<%=frmName%>" property="note" filter="false"/>
				</TD>
			</TR>
		</logic:notEmpty>		
		
		<tt:hasProperty name="unitime.minimap.url">
			<% if (frm.getCoordinateX() != null && frm.getCoordinateY() != null) { %>
						</table>
					</td><td width="1%" nowrap="nowrap" style="padding-right: 3px;">
						<img src="<%=ApplicationProperties.getProperty("unitime.minimap.url")
							.replace("%x",frm.getCoordinateX().toString())
							.replace("%y",frm.getCoordinateY().toString())
							.replace("%n",frm.getName())
							.replace("%i",frm.getExternalId() == null ? "" : frm.getExternalId())%>" border='0' alt="Minimap" style="border: 1px solid #9CB0CE;"/>
					</td></tr>
				</table>
				<table width="100%" border="0" cellspacing="0" cellpadding="3">
			<% } %>
		</tt:hasProperty>
		
		<TR>
			<TD colspan='2'>&nbsp;</TD>
		</TR>
		
		<sec:authorize access="hasPermission(#roomDetailForm.id, 'Location', 'RoomDetailAvailability')">
		
		<TR>
			<TD colspan='2'>
				<tt:section-title>Room Availability</tt:section-title>
			</TD>
		</TR>

		<TR>
			<TD colspan="2">
				<span id='UniTimeGWT:RoomSharingWidget' style="display: none;"><bean:write name="<%=frmName%>" property="id"/></span>
				<%-- <%=frm.getSharingTable()%> --%>
			</TD>
		</TR>
		
		<sec:authorize access="hasPermission(#roomDetailForm.id, 'Location', 'RoomEditAvailability')">
			<TR>
				<TD colspan="2" align='center' style='border-top:black 1px dashed'>
					<font size='-1'><i>
						Room Availability table is read-only. To edit this table, please click Edit Room Availability button.
					</i></font>
				</TD>
			</TR>
		</sec:authorize>

		</sec:authorize>
		<sec:authorize access="hasPermission(#roomDetailForm.id, 'Location', 'RoomDetailPeriodPreferences')">
		<logic:iterate scope="request" name="examTypes" id="type" type="org.unitime.timetable.model.ExamType">
			<logic:equal name="<%=frmName%>" property='<%="examEnabled("+type.getUniqueId()+")"%>' value="true">
				<logic:notEmpty scope="request" name='<%="PeriodPrefs" + type.getUniqueId() %>'>
					<logic:equal name="<%=frmName%>" property='<%="examEnabled("+type.getUniqueId()+")"%>' value="true">
						<TR id='<%="exPref" + type.getUniqueId() %>'>
					</logic:equal>
					<logic:notEqual name="<%=frmName%>" property='<%="examEnabled("+type.getUniqueId()+")"%>' value="true">
						<TR id='<%="exPref" + type.getUniqueId() %>' style='display:none;'>
					</logic:notEqual>
						<TD nowrap valign="top"><bean:write name="type" property="label"/> Examination<br>Periods Preferences:</TD>
						<TD>
							<bean:write scope="request" name='<%="PeriodPrefs" + type.getUniqueId() %>' filter="false"/>
						</TD>
					</TR>
					<sec:authorize access="(#roomDetailForm.nonUniv == true and hasPermission(#roomDetailForm.id, 'NonUniversityLocation', 'NonUniversityLocationEdit')) or (#roomDetailForm.nonUniv == false and hasPermission(#roomDetailForm.id, 'Location', 'RoomEdit'))">
						<TR>
							<TD colspan="2" align="center" style='border-top:black 1px dashed'>
								<font size='-1'><i>
									<bean:write name="type" property="label"/> Examination Period Preferences table is read-only. To edit this table, please click Edit Room button.
								</i></font>
							</TD>
						</TR>
					</sec:authorize>
				</logic:notEmpty>
			</logic:equal>
		</logic:iterate>
		</sec:authorize>
		
		<sec:authorize access="hasPermission(#roomDetailForm.id, 'Location', 'RoomDetailEventAvailability')">
		
		<TR>
			<TD colspan='2'>
				<tt:section-title>Room Event Availability</tt:section-title>
			</TD>
		</TR>

		<TR>
			<TD colspan="2">
				<span id='UniTimeGWT:RoomEventAvailabilityWidget' style="display: none;"><bean:write name="<%=frmName%>" property="id"/></span>
				<%-- <%=frm.getSharingTable()%> --%>
			</TD>
		</TR>
		
		<sec:authorize access="hasPermission(#roomDetailForm.id, 'Location', 'RoomEditEventAvailability')">
			<TR>
				<TD colspan="2" align='center' style='border-top:black 1px dashed'>
					<font size='-1'><i>
						Room Event Availability table is read-only. To edit this table, please click Edit Event Availability button.
					</i></font>
				</TD>
			</TR>
		</sec:authorize>

		</sec:authorize>		

		<tt:last-change type='Location'>
			<bean:write name="<%=frmName%>" property="id"/>
		</tt:last-change>		
		
		<TR>
			<TD colspan="2">
				<div id='UniTimeGWT:RoomNoteChanges' style="display: none;"><bean:write name="<%=frmName%>" property="id"/></div>
			</TD>
		</TR>

		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>
		
		<TR>
			<TD colspan='2' align='right'>
				<sec:authorize access="(#roomDetailForm.nonUniv == true and hasPermission(#roomDetailForm.id, 'NonUniversityLocation', 'NonUniversityLocationEdit')) or (#roomDetailForm.nonUniv == false and hasPermission(#roomDetailForm.id, 'Location', 'RoomEdit'))">
					<html:submit property="doit" 
							accesskey="R" styleClass="btn" titleKey="title.modifyRoom"
							>
							<bean:message key="button.modifyRoom" />
					</html:submit>						
				</sec:authorize>
				<sec:authorize access="hasPermission(#roomDetailForm.id, 'Location', 'RoomEditAvailability')">
					&nbsp;
					<html:submit property="doit" 
							accesskey="A" styleClass="btn" titleKey="title.modifyRoomDepts"
						>
						<bean:message key="button.modifyRoomDepts" />
					</html:submit>
				</sec:authorize>
				<sec:authorize access="hasPermission(#roomDetailForm.id, 'Location', 'RoomEditPreference')">
					&nbsp;
					<html:submit property="doit" 
							accesskey="M" styleClass="btn" titleKey="title.modifyRoomPreference"
							>
							<bean:message key="button.modifyRoomPreference" />
					</html:submit>
				</sec:authorize>
				<sec:authorize access="hasPermission(#roomDetailForm.id, 'Location', 'RoomEditGroups')">
					&nbsp;
					<html:submit property="doit" 
							accesskey="G" styleClass="btn" titleKey="title.modifyRoomGroups"
							>
						<bean:message key="button.modifyRoomGroups" />
					</html:submit>
				</sec:authorize>
				<sec:authorize access="hasPermission(#roomDetailForm.id, 'Location', 'RoomEditFeatures')">
					&nbsp;
					<html:submit property="doit" 
						accesskey="F" styleClass="btn" titleKey="title.modifyRoomFeatures"
						>
						<bean:message key="button.modifyRoomFeatures" />
					</html:submit>
				</sec:authorize>
				<sec:authorize access="hasPermission(#roomDetailForm.id, 'Location', 'RoomEditEventAvailability')">
					&nbsp;
					<html:submit property="doit" accesskey="E" styleClass="btn" title="Edit Event Availability (Alt+E)">Edit Event Availability</html:submit>
				</sec:authorize>
				<sec:authorize access="(#roomDetailForm.nonUniv == true and hasPermission(#roomDetailForm.id, 'NonUniversityLocation', 'NonUniversityLocationDelete')) or (#roomDetailForm.nonUniv == false and hasPermission(#roomDetailForm.id, 'Location', 'RoomDelete'))">
					&nbsp;
					<html:submit property="doit"  styleClass="btn" accesskey="D" titleKey="title.removeRoom" onclick="confirmDelete();">
						<bean:message key="button.delete" />
					</html:submit>
				</sec:authorize>
			<logic:notEmpty name="<%=frmName%>" property="previous">
				<logic:greaterEqual name="<%=frmName%>" property="previous" value="0">
					&nbsp;
					<html:submit property="doit"  styleClass="btn" accesskey="P" titleKey="title.previousRoom">
						<bean:message key="button.previousRoom" />
					</html:submit>
				</logic:greaterEqual>
				</logic:notEmpty>
				<logic:notEmpty name="<%=frmName%>" property="next">
					<logic:greaterEqual name="<%=frmName%>" property="next" value="0">
						&nbsp;
						<html:submit property="doit"  styleClass="btn" accesskey="N" titleKey="title.nextRoom">
							<bean:message key="button.nextRoom" />
						</html:submit>
					</logic:greaterEqual>
				</logic:notEmpty>
				&nbsp;
				<tt:back styleClass="btn" name="Back" title="Return to %% (Alt+B)" accesskey="B" type="Location">
					<bean:write name="<%=frmName%>" property="id"/>
				</tt:back>
			</TD>
		</TR>

	</TABLE>
	</loc:bundle>			
</html:form>
