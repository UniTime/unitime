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
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%> 
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<tt:session-context/>
<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(sessionContext) %>
		
		function confirmDelete() {
			if (jsConfirm!=null && !jsConfirm)
				return true;

			if(confirm('The department and all associated data will be deleted. Continue?')) {
				return true;
			}
			return false;
		}

	// -->
</SCRIPT>

<html:form action="/departmentEdit">
	<html:hidden property="id"/>
	<html:hidden property="sessionId"/>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">

		<TR>
			<TD colspan="2">
			
				<tt:section-header>
					<tt:section-title>
						<logic:notEmpty name="departmentEditForm" property="id">
							<bean:write name="departmentEditForm" property="deptCode"/> 
							- 
							<bean:write name="departmentEditForm" property="name"/>
						</logic:notEmpty>
					</tt:section-title>
						
					<logic:empty name="departmentEditForm" property="id">
						<html:submit property="op" styleClass="btn" accesskey="S" titleKey="title.saveDepartment">
							<bean:message key="button.saveDepartment"/>
						</html:submit>
					</logic:empty>

					<logic:notEmpty name="departmentEditForm" property="id">
						<html:submit property="op" styleClass="btn" accesskey="U" titleKey="title.updateDepartment">
							<bean:message key="button.updateDepartment"/>
						</html:submit>
						<sec:authorize access="hasPermission(#departmentEditForm.id, 'Department', 'DepartmentDelete')">
							<html:submit property="op" onclick="return confirmDelete();" styleClass="btn" accesskey="D" titleKey="title.deleteDepartment">
								<bean:message key="button.deleteDepartment"/>
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
			<TD width='10%'>Code:</TD>
			<TD>
				<html:text property="deptCode" size="50" maxlength="50"/>
			</TD>
		</TR>
		
		<TR>
			<TD>Abbreviation:</TD>
			<TD>
				<html:text property="abbv" size="20" maxlength="20"/>
			</TD>
		</TR>

		<TR>
			<TD>Name:</TD>
			<TD>
				<html:text property="name" size="100" maxlength="100"/>
			</TD>
		</TR>

		<TR>
			<TD nowrap>Department Status:</TD>
			<TD>
				<html:select property="statusType">
					<html:option value="">Session Default</html:option>
					<html:optionsCollection property="statusOptions" value="reference" label="label" />
				</html:select>
			</TD>
		</TR>
		
		<TR>
			<TD>External ID:</TD>
			<TD>
				<html:text property="externalId" size="40" maxlength="40"/>
			</TD>
		</TR>
		<TR>
			<TD nowrap>External Manager:</TD>
			<TD>
				<logic:empty name="departmentEditForm" property="id">
					<html:checkbox property="isExternal"/>
				</logic:empty>
				<logic:notEmpty name="departmentEditForm" property="id">
					<sec:authorize access="hasPermission(#departmentEditForm.id, 'Department', 'DepartmentEditChangeExternalManager')">
						<html:checkbox property="isExternal"/>
					</sec:authorize>
					<sec:authorize access="!hasPermission(#departmentEditForm.id, 'Department', 'DepartmentEditChangeExternalManager')">
						<html:checkbox property="isExternal" disabled="true"/>
						<html:hidden property="isExternal"/>
					</sec:authorize>
				</logic:notEmpty>
			</TD>
		</TR>

		<TR>
			<TD nowrap>External Manager Abbreviation:</TD>
			<TD>
				<html:text property="extAbbv" size="10" maxlength="10"/>
			</TD>
		</TR>

		<TR>
			<TD nowrap>External Manager Name:</TD>
			<TD>
				<html:text property="extName" size="30" maxlength="30"/>
			</TD>
		</TR>

		<TR>
			<TD nowrap>Distribution Preference Priority:</TD>
			<TD>
				<html:text property="distPrefPriority" size="10" maxlength="5"/>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap>Allow Required Time:</TD>
			<TD>
				<html:checkbox property="allowReqTime"/>
			</TD>
		</TR>

		<TR>
			<TD nowrap>Allow Required Room:</TD>
			<TD>
				<html:checkbox property="allowReqRoom"/>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap>Allow Required Distribution:</TD>
			<TD>
				<html:checkbox property="allowReqDist"/>
			</TD>
		</TR>
		
		<TR>
			<TD nowrap>Event Management:</TD>
			<TD>
				<html:checkbox property="allowEvents"/>
			</TD>
		</TR>
		
		<TR>
			<TD colspan="2">
			<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
			</TD>
		</TR>
		
		<TR>
			<TD align="right" colspan='2'>
				<logic:empty name="departmentEditForm" property="id">
					<html:submit property="op" styleClass="btn" accesskey="S" titleKey="title.saveDepartment">
						<bean:message key="button.saveDepartment"/>
					</html:submit>
				</logic:empty>

				<logic:notEmpty name="departmentEditForm" property="id">
					<html:submit property="op" styleClass="btn" accesskey="U" titleKey="title.updateDepartment">
						<bean:message key="button.updateDepartment"/>
					</html:submit>
					<sec:authorize access="hasPermission(#departmentEditForm.id, 'Department', 'DepartmentDelete')">
						<html:submit property="op" onclick="return confirmDelete();" styleClass="btn" accesskey="D" titleKey="title.deleteDepartment">
							<bean:message key="button.deleteDepartment"/>
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
