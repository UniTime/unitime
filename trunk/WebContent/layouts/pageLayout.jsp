<!DOCTYPE html>
<%-- 
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 --%>
<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8" errorPage="/error.jsp"%>
<%@ page import="org.unitime.timetable.util.Constants"%>
<%@ page import="org.unitime.localization.impl.Localization"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld"	prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld"	prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
	
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
    <meta name="gwt:property" content="locale=<%=Localization.getFirstLocale()%>">
    <meta charset="UTF-8"/>
	<style type="text/css">@import url(scripts/jscalendar/calendar-blue.css);</style>
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
    <script language="JavaScript" type="text/javascript" src="scripts/validator.js"></script>
	<script language="JavaScript" type="text/javascript" src="scripts/jscalendar/calendar.js"></script>
	<script language="JavaScript" type="text/javascript" src="scripts/jscalendar/lang/calendar-en.js"></script>
	<script language="JavaScript" type="text/javascript" src="scripts/jscalendar/calendar-setup.js"></script>
	<script language="JavaScript" type="text/javascript" src="scripts/select.js"></script>
	<script language="JavaScript" type="text/javascript" src="scripts/rtt.js"></script>
	<script type="text/javascript" language="javascript" src="unitime/unitime.nocache.js"></script>
</head>
<body class="unitime-Body" <tiles:getAsString name="onLoadFunction" />>
	<tt:form-factor value="unknown"><span id='UniTimeGWT:DetectFormFactor' style="display: none;"></span></tt:form-factor>
	<script language="JavaScript" type="text/javascript">
		if (!String.prototype.trim) {
			String.prototype.trim = function() {
				return this.replace(/^\s+|\s+$/g,"");
			};
		}
	</script>
    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex="-1" style="position:absolute;width:0;height:0;border:0"></iframe>
    <iframe src="javascript:''" id="__printingFrame" tabIndex="-1" style="position:absolute;width:0;height:0;border:0"></iframe>
    
    <tt:form-factor value="desktop">
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
    </tt:form-factor>
    
    <tt:hasProperty name="tmtbl.global.warn">
    	<div class='unitime-PageWarn'><tt:property name="tmtbl.global.warn"/></div>
	</tt:hasProperty>
	<tt:page-warning prefix="tmtbl.page.warn." style="unitime-PageWarn"/>
	<tt:page-warning prefix="tmtbl.page.info." style="unitime-PageMessage"/>
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
	
<tt:form-factor value="mobile">
	<span class="unitime-MobilePage">
	<span class="unitime-MobilePageHeader">
		<span class="row">
			<logic:equal name="showMenu" value="true">
				<span id='UniTimeGWT:MobileMenu' class="menu"></span>
			</logic:equal>
			<span class="logo"><a href='main.jsp' tabIndex="-1">
				<tt:form-factor value="phone"><img src="images/unitime-phone.png" border="0"/></tt:form-factor>
				<tt:form-factor value="tablet"><img src="images/unitime-tablet.png" border="0"/></tt:form-factor>
			</a></span>
			<span id='UniTimeGWT:Title' class="title"><bean:write name="title" scope="request"/></span>
		</span>
	</span>
	<logic:equal name="showMenu" value="true">
		<span class='unitime-MobileHeader'><span id='UniTimeGWT:Header' class="unitime-InfoPanel"></span></span>
	</logic:equal>
	<span id='UniTimeGWT:TitlePanel' class="unitime-MobileNavigation"><tiles:insert attribute="header"><tiles:put name="showNavigation" value="${showNavigation}"/></tiles:insert></span>
	<span id='UniTimeGWT:Content'>
		<tiles:insert attribute="body">
			<tiles:put name="body2" value="${body2}"/>
			<tiles:put name="action2" value="${action2}"/>
		</tiles:insert>
	</span>
	<span class="unitime-MobileFooter">
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
		<span class='unitime-MobileDisclaimer'><tt:property name="tmtbl.page.disclaimer"/></span>
	</tt:hasProperty>
	</span>
</tt:form-factor>
<tt:form-factor value="desktop">
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
    			<span class="logo"><a href='main.jsp' tabIndex="-1"><img src="images/unitime.png" border="0"/></a></span>
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
	
</tt:form-factor>
</body>
</html:html>
