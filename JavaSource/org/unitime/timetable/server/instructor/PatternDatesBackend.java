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
package org.unitime.timetable.server.instructor;

import java.util.Calendar;
import java.util.Date;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.command.server.GwtRpcLogging;
import org.unitime.timetable.gwt.command.server.GwtRpcLogging.Level;
import org.unitime.timetable.gwt.shared.EventInterface.SessionMonth;
import org.unitime.timetable.gwt.shared.InstructorInterface.PatternDatesRequest;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.DateUtils;

@GwtRpcImplements(PatternDatesRequest.class)
@GwtRpcLogging(Level.DISABLED)
public class PatternDatesBackend implements GwtRpcImplementation<PatternDatesRequest, GwtRpcResponseList<SessionMonth>> {

	@Override
	public GwtRpcResponseList<SessionMonth> execute(PatternDatesRequest request, SessionContext context) {
		Session session = SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId());
		return listMonths(session);
	}
	
	public static GwtRpcResponseList<SessionMonth> listMonths(Session session) {
		GwtRpcResponseList<SessionMonth> response = new GwtRpcResponseList<SessionMonth>();
		
		Calendar calendar = Calendar.getInstance();

		int firstDayOfWeek = ApplicationProperty.TimeGridFirstDayOfWeek.intValue();
		int firstDay = Calendar.MONDAY;
		switch (firstDayOfWeek) {
		case 0: firstDay = Calendar.MONDAY; break;
		case 1: firstDay = Calendar.TUESDAY; break;
		case 2: firstDay = Calendar.WEDNESDAY; break;
		case 3: firstDay = Calendar.THURSDAY; break;
		case 4: firstDay = Calendar.FRIDAY; break;
		case 5: firstDay = Calendar.SATURDAY; break;
		case 6: firstDay = Calendar.SUNDAY; break;
		}
		
		for (int month = session.getPatternStartMonth(); month <= session.getPatternEndMonth(); month ++) {
			calendar.setTime(DateUtils.getDate(1, month, session.getSessionStartYear()));
			
			SessionMonth m = new SessionMonth(
					calendar.get(Calendar.YEAR),
					calendar.get(Calendar.MONTH));
			
			int nrDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			for (int i = 0; i < nrDays; i++) {
				switch (session.getHoliday(1 + i, month)) {
				case Session.sHolidayTypeBreak:
					m.setFlag(i, SessionMonth.Flag.BREAK);
					break;
				case Session.sHolidayTypeHoliday:
					m.setFlag(i, SessionMonth.Flag.HOLIDAY);
					break;
				}
				
				if (compare(calendar.getTime(), session.getSessionBeginDateTime()) == 0)
					m.setFlag(i, SessionMonth.Flag.START);

				if (compare(calendar.getTime(), session.getClassesEndDateTime()) == 0)
					m.setFlag(i, SessionMonth.Flag.END);
				
				if (compare(calendar.getTime(), session.getSessionBeginDateTime()) >= 0 &&
					compare(calendar.getTime(), session.getClassesEndDateTime()) <= 0)
					m.setFlag(i, SessionMonth.Flag.CLASSES);
				
				int dayInv = (7 + calendar.get(Calendar.DAY_OF_WEEK) - firstDay) % 7;
				if (dayInv >= 5)
					m.setFlag(i, SessionMonth.Flag.WEEKEND);
				
				calendar.add(Calendar.DAY_OF_YEAR, 1);
			}
			
			response.add(m);
		}
		
		return response;
	}
	
	private static int compare(Date d1, Date d2) {
		Calendar c1 = Calendar.getInstance(); c1.setTime(d1);
		Calendar c2 = Calendar.getInstance(); c2.setTime(d2);
		int cmp = compare(c1, c2, Calendar.YEAR);
		if (cmp != 0) return cmp;
		return compare(c1, c2, Calendar.DAY_OF_YEAR);
	}
	
	private static int compare(Calendar c1, Calendar c2, int field) {
		return Integer.valueOf(c1.get(field)).compareTo(c2.get(field));
	}

	

}
