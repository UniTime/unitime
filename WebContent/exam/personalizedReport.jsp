<%--
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime.org
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
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<tiles:importAttribute />
<html:form action="/exams">
	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
		<TR><TD align='right'>
			<logic:equal name="personalizedExamReportForm" property="canExport" value="true">
				<html:submit accesskey="P" property="op" value="Export PDF" title="Export PDF (Alt+P)"/>
			</logic:equal>
			<html:submit accesskey="L" property="op" value="Log Out" title="Log out (Alt+L)"/>
		</TD></TR>
	</TABLE>
	<logic:notEmpty scope="request" name="sessions">
		<BR>
		<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
			<bean:write scope="request" name="sessions" filter="false"/>
		</TABLE>
	</logic:notEmpty>
	<logic:notEmpty scope="request" name="iclsschd">
		<BR>
		<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
			<bean:write scope="request" name="iclsschd" filter="false"/>
		</TABLE>
	</logic:notEmpty>
	<logic:notEmpty scope="request" name="ischedule">
		<BR>
		<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
			<bean:write scope="request" name="ischedule" filter="false"/>
		</TABLE>
	</logic:notEmpty>
	<logic:notEmpty scope="request" name="iconf">
		<BR>
		<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
			<bean:write scope="request" name="iconf" filter="false"/>
		</TABLE>
	</logic:notEmpty>
	<logic:notEmpty scope="request" name="sconf">
		<BR>
		<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
			<bean:write scope="request" name="sconf" filter="false"/>
		</TABLE>
	</logic:notEmpty>
	<logic:notEmpty scope="request" name="clsschd">
		<BR>
		<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
			<bean:write scope="request" name="clsschd" filter="false"/>
		</TABLE>
	</logic:notEmpty>
	<logic:notEmpty scope="request" name="schedule">
		<BR>
		<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
			<bean:write scope="request" name="schedule" filter="false"/>
		</TABLE>
	</logic:notEmpty>
	<logic:notEmpty scope="request" name="conf">
		<BR>
		<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
			<bean:write scope="request" name="conf" filter="false"/>
		</TABLE>
	</logic:notEmpty>
	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
		<TR><TD><tt:section-title/></TD></TR>
		<TR><TD align='right'>
			<logic:equal name="personalizedExamReportForm" property="canExport" value="true">
				<html:submit accesskey="P" property="op" value="Export PDF" title="Export PDF (Alt+P)"/>
			</logic:equal>
			<html:submit accesskey="L" property="op" value="Log Out" title="Log out (Alt+L)"/>
		</TD></TR>
	</TABLE>
</html:form>