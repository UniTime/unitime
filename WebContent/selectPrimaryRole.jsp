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
<%@ page language="java" autoFlush="true" errorPage="error.jsp" %>
<%@ page import="org.unitime.timetable.model.Roles"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%> 
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>

	<html:form action="selectPrimaryRole.do" target="_top">
	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD>
				<DIV class="WelcomeRowHead">Select Session / Role</DIV>
			</TD>
		</TR>

		<TR>
			<TD>
				&nbsp;<BR>
				<% 
					String list = request.getParameter("list");
				   	if (list==null || !list.equals("Y")) 
						out.println("A default role and/or academic session could not be assigned."); 
				   	
				   	java.util.Vector v = (java.util.Vector) request.getAttribute(Roles.USER_ROLES_ATTR_NAME);
				   	if (v!=null && v.size()>0 ) 
						out.println("Please select one of the academic session / role combinations below to proceed.<BR>&nbsp;");
					else
						out.println(
							"<BR>&nbsp;<BR>&nbsp;<BR>&nbsp;<BR>" +
							"<div align=center class=errorCell>" +
							" 	<font class=errorMessage>" +
							"   <b>Reason</b>: There are no academic sessions that can be edited by your user role.</font>" +
							"</div><BR>&nbsp;");
				%>
				
			</TD>
		</TR>

		<%
			String oldRole = "";
		%>
		<TR>
			<TD>
			<TABLE border="0" cellspacing="0" cellpadding="1">
			<logic:iterate scope="request" name="<%=Roles.USER_ROLES_ATTR_NAME%>" id="userRole">
	            	<%
		
					String roleToken = userRole.toString();
		            int indx = roleToken.indexOf("-");
		            String acadYearTerm = roleToken.substring(0, indx);
		            String currentRole = roleToken.substring(indx+1);
	
	            	if (!oldRole.equals(currentRole)) {
	            	
	            		String currentRoleLabel = currentRole;
	            			
			            out.print ("<TR><TD>&nbsp;<B><U>" + currentRoleLabel + "</U></B></TD><TD>&nbsp;</TD></TR>  ");
			            oldRole = currentRole;
		            }
		            
	            	%>
	            	<TR>
	            		<TD>&nbsp;</TD>
	            		<TD>	            	
			            	<html:radio property="primaryRole" value="<%=userRole.toString()%>" /> 
			            	<%= acadYearTerm %>
			            </TD>
			</logic:iterate>
			</TABLE>
			</TD>
		</TR>
		
		<TR>
			<TD>
				<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
			</TD>
		</TR>

		<%	if (v!=null && v.size()>0 ) { %>
		<TR>
			<TD align="right">
				<html:hidden property="action" value="selectRole" />
				<html:submit accesskey="A" titleKey="title.applySessionRole" styleClass="btn">
					<bean:message key="button.applySessionRole" />
				</html:submit>
			</TD>
		</TR>		
		<% } %>
		
		<TR>
			<TD>
				&nbsp;<BR><html:errors /><BR>&nbsp;
			</TD>
		</TR>
	</TABLE>
	</html:form>

