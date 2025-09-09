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
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.model.AreaClassificationMajor;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.Instructor;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.SctAssignment;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.model.StudentGroup;
import org.cpsolver.studentsct.report.AbstractStudentSectioningReport;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;

/**
 * @author Tomas Muller
 */
public class DependentCourseIssues extends AbstractStudentSectioningReport {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	
	public DependentCourseIssues(StudentSectioningModel model) {
        super(model);
    }
	
	@Override
	public CSVFile createTable(Assignment<Request, Enrollment> assignment, DataProperties properties) {
        CSVFile csv = new CSVFile();
        csv.setHeader(new CSVFile.CSVField[] {
                new CSVFile.CSVField("__Student"),
                new CSVFile.CSVField(MSG.reportStudentId()), new CSVFile.CSVField(MSG.reportStudentName()),
                new CSVFile.CSVField(MSG.reportStudentCurriculum()), new CSVFile.CSVField(MSG.reportStudentGroup()), new CSVFile.CSVField(MSG.reportStudentAdvisor()),
                new CSVFile.CSVField(MSG.reportCourse()), new CSVFile.CSVField(MSG.colSubpart()),
                new CSVFile.CSVField(MSG.reportClass()), new CSVFile.CSVField(MSG.reportMeetingTime()),
                new CSVFile.CSVField(MSG.reportDatePattern()), new CSVFile.CSVField(MSG.colRoom()),
                new CSVFile.CSVField(MSG.reportNotAssigned()),
                });
        
        for (Student student: getModel().getStudents()) {
        	for (Request request: student.getRequests()) {
        		Enrollment enrollment = assignment.getValue(request);
    			if (enrollment == null || !enrollment.isCourseRequest() || !enrollment.getCourse().hasParent()) continue;
    			if (!matches(request, enrollment)) continue;
    			
    			Course parent = enrollment.getCourse().getParent();
    			for (Request otherRequest: student.getRequests()) {
    				if (otherRequest.hasCourse(parent)) {
    					Enrollment e = assignment.getValue(otherRequest);
    					if (e == null || e.getCourse() == null || !parent.equals(e.getCourse())) {
    						// not assigned parent course
    	    				List<CSVFile.CSVField> line = new ArrayList<CSVFile.CSVField>();
    			            line.add(new CSVFile.CSVField(student.getId()));
    			            line.add(new CSVFile.CSVField(student.getExternalId()));
    			            line.add(new CSVFile.CSVField(student.getName()));
    			            line.add(new CSVFile.CSVField(curriculum(student)));
    			            line.add(new CSVFile.CSVField(group(student)));
    			            line.add(new CSVFile.CSVField(advisor(student)));
    			            line.add(new CSVFile.CSVField(enrollment.getCourse().getName()));
    			            String type = "", section = "", time = "", room = "", date = "";
    			            for (Section s: enrollment.getSections()) {
    			            	type += (type.isEmpty() ? "" : "\n") + (s.getSubpart().getName() == null ? s.getSubpart().getInstructionalType() : s.getSubpart().getName());
    			            	section += (section.isEmpty() ? "" : "\n") + s.getName(enrollment.getCourse().getId());
    			            	time += (time.isEmpty() ? "" : "\n") + (s.getTime() == null || s.getTime().getDayCode() == 0 ? "" : s.getTime().getDayHeader() + " " + s.getTime().getStartTimeHeader(isUseAmPm()) + " - " + s.getTime().getEndTimeHeader(isUseAmPm()));
    			            	date += (date.isEmpty() ? "" : "\n") + (s.getTime() == null || s.getTime().getDatePatternName() == null ? "" : s.getTime().getDatePatternName());
    			            	room += (room.isEmpty() ? "" : "\n") + rooms(s);
    			            }
    			            line.add(new CSVFile.CSVField(type));
    			            line.add(new CSVFile.CSVField(section));
    			            line.add(new CSVFile.CSVField(time));
    			            line.add(new CSVFile.CSVField(date));
    			            line.add(new CSVFile.CSVField(room));
    			            line.add(new CSVFile.CSVField(parent.getName()));
    			            csv.addLine(line);
    					}
    				}
    			}
    			

        	}
        }
        return csv;
	}

    protected String rooms(SctAssignment section) {
        if (section.getNrRooms() == 0) return "";
        String ret = "";
        for (RoomLocation r: section.getRooms())
            ret += (ret.isEmpty() ? "" : ",\n") + r.getName();
        return ret;
    }
    
    protected String curriculum(Student student) {
        String curriculum = "";
        for (AreaClassificationMajor acm: student.getAreaClassificationMajors())
                curriculum += (curriculum.isEmpty() ? "" : ",\n") + acm.toString();
        return curriculum;
    }
    
    protected String group(Student student) {
        String group = "";
        Set<String> groups = new TreeSet<String>();
        for (StudentGroup g: student.getGroups())
                groups.add(g.getReference());
        for (String g: groups)
                group += (group.isEmpty() ? "" : ",\n") + g;
        return group;           
    }
    
    protected String advisor(Student student) {
        String advisors = "";
        for (Instructor instructor: student.getAdvisors())
                advisors += (advisors.isEmpty() ? "" : ",\n") + instructor.getName();
        return advisors;
    }
}
