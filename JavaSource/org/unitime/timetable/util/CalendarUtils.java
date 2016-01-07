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
package org.unitime.timetable.util;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * @author Heston Fernandes, Tomas Muller
 */
public class CalendarUtils {

	/**
	 * Check if a string is a valid date
	 * @param date String to be checked
	 * @param dateFormat format of the date e.g. MM/dd/yyyy - see SimpleDateFormat
	 * @return true if it is a valid date
	 * Use {@link Formats.Format.isValid(boolean)} instead.
	 */
	@Deprecated
	public static boolean isValidDate(String date, String dateFormat) {
		return Formats.getDateFormat(dateFormat).isValid(date);
	}
	
	/**
	 * Parse a string to give a Date object
	 * @param date
	 * @param dateFormat format of the date e.g. MM/dd/yyyy - see SimpleDateFormat
	 * @return null if not a valid date
	 * Use {@link Formats.Format.parse(String)} instead.
	 */
	public static Date getDate(String date, String dateFormat) {
		try {
			return Formats.getDateFormat(dateFormat).parse(date);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static int date2dayOfYear(int sessionYear, Date meetingDate) {
		Calendar c = Calendar.getInstance(Locale.US);
		c.setTime(meetingDate);
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
		return dayOfYear;
	}
	
	public static Date dateOfYear2date(int sessionYear, int dayOfYear) {
		Calendar c = Calendar.getInstance(Locale.US);
		c.set(sessionYear, 11, 31, 0, 0, 0);
		c.set(Calendar.MILLISECOND, 0);
		if (dayOfYear <= 0) {
			c.set(Calendar.YEAR, sessionYear - 1);
			dayOfYear += c.get(Calendar.DAY_OF_YEAR);
		} else if (dayOfYear > c.get(Calendar.DAY_OF_YEAR)) {
			dayOfYear -= c.get(Calendar.DAY_OF_YEAR);
			c.set(Calendar.YEAR, sessionYear + 1);
		}
		c.set(Calendar.DAY_OF_YEAR, dayOfYear);
		System.err.println(c.getTime() + " (" + c.getTime().getTime() + ")");
		return c.getTime();
	}
}
