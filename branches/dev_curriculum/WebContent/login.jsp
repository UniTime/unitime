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
<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8" errorPage="/error.jsp"%>
<%@ page import="org.unitime.timetable.ApplicationProperties" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<HTML>
	<HEAD>
		<META http-equiv="Content-Type"
			content="text/html; charset=ISO-8859-1">
 	   <link type="text/css" rel="stylesheet" href="unitime/gwt/standard/standard.css">
 	   <link type="text/css" rel="stylesheet" href="styles/unitime.css">
 	   <link type="text/css" rel="stylesheet" href="styles/timetabling.css">
		<link rel="shortcut icon" href="images/timetabling.ico" />
	    <script type="text/javascript" language="javascript" src="unitime/unitime.nocache.js"></script>
		<TITLE>UniTime 3.2| Log In</TITLE>
	</HEAD>
	<BODY class="bodyMain" onload="document.forms[0].username.focus();">
	
	<% if (ApplicationProperties.getProperty("tmtbl.header.external", "").trim().length()>0) { %>
	<jsp:include flush="true" page='<%=ApplicationProperties.getProperty("tmtbl.header.external")%>' />
	<% } %>
	
    <span id='UniTimeGWT:TopMenu' style="display: none;" ></span>
	
<%
	String errorMsg = null;
	if (request.getParameter("e")!=null) {
		String eNum = request.getParameter("e");
		if (eNum.equals("1"))
			errorMsg = "Invalid username/password";
		if (eNum.equals("2"))
			errorMsg = "Authentication failed";
		if (eNum.equals("3"))
			errorMsg = "Authentication failed";
		if (eNum.equals("4"))
			errorMsg = "User temporarily locked out -<br> Exceeded maximum failed login attempts.";
	} else if (request.getParameter("m")!=null) {
		errorMsg = (String)request.getParameter("m");
	}
 %>		
		<BR>
		
		<FORM method="post" action="login.do">
			<INPUT type="hidden" name="cs" value="login">

			<TABLE border="0" cellspacing="0" cellpadding="0" align="center">
				<TR>
					<TD>&nbsp;</TD>
					<TD class="H3" align="center" colspan="3">
						<IMG src="images/timetabling-nocopy.jpg" alt="Timetabling" title="Timetabling Log In" style="margin-bottom: 5px;">
						<BR>
						&nbsp;
						<% if (errorMsg!=null)  { %>
							<span class="errorMessage">
							<%= errorMsg %>
							</span>
						<% } %>
					</TD>
				</TR>

				<TR>
					<TD align="left" rowspan="2" valign="middle">
						<IMG src="images/unitime.png" border="0" align="bottom" alt="UniTime" title="UniTime" hspace="20">
					</TD>
					<TD colspan="3">
						<DIV align="left" id="login">
							<DIV class="BrownBG">
								<DIV class="H40px"></DIV>
								<DIV>
									<LABEL>
										Username:
									</LABEL>
								</DIV>
								<DIV class="txtField">
									<INPUT type="text" id="loginId" name="username" class="" value="">
								</DIV>
								<DIV class="H20px"></DIV>
								<DIV>
									<LABEL>
										Password:
									</LABEL>
								</DIV>
								<DIV class="txtField">
									<INPUT type="password" id="pwd" class="" name="password" value="">
								</DIV>
							</DIV>
						</DIV>
					</TD>
				</TR>
				<TR>
					<TD><DIV align="left" id="login"><DIV class="bottom"><IMG src="images/login_bg_2.jpg"/><INPUT id="submit" name="submit" type="image" src="images/login_bg_3.jpg" border="0" align="top" value="log in"><IMG src="images/login_bg_4.jpg"/></DIV></DIV></TD>
				</TR>
			</TABLE>
		</FORM>
		
		<%@ include file="/initializationError.jspf"%>
		
		<BR>
		&nbsp;
		<BR>
		&nbsp;

	<tt:hasProperty name="tmtbl.page.disclaimer">
		<table align="center" class="unitime-Footer">
			<tr>
    			<td colspan="3" align="center" style="color:#777777;">
    				<tt:property name="tmtbl.page.disclaimer"/>
    			</td>
    		</tr>
    	</table>
    </tt:hasProperty>
		

		<% if (ApplicationProperties.getProperty("tmtbl.footer.external", "").trim().length()>0) { %>
			<jsp:include flush="true" page='<%=ApplicationProperties.getProperty("tmtbl.footer.external")%>' />
		<% } %>
</HTML>
