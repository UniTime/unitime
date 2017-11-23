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

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.widgets.WeekSelector.WeekSelectorRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.EventInterface.DateInterface;
import org.unitime.timetable.gwt.shared.EventInterface.WeekInterface;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(WeekSelectorRequest.class)
public class WeekSelectorBackend implements GwtRpcImplementation<WeekSelectorRequest, GwtRpcResponseList<WeekInterface>> {
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	@Override
	public GwtRpcResponseList<WeekInterface> execute(WeekSelectorRequest command, SessionContext context) {
		GwtRpcResponseList<WeekInterface> ret = new GwtRpcResponseList<WeekInterface>();
		Session session = SessionDAO.getInstance().get(command.getSessionId());
		Calendar c = Calendar.getInstance(Locale.US);
		c.setTime(session.getEventBeginDate());
		int firstDayOfWeek = ApplicationProperty.EventGridStartDay.intValue();
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
		while (c.get(Calendar.DAY_OF_WEEK) != firstDay) {
			c.add(Calendar.DAY_OF_YEAR, -1);
		}
		int sessionYear = session.getSessionStartYear();
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT);
		while (!c.getTime().after(session.getEventEndDate())) {
			int dayOfYear = c.get(Calendar.DAY_OF_YEAR);
			if (c.get(Calendar.YEAR) < sessionYear) {
				Calendar x = Calendar.getInstance(Locale.US);
			    x.set(c.get(Calendar.YEAR),11,31,0,0,0);
			    dayOfYear -= x.get(Calendar.DAY_OF_YEAR);
			} else if (c.get(Calendar.YEAR) > sessionYear) {
				Calendar x = Calendar.getInstance(Locale.US);
			    x.set(sessionYear,11,31,0,0,0);
			    dayOfYear += x.get(Calendar.DAY_OF_YEAR);
			}
			WeekInterface week = new WeekInterface();
			week.setDayOfYear(dayOfYear);
			for (int i = 0; i < 7; i++) {
				week.addDayName(new DateInterface(df.format(c.getTime()), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH)));
				c.add(Calendar.DAY_OF_YEAR, 1);
			}
			ret.add(week);
		}
		return ret;
	}

}
