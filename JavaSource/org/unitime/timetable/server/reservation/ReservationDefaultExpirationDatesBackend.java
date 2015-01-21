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
package org.unitime.timetable.server.reservation;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
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
		String expirationStr = ApplicationProperty.ReservationExpirationDate.value(type, ApplicationProperty.ReservationExpirationDateGlobal.value()); 
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

		String expInDaysStr = ApplicationProperty.ReservationExpirationInDays.value(type, ApplicationProperty.ReservationExpirationInDaysGlobal.value());
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
