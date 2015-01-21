<%-- 
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 --%>
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
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
					<html:select property="department">
						<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
						<html:options collection="<%=Department.DEPT_ATTR_NAME%>" property="uniqueId" labelProperty="label"/>
					</html:select>					
				</logic:empty>
				<logic:notEmpty name="subjectAreaEditForm" property="uniqueId">
					<sec:authorize access="hasPermission(#subjectAreaEditForm.uniqueId, 'SubjectArea', 'SubjectAreaChangeDepartment')">
						<html:select property="department" >
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

	
