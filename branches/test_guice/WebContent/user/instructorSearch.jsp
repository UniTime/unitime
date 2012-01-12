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
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.timetable.form.InstructorSearchForm" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/localization.tld" prefix="loc" %>

<%
	// Get Form 
	String frmName = "instructorSearchForm";	
	InstructorSearchForm frm = (InstructorSearchForm) request.getAttribute(frmName);	
%>
	
<tiles:importAttribute />
<loc:bundle name="CourseMessages">
<html:form action="instructorList">
	<TABLE border="0" cellspacing="0" cellpadding="3">
		<% if (frm.isDisplayDeptList()) {%>
		<TR>
			<TD>
				<B><loc:message name="propertyDepartment"/></B>
				<html:select property="deptUniqueId"
					onchange="displayLoading(); submit()"
					onfocus="setUp();" 
					onkeypress="return selectSearch(event, this);" 
					onkeydown="return checkKey(event, this);" >
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:options collection="<%=Department.DEPT_ATTR_NAME%>" 
						property="value" labelProperty="label"/>
					</html:select>
			</TD>
		
			<TD align="right">			
				&nbsp;&nbsp;&nbsp;
				<html:submit property='op' onclick="displayLoading();" 
						accesskey="<%=MSG.accessSearchInstructors() %>" 
						styleClass="btn" 
						title="<%=MSG.titleSearchInstructors(MSG.accessSearchInstructors()) %>">
					<loc:message name="actionSearchInstructors" />
				</html:submit>
				&nbsp;&nbsp;
				<html:submit property='op' onclick="displayLoading();" 
						accesskey="<%=MSG.accessExportPdf() %>" 
						styleClass="btn" 
						title="<%=MSG.titleExportPdf(MSG.accessExportPdf()) %>">
					<loc:message name="actionExportPdf" />
				</html:submit>
			</TD>
		</TR>
		<% } %>
		
	</TABLE>
</html:form>
</loc:bundle>

<logic:notEmpty name="body2">
	<script language="javascript">displayLoading();</script>
	<tiles:insert attribute="body2" />
	<script language="javascript">displayElement('loading', false);</script>
</logic:notEmpty>
