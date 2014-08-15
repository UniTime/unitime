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
<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8" isErrorPage="true"%>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ page import="org.unitime.commons.web.WebOutputStream"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="org.unitime.timetable.util.AccessDeniedException"%>
<%@ page import="org.unitime.timetable.util.Constants"%>

<%
	try {
		if (exception==null && session.getAttribute("exception")!=null) {
			exception = (Exception)session.getAttribute("exception");
			session.removeAttribute("exception");
 		}
 	} catch (IllegalStateException e) {}
 	if (exception instanceof AccessDeniedException || "Access Denied.".equals(exception.getMessage())) {
%>
		<jsp:forward page="/loginRequired.do">
			<jsp:param name="message" value="<%=exception.getMessage()%>"/>
		</jsp:forward>
<%
 	}
 %>
 <% if (exception != null) request.setAttribute("__exception", exception); %>
<HTML>
<HEAD>
	<META http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <meta charset="UTF-8"/>
	<TITLE>UniTime <%=Constants.VERSION%>| Error</TITLE>
	<link type="text/css" rel="stylesheet" href="unitime/gwt/standard/standard.css">
    <link type="text/css" rel="stylesheet" href="styles/unitime.css">
    <link type="text/css" rel="stylesheet" href="styles/unitime-mobile.css">
	<LINK rel="stylesheet" type="text/css" href="styles/timetabling.css" />
    <tt:hasProperty name="tmtbl.custom.css">
		<link rel="stylesheet" type="text/css" href="%tmtbl.custom.css%" />
    </tt:hasProperty>
    <script type="text/javascript" language="javascript" src="unitime/unitime.nocache.js"></script>
</HEAD>
<BODY class="bodyMain">
	<tt:form-factor value="unknown"><span id='UniTimeGWT:DetectFormFactor' style="display: none;"></span></tt:form-factor>
    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex="-1" style="position:absolute;width:0;height:0;border:0"></iframe>
    <iframe src="javascript:''" id="__printingFrame" tabIndex="-1" style="position:absolute;width:0;height:0;border:0"></iframe>

    <tt:form-factor value="desktop">
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
	<tt:hasProperty name="tmtbl.page.warn.error">
		<div class='unitime-PageWarn'><tt:property name="tmtbl.page.warn.error"/></div>
	</tt:hasProperty>
	<tt:hasProperty name="tmtbl.page.info.error">
		<div class='unitime-PageMessage'><tt:property name="tmtbl.page.info.error"/></div>
	</tt:hasProperty>
	<tt:offering-locks/>
	
<tt:form-factor value="mobile">
	<span class="unitime-MobilePage">
	<span class="unitime-MobilePageHeader">
		<span class="row">
			<span id='UniTimeGWT:MobileMenu' class="menu"></span>
			<span class="logo"><a href='main.jsp' tabIndex="-1">
				<tt:form-factor value="phone"><img src="images/unitime-phone.png" border="0"/></tt:form-factor>
				<tt:form-factor value="tablet"><img src="images/unitime-tablet.png" border="0"/></tt:form-factor>
			</a></span>
			<span id='UniTimeGWT:Title' class="title">Runtime Error</span>
		</span>
	</span>
	<span class='unitime-MobileHeader'><span id='UniTimeGWT:Header' class="unitime-InfoPanel"></span></span>
	<span id='UniTimeGWT:TitlePanel' class="unitime-MobileNavigation"></span>
	<% if (exception!=null) { %>
		<TABLE width="100%" border="0">
			<TR>
				<TD colspan="2">
					<DIV class="WelcomeRowHead">
						<FONT color="898989">Error: </FONT>
						<FONT color="#FF0000"><%= 
						(exception.getMessage() != null
	                        && exception.getMessage().indexOf("$jsp:") >= 0 
	                    ? exception.getMessage()
	                               .substring( exception.getMessage().indexOf(':') + 2 )
	                    : exception.getMessage())%></FONT>
	                </DIV>
				</TD>
			</TR>
		<% 
		    WebOutputStream wos = new WebOutputStream();
	        PrintWriter pw = new PrintWriter(wos);
	        exception.printStackTrace(pw);
	        pw.close();
			String stackTrace = wos.toString();
		%>
			<TR align="left" valign="top">
				<TD><FONT color="898989">Trace: </FONT></TD>
				<TD> <FONT color="898989"> <%
					       out.print(stackTrace);
				    %></FONT>
				</TD>
			</TR>
		</TABLE>
	<% } %>
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
    <span class='main'><span class='body' id="unitime-Page">
    	<span class="unitime-PageHeader" id="unitime-Header">
    		<span class="row">
    			<span class="logo"><a href='main.jsp' tabIndex="-1"><img src="images/unitime.png" border="0"/></a></span>
    			<span class="content">
					<span id='UniTimeGWT:Title' class="title">Runtime Error</span>
					<span class='unitime-Header'><span id='UniTimeGWT:Header' class="unitime-InfoPanel"></span></span>
					<span id='UniTimeGWT:TitlePanel' class='navigation'></span>
				</span>
			</span>
		</span>
	<% if (exception!=null) { %>
		<TABLE width="100%" border="0">
			<TR>
				<TD colspan="2">
					<DIV class="WelcomeRowHead">
						<FONT color="898989">Error: </FONT>
						<FONT color="#FF0000"><%= 
						(exception.getMessage() != null
	                        && exception.getMessage().indexOf("$jsp:") >= 0 
	                    ? exception.getMessage()
	                               .substring( exception.getMessage().indexOf(':') + 2 )
	                    : exception.getMessage())%></FONT>
	                </DIV>
				</TD>
			</TR>
		<% 
		    WebOutputStream wos = new WebOutputStream();
	        PrintWriter pw = new PrintWriter(wos);
	        exception.printStackTrace(pw);
	        pw.close();
			String stackTrace = wos.toString();
		%>
			<TR align="left" valign="top">
				<TD><FONT color="898989">Trace: </FONT></TD>
				<TD> <FONT color="898989"> <%
					       out.print(stackTrace);
				    %></FONT>
				</TD>
			</TR>
		</TABLE>
	<% } %>
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
		<span class="unitime-VisibleAriaStatus" id='UniTimeGWT:AriaStatus'></span>
	</span>
</span></span></span>
</tt:form-factor>

  </body>
</HTML>
