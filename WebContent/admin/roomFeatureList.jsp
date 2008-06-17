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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">

	<!-- Buttons -->
	<TR>
		<TD valign="middle" colspan="4">
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
						<logic:equal name="roomFeatureListForm" property="deptSize" value="false">
							<TD>
								<html:form action="roomFeatureList" styleClass="FormWithNoPadding">			
									<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="P" titleKey="title.exportPDF">
										<bean:message key="button.exportPDF" />
									</html:submit>
								</html:form>
							</TD>
						</logic:equal>
						<logic:equal name="roomFeatureListForm" property="canAdd" value="true">
							<TD align="right">
								<html:form action="roomFeatureAdd" styleClass="FormWithNoPadding">			
									<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="F" titleKey="title.addRoomFeature">
										<bean:message key="button.addRoomFeature" />
									</html:submit>
								</html:form>
							</TD>
						</logic:equal>
						<%--
						<TD align="right">
							<html:form action="roomList" styleClass="FormWithNoPadding">
								<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="R" titleKey="title.returnToRoomList">
									<bean:message key="button.returnToRoomList" />
								</html:submit>
							</html:form>
						</TD>
						--%>
					</TR>
				</TABLE>
			</tt:section-header>
		</TD>
	<TR>

	<logic:messagesPresent>
	<TR>
		<TD colspan="4" align="left" class="errorCell">
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
			<TD valign="middle" colspan="4">
				&nbsp;<A name="roomFeaturesGlobal"/>
			</TD>
		</TR>
		<%=request.getAttribute("roomFeaturesGlobal")%>
	<%}%>

	<% if (request.getAttribute("roomFeaturesDepartment") != null) {%>
		<TR>
			<TD valign="middle" colspan="4">
				&nbsp;<A name="roomFeaturesDepartment"/>
			</TD>
		</TR>
		<%=request.getAttribute("roomFeaturesDepartment")%>
	<%}%>
	
	<% if (request.getAttribute("roomFeaturesDepartment") == null && request.getAttribute("roomFeaturesGlobal") == null) {%>
		<TR>
			<TD valign="middle" colspan="4">
				<i>There are no room features available for the selected department.</i>
			</TD>
		</TR>
	<% } %>

	<TR>
		<TD valign="middle" colspan="4">&nbsp;</TD>
	<TR>

	<TR>
		<TD valign="middle" colspan="4">
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
	<TR>

<!-- Buttons -->
	<TR>
		<TD valign="middle" colspan="4">
			<TABLE align="right" cellspacing="0" cellpadding="2" class="FormWithNoPadding">
				<TR>
					<logic:equal name="roomFeatureListForm" property="deptSize" value="false">
						<TD>
							<html:form action="roomFeatureList" styleClass="FormWithNoPadding">			
								<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="P" titleKey="title.exportPDF">
									<bean:message key="button.exportPDF" />
								</html:submit>
							</html:form>
						</TD>
					</logic:equal>
					<logic:equal name="roomFeatureListForm" property="canAdd" value="true">
						<TD align="right">
							<html:form action="roomFeatureAdd" styleClass="FormWithNoPadding">			
								<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="F" titleKey="title.addRoomFeature">
									<bean:message key="button.addRoomFeature" />
								</html:submit>
							</html:form>
						</TD>
					</logic:equal>
				
					<%--
					<TD align="right">
						<html:form action="roomList" styleClass="FormWithNoPadding">
							<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="R" titleKey="title.returnToRoomList">
								<bean:message key="button.returnToRoomList" />
							</html:submit>
						</html:form>
					</TD>
					--%>
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
