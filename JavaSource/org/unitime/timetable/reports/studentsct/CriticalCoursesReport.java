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

import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.model.AcademicAreaCode;
import org.cpsolver.studentsct.model.AreaClassificationMajor;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.report.StudentSectioningReport;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.model.dao.StudentDAO;

/**
 * @author Tomas Muller
 */
public class CriticalCoursesReport implements StudentSectioningReport {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
    private StudentSectioningModel iModel = null;

    public CriticalCoursesReport(StudentSectioningModel model) {
        iModel = model;
    }

    public StudentSectioningModel getModel() {
        return iModel;
    }
    
    protected String curriculum(Student student) {
        String curriculum = "";
        for (AreaClassificationMajor acm: student.getAreaClassificationMajors())
                curriculum += (curriculum.isEmpty() ? "" : ", ") + acm.toString();
        return curriculum;
    }
    
    protected String group(Student student) {
        String group = "";
        Set<String> groups = new TreeSet<String>();
        for (AcademicAreaCode aac: student.getMinors())
                if (!"A".equals(aac.getArea()))
                	groups.add(aac.getCode());
        for (String g: groups)
        	group += (group.isEmpty() ? "" : ", ") + g;
        return group;           
    }
    
    @Override
    public CSVFile create(Assignment<Request, Enrollment> assignment, DataProperties properties) {
        CSVFile csv = new CSVFile();
        csv.setHeader(new CSVFile.CSVField[] {
                new CSVFile.CSVField("__Student"),
                new CSVFile.CSVField(MSG.reportStudentId()),
        		new CSVFile.CSVField(MSG.reportStudentName()),
        		new CSVFile.CSVField(MSG.reportStudentEmail()),
        		new CSVFile.CSVField(MSG.reportStudentCurriculum()),
        		new CSVFile.CSVField(MSG.reportStudentGroup()),
                new CSVFile.CSVField(MSG.reportPriority()),
                new CSVFile.CSVField(MSG.reportCourse()),
                new CSVFile.CSVField(MSG.report1stAlt()),
                new CSVFile.CSVField(MSG.report2ndAlt()),
                new CSVFile.CSVField(MSG.reportEnrolledCourse()),
                new CSVFile.CSVField(MSG.reportEnrolledChoice())
                });
        for (Student student: getModel().getStudents()) {
            if (student.isDummy()) continue;
            int priority = 0;
            for (Request r: student.getRequests()) {
                if (r instanceof CourseRequest) {
                    CourseRequest cr = (CourseRequest)r;
                    priority ++;
                    if (!cr.isCritical() || cr.isAlternative()) continue;
                    Enrollment e = cr.getAssignment(assignment);
                    Course course = cr.getCourses().get(0);
                    Course alt1 = (cr.getCourses().size() < 2 ? null : cr.getCourses().get(1));
                    Course alt2 = (cr.getCourses().size() < 3 ? null : cr.getCourses().get(2));
                    Course enrolled = (e == null ? null : e.getCourse());
                    org.unitime.timetable.model.Student dbStudent = StudentDAO.getInstance().get(student.getId());
                    csv.addLine(new CSVFile.CSVField[] {
                            new CSVFile.CSVField(student.getId()),
                            new CSVFile.CSVField(student.getExternalId()),
                            new CSVFile.CSVField(student.getName()),
                            new CSVFile.CSVField(dbStudent == null ? null : dbStudent.getEmail()),
                            new CSVFile.CSVField(curriculum(student)),
                            new CSVFile.CSVField(group(student)),
                            new CSVFile.CSVField(priority),
                            new CSVFile.CSVField(course.getName()),
                            new CSVFile.CSVField(alt1 == null ? "" : alt1.getName()),
                            new CSVFile.CSVField(alt2 == null ? "" : alt2.getName()),
                            new CSVFile.CSVField(enrolled == null ? "" : enrolled.getName()),
                            new CSVFile.CSVField(enrolled == null ? "" : String.valueOf(cr.getCourses().indexOf(enrolled) + 1))
                    });
                }
            }
        }
        return csv;
    }
}