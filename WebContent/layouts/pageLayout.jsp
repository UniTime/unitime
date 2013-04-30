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
<%@ page import="java.text.NumberFormat" %>
<%@ page import="net.sf.cpsolver.ifs.util.JProf" %>
<%@ page import="java.util.Date" %>
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
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="gwt:property" content="locale=<%=Localization.getFirstLocale()%>">
    <meta charset="UTF-8"/>
	<style type="text/css">@import url(scripts/jscalendar/calendar-blue.css);</style>
	<link type="text/css" rel="stylesheet" href="unitime/gwt/standard/standard.css">
    <link type="text/css" rel="stylesheet" href="styles/unitime.css">
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
	<script language="JavaScript" type="text/javascript">
		if (!String.prototype.trim) {
			String.prototype.trim = function() {
				return this.replace(/^\s+|\s+$/g,"");
			};
		}
	</script>
    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex="-1" style="position:absolute;width:0;height:0;border:0"></iframe>
    <iframe src="javascript:''" id="__printingFrame" tabIndex="-1" style="position:absolute;width:0;height:0;border:0"></iframe>
    
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
    
    <tt:hasProperty name="tmtbl.global.warn">
		<table width='100%' border='0' cellpadding='3' cellspacing='0'><tr><td class="reqGlobalWarn" style='padding-left:10px;'>
			<tt:property name="tmtbl.global.warn"/>
		</td></tr></table>
	</tt:hasProperty>
	<tt:page-warning prefix="tmtbl.page.warn." style="unitime-MessageYellow"/>
	<tt:page-warning prefix="tmtbl.page.info." style="unitime-MessageBlue"/>
	<% if (request.getAttribute(Constants.REQUEST_OPEN_URL)!=null) { %>
		<script language="JavaScript">
			<% String url = request.getAttribute(Constants.REQUEST_OPEN_URL).toString(); %>
			window.open('<%=url%>');
		</script>
		<table width='100%' border='0' cellpadding='3' cellspacing='0'><tr>
			<td 
				class="popupBlocked" style='padding-left:10px;'
				onMouseOver="this.style.backgroundColor='#BBCDD0';"
				onMouseOut="this.style.backgroundColor='#DFE7F2';">
				<a class='noFancyLinks' href="<%=request.getAttribute(Constants.REQUEST_OPEN_URL)%>">
					If the pop-up window was blocked, you can follow this link to retrieve the exported file.
				</a>
			</td></tr>
		</table>
	<% } %>
	<% if (session.getAttribute(Constants.REQUEST_WARN)!=null) { %>
		<table width='100%' border='0' cellpadding='3' cellspacing='0'><tr><td class="reqWarn" style='padding-left:10px;'>
				<%=session.getAttribute(Constants.REQUEST_WARN)%>
		</td></tr></table>
	<% session.removeAttribute(Constants.REQUEST_WARN);
	   } %>
	<% if (session.getAttribute(Constants.REQUEST_MSSG)!=null) { %>
		<table width='100%' border='0' cellpadding='3' cellspacing='0'><tr><td class="reqMsg" style='padding-left:10px;'>
				<%=session.getAttribute(Constants.REQUEST_MSSG)%>
		</td></tr></table>
	<% session.removeAttribute(Constants.REQUEST_MSSG);
	   } %>
	<tiles:importAttribute name="showSolverWarnings" scope="request"/>
	<logic:notEqual name="showSolverWarnings" value="none">
		<tt:solver-warnings><bean:write name="showSolverWarnings"/></tt:solver-warnings>
	</logic:notEqual>
	<tt:offering-locks/>
		
	<tiles:importAttribute/>
	<tiles:importAttribute name="title" scope="request"/>
	<tiles:importAttribute name="showNavigation" scope="request"/>
	<div id="contentMain">
	<table align="center">
    <tr>
    <td valign="top" rowspan="2" id="unitime-SideMenu">
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
	    <table class="unitime-Page" id="unitime-Page" width="100%">
	    <tr><td>
    		<table class="unitime-MainTable" cellpadding="2" cellspacing="0" width="100%">
		   		<tr><td rowspan="3"><a href='main.jsp' tabIndex="-1"><img src="images/unitime.png" border="0"/></a></td>
		   			<td nowrap="nowrap" width="100%" align="right" valign="middle" class="unitime-Title" style="padding-right: 20px; height: 55px;">
		   				<span id='UniTimeGWT:Title'><bean:write name="title" scope="request"/></span>
		   			</td>
	    		</tr><tr>
	    			<td width="100%" align="right" valign="middle" nowrap="nowrap" style="height:34px;">
					    <logic:equal name="showMenu" value="true">
	    					<span id='UniTimeGWT:Header'></span>
	    				</logic:equal>
	    			</td>
	    		</tr><tr>
	    			<td width="100%" align="left" valign="middle">
	    				<span id='UniTimeGWT:TitlePanel'>
	    					<tiles:insert attribute="header">
								<tiles:put name="showNavigation" value="${showNavigation}"/>
							</tiles:insert>
						</span>
	    			</td>
	    		</tr>
	    	</table>
	    </td></tr><tr><td style="min-width: 800px">
        	<span id='UniTimeGWT:Content'>
	    		<tiles:insert attribute="body">
					<tiles:put name="body2" value="${body2}"/>
					<tiles:put name="action2" value="${action2}"/>
				</tiles:insert>
        	</span>
	    </td></tr></table>
    </td></tr><tr><td valign="top">
    	<table class="unitime-Footer" cellpadding="0" cellspacing="0">
    		<tr>
    			<td width="33%" align="left" class="unitime-FooterText"><span id="UniTimeGWT:Version"></span>
    				<logic:notEmpty scope="request" name="TimeStamp">
<% 
		double endTime = JProf.currentTimeSec();
		double startTime = ((Double)request.getAttribute("TimeStamp")).doubleValue();
		double diff = endTime - startTime;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);	
%>
					Page generated in <%=nf.format(diff)%> sec.
					</logic:notEmpty>
    			</td>
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
    	</table>
	</td></tr></table>
	</div>
	
<div id="loadingMain" style="visibility:hidden;display:none">
	<table width="100%" height="100%" align="center" cellpadding="0" cellspacing="0" border="0">
		<tr>
			<td valign="middle" align="center">
				<font class="WelcomeRowHeadNoLine">Loading</font><br>&nbsp;<br>
				<img align="middle" vspace="5" border="0" src="images/loading.gif">
			</td>
		</tr>
	</table>
</div>
</body>
</html:html>
