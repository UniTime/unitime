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
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.PitClass;
import org.unitime.timetable.model.PitClassInstructor;
import org.unitime.timetable.model.PitDepartmentalInstructor;
import org.unitime.timetable.model.PointInTimeData;
import org.unitime.timetable.model.TeachingResponsibility;

public class AllWSCHForDepartmentByInstructor extends WSCHByDepartment {
	private ArrayList<Long> iDepartmentIds;

	public AllWSCHForDepartmentByInstructor() {
		super();
		getParameters().add(Parameter.DEPARTMENTS);
	}

	@Override
	protected void intializeHeader() {
		ArrayList<String> hdr = new ArrayList<String>();
		hdr.add(MSG.columnDepartmentCode());
		hdr.add(MSG.columnDepartmentAbbreviation());
		hdr.add(MSG.columnDepartmentName());
		hdr.add(MSG.columnInstructor());
		hdr.add(MSG.columnInstructorExternalId());
		hdr.add(MSG.columnOrganizedWeeklyClassHours());
		hdr.add(MSG.columnNotOrganizedWeeklyClassHours());
		hdr.add(MSG.columnWeeklyClassHours());
		hdr.add(MSG.columnOrganizedWeeklyStudentClassHours());
		hdr.add(MSG.columnNotOrganizedWeeklyStudentClassHours());
		hdr.add(MSG.columnWeeklyStudentClassHours());
		setHeader(hdr);
	}

	@Override
	protected void parseParameters() {
		super.parseParameters();
		if (getParameterValues().get(Parameter.DEPARTMENTS).size() < 1){
			//TODO: error
		} else {
			setDepartmentIds(getParameterValues().get(Parameter.DEPARTMENTS));
		}
	}
	
	@Override
	public String reportName() {
		return(MSG.deptWSCHReportAllHoursForDepartmentByInstructor());
	}

	@Override
	public String reportDescription() {
		return(MSG.deptWSCHReportAllHoursForDepartmentByInstructorNote());
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
		for(Long deptId : getDepartmentIds()){
			Department d = (Department) hibSession.createQuery("from Department d where d.uniqueId = :id").setLong("id", deptId).setCacheable(true).uniqueResult();
			HashMap<PitDepartmentalInstructor, InstructorHours> positionClassHours = new HashMap<PitDepartmentalInstructor, InstructorHours>();
			for (Long pioUid : findAllPitInstructionalOfferingUniqueIdsForDepartment(pointInTimeData, deptId, hibSession)) {
				for(PitClass pc : findAllPitClassesForPitInstructionalOfferingId(pointInTimeData, pioUid, hibSession)) {
					if (processedClasses.contains(pc.getUniqueId())){
						continue;
					}
					processedClasses.add(pc.getUniqueId());

					if (pc.getPitClassInstructors() == null || pc.getPitClassInstructors().isEmpty()) {
						InstructorHours ih = positionClassHours.get(null);
						if (ih == null) {
							ih = new InstructorHours(null, getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm());
							positionClassHours.put(null, ih);
						}
						ih.addClassHours(null, pc);
					} else {
						int numInstructors = 0;
						for(PitClassInstructor pci : pc.getPitClassInstructors()) {
							InstructorHours ih = positionClassHours.get(pci.getPitDepartmentalInstructor());
							if (ih == null) {
								ih = new InstructorHours(pci.getPitDepartmentalInstructor(), getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm());
								positionClassHours.put(pci.getPitDepartmentalInstructor(), ih);
							}
							if(ih.addClassHours(pci, pc)) {
								numInstructors++;
							}
						}
						if (numInstructors == 0){
							InstructorHours ih = positionClassHours.get(null);
							if (ih == null) {
								ih = new InstructorHours(null, getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm());
								positionClassHours.put(null, ih);
							}
							ih.addClassHours(null, pc);
						}
					}
				}	
			   
			}
			for(PitDepartmentalInstructor pdi : positionClassHours.keySet()) {
				InstructorHours ih = positionClassHours.get(pdi);
				ArrayList<String> row = new ArrayList<String>();
				row.add(d.getDeptCode());
				row.add(d.getAbbreviation());
				row.add(d.getName());
				row.add(pdi == null? MSG.labelUnknown() : pdi.getName(DepartmentalInstructor.sNameFormatLastFirstMiddle));
				row.add(pdi == null? MSG.labelUnknown() : (pdi.getExternalUniqueId() == null ? MSG.labelUnknown() : pdi.getExternalUniqueId()));
				row.add(Float.toString(ih.getOrganizedWeeklyClassHours()));
				row.add(Float.toString(ih.getNotOrganizedWeeklyClassHours()));
				row.add(Float.toString(ih.getWeeklyClassHours()));
				row.add(Float.toString(ih.getOrganizedWeeklyStudentClassHours()));
				row.add(Float.toString(ih.getNotOrganizedWeeklyStudentClassHours()));
				row.add(Float.toString(ih.getWeeklyStudentClassHours()));
				addDataRow(row);
			}
		}
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

	private class InstructorHours {
		private float iWeeklyClassHours = 0.0f;
		private float iOrganizedWeeklyClassHours = 0.0f;
		private float iNotOrganizedWeeklyClassHours = 0.0f;
		private float iWeeklyStudentClassHours = 0.0f;
		private float iOrganizedWeeklyStudentClassHours = 0.0f;
		private float iNotOrganizedWeeklyStudentClassHours = 0.0f;
		private PitDepartmentalInstructor iPitDepartmentalInstructor;
		private Float iStandardMinutesInReportingHour;
		private Float iStandardWeeksInReportingTerm;
		
		public PitDepartmentalInstructor getPitDeparmentInstructor() {
			return iPitDepartmentalInstructor;
		}

		public float getOrganizedWeeklyClassHours() {
			return iOrganizedWeeklyClassHours;
		}

		public float getNotOrganizedWeeklyClassHours() {
			return iNotOrganizedWeeklyClassHours;
		}

		public float getOrganizedWeeklyStudentClassHours() {
			return iOrganizedWeeklyStudentClassHours;
		}

		public float getNotOrganizedWeeklyStudentClassHours() {
			return iNotOrganizedWeeklyStudentClassHours;
		}

		public float getWeeklyClassHours() {
			return this.iWeeklyClassHours;
		}

		public float getWeeklyStudentClassHours() {
			return this.iWeeklyStudentClassHours;
		}

		public InstructorHours(PitDepartmentalInstructor pitDepartmentalInstructor, Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm) {
			this.iPitDepartmentalInstructor = pitDepartmentalInstructor;
			this.iStandardMinutesInReportingHour = standardMinutesInReportingHour;
			this.iStandardWeeksInReportingTerm = standardWeeksInReportingTerm;
		}
		
		public boolean addClassHours(PitClassInstructor pitClassInstructor, PitClass pitClass) {
			if (pitClassInstructor == null){
				if (this.iPitDepartmentalInstructor == null) {
					updateHours(pitClass, 100);
					return(true);
				}
				return(false);
			} else {
				if (pitClassInstructor.getResponsibility() != null && pitClassInstructor.getResponsibility().hasOption(TeachingResponsibility.Option.auxiliary)) {
					return(false);
				}
				if (pitClassInstructor.getNormalizedPercentShare().intValue() == 0){
					return(false);
				}
				if (pitClassInstructor.getPitDepartmentalInstructor() == null) {
					if (this.iPitDepartmentalInstructor == null) {
						updateHours(pitClass, pitClassInstructor.getNormalizedPercentShare());
						return(true);
					}						
				} else if (pitClassInstructor.getPitDepartmentalInstructor().equals(getPitDeparmentInstructor())) {
					updateHours(pitClass, pitClassInstructor.getNormalizedPercentShare());
					return(true);
				}
				return(false);
			} 
		}
		
		private void updateHours(PitClass pitClass, Integer percentShare) {
			iWeeklyClassHours += (pitClass.getAllWeeklyClassHours(iStandardMinutesInReportingHour, iStandardWeeksInReportingTerm) * (percentShare / 100.0f));
			iOrganizedWeeklyClassHours += (pitClass.getOrganizedWeeklyClassHours(iStandardMinutesInReportingHour, iStandardWeeksInReportingTerm) * (percentShare / 100.0f));
			iNotOrganizedWeeklyClassHours += (pitClass.getNotOrganizedWeeklyClassHours(iStandardMinutesInReportingHour, iStandardWeeksInReportingTerm) * (percentShare / 100.0f));
			iWeeklyStudentClassHours += (pitClass.getAllWeeklyStudentClassHours(iStandardMinutesInReportingHour, iStandardWeeksInReportingTerm) * (percentShare / 100.0f));
			iOrganizedWeeklyStudentClassHours += (pitClass.getOrganizedWeeklyStudentClassHours(iStandardMinutesInReportingHour, iStandardWeeksInReportingTerm) * (percentShare  / 100.0f));
			iNotOrganizedWeeklyStudentClassHours += (pitClass.getNotOrganizedWeeklyStudentClassHours(iStandardMinutesInReportingHour, iStandardWeeksInReportingTerm) * (percentShare / 100.0f));										
		}

	}

}
