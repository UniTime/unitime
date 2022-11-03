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
<loc:bundle name="CourseMessages">
<s:form action="selectPrimaryRole" id="form" target="_top">
	<s:hidden name="form.authority" id="authority"/>
	<s:hidden name="form.target"/>
	<table class="unitime-MainTable">
		<s:if test="list != \"Y\"">
			<tr><td colspan='4'><i><loc:message name="infoNoDefaultAuthority"/><br><br></i></td></tr>
		</s:if>
		<s:property value="#request.userRoles" escapeHtml="false"/>
	</table>
</s:form>
</loc:bundle>