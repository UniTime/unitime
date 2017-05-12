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
package org.unitime.timetable.reports.studentsct;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.AssignmentComparator;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.SctAssignment;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.report.StudentSectioningReport;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;

/**
 * @author Tomas Muller
 */
public class UnasignedCourseRequests implements StudentSectioningReport {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
    private StudentSectioningModel iModel = null;
    
    public UnasignedCourseRequests(StudentSectioningModel model) {
        iModel = model;
    }

    public StudentSectioningModel getModel() {
        return iModel;
    }

	@Override
	public CSVFile create(Assignment<Request, Enrollment> assignment, DataProperties properties) {
        return createTable(assignment,
        		properties.getPropertyBoolean("lastlike", false),
        		properties.getPropertyBoolean("real", true),
        		properties.getPropertyBoolean("useAmPm", true)
        		);
	}
	
	public CSVFile createTable(final Assignment<Request, Enrollment> assignment, boolean includeLastLikeStudents, boolean includeRealStudents, final boolean useAmPm) {
		CSVFile csv = new CSVFile();
		csv.setHeader(new CSVFile.CSVField[] {
				new CSVFile.CSVField("__Student"),
        		new CSVFile.CSVField(MSG.reportStudentId()),
        		new CSVFile.CSVField(MSG.reportStudentName()),
        		new CSVFile.CSVField(MSG.reportStudentEmail()),
        		new CSVFile.CSVField(MSG.reportUnassignedCourse()),
        		new CSVFile.CSVField(MSG.reportAssignmentConflict())
                });
		for (Student student: getModel().getStudents()) {
			if (student.isDummy() && !includeLastLikeStudents) continue;
        	if (!student.isDummy() && !includeRealStudents) continue;
        	for (Request request: student.getRequests()) {
        		if (request instanceof FreeTimeRequest) continue;
        		Enrollment enrollment = assignment.getValue(request);
        		if (enrollment != null || !student.canAssign(assignment, request)) continue;
        		CourseRequest courseRequest = (CourseRequest)request;
        		List<CSVFile.CSVField> line = new ArrayList<CSVFile.CSVField>();
        		line.add(new CSVFile.CSVField(student.getId()));
        		line.add(new CSVFile.CSVField(student.getExternalId()));
	            line.add(new CSVFile.CSVField(student.getName()));
	            org.unitime.timetable.model.Student dbStudent = StudentDAO.getInstance().get(student.getId());
	            if (dbStudent != null)
	            	line.add(new CSVFile.CSVField(dbStudent.getEmail()));
	            else
	            	line.add(new CSVFile.CSVField(""));
	            line.add(new CSVFile.CSVField(courseRequest.getCourses().get(0).getName()));
	            
				TreeSet<Enrollment> overlaps = new TreeSet<Enrollment>(new Comparator<Enrollment>() {
					@Override
					public int compare(Enrollment o1, Enrollment o2) {
						return o1.getRequest().compareTo(o2.getRequest());
					}
				});
				Hashtable<CourseRequest, TreeSet<Section>> overlapingSections = new Hashtable<CourseRequest, TreeSet<Section>>();
				List<Enrollment> av = courseRequest.getAvaiableEnrollmentsSkipSameTime(assignment);
				if (av.isEmpty() || (av.size() == 1 && av.get(0).equals(courseRequest.getInitialAssignment()) && getModel().inConflict(assignment, av.get(0)))) {
					if (courseRequest.getCourses().get(0).getLimit() >= 0)
						line.add(new CSVFile.CSVField(MSG.courseIsFull()));
					else
						line.add(new CSVFile.CSVField(MSG.classNotAvailable()));
				} else {
					for (Iterator<Enrollment> e = av.iterator(); e.hasNext();) {
						Enrollment enrl = e.next();
						for (Request q: enrl.getStudent().getRequests()) {
							if (q.equals(request)) continue;
							Enrollment x = assignment.getValue(q);
							if (x == null || x.getAssignments() == null || x.getAssignments().isEmpty()) continue;
					        for (Iterator<SctAssignment> i = x.getAssignments().iterator(); i.hasNext();) {
					        	SctAssignment a = i.next();
								if (a.isOverlapping(enrl.getAssignments())) {
									overlaps.add(x);
									if (x.getRequest() instanceof CourseRequest) {
										CourseRequest cr = (CourseRequest)x.getRequest();
										TreeSet<Section> ss = overlapingSections.get(cr);
										if (ss == null) { ss = new TreeSet<Section>(new AssignmentComparator<Section, Request, Enrollment>(assignment)); overlapingSections.put(cr, ss); }
										ss.add((Section)a);
									}
								}
					        }
						}
					}
					if (!overlaps.isEmpty()) {
						String overlap = "";
						for (Enrollment q: overlaps) {
							if (!overlap.isEmpty()) overlap += "\n";
							if (q.getRequest() instanceof FreeTimeRequest) {
								overlap += OnlineSectioningHelper.toString((FreeTimeRequest)q.getRequest());
							} else {
								CourseRequest cr = (CourseRequest)q.getRequest();
								Course o = q.getCourse();
								String ov = MSG.course(o.getSubjectArea(), o.getCourseNumber());
								if (overlapingSections.get(cr).size() == 1)
									for (Iterator<Section> i = overlapingSections.get(cr).iterator(); i.hasNext();) {
										Section s = i.next();
										ov += " " + s.getSubpart().getName();
										if (i.hasNext()) ov += ",";
									}
								overlap += ov;
							}
						}
						line.add(new CSVFile.CSVField(MSG.conflictWithFirst(overlap)));
					} else {
						line.add(new CSVFile.CSVField(MSG.courseNotAssigned()));
					}
				}
        		csv.addLine(line);
        	}
		}
		
		return csv;
	}

}
