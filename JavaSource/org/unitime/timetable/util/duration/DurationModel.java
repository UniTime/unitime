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

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.TimePattern;

public interface DurationModel {
	
	/**
	 * Parameter format
	 * @return regular expression matching the parameter, null if there is no parameter
	 */
	public String getParamterFormat();
	
	/**
	 * Check if the given selection is valid.
	 * @param minutes number of minutes set on the scheduling subpart
	 * @param datePattern selected date pattern (alternative pattern sets are allowed)
	 * @param timePattern selected time pattern
	 * @return true if there is a valid combinations of days meeting the given criteria
	 */
	public boolean isValidCombination(int minutes, DatePattern datePattern, TimePattern timePattern);
	
	/**
	 * Check if the given selection is valid.
	 * @param minutes number of minutes set on the scheduling subpart
	 * @param datePattern selected date pattern
	 * @param timePattern selected time pattern
	 * @param dayCode selected days of week (alternative pattern sets are allowed)
	 * @return true if there is a valid combinations of days meeting the given criteria
	 */
	public boolean isValidSelection(int minutes, DatePattern datePattern, TimePattern timePattern, int dayCode);
	
	/**
	 * Get combinations of days that meet the given selection.
	 * @param minutes number of minutes set on the scheduling subpart
	 * @param datePattern selected date pattern (alternative pattern sets are allowed)
	 * @param timePattern selected time pattern (exact time pattern is NOT allowed)
	 * @return list of day codes (days of week, given by {@link TimePattern#getDays()}) that meet the selected minutes and date pattern
	 */
	public Set<Integer> getDayCodes(int minutes, DatePattern datePattern, TimePattern timePattern);
	
	/**
	 * Get all dates meeting the given selection.
	 * @param minutes number of minutes set on the scheduling subpart
	 * @param datePattern selected date pattern
	 * @param dayCode selected days of week (alternative pattern sets are NOT allowed)
	 * @param minutesPerMeeting minutes per meeting
	 * @return list of dates that meet the selected minutes and date pattern
	 */
	public List<Date> getDates(int minutes, DatePattern datePattern, int dayCode, int minutesPerMeeting);
	
	/**
	 * Compute number of minutes for a selected exact time
	 * @param minutes number of minutes set on the scheduling subpart
	 * @param datePattern selected date pattern (alternative pattern sets are NOT allowed)
	 * @param dayCode selected days of week 
	 * @return number of arranged hours, null if zero
	 */
	public int getExactTimeMinutesPerMeeting(int minutes, DatePattern datePattern, int dayCode);
	
	/**
	 * Return number of arranged hours (if there is no time pattern)
	 * @param minutes number of minutes set on the scheduling subpart
	 * @param datePattern selected date pattern
	 */
	public Integer getArrangedHours(int minutes, DatePattern datePattern);

}
