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
package org.unitime.timetable.server.sectioning;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.Offering;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Request.RequestPriority;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.report.AbstractStudentSectioningReport;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.model.AdvisorCourseRequest;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.advisors.AdvisorGetCourseRequests;

public class CourseRequestAltStats extends AbstractStudentSectioningReport {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	DecimalFormat df = new DecimalFormat("#,##0.00");
	DecimalFormat df2 = new DecimalFormat("#,##0");

	public CourseRequestAltStats(StudentSectioningModel model) {
        super(model);
    }
	
	private String percent(int value, int base) {
		if (base == 0) return "";
		return df.format(100.0 * value / base) + " %";
	}

	@Override
	public CSVFile createTable(Assignment<Request, Enrollment> assignment, DataProperties properties) {
		Map<String, Check> counters = new HashMap<String, Check>();
		Check total = new Check();
		for (Student student: getModel().getStudents()) {
			String area = null;
			if (student.getPrimaryMajor() != null)
				area = student.getPrimaryMajor().getArea();
			if (area == null) area = "Unkown";
			if (!matches(student)) continue;
			Check check = new Check();
			Set<String> assignedCourses = new HashSet<String>(); 
			for (Request r: student.getRequests()) {
				Enrollment e = assignment.getValue(r);
				if (!(r instanceof CourseRequest)) continue;
				if (e != null) assignedCourses.add(e.getCourse().getName());
				if (!matches(r, e)) continue;
				CourseRequest cr = (CourseRequest)r;
				if (cr.isAlternative()) {
					check.set("subst");
					if (e != null) check.set("subst-assigned");
					if (e != null) check.set("req-assigned", cr.getRequestPriority());
				} else {
					check.set("req", cr.getRequestPriority());
					if (e != null) check.set("req-assigned", cr.getRequestPriority());
					if (!cr.isWaitlist()) check.set("subst-allow");
					if (cr.getCourses().size() > 1) {
						check.set("alt", cr.getRequestPriority());
						if (e != null) {
							check.set("alt-assigned", cr.getRequestPriority());
							if (cr.getCourses().get(0).equals(e.getCourse()))
								check.set("alt-assigned-1st", cr.getRequestPriority());
						}
					} else {
						check.set("no-alt", cr.getRequestPriority());
						if (e != null)
							check.set("no-alt-assigned", cr.getRequestPriority());
					}
				}
			}
			if (!check.is("req")) continue;
			
			org.unitime.timetable.model.Student dbStudent = StudentDAO.getInstance().get(student.getId());
			if (dbStudent == null && student.getExternalId() != null) {
				dbStudent = org.unitime.timetable.model.Student.findByExternalId(ApplicationProperties.getSessionId(), student.getExternalId());
			}
			if (dbStudent != null && !dbStudent.getAdvisorCourseRequests().isEmpty()) {
				CourseRequestInterface acr = new CourseRequestInterface();
				List<AdvisorCourseRequest> recommendations = new ArrayList<AdvisorCourseRequest>(dbStudent.getAdvisorCourseRequests());
				Collections.sort(recommendations);
				AdvisorGetCourseRequests.fillCourseRequests(acr, recommendations);
				check.set("advised");
				for (org.unitime.timetable.gwt.shared.CourseRequestInterface.Request r: acr.getCourses()) {
					if (!matches(student, r)) continue;
					if (r.hasRequestedCourse()) {
						CourseDemand.Critical critical = CourseDemand.Critical.NORMAL;
						if (r.hasCritical())
							critical = CourseDemand.Critical.values()[r.getCritical()];
						RequestPriority rp = critical.toRequestPriority();
						int index = 0;
						for (RequestedCourse rc: r.getRequestedCourse()) {
							if (rc.hasCourseName() && index == 0 && rc.hasCourseId()) {
								check.set("adv-req", rp);
								if (r.countRequestedCourses() == 1) {
									check.set("adv-no-alt", rp);
								} else {
									check.set("adv-alt", rp);
								}
								if (!r.isNoSub()) check.set("adv-subst-allow");
							}
							if (rc.hasCourseName() && assignedCourses.contains(rc.getCourseName()) && rc.hasCourseId()) {
								check.set("adv-req-assigned", rp);
								if (r.countRequestedCourses() == 1)
									check.set("adv-no-alt-assigned", rp);
								else {
									check.set("adv-alt-assigned", rp);
									if (index == 0)
										check.set("adv-alt-assigned-1st", rp);
								}
							}
							index ++;
						}
					}
				}
				for (org.unitime.timetable.gwt.shared.CourseRequestInterface.Request r: acr.getAlternatives()) {
					if (!matches(student, r)) continue;
					if (r.hasRequestedCourse()) {
						int index = 0;
						for (RequestedCourse rc: r.getRequestedCourse()) {
							if (rc.hasCourseName() && index == 0 && rc.hasCourseId()) {
								check.set("adv-subst");
							}
							if (rc.hasCourseName() && assignedCourses.contains(rc.getCourseName()) && rc.hasCourseId()) {
								check.set("adv-subst-assigned");
								check.set("adv-req-assigned", RequestPriority.Normal);
								
							}
							index ++;
						}
					}
				}
			}
			
			Check counter = counters.get(area);
			if (counter == null) {
				counter = new Check();
				counters.put(area, counter);
			}
			counter.inc("students");
			if (check.is("subst") && check.is("subst-allow")) {
				counter.inc("subst");
				if (check.is("subst-assigned"))
					counter.inc("subst-assigned");
			}
			counter.inc("req", check);
			counter.inc("req-assigned", check);
			counter.inc("alt", check);
			counter.inc("alt-assigned", check);
			counter.inc("alt-assigned-1st", check);
			counter.inc("no-alt", check);
			counter.inc("no-alt-assigned", check);
			
			total.inc("students");
			if (check.is("subst") && check.is("subst-allow")) {
				total.inc("subst");
				if (check.is("subst-assigned"))
					total.inc("subst-assigned");
			}
			total.inc("req", check);
			total.inc("req-assigned", check);
			total.inc("alt", check);
			total.inc("alt-assigned", check);
			total.inc("alt-assigned-1st", check);
			total.inc("no-alt", check);
			total.inc("no-alt-assigned", check);
			
			if (check.is("advised")) {
				counter.inc("advised");
				if (check.is("adv-subst") && check.is("adv-subst-allow")) {
					counter.inc("adv-subst");
					if (check.is("adv-subst-assigned"))
						counter.inc("adv-subst-assigned");
				}
				counter.inc("adv-req", check);
				counter.inc("adv-req-assigned", check);
				counter.inc("adv-alt", check);
				counter.inc("adv-alt-assigned", check);
				counter.inc("adv-alt-assigned-1st", check);
				counter.inc("adv-no-alt", check);
				counter.inc("adv-no-alt-assigned", check);	
				
				total.inc("advised");
				if (check.is("adv-subst") && check.is("adv-subst-allow")) {
					total.inc("adv-subst");
					if (check.is("adv-subst-assigned"))
						total.inc("adv-subst-assigned");
				}
				total.inc("adv-req", check);
				total.inc("adv-req-assigned", check);
				total.inc("adv-alt", check);
				total.inc("adv-alt-assigned", check);
				total.inc("adv-alt-assigned-1st", check);
				total.inc("adv-no-alt", check);
				total.inc("adv-no-alt-assigned", check);	
			}

		}
		
		CSVFile csv = new CSVFile();
        csv.setHeader(new CSVFile.CSVField[] {
                new CSVFile.CSVField(MSG.colArea()),
                new CSVFile.CSVField("Students"),
                
                new CSVFile.CSVField("Course\nRequests"),
                new CSVFile.CSVField("Assigned\nCourses"),
                new CSVFile.CSVField("Assigned\nCourses [%]"),
                new CSVFile.CSVField("Requests\nNo Alternatives"),
                new CSVFile.CSVField("No Alts\nof Courses [%]"),
                new CSVFile.CSVField("Assigned\nNo Alts"),
                new CSVFile.CSVField("Assigned\nNo Alts [%]"),
                new CSVFile.CSVField("Requests\nw/Alternatives"),
                new CSVFile.CSVField("With Alts\nof Courses [%]"),
                new CSVFile.CSVField("Assigned\nw/Alternatives"),
                new CSVFile.CSVField("Assigned\nw/Alternatives [%]"),
                new CSVFile.CSVField("Assigned\n1st Choice"),
                new CSVFile.CSVField("Assigned\n1st Choice [%]"),
                new CSVFile.CSVField("Students\nw/Substitutes"),
                new CSVFile.CSVField("Students\nw/Substitutes [%]"),
                new CSVFile.CSVField("Assigned\nSubstitute"),
                new CSVFile.CSVField("Assigned\nSubstitute [%]"),
                
                new CSVFile.CSVField("Advised\nStudents"),
                new CSVFile.CSVField("Advised\nStudents [%]"),
                new CSVFile.CSVField("Course\nRecommendations"),
                new CSVFile.CSVField("Assigned\nRecommendations"),
                new CSVFile.CSVField("Assigned\nRecommendations [%]"),
                new CSVFile.CSVField("Recommendations\nNo Alternatives"),
                new CSVFile.CSVField("No Alts\nof Recommendations [%]"),
                new CSVFile.CSVField("Assigned\nNo Alts"),
                new CSVFile.CSVField("Assigned\nNo Alts [%]"),
                new CSVFile.CSVField("Recommendations\nw/Alternatives"),
                new CSVFile.CSVField("With Alts\nof Recommendations [%]"),
                new CSVFile.CSVField("Assigned\nw/Alternatives"),
                new CSVFile.CSVField("Assigned\nw/Alternatives [%]"),
                new CSVFile.CSVField("Assigned\n1st Choice"),
                new CSVFile.CSVField("Assigned\n1st Choice [%]"),
                new CSVFile.CSVField("Recommended\nw/Substitutes"),
                new CSVFile.CSVField("Recommended\nw/Substitutes [%]"),
                new CSVFile.CSVField("Assigned\nSubstitute"),
                new CSVFile.CSVField("Assigned\nSubstitute [%]"),
                
                new CSVFile.CSVField("Vital\nCourse\nRequests"),
                new CSVFile.CSVField("Assigned\nVital\nCourses"),
                new CSVFile.CSVField("Assigned\nVital\nCourses [%]"),
                new CSVFile.CSVField("Requests\nVital\nNo Alternatives"),
                new CSVFile.CSVField("Vital\nNo Alts\nof Courses [%]"),
                new CSVFile.CSVField("Assigned\nVital\nNo Alts"),
                new CSVFile.CSVField("Assigned\nVital\nNo Alts [%]"),
                new CSVFile.CSVField("Vital\nRequests\nw/Alternatives"),
                new CSVFile.CSVField("Vital\nWith Alts\nof Courses [%]"),
                new CSVFile.CSVField("Assigned\nVital\nw/Alternatives"),
                new CSVFile.CSVField("Assigned\nVital\nw/Alternatives [%]"),
                new CSVFile.CSVField("Assigned\nVital\n1st Choice"),
                new CSVFile.CSVField("Assigned\nVital\n1st Choice [%]"),
                
                new CSVFile.CSVField("Vital\nCourse\nRecommendations"),
                new CSVFile.CSVField("Assigned\nVital\nRecommendations"),
                new CSVFile.CSVField("Assigned\nVital\nRecommendations [%]"),
                new CSVFile.CSVField("Vital\nRecommendations\nNo Alternatives"),
                new CSVFile.CSVField("Vital\nNo Alts\nof Recommendations [%]"),
                new CSVFile.CSVField("Assigned\nVital\nNo Alts"),
                new CSVFile.CSVField("Assigned\nVital\nNo Alts [%]"),
                new CSVFile.CSVField("Vital\nRecommendations\nw/Alternatives"),
                new CSVFile.CSVField("Vital\nWith Alts\nof Recommendations [%]"),
                new CSVFile.CSVField("Assigned\nVital\nw/Alternatives"),
                new CSVFile.CSVField("Assigned\nVital\nw/Alternatives [%]"),
                new CSVFile.CSVField("Assigned\nVital\n1st Choice"),
                new CSVFile.CSVField("Assigned\nVital\n1st Choice [%]"),
                });

        TreeSet<String> areas = new TreeSet<String>(counters.keySet());
        for (String area: areas) {
        	Check counter = counters.get(area);
        	csv.addLine(new CSVFile.CSVField[] {
        			new CSVFile.CSVField(area),
        			new CSVFile.CSVField(df2.format(counter.get("students"))),

        			new CSVFile.CSVField(df2.format(counter.get("req"))),
        			new CSVFile.CSVField(df2.format(counter.get("req-assigned"))),
        			new CSVFile.CSVField(percent(counter.get("req-assigned"), counter.get("req"))),
        			new CSVFile.CSVField(df2.format(counter.get("no-alt"))),
        			new CSVFile.CSVField(percent(counter.get("no-alt"), counter.get("req"))),
        			new CSVFile.CSVField(df2.format(counter.get("no-alt-assigned"))),
        			new CSVFile.CSVField(percent(counter.get("no-alt-assigned"), counter.get("no-alt"))),
        			new CSVFile.CSVField(df2.format(counter.get("alt"))),
        			new CSVFile.CSVField(percent(counter.get("alt"), counter.get("req"))),
        			new CSVFile.CSVField(df2.format(counter.get("alt-assigned"))),
        			new CSVFile.CSVField(percent(counter.get("alt-assigned"), counter.get("alt"))),
        			new CSVFile.CSVField(df2.format(counter.get("alt-assigned-1st"))),
        			new CSVFile.CSVField(percent(counter.get("alt-assigned-1st"), counter.get("alt-assigned"))),
        			new CSVFile.CSVField(df2.format(counter.get("subst"))),
        			new CSVFile.CSVField(percent(counter.get("subst"), counter.get("students"))),
        			new CSVFile.CSVField(df2.format(counter.get("subst-assigned"))),
        			new CSVFile.CSVField(percent(counter.get("subst-assigned"), counter.get("subst"))),
        			
        			new CSVFile.CSVField(df2.format(counter.get("advised"))),
        			new CSVFile.CSVField(percent(counter.get("advised"), counter.get("students"))),
        			new CSVFile.CSVField(df2.format(counter.get("adv-req"))),
        			new CSVFile.CSVField(df2.format(counter.get("adv-req-assigned"))),
        			new CSVFile.CSVField(percent(counter.get("adv-req-assigned"), counter.get("adv-req"))),
        			new CSVFile.CSVField(df2.format(counter.get("adv-no-alt"))),
        			new CSVFile.CSVField(percent(counter.get("adv-no-alt"), counter.get("adv-req"))),
        			new CSVFile.CSVField(df2.format(counter.get("adv-no-alt-assigned"))),
        			new CSVFile.CSVField(percent(counter.get("adv-no-alt-assigned"), counter.get("adv-no-alt"))),
        			new CSVFile.CSVField(df2.format(counter.get("adv-alt"))),
        			new CSVFile.CSVField(percent(counter.get("adv-alt"), counter.get("adv-req"))),
        			new CSVFile.CSVField(df2.format(counter.get("adv-alt-assigned"))),
        			new CSVFile.CSVField(percent(counter.get("adv-alt-assigned"), counter.get("adv-alt"))),
        			new CSVFile.CSVField(df2.format(counter.get("adv-alt-assigned-1st"))),
        			new CSVFile.CSVField(percent(counter.get("adv-alt-assigned-1st"), counter.get("adv-alt-assigned"))),
        			new CSVFile.CSVField(df2.format(counter.get("adv-subst"))),
        			new CSVFile.CSVField(percent(counter.get("adv-subst"), counter.get("advised"))),
        			new CSVFile.CSVField(df2.format(counter.get("adv-subst-assigned"))),
        			new CSVFile.CSVField(percent(counter.get("adv-subst-assigned"), counter.get("adv-subst"))),
        			
        			new CSVFile.CSVField(df2.format(counter.get("req", RequestPriority.Vital))),
        			new CSVFile.CSVField(df2.format(counter.get("req-assigned", RequestPriority.Vital))),
        			new CSVFile.CSVField(percent(counter.get("req-assigned", RequestPriority.Vital), counter.get("req", RequestPriority.Vital))),
        			new CSVFile.CSVField(df2.format(counter.get("no-alt", RequestPriority.Vital))),
        			new CSVFile.CSVField(percent(counter.get("no-alt", RequestPriority.Vital), counter.get("req", RequestPriority.Vital))),
        			new CSVFile.CSVField(df2.format(counter.get("no-alt-assigned", RequestPriority.Vital))),
        			new CSVFile.CSVField(percent(counter.get("no-alt-assigned", RequestPriority.Vital), counter.get("no-alt", RequestPriority.Vital))),
        			new CSVFile.CSVField(df2.format(counter.get("alt", RequestPriority.Vital))),
        			new CSVFile.CSVField(percent(counter.get("alt", RequestPriority.Vital), counter.get("req", RequestPriority.Vital))),
        			new CSVFile.CSVField(df2.format(counter.get("alt-assigned", RequestPriority.Vital))),
        			new CSVFile.CSVField(percent(counter.get("alt-assigned", RequestPriority.Vital), counter.get("alt", RequestPriority.Vital))),
        			new CSVFile.CSVField(df2.format(counter.get("alt-assigned-1st", RequestPriority.Vital))),
        			new CSVFile.CSVField(percent(counter.get("alt-assigned-1st", RequestPriority.Vital), counter.get("alt-assigned", RequestPriority.Vital))),
        			
        			new CSVFile.CSVField(df2.format(counter.get("adv-req", RequestPriority.Vital))),
        			new CSVFile.CSVField(df2.format(counter.get("adv-req-assigned", RequestPriority.Vital))),
        			new CSVFile.CSVField(percent(counter.get("adv-req-assigned", RequestPriority.Vital), counter.get("adv-req", RequestPriority.Vital))),
        			new CSVFile.CSVField(df2.format(counter.get("adv-no-alt", RequestPriority.Vital))),
        			new CSVFile.CSVField(percent(counter.get("adv-no-alt", RequestPriority.Vital), counter.get("adv-req", RequestPriority.Vital))),
        			new CSVFile.CSVField(df2.format(counter.get("adv-no-alt-assigned", RequestPriority.Vital))),
        			new CSVFile.CSVField(percent(counter.get("adv-no-alt-assigned", RequestPriority.Vital), counter.get("adv-no-alt", RequestPriority.Vital))),
        			new CSVFile.CSVField(df2.format(counter.get("adv-alt", RequestPriority.Vital))),
        			new CSVFile.CSVField(percent(counter.get("adv-alt", RequestPriority.Vital), counter.get("adv-req", RequestPriority.Vital))),
        			new CSVFile.CSVField(df2.format(counter.get("adv-alt-assigned", RequestPriority.Vital))),
        			new CSVFile.CSVField(percent(counter.get("adv-alt-assigned", RequestPriority.Vital), counter.get("adv-alt", RequestPriority.Vital))),
        			new CSVFile.CSVField(df2.format(counter.get("adv-alt-assigned-1st", RequestPriority.Vital))),
        			new CSVFile.CSVField(percent(counter.get("adv-alt-assigned-1st", RequestPriority.Vital), counter.get("adv-alt-assigned", RequestPriority.Vital))),
        	});
        }
        
        Check counter = total;
        csv.addLine(new CSVFile.CSVField[] {
    			new CSVFile.CSVField("Total"),
    			new CSVFile.CSVField(df2.format(counter.get("students"))),
    			
    			new CSVFile.CSVField(df2.format(counter.get("req"))),
    			new CSVFile.CSVField(df2.format(counter.get("req-assigned"))),
    			new CSVFile.CSVField(percent(counter.get("req-assigned"), counter.get("req"))),
    			new CSVFile.CSVField(df2.format(counter.get("no-alt"))),
    			new CSVFile.CSVField(percent(counter.get("no-alt"), counter.get("req"))),
    			new CSVFile.CSVField(df2.format(counter.get("no-alt-assigned"))),
    			new CSVFile.CSVField(percent(counter.get("no-alt-assigned"), counter.get("no-alt"))),
    			new CSVFile.CSVField(df2.format(counter.get("alt"))),
    			new CSVFile.CSVField(percent(counter.get("alt"), counter.get("req"))),
    			new CSVFile.CSVField(df2.format(counter.get("alt-assigned"))),
    			new CSVFile.CSVField(percent(counter.get("alt-assigned"), counter.get("alt"))),
    			new CSVFile.CSVField(df2.format(counter.get("alt-assigned-1st"))),
    			new CSVFile.CSVField(percent(counter.get("alt-assigned-1st"), counter.get("alt-assigned"))),
    			new CSVFile.CSVField(df2.format(counter.get("subst"))),
    			new CSVFile.CSVField(percent(counter.get("subst"), counter.get("students"))),
    			new CSVFile.CSVField(df2.format(counter.get("subst-assigned"))),
    			new CSVFile.CSVField(counter.get("subst") == 0 ? "" : percent(counter.get("subst-assigned"), counter.get("subst"))),
    			
    			new CSVFile.CSVField(df2.format(counter.get("advised"))),
    			new CSVFile.CSVField(percent(counter.get("advised"), counter.get("students"))),
    			new CSVFile.CSVField(df2.format(counter.get("adv-req"))),
    			new CSVFile.CSVField(df2.format(counter.get("adv-req-assigned"))),
    			new CSVFile.CSVField(percent(counter.get("adv-req-assigned"), counter.get("adv-req"))),
    			new CSVFile.CSVField(df2.format(counter.get("adv-no-alt"))),
    			new CSVFile.CSVField(percent(counter.get("adv-no-alt"), counter.get("adv-req"))),
    			new CSVFile.CSVField(df2.format(counter.get("adv-no-alt-assigned"))),
    			new CSVFile.CSVField(percent(counter.get("adv-no-alt-assigned"), counter.get("adv-no-alt"))),
    			new CSVFile.CSVField(df2.format(counter.get("adv-alt"))),
    			new CSVFile.CSVField(percent(counter.get("adv-alt"), counter.get("adv-req"))),
    			new CSVFile.CSVField(df2.format(counter.get("adv-alt-assigned"))),
    			new CSVFile.CSVField(percent(counter.get("adv-alt-assigned"), counter.get("adv-alt"))),
    			new CSVFile.CSVField(df2.format(counter.get("adv-alt-assigned-1st"))),
    			new CSVFile.CSVField(percent(counter.get("adv-alt-assigned-1st"), counter.get("adv-alt-assigned"))),
    			new CSVFile.CSVField(df2.format(counter.get("adv-subst"))),
    			new CSVFile.CSVField(percent(counter.get("adv-subst"), counter.get("advised"))),
    			new CSVFile.CSVField(df2.format(counter.get("adv-subst-assigned"))),
    			new CSVFile.CSVField(counter.get("adv-subst") == 0 ? "" : percent(counter.get("adv-subst-assigned"), counter.get("adv-subst"))),
    			    			
    			new CSVFile.CSVField(df2.format(counter.get("req", RequestPriority.Vital))),
    			new CSVFile.CSVField(df2.format(counter.get("req-assigned", RequestPriority.Vital))),
    			new CSVFile.CSVField(percent(counter.get("req-assigned", RequestPriority.Vital), counter.get("req", RequestPriority.Vital))),
    			new CSVFile.CSVField(df2.format(counter.get("no-alt", RequestPriority.Vital))),
    			new CSVFile.CSVField(percent(counter.get("no-alt", RequestPriority.Vital), counter.get("req", RequestPriority.Vital))),
    			new CSVFile.CSVField(df2.format(counter.get("no-alt-assigned", RequestPriority.Vital))),
    			new CSVFile.CSVField(percent(counter.get("no-alt-assigned", RequestPriority.Vital), counter.get("no-alt", RequestPriority.Vital))),
    			new CSVFile.CSVField(df2.format(counter.get("alt", RequestPriority.Vital))),
    			new CSVFile.CSVField(percent(counter.get("alt", RequestPriority.Vital), counter.get("req", RequestPriority.Vital))),
    			new CSVFile.CSVField(df2.format(counter.get("alt-assigned", RequestPriority.Vital))),
    			new CSVFile.CSVField(percent(counter.get("alt-assigned", RequestPriority.Vital), counter.get("alt", RequestPriority.Vital))),
    			new CSVFile.CSVField(df2.format(counter.get("alt-assigned-1st", RequestPriority.Vital))),
    			new CSVFile.CSVField(percent(counter.get("alt-assigned-1st", RequestPriority.Vital), counter.get("alt-assigned", RequestPriority.Vital))),
    			
    			new CSVFile.CSVField(df2.format(counter.get("adv-req", RequestPriority.Vital))),
    			new CSVFile.CSVField(df2.format(counter.get("adv-req-assigned", RequestPriority.Vital))),
    			new CSVFile.CSVField(percent(counter.get("adv-req-assigned", RequestPriority.Vital), counter.get("adv-req", RequestPriority.Vital))),
    			new CSVFile.CSVField(df2.format(counter.get("adv-no-alt", RequestPriority.Vital))),
    			new CSVFile.CSVField(percent(counter.get("adv-no-alt", RequestPriority.Vital), counter.get("adv-req", RequestPriority.Vital))),
    			new CSVFile.CSVField(df2.format(counter.get("adv-no-alt-assigned", RequestPriority.Vital))),
    			new CSVFile.CSVField(percent(counter.get("adv-no-alt-assigned", RequestPriority.Vital), counter.get("adv-no-alt", RequestPriority.Vital))),
    			new CSVFile.CSVField(df2.format(counter.get("adv-alt", RequestPriority.Vital))),
    			new CSVFile.CSVField(percent(counter.get("adv-alt", RequestPriority.Vital), counter.get("adv-req", RequestPriority.Vital))),
    			new CSVFile.CSVField(df2.format(counter.get("adv-alt-assigned", RequestPriority.Vital))),
    			new CSVFile.CSVField(percent(counter.get("adv-alt-assigned", RequestPriority.Vital), counter.get("adv-alt", RequestPriority.Vital))),
    			new CSVFile.CSVField(df2.format(counter.get("adv-alt-assigned-1st", RequestPriority.Vital))),
    			new CSVFile.CSVField(percent(counter.get("adv-alt-assigned-1st", RequestPriority.Vital), counter.get("adv-alt-assigned", RequestPriority.Vital))),
    	});

		return csv;
	}
	
	private static class Check {
		Map<String, Map<RequestPriority, Boolean>> iCheck = new HashMap<String, Map<RequestPriority, Boolean>>();
		Map<String, Map<RequestPriority, Integer>> iCounts = new HashMap<String, Map<RequestPriority, Integer>>();
		
		public boolean is(String check) {
			Map<RequestPriority, Boolean> ch = iCheck.get(check);
			if (ch == null) return false;
			for (Boolean b: ch.values())
				if (b.booleanValue()) return true;
			return false;
		}
		public int get(String check, RequestPriority rp) {
			Map<RequestPriority, Integer> cnt = iCounts.get(check);
			if (cnt == null) return 0;
			Integer c = cnt.get(rp);
			return c == null ? 0 : c.intValue();
		}
		public int get(String check) {
			Map<RequestPriority, Integer> cnt = iCounts.get(check);
			if (cnt == null) return 0;
			int total = 0;
			for (Integer c: cnt.values())
				total += c;
			return total;
		}

		public void set(String check, RequestPriority rp, Boolean value, int inc) {
			Map<RequestPriority, Boolean> ch = iCheck.get(check);
			if (ch == null) {
				ch = new HashMap<Request.RequestPriority, Boolean>();
				iCheck.put(check, ch);
			}
			if (value != null)
				ch.put(rp, value);
			else
				ch.remove(rp);
			Map<RequestPriority, Integer> cnt = iCounts.get(check);
			if (cnt == null) {
				cnt = new HashMap<Request.RequestPriority, Integer>();
				iCounts.put(check, cnt);
			}
			Integer old = cnt.get(rp);
			if (Boolean.TRUE.equals(value))
				cnt.put(rp, inc + (old == null ? 0 : old.intValue()));
		}
		public void set(String check, RequestPriority rp) {
			set(check, rp, true, 1);
		}
		public void set(String check) {
			set(check, RequestPriority.Normal, true, 1);
		}
		public void inc(String check) {
			set(check, RequestPriority.Normal, true, 1);
		}
		public void inc(String check, Check other) {
			for (RequestPriority rp: RequestPriority.values()) {
				int cnt = other.get(check, rp);
				set(check, rp, cnt > 0, cnt);
			}
		}
	}
	
	private Map<String, Course> iCourseCacheNames;
	private Map<Long, Course> iCourseCacheIds;
	protected Course findCourse(RequestedCourse rc) {
		if (iCourseCacheIds == null) {
			iCourseCacheIds = new HashMap<Long, Course>();
			iCourseCacheNames = new HashMap<String, Course>();
			for (Offering o: getModel().getOfferings())
				for (Course c: o.getCourses()) {
					iCourseCacheIds.put(c.getId(), c);
					iCourseCacheNames.put(c.getName(), c);
				}
		}
		Course ret = null;
		if (rc.hasCourseId())
			ret = iCourseCacheIds.get(rc.getCourseId());
		if (ret == null && rc.hasCourseName())
			ret = iCourseCacheNames.get(rc.getCourseName());
		return ret;
	}
	
    public boolean matches(Student s, org.unitime.timetable.gwt.shared.CourseRequestInterface.Request r) {
    	if (r.hasRequestedCourse())
			for (RequestedCourse rc: r.getRequestedCourse()) {
				Course c = findCourse(rc);
				if (c != null && super.matches(c)) return true;
			}
    	return false;
    }
}
