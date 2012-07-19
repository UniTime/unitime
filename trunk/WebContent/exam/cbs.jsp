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
<%@ page language="java" autoFlush="true"%>
<%@page import="org.unitime.timetable.solver.exam.ui.ExamConflictStatisticsInfo"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>

<tiles:importAttribute />
<html:form action="/ecbs">
	<script language="JavaScript">blToggleHeader('Filter','dispFilter');blStart('dispFilter');</script>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD>Mode:</TD>
			<TD>
				<html:select property="type">
					<html:options name="ecbsForm" property="types"/>
				</html:select>
			</TD>
		</TR>
		<TR>
			<TD>Limit:</TD>
			<TD>
				<html:text property="limit" size="5" maxlength="5"/> %
				&nbsp;<html:errors property="limit"/>
			</TD>
		</TR>
		<TR>
			<TD colspan='2' align='right'>
				<html:submit onclick="displayLoading();" property="op" value="Change"/>
				<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh"/>
			</TD>
		</TR>
	</TABLE>
	<script language="JavaScript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
			<TR>
				<TD colspan='2' align='right'>
					<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh"/>
				</TD>
			</TR>
		</TABLE>
	<script language="JavaScript">blEndCollapsed('dispFilter');</script>
	
	<logic:notEmpty name="cbs" scope="request">
		<bean:define id="limit" name="ecbsForm" property="limit" type="Double"/>
		<bean:define id="type" name="ecbsForm" property="typeInt" type="Integer"/>
		<bean:define id="cbs" name="cbs" type="ExamConflictStatisticsInfo" scope="request"/>
		<font size='2'>
			<% ExamConflictStatisticsInfo.printHtmlHeader(out); %>
			<% cbs.printHtml(out, limit / 100.0, type, true); %>
		</font>
		<table border='0' width='100%'><tr><td>
			<tt:displayPrefLevelLegend/>
		</td></tr></table>
	</logic:notEmpty>
	<logic:notEmpty name="warning" scope="request">
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
			<TR>
				<TD colspan="2">	
					<i><bean:write name="warning" scope="request"/></i>
				</TD>
			</TR>
		</TABLE>	
	</logic:notEmpty>
</html:form>
