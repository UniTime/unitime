<%--
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC
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
<%@ page import="org.unitime.commons.web.WebOutputStream"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="org.unitime.commons.web.Web"%>
<%@ page import="org.unitime.commons.Debug"%>
<%@ page isErrorPage="true"%>
<%@ page import="org.unitime.commons.Email" %>
<%@ page import="java.util.Vector" %>
<%@ page import="org.unitime.timetable.ApplicationProperties" %>

<%@ include file="/checkLogin.jspf"%>
<%@ include file="/checkAccessLevel.jspf"%>

<%	if (exception==null && session.getAttribute("exception")!=null) {
		exception = (Exception)session.getAttribute("exception");
 		session.removeAttribute("exception");
 	}
 	if (exception!=null && exception.getMessage()!=null && exception.getMessage().startsWith("Access Denied") ) { %>
 		<jsp:forward page="/loginRequired.do">
			<jsp:param name="message" value="<%=exception.getMessage()%>"/>
		</jsp:forward>
<%	}%>
<HTML>
<HEAD>
	<TITLE>UniTime 3.2| Error</TITLE>
	<META http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link type="text/css" rel="stylesheet" href="styles/unitime.css">
	<LINK rel="stylesheet" type="text/css" href="styles/timetabling.css" />
</HEAD>
<BODY class="bodyMain">
	<table align="center">
    <tr>
    <td valign="top">
	    <table class="unitime-Page" width="100%"><tr>
	    <td>
    		<table class="unitime-MainTable" cellpadding="2" cellspacing="0" width="100%">
		   		<tr><td rowspan="3">
	    			<a href='http://www.unitime.org'>
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
		<%	try {
				String errorEmail = ApplicationProperties.getProperty("tmtbl.error.email");
				String subject = ApplicationProperties.getProperty("tmtbl.error.subject");

				if (session.getAttribute("userTrace") !=null
						&& errorEmail!=null && errorEmail.trim().length()>0 
						&& exception.getMessage().toLowerCase().indexOf("access denied")<0) {	
					Email email = new Email();
					email.setSubject(subject);
					email.setText("Server: " +  request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/\n"
								+ "Exception: " + exception.getMessage() + "\n" 
								+ "Stack Trace: " + stackTrace.replaceAll("<br>\n", "") + "\n\n"								
								+ (String) session.getAttribute("userTrace"));
					email.addRecipient(errorEmail, null);
					email.addNotifyCC();
					email.send();
				}
			} catch (Exception e) {
				Debug.error(e);
			}
			
			Debug.error(exception);
		%>
		</TABLE>
	<% } %>	    
	    </td></tr></table>
    </td></tr><tr><td>
    	<table class="unitime-Footer">
    		<tr>
    			<td width="33%" align="left"></td>
    			<td width="34%" align="center" nowrap="nowrap"><a class='unitime-FooterLink' href='http://www.unitime.org'>&copy; 2010 UniTime.org</a></td>
    			<td width="33%" align="right"></td>
    			</tr>
    	</table>
	</td></tr></table>
  </body>
</HTML>
