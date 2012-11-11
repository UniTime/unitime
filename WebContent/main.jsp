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
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<HTML>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta charset="UTF-8"/>
	<title>UniTime <%=Constants.VERSION%>| University Timetabling Application</title>
	<link rel="shortcut icon" href="images/timetabling.ico" />
	<link type="text/css" rel="stylesheet" href="unitime/gwt/standard/standard.css">
    <link type="text/css" rel="stylesheet" href="styles/unitime.css">
    <link type="text/css" rel="stylesheet" href="styles/timetabling.css">
    <tt:hasProperty name="tmtbl.custom.css">
		<LINK rel="stylesheet" type="text/css" href="%tmtbl.custom.css%" />
    </tt:hasProperty>
    <script language="JavaScript" type="text/javascript" src="scripts/loading.js"></script>
    <script type="text/javascript" language="javascript" src="unitime/unitime.nocache.js"></script>
</head>
<BODY class="unitime-Body">

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
		<table width='100%' border='0' cellpadding='3' cellspacing='0'><tr><td class="reqGlobalWarn" style='padding-left:10px;'>
			<tt:property name="tmtbl.global.warn"/>
		</td></tr></table>
	</tt:hasProperty>
	<tt:offering-locks/>
	
	<tt:propertyNotEquals name="unitime.warn.chromeframe" value="false">
	<!--[if IE]>
    <script type="text/javascript" src="scripts/CFInstall.min.js"></script>
    
    <table width='100%' border='0' cellpadding='3' cellspacing='0' style='display:none;' id='__ie_no_chrome'>
      <tr><td class="reqMsg" width='5'>&nbsp;</td>
      	  <td class="reqMsg">
      	  	<a class='noFancyLinks' href="http://google.com/chromeframe">The UniTime application may run very slow in Internet Explorer. To speed it up, please click here and install Google Chrome Frame plug-in.</a></td>
      </tr>
    </table>
    
    <div style='display:none;'><div id='__ie_chrome_plugin'></div></div>
    
    <script>
     function ie_no_chrome() {
       document.getElementById('__ie_no_chrome').style.display = 'table';
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

<div id="contentMain">
	<table align="center" width="900px">
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
    <td valign="top" >
	    <table class="unitime-Page" width="100%" background="images/logofaded.jpg" style="background-repeat:no-repeat;background-position: center;">
	    <tr><td>
    		<table class="unitime-MainTable" cellpadding="2" cellspacing="0" width="100%">
		   		<tr><td rowspan="2"><a href='http://www.unitime.org' tabindex="-1"><img src="images/unitime.png" border="0"/></a></td>
		   			<td nowrap="nowrap" width="100%" align="right" valign="middle" class="unitime-Title" style="padding-right: 20px; padding-top: 5px; height: 55px;">
		   				<span class='unitime-Title'>University Timetabling Application</span>
		   			</td>
	    		</tr>
	    		<tr><td width="100%" align="right" valign="middle" nowrap="nowrap" style="height: 34px;">
	    			<span id='UniTimeGWT:Header'></span>
	    		</td></tr>
	    	</table>
	    </td></tr><tr><td>
<TABLE width="100%" height="600px" align="center" >
	<TR>
	<% 
	String sysMessage = ApplicationProperties.getProperty("tmtbl.system_message");
	boolean showBackground = (sysMessage == null || sysMessage.trim().isEmpty());
	%>
	<tt:registration method="hasMessage">
	<% showBackground = false; %>
	</tt:registration>
		<TD align="center" valign="top" width="100%" height="100%" <%=(showBackground ? "background=\"images/logo.jpg\"" : "" )%> style="background-repeat:no-repeat;background-position: center;">
		<TABLE width="100%" cellspacing="2" cellpadding="2" border="0">
			<% 
				if (sysMessage != null && !sysMessage.trim().isEmpty()) {
			%>
			<TR>
				<TD class="WelcomeRowHead" align="left">System Messages</TD>
			</TR>
			<TR>
				<TD align="left">
					&nbsp;<BR>
					<FONT class="normalBlack">
						<%= sysMessage %>
					</FONT>
					<BR>&nbsp;
				</TD>
			</TR>
			<%
				}
			%>
			<tt:registration method="hasMessage">
				<TR>
					<TD class="WelcomeRowHead" align="left">Messages from UniTime</TD>
				</TR>
				<TR>
					<TD align="left">
						&nbsp;<BR>
						<FONT class="normalBlack">
							<tt:registration method="message"/>
						</FONT>
						<BR>&nbsp;
					</TD>
				</TR>
			</tt:registration>
		</TABLE>
		</TD>
	</TR>
</TABLE>
	    </td></tr></table>
    </td></tr><tr><td valign="top">
    	<table class="unitime-Footer" cellpadding="0" cellspacing="0">
    		<tr>
    			<td width="33%" align="left" class="unitime-FooterText"><span id="UniTimeGWT:Version"></span></td>
    			<!-- WARNING: Changing or removing the copyright notice will violate the license terms. If you need a different licensing, please contact us at support@unitime.org -->
    			<td width="34%" align="center" class="unitime-FooterText"><tt:copy/></td>
    			<td width="33%" align="right" class="unitime-FooterText"><tt:registration update="true"/></td>
    		</tr>
    		<tt:hasProperty name="tmtbl.page.disclaimer">
    			<tr>
    				<td colspan="3" class="unitime-Disclaimer">
    					<tt:property name="tmtbl.page.disclaimer"/>
    				</td>
    			</tr>
    		</tt:hasProperty>
    	</table>
	</td></tr></table>
</div>

<div id="loadingMain" style="visibility:hidden;display:none">
<TABLE width="100%" height="100%" align="center" cellpadding="0" cellspacing="0" border="0">
	<TR>
		<TD valign="middle" align="center">
			<font class="WelcomeRowHeadNoLine">Loading</font><br>&nbsp;<br>
			<IMG align="middle" vspace="5" border="0" src="images/loading.gif">
		</TD>
	</TR>
</TABLE>		
</div>				
	
</BODY>
<script language="JavaScript" type="text/javascript">
	if (parent && parent.hideGwtDialog && parent.refreshPage) {
		parent.hideGwtDialog();
		parent.refreshPage();
	}
</script>
</HTML>
