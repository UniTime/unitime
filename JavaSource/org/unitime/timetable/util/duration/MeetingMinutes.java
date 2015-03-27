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

import org.unitime.timetable.model.DatePattern;

public class MeetingMinutes extends MeetingCountingDuration {
	protected double iLowerBound = 0.95;
	protected double iUpperBound = 1.10;
	
	public MeetingMinutes(String parameter) {
		if (parameter != null) {
			Matcher matcher = Pattern.compile(getParamterFormat()).matcher(parameter);
	        if (matcher.find()) {
	        	iLowerBound = Double.parseDouble(matcher.group(1));
	        	iUpperBound = Double.parseDouble(matcher.group(2));
	        }
		}
	}

	@Override
	public boolean check(int minutes, int semesterMinutes) {
		return iLowerBound * minutes <= semesterMinutes && semesterMinutes <= iUpperBound * minutes;
	}
	
	@Override
	public Integer getMaxMeetings(int minutes, int minutesPerMeeting) {
		return (int) Math.ceil(minutes / minutesPerMeeting); 
	}
	
	@Override
	public String getParamterFormat() {
		return "([0-9]*\\.?[0-9]+),([0-9]*\\.?[0-9]+)";
	}
	
	@Override
	public int getExactTimeMinutesPerMeeting(int minutes, DatePattern datePattern, int dayCode) {
		return minutes / nbrMeetings(datePattern, dayCode);
	}
	
	@Override
	public Integer getArrangedHours(int minutes, DatePattern datePattern) {
		if (minutes <= 0) return null;
		if (datePattern.getType() != null && datePattern.getType() == DatePattern.sTypePatternSet) {
			for (DatePattern child: datePattern.findChildren())
				return new Integer(Math.round(minutes / (50f * child.getEffectiveNumberOfWeeks())));
		}
		return new Integer(Math.round(minutes / (50f * datePattern.getEffectiveNumberOfWeeks())));
	}
}
