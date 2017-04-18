package org.unitime.timetable.reports.pointintimedata;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.PitClass;
import org.unitime.timetable.model.PointInTimeData;

public abstract class WSCHByDepartment extends BasePointInTimeDataReports {

	public WSCHByDepartment() {
		super();
	}
	
	@Override
	protected void intializeHeader() {
		ArrayList<String> hdr = new ArrayList<String>();
		hdr.add(MSG.columnDepartmentCode());
		hdr.add(MSG.columnDepartmentAbbreviation());
		hdr.add(MSG.columnDepartmentName());
		hdr.add(MSG.columnWeeklyClassHours());
		hdr.add(MSG.columnWeeklyStudentClassHours());
		setHeader(hdr);
	}
	
	protected abstract float weeklyClassHours(PitClass pitClass);
	protected abstract float weeklyStudentClassHours(PitClass pitClass);

	public void createWeeklyStudentContactHoursByDepartmentReportFor(PointInTimeData pointInTimeData, Session hibSession) {
		for(Department d : pointInTimeData.getSession().getDepartments()) {
			float deptTotalWsch = 0;
			float deptTotalWch = 0;
			List<PitClass> pitClassesForDept = findAllPitClassesWithContactHoursForDepartment(pointInTimeData, d, hibSession);
			for(PitClass pc : pitClassesForDept) {
				deptTotalWch += weeklyClassHours(pc);
				deptTotalWsch += weeklyStudentClassHours(pc);
			}
			if (!d.getSubjectAreas().isEmpty()) {
				ArrayList<String> row = new ArrayList<String>();
				row.add(d.getDeptCode());
				row.add(d.getAbbreviation());
				row.add(d.getName());
				row.add(Float.toString(deptTotalWch));
				row.add(Float.toString(deptTotalWsch));
				addDataRow(row);
			}
		}	
	}

	@Override
	protected void runReport(org.hibernate.Session hibSession) {
		PointInTimeData pitd = (PointInTimeData)hibSession
				.createQuery("from PointInTimeData pitd where pitd.uniqueId = :uid")
				.setLong("uid", getPointInTimeDataUniqueId().longValue())
				.uniqueResult();
		createWeeklyStudentContactHoursByDepartmentReportFor(pitd, hibSession);
		
	}

}