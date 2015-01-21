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
<%@ page language="java" autoFlush="true"%>
<%@ page import="org.unitime.timetable.webutil.timegrid.ExamGridTable" %>
<%@ page import="org.unitime.commons.Debug" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
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
