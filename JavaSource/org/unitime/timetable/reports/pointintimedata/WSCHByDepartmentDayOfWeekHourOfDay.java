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
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.hibernate.Session;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.PitClass;
import org.unitime.timetable.model.PointInTimeData;
import org.unitime.timetable.model.SubjectArea;

public class WSCHByDepartmentDayOfWeekHourOfDay extends WSCHByDayOfWeekAndHourOfDay {
	
	TreeSet<Department> depts = new TreeSet<Department>();
	
	@Override
	public String reportName() {
		return(MSG.wseByDeptDayOfWeekAndHourOfDayReport());
	}

	@Override
	public String reportDescription() {
		return(MSG.wseByDeptDayOfWeekAndHourOfDayReportNote());
	}
	@Override
	protected void intializeHeader() {
		ArrayList<String> hdr = new ArrayList<String>();
		hdr.add(MSG.columnDepartmentCode());
		hdr.add(MSG.columnDepartmentAbbreviation());
		hdr.add(MSG.columnDepartmentName());
		hdr.add(MSG.columnDayOfWeek());
		addTimeColumns(hdr);
		setHeader(hdr);
	}


	@Override
	public void createRoomUtilizationReportFor(PointInTimeData pointInTimeData, Session hibSession) {
		
		calculatePeriodsWithEnrollments(pointInTimeData, hibSession);
		
		int minute = (startOnHalfHour ? 30 : 0);
		for(Department department : depts) {
			for(int dayOfWeek = 1 ; dayOfWeek < 8 ; dayOfWeek++) {
				ArrayList<String> row = new ArrayList<String>();
				row.add(department.getDeptCode());
				row.add(department.getAbbreviation());
				row.add(department.getName());
				row.add(getDayOfWeekLabel(periodDayOfWeek(dayOfWeek)));
				for(int hourOfDay = 0 ; hourOfDay < 24 ; hourOfDay++) {
					String key = getPeriodTag(department.getUniqueId().toString(), dayOfWeek, hourOfDay, minute);
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
		sb.append("select distinct pco.subjectArea.department, pc")
		  .append("	from PitClass pc") 
		  .append(" inner join pc.pitClassEvents as pce")
		  .append(" inner join pce.pitClassMeetings as pcm")
		  .append(" inner join pcm.pitClassMeetingUtilPeriods as pcmup")
		  .append("	inner join pc.pitSchedulingSubpart.pitInstrOfferingConfig.pitInstructionalOffering.pitCourseOfferings as pco")
		  .append("	where pc.pitSchedulingSubpart.pitInstrOfferingConfig.pitInstructionalOffering.pointInTimeData.uniqueId = :sessId")
		  .append(" and pco.subjectArea.uniqueId = :saId")
		  .append("	and pco.isControl = true")
		  .append(" and  pc.pitSchedulingSubpart.itype.organized = true");
		
		HashSet<Long> processedClasses = new HashSet<Long>();
		for (SubjectArea subjectArea : pointInTimeData.getSession().getSubjectAreas()){
			for (Object[] result : (List<Object[]>) hibSession.createQuery(sb.toString())
									.setLong("sessId", pointInTimeData.getUniqueId().longValue())
									.setLong("saId", subjectArea.getUniqueId().longValue())
									.setCacheable(true)
									.list()) {
				
				Department department = (Department) result[0];
				PitClass pc = (PitClass) result[1];
				if (processedClasses.contains(pc.getUniqueId())){
					continue;
				}
				processedClasses.add(pc.getUniqueId());
			
				depts.add(department);
				
				for (Date meetingPeriod : pc.getUniquePeriods()) {			
					String label = getPeriodTag(department.getUniqueId().toString(), meetingPeriod);
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
}
