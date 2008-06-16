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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp"%>
<%@ page import="org.unitime.timetable.form.RoomListForm" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<%
	// Get Form
	String frmName = "roomListForm";
	RoomListForm frm = (RoomListForm) request.getAttribute(frmName);
%>

<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">
<%
	String colspan = request.getAttribute("colspan")!=null
					 ? request.getAttribute("colspan").toString()
					 : "7";
%>

<!-- Buttons -->
	<TR>
		<TD valign="middle" colspan="<%=colspan%>">
		<tt:section-header>
			<tt:section-title>
				<span style='font-weight:normal;'>
					<logic:iterate name="<%=frmName%>" property="roomTypes" id="roomType">
						<bean:define name="roomType" property="reference" id="ref"/>
						<logic:notEmpty name="<%=(String)ref%>" scope="request">
							<A class="l7" href="<%="#"+ref%>"><bean:write name="roomType" property="label"/></A>&nbsp;
						</logic:notEmpty>
					</logic:iterate>
					<logic:notEmpty name="nonUnivLocation" scope="request">
						<A class="l7" href="#nonUnivLocation">Non University Locations</A>&nbsp;
					</logic:notEmpty>
				</span>
			</tt:section-title>
			<TABLE align="right" cellspacing="0" cellpadding="2" class="FormWithNoPadding">
				<TR>
					<logic:equal name="<%=frmName%>" property="deptSize" value="false">
						<TD>
							<html:form action="roomList" styleClass="FormWithNoPadding">			
								<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="P" titleKey="title.exportPDF">
									<bean:message key="button.exportPDF" />
								</html:submit>
							</html:form>
						</TD>
					</logic:equal>
					<logic:equal name="<%=frmName%>" property="canAdd" value="true">
						<TD nowrap>
							<html:form action="editRoom" styleClass="FormWithNoPadding">
								<html:hidden property="op" value="Add"/>
								<html:submit onclick="displayLoading();" styleClass="btn" accesskey="R" titleKey="title.addRoom">
									<bean:message key="button.addRoom" />
								</html:submit>
							</html:form>
						</TD>
					</logic:equal>
					<logic:equal name="<%=frmName%>" property="canAddNonUniv" value="true">
						<TD nowrap>
							<html:form action="addNonUnivLocation" styleClass="FormWithNoPadding">
								<html:submit onclick="displayLoading();" styleClass="btn" accesskey="N" titleKey="title.addNonUnivLocation">
									<bean:message key="button.addNonUnivLocation" />
								</html:submit>
							</html:form>
						</TD>
					</logic:equal>
					<logic:equal name="<%=frmName%>" property="canAddSpecial" value="true">
						<TD nowrap>
							<html:form action="addSpecialUseRoom" styleClass="FormWithNoPadding">
								<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="U" titleKey="title.addSpecialUseRoom">
									<bean:message key="button.addSpecialUseRoom" />
								</html:submit>
							</html:form>
						</TD>
					</logic:equal>
					<logic:equal name="<%=frmName%>" property="editRoomSharing" value="true">
						<logic:notEqual name="<%=frmName%>" property="deptCodeX" value="All">
							<TD nowrap>
								<html:form action="roomDeptEdit" styleClass="FormWithNoPadding">
									<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="E" titleKey="title.editRoomSharing">
										<bean:message key="button.editRoomSharing" />
									</html:submit>
								</html:form>
							</TD>
						</logic:notEqual>
					</logic:equal>

					<%--
					<TD nowrap>
						<html:form action="roomFeatureList" styleClass="FormWithNoPadding">
							<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="F" titleKey="title.roomFeatures" >
								<bean:message key="button.roomFeatures" />
							</html:submit>
						</html:form>
					</TD>
					<TD nowrap>
						<html:form action="roomGroupList" styleClass="FormWithNoPadding">
							<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="G" titleKey="title.roomGroups">
								<bean:message key="button.roomGroups" />
							</html:submit>
						</html:form>
					</TD>
					<TD nowrap>
						<html:form action="roomDeptList" styleClass="FormWithNoPadding">
							<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="D" titleKey="title.roomDepts">
								<bean:message key="button.roomDepts" />
							</html:submit>
						</html:form>
					</TD>
					--%>
				</TR>
			</TABLE>
		</tt:section-header>
		</TD>
	<TR>

<!--
	<logic:messagesPresent>
	<TR>
		<TD colspan="<%=colspan%>" align="left" class="errorCell">
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
-->
<!-- rooms -->
	<% boolean empty = true; %>
	<logic:iterate name="<%=frmName%>" property="roomTypes" id="roomType">
		<bean:define name="roomType" property="reference" id="ref"/>
		<logic:notEmpty name="<%=(String)ref%>" scope="request">
			<% empty = false; %>
			<TR>
				<TD valign="middle" colspan="<%=colspan%>">
					&nbsp;<A name="<%=(String)ref%>"/>
				</TD>
			<TR>
			<TR>
				<bean:write name="<%=(String)ref%>" scope="request" filter="false"/>
			</TR>
		</logic:notEmpty>
	</logic:iterate>
	<logic:notEmpty name="nonUnivLocation" scope="request">
		<% empty = false; %>
		<TR>
			<TD valign="middle" colspan="<%=colspan%>">
				&nbsp;<A name="nonUnivLocation"/>
			</TD>
		<TR>
		<TR>
			<bean:write name="nonUnivLocation" scope="request" filter="false"/>
		</TR>
	</logic:notEmpty>

