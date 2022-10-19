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
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="tt" uri="http://www.unitime.org/tags-custom" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="loc" uri="http://www.unitime.org/tags-localization" %>
<script type="text/javascript" src="scripts/block.js"></script>
<loc:bundle name="CourseMessages"><s:set var="msg" value="#attr.MSG"/> 
<s:form action="classes">
	<s:if test="!form.sessions.isEmpty()">
	<s:hidden name="op" value="" id="op"/>
	<table class="unitime-MainTable">
	<TR>
		<TD>
			<tt:section-title><loc:message name="filter"/></tt:section-title>
		</TD>
	</TR>
	<TR>
		<TD>
			<loc:message name="filterTerm"/>
			<s:select name="form.session" list="form.sessions" listKey="value" listValue="label" id="sessionId"
				onchange="document.getElementById('op').value='Change';submit();"/>,&nbsp;&nbsp;&nbsp;
			<loc:message name="filterSubject"/>
			<s:select name="form.subjectArea" list="form.subjectAreas" listKey="value" listValue="label" id="subjectAbbv"
				headerKey="" headerValue="%{#msg.itemSelect()}"/>
				&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<loc:message name="filterCourseNumber"/>
			<tt:course-number name="form.courseNumber" title="%{#msg.tooltipCourseNumber()}" size="15"
				configuration="sessionId=\${sessionId};subjectAbbv=\${subjectAbbv};notOffered=exclude"/>
			&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
			<s:submit name='form.op' value="%{#msg.buttonApply()}" onclick="displayLoading();"/>
		</TD>
	</TR>
	</TABLE>
	</s:if>

	<BR>
	<s:if test="form.table == null">
		<table class="unitime-MainTable">
			<tr><td style='color:red;font-weight:bold;'>
				<s:property value="form.emptyMessage"/>
			</td></tr>
		</table>
	</s:if>
	<s:else>
		<table class="unitime-MainTable">
			<s:property value="form.table" escapeHtml="false"/>
		</table>
	</s:else>
	
	<s:if test="form.session != null">
	<tt:propertyEquals name="tmtbl.authentication.norole" value="true">
		<BR>
		<a id="login"></a>
		<TABLE class='unitime-Table' style="width:100%;">
		<TR>
			<TD>
				<tt:section-title><loc:message name="sectPersonalSchedule"/></tt:section-title>
			</TD>
		</TR>
 		<s:if test="form.message != null">
			<TR>
				<TD style='color:red;font-weight:bold;'>
					<s:property value="form.message"/>
				</TD>
			</TR>
		</s:if>
 		<s:if test="#request.message != null">
			<TR>
				<TD style='color:red;font-weight:bold;'>
					<s:property value="#request.message"/>
				</TD>
			</TR>
		</s:if>
		<TR>
			<TD>
				<loc:message name="propUserName"/>
				<s:textfield name="form.username" size="25"/>,&nbsp;&nbsp;&nbsp;
				<loc:message name="propUserPassword"/>
				<s:password name="form.password" size="25"/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
				<s:submit name='form.op' value="%{#msg.buttonLogIn()}" onclick="displayLoading();"/>
				<tt:hasProperty name="tmtbl.classes.login.message">
					&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
					<i><tt:property name="tmtbl.classes.login.message"/></i>
				</tt:hasProperty>
				<tt:notHasProperty name="tmtbl.classes.login.message">
					<tt:hasProperty name="tmtbl.exams.login.message">
						&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
						<i><tt:property name="tmtbl.exams.login.message"/></i>
					</tt:hasProperty>
				</tt:notHasProperty>
			</TD>
		</TR>
		</TABLE>
		<s:if test="form.message != null || #request.message != null">
			<SCRIPT type="text/javascript">
				location.hash = 'login';
			</SCRIPT>
		</s:if>
	</tt:propertyEquals>
	</s:if>
</s:form>
</loc:bundle>