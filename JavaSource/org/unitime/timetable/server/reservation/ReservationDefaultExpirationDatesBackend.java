/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.server.reservation;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.ReservationInterface.DefaultExpirationDates;
import org.unitime.timetable.gwt.shared.ReservationInterface.ReservationDefaultExpirationDatesRpcRequest;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(ReservationDefaultExpirationDatesRpcRequest.class)
public class ReservationDefaultExpirationDatesBackend implements GwtRpcImplementation<ReservationDefaultExpirationDatesRpcRequest, DefaultExpirationDates> {
	private static String[] sTypes = new String[] { "individual", "group", "curriculum", "course" };
	private static Formats.Format<Date> sDF = Formats.getDateFormat("yyyy-MM-dd");

	@Override
	public DefaultExpirationDates execute(ReservationDefaultExpirationDatesRpcRequest request, SessionContext context) {
		DefaultExpirationDates expirations = new DefaultExpirationDates();

		if (request.getSessionId() != null) {
			Session session = SessionDAO.getInstance().get(request.getSessionId());
			for (String type: sTypes) {
				expirations.setExpirationDate(type, getDefaultExpirationDate(session, type));
			}
		} else if (context.getUser() != null && context.getUser().getCurrentAcademicSessionId() != null) {
			Session session = SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId());
			for (String type: sTypes) {
				expirations.setExpirationDate(type, getDefaultExpirationDate(session, type));
			}
		}
		
		return expirations;
	}

	public Date getDefaultExpirationDate(Session session, String type) {
		String expirationStr = ApplicationProperties.getProperty("unitime.reservations." + type + ".expiration_date", ApplicationProperties.getProperty("unitime.reservations.expiration_date"));
		Date expiration = null;
		if (expirationStr != null && !expirationStr.isEmpty()) {
			try {
				Calendar cal = Calendar.getInstance(Localization.getJavaLocale());
				cal.setTime(session.getSessionBeginDateTime());
				cal.add(Calendar.DAY_OF_YEAR, Integer.parseInt(expirationStr));
				expiration = cal.getTime();
			} catch (NumberFormatException e) {
				try {
					expiration = sDF.parse(expirationStr);
				} catch (ParseException f) {}
			}
		}

		String expInDaysStr = ApplicationProperties.getProperty("unitime.reservations." + type + ".expire_in_days", ApplicationProperties.getProperty("unitime.reservations.expire_in_days"));
		if (expInDaysStr != null && !expInDaysStr.isEmpty()) {
			try {
				Calendar cal = Calendar.getInstance(Localization.getJavaLocale());
				cal.set(Calendar.HOUR_OF_DAY, 0);
				cal.set(Calendar.MINUTE, 0);
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				cal.add(Calendar.DAY_OF_YEAR, Integer.parseInt(expInDaysStr));
				if (expiration == null || cal.getTime().after(expiration))
					expiration = cal.getTime();
			} catch (NumberFormatException e) {}
		}
		
		return expiration;
	}
}
