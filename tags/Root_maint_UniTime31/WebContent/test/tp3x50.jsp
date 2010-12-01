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
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="org.unitime.timetable.webutil.RequiredTimeTable" %>
<%@ page import="org.unitime.timetable.model.TimePattern" %>
<%@ page import="org.unitime.timetable.model.dao.TimePatternDAO" %>
<script language="javascript" src="../scripts/rtt.js"></script>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html style="background-color:white">
  <head>
    <title>Time pattern test page (3x50)</title>
  </head>
  <body><form method="post" action="tp3x50.jsp">
  <input type='hidden' name='canEdit' value='1'/>
<%
	TimePattern timePattern = null;
	List timePatterns = (new TimePatternDAO()).findAll();
	for (Iterator i=timePatterns.iterator();i.hasNext();) {
		TimePattern tp = (TimePattern)i.next();
		if (tp.getSlotsPerMtg().intValue()==12 && tp.getNrMeetings().intValue()==3) {
			timePattern  = tp; break;
		}
	}
	boolean canEdit = request.getParameter("canEdit")==null || "1".equals(request.getParameter("canEdit"));
	for (int i=1;i<20;i++) {
		RequiredTimeTable rtt = timePattern.getRequiredTimeTable(true);
		rtt.setName("t"+i);
		rtt.update(request);
		out.println(rtt.print(canEdit,false,canEdit,true));
		out.println("<br>");
	}
%>
	<input type='submit' value='Update' accesskey="0"/>
<% if (canEdit) { %>
	<input type='button' value='Read-Only' onClick="canEdit.value=0;submit();"/>
<% } %>
	</form>
  </body>
</html>
