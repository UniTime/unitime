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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp"%>
<%@ page import="org.unitime.timetable.form.ClassAssignmentsReportForm"%>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.timetable.model.ItypeDesc" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/struts-layout.tld" prefix="layout"%>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>
<tiles:importAttribute />

<%// Get Form 
			String frmName = "classAssignmentsReportForm";
			ClassAssignmentsReportForm frm = (ClassAssignmentsReportForm) request
					.getAttribute(frmName);
%>

<html:form action="/classAssignmentsReportSearch">
	<TABLE border="0" cellspacing="5" width='93%'>
		<TR>
			<TD>
				<script language="JavaScript" type="text/javascript">blToggleHeader('Filter','dispFilter');blStart('dispFilter');</script>
				<TABLE border="0" cellspacing="0" cellpadding="3">
					<TR>
						<TD>
							<B>Manager:</B>
						</TD>
						<TD>
							<html:select property="filterManager">	
								<html:option value="">All</html:option>				
								<html:option value="-2">Department</html:option>
								<html:options collection="<%=Department.EXTERNAL_DEPT_ATTR_NAME%>" property="uniqueId" labelProperty="managingDeptLabel" />
							</html:select>
						</TD>
					</TR>
					<TR>
						<TD>
							<B>Instructional Type:</B>
						</TD>
						<TD>
							<html:select property="filterIType">
								<html:option value="">All</html:option>
								<html:options collection="<%=ItypeDesc.ITYPE_ATTR_NAME%>" property="itype" labelProperty="desc" />
							</html:select>
						</TD>
					</TR>
					<%-- //NO instructor is shown on class assignment page -> display no filter
					<TR>
						<TD>
							<B>Instructor:</B>
						</TD>
						<TD>
							<html:text property="filterInstructor" size="25"/>
						</TD>
					</TR>
					--%>
					<TR>
						<TD>
							<B>Assigned Time:</B>
						</TD>
						<TD>
							<html:checkbox property="filterAssignedTimeMon"/>
							Mon&nbsp;
							<html:checkbox property="filterAssignedTimeTue"/>
							Tue&nbsp;
							<html:checkbox property="filterAssignedTimeWed"/>
							Wed&nbsp;
							<html:checkbox property="filterAssignedTimeThu"/>
							Thu&nbsp;
							<html:checkbox property="filterAssignedTimeFri"/>
							Fri&nbsp;
							<html:checkbox property="filterAssignedTimeSat"/>
							Sat&nbsp;
							<html:checkbox property="filterAssignedTimeSun"/>
							Sun
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD>
							from
							<html:select property="filterAssignedTimeHour">
								<html:options property="filterAssignedTimeHours"/>
							</html:select>
							:
							<html:select property="filterAssignedTimeMin">
								<html:options property="filterAssignedTimeMins"/>
							</html:select>
							<html:select property="filterAssignedTimeAmPm">
								<html:options property="filterAssignedTimeAmPms"/>
							</html:select>
							&nbsp;&nbsp;
							for
							<html:text property="filterAssignedTimeLength" size="3" maxlength="4"/>
							minutes
						</TD>
					</TR>
					<TR>
						<TD>
							<B>Assigned Room:</B>
						</TD>
						<TD colspan='2'>
							<html:text property="filterAssignedRoom" size="25"/>
						</TD>
					</TR>
					<TR>
						<TD>
							<B>Sort By:</B>
						</TD>
						<TD>
							<html:select property="sortBy">
								<html:options property="sortByOptions"/>
							</html:select>
							<BR>
							<html:checkbox property="sortByKeepSubparts"/>
							Sort classes only within scheduling subparts
						</TD>
					</TR>
					<TR>
						<TD colspan='2' align='right'>
							<br>
						</TD>
					</TR>
				</TABLE>

				<script language="JavaScript" type="text/javascript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
				<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
					<TR>
						<TD colspan='2' align='right'>
							<br>
						</TD>
					</TR>
				</TABLE>
				<script language="JavaScript" type="text/javascript">blEnd('dispFilter');</script>
			</TD>
		</TR>
		<TR>
		<TD><TABLE border="0" cellspacing="5">
		<TR>
			<TD valign="top">
				<b>Subject:</b>
			</TD>
			<TD>
				<html:select size="<%=String.valueOf(Math.min(7,frm.getSubjectAreas().size()))%>" name="classAssignmentsReportForm" styleClass="cmb" property="subjectAreaIds" multiple="true" onfocus="setUp();" onkeypress="return selectSearch(event, this);" onkeydown="return checkKey(event, this);">
					<html:optionsCollection property="subjectAreas" label="subjectAreaAbbreviation" value="uniqueId" />
				</html:select>
			</TD>
			<TD align="left" valign="top">
				&nbsp;&nbsp;&nbsp;
				<html:submit property="doit" onclick="displayLoading();" accesskey="S" styleClass="btn" titleKey="title.searchClassAssignments">
					<bean:message key="button.searchClasses" />
				</html:submit>
				&nbsp;&nbsp;&nbsp;
				<html:submit property="doit" onclick="displayLoading();" accesskey="P" styleClass="btn" titleKey="title.exportPDF">
					<bean:message key="button.exportPDF" />
				</html:submit>
				&nbsp;&nbsp;&nbsp;
				<html:submit property="doit" accesskey="C" styleClass="btn" titleKey="title.exportCSV">
					<bean:message key="button.exportCSV" />
				</html:submit>
				<tt:propertyEquals name="tmtbl.exportMSF" value="true">
					<logic:equal name="<%=frmName%>" property="isAdmin" value="true">
						&nbsp;&nbsp;&nbsp;
						<html:submit property="doit" accesskey="M" styleClass="btn">
							<bean:message key="button.msfExport" />
						</html:submit>
					</logic:equal>
				</tt:propertyEquals>
			</TD>
		</TR>
		</TABLE></TD></TR>
		<TR>
			<TD align="center">
				<html:errors />
			</TD>
		</TR>
	</TABLE>
</html:form>

<logic:notEmpty name="body2">
	<script language="javascript">displayLoading();</script>
	<tiles:insert attribute="body2" />
	<script language="javascript">displayElement('loading', false);</script>
</logic:notEmpty>
