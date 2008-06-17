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
<%@ page language="java"%>
<%@ page errorPage="error.jsp"%>
<%@ page import="org.unitime.timetable.model.ApplicationConfig"%>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<HTML>
<HEAD>
	<LINK rel="stylesheet" type="text/css" href="styles/timetabling.css">
    <SCRIPT language="JavaScript" type="text/javascript" src="scripts/loading.js"></SCRIPT>
</HEAD>
<BODY class="bodyStyle">
	<tt:hasProperty name="tmtbl.global.warn">
		<table width='100%' border='0' cellpadding='3' cellspacing='0'><tr><td class="reqGlobalWarn" width='5'>&nbsp;</td><td class="reqGlobalWarn" >
			<tt:property name="tmtbl.global.warn"/>
		</td></tr></table>
	</tt:hasProperty>
<DIV id="contentMain">
&nbsp;
<BR>
<% 
	String imageFile = "logo.jpg";
	String sysMessage = ApplicationConfig.getConfigValue(Constants.CFG_SYSTEM_MESSAGE, "");
	if (sysMessage!=null && sysMessage.trim().length()>0) {
		imageFile = "logofaded.jpg";
	}
%>

<TABLE width="650" height="600" align="center"
	background="images/<%=imageFile%>" style="background-repeat:no-repeat;background-position:top;">
	<TR>
		<TD align="center" valign="top" width="100%" height="100%">
		<TABLE width="100%" cellspacing="2" cellpadding="2" border="0">
			<TR>
				<TD align="left">&nbsp;<BR>&nbsp;<BR>&nbsp;<BR></TD>
			</TR>
			<% 
				if (sysMessage!=null && sysMessage.trim().length()>0) {
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
		</TABLE>
		</TD>
	</TR>
</TABLE>
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
</HTML>
