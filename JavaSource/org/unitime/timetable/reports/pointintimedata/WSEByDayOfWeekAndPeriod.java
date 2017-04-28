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
package org.unitime.timetable.reports.pointintimedata;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.hibernate.Session;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PitClass;
import org.unitime.timetable.model.PointInTimeData;

/**
 * @author says
 *
 */
public class WSEByDayOfWeekAndPeriod extends BasePointInTimeDataReports {
	private ArrayList<Long> iDepartmentIds;
	private ArrayList<Long> iSubjectAreaIds;
	private ArrayList<Long> iRoomTypeIds;
	private ArrayList<Long> iPositionTypeIds;
	private Integer iMinimumLocationCapacity;
	private Integer iMaximumLocationCapacity;
	private HashMap<String, PeriodEnrollment> periodEnrollmentMap = new HashMap<String, PeriodEnrollment>();
	
	public static String dayOfWeekTimeLabelFor(java.util.Date date){
		return(Localization.getDateFormat("EEEE, HH:mm").format(date));
	}

	public WSEByDayOfWeekAndPeriod() {
		super();
		getParameters().add(Parameter.DEPARTMENTS);
		getParameters().add(Parameter.SUBJECTS);
		getParameters().add(Parameter.RoomTypes);
		getParameters().add(Parameter.PositionTypes);
		getParameters().add(Parameter.MINIMUM_LOCATION_CAPACITY);
		getParameters().add(Parameter.MAXIMUM_LOCATION_CAPACITY);
	}

	@Override
	protected void intializeHeader() {
		ArrayList<String> hdr = new ArrayList<String>();
		hdr.add(MSG.columnDayOfWeek());
		hdr.add(MSG.columnPeriod());
		hdr.add(MSG.columnOrganizedWeeklyStudentEnrollmentPerPeriod());
		hdr.add(MSG.columnNotOrganizedWeeklyStudentEnrollmentPerPeriod());
		hdr.add(MSG.columnWeeklyStudentEnrollmentPerPeriod());
		setHeader(hdr);
	}

	@Override
	public String reportName() {
		return(MSG.wseByDayOfWeekAndPeriodReport());
	}

	@Override
	public String reportDescription() {
		return(MSG.wseByDayOfWeekAndPeriodReportNote());
	}

	public void createRoomUtilizationReportFor(PointInTimeData pointInTimeData, Session hibSession) {
		HashSet<Long> validRoomPermanentIds = new HashSet<Long>();
		for(Location l : pointInTimeData.getSession().getRooms()){
			if (getRoomTypeIds().contains(l.getRoomType().getUniqueId())
					&& l.getCapacity().intValue() <= getMaximumLocationCapacity().intValue()
					&& l.getCapacity().intValue() >= getMinimumLocationCapacity().intValue()){
				validRoomPermanentIds.add(l.getPermanentId());
			}
		}
		
		HashSet<PitClass> pitClasses = findAllPitClassesWithContactHoursForDepartmentsAndSubjectAreas(pointInTimeData, hibSession);
		ArrayList<Long> positionIds = null;
		if (BasePointInTimeDataReports.Parameter.PositionTypes.values(null).size() != getPositionTypeIds().size()){
			positionIds = getPositionTypeIds();
		}
		
		for(PitClass pc : pitClasses){
			HashMap<java.util.Date, Float> datePeriodEnrollments = pc.findPeriodEnrollmentsForCriteria(validRoomPermanentIds, positionIds);
			for(java.util.Date date : datePeriodEnrollments.keySet()) {
				String label = dayOfWeekTimeLabelFor(date);
				PeriodEnrollment pe = periodEnrollmentMap.get(label);
				if (pe == null) {
					pe = new PeriodEnrollment(date, getStandardWeeksInReportingTerm());
					periodEnrollmentMap.put(label, pe);
				}
				pe.addEnrollment(pc, datePeriodEnrollments.get(date));
			}
		}

		TreeSet<WSEByDayOfWeekAndPeriod.PeriodEnrollment> ts = new TreeSet<WSEByDayOfWeekAndPeriod.PeriodEnrollment>();
		ts.addAll(periodEnrollmentMap.values());

		for(PeriodEnrollment pe : ts){
			ArrayList<String> row = new ArrayList<String>();
			row.add(pe.getDayOfWeekLabel());
			row.add(pe.getTimeLabel());
			row.add(Float.toString(pe.getOrganizedWeeklyStudentEnrollment()));
			row.add(Float.toString(pe.getNotOrganizedWeeklyStudentEnrollment()));
			row.add(Float.toString(pe.getWeeklyStudentEnrollment()));
			addDataRow(row);			
		}
		
	}

	@SuppressWarnings("unchecked")
	private HashSet<PitClass> findAllPitClassesWithContactHoursForDepartmentsAndSubjectAreas(
			PointInTimeData pointInTimeData, Session hibSession) {
		
		HashSet<PitClass> pitClasses = new HashSet<PitClass>();
				
		for(Long deptId : getDepartmentIds()) {
			List<PitClass> pitClassesQueryResult = findAllPitClassesWithContactHoursForDepartment(pointInTimeData, deptId, hibSession);
			for(PitClass pc : pitClassesQueryResult) {
				if(pc.getPitSchedulingSubpart().getPitInstrOfferingConfig().getPitInstructionalOffering().getControllingPitCourseOffering().isIsControl().booleanValue() 
						&& getSubjectAreaIds().contains(pc.getPitSchedulingSubpart().getPitInstrOfferingConfig().getPitInstructionalOffering().getControllingPitCourseOffering().getSubjectArea().getUniqueId())) {
					pitClasses.add(pc);
				};
			}
		}
		return(pitClasses);

	}

	@Override
	protected void parseParameters() {
		super.parseParameters();
		
		if (getParameterValues().get(Parameter.DEPARTMENTS).size() < 1){
			//TODO: error
		} else {
			setDepartmentIds(getParameterValues().get(Parameter.DEPARTMENTS));
		}

		if (getParameterValues().get(Parameter.SUBJECTS).size() < 1){
			//TODO: error
		} else {
			setSubjectAreaIds(getParameterValues().get(Parameter.SUBJECTS));
		}

		if (getParameterValues().get(Parameter.PositionTypes).size() < 1){
			//TODO: error
		} else {
			setPositionTypeIds(getParameterValues().get(Parameter.PositionTypes));
		}

		if (getParameterValues().get(Parameter.RoomTypes).size() < 1){
			//TODO: error
		} else {
			setRoomTypeIds(getParameterValues().get(Parameter.RoomTypes));
		}

		if (getParameterValues().get(Parameter.MINIMUM_LOCATION_CAPACITY).size() != 1){
			//TODO: error
		} else {
			setMinimumLocationCapacity((Integer)getParameterValues().get(Parameter.MINIMUM_LOCATION_CAPACITY).get(0));
		}
		if (getParameterValues().get(Parameter.MAXIMUM_LOCATION_CAPACITY).size() != 1){
			//TODO: error
		} else {
			setMaximumLocationCapacity((Integer)getParameterValues().get(Parameter.MAXIMUM_LOCATION_CAPACITY).get(0));
		}

	}

	public ArrayList<Long> getPositionTypeIds() {
		return iPositionTypeIds;
	}

	public void setPositionTypeIds(ArrayList<Object> positionTypeIds) {
		this.iPositionTypeIds = new ArrayList<Long>();
		for(Object o : positionTypeIds) {
			this.iPositionTypeIds.add((Long) o);
		}
	}

	public ArrayList<Long> getSubjectAreaIds() {
		return iSubjectAreaIds;
	}

	public void setSubjectAreaIds(ArrayList<Object> subjectAreaIds) {
		this.iSubjectAreaIds = new ArrayList<Long>();
		for(Object o : subjectAreaIds) {
			this.iSubjectAreaIds.add((Long) o);
		}
	}

	public Integer getMinimumLocationCapacity() {
		return iMinimumLocationCapacity;
	}

	public void setMinimumLocationCapacity(Integer minimumLocationCapacity) {
		this.iMinimumLocationCapacity = minimumLocationCapacity;
	}

	public Integer getMaximumLocationCapacity() {
		return iMaximumLocationCapacity;
	}

	public void setMaximumLocationCapacity(Integer maximumLocationCapacity) {
		this.iMaximumLocationCapacity = maximumLocationCapacity;
	}

	@Override
	protected void runReport(org.hibernate.Session hibSession) {
		PointInTimeData pitd = (PointInTimeData)hibSession
				.createQuery("from PointInTimeData pitd where pitd.uniqueId = :uid")
				.setLong("uid", getPointInTimeDataUniqueId().longValue())
				.uniqueResult();
		createRoomUtilizationReportFor(pitd, hibSession);
		
	}

	public ArrayList<Long> getDepartmentIds() {
		return iDepartmentIds;
	}

	public void setDepartmentIds(ArrayList<Object> departmentIds) {
		this.iDepartmentIds = new ArrayList<Long>();
		for(Object o : departmentIds) {
			this.iDepartmentIds.add((Long) o);
		}
	}

	public ArrayList<Long> getRoomTypeIds() {
		return iRoomTypeIds;
	}

	public void setRoomTypeIds(ArrayList<Object> roomTypeIds) {
		this.iRoomTypeIds = new ArrayList<Long>();
		for(Object o : roomTypeIds) {
			this.iRoomTypeIds.add((Long) o);
		}
	}

	private class PeriodEnrollment implements Comparable<PeriodEnrollment>{
		private float iWeeklyStudentEnrollment = 0.0f;
		private float iOrganizedWeeklyStudentEnrollment = 0.0f;
		private float iNotOrganizedWeeklyStudentEnrollment = 0.0f;
		private java.util.Date iPeriod;
		private Float iStandardWeeksInReportingTerm;
		private int iDayOfWeek;
		private int iHourOfDay;
		private int iMinute;
		
		
		public java.util.Date getPeriod() {
			return iPeriod;
		}
		
		public int getDayOfWeek(){
			return(iDayOfWeek);
		}

		public int getHourOfDay(){
			return(iHourOfDay);
		}
		
		public int getMinute(){
			return(iMinute);
		}
		public float getOrganizedWeeklyStudentEnrollment() {
			return iOrganizedWeeklyStudentEnrollment;
		}

		public float getNotOrganizedWeeklyStudentEnrollment() {
			return iNotOrganizedWeeklyStudentEnrollment;
		}

		public float getWeeklyStudentEnrollment() {
			return this.iWeeklyStudentEnrollment;
		}
		
		public String getDayOfWeekLabel() {
			return(Localization.getDateFormat("EEEE").format(getPeriod()));
		}
		
		public String getTimeLabel() {
			return(Localization.getDateFormat("HH:mm").format(getPeriod()));
		}

		public PeriodEnrollment(java.util.Date period, Float standardWeeksInReportingTerm) {
			this.iPeriod = period;
			this.iStandardWeeksInReportingTerm = standardWeeksInReportingTerm;
			Calendar cal = Calendar.getInstance();
			cal.setTime(period);
			iDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
			iHourOfDay = cal.get(Calendar.HOUR_OF_DAY);
			iMinute = cal.get(Calendar.MINUTE);
			
		}
		
		public void addEnrollment(PitClass pitClass, Float enrollment) {
			iWeeklyStudentEnrollment += (enrollment.floatValue() / iStandardWeeksInReportingTerm.floatValue());
			if (pitClass.isOrganized()) {
				iOrganizedWeeklyStudentEnrollment += (enrollment / iStandardWeeksInReportingTerm.floatValue());
			} else {
				iNotOrganizedWeeklyStudentEnrollment += (enrollment / iStandardWeeksInReportingTerm.floatValue());
			}
		}

		@Override
		public int compareTo(PeriodEnrollment o) {
			if (this.iDayOfWeek == o.getDayOfWeek()){
				if (this.iHourOfDay == o.getHourOfDay()){
					if (this.iMinute == o.getMinute()){
						return(0);
					} else if(this.iMinute < o.getMinute()){
						return(-1);
					} else {
						return(1);
					}	
				} else if (this.iHourOfDay < o.getHourOfDay()){
					return(-1);
				} else {
					return(1);
				}
			} else if (this.iDayOfWeek < o.getDayOfWeek()) {
				return(-1);
			} else {
				return(1);
			}
		}
		
		@Override
		public boolean equals(Object o){
			
			if (o != null && o instanceof PeriodEnrollment) {
				PeriodEnrollment pe2 = (PeriodEnrollment) o;
				return(0 == this.compareTo(pe2));
			} else {
				return(false);
			}
		}
		
	}

	
}
