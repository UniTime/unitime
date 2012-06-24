/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
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
*/
package org.unitime.timetable.events;

import java.util.Calendar;
import java.util.Date;

import org.unitime.timetable.gwt.client.events.SessionDatesSelector;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcHelper;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.util.DateUtils;

public class DateSelectorBackend extends EventAction<SessionDatesSelector.RequestSessionDetails, GwtRpcResponseList<SessionDatesSelector.SessionMonth>> {

	@Override
	public GwtRpcResponseList<SessionDatesSelector.SessionMonth> execute(SessionDatesSelector.RequestSessionDetails command, GwtRpcHelper helper, EventRights rights) {
		Session session = SessionDAO.getInstance().get(command.getSessionId());
		
		GwtRpcResponseList<SessionDatesSelector.SessionMonth> response = new GwtRpcResponseList<SessionDatesSelector.SessionMonth>();
		
		Calendar calendar = Calendar.getInstance();
		for (int month = session.getStartMonth(); month <= session.getEndMonth(); month ++) {
			calendar.setTime(DateUtils.getDate(1, month, session.getSessionStartYear()));
			
			SessionDatesSelector.SessionMonth m = new SessionDatesSelector.SessionMonth(
					calendar.get(Calendar.YEAR),
					calendar.get(Calendar.MONTH));
			
			int nrDays = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
			for (int i = 0; i < nrDays; i++) {
				switch (session.getHoliday(1 + i, month)) {
				case Session.sHolidayTypeBreak:
					m.setFlag(i, SessionDatesSelector.SessionMonth.Flag.BREAK);
					break;
				case Session.sHolidayTypeHoliday:
					m.setFlag(i, SessionDatesSelector.SessionMonth.Flag.HOLIDAY);
					break;
				}
				
				if (compare(calendar.getTime(), session.getSessionBeginDateTime()) == 0)
					m.setFlag(i, SessionDatesSelector.SessionMonth.Flag.START);

				if (compare(calendar.getTime(), session.getSessionEndDateTime()) == 0)
					m.setFlag(i, SessionDatesSelector.SessionMonth.Flag.END);

				if (compare(calendar.getTime(), session.getExamBeginDate()) == 0)
					m.setFlag(i, SessionDatesSelector.SessionMonth.Flag.EXAM_START);

				if (compare(calendar.getTime(), session.getEventBeginDate()) < 0 || compare(calendar.getTime(), session.getEventEndDate()) > 0)
					m.setFlag(i, SessionDatesSelector.SessionMonth.Flag.DISABLED);
				else if (rights.isPastOrOutside(calendar.getTime()))
					m.setFlag(i, SessionDatesSelector.SessionMonth.Flag.PAST);
				
				calendar.add(Calendar.DAY_OF_YEAR, 1);
			}
			
			response.add(m);
		}
		
		return response;
	}
	
	private int compare(Date d1, Date d2) {
		Calendar c1 = Calendar.getInstance(); c1.setTime(d1);
		Calendar c2 = Calendar.getInstance(); c2.setTime(d2);
		int cmp = compare(c1, c2, Calendar.YEAR);
		if (cmp != 0) return cmp;
		return compare(c1, c2, Calendar.DAY_OF_YEAR);
	}
	
	private int compare(Calendar c1, Calendar c2, int field) {
		return new Integer(c1.get(field)).compareTo(c2.get(field));
	}

}
