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
<%@page import="java.util.Locale"%>
<%@page import="java.util.Calendar"%>
<%@ page import="java.text.DecimalFormat"%>
<%@ page import="java.text.DateFormat"%>
<%@ page import="org.unitime.commons.web.*"%>
<%@page import="org.unitime.timetable.model.RoomType"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld"	prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld"	prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld"	prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<html:form action="sessionEdit">

	<table width="100%" border="0" cellspacing="0" cellpadding="3">
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

	<table width="100%" border="0" cellspacing="0" cellpadding="3">
		<%
			WebTable webTable = new WebTable(
					12, "", "sessionList.do?order=%%",					
					new String[] {
						"Default", "Academic<br>Session", "Academic<br>Initiative", "Session<br>Begins",
						"Classes<br>End", "Session<br>Ends", "Exams<br>Begins", "Date<br>Pattern", "Status", "Subject<br>Areas", 
						"Events<br>Begins", "Events<br>Ends", "Event<br>Management", "<br>Enrollment", "Deadline<br>Change", "<br>Drop", "Sectioning<br>Status" },
					new String[] { "center", "left", "left", "left", "left",
						"left", "left", "left", "left", "right", "left", "left", "left", "left", "left", "left", "left" }, 
					new boolean[] { true, true, true, false, false, false, true, false, true, true, true, true, true, true, true, true });
					
			webTable.enableHR("#9CB0CE");
					
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
					
					Calendar ce = Calendar.getInstance(Locale.US); ce.setTime(s.getSessionBeginDateTime());
					ce.add(Calendar.WEEK_OF_YEAR, s.getLastWeekToEnroll()); ce.add(Calendar.DAY_OF_YEAR, -1);

					Calendar cc = Calendar.getInstance(Locale.US); cc.setTime(s.getSessionBeginDateTime());
					cc.add(Calendar.WEEK_OF_YEAR, s.getLastWeekToChange()); cc.add(Calendar.DAY_OF_YEAR, -1);

					Calendar cd = Calendar.getInstance(Locale.US); cd.setTime(s.getSessionBeginDateTime());
					cd.add(Calendar.WEEK_OF_YEAR, s.getLastWeekToDrop()); cd.add(Calendar.DAY_OF_YEAR, -1);
					
					webTable
					.addLine(
							"onClick=\"document.location='sessionEdit.do?doit=editSession&sessionId=" + s.getSessionId() + "';\"",
							new String[] {
								s.getIsDefault() ? "<img src='images/tick.gif'> " : "&nbsp; ", 
								s.getAcademicTerm() + " " + s.getSessionStartYear(),
								s.academicInitiativeDisplayString(),
								df.format(s.getSessionBeginDateTime()).replace(" ", "&nbsp;"),
								df.format(s.getClassesEndDateTime()).replace(" ", "&nbsp;"),
								df.format(s.getSessionEndDateTime()).replace(" ", "&nbsp;"),
								(s.getExamBeginDate()==null?"N/A":df.format(s.getExamBeginDate()).replace(" ", "&nbsp;")),
								s.getDefaultDatePattern()!=null ? s.getDefaultDatePattern().getName() : "-", 
								s.statusDisplayString(),
								df5.format(s.getSubjectAreas().size()),
								(s.getEventBeginDate()==null?"N/A":df.format(s.getEventBeginDate()).replace(" ", "&nbsp;")),
								(s.getEventEndDate()==null?"N/A":df.format(s.getEventEndDate()).replace(" ", "&nbsp;")),
								roomTypes,
								df.format(ce.getTime()).replace(" ", "&nbsp;"),
								df.format(cc.getTime()).replace(" ", "&nbsp;"),
								df.format(cd.getTime()).replace(" ", "&nbsp;"),
								(s.getDefaultSectioningStatus() == null ? "&nbsp;" : s.getDefaultSectioningStatus().getReference()),
								 },
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
								s.getEventBeginDate(),
								s.getEventEndDate(),
								roomTypes,
								ce.getTime(), cc.getTime(), cd.getTime(),
								(s.getDefaultSectioningStatus() == null ? " " : s.getDefaultSectioningStatus().getReference()) } );
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
	
	<table width="100%" border="0" cellspacing="0" cellpadding="3">
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
	
