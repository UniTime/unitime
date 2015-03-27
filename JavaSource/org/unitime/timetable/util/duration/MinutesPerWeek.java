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
package org.unitime.timetable.util.duration;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.EventDateMapping;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternDays;
import org.unitime.timetable.util.Constants;

public class MinutesPerWeek implements DurationModel {
	
	public MinutesPerWeek(String paramter) {}

	/**
	 * A combination is valid when the number of minutes per week matches the number of meetings times number of
	 * minutes per week of the time pattern.
	 */
	@Override
	public boolean isValidCombination(int minsPerWeek, DatePattern datePattern, TimePattern timePattern) {
		return minsPerWeek == timePattern.getNrMeetings() * timePattern.getMinPerMtg();
	}
	

	/**
	 * All day codes of the selected time pattern are valid.
	 * Only exclude cases when there is no date matching the day code and the date pattern
	 * (e.g., when the selected day is Wednesday, but there is no Wednesday marked yellow in the date pattern).
	 */
	@Override
	public boolean isValidSelection(int minsPerWeek, DatePattern datePattern, TimePattern timePattern, int dayCode) {
		if (!isValidCombination(minsPerWeek, datePattern, timePattern)) return false;
		if (datePattern.getType() != null && datePattern.getType() == DatePattern.sTypePatternSet) {
			for (DatePattern child: datePattern.findChildren())
				if (hasDates(child, dayCode)) return true;
			return false;
		}
		return hasDates(datePattern, dayCode);
	}

	/**
	 * All day codes of the selected time pattern are valid.
	 * Only exclude cases when there is no date matching the day code and the date pattern
	 * (e.g., when the selected day is Wednesday, but there is no Wednesday marked yellow in the date pattern).
	 */
	@Override
	public Set<Integer> getDayCodes(int minsPerWeek, DatePattern datePattern, TimePattern timePattern) {
		Set<Integer> ret = new HashSet<Integer>();
		for (TimePatternDays days: timePattern.getDays())
			if (isValidSelection(minsPerWeek, datePattern, timePattern, days.getDayCode()))
				ret.add(days.getDayCode());
		return ret;
	}
	
	protected boolean hasDates(DatePattern datePattern, int dayCode) {
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(datePattern.getStartDate()); cal.setLenient(true);
        String pattern = datePattern.getPattern();
        for (int idx = 0; idx < pattern.length(); idx++) {
        	if (pattern.charAt(idx) == '1') {
        		boolean offered = false;
        		switch (cal.get(Calendar.DAY_OF_WEEK)) {
        			case Calendar.MONDAY : offered = ((dayCode & Constants.DAY_CODES[Constants.DAY_MON]) != 0); break;
        			case Calendar.TUESDAY : offered = ((dayCode & Constants.DAY_CODES[Constants.DAY_TUE]) != 0); break;
        			case Calendar.WEDNESDAY : offered = ((dayCode & Constants.DAY_CODES[Constants.DAY_WED]) != 0); break;
        			case Calendar.THURSDAY : offered = ((dayCode & Constants.DAY_CODES[Constants.DAY_THU]) != 0); break;
        			case Calendar.FRIDAY : offered = ((dayCode & Constants.DAY_CODES[Constants.DAY_FRI]) != 0); break;
        			case Calendar.SATURDAY : offered = ((dayCode & Constants.DAY_CODES[Constants.DAY_SAT]) != 0); break;
        			case Calendar.SUNDAY : offered = ((dayCode & Constants.DAY_CODES[Constants.DAY_SUN]) != 0); break;
        		}
        		if (offered) return true;
        	}
        	cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return false;
	}

	/**
	 * Return all dates marked yellow in the given date pattern that are matching the selected day code (days of week)
	 */
	@Override
	public List<Date> getDates(int minsPerWeek, DatePattern datePattern, int dayCode, int minutesPerMeeting) {
		List<Date> ret = new ArrayList<Date>();
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(datePattern.getStartDate()); cal.setLenient(true);
        EventDateMapping.Class2EventDateMap class2eventDates = EventDateMapping.getMapping(datePattern.getSession().getUniqueId());
        String pattern = datePattern.getPattern();
        for (int idx = 0; idx < pattern.length(); idx++) {
        	if (pattern.charAt(idx) == '1') {
        		boolean offered = false;
        		switch (cal.get(Calendar.DAY_OF_WEEK)) {
        			case Calendar.MONDAY : offered = ((dayCode & Constants.DAY_CODES[Constants.DAY_MON]) != 0); break;
        			case Calendar.TUESDAY : offered = ((dayCode & Constants.DAY_CODES[Constants.DAY_TUE]) != 0); break;
        			case Calendar.WEDNESDAY : offered = ((dayCode & Constants.DAY_CODES[Constants.DAY_WED]) != 0); break;
        			case Calendar.THURSDAY : offered = ((dayCode & Constants.DAY_CODES[Constants.DAY_THU]) != 0); break;
        			case Calendar.FRIDAY : offered = ((dayCode & Constants.DAY_CODES[Constants.DAY_FRI]) != 0); break;
        			case Calendar.SATURDAY : offered = ((dayCode & Constants.DAY_CODES[Constants.DAY_SAT]) != 0); break;
        			case Calendar.SUNDAY : offered = ((dayCode & Constants.DAY_CODES[Constants.DAY_SUN]) != 0); break;
        		}
        		if (offered) ret.add(class2eventDates.getEventDate(cal.getTime()));
        	}
        	cal.add(Calendar.DAY_OF_YEAR, 1);
        }
		return ret;
	}

	@Override
	public String getParamterFormat() {
		return null;
	}

	@Override
	public int getExactTimeMinutesPerMeeting(int minsPerWeek, DatePattern datePattern, int dayCode) {
		return minsPerWeek / DayCode.nrDays(dayCode);
	}
	
	@Override
	public Integer getArrangedHours(int minsPerWeek, DatePattern datePattern) {
		return minsPerWeek <= 0 ? null : new Integer(Math.round(minsPerWeek / 50f));
	}
}
