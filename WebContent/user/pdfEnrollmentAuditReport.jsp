<%--
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC
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
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<tiles:importAttribute />
<html:form action="/enrollmentAuditPdfReport">
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<logic:messagesPresent>
		<TR>
			<TD colspan='2'>
				<tt:section-header>
					<tt:section-title><font color='red'>Errors</font></tt:section-title>
					<logic:empty name="enrollmentAuditPdfReportForm" property="report">
						<html:submit onclick="displayLoading();" accesskey="G" property="op" value="Generate" title="Generate Report (Alt+G)"/>
					</logic:empty>
					<logic:notEmpty name="enrollmentAuditPdfReportForm" property="report">
						<html:submit onclick="displayLoading();" accesskey="B" property="op" value="Back" title="Back (Alt+B)"/>
					</logic:notEmpty>
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
	<logic:notEmpty name="enrollmentAuditPdfReportForm" property="report">
		<TR>
			<TD colspan='2'>
				<tt:section-header>
					<tt:section-title>Log</tt:section-title>
					<logic:messagesNotPresent>
						<html:submit onclick="displayLoading();" accesskey="B" property="op" value="Back" title="Back (Alt+B)"/>
					</logic:messagesNotPresent>
				</tt:section-header>
			</TD>
		</TR>
		<TR>
  			<TD colspan='2'>
  				<blockquote>
  					<bean:write name="enrollmentAuditPdfReportForm" property="report" filter="false"/>
  				</blockquote>
  			</TD>
		</TR>
		<TR>
			<TD colspan='2'>
				<tt:section-title>&nbsp;</tt:section-title>
			</TD>
		</TR>
		<TR>
			<TD colspan='2' align='right'>
				<html:submit onclick="displayLoading();" accesskey="B" property="op" value="Back" title="Back (Alt+B)"/>
			</TD>
		</TR>
	</logic:notEmpty>
	<logic:empty name="enrollmentAuditPdfReportForm" property="report">
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
  		<TD width="10%" nowrap valign='top'>Subject Areas:</TD>
		<TD>
			<html:checkbox property="all" onclick="selectionChanged();"/>All Subject Areas<br>
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
			<logic:iterate name="enrollmentAuditPdfReportForm" property="allReports" id="report">
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
		<TD><html:checkbox property="externalId"/>Display Student Id<br>
			<html:checkbox property="studentName"/>Display Student Name</TD>
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
	<logic:equal name="enrollmentAuditPdfReportForm" property="canEmail" value="false">
		<html:hidden property="email"/>
	</logic:equal>
	<logic:equal name="enrollmentAuditPdfReportForm" property="canEmail" value="true">
	<TR>
		<TD rowspan='1' valign='top'>Delivery:</TD>
		<TD>
			<html:checkbox property="email" onclick="document.getElementById('eml').style.display=(this.checked?'block':'none');"/> Email
			<bean:define name="enrollmentAuditPdfReportForm" property="email" id="email"/>
			<table border='0' id='eml' style='display:<%=(Boolean)email?"block":"none"%>;'>
				<tr>
					<td valign='top'>Address:</td>
					<td><html:textarea property="address" rows="3" cols="70"/></td>
				</tr>
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
	</logic:empty>
	</TABLE>
<script type="text/javascript" language="javascript">
	function selectionChanged() {
		if (document.getElementsByName('all')==null || document.getElementsByName('all').length==0) return;
		var allSubjects = document.getElementsByName('all')[0].checked;
		var objSubjects = document.getElementsByName('subjects')[0];
		var objReports = document.getElementsByName('reports');
		var objSince = document.getElementsByName('since')[0];
		var studentSchedule = false;
		var instructorSchedule = false;
		objSubjects.disabled=allSubjects;
	}
</script>
</html:form>
