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
<%@ page import="org.unitime.timetable.ApplicationProperties"%>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>

<HTML>
<head>
    <meta charset="UTF-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=Edge">
	<title>UniTime <%=Constants.VERSION%>| University Timetabling Application</title>
	<link rel="shortcut icon" href="images/timetabling.ico" />
	<link type="text/css" rel="stylesheet" href="unitime/gwt/standard/standard.css">
    <link type="text/css" rel="stylesheet" href="styles/unitime.css">
    <link type="text/css" rel="stylesheet" href="styles/unitime-mobile.css">
    <link type="text/css" rel="stylesheet" href="styles/timetabling.css">
    <tt:hasProperty name="tmtbl.custom.css">
		<LINK rel="stylesheet" type="text/css" href="%tmtbl.custom.css%" />
    </tt:hasProperty>
    <script language="JavaScript" type="text/javascript" src="scripts/loading.js"></script>
    <script type="text/javascript" language="javascript" src="unitime/unitime.nocache.js"></script>
</head>
<BODY class="unitime-Body">
	<tt:form-factor value="unknown"><span id='UniTimeGWT:DetectFormFactor' style="display: none;">true</span></tt:form-factor>
    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex="-1" style="position:absolute;width:0;height:0;border:0"></iframe>
    <iframe src="javascript:''" id="__printingFrame" tabIndex="-1" style="position:absolute;width:0;height:0;border:0"></iframe>

	<tt:form-factor value="desktop">    
    <tt:notHasProperty name="unitime.menu.style" user="true">
	   	<span id='UniTimeGWT:DynamicTopMenu' style="display: block; height: 23px;" ></span>
    </tt:notHasProperty>
    <tt:propertyEquals name="unitime.menu.style" user="true" value="Dynamic On Top">
    	<span id='UniTimeGWT:DynamicTopMenu' style="display: block; height: 23px;" ></span>
    </tt:propertyEquals>
    <tt:propertyEquals name="unitime.menu.style" user="true" value="Static On Top">
    	<span id='UniTimeGWT:TopMenu' style="display: block; height: 23px;" ></span>
    </tt:propertyEquals>
    </tt:form-factor>

    <tt:hasProperty name="tmtbl.global.warn">
    	<div class='unitime-PageWarn'><tt:property name="tmtbl.global.warn"/></div>
	</tt:hasProperty>
	<tt:offering-locks/>
  	
<%
	String sysMessage = ApplicationProperties.getProperty("tmtbl.system_message");
	boolean showBackground = (sysMessage == null || sysMessage.trim().isEmpty());
	if ("cas-logout".equals(request.getParameter("op"))) {
		sysMessage = "You have been successfully logged out of UniTime, click <a href='j_spring_cas_security_logout'>here</a> to log out of all other applications as well."; 
	}
%>
<tt:registration method="hasMessage">
	<% showBackground = false; %>
</tt:registration>

<tt:form-factor value="mobile">
	<span class="unitime-MobilePage">
	<span class='body' style="display:block;background-image:url('images/logofaded.jpg');backbackground-repeat:no-repeat;background-position: center; margin-bottom: -10px;">
	<span class="unitime-MobilePageHeader">
		<span class="row">
			<span id='UniTimeGWT:MobileMenu' class="menu"></span>
			<span class="logo"><a href='main.jsp' tabIndex="-1">
				<tt:form-factor value="phone"><img src="images/unitime-phone.png" border="0"/></tt:form-factor>
				<tt:form-factor value="tablet"><img src="images/unitime-tablet.png" border="0"/></tt:form-factor>
			</a></span>
			<span class="title">University Timetabling Application</span>
		</span>
	</span>
	<span class='unitime-MobileHeader'><span id='UniTimeGWT:Header' class="unitime-InfoPanel"></span></span>
	<span id='UniTimeGWT:Content' <%=(!showBackground ? "class='unitime-MobileMainContent'" : "class='unitime-MobileMainContent unitime-MainLogo'")%>>
		<% if (sysMessage != null && !sysMessage.trim().isEmpty()) { %>
			<span class='messages'>
				<div class='WelcomeRowHead'>System Messages</div>
				<div class='message'><%= sysMessage %></div>
			</span>
		<% } %>
		<tt:registration method="hasMessage">
			<span class='messages'>
				<div class='WelcomeRowHead'>Messages from UniTime</div>
				<div class='message'><tt:registration method="message"/></div>
			</span>
		</tt:registration>
	</span>
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
</tt:form-factor>
<tt:form-factor value="desktop">
	<span class="unitime-Page"><span class='row'>
	<span class='sidebar' id="unitime-SideMenu">
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
    <span class='main'><span class='body' id="unitime-Page" style="background-image:url('images/logofaded.jpg');backbackground-repeat:no-repeat;background-position: center;">
    	<span class="unitime-PageHeader" id="unitime-Header">
    		<span class="row">
    			<span class="logo"><a href='http://www.unitime.org' tabIndex="-1"><img src="images/unitime.png" border="0"/></a></span>
    			<span class="content">
					<span class="title">University Timetabling Application</span>
					<span class='unitime-Header'><span id='UniTimeGWT:Header' class="unitime-InfoPanel"></span></span>
				</span>
			</span>
		</span>
	<span id='UniTimeGWT:Content' <%=(!showBackground ? "class='unitime-MainContent'" : "class='unitime-MainContent unitime-MainLogo'")%>>
		<% if (sysMessage != null && !sysMessage.trim().isEmpty()) { %>
			<span class='messages'>
				<div class='WelcomeRowHead'>System Messages</div>
				<div class='message'><%= sysMessage %></div>
			</span>
		<% } %>
		<tt:registration method="hasMessage">
			<span class='messages'>
				<div class='WelcomeRowHead'>Messages from UniTime</div>
				<div class='message'><tt:registration method="message"/></div>
			</span>
		</tt:registration>
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
	
</BODY>
<script language="JavaScript" type="text/javascript">
	if (parent && parent.hideGwtDialog && parent.refreshPage) {
		parent.hideGwtDialog();
		parent.refreshPage();
	}
</script>
</HTML>
