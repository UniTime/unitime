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
	<LINK rel="stylesheet" type="text/css" href="styles/unitime.css">
	<LINK rel="stylesheet" type="text/css" href="styles/timetabling.css">
    <tt:hasProperty name="tmtbl.custom.css">
		<LINK rel="stylesheet" type="text/css" href="%tmtbl.custom.css%" />
    </tt:hasProperty>
    <SCRIPT language="JavaScript" type="text/javascript" src="scripts/loading.js"></SCRIPT>
</HEAD>
<BODY class="unitime-Body">
	<tt:hasProperty name="tmtbl.global.warn">
		<table width='100%' border='0' cellpadding='3' cellspacing='0'><tr><td class="reqGlobalWarn" width='5'>&nbsp;</td><td class="reqGlobalWarn" >
			<tt:property name="tmtbl.global.warn"/>
		</td></tr></table>
	</tt:hasProperty>

<div id="contentMain">
<% 
	String imageFile = "logo.jpg";
	String sysMessage = ApplicationConfig.getConfigValue(Constants.CFG_SYSTEM_MESSAGE, "");
	if (sysMessage!=null && sysMessage.trim().length()>0) {
		imageFile = "logofaded.jpg";
	}
%>
	<table align="center">
		<tr><td>
		</td></tr>
    <tr><td valign="top">
	    <table class="unitime-Page" width="100%">
	    <tr><td>
    		<table id="UniTimeGWT:Header" class="unitime-MainTable" cellpadding="2" cellspacing="0" width="100%">
		   		<tr><td rowspan="2"><a href='http://www.unitime.org'><img src="images/unitime.png" border="0"/></a></td>
		   			<td nowrap="nowrap" width="100%" align="right" valign="middle" class="unitime-Title" style="padding-right: 20px; padding-top: 5px">
		   				<span class='unitime-Title'>University Timetabling Application</span>
		   			</td>
	    		</tr>
	    	</table>
	    </td></tr><tr><td style="min-width: 800px">
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
	    </td></tr></table>
    </td></tr><tr><td>
    	<table class="unitime-Footer">
    		<tr>
    			<td width="33%"></td>
    			<td width="34%" align="center" nowrap="nowrap"><a class='unitime-FooterLink' href='http://www.unitime.org'>&copy; 2008 - 2010 UniTime.org</a></td>
    			<td width="33%" align="right" nowrap="nowrap">
    			</td>
    			</tr>
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
</HTML>
