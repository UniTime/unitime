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
<%@ page import="org.unitime.timetable.webutil.WebInstructionalOfferingTableBuilder"%>
<%@ page import="org.unitime.timetable.form.ClassListForm" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.timetable.model.ItypeDesc" %>
<%@page import="org.unitime.timetable.model.StudentClassEnrollment"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/localization.tld" prefix="loc" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
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
	<script language="javascript">displayElement('loading', false);</script>
</logic:notEmpty>

<SCRIPT type="text/javascript" language="javascript">
	function jumpToAnchor() {
    <% if (request.getAttribute("hash") != null) { %>
  		location.hash = "<%=request.getAttribute("hash")%>";
	<% } %>
	    self.focus();
  	}
</SCRIPT>
