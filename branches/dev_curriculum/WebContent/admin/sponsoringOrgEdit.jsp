<%-- 
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 --%>

<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

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
						<html:submit property="op" styleClass="btn" accesskey="D" 
							title="Delete (Alt+D)" value="Delete"/>
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
					<html:submit property="op" styleClass="btn" accesskey="D" 
						title="Delete (Alt+D)" value="Delete"/>
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