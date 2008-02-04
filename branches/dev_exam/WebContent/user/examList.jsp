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
<%@ page language="java" autoFlush="true"%>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles"%>
<%@ taglib uri="/WEB-INF/tld/struts-layout.tld" prefix="layout"%>
<tiles:importAttribute />
<html:form action="/examList">
	<TABLE border='0'>
		<TR>
			<TH valign="top">Subject:</TH>
			<TD valign="top">
				<html:select name="examListForm" property="subjectAreaId"
					onfocus="setUp();" 
					onkeypress="return selectSearch(event, this);" 
					onkeydown="return checkKey(event, this);" >
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:optionsCollection property="subjectAreas"	label="subjectAreaAbbreviation" value="uniqueId" />
				</html:select>
			</TD>
			<TH valign="top">Course Number:</TH>
			<TD valign="top">
				<layout:suggest 
					suggestAction="/getCourseNumbers" property="courseNbr" styleId="courseNbr" 
					suggestCount="15" size="5" maxlength="5" layout="false" all="true"
					minWordLength="2" 
					tooltip="Course numbers can be specified using wildcard (*). E.g. 2*"
					onblur="hideSuggestionList('courseNbr');" />
			</TD>
			<TD valign="top">
				&nbsp;&nbsp;&nbsp;
				<html:submit
					accesskey="S" styleClass="btn" titleKey="title.search" property="op"
					onclick="displayLoading();">
					<bean:message key="button.search" />
				</html:submit> 
			</TD>
			<TD valign="top">
				&nbsp;&nbsp;&nbsp;
				<html:submit
					accesskey="P" styleClass="btn" titleKey="title.exportPDF" property="op"
					onclick="displayLoading();">
					<bean:message key="button.exportPDF" />
				</html:submit> 
			</TD>
		</TR>
		<TR>
			<TD colspan="5" align="center">
				<html:errors />
			</TD>
		</TR>
	</TABLE>

	<logic:notEmpty scope="request" name="ExamList.table">
		<BR><BR>
		<TABLE width="99%" border="0" cellspacing="0" cellpadding="3">
			<bean:write scope="request" name="ExamList.table" filter="false"/>
		</TABLE>
	</logic:notEmpty>

	<logic:notEmpty scope="request" name="hash">
		<SCRIPT type="text/javascript" language="javascript">
			location.hash = '<%=request.getAttribute("hash")%>';
		</SCRIPT>
	</logic:notEmpty>
</html:form>