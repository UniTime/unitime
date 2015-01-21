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
<%@ page import="org.unitime.timetable.form.EditRoomPrefForm" %>
<%@ page import="org.unitime.timetable.model.PreferenceLevel" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>

<%
	// Get Form 
	String frmName = "editRoomPrefForm";	
	EditRoomPrefForm frm = (EditRoomPrefForm) request.getAttribute(frmName);
%>	

<tiles:importAttribute />
<html:form action="/editRoomPref">
	<html:hidden property="id"/>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title><%=frm.getName()%></tt:section-title>
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="U" titleKey="title.updateRoomPref">
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
		
		<logic:iterate name="<%=frmName%>" property="depts" id="dept" indexId="ctr">
		<% Department d = (Department) dept; %>
		<TR>
			<TD nowrap><%=d.getDeptCode().trim() + " - " + d.getName().trim()%>&nbsp;&nbsp;&nbsp;
			</TD>
			
			<TD width='100%'>
				<html:select property='<%= "roomPrefLevels[" + ctr + "]" %>' >			
					<logic:iterate scope="request" name="<%=PreferenceLevel.PREF_LEVEL_ATTR_NAME%>" id="prLevel">
						<% PreferenceLevel pr = (PreferenceLevel)prLevel; %>			
					<html:option
						style='<%="background-color:" + pr.prefcolor() + ";"%>'
						value="<%=pr.getUniqueId().toString()%>" >
						<%=pr.getPrefName()%>
					</html:option>
				   	</logic:iterate>
				</html:select>
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
					<html:submit property="doit" onclick="displayLoading();" styleClass="btn" accesskey="U" titleKey="title.updateRoomPref">
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
