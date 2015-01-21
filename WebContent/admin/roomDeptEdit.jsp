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
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>

<tiles:importAttribute />
<html:form action="/roomDeptEdit">
	<html:hidden property="id"/>
	<html:hidden property="examType"/>
	<input type='hidden' name='ord' value=''>
	
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD>
				<tt:section-header>
					<tt:section-title><bean:write name="roomDeptEditForm" property="name"/></tt:section-title>
					<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="U" 
							title="Update Room Department (Alt+U)">
						<bean:message key="button.update" />
					</html:submit>
					<html:submit property="op"  styleClass="btn" accesskey="B" titleKey="title.returnToRoomList">
						<bean:message key="button.returnToRoomList" />
					</html:submit>
				</tt:section-header>
			</TD>
		</TR>

		<logic:messagesPresent>
			<TR>
				<TD align="left" class="errorCell">
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

		<tr><td><table border="0" cellspacing="0" cellpadding="3" width='100%'>
			<bean:write name="roomDeptEditForm" property="table" filter="false"/>
		</table></td></tr>
		
		<tr><td><tt:section-title/></td></tr>

		<TR>
			<TD align='right'>
				<html:submit property="op" onclick="displayLoading();" styleClass="btn" accesskey="U" 
						title="Update Room Department (Alt+U)">
					<bean:message key="button.update" />
				</html:submit>
				<html:submit property="op"  styleClass="btn" accesskey="B" titleKey="title.returnToRoomList">
					<bean:message key="button.returnToRoomList" />
				</html:submit>
			</TD>
		</TR>
	</TABLE>
</html:form>
