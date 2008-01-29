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
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="org.unitime.timetable.webutil.RequiredTimeTable" %>
<%@ page import="org.unitime.timetable.model.*" %>
<%@ page import="org.unitime.timetable.model.dao.*"%>
<script language="javascript" src="../scripts/rtt.js"></script>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html style="background-color:white">
  <head>
    <title>Matching time patterns</title>
  </head>
  <body><form method="post" action="tpmatch.jsp">
<% 
	Session acadSession = Session.defaultSession();
	if (request.getParameter("session")!=null)
		acadSession = new SessionDAO().get(Long.parseLong(request.getParameter("session")));
	TimePattern pattern = null;
	if (request.getParameter("pattern")!=null)
		pattern = new TimePatternDAO().get(Long.parseLong(request.getParameter("pattern")));
	if (pattern!=null && acadSession==null) acadSession = pattern.getSession();
	Session newSession = acadSession;
	if (request.getParameter("newSession")!=null)
		newSession = new SessionDAO().get(Long.parseLong(request.getParameter("newSession")));
	int alg = TimePatternModel.sMixAlgMinMax;
	if (request.getParameter("alg")!=null)
		alg = Integer.parseInt(request.getParameter("alg"));
%>
  	Academic Session: <select name='session'>
<%
		for (Iterator i=Session.getAllSessions().iterator();i.hasNext();) {
			Session s = (Session)i.next();
%>
			<option value='<%=s.getUniqueId()%>' <%=(s.equals(acadSession)?"selected":"")%>><%=s.getLabel()%></option>
<%
		}
%>
   </select><br>
<%
	if (acadSession!=null) {
%>
	Time Pattern: <select name='pattern'>
<%
		for (Iterator i=TimePattern.findAll(acadSession,null).iterator();i.hasNext();) {
			TimePattern tp = (TimePattern)i.next();
%>
			<option value='<%=tp.getUniqueId()%>' <%=(tp.equals(pattern)?"selected":"")%>><%=tp.getName()%></option>
<%
		}
%>
	</select><br>
<%
	}
%>
	Algorithm: <select name='alg'>
<%
		for (int i=0;i<TimePatternModel.sMixAlgs.length;i++) { 
%>
			<option value='<%=i%>' <%=(alg==i?"selected":"")%>><%=TimePatternModel.sMixAlgs[i]%></option>
<%
		}
%>
   </select><br>
  	New Academic Session: <select name='newSession'>
<%
		for (Iterator i=Session.getAllSessions().iterator();i.hasNext();) {
			Session s = (Session)i.next();
%>
			<option value='<%=s.getUniqueId()%>' <%=(s.equals(newSession)?"selected":"")%>><%=s.getLabel()%></option>
<%
		}
%>
   </select><br>
<%
	if (pattern!=null) {
		RequiredTimeTable rtt = pattern.getRequiredTimeTable(true);
		rtt.update(request);
		out.println(rtt.print(true,false,true,true));
		out.println("<br>");
		if (newSession!=null) {
			TimePattern newPattern = TimePattern.getMatchingTimePattern(newSession.getUniqueId(), pattern);
			if (newPattern==null)
				out.println("<font color='red'>No matching time pattern found.</font>");
			else {
				RequiredTimeTable newRtt = newPattern.getRequiredTimeTable(true);
				((TimePatternModel)newRtt.getModel()).combineMatching((TimePatternModel)rtt.getModel(), true, alg);
				newRtt.setName("new");
				out.println(newRtt.print(false,false,false,true));
				out.println("<br>");
			}
		}
	}
%>
	<br>
	<input type='submit' value='Update' accesskey="0"/>
	</form>
  </body>
</html>