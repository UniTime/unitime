/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.onlinesectioning.reports;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Action;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Enrollment;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Enrollment.EnrollmentType;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Section;

public class SchedulinAssistantReport implements OnlineSectioningReport.Report {

	@Override
	public String getYear() {
		return System.getProperty("year", "2012");
	}

	@Override
	public String getTerm() {
		return System.getProperty("term", "Fall");
	}

	@Override
	public String getCampus() {
		return System.getProperty("campus", "PWL");
	}

	@Override
	public File getReportFolder() {
		return new File(System.getProperty("user.home", "~"));
	}

	@Override
	public String[] getOperations() {
		return new String[] { "section", "suggestions", "reload-student" };
	}
	
	public double diff(Enrollment e1, Enrollment e2) {
		if (e1.getSectionCount() == 0)
			return e2.getSectionCount() == 0 ? 1.0 : 0.0;
		double cnt = 0.0;
		for (Section s1: e1.getSectionList()) {
			boolean match = false;
			for (Section s2: e2.getSectionList()) {
				if (s1.getClazz().getExternalId().equals(s2.getClazz().getExternalId())) {
					match = true; break;
				}
			}
			if (match) cnt += 1.0; 
		}
		return cnt / Math.max(e1.getSectionCount(), e2.getSectionCount());
	}
	
	public double diff(List<Enrollment> computed, Enrollment saved) {
		double diff = 0.0;
		for (Enrollment e: computed)
			diff = Math.max(diff, diff(e, saved));
		return diff;
	}
	
	public double diff(Enrollment e1, Set<Long> e2) {
		if (e1.getSectionCount() == 0)
			return e2.size() == 0 ? 1.0 : 0.0;
		double cnt = 0.0;
		for (Section s1: e1.getSectionList()) {
			if (e2.contains(s1.getClazz().getUniqueId())) cnt ++;
		}
		return cnt / Math.max(e1.getSectionCount(), e2.size());
	}
	
	public double diff(List<Enrollment> computed, Set<Long> enrollment) {
		double diff = 0.0;
		for (Enrollment e: computed)
			diff = Math.max(diff, diff(e, enrollment));
		return diff;
	}
	
	private double minutes(Action a1, Action a2) {
		return Math.abs(a1.getStartTime() - a2.getStartTime()) / 60000.0;
	}

	@Override
	public void process(OnlineSectioningReport report, String student, List<Action> actions) {
		boolean hasSection = false;
		for (Action action: actions) {
			if ("section".equals(action.getOperation())) { hasSection = true; break; }
		}
		if (!hasSection) return;
		
		long done = Math.round(report.inc("Students", 1.0));
		
		Action firstSection = null;
		Action lastSectionOrSuggestion = null;
		int nrSections = 0;
		int nrSuggestions = 0;
		int nrCycles = 0;
		
		Set<Long> enrollment = new HashSet<Long>();
		for (Number classId: (List<Number>)StudentDAO.getInstance().getSession().createQuery(
				"select e.clazz.uniqueId from StudentClassEnrollment e where e.student.uniqueId = :studentId")
				.setLong("studentId", actions.get(0).getStudent().getUniqueId()).list())
			enrollment.add(classId.longValue());
		
		List<Enrollment> last = new ArrayList<Enrollment>();
		List<Enrollment> all = new ArrayList<Enrollment>();
		Enrollment first = null;
		Enrollment lastBanner = null;
		
		Action previous = null;
		for (Action action: actions) {
			if (previous != null && minutes(previous, action) > 120.0) {
				firstSection = null; lastSectionOrSuggestion = null;
				nrSections = 0; nrSuggestions = 0; first = null; last.clear();
			}
			previous = action;
			if ("section".equals(action.getOperation())) {
				Enrollment computed = null;
				for (Enrollment e: action.getEnrollmentList()) {
					if (e.getType() == EnrollmentType.COMPUTED && e.getSectionCount() > 0) { computed = e; break; }
				}
				if (computed == null) continue;
				if (first == null) first = computed;
				last.clear(); last.add(computed); all.add(computed);
				nrSections ++; lastSectionOrSuggestion = action;
				if (firstSection == null) firstSection = action;
			}
			if ("suggestions".equals(action.getOperation())) {
				Enrollment computed = null;
				for (Enrollment e: action.getEnrollmentList()) {
					if (e.getType() == EnrollmentType.COMPUTED && e.getSectionCount() > 0) {
						computed = e; 
						last.add(e); all.add(e);
					}
				}
				if (computed == null) continue;
				nrSuggestions ++; lastSectionOrSuggestion = action;
			}
			if ("reload-student".equals(action.getOperation())) {
				if (firstSection == null) continue;
				Enrollment stored = null;
				for (Enrollment e: action.getEnrollmentList()) {
					if (e.getType() == EnrollmentType.STORED && e.getSectionCount() > 0) {
						stored = e; break;
					}
				}
				if (stored == null) continue;
				lastBanner = stored;
				report.inc("Sections per cycle", nrSections);
				report.inc("Suggestions per cycle", nrSuggestions);
				double assistantTime = minutes(lastSectionOrSuggestion, firstSection);
				double assistantDiff = diff(last, first);
				if (assistantDiff > 0.0) {
					report.inc("Assistant time [min]", assistantTime);
					report.inc("Difference [first - last]", assistantDiff);
				}
				double bannerTime = minutes(action, lastSectionOrSuggestion);
				double bannerDiff = diff(last, stored);
				if (bannerDiff > 0.0) {
					report.inc("Banner time [min]", bannerTime);
					report.inc("Difference [last - Banner]", bannerDiff);
				}
				double bannerDiff2 = diff(all, stored);
				if (bannerDiff2 > 0.0)
					report.inc("Difference [all - Banner]", bannerDiff2);
				firstSection = null; lastSectionOrSuggestion = null; first = null; last.clear();
				nrSections = 0; nrSuggestions = 0; nrCycles++;
			}
		}
		report.inc("Cycles per student", nrCycles);
		if (!all.isEmpty() && !enrollment.isEmpty()) {
			report.inc("Difference [last - enrollment]", diff(all, enrollment));
			if (nrCycles > 0)
				report.inc("Difference [last - enrollment] (cycles > 0)", diff(all, enrollment));
		}
		if (lastBanner != null && !all.isEmpty()) {
			report.inc("Difference [all - last Banner]", diff(all, lastBanner));
		}
		
		if ((done % 1) == 0) {
			OnlineSectioningReport.sLog.info("---- after " + done + " students");
			for (String name: new TreeSet<String>(report.iCounters.keySet())) {
				OnlineSectioningReport.sLog.info(name + ": " + report.iCounters.get(name));
			}			
		}
		
	}
	
	public static void main(String[] args) {
		try {
			new OnlineSectioningReport(new SchedulinAssistantReport()).run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


}
