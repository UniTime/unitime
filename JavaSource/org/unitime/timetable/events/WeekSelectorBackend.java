/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
import java.util.Locale;

import org.unitime.localization.impl.Localization;
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
		while (c.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
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
