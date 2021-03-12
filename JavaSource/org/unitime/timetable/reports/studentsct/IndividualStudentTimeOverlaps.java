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
import org.cpsolver.studentsct.model.AreaClassificationMajor;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Instructor;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.SctAssignment;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.model.StudentGroup;
import org.cpsolver.studentsct.report.StudentSectioningReport;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
public class IndividualStudentTimeOverlaps implements StudentSectioningReport {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
    private static DecimalFormat sDF1 = new DecimalFormat("0.####");

    private StudentSectioningModel iModel = null;

    public IndividualStudentTimeOverlaps(StudentSectioningModel model) {
        iModel = model;
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
    
    public int nrSharedDays(TimeLocation t1, TimeLocation t2, int dayOfWeekOffset) {
    	if (t1 == null || t2 == null) return 0;
    	int count = 0;
    	int idx = -1;
        while ((idx = t1.getWeekCode().nextSetBit(1 + idx)) >= 0) {
            int dow = (idx + dayOfWeekOffset) % 7;
            if ((t1.getDayCode() & Constants.DAY_CODES[dow]) != 0) {
            	if (t2.getWeekCode().get(idx) && (t2.getDayCode() & Constants.DAY_CODES[dow]) != 0)
            		count ++;
            }
        }
        return count;
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
    
    public CSVFile createTable(final Assignment<Request, Enrollment> assignment, boolean includeLastLikeStudents, boolean includeRealStudents, final boolean useAmPm, boolean includeAllowedOverlaps, boolean ignoreBreakTimeConflicts, int dayOfWeekOffset) {
        CSVFile csv = new CSVFile();
        if (includeAllowedOverlaps) {
            csv.setHeader(new CSVFile.CSVField[] {
            		new CSVFile.CSVField("__Student"),
                    new CSVFile.CSVField(MSG.reportStudentId()),
            		new CSVFile.CSVField(MSG.reportStudentName()),
            		new CSVFile.CSVField(MSG.reportStudentEmail()),
            		new CSVFile.CSVField(MSG.reportStudentCurriculum()),
            		new CSVFile.CSVField(MSG.reportStudentGroup()),
            		new CSVFile.CSVField(MSG.reportStudentAdvisor()),
            		new CSVFile.CSVField(MSG.reportAllowedOverlap()),
            		new CSVFile.CSVField(MSG.reportCourse()), new CSVFile.CSVField(MSG.reportClass()), new CSVFile.CSVField(MSG.reportMeetingTime()), new CSVFile.CSVField(MSG.reportDatePattern()),
            		new CSVFile.CSVField(MSG.reportSubpartOverlap()), new CSVFile.CSVField(MSG.reportTimeOverride()),
            		new CSVFile.CSVField(MSG.reportConflictingCourse()), new CSVFile.CSVField(MSG.reportConflictingClass()), new CSVFile.CSVField(MSG.reportConflictingMeetingTime()), new CSVFile.CSVField(MSG.reportConflictingDatePattern()),
            		new CSVFile.CSVField(MSG.reportSubpartOverlap()), new CSVFile.CSVField(MSG.reportTimeOverride()),
            		new CSVFile.CSVField(MSG.reportIgnoreConflicts()),
                    new CSVFile.CSVField(MSG.reportOverlapMinutes()), new CSVFile.CSVField(MSG.reportOverlappingMeetings())
                    });
        } else {
            csv.setHeader(new CSVFile.CSVField[] {
            		new CSVFile.CSVField("__Student"),
                    new CSVFile.CSVField(MSG.reportStudentId()),
            		new CSVFile.CSVField(MSG.reportStudentName()),
            		new CSVFile.CSVField(MSG.reportStudentEmail()),
            		new CSVFile.CSVField(MSG.reportStudentCurriculum()),
            		new CSVFile.CSVField(MSG.reportStudentGroup()),
            		new CSVFile.CSVField(MSG.reportStudentAdvisor()),
            		new CSVFile.CSVField(MSG.reportCourse()), new CSVFile.CSVField(MSG.reportClass()), new CSVFile.CSVField(MSG.reportMeetingTime()), new CSVFile.CSVField(MSG.reportDatePattern()),
            		new CSVFile.CSVField(MSG.reportConflictingCourse()), new CSVFile.CSVField(MSG.reportConflictingClass()), new CSVFile.CSVField(MSG.reportConflictingMeetingTime()), new CSVFile.CSVField(MSG.reportConflictingDatePattern()),
                    new CSVFile.CSVField(MSG.reportOverlapMinutes()), new CSVFile.CSVField(MSG.reportOverlappingMeetings())
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
        	for (int i = 0; i < student.getRequests().size() - 1; i++) {
        		Request r1 = student.getRequests().get(i);
        		Enrollment e1 = assignment.getValue(r1);
        		if (e1 == null || r1 instanceof FreeTimeRequest) continue;
        		for (int j = i + 1; j < student.getRequests().size(); j++) {
        			Request r2 = student.getRequests().get(j);
        			Enrollment e2 = assignment.getValue(r2);
        			if (e2 == null || r2 instanceof FreeTimeRequest) continue;
        	        for (Section s1 : e1.getSections()) {
        	            for (Section s2 : e2.getSections()) {
        	                if (inConflict(s1, s2, ignoreBreakTimeConflicts)) {
        	                	if (!includeAllowedOverlaps && (e1.isAllowOverlap() || e2.isAllowOverlap() || !s1.isOverlapping(s2))) continue;
        	                	
                				List<CSVFile.CSVField> line = new ArrayList<CSVFile.CSVField>();
                				line.add(new CSVFile.CSVField(student.getId()));
                	            line.add(new CSVFile.CSVField(student.getExternalId()));
                	            line.add(new CSVFile.CSVField(student.getName()));
                	            org.unitime.timetable.model.Student s = StudentDAO.getInstance().get(student.getId());
                	            if (s != null)
                	            	line.add(new CSVFile.CSVField(s.getEmail()));
                	            line.add(new CSVFile.CSVField(curriculum(student)));
                	            line.add(new CSVFile.CSVField(group(student)));
                	            line.add(new CSVFile.CSVField(advisor(student)));

                	            if (includeAllowedOverlaps)
                	            	line.add(new CSVFile.CSVField(e1.isAllowOverlap() || e2.isAllowOverlap() || !s1.isOverlapping(s2)));
                	            
                	            line.add(new CSVFile.CSVField(e1.getCourse().getName()));
                	            line.add(new CSVFile.CSVField(s1.getSubpart().getName() + " " + s1.getName(e1.getCourse().getId())));
                	            line.add(new CSVFile.CSVField(s1.getTime() == null ? "" : s1.getTime().getDayHeader() + " " + s1.getTime().getStartTimeHeader(useAmPm) + " - " + s1.getTime().getEndTimeHeader(useAmPm)));
                	            line.add(new CSVFile.CSVField(s1.getTime() == null ? "" : s1.getTime().getDatePatternName()));
                	            if (includeAllowedOverlaps) {
                	            	line.add(new CSVFile.CSVField(s1.getSubpart().isAllowOverlap()));
                	            	line.add(new CSVFile.CSVField(e1.getReservation() != null && e1.getReservation().isAllowOverlap()));
                	            }
                	            
                	            line.add(new CSVFile.CSVField(e2.getCourse().getName()));
                	            line.add(new CSVFile.CSVField(s2.getSubpart().getName() + " " + s2.getName(e2.getCourse().getId())));
                	            line.add(new CSVFile.CSVField(s2.getTime() == null ? "" : s2.getTime().getDayHeader() + " " + s2.getTime().getStartTimeHeader(useAmPm) + " - " + s2.getTime().getEndTimeHeader(useAmPm)));
                	            line.add(new CSVFile.CSVField(s2.getTime() == null ? "" : s2.getTime().getDatePatternName()));
                	            if (includeAllowedOverlaps) {
                	            	line.add(new CSVFile.CSVField(s2.getSubpart().isAllowOverlap()));
                	            	line.add(new CSVFile.CSVField(e2.getReservation() != null && e2.getReservation().isAllowOverlap()));
                	            }
                	            
                	            if (includeAllowedOverlaps)
                	            	line.add(new CSVFile.CSVField(s1.isToIgnoreStudentConflictsWith(s2.getId())));
                	            line.add(new CSVFile.CSVField(sDF1.format(share(s1, s2, ignoreBreakTimeConflicts))));
                	            line.add(new CSVFile.CSVField(nrSharedDays(s1.getTime(), s2.getTime(), dayOfWeekOffset)));
                	            
                	            csv.addLine(line);
        	                }
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
        		properties.getPropertyBoolean("ignoreBreakTimeConflicts", false),
        		properties.getPropertyInt("DatePattern.DayOfWeekOffset", 0)
        		);
    }
}