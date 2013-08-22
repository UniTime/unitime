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
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta charset="UTF-8"/>
	<TITLE>UniTime <%=Constants.VERSION%>| Error</TITLE>
    <link type="text/css" rel="stylesheet" href="styles/unitime.css">
	<LINK rel="stylesheet" type="text/css" href="styles/timetabling.css" />
    <tt:hasProperty name="tmtbl.custom.css">
		<link rel="stylesheet" type="text/css" href="%tmtbl.custom.css%" />
    </tt:hasProperty>
    <script type="text/javascript" language="javascript" src="unitime/unitime.nocache.js"></script>
</HEAD>
<BODY class="bodyMain">
	<table align="center">
    <tr>
    <td valign="top">
	    <table class="unitime-Page" width="100%"><tr>
	    <td>
    		<table class="unitime-MainTable" cellpadding="2" cellspacing="0" width="100%">
		   		<tr><td rowspan="3">
	    			<a href='main.jsp'>
	    				<img src="images/unitime.png" border="0"/>
	    			</a>
	    		</td><td nowrap="nowrap" class="unitime-Title" width="100%" align="right" valign="middle" style="padding-right: 20px;">
	    			Runtime Error
	    		</td></tr>
	    	</table>
	    </td></tr><tr><td>
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
	    </td></tr></table>
    </td></tr><tr><td>
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
    	</table>
	</td></tr></table>
  </body>
</HTML>
