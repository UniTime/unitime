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
<%@ page import="org.unitime.timetable.model.Building" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.timetable.form.SpecialUseRoomForm" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>

<%
	// Get Form 
	String frmName = "specialUseRoomForm";	
	SpecialUseRoomForm frm = (SpecialUseRoomForm) request.getAttribute(frmName);
%>	
<tiles:importAttribute />

<html:form action="/addSpecialUseRoom" focus="deptCode">
	
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header title="Add Special Use Room">
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="A" 
							title="Add Special Use Room (Alt+A)">
						<bean:message key="button.addNew" />
					</html:submit>
					&nbsp;
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="B" 
						title="Back to Room List (Alt+B)">
						<bean:message key="button.returnToRoomList"/>
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
				<TD>Department:</TD>
				<TD>
					<html:select property="deptCode">
						<logic:empty name="<%=frmName%>" property="deptCode">
							<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
						</logic:empty>
						<logic:equal name="<%=frmName%>" property="deptCode" value="<%=Constants.ALL_OPTION_VALUE%>">
							<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
						</logic:equal>
						<html:options collection="<%=Department.DEPT_ATTR_NAME%>" property="value" labelProperty="label"/>
					</html:select>
				</TD>
			</TR>
				
			<TR>
				<TD>Building:</TD>
				<TD>
					<html:select property="bldgId">  
						<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
						<html:options collection="<%=Building.BLDG_LIST_ATTR_NAME%>" 
							property="value" labelProperty="label"/>
					</html:select>
				</TD>
			</TR>
			
			<TR>
				<TD>Room Number:</TD>
				<TD>
					<html:text property="roomNum" maxlength="40" size="20" />
				</TD>
			</TR>
			
			<!-- 
			<TR>
				<TD nowrap>Ignore Too Far Distance:</TD>
				<TD width='100%'>
					<html:checkbox property="ignoreTooFar" />
				</TD>
			</TR>

			<TR>
				<TD nowrap>Ignore Room Checks:</TD>
				<TD>
					<html:checkbox property="ignoreRoomCheck" />
				</TD>
			</TR>
			-->

		<TR>
			<TD colspan='2'>
				<tt:section-title/>
			</TD>
		</TR>
		
		<TR>
			<TD colspan='2' align='right'>
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="A" 
							title="Add Special Use Room (Alt+A)">
						<bean:message key="button.addNew" />
					</html:submit>
					&nbsp;
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="B" 
						title="Back to Room List (Alt+B)">
						<bean:message key="button.returnToRoomList"/>
					</html:submit>
			</TD>
		</TR>
	</TABLE>
</html:form>
