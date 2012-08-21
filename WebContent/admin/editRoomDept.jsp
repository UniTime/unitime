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
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.timetable.form.EditRoomDeptForm" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<%
	// Get Form 
	String frmName = "editRoomDeptForm";	
	EditRoomDeptForm frm = (EditRoomDeptForm) request.getAttribute(frmName);
%>	
<tt:session-context/>
<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(sessionContext) %>
	// -->
</SCRIPT>

<tiles:importAttribute />
<html:form action="/editRoomDept">
	<html:hidden property="id"/>
	<html:hidden property="departments"/>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title><%=frm.getName()%></tt:section-title>
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="U" title="Update and Return to Room Detail Page (Alt+U)">
						<bean:message key="button.update" />
					</html:submit>
					&nbsp;
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="B" title="Return to Room Detail Page (Alt+B)">
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
			<TD colspan='2'>
				<bean:write name="<%=frmName%>" property="sharingTable" filter="false"/>
			</TD>
		</TR>
		
		
		<%--
		<logic:equal name="<%=frmName%>" property="nonUniv" value="false">
		--%>
			<TR>
				<TD colspan='2' align='right'>
					<html:select property="dept">
						<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
						<html:options collection="<%=Department.DEPT_ATTR_NAME%>" property="value" labelProperty="label"/>
					</html:select>
					&nbsp;
					<html:submit property="doit" 
						accesskey="A" styleClass="btn" titleKey="title.addRoomDept" onclick="displayLoading();">
						<bean:message key="button.addRoomDept" />
					</html:submit>
					<html:submit property="doit" 
						accesskey="R" styleClass="btn" titleKey="title.removeRoomDept" onclick="displayLoading();">
						<bean:message key="button.removeRoomDept" />
					</html:submit>
				</TD>
			</TR>
		<%--
		</logic:equal>
		--%>
			
		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>
			
		<TR>
			<TD colspan='2' align='right'>
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="U" title="Update and Return to Room Detail Page (Alt+U)">
						<bean:message key="button.update" />
					</html:submit>
					&nbsp;
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="B" title="Return to Room Detail Page (Alt+B)">
						<bean:message key="button.returnToRoomDetail" />
					</html:submit>
			</TD>
		</TR>
	</TABLE>
</html:form>
