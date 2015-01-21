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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp"%>
<%@ page import="org.unitime.timetable.form.ClassAssignmentsReportForm"%>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.timetable.model.ItypeDesc" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>
<tiles:importAttribute />

<%// Get Form 
			String frmName = "classAssignmentsReportForm";
			ClassAssignmentsReportForm frm = (ClassAssignmentsReportForm) request.getAttribute(frmName);
%>

<html:form action="/classAssignmentsReportSearch">
	<TABLE border="0" cellspacing="5" width='100%'>
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
						<TD>
							<B>Cross Lists:</B>
						</TD>
						<TD>
							<html:checkbox property="showCrossListedClasses"/>
							Show cross-listed classes
						</TD>
					</TR>
					<TR>
						<TD colspan='2' align='right'>
							<br>
						</TD>
					</TR>
				</TABLE>

				<script language="JavaScript" type="text/javascript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
				<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
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
				<html:select size="<%=String.valueOf(Math.min(7,frm.getSubjectAreas().size()))%>" name="classAssignmentsReportForm" styleClass="cmb" property="subjectAreaIds" multiple="true">
					<html:optionsCollection property="subjectAreas" label="subjectAreaAbbreviation" value="uniqueId" />
				</html:select>
			</TD>
			<TD align="left" valign="top" nowrap>
				&nbsp;&nbsp;&nbsp;
				<html:submit property="doit" onclick="displayLoading();" accesskey="S" styleClass="btn" titleKey="title.searchClassAssignments">
					<bean:message key="button.searchClasses" />
				</html:submit>
				<sec:authorize access="hasPermission(null, 'Session', 'ClassAssignmentsExportPdf')">
					&nbsp;&nbsp;&nbsp;
					<html:submit property="doit" accesskey="P" styleClass="btn" titleKey="title.exportPDF">
						<bean:message key="button.exportPDF" />
					</html:submit>
				</sec:authorize>
				<sec:authorize access="hasPermission(null, 'Session', 'ClassAssignmentsExportCsv')">
					&nbsp;&nbsp;&nbsp;
					<html:submit property="doit" accesskey="C" styleClass="btn" titleKey="title.exportCSV">
						<bean:message key="button.exportCSV" />
					</html:submit>
				</sec:authorize>
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
