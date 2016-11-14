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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.extension.TimeOverlapsCounter;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.SctAssignment;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.model.Unavailability;
import org.cpsolver.studentsct.report.StudentSectioningReport;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
public class StudentAvailabilityConflicts implements StudentSectioningReport {
    private static DecimalFormat sDF1 = new DecimalFormat("0.####");
    private StudentSectioningModel iModel = null;
    private TimeOverlapsCounter iTOC = null;
    
    public StudentAvailabilityConflicts(StudentSectioningModel model) {
        iModel = model;
        iTOC = model.getTimeOverlaps();
        if (iTOC == null) {
            iTOC = new TimeOverlapsCounter(null, model.getProperties());
        }
    }

    public StudentSectioningModel getModel() {
        return iModel;
    }
    
    public boolean shareHoursIgnoreBreakTime(TimeLocation t1, TimeLocation t2) {
    	int s1 = t1.getStartSlot() * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
    	int e1 = (t1.getStartSlot() + t1.getLength()) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN - t1.getBreakTime();
    	int s2 = t2.getStartSlot() * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
    	int e2 = (t2.getStartSlot() + t2.getLength()) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN - t2.getBreakTime();
    	return e1 > s2 && e2 > s1;
    }
    
    public boolean inConflict(SctAssignment a1, SctAssignment a2, boolean ignoreBreakTimeConflicts) {
        if (a1.getTime() == null || a2.getTime() == null) return false;
        if (ignoreBreakTimeConflicts) {
        	TimeLocation t1 = a1.getTime();
        	TimeLocation t2 = a2.getTime();
        	return t1.shareDays(t2) && shareHoursIgnoreBreakTime(t1, t2) && t1.shareWeeks(t2);
        } else {
        	return a1.getTime().hasIntersection(a2.getTime());
        }
    }
    
    public int nrSharedHoursIgnoreBreakTime(TimeLocation t1, TimeLocation t2) {
    	int s1 = t1.getStartSlot() * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
    	int e1 = (t1.getStartSlot() + t1.getLength()) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN - t1.getBreakTime();
    	int s2 = t2.getStartSlot() * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
    	int e2 = (t2.getStartSlot() + t2.getLength()) * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN - t2.getBreakTime();
    	int end = Math.min(e1, e2);
    	int start = Math.max(s1, s2);
        return (end < start ? 0 : end - start);
    }
    
    public int share(SctAssignment a1, SctAssignment a2, boolean ignoreBreakTimeConflicts) {
        if (!inConflict(a1, a2, ignoreBreakTimeConflicts)) return 0;
        if (ignoreBreakTimeConflicts) {
        	return a1.getTime().nrSharedDays(a2.getTime()) * nrSharedHoursIgnoreBreakTime(a1.getTime(), a2.getTime());
        } else {
        	return 5 * a1.getTime().nrSharedDays(a2.getTime()) * a1.getTime().nrSharedHours(a2.getTime());
        }
    }
    
