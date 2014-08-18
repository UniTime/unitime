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
<%@ page import="org.unitime.timetable.form.RoomGroupEditForm" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
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
<html:form action="/roomGroupAdd" focus="name">
	<html:hidden property="id"/>
	<html:hidden property="sessionId"/>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan='2'>
			<tt:section-header>
				<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="A" 
						title="Add New Room Group (Alt+A)">
					<bean:message key="button.addNew" />
				</html:submit>
				<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="R" 
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

		<sec:authorize access="hasPermission(null, 'Session', 'GlobalRoomGroupAdd') and hasPermission(null, 'Department', 'DepartmentRoomGroupAdd')">
			<TR>
				<TD>Global:</TD>
				<TD>
					<html:checkbox property="global" onclick="document.getElementById('department').style.display = (this.checked ? 'none' : 'table-row');"/>
				</TD>
			</TR>
			<TR id="department" <%=(frm.isGlobal() ? "style='display:none;'" : "")%>>
				<TD>Department:</TD>
				<TD>
					<html:select property="deptCode">
						<logic:empty name="<%=frmName%>" property="deptCode">
							<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
						</logic:empty>
						<html:options collection="<%=Department.DEPT_ATTR_NAME%>" property="value" labelProperty="label"/>
					</html:select>
				</TD>
			</TR>
		</sec:authorize>
		<sec:authorize access="!hasPermission(null, 'Session', 'GlobalRoomGroupAdd') or !hasPermission(null, 'Department', 'DepartmentRoomGroupAdd')">
			<TR>
				<TD>Global:</TD>
				<TD>
					<html:checkbox property="global" disabled="true"/>
					<html:hidden property="global" />
				</TD>
			</TR>
			<logic:equal name="<%=frmName%>" property="global" value="false">
				<TR>
					<TD>Department:</TD>
					<TD>
						<html:select property="deptCode">
							<logic:empty name="<%=frmName%>" property="deptCode">
								<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
							</logic:empty>
							<html:options collection="<%=Department.DEPT_ATTR_NAME%>" property="value" labelProperty="label"/>
						</html:select>
					</TD>
				</TR>
			</logic:equal>
		</sec:authorize>
		
		<TR>
			<TD>Description:</TD>
			<TD>
				<html:textarea property="desc" rows="4" cols="50" />
			</TD>
		</TR>

		<TR>
			<TD valign="middle" colspan="2">
				<tt:section-title/>
			</TD>
		</TR>
			
		<TR>
			<TD colspan='2' align='right'>
				<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="A" 
						title="Add New Room Group (Alt+A)">
					<bean:message key="button.addNew" />
				</html:submit>
				<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="R" 
					title="Back to Room Groups (Alt+B)">
					<bean:message key="button.returnToRoomGroupList"/>
				</html:submit>
			</TD>
		</TR>
	</TABLE>

</html:form>