<!-- Buttons -->
<% if (!empty) {%>

	<TR>
		<TD valign="middle" colspan="<%=colspan%>">&nbsp;</TD>
	<TR>

	<TR>
		<TD valign="middle" colspan="<%=colspan%>">
			<tt:section-header>
			<tt:section-title>
				<logic:iterate name="<%=frmName%>" property="roomTypes" id="roomType">
					<bean:define name="roomType" property="reference" id="ref"/>
					<logic:notEmpty name="<%=(String)ref%>" scope="request">
						<A class="l7" href="<%="#"+ref%>"><bean:write name="roomType" property="label"/></A>&nbsp;
					</logic:notEmpty>
				</logic:iterate>
				<logic:notEmpty name="nonUnivLocation" scope="request">
					<A class="l7" href="#nonUnivLocation">Non University Locations</A>&nbsp;
				</logic:notEmpty>
			</tt:section-title>
			</tt:section-header>
		</TD>
	<TR>

	<TR>
		<TD valign="middle" colspan="<%=colspan%>" align="right">
			<TABLE align="right" cellspacing="0" cellpadding="2" class="FormWithNoPadding">
				<TR>
					<logic:equal name="<%=frmName%>" property="deptSize" value="false">
						<TD>
							<html:form action="roomList" styleClass="FormWithNoPadding">			
								<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="P" titleKey="title.exportPDF">
									<bean:message key="button.exportPDF" />
								</html:submit>
							</html:form>
						</TD>
					</logic:equal>
					<logic:equal name="<%=frmName%>" property="canAdd" value="true">
						<TD nowrap>
							<html:form action="editRoom" styleClass="FormWithNoPadding">
								<html:hidden property="op" value="Add"/>
								<html:submit onclick="displayLoading();" styleClass="btn" accesskey="R" titleKey="title.addRoom">
									<bean:message key="button.addRoom" />
								</html:submit>
							</html:form>
						</TD>
					</logic:equal>
					<logic:equal name="<%=frmName%>" property="canAddNonUniv" value="true">
						<TD nowrap>
							<html:form action="addNonUnivLocation" styleClass="FormWithNoPadding">
								<html:submit onclick="displayLoading();" styleClass="btn" accesskey="N" titleKey="title.addNonUnivLocation">
									<bean:message key="button.addNonUnivLocation" />
								</html:submit>
							</html:form>
						</TD>
					</logic:equal>
					<logic:equal name="<%=frmName%>" property="canAddSpecial" value="true">
						<TD nowrap>
							<html:form action="addSpecialUseRoom" styleClass="FormWithNoPadding">
								<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="S" titleKey="title.addSpecialUseRoom">
									<bean:message key="button.addSpecialUseRoom" />
								</html:submit>
							</html:form>
						</TD>
					</logic:equal>
					<%--
					<TD nowrap>
						<html:form action="roomFeatureList" styleClass="FormWithNoPadding">
							<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="F" titleKey="title.roomFeatures" >
								<bean:message key="button.roomFeatures" />
							</html:submit>
						</html:form>
					</TD>
					<TD nowrap>
						<html:form action="roomGroupList" styleClass="FormWithNoPadding">
							<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="G" titleKey="title.roomGroups">
								<bean:message key="button.roomGroups" />
							</html:submit>
						</html:form>
					</TD>
					<TD nowrap>
						<html:form action="roomDeptList" styleClass="FormWithNoPadding">
							<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="D" titleKey="title.roomDepts">
								<bean:message key="button.roomDepts" />
							</html:submit>
						</html:form>
					</TD>
					--%>
					<logic:equal name="<%=frmName%>" property="editRoomSharing" value="true">
						<logic:notEqual name="<%=frmName%>" property="deptCodeX" value="All">
							<TD nowrap>
								<html:form action="roomDeptEdit" styleClass="FormWithNoPadding">
									<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="E" titleKey="title.editRoomSharing">
										<bean:message key="button.editRoomSharing" />
									</html:submit>
								</html:form>
							</TD>
						</logic:notEqual>
					</logic:equal>
				</TR>
			</TABLE>
		</TD>
	<TR>
<%}%>

</TABLE>

<SCRIPT type="text/javascript" language="javascript">
    <% if (request.getAttribute("hash") != null) { %>
  		location.hash = "<%=request.getAttribute("hash")%>";
	<% } %>
</SCRIPT>