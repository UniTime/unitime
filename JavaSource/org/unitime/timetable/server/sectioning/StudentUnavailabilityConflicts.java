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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.coursett.Constants;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.extension.StudentQuality;
import org.cpsolver.studentsct.model.AreaClassificationMajor;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.Instructor;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.SctAssignment;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.model.StudentGroup;
import org.cpsolver.studentsct.model.Unavailability;
import org.cpsolver.studentsct.report.AbstractStudentSectioningReport;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;

/**
 * @author Tomas Muller
 */
public class StudentUnavailabilityConflicts extends AbstractStudentSectioningReport {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private static GwtMessages GWT_MSG = Localization.create(GwtMessages.class);

	public StudentUnavailabilityConflicts(StudentSectioningModel model) {
        super(model);
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

    @Override
    public CSVFile createTable(Assignment<Request, Enrollment> assignment, DataProperties properties) {
        CSVFile csv = new CSVFile();
        csv.setHeader(new CSVFile.CSVField[] {
                new CSVFile.CSVField("__Student"),
                new CSVFile.CSVField(MSG.reportStudentId()), new CSVFile.CSVField(MSG.reportStudentName()),
                new CSVFile.CSVField(MSG.reportStudentCurriculum()), new CSVFile.CSVField(MSG.reportStudentGroup()), new CSVFile.CSVField(MSG.reportStudentAdvisor()),
                new CSVFile.CSVField(MSG.reportCourse()), new CSVFile.CSVField(MSG.reportClass()), new CSVFile.CSVField(MSG.reportMeetingTime()), new CSVFile.CSVField(MSG.colRoom()),
                new CSVFile.CSVField(MSG.reportUnavailability(MSG.reportCourse())), new CSVFile.CSVField(MSG.reportUnavailability(MSG.reportClass())),
                new CSVFile.CSVField(MSG.reportUnavailability(MSG.reportMeetingTime())), new CSVFile.CSVField(MSG.reportUnavailability(MSG.colRoom())),
                new CSVFile.CSVField(MSG.reportProblem()),
                new CSVFile.CSVField(MSG.reportTeachingAssignment()),
                new CSVFile.CSVField(MSG.reportAllowedOverlap()),
                });
        
        StudentQuality q = getModel().getStudentQuality();
        for (Student student: getModel().getStudents()) {
        	for (Request request: student.getRequests()) {
        		Enrollment enrollment = assignment.getValue(request);
    			if (enrollment == null || !enrollment.isCourseRequest()) continue;
    			if (!matches(request, enrollment)) continue;
    			for (Section section: enrollment.getSections()) {
    				TimeLocation time = section.getTime();
    				if (time == null) continue;
    				for (Unavailability unavailability: student.getUnavailabilities()) {
    					// if (unavailability.isTeachingAssignment()) continue;
    					TimeLocation uaTime = unavailability.getTime();
    					if (uaTime == null) continue;
    					if (uaTime.hasIntersection(time)) {
    						List<CSVFile.CSVField> line = new ArrayList<CSVFile.CSVField>();
	    		            line.add(new CSVFile.CSVField(student.getId()));
	    		            line.add(new CSVFile.CSVField(student.getExternalId()));
	    		            line.add(new CSVFile.CSVField(student.getName()));
	    		            line.add(new CSVFile.CSVField(curriculum(student)));
	    		            line.add(new CSVFile.CSVField(group(student)));
	    		            line.add(new CSVFile.CSVField(advisor(student)));
	    		            line.add(new CSVFile.CSVField(enrollment.getCourse().getName()));
	    		            line.add(new CSVFile.CSVField(section.getSubpart().getName() + " " + section.getName(enrollment.getCourse().getId())));
	    		            line.add(new CSVFile.CSVField(time.getDayHeader() + " " + time.getStartTimeHeader(isUseAmPm()) + " - " + time.getEndTimeHeader(isUseAmPm())));
	    		            line.add(new CSVFile.CSVField(rooms(section)));
	    		            line.add(new CSVFile.CSVField(unavailability.getCourseName()));
	    		            line.add(new CSVFile.CSVField(unavailability.getSectionName()));
	    		            line.add(new CSVFile.CSVField(uaTime.getDayHeader() + " " + uaTime.getStartTimeHeader(isUseAmPm()) + " - " + uaTime.getEndTimeHeader(isUseAmPm())));
	    		            line.add(new CSVFile.CSVField(rooms(unavailability)));
	    		            line.add(new CSVFile.CSVField(unavailability.isOverlapping(section) ?
	    		            		MSG.reportAllowedOverlap( 5 * time.nrSharedDays(uaTime) * time.nrSharedHours(uaTime)) :
	    		            		MSG.reportTimeConflict()));
	    		            line.add(new CSVFile.CSVField(unavailability.isTeachingAssignment() ? GWT_MSG.exportTrue() : GWT_MSG.exportFalse()));
	    		            line.add(new CSVFile.CSVField(section.isAllowOverlap() || unavailability.isAllowOverlap() ? GWT_MSG.exportTrue() : GWT_MSG.exportFalse()));
	    		            csv.addLine(line);
    					} else if (q != null && StudentQuality.Type.UnavailabilityDistance.inConflict(q.getStudentQualityContext(), section, unavailability)) {
    						TimeLocation t1 = time;
    		                TimeLocation t2 = uaTime;
    		                int a1 = t1.getStartSlot(), a2 = t2.getStartSlot();
    		                int distTime = q.getStudentQualityContext().getUnavailabilityDistanceInMinutes(section.getPlacement(), unavailability);
    		                int breakTime = (a1 + t1.getNrSlotsPerMeeting() <= a2 ? 
    		                		t1.getBreakTime() + Constants.SLOT_LENGTH_MIN * (a2 - a1 - t1.getLength()) :
    		                		t2.getBreakTime() + Constants.SLOT_LENGTH_MIN * (a1 - a2 - t2.getLength()));
    						List<CSVFile.CSVField> line = new ArrayList<CSVFile.CSVField>();
	    		            line.add(new CSVFile.CSVField(student.getId()));
	    		            line.add(new CSVFile.CSVField(student.getExternalId()));
	    		            line.add(new CSVFile.CSVField(student.getName()));
	    		            line.add(new CSVFile.CSVField(curriculum(student)));
	    		            line.add(new CSVFile.CSVField(group(student)));
	    		            line.add(new CSVFile.CSVField(advisor(student)));
	    		            line.add(new CSVFile.CSVField(enrollment.getCourse().getName()));
	    		            line.add(new CSVFile.CSVField(section.getSubpart().getName() + " " + section.getName(enrollment.getCourse().getId())));
	    		            line.add(new CSVFile.CSVField(time.getDayHeader() + " " + time.getStartTimeHeader(isUseAmPm()) + " - " + time.getEndTimeHeader(isUseAmPm())));
	    		            line.add(new CSVFile.CSVField(rooms(section)));
	    		            line.add(new CSVFile.CSVField(unavailability.getCourseName()));
	    		            line.add(new CSVFile.CSVField(unavailability.getSectionName()));
	    		            line.add(new CSVFile.CSVField(uaTime.getDayHeader() + " " + uaTime.getStartTimeHeader(isUseAmPm()) + " - " + uaTime.getEndTimeHeader(isUseAmPm())));
	    		            line.add(new CSVFile.CSVField(rooms(unavailability)));
	    		            line.add(new CSVFile.CSVField(MSG.reportDistanceConflict(breakTime, distTime)));
	    		            line.add(new CSVFile.CSVField(unavailability.isTeachingAssignment() ? GWT_MSG.exportTrue() : GWT_MSG.exportFalse()));
	    		            line.add(new CSVFile.CSVField(section.isAllowOverlap() || unavailability.isAllowOverlap() ? GWT_MSG.exportTrue() : GWT_MSG.exportFalse()));
	    		            csv.addLine(line);
    					}
    				}
    			}
        	}
        }
        return csv;
    }
}
