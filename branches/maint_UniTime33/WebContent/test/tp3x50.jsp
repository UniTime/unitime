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
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="org.unitime.timetable.webutil.RequiredTimeTable" %>
<%@ page import="org.unitime.timetable.model.TimePattern" %>
<%@ page import="org.unitime.timetable.model.dao.TimePatternDAO" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>Time pattern test page (3x50)</title>
	<script language="javascript" src="../scripts/rtt.js"></script>
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
