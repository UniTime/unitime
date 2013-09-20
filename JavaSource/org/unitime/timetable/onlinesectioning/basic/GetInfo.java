/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.basic;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.sf.cpsolver.studentsct.extension.DistanceConflict;
import net.sf.cpsolver.studentsct.extension.TimeOverlapsCounter;
import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.FreeTimeRequest;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;
import net.sf.cpsolver.studentsct.model.Subpart;

import org.unitime.timetable.onlinesectioning.CourseInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.CourseInfoMatcher;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.StudentMatcher;
import org.unitime.timetable.onlinesectioning.solver.StudentSchedulingAssistantWeights;

public class GetInfo implements OnlineSectioningAction<Map<String, String>>{
	private static final long serialVersionUID = 1L;

	@Override
	public Map<String, String> execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Map<String, String> info = new HashMap<String, String>();
		Lock lock = server.readLock();
		try {
			DecimalFormat df = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
			StudentSchedulingAssistantWeights w = new StudentSchedulingAssistantWeights(server.getConfig());
			DistanceConflict dc = new DistanceConflict(server.getDistanceMetric(), server.getConfig());
			TimeOverlapsCounter toc = new TimeOverlapsCounter(null, server.getConfig());
			
			int nrVars = 0, assgnVars = 0, nrStud = 0, compStud = 0, dist = 0, overlap = 0, free = 0;
			double value = 0.0;
			for (Student student: server.findStudents(new StudentMatcher() {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean match(Student student) {
					return true;
				}
			})) {
				boolean complete = true;
				for (Request request: student.getRequests()) {
					if (request instanceof FreeTimeRequest) continue;
					if (request.getAssignment() != null) {
						assgnVars ++; nrVars ++;
						value += w.getWeight(request.getAssignment());
					} else if (student.canAssign(request)) {
						nrVars ++; complete = false;
					}
				}
				nrStud ++;
				if (complete) compStud ++;
				for (int i = 0; i < student.getRequests().size() - 1; i++) {
					Enrollment e1 = student.getRequests().get(i).getAssignment();
					if (e1 == null || !e1.isCourseRequest()) continue;
					dist += dc.nrConflicts(e1);
					free += toc.nrFreeTimeConflicts(e1);
					for (int j = i + 1; j < student.getRequests().size(); j++) {
						Request r2 = student.getRequests().get(j);
						if (r2 instanceof FreeTimeRequest) continue;
						Enrollment e2 = r2.getAssignment();
						if (e2 == null) continue;
						dist += dc.nrConflicts(e1, e2);
						overlap += toc.nrConflicts(e1, e2);
					}
				}
			}
			info.put("Assigned variables", df.format(100.0 * assgnVars / nrVars) + "% (" + nrVars + "/" + assgnVars + ")");
			info.put("Overall solution value", df.format(value));
			info.put("Students with complete schedule", df.format(100.0 * compStud / nrStud) + "% (" + compStud + "/" + nrStud + ")");
			info.put("Student distance conflicts", df.format(1.0 * dist / nrStud) + " (" + dist + ")");
            info.put("Time overlapping conflicts", df.format(overlap / 12.0 / nrStud) + "h (" + overlap + ")");
            info.put("Free time overlapping conflicts", df.format(free / 12.0 / nrStud) + "h (" + free + ")");
        
	        double disbWeight = 0;
	        int disbSections = 0;
	        int disb10Sections = 0;
	        Set<Long> offerings = new HashSet<Long>();
	        for (CourseInfo ci: server.findCourses(new CourseInfoMatcher() {
				private static final long serialVersionUID = 1L;

				@Override
				public boolean match(CourseInfo course) {
					return true;
				}
			})) {
	        	Course course = server.getCourse(ci.getUniqueId());
	        	if (offerings.add(course.getOffering().getId())) {
		            for (Config config: course.getOffering().getConfigs()) {
		                double enrl = config.getEnrollments().size();
		                for (Subpart subpart: config.getSubparts()) {
		                    if (subpart.getSections().size() <= 1) continue;
		                    if (subpart.getLimit() > 0) {
		                        // sections have limits -> desired size is section limit x (total enrollment / total limit)
		                        double ratio = enrl / subpart.getLimit();
		                        for (Section section: subpart.getSections()) {
		                            double desired = ratio * section.getLimit();
		                            disbWeight += Math.abs(section.getEnrollments().size() - desired);
		                            disbSections ++;
		                            if (Math.abs(desired - section.getEnrollments().size()) >= Math.max(1.0, 0.1 * section.getLimit()))
		                                disb10Sections++;
		                        }
		                    } else {
		                        // unlimited sections -> desired size is total enrollment / number of sections
		                        for (Section section: subpart.getSections()) {
		                            double desired = enrl / subpart.getSections().size();
		                            disbWeight += Math.abs(section.getEnrollments().size() - desired);
		                            disbSections ++;
		                            if (Math.abs(desired - section.getEnrollments().size()) >= Math.max(1.0, 0.1 * desired))
		                                disb10Sections++;
		                        }
		                    }
		                }
		            }
		        }
		        if (disbSections != 0) {
		            info.put("Average disbalance", df.format(disbWeight / disbSections) + " (" + df.format(assgnVars == 0 ? 0.0 : 100.0 * disbWeight / assgnVars) + "%)");
		            info.put("Sections disbalanced by 10% or more", disb10Sections + " (" + df.format(disbSections == 0 ? 0.0 : 100.0 * disb10Sections / disbSections) + "%)");
		        }	        		
	        	}
		} finally {
			lock.release();
		}
		return info;		
	}
	
	@Override
	public String name() {
		return "info";
	}

}
