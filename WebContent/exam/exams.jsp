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
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<tiles:importAttribute />
<html:form action="/exams">
	<logic:notEmpty name="examsForm" property="sessions">
	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
	<html:hidden property="op"/>
	<TR>
		<TD>
			<tt:section-title>Filter</tt:section-title>
		</TD>
	</TR>
	<TR>
		<TD>
			Term:
			<html:select property="session">
				<html:optionsCollection property="sessions" label="label" value="value"/>
			</html:select>,&nbsp;&nbsp;&nbsp;
			Exams:
			<html:select property="examType">
				<html:options collection="examTypes" property="uniqueId" labelProperty="label"/>
			</html:select>,&nbsp;&nbsp;&nbsp;
			Subject:
			<html:select property="subjectArea">
				<html:option value="">Select...</html:option>
				<logic:equal name="examsForm" property="canRetrieveAllExamForAllSubjects" value="true" >
					<html:option value="--ALL--">All</html:option>
				</logic:equal>
				<html:options property="subjectAreas"/>
			</html:select>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<html:submit onclick="op.value='Apply'; this.disabled=true; if (document.getElementById('s2')) document.getElementById('s2').disabled=true; displayLoading();form.submit()" styleId="s1" accesskey="A" value="Apply" title="Apply (Alt+A)"/>
		</TD>
	</TR>
	</TABLE>
	</logic:notEmpty>

	<BR>
	
	<logic:empty name="examsForm" property="table">
		<table width='100%' border='0' cellspacing='0' cellpadding='3'>
			<tr><td style='color:red;font-weight:bold;'>
				<logic:empty name="examsForm" property="session">
					There are no examinations available at the moment. 
				</logic:empty>
				<logic:notEmpty name="examsForm" property="session">
					<logic:empty name="examsForm" property="subjectArea">
						No subject area selected.
					</logic:empty>
					<logic:notEmpty name="examsForm" property="subjectArea">
						<logic:equal name="examsForm" property="subjectArea" value="--ALL--">
							There are no <bean:write name="examsForm" property="examTypeLabel"/> examinations available for <bean:write name="examsForm" property="sessionLabel"/> at the moment.
						</logic:equal>
						<logic:notEqual name="examsForm" property="subjectArea" value="--ALL--">
							There are no <bean:write name="examsForm" property="examTypeLabel"/> examinations available for <bean:write name="examsForm" property="subjectArea"/> subject area at the moment.
						</logic:notEqual>
					</logic:notEmpty>
				</logic:notEmpty>
			</td></tr>
		</table>
	</logic:empty>
	
	<logic:notEmpty name="examsForm" property="table">
		<table width='100%' border='0' cellspacing='0' cellpadding='3'>
			<bean:write name="examsForm" property="table" filter="false"/>
		</table>
	</logic:notEmpty>
	
	<logic:notEmpty name="examsForm" property="session">
	<tt:propertyEquals name="tmtbl.authentication.norole" value="true">
		<BR>
		<a name="login"></a>
		<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD>
				<tt:section-title>Personal Schedule</tt:section-title>
			</TD>
		</TR>
 		<logic:notEmpty name="examsForm" property="message">
			<TR>
				<TD style='color:red;font-weight:bold;'>
					<bean:write name="examsForm" property="message"/>
				</TD>
			</TR>
 		</logic:notEmpty>
 		<logic:notEmpty name="message" scope="request">
			<TR>
				<TD style='color:red;font-weight:bold;'>
					<bean:write name="message" scope="request"/>
				</TD>
			</TR>
 		</logic:notEmpty>
		<TR>
			<TD>
				User:
				<html:text property="username" size="25"/>,&nbsp;&nbsp;&nbsp;
				Password:
				<html:password property="password" size="25"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<html:submit onclick="op.value='Apply'; this.disabled=true; document.getElementById('s1').disabled=true; displayLoading();form.submit()" styleId="s2" accesskey="A" value="Apply" title="Apply (Alt+A)"/>
				<tt:hasProperty name="tmtbl.exams.login.message">
					&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
					<i><tt:property name="tmtbl.exams.login.message"/></i>
				</tt:hasProperty>
			</TD>
		</TR>
		</TABLE>
		<logic:notEmpty name="examsForm" property="message">
			<SCRIPT type="text/javascript" language="javascript">
				location.hash = 'login';
			</SCRIPT>
		</logic:notEmpty>
		<logic:notEmpty name="mesage" scope="request">
			<SCRIPT type="text/javascript" language="javascript">
				location.hash = 'login';
			</SCRIPT>
		</logic:notEmpty>
	</tt:propertyEquals>
	</logic:notEmpty>
</html:form>
