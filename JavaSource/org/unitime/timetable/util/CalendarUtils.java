/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.util;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
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
	 */
	public static boolean isValidDate(String date, String dateFormat) {
		
		if (date==null || dateFormat==null || date.trim().length()==0 || dateFormat.trim().length()==0)
			return false;
		
		SimpleDateFormat df = new SimpleDateFormat(dateFormat);
		df.setLenient(false);
		ParsePosition pos = new ParsePosition(0);
		
		try {
			Date d = df.parse(date, pos);
			if (d==null || pos.getErrorIndex()!=-1 || pos.getIndex()!=date.length())
				return false;			
		}
		catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Parse a string to give a Date object
	 * @param date
	 * @param dateFormat format of the date e.g. MM/dd/yyyy - see SimpleDateFormat
	 * @return null if not a valid date
	 */
	public static Date getDate(String date, String dateFormat) {
		try {
			if (isValidDate(date, dateFormat))
				return new SimpleDateFormat(dateFormat).parse(date);
		}
		catch (Exception e) { }
		
		return null;
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
		if (dayOfYear <= 0) {
			c.set(Calendar.YEAR, sessionYear - 1);
			dayOfYear += c.get(Calendar.DAY_OF_YEAR);
		} else if (dayOfYear > c.get(Calendar.DAY_OF_YEAR)) {
			dayOfYear -= c.get(Calendar.DAY_OF_YEAR);
			c.set(Calendar.YEAR, sessionYear + 1);
		}
		c.set(Calendar.DAY_OF_YEAR, dayOfYear);
		return c.getTime();
	}
}
