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

<html:form action="/sponsoringOrgList">

<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD>
			<tt:section-header>
				<tt:section-title>Sponsoring Organizations</tt:section-title>
				<sec:authorize access="hasPermission(null, null, 'SponsoringOrganizationAdd')">
					<html:submit property="op" styleClass="btn" accesskey="A" 
						title="Add Sponsoring Organization (Alt+A)" value="Add Organization"/>
				</sec:authorize>
			</tt:section-header>
		</TD>
	</TR>
	<TR>
		<TD>
			<Table width='100%' cellspacing="0" cellpadding="3">
				<bean:write name="table" scope="request" filter="false"/>
			</Table>
		</TD>
	</TR>
	<TR>
		<TD>
			<tt:section-title/>
		</TD>
	</TR>
	<TR>
		<TD align='right'>
			<sec:authorize access="hasPermission(null, null, 'SponsoringOrganizationAdd')">
				<html:submit property="op" styleClass="btn" accesskey="A" 
					title="Add Sponsoring Organization (Alt+A)" value="Add Organization"/>
			</sec:authorize>
		</TD>
	</TR>

</TABLE>

</html:form>