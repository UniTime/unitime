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

import org.cpsolver.coursett.Constants;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.ifs.assignment.Assignment;
import org.cpsolver.ifs.util.CSVFile;
import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.CSVFile.CSVField;
import org.cpsolver.studentsct.StudentSectioningModel;
import org.cpsolver.studentsct.extension.DistanceConflict;
import org.cpsolver.studentsct.extension.TimeOverlapsCounter;
import org.cpsolver.studentsct.extension.TimeOverlapsCounter.Conflict;
import org.cpsolver.studentsct.model.AreaClassificationMajor;
import org.cpsolver.studentsct.model.Choice;
import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Offering;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.SctAssignment;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Request.RequestPriority;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.model.Student.StudentPriority;
import org.cpsolver.studentsct.model.Subpart;
import org.cpsolver.studentsct.model.Unavailability;
import org.cpsolver.studentsct.report.AbstractStudentSectioningReport;
import org.cpsolver.studentsct.report.StudentSectioningReport;

/**
 * @author Tomas Muller
 */
public class StudentSchedulingSolutionStatisticsReport extends AbstractStudentSectioningReport {
    protected static DecimalFormat sIntFormat = new DecimalFormat("#,##0");
    protected static DecimalFormat sPercentFormat = new DecimalFormat("0.00");
    protected static DecimalFormat sDoubleFormat = new DecimalFormat("0.00");

    public StudentSchedulingSolutionStatisticsReport(StudentSectioningModel model) {
        super(model);
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
            for (org.cpsolver.studentsct.model.StudentGroup g: student.getGroups())
                if (iGroup.equalsIgnoreCase(g.getReference())) return true;
            return false;
        }
    }
    
    public static class PriorityFilter implements StudentFilter {
    	private StudentPriority iPriority;
        public PriorityFilter(StudentPriority p) {
        	iPriority = p;
        }
        @Override
        public boolean matches(Student student) {
            return student.getPriority() == iPriority;
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
    
    public static class OnlineFilter implements StudentFilter {
        public OnlineFilter() {
        }
        @Override
        public boolean matches(Student student) {
        	for (org.cpsolver.studentsct.model.StudentGroup aac: student.getGroups()) {
                if ("SCOVIDONL".equalsIgnoreCase(aac.getReference())) return true;
                if ("SCONTONL".equalsIgnoreCase(aac.getReference())) return true;
                if ("SCOVIDPMPE".equalsIgnoreCase(aac.getReference())) return true;
        	}
        	return false;
        }
    }
    
    public static class AthletesFilter implements StudentFilter {
        public AthletesFilter() {
        }
        @Override
        public boolean matches(Student student) {
        	for (org.cpsolver.studentsct.model.StudentGroup aac: student.getGroups()) {
        		if ("SPORT".equalsIgnoreCase(aac.getType())) return true;
        	}
        	return false;
        }
    }
    
    public static class OnlineLateFilter extends OnlineFilter {
        public OnlineLateFilter() {
        }
        @Override
        public boolean matches(Student student) {
        	if (!super.matches(student)) return false;
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
        	for (org.cpsolver.studentsct.model.StudentGroup aac: student.getGroups()) {
                if (aac.getReference() != null && aac.getReference().startsWith("STAR")) return true;
                if (aac.getReference() != null && aac.getReference().startsWith("VSTAR")) return true;
                if (aac.getReference() != null && aac.getReference().startsWith("NewStCRF")) return true;
                if (aac.getReference() != null && aac.getReference().startsWith("NewStOther")) return true;
        	}
        	return false;
        }
    }
    
    private static StudentFilter FILTER_ALL = new AndFilter(new NotFilter(new DummyOrNoRequestsFilter()), new NotFilter(new OnlineLateFilter()));
    private static StudentFilter FILTER_ALL_RES = new AndFilter(new NotFilter(new DummyOrNoRequestsFilter()), new NotFilter(new OnlineFilter()));
    
    public enum StudentGroup implements StudentFilter {
        ALL("All Students", FILTER_ALL),
        
        DUMMY("Projected", new DummyFilter()),
        // ONLINE_LATE("Online-Late", new OnlineLateFilter()),
        
        PRIORITY("Priority", new AndFilter(new PriorityFilter(StudentPriority.Priority), FILTER_ALL)),
        SENIOR("Seniors", new AndFilter(new PriorityFilter(StudentPriority.Senior), FILTER_ALL)),
        JUNIOR("Juniors", new AndFilter(new PriorityFilter(StudentPriority.Junior), FILTER_ALL)),
        SOPHOMORE("Sophomores", new AndFilter(new PriorityFilter(StudentPriority.Sophomore), FILTER_ALL)),
        FRESHMEN("Freshmen", new AndFilter(new PriorityFilter(StudentPriority.Freshmen), FILTER_ALL)),
        NORMAL("Non-priority", new AndFilter(new PriorityFilter(StudentPriority.Normal), FILTER_ALL)),
        
        REBATCH("RE-BATCH", new AndFilter(new GroupFilter("RE-BATCH"), FILTER_ALL_RES)),
        ONLINE("Online", new AndFilter(new OnlineFilter(), FILTER_ALL)),
        	GR_SCONTONL("SCONTONL", new AndFilter(new GroupFilter("SCONTONL"), FILTER_ALL)),
        	GR_SCOVIDONL("SCOVIDONL", new AndFilter(new GroupFilter("SCOVIDONL"), FILTER_ALL)),
        	GR_SCOVIDPMPE("SCOVIDPMPE", new AndFilter(new GroupFilter("SCOVIDPMPE"), FILTER_ALL)),
        PREREG("PREREG", new AndFilter(new GroupFilter("PREREG"), FILTER_ALL_RES, new NotFilter(new StarFilter()))),
        STAR("STAR", new AndFilter(new StarFilter(), FILTER_ALL_RES, new NotFilter(new GroupFilter("RE-BATCH")))),
        	GR_STAR("On-campus STAR", new AndFilter(new GroupFilter("STAR"), FILTER_ALL_RES, new NotFilter(new GroupFilter("RE-BATCH")))),
        	GR_VSTAR("Virtual STAR", new AndFilter(new GroupFilter("VSTAR"), FILTER_ALL_RES, new NotFilter(new GroupFilter("RE-BATCH")))),
        OTHER("Other", new AndFilter(FILTER_ALL_RES, new NotFilter(new GroupFilter("RE-BATCH")), new NotFilter(new GroupFilter("PREREG")), new NotFilter(new StarFilter()))),
        
        ATHLETES("Athletes", new AndFilter(new AthletesFilter(), FILTER_ALL)),
        PRIORITY_ATHLETES("Priority\nAthletes", new AndFilter(new AthletesFilter(), new PriorityFilter(StudentPriority.Priority), FILTER_ALL)),
        OTHER_ATHLETES("Other\nAthletes", new AndFilter(new AthletesFilter(), new NotFilter(new PriorityFilter(StudentPriority.Priority)), FILTER_ALL)),
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
        public boolean matches(Student student, StudentSectioningReport.Filter filter) { return iFilter.matches(student) && filter.matches(student); }
    }
    
    public static interface Statistic {
        public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment, StudentSectioningReport.Filter filter);
    }
    
    public enum Statistics {
        NBR_STUDENTS(
        		"Number of Students",
        		"Number of students for which a schedule was computed",
        		new Statistic() {
            @Override
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment, StudentSectioningReport.Filter filter) {
                int count = 0;
                for (Student student: model.getStudents()) {
                    if (!group.matches(student, filter)) continue;
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
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment, StudentSectioningReport.Filter filter) {
                int total = 0;
                int[] missing = new int[] {0, 0, 0, 0};
                int complete = 0;
                for (Student student: model.getStudents()) {
                    if (!group.matches(student, filter)) continue;
                    total ++;
                    int nrRequests = 0;
                    int nrAssignedRequests = 0;
                    for (Request r : student.getRequests()) {
                        if (!(r instanceof CourseRequest)) continue; // ignore free times
                        if (!filter.matches(r)) continue; // check the filter
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
        				"Requested Courses", "- pre-enrolled", "- impossible",
        				"Courses per Student", "Assigned Courses", "- 1st choice", "- 2nd choice", "- 3rd choice", "- 4th+ choice", "- substitute"},
        		new String[] {
        				"Total number of requested courses by all students (not counting substitutes or alternatives)",
        				"Percentage of requested courses that were already enrolled (solver was not allowed to change)",
        				"Percentage of requested courses that have no possible enrollment (e.g., due to having all classes disabled)",
        				"The average number of course requested per student",
        				"Percentage of all course requests satisfied",
        				"Out of the above, the percentage of cases where the 1st choice course was given",
        				"2nd choice (1st alternative) course was given", "3rd choice course was given", "4th or later choice was given",
        				"a substitute course was given instead",
        		},
        		new Statistic() {
            @Override
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment, StudentSectioningReport.Filter filter) {
                int requests = 0, students = 0, assigned = 0;
                int fixed = 0, initial = 0;
                int noenrl = 0;
                int[] assignedChoice = new int[] {0, 0, 0, 0};
                int assignedSubst = 0;
                int assignedChoiceTotal = 0;
                for (Student student: model.getStudents()) {
                    if (!group.matches(student, filter)) continue;
                    students ++;
                    for (Request r : student.getRequests()) {
                        if (!(r instanceof CourseRequest)) continue; // ignore free times
                        if (!filter.matches(r)) continue; // check the filter
                        if (!r.isAlternative()) requests ++;
                        if (!r.isAlternative() && ((CourseRequest)r).isFixed()) fixed++;
                        if (!r.isAlternative() && ((CourseRequest)r).computeRandomEnrollments(assignment, 1).isEmpty()) noenrl ++;
                        Enrollment e = r.getAssignment(assignment);
                        if (r.getInitialAssignment() != null && r.getInitialAssignment().equals(e)) initial ++;
                        if (e != null) {
                        	assigned ++;
                        	if (r.isAlternative())
                        		assignedSubst ++;
                        	else
                        		assignedChoice[Math.min(e.getTruePriority(), assignedChoice.length - 1)] ++;
                            assignedChoiceTotal ++;
                        }
                    }
                }
                if (fixed == 0 && initial > 0)
                	fixed = initial;
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
                            "",
                            "",
                            };
                return new String[] {
                        sIntFormat.format(requests),
                        (fixed == 0 ? "" : sPercentFormat.format(100.0 * fixed / requests) + "%"),
                        (noenrl == 0 ? "" : sPercentFormat.format(100.0 * noenrl / requests) + "%"),
                        sDoubleFormat.format(((double)requests)/students),
                        sPercentFormat.format(100.0 * assigned / requests) + "%",
                        sPercentFormat.format(100.0 * assignedChoice[0] / assignedChoiceTotal) + "%",
                        sPercentFormat.format(100.0 * assignedChoice[1] / assignedChoiceTotal) + "%",
                        sPercentFormat.format(100.0 * assignedChoice[2] / assignedChoiceTotal) + "%",
                        sPercentFormat.format(100.0 * assignedChoice[3] / assignedChoiceTotal) + "%",
                        sPercentFormat.format(100.0 * assignedSubst / assignedChoiceTotal) + "%",
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
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment, StudentSectioningReport.Filter filter) {
                int[] notAssignedPriority = new int[] {0, 0, 0, 0, 0, 0};
                int notAssignedTotal = 0;
                int avgPriority = 0;
                for (Student student: model.getStudents()) {
                    if (!group.matches(student, filter)) continue;
                    for (Request r : student.getRequests()) {
                        if (!(r instanceof CourseRequest)) continue; // ignore free times
                        if (!filter.matches(r)) continue; // check the filter
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
                    "PHIL 26000", "SCLA 10100", "SCLA 10200", "SPAN 33000",
                    "EDCI 49600", "EDCI 49800",  "EDPS 49800", "ENGL 30400",
                    "ENGL 38000", "HDFS 45000",
            };
            private boolean isComCourse(Course course) {
                for (String c: sComCourses) {
                    if (course.getName().startsWith(c)) return true;
                }
                return false;
            }
            @Override
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment, StudentSectioningReport.Filter filter) {
            	
            	
                int assigned = 0, notAssigned = 0;
                for (Student student: model.getStudents()) {
                    if (!group.matches(student, filter)) continue;
                    for (Request r : student.getRequests()) {
                        if (!(r instanceof CourseRequest)) continue; // ignore free times
                        if (!filter.matches(r)) continue; // check the filter
                        CourseRequest cr = (CourseRequest)r;
                        Enrollment e = cr.getAssignment(assignment);
                        if (e != null && isComCourse(e.getCourse())) {
                        	assigned ++;
                        } else if (e == null && isComCourse(cr.getCourses().get(0)) && student.canAssign(assignment, r) && !r.isAlternative()) {
                        	notAssigned ++;
                        }
                    }
                }
                return new String[] { sIntFormat.format(assigned), sIntFormat.format(notAssigned) };
            }
        }, true),
        LC(new String[] {"LC courses", "Assigned LC courses"},
        		new String[] {
        				"Number of course requests with a matching LC reservation"
        		},
        		new Statistic() {
            @Override
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment, StudentSectioningReport.Filter filter) {
                int assigned = 0, total = 0;
                for (Student student: model.getStudents()) {
                    if (!group.matches(student, filter)) continue;
                    for (Request r : student.getRequests()) {
                        if (!(r instanceof CourseRequest)) continue; // ignore free times
                        if (!filter.matches(r)) continue; // check the filter
                        CourseRequest cr = (CourseRequest)r;
                        if (!cr.isAlternative() && cr.getRequestPriority() == RequestPriority.LC) {
                            total ++;
                            if (cr.isAssigned(assignment)) assigned ++;
                        }
                    }
                }
                if (total == 0) return new String[] { "N/A", ""};
                return new String[] { sIntFormat.format(total), sPercentFormat.format(100.0 * assigned / total) + "%" };
            }
        }),
        CRITICAL(new String[] {"Critical courses", "Assigned critical courses"},
        		new String[] {
        				"Number of course requests marked as critical (~ course/group/placeholder critical in degree plan)"
        		},
        		new Statistic() {
            @Override
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment, StudentSectioningReport.Filter filter) {
                int assigned = 0, total = 0;
                for (Student student: model.getStudents()) {
                    if (!group.matches(student, filter)) continue;
                    for (Request r : student.getRequests()) {
                        if (!(r instanceof CourseRequest)) continue; // ignore free times
                        if (!filter.matches(r)) continue; // check the filter
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
        VITAL(new String[] {"Vital courses", "Assigned vital courses"},
        		new String[] {
        				"Number of course requests marked as vital by advisors"
        		},
        		new Statistic() {
            @Override
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment, StudentSectioningReport.Filter filter) {
                int assigned = 0, total = 0;
                for (Student student: model.getStudents()) {
                    if (!group.matches(student, filter)) continue;
                    for (Request r : student.getRequests()) {
                        if (!(r instanceof CourseRequest)) continue; // ignore free times
                        if (!filter.matches(r)) continue; // check the filter
                        CourseRequest cr = (CourseRequest)r;
                        if (!cr.isAlternative() && cr.getRequestPriority() == RequestPriority.Vital) {
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
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment, StudentSectioningReport.Filter filter) {
                int assigned = 0, total = 0;
                for (Student student: model.getStudents()) {
                    if (!group.matches(student, filter)) continue;
                    for (Request r : student.getRequests()) {
                        if (!(r instanceof CourseRequest)) continue; // ignore free times
                        if (!filter.matches(r)) continue; // check the filter
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
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment, StudentSectioningReport.Filter filter) {
                int prefs = 0, configPrefs = 0, sectionPrefs = 0;
                double sectionPref = 0.0, configPref = 0.0;
                double satisfied = 0.0;
                for (Student student: model.getStudents()) {
                    if (!group.matches(student, filter)) continue;
                    for (Request r : student.getRequests()) {
                        if (!(r instanceof CourseRequest)) continue; // ignore free times
                        if (!filter.matches(r)) continue; // check the filter
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
        BALANCING(new String[] {"Unbalanced sections", "- average disbalance"},
        		new String[] {"Classes dis-balanced by 10% or more", "Average difference between target and actual enrollment in the section"},
        	new Statistic() {
            @Override
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment, StudentSectioningReport.Filter filter) {
            	double disbWeight = 0;
            	int disb10Sections = 0;
                int totalSections = 0;
                for (Offering offering: model.getOfferings()) {
                	if (offering.isDummy()) continue;
                    for (Config config: offering.getConfigs()) {
                        double enrl = 0;
                        for (Enrollment e: config.getEnrollments(assignment)) {
                            if (group.matches(e.getStudent()) && filter.matches(e.getRequest(), e)) enrl += e.getRequest().getWeight();
                        }
                        for (Subpart subpart: config.getSubparts()) {
                            if (subpart.getSections().size() <= 1) continue;
                            if (subpart.getLimit() > 0) {
                                // sections have limits -> desired size is section limit x (total enrollment / total limit)
                                double ratio = enrl / subpart.getLimit();
                                for (Section section: subpart.getSections()) {
                                    double sectEnrl = 0;
                                    for (Enrollment e: section.getEnrollments(assignment)) {
                                        if (group.matches(e.getStudent()) && filter.matches(e.getRequest(), e)) sectEnrl += e.getRequest().getWeight();
                                    }
                                    double desired = ratio * section.getLimit();
                                    disbWeight += Math.abs(sectEnrl - desired);
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
                                        if (group.matches(e.getStudent()) && filter.matches(e.getRequest(), e)) sectEnrl += e.getRequest().getWeight();
                                    }
                                    double desired = enrl / subpart.getSections().size();
                                    disbWeight += Math.abs(sectEnrl - desired);
                                    if (Math.abs(desired - sectEnrl) >= Math.max(1.0, 0.1 * desired)) {
                                        disb10Sections++;
                                    }
                                    totalSections++;
                                }
                            }
                        }
                    }
                }
                return new String[] {
                		sPercentFormat.format(100.0 * disb10Sections / totalSections) + "%",
                		sDoubleFormat.format(disbWeight / totalSections)
                };
            }
        }, true),
        DISTANCE(new String[] {"Distance conflicts", "- students with distance conflicts", "- average distance in minutes",
        		"Distance conflicts (SD)", "- students with distance conflicts", "- average distance in minutes"
        }, new String[] {"Total number of distance conflicts",
        		"Total number of students with one or more distance conflicts",
        		"Average distance between two classes in minutes per conflict",
        		"Total number of distance conflicts (students needed short distances)",
        		"Total number of SD students with one or more distance conflicts",
        		"Average distance between two classes in minutes per conflict"},
        		new Statistic() {
        	
            protected int getDistanceInMinutes(StudentSectioningModel model, RoomLocation r1, RoomLocation r2) {
                if (r1.getId().compareTo(r2.getId()) > 0) return getDistanceInMinutes(model, r2, r1);
                if (r1.getId().equals(r2.getId()) || r1.getIgnoreTooFar() || r2.getIgnoreTooFar())
                    return 0;
                if (r1.getPosX() == null || r1.getPosY() == null || r2.getPosX() == null || r2.getPosY() == null)
                    return model.getDistanceMetric().getMaxTravelDistanceInMinutes();
                return  model.getDistanceMetric().getDistanceInMinutes(r1.getId(), r1.getPosX(), r1.getPosY(), r2.getId(), r2.getPosX(), r2.getPosY());
            }

            protected int getDistanceInMinutes(StudentSectioningModel model, Placement p1, Placement p2) {
                if (p1.isMultiRoom()) {
                    if (p2.isMultiRoom()) {
                        int dist = 0;
                        for (RoomLocation r1 : p1.getRoomLocations()) {
                            for (RoomLocation r2 : p2.getRoomLocations()) {
                                dist = Math.max(dist, getDistanceInMinutes(model, r1, r2));
                            }
                        }
                        return dist;
                    } else {
                        if (p2.getRoomLocation() == null)
                            return 0;
                        int dist = 0;
                        for (RoomLocation r1 : p1.getRoomLocations()) {
                            dist = Math.max(dist, getDistanceInMinutes(model, r1, p2.getRoomLocation()));
                        }
                        return dist;
                    }
                } else if (p2.isMultiRoom()) {
                    if (p1.getRoomLocation() == null)
                        return 0;
                    int dist = 0;
                    for (RoomLocation r2 : p2.getRoomLocations()) {
                        dist = Math.max(dist, getDistanceInMinutes(model, p1.getRoomLocation(), r2));
                    }
                    return dist;
                } else {
                    if (p1.getRoomLocation() == null || p2.getRoomLocation() == null)
                        return 0;
                    return getDistanceInMinutes(model, p1.getRoomLocation(), p2.getRoomLocation());
                }
            }
        	
        	public boolean inConflict(StudentSectioningModel model, Student student, Section s1, Section s2) {
                if (s1.getPlacement() == null || s2.getPlacement() == null)
                    return false;
                TimeLocation t1 = s1.getTime();
                TimeLocation t2 = s2.getTime();
                if (!t1.shareDays(t2) || !t1.shareWeeks(t2))
                    return false;
                int a1 = t1.getStartSlot(), a2 = t2.getStartSlot();
                if (student.isNeedShortDistances()) {
                    if (model.getDistanceMetric().doComputeDistanceConflictsBetweenNonBTBClasses()) {
                        if (a1 + t1.getNrSlotsPerMeeting() <= a2) {
                            int dist = getDistanceInMinutes(model, s1.getPlacement(), s2.getPlacement());
                            if (dist > Constants.SLOT_LENGTH_MIN * (a2 - a1 - t1.getLength()))
                                return true;
                        } else if (a2 + t2.getNrSlotsPerMeeting() <= a1) {
                            int dist = getDistanceInMinutes(model, s1.getPlacement(), s2.getPlacement());
                            if (dist > Constants.SLOT_LENGTH_MIN * (a1 - a2 - t2.getLength()))
                                return true;
                        }
                    } else {
                        if (a1 + t1.getNrSlotsPerMeeting() == a2) {
                            int dist = getDistanceInMinutes(model, s1.getPlacement(), s2.getPlacement());
                            if (dist > 0) return true;
                        } else if (a2 + t2.getNrSlotsPerMeeting() == a1) {
                            int dist = getDistanceInMinutes(model, s1.getPlacement(), s2.getPlacement());
                            if (dist > 0) return true;
                        }
                    }
                    return false;
                }
                if (model.getDistanceMetric().doComputeDistanceConflictsBetweenNonBTBClasses()) {
                    if (a1 + t1.getNrSlotsPerMeeting() <= a2) {
                        int dist = getDistanceInMinutes(model, s1.getPlacement(), s2.getPlacement());
                        if (dist > t1.getBreakTime() + Constants.SLOT_LENGTH_MIN * (a2 - a1 - t1.getLength()))
                            return true;
                    } else if (a2 + t2.getNrSlotsPerMeeting() <= a1) {
                        int dist = getDistanceInMinutes(model, s1.getPlacement(), s2.getPlacement());
                        if (dist > t2.getBreakTime() + Constants.SLOT_LENGTH_MIN * (a1 - a2 - t2.getLength()))
                            return true;
                    }
                } else {
                    if (a1 + t1.getNrSlotsPerMeeting() == a2) {
                        int dist = getDistanceInMinutes(model, s1.getPlacement(), s2.getPlacement());
                        if (dist > t1.getBreakTime())
                            return true;
                    } else if (a2 + t2.getNrSlotsPerMeeting() == a1) {
                        int dist = getDistanceInMinutes(model, s1.getPlacement(), s2.getPlacement());
                        if (dist > t2.getBreakTime())
                            return true;
                    }
                }
                return false;
            }

        	public Set<DistanceConflict.Conflict> conflicts(StudentSectioningModel model, Enrollment e1) {
                Set<DistanceConflict.Conflict> ret = new HashSet<DistanceConflict.Conflict>();
                if (!e1.isCourseRequest())
                    return ret;
                for (Section s1 : e1.getSections()) {
                    for (Section s2 : e1.getSections()) {
                        if (s1.getId() < s2.getId() && inConflict(model, e1.getStudent(), s1, s2))
                            ret.add(new DistanceConflict.Conflict(e1.getStudent(), e1, s1, e1, s2));
                    }
                }
                return ret;
            }
        	
        	public Set<DistanceConflict.Conflict> conflicts(StudentSectioningModel model, Enrollment e1, Enrollment e2) {
                Set<DistanceConflict.Conflict> ret = new HashSet<DistanceConflict.Conflict>();
                if (!e1.isCourseRequest() || !e2.isCourseRequest() || !e1.getStudent().equals(e2.getStudent()))
                    return ret;
                for (Section s1 : e1.getSections()) {
                    for (Section s2 : e2.getSections()) {
                        if (inConflict(model, e1.getStudent(), s1, s2))
                            ret.add(new DistanceConflict.Conflict(e1.getStudent(), e1, s1, e2, s2));
                    }
                }
                return ret;
            }
        	
        	public Set<DistanceConflict.Conflict> computeAllConflicts(StudentSectioningModel model, Assignment<Request, Enrollment> assignment) {
                Set<DistanceConflict.Conflict> ret = new HashSet<DistanceConflict.Conflict>();
                for (Request r1 : model.variables()) {
                    Enrollment e1 = assignment.getValue(r1);
                    if (e1 == null || !(r1 instanceof CourseRequest))
                        continue;
                    ret.addAll(conflicts(model, e1));
                    for (Request r2 : r1.getStudent().getRequests()) {
                        Enrollment e2 = assignment.getValue(r2);
                        if (e2 == null || r1.getId() >= r2.getId() || !(r2 instanceof CourseRequest))
                            continue;
                        ret.addAll(conflicts(model, e1, e2));
                    }
                }
                return ret;
            }
        	
        	@Override
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment, StudentSectioningReport.Filter filter) {
        		if (model.getDistanceMetric() == null)
        			return new String[] {"N/A", "", ""};
        		Set<DistanceConflict.Conflict> conflicts = computeAllConflicts(model, assignment);
        		Set<Student> students = new HashSet<Student>(), studentsSD = new HashSet<Student>();
            	double distance = 0, distanceSD = 0;
            	int total = 0, totalSD = 0;
            	for (DistanceConflict.Conflict conflict: conflicts) {
            		if (group.matches(conflict.getStudent()) && filter.matches(conflict.getR1(), conflict.getE1())) {
            			if (conflict.getStudent().isNeedShortDistances()) {
            				totalSD ++;
            				studentsSD.add(conflict.getStudent());
            				distanceSD += Placement.getDistanceInMinutes(model.getDistanceMetric(), conflict.getS1().getPlacement(), conflict.getS2().getPlacement());
            			} else {
                			total ++;
                			students.add(conflict.getStudent());
                			distance += Placement.getDistanceInMinutes(model.getDistanceMetric(), conflict.getS1().getPlacement(), conflict.getS2().getPlacement());
            			}
            		}
            	}
        		return new String[] {
        				sIntFormat.format(total),
        				sIntFormat.format(students.size()),
        				(total == 0 ? "" : sDoubleFormat.format(distance / total)),
        				sIntFormat.format(totalSD),
        				sIntFormat.format(studentsSD.size()),
        				(totalSD == 0 ? "" : sDoubleFormat.format(distanceSD / totalSD))
        		};
        	}
        }, true),
        OVERLAP(new String[] {"Free time conflict", "- students in conflict", "- average minutes", "Course time conflict", "- students in conflict", "- average minutes", "Teaching conflicts", "- students in conflict", "- average minutes"},
        		new String[] {
        				"Total number of free time conflicts",
        				"Total number of students with a free time conflict",
        				"For students with a free time conflict, the average number of overlapping minutes per student",
        				"Total number of course time conflicts",
        				"Total number of students with a course time conflict",
        				"For students with a course time conflict, the average number of overlapping minutes per student",
        				"Total number of teaching time conflicts",
        				"Total number of students with a teaching conflict",
        				"For students with a teaching time conflict, the average number of overlapping minutes per student"
        		},
        		new Statistic() {
        	
        	public boolean inConflict(SctAssignment a1, SctAssignment a2) {
                if (a1.getTime() == null || a2.getTime() == null) return false;
                if (a1 instanceof Section && a2 instanceof Section && ((Section)a1).isToIgnoreStudentConflictsWith(a2.getId())) return false;
                return a1.getTime().hasIntersection(a2.getTime());
            }
        	
        	public int share(SctAssignment a1, SctAssignment a2) {
                if (!inConflict(a1, a2)) return 0;
                return a1.getTime().nrSharedDays(a2.getTime()) * a1.getTime().nrSharedHours(a2.getTime());
            }
        	
        	public Set<Conflict> conflicts(Enrollment e1, Enrollment e2) {
                Set<Conflict> ret = new HashSet<Conflict>();
                if (!e1.getStudent().equals(e2.getStudent())) return ret;
                if (e1.getRequest() instanceof FreeTimeRequest && e2.getRequest() instanceof FreeTimeRequest) return ret;
                for (SctAssignment s1 : e1.getAssignments()) {
                    for (SctAssignment s2 : e2.getAssignments()) {
                        if (inConflict(s1, s2))
                            ret.add(new Conflict(e1.getStudent(), share(s1, s2), e1, s1, e2, s2));
                    }
                }
                return ret;
            }

        	public Set<Conflict> computeAllConflicts(StudentSectioningModel model, Assignment<Request, Enrollment> assignment) {
                Set<Conflict> ret = new HashSet<Conflict>();
                for (Request r1 : model.variables()) {
                    Enrollment e1 = assignment.getValue(r1);
                    if (e1 == null || r1 instanceof FreeTimeRequest) continue;
                    for (Request r2 : r1.getStudent().getRequests()) {
                        Enrollment e2 = assignment.getValue(r2);
                        if (r2 instanceof FreeTimeRequest) {
                            FreeTimeRequest ft = (FreeTimeRequest)r2;
                            ret.addAll(conflicts(e1, ft.createEnrollment()));
                        } else if (e2 != null && r1.getId() < r2.getId()) {
                            ret.addAll(conflicts(e1, e2));
                        }                    
                    }
                    for (Unavailability unavailability: e1.getStudent().getUnavailabilities())
                        for (SctAssignment section: e1.getAssignments())
                            if (inConflict(section, unavailability))
                                ret.add(new Conflict(e1.getStudent(), share(section, unavailability), e1, section, unavailability.createEnrollment(), unavailability));
                }
                return ret;
            }
        	
            @Override
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment, StudentSectioningReport.Filter filter) {
            	Set<Student> timeFt = new HashSet<Student>();
            	Set<Student> timeCourse = new HashSet<Student>();
            	Set<Student> timeUnav = new HashSet<Student>();
            	int ftMin = 0, courseMin = 0, unavMin = 0;
            	int totFt = 0, totCourse = 0, totUn = 0;
                Set<TimeOverlapsCounter.Conflict> conf = computeAllConflicts(model, assignment);
                for (TimeOverlapsCounter.Conflict c: conf) {
                    if (group.matches(c.getStudent()) && filter.matches(c.getR1(), c.getE1())) {
                        if (c.getR1() instanceof CourseRequest && c.getR2() instanceof CourseRequest) {
                        	totCourse ++;
                            courseMin += 5 * c.getShare();
                            timeCourse.add(c.getStudent());
                        } else if (c.getS2() instanceof Unavailability) {
                        	totUn ++;
                        	unavMin += 5 * c.getShare();
                        	timeUnav.add(c.getStudent());
                        } else {
                        	totFt ++;
                            ftMin += 5 * c.getShare();
                            timeFt.add(c.getStudent());
                        }
                    }
                }
                return new String[] {
                		sIntFormat.format(totFt),
                        sIntFormat.format(timeFt.size()),
                        (timeFt.isEmpty() ? "" : sDoubleFormat.format(((double)ftMin) / timeFt.size())),
                        sIntFormat.format(totCourse),
                        sIntFormat.format(timeCourse.size()),
                        (timeCourse.isEmpty() ? "" : sDoubleFormat.format(((double)courseMin) / timeCourse.size())),
                        sIntFormat.format(totUn),
                        sIntFormat.format(timeUnav.size()),
                        (timeUnav.isEmpty() ? "" : sDoubleFormat.format(((double)unavMin) / timeUnav.size()))
                };
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
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment, StudentSectioningReport.Filter filter) {
                int total12 = 0, assigned12 = 0;
                int total15 = 0, assigned15 = 0;
                for (Student student: model.getStudents()) {
                    if (!group.matches(student, filter)) continue;
                    float credit = 0;
                    float assignedCredit = 0;
                    for (Request r : student.getRequests()) {
                        if (!(r instanceof CourseRequest)) continue; // ignore free times
                        if (!filter.matches(r)) continue; // check the filter
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
        F2F(new String[] {
        			"Residential Students",
        			"Arranged Hours Assignments", "- percentage of all assignments",
        			"Online Assignments", "- percentage of all assignments",
        			"Students with no face-to-face classes", "- percentage of all undergrad students",
        			"Students with <50% classes face-to-face", "- percentage of all undergrad students"},
        		new String[] {
        			"Number of students that are NOT online-only (only residential students are counted in the following numbers)",
        			"Number of class assignments that are Arranged Hours", "Percentage of all class assignments",
        			"Number of class assignments that are Online (no time, time with no room, or time with ONLINE room)", "Percentage of all class assignments",
        			"Total number of undergraduate students with no face-to-face classes.", "Percentage of all undergraduate students",
        			"Total number of undergraduate students with less than half of their schedule face-to-face.", "Percentage of all undergraduate students",
        		},
        		new Statistic() {
            @Override
            public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment, StudentSectioningReport.Filter filter) {
            	int arrClass = 0, onlineClass = 0, allClass = 0;
            	int residentialStudents = 0;
            	for (Student student: model.getStudents()) {
            		if (!group.matches(student, filter)) continue;
            		if (!FILTER_ALL_RES.matches(student)) { continue; }
            		residentialStudents ++;
            		for (Request r: student.getRequests()) {
            			Enrollment e = r.getAssignment(assignment);
            			if (e != null && e.isCourseRequest()) {
            				for (Section section: e.getSections()) {
            					if (section.isOnline()) onlineClass ++;
            					if (!section.hasTime()) arrClass ++;
            					allClass ++;
            				}
            			}
            		}
            	}
            	int online = 0;
                int half = 0;
                int total = 0;
                for (Student student: model.getStudents()) {
                    if (!group.matches(student, filter)) continue;
                    if (!FILTER_ALL_RES.matches(student)) continue;
                    boolean gr = false;
                    for (AreaClassificationMajor acm: student.getAreaClassificationMajors()) {
                    	if (acm.getClassification().startsWith("G") || acm.getClassification().startsWith("P")) gr = true;
                    }
                    if (gr) continue;
                    int sections = 0, onlineSections = 0;
                    for (Request r : student.getRequests()) {
                        if (!(r instanceof CourseRequest)) continue; // ignore free times
                        if (!filter.matches(r)) continue; // check the filter
                        Enrollment e = r.getAssignment(assignment);
                        if (e != null)
                            for (Section s: e.getSections()) {
                                sections ++;
                                if (s.isOnline()) onlineSections ++;
                            }
                    }
                    if (sections > 0) {
                        total ++;
                        if (onlineSections == sections) online ++;
                        if (onlineSections > 0.5 * sections) half++;
                    }
                }
                return new String[] {
                		sIntFormat.format(residentialStudents),
                		(residentialStudents == 0 ? "" : sIntFormat.format(arrClass)),
                		(residentialStudents == 0 ? "" : sPercentFormat.format(100.0 * arrClass / allClass) + "%"),
                		(onlineClass == 0 ? "" : sIntFormat.format(onlineClass)), 
                		(onlineClass == 0 ? "" : sPercentFormat.format(100.0 * onlineClass / allClass) + "%"),
                        (online == 0 ? "" : sIntFormat.format(online)),
                        (online == 0 ? "" : sPercentFormat.format(100.0 * online / total) + "%"),
                        (half == 0 ? "" : sIntFormat.format(half)),
                        (half == 0 ? "" : sPercentFormat.format(100.0 * half / total) + "%")
                };
            }
        }, true),
        FULL_OFFERINGS(
        		new String[] {
        				"Full Offerings", "- percentage of all requested offerings", "- percentage of all assignments",
        				"Offerings with ≤ 2% available", "- percentage of all requested offerings", "- percentage of all assignments",
        				"Offerings with ≤ 5% available", "- percentage of all requested offerings", "- percentage of all assignments",
        				"Offerings with ≤ 10% available", "- percentage of all requested offerings", "- percentage of all assignments",
        				"Full Sections", "- percentage of all sections", "- percentage of all assignments",
        				"Disabled Sections", "- percentage of all sections", "- percentage of all assignments",
        				"Sections with ≤ 2% available", "- percentage of all sections", "- percentage of all assignments",
        				"Sections with ≤ 5% available", "- percentage of all sections", "- percentage of all assignments",
        				"Sections with ≤ 10% available", "- percentage of all sections", "- percentage of all assignments",
        				},
        		new String[] {
        				"Number of instructional offerings that are completely full (only counting courses that are requested by the students)",
        					"Percentage full offerings vs all requested offerings",
        					"Percentage of all course assignments that are for courses that are full",
        				"Number of instructional offerings that have 2% or less space available", "", "",
        				"Number of instructional offerings that have 5% or less space available", "", "",
        				"Number of instructional offerings that have 10% or less space available", "", "",
        				"Number of sections that have no space available (only counting sections from courses that are requested by the students)",
        					"Percentage full sections vs all sections of the requested courses",
        					"Percentage of all class assignments that are in sections that are full",
        				"Number of sections that are disabled",
        					"Percentage disabled sections vs all sections of the requested courses",
    						"Percentage of all class assignments that are in sections that are disabled",
        				"Number of sections that have 2% or less space available", "", "",
        				"Number of sections that have 5% or less space available", "", "",
        				"Number of sections that have 10% or less space available", "", "",
        				},
        new Statistic() {
        	
        	protected int getEnrollments(StudentGroup group, Section section, Assignment<Request, Enrollment> assignment, StudentSectioningReport.Filter filter) {
        		int enrl = 0;
        		for (Enrollment e: section.getEnrollments(assignment)) {
                    if (group.matches(e.getStudent()) && filter.matches(e.getRequest(), e)) enrl ++;
                }
        		return enrl;
        	}
        	
        	protected int getEnrollments(StudentGroup group, Config config, Assignment<Request, Enrollment> assignment, StudentSectioningReport.Filter filter) {
        		int enrl = 0;
        		for (Enrollment e: config.getEnrollments(assignment)) {
                    if (group.matches(e.getStudent()) && filter.matches(e.getRequest(), e)) enrl ++;
                }
        		return enrl;
        	}
        	
			@Override
			public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment, StudentSectioningReport.Filter filter) {
				
		        int nbrSections = 0, nbrFullSections = 0, nbrSections98 = 0, nbrSections95 = 0, nbrSections90 = 0, nbrSectionsDis = 0;
		        int enrlSections = 0, enrlFullSections = 0, enrlSections98 = 0, enrlSections95 = 0, enrlSections90 = 0, enrlSectionsDis = 0;
		        int nbrOfferings = 0, nbrFullOfferings = 0, nbrOfferings98 = 0, nbrOfferings95 = 0, nbrOfferings90 = 0;
		        int enrlOfferings = 0, enrlOfferingsFull = 0, enrlOfferings98 = 0, enrlOfferings95 = 0, enrlOfferings90 = 0;
		        for (Offering offering: model.getOfferings()) {
		        	if (offering.isDummy()) continue;
		        	int crs = 0;
	        		for (Course course: offering.getCourses()) {
	        			for (CourseRequest cr: course.getRequests()) {
	        				if (group.matches(cr.getStudent()) && filter.matches(cr)) crs++;
	        			}
	        		}
	        		if (crs == 0) continue;
		            int offeringLimit = 0, offeringEnrollment = 0, offeringMatchingEnrollment = 0;
		            for (Config config: offering.getConfigs()) {
		                int configLimit = config.getLimit();
		                for (Subpart subpart: config.getSubparts()) {
		                    int subpartLimit = 0;
		                    for (Section section: subpart.getSections()) {
		                        if (section.isCancelled()) continue;
		                        int enrl = section.getEnrollments(assignment).size();
		                        int matchingEnrl = getEnrollments(group, section, assignment, filter);
		                        if (section.getLimit() < 0 || subpartLimit < 0)
		                            subpartLimit = -1;
		                        else
		                            subpartLimit += (section.isEnabled() ? section.getLimit() : enrl);
		                        nbrSections ++;
		                        enrlSections += matchingEnrl;
		                        if (section.getLimit() >= 0 && section.getLimit() <= enrl) {
		                            nbrFullSections ++;
		                            enrlFullSections += matchingEnrl;
		                        }
		                        if (!section.isEnabled()) { //&& (enrl > 0 || section.getLimit() >= 0)) {
		                            nbrSectionsDis ++;
		                            enrlSectionsDis += matchingEnrl;
		                        }
		                        if (section.getLimit() >= 0 && (section.getLimit() - enrl) <= Math.round(0.02 * section.getLimit())) {
		                            nbrSections98 ++;
		                            enrlSections98 += matchingEnrl;
		                        }
		                        if (section.getLimit() >= 0 && (section.getLimit() - enrl) <= Math.round(0.05 * section.getLimit())) {
		                            nbrSections95 ++;
		                            enrlSections95 += matchingEnrl;
		                        }
		                        if (section.getLimit() >= 0 && (section.getLimit() - enrl) <= Math.round(0.10 * section.getLimit())) {
		                            nbrSections90 ++;
		                            enrlSections90 += matchingEnrl;
		                        }
		                    }
		                    if (configLimit < 0 || subpartLimit < 0)
		                        configLimit = -1;
		                    else
		                        configLimit = Math.min(configLimit, subpartLimit);
		                }
		                if (offeringLimit < 0 || configLimit < 0)
		                    offeringLimit = -1;
		                else
		                    offeringLimit += configLimit;
		                offeringEnrollment += config.getEnrollments(assignment).size();
		                offeringMatchingEnrollment += getEnrollments(group, config, assignment, filter);
		            }
		            nbrOfferings ++;
		            enrlOfferings += offeringMatchingEnrollment;
		            
		            if (offeringLimit >=0 && offeringEnrollment >= offeringLimit) {
		                nbrFullOfferings ++;
		                enrlOfferingsFull += offeringMatchingEnrollment;
		            }
		            if (offeringLimit >= 0 && (offeringLimit - offeringEnrollment) <= Math.round(0.02 * offeringLimit)) {
		                nbrOfferings98++;
		                enrlOfferings98 += offeringMatchingEnrollment;
		            }
		            if (offeringLimit >= 0 && (offeringLimit - offeringEnrollment) <= Math.round(0.05 * offeringLimit)) {
		                nbrOfferings95++;
		                enrlOfferings95 += offeringMatchingEnrollment;
		            }
		            if (offeringLimit >= 0 && (offeringLimit - offeringEnrollment) <= Math.round(0.10 * offeringLimit)) {
		                nbrOfferings90++;
		                enrlOfferings90 += offeringMatchingEnrollment;
		            }
		        }
		        return new String[] {
		        		sIntFormat.format(nbrFullOfferings), sPercentFormat.format(100.0 * nbrFullOfferings / nbrOfferings) + "%", sPercentFormat.format(100.0 * enrlOfferingsFull / enrlOfferings) + "%",
		        		sIntFormat.format(nbrOfferings98), sPercentFormat.format(100.0 * nbrOfferings98 / nbrOfferings) + "%", sPercentFormat.format(100.0 * enrlOfferings98 / enrlOfferings) + "%",
		        		sIntFormat.format(nbrOfferings95), sPercentFormat.format(100.0 * nbrOfferings95 / nbrOfferings) + "%", sPercentFormat.format(100.0 * enrlOfferings95 / enrlOfferings) + "%",
		        		sIntFormat.format(nbrOfferings90), sPercentFormat.format(100.0 * nbrOfferings90 / nbrOfferings) + "%", sPercentFormat.format(100.0 * enrlOfferings90 / enrlOfferings) + "%",
		        		sIntFormat.format(nbrFullSections), sPercentFormat.format(100.0 * nbrFullSections / nbrSections) + "%", sPercentFormat.format(100.0 * enrlFullSections / enrlSections) + "%",
		        		sIntFormat.format(nbrSectionsDis), sPercentFormat.format(100.0 * nbrSectionsDis / nbrSections) + "%", sPercentFormat.format(100.0 * enrlSectionsDis / enrlSections) + "%",
		        		sIntFormat.format(nbrSections98), sPercentFormat.format(100.0 * nbrSections98 / nbrSections) + "%", sPercentFormat.format(100.0 * enrlSections98 / enrlSections) + "%",
		        		sIntFormat.format(nbrSections95), sPercentFormat.format(100.0 * nbrSections95 / nbrSections) + "%", sPercentFormat.format(100.0 * enrlSections95 / enrlSections) + "%",
		        		sIntFormat.format(nbrSections90), sPercentFormat.format(100.0 * nbrSections90 / nbrSections) + "%", sPercentFormat.format(100.0 * enrlSections90 / enrlSections) + "%",
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
        public String[] getValues(StudentGroup group, StudentSectioningModel model, Assignment<Request, Enrollment> assignment, StudentSectioningReport.Filter filter) {
            return iStatistic.getValues(group, model, assignment, filter);
        }
        public boolean isNewLine() { return iNewLine; }
    }
    
    @Override
    public CSVFile createTable(Assignment<Request, Enrollment> assignment, DataProperties properties) {
        CSVFile csv = new CSVFile();
        List<CSVField> header = new ArrayList<CSVField>();
        List<StudentGroup> groups = new ArrayList<StudentGroup>();
        header.add(new CSVField(""));
        Map<Integer, StudentGroup> counts = new HashMap<Integer, StudentGroup>();

        for (StudentGroup g: StudentGroup.values()) {
            int nrStudents = 0;
            for (Student student: getModel().getStudents()) {
                if (g.matches(student, this)) nrStudents ++;
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
                String[] values = stat.getValues(g, getModel(), assignment, this);
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
