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
<loc:bundle name="CourseMessages"><s:set var="msg" value="#attr.MSG"/>
<s:form action="sessionEdit">
	<table class="unitime-MainTable">
		<tr>
			<td>
				<tt:section-header>
					<tt:section-title>
					</tt:section-title>
					<sec:authorize access="hasPermission(null, null, 'AcademicSessionAdd')">
						<s:submit name='op' value='%{#msg.actionAddAcademicSession()}'
							accesskey="%{#msg.accessAddAcademicSession()}" title="%{#msg.titleAddAcademicSession(#msg.accessAddAcademicSession())}"/>
					</sec:authorize>
				</tt:section-header>
			</td>
		</tr>
	</table>
	<table class="unitime-MainTable">
		<s:property value="#request.table" escapeHtml="false"/>
	</table>
	<table class="unitime-MainTable">
		<tr>
			<td align="center" class="WelcomeRowHead">
			&nbsp;
			</td>
		</tr>
		<tr>
			<td align="right">
				<sec:authorize access="hasPermission(null, null, 'AcademicSessionAdd')">
					<s:submit name='op' value='%{#msg.actionAddAcademicSession()}'
						accesskey="%{#msg.accessAddAcademicSession()}" title="%{#msg.titleAddAcademicSession(#msg.accessAddAcademicSession())}"/>
				</sec:authorize>
			</td>
		</tr>
	</table>
</s:form>
</loc:bundle>