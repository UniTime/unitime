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

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.base.BasePitClass;
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.reports.pointintimedata.BasePointInTimeDataReports;
import org.unitime.timetable.util.Constants;

public class PitClass extends BasePitClass {

	/**
	 * 
	 */
	private static final long serialVersionUID = 642241232877633526L;
	private float weeklyClassHours = -1;
	private float weeklyStudentClassHours = -1;

	public PitClass() {
		super();
	}
		
	private HashMap<Long, HashSet<java.util.Date>> locationPeriodUseMap = null;
	private Set<Long> locationPermanentIdList = null;
	private HashSet<java.util.Date> uniquePeriods = null;
	
	private float calculateWCHforLocationFromPeriods(Long location, Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		float minutesInReportingHour = (standardMinutesInReportingHour == null ? ApplicationProperty.StandardMinutesInReportingHour.floatValue() : standardMinutesInReportingHour.floatValue());
		float weeksInReportingTerm = (standardWeeksInReportingTerm == null ? ApplicationProperty.StandardWeeksInReportingTerm.floatValue() : standardWeeksInReportingTerm);
		HashSet<java.util.Date> periods = getLocationPeriodUseMap().get(location);
		if (periods != null) {
			return(periods.size() * (Constants.SLOT_LENGTH_MIN * 1.0f) /  minutesInReportingHour	/ weeksInReportingTerm);			
		} else {
			return(0f);
		}
	}

	public HashMap<Long, HashSet<java.util.Date>> getLocationPeriodUseMap(){
		if (locationPeriodUseMap == null){
			initializeRoomData();
		}
		return(locationPeriodUseMap);
	}
	
