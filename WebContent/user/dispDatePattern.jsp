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
<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8" errorPage="/error.jsp"%>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="loc" uri="http://www.unitime.org/tags-localization" %>
<loc:bundle name="CourseMessages"><s:set var="msg" value="#attr.MSG"/>
<s:set var="dp" value="datePattern"/>
<html>
  <head>
    <title>
    	<loc:message name="sectPreviewOfDatePattern"><s:property value="#dp.name"/></loc:message>
    </title>
    <link rel="stylesheet" type="text/css" href="styles/timetabling.css">
    <script type='text/javascript' src='scripts/datepatt.js'></script>
  </head>
  <body class="bodyStyle">
  	<table class="unitime-MainTable"><tr>
  		<s:if test="#dp != null">
  			<td align='center'>
  				<s:property value="#dp.getPatternHtml(false,null,false)" escapeHtml="false"/>
  			</td>
  		</s:if><s:else>
  			<td align='center' style="padding: 20px;">
  				<i><loc:message name="infoNoDefaultDatePattern"/></i>
  			</td>
  		</s:else>
	</tr><tr>
		<td align='center'>
			<tt:displayPrefLevelLegend prefs="false" dpOffered="true" dpBackgrounds="true" separator="top"/>
		</td>
	</tr></table>
  </body>
</html>
</loc:bundle>