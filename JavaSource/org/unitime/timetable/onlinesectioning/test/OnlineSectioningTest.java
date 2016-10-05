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
package org.unitime.timetable.onlinesectioning.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;



import org.cpsolver.coursett.Constants;
import org.cpsolver.ifs.heuristics.RouletteWheelSelection;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningTestFwk;
import org.unitime.timetable.onlinesectioning.basic.GetRequest;
import org.unitime.timetable.onlinesectioning.solver.ComputeSuggestionsAction;
import org.unitime.timetable.onlinesectioning.solver.FindAssignmentAction;
import org.unitime.timetable.onlinesectioning.updates.EnrollStudent;

/**
 * @author Tomas Muller
 */
public class OnlineSectioningTest extends OnlineSectioningTestFwk {
	private static String[] sDays = new String[] {"M", "T", "W", "R", "F", "S", "X"};
	
	private static List<ClassAssignmentInterface.ClassAssignment> toClassAssignments(ClassAssignmentInterface assignment) {
		if (assignment == null) return null;
		List<ClassAssignmentInterface.ClassAssignment> ret = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
		for (ClassAssignmentInterface.CourseAssignment course: assignment.getCourseAssignments())
			ret.addAll(course.getClassAssignments());
		return ret;
	}
	
	private ClassAssignmentInterface.CourseAssignment course(CourseRequestInterface.Request request, ClassAssignmentInterface assignment) {
		if (assignment == null) return null;
		for (CourseAssignment course: assignment.getCourseAssignments()) {
			if (request.hasRequestedCourse()) {
				for (RequestedCourse rc: request.getRequestedCourse()) {
					if (rc.isFreeTime()) {
						for (CourseRequestInterface.FreeTime ft: rc.getFreeTime()) {
							for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments()) {
								if (ft.getStart() == clazz.getStart() && ft.getLength() == clazz.getLength() && ft.getDaysString(sDays, "").equals(clazz.getDaysString(sDays, ""))) {
									return course;
								}
							}
						}		
					} else if (rc.isCourse()) {
						if (rc.equals(course)) return course;
					}
				}
			}
		}
		return null;
	}
	
	private double penalty(StudentPreferencePenalties penalty, CourseRequestInterface request, ClassAssignmentInterface assignment) {
		double ret = 0.0;
		int unassigned = 0;
		for (CourseRequestInterface.Request r: request.getCourses()) {
			CourseAssignment course = course(r, assignment);
			if (course == null || !course.isAssigned()) {
				ret += 10.0;
				if (r.hasRequestedCourse()) unassigned++;
			} else {
				ret += penalty.getPenalty(course);
			}
		}
		for (CourseRequestInterface.Request r: request.getAlternatives()) {
			if (unassigned <= 0) break;
			CourseAssignment course = course(r, assignment);
			if (course != null && course.isAssigned()) {
				unassigned--;
				ret += penalty.getPenalty(course);
			}
		}
		return ret;
	}
	
	private boolean isBetter(StudentPreferencePenalties penalty, CourseRequestInterface request, ClassAssignmentInterface assignment, ClassAssignmentInterface suggestion) {
		return penalty(penalty, request, suggestion) < penalty(penalty, request, assignment); 
	}
	
	@SuppressWarnings("unused")
	private String toString(StudentPreferencePenalties penalty, CourseRequestInterface request, ClassAssignmentInterface assignment) {
		String ret = sDF.format(penalty(penalty, request, assignment)) + "/{";
		int priority = 1;
		int unassigned = 0;
		for (CourseRequestInterface.Request r: request.getCourses()) {
			CourseAssignment course = course(r, assignment);
			if (course == null || !course.isAssigned()) {
				ret += "\n    " + priority + ". " + r + " NOT ASSIGNED";
				if (r.hasRequestedCourse()) unassigned++;
			} else {
				ret += "\n   " + priority + ". " + r + ": " + sDF.format(penalty.getPenalty(course)) + "/" + course.getCourseName();
				for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments())
					ret += "\n       " + sDF.format(penalty.getPenalty(clazz)) + "/" + clazz.toString();
			}
			priority++;
		}
		priority = 1;
		for (CourseRequestInterface.Request r: request.getAlternatives()) {
			if (unassigned <= 0) break;
			CourseAssignment course = course(r, assignment);
			if (course != null && course.isAssigned()) {
				unassigned--;
				ret += "\n  A" + priority + ". " + r + ": " + sDF.format(penalty.getPenalty(course)) + "/" + course.getCourseName();
				for (ClassAssignmentInterface.ClassAssignment clazz: course.getClassAssignments())
					ret += "\n       " + sDF.format(penalty.getPenalty(clazz)) + "/" + clazz.toString();
			}
			priority++;
		}
		return ret + "\n}";
	}

	public List<Operation> operations() {
		
		getServer().getAcademicSession().setSectioningEnabled(true);

		org.hibernate.Session hibSession = new _RootDAO().getSession();
		
		List<Operation> operations = new ArrayList<Operation>();
		
		final boolean suggestions = "true".equals(System.getProperty("suggestions", "false"));
		
		for (final Long studentId: (List<Long>)hibSession.createQuery(
				"select s.uniqueId from Student s where s.session.uniqueId = :sessionId")
				.setLong("sessionId", getServer().getAcademicSession().getUniqueId()).list()) {
			
			CourseRequestInterface request = getServer().execute(createAction(GetRequest.class).forStudent(studentId), user());
			if (request == null || request.getCourses().isEmpty()) continue;
			
			operations.add(new Operation() {
				@Override
				public double execute(OnlineSectioningServer s) {
					StudentPreferencePenalties penalties = new StudentPreferencePenalties(StudentPreferencePenalties.DistType.Preference);
					CourseRequestInterface request = s.execute(createAction(GetRequest.class).forStudent(studentId), user());
					if (request != null && !request.getCourses().isEmpty()) {
						FindAssignmentAction action = null;
						double value = 0.0;
						for (int i = 1; i <= 5; i++) {
							try {
								action = s.createAction(FindAssignmentAction.class).forRequest(request).withAssignment(new ArrayList<ClassAssignmentInterface.ClassAssignment>()); 
								List<ClassAssignmentInterface> ret = s.execute(action, user());
								ClassAssignmentInterface assignment = (ret == null || ret.isEmpty() ? null : ret.get(0));
								List<ClassAssignmentInterface.ClassAssignment> classes = toClassAssignments(assignment);
								if (assignment != null) value = assignment.getValue();
								if (suggestions) {
									for (int x = 0; x < (classes == null ? 0 : classes.size()); x++) {
										List<ClassAssignmentInterface> suggestions = s.execute(s.createAction(ComputeSuggestionsAction.class).forRequest(request).withAssignment(classes).withSelection(classes.get(x)), user());
										if (suggestions != null && !suggestions.isEmpty()) {
											for (ClassAssignmentInterface suggestion: suggestions) {
												if (isBetter(penalties, request, assignment, suggestion)) {
													assignment = suggestion;
													classes = toClassAssignments(assignment);
												}
											}
										}
									}									
								}
								if (classes != null)
									s.execute(s.createAction(EnrollStudent.class).forStudent(studentId).withRequest(request).withAssignment(classes), user());
								break;
							} catch (SectioningException e) {
								if (e.getMessage().contains("the class is no longer available")) {
									sLog.warn("Enrollment failed: " +e.getMessage() + " become unavailable (" + i + ". attempt)");
									continue;
								}
							}
						}
						return value;
					} else {
						return 1.0;
					}
				}
			});
		}
		
		hibSession.close();
		
		Collections.shuffle(operations);

		return operations;
	}
	
	public static void main(String args[]) {
		new OnlineSectioningTest().test(
				Integer.valueOf(System.getProperty("nrTasks", "-1")),
				Integer.valueOf(System.getProperty("nrConcurrent", "10")));
	}
	
	public static class StudentPreferencePenalties {
	    public static enum DistType {
	    	Uniform, Preference, PreferenceQuadratic, PreferenceReverse
	    };
	    public static int[][] sStudentRequestDistribution = new int[][] {
	    // morning, 7:30a, 8:30a, 9:30a, 10:30a, 11:30a, 12:30p, 1:30p, 2:30p, 3:30p, 4:30p, evening
	            { 1, 1, 4, 7, 10, 10, 5, 8, 8, 6, 3, 1 }, // Monday
	            { 1, 2, 4, 7, 10, 10, 5, 8, 8, 6, 3, 1 }, // Tuesday
	            { 1, 2, 4, 7, 10, 10, 5, 8, 8, 6, 3, 1 }, // Wednesday
	            { 1, 2, 4, 7, 10, 10, 5, 8, 8, 6, 3, 1 }, // Thursday
	            { 1, 2, 4, 7, 10, 10, 5, 4, 3, 2, 1, 1 }, // Friday
	            { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 }, // Saturday
	            { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 } // Sunday
	    };
	    private HashMap<String, Double> iWeight = new HashMap<String, Double>();

	    public StudentPreferencePenalties(DistType disributionType) {
	        RouletteWheelSelection<int[]> roulette = new RouletteWheelSelection<int[]>();
	        for (int d = 0; d < sStudentRequestDistribution.length; d++)
	            for (int t = 0; t < sStudentRequestDistribution[d].length; t++) {
	            	switch (disributionType) {
	            	case Uniform:
	            		roulette.add(new int[] { d, t }, 1);
	            		break;
	            	case Preference:
	                    roulette.add(new int[] { d, t }, sStudentRequestDistribution[d][t]);
	                    break;
	            	case PreferenceQuadratic:
	                    roulette.add(new int[] { d, t }, sStudentRequestDistribution[d][t] * sStudentRequestDistribution[d][t]);
	                    break;
	            	case PreferenceReverse:
	                    roulette.add(new int[] { d, t }, 11 - sStudentRequestDistribution[d][t]);
	                    break;
	                default:
	                    roulette.add(new int[] { d, t }, 1);
	                    break;
	                }
	            }
	        int idx = 0;
	        while (roulette.hasMoreElements()) {
	            int[] dt = roulette.nextElement();
	            iWeight.put(dt[0] + "." + dt[1], new Double(((double) idx) / (roulette.size() - 1)));
	            idx++;
	        }
	    }
	    
	    public String toString(int day, int time) {
	        if (time == 0)
	            return Constants.DAY_NAMES_SHORT[day] + " morning";
	        if (time == 11)
	            return Constants.DAY_NAMES_SHORT[day] + " evening";
	        return Constants.DAY_NAMES_SHORT[day] + " " + (6 + time) + ":30";
	    }
	    
	    public static int time(int slot) {
	        int s = slot % Constants.SLOTS_PER_DAY;
	        int min = (s * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN);
	        if (min < 450) return 0; // morning
	        int idx = 1 + (min - 450) / 60;
	        return (idx > 11 ? 11 : idx); // 11+ is evening
	    }
	    
	    public double getPenalty(ClassAssignmentInterface.ClassAssignment assignment) {
	    	if (!assignment.isAssigned()) return 1.0;
	        int nrSlots = 0;
	        double penalty = 0.0;
	        for (int day: assignment.getDays()) {
	        	for (int idx = 0; idx < assignment.getLength(); idx++) {
	        		int slot = assignment.getStart() + idx;
	        		nrSlots++;
		            penalty += (iWeight.get(day + "." + time(slot))).doubleValue();
	        	}
	        }
	        return penalty / nrSlots;
	    }
	    
	    public double getPenalty(ClassAssignmentInterface.CourseAssignment assignment) {
	    	if (assignment.getClassAssignments().isEmpty()) return 1.0;
	    	double penalty = 0.0;
	    	for (ClassAssignmentInterface.ClassAssignment clazz: assignment.getClassAssignments())
	    		penalty += getPenalty(clazz);
	    	return penalty / assignment.getClassAssignments().size();
	    }
	}
}
