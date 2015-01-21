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
<%@ page import="org.unitime.timetable.form.EditRoomGroupForm" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>

<%
	// Get Form 
	String frmName = "editRoomGroupForm";	
	EditRoomGroupForm frm = (EditRoomGroupForm) request.getAttribute(frmName);
%>	

<tiles:importAttribute />

<html:form action="/editRoomGroup">
	<html:hidden property="id"/>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title><%=frm.getName()%></tt:section-title>
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="U" titleKey="title.updateRoomGroups">
						<bean:message key="button.update" />
					</html:submit>
					&nbsp;
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="B" titleKey="title.returnToRoomList">
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
	
		<logic:iterate name="<%=frmName%>" property="globalRoomGroupIds" id="globalRoomGroupId" indexId="ctr">
			<TR>
				<TD nowrap>
					<logic:equal name="ctr" value="0">
						Global Groups:
					</logic:equal>
					<logic:notEqual name="ctr" value="0">
						&nbsp;
					</logic:notEqual>
				</TD>
				<TD width='100%'>
				<% 
					boolean disabled1 = true;
					if (frm.getGlobalRoomGroupsEditable(ctr.intValue()).equalsIgnoreCase("true")) {
						disabled1 = false;
					}			
				%> 
					<% if (disabled1) { %>
						<html:hidden property='<%= "globalRoomGroupsAssigned[" + ctr + "]" %>'/>
					<% } %>
					<html:checkbox property='<%= "globalRoomGroupsAssigned[" + ctr + "]" %>' 
									disabled="<%=disabled1%>" >
					</html:checkbox>
					<html:hidden property='<%= "globalRoomGroupIds[" + ctr + "]" %>'/>
					<html:hidden property='<%= "globalRoomGroupNames[" + ctr + "]" %>'/>
					&nbsp;&nbsp;
					<bean:write name="<%=frmName%>" property='<%= "globalRoomGroupNames[" + ctr + "]" %>'/> &nbsp;	
				</TD>
			</TR>
		</logic:iterate>
			
		<logic:iterate name="<%=frmName%>" property="managerRoomGroupIds" id="managerRoomGroupId" indexId="ctr">
			<TR>
				<TD nowrap>
					<logic:equal name="ctr" value="0">
						Manager Groups:
					</logic:equal>
					<logic:notEqual name="ctr" value="0">
						&nbsp;
					</logic:notEqual>
				</TD>
				<TD align='left' width='100%'>	
				<% 
					boolean disabled2 = true;
					if (frm.getManagerRoomGroupsEditable(ctr.intValue()).equalsIgnoreCase("true")) {
						disabled2 = false;
					}			
				%> 
					<% if (disabled2) { %>
						<html:hidden property='<%= "managerRoomGroupsAssigned[" + ctr + "]" %>'/>
					<% } %>
					<html:checkbox property='<%= "managerRoomGroupsAssigned[" + ctr + "]" %>' 
									disabled="<%=disabled2%>" >
					</html:checkbox>
					<html:hidden property='<%= "managerRoomGroupIds[" + ctr + "]" %>'/>
					<html:hidden property='<%= "managerRoomGroupNames[" + ctr + "]" %>'/>
					&nbsp;&nbsp;
					<bean:write name="<%=frmName%>" property='<%= "managerRoomGroupNames[" + ctr + "]" %>'/> &nbsp;	
				</TD>
			</TR>
		</logic:iterate>

		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>
		
		<TR>
			<TD colspan='2' align='right'>
				<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="U" titleKey="title.updateRoomGroups">
					<bean:message key="button.update" />
				</html:submit>
				&nbsp;
				<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="B" titleKey="title.returnToRoomList">
					<bean:message key="button.returnToRoomDetail" />
				</html:submit>
			</TD>
		</TR>
	</TABLE>
	
</html:form>
