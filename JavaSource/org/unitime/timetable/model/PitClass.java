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

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.base.BasePitClass;

public class PitClass extends BasePitClass {

	/**
	 * 
	 */
	private static final long serialVersionUID = 642241232877633526L;
	private float weeklyContactHours = -1;
	private float weeklyStudentContactHours = -1;

	public PitClass() {
		super();
	}
		
	private float calculateWCHfromPeriods(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		float minutesInReportingHour = (standardMinutesInReportingHour == null ? ApplicationProperty.StandardMinutesInReportingHour.floatValue() : standardMinutesInReportingHour.floatValue());
		float weeksInReportingTerm = (standardWeeksInReportingTerm == null ? ApplicationProperty.StandardWeeksInReportingTerm.floatValue() : standardWeeksInReportingTerm);
		float contactPeriods = 0;
		for(PitClassEvent pce : this.getPitClassEvents()){
			for(PitClassMeeting pcm : pce.getPitClassMeetings()){
				if ((this.getPitSchedulingSubpart()
						.getPitInstrOfferingConfig()
						.getPitInstructionalOffering()
						.getPointInTimeData()
						.getSession()
						.getSessionBeginDateTime().compareTo(pcm.getMeetingDate()) <= 0)
						&& this.getPitSchedulingSubpart()
						.getPitInstrOfferingConfig()
						.getPitInstructionalOffering()
						.getPointInTimeData()
						.getSession()
						.getSessionEndDateTime().compareTo(pcm.getMeetingDate()) >= 0) {
					contactPeriods += pcm.getPitClassMeetingUtilPeriods().size();
				}
			}
		}		
		return(contactPeriods * 5 / roomCount().floatValue() / minutesInReportingHour	/ weeksInReportingTerm);
	}

	private float calculateWCH(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		float minutesInReportingHour = (standardMinutesInReportingHour == null ? ApplicationProperty.StandardMinutesInReportingHour.floatValue() : standardMinutesInReportingHour.floatValue());
		float weeksInReportingTerm = (standardWeeksInReportingTerm == null ? ApplicationProperty.StandardWeeksInReportingTerm.floatValue() : standardWeeksInReportingTerm);
		float contactMinutes = 0;
		for(PitClassEvent pce : this.getPitClassEvents()){
			for(PitClassMeeting pcm : pce.getPitClassMeetings()){
				if ((this.getPitSchedulingSubpart()
						.getPitInstrOfferingConfig()
						.getPitInstructionalOffering()
						.getPointInTimeData()
						.getSession()
						.getSessionBeginDateTime().compareTo(pcm.getMeetingDate()) <= 0)
						&& this.getPitSchedulingSubpart()
						.getPitInstrOfferingConfig()
						.getPitInstructionalOffering()
						.getPointInTimeData()
						.getSession()
						.getSessionEndDateTime().compareTo(pcm.getMeetingDate()) >= 0) {
					contactMinutes += pcm.getCalculatedMinPerMtg();
				}
			}
		}		
		return(contactMinutes / roomCount().floatValue() / minutesInReportingHour / weeksInReportingTerm);
	}
	
	private Integer roomCount() {
		if (getNbrRooms() == null || getNbrRooms().intValue() <= 0) {
			return(new Integer(1));
		}
		return(getNbrRooms());
	}
	
	private float calculateWSCH(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		return(calculateWCH(standardMinutesInReportingHour, standardWeeksInReportingTerm) * this.getStudentEnrollments().size());
	}

	public float getOrganizedWeeklyContactHoursFromUtilPeriods(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (getPitSchedulingSubpart().getItype().isOrganized().booleanValue()){
			return(getAllWeeklyContactHoursFromUtilPeriods(standardMinutesInReportingHour, standardWeeksInReportingTerm));
		} else {
			return(0);
		}
	}

	public float getAllWeeklyContactHoursFromUtilPeriods(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		return(calculateWCHfromPeriods(standardMinutesInReportingHour, standardWeeksInReportingTerm));
	}

	public float getOrganizedWeeklyContactHours(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (getPitSchedulingSubpart().getItype().isOrganized().booleanValue()){
			return(getAllWeeklyContactHours(standardMinutesInReportingHour, standardWeeksInReportingTerm));
		} else {
			return(0);
		}
	}
	
	public float getNotOrganizedWeeklyContactHours(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (!getPitSchedulingSubpart().getItype().isOrganized().booleanValue()){
			return(getAllWeeklyContactHours(standardMinutesInReportingHour, standardWeeksInReportingTerm));
		} else {
			return(0);
		}
	}

	public float getOrganizedWeeklyStudentContactHours(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (getPitSchedulingSubpart().getItype().isOrganized().booleanValue()){
			return(getAllWeeklyStudentContactHours(standardMinutesInReportingHour, standardWeeksInReportingTerm));
		} else {
			return(0);
		}
	}

	public float getNotOrganizedWeeklyStudentContactHours(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (!getPitSchedulingSubpart().getItype().isOrganized().booleanValue()){
			return(getAllWeeklyStudentContactHours(standardMinutesInReportingHour, standardWeeksInReportingTerm));
		} else {
			return(0);
		}
	}

	public float getAllWeeklyContactHours(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (this.weeklyContactHours < 0){
			this.weeklyContactHours = calculateWCH(standardMinutesInReportingHour, standardWeeksInReportingTerm);
		}
		return(this.weeklyContactHours);
	}
	
	public float getAllWeeklyStudentContactHours(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (this.weeklyStudentContactHours < 0){
			this.weeklyStudentContactHours = calculateWSCH(standardMinutesInReportingHour, standardWeeksInReportingTerm);
		}
		return(this.weeklyStudentContactHours);
	}

}
