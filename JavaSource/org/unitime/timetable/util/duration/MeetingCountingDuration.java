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

import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.EventDateMapping;
import org.unitime.timetable.model.TimePattern;
import org.unitime.timetable.model.TimePatternDays;
import org.unitime.timetable.util.Constants;

public abstract class MeetingCountingDuration implements DurationModel {

	public abstract boolean check(int minutes, int semesterMinutes);
	public abstract Integer getMaxMeetings(int minutes, int minutesPerMeeting);

	@Override
	public boolean isValidCombination(int minutes, DatePattern datePattern, TimePattern timePattern) {
		if (datePattern == null) return false;
		if (timePattern.getType() != null && timePattern.getType() == TimePattern.sTypeExactTime)
			return true;
		if (datePattern.getType() != null && datePattern.getType() == DatePattern.sTypePatternSet) {
			for (DatePattern child: datePattern.findChildren())
				if (isValidCombination(minutes, child, timePattern)) return true;
			return false;
		} else {
			for (TimePatternDays days: timePattern.getDays())
				if (check(minutes, nbrMeetings(datePattern, days.getDayCode()) * timePattern.getMinPerMtg())) return true;
			return false;
		}
	}
	
	protected int nbrMeetings(DatePattern datePattern, int dayCode) {
		if (datePattern == null) return 0;
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(datePattern.getStartDate()); cal.setLenient(true);
        String pattern = datePattern.getPattern();
        int ret = 0;
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
        		if (offered) ret++;
        	}
        	cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return ret;
	}
	
	@Override
	public boolean isValidSelection(int minutes, DatePattern datePattern, TimePattern timePattern, int dayCode) {
		if (!isValidCombination(minutes, datePattern, timePattern)) return false;
		if (datePattern.getType() != null && datePattern.getType() == DatePattern.sTypePatternSet) {
			for (DatePattern child: datePattern.findChildren())
				if (check(minutes, nbrMeetings(child, dayCode) * timePattern.getMinPerMtg()))
					return true;
			return false;
		}
		return check(minutes, nbrMeetings(datePattern, dayCode) * timePattern.getMinPerMtg());
	}
	
	@Override
	public Set<Integer> getDayCodes(int minutes, DatePattern datePattern, TimePattern timePattern) {
		Set<Integer> ret = new HashSet<Integer>();
		for (TimePatternDays days: timePattern.getDays())
			if (isValidSelection(minutes, datePattern, timePattern, days.getDayCode()))
				ret.add(days.getDayCode());
		return ret;
	}
	
	@Override
	public List<Date> getDates(int minutes, DatePattern datePattern, int dayCode, int minutesPerMeeting) {
		List<Date> ret = new ArrayList<Date>();
		if (datePattern == null) return ret;
		Calendar cal = Calendar.getInstance(Locale.US);
		cal.setTime(datePattern.getStartDate()); cal.setLenient(true);
        EventDateMapping.Class2EventDateMap class2eventDates = EventDateMapping.getMapping(datePattern.getSession().getUniqueId());
        String pattern = datePattern.getPattern();
        Integer max = getMaxMeetings(minutes, minutesPerMeeting);
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
        	if (max != null && ret.size() >= max) break;
        }
		return ret;
	}
}
