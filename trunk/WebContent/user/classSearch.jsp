<%--
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC
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
<%@ page import="org.unitime.timetable.webutil.WebInstructionalOfferingTableBuilder"%>
<%@ page import="org.unitime.timetable.form.ClassListForm" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.timetable.model.ItypeDesc" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/struts-layout.tld" prefix="layout"%>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>
<%
	String frmName = "classListForm";	
	ClassListForm frm = (ClassListForm) request.getAttribute(frmName);
%>	
<tiles:importAttribute />
<html:form action="/classSearch">
	<html:hidden property="doit" value="Search"/>
	<TABLE border="0" cellspacing="5" width='93%'>
		<TR>
			<TD colspan="6">
				<script language="JavaScript" type="text/javascript">blToggleHeader('Filter','dispFilter');blStart('dispFilter');</script>
				<TABLE border="0" cellspacing="0" cellpadding="3">
					<TR>
						<TD>
							<B>Optional Columns:</B>
						</TD>
						<TD>
							<html:checkbox property="divSec" />
							<%=WebInstructionalOfferingTableBuilder.DIV_SEC%>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD>
							<html:checkbox property="limit" />
							<%=WebInstructionalOfferingTableBuilder.LIMIT%>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD>
							<html:checkbox property="roomLimit" />
							<%=WebInstructionalOfferingTableBuilder.ROOM_RATIO%>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD>
							<html:checkbox property="manager" />
							<%=WebInstructionalOfferingTableBuilder.MANAGER%>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD>
							<html:checkbox property="datePattern" />
							<%=WebInstructionalOfferingTableBuilder.DATE_PATTERN%>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD>
							<html:checkbox property="timePattern" />
							<%=WebInstructionalOfferingTableBuilder.TIME_PATTERN%>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD>
							<html:checkbox property="preferences" />
							<%=WebInstructionalOfferingTableBuilder.PREFERENCES%>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD>
							<html:checkbox property="instructor" />
							<%=WebInstructionalOfferingTableBuilder.INSTRUCTOR%>
						</TD>
					</TR>
					<logic:notEmpty name="classListForm" property="timetable">
						<TR>
							<TD></TD>
							<TD>
								<html:checkbox property="timetable" />
								<%=WebInstructionalOfferingTableBuilder.TIMETABLE%>
							</TD>
						</TR>
					</logic:notEmpty>
					<TR>
						<TD></TD>
						<TD>
							<html:checkbox property="schedulePrintNote" />
							<%=WebInstructionalOfferingTableBuilder.SCHEDULE_PRINT_NOTE%>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD>
							<html:checkbox property="note" />
							<%=WebInstructionalOfferingTableBuilder.NOTE%>
						</TD>
					</TR>
					<logic:equal name="classListForm" property="canSeeExams" value="true">
						<TR>
							<TD></TD>
							<TD>
								<html:checkbox property="exams" />
								Examinations
							</TD>
						</TR>
					</logic:equal>
					<html:hidden property="canSeeExams"/>
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
					<TR>
						<TD>
							<B>Instructor:</B>
						</TD>
						<TD>
							<html:text property="filterInstructor" size="25"/>
						</TD>
					</TR>
					<logic:notEmpty name="classListForm" property="timetable">
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
					</logic:notEmpty>
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
			<TD valign="top">
				<b><A name="Search">Subject:</A></b>
			</TD>
			<TD>
				<% if (frm.getSubjectAreas().size()==1) { %>
					<html:select property="subjectAreaIds" onfocus="setUp();" onkeypress="return selectSearch(event, this);" onkeydown="return checkKey(event, this);">
						<html:optionsCollection property="subjectAreas" label="subjectAreaAbbreviation" value="uniqueId" />
					</html:select>
				<% } else { %>
					<html:select size="<%=String.valueOf(Math.min(7,frm.getSubjectAreas().size()))%>" property="subjectAreaIds" multiple="true" onfocus="setUp();" onkeypress="return selectSearch(event, this);" onkeydown="return checkKey(event, this);">
						<html:optionsCollection property="subjectAreas" label="subjectAreaAbbreviation" value="uniqueId" />
					</html:select>
				<% } %>
			</TD>
			<TD valign="top" nowrap>
				<B>Course Number:</B>
			</TD>
			<TD valign="top">
				<!-- html:text property="courseNbr" size="5" maxlength="5" / -->
				<layout:suggest 
					suggestAction="/getCourseNumbers" property="courseNbr" styleId="courseNbr" 
					suggestCount="15" size="5" maxlength="5" layout="false" all="true" 
				 	tooltip="Course numbers can be specified using wildcard (*). E.g. 2*"
					onblur="hideSuggestionList('courseNbr');" />
			</TD>
			<TD valign="top" nowrap>
				&nbsp;&nbsp;&nbsp;
				<html:submit onclick="doit.value=this.value;displayLoading();" accesskey="S" styleClass="btn" titleKey="title.searchClasses">
					<bean:message key="button.searchClasses" />
				</html:submit>
				&nbsp;&nbsp;&nbsp;
				<html:submit
					accesskey="P" styleClass="btn" titleKey="title.exportPDF"
					onclick="doit.value=this.value;displayLoading();">
					<bean:message key="button.exportPDF" />
				</html:submit> 
			</TD>
			<TD width='100%'></TD>
		</TR>
		<TR>
			<TD colspan="6" align="center">
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

<SCRIPT type="text/javascript" language="javascript">
	function jumpToAnchor() {
    <% if (request.getAttribute("hash") != null) { %>
  		location.hash = "<%=request.getAttribute("hash")%>";
	<% } %>
	    self.focus();
  	}
</SCRIPT>
