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
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">

	<!-- Buttons -->
	<TR>
		<TD valign="middle" colspan="5">
			<tt:section-header>
				<tt:section-title>
					<span style='font-weight:normal;'>
					<% if (request.getAttribute("roomFeaturesGlobal") != null) {%>
						<A class="l7" href="#roomFeaturesGlobal">Global Room Features</A>&nbsp;
					<% } %>
					<% if (request.getAttribute("roomFeaturesDepartment") != null) {%>
						<A class="l7" href="#roomFeaturesDepartment">Department Room Features</A>&nbsp;
					<% } %>
					</span>
				</tt:section-title>
				<TABLE align="right" cellspacing="0" cellpadding="2" class="FormWithNoPadding">
					<TR>
						<sec:authorize access="hasPermission(null, 'Session', 'GlobalRoomFeatureAdd') or hasPermission(#roomFeatureListForm.deptCodeX, 'Department', 'DepartmentRoomFeatureAdd')">
							<TD align="right">
								<html:form action="roomFeatureAdd" styleClass="FormWithNoPadding">			
									<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="F" titleKey="title.addRoomFeature">
										<bean:message key="button.addRoomFeature" />
									</html:submit>
								</html:form>
							</TD>
						</sec:authorize>
					</TR>
				</TABLE>
			</tt:section-header>
		</TD>
	</TR>

	<logic:messagesPresent>
	<TR>
		<TD colspan="5" align="left" class="errorCell">
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

<!-- room features list -->
	<% if (request.getAttribute("roomFeaturesGlobal") != null) {%>
		<TR>
			<TD valign="middle" colspan="5">
				&nbsp;<A name="roomFeaturesGlobal"></A>
			</TD>
		</TR>
		<%=request.getAttribute("roomFeaturesGlobal")%>
	<%}%>

	<% if (request.getAttribute("roomFeaturesDepartment") != null) {%>
		<TR>
			<TD valign="middle" colspan="5">
				&nbsp;<A name="roomFeaturesDepartment"></A>
			</TD>
		</TR>
		<%=request.getAttribute("roomFeaturesDepartment")%>
	<%}%>
	
	<% if (request.getAttribute("roomFeaturesDepartment") == null && request.getAttribute("roomFeaturesGlobal") == null) {%>
		<TR>
			<TD valign="middle" colspan="5">
				<i>There are no room features available for the selected department.</i>
			</TD>
		</TR>
	<% } %>

	<TR>
		<TD valign="middle" colspan="5">&nbsp;</TD>
	</TR>

	<TR>
		<TD valign="middle" colspan="5">
			<tt:section-header>
			<tt:section-title>
				<% if (request.getAttribute("roomFeaturesGlobal") != null) {%>
					<A class="l7" href="#roomFeaturesGlobal"><span style='font-weight:normal;'>Global Room Features</span></A>&nbsp;
				<% } %>
				<% if (request.getAttribute("roomFeaturesDepartment") != null) {%>
					<A class="l7" href="#roomFeaturesDepartment"><span style='font-weight:normal;'>Department Room Features</span></A>&nbsp;
				<% } %>
			</tt:section-title>
			</tt:section-header>
		</TD>
	</TR>

<!-- Buttons -->
	<TR>
		<TD valign="middle" colspan="5">
			<TABLE align="right" cellspacing="0" cellpadding="2" class="FormWithNoPadding">
					<TR>
						<sec:authorize access="hasPermission(null, 'Session', 'GlobalRoomFeatureAdd') or hasPermission(#roomFeatureListForm.deptCodeX, 'Department', 'DepartmentRoomFeatureAdd')">
							<TD align="right">
								<html:form action="roomFeatureAdd" styleClass="FormWithNoPadding">			
									<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="F" titleKey="title.addRoomFeature">
										<bean:message key="button.addRoomFeature" />
									</html:submit>
								</html:form>
							</TD>
						</sec:authorize>
					</TR>
				</TABLE>
		</TD>
	</TR>
</TABLE>
			
<SCRIPT type="text/javascript" language="javascript">
    <% if (request.getAttribute("hash") != null) { %>
  		location.hash = "<%=request.getAttribute("hash")%>";
	<% } %>
</SCRIPT>			
