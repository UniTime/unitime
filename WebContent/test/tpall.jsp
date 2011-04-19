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
<%@ page import="org.unitime.timetable.model.Session" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>Time pattern test page (all)</title>
	<script language="javascript" src="../scripts/rtt.js"></script>
  </head>
  <body><form method="post" action="tpall.jsp">
  <input type='hidden' name='canEdit' value='1'/>
  
<%
try {
	Long sessionId = Session.defaultSession().getUniqueId();
	List<TimePattern> timePatterns = TimePattern.findAll(sessionId,true);
	boolean canEdit = request.getParameter("canEdit")==null || "1".equals(request.getParameter("canEdit"));
	for (Iterator i=timePatterns.iterator();i.hasNext();) {
		TimePattern tp = (TimePattern)i.next();
		if (tp.getType().intValue()==TimePattern.sTypeExtended) continue;
		RequiredTimeTable rtt = tp.getRequiredTimeTable(true);
		rtt.setName("t"+tp.getUniqueId());
		rtt.update(request);
		out.println(rtt.print(canEdit,false,canEdit,true));
		out.println("<br>");
	}
%>
	<input type='submit' value='Update' accesskey="0"/>
<% if (canEdit) { %>
	<input type='button' value='Read-Only' onClick="canEdit.value=0;submit();"/>
<% } 
} catch (Exception e) {
	e.printStackTrace();
	throw e;
}
%>
	</form>
  </body>
</html>
