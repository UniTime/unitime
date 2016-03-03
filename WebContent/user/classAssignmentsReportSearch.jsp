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
<%@ taglib uri="http://www.unitime.org/tags-localization" prefix="loc" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>
<tiles:importAttribute />

<%// Get Form 
			String frmName = "classAssignmentsReportForm";
			ClassAssignmentsReportForm frm = (ClassAssignmentsReportForm) request.getAttribute(frmName);
%>

<html:form action="/classAssignmentsReportSearch">
<loc:bundle name="CourseMessages">
	<TABLE border="0" cellspacing="5" width='100%'>
		<TR>
			<TD>
				<script language="JavaScript" type="text/javascript">blToggleHeader('<loc:message name="filter"/>','dispFilter');blStart('dispFilter');</script>
				<TABLE border="0" cellspacing="0" cellpadding="3">
					<TR>
						<TD>
							<B><loc:message name="filterManager"/></B>
						</TD>
						<TD>
							<html:select property="filterManager">	
								<html:option value=""><loc:message name="dropManagerAll"/></html:option>				
								<html:option value="-2"><loc:message name="dropDeptDepartment"/></html:option>
								<html:options collection="<%=Department.EXTERNAL_DEPT_ATTR_NAME%>" property="uniqueId" labelProperty="managingDeptLabel" />
							</html:select>
						</TD>
					</TR>
					<TR>
						<TD>
							<B><loc:message name="filterInstructionalType"/></B>
						</TD>
						<TD>
							<html:select property="filterIType">
								<html:option value=""><loc:message name="dropITypeAll"/></html:option>
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
							<B><loc:message name="filterAssignedTime"/></B>
						</TD>
						<TD>
							<loc:bundle name="ConstantsMessages" id="CONST">
								<html:checkbox property="filterAssignedTimeMon"/>
								<loc:message name="mon" id="CONST"/>&nbsp;
								<html:checkbox property="filterAssignedTimeTue"/>
								<loc:message name="tue" id="CONST"/>&nbsp;
								<html:checkbox property="filterAssignedTimeWed"/>
								<loc:message name="wed" id="CONST"/>&nbsp;
								<html:checkbox property="filterAssignedTimeThu"/>
								<loc:message name="thu" id="CONST"/>&nbsp;
								<html:checkbox property="filterAssignedTimeFri"/>
								<loc:message name="fri" id="CONST"/>&nbsp;
								<html:checkbox property="filterAssignedTimeSat"/>
								<loc:message name="sat" id="CONST"/>&nbsp;
								<html:checkbox property="filterAssignedTimeSun"/>
								<loc:message name="sun" id="CONST"/>&nbsp;
							</loc:bundle>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD>
							<loc:message name="filterTimeFrom"/>
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
							<loc:message name="filterTimeFor"/>
							<html:text property="filterAssignedTimeLength" size="3" maxlength="4"/>
							<loc:message name="filterTimeMinutes"/>
						</TD>
					</TR>
					<TR>
						<TD>
							<B><loc:message name="filterAssignedRoom"/></B>
						</TD>
						<TD colspan='2'>
							<html:text property="filterAssignedRoom" size="25"/>
						</TD>
					</TR>
					<TR>
						<TD>
							<B><loc:message name="filterSortBy"/></B>
						</TD>
						<TD>
							<html:select property="sortBy">
								<html:options property="sortByOptions"/>
							</html:select>
							<BR>
							<html:checkbox property="sortByKeepSubparts"/>
							<loc:message name="checkSortWithinSubparts"/>
						</TD>
					</TR>
					<TR>
						<TD>
							<B><loc:message name="filterCrossList"/></B>
						</TD>
						<TD>
							<html:checkbox property="showCrossListedClasses"/>
							<loc:message name="showCrossListedClasses"/>
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
				<b><loc:message name="filterSubject"/></b>
			</TD>
			<TD>
				<html:select size="<%=String.valueOf(Math.min(7,frm.getSubjectAreas().size()))%>" name="classAssignmentsReportForm" styleClass="cmb" property="subjectAreaIds" multiple="true">
					<html:optionsCollection property="subjectAreas" label="subjectAreaAbbreviation" value="uniqueId" />
				</html:select>
			</TD>
			<TD align="left" valign="top" nowrap>
				&nbsp;&nbsp;&nbsp;
				<html:submit property="doit" onclick="displayLoading();" accesskey="<%=MSG.accessSearchClasses()%>" styleClass="btn" title="<%=MSG.titleSearchClasses(MSG.accessSearchClasses())%>">
					<loc:message name="actionSearchClassAssignments"/>
				</html:submit>
				<sec:authorize access="hasPermission(null, 'Session', 'ClassAssignmentsExportPdf')">
					&nbsp;&nbsp;&nbsp;
					<html:submit property="doit" accesskey="<%=MSG.accessExportPdf()%>" styleClass="btn" title='<%=MSG.titleExportPdf(MSG.accessExportPdf())%>'>
						<loc:message name="actionExportPdf"/>
					</html:submit>
				</sec:authorize>
				<sec:authorize access="hasPermission(null, 'Session', 'ClassAssignmentsExportCsv')">
					&nbsp;&nbsp;&nbsp;
					<html:submit property="doit" accesskey="<%=MSG.accessExportCsv()%>"  styleClass="btn" title='<%=MSG.titleExportCsv(MSG.accessExportCsv())%>'>
						<loc:message name="actionExportCsv"/>
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
</loc:bundle>
</html:form>

<logic:notEmpty name="body2">
	<script language="javascript">displayLoading();</script>
	<tiles:insert attribute="body2" />
	<script language="javascript">displayElement('loading', false);</script>
</logic:notEmpty>
