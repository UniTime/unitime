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
<%@ page import="org.unitime.timetable.ApplicationProperties" %>
<%@page import="net.sf.cpsolver.ifs.util.JProf"%>
<%@page import="java.text.NumberFormat"%>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib prefix='c' uri='http://java.sun.com/jstl/core_rt' %>

<HTML>
	<HEAD>
    	<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
	    <meta charset="UTF-8"/>
 	   <link type="text/css" rel="stylesheet" href="unitime/gwt/standard/standard.css">
 	   <link type="text/css" rel="stylesheet" href="styles/unitime.css">
 	   <link type="text/css" rel="stylesheet" href="styles/timetabling.css">
		<link rel="shortcut icon" href="images/timetabling.ico" />
	    <script type="text/javascript" language="javascript" src="unitime/unitime.nocache.js"></script>
		<TITLE>UniTime <%=Constants.VERSION%>| Log In</TITLE>
	</HEAD>
	<BODY class="bodyMain" onload="document.forms[0].j_username.focus();">
	
	<% if (ApplicationProperties.getProperty("tmtbl.header.external", "").trim().length()>0) { %>
	<jsp:include flush="true" page='<%=ApplicationProperties.getProperty("tmtbl.header.external")%>' />
	<% } %>
	
    <span id='UniTimeGWT:TopMenu' style="display: block; height: 23px;"></span>
	
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
		
		<FORM name="f" action="<c:url value='j_spring_security_check'/>" method="POST">
			<INPUT type="hidden" name="cs" value="login">
			<INPUT type="hidden" name="menu" value="<%=request.getParameter("menu") == null ? "" : request.getParameter("menu") %>">

			<TABLE border="0" cellspacing="0" cellpadding="0" align="center" style="margin-top: 30px; margin-bottom: 50px;">
				<TR>
					<TD class="H3" align="center" colspan="3">
						<IMG src="images/timetabling-nocopy.jpg" alt="Timetabling" title="Timetabling Log In" style="margin-bottom: 20px;">
						<% if (errorMsg!=null)  { %>
							<div style="color: red; margin-bottom: 10px;">
							<%= errorMsg %>
							</div>
						<% } %>
						<c:if test="${not empty SPRING_SECURITY_LAST_EXCEPTION.message}">
							<div style="color: red; margin-bottom: 10px;">
								Authentication failed: <c:out value="${SPRING_SECURITY_LAST_EXCEPTION.message}"/>.
							</div>
						</c:if>
					</TD>
				</TR>

				<TR>
					<TD align="left" rowspan="2" valign="middle">
						<IMG src="images/unitime.png" border="0" align="bottom" alt="UniTime" title="UniTime" hspace="20">
					</TD>
					<TD>
						<DIV align="center" id="login">
							<DIV class="BrownBG">
								<DIV class="H40px"></DIV>
								<DIV>
									<LABEL>
										Username:
									</LABEL>
								</DIV>
								<DIV class="txtField">
									<input type='text' name='j_username' value='<c:if test="${not empty SPRING_SECURITY_LAST_USERNAME}"><c:out value="${SPRING_SECURITY_LAST_USERNAME}"/></c:if>' aria-label='Enter user name'/>
								</DIV>
								<DIV class="H20px"></DIV>
								<DIV>
									<LABEL>
										Password:
									</LABEL>
								</DIV>
								<DIV class="txtField">
									<input type='password' name='j_password' aria-label='Enter password'>
								</DIV>
							</DIV>
						</DIV>
					</TD>
					<TD rowspan="2" width="150px">
					</TD>
				</TR>
				<TR>
					<TD><DIV align="center" id="login"><DIV class="bottom"><IMG src="images/login_bg_2.jpg"/><INPUT id="submit" name="submit" type="image" src="images/login_bg_3.jpg" border="0" align="top" value="log in" alt="Submit login information."><IMG src="images/login_bg_4.jpg"/></DIV></DIV></TD>
				</TR>
				<c:if test="${SUGGEST_PASSWORD_RESET}">
						<tr><td class="unitime-Footer" align="center" colspan="3" style="padding-top: 10px; color: #9CB0CE;">
							<a href='gwt.jsp?page=password&reset=1' class='unitime-FooterLink'>Forgot your password?</a>
						</td></tr>
				</c:if>
			</TABLE>
		</FORM>
		
		<%@ include file="/initializationError.jspf"%>
		
    	<table class="unitime-Footer">
    		<tr>
    			<!-- WARNING: Changing or removing the copyright notice will violate the license terms. If you need a different licensing, please contact us at support@unitime.org -->
    			<td align="center" class="unitime-FooterText"><span id="UniTimeGWT:Version"></span><tt:copy br="false"/></td>
    		</tr>
			<tt:hasProperty name="tmtbl.page.disclaimer">
				<tr>
    				<td align="center" style="color:#777777;">
    					<tt:property name="tmtbl.page.disclaimer"/>
    				</td>
    			</tr>
    		</tt:hasProperty>
    	</table>
		

		<% if (ApplicationProperties.getProperty("tmtbl.footer.external", "").trim().length()>0) { %>
			<jsp:include flush="true" page='<%=ApplicationProperties.getProperty("tmtbl.footer.external")%>' />
		<% } %>
</HTML>
