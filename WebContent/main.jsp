<!DOCTYPE html>
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
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="tt" uri="http://www.unitime.org/tags-custom" %>
<%@ taglib prefix="loc" uri="http://www.unitime.org/tags-localization" %>
<loc:bundle name="CourseMessages">
<s:set var="showBackground" value="true"/>
<s:if test="message != null && !message.isEmpty()">
	<s:set var="showBackground" value="false"/>
	<div class='messages'>
		<div class='WelcomeRowHead'><loc:message name="sectSystemMessages"/></div>
		<div class='message'><s:property value="message" escapeHtml="false"/></div>
	</div>
</s:if>
<tt:registration method="hasMessage">
	<s:set var="showBackground" value="false"/>
	<div class='messages'>
		<div class='WelcomeRowHead'><loc:message name="sectRegistrationMessages"/></div>
		<div class='message'><tt:registration method="message"/></div>
	</div>
</tt:registration>
<s:if test="hasInitializationError">
	<s:set var="showBackground" value="false"/>
	<div class='messages'>
		<div class='WelcomeRowHead' style="color: red;"><loc:message name="errorUniTimeFailedToStart"><%=Constants.getVersion()%></loc:message></div>
		<div class='message'><s:property value="%{printInitializationError()}" escapeHtml="false"/></div>
	</div>
</s:if>
<s:if test="#showBackground == true">
	<script type="text/javascript">
		document.getElementById('unitime-Page').className = 'body unitime-MainLogoFaded';
		document.getElementById('UniTimeGWT:Content').className = 'unitime-MainContent unitime-MainLogo';
	</script>
</s:if>
<s:else>
	<script type="text/javascript">
		document.getElementById('unitime-Page').className = 'body unitime-MainLogoFaded';
		document.getElementById('UniTimeGWT:Content').className = 'unitime-MainContent';
	</script>
</s:else>
<script type="text/javascript">
	if (parent && parent.hideGwtDialog && parent.refreshPage) {
		parent.hideGwtDialog();
		parent.refreshPage();
	}
</script>
</loc:bundle>