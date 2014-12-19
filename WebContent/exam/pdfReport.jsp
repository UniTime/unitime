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
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<tiles:importAttribute />
<html:form action="/examPdfReport">
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
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
	<logic:notEmpty name="table" scope="request">
		<TR><TD colspan="2">
			<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
				<bean:write name="table" scope="request" filter="false"/>
			</TABLE>
		</TD></TR>
		<TR><TD colspan='2'>&nbsp;</TD></TR>
	</logic:notEmpty>
	<logic:notEmpty name="log" scope="request">
		<TR>
			<TD colspan='2'>
				<tt:section-header>
					<tt:section-title>
						Log of <bean:write name="logname" scope="request" filter="false"/>
					</tt:section-title>
					<bean:define id="logid" name="logid" scope="request"/>
					<input type="hidden" name="log" value="<%=logid%>">
					<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh" title="Refresh Log (Alt+R)"/>
				</tt:section-header>
			</TD>
		</TR>
		<TR>
  			<TD colspan='2'>
  				<blockquote>
	  				<bean:write name="log" scope="request" filter="false"/>
  				</blockquote>
  			</TD>
		</TR>
	</logic:notEmpty>
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
				<html:options collection="examTypes" property="uniqueId" labelProperty="label"/>
			</html:select>
		</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap valign='top'>Subject Areas:</TD>
		<TD>
			<html:checkbox property="all" onclick="selectionChanged();"/>All Subject Areas (on one report)<br>
			<html:select property="subjects" multiple="true" size="7">
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
				<html:multibox property="reports" onclick="selectionChanged();">
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
  		<TD width="10%" nowrap valign='top'>All Reports:</TD>
		<TD><html:checkbox property="itype"/>Display Instructional Type<br>
			<html:checkbox property="ignoreEmptyExams"/>Skip Exams with No Enrollment</TD>
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
  		<TD width="10%" nowrap valign='top' rowspan="2">Verification Report:</TD>
		<TD><html:checkbox property="dispLimit"/>Display Limits &amp; Enrollments</TD>
	</TR>
	<TR>
		<TD><html:checkbox property="dispNote"/>Display Class Schedule Notes</TD>
	</TR>
	<TR>
  		<TD width="10%" nowrap valign='top' rowspan='2'>Individual Reports:</TD>
  		<TD><html:checkbox property="classSchedule"/>Include Class Schedule</TD>
  	</TR>
  	<TR>
		<TD>
			<span style="display: table;">
				<span style="display: table-row;">
					<span style="display: table-cell; vertical-align: middle; padding-right: 5px;">Date: </span>
					<tt:calendar property="since" outerStyle="display: table-cell;"/>
					<span style="display: table-cell; font-style: italic; padding-left: 5px; vertical-align: middle;">(Only email instructors/students that have a change in their schedule since this date, email all when empty)
				</span>
			</span>
		</TD>
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
	<logic:equal name="examPdfReportForm" property="canEmail" value="false">
		<html:hidden property="email"/>
	</logic:equal>
	<logic:equal name="examPdfReportForm" property="canEmail" value="true">
	<TR>
		<TD rowspan='1' valign='top'>Delivery:</TD>
		<TD>
			<html:checkbox property="email" onclick="document.getElementById('eml').style.display=(this.checked?'block':'none');"/> Email
			<bean:define name="examPdfReportForm" property="email" id="email"/>
			<table border='0' id='eml' style='display:<%=(Boolean)email?"block":"none"%>;'>
				<sec:authorize access="hasPermission(null, null, 'DepartmentIndependent')">
					<tr>
						<td rowspan='4' valign='top'>Address:</td>
						<td><html:textarea property="address" rows="3" cols="70"/></td>
					</tr>
					<tr><td>
						<html:checkbox property="emailDeputies" styleId="ed"/> All Involved Department Schedule Managers
					</td></tr>
					<tr><td>
						<html:checkbox property="emailInstructors" styleId="ed"/> Send Individual Instructor Schedule Reports to All Involved Instructors
					</td></tr>
					<tr><td>
						<html:checkbox property="emailStudents" styleId="ed"/> Send Individual Student Schedule Reports to All Involved Students
					</td></tr>
				</sec:authorize>
				<sec:authorize access="!hasPermission(null, null, 'DepartmentIndependent')">
					<tr>
						<td valign='top'>Address:</td>
						<td><html:textarea property="address" rows="3" cols="70"/></td>
					</tr>
				</sec:authorize>
				<tr><td valign='top'>CC:</td><td>
					<html:textarea property="cc" rows="2" cols="70"/>
				</td></tr>
				<tr><td valign='top'>BCC:</td><td>
					<html:textarea property="bcc" rows="2" cols="70"/>
				</td></tr>
				<tr><td valign='top' style='border-top: black 1px dashed;'>Subject:</td><td style='border-top: black 1px dashed;'>
					<html:text property="subject" size="70" style="margin-top:2px;"/>
				</td></tr>
				<tr><td valign='top'>Message:</td><td>
					<html:textarea property="message" rows="10" cols="70"/>
				</td></tr>
			</table>
		</TD>
	</TR>
	</logic:equal>
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
<script type="text/javascript" language="javascript">
	function selectionChanged() {
		if (document.getElementsByName('all')==null || document.getElementsByName('all').length==0) return;
		var allSubjects = document.getElementsByName('all')[0].checked;
		var objSubjects = document.getElementsByName('subjects')[0];
		var objEmailDeputies = document.getElementsByName('emailDeputies')[0];
		var objEmailInstructors = document.getElementsByName('emailInstructors')[0];
		var objEmailStudents = document.getElementsByName('emailStudents')[0];
		var objReports = document.getElementsByName('reports');
		var objSince = document.getElementsByName('since')[0];
		var studentSchedule = false;
		var instructorSchedule = false;
		for (var i=0;i<objReports.length;i++) {
			if ('Individual Student Schedule'==objReports[i].value) studentSchedule = objReports[i].checked;
			if ('Individual Instructor Schedule'==objReports[i].value) instructorSchedule = objReports[i].checked;
		}
		objSubjects.disabled=allSubjects;
		objEmailDeputies.disabled=allSubjects; 
		objEmailInstructors.disabled=!instructorSchedule;
		objEmailStudents.disabled=!studentSchedule;
		if (allSubjects) {
			objEmailDeputies.checked=false;
		}
		if (!studentSchedule) objEmailStudents.checked=false;
		if (!instructorSchedule) objEmailInstructors.checked=false;
		objSince.disabled=objEmailInstructors.disabled && objEmailStudents.disabled;
	}
</script>
</html:form>
