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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.hibernate.Session;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.PitClass;
import org.unitime.timetable.model.PointInTimeData;
import org.unitime.timetable.model.Room;
import org.unitime.timetable.util.Constants;

/**
 * @author says
 *
 */
public class RoomUtilization extends BasePointInTimeDataReports {
	protected ArrayList<Long> iDepartmentIds;
	protected ArrayList<Long> iRoomTypeIds;

	public RoomUtilization() {
		super();
		getParameters().add(Parameter.DEPARTMENTS);
		getParameters().add(Parameter.RoomTypes);
	}

	@Override
	protected void intializeHeader() {
		ArrayList<String> hdr = new ArrayList<String>();
		hdr.add(MSG.columnRoomDepartmentCode());
		hdr.add(MSG.columnRoomDepartmentAbbreviation());
		hdr.add(MSG.columnRoomDepartmentName());
		hdr.add(MSG.columnBuilding());
		hdr.add(MSG.columnRoom());
		hdr.add(MSG.columnRoomType());
		hdr.add(MSG.columnCapacity());
		hdr.add(MSG.columnStationHours());
		hdr.add(MSG.columnOccupancy());
		hdr.add(MSG.columnOrganizedWeeklyRoomHours());
		hdr.add(MSG.columnNotOrganizedWeeklyRoomHours());
		hdr.add(MSG.columnWeeklyRoomHours());
		hdr.add(MSG.columnOrganizedWeeklyStudentClassHours());
		hdr.add(MSG.columnNotOrganizedWeeklyStudentClassHours());
		hdr.add(MSG.columnWeeklyStudentClassHours());
		setHeader(hdr);
	}

	@Override
	public String reportName() {
		return(MSG.roomUtilizationReport());
	}

	@Override
	public String reportDescription() {
		return(MSG.roomUtilizationReportNote());
	}

	
	private void addRowForLocation(Location location, LocationHours locationHours) {
		ArrayList<String> row = new ArrayList<String>();
		row.add(location.getControllingDepartment() == null ? "" : location.getControllingDepartment().getDeptCode());
		row.add(location.getControllingDepartment() == null ? "" : location.getControllingDepartment().getAbbreviation());
		row.add(location.getControllingDepartment() == null ? "" : location.getControllingDepartment().getName());
		if (location instanceof NonUniversityLocation) {
			NonUniversityLocation nul = (NonUniversityLocation) location;
			row.add(nul.getName());
			row.add("");
		} else {
			Room r = (Room) location;
			row.add(r.getBuildingAbbv());
			row.add(r.getRoomNumber());
		}
		row.add(location.getRoomTypeLabel());
		row.add(location.getCapacity().toString());
		row.add((Float.toString(location.getCapacity().intValue() * locationHours.getWeeklyRoomHours())));
		row.add(Float.toString(locationHours.getWeeklyStudentClassHours()/(location.getCapacity().intValue() * locationHours.getWeeklyRoomHours())));
		row.add(Float.toString(locationHours.getOrganizedWeeklyRoomHours()));
		row.add(Float.toString(locationHours.getNotOrganizedWeeklyRoomHours()));
		row.add(Float.toString(locationHours.getWeeklyRoomHours()));
		row.add(Float.toString(locationHours.getOrganizedWeeklyStudentClassHours()));
		row.add(Float.toString(locationHours.getNotOrganizedWeeklyStudentClassHours()));
		row.add(Float.toString(locationHours.getWeeklyStudentClassHours()));
		addDataRow(row);
	}

