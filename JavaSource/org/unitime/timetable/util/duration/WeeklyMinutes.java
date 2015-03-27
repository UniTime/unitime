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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.TimePattern;

public class WeeklyMinutes extends MinutesPerWeek {
	private Float iSemesterWeeks = null;
	
	public WeeklyMinutes(String parameter) {
		super(parameter);
		if (parameter != null && !parameter.isEmpty()) {
			Matcher matcher = Pattern.compile(getParamterFormat()).matcher(parameter);
	        if (matcher.find()) {
	        	iSemesterWeeks = Float.valueOf(matcher.group(1));
	        }
		}
	}
	
	protected float getSemesterWeeks(DatePattern datePattern) {
		if (iSemesterWeeks != null)
			return iSemesterWeeks;
		if (datePattern != null) {
			DatePattern defaultDatePattern = datePattern.getSession().getDefaultDatePattern();
			if (defaultDatePattern != null)
				return defaultDatePattern.getEffectiveNumberOfWeeks();
			else
				return datePattern.getEffectiveNumberOfWeeks();
		}
		return 15f;
	}
	
	/**
	 * A combination is valid when the number of semester minutes matches the number of meetings times number of
	 * minutes per week of the time pattern, multiplied by the number of weeks of the date pattern.<br>
	 * <code>weekly minutes == number of meetings x number of minutes per meeting x number of weeks / semester weeks</code><br>
	 * Semester weeks are provided with the given parameter or (if not set) taken from the default date pattern.
	 */
	@Override
	public boolean isValidCombination(int weeklyMinutes, DatePattern datePattern, TimePattern timePattern) {
		if (timePattern.getType() != null && timePattern.getType() == TimePattern.sTypeExactTime)
			return true;
		if (datePattern.getType() != null && datePattern.getType() == DatePattern.sTypePatternSet) {
			for (DatePattern child: datePattern.findChildren())
				if (isValidCombination(weeklyMinutes, child, timePattern)) return true;
			return false;
		} else {
			return weeklyMinutes == datePattern.getEffectiveNumberOfWeeks() * timePattern.getNrMeetings() * timePattern.getMinPerMtg() / getSemesterWeeks(datePattern);
		}
	}

	@Override
	public String getParamterFormat() {
		return "([0-9]*\\.?[0-9]+)?";
	}
	
	@Override
	public int getExactTimeMinutesPerMeeting(int weeklyMinutes, DatePattern datePattern, int dayCode) {
		if (datePattern.getType() != null && datePattern.getType() == DatePattern.sTypePatternSet) {
			for (DatePattern child: datePattern.findChildren())
				return Math.round((getSemesterWeeks(datePattern) * weeklyMinutes) / (DayCode.nrDays(dayCode) * child.getEffectiveNumberOfWeeks()));
		}
		return Math.round((getSemesterWeeks(datePattern) * weeklyMinutes) / (DayCode.nrDays(dayCode) * datePattern.getEffectiveNumberOfWeeks()));
	}
	
	@Override
	public Integer getArrangedHours(int weeklyMinutes, DatePattern datePattern) {
		if (weeklyMinutes <= 0) return null;
		if (datePattern.getType() != null && datePattern.getType() == DatePattern.sTypePatternSet) {
			for (DatePattern child: datePattern.findChildren())
				return new Integer(Math.round((getSemesterWeeks(datePattern) * weeklyMinutes) / (child.getEffectiveNumberOfWeeks() * 50f)));
		}
		return new Integer(Math.round((getSemesterWeeks(datePattern) * weeklyMinutes) / (datePattern.getEffectiveNumberOfWeeks() * 50f)));
	}
}
