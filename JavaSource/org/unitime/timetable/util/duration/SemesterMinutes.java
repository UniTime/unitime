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

import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.TimePattern;

public class SemesterMinutes extends MinutesPerWeek {
	
	public SemesterMinutes(String parameter) {
		super(parameter);
	}

	/**
	 * A combination is valid when the number of semester minutes matches the number of meetings times number of
	 * minutes per week of the time pattern, multiplied by the number of weeks of the date pattern.<br>
	 * <code>semester minutes == number of meetings x number of minutes per meeting x number of weeks</code> 
	 */
	@Override
	public boolean isValidCombination(int semesterMinutes, DatePattern datePattern, TimePattern timePattern) {
		if (datePattern == null) return false;
		if (timePattern.getType() != null && timePattern.getType() == TimePattern.sTypeExactTime)
			return true;
		if (datePattern.getType() != null && datePattern.getType() == DatePattern.sTypePatternSet) {
			for (DatePattern child: datePattern.findChildren())
				if (isValidCombination(semesterMinutes, child, timePattern)) return true;
			return false;
		} else {
			return semesterMinutes == datePattern.getEffectiveNumberOfWeeks() * timePattern.getNrMeetings() * timePattern.getMinPerMtg();
		}
	}
	
	@Override
	public int getExactTimeMinutesPerMeeting(int semesterMinutes, DatePattern datePattern, int dayCode) {
		if (datePattern == null) return 0;
		if (datePattern.getType() != null && datePattern.getType() == DatePattern.sTypePatternSet) {
			for (DatePattern child: datePattern.findChildren())
				return Math.round(semesterMinutes / (DayCode.nrDays(dayCode) * child.getEffectiveNumberOfWeeks()));
		}
		return Math.round(semesterMinutes / (DayCode.nrDays(dayCode) * datePattern.getEffectiveNumberOfWeeks()));
	}
	
	@Override
	public Integer getArrangedHours(int semesterMinutes, DatePattern datePattern) {
		if (semesterMinutes <= 0 || datePattern == null) return null;
		if (datePattern.getType() != null && datePattern.getType() == DatePattern.sTypePatternSet) {
			for (DatePattern child: datePattern.findChildren())
				return new Integer(Math.round(semesterMinutes / (50f * child.getEffectiveNumberOfWeeks())));
		}
		return new Integer(Math.round(semesterMinutes / (50f * datePattern.getEffectiveNumberOfWeeks())));
	}
}
