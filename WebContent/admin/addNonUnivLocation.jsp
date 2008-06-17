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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.commons.web.Web" %>
<%@ page import="org.unitime.timetable.model.Roles" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.form.NonUnivLocationForm" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<%		
	boolean flag = true;
	if(Web.hasRole(request.getSession(), new String[] { Roles.ADMIN_ROLE})) 
		flag = false;
			
	// Get Form 
	String frmName = "nonUnivLocationForm";	
	NonUnivLocationForm frm = (NonUnivLocationForm) request.getAttribute(frmName);
%>	

<tiles:importAttribute />
<html:form action="/addNonUnivLocation" focus="name">
	
	<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header title="Add Non University Location">
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="A" 
							title="Add Non University Location (Alt+A)">
						<bean:message key="button.addNew" />
					</html:submit>
					&nbsp;
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="B" 
						title="Back to Room List (Alt+B)">
						<bean:message key="button.returnToRoomList"/>
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
					<html:text property="name" maxlength="9" size="10" />
				<TD>
			</TR>
			
			<TR>
				<TD>Capacity:</TD>
				<TD>
					<html:text property="capacity" maxlength="15" size="10"/>
				</TD>
			</TR>
			
			<TR>
				<TD nowrap>Ignore Too Far Distances:</TD>
				<TD>
					<html:checkbox property="ignoreTooFar" />
				</TD>
			</TR>
			
			<TR>
				<TD nowrap>Ignore Room Checks:</TD>
				<TD>
					<html:checkbox property="ignoreRoomCheck" />
				</TD>
			</TR>

			<TR>
				<TD>Department:</TD>
				<TD>
				<% if (frm.getDeptSize() == 1) {%>
					<%=frm.getDeptName(frm.getDeptCode(), request)%>
					<html:hidden property="deptCode" />
				<% } else { %>
					<html:select property="deptCode">
						<logic:empty name="<%=frmName%>" property="deptCode">
							<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
						</logic:empty>
						<logic:equal name="<%=frmName%>" property="deptCode" value="<%=Constants.ALL_OPTION_LABEL%>">
							<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
						</logic:equal>
						<html:options collection="<%=Department.DEPT_ATTR_NAME%>" property="value" labelProperty="label"/>
					</html:select>
				<%}%>
				</TD>
			</TR>
			
		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>
		
		<TR>
			<TD colspan='2' align='right'>
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="A" 
							title="Add Non University Location (Alt+A)">
						<bean:message key="button.addNew" />
					</html:submit>
					&nbsp;
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="B" 
						title="Back to Room List (Alt+B)">
						<bean:message key="button.returnToRoomList"/>
					</html:submit>
			</TD>
		</TR>
	</TABLE>
</html:form>

<script type="text/javascript" language="javascript">
	// Validator
	var frmvalidator  = new Validator("nonUnivLocationForm");
	frmvalidator.addValidation("capacity","numeric");	
</script>
