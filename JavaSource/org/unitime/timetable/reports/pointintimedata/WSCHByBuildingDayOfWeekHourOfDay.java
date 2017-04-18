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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import org.hibernate.Session;
import org.unitime.timetable.model.Building;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.NonUniversityLocation;
import org.unitime.timetable.model.PitClass;
import org.unitime.timetable.model.PointInTimeData;
import org.unitime.timetable.model.Room;

public class WSCHByBuildingDayOfWeekHourOfDay extends WSCHByDayOfWeekAndHourOfDay {
	
	private TreeSet<Building> usedBuildings = new TreeSet<Building>();
	
	@Override
	public String reportName() {
		return(MSG.wseByBuildingDayOfWeekAndHourOfDayReport());
	}

	@Override
	public String reportDescription() {
		return(MSG.wseByBuildingDayOfWeekAndHourOfDayReportNote());
	}
	@Override
	protected void intializeHeader() {
		ArrayList<String> hdr = new ArrayList<String>();
		hdr.add(MSG.columnBuilding());
		hdr.add(MSG.columnDayOfWeek());
		addTimeColumns(hdr);
		setHeader(hdr);
	}


	@Override
	public void createRoomUtilizationReportFor(PointInTimeData pointInTimeData, Session hibSession) {
		
		calculatePeriodsWithEnrollments(pointInTimeData, hibSession);
		
		int minute = (startOnHalfHour ? 30 : 0);
		for(Building b : usedBuildings) {
			for(int dayOfWeek = 1 ; dayOfWeek < 8 ; dayOfWeek++) {
				ArrayList<String> row = new ArrayList<String>();
				row.add(b.getAbbreviation());
				row.add(getDayOfWeekLabel(periodDayOfWeek(dayOfWeek)));
				for(int hourOfDay = 0 ; hourOfDay < 24 ; hourOfDay++) {
					String key = getPeriodTag(b.getUniqueId().toString(), dayOfWeek, hourOfDay, minute);
					row.add(periodEnrollmentMap.get(key) == null ? "0": "" + periodEnrollmentMap.get(key).getWeeklyStudentEnrollment());
				}
				addDataRow(row);			
			}
		}
				
	}
	

	@SuppressWarnings("unchecked")
	private void calculatePeriodsWithEnrollments (
			PointInTimeData pointInTimeData, Session hibSession) {
		
		HashMap<Long, Building> permIdToBuilding = new HashMap<Long, Building>();
		Building nonUniversityLocationBuilding = new Building();
		nonUniversityLocationBuilding.setAbbreviation(MSG.labelUnknown());
		nonUniversityLocationBuilding.setAbbrName(MSG.labelUnknown());
		nonUniversityLocationBuilding.setName(MSG.labelUnknown());
		nonUniversityLocationBuilding.setUniqueId(new Long(-1));
		
		for(Location l : pointInTimeData.getSession().getRooms()) {
			if (l instanceof Room) {
				Room r = (Room) l;
				permIdToBuilding.put(r.getPermanentId(), r.getBuilding());
			} else if (l instanceof NonUniversityLocation) {
				NonUniversityLocation nul = (NonUniversityLocation) l;
				permIdToBuilding.put(nul.getPermanentId(), nonUniversityLocationBuilding);
			}
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct pc")
		  .append("	from PitClass pc") 
		  .append(" inner join pc.pitClassEvents as pce")
		  .append(" inner join pce.pitClassMeetings as pcm")
		  .append(" inner join pcm.pitClassMeetingUtilPeriods as pcmup")
		  .append("	where pc.pitSchedulingSubpart.pitInstrOfferingConfig.pitInstructionalOffering.pointInTimeData.uniqueId = :sessId")
		  .append(" and pc.pitSchedulingSubpart.itype.organized = true");
		
		for (PitClass pc : (List<PitClass>) hibSession.createQuery(sb.toString())
								.setLong("sessId", pointInTimeData.getUniqueId().longValue())
								.setCacheable(true)
								.list()) {
			
			for(Long roomPermanentId : pc.getLocationPeriodUseMap().keySet()) {
				if (permIdToBuilding.get(roomPermanentId) == null) {
					continue;
				}
				for(Date period : pc.getLocationPeriodUseMap().get(roomPermanentId)) {
					String label = getPeriodTag(permIdToBuilding.get(roomPermanentId).getUniqueId().toString(), period);
					usedBuildings.add(permIdToBuilding.get(roomPermanentId));
					PeriodEnrollment pe = periodEnrollmentMap.get(label);
					if (pe == null) {
						pe = new PeriodEnrollment(label, getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm());
						periodEnrollmentMap.put(label, pe);
					}
					pe.addEnrollment((1.0f * pc.getEnrollment()) / pc.countRoomsForPeriod(period));
				}
			}

		}

	}
}
