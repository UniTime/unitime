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
<%@page import="org.unitime.timetable.form.InstructionalOfferingListForm"%>
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.webutil.WebInstructionalOfferingTableBuilder" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ taglib uri="/WEB-INF/tld/localization.tld" prefix="loc" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>
<%
	String frmName = "instructionalOfferingListForm";	
	InstructionalOfferingListForm frm = (InstructionalOfferingListForm) request.getAttribute(frmName);
%>	
<tiles:importAttribute />
<html:form action="/instructionalOfferingSearch">
<loc:bundle name="CourseMessages">
	<html:hidden property="doit" value="Search"/>
	<TABLE border="0" cellspacing="0" cellpadding="3" width="100%">
		<TR>
			<TD colspan="6">
				<script language="JavaScript" type="text/javascript">blToggleHeader('<loc:message name="filter"/>','dispFilter');blStart('dispFilter');</script>
				<TABLE border="0" cellspacing="0" cellpadding="3">
					<TR>
						<TD>
							<B><loc:message name="filterOptionalColumns" /></B>
						</TD>
						<TD colspan="2">
							<html:checkbox property="divSec" />
							<loc:message name="columnExternalId"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD colspan="2">
							<html:checkbox property="enrollmentInformation" value="1" onclick="if (document.forms[0].enrollmentInformation.checked){document.forms[0].demand.checked = true;document.forms[0].projectedDemand.checked = true;document.forms[0].limit.checked = true;document.forms[0].roomLimit.checked = true;} else {document.forms[0].demand.checked = false;document.forms[0].projectedDemand.checked = false;document.forms[0].limit.checked = false;document.forms[0].roomLimit.checked = false;};"/>
							<loc:message name="columnEnrollmentInformation"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD width="10%"></TD>						
						<TD>
							<html:checkbox property="demand"  />
							<loc:message name="columnDemand"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>						
						<TD>
							<html:checkbox property="projectedDemand" />
							<loc:message name="columnProjectedDemand"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>						
						<TD>
							<html:checkbox property="limit" />
							<loc:message name="columnLimit"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>						
						<TD></TD>
						<TD>
							<html:checkbox property="roomLimit" />
							<loc:message name="columnRoomRatio"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD colspan="2">
							<html:checkbox property="manager" />
							<loc:message name="columnManager"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD colspan="2">
							<html:checkbox property="dateTimeInformation" value="1" onclick="if (document.forms[0].dateTimeInformation.checked){document.forms[0].datePattern.checked = true;document.forms[0].minPerWk.checked = true;document.forms[0].timePattern.checked = true;} else {document.forms[0].datePattern.checked = false;document.forms[0].minPerWk.checked = false;document.forms[0].timePattern.checked = false;};"/>
							<loc:message name="columnDateTimeInformation"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<html:checkbox property="datePattern" />
							<loc:message name="columnDatePattern"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<html:checkbox property="minPerWk" />
							<loc:message name="columnMinPerWk"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<html:checkbox property="timePattern" />
							<loc:message name="columnTimePattern"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD colspan="2">
							<html:checkbox property="preferences" />
							<loc:message name="columnPreferences"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD colspan="2">
							<html:checkbox property="instructor" />
							<loc:message name="columnInstructor"/>
						</TD>
					</TR>
					<logic:notEmpty name="instructionalOfferingListForm" property="timetable">
						<TR>
							<TD></TD>
							<TD colspan="2">
								<html:checkbox property="timetable" />
								<loc:message name="columnTimetable"/>
							</TD>
						</TR>
					</logic:notEmpty>
					<TR>
						<TD></TD>
						<TD colspan="2">
							<html:checkbox property="catalogInformation" value="1" onclick="if (document.forms[0].catalogInformation.checked){document.forms[0].title.checked = true;document.forms[0].credit.checked = true;document.forms[0].subpartCredit.checked = true;document.forms[0].consent.checked = true;document.forms[0].schedulePrintNote.checked = true;} else {document.forms[0].title.checked = false;document.forms[0].credit.checked = false;document.forms[0].subpartCredit.checked = false;document.forms[0].consent.checked = false;document.forms[0].schedulePrintNote.checked = false;};"/>
							<loc:message name="columnCatalogInformation"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<html:checkbox property="title" />
							<loc:message name="columnTitle"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<html:checkbox property="credit" />
							<loc:message name="columnOfferingCredit"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<html:checkbox property="subpartCredit" />
							<loc:message name="columnSubpartCredit"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<html:checkbox property="consent" />
							<loc:message name="columnConsent"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<html:checkbox property="schedulePrintNote" />
							<loc:message name="columnSchedulePrintNote"/>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD colspan="2">
							<html:checkbox property="note" />
							<loc:message name="columnNote"/>
						</TD>
					</TR>
					<sec:authorize access="hasPermission(null, 'Session', 'Examinations')">
						<TR>
							<TD></TD>
							<TD colspan="2">
								<html:checkbox property="exams" />
								<loc:message name="columnExams"/>
							</TD>
						</TR>
					</sec:authorize>
					<TR>
						<TD>
							<B><loc:message name="filterSortBy"/></B>
						</TD>
						<TD colspan="2">
							<html:select property="sortBy">
								<html:options property="sortByOptions"/>
							</html:select>
						</TD>
					</TR>
				</TABLE>

				<script language="JavaScript" type="text/javascript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
				<TABLE class="wide-table">
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
			<TH valign="top" nowrap><loc:message name="filterSubject"/></TH>
			<TD valign="top" nowrap>
				<% if (frm.getSubjectAreas().size()==1) { %>
					<html:select property="subjectAreaIds" styleId="subjectAreaIds">
						<html:optionsCollection property="subjectAreas" label="subjectAreaAbbreviation" value="uniqueId" />
					</html:select>
				<% } else { %>
					<html:select size="<%=String.valueOf(Math.min(7,frm.getSubjectAreas().size()))%>" property="subjectAreaIds" multiple="true" styleId="subjectAreaIds">
						<html:optionsCollection property="subjectAreas" label="subjectAreaAbbreviation" value="uniqueId" />
					</html:select>
				<% } %>
			</TD>
			<TH valign="top" nowrap><loc:message name="filterCourseNumber"/></TH>
			<TD valign="top" nowrap>
				<tt:course-number property="courseNbr" configuration="subjectId=\${subjectAreaIds};notOffered=include" size="10"
					title="Course numbers can be specified using wildcard (*). E.g. 2*"/>
			</TD>
			<TD valign="top" nowrap>
				&nbsp;&nbsp;&nbsp;
				<html:submit
					accesskey="<%=MSG.accessSearchInstructionalOfferings()%>" styleClass="btn" title='<%=MSG.titleSearchInstructionalOfferings(MSG.accessSearchInstructionalOfferings())%>'
					onclick="doit.value=this.value;displayLoading();">
					<loc:message name="actionSearchInstructionalOfferings"/>
				</html:submit> 
				
				<sec:authorize access="hasPermission(null, 'Department', 'InstructionalOfferingsExportPDF')">
				<html:submit
					accesskey="<%=MSG.accessExportPdf()%>" styleClass="btn" title='<%=MSG.titleExportPdf(MSG.accessExportPdf())%>'
					onclick="doit.value=this.value;">
					<loc:message name="actionExportPdf"/>
				</html:submit> 
				</sec:authorize>

				<sec:authorize access="hasPermission(null, 'Department', 'InstructionalOfferingsWorksheetPDF')">
				<tt:propertyEquals name="tmtbl.pdf.worksheet" value="true">
					<html:submit
						accesskey="<%=MSG.accessWorksheetPdf()%>" styleClass="btn" title='<%=MSG.titleWorksheetPdf(MSG.accessWorksheetPdf())%>'
						onclick="doit.value=this.value;">
						<loc:message name="actionWorksheetPdf"/>
					</html:submit>
				</tt:propertyEquals>
				</sec:authorize>

				<sec:authorize access="hasPermission(null, 'SubjectArea', 'AddCourseOffering')">
				<html:submit
					accesskey="<%=MSG.accessAddNewInstructionalOffering()%>" styleClass="btn" title='<%=MSG.titleAddNewInstructionalOffering(MSG.accessAddNewInstructionalOffering())%>'
					onclick="doit.value=this.value;">
					<loc:message name="actionAddNewInstructionalOffering"/>
				</html:submit>
				</sec:authorize>
				
			</TD>
			<TD width="100%"></TD>
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