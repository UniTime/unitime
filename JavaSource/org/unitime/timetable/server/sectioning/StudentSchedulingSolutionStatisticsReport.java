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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.CSVFile.CSVField;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.extension.StudentQuality;
import org.cpsolver.studentsct.extension.TimeOverlapsCounter;
import org.cpsolver.studentsct.model.AcademicAreaCode;
import org.cpsolver.studentsct.model.AreaClassificationMajor;
import org.cpsolver.studentsct.model.Choice;
import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.Offering;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Request.RequestPriority;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.model.Subpart;
import org.cpsolver.studentsct.model.Unavailability;
import org.cpsolver.studentsct.report.StudentSectioningReport;

/**
 * @author Tomas Muller
 */
public class StudentSchedulingSolutionStatisticsReport implements StudentSectioningReport {
    private StudentSectioningModel iModel = null;
    protected static DecimalFormat sIntFormat = new DecimalFormat("#,##0");
    protected static DecimalFormat sPercentFormat = new DecimalFormat("0.00");
    protected static DecimalFormat sDoubleFormat = new DecimalFormat("0.00");

    public StudentSchedulingSolutionStatisticsReport(StudentSectioningModel model) {
        iModel = model;
    }

    public StudentSectioningModel getModel() {
        return iModel;
    }
    
    public static interface StudentFilter {
        public boolean matches(Student student);
    }
    
    public static class NotFilter implements StudentFilter {
        StudentFilter iFilter;
        public NotFilter(StudentFilter filter) {
            iFilter = filter;
        }
        @Override
        public boolean matches(Student student) {
            return !iFilter.matches(student);
        }
    }
    
    public static class OrFilter implements StudentFilter {
        StudentFilter[] iFilters;
        public OrFilter(StudentFilter... filters) {
            iFilters = filters;
        }
        @Override
        public boolean matches(Student student) {
            for (StudentFilter filter: iFilters)
                if (filter.matches(student)) return true;
            return false;
        }
    }
    
    public static class AndFilter implements StudentFilter {
        StudentFilter[] iFilters;
        public AndFilter(StudentFilter... filters) {
            iFilters = filters;
        }
        @Override
        public boolean matches(Student student) {
            for (StudentFilter filter: iFilters)
                if (!filter.matches(student)) return false;
            return true;
        }
    }
    
    public static class GroupFilter implements StudentFilter {
        private String iGroup;
        public GroupFilter(String group) {
            iGroup = group;
        }
        @Override
        public boolean matches(Student student) {
            for (AcademicAreaCode aac: student.getMinors())
                if (iGroup.equalsIgnoreCase(aac.getCode())) return true;
            return false;
        }
    }
    
    public static class PriorityFilter implements StudentFilter {
        public PriorityFilter() {
        }
        @Override
        public boolean matches(Student student) {
            return student.isPriority();
        }
    }
    
    public static class DummyFilter implements StudentFilter {
        public DummyFilter() {
        }
        @Override
        public boolean matches(Student student) {
            return student.isDummy();
        }
    }
    
    public static class DummyOrNoRequestsFilter implements StudentFilter {
        public DummyOrNoRequestsFilter() {
        }
        @Override
        public boolean matches(Student student) {
            return student.isDummy() || student.getRequests().isEmpty();
        }
    }
    
    public static class OnlineLateFilter implements StudentFilter {
        public OnlineLateFilter() {
        }
        @Override
        public boolean matches(Student student) {
        	boolean online = false;
        	for (AcademicAreaCode aac: student.getMinors()) {
                if ("SCONTONL".equalsIgnoreCase(aac.getCode())) { online = true; break; }
                if ("SCOVIDONL".equalsIgnoreCase(aac.getCode())) { online = true; break; }
        	}
        	if (!online) return false;
        	boolean hasOL = false;
        	boolean hasRS = false;
        	for (Request r: student.getRequests()) {
        		if (r instanceof CourseRequest) {
        			CourseRequest cr = (CourseRequest)r;
        			for (Course c: cr.getCourses()) {
        				if (c.getName().matches(".* [0-9]+I?OL(\\-[A-Za-z]+)?"))
        					hasOL = true;
        				else
        					hasRS = true;
        			}
        		}
        	}
        	return hasRS && !hasOL;
        }
    }
    
    public static class StarFilter implements StudentFilter {
        public StarFilter() {
        }
        @Override
        public boolean matches(Student student) {
        	for (AcademicAreaCode aac: student.getMinors()) {
                if (aac.getCode() != null && aac.getCode().startsWith("STAR")) return true;
                if (aac.getCode() != null && aac.getCode().startsWith("VSTAR")) return true;
        	}
        	return false;
        }
    }
    
    public static enum StudentGroup implements StudentFilter {
        ALL("All Students", new AndFilter(new NotFilter(new DummyOrNoRequestsFilter()), new NotFilter(new OnlineLateFilter()))),
        DUMMY("Projected Students", new DummyFilter()),
        PRIORITY("Priority Students", new PriorityFilter()),
        REBATCH("RE-BATCH", new AndFilter(new NotFilter(new DummyOrNoRequestsFilter()), new AndFilter(new GroupFilter("RE-BATCH"), new NotFilter(new GroupFilter("SCONTONL")), new NotFilter(new GroupFilter("SCOVIDONL"))))),
        SCONTONL("SCONTONL", new AndFilter(new NotFilter(new DummyOrNoRequestsFilter()), new GroupFilter("SCONTONL"), new NotFilter(new OnlineLateFilter()))),
        SCOVIDONL("SCOVIDONL", new AndFilter(new NotFilter(new DummyOrNoRequestsFilter()), new GroupFilter("SCOVIDONL"), new NotFilter(new OnlineLateFilter()))),
        PREREG("PREREG", new AndFilter(new NotFilter(new DummyOrNoRequestsFilter()), new AndFilter(new GroupFilter("PREREG"), new NotFilter(new GroupFilter("SCONTONL")), new NotFilter(new GroupFilter("SCOVIDONL")), new NotFilter(new GroupFilter("RE-BATCH")), new NotFilter(new StarFilter())))),
        STAR("STAR", new AndFilter(new NotFilter(new DummyOrNoRequestsFilter()), new AndFilter(new StarFilter(), new NotFilter(new GroupFilter("SCONTONL")), new NotFilter(new GroupFilter("SCOVIDONL")), new NotFilter(new GroupFilter("RE-BATCH"))))),
        OTHER("Other Students", new AndFilter(new NotFilter(new DummyOrNoRequestsFilter()), new NotFilter(new OrFilter(new GroupFilter("RE-BATCH"), new GroupFilter("SCONTONL"), new GroupFilter("SCOVIDONL"), new GroupFilter("PREREG"), new StarFilter())))),
        ;
        String iName;
        StudentFilter iFilter;
        StudentGroup(String name, StudentFilter filter) {
            iName = name;
            iFilter = filter;
        }
        public String getName() { return iName; }
        @Override
        public boolean matches(Student student) { return iFilter.matches(student); }
    }
    
    public static interface Statistic {
        public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment);
    }
    
    public static enum Statistics {
        NBR_STUDENTS(
        		"Number of Students",
        		"Number of students for which a schedule was computed",
        		new Statistic() {
            @Override
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment) {
                int count = 0;
                for (Student student: model.getStudents()) {
                    if (!group.matches(student)) continue;
                    count ++;
                }
                return new String[] {sIntFormat.format(count)};
            }
        }),
        COMPL_SCHEDULE(
        		new String[] {"Complete Schedule","- missing one course", "- missing two courses", "- missing three courses", "- missing four or more courses"},
        		new String[] {
        			"Percentage of students with a complete schedule (all requested courses assigned or reaching max credit)",
        			"Students that did not get a requested course",
        			"Students that did not get two requested courses"
        		},
        		new Statistic() {
            @Override
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment) {
                int total = 0;
                int[] missing = new int[] {0, 0, 0, 0};
                int complete = 0;
                for (Student student: model.getStudents()) {
                    if (!group.matches(student)) continue;
                    total ++;
                    int nrRequests = 0;
                    int nrAssignedRequests = 0;
                    for (Request r : student.getRequests()) {
                        if (!(r instanceof CourseRequest)) continue; // ignore free times
                        if (!r.isAlternative()) nrRequests++;
                        if (r.isAssigned(assignment)) nrAssignedRequests++;
                    }
                    if (nrAssignedRequests < nrRequests) {
                        missing[Math.min(nrRequests - nrAssignedRequests, missing.length) - 1] ++;
                    }
                    if (student.isComplete(assignment)) complete ++;
                }
                return new String[] {
                        sPercentFormat.format(100.0 * complete / total) + "%",
                        sPercentFormat.format(100.0 * missing[0] / total) + "%",
                        sPercentFormat.format(100.0 * missing[1] / total) + "%",
                        sPercentFormat.format(100.0 * missing[2] / total) + "%",
                        sPercentFormat.format(100.0 * missing[3] / total) + "%"
                };
            }
        }),
        REQUESTED_COURSES(
        		new String[] {
        				"Requested Courses", "- fixed",
        				"Courses per Student", "Assigned Courses", "- 1st choice", "- 2nd choice", "- 3rd choice", "- 4th+ choice"},
        		new String[] {
        				"Total number of requested courses by all students (not counting substitutes or alternatives)",
        				"Percentage of requested courses that were already enrolled (solver was not allowed to change)",
        				"The average number of course requested per student",
        				"Percentage of all course requests satisfied",
        				"Out of the above, the percentage of cases where the 1st choice course was given"
        		},
        		new Statistic() {
            @Override
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment) {
                int requests = 0, students = 0, assigned = 0;
                int fixed = 0;
                int[] assignedChoice = new int[] {0, 0, 0, 0};
                int assignedChoiceTotal = 0;
                for (Student student: model.getStudents()) {
                    if (!group.matches(student)) continue;
                    students ++;
                    for (Request r : student.getRequests()) {
                        if (!(r instanceof CourseRequest)) continue; // ignore free times
                        if (!r.isAlternative()) requests ++;
                        if (!r.isAlternative() && ((CourseRequest)r).isFixed()) fixed++;
                        Enrollment e = r.getAssignment(assignment);
                        if (e != null) {
                            assigned ++;
                            assignedChoice[Math.min(e.getTruePriority(), assignedChoice.length - 1)] ++;
                            assignedChoiceTotal ++;
                        }
                    }
                }
                if (requests == 0)
                	return new String[] {
                            sIntFormat.format(requests),
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            };
                return new String[] {
                        sIntFormat.format(requests),
                        (fixed == 0 ? "" : sPercentFormat.format(100.0 * fixed / requests) + "%"),
                        sDoubleFormat.format(((double)requests)/students),
                        sPercentFormat.format(100.0 * assigned / requests) + "%",
                        sPercentFormat.format(100.0 * assignedChoice[0] / assignedChoiceTotal) + "%",
                        sPercentFormat.format(100.0 * assignedChoice[1] / assignedChoiceTotal) + "%",
                        sPercentFormat.format(100.0 * assignedChoice[2] / assignedChoiceTotal) + "%",
                        sPercentFormat.format(100.0 * assignedChoice[3] / assignedChoiceTotal) + "%",
                        };
            }
        }),
        NOT_ASSIGNED_PRIORITY(
        		new String[] {"Not-assigned priority", "- 1st priority not assigned",
                "- 2nd priority not assigned", "- 3rd priority not assigned", "- 4th priority not assigned",
                "- 5th priority not assigned", "- 6th or later priority not assigned"},
        		new String[] {
        				"The average priority of the course requests that were not satisfied",
        				"Number of cases where a student did not get a 1st priority course",
        				"Number of cases where a student did not get a 2nd priority course"
        		},
        		new Statistic() {
            @Override
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment) {
                int[] notAssignedPriority = new int[] {0, 0, 0, 0, 0, 0};
                int notAssignedTotal = 0;
                int avgPriority = 0;
                for (Student student: model.getStudents()) {
                    if (!group.matches(student)) continue;
                    for (Request r : student.getRequests()) {
                        if (!(r instanceof CourseRequest)) continue; // ignore free times
                        Enrollment e = r.getAssignment(assignment);
                        if (e == null) {
                            if (!r.isAlternative()) {
                                notAssignedPriority[Math.min(r.getPriority(), notAssignedPriority.length - 1)] ++;
                                notAssignedTotal ++;
                                avgPriority += r.getPriority();
                            }
                        }
                    }
                }
                if (notAssignedTotal == 0)
                	return new String[] {
                            "",
                            "",
                            "",
                            "",
                            "",
                            "",
                            ""
                            };
                return new String[] {
                        sDoubleFormat.format(1.0 + ((double)avgPriority) / notAssignedTotal),
                        sIntFormat.format(notAssignedPriority[0]),
                        sIntFormat.format(notAssignedPriority[1]),
                        sIntFormat.format(notAssignedPriority[2]),
                        sIntFormat.format(notAssignedPriority[3]),
                        sIntFormat.format(notAssignedPriority[4]),
                        sIntFormat.format(notAssignedPriority[5])
                        };
            }
        }, true),
        ASSIGNED_COM(new String[] {"Assigned WC/OC", "Missing space in WC/OC"},
        		new String[] {
        				"Number of students enrolled in a WC/OC course",
        				"Number of unassigned course requests in written/oral communication courses"
        		},
        		new Statistic() {
            String[] sComCourses = new String[] {
                    "AMST 10100", "CLCS 23100", "CLCS 23700", "CLCS 33900",
                    "COM 11400", "COM 20400", "COM 21700", "EDCI 20500",
                    "EDPS 31500", "ENGL 10600", "ENGL 10800", "HONR 19903",
                    "PHIL 26000", "SCLA 10100", "SCLA 10200", "SPAN 33000"
            };
            private boolean isComCourse(Course course) {
                for (String c: sComCourses) {
                    if (course.getName().startsWith(c)) return true;
                }
                return false;
            }
            @Override
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment) {
                int assigned = 0, notAssigned = 0;
                for (Student student: model.getStudents()) {
                    if (!group.matches(student)) continue;
                    for (Request r : student.getRequests()) {
                        if (!(r instanceof CourseRequest)) continue; // ignore free times
                        CourseRequest cr = (CourseRequest)r;
                        if (isComCourse(cr.getCourses().get(0))) {
                            if (r.isAssigned(assignment)) assigned ++;
                            else if (student.canAssign(assignment, r) && !r.isAlternative()) notAssigned ++;
                        }
                    }
                }
                return new String[] { sIntFormat.format(assigned), sIntFormat.format(notAssigned) };
            }
        }, true),
        CRITICAL(new String[] {"Critical courses", "Assigned critical courses"},
        		new String[] {
        				"Number of course requests marked as critical (~ course/group/placeholder critical in degree plan)"
        		},
        		new Statistic() {
            @Override
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment) {
                int assigned = 0, total = 0;
                for (Student student: model.getStudents()) {
                    if (!group.matches(student)) continue;
                    for (Request r : student.getRequests()) {
                        if (!(r instanceof CourseRequest)) continue; // ignore free times
                        CourseRequest cr = (CourseRequest)r;
                        if (!cr.isAlternative() && cr.getRequestPriority() == RequestPriority.Critical) {
                            total ++;
                            if (cr.isAssigned(assignment)) assigned ++;
                        }
                    }
                }
                if (total == 0) return new String[] { "N/A", ""};
                return new String[] { sIntFormat.format(total), sPercentFormat.format(100.0 * assigned / total) + "%" };
            }
        }),
        IMPORTANT(new String[] {"Important courses", "Assigned important courses"},
        		new String[] {
        				"Number of course requests marked as important (~ course/group/placeholder critical in the first choice major)"
        		},
        		new Statistic() {
            @Override
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment) {
                int assigned = 0, total = 0;
                for (Student student: model.getStudents()) {
                    if (!group.matches(student)) continue;
                    for (Request r : student.getRequests()) {
                        if (!(r instanceof CourseRequest)) continue; // ignore free times
                        CourseRequest cr = (CourseRequest)r;
                        if (!cr.isAlternative() && cr.getRequestPriority() == RequestPriority.Important) {
                            total ++;
                            if (cr.isAssigned(assignment)) assigned ++;
                        }
                    }
                }
                if (total == 0) return new String[] { "N/A", ""};
                return new String[] { sIntFormat.format(total), sPercentFormat.format(100.0 * assigned / total) + "%" };
            }
        }, true),
        PREFERENCES(new String[] {"Course requests with preferences", "Satisfied preferences", "- instructional method", "- classes"},
        		new String[] {
        				"Course requests with IM or section preferences",
        				"Percentage of satisfied preferences (both class and IM)",
        				"Percentage of cases when the preferred instructional method was given to the student",
        				"Percentage of cases when the preferred class was given to the student"
        		},
        		new Statistic() {
            @Override
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment) {
                int prefs = 0, configPrefs = 0, sectionPrefs = 0;
                double sectionPref = 0.0, configPref = 0.0;
                double satisfied = 0.0;
                for (Student student: model.getStudents()) {
                    if (!group.matches(student)) continue;
                    for (Request r : student.getRequests()) {
                        if (!(r instanceof CourseRequest)) continue; // ignore free times
                        CourseRequest cr = (CourseRequest)r;
                        Enrollment e = r.getAssignment(assignment);
                        if (e != null) {
                            if (r.hasSelection()) {
                                prefs ++;
                                satisfied += //0.3 * e.percentSelectedSameConfig() + 0.7 * e.percentSelectedSameSection(); 
                                		e.percentSelected();
                                for (Choice ch: cr.getSelectedChoices()) {
                                    if (ch.getConfigId() != null) {
                                        configPrefs ++;
                                        configPref += e.percentSelectedSameConfig();
                                        break;
                                    }
                                }
                                for (Choice ch: cr.getSelectedChoices()) {
                                    if (ch.getSectionId() != null) {
                                        sectionPrefs ++;
                                        sectionPref += e.percentSelectedSameSection();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                if (prefs == 0) return new String[] { "N/A", "", "", ""};
                return new String[] { sIntFormat.format(prefs), sPercentFormat.format(100.0 * satisfied / prefs) + "%",
                        sPercentFormat.format(100.0 * sectionPref / sectionPrefs) + "%",
                        sPercentFormat.format(100.0 * configPref / configPrefs) + "%"
                };
            }
        }, true),
        BALANCING("Unbalanced sections", "Classes dis-balanced by 10% or more",
        	new Statistic() {
            @Override
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment) {
                int disb10Sections = 0;
                int totalSections = 0;
                for (Offering offering: model.getOfferings()) {
                    for (Config config: offering.getConfigs()) {
                        double enrl = 0;
                        for (Enrollment e: config.getEnrollments(assignment)) {
                            if (group.matches(e.getStudent())) enrl += e.getRequest().getWeight();
                        }
                        for (Subpart subpart: config.getSubparts()) {
                            if (subpart.getSections().size() <= 1) continue;
                            if (subpart.getLimit() > 0) {
                                // sections have limits -> desired size is section limit x (total enrollment / total limit)
                                double ratio = enrl / subpart.getLimit();
                                for (Section section: subpart.getSections()) {
                                    double sectEnrl = 0;
                                    for (Enrollment e: section.getEnrollments(assignment)) {
                                        if (group.matches(e.getStudent())) sectEnrl += e.getRequest().getWeight();
                                    }
                                    double desired = ratio * section.getLimit();
                                    if (Math.abs(desired - sectEnrl) >= Math.max(1.0, 0.1 * section.getLimit())) {
                                        disb10Sections++;
                                    }
                                    totalSections++;
                                }
                            } else {
                                // unlimited sections -> desired size is total enrollment / number of sections
                                for (Section section: subpart.getSections()) {
                                    double sectEnrl = 0;
                                    for (Enrollment e: section.getEnrollments(assignment)) {
                                        if (group.matches(e.getStudent())) sectEnrl += e.getRequest().getWeight();
                                    }
                                    double desired = enrl / subpart.getSections().size();
                                    if (Math.abs(desired - sectEnrl) >= Math.max(1.0, 0.1 * desired)) {
                                        disb10Sections++;
                                    }
                                    totalSections++;
                                }
                            }
                        }
                    }
                }
                return new String[] { sPercentFormat.format(100.0 * disb10Sections / totalSections) + "%" };
            }
        }),
        DISTANCE(new String[] {"Distance conflicts", "- students with a free time conflict", "- average minutes", "- students with a course time conflict", "- average minutes"},
        		new String[] {
        				"Total number of distance conflicts",
        				"Total number of students with a free time conflict",
        				"For those with a free time conflict, the average number of overlapping minutes",
        				"Total number of students with a course conflict",
        				"For those with a course time conflict, the average number of overlapping minutes"
        		},
        		new Statistic() {
            @Override
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment) {
                if (model.getStudentQuality() != null) {
                	int dc = 0;
                	Set<Student> dcFt = new HashSet<Student>();
                	Set<Student> dcCourse = new HashSet<Student>();
                    int ftMin = 0, courseMin = 0;
                    for (StudentQuality.Conflict c: model.getStudentQuality().getContext(assignment).computeAllConflicts(StudentQuality.Type.CourseTimeOverlap, assignment)) {
                        if (group.matches(c.getStudent())) {
                        	dcCourse.add(c.getStudent());
                            courseMin += 5 * c.getPenalty();
                            dc ++;
                        }
                    }
                    for (StudentQuality.Conflict c: model.getStudentQuality().getContext(assignment).computeAllConflicts(StudentQuality.Type.FreeTimeOverlap, assignment)) {
                        if (group.matches(c.getStudent())) {
                        	dcFt.add(c.getStudent());
                            ftMin += 5 * c.getPenalty();
                            dc ++;
                        }
                    }
                    for (StudentQuality.Conflict c: model.getStudentQuality().getContext(assignment).computeAllConflicts(StudentQuality.Type.Unavailability, assignment)) {
                    	if (group.matches(c.getStudent())) {
                            dc ++;
                        }
                    }
                    return new String[] {
                            sIntFormat.format(dc),
                            sIntFormat.format(dcFt.size()),
                            (dcFt.isEmpty() ? "" : sDoubleFormat.format(((double)ftMin) / dcFt.size())),
                            sIntFormat.format(dcCourse.size()),
                            (dcCourse.isEmpty() ? "" : sDoubleFormat.format(((double)courseMin) / dcCourse.size()))
                    };
                } else if (model.getTimeOverlaps() != null && model.getTimeOverlaps().getTotalNrConflicts(assignment) != 0) {
                	int dc = 0;
                	Set<Student> dcFt = new HashSet<Student>();
                	Set<Student> dcCourse = new HashSet<Student>();
                	int ftMin = 0, courseMin = 0;
                    Set<TimeOverlapsCounter.Conflict> conf = model.getTimeOverlaps().getContext(assignment).computeAllConflicts(assignment);
                    for (TimeOverlapsCounter.Conflict c: conf) {
                        if (group.matches(c.getStudent())) {
                            if (c.getR1() instanceof CourseRequest && c.getR2() instanceof CourseRequest) {
                                courseMin += 5 * c.getShare();
                                dcCourse.add(c.getStudent());
                                dc ++;
                            } else if (c.getS2() instanceof Unavailability) {
                            	dc ++;
                            } else {
                                ftMin += 5 * c.getShare();
                                dcFt.add(c.getStudent());
                                dc ++;
                            }
                        }
                    }
                    return new String[] {
                            sIntFormat.format(dc),
                            sIntFormat.format(dcFt.size()),
                            (dcFt.isEmpty() ? "" : sDoubleFormat.format(((double)ftMin) / dcFt.size())),
                            sIntFormat.format(dcCourse.size()),
                            (dcCourse.isEmpty() ? "" : sDoubleFormat.format(((double)courseMin) / dcCourse.size()))
                    };
                } else {
                    return new String[] { "N/A", "", "" };
                }
            }
        }, true),
        CREDITS(new String[] { "Students requesting 12+ credits", "- 12+ credits assigned", "Students requesting 15+ credits", "- 15+ credits assigned" },
        		new String[] {
        				"Total number of students requesting 12 or more credit hours",
        				"Out of these, the percentage of students having 12 or more credits assigned",
        				"Total number of students requesting 15 or more credit hours",
        				"Out of these, the percentage of students having 15 or more credits assigned",
        		},
        		new Statistic() {
            @Override
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment) {
                int total12 = 0, assigned12 = 0;
                int total15 = 0, assigned15 = 0;
                for (Student student: model.getStudents()) {
                    if (!group.matches(student)) continue;
                    float credit = 0;
                    float assignedCredit = 0;
                    for (Request r : student.getRequests()) {
                        if (!(r instanceof CourseRequest)) continue; // ignore free times
                        CourseRequest cr = (CourseRequest)r;
                        if (!cr.isAlternative()) {
                            Course c = cr.getCourses().get(0);
                            if (c.hasCreditValue())
                                credit += c.getCreditValue();
                            else
                                credit += cr.getMinCredit();
                        }
                        Enrollment e = cr.getAssignment(assignment);
                        if (e != null) {
                            assignedCredit += e.getCredit();
                        }
                    }
                    if (credit >= 12f) {
                        total12 ++;
                        if (assignedCredit >= 12f)
                            assigned12 ++;
                    }
                    if (credit >= 15f) {
                        total15 ++;
                        if (assignedCredit >= 15f)
                            assigned15 ++;
                    }
                }
                return new String[] {
                        sIntFormat.format(total12),
                        (total12 == 0 ? "" : sPercentFormat.format(100.0 * assigned12 / total12) + "%"),
                        sIntFormat.format(total15),
                        (total15 == 0 ? "" : sPercentFormat.format(100.0 * assigned15 / total15) + "%"),
                };
            }
        }, true),
        F2F(new String[] {"Students with no face-to-face classes", "Students with <50% classes face-to-face"},
        		new String[] {
        			"Total number of undergraduate students with no face-to-face classes.",
        			"Total number of undergraduate students with less than half of their schedule face-to-face."
        		},
        		new Statistic() {
            @Override
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment) {
                int online = 0;
                int half = 0;
                for (Student student: model.getStudents()) {
                    if (!group.matches(student)) continue;
                    boolean gr = false;
                    for (AreaClassificationMajor acm: student.getAreaClassificationMajors()) {
                    	if (acm.getClassification().startsWith("G") || acm.getClassification().startsWith("P")) gr = true;
                    }
                    if (gr) continue;
                    int sections = 0, onlineSections = 0;
                    for (Request r : student.getRequests()) {
                        if (!(r instanceof CourseRequest)) continue; // ignore free times
                        Enrollment e = r.getAssignment(assignment);
                        if (e != null)
                            for (Section s: e.getSections()) {
                                sections ++;
                                if (s.isOnline()) onlineSections ++;
                            }
                    }
                    if (sections > 0) {
                        if (onlineSections == sections) {
                        	online ++;
                        	/*
                        	if (group == StudentGroup.REBATCH) {
                        		System.out.println(student.getExternalId() + ": " + student.getName() + " [" + sections + "]");
                        		for (AreaClassificationMajor acm: student.getAreaClassificationMajors()) {
                        			System.out.println("-- " + acm.getArea() + "/" + acm.getMajor() + " " + acm.getClassification());
                                }
                        		for (AcademicAreaCode aac: student.getMinors()) {
                        			System.out.println("-- " + aac.getCode());
                        		}
                        	}
                        	*/
                        } else {
                        	/*
                        	if (group == StudentGroup.SCONTONL || group == StudentGroup.SCOVIDONL) {
                        		System.out.println(student.getExternalId() + ": " + student.getName() + " [" + sections + "]");
                        		for (AreaClassificationMajor acm: student.getAreaClassificationMajors()) {
                        			System.out.println("-- " + acm.getArea() + "/" + acm.getMajor() + " " + acm.getClassification());
                                }
                        		for (AcademicAreaCode aac: student.getMinors()) {
                        			System.out.println("-- " + aac.getCode());
                        		}
                        	}*/
                        }
                        if (onlineSections > 0.5 * sections) half++;
                    }
                }
                if (half == 0) return new String[] { "", ""};
                return new String[] {
                        sIntFormat.format(online),
                        sIntFormat.format(half)
                };
            }
        }),
    
        
        ;
        String[] iNames;
        String[] iNotes;
        Statistic iStatistic;
        boolean iNewLine = false;
        Statistics(String[] names, String notes[], Statistic stat, boolean nl) {
            iNames = names; iNotes = notes; iStatistic = stat; iNewLine = nl;
        }
        Statistics(String name, String note, Statistic stat, boolean nl) {
            this(new String[] {name}, new String[] {note}, stat, nl);
        }
        Statistics(String[] names, String notes[], Statistic stat) {
            this(names, notes, stat, false);
        }
        Statistics(String name, String note, Statistic stat) {
            this(name, note, stat, false);
        }
        public String[] getNames() { return iNames; }
        public String[] getNotes() { return iNotes; }
        public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment) {
            return iStatistic.getValues(group, model, assignment);
        }
        public boolean isNewLine() { return iNewLine; }
    }
    
    @Override
    public CSVFile create(Assignment<Request, Enrollment> assignment, DataProperties properties) {
        CSVFile csv = new CSVFile();
        List<CSVField> header = new ArrayList<CSVField>();
        List<StudentGroup> groups = new ArrayList<StudentGroup>();
        header.add(new CSVField(""));
        Map<Integer, StudentGroup> counts = new HashMap<Integer, StudentGroup>();
        for (StudentGroup g: StudentGroup.values()) {
            int nrStudents = 0;
            for (Student student: getModel().getStudents()) {
                if (g.matches(student)) nrStudents ++;
            }
            if (nrStudents > 0 && !counts.containsKey(nrStudents)) {
                groups.add(g);
                header.add(new CSVField(g.getName()));
                counts.put(nrStudents, g);
            }
        }
        header.add(new CSVField("Note"));
        csv.setHeader(header);
        for (Statistics stat: Statistics.values()) {
            String[] names = stat.getNames();
            List<List<CSVField>> table = new ArrayList<List<CSVField>>();
            for (String name: names) {
                List<CSVField> line = new ArrayList<CSVField>(); line.add(new CSVField(name)); 
                table.add(line);
            }
            for (StudentGroup g: groups) {
                String[] values = stat.getValues(g, getModel(), assignment);
                for (int i = 0; i < values.length; i++) {
                    table.get(i).add(new CSVField(values[i]));
                }
            }
            String[] notes = stat.getNotes();
            for (int i = 0; i < notes.length; i++) {
                table.get(i).add(new CSVField(notes[i]));
            }
            for (List<CSVField> line: table) {
                csv.addLine(line);
            }
            if (stat.isNewLine())
                csv.addLine(new CSVField[] {new CSVField(" ")});
        }
        return csv;
    }

}
