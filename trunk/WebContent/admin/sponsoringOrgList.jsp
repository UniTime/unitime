<%-- 
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
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