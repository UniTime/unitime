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
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.assignment.AssignmentComparator;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.model.AreaClassificationMajor;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Instructor;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Request.RequestPriority;
import org.cpsolver.studentsct.model.SctAssignment;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.model.StudentGroup;
import org.cpsolver.studentsct.report.AbstractStudentSectioningReport;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.solver.SectioningRequest;

/**
 * @author Tomas Muller
 */
public class UnasignedCourseRequests extends AbstractStudentSectioningReport {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
    
    public UnasignedCourseRequests(StudentSectioningModel model) {
        super(model);
    }

    protected String curriculum(Student student) {
        String curriculum = "";
        for (AreaClassificationMajor acm: student.getAreaClassificationMajors())
                curriculum += (curriculum.isEmpty() ? "" : ", ") + acm.toString();
        return curriculum;
    }
    
    protected String group(Student student) {
    	String group = "";
    	for (StudentGroup aac: student.getGroups())
    		group += (group.isEmpty() ? "" : ", ") + aac.getReference();
    	return group;    	
    }
    
    protected String advisor(Student student) {
        String advisors = "";
        for (Instructor instructor: student.getAdvisors())
        	advisors += (advisors.isEmpty() ? "" : ", ") + instructor.getName();
        return advisors;
    }
	
    @Override
    public CSVFile createTable(Assignment<Request, Enrollment> assignment, DataProperties properties) {
		Set<String> types = new HashSet<String>();
		for (String type: properties.getProperty("type", "").split("\\,"))
			if (!type.isEmpty())
				types.add(type);		
		CSVFile csv = new CSVFile();
		if (types.size() != 1)
			csv.setHeader(new CSVFile.CSVField[] {
					new CSVFile.CSVField("__Student"),
	        		new CSVFile.CSVField(MSG.reportStudentId()),
	        		new CSVFile.CSVField(MSG.reportStudentName()),
	        		new CSVFile.CSVField(MSG.reportStudentEmail()),
	        		new CSVFile.CSVField(MSG.reportStudentPriority()),
	        		new CSVFile.CSVField(MSG.reportStudentCurriculum()),
	        		new CSVFile.CSVField(MSG.reportStudentGroup()),
	        		new CSVFile.CSVField(MSG.reportStudentAdvisor()),
	        		new CSVFile.CSVField(MSG.reportUnassignedCourse()),
	        		new CSVFile.CSVField(MSG.reportCourseRequestPriority()),
	        		new CSVFile.CSVField(MSG.reportAssignmentConflict()),
	        		new CSVFile.CSVField(MSG.reportConflictingCourseRequestPriority()),
	                });
		else
			csv.setHeader(new CSVFile.CSVField[] {
					new CSVFile.CSVField("__Student"),
	        		new CSVFile.CSVField(MSG.reportStudentId()),
	        		new CSVFile.CSVField(MSG.reportStudentName()),
	        		new CSVFile.CSVField(MSG.reportStudentEmail()),
	        		new CSVFile.CSVField(MSG.reportStudentPriority()),
	        		new CSVFile.CSVField(MSG.reportStudentCurriculum()),
	        		new CSVFile.CSVField(MSG.reportStudentGroup()),
	        		new CSVFile.CSVField(MSG.reportStudentAdvisor()),
	        		new CSVFile.CSVField(MSG.reportUnassignedCourse()),
	        		new CSVFile.CSVField(MSG.reportAssignmentConflict()),
	        		new CSVFile.CSVField(MSG.reportConflictingCourseRequestPriority()),
	                });
		for (Student student: getModel().getStudents()) {
        	for (Request request: student.getRequests()) {
        		if (request instanceof FreeTimeRequest) continue;
        		Enrollment enrollment = assignment.getValue(request);
        		if (enrollment != null || !student.canAssign(assignment, request)) continue;
        		if (!matches(request, enrollment)) continue;
        		CourseRequest courseRequest = (CourseRequest)request;
        		if (!types.isEmpty() && (courseRequest.getRequestPriority() == null || !types.contains(courseRequest.getRequestPriority().name()))) continue;
        		List<CSVFile.CSVField> line = new ArrayList<CSVFile.CSVField>();
        		line.add(new CSVFile.CSVField(student.getId()));
        		line.add(new CSVFile.CSVField(student.getExternalId()));
	            line.add(new CSVFile.CSVField(student.getName()));
	            org.unitime.timetable.model.Student dbStudent = StudentDAO.getInstance().get(student.getId());
	            if (dbStudent != null)
	            	line.add(new CSVFile.CSVField(dbStudent.getEmail()));
	            else
	            	line.add(new CSVFile.CSVField(""));
	            line.add(new CSVFile.CSVField(student.getPriority() == null ? "" : student.getPriority().name()));
	            line.add(new CSVFile.CSVField(curriculum(student)));
	            line.add(new CSVFile.CSVField(group(student)));
	            line.add(new CSVFile.CSVField(advisor(student)));
	            line.add(new CSVFile.CSVField(courseRequest.getCourses().get(0).getName()));
	            if (types.size() != 1)
	            	line.add(new CSVFile.CSVField(courseRequest.getRequestPriority() == null ? "" : courseRequest.getRequestPriority().name()));
	            
				TreeSet<Enrollment> overlaps = new TreeSet<Enrollment>(new Comparator<Enrollment>() {
					@Override
					public int compare(Enrollment o1, Enrollment o2) {
						return o1.getRequest().compareTo(o2.getRequest());
					}
				});
				Hashtable<CourseRequest, TreeSet<Section>> overlapingSections = new Hashtable<CourseRequest, TreeSet<Section>>();
				List<Enrollment> av = courseRequest.getAvaiableEnrollmentsSkipSameTime(assignment);
				RequestPriority conflictPriority = null;
				if (av.isEmpty() || (av.size() == 1 && av.get(0).equals(courseRequest.getInitialAssignment()) && getModel().inConflict(assignment, av.get(0)))) {
					if (courseRequest.getCourses().get(0).getLimit() >= 0)
						line.add(new CSVFile.CSVField(MSG.courseIsFull()));
					else if (SectioningRequest.hasInconsistentRequirements(courseRequest, null))
						line.add(new CSVFile.CSVField(MSG.classNotAvailableDueToStudentPrefs()));
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
						TreeSet<String> ts = new TreeSet<String>();
						for (Enrollment q: overlaps) {
							if (q.getRequest() instanceof FreeTimeRequest) {
								ts.add(OnlineSectioningHelper.toString((FreeTimeRequest)q.getRequest()));
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
								ts.add(ov);
								if (cr.getRequestPriority() != null && (conflictPriority == null || conflictPriority.ordinal() > cr.getRequestPriority().ordinal()))
									conflictPriority = cr.getRequestPriority();
							}
						}
						String message = "";
						for (Iterator<String> i = ts.iterator(); i.hasNext();) {
							String x = i.next();
							if (message.isEmpty())
								message += MSG.conflictWithFirst(x);
							else if (!i.hasNext())
								message += MSG.conflictWithLast(x);
							else
								message += MSG.conflictWithMiddle(x);
						}
						line.add(new CSVFile.CSVField(message));
					} else {
						line.add(new CSVFile.CSVField(MSG.courseNotAssigned()));
					}
				}
				line.add(new CSVFile.CSVField(conflictPriority == null ? "" : conflictPriority.name()));
        		csv.addLine(line);
        	}
		}
		
		return csv;
	}

}
