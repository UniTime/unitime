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
<html:form action="/examPdfReport">
	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
	<logic:messagesPresent>
		<TR>
			<TD colspan='2'>
				<tt:section-header>
					<tt:section-title><font color='red'>Errors</font></tt:section-title>
					<html:submit onclick="displayLoading();" accesskey="G" property="op" value="Generate" title="Generate Report (Alt+G)"/>
				</tt:section-header>
			</TD>
		</TR>
		<TR>
			<TD colspan="2" align="left" class="errorCell">
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
		<TR><TD>&nbsp;</TD></TR>
	</logic:messagesPresent>
	<TR>
		<TD colspan='2'>
			<tt:section-header>
				<tt:section-title>Input Data</tt:section-title>
				<logic:messagesNotPresent>
					<html:submit onclick="displayLoading();" accesskey="G" property="op" value="Generate" title="Generate Report (Alt+G)"/>
				</logic:messagesNotPresent>
			</tt:section-header>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap>Examination Problem:</TD>
		<TD>
			<html:select property="examType">
				<html:optionsCollection property="examTypes" label="label" value="value"/>
			</html:select>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap valign='top'>Subject Areas:</TD>
		<TD>
			<bean:define name="examPdfReportForm" property="all" id="all"/>
			<html:checkbox property="all" onclick="subjects.disabled=this.checked;"/>All Subject Areas (on one report)<br>
			<html:select property="subjects" multiple="true" size="7" disabled="<%=(Boolean)all%>"
				onfocus="setUp();" onkeypress="return selectSearch(event, this);" onkeydown="return checkKey(event, this);">
				<html:optionsCollection property="subjectAreas"	label="subjectAreaAbbreviation" value="uniqueId" />
			</html:select>
		</TD>
	</TR>
	<TR>
		<TD colspan='2'>
			<tt:section-title><br>Report</tt:section-title>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap valign='top'>Report:</TD>
		<TD>
			<logic:iterate name="examPdfReportForm" property="allReports" id="report">
				<html:multibox property="reports">
					<bean:write name="report"/>
				</html:multibox>
				<bean:write name="report"/><br>
			</logic:iterate>
		</TD>
	</TR>
	<TR>
		<TD colspan='2'>
			<tt:section-title><br>Parameters</tt:section-title>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap valign='top'>Conflicts Reports:</TD>
		<TD>
			<html:checkbox property="direct"/>Display Direct Conflicts<br>
			<html:checkbox property="m2d"/>Display More Than 2 Exams A Day Conflicts<br>
			<html:checkbox property="btb"/>Display Back-To-Back Conflicts
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap valign='top' rowspan='2'>Reports with Rooms:</TD>
		<TD><html:checkbox property="dispRooms"/>Display Rooms</TD>
	</TR>
	<TR>
		<TD>No Room: <html:text property="noRoom" size="11" maxlength="11"/></TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap valign='top' rowspan='3'>Period Chart:</TD>
		<TD><html:checkbox property="totals"/>Display Totals</TD>
	</TR>
	<TR>
		<TD>Limit: <html:text property="limit" size="4" maxlength="4"/></TD>
	</TR>
	<TR>
		<TD>Room Codes: <html:text property="roomCodes" size="70" maxlength="200"/></TD>
	</TR>
	<TR>
		<TD colspan='2' valign='top'>
			<tt:section-title><br>Output</tt:section-title>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap>Format:</TD>
		<TD>
			<html:select property="mode">
				<html:options property="modes"/>
			</html:select>
		</TD>
	</TR>
	<TR>
		<TD colspan='2'>
			<tt:section-title><br>&nbsp;</tt:section-title>
		</TD>
	</TR>
	<TR>
		<TD colspan='2' align='right'>
			<html:submit onclick="displayLoading();" accesskey="G" property="op" value="Generate" title="Generate Report (Alt+G)"/>
		</TD>
	</TR>
	</TABLE>
</html:form>