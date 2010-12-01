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
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<tiles:importAttribute />

<html:form action="/eventStandardNotes">

<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD>
			<tt:section-header>
				<tt:section-title>Standard Event Notes</tt:section-title>
				<html:submit property="op" styleClass="btn" accesskey="A" 
					title="Add a Standard Event Note (Alt+A)" value="Add Standard Note"/>
			</tt:section-header>
		</TD>
	</TR>
	<TR>
		<TD>
			<Table width='100%' cellspacing="0" cellpadding="3">
				<bean:write name="eventStandardNotesForm" property="table" filter="false"/>
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
			<html:submit property="op" styleClass="btn" accesskey="A" 
			title="Add a Standard Event Note (Alt+A)" value="Add Standard Note"/>
		</TD>
	</TR>

</TABLE>

</html:form>