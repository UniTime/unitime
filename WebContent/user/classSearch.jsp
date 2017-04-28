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
<%@ page import="org.unitime.timetable.webutil.WebInstructionalOfferingTableBuilder"%>
<%@ page import="org.unitime.timetable.form.ClassListForm" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.timetable.model.ItypeDesc" %>
<%@page import="org.unitime.timetable.model.StudentClassEnrollment"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.unitime.org/tags-localization" prefix="loc" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>
<%
	String frmName = "classListForm";	
	ClassListForm frm = (ClassListForm) request.getAttribute(frmName);
%>	
<tiles:importAttribute />
<html:form action="/classSearch">
<loc:bundle name="CourseMessages">
	<html:hidden property="doit" value="Search"/>
	<TABLE border="0" cellspacing="5" width='100%'>
		<TR>
			<TD colspan="6">
				<script language="JavaScript" type="text/javascript">blToggleHeader('<loc:message name="filter"/>','dispFilter');blStart('dispFilter');</script>
				<TABLE border="0" cellspacing="0" cellpadding="3">
					<TR>
						<TD>
							<B><loc:message name="filterOptionalColumns" /></B>
						</TD>
						<TD>
							<html:checkbox property="divSec" />
							<loc:message name="columnExternalId"/>
						</TD>
					</TR>
					<logic:equal name="<%=frmName%>" property="demandIsVisible" value="true">
						<TR>
							<TD></TD>
							<TD>
								<html:checkbox property="demand" />
								<loc:message name="columnDemand"/>
							</TD>
						</TR>
					</logic:equal>
					<html:hidden property="demandIsVisible"/>
					<TR>
						<TD></TD>
						<TD>
							<html:checkbox property="limit" />
							<loc:message name="columnLimit"/>
						</TD>
					</TR>
					<logic:notEmpty name="<%=frmName%>" property="snapshotLimit">
						<TR>
							<TD></TD>
							<TD colspan="2">
								<html:checkbox property="snapshotLimit" />
								<loc:message name="columnSnapshotLimit"/>
							</TD>
						</TR>
					</logic:notEmpty>
					<TR>
						<TD></TD>
						<TD>
							<html:checkbox property="roomLimit" />
							<loc:message name="columnRoomRatio"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD>
							<html:checkbox property="manager" />
							<loc:message name="columnManager"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD>
							<html:checkbox property="datePattern" />
							<loc:message name="columnDatePattern"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD>
							<html:checkbox property="timePattern" />
							<loc:message name="columnTimePattern"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD>
							<html:checkbox property="preferences" />
							<loc:message name="columnPreferences"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD>
							<html:checkbox property="instructorAssignment" />
							<loc:message name="includeInstructorScheduling"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD>
							<html:checkbox property="instructor" />
							<loc:message name="columnInstructor"/>
						</TD>
					</TR>
					<logic:notEmpty name="classListForm" property="timetable">
						<TR>
							<TD></TD>
							<TD>
								<html:checkbox property="timetable" />
								<loc:message name="columnTimetable"/>
							</TD>
						</TR>
					</logic:notEmpty>
					<TR>
						<TD></TD>
						<TD>
							<html:checkbox property="schedulePrintNote" />
							<loc:message name="columnSchedulePrintNote"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD>
							<html:checkbox property="note" />
							<loc:message name="columnNote"/>
						</TD>
					</TR>
					<sec:authorize access="hasPermission(null, 'Session', 'Examinations')">
						<TR>
							<TD></TD>
							<TD>
								<html:checkbox property="exams" />
								<loc:message name="columnExams"/>
							</TD>
						</TR>
					</sec:authorize>
					<TR>
						<TD>
							<B><loc:message name="filterManager" /></B>
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
					<TR>
						<TD>
							<B><loc:message name="filterInstructor"/></B>
						</TD>
						<TD>
							<html:text property="filterInstructor" size="25"/>
						</TD>
					</TR>
					<logic:notEmpty name="classListForm" property="timetable">
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
					</logic:notEmpty>
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
						<TD>
							<B><loc:message name="filterCancelledClasses"/></B>
						</TD>
						<TD>
							<html:checkbox property="includeCancelledClasses"/>
							<loc:message name="showCancelledClasses"/>
						</TD>
					</TR>
					<TR>
						<TD>
							<B><loc:message name="filterNeedInstructorAssignment"/></B>
						</TD>
						<TD>
							<html:checkbox property="filterNeedInstructor"/>
							<loc:message name="showNeedInstructorClasses"/>
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
			<TD valign="top">
				<b><A name="Search"></A><loc:message name="filterSubject"/></b>
			</TD>
			<TD>
				<% if (frm.getSubjectAreas().size()==1) { %>
					<html:select property="subjectAreaIds" styleId="subjectId">
						<html:optionsCollection property="subjectAreas" label="subjectAreaAbbreviation" value="uniqueId" />
					</html:select>
				<% } else { %>
					<html:select size="<%=String.valueOf(Math.min(7,frm.getSubjectAreas().size()))%>" property="subjectAreaIds" multiple="true" styleId="subjectId">
						<html:optionsCollection property="subjectAreas" label="subjectAreaAbbreviation" value="uniqueId" />
					</html:select>
				<% } %>
			</TD>
			<TD valign="top" nowrap>
				<B><loc:message name="filterCourseNumber"/></B>
			</TD>
			<TD valign="top">
				<tt:course-number property="courseNbr" configuration="subjectId=\${subjectId};notOffered=exclude" size="10"
					title="Course numbers can be specified using wildcard (*). E.g. 2*"/>
				<!-- html:text property="courseNbr" size="10" maxlength="10" / -->
			</TD>
			<TD valign="top" nowrap>
				&nbsp;&nbsp;&nbsp;
				<html:submit onclick="doit.value=this.value;displayLoading();" 
					accesskey="<%=MSG.accessSearchClasses()%>"
					styleClass="btn" 
					title="<%=MSG.titleSearchClasses(MSG.accessSearchClasses())%>">
					<loc:message name="actionSearchClasses"/>
				</html:submit>
				<sec:authorize access="hasPermission(null, 'Department', 'ClassesExportPDF')">
				&nbsp;&nbsp;&nbsp;
				<html:submit
					accesskey="<%=MSG.accessExportPdf()%>" 
					styleClass="btn" 
					title='<%=MSG.titleExportPdf(MSG.accessExportPdf())%>'
					onclick="doit.value=this.value;">
					<loc:message name="actionExportPdf"/>
				</html:submit> 
				&nbsp;&nbsp;&nbsp;
				<html:submit
					accesskey="<%=MSG.accessExportCsv()%>" 
					styleClass="btn" 
					title='<%=MSG.titleExportCsv(MSG.accessExportCsv())%>'
					onclick="doit.value=this.value;">
					<loc:message name="actionExportCsv"/>
				</html:submit> 
				</sec:authorize>
			</TD>
			<TD width='100%'></TD>
		</TR>
		<TR>
			<TD colspan="6" align="center">
				<html:errors />
			</TD>
		</TR>
	</TABLE>
</loc:bundle>
</html:form>

<logic:notEmpty name="body2">
	<script language="javascript">displayLoading();</script>
	<tiles:insert attribute="body2" />
	<script language="javascript">hideLoading();</script>
</logic:notEmpty>

<SCRIPT type="text/javascript" language="javascript">
	function jumpToAnchor() {
    <% if (request.getAttribute("hash") != null) { %>
  		location.hash = "<%=request.getAttribute("hash")%>";
	<% } %>
	    self.focus();
  	}
</SCRIPT>
