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
<table class="unitime-MainTable">
	<s:set var="colspan" value="%{chartWindows.length}"/>
	<s:iterator value="chartWindows" var="ch">
		<tr><td colspan="${colspan}">
			<tt:section-title><s:property value="#ch.name"/></tt:section-title>
		</td></tr>
		<tr>
		<s:iterator value="chartTypes" var="t">
			<s:set var="url" value="%{getChartUrl(#ch,#t)}"/>
			<td><img src="${url}" border="0"/></td>
		</s:iterator>
		</tr>
	</s:iterator>
</table>
<table class="unitime-MainTable">
	<s:property value="queryTable" escapeHtml="false"/>
</table>