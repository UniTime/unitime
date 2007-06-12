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
<%@ page import="org.unitime.timetable.model.dao.TimePatternDAO" %>
<%@ page import="org.unitime.timetable.model.TimePattern" %>
<%@ page import="org.unitime.timetable.model.TimePatternModel" %>
<script language="javascript" src="../scripts/rtt.js"></script>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html style="background-color:white">
  <head>
    <title>Combining time patterns</title>
  </head>
  <body><form method="post" action="tpcomb.jsp">
<%
try {
	TimePattern timePattern = null;
	for (Iterator i=TimePattern.findByMinPerWeek("puWestLafayetteTrdtn","Fal",false,false,false,30, null).iterator();i.hasNext();) {
		timePattern = (TimePattern)i.next();
		break;
	}
	RequiredTimeTable rttMain = timePattern.getRequiredTimeTable(true);
	rttMain.setName("tmaim");
	rttMain.update(request);
	out.println(rttMain.print(true,false,true,true));
	int alg = TimePatternModel.sMixAlgAverage;
	if (request.getParameter("alg")!=null)
		alg = Integer.parseInt(request.getParameter("alg"));
%>
	<br>
	Algorithm: <select name='alg'>
<%
		for (int i=0;i<TimePatternModel.sMixAlgs.length;i++) { 
%>
			<option value='<%=i%>' <%=(alg==i?"selected":"")%>><%=TimePatternModel.sMixAlgs[i]%></option>
<%
		}
%>
   </select> <input type='submit' value='Update' accesskey="0"/><br><hr>
<%
	Vector timePatterns = new Vector((new TimePatternDAO()).findAll());
	Collections.sort(timePatterns);
	for (Iterator i=timePatterns.iterator();i.hasNext();) {
		TimePattern tp = (TimePattern)i.next();
		if (!tp.getAcademicInitiative().equals("puWestLafayetteTrdtn")) continue;
		if (!tp.getAcademicTerm().equals("Fal")) continue;
		RequiredTimeTable rtt = tp.getRequiredTimeTable(true);
		if (rtt.getModel().isExactTime()) continue;
		//rtt.update(request);
		((TimePatternModel)rtt.getModel()).combineWith((TimePatternModel)rttMain.getModel(),true, alg);
		rtt.setName("t"+tp.getUniqueId());
		out.println(rtt.print(false,false,false,true));
		out.println("<br>");
	}
} catch (Exception e) {
	e.printStackTrace();
	throw e;
}
%>
	</form>
  </body>
</html>