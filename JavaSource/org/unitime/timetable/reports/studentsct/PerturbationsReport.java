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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.report.StudentSectioningReport;
import org.unitime.timetable.model.dao.StudentDAO;

/**
 * @author Tomas Muller
 */
public class PerturbationsReport implements StudentSectioningReport {
    private static DecimalFormat sDF = new DecimalFormat("0.000");
    private StudentSectioningModel iModel;
    
    protected double iSameChoiceWeight = 0.900;
    protected double iSameTimeWeight = 0.700;
    protected double iSameConfigWeight = 0.500;
    
    public PerturbationsReport(StudentSectioningModel model) {
        iModel = model;
        iSameChoiceWeight = model.getProperties().getPropertyDouble("StudentWeights.SameChoice", iSameChoiceWeight);
        iSameTimeWeight = model.getProperties().getPropertyDouble("StudentWeights.SameTime", iSameTimeWeight);
        iSameConfigWeight = model.getProperties().getPropertyDouble("StudentWeights.SameConfig", iSameConfigWeight);
    }
    
    protected double getDifference(Section section, Enrollment other) {
    	if (section == null) {
    		return 0.0;
    	} else if (section.getSubpart().getConfig().equals(other.getConfig())) {
    		for (Section initial: other.getSections()) {
                if (section.getSubpart().equals(initial.getSubpart())) {
                    if (section.equals(initial)) {
                        return 1.0;
                    } else if (section.sameChoice(initial)) {
                    	return iSameChoiceWeight;
                    } else if (section.sameTime(initial)) {
                    	return iSameTimeWeight;
                    }
                }
    		}
    	} else {
    		for (Section initial: other.getSections()) {
                if (section.sameChoice(initial)) {
                    return iSameChoiceWeight;
                } else if (section.sameInstructionalType(initial) && section.sameTime(initial)) {
                    return iSameTimeWeight;
                }
            }
    	}
        return 0.0;
    }

    @Override
    public CSVFile create(Assignment<Request, Enrollment> assignment, DataProperties properties) {
        boolean useAmPm = properties.getPropertyBoolean("useAmPm", true);
        CSVFile csv = new CSVFile();
        csv.setHeader(new CSVFile.CSVField[] {
        		new CSVFile.CSVField("__Student"),
        		new CSVFile.CSVField("Student\nId"),
        		new CSVFile.CSVField("Student\nName"),
        		new CSVFile.CSVField("Student\nEmail"),
                new CSVFile.CSVField("Course"),
                new CSVFile.CSVField("Original\nClass"),
                new CSVFile.CSVField("Original\nTime"),
                new CSVFile.CSVField("Original\nDate"),
                new CSVFile.CSVField("Original\nRoom"),
                new CSVFile.CSVField("Assigned\nClass"),
                new CSVFile.CSVField("Assigned\nTime"),
                new CSVFile.CSVField("Assigned\nDate"),
                new CSVFile.CSVField("Assigned\nRoom"),
                new CSVFile.CSVField("Penalization"),
                });
        
        Set<Student> students = new TreeSet<Student>(new Comparator<Student>() {
			@Override
			public int compare(Student s1, Student s2) {
				return s1.getExternalId().compareTo(s2.getExternalId());
			}
		});
        students.addAll(iModel.getStudents());
        
        boolean includeLastLikeStudents = properties.getPropertyBoolean("lastlike", false);
		boolean includeRealStudents = properties.getPropertyBoolean("real", true);
        
        for (Student student: students) {
        	if (student.isDummy()) continue;
        	if (student.isDummy() && !includeLastLikeStudents) continue;
        	if (!student.isDummy() && !includeRealStudents) continue;
        	for (Request r: student.getRequests()) {
        		Enrollment e = assignment.getValue(r);
        		Enrollment i = r.getInitialAssignment();
        		if (i == null || r instanceof FreeTimeRequest || i.equals(e)) continue;
        		org.unitime.timetable.model.Student s = StudentDAO.getInstance().get(student.getId());
        		if (e == null) {
        			for (Section sct : i.getSections()) {
                		List<CSVFile.CSVField> line = new ArrayList<CSVFile.CSVField>();
                		line.add(new CSVFile.CSVField(student.getId()));
            			line.add(new CSVFile.CSVField(student.getExternalId()));
        	            line.add(new CSVFile.CSVField(student.getName()));
        	            line.add(new CSVFile.CSVField(s == null ? null : s.getEmail()));
        	            line.add(new CSVFile.CSVField(i.getCourse().getName()));
        	            line.add(new CSVFile.CSVField(sct.getSubpart().getName() + " " + sct.getName(i.getCourse().getId())));
        	            line.add(new CSVFile.CSVField(sct.getTime() == null ? "" : sct.getTime().getDayHeader() + " " + sct.getTime().getStartTimeHeader(useAmPm) + " - " + sct.getTime().getEndTimeHeader(useAmPm)));
        	            line.add(new CSVFile.CSVField(sct.getTime() == null ? "" : sct.getTime().getDatePatternName()));
        	            line.add(new CSVFile.CSVField(sct.getNrRooms() == 0 ? "" : sct.getPlacement().getRoomName(", ")));
        	            line.add(new CSVFile.CSVField("Not Assigned"));
        	            line.add(new CSVFile.CSVField(""));
        	            line.add(new CSVFile.CSVField(""));
        	            line.add(new CSVFile.CSVField(""));
        	            line.add(new CSVFile.CSVField(sDF.format(1.0 - getDifference(null, i))));        	            
        	            csv.addLine(line);
        			}
        		} else if (i.getConfig().equals(e.getConfig())) {
        			for (Section org : i.getSections()) {
                		Section sct = null;
                		for (Section x: e.getSections())
                			if (org.getSubpart().equals(x.getSubpart())) { sct = x; break; }
                		if (sct == null || org.equals(sct)) continue;
                		List<CSVFile.CSVField> line = new ArrayList<CSVFile.CSVField>();
                		line.add(new CSVFile.CSVField(student.getId()));
                		line.add(new CSVFile.CSVField(student.getExternalId()));
        	            line.add(new CSVFile.CSVField(student.getName()));
        	            line.add(new CSVFile.CSVField(s == null ? null : s.getEmail()));
        	            line.add(new CSVFile.CSVField(i.getCourse().getName()));
        	            line.add(new CSVFile.CSVField(org.getSubpart().getName() + " " + org.getName(i.getCourse().getId())));
        	            line.add(new CSVFile.CSVField(org.getTime() == null ? "" : org.getTime().getDayHeader() + " " + org.getTime().getStartTimeHeader(useAmPm) + " - " + org.getTime().getEndTimeHeader(useAmPm)));
        	            line.add(new CSVFile.CSVField(org.getTime() == null ? "" : org.getTime().getDatePatternName()));
        	            line.add(new CSVFile.CSVField(org.getNrRooms() == 0 ? "" : org.getPlacement().getRoomName(", ")));
        	            line.add(new CSVFile.CSVField(sct.getSubpart().getName() + " " + sct.getName(i.getCourse().getId())));
        	            line.add(new CSVFile.CSVField(sct.getTime() == null ? "" : sct.getTime().getDayHeader() + " " + sct.getTime().getStartTimeHeader(useAmPm) + " - " + sct.getTime().getEndTimeHeader(useAmPm)));
        	            line.add(new CSVFile.CSVField(sct.getTime() == null ? "" : sct.getTime().getDatePatternName()));
        	            line.add(new CSVFile.CSVField(sct.getNrRooms() == 0 ? "" : sct.getPlacement().getRoomName(", ")));
        	            line.add(new CSVFile.CSVField(sDF.format(1.0 - getDifference(sct, i))));
        	            csv.addLine(line);
        			}
        		} else {
        			Iterator<Section> orgIt = i.getSections().iterator();
        			Iterator<Section> sctIt = e.getSections().iterator();
        			while (orgIt.hasNext() || sctIt.hasNext()) {
        				Section sct = (sctIt.hasNext() ? sctIt.next() : null);
        				Section org = (orgIt.hasNext() ? orgIt.next() : null);
        				List<CSVFile.CSVField> line = new ArrayList<CSVFile.CSVField>();
        				line.add(new CSVFile.CSVField(student.getId()));
        				line.add(new CSVFile.CSVField(student.getExternalId()));
        	            line.add(new CSVFile.CSVField(student.getName()));
        	            line.add(new CSVFile.CSVField(s == null ? null : s.getEmail()));
        	            line.add(new CSVFile.CSVField(i.getCourse().getName()));
        	            if (org != null) {
        	            	line.add(new CSVFile.CSVField(org.getSubpart().getName() + " " + org.getName(i.getCourse().getId())));
        	            	line.add(new CSVFile.CSVField(org.getTime() == null ? "" : org.getTime().getDayHeader() + " " + org.getTime().getStartTimeHeader(useAmPm) + " - " + org.getTime().getEndTimeHeader(useAmPm)));
            	            line.add(new CSVFile.CSVField(org.getTime() == null ? "" : org.getTime().getDatePatternName()));
        	            	line.add(new CSVFile.CSVField(org.getNrRooms() == 0 ? "" : org.getPlacement().getRoomName(", ")));
        	            } else {
        	            	line.add(new CSVFile.CSVField(""));
        	            	line.add(new CSVFile.CSVField(""));
        	            	line.add(new CSVFile.CSVField(""));
        	            	line.add(new CSVFile.CSVField(""));
        	            }
        	            if (sct != null) {
        	            	line.add(new CSVFile.CSVField(sct.getSubpart().getName() + " " + sct.getName(i.getCourse().getId())));
        	            	line.add(new CSVFile.CSVField(sct.getTime() == null ? "" : sct.getTime().getDayHeader() + " " + sct.getTime().getStartTimeHeader(useAmPm) + " - " + sct.getTime().getEndTimeHeader(useAmPm)));
            	            line.add(new CSVFile.CSVField(sct.getTime() == null ? "" : sct.getTime().getDatePatternName()));
        	            	line.add(new CSVFile.CSVField(sct.getNrRooms() == 0 ? "" : sct.getPlacement().getRoomName(", ")));
        	            	line.add(new CSVFile.CSVField(sDF.format(1.0 - getDifference(sct, i))));
        	            } else {
        	            	line.add(new CSVFile.CSVField(""));
        	            	line.add(new CSVFile.CSVField(""));
        	            	line.add(new CSVFile.CSVField(""));
        	            	line.add(new CSVFile.CSVField(""));
        	            	line.add(new CSVFile.CSVField(""));
        	            }
        	            csv.addLine(line);
        			}
        		}
        	}        	
        }
        
        return csv;
    }

}
