<%-- 
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 --%>
<%@ page language="java" %>
<%@ page errorPage="/error.jsp" %>
<%@ page import="java.text.NumberFormat" %>
<%@ page import="net.sf.cpsolver.ifs.util.JProf" %>
<%@ page import="java.util.Date" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld"	prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld"	prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
	
<tiles:importAttribute name="checkLogin" scope="request"/>
<tiles:importAttribute name="checkAdmin" scope="request"/>
<tiles:importAttribute name="checkAccessLevel" scope="request"/>

<logic:equal name="checkLogin" value="true">
	<%@ include file="/checkLogin.jspf"%>
</logic:equal>
<logic:equal name="checkAdmin" value="true">
	<%@ include file="/checkAdmin.jspf"%>
</logic:equal>
<logic:equal name="checkAccessLevel" value="true">
	<%@ include file="/checkAccessLevel.jspf"%>
</logic:equal>

<%String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
String serverPath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/";
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html:html>
<HEAD>
	<META http-equiv="pragma" content="no-cache">
	<META http-equiv="cache-control" content="no-cache">
	<META http-equiv="expires" content="0">
	<STYLE type="text/css">@import url(<%=basePath%>scripts/jscalendar/calendar-blue.css);</STYLE>
    <LINK rel="stylesheet" type="text/css" href="<%=basePath%>styles/timetabling.css">
    <tt:hasProperty name="tmtbl.custom.css">
    	<LINK rel="stylesheet" type="text/css" href="<%=basePath%>%tmtbl.custom.css%" />
    </tt:hasProperty>
    <link rel="shortcut icon" href="<%=basePath%>images/timetabling.ico" />
	<TITLE><tiles:getAsString name="title" /></TITLE>
    <SCRIPT language="JavaScript" type="text/javascript" src="<%=basePath%>scripts/loading.js"></SCRIPT>
    <SCRIPT language="JavaScript" type="text/javascript" src="<%=basePath%>scripts/validator.js"></SCRIPT>
	<SCRIPT language="JavaScript" type="text/javascript" src="<%=basePath%>scripts/jscalendar/calendar.js"></SCRIPT>
	<SCRIPT language="JavaScript" type="text/javascript" src="<%=basePath%>scripts/jscalendar/lang/calendar-en.js"></SCRIPT>
	<SCRIPT language="JavaScript" type="text/javascript" src="<%=basePath%>scripts/jscalendar/calendar-setup.js"></SCRIPT>
	<SCRIPT language="JavaScript" type="text/javascript" src="<%=basePath%>scripts/select.js"></SCRIPT>
	<SCRIPT language="JavaScript" type="text/javascript" src="<%=basePath%>scripts/rtt.js"></SCRIPT>
	<SCRIPT language="javascript"><!--
		function doLoad() {
			// Trick 1 to prevent use of back button
			if(window.history.forward(1) != null)
                 window.history.forward(1);

            // Focus on frame 
			self.focus();
		}
	// --></SCRIPT>
</HEAD>
<BODY class="bodyStyle" <tiles:getAsString name="onLoadFunction" />>
	<tt:hasProperty name="tmtbl.global.warn">
		<table width='100%' border='0' cellpadding='3' cellspacing='0'><tr><td class="reqGlobalWarn" width='5'>&nbsp;</td><td class="reqGlobalWarn" >
			<tt:property name="tmtbl.global.warn"/>
		</td></tr></table>
	</tt:hasProperty>
	<% if (request.getAttribute(Constants.REQUEST_OPEN_URL)!=null) { %>
		<script language="JavaScript">
			window.open('<%=request.getAttribute(Constants.REQUEST_OPEN_URL)%>');
		</script>
		<table width='100%' border='0' cellpadding='3' cellspacing='0'><tr>
			<td class="popupBlocked" width='5'>&nbsp;</td>
			<td 
				class="popupBlocked" 
				onMouseOver="this.style.backgroundColor='#BBCDD0';"
				onMouseOut="this.style.backgroundColor='#DFE7F2';">
				<a class='noFancyLinks' href="<%=request.getAttribute(Constants.REQUEST_OPEN_URL)%>">
					If the pop-up window was blocked, you can follow this link to retrieve the exported file.
				</a>
			</td></tr>
		</table>
	<% } %>
	<% if (request.getAttribute(Constants.REQUEST_WARN)!=null) { %>
		<table width='100%' border='0' cellpadding='3' cellspacing='0'><tr><td class="reqWarn" width='5'>&nbsp;</td><td class="reqWarn" >
				<%=request.getAttribute(Constants.REQUEST_WARN)%>
		</td></tr></table>
	<% } %>
	<tiles:importAttribute name="showSolverWarnings" scope="request"/>
	<logic:equal name="showSolverWarnings" value="true">
		<tt:solver-warnings/>
	</logic:equal>
	<DIV id="contentMain">
	<tiles:importAttribute/>
	<tiles:importAttribute name="title" scope="request"/>
	<tiles:importAttribute name="showNavigation" scope="request"/>
	<tiles:importAttribute name="helpFile" scope="request"/>
	<tiles:insert attribute="header">
		<tiles:put name="helpFile" value="${helpFile}"/>
		<tiles:put name="showNavigation" value="${showNavigation}"/>
	</tiles:insert>
	<BLOCKQUOTE>
	<tiles:insert attribute="body">
		<tiles:put name="body2" value="${body2}"/>
		<tiles:put name="action2" value="${action2}"/>
	</tiles:insert>
	
<%
		try {
			String sb = "";
			if (session.getAttribute("userTrace")==null) {
				sb = "User: " + Web.getUser(session).getLogin() + "\nPage: " + request.getAttribute("title");			
			}
			else {
				sb = (String) session.getAttribute("userTrace");
				sb += "\n" 
					+ new Date().toString() + " : "
					+ request.getAttribute("title") 
					+ (request.getParameter("op")!=null 
						? " - " + request.getParameter("op") 
						: (request.getParameter("doit")!=null 
							? " - " + request.getParameter("doit") 
							: "") );
			}
			
			session.setAttribute("userTrace", sb);
		}
		catch (Exception e) {
		}
%>		
	
	<logic:notEmpty scope="request" name="TimeStamp"> 
<% 
		//long endTime = new Date().getTime(); 
		//float diff = ((float)(endTime - beginTime))/1000.0f;
		double endTime = JProf.currentTimeSec();
		double startTime = ((Double)request.getAttribute("TimeStamp")).doubleValue();
		double diff = endTime - startTime;
		NumberFormat nf = NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);	
%>
		<P align="right" class="font8Gray">&nbsp; Page generated in <%=nf.format(diff)%> sec. &nbsp; </P>	
	</logic:notEmpty>
	</BLOCKQUOTE>
	</DIV>				
	
	<DIV id="loadingMain" style="visibility:hidden;display:none">
	<TABLE width="100%" height="100%" align="center" cellpadding="0" cellspacing="0" border="0">
		<TR>
			<TD valign="middle" align="center">
				<font class="WelcomeRowHeadNoLine">Loading</font><br>&nbsp;<br>
				<IMG align="middle" vspace="5" border="0" src="images/loading.gif">
			</TD>
		</TR>
	</TABLE>		
	</DIV>				
	
</BODY>
</html:html>
