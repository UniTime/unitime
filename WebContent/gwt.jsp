<!-- 
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC
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
 -->
<%@ page language="java" pageEncoding="utf-8" contentType="text/html;charset=utf-8"%>
<%@ page errorPage="error.jsp"%>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <meta http-equiv="content-type" content="text/html; charset=UTF-8">
    <link type="text/css" rel="stylesheet" href="unitime/gwt/standard/standard.css">
    <link type="text/css" rel="stylesheet" href="styles/unitime.css">
    <link rel="shortcut icon" href="images/timetabling.ico">
    <title>UniTime 3.2| University Timetabling Application</title>
  </head>
  <body class="unitime-Body">
    <iframe src="javascript:''" id="__gwt_historyFrame" tabIndex="-1" style="position:absolute;width:0;height:0;border:0"></iframe>
    <iframe src="javascript:''" id="__printingFrame" tabIndex="-1" style="position:absolute;width:0;height:0;border:0"></iframe>
    
    <span id='UniTimeGWT:MenuBar' style="display: none;" ></span>

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
	    			<span id='UniTimeGWT:Title'></span>
	    		</td></tr>
	    		<tr><td width="100%" align="right" valign="middle" nowrap="nowrap">
	    			<span id='UniTimeGWT:Header'></span>
	    		</td></tr>
	    		<tr><td width="100%" align="left" valign="middle" width="100%">
	    			<span id='UniTimeGWT:TitlePanel'></span>
	    		</td></tr>
	    	</table>
	    </td></tr><tr><td>
	    	<table id="UniTimeGWT:Loading" class="unitime-MainTable" cellpadding="2" cellspacing="0" width="100%">
	    		<tr><td align="center">
	    			<i>Page is loading, please wait ...</i>
	    		</td></tr>
	    	</table>
	    </td></tr><tr><td>
	    	<span id='UniTimeGWT:Body'></span>
	    </td></tr></table>
    </td></tr><tr><td>
    	<table class="unitime-Footer">
    		<tr>
    			<td width="33%" align="left" nowrap="nowrap"><span id="UniTimeGWT:Version"></span></td>
    			<td width="34%" align="center" nowrap="nowrap"><a class='unitime-FooterLink' href='http://www.unitime.org'>&copy; 2010 UniTime.org</a></td>
    			<td width="33%" align="right"></td>
    		</tr>
    		<tt:hasProperty name="tmtbl.page.disclaimer">
    			<tr>
    				<td colspan="3" align="center" style="color:#777777; max-width: 800px;">
    					<tt:property name="tmtbl.page.disclaimer"/>
    				</td>
    			</tr>
    		</tt:hasProperty>
    	</table>
	</td></tr></table>
    <script type="text/javascript" language="javascript" src="unitime/unitime.nocache.js"></script>
  </body>
</html>
