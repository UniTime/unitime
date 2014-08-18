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
<%@ page import="org.unitime.timetable.form.RoomListForm" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<%
	// Get Form
	String frmName = "roomListForm";
	RoomListForm frm = (RoomListForm) request.getAttribute(frmName);
%>

<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<bean:define id="colspan" name="colspan" scope="request"/>

<!-- Buttons -->
	<TR>
		<TD valign="middle" colspan="<%=colspan%>">
		<tt:section-header>
			<tt:section-title>
				<span style='font-weight:normal;'>
					<logic:iterate name="<%=frmName%>" property="allRoomTypes" id="roomType">
						<bean:define name="roomType" property="reference" id="ref"/>
						<logic:notEmpty name="<%=(String)ref%>" scope="request">
							<A class="l7" href='<%="#"+ref%>'><bean:write name="roomType" property="label"/></A>&nbsp;
						</logic:notEmpty>
					</logic:iterate>
				</span>
			</tt:section-title>
			<TABLE align="right" cellspacing="0" cellpadding="2" class="FormWithNoPadding">
				<TR>
					<sec:authorize access="((#roomListForm.deptCodeX == 'All' or #roomListForm.deptCodeX matches 'Exam[0-9]*') and hasPermission(null, 'Department', 'AddRoom')) or hasPermission(#roomListForm.deptCodeX, 'Department', 'AddRoom')">
						<TD nowrap>
							<html:form action="editRoom" styleClass="FormWithNoPadding">
								<html:hidden property="op" value="Add"/>
								<html:submit onclick="displayLoading();" styleClass="btn" accesskey="R" titleKey="title.addRoom">
									<bean:message key="button.addRoom" />
								</html:submit>
							</html:form>
						</TD>
					</sec:authorize>
					<sec:authorize access="((#roomListForm.deptCodeX == 'All' or #roomListForm.deptCodeX matches 'Exam[0-9]*') and hasPermission(null, 'Department', 'AddNonUnivLocation')) or hasPermission(#roomListForm.deptCodeX, 'Department', 'AddNonUnivLocation')">
						<TD nowrap>
							<html:form action="addNonUnivLocation" styleClass="FormWithNoPadding">
								<html:submit onclick="displayLoading();" styleClass="btn" accesskey="N" titleKey="title.addNonUnivLocation">
									<bean:message key="button.addNonUnivLocation" />
								</html:submit>
							</html:form>
						</TD>
					</sec:authorize>
					<sec:authorize access="((#roomListForm.deptCodeX == 'All' or #roomListForm.deptCodeX matches 'Exam[0-9]*') and hasPermission(null, 'Department', 'AddSpecialUseRoom')) or hasPermission(#roomListForm.deptCodeX, 'Department', 'AddSpecialUseRoom')">
						<TD nowrap>
							<html:form action="addSpecialUseRoom" styleClass="FormWithNoPadding">
								<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="U" titleKey="title.addSpecialUseRoom">
									<bean:message key="button.addSpecialUseRoom" />
								</html:submit>
							</html:form>
						</TD>
					</sec:authorize>
					<sec:authorize access="hasPermission(#roomListForm.deptCodeX, 'Department', 'EditRoomDepartments')">
						<TD nowrap>
							<html:form action="roomDeptEdit" styleClass="FormWithNoPadding">
								<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="E" titleKey="title.editRoomSharing">
									<bean:message key="button.editRoomSharing" />
								</html:submit>
							</html:form>
						</TD>
					</sec:authorize>
					<sec:authorize access="#roomListForm.deptCodeX matches 'Exam[0-9]*' and hasPermission(null, 'Session', 'EditRoomDepartmentsExams')">
						<TD nowrap>
							<html:form action="roomDeptEdit" styleClass="FormWithNoPadding">
								<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="E" titleKey="title.editRoomSharing">
									<bean:message key="button.editRoomSharing" />
								</html:submit>
							</html:form>
						</TD>
					</sec:authorize>
				</TR>
			</TABLE>
		</tt:section-header>
		</TD>
	</TR>

<!-- rooms -->
	<% boolean empty = true; %>
	<logic:iterate name="<%=frmName%>" property="allRoomTypes" id="roomType">
		<bean:define name="roomType" property="reference" id="ref"/>
		<logic:notEmpty name="<%=(String)ref%>" scope="request">
			<% empty = false; %>
			<TR>
				<TD valign="middle" colspan="<%=colspan%>">
					&nbsp;<A name="<%=(String)ref%>"></A>
				</TD>
			</TR>
			<bean:write name="<%=(String)ref%>" scope="request" filter="false"/>
		</logic:notEmpty>
	</logic:iterate>

