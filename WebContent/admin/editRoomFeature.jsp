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
<%@ page import="org.unitime.timetable.form.EditRoomFeatureForm" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>

<%
	// Get Form 
	String frmName = "editRoomFeatureForm";	
	EditRoomFeatureForm frm = (EditRoomFeatureForm) request.getAttribute(frmName);
%>	

<tiles:importAttribute />
<html:form action="/editRoomFeature" focus="name">
	<html:hidden property="id"/>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title><%=frm.getRoomLabel()%></tt:section-title>
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="U" titleKey="title.updateRoomFeatures">
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
	
		<logic:iterate name="<%=frmName%>" property="globalRoomFeatureIds" id="globalRoomFeatureId" indexId="ctr">
			<TR>
				<TD nowrap>
					<logic:equal name="ctr" value="0">
						Global Features:
					</logic:equal>
					<logic:notEqual name="ctr" value="0">
						&nbsp;
					</logic:notEqual>
				</TD>
				<TD width='100%' align="left">
				<% 
					boolean disabled1 = true;
					if (frm.getGlobalRoomFeaturesEditable(ctr.intValue()).equalsIgnoreCase("true")) {
						disabled1 = false;
					}								
				%> 				
					<% if (disabled1) { %><html:hidden property='<%= "globalRoomFeaturesAssigned[" + ctr + "]" %>'/><% } %>
					<html:checkbox property='<%= "globalRoomFeaturesAssigned[" + ctr + "]" %>' 
									disabled="<%=disabled1%>" >
					</html:checkbox>
					<html:hidden property='<%= "globalRoomFeatureIds[" + ctr + "]" %>'/>
					<html:hidden property='<%= "globalRoomFeatureNames[" + ctr + "]" %>'/>
					&nbsp;&nbsp;
					<bean:write name="<%=frmName%>" property='<%= "globalRoomFeatureNames[" + ctr + "]" %>'/> &nbsp;	
				</TD>
			</TR>
		</logic:iterate>
			
		<logic:iterate name="<%=frmName%>" property="departmentRoomFeatureIds" id="departmentRoomFeatureId" indexId="ctr">
			<TR>
				<TD nowrap>
					<logic:equal name="ctr" value="0">
						Department Features:
					</logic:equal>
					<logic:notEqual name="ctr" value="0">
						&nbsp;
					</logic:notEqual>
				</TD>
				<TD align='left' width='100%'>	
			
				<% 
					boolean disabled2 = true;
					if (frm.getdepartmentRoomFeaturesEditable(ctr.intValue()).equalsIgnoreCase("true")) {
						disabled2 = false;
					}			
				%> 
					<% if (disabled2) { %><html:hidden property='<%= "departmentRoomFeaturesAssigned[" + ctr + "]" %>'/><% } %>
					<html:checkbox property='<%= "departmentRoomFeaturesAssigned[" + ctr + "]" %>' 
									disabled="<%=disabled2%>" >
					</html:checkbox>
					<html:hidden property='<%= "departmentRoomFeatureIds[" + ctr + "]" %>'/>
					<html:hidden property='<%= "departmentRoomFeatureNames[" + ctr + "]" %>'/>
					&nbsp;&nbsp;
					<bean:write name="<%=frmName%>" property='<%= "departmentRoomFeatureNames[" + ctr + "]" %>'/> &nbsp;	
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
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="U" titleKey="title.updateRoomFeatures">
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
