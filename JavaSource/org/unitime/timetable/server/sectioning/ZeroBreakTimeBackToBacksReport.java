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

import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.model.AreaClassificationMajor;
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
public class ZeroBreakTimeBackToBacksReport extends AbstractStudentSectioningReport {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);

	public ZeroBreakTimeBackToBacksReport(StudentSectioningModel model) {
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
                new CSVFile.CSVField(MSG.reportBTB(MSG.reportCourse())), new CSVFile.CSVField(MSG.reportBTB(MSG.reportClass())),
                new CSVFile.CSVField(MSG.reportBTB(MSG.reportMeetingTime())), new CSVFile.CSVField(MSG.reportBTB(MSG.colRoom())),
                });
        
        for (Student student: getModel().getStudents()) {
        	for (Request request: student.getRequests()) {
        		Enrollment enrollment = assignment.getValue(request);
    			if (enrollment == null || !enrollment.isCourseRequest()) continue;
    			if (!matches(request, enrollment)) continue;
    			for (Section section: enrollment.getSections()) {
    				TimeLocation time = section.getTime();
    				if (time == null || time.getBreakTime() > 0) continue;
    				for (Request btbRequest: student.getRequests()) {
    					Enrollment btbEnrollment = assignment.getValue(btbRequest);
    	    			if (btbEnrollment == null || !btbEnrollment.isCourseRequest()) continue;
    	    			for (Section btbSection: btbEnrollment.getSections()) {
    	    				TimeLocation btbTime = btbSection.getTime();
    	    				if (btbTime != null && time.shareWeeks(btbTime) && time.shareDays(btbTime) && time.getStartSlot() + time.getLength() == btbTime.getStartSlot()) {
    	    					List<CSVFile.CSVField> line = new ArrayList<CSVFile.CSVField>();
    	    		            line.add(new CSVFile.CSVField(student.getId()));
    	    		            line.add(new CSVFile.CSVField(student.getExternalId()));
    	    		            line.add(new CSVFile.CSVField(student.getName()));
    	    		            line.add(new CSVFile.CSVField(curriculum(student)));
    	    		            line.add(new CSVFile.CSVField(group(student)));
    	    		            line.add(new CSVFile.CSVField(advisor(student)));
    	    		            line.add(new CSVFile.CSVField(enrollment.getCourse().getName()));
    	    		            line.add(new CSVFile.CSVField(section.getName(enrollment.getCourse().getId())));
    	    		            line.add(new CSVFile.CSVField(time.getDayHeader() + " " + time.getStartTimeHeader(isUseAmPm()) + " - " + time.getEndTimeHeader(isUseAmPm())));
    	    		            line.add(new CSVFile.CSVField(rooms(section)));
    	    		            line.add(new CSVFile.CSVField(btbEnrollment.getCourse().getName()));
    	    		            line.add(new CSVFile.CSVField(btbSection.getName(btbEnrollment.getCourse().getId())));
    	    		            line.add(new CSVFile.CSVField(btbTime.getDayHeader() + " " + btbTime.getStartTimeHeader(isUseAmPm()) + " - " + btbTime.getEndTimeHeader(isUseAmPm())));
    	    		            line.add(new CSVFile.CSVField(rooms(btbSection)));
    	    		            csv.addLine(line);
    	    				}
    	    			}
    				}
    			}
        	}
        }
        return csv;
    }
}
