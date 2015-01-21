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
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.timetable.form.EditRoomDeptForm" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>

<%
	// Get Form 
	String frmName = "editRoomDeptForm";	
	EditRoomDeptForm frm = (EditRoomDeptForm) request.getAttribute(frmName);
%>	
<tt:session-context/>
<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(sessionContext) %>
	// -->
</SCRIPT>

<tiles:importAttribute />
<html:form action="/editRoomDept">
	<html:hidden property="id"/>
	<html:hidden property="departments"/>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title><%=frm.getName()%></tt:section-title>
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="U" title="Update and Return to Room Detail Page (Alt+U)">
						<bean:message key="button.update" />
					</html:submit>
					&nbsp;
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="B" title="Return to Room Detail Page (Alt+B)">
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
	
		<TR>
			<TD colspan='2'>
				<bean:write name="<%=frmName%>" property="sharingTable" filter="false"/>
			</TD>
		</TR>
		
		
		<%--
		<logic:equal name="<%=frmName%>" property="nonUniv" value="false">
		--%>
			<TR>
				<TD colspan='2' align='right'>
					<html:select property="dept">
						<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
						<html:options collection="<%=Department.DEPT_ATTR_NAME%>" property="value" labelProperty="label"/>
					</html:select>
					&nbsp;
					<html:submit property="doit" 
						accesskey="A" styleClass="btn" titleKey="title.addRoomDept" onclick="displayLoading();">
						<bean:message key="button.addRoomDept" />
					</html:submit>
					<html:submit property="doit" 
						accesskey="R" styleClass="btn" titleKey="title.removeRoomDept" onclick="displayLoading();">
						<bean:message key="button.removeRoomDept" />
					</html:submit>
				</TD>
			</TR>
		<%--
		</logic:equal>
		--%>
			
		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>
			
		<TR>
			<TD colspan='2' align='right'>
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="U" title="Update and Return to Room Detail Page (Alt+U)">
						<bean:message key="button.update" />
					</html:submit>
					&nbsp;
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="B" title="Return to Room Detail Page (Alt+B)">
						<bean:message key="button.returnToRoomDetail" />
					</html:submit>
			</TD>
		</TR>
	</TABLE>
</html:form>