<!-- Buttons -->
<% if (!empty) {%>

	<TR>
		<TD valign="middle" colspan="<%=colspan%>">&nbsp;</TD>
	</TR>

	<TR>
		<TD valign="middle" colspan="<%=colspan%>">
			<tt:section-header>
			<tt:section-title>
				<span style='font-weight:normal;'>
				<logic:iterate name="<%=frmName%>" property="allRoomTypes" id="roomType">
					<bean:define name="roomType" property="reference" id="ref"/>
					<logic:notEmpty name="<%=(String)ref%>" scope="request">
						<A class="l7" href='<%="#"+ref%>'><bean:write name="roomType" property="label"/></A>&nbsp;
					</logic:notEmpty>
				</logic:iterate>
				</span>
			</tt:section-title>
			</tt:section-header>
		</TD>
	</TR>

	<TR>
		<TD valign="middle" colspan="<%=colspan%>" align="right">
			<TABLE align="right" cellspacing="0" cellpadding="2" class="FormWithNoPadding">
				<TR>
					<sec:authorize access="((#roomListForm.deptCodeX == 'All' or #roomListForm.deptCodeX matches 'Exam[0-9]*') and hasPermission(null, 'Department', 'AddRoom')) or hasPermission(#roomListForm.deptCodeX, 'Department', 'AddRoom')">
						<TD nowrap>
							<html:form action="editRoom" styleClass="FormWithNoPadding">
								<html:hidden property="op" value="Add"/>
								<html:submit onclick="displayLoading();" styleClass="btn" accesskey="R" titleKey="title.addRoom">
									<bean:message key="button.addRoom" />
								</html:submit>
							</html:form>
						</TD>
					</sec:authorize>
					<sec:authorize access="((#roomListForm.deptCodeX == 'All' or #roomListForm.deptCodeX matches 'Exam[0-9]*') and hasPermission(null, 'Department', 'AddNonUnivLocation')) or hasPermission(#roomListForm.deptCodeX, 'Department', 'AddNonUnivLocation')">
						<TD nowrap>
							<html:form action="addNonUnivLocation" styleClass="FormWithNoPadding">
								<html:submit onclick="displayLoading();" styleClass="btn" accesskey="N" titleKey="title.addNonUnivLocation">
									<bean:message key="button.addNonUnivLocation" />
								</html:submit>
							</html:form>
						</TD>
					</sec:authorize>
					<sec:authorize access="((#roomListForm.deptCodeX == 'All' or #roomListForm.deptCodeX matches 'Exam[0-9]*') and hasPermission(null, 'Department', 'AddSpecialUseRoom')) or hasPermission(#roomListForm.deptCodeX, 'Department', 'AddSpecialUseRoom')">
						<TD nowrap>
							<html:form action="addSpecialUseRoom" styleClass="FormWithNoPadding">
								<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="S" titleKey="title.addSpecialUseRoom">
									<bean:message key="button.addSpecialUseRoom" />
								</html:submit>
							</html:form>
						</TD>
					</sec:authorize>
					<sec:authorize access="hasPermission(#roomListForm.deptCodeX, 'Department', 'EditRoomDepartments')">
						<TD nowrap>
							<html:form action="roomDeptEdit" styleClass="FormWithNoPadding">
								<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="E" titleKey="title.editRoomSharing">
									<bean:message key="button.editRoomSharing" />
								</html:submit>
							</html:form>
						</TD>
					</sec:authorize>
					<sec:authorize access="#roomListForm.deptCodeX matches 'Exam[0-9]*' and hasPermission(null, 'Session', 'EditRoomDepartmentsExams')">
						<TD nowrap>
							<html:form action="roomDeptEdit" styleClass="FormWithNoPadding">
								<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="E" titleKey="title.editRoomSharing">
									<bean:message key="button.editRoomSharing" />
								</html:submit>
							</html:form>
						</TD>
					</sec:authorize>
				</TR>
			</TABLE>
		</TD>
	</TR>
<%}%>

</TABLE>
<SCRIPT type="text/javascript" language="javascript">
    <% if (request.getAttribute("hash") != null) { %>
  		location.hash = "<%=request.getAttribute("hash")%>";
	<% } %>
</SCRIPT>
