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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.webutil.WebInstructionalOfferingTableBuilder" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/struts-layout.tld" prefix="layout" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>

<tiles:importAttribute />
<html:form action="/instructionalOfferingSearch">
	<html:hidden property="doit" value="Search"/>
	<TABLE border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="5">
				<script language="JavaScript" type="text/javascript">blToggleHeader('Filter','dispFilter');blStart('dispFilter');</script>
				<TABLE border="0" cellspacing="0" cellpadding="3">
					<TR>
						<TD>
							<B>Optional Columns:</B>
						</TD>
						<TD colspan="2">
							<html:checkbox property="divSec" />
							<%=WebInstructionalOfferingTableBuilder.DIV_SEC%>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD colspan="2">
							<html:checkbox property="enrollmentInformation" value="1" onclick="if (document.forms[0].enrollmentInformation.checked){document.forms[0].demand.checked = true;document.forms[0].projectedDemand.checked = true;document.forms[0].limit.checked = true;document.forms[0].roomLimit.checked = true} else {document.forms[0].demand.checked = false;document.forms[0].projectedDemand.checked = false;document.forms[0].limit.checked = false;document.forms[0].roomLimit.checked = false};"/>
							Enrollment Information
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD width="10%"></TD>						
						<TD>
							<html:checkbox property="demand"  />
							<%=WebInstructionalOfferingTableBuilder.DEMAND%>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>						
						<TD>
							<html:checkbox property="projectedDemand" />
							<%=WebInstructionalOfferingTableBuilder.PROJECTED_DEMAND%>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>						
						<TD>
							<html:checkbox property="limit" />
							<%=WebInstructionalOfferingTableBuilder.LIMIT%>
						</TD>
					</TR>
					<TR>
						<TD></TD>						
						<TD></TD>
						<TD>
							<html:checkbox property="roomLimit" />
							<%=WebInstructionalOfferingTableBuilder.ROOM_RATIO%>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD colspan="2">
							<html:checkbox property="manager" />
							<%=WebInstructionalOfferingTableBuilder.MANAGER%>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD colspan="2">
							<html:checkbox property="dateTimeInformation" value="1" onclick="if (document.forms[0].dateTimeInformation.checked){document.forms[0].datePattern.checked = true;document.forms[0].minPerWk.checked = true;document.forms[0].timePattern.checked = true} else {document.forms[0].datePattern.checked = false;document.forms[0].minPerWk.checked = false;document.forms[0].timePattern.checked = false};"/>
							Date/Time Information
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<html:checkbox property="datePattern" />
							<%=WebInstructionalOfferingTableBuilder.DATE_PATTERN%>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<html:checkbox property="minPerWk" />
							<%=WebInstructionalOfferingTableBuilder.MIN_PER_WK%>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<html:checkbox property="timePattern" />
							<%=WebInstructionalOfferingTableBuilder.TIME_PATTERN%>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD colspan="2">
							<html:checkbox property="preferences" />
							<%=WebInstructionalOfferingTableBuilder.PREFERENCES%>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD colspan="2">
							<html:checkbox property="instructor" />
							<%=WebInstructionalOfferingTableBuilder.INSTRUCTOR%>
						</TD>
					</TR>
					<logic:notEmpty name="instructionalOfferingListForm" property="timetable">
						<TR>
							<TD></TD>
							<TD colspan="2">
								<html:checkbox property="timetable" />
								<%=WebInstructionalOfferingTableBuilder.TIMETABLE%>
							</TD>
						</TR>
					</logic:notEmpty>
					<TR>
						<TD></TD>
						<TD colspan="2">
							<html:checkbox property="catalogInformation" value="1" onclick="if (document.forms[0].catalogInformation.checked){document.forms[0].title.checked = true;document.forms[0].credit.checked = true;document.forms[0].subpartCredit.checked = true;document.forms[0].consent.checked = true;document.forms[0].designatorRequired.checked = true;document.forms[0].schedulePrintNote.checked = true} else {document.forms[0].title.checked = false;document.forms[0].credit.checked = false;document.forms[0].subpartCredit.checked = false;document.forms[0].consent.checked = false;document.forms[0].designatorRequired.checked = false;document.forms[0].schedulePrintNote.checked = false};"/>
							Catalog Information
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<html:checkbox property="title" />
							<%=WebInstructionalOfferingTableBuilder.TITLE%>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<html:checkbox property="credit" />
							<%=WebInstructionalOfferingTableBuilder.CREDIT%>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<html:checkbox property="subpartCredit" />
							<%=WebInstructionalOfferingTableBuilder.SCHEDULING_SUBPART_CREDIT%>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<html:checkbox property="consent" />
							<%=WebInstructionalOfferingTableBuilder.CONSENT%>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<html:checkbox property="designatorRequired" />
							<%=WebInstructionalOfferingTableBuilder.DESIGNATOR_REQ%>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD></TD>
						<TD>
							<html:checkbox property="schedulePrintNote" />
							<%=WebInstructionalOfferingTableBuilder.SCHEDULE_PRINT_NOTE_FILTER%>
						</TD>
					</TR>
					<TR>
						<TD></TD>
						<TD colspan="2">
							<html:checkbox property="note" />
							<%=WebInstructionalOfferingTableBuilder.NOTE%>
						</TD>
					</TR>
					<logic:equal name="instructionalOfferingListForm" property="canSeeExams" value="true">
						<TR>
							<TD></TD>
							<TD colspan="2">
								<html:checkbox property="exams" />
								Examinations
							</TD>
						</TR>
					</logic:equal>
					<html:hidden property="canSeeExams"/>
					<TR>
						<TD>
							<B>Sort By:</B>
						</TD>
						<TD colspan="2">
							<html:select property="sortBy">
								<html:options property="sortByOptions"/>
							</html:select>
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
			<TH valign="top">Subject:</TH>
			<TD valign="top">
				<html:select name="instructionalOfferingListForm" property="subjectAreaId"
					onfocus="setUp();" 
					onkeypress="return selectSearch(event, this);" 
					onkeydown="return checkKey(event, this);" >
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:optionsCollection property="subjectAreas"	label="subjectAreaAbbreviation" value="uniqueId" />
				</html:select>
			</TD>
			<TH valign="top">Course Number:</TH>
			<TD valign="top">
				<!-- html:text property="courseNbr" size="10" maxlength="10" / -->
				<layout:suggest 
					suggestAction="/getCourseNumbers" property="courseNbr" styleId="courseNbr" 
					suggestCount="15" size="10" maxlength="10" layout="false" all="true"
					minWordLength="2" 
					tooltip="Course numbers can be specified using wildcard (*). E.g. 2*"
					onblur="hideSuggestionList('courseNbr');" />
			</TD>
			<TD valign="top">
				&nbsp;&nbsp;&nbsp;
				<html:submit
					accesskey="S" styleClass="btn" titleKey="title.searchOffering"
					onclick="doit.value=this.value;displayLoading();">
					<bean:message key="button.searchInstructionalOfferings" />
				</html:submit> 
				
				<html:submit
					accesskey="P" styleClass="btn" titleKey="title.exportPDF"
					onclick="doit.value=this.value;displayLoading();">
					<bean:message key="button.exportPDF" />
				</html:submit> 

				<tt:propertyEquals name="tmtbl.pdf.worksheet" value="true">
					<html:submit
						accesskey="W" styleClass="btn" titleKey="title.worksheetPDF"
						onclick="doit.value=this.value;displayLoading();">
						<bean:message key="button.worksheetPDF" />
					</html:submit>
				</tt:propertyEquals> 

				<html:submit
					accesskey="A" styleClass="btn" titleKey="title.addNewOffering"
					onclick="doit.value=this.value;">
					<bean:message key="button.addNew" />
				</html:submit>
				
			</TD>
		</TR>
		<TR>
			<TD colspan="5" align="center">
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

