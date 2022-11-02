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
<%@ page import="org.unitime.timetable.util.Constants"%>
<%@ page import="org.unitime.localization.impl.Localization"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="tt" uri="http://www.unitime.org/tags-custom" %>
<%@ taglib prefix="loc" uri="http://www.unitime.org/tags-localization"%>

<tiles:importAttribute name="showMenu" scope="request"/>
<tiles:importAttribute name="checkLogin" scope="request"/>
<s:if test="#request.checkLogin">
	<%@ include file="/checkLogin.jspf"%>
</s:if>
<tiles:importAttribute name="checkRole" scope="request"/>
<s:if test="#request.checkRole">
	<%@ include file="/checkRole.jspf"%>
</s:if>
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
	<title>UniTime <%=Constants.VERSION%>| <tiles:getAsString name="title" /></title>
    <script type="text/javascript" src="scripts/loading.js"></script>
	<script type="text/javascript" src="scripts/rtt.js"></script>
	<script type="text/javascript" src="unitime/unitime.nocache.js"></script>
</head>
<body class="unitime-Body">
	<loc:bundle name="org.unitime.timetable.gwt.resources.GwtMessages" id="GWTMSG">
	<script type="text/javascript">
		if (!String.prototype.trim) {
			String.prototype.trim = function() {
				return this.replace(/^\s+|\s+$/g,"");
			};
		}
		setPageLoadingMessage("<%=GWTMSG.waitLoadingPage().replace("\"", "\"\"")%>");
	</script>
	</loc:bundle>
    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex="-1" style="position:absolute;width:0;height:0;border:0"></iframe>
    <iframe src="javascript:''" id="__printingFrame" tabIndex="-1" style="position:absolute;width:0;height:0;border:0"></iframe>
    
    <span class='top-menu'>
    <s:if test="#request.showMenu"> 
    	<tt:notHasProperty name="unitime.menu.style" user="true">
	    	<span id='UniTimeGWT:DynamicTopMenu' style="display: block; height: 23px;"></span>
    	</tt:notHasProperty>
    	<tt:propertyEquals name="unitime.menu.style" user="true" value="Dynamic On Top">
    		<span id='UniTimeGWT:DynamicTopMenu' style="display: block; height: 23px;"></span>
    	</tt:propertyEquals>
    	<tt:propertyEquals name="unitime.menu.style" user="true" value="Static On Top">
    		<span id='UniTimeGWT:TopMenu' style="display: block; height: 23px;"></span>
    	</tt:propertyEquals>
    </s:if>
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
	<tt:page-warning prefix="tmtbl.page.warn." style="unitime-PageWarn"/>
	<tt:page-warning prefix="tmtbl.page.info." style="unitime-PageMessage"/>
	<tt:page-warning prefix="tmtbl.page.error." style="unitime-PageError"/>
	<tt:page-file/>
	<s:if test="#request.RqWarn != null">
		<div class='unitime-PageWarn'><s:property value="#request.RqWarn"/></div>
	</s:if>
	<s:if test="#request.RqMsg != null">
		<div class='unitime-PageMessage'><s:property value="#request.RqMsg"/></div>
	</s:if>
	<tiles:importAttribute name="showSolverWarnings" scope="request"/>
	<s:if test="#request.showSolverWarnings != 'none'">
		<tt:solver-warnings><s:property value="#request.showSolverWarnings"/></tt:solver-warnings>
	</s:if>
	<tt:offering-locks/>
		
	<span class="unitime-Page"><span class='row'>
	<span class='sidebar' id="unitime-SideMenu">
    	<s:if test="#request.showMenu">
    		<tt:propertyEquals name="unitime.menu.style" user="true" value="Stack On Side">
    			<span id='UniTimeGWT:SideStackMenu' style="display: block;" ></span>
	    	</tt:propertyEquals>
    		<tt:propertyEquals name="unitime.menu.style" user="true" value="Tree On Side">
    			<span id='UniTimeGWT:SideTreeMenu' style="display: block;" ></span>
	    	</tt:propertyEquals>
    		<tt:propertyEquals name="unitime.menu.style" user="true" value="Static Stack On Side">
    			<span id='UniTimeGWT:StaticSideStackMenu' style="display: block;" ></span>
		    </tt:propertyEquals>
    		<tt:propertyEquals name="unitime.menu.style" user="true" value="Static Tree On Side">
    			<span id='UniTimeGWT:StaticSideTreeMenu' style="display: block;" ></span>
		    </tt:propertyEquals>
    		<tt:propertyEquals name="unitime.menu.style" user="true" value="Dynamic Stack On Side">
    			<span id='UniTimeGWT:SideStackMenu' style="display: block;" ></span>
		    </tt:propertyEquals>
    		<tt:propertyEquals name="unitime.menu.style" user="true" value="Dynamic Tree On Side">
    			<span id='UniTimeGWT:SideTreeMenu' style="display: block;" ></span>
		    </tt:propertyEquals>
	    </s:if>
    <script type="text/javascript">
    	var sideMenu = document.getElementById("unitime-SideMenu").getElementsByTagName("span");
    	if (sideMenu.length > 0) {
    		var c = unescape(document.cookie);
    		var c_start = c.indexOf("UniTime:SideBar=");
    		if (c_start >= 0) {
    			c_start = c.indexOf("|W:", c_start) + 3;
    			var c_end = c.indexOf(";", c_start);
    			if (c_end < 0) c_end=c.length;
    			var width = c.substring(c_start, c_end);
    			sideMenu[0].style.width = width + "px";
    			// alert(c.substring(c.indexOf("UniTime:SideBar=") + 16, c_end));
    		} else {
    			sideMenu[0].style.width = (sideMenu[0].id.indexOf("StackMenu") >= 0 ? "172px" : "152px");
    		}
    	}
    </script>
	</span>
    <span class='main'><span class='body' id="unitime-Page">
    	<span class="unitime-PageHeader" id="unitime-Header">
    		<span class="row">
    			<span class="mobile-menu-button" id='UniTimeGWT:MobileMenuButton'></span>
    			<a href='main.action' tabIndex="-1" class="logo"></a>
    			<span class="content">
					<span id='UniTimeGWT:Title' class="title"><tiles:getAsString name="title" /></span>
					<s:if test="#request.showMenu">
						<span class='unitime-Header'><span id='UniTimeGWT:Header' class="unitime-InfoPanel"></span></span>
					</s:if>
	    			<span id='UniTimeGWT:TitlePanel' class='navigation'>
	    				<tiles:importAttribute name="showNavigation" scope="request"/>	
	    				<tiles:insertAttribute name="header">
	    					<tiles:putAttribute name="showNavigation" value="#request.showNavigation"/>
	    				</tiles:insertAttribute>
					</span>
				</span>
			</span>
		</span>
		<span class="mobile-menu" id='UniTimeGWT:MobileMenuPanel'></span>
		<span class='content unitime-Struts2'>
        	<span id='UniTimeGWT:Content'>
	    		<tiles:insertAttribute name="body"/>
        	</span>
        </span>
    </span><span class='footer' id="unitime-Footer">
		<span class="unitime-Footer">
			<span class="row">
				<span class="cell left">
					<span id='UniTimeGWT:Version'></span>
					<tiles:importAttribute name="includeTimeStamp" scope="request"/>
					<s:if test="#request.includeTimeStamp">
						<tt:time-stamp/>
					</s:if>
				</span>
    			<%-- WARNING: Changing or removing the copyright notice will violate the license terms. If you need a different licensing, please contact us at support@unitime.org --%>
				<span class="cell middle"><tt:copy/></span>
				<tiles:importAttribute name="updateRegistration" scope="request"/>
				<s:if test="#request.updateRegistration">
					<span class="cell right"><tt:registration update="true"/></span>
				</s:if>
				<s:else>
					<span class="cell right"><tt:registration/></span>
				</s:else>
			</span>
		</span>
		<tt:hasProperty name="tmtbl.page.disclaimer">
			<span class='unitime-Disclaimer'><tt:property name="tmtbl.page.disclaimer"/></span>
		</tt:hasProperty>
	</span>
</span></span></span>
	
</body>
</html>