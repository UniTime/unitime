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
import java.util.List;

import org.hibernate.Session;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.PitClass;
import org.unitime.timetable.model.PitClassEvent;
import org.unitime.timetable.model.PointInTimeData;

public class AllWSCHForDepartmentByClass extends WSCHByDepartment {
	private Long departmentId;

	public AllWSCHForDepartmentByClass() {
		super();
		getParameters().add(Parameter.DEPARTMENT);
	}

	@Override
	protected void intializeHeader() {
		ArrayList<String> hdr = new ArrayList<String>();
		hdr.add(MSG.columnDepartmentCode());
		hdr.add(MSG.columnDepartmentAbbreviation());
		hdr.add(MSG.columnDepartmentName());
		hdr.add(MSG.columnSubjectArea());
		hdr.add(MSG.columnCourseNumber());
		hdr.add(MSG.columnItype());
		hdr.add(MSG.columnOrganized());
		hdr.add(MSG.columnSectionNumber());
		hdr.add(MSG.columnExternalId());
		hdr.add(MSG.columnOrganizedWeeklyClassHours());
		hdr.add(MSG.columnNotOrganizedWeeklyClassHours());
		hdr.add(MSG.columnWeeklyClassHours());
		hdr.add(MSG.columnOrganizedWeeklyStudentClassHours());
		hdr.add(MSG.columnNotOrganizedWeeklyStudentClassHours());
		hdr.add(MSG.columnWeeklyStudentClassHours());
		hdr.add(MSG.columnNumberOfClassMeetings());
		setHeader(hdr);
	}

	@Override
	protected void parseParameters() {
		super.parseParameters();
		if (getParameterValues().get(Parameter.DEPARTMENT).size() != 1){
			//TODO: error
		} else {
			setDepartmentId((Long)getParameterValues().get(Parameter.DEPARTMENT).get(0));
		}
	}
	
	@Override
	public String reportName() {
		return(MSG.deptWSCHReportAllHoursForDepartmentByClass());
	}

	@Override
	public String reportDescription() {
		return(MSG.deptWSCBReportAllHoursForDepartmentByClassNote());
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
		Department d = (Department) hibSession.createQuery("from Department d where d.uniqueId = :id").setLong("id", getDepartmentId()).setCacheable(true).uniqueResult();
		List<PitClass> pitClassesForDept = findAllPitClassesWithContactHoursForDepartment(pointInTimeData, d, hibSession);
		for(PitClass pc : pitClassesForDept) {
			ArrayList<String> row = new ArrayList<String>();
			row.add(d.getDeptCode());
			row.add(d.getAbbreviation());
			row.add(d.getName());
			row.add(pc.getPitSchedulingSubpart().getPitInstrOfferingConfig().getPitInstructionalOffering().getControllingPitCourseOffering().getSubjectArea().getSubjectAreaAbbreviation());
			row.add(pc.getPitSchedulingSubpart().getPitInstrOfferingConfig().getPitInstructionalOffering().getControllingPitCourseOffering().getCourseNbr());
			row.add(pc.getPitSchedulingSubpart().getItype().getAbbv());
			row.add(pc.getPitSchedulingSubpart().getItype().getOrganized().toString());
			row.add(pc.getSectionNumber().toString() + (pc.getPitSchedulingSubpart().getSchedulingSubpartSuffixCache().equals("-")?"":pc.getPitSchedulingSubpart().getSchedulingSubpartSuffixCache()));
			row.add(pc.getExternalUniqueId());
			row.add(Float.toString(pc.getOrganizedWeeklyClassHours(getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm())));
			row.add(Float.toString(pc.getNotOrganizedWeeklyClassHours(getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm())));
			row.add(Float.toString(weeklyClassHours(pc)));
			row.add(Float.toString(pc.getOrganizedWeeklyStudentClassHours(getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm())));
			row.add(Float.toString(pc.getNotOrganizedWeeklyStudentClassHours(getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm())));
			row.add(Float.toString(weeklyStudentClassHours(pc)));
			int cnt = 0;
			for(PitClassEvent pce : pc.getPitClassEvents()){
				cnt += pce.getPitClassMeetings().size();
			}
			row.add(Integer.toString(cnt));
			addDataRow(row);
		}	
	}

	public Long getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Long departmentId) {
		this.departmentId = departmentId;
	}



}
