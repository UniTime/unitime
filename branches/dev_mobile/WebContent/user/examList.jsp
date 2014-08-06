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
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/struts-layout.tld" prefix="layout"%>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<tiles:importAttribute />
<html:form action="/examList">
	<TABLE border='0'>
		<TR>
			<TH valign="middle">Type:</TH>
			<TD valign="middle">
				<html:select name="examListForm" property="examType">
					<html:options collection="examTypes" property="uniqueId" labelProperty="label" />
				</html:select>
			</TD>
			<TH valign="middle">Subject:</TH>
			<TD valign="middle">
				<html:select name="examListForm" property="subjectAreaId"
					onfocus="setUp();" 
					onkeypress="return selectSearch(event, this);" 
					onkeydown="return checkKey(event, this);" >
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<sec:authorize access="hasPermission(null, null, 'DepartmentIndependent')">
						<html:option value="<%=Constants.ALL_OPTION_VALUE%>"><%=Constants.ALL_OPTION_LABEL%></html:option>
					</sec:authorize>
					<html:optionsCollection property="subjectAreas"	label="subjectAreaAbbreviation" value="uniqueId" />
				</html:select>
			</TD>
			<TH valign="middle">Course Number:</TH>
			<TD valign="middle">
				<layout:suggest 
					suggestAction="/getCourseNumbers" property="courseNbr" styleId="courseNbrText" 
					suggestCount="15" size="10" maxlength="10" layout="false" all="true"
					minWordLength="2" 
					tooltip="Course numbers can be specified using wildcard (*). E.g. 2*"
					onblur="blurSuggestionList('courseNbrText');" />
			</TD>
			<TD valign="middle" nowrap>
				&nbsp;&nbsp;&nbsp;
				<html:submit
					accesskey="S" styleClass="btn" titleKey="title.search" property="op"
					onclick="displayLoading();">
					<bean:message key="button.search" />
				</html:submit> 
			</TD>
			<TD valign="middle">
				<html:submit
					accesskey="P" styleClass="btn" titleKey="title.exportPDF" property="op">
					<bean:message key="button.exportPDF" />
				</html:submit> 
			</TD>
			<TD valign="middle">
				<html:submit
					accesskey="C" styleClass="btn" titleKey="title.exportCSV" property="op">
					<bean:message key="button.exportCSV" />
				</html:submit> 
			</TD>
			<sec:authorize access="hasPermission(null, 'Session', 'ExaminationAdd')">
			<TD valign="middle">
				<html:submit
					accesskey="A" styleClass="btn" titleKey="title.addExam" property="op"
					onclick="displayLoading();">
					<bean:message key="button.addExam" />
				</html:submit> 
			</TD>
			</sec:authorize>
		</TR>
		<TR>
			<TD colspan="5" align="center">
				<html:errors />
			</TD>
		</TR>
	</TABLE>

	<logic:notEmpty scope="request" name="ExamList.table">
		<BR><BR>
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
			<bean:write scope="request" name="ExamList.table" filter="false"/>
		</TABLE>
	</logic:notEmpty>

	<logic:notEmpty scope="request" name="hash">
		<SCRIPT type="text/javascript" language="javascript">
			location.hash = '<%=request.getAttribute("hash")%>';
		</SCRIPT>
	</logic:notEmpty>
</html:form>
