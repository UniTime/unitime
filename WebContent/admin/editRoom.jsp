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
<%@ page import="org.unitime.timetable.form.EditRoomForm" %>
<%@ page import="org.apache.struts.util.LabelValueBean" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.commons.web.Web" %>
<%@ page import="org.unitime.timetable.model.Roles" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<%
	// Get Form 
	String frmName = "editRoomForm";	
	EditRoomForm frm = (EditRoomForm) request.getAttribute(frmName);
	boolean admin = Web.hasRole(request.getSession(), new String[] { Roles.ADMIN_ROLE});
%>	

<tiles:importAttribute />
<html:form action="/editRoom" focus="name">
	<html:hidden property="id"/>

	<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="U" titleKey="title.updateRoom">
						<bean:message key="button.update" />
					</html:submit>
					&nbsp;
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="B" titleKey="title.returnToRoomList">
						<bean:message key="button.returnToRoomDetail" />
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
			<TD>Name:</TD>
			<TD width='100%'>
				<bean:write name="<%=frmName%>" property="bldgName"/>
				<html:text property="name" maxlength="20" size="20" />
			<TD>
		</TR>
			
		<logic:equal name="<%=frmName%>" property="room" value="true">
			<% if (admin) { %>
				<TR>
					<TD>External Id:</TD>
					<TD width='100%'>
						<html:text property="externalId" maxlength="40" size="40" />
					<TD>
				</TR>
			<% } else { %>
				<html:hidden property="externalId"/>
			<% } %>
		</logic:equal>
		
		<logic:equal name="<%=frmName%>" property="room" value="true">
			<% if (admin) { %>
				<TR>
					<TD>Type:</TD>
					<TD width='100%'>
						<html:select property="type">
							<html:option value="genClassroom">Classroom</html:option>
							<html:option value="computingLab">Computing Laboratory</html:option>
							<html:option value="departmental">Additional Instructional Room</html:option>
							<html:option value="specialUse">Special Use Room</html:option>
						</html:select>
					<TD>
				</TR>
			<% } else { %>
				<html:hidden property="type"/>
			<% } %>
		</logic:equal>

		<TR>
			<TD>Capacity:</TD>
			<TD>
				<html:text property="capacity" maxlength="15" size="10"/>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap>Controlling Department:</TD>
			<TD>
				<logic:equal name="<%=frmName%>" property="owner" value="true">
					<html:select property="controlDept">
						<html:option value="<%=Constants.BLANK_OPTION_VALUE%>">No controlling department</html:option>
						<html:options collection="<%=Department.DEPT_ATTR_NAME%>" property="value" labelProperty="label"/>
					</html:select>
				</logic:equal>
				<logic:equal name="<%=frmName%>" property="owner" value="false">
					<html:hidden property="controlDept"/>
					<logic:iterate scope="request" name="<%=Department.DEPT_ATTR_NAME%>" id="d">
						<logic:equal name="<%=frmName%>" property="controlDept" value="<%=((LabelValueBean)d).getValue()%>">
							<bean:write name="d" property="label"/>
						</logic:equal>
					</logic:iterate>
				</logic:equal>
			</TD>
		</TR>
			
		<logic:equal name="<%=frmName%>" property="room" value="true">
			<TR>
				<TD>Coordinates:</TD>
				<TD>
					<html:text property="coordX" maxlength="5" size="5"/>, <html:text property="coordY" maxlength="5" size="5"/>
				</TD>
			</TR>
		</logic:equal>
		<logic:equal name="<%=frmName%>" property="room" value="false">
			<html:hidden property="coordX" />
			<html:hidden property="coordY" />
		</logic:equal>

		<logic:equal name="<%=frmName%>" property="room" value="false">
			<TR>
				<TD nowrap>Ignore Too Far Distances:</TD>
				<TD>
					<html:checkbox property="ignoreTooFar" />
				</TD>
			</TR>
		</logic:equal>
		<logic:equal name="<%=frmName%>" property="room" value="false">
			<html:hidden property="ignoreTooFar" />
		</logic:equal>
			
		<TR>
			<TD nowrap>Ignore Room Checks:</TD>
			<TD>
				<html:checkbox property="ignoreRoomCheck" />
			</TD>
		</TR>
		
		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>
			
		<TR>
			<TD colspan='2' align='right'>
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="U" titleKey="title.updateRoom">
						<bean:message key="button.update" />
					</html:submit>
					&nbsp;
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="B" titleKey="title.returnToRoomList">
						<bean:message key="button.returnToRoomDetail" />
					</html:submit>
			</TD>
		</TR>
	</TABLE>
	
</html:form>

<script type="text/javascript" language="javascript">
	// Validator
	var frmvalidator  = new Validator("editRoomForm");
	frmvalidator.addValidation("capacity","numeric");	
</script>