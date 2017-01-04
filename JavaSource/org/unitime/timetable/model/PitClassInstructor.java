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

import org.unitime.timetable.model.base.BasePitClassInstructor;

public class PitClassInstructor extends BasePitClassInstructor {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3620357334196646777L;
	private float allWeeklyContactHours = -1;
	private float allWeeklyStudentContactHours = -1;
	private float organizedWeeklyContactHours = -1;
	private float organizedWeeklyStudentContactHours = -1;

	public PitClassInstructor() {
		super();
	}
	
	public float getAllWeeklyContactHours(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (this.allWeeklyContactHours < 0){
			this.allWeeklyContactHours = calculateAllWCH(standardMinutesInReportingHour, standardWeeksInReportingTerm);
		}
		return(this.allWeeklyContactHours);
	}
	
	public float getOrganizedWeeklyContactHours(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (this.organizedWeeklyContactHours < 0){
			this.organizedWeeklyContactHours = calculateOrganizedWCH(standardMinutesInReportingHour, standardWeeksInReportingTerm);
		}
		return(this.organizedWeeklyContactHours);
	}

	private float calculateAllWCH(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm) {
		if (this.getNormalizedPercentShare().intValue() > 0){
			return(this.getPitClassInstructing().getAllWeeklyContactHours(standardMinutesInReportingHour, standardWeeksInReportingTerm) * this.getNormalizedPercentShare().intValue() / 100);
		} else {
			return 0;
		}
	}

	private float calculateOrganizedWCH(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm) {
		if (this.getNormalizedPercentShare().intValue() > 0){
			return(this.getPitClassInstructing().getOrganizedWeeklyContactHours(standardMinutesInReportingHour, standardWeeksInReportingTerm) * this.getNormalizedPercentShare().intValue() / 100);
		} else {
			return 0;
		}
	}

	private float calculateAllWSCH(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm) {
		return(this.getAllWeeklyContactHours(standardMinutesInReportingHour, standardWeeksInReportingTerm) * this.getPitClassInstructing().getEnrollment());
	}

	private float calculateOrganizedWSCH(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm) {
		return(this.getOrganizedWeeklyContactHours(standardMinutesInReportingHour, standardWeeksInReportingTerm) * this.getPitClassInstructing().getEnrollment());
	}

	public float getAllWeeklyStudentContactHours(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (this.allWeeklyStudentContactHours < 0){
			this.allWeeklyStudentContactHours = calculateAllWSCH(standardMinutesInReportingHour, standardWeeksInReportingTerm);
		}
		return(this.allWeeklyStudentContactHours);
	}

	public float getOrganizedWeeklyStudentContactHours(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (this.organizedWeeklyStudentContactHours < 0){
			this.organizedWeeklyStudentContactHours = calculateOrganizedWSCH(standardMinutesInReportingHour, standardWeeksInReportingTerm);
		}
		return(this.organizedWeeklyStudentContactHours);
	}
	
}
