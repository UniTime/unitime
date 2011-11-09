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
<%@ page import="org.unitime.commons.web.Web" %>
<%@ page import="org.unitime.timetable.model.Roles" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ taglib uri="/WEB-INF/tld/localization.tld" prefix="loc" %>

<%
	boolean flag = true;
	if(Web.hasRole(request.getSession(), new String[] { Roles.ADMIN_ROLE})) 
		flag = false;
%>

<loc:bundle name="CourseMessages">
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan="11">
					
				<tt:section-header>
					<tt:section-title>
						<loc:message name="sectionTitleInstructorList"/>
					</tt:section-title>						
					<logic:equal name="instructorSearchForm" property="editable" value="true">
						<TABLE border="0" cellspacing="1" cellpadding="0" align="right">
						<TR>
							<logic:equal name="instructorSearchForm" property="displayDeptList" value="false">
								<TD>
									<html:form action="instructorList" styleClass="FormWithNoPadding">			
										<html:submit property="op" onclick="displayLoading();" styleClass="btn" 
												accesskey="<%=MSG.accessExportPdf() %>" 
												title="<%=MSG.titleExportPdf(MSG.accessExportPdf()) %>">
											<loc:message name="actionExportPdf" />
										</html:submit>
									</html:form>
								</TD>
							</logic:equal>
							<TD>
								<html:form action="instructorListUpdate" styleClass="FormWithNoPadding">			
									<html:submit onclick="displayLoading();" styleClass="btn" 
											accesskey="<%=MSG.accessManageInstructorList() %>" 
											title="<%=MSG.titleManageInstructorList(MSG.accessManageInstructorList()) %>">
										<loc:message name="actionManageInstructorList" />
									</html:submit>
								</html:form>
							</TD>
							<TD>
								<html:form action="instructorAdd" styleClass="FormWithNoPadding">			
									<html:submit onclick="displayLoading();" styleClass="btn" 
											accesskey="<%=MSG.accessAddNewInstructor() %>" 
											title="<%=MSG.titleAddNewInstructor(MSG.accessAddNewInstructor()) %>">
										<loc:message name="actionAddNewInstructor" />
									</html:submit>
								</html:form>
							</TD>
						</TR>
						</TABLE>
					</logic:equal>
				</tt:section-header>					
			</TD>
		</TR>		

		<logic:messagesPresent>
		<TR>
			<TD colspan="2" align="left" class="errorCell">
			    <html:messages id="error">
					${error}
			    </html:messages>
			</TD>
		</TR>
		</logic:messagesPresent>
		
	<% if (request.getAttribute("instructorList") != null) {%>
	<TR>
		<%=request.getAttribute("instructorList")%>
	</TR>
	<%}%>
	
	<TR>		
		<TD colspan="11" align="right" class="WelcomeRowHead">
		&nbsp;
		</TD>
	</TR>

	<logic:equal name="instructorSearchForm" property="editable" value="true">
		<TR>		
			<TD colspan="11" align="right">
				<TABLE border="0" cellspacing="1" cellpadding="0" align="right">
				<TR>
					<logic:equal name="instructorSearchForm" property="displayDeptList" value="false">
						<TD>
							<html:form action="instructorList" styleClass="FormWithNoPadding">			
								<html:submit property="op" onclick="displayLoading();" 
										styleClass="btn" 
										accesskey="<%=MSG.accessExportPdf() %>" 
										title="<%=MSG.titleExportPdf(MSG.accessExportPdf()) %>">
									<loc:message name="actionExportPdf" />
								</html:submit>
							</html:form>
						</TD>
					</logic:equal>
					<TD>
						<html:form action="instructorListUpdate" styleClass="FormWithNoPadding">			
							<html:submit onclick="displayLoading();" styleClass="btn" 
									accesskey="<%=MSG.accessManageInstructorList() %>" 
									title="<%=MSG.titleManageInstructorList(MSG.accessManageInstructorList()) %>">
								<loc:message name="actionManageInstructorList" />
							</html:submit>
						</html:form>
					</TD>
					<TD>
						<html:form action="instructorAdd" styleClass="FormWithNoPadding">			
							<html:submit onclick="displayLoading();" styleClass="btn" 
									accesskey="<%=MSG.accessAddNewInstructor() %>" 
									title="<%=MSG.titleAddNewInstructor(MSG.accessAddNewInstructor()) %>">
								<loc:message name="actionAddNewInstructor" />
							</html:submit>
						</html:form>
					</TD>
				</TR>
				</TABLE>
			</TD>
		</TR>
	</logic:equal>
	
</TABLE>
</loc:bundle>

