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
<%@ page import="org.unitime.timetable.webutil.timegrid.ExamGridTable" %>
<%@ page import="org.unitime.commons.Debug" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>
<tiles:importAttribute />
<html:form action="/examGrid">
<%
try {
%>
	<script language="JavaScript">blToggleHeader('Filter','dispFilter');blStart('dispFilter');</script>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD width="10%" nowrap>Show classes/courses:</TD>
			<TD>
				<html:checkbox property="showSections"/>
			</TD>
		</TR>
		<TR>
  			<TD nowrap>Examination Problem:</TD>
			<TD>
				<html:select property="examType" onchange="javascript: showDates(this.value);">
					<html:options collection="examTypes" labelProperty="label" property="uniqueId"/>
				</html:select>
			</TD>
		</TR>
		<TR>
			<TD>Resource:</TD>
			<TD>
				<html:select property="resource">
					<html:optionsCollection name="examGridForm" property="resources" label="label" value="value"/>
				</html:select>
			</TD>
		</TR>
		<TR>
			<TD>Filter:</TD>
			<TD>
				<html:text property="filter" maxlength="1000" size="40"/>
			</TD>
		</TR>
		<script>var rowToExamType = new Array();</script>
		<logic:iterate scope="request" name="examTypes" id="et" indexId="idx">
			<bean:define name="et" property="uniqueId" id="examType"/>
			<script>rowToExamType[<%=idx%>] = <%=examType%>;</script>
			<TR id='<%="daterow."+examType%>'>
				<TD>Date:</TD>
				<TD>
					<html:select property='<%="date("+examType+")"%>'>
						<html:optionsCollection name="examGridForm" property='<%="dates("+examType+")"%>' label="label" value="value"/>
					</html:select>
				</TD>
			</TR>
			<TR id='<%="timerow."+examType%>'>
				<TD>Time:</TD>
				<TD>
					<html:select property='<%="startTime("+examType+")"%>'>
						<html:optionsCollection name="examGridForm" property='<%="startTimes("+examType+")"%>' label="label" value="value"/>
					</html:select>
					-
					<html:select property='<%="endTime("+examType+")"%>'>
						<html:optionsCollection name="examGridForm" property='<%="endTimes("+examType+")"%>' label="label" value="value"/>
					</html:select>
				</TD>
			</TR>
		</logic:iterate>
		<script language="JavaScript" type="text/javascript">
			function showDates(examType) {
				for (var x = 0; x < rowToExamType.length; x++) {
					var disp = (rowToExamType[x] == examType ? null : "none");
					document.getElementById("daterow."+rowToExamType[x]).style.display=disp;
					document.getElementById("timerow."+rowToExamType[x]).style.display=disp;
				}
			}
			showDates(document.getElementsByName('examType')[0].value);
		</script>
		<TR>
			<TD>Display Mode:</TD>
			<TD>
				<html:select property="dispMode">
					<html:optionsCollection name="examGridForm" property="dispModes" label="label" value="value"/>
				</html:select>
			</TD>
		</TR>
		<TR>
			<TD>Background:</TD>
			<TD>
				<html:select property="background">
					<html:optionsCollection name="examGridForm" property="backgrounds" label="label" value="value"/>
				</html:select>
			</TD>
		</TR>
		<TR>
			<TD>Show period preferences:</TD>
			<TD>
				<html:checkbox property="bgPreferences"/>
			</TD>
		</TR>
		<TR>
			<TD>Order By:</TD>
			<TD>
				<html:select property="order">
					<html:optionsCollection name="examGridForm" property="orders" label="label" value="value"/>
				</html:select>
			</TD>
		</TR>
		<TR>
			<TD colspan='2' align='right'>
				<html:submit onclick="displayLoading();" property="op" value="Change"/>
				<html:submit property="op" value="Export PDF"/>
				<html:submit onclick="displayLoading();" property="op" accesskey="R" value="Refresh"/>
			</TD>
		</TR>
	</TABLE>
	<script language="JavaScript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
			<TR>
				<TD colspan='2' align='right'>
					<html:submit property="op" value="Export PDF"/>
					<html:submit onclick="displayLoading();" property="op" accesskey="R" value="Refresh"/>
				</TD>
			</TR>
		</TABLE>
	<script language="JavaScript">blEndCollapsed('dispFilter');</script>
	
	<br><br>
	<a name='timetable'></a>
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2">
				<tt:section-header>
					<tt:section-title>
						Examination Timetable
					</tt:section-title>
					<A class="l7" href="#legend">Legend</A>&nbsp;
				</tt:section-header>
			</TD>
		</TR>
<%
			ExamGridTable table = (ExamGridTable)request.getAttribute("table");
			if (table.models().isEmpty()) { 
%>
				<TR>
					<TD>
						<i>No resource matches the above criteria (or there is no resource at all).</i>
					</TD>
				</TR>
			</TABLE>
<%
			} else {
%>
			</TABLE>
<%

				table.printToHtml(out);
			}
%>
		
	<BR>
	<a name='legend'></a>
	<TABLE width="100%" border="0" >
		<TR>
			<TD colspan="3">
				<tt:section-header>
					<tt:section-title>
						Legend
					</tt:section-title>
					<A class="l7" href="#timetable">Examination Timetable</A>&nbsp;
				</tt:section-header>
			</TD>
		</TR>
<%
			table.printLegend(out);
%>
	</TABLE>
<%
} catch (Exception e) {
	Debug.error(e);
%>		
		<font color='red'><B>ERROR:<%=e.getMessage()%></B></font>
<%
}
%>
</html:form>
