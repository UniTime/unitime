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
package org.unitime.timetable.events;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.EventInterface.RequestSessionDetails;
import org.unitime.timetable.gwt.shared.EventInterface.SessionMonth;
import org.unitime.timetable.model.EventDateMapping;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.DateUtils;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(RequestSessionDetails.class)
public class DateSelectorBackend extends EventAction<RequestSessionDetails, GwtRpcResponseList<SessionMonth>> {

	@Override
	public GwtRpcResponseList<SessionMonth> execute(RequestSessionDetails command, EventContext context) {
		Session session = SessionDAO.getInstance().get(command.getSessionId());
		
		GwtRpcResponseList<SessionMonth> response = new GwtRpcResponseList<SessionMonth>();
		
		Calendar calendar = Calendar.getInstance();

		List<Date> finals = new ArrayList<Date>();
		for (Number dateOffset: (List<Number>)SessionDAO.getInstance().getSession().createQuery(
				"select distinct dateOffset from ExamPeriod where session.uniqueId = :sessionId and examType.type = :finalType")
				.setLong("sessionId", command.getSessionId()).setInteger("finalType", ExamType.sExamTypeFinal).setCacheable(true).list()) {
		    calendar.setTime(session.getExamBeginDate());
		    calendar.add(Calendar.DAY_OF_YEAR, dateOffset.intValue());
		    finals.add(calendar.getTime());
		}
		
		EventDateMapping.Class2EventDateMap class2eventDateMap = (context.hasPermission(Right.EventDateMappings) ? EventDateMapping.getMapping(command.getSessionId()) : null);
		
		for (int month = session.getStartMonth(); month <= session.getEndMonth(); month ++) {
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

				for (Date finalDate: finals) {
					if (compare(calendar.getTime(), finalDate) == 0)
						m.setFlag(i, SessionMonth.Flag.FINALS);
				}
				
				if (compare(calendar.getTime(), session.getEventBeginDate()) < 0 || compare(calendar.getTime(), session.getEventEndDate()) > 0)
					m.setFlag(i, SessionMonth.Flag.DISABLED);
				else if (context.isPastOrOutside(calendar.getTime()))
					m.setFlag(i, SessionMonth.Flag.PAST);
				
				switch (calendar.get(Calendar.DAY_OF_WEEK)) {
				case Calendar.SATURDAY:
				case Calendar.SUNDAY:
					m.setFlag(i, SessionMonth.Flag.WEEKEND);
				}
				
				if (class2eventDateMap != null && class2eventDateMap.hasClassDate(calendar.getTime()))
					m.setFlag(i, SessionMonth.Flag.DATE_MAPPING_CLASS);
				
				if (class2eventDateMap != null && class2eventDateMap.hasEventDate(calendar.getTime()))
					m.setFlag(i, SessionMonth.Flag.DATE_MAPPING_EVENT);

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
