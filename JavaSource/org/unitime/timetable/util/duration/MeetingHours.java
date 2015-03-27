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

public class MeetingHours extends MeetingCountingDuration {
	protected int iMinutesPerHour = 50;
	protected double iLowerBound = 0.95;
	protected double iUpperBound = 1.10;
	
	public MeetingHours(String parameter) {
		if (parameter != null && !parameter.isEmpty()) {
			Matcher matcher = Pattern.compile(getParamterFormat()).matcher(parameter);
	        if (matcher.find()) {
	        	iMinutesPerHour = Integer.parseInt(matcher.group(1));
	        	iLowerBound = Double.parseDouble(matcher.group(2));
	        	iUpperBound = Double.parseDouble(matcher.group(3));
	        }
		}
	}

	@Override
	public boolean check(int hours, int semesterMinutes) {
		return iLowerBound * hours * iMinutesPerHour <= semesterMinutes && semesterMinutes <= iUpperBound * hours * iMinutesPerHour;
	}
	
	@Override
	public Integer getMaxMeetings(int hours, int minutesPerMeeting) {
		return (int) Math.ceil(((double) hours * iMinutesPerHour) / minutesPerMeeting); 
	}

	@Override
	public String getParamterFormat() {
		return "([0-9]+),([0-9]*\\.?[0-9]+),([0-9]*\\.?[0-9]+)";
	}
	
	@Override
	public int getExactTimeMinutesPerMeeting(int hours, DatePattern datePattern, int dayCode) {
		if (datePattern.getType() != null && datePattern.getType() == DatePattern.sTypePatternSet) {
			for (DatePattern child: datePattern.findChildren())
				return (iMinutesPerHour * hours) / nbrMeetings(child, dayCode);
		}
		return (iMinutesPerHour * hours) / nbrMeetings(datePattern, dayCode);
	}
	
	@Override
	public Integer getArrangedHours(int hours, DatePattern datePattern) {
		if (hours <= 0) return null;
		if (datePattern.getType() != null && datePattern.getType() == DatePattern.sTypePatternSet) {
			for (DatePattern child: datePattern.findChildren())
				return new Integer(Math.round(((float)hours) / child.getEffectiveNumberOfWeeks()));
		}
		return new Integer(Math.round(((float)hours) / datePattern.getEffectiveNumberOfWeeks()));
	}
}
