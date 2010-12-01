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
<%@ page import="org.unitime.timetable.model.Room" %>
<%@ page import="org.unitime.timetable.model.RoomSharingModel" %>
<%@ page import="org.unitime.timetable.model.dao.RoomDAO" %>
<%@ page import="org.unitime.timetable.model.Department" %>
<%@ page import="org.unitime.timetable.model.Session" %>
<script language="javascript" src="../scripts/rtt.js"></script>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html style="background-color:white">
  <head>
    <title>Room sharing test</title>
  </head>
  <body><form method="post" action="roomshare.jsp">
  <input type='hidden' name='canEdit' value='1'/>
<%
	boolean canEdit = request.getParameter("canEdit")==null || "1".equals(request.getParameter("canEdit"));
	Room room = (new RoomDAO()).get(new Long(3798)); //findRoom("2006Fal","GRIS","180"); //take some room here
	//RequiredTimeTable rtt = room.getRoomSharingTable();

	Long sessionId = Session.defaultSession().getUniqueId();
	Vector departments = new Vector();
	//departments.add(Department.findByDeptCode("1980",sessionId));
	departments.add(Department.findByDeptCode("1994",sessionId));
	departments.add(Department.findByDeptCode("1280",sessionId));
	departments.add(Department.findByDeptCode("1284",sessionId));
	departments.add(Department.findByDeptCode("1282",sessionId));
	departments.add(Department.findByDeptCode("1067",sessionId));
	departments.add(Department.findByDeptCode("1245",sessionId));
	departments.add(Department.findByDeptCode("1599",sessionId));
	departments.add(Department.findByDeptCode("1368",sessionId));
	departments.add(Department.findByDeptCode("1300",sessionId));
	departments.add(Department.findByDeptCode("1301",sessionId));
	departments.add(Department.findByDeptCode("1297",sessionId));
	departments.add(Department.findByDeptCode("1298",sessionId));
	departments.add(Department.findByDeptCode("1954",sessionId));
	departments.add(Department.findByDeptCode("1370",sessionId));
	departments.add(Department.findByDeptCode("1550",sessionId));
	RequiredTimeTable rtt = room.getRoomSharingTable(departments);
	rtt.update(request);
	
	if (!canEdit) {
		//to save data beck to room call room.setRoomSharingModel (after update):
		//   room.setRoomSharingModel((RoomSharingModel)rtt.getModel());
		%><img src="../temp/<%=rtt.createImage(false).getName()%>" border="0"><br><%
	}
	out.println(rtt.print(canEdit,false,canEdit,false));
%>
	<br>
	Pattern: <%=rtt.getModel().getPreferences()%><br>
	ManagerIds: <%=((RoomSharingModel)rtt.getModel()).getManagerIds()%><br>
	
	<input type='submit' value='Update' accesskey="0"/>
<% if (canEdit) { %>
	<input type='button' value='Read-Only' onClick="canEdit.value=0;submit();"/>
<% } %>
	</form>
  </body>
</html>
