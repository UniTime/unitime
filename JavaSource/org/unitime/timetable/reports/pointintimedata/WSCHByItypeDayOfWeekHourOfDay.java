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
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.PitClass;
import org.unitime.timetable.model.PointInTimeData;

public class WSCHByItypeDayOfWeekHourOfDay extends WSCHByDayOfWeekAndHourOfDay {
	
	@Override
	public String reportName() {
		return(MSG.wseByItypeDayOfWeekAndHourOfDayReport());
	}

	@Override
	public String reportDescription() {
		return(MSG.wseByItypeDayOfWeekAndHourOfDayReportNote());
	}
	@Override
	protected void intializeHeader() {
		ArrayList<String> hdr = new ArrayList<String>();
		hdr.add(MSG.columnItype());
		hdr.add(MSG.columnOrganized());
		hdr.add(MSG.columnDayOfWeek());
		addTimeColumns(hdr);
		setHeader(hdr);
	}


	@Override
	public void createRoomUtilizationReportFor(PointInTimeData pointInTimeData, Session hibSession) {
		
		calculatePeriodsWithEnrollments(pointInTimeData, hibSession);
		
		int minute = (startOnHalfHour ? 30 : 0);
		for(ItypeDesc itype : ItypeDesc.findAll(true)) {
			for(int dayOfWeek = 1 ; dayOfWeek < 8 ; dayOfWeek++) {
				ArrayList<String> row = new ArrayList<String>();
				row.add(itype.getDesc());
				row.add(itype.getOrganized().toString());
				row.add(getDayOfWeekLabel(periodDayOfWeek(dayOfWeek)));
				for(int hourOfDay = 0 ; hourOfDay < 24 ; hourOfDay++) {
					String key = getPeriodTag(itype.getAbbv(), dayOfWeek, hourOfDay, minute);
					row.add(periodEnrollmentMap.get(key) == null ? "0": "" + periodEnrollmentMap.get(key).getWeeklyStudentEnrollment());
				}
				addDataRow(row);			
			}
		}
				
	}

	@SuppressWarnings("unchecked")
	private void calculatePeriodsWithEnrollments (
			PointInTimeData pointInTimeData, Session hibSession) {
		
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct pc.pitSchedulingSubpart.itype, pc")
		  .append("	from PitClass pc") 
		  .append(" inner join pc.pitClassEvents as pce")
		  .append(" inner join pce.pitClassMeetings as pcm")
		  .append(" inner join pcm.pitClassMeetingUtilPeriods as pcmup")
		  .append("	where pc.pitSchedulingSubpart.pitInstrOfferingConfig.pitInstructionalOffering.pointInTimeData.uniqueId = :sessId");
		
		for (Object[] result : (List<Object[]>) hibSession.createQuery(sb.toString())
								.setLong("sessId", pointInTimeData.getUniqueId().longValue())
								.setCacheable(true)
								.list()) {
			
			ItypeDesc itype = (ItypeDesc) result[0];
			PitClass pc = (PitClass) result[1];
			for (Date meetingPeriod : pc.getUniquePeriods()) {			
				String label = getPeriodTag((itype.getParent() == null ? itype.getAbbv() : itype.getParent().getAbbv()), meetingPeriod);
				PeriodEnrollment pe = periodEnrollmentMap.get(label);
				if (pe == null) {
					pe = new PeriodEnrollment(label, getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm());
					periodEnrollmentMap.put(label, pe);
				}
				
				pe.addEnrollment(pc.getEnrollment());
			}

		}

	}
}
