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
<%@ page import="org.unitime.timetable.util.Constants"%>
<%@ page import="org.unitime.localization.impl.Localization"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="tt" uri="http://www.unitime.org/tags-custom" %>
<%@ taglib prefix="loc" uri="http://www.unitime.org/tags-localization" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core_rt" %>
<loc:bundle name="CourseMessages">
<html>
<head>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">
	<meta http-equiv="X-UA-Compatible" content="IE=Edge">
	<meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="gwt:property" content="locale=<%=Localization.getFirstLocale()%>">
    <meta charset="UTF-8"/>
	<link type="text/css" rel="stylesheet" href="unitime/gwt/standard/standard.css">
    <link type="text/css" rel="stylesheet" href="styles/unitime.css">
    <link type="text/css" rel="stylesheet" href="styles/unitime-mobile.css">
    <!--[if IE]>
	    <link type="text/css" rel="stylesheet" href="styles/unitime-ie.css">
    <![endif]-->
    <loc:rtl><link type="text/css" rel="stylesheet" href="styles/unitime-rtl.css"></loc:rtl>
	<link rel="stylesheet" type="text/css" href="styles/timetabling.css">
    <tt:hasProperty name="tmtbl.custom.css">
    	<link rel="stylesheet" type="text/css" href="%tmtbl.custom.css%" />
    </tt:hasProperty>
    <link rel="shortcut icon" href="images/timetabling.ico" />
	<title>UniTime <%=Constants.VERSION%>| <loc:message name="pageLogIn"/></title>
    <script type="text/javascript" src="scripts/loading.js"></script>
	<script type="text/javascript" src="scripts/rtt.js"></script>
	<script type="text/javascript" src="unitime/unitime.nocache.js"></script>
</head>
<body class="unitime-Body" onload="document.getElementById('username').focus();">
	<s:if test="externalHeader != null && !externalHeader.isEmpty()">
		<s:include value="%{externalHeader}"/>
	</s:if>
	<span class='top-menu'>
    	<span id='UniTimeGWT:TopMenu' style="display: block; height: 23px;"></span>
    </span>
	<tt:hasProperty name="tmtbl.global.info">
    	<div class='unitime-PageMessage'><tt:property name="tmtbl.global.info"/></div>
	</tt:hasProperty>
	<tt:hasProperty name="tmtbl.global.warn">
    	<div class='unitime-PageWarn'><tt:property name="tmtbl.global.warn"/></div>
	</tt:hasProperty>
	<tt:hasProperty name="tmtbl.global.error">
    	<div class='unitime-PageError'><tt:property name="tmtbl.global.error"/></div>
	</tt:hasProperty>
	<tt:page-warning prefix="tmtbl.page.warn." style="unitime-PageWarn" page="login"/>
	<tt:page-warning prefix="tmtbl.page.info." style="unitime-PageMessage" page="login"/>
	<tt:page-warning prefix="tmtbl.page.error." style="unitime-PageError" page="login"/>
<form action="login" name="f" method="POST">
	<INPUT type="hidden" name="cs" value="login">
	<s:hidden name="menu"/>
	<s:hidden name="target"/>
	<div class='unitime-Login'>
		<span class="mobile-menu-button" id='UniTimeGWT:MobileMenuButton'></span>
		<span class='logo'><img src="images/unitime.png" border="0" alt="UniTime"></span>
		<span class='header'>
			<span class='h1'><loc:message name="pageLogInH1"/></span>
			<span class='h2'><loc:message name="pageLogInH2"/></span>
		</span>
		<span class="mobile-menu" id='UniTimeGWT:MobileMenuPanel'></span>
		<s:if test="errorMsg != null"><div class='error'><s:property value="errorMsg"/></div></s:if>
		<c:if test="${not empty SPRING_SECURITY_LAST_EXCEPTION.message}">
			<div class='error'><loc:message name="errorAuthenticationFailedWithReason"><c:out value="${SPRING_SECURITY_LAST_EXCEPTION.message}"/></loc:message></div>
		</c:if>
		<span class="login-outer-box">
			<span class="login-box">
				<span class="table">
					<span class="row">
						<span class="left-cell"><loc:message name="propertyUsername"/></span>
						<span class="right-cell">
							<input type='text' name='username' value='<c:if test="${not empty SPRING_SECURITY_LAST_USERNAME}"><c:out value="${SPRING_SECURITY_LAST_USERNAME}"/></c:if>' aria-label='${MSG.ariaEnterUserName()}' id='username'/>
						</span>
					</span>
					<span class="row">
						<span class="left-cell"><loc:message name="propertyPassword"/></span>
						<span class="right-cell">
							<input type='password' name='password' aria-label='${MSG.ariaEnterPassword()}'>
						</span>
					</span>
				</span>
				<span style="display: block; text-align:center; padding: 10px 5px 0px 5px; font-size: larger;">
					<input type="submit" value="${MSG.actionLogIn()}" aria-label="${MSG.ariaLogIn()}">
				</span>
			</span>
		</span>
		<c:if test="${SUGGEST_PASSWORD_RESET}">
			<span class='forgot'><a href='gwt.jsp?page=password&reset=1' class='unitime-FooterLink'><loc:message name="linkForgotYourPassword"/></a></span>
		</c:if>
	</div>
</form>
	<s:if test="hasInitializationError">
		<div class='unitime-InitializationError'>
			<loc:message name="errorUniTimeFailedToStart"><%=Constants.getVersion()%></loc:message><br/>
			<s:property value="%{printInitializationError()}" escapeHtml="false"/>
		</div>
	</s:if>
	<span class="unitime-Footer">
		<span class="row">
			<span class="cell middle">
				<span id='UniTimeGWT:Version'></span>
				<tt:copy br="false"/>
			</span>
		</span>
	</span>
	<tt:hasProperty name="tmtbl.page.disclaimer">
		<span class='unitime-Disclaimer'><tt:property name="tmtbl.page.disclaimer"/></span>
	</tt:hasProperty>
	<s:if test="externalFooter != null && !externalFooter.isEmpty()">
		<s:include value="%{externalFooter}"/>
	</s:if>
</body>
</html>
</loc:bundle>