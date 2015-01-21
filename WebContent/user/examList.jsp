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
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
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
				<html:select name="examListForm" property="subjectAreaId" styleId="subjectId" >
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<sec:authorize access="hasPermission(null, null, 'DepartmentIndependent')">
						<html:option value="<%=Constants.ALL_OPTION_VALUE%>"><%=Constants.ALL_OPTION_LABEL%></html:option>
					</sec:authorize>
					<html:optionsCollection property="subjectAreas"	label="subjectAreaAbbreviation" value="uniqueId" />
				</html:select>
			</TD>
			<TH valign="middle">Course Number:</TH>
			<TD valign="middle">
				<tt:course-number property="courseNbr" configuration="subjectId=\${subjectId};notOffered=exclude" size="10"
					title="Course numbers can be specified using wildcard (*). E.g. 2*"/>
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