    public CSVFile createTable(final Assignment<Request, Enrollment> assignment, boolean includeLastLikeStudents, boolean includeRealStudents, final boolean useAmPm, boolean includeAllowedOverlaps, boolean ignoreBreakTimeConflicts) {
        CSVFile csv = new CSVFile();
        if (includeAllowedOverlaps) {
            csv.setHeader(new CSVFile.CSVField[] {
            		new CSVFile.CSVField("Student\nId"),
            		new CSVFile.CSVField("Student\nName"),
            		new CSVFile.CSVField("Student\nEmail"),
            		new CSVFile.CSVField("Allowed\nOverlap"),
            		new CSVFile.CSVField("Course"), new CSVFile.CSVField("Class"), new CSVFile.CSVField("Meeting Time"),
            		new CSVFile.CSVField("Subpart\nOverlap"), new CSVFile.CSVField("Time\nOverride"),
            		new CSVFile.CSVField("Conflicting\nAssignment"), new CSVFile.CSVField("Conflicting\nMeeting Time"),
            		new CSVFile.CSVField("Teaching\nOverlap"), new CSVFile.CSVField("Overlap\n[min]")
                    });
        } else {
            csv.setHeader(new CSVFile.CSVField[] {
            		new CSVFile.CSVField("Student\nId"),
            		new CSVFile.CSVField("Student\nName"),
            		new CSVFile.CSVField("Student\nEmail"),
            		new CSVFile.CSVField("Course"), new CSVFile.CSVField("Class"), new CSVFile.CSVField("Meeting Time"),
            		new CSVFile.CSVField("Conflicting\nAssignment"), new CSVFile.CSVField("Conflicting\nMeeting Time"),
                    new CSVFile.CSVField("Overlap\n[min]")
                    });
        }
        
        Set<Student> students = new TreeSet<Student>(new Comparator<Student>() {
			@Override
			public int compare(Student s1, Student s2) {
				return s1.getExternalId().compareTo(s2.getExternalId());
			}
		});
        students.addAll(getModel().getStudents());
        
        for (Student student: students) {
        	if (student.isDummy() && !includeLastLikeStudents) continue;
        	if (!student.isDummy() && !includeRealStudents) continue;
        	if (student.getUnavailabilities().isEmpty()) continue;
        	for (Request r: student.getRequests()) {
        		Enrollment e = assignment.getValue(r);
        		if (e == null || r instanceof FreeTimeRequest) continue;
        		for (Section s : e.getSections()) {
        			for (Unavailability u: student.getUnavailabilities()) {
        				if (inConflict(s, u, ignoreBreakTimeConflicts)) {
        					if (!includeAllowedOverlaps && (e.isAllowOverlap() || u.isAllowOverlap() || !s.isOverlapping(u))) continue;
        					List<CSVFile.CSVField> line = new ArrayList<CSVFile.CSVField>();
            	            line.add(new CSVFile.CSVField(student.getExternalId()));
            	            line.add(new CSVFile.CSVField(student.getName()));
            	            org.unitime.timetable.model.Student dbStudent = StudentDAO.getInstance().get(student.getId());
            	            if (dbStudent != null)
            	            	line.add(new CSVFile.CSVField(dbStudent.getEmail()));
            	            if (includeAllowedOverlaps)
            	            	line.add(new CSVFile.CSVField(e.isAllowOverlap() || u.isAllowOverlap() || !s.isOverlapping(u)));
            	            line.add(new CSVFile.CSVField(e.getCourse().getName()));
            	            line.add(new CSVFile.CSVField(s.getSubpart().getName() + " " + s.getName(e.getCourse().getId())));
            	            line.add(new CSVFile.CSVField(s.getTime() == null ? "" : s.getTime().getDayHeader() + " " + s.getTime().getStartTimeHeader(useAmPm) + " - " + s.getTime().getEndTimeHeader(useAmPm)));
            	            if (includeAllowedOverlaps) {
            	            	line.add(new CSVFile.CSVField(s.getSubpart().isAllowOverlap()));
            	            	line.add(new CSVFile.CSVField(e.getReservation() != null && e.getReservation().isAllowOverlap()));
            	            }
            	            if (u.getSection().getSubpart() == null)
                	            line.add(new CSVFile.CSVField(u.getSection().getName()));
            	            else
            	            	line.add(new CSVFile.CSVField(u.getSection().getSubpart().getConfig().getOffering().getName() + " " + u.getSection().getSubpart().getName() + " " + u.getSection().getName()));
            	            line.add(new CSVFile.CSVField(u.getTime() == null ? "" : u.getTime().getDayHeader() + " " + u.getTime().getStartTimeHeader(useAmPm) + " - " + u.getTime().getEndTimeHeader(useAmPm)));
            	            if (includeAllowedOverlaps) {
                	            line.add(new CSVFile.CSVField(u.isAllowOverlap()));
            	            }
            	            line.add(new CSVFile.CSVField(sDF1.format(share(s, u, ignoreBreakTimeConflicts))));
            	            csv.addLine(line);
        	            }
        	        }
                }
            }
        }
 
        return csv;
    }

    @Override
    public CSVFile create(Assignment<Request, Enrollment> assignment, DataProperties properties) {
        return createTable(assignment,
        		properties.getPropertyBoolean("lastlike", false),
        		properties.getPropertyBoolean("real", true),
        		properties.getPropertyBoolean("useAmPm", true),
        		properties.getPropertyBoolean("includeAllowedOverlaps", true),
        		properties.getPropertyBoolean("ignoreBreakTimeConflicts", false)
        		);
    }
}
