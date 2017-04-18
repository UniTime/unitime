package org.unitime.timetable.reports.pointintimedata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.hibernate.Session;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.PitClass;
import org.unitime.timetable.model.PitClassInstructor;
import org.unitime.timetable.model.PointInTimeData;
import org.unitime.timetable.model.PositionType;
import org.unitime.timetable.model.TeachingResponsibility;

public class AllWSCHForDepartmentByInstructorPosition extends WSCHByDepartment {
	private ArrayList<Long> iDepartmentIds;

	public AllWSCHForDepartmentByInstructorPosition() {
		super();
		getParameters().add(Parameter.DEPARTMENTS);
	}

	@Override
	protected void intializeHeader() {
		ArrayList<String> hdr = new ArrayList<String>();
		hdr.add(MSG.columnDepartmentCode());
		hdr.add(MSG.columnDepartmentAbbreviation());
		hdr.add(MSG.columnDepartmentName());
		hdr.add(MSG.columnPosition());
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
		return(MSG.deptWSCHReportAllHoursForDepartmentByPosition());
	}

	@Override
	public String reportDescription() {
		return(MSG.deptWSCBReportAllHoursForDepartmentByPositionNote());
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
		for(Long deptId : getDepartmentIds()){
			Department d = (Department) hibSession.createQuery("from Department d where d.uniqueId = :id").setLong("id", deptId).setCacheable(true).uniqueResult();
			HashMap<PositionType, PositionHours> positionClassHours = new HashMap<PositionType, PositionHours>();
			List<PitClass> pitClassesForDept = findAllPitClassesWithContactHoursForDepartment(pointInTimeData, d, hibSession);
			for(PitClass pc : pitClassesForDept) {
				if (pc.getPitClassInstructors() == null || pc.getPitClassInstructors().isEmpty()) {
					PositionHours ph = positionClassHours.get(null);
					if (ph == null) {
						ph = new PositionHours(null, getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm());
						positionClassHours.put(null, ph);
					}
					ph.addClassHours(null, pc);
				} else {
					int numInstructors = 0;
					for(PitClassInstructor pci : pc.getPitClassInstructors()) {
						PositionHours ph = positionClassHours.get(pci.getPitDepartmentalInstructor().getPositionType());
						if (ph == null) {
							ph = new PositionHours(pci.getPitDepartmentalInstructor().getPositionType(), getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm());
							positionClassHours.put(pci.getPitDepartmentalInstructor().getPositionType(), ph);
						}
						if(ph.addClassHours(pci, pc)) {
							numInstructors++;
						}
					}
					if (numInstructors == 0){
						PositionHours ph = positionClassHours.get(null);
						if (ph == null) {
							ph = new PositionHours(null, getStandardMinutesInReportingHour(), getStandardWeeksInReportingTerm());
							positionClassHours.put(null, ph);
						}
						ph.addClassHours(null, pc);
					}

				}
			}	
			for(PositionType pt : positionClassHours.keySet()) {
				PositionHours ph = positionClassHours.get(pt);
				ArrayList<String> row = new ArrayList<String>();
				row.add(d.getDeptCode());
				row.add(d.getAbbreviation());
				row.add(d.getName());
				row.add(pt == null? MSG.labelUnknown() : pt.getLabel());
				row.add(Float.toString(ph.getOrganizedWeeklyClassHours()));
				row.add(Float.toString(ph.getNotOrganizedWeeklyClassHours()));
				row.add(Float.toString(ph.getWeeklyClassHours()));
				row.add(Float.toString(ph.getOrganizedWeeklyStudentClassHours()));
				row.add(Float.toString(ph.getNotOrganizedWeeklyStudentClassHours()));
				row.add(Float.toString(ph.getWeeklyStudentClassHours()));
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

	private class PositionHours {
		private float iWeeklyClassHours = 0.0f;
		private float iOrganizedWeeklyClassHours = 0.0f;
		private float iNotOrganizedWeeklyClassHours = 0.0f;
		private float iWeeklyStudentClassHours = 0.0f;
		private float iOrganizedWeeklyStudentClassHours = 0.0f;
		private float iNotOrganizedWeeklyStudentClassHours = 0.0f;
		private PositionType iPositionType;
		private Float iStandardMinutesInReportingHour;
		private Float iStandardWeeksInReportingTerm;
		
		public PositionType getPositionType() {
			return iPositionType;
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

		public PositionHours(PositionType positionType, Float standardMinutesInReportingHour, Float standardWeeksInReportingTerm) {
			this.iPositionType = positionType;
			this.iStandardMinutesInReportingHour = standardMinutesInReportingHour;
			this.iStandardWeeksInReportingTerm = standardWeeksInReportingTerm;
		}
		
		public boolean addClassHours(PitClassInstructor pitClassInstructor, PitClass pitClass) {
			if (pitClassInstructor == null){
				if (this.iPositionType == null) {
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
				if (pitClassInstructor.getPitDepartmentalInstructor().getPositionType() == null) {
					if (this.iPositionType == null) {
						updateHours(pitClass, pitClassInstructor.getNormalizedPercentShare());
						return(true);
					}				
					return(false);
				} else if (pitClassInstructor.getPitDepartmentalInstructor().getPositionType().equals(getPositionType())) {
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
