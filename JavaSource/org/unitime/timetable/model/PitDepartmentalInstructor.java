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

import org.unitime.timetable.model.base.BasePitDepartmentalInstructor;

public class PitDepartmentalInstructor extends BasePitDepartmentalInstructor {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7035406494022531331L;
	private float organizedWeeklyContactHours = -1;
	private float organizedWeeklyStudentContactHours = -1;
	private float allWeeklyContactHours = -1;
	private float allWeeklyStudentContactHours = -1;

	public PitDepartmentalInstructor() {
		super();
	}

	public float getOrganizedWeeklyContactHours(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (this.organizedWeeklyContactHours < 0){
			for(PitClassInstructor pci : this.getPitClassesInstructing()){
				this.organizedWeeklyContactHours += pci.getOrganizedWeeklyContactHours(standardMinutesInReportingHour, standardWeeksInReportingTerm);
			}
		}
		return(this.organizedWeeklyContactHours);
	}
	
	public float getOrganizedWeeklyStudentContactHours(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (this.organizedWeeklyStudentContactHours < 0){
			for(PitClassInstructor pci : this.getPitClassesInstructing()){
				this.organizedWeeklyStudentContactHours += pci.getAllWeeklyStudentContactHours(standardMinutesInReportingHour, standardWeeksInReportingTerm);
			}
		}
		return(this.organizedWeeklyStudentContactHours);
	}

	public float getAllWeeklyContactHours(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (this.allWeeklyContactHours < 0){
			for(PitClassInstructor pci : this.getPitClassesInstructing()){
				this.allWeeklyContactHours += pci.getAllWeeklyContactHours(standardMinutesInReportingHour, standardWeeksInReportingTerm);
			}
		}
		return(this.allWeeklyContactHours);
	}
	
	public float getAllWeeklyStudentContactHours(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (this.allWeeklyStudentContactHours < 0){
			for(PitClassInstructor pci : this.getPitClassesInstructing()){
				this.allWeeklyStudentContactHours += pci.getAllWeeklyStudentContactHours(standardMinutesInReportingHour, standardWeeksInReportingTerm);
			}
		}
		return(this.allWeeklyStudentContactHours);
	}


}
