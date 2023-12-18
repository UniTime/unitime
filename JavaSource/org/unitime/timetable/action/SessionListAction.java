/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.action;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TreeSet;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.form.BlankForm;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.context.UniTimeUserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Formats;


/** 
 * @author Tomas Muller
 */
@Action(value="sessionList", results = {
		@Result(name = "showSessionList", type = "tiles", location = "sessionList.tiles")
	})
@TilesDefinition(name = "sessionList.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Academic Sessions"),
		@TilesPutAttribute(name = "body", value = "/admin/sessionList.jsp")
	})
public class SessionListAction extends UniTimeAction<BlankForm> {
	private static final long serialVersionUID = -6663444732727632201L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static StudentSectioningMessages SCT_MSG = Localization.create(StudentSectioningMessages.class);

	public String execute() throws Exception {
        // Check access
		sessionContext.checkPermission(Right.AcademicSessions);

		WebTable webTable = new WebTable(
				12, "", "sessionList.action?order=%%",
				new String[] {
					MSG.columnAcademicSessionDefault().replace("\n", "<br>"),
					MSG.columnAcademicSessionTermYear().replace("\n", "<br>"),
					MSG.columnAcademicSessionInitiative().replace("\n", "<br>"),
					MSG.columnAcademicSessionStartDate().replace("\n", "<br>"),
					MSG.columnAcademicSessionClassesEndDate().replace("\n", "<br>"),
					MSG.columnAcademicSessionEndDate().replace("\n", "<br>"),
					MSG.columnAcademicSessionExamStartDate().replace("\n", "<br>"),
					MSG.columnAcademicSessionDefaultDatePattern().replace("\n", "<br>"),
					MSG.columnAcademicSessionCurrentStatus().replace("\n", "<br>"),
					MSG.columnAcademicSessionClassDuration().replace("\n", "<br>"),
					MSG.columnAcademicSessionEventStartDate().replace("\n", "<br>"),
					MSG.columnAcademicSessionEventEndDate().replace("\n", "<br>"),
					MSG.columnAcademicSessionEnrollmentAddDeadline().replace("\n", "<br>"),
					MSG.columnAcademicSessionEnrollmentChangeDeadline().replace("\n", "<br>"),
					MSG.columnAcademicSessionEnrollmentDropDeadline().replace("\n", "<br>"),
					MSG.columnAcademicSessionSectioningStatus().replace("\n", "<br>"),
					MSG.columnAcademicSessionDefaultInstructionalMethod().replace("\n", "<br>"),
					MSG.columnAcademicSessionNotificationsDates().replace("\n", "<br>"),
					},
				new String[] { "center", "left", "left", "left", "left",
					"left", "left", "left", "left", "right", "left", "left", "left", "left", "left", "left", "left", "left" }, 
				new boolean[] { true, true, true, false, false, false, true, false, true, true, true, true, true, true, true, true, true });
		
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.SESSION_DATE);
		Formats.Format<Date> dsf = Formats.getDateFormat(Formats.Pattern.DATE_SHORT);
		
		TreeSet<Session> sessions = new TreeSet<Session>(SessionDAO.getInstance().findAll());
		Session defaultSession = UniTimeUserContext.defaultSession(sessions, sessionContext.getUser().getCurrentAuthority(), UserProperty.PrimaryCampus.get(sessionContext.getUser()));

		for (Session s: SessionDAO.getInstance().findAll()) {
			Calendar ce = Calendar.getInstance(Locale.US); ce.setTime(s.getSessionBeginDateTime());
			ce.add(Calendar.WEEK_OF_YEAR, s.getLastWeekToEnroll()); ce.add(Calendar.DAY_OF_YEAR, -1);

			Calendar cc = Calendar.getInstance(Locale.US); cc.setTime(s.getSessionBeginDateTime());
			cc.add(Calendar.WEEK_OF_YEAR, s.getLastWeekToChange()); cc.add(Calendar.DAY_OF_YEAR, -1);

			Calendar cd = Calendar.getInstance(Locale.US); cd.setTime(s.getSessionBeginDateTime());
			cd.add(Calendar.WEEK_OF_YEAR, s.getLastWeekToDrop()); cd.add(Calendar.DAY_OF_YEAR, -1);
			
			String notifications = "";
			if (s.getNotificationsBeginDate() != null) {
				if (s.getNotificationsEndDate() != null) {
					notifications = MSG.notificationDatesBetween(dsf.format(s.getNotificationsBeginDate()), dsf.format(s.getNotificationsEndDate()));
				} else {
					notifications = MSG.notificationDatesFrom(dsf.format(s.getNotificationsBeginDate()));
				}
			} else if (s.getNotificationsEndDate() != null) {
				notifications = MSG.notificationDatesTo(dsf.format(s.getNotificationsEndDate()));
			}
			
			webTable.addLine(
					sessionContext.hasPermission(s, Right.AcademicSessionEdit) ?  "onClick=\"document.location='sessionEdit.action?op=editSession&sessionId=" + s.getSessionId() + "';\"" : null,
					new String[] {
						s.equals(defaultSession) ? "<img src='images/accept.png'> " : "&nbsp; ", 
						s.getAcademicTerm() + " " + s.getAcademicYear(),
						s.academicInitiativeDisplayString(),
						df.format(s.getSessionBeginDateTime()).replace(" ", "&nbsp;"),
						df.format(s.getClassesEndDateTime()).replace(" ", "&nbsp;"),
						df.format(s.getSessionEndDateTime()).replace(" ", "&nbsp;"),
						(s.getExamBeginDate()==null?"N/A":df.format(s.getExamBeginDate()).replace(" ", "&nbsp;")),
						s.getDefaultDatePattern()!=null ? s.getDefaultDatePattern().getName() : "-", 
						s.statusDisplayString(),
						s.getDefaultClassDurationType() == null ? "&nbsp;" : s.getDefaultClassDurationType().getAbbreviation(),
						(s.getEventBeginDate()==null?"N/A":df.format(s.getEventBeginDate()).replace(" ", "&nbsp;")),
						(s.getEventEndDate()==null?"N/A":df.format(s.getEventEndDate()).replace(" ", "&nbsp;")),
						df.format(ce.getTime()).replace(" ", "&nbsp;"),
						df.format(cc.getTime()).replace(" ", "&nbsp;"),
						df.format(cd.getTime()).replace(" ", "&nbsp;"),
						(s.getDefaultSectioningStatus() == null ? "&nbsp;" : s.getDefaultSectioningStatus().getReference()),
						(s.getDefaultInstructionalMethod() ==  null ? "&nbsp;" : s.getDefaultInstructionalMethod().getReference()),
						notifications,
						},
					new Comparable[] {
						s.equals(defaultSession) ? "<img src='images/accept.png'>" : "",
						s.getLabel(),
						s.academicInitiativeDisplayString(),
						s.getSessionBeginDateTime(),
						s.getClassesEndDateTime(),
						s.getSessionEndDateTime(),
						s.getExamBeginDate(),
						s.getDefaultDatePattern()!=null ? s.getDefaultDatePattern().getName() : "-", 
						s.statusDisplayString(),
						s.getDefaultClassDurationType() == null ? " " : s.getDefaultClassDurationType().getAbbreviation(),
						s.getEventBeginDate(),
						s.getEventEndDate(),
						ce.getTime(), cc.getTime(), cd.getTime(),
						(s.getDefaultSectioningStatus() == null ? " " : s.getDefaultSectioningStatus().getReference()),
						(s.getDefaultInstructionalMethod() ==  null ? "" : s.getDefaultInstructionalMethod().getReference()),
						(s.getNotificationsBeginDate() != null ? s.getNotificationsBeginDate() : s.getNotificationsEndDate()),
						});
		}
				
		webTable.enableHR("#9CB0CE");
		
		int orderCol = 4;
		if (request.getParameter("order")!=null) {
			try {
				orderCol = Integer.parseInt(request.getParameter("order"));
			} catch (Exception e){
				orderCol = 4;
			}
		}
		request.setAttribute("table", webTable.printTable(orderCol));
		
		return "showSessionList";
	}

}
