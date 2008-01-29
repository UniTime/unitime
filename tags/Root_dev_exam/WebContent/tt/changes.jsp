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
<%@ page import="org.unitime.commons.Debug" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>

<tiles:importAttribute />
<html:form action="/solutionChanges">
<%
try {
%>
	<script language="JavaScript">blToggleHeader('Filter','dispFilter');blStart('dispFilter');</script>
	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD>Compare with:</TD>
		<TD>
			<html:select property="reference">
				<html:options name="solutionChangesForm" property="references"/>
			</html:select>
		</TD>
	</TR>
		<TR>
			<TD>Simplified mode:</TD>
			<TD>
				<html:checkbox property="simpleMode"/>
			</TD>
		</TR>
		<TR>
			<TD>Reversed mode (current &rarr; compared solution):</TD>
			<TD>
				<html:checkbox property="reversedMode"/>
			</TD>
		</TR>
	<TR>
		<TD colspan='2' align='right'>
			<html:submit onclick="displayLoading();" property="op" value="Apply"/>
			<html:submit onclick="displayLoading();" property="op" value="Export PDF"/>
			<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh"/>
		</TD>
	</TR>
	</TABLE>
	<script language="JavaScript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
		<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
			<TR>
				<TD colspan='2' align='right'>
					<html:submit onclick="displayLoading();" property="op" value="Export PDF"/>
					<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh"/>
				</TD>
			</TR>
		</TABLE>
	<script language="JavaScript">blEndCollapsed('dispFilter');</script>
	<BR><BR>
<%
	String changes = (String)request.getAttribute("SolutionChanges.table");
	if (changes!=null) {
%>
		<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
			<%=changes%>
			<tr>
				<td colspan="<%=request.getAttribute("SolutionChanges.table.colspan")%>">
					<tt:displayPrefLevelLegend/>
				</td>
			</tr>
		</TABLE>
<%
	} else {
%>
		<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
			<TR>
				<TD colspan="2">
					<DIV class="WelcomeRowHead">
					Changes
					</DIV>
				</TD>
			</TR>
			<TR>
				<TD>
					<I><%=request.getAttribute("SolutionChanges.message")%></I>
				</TD>
			</TR>
		</TABLE>
<%
	}
%>
<%
} catch (Exception e) {
	Debug.error(e);
%>		
		<font color='red'><B>ERROR:<%=e.getMessage()%></B></font>
<%
}
%>
</html:form>