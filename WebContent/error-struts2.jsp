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
<table class="unitime-MainTable" style="max-width: 800px;">
	<tr><td colspan="2">
	<% try { %>
		<div class="WelcomeRowHead"><loc:message name="propError"/> <font color="#FF0000"><s:property value="exception.message"/></font></div>
	<% } catch (Exception e) { %>
		<div class="WelcomeRowHead"><loc:message name="propError"/> <font color="#FF0000"><s:property value="exception"/></font></div>
	<% } %>
	</td></tr>
	<tr>
		<td><loc:message name="propQuery"/></td><td style="word-break: break-word;"><s:property value="URL"/></td>
	</tr>		
	<tr align="left" valign="top">
		<td><loc:message name="propStackTrace"/></td><td style="white-space: pre; color: #898989;"><s:property value="exceptionStack"/></td>
	</tr>
</table>
</loc:bundle>