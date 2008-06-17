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
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.commons.web.Web" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(Web.getUser(session)) %>
		
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
	<html:hidden property="canDelete" />
	<html:hidden property="canChangeDepartment" />
	
	<TABLE width="95%" border="0" cellspacing="0" cellpadding="3">

		<TR>
			<TD colspan="2">
			
				<tt:section-header>
					<tt:section-title>
						<logic:notEmpty name="subjectAreaEditForm" property="uniqueId">
							<bean:write name="subjectAreaEditForm" property="abbv"/> 
							- 
							<bean:write name="subjectAreaEditForm" property="longTitle"/>
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
						
						<logic:equal name="subjectAreaEditForm" property="canDelete" value="true">
							<html:submit property="op" onclick="return confirmDelete();" styleClass="btn" accesskey="D" titleKey="title.deleteSubjectArea">
								<bean:message key="button.deleteSubjectArea"/>
							</html:submit>
						</logic:equal>
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
			<TD><%= Web.getUser(session).getAttribute(Constants.ACAD_YRTERM_LABEL_ATTR_NAME) %></TD>
		</TR>

		<TR>
			<TD>Abbreviation:</TD>
			<TD>
				<html:text property="abbv" size="10" maxlength="10"/>
			</TD>
		</TR>
		
		<TR>
			<TD>Short Title:</TD>
			<TD>
				<html:text property="shortTitle" size="30" maxlength="50"/>
			</TD>
		</TR>
		
		<TR>
			<TD>Long Title:</TD>
			<TD>
				<html:text property="longTitle" size="40" maxlength="100"/>
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
				<logic:equal name="subjectAreaEditForm" property="canChangeDepartment" value="true">
					<html:select property="department"
						onfocus="setUp();" 
						onkeypress="return selectSearch(event, this);" 
						onkeydown="return checkKey(event, this);" >
						<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
						<html:options collection="<%=Department.DEPT_ATTR_NAME%>" property="uniqueId" labelProperty="label"/>
					</html:select>
				</logic:equal>
				<logic:equal name="subjectAreaEditForm" property="canChangeDepartment" value="false">
					<html:hidden property="department"/>
					<html:select property="department" disabled="true">
						<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
						<html:options collection="<%=Department.DEPT_ATTR_NAME%>" property="uniqueId" labelProperty="label"/>
					</html:select>
				</logic:equal>
			</TD>
		</TR>

		<TR>
			<TD>Schedule Book Only:</TD>
			<TD>
				<html:checkbox property="scheduleBkOnly"/>
			</TD>
		</TR>
		
		<TR>
			<TD>Pseudo:</TD>
			<TD>
				<html:checkbox property="pseudo"/>
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
					
					<logic:equal name="subjectAreaEditForm" property="canDelete" value="true">
						<html:submit property="op" onclick="return confirmDelete();" styleClass="btn" accesskey="D" titleKey="title.deleteSubjectArea">
							<bean:message key="button.deleteSubjectArea"/>
						</html:submit>
					</logic:equal>
				</logic:notEmpty>

				<html:submit property="op" styleClass="btn" accesskey="B" titleKey="title.backToPrevious">
					<bean:message key="button.backToPrevious"/>
				</html:submit>
			</TD>
		</TR>
		
	</TABLE>
</html:form>

	
