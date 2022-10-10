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
<%@ taglib prefix="loc" uri="http://www.unitime.org/tags-localization" %>
<script type="text/javascript" src="scripts/block.js"></script>
<loc:bundle name="CourseMessages"><s:set var="msg" value="#attr.MSG"/> 
<s:form action="manageSolvers">
	<s:iterator value="solverTypes" var="type">
		<table class="unitime-MainTable" style="padding-bottom: 20px;">
			<s:property value="getSolverTable(#type)" escapeHtml="false"/>
		</table>	
	</s:iterator>
	<table class="unitime-MainTable" style="padding-bottom: 20px;">
		<s:property value="onlineSolvers" escapeHtml="false"/>
	</table>
	<s:if test="hasServers() == true">
		<table class="unitime-MainTable">
			<s:property value="servers" escapeHtml="false"/>
		</table>
	</s:if>
	<table class="unitime-MainTable">
		<TR>
			<TD colspan='2'><DIV class="WelcomeRowHeadBlank">&nbsp;</DIV></TD>
		</TR>
		<TR>
			<TD align='right'>
				<s:if test="canDeselect() == true">
					<s:submit name="op" value="%{deselect}" onclick="displayLoading();"/>
				</s:if>
				<s:submit name="op" value="%{#msg.buttonRefresh()}" onclick="displayLoading();"/>
			</TD>
		</TR>
	</table>
</s:form>
</loc:bundle>