	public void createRoomUtilizationReportFor(PointInTimeData pointInTimeData, Session hibSession) {
		HashMap<Long, RoomUtilization.LocationHours> locationUtilization = new HashMap<Long, RoomUtilization.LocationHours>();
		HashSet<PitClass> pitClasses = findAllPitClassesWithContactHoursForRoomDepartmentsAndRoomTypes(pointInTimeData, hibSession);
		for(PitClass pc : pitClasses) {
			for(Long locationPermanentId : pc.getLocationPermanentIdList()) {
				LocationHours lh = locationUtilization.get(locationPermanentId);
				if (lh == null) {
					lh = new LocationHours(locationPermanentId, getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm());
					locationUtilization.put(locationPermanentId, lh);
				}
				lh.addRoomHours(pc);
			}
		}
		if (pointInTimeData.getSession().getRooms() != null && !pointInTimeData.getSession().getRooms().isEmpty()) {
			HashSet<Location> locations = new HashSet<Location>();
			locations.addAll(pointInTimeData.getSession().getRooms());
			for (Location l : locations) {
				LocationHours lh = locationUtilization.get(l.getPermanentId());
				if (lh != null && l.getControllingDepartment() != null && getDepartmentIds().contains(l.getControllingDepartment().getUniqueId()) && getRoomTypeIds().contains(l.getRoomType().getUniqueId())) {
					addRowForLocation(l, lh);
				} else if (lh != null && l.getControllingDepartment() == null && getRoomTypeIds().contains(l.getRoomType().getUniqueId())) {
					addRowForLocation(l, lh);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	protected HashSet<PitClass> findAllPitClassesWithContactHoursForRoomDepartmentsAndRoomTypes(
			PointInTimeData pointInTimeData, Session hibSession) {
		
		HashSet<PitClass> pitClasses = new HashSet<PitClass>();
		
		StringBuilder sb = new StringBuilder();
		sb.append("select pc")
		  .append(" from PitClass pc ")
		  .append(" where pc.uniqueId in ( select pcm.pitClassEvent.pitClass.uniqueId from PitClassMeeting pcm, Location l inner join l.roomDepts as rd "
		  		+ " where pcm.pitClassEvent.pitClass.pitSchedulingSubpart.pitInstrOfferingConfig.pitInstructionalOffering.pointInTimeData.uniqueId = :pitdUid")
		  .append(" and pcm.locationPermanentId = l.permanentId")
		  .append(" and l.session.uniqueId = pcm.pitClassEvent.pitClass.pitSchedulingSubpart.pitInstrOfferingConfig.pitInstructionalOffering.pointInTimeData.session.uniqueId")
//		  .append(" and rd.control = true")
		  .append(" and rd.department.uniqueId = :deptId")
		  .append(" and l.roomType.uniqueId in ( ");
		boolean first = true;
		for(Long rtId : getRoomTypeIds()){
			if (first) {
				first = false;
			} else {
				sb.append(", ");
			}
			sb.append(rtId.toString());
		}
		sb.append(" ) ) ");
;
				
		for(Long deptId : getDepartmentIds()) {
			pitClasses.addAll((List<PitClass>)hibSession.createQuery(sb.toString())
					.setLong("pitdUid", pointInTimeData.getUniqueId().longValue())
					.setLong("deptId", deptId.longValue())
					.setCacheable(true)
					.list());
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

		if (getParameterValues().get(Parameter.RoomTypes).size() < 1){
			//TODO: error
		} else {
			setRoomTypeIds(getParameterValues().get(Parameter.RoomTypes));
		}

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

	protected class LocationHours {
		private float iWeeklyStudentClassHours = 0.0f;
		private float iOrganizedWeeklyStudentClassHours = 0.0f;
		private float iNotOrganizedWeeklyStudentClassHours = 0.0f;
		private Long iLocationPermanentId;
		private HashSet<java.util.Date> organizedPeriods = new HashSet<java.util.Date>();
		private HashSet<java.util.Date> notOrganizedPeriods = new HashSet<java.util.Date>();
		private HashSet<java.util.Date> allPeriods = new HashSet<java.util.Date>();
		private Float iStandardMinutesInReportingHour;
		private Float iStandardWeeksInReportingTerm;
		private HashSet<Long> classIds = new HashSet<Long>();
		
		public Long getLocationPermanentId() {
			return iLocationPermanentId;
		}

		public float getOrganizedWeeklyRoomHours() {
			return ((Constants.SLOT_LENGTH_MIN * 1.0f) * organizedPeriods.size() / iStandardMinutesInReportingHour / iStandardWeeksInReportingTerm);
		}

		public float getNotOrganizedWeeklyRoomHours() {
			return ((Constants.SLOT_LENGTH_MIN * 1.0f) * notOrganizedPeriods.size() / iStandardMinutesInReportingHour / iStandardWeeksInReportingTerm);
		}

		public float getOrganizedWeeklyStudentClassHours() {
			return iOrganizedWeeklyStudentClassHours;
		}

		public float getNotOrganizedWeeklyStudentClassHours() {
			return iNotOrganizedWeeklyStudentClassHours;
		}

		public float getWeeklyRoomHours() {
			return ((Constants.SLOT_LENGTH_MIN * 1.0f) * allPeriods.size() / iStandardMinutesInReportingHour / iStandardWeeksInReportingTerm);
		}

		public float getWeeklyStudentClassHours() {
			return this.iWeeklyStudentClassHours;
		}

		public LocationHours(Long locationPermanentId, Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm) {
			this.iLocationPermanentId = locationPermanentId;
			this.iStandardMinutesInReportingHour = standardMinutesInReportingHour;
			this.iStandardWeeksInReportingTerm = standardWeeksInReportingTerm;
		}
		
		private void addPeriods(PitClass pitClass) {
			if (pitClass.getPeriodsForLocation(getLocationPermanentId()) != null) {
				if (pitClass.isOrganized()) {
					organizedPeriods.addAll(pitClass.getPeriodsForLocation(getLocationPermanentId()));
				} else {
					notOrganizedPeriods.addAll(pitClass.getPeriodsForLocation(getLocationPermanentId()));
				}
				allPeriods.addAll(pitClass.getPeriodsForLocation(getLocationPermanentId()));
			}
		}
		
		public void addRoomHours(PitClass pitClass) {
			if(classIds.contains(pitClass.getUniqueId())){
				throw new Error("Counted class twice:  " + pitClass.getUniqueId().toString());
			} else {
				classIds.add(pitClass.getUniqueId());
			}
			addPeriods(pitClass);
			iWeeklyStudentClassHours += (pitClass.getAllWeeklyStudentClassHoursForLocation(getLocationPermanentId(), iStandardMinutesInReportingHour, iStandardWeeksInReportingTerm));
			iOrganizedWeeklyStudentClassHours += (pitClass.getOrganizedWeeklyStudentClassHoursForLocation(getLocationPermanentId(), iStandardMinutesInReportingHour, iStandardWeeksInReportingTerm));
			iNotOrganizedWeeklyStudentClassHours += (pitClass.getNotOrganizedWeeklyStudentClassHoursForLocation(getLocationPermanentId(), iStandardMinutesInReportingHour, iStandardWeeksInReportingTerm));										
		}
		
	}

	
}
