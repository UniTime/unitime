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
<%@ page import="org.unitime.commons.web.WebOutputStream"%>
<%@ page import="java.io.PrintWriter"%>
<%@ page import="org.unitime.commons.web.Web"%>
<%@ page import="org.unitime.commons.Debug"%>
<%@ page isErrorPage="true"%>
<%@ page import="org.unitime.commons.Email" %>
<%@ page import="java.util.Vector" %>
<%@ page import="org.unitime.timetable.ApplicationProperties" %>

<%
 if (exception!=null && exception.getMessage() != null
       && exception.getMessage().startsWith("Access Denied") ) {%>
 	<jsp:forward page="/loginRequired.do">
			<jsp:param name="message" value="<%=exception.getMessage()%>"/>
	</jsp:forward>
<%} else if (!Web.isLoggedIn(session)) {%>
 	<jsp:forward page="/loginRequired.do">
			<jsp:param name="message" value="Your timetabling session has expired. Please log in again."/>
	</jsp:forward>
<%} else if (exception==null && session.getAttribute("exception")!=null) {
 	exception = (Exception)session.getAttribute("exception");
 	session.removeAttribute("exception");
 }
%>
<HTML>
<HEAD>
	<TITLE>Timetabling - Error</TITLE>
	<META http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<LINK rel="stylesheet" type="text/css" href="styles/timetabling.css" />
</HEAD>
<BODY class="bodyStyle" background="images/bkrnd2.jpg">
	<BR>
	<TABLE border="0" width="100%">
		<TR>
			<TD width="12">&nbsp;</TD>
			<TD height="45" align="right" class="WelcomeHead">
				<DIV class="H3">Runtime Error &nbsp;</DIV>
			</TD>
			<TD width="55"><IMG align="middle" src="images/logosmall.jpg" border="0"></TD>
		</TR>
	</TABLE>
	<BR>
	<% if (exception!=null) { %>
	<BLOCKQUOTE>
		<TABLE width="90%" border="0">
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
			
			if(Web.isAdmin(session)) { %>
			<TR align="left" valign="top">
				<TD><FONT color="898989">Trace: </FONT></TD>
				<TD> <FONT color="898989"> <%
					       out.print(stackTrace);
				    %></FONT>
				</TD>
			</TR>
		<% 	}
		
			Vector sessionTrace = new Vector();
			try {
				String errorEmail = (String) ApplicationProperties.getProperty("tmtbl.error.email");
				String smtpDomain = (String) ApplicationProperties.getProperty("tmtbl.smtp.domain");
				String smtpHost = (String) ApplicationProperties.getProperty("tmtbl.smtp.host");
				String sentFrom = (String) ApplicationProperties.getProperty("tmtbl.error.sentFrom");
				String replyTo = (String) ApplicationProperties.getProperty("tmtbl.error.replyTo");
				String subject = (String) ApplicationProperties.getProperty("tmtbl.error.subject");

				if (session.getAttribute("userTrace")!=null
						&& errorEmail!=null && errorEmail.trim().length()>0 
						&& exception.getMessage().toLowerCase().indexOf("access denied")<0) {				
					String data = "Server: " +  request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath()+"/\n"
								+ "Exception: " + exception.getMessage() + "\n" 
								+ "Stack Trace: " + stackTrace.replaceAll("<br>\n", "") + "\n\n"								
								+ (String) session.getAttribute("userTrace");
					Email email = new Email();
					email.sendMail(smtpHost, smtpDomain, sentFrom, replyTo, errorEmail, subject, data, sessionTrace);
				}
			}
			catch (Exception e) {
				Debug.error(e);
			}
			
			Debug.error(exception);
		%>
		</TABLE>
	</BLOCKQUOTE>
	<% } %>
</BODY>
</HTML>
