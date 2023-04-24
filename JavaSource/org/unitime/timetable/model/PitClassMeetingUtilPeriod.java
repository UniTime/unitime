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
package org.unitime.timetable.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.unitime.timetable.model.base.BasePitClassMeetingUtilPeriod;
import org.unitime.timetable.util.Constants;

@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, include = "non-lazy")
@Table(name = "pit_class_mtg_util_period")
public class PitClassMeetingUtilPeriod extends BasePitClassMeetingUtilPeriod {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6064155179953261071L;
	public PitClassMeetingUtilPeriod() {
		super();
	}

	public Date periodDateTime() {
	        Calendar c = Calendar.getInstance(Locale.US);
	        c.setTime(this.getPitClassMeeting().getMeetingDate());
	        int min = (this.getTimeSlot().intValue()*Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN);
	        c.set(Calendar.HOUR, min/60);
	        c.set(Calendar.MINUTE, min%60);
	        return c.getTime();
	}
}
