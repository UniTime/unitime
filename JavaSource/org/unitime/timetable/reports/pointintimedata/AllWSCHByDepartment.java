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
import java.util.HashSet;

import org.hibernate.Session;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.PitClass;
import org.unitime.timetable.model.PointInTimeData;

/**
 * @author says
 *
 */
public class AllWSCHByDepartment extends WSCHByDepartment {

	public AllWSCHByDepartment() {
		super();
	}

	@Override
	protected void intializeHeader() {
		ArrayList<String> hdr = new ArrayList<String>();
		hdr.add(MSG.columnDepartmentCode());
		hdr.add(MSG.columnDepartmentAbbreviation());
		hdr.add(MSG.columnDepartmentName());
		hdr.add(MSG.columnOrganizedWeeklyClassHours());
		hdr.add(MSG.columnNotOrganizedWeeklyClassHours());
		hdr.add(MSG.columnWeeklyClassHours());
		hdr.add(MSG.columnOrganizedWeeklyStudentClassHours());
		hdr.add(MSG.columnNotOrganizedWeeklyStudentClassHours());
		hdr.add(MSG.columnWeeklyStudentClassHours());
		setHeader(hdr);
	}

	@Override
	public String reportName() {
		return(MSG.deptWSCHReportAllHours());
	}

	@Override
	public String reportDescription() {
		return(MSG.deptWSCHReportAllHoursNote());
	}

	@Override
	protected float weeklyClassHours(PitClass pitClass) {
		return(pitClass.getAllWeeklyClassHours(getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm()));
	}

	@Override
	protected float weeklyStudentClassHours(PitClass pitClass) {
		return(pitClass.getAllWeeklyStudentClassHours(getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm()));
	}

	@Override
	public void createWeeklyStudentContactHoursByDepartmentReportFor(PointInTimeData pointInTimeData, Session hibSession) {
		HashSet<Long> processedClasses = new HashSet<Long>();
		for(Department d : pointInTimeData.getSession().getDepartments()) {
			if (!d.getSubjectAreas().isEmpty()) {
				float deptTotalWsch = 0;
				float deptTotalWch = 0;
				float deptTotalOrgWsch = 0;
				float deptTotalOrgWch = 0;
				float deptTotalNotOrgWsch = 0;
				float deptTotalNotOrgWch = 0;
					
				for (Long pioUid : findAllPitInstructionalOfferingUniqueIdsForDepartment(pointInTimeData, d.getUniqueId(), hibSession)) {
					for(PitClass pc : findAllPitClassesForPitInstructionalOfferingId(pointInTimeData, pioUid, hibSession)) {
						if (processedClasses.contains(pc.getUniqueId())){
							continue;
						}
						processedClasses.add(pc.getUniqueId());
						
						deptTotalWch += weeklyClassHours(pc);
						deptTotalWsch += weeklyStudentClassHours(pc);
						deptTotalOrgWsch += pc.getOrganizedWeeklyStudentClassHours(getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm());
						deptTotalOrgWch += pc.getOrganizedWeeklyClassHours(getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm());
						deptTotalNotOrgWsch += pc.getNotOrganizedWeeklyStudentClassHours(getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm());
						deptTotalNotOrgWch += pc.getNotOrganizedWeeklyClassHours(getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm());
					}
				}

				ArrayList<String> row = new ArrayList<String>();
				row.add(d.getDeptCode());
				row.add(d.getAbbreviation());
				row.add(d.getName());
				row.add(Float.toString(deptTotalOrgWch));
				row.add(Float.toString(deptTotalNotOrgWch));
				row.add(Float.toString(deptTotalWch));
				row.add(Float.toString(deptTotalOrgWsch));
				row.add(Float.toString(deptTotalNotOrgWsch));
				row.add(Float.toString(deptTotalWsch));
				addDataRow(row);
			}
		}	
	}


	
}
