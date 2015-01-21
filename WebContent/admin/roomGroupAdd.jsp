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
<%@ page import="org.unitime.timetable.form.RoomGroupEditForm" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<%
	// Get Form 
	String frmName = "roomGroupEditForm";		
	RoomGroupEditForm frm = (RoomGroupEditForm) request.getAttribute(frmName);
%>	

<tiles:importAttribute />
<html:form action="/roomGroupAdd" focus="name">
	<html:hidden property="id"/>
	<html:hidden property="sessionId"/>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan='2'>
			<tt:section-header>
				<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="A" 
						title="Add New Room Group (Alt+A)">
					<bean:message key="button.addNew" />
				</html:submit>
				<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="R" 
					title="Back to Room Groups (Alt+B)">
					<bean:message key="button.returnToRoomGroupList"/>
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
			<TD>Name: <font class="reqField">*</font></TD>
			<TD>
				<html:text property="name" maxlength="60" size="60" />
			</TD>
		</TR>
			
		<TR>
			<TD>Abbreviation: <font class="reqField">*</font></TD>
			<TD>
				<html:text property="abbv" maxlength="60" size="60" />
			</TD>
		</TR>

		<sec:authorize access="hasPermission(null, 'Session', 'GlobalRoomGroupAdd') and hasPermission(null, 'Department', 'DepartmentRoomGroupAdd')">
			<TR>
				<TD>Global:</TD>
				<TD>
					<html:checkbox property="global" onclick="document.getElementById('department').style.display = (this.checked ? 'none' : 'table-row');"/>
				</TD>
			</TR>
			<TR id="department" <%=(frm.isGlobal() ? "style='display:none;'" : "")%>>
				<TD>Department:</TD>
				<TD>
					<html:select property="deptCode">
						<logic:empty name="<%=frmName%>" property="deptCode">
							<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
						</logic:empty>
						<html:options collection="<%=Department.DEPT_ATTR_NAME%>" property="value" labelProperty="label"/>
					</html:select>
				</TD>
			</TR>
		</sec:authorize>
		<sec:authorize access="!hasPermission(null, 'Session', 'GlobalRoomGroupAdd') or !hasPermission(null, 'Department', 'DepartmentRoomGroupAdd')">
			<TR>
				<TD>Global:</TD>
				<TD>
					<html:checkbox property="global" disabled="true"/>
					<html:hidden property="global" />
				</TD>
			</TR>
			<logic:equal name="<%=frmName%>" property="global" value="false">
				<TR>
					<TD>Department:</TD>
					<TD>
						<html:select property="deptCode">
							<logic:empty name="<%=frmName%>" property="deptCode">
								<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
							</logic:empty>
							<html:options collection="<%=Department.DEPT_ATTR_NAME%>" property="value" labelProperty="label"/>
						</html:select>
					</TD>
				</TR>
			</logic:equal>
		</sec:authorize>
		
		<TR>
			<TD>Description:</TD>
			<TD>
				<html:textarea property="desc" rows="4" cols="50" />
			</TD>
		</TR>

		<TR>
			<TD valign="middle" colspan="2">
				<tt:section-title/>
			</TD>
		</TR>
			
		<TR>
			<TD colspan='2' align='right'>
				<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="A" 
						title="Add New Room Group (Alt+A)">
					<bean:message key="button.addNew" />
				</html:submit>
				<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="R" 
					title="Back to Room Groups (Alt+B)">
					<bean:message key="button.returnToRoomGroupList"/>
				</html:submit>
			</TD>
		</TR>
	</TABLE>

</html:form>
