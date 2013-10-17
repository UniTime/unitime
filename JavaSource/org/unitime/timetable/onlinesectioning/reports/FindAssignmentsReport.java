/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
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
import java.util.HashSet;
import java.util.List;
import java.util.TreeSet;

import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Action;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Enrollment;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Entity;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Message;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Request;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog.Section;
import org.unitime.timetable.onlinesectioning.reports.OnlineSectioningReport.Counter;

/**
 * @author Tomas Muller
 */
public class FindAssignmentsReport implements OnlineSectioningReport.Report {

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
		return new String[] { "section" };
	}

	@Override
	public void process(OnlineSectioningReport report, String student, List<Action> actions) {
		TreeSet<String> courses = new TreeSet<String>();
		int idx = 0;
		Counter cpu = new Counter();
		Counter err = new Counter();
		Counter req = new Counter();
		Counter rft = new Counter();
		Counter asgn = new Counter();
		Counter asft = new Counter();
		Counter val = new Counter();
		boolean useft = false;
		boolean usealt = false;
		for (Action action: actions) {
			cpu.inc(action.getCpuTime() / 1e6);
			report.inc("CPU Time", action.getCpuTime() / 1e9);
			for (Message message: action.getMessageList()) {
				if (message.getLevel() == Message.Level.ERROR || message.getLevel() == Message.Level.FATAL) {
					report.inc("Error " + message.getText(), 1);
					err.inc(1);
				}
			}
			Enrollment computed = null;
			for (Enrollment enrollment: action.getEnrollmentList()) {
				if (enrollment.getType() == Enrollment.EnrollmentType.COMPUTED) {
					computed = enrollment; break;
				}
			}
			HashSet<String> assigned = new HashSet<String>();
			
			int ac = 0, aft = 0;
			if (computed != null) {
				if (computed.hasValue()) {
					report.inc("Value", computed.getValue());
					val.inc(computed.getValue());
				}
				for (Section section: computed.getSectionList()) {
					if (section.hasCourse() && section.getCourse().hasName()) {
						if (assigned.add(section.getCourse().getName())) {
							report.inc("Courses", section.getCourse().getName(), "Assigned", 1);
							ac ++;
							int priority = 0; boolean alt = false;
							requests: for (Request request: action.getRequestList())
								for (Entity course: request.getCourseList())
									if (course.getUniqueId() == section.getCourse().getUniqueId()) {
										priority = request.getPriority();
										alt = request.getAlternative();
										break requests;
									}
							report.inc("Courses", section.getCourse().getName(), "Priority", priority);
							report.inc("Courses", section.getCourse().getName(), "Alternative", alt ? 1 : 0);
						}
					} else {
						aft ++;
					}
				}
				asgn.inc(ac);
				asft.inc(aft);
			}
			
			int rc = 0;
			int ft = 0;
			for (Request request: action.getRequestList()) {
				if (!request.getAlternative() && request.getCourseCount() > 0) rc ++;
				if (request.getAlternative() || request.getCourseCount() > 1) usealt = true;
				for (Entity course: request.getCourseList())
					if (course.hasName()) {
						courses.add(course.getName());
						report.inc("Courses", course.getName(), "Requested", 1);
						if (idx == 0)
							report.inc("Courses", course.getName(), "First", 1);
						if (idx == actions.size() - 1)
							report.inc("Courses", course.getName(), "Last", 1);
					}
				if (request.getFreeTimeCount() > 0)
					ft++;
			}
			req.inc(rc);
			rft.inc(ft);
			idx ++;
			report.inc("[1] Courses", rc);
			report.inc("[1] Assigned Courses", ac);
			report.inc("[1] Free Times", ft);
			report.inc("[1] Assigned Free Times", aft);
			report.inc("[1] Complete Schedule", (rc == ac ? 1 : 0));
			report.inc("[1] Complete Schedule Including Free Times", (rc == ac && ft == aft ? 1 : 0));
			report.inc("[1] Has Free Times", (ft > 0 ? 1 : 0));
			if (ft > 0) useft = true;
			
		}
		for (String course: courses) {
			report.inc("Courses", course, "Student", 1);
		}
		report.inc("Students", student, "Avg. CPU", cpu.avg());
		report.inc("Students", student, "Calls", cpu.count());
		report.inc("Students", student, "Errors", err.count());
		report.inc("Students", student, "Requests [Course]", req.avg());
		report.inc("Students", student, "Requests [Free Time]", rft.avg());
		report.inc("Students", student, "Assigned [Course]", asgn.avg());
		report.inc("Students", student, "Assigned [Free Time]", asft.avg());
		report.inc("Students", student, "Value", val.avg());
		report.inc("Courses", req.avg());
		report.inc("Free Times", rft.avg());
		report.inc("Assigned Course", asgn.avg());
		report.inc("Assigned Free Time", asft.avg());
		report.inc("Students Using Free Times", (useft ? 1 : 0));
		report.inc("Students Using Alternatives", (usealt ? 1 : 0));
		
		long done = Math.round(report.inc("Students", 1.0));
		if ((done % 100) == 0) {
			OnlineSectioningReport.sLog.info("---- after " + done + " students");
			for (String name: new TreeSet<String>(report.iCounters.keySet())) {
				OnlineSectioningReport.sLog.info(name + ": " + report.iCounters.get(name));
			}			
		}
	}
	
	public static void main(String[] args) {
		try {
			new OnlineSectioningReport(new FindAssignmentsReport()).run();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
