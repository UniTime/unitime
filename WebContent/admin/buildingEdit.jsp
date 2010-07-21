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
<%@ page language="java" autoFlush="true"%>
<%@ page import="org.unitime.commons.web.Web" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@page import="net.sf.cpsolver.ifs.util.DistanceMetric"%>
<%@page import="org.unitime.timetable.ApplicationProperties"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<tiles:importAttribute />

<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(Web.getUser(session)) %>
		
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

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
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
						<html:submit property="op" value="Delete" title="Delete (Alt+D)" accesskey="D" onclick="return confirmDelete();"/> 
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
				<html:text property="abbreviation" size="10" maxlength="10"/>
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
				<html:text property="coordX" size="12" maxlength="12"/>,
				<html:text property="coordY" size="12" maxlength="12"/>
				&nbsp;<html:errors property="coordX"/> <html:errors property="coordy"/>
				<% DistanceMetric.Ellipsoid ellipsoid = DistanceMetric.Ellipsoid.valueOf(ApplicationProperties.getProperty("unitime.distance.ellipsoid", DistanceMetric.Ellipsoid.LEGACY.name())); %>
				&nbsp;&nbsp;&nbsp;<i><%=ellipsoid.getEclipsoindName()%></i>
			</TD>
		</TR>

		<tr>
			<td valign="middle" colspan="2">
				<tt:section-title/>
			</td>
		<tr>
		
		<TR>
			<TD align="right" colspan="2">
				<logic:equal name="buildingEditForm" property="op" value="Save">
					<html:submit property="op" value="Save" title="Save (Alt+S)" accesskey="S"/> 
				</logic:equal>
				<logic:equal name="buildingEditForm" property="op" value="Update">
					<html:submit property="op" value="Update" title="Update (Alt+U)" accesskey="U"/> 
					<html:submit property="op" value="Delete" title="Delete (Alt+D)" accesskey="D" onclick="return confirmDelete();"/> 
				</logic:equal>
				<html:submit property="op" value="Back" title="Back (Alt+B)" accesskey="B"/> 
			</TD>
		</TR>
	</TABLE>

</html:form>