	public HashSet<java.util.Date> getUniquePeriods(){
		if (uniquePeriods == null){
			initializeRoomData();
		}
		return(uniquePeriods);
	}

	
	private void initializeRoomData() {

			locationPeriodUseMap = new HashMap<Long, HashSet<java.util.Date>>();
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
							.getClassesEndDateTime().compareTo(pcm.getMeetingDate()) >= 0) {
						for(PitClassMeetingUtilPeriod pcmup : pcm.getPitClassMeetingUtilPeriods()){
							HashSet<java.util.Date> periods = locationPeriodUseMap.get(pcm.getLocationPermanentId());
							if (periods == null) {
								periods = new HashSet<java.util.Date>();
								locationPeriodUseMap.put(pcm.getLocationPermanentId(), periods);
							}
							periods.add(pcmup.periodDateTime());
						}
					}
				}
			}	
			locationPermanentIdList = locationPeriodUseMap.keySet();

			uniquePeriods = new HashSet<java.util.Date>();
			for(HashSet<java.util.Date> periods : locationPeriodUseMap.values()) {
				for(java.util.Date period : periods){
					uniquePeriods.addAll(periods);
				}
			}

	}
	
	public Set<Long> getLocationPermanentIdList() {
		if(locationPermanentIdList == null) {
			initializeRoomData();
		}
		return(locationPermanentIdList);
	}
	
	public HashSet<java.util.Date> getPeriodsForLocation (Long location) {
		return(getLocationPeriodUseMap().get(location));
	}

	public boolean isOrganized() {
		return(getPitSchedulingSubpart().getItype().isOrganized().booleanValue());
	}
	
	public float countRoomsForPeriod(java.util.Date period) {
		float cnt = 0f;
		for(HashSet<java.util.Date> periods : getLocationPeriodUseMap().values()) {
			if (periods.contains(period)){
				cnt++;
			}
		}
		return(cnt);
	}
	
	private float calculateWSCHfromPeriodsForLocation(Long location, Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		float minutesInReportingHour = (standardMinutesInReportingHour == null ? ApplicationProperty.StandardMinutesInReportingHour.floatValue() : standardMinutesInReportingHour.floatValue());
		float weeksInReportingTerm = (standardWeeksInReportingTerm == null ? ApplicationProperty.StandardWeeksInReportingTerm.floatValue() : standardWeeksInReportingTerm);

		HashSet<java.util.Date> periods = getLocationPeriodUseMap().get(location);
		

		float wrm = 0.0f;
		for(java.util.Date period : periods){
			wrm += ((Constants.SLOT_LENGTH_MIN * 1.0f) / countRoomsForPeriod(period));
		}
		
		return(wrm * getStudentEnrollments().size()  / minutesInReportingHour	/ weeksInReportingTerm);
	}
	
	


	private float calculateWCH(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		float minutesInReportingHour = (standardMinutesInReportingHour == null ? ApplicationProperty.StandardMinutesInReportingHour.floatValue() : standardMinutesInReportingHour.floatValue());
		float weeksInReportingTerm = (standardWeeksInReportingTerm == null ? ApplicationProperty.StandardWeeksInReportingTerm.floatValue() : standardWeeksInReportingTerm);
		
		return((Constants.SLOT_LENGTH_MIN * 1.0f) * getUniquePeriods().size()  / minutesInReportingHour	/ weeksInReportingTerm);
	}
		
	private float calculateWSCH(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		return(getAllWeeklyClassHours(standardMinutesInReportingHour, standardWeeksInReportingTerm) * this.getStudentEnrollments().size());
	}

	public float getOrganizedWeeklyRoomHoursFromUtilPeriodsForLocation(Long location, Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (getPitSchedulingSubpart().getItype().isOrganized().booleanValue()){
			return(calculateWCHforLocationFromPeriods(location, standardMinutesInReportingHour, standardWeeksInReportingTerm));
		} else {
			return(0);
		}
	}

	public float getAllWeeklyRoomHoursFromUtilPeriodsForLocation(Long location, Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		return(calculateWCHforLocationFromPeriods(location, standardMinutesInReportingHour, standardWeeksInReportingTerm));
	}

	public float getNotOrganizedWeeklyRoomHoursFromUtilPeriodsForLocation(Long location, Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (!getPitSchedulingSubpart().getItype().isOrganized().booleanValue()){
			return(calculateWCHforLocationFromPeriods(location, standardMinutesInReportingHour, standardWeeksInReportingTerm));
		} else {
			return(0);
		}
	}

	public float getOrganizedWeeklyClassHours(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (getPitSchedulingSubpart().getItype().isOrganized().booleanValue()){
			return(getAllWeeklyClassHours(standardMinutesInReportingHour, standardWeeksInReportingTerm));
		} else {
			return(0);
		}
	}
	
	public float getNotOrganizedWeeklyClassHours(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (!getPitSchedulingSubpart().getItype().isOrganized().booleanValue()){
			return(getAllWeeklyClassHours(standardMinutesInReportingHour, standardWeeksInReportingTerm));
		} else {
			return(0);
		}
	}

	public float getOrganizedWeeklyStudentClassHours(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (getPitSchedulingSubpart().getItype().isOrganized().booleanValue()){
			return(getAllWeeklyStudentClassHours(standardMinutesInReportingHour, standardWeeksInReportingTerm));
		} else {
			return(0);
		}
	}

	public float getNotOrganizedWeeklyStudentClassHours(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (!getPitSchedulingSubpart().getItype().isOrganized().booleanValue()){
			return(getAllWeeklyStudentClassHours(standardMinutesInReportingHour, standardWeeksInReportingTerm));
		} else {
			return(0);
		}
	}

	public float getAllWeeklyClassHours(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (this.weeklyClassHours < 0){
			this.weeklyClassHours = calculateWCH(standardMinutesInReportingHour, standardWeeksInReportingTerm);
		}
		return(this.weeklyClassHours);
	}
	
	public float getAllWeeklyStudentClassHours(Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (this.weeklyStudentClassHours < 0){
			this.weeklyStudentClassHours = calculateWSCH(standardMinutesInReportingHour, standardWeeksInReportingTerm);
		}
		return(this.weeklyStudentClassHours);
	}

	public float getAllWeeklyStudentClassHoursForLocation(Long location, Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		return(calculateWSCHfromPeriodsForLocation(location, standardMinutesInReportingHour, standardWeeksInReportingTerm));
	}
	
	public float getOrganizedWeeklyStudentClassHoursForLocation(Long location, Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (getPitSchedulingSubpart().getItype().isOrganized().booleanValue()){
			return(getAllWeeklyStudentClassHoursForLocation(location, standardMinutesInReportingHour, standardWeeksInReportingTerm));
		} else {
			return(0);
		}
	}

	public float getNotOrganizedWeeklyStudentClassHoursForLocation(Long location, Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm){
		if (!getPitSchedulingSubpart().getItype().isOrganized().booleanValue()){
			return(getAllWeeklyStudentClassHoursForLocation(location, standardMinutesInReportingHour, standardWeeksInReportingTerm));
		} else {
			return(0);
		}
	}
	

	public HashMap<java.util.Date, Float> findPeriodEnrollmentsForCriteria(HashSet<Long> validRoomPermanentIds, ArrayList<Long> positionTypes){
		HashMap<java.util.Date, Float> periodEnrollmentMap = new HashMap<java.util.Date, Float>();
		
		float ratio = 0.0f;
		if (positionTypes != null && !positionTypes.isEmpty()) {
			if (this.getPitClassInstructors() == null || this.getPitClassInstructors().isEmpty()){
				if (positionTypes.contains(new Long(-1))){
					ratio = 1.0f;					
				}
			} else {
				for(PitClassInstructor pci : this.getPitClassInstructors()) {
					if (pci.getPitDepartmentalInstructor().getPositionType() != null 
							&& positionTypes.contains(pci.getPitDepartmentalInstructor().getPositionType().getUniqueId())) {
						ratio += (pci.getNormalizedPercentShare().intValue()/100.0f);
					} else if (pci.getPitDepartmentalInstructor().getPositionType() == null
							&& positionTypes.contains(new Long(-1))) {
						ratio += (pci.getNormalizedPercentShare().intValue()/100.0f);					
					}
				}
			}
		} else {
			ratio = 1.0f;
		}
		
		HashSet<Long> applicableLocationPermananetIds = new HashSet<Long>();
		for (Long permId : getLocationPermanentIdList()){
			if(validRoomPermanentIds.contains(permId)) {
				applicableLocationPermananetIds.add(permId);
			}
		}
		if (applicableLocationPermananetIds.size() == getLocationPermanentIdList().size()){
			for(java.util.Date period : uniquePeriods){
				periodEnrollmentMap.put(period, (this.getEnrollment().intValue() * ratio));
			}
		} else {
			for(PitClassEvent pce : this.getPitClassEvents()){
				for (PitClassMeeting pcm : pce.getPitClassMeetings()){
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
						.getClassesEndDateTime().compareTo(pcm.getMeetingDate()) >= 0) {

						if (applicableLocationPermananetIds.contains(pcm.getLocationPermanentId())) {
							for(PitClassMeetingUtilPeriod pcmup : pcm.getPitClassMeetingUtilPeriods()){				
								periodEnrollmentMap.put(pcmup.periodDateTime(), (this.getEnrollment().intValue() * (1.0f / countRoomsForPeriod(pcmup.periodDateTime())) * ratio));
							}
						}
					}
				}
			}			
		}
			
		return(periodEnrollmentMap);
	}
}
