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
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<tiles:importAttribute />

<html:form action="/sponsoringOrgEdit">
<html:hidden property="screen"/>
<html:hidden property="id"/>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
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
					<logic:notEqual name="sponsoringOrgEditForm" property="screen" value="add">		
						<tt:section-header>
						<tt:section-title> Edit <bean:write name="sponsoringOrgEditForm" property="orgName"/></tt:section-title>
						<html:submit property="op" styleClass="btn" accesskey="U" 
							title="Update (Alt+U)" value="Update"/>
						<sec:authorize access="hasPermission(#sponsoringOrgEditForm.id, 'SponsoringOrganization', 'SponsoringOrganizationDelete')">
							<html:submit property="op" styleClass="btn" accesskey="D" 
								title="Delete (Alt+D)" value="Delete"/>
						</sec:authorize>
						<html:submit property="op" styleClass="btn" accesskey="B" 
							title="Back to Sponsoring Organizations (Alt+B)" value="Back"/>
						</tt:section-header>
					</logic:notEqual>
					<logic:equal name="sponsoringOrgEditForm" property="screen" value="add">
						<tt:section-header>
						<tt:section-title> Add a New Sponsoring Organization</tt:section-title>
						<html:submit property="op" styleClass="btn" accesskey="S" 
							title="Save (Alt+S)" value="Save"/>
						<html:submit property="op" styleClass="btn" accesskey="B" 
							title="Back to Sponsoring Organizations (Alt+B)" value="Back"/>
						</tt:section-header>
					</logic:equal>
			</TD>
		</TR>
		
		<tr>
			<td> Name: </td> 
			<td> <html:text property="orgName" maxlength="100" size="50" /></td>
		</tr>
		<tr>
			<td> Email: </td>
			<td> <html:text property="orgEmail" maxlength="100" size="50" /></td>
		</tr>

		<TR>
			<td colspan='2'>
				<tt:section-title/>
			</td>
		</TR>
		
		<TR>
			<TD colspan='2' align='right'>
				<logic:notEqual name="sponsoringOrgEditForm" property="screen" value="add">		
					<html:submit property="op" styleClass="btn" accesskey="U" 
						title="Update (Alt+U)" value="Update"/>
					<sec:authorize access="hasPermission(#sponsoringOrgEditForm.id, 'SponsoringOrganization', 'SponsoringOrganizationDelete')">
						<html:submit property="op" styleClass="btn" accesskey="D" 
							title="Delete (Alt+D)" value="Delete"/>
					</sec:authorize>
					<html:submit property="op" styleClass="btn" accesskey="B" 
						title="Back to Sponsoring Organizations (Alt+B)" value="Back"/>
				</logic:notEqual>
				<logic:equal name="sponsoringOrgEditForm" property="screen" value="add">
					<html:submit property="op" styleClass="btn" accesskey="S" 
						title="Save (Alt+S)" value="Save"/>
					<html:submit property="op" styleClass="btn" accesskey="B" 
						title="Back to Sponsoring Organizations (Alt+B)" value="Back"/>
				</logic:equal>
			</TD>
		</TR>
	</TABLE>

</html:form>