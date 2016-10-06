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
<%@ taglib uri="http://struts.apache.org/tags-bean"	prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html"	prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.unitime.org/tags-localization" prefix="loc" %>
	
<tiles:importAttribute name="checkLogin" scope="request"/>
<tiles:importAttribute name="checkRole" scope="request"/>
<tiles:importAttribute name="checkAdmin" scope="request"/>
<tiles:importAttribute name="showMenu" scope="request"/>

<logic:equal name="checkLogin" value="true">
	<%@ include file="/checkLogin.jspf"%>
</logic:equal>
<logic:equal name="checkRole" value="true">
	<%@ include file="/checkRole.jspf"%>
</logic:equal>
<logic:equal name="checkAdmin" value="true">
	<%@ include file="/checkAdmin.jspf"%>
</logic:equal>

<html:html>
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
	<link rel="stylesheet" type="text/css" href="styles/timetabling.css">
    <tt:hasProperty name="tmtbl.custom.css">
    	<link rel="stylesheet" type="text/css" href="%tmtbl.custom.css%" />
    </tt:hasProperty>
    <link rel="shortcut icon" href="images/timetabling.ico" />
	<title>UniTime <%=Constants.VERSION%>| <tiles:getAsString name="title" /></title>
    <script language="JavaScript" type="text/javascript" src="scripts/loading.js"></script>
	<script language="JavaScript" type="text/javascript" src="scripts/rtt.js"></script>
	<script type="text/javascript" language="javascript" src="unitime/unitime.nocache.js"></script>
</head>
<body class="unitime-Body" <tiles:getAsString name="onLoadFunction" />>
	<loc:bundle name="org.unitime.timetable.gwt.resources.GwtMessages" id="GWTMSG">
	<script language="JavaScript" type="text/javascript">
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
    <logic:equal name="showMenu" value="true">
    	<tt:notHasProperty name="unitime.menu.style" user="true">
	    	<span id='UniTimeGWT:DynamicTopMenu' style="display: block; height: 23px;"></span>
    	</tt:notHasProperty>
    	<tt:propertyEquals name="unitime.menu.style" user="true" value="Dynamic On Top">
    		<span id='UniTimeGWT:DynamicTopMenu' style="display: block; height: 23px;"></span>
    	</tt:propertyEquals>
    	<tt:propertyEquals name="unitime.menu.style" user="true" value="Static On Top">
    		<span id='UniTimeGWT:TopMenu' style="display: block; height: 23px;"></span>
    	</tt:propertyEquals>
    </logic:equal>
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
	<logic:notEmpty name="<%=Constants.REQUEST_WARN%>" scope="request">
		<div class='unitime-PageWarn'><bean:write name="<%=Constants.REQUEST_WARN%>" scope="request"/></div>
	</logic:notEmpty>
	<logic:notEmpty name="<%=Constants.REQUEST_MSSG%>" scope="request">
		<div class='unitime-PageMessage'><bean:write name="<%=Constants.REQUEST_MSSG%>" scope="request"/></div>
	</logic:notEmpty>
	<tiles:importAttribute name="showSolverWarnings" scope="request"/>
	<logic:notEqual name="showSolverWarnings" value="none">
		<tt:solver-warnings><bean:write name="showSolverWarnings"/></tt:solver-warnings>
	</logic:notEqual>
	<tt:offering-locks/>
		
	<tiles:importAttribute/>
	<tiles:importAttribute name="title" scope="request"/>
	<tiles:importAttribute name="showNavigation" scope="request"/>
	
	<span class="unitime-Page"><span class='row'>
	<span class='sidebar' id="unitime-SideMenu">
    	<logic:equal name="showMenu" value="true">
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
	    </logic:equal>
    <script language="JavaScript" type="text/javascript">
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
    			<a href='main.jsp' tabIndex="-1" class="logo"></a>
    			<span class="content">
					<span id='UniTimeGWT:Title' class="title"><bean:write name="title" scope="request"/></span>
					<logic:equal name="showMenu" value="true">
						<span class='unitime-Header'><span id='UniTimeGWT:Header' class="unitime-InfoPanel"></span></span>
					</logic:equal>
	    			<span id='UniTimeGWT:TitlePanel' class='navigation'>
	    				<tiles:insert attribute="header">
	    					<tiles:put name="showNavigation" value="${showNavigation}"/>
						</tiles:insert>
					</span>
				</span>
			</span>
		</span>
		<span class="mobile-menu" id='UniTimeGWT:MobileMenuPanel'></span>
		<span class='content'>
        	<span id='UniTimeGWT:Content'>
	    		<tiles:insert attribute="body">
					<tiles:put name="body2" value="${body2}"/>
					<tiles:put name="action2" value="${action2}"/>
				</tiles:insert>
        	</span>
        </span>
    </span><span class='footer' id="unitime-Footer">
		<span class="unitime-Footer">
			<span class="row">
				<span class="cell left">
					<span id='UniTimeGWT:Version'></span>
					<tt:time-stamp/>
				</span>
    			<%-- WARNING: Changing or removing the copyright notice will violate the license terms. If you need a different licensing, please contact us at support@unitime.org --%>
				<span class="cell middle"><tt:copy/></span>
				<span class="cell right"><tt:registration/></span>
			</span>
		</span>
		<tt:hasProperty name="tmtbl.page.disclaimer">
			<span class='unitime-Disclaimer'><tt:property name="tmtbl.page.disclaimer"/></span>
		</tt:hasProperty>
	</span>
</span></span></span>
	
</body>
</html:html>
