<!DOCTYPE html>
<!-- 
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC
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
 -->
<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8" errorPage="/error.jsp"%>
<%@ page import="org.unitime.localization.impl.Localization"%>
<%@ page import="org.unitime.timetable.util.Constants"%>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<html>
  <head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="gwt:property" content="locale=<%=Localization.getFirstLocale()%>">
    <meta charset="UTF-8"/>
	<style type="text/css">@import url(scripts/jscalendar/calendar-blue.css);</style>
    <link type="text/css" rel="stylesheet" href="unitime/gwt/standard/standard.css">
    <link type="text/css" rel="stylesheet" href="styles/unitime.css">
    <!--[if IE]>
	    <link type="text/css" rel="stylesheet" href="styles/unitime-ie.css">
    <![endif]-->
    <tt:hasProperty name="tmtbl.custom.css">
    	<link rel="stylesheet" type="text/css" href="%tmtbl.custom.css%" />
    </tt:hasProperty>
    <link rel="shortcut icon" href="images/timetabling.ico">
    <title>UniTime <%=Constants.VERSION%>| University Timetabling Application</title>
	<script language="JavaScript" type="text/javascript" src="scripts/jscalendar/calendar.js"></script>
	<script language="JavaScript" type="text/javascript" src="scripts/jscalendar/lang/calendar-en.js"></script>
	<script language="JavaScript" type="text/javascript" src="scripts/jscalendar/calendar-setup.js"></script>
  </head>
  <body class="unitime-Body">
    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex="-1" style="position:absolute;width:0;height:0;border:0"></iframe>
    <iframe src="javascript:''" id="__printingFrame" tabIndex="-1" style="position:absolute;width:0;height:0;border:0"></iframe>
    
    <tt:notHasProperty name="unitime.menu.style" user="true">
	   	<span id='UniTimeGWT:DynamicTopMenu' style="display: block; height: 23px;" ></span>
    </tt:notHasProperty>
    <tt:propertyEquals name="unitime.menu.style" user="true" value="Dynamic On Top">
    	<span id='UniTimeGWT:DynamicTopMenu' style="display: block; height: 23px;" ></span>
    </tt:propertyEquals>
    <tt:propertyEquals name="unitime.menu.style" user="true" value="Static On Top">
    	<span id='UniTimeGWT:TopMenu' style="display: block; height: 23px;" ></span>
    </tt:propertyEquals>

	<tt:hasProperty name="tmtbl.global.warn">
		<table width='100%' border='0' cellpadding='3' cellspacing='0'><tr><td class="unitime-MessageYellow" style='padding-left:10px;'>
			<tt:property name="tmtbl.global.warn"/>
		</td></tr></table>
	</tt:hasProperty>

	<tt:page-warning prefix="tmtbl.page.warn." style="unitime-MessageYellow"/>
	<tt:page-warning prefix="tmtbl.page.info." style="unitime-MessageBlue"/>

	<tt:offering-locks/>

	<tt:propertyNotEquals name="unitime.warn.chromeframe" value="false">
	<!--[if IE]>
    <script type="text/javascript" src="scripts/CFInstall.min.js"></script>
    
    <div style='display:none;'><div id='__ie_chrome_plugin'></div></div>
    
    <script>
     function ie_no_chrome() {
       window.gwtOnLoad = function() {
       	gwtShowMessage("<a class='unitime-NoFancyLink' href=\"http://google.com/chromeframe\" target='chromeframe'>The UniTime application may run very slow in Internet Explorer. To speed it up, please click here and install Google Chrome Frame plug-in.</a>");
       };
     }
     window.attachEvent("onload", function() {
       CFInstall.check({
         mode: "inline",
         node: "__ie_chrome_plugin",
         onmissing: ie_no_chrome
       });
     });
    </script>
  	<![endif]-->
  	</tt:propertyNotEquals>
  	
    <table align="center">
    <tr>
    <td valign="top" rowspan="2" id="unitime-SideMenu">
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
    </td>
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
    <td valign="top">
	    <table class="unitime-Page" id="unitime-Page" width="100%"><tr>
	    <td>
    		<table class="unitime-MainTable" cellpadding="2" cellspacing="0" width="100%" style="min-width: 800px">
		   		<tr><td rowspan="3">
	    			<a href='main.jsp' tabindex="-1">
	    				<img src="images/unitime.png" border="0" alt="UniTime logo, click here to get to the welcome page."/>
	    			</a>
	    		</td><td nowrap="nowrap" class="unitime-Title" width="100%" align="right" valign="middle" style="padding-right: 20px; height: 55px;">
	    			<span id='UniTimeGWT:Title'></span>
	    		</td></tr>
	    		<tr><td width="100%" align="right" valign="middle" nowrap="nowrap" style="width: 34px;">
	    			<span id='UniTimeGWT:Header'></span>
	    		</td></tr>
	    		<tr><td width="100%" align="left" valign="middle" width="100%">
	    			<span id='UniTimeGWT:TitlePanel'></span>
	    		</td></tr>
	    	</table>
	    </td></tr><tr><td>
	    	<table id="UniTimeGWT:Loading" class="unitime-MainTable" cellpadding="2" cellspacing="0" width="100%">
	    		<tr><td align="center">
	    			<i>Page is loading, please wait ...</i>
	    		</td></tr>
	    	</table>
	    </td></tr><tr><td>
	    	<span id='UniTimeGWT:Body'></span>
	    </td></tr></table>
    </td></tr><tr><td valign="top">
    	<table class="unitime-Footer" cellpadding="0" cellspacing="0">
    		<tr>
    			<td width="33%" align="left" class="unitime-FooterText"><span id="UniTimeGWT:Version"></span></td>
    			<!-- WARNING: Changing or removing the copyright notice will violate the license terms. If you need a different licensing, please contact us at support@unitime.org -->
    			<td width="34%" align="center" class="unitime-FooterText"><tt:copy/></td>
    			<td width="33%" align="right" class="unitime-FooterText"><tt:registration/></td>
    		</tr>
    		<tt:hasProperty name="tmtbl.page.disclaimer">
    			<tr>
    				<td colspan="3" class="unitime-Disclaimer">
    					<tt:property name="tmtbl.page.disclaimer"/>
    				</td>
    			</tr>
    		</tt:hasProperty>
    		<tr><td colspan="3" class="unitime-VisibleAriaStatus" id='UniTimeGWT:AriaStatus'></td></tr>
    	</table>
	</td></tr></table>
    <script type="text/javascript" language="javascript" src="unitime/unitime.nocache.js"></script>
  </body>
</html>
