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
<%@ page import="java.text.DecimalFormat"%>
<%@ page import="java.text.DateFormat"%>
<%@ page import="org.unitime.commons.web.*"%>
<%@page import="org.unitime.timetable.model.RoomType"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld"	prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld"	prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld"	prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<html:form action="sessionEdit">

	<table width="98%" border="0" cellspacing="0" cellpadding="3">
		<tr>
			<td>
				<tt:section-header>
					<tt:section-title>
						
					</tt:section-title>
					<html:submit property="doit" styleClass="btn" accesskey="A" titleKey="title.addSession">
						<bean:message key="button.addSession" />
					</html:submit>
				</tt:section-header>
			</td>
		</tr>
	</table>

	<table width="90%" border="0" cellspacing="0" cellpadding="3">
		<%
			WebTable webTable = new WebTable(
					10, "", "sessionList.do?order=%%",					
					new String[] {
						"Default", "Academic<br>Session", "Academic<br>Initiative", "Session<br>Begins",
						"Classes<br>End", "Session<br>Ends", "Exams<br>Begins", "Date<br>Pattern", "Status", "Subject<br>Areas", "Event<br>Management" },
					new String[] { "center", "left", "left", "left", "left",
						"left", "left", "left", "left", "right", "left" }, 
					new boolean[] { true, true, true, false, false, false, true, false, true, true });
					
			webTable.enableHR("#EFEFEF");
	        webTable.setRowStyle("white-space: nowrap");
					
		%>

		<logic:iterate name="sessionListForm" property="sessions" id="sessn">
			<%
					DecimalFormat df5 = new DecimalFormat("####0");
					DateFormat df = DateFormat.getDateInstance();
					org.unitime.timetable.model.Session s = (org.unitime.timetable.model.Session) sessn;
					String roomTypes = ""; boolean all = true;
					for (RoomType t : RoomType.findAll()) {
						if (t.getOption(s).canScheduleEvents()) {
							if (roomTypes.length()>0) roomTypes+=", ";
							roomTypes+=t.getLabel();
						} else all = false;
					}
					if (all) roomTypes = "<i>All</i>";
					if (roomTypes.length()==0) roomTypes = "<i>N/A</i>";
					webTable
					.addLine(
							"onClick=\"document.location='sessionEdit.do?doit=editSession&sessionId=" + s.getSessionId() + "';\"",
							new String[] {
								s.getIsDefault() ? "<img src='images/tick.gif'> " : "&nbsp; ", 
								s.getLabel() + "&nbsp;",
								s.academicInitiativeDisplayString() + "&nbsp;",
								df.format(s.getSessionBeginDateTime()) + "&nbsp;",
								df.format(s.getClassesEndDateTime()) + "&nbsp;",
								df.format(s.getSessionEndDateTime()) + "&nbsp;",
								df.format(s.getExamBeginDate()) + "&nbsp;",
								s.getDefaultDatePattern()!=null ? s.getDefaultDatePattern().getName() : "-", 
								s.statusDisplayString() + "&nbsp;",
								df5.format(s.getSubjectAreas().size()),
								roomTypes },
							new Comparable[] {
								s.getIsDefault() ? "<img src='images/tick.gif'>" : "",
								s.getLabel(),
								s.academicInitiativeDisplayString(),
								s.getSessionBeginDateTime(),
								s.getClassesEndDateTime(),
								s.getSessionEndDateTime(),
								s.getExamBeginDate(),
								s.getDefaultDatePattern()!=null ? s.getDefaultDatePattern().getName() : "-", 
								s.statusDisplayString(),
								df5.format(s.getSubjectAreas().size()),
								roomTypes } );
			%>

		</logic:iterate>
		<%-- end interate --%>
		<%
		int orderCol = 4;
		if (request.getParameter("order")!=null) {
			try {
				orderCol = Integer.parseInt(request.getParameter("order"));
			}
			catch (Exception e){
				orderCol = 4;
			}
		}
		out.println(webTable.printTable(orderCol));
		%>

		<%-- print out the add link --%>

	</table>
	
	<table width="98%" border="0" cellspacing="0" cellpadding="3">
		<tr>
			<td align="center" class="WelcomeRowHead">
			&nbsp;
			</td>
		</tr>
		<tr>
			<td align="right">
				<html:submit property="doit" styleClass="btn" accesskey="A" titleKey="title.addSession">
					<bean:message key="button.addSession" />
				</html:submit>
			</td>
		</tr>
	</table>

</html:form>
	