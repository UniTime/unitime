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
package org.unitime.timetable.server.administration.session;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.admin.AcademicSessionsPage.AcademicSessionsRequest;
import org.unitime.timetable.gwt.client.admin.AcademicSessionsPage.AcademicSessionsResponse;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.context.UniTimeUserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Formats;

@GwtRpcImplements(AcademicSessionsRequest.class)
public class AcademicSessionsBackend implements GwtRpcImplementation<AcademicSessionsRequest, AcademicSessionsResponse>{
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static StudentSectioningMessages SCT_MSG = Localization.create(StudentSectioningMessages.class);

	@Override
	public AcademicSessionsResponse execute(AcademicSessionsRequest request, SessionContext context) {
		context.checkPermission(Right.AcademicSessions);
		
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.SESSION_DATE);
		Formats.Format<Date> dsf = Formats.getDateFormat(Formats.Pattern.DATE_SHORT);

		TableInterface table = new TableInterface();
		table.setId("AcademicSessions");
		table.setDefaultSortCookie("!" + MSG.columnAcademicSessionStartDate());
		
		LineInterface header = table.addHeader();
		header.addCell(MSG.columnAcademicSessionDefault()).setTextAlignment(Alignment.CENTER);
        header.addCell(MSG.columnAcademicSessionTermYear());
        header.addCell(MSG.columnAcademicSessionInitiative());
        header.addCell(MSG.columnAcademicSessionStartDate());
        header.addCell(MSG.columnAcademicSessionClassesEndDate());
        header.addCell(MSG.columnAcademicSessionEndDate());
        header.addCell(MSG.columnAcademicSessionExamStartDate());
        header.addCell(MSG.columnAcademicSessionDefaultDatePattern());
        header.addCell(MSG.columnAcademicSessionCurrentStatus());
        header.addCell(MSG.columnAcademicSessionClassDuration()).setTextAlignment(Alignment.CENTER);
        header.addCell(MSG.columnAcademicSessionEventStartDate());
        header.addCell(MSG.columnAcademicSessionEventEndDate());
        header.addCell(MSG.columnAcademicSessionEnrollmentAddDeadline());
        header.addCell(MSG.columnAcademicSessionEnrollmentChangeDeadline());
        header.addCell(MSG.columnAcademicSessionEnrollmentDropDeadline());
        header.addCell(MSG.columnAcademicSessionSectioningStatus());
        header.addCell(MSG.columnAcademicSessionDefaultInstructionalMethod());
        header.addCell(MSG.columnAcademicSessionNotificationsDates());
        for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    		cell.addStyle("white-space: pre-wrap;");
    		cell.setSortable(true);
    	}
        
		TreeSet<Session> sessions = new TreeSet<Session>(SessionDAO.getInstance().findAll());
		Session defaultSession = UniTimeUserContext.defaultSession(sessions, context.getUser().getCurrentAuthority(), UserProperty.PrimaryCampus.get(context.getUser()));

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
			
			LineInterface line = table.addLine();
			line.setId(s.getUniqueId());
			if (context.hasPermission(s, Right.AcademicSessionEdit))
				line.setURL("#" + s.getUniqueId());
			line.setAnchor("A" + s.getUniqueId());
			CellInterface defSes = line.addCell().setComparable(s.equals(defaultSession), s.getSessionBeginDateTime());
			if (s.equals(defaultSession))
				defSes.addImage().setSource("images/accept.png").setAlt(MSG.altYes());
			
			line.addCell(s.getAcademicTerm() + " " + s.getAcademicYear()).setComparable(s.getLabel());
			line.addCell(s.academicInitiativeDisplayString()).setComparable(s.academicInitiativeDisplayString(), s.getSessionBeginDateTime());
			line.addCell(df.format(s.getSessionBeginDateTime())).setComparable(s.getSessionBeginDateTime());
			line.addCell(df.format(s.getClassesEndDateTime())).setComparable(s.getClassesEndDateTime());
			line.addCell(df.format(s.getSessionEndDateTime())).setComparable(s.getSessionEndDateTime());
			line.addCell(s.getExamBeginDate() == null ? MSG.notApplicable() : df.format(s.getExamBeginDate())).setComparable(s.getExamBeginDate());
			line.addCell(s.getDefaultDatePattern() == null ? "-" : s.getDefaultDatePattern().getName());
			line.addCell(s.statusDisplayString());
			line.addCell(s.getDefaultClassDurationType() == null ? "" : s.getDefaultClassDurationType().getAbbreviation());
			line.addCell(s.getEventBeginDate() == null ? MSG.notApplicable() : df.format(s.getEventBeginDate())).setComparable(s.getEventBeginDate());
			line.addCell(s.getEventEndDate() == null ? MSG.notApplicable() : df.format(s.getEventEndDate())).setComparable(s.getEventEndDate());
			line.addCell(df.format(ce.getTime())).setComparable(ce.getTime());
			line.addCell(df.format(cc.getTime())).setComparable(cc.getTime());
			line.addCell(df.format(cd.getTime())).setComparable(cd.getTime());
			line.addCell(s.getDefaultSectioningStatus() == null ? "" : s.getDefaultSectioningStatus().getReference());
			line.addCell(s.getDefaultInstructionalMethod() == null ? "" : s.getDefaultInstructionalMethod().getReference());
			line.addCell(notifications).setComparable(s.getNotificationsBeginDate() != null ? s.getNotificationsBeginDate() : s.getNotificationsEndDate());
		}

		AcademicSessionsResponse response = new AcademicSessionsResponse();
		response.setSessionsTable(table);
		response.setCanAdd(context.hasPermission(Right.AcademicSessionAdd));
		
		return response;
	}

}
