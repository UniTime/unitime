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
<%@ page language="java" autoFlush="true"%>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@page import="org.cpsolver.ifs.util.DistanceMetric"%>
<%@page import="org.unitime.timetable.ApplicationProperties"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<tiles:importAttribute />
<tt:session-context/>
<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(sessionContext) %>
		
		function confirmDelete() {
			if (jsConfirm!=null && !jsConfirm)
				return true;

			if(confirm('The building and all its rooms will be deleted. Continue?')) {
				return true;
			}
			return false;
		}

	// -->
</SCRIPT>


<html:form action="/buildingEdit">
	<html:hidden property="uniqueId"/>
	<html:hidden property="sessionId"/>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="2">
		<TR>
			<TD colspan="2">
			<tt:section-header>
					<tt:section-title>
						<logic:equal name="buildingEditForm" property="op" value="Save">
						Add
						</logic:equal>
						<logic:equal name="buildingEditForm" property="op" value="Update">
						Edit
						</logic:equal>
						Building
					</tt:section-title>
					<logic:equal name="buildingEditForm" property="op" value="Save">
						<html:submit property="op" value="Save" title="Save (Alt+S)" accesskey="S"/> 
					</logic:equal>
					<logic:equal name="buildingEditForm" property="op" value="Update">
						<html:submit property="op" value="Update" title="Update (Alt+U)" accesskey="U"/> 
						<sec:authorize access="hasPermission(#buildingEditForm.uniqueId, 'Building', 'BuildingDelete')">
							<html:submit property="op" value="Delete" title="Delete (Alt+D)" accesskey="D" onclick="return confirmDelete();"/>
						</sec:authorize> 
					</logic:equal>
					<html:submit property="op" value="Back" title="Back (Alt+B)" accesskey="B"/> 
				</tt:section-header>
			</TD>
		</TR>

		<TR>
			<TD>Name:</TD>
			<TD>
				<html:text property="name" size="100" maxlength="80"/>
				&nbsp;<html:errors property="name"/>
			</TD>
		</TR>

		<TR>
			<TD>Abbreviation:</TD>
			<TD>
				<html:text property="abbreviation" size="20" maxlength="20"/>
				&nbsp;<html:errors property="abbreviation"/>
			</TD>
		</TR>

		<TR>
			<TD>External ID:</TD>
			<TD>
				<html:text property="externalId" size="40" maxlength="40"/>
				&nbsp;<html:errors property="externalId"/>
			</TD>
		</TR>
		
		<TR>
			<TD>Coordinates:</TD>
			<TD>
				<html:text property="coordX" size="12" maxlength="12" styleId="coordX"/>,
				<html:text property="coordY" size="12" maxlength="12" styleId="coordY"/>
				&nbsp;<html:errors property="coordX"/> <html:errors property="coordy"/>
				<% DistanceMetric.Ellipsoid ellipsoid = DistanceMetric.Ellipsoid.valueOf(ApplicationProperties.getProperty("unitime.distance.ellipsoid", DistanceMetric.Ellipsoid.LEGACY.name())); %>
				&nbsp;&nbsp;&nbsp;<i><%=ellipsoid.getEclipsoindName()%></i>
			</TD>
		</TR>

		<tt:propertyEquals name="unitime.coordinates.googlemap" value="true">
			<TR>
				<TD valign="top">Map:</TD>
				<TD>
					<div id='UniTimeGWT:Map' style="display: none;"></div>
				</TD>
			</TR>
		</tt:propertyEquals>
		<tt:propertyNotEquals name="unitime.coordinates.googlemap" value="true">
			<tt:propertyEquals name="unitime.coordinates.leafletmap" value="true">
				<TR>
					<TD valign="top">Map:</TD>
					<TD>
						<div id='UniTimeGWT:Map' style="display: none;"></div>
					</TD>
				</TR>
			</tt:propertyEquals>
		</tt:propertyNotEquals>

		<TR>
			<TD colspan='2'>
				<table width="100%" border="0" cellspacing="0" cellpadding="2" style="border-top: 1px solid #9CB0CE;"><tr><td align='left'>
				<logic:equal name="buildingEditForm" property="op" value="Update">
					<html:checkbox property="updateRoomCoordinates"><i>Update room coordinates to match the building coordinates.</i></html:checkbox>
				</logic:equal>
				</td><td align='right'>
				<logic:equal name="buildingEditForm" property="op" value="Save">
					<html:submit property="op" value="Save" title="Save (Alt+S)" accesskey="S"/> 
				</logic:equal>
				<logic:equal name="buildingEditForm" property="op" value="Update">
					<html:submit property="op" value="Update" title="Update (Alt+U)" accesskey="U"/> 
					<sec:authorize access="hasPermission(#buildingEditForm.uniqueId, 'Building', 'BuildingDelete')">
						<html:submit property="op" value="Delete" title="Delete (Alt+D)" accesskey="D" onclick="return confirmDelete();"/>
					</sec:authorize> 
				</logic:equal>
				<html:submit property="op" value="Back" title="Back (Alt+B)" accesskey="B"/> 
				</td></tr>
				</table>
			</TD>
		</TR>
	</TABLE>

</html:form>