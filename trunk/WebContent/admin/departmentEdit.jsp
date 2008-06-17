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
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.commons.web.Web" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%> 
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(Web.getUser(session)) %>
		
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
	<html:hidden property="canDelete"/>
	<html:hidden property="canChangeExternalManagement"/>

	<TABLE width="95%" border="0" cellspacing="0" cellpadding="3">

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
						<logic:equal name="departmentEditForm" property="canDelete" value="true">
							<html:submit property="op" onclick="return confirmDelete();" styleClass="btn" accesskey="D" titleKey="title.deleteDepartment">
								<bean:message key="button.deleteDepartment"/>
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
			<TD width='10%'>Number:</TD>
			<TD>
				<html:text property="deptCode" size="4" maxlength="4"/>
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
				<html:text property="name" size="50" maxlength="50"/>
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
				<html:text property="externalId" size="50" maxlength="50"/>
			</TD>
		</TR>
		<TR>
			<TD nowrap>External Manager:</TD>
			<TD>
				<logic:equal name="departmentEditForm" property="canChangeExternalManagement" value="true">
					<html:checkbox property="isExternal"/>
				</logic:equal>
				<logic:equal name="departmentEditForm" property="canChangeExternalManagement" value="false">
					<html:checkbox property="isExternal" disabled="true"/>
					<html:hidden property="isExternal"/>
				</logic:equal>
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
				<html:text property="extName" size="50" maxlength="50"/>
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
					<logic:equal name="departmentEditForm" property="canDelete" value="true">
						<html:submit property="op" onclick="return confirmDelete();" styleClass="btn" accesskey="D" titleKey="title.deleteDepartment">
							<bean:message key="button.deleteDepartment"/>
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
