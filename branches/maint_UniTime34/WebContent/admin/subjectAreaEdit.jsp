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
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<tt:session-context/>
<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(sessionContext) %>
		
		function confirmDelete() {
			if (jsConfirm!=null && !jsConfirm)
				return true;

			if(confirm('The subject area and all associated data will be deleted. Continue?')) {
				return true;
			}
			return false;
		}

	// -->
</SCRIPT>
<tiles:importAttribute />

<html:form method="post" action="subjectAreaEdit.do">
<html:hidden name="subjectAreaEditForm" property="uniqueId" />
	
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">

		<TR>
			<TD colspan="2">
			
				<tt:section-header>
					<tt:section-title>
						<logic:notEmpty name="subjectAreaEditForm" property="uniqueId">
							<bean:write name="subjectAreaEditForm" property="abbv"/> 
							- 
							<bean:write name="subjectAreaEditForm" property="title"/>
						</logic:notEmpty>
					</tt:section-title>
						
					<logic:empty name="subjectAreaEditForm" property="uniqueId">
						<html:submit property="op" styleClass="btn" accesskey="S" titleKey="title.saveSubjectArea">
							<bean:message key="button.saveSubjectArea"/>
						</html:submit>
					</logic:empty>
						
					<logic:notEmpty name="subjectAreaEditForm" property="uniqueId">
						<html:submit property="op" styleClass="btn" accesskey="U" titleKey="title.updateSubjectArea">
							<bean:message key="button.updateSubjectArea"/>
						</html:submit>
						
						<sec:authorize access="hasPermission(#subjectAreaEditForm.uniqueId, 'SubjectArea', 'SubjectAreaDelete')">
							<html:submit property="op" onclick="return confirmDelete();" styleClass="btn" accesskey="D" titleKey="title.deleteSubjectArea">
								<bean:message key="button.deleteSubjectArea"/>
							</html:submit>
						</sec:authorize>
					</logic:notEmpty>

					<html:submit property="op" styleClass="btn" accesskey="B" titleKey="title.backToPrevious">
						<bean:message key="button.backToPrevious"/>
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
			<TD>Academic Session: </TD>
			<TD><%= sessionContext.getUser().getCurrentAuthority().getQualifiers("Session").get(0).getQualifierLabel() %></TD>
		</TR>

		<TR>
			<TD>Abbreviation:</TD>
			<TD>
				<html:text property="abbv" size="10" maxlength="10"/>
			</TD>
		</TR>
		
		<TR>
			<TD>Title:</TD>
			<TD>
				<html:text property="title" size="40" maxlength="100"/>
			</TD>
		</TR>
		
		<TR>
			<TD>External ID:</TD>
			<TD>
				<html:text property="externalId" size="40" maxlength="40"/>
			</TD>
		</TR>
		
		<TR>
			<TD>Department:</TD>
			<TD>
				<logic:empty name="subjectAreaEditForm" property="uniqueId">
					<html:select property="department"
						onfocus="setUp();" 
						onkeypress="return selectSearch(event, this);" 
						onkeydown="return checkKey(event, this);" >
						<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
						<html:options collection="<%=Department.DEPT_ATTR_NAME%>" property="uniqueId" labelProperty="label"/>
					</html:select>					
				</logic:empty>
				<logic:notEmpty name="subjectAreaEditForm" property="uniqueId">
					<sec:authorize access="hasPermission(#subjectAreaEditForm.uniqueId, 'SubjectArea', 'SubjectAreaChangeDepartment')">
						<html:select property="department"
							onfocus="setUp();" 
							onkeypress="return selectSearch(event, this);" 
							onkeydown="return checkKey(event, this);" >
							<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
							<html:options collection="<%=Department.DEPT_ATTR_NAME%>" property="uniqueId" labelProperty="label"/>
						</html:select>
					</sec:authorize>
					<sec:authorize access="!hasPermission(#subjectAreaEditForm.uniqueId, 'SubjectArea', 'SubjectAreaChangeDepartment')">
						<html:hidden property="department"/>
						<html:select property="department" disabled="true">
							<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
							<html:options collection="<%=Department.DEPT_ATTR_NAME%>" property="uniqueId" labelProperty="label"/>
						</html:select>
					</sec:authorize>
				</logic:notEmpty>
			</TD>
		</TR>

		<TR>
			<TD colspan="2">
			<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
			</TD>
		</TR>
		
		<TR>
			<TD align="right" colspan='2'>
				<logic:empty name="subjectAreaEditForm" property="uniqueId">
					<html:submit property="op" styleClass="btn" accesskey="S" titleKey="title.saveSubjectArea">
						<bean:message key="button.saveSubjectArea"/>
					</html:submit>
				</logic:empty>
					
				<logic:notEmpty name="subjectAreaEditForm" property="uniqueId">
					<html:submit property="op" styleClass="btn" accesskey="U" titleKey="title.updateSubjectArea">
						<bean:message key="button.updateSubjectArea"/>
					</html:submit>
					
					<sec:authorize access="hasPermission(#subjectAreaEditForm.uniqueId, 'SubjectArea', 'SubjectAreaDelete')">
						<html:submit property="op" onclick="return confirmDelete();" styleClass="btn" accesskey="D" titleKey="title.deleteSubjectArea">
							<bean:message key="button.deleteSubjectArea"/>
						</html:submit>
					</sec:authorize>
				</logic:notEmpty>

				<html:submit property="op" styleClass="btn" accesskey="B" titleKey="title.backToPrevious">
					<bean:message key="button.backToPrevious"/>
				</html:submit>
			</TD>
		</TR>
		
	</TABLE>
</html:form>

	
