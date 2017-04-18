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

import org.hibernate.Session;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PitClass;
import org.unitime.timetable.model.PointInTimeData;
import org.unitime.timetable.model.RoomType;

/**
 * @author says
 *
 */
public class RoomTypeUtilizationByDepartment extends RoomUtilization {

	public RoomTypeUtilizationByDepartment() {
		super();
	}

	@Override
	protected void intializeHeader() {
		ArrayList<String> hdr = new ArrayList<String>();
		hdr.add(MSG.columnRoomDepartmentCode());
		hdr.add(MSG.columnRoomDepartmentAbbreviation());
		hdr.add(MSG.columnRoomDepartmentName());
		hdr.add(MSG.columnRoomType());
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
		return(MSG.roomTypeUtilizationByDepartmentReport());
	}

	@Override
	public String reportDescription() {
		return(MSG.roomTypeUtilizationByDepartmentReportNote());
	}
	
	private void addRowForDepartmentRoomType(DepartmentRoomTypeHours departmentRoomTypeHours) {
		ArrayList<String> row = new ArrayList<String>();
		row.add(departmentRoomTypeHours.getDepartment() == null ? "" : departmentRoomTypeHours.getDepartment().getDeptCode());
		row.add(departmentRoomTypeHours.getDepartment() == null ? "" : departmentRoomTypeHours.getDepartment().getAbbreviation());
		row.add(departmentRoomTypeHours.getDepartment() == null ? "" : departmentRoomTypeHours.getDepartment().getName());
		row.add(departmentRoomTypeHours.getRoomType().getLabel());
		row.add((Float.toString(departmentRoomTypeHours.getStationHours())));
		row.add(Float.toString(departmentRoomTypeHours.getOccupancy()));
		row.add(Float.toString(departmentRoomTypeHours.getOrganizedWeeklyRoomHours()));
		row.add(Float.toString(departmentRoomTypeHours.getNotOrganizedWeeklyRoomHours()));
		row.add(Float.toString(departmentRoomTypeHours.getWeeklyRoomHours()));
		row.add(Float.toString(departmentRoomTypeHours.getOrganizedWeeklyStudentClassHours()));
		row.add(Float.toString(departmentRoomTypeHours.getNotOrganizedWeeklyStudentClassHours()));
		row.add(Float.toString(departmentRoomTypeHours.getWeeklyStudentClassHours()));
		addDataRow(row);
	}

	public void createRoomUtilizationReportFor(PointInTimeData pointInTimeData, Session hibSession) {
		HashMap<Long, RoomTypeUtilizationByDepartment.LocationHours> locationUtilization = new HashMap<Long, RoomTypeUtilizationByDepartment.LocationHours>();
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
		HashMap<Department, HashMap<RoomType, DepartmentRoomTypeHours>> departmentRoomTypeHours = new HashMap<Department, HashMap<RoomType,DepartmentRoomTypeHours>>(); 
		HashSet<Location> locations = new HashSet<Location>();
		if (pointInTimeData.getSession().getRooms() != null && !pointInTimeData.getSession().getRooms().isEmpty()) {
			locations.addAll(pointInTimeData.getSession().getRooms());
	
			for(Location l : locations) {
				LocationHours lh = locationUtilization.get(l.getPermanentId());
				if (lh != null 
						&& ((l.getControllingDepartment() != null 
							&& getDepartmentIds().contains(l.getControllingDepartment().getUniqueId()) 
							&& getRoomTypeIds().contains(l.getRoomType().getUniqueId()))
						|| (l.getControllingDepartment() == null 
							&& getRoomTypeIds().contains(l.getRoomType().getUniqueId())))) {
					HashMap<RoomType, DepartmentRoomTypeHours> rth = departmentRoomTypeHours.get(l.getControllingDepartment());
					if(rth == null){
						rth = new HashMap<RoomType, RoomTypeUtilizationByDepartment.DepartmentRoomTypeHours>();
						departmentRoomTypeHours.put(l.getControllingDepartment(), rth);
					}
					DepartmentRoomTypeHours drth = rth.get(l.getRoomType());
					if (drth == null) {
						drth = new DepartmentRoomTypeHours(l.getControllingDepartment(), l.getRoomType());
						rth.put(l.getRoomType(), drth);
					}
					drth.addRoomHours(l, lh);
				} 
			}
		}
		
		for(Department d : departmentRoomTypeHours.keySet()){			
			for(RoomType rt : departmentRoomTypeHours.get(d).keySet()){
				addRowForDepartmentRoomType(departmentRoomTypeHours.get(d).get(rt));
			}
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

	private class DepartmentRoomTypeHours {
		private float iWeeklyRoomHours = 0.0f;
		private float iOrganizedWeeklyRoomHours = 0.0f;
		private float iNotOrganizedWeeklyRoomHours = 0.0f;
		private float iWeeklyStudentClassHours = 0.0f;
		private float iOrganizedWeeklyStudentClassHours = 0.0f;
		private float iNotOrganizedWeeklyStudentClassHours = 0.0f;
		private float iStationHours = 0.0f;
		private RoomType iRoomType;
		private Department iDepartment;
		
		public RoomType getRoomType() {
			return iRoomType;
		}

		public Department getDepartment(){
			return iDepartment;
		}

		public float getOrganizedWeeklyStudentClassHours() {
			return iOrganizedWeeklyStudentClassHours;
		}

		public float getNotOrganizedWeeklyStudentClassHours() {
			return iNotOrganizedWeeklyStudentClassHours;
		}

		public float getWeeklyStudentClassHours() {
			return this.iWeeklyStudentClassHours;
		}

		public float getWeeklyRoomHours() {
			return iWeeklyRoomHours;
		}


		public float getOrganizedWeeklyRoomHours() {
			return iOrganizedWeeklyRoomHours;
		}


		public float getNotOrganizedWeeklyRoomHours() {
			return iNotOrganizedWeeklyRoomHours;
		}


		public float getStationHours() {
			return iStationHours;
		}

		public float getOccupancy() {
			return(getWeeklyStudentClassHours()/getStationHours());
		}

		public DepartmentRoomTypeHours(Department department, RoomType roomType) {
			this.iDepartment = department;
			this.iRoomType = roomType;
		}
		
		
		public void addRoomHours(Location location, LocationHours locationHours) {
			iStationHours += location.getCapacity().intValue() * locationHours.getWeeklyRoomHours();
			iWeeklyRoomHours += (locationHours.getWeeklyRoomHours());
			iOrganizedWeeklyRoomHours += (locationHours.getOrganizedWeeklyRoomHours());
			iNotOrganizedWeeklyRoomHours += (locationHours.getNotOrganizedWeeklyRoomHours());
			iWeeklyStudentClassHours += (locationHours.getWeeklyStudentClassHours());
			iOrganizedWeeklyStudentClassHours += (locationHours.getOrganizedWeeklyStudentClassHours());
			iNotOrganizedWeeklyStudentClassHours += (locationHours.getNotOrganizedWeeklyStudentClassHours());
			
		}
		
	}

	
}
