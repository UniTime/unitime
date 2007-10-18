<%-- 
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org
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
<%@ page language="java" autoFlush="true"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>

<tiles:importAttribute />

<html:form action="/itypeDescEdit">
	<html:hidden property="uniqueId"/>
	<html:hidden property="canDelete"/>

	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<DIV class="WelcomeRowHead">
				<logic:equal name="itypeDescEditForm" property="op" value="Save">
				Add
				</logic:equal>
				<logic:equal name="itypeDescEditForm" property="op" value="Update">
				Edit
				</logic:equal>
				Instructional Type
				</DIV>
			</TD>
		</TR>

		<TR>
			<TD>IType:</TD>
			<TD>
				<logic:equal name="itypeDescEditForm" property="op" value="Save">
					<html:text property="id" size="2" maxlength="2"/>
					&nbsp;<html:errors property="id"/>
				</logic:equal>
				<logic:equal name="itypeDescEditForm" property="op" value="Update">
					<html:hidden property="id"/>
					<bean:write name="itypeDescEditForm" property="id"/>
				</logic:equal>
			</TD>
		</TR>

		<TR>
			<TD>Abbreviation:</TD>
			<TD>
				<html:text property="abbreviation" size="7" maxlength="7"/>
				&nbsp;<html:errors property="abbreviation"/>
			</TD>
		</TR>

		<TR>
			<TD>Name:</TD>
			<TD>
				<html:text property="name" size="50" maxlength="50"/>
				&nbsp;<html:errors property="name"/>
			</TD>
		</TR>

		<TR>
			<TD>Reference:</TD>
			<TD>
				<html:text property="reference" size="20" maxlength="20"/>
				&nbsp;<html:errors property="reference"/>
			</TD>
		</TR>

		<TR>
			<TD>Type:</TD>
			<TD>
				<html:select property="type">
					<html:options name="itypeDescEditForm" property="types"/>
				</html:select>
				&nbsp;<html:errors property="type"/>
			</TD>
		</TR>
		
		<TR>
			<TD align="right" colspan="2">
				<logic:equal name="itypeDescEditForm" property="op" value="Save">
					<html:submit property="op" value="Save" title="Save (Alt+S)" accesskey="S"/> 
				</logic:equal>
				<logic:equal name="itypeDescEditForm" property="op" value="Update">
					<html:submit property="op" value="Update" title="Update (Alt+U)" accesskey="U"/>
					<logic:equal name="itypeDescEditForm" property="canDelete" value="true"> 
						<html:submit property="op" value="Delete" title="Delete (Alt+D)" accesskey="D"/>
					</logic:equal> 
				</logic:equal>
				<html:submit property="op" value="Back" title="Back (Alt+B)" accesskey="B"/> 
			</TD>
		</TR>
	</TABLE>

</html:form>