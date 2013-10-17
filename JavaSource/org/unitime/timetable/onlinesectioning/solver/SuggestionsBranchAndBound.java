/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.timetable.onlinesectioning.solver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.util.Formats;

import net.sf.cpsolver.coursett.model.RoomLocation;
import net.sf.cpsolver.coursett.model.TimeLocation;
import net.sf.cpsolver.ifs.model.Value;
import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.ToolBox;
import net.sf.cpsolver.studentsct.StudentSectioningModel;
import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.FreeTimeRequest;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;

/**
 * @author Tomas Muller
 */
public class SuggestionsBranchAndBound {
	private static StudentSectioningConstants CONSTANTS = Localization.create(StudentSectioningConstants.class);
	private Hashtable<CourseRequest, Set<Section>> iRequiredSections = null;
	private Set<FreeTimeRequest> iRequiredFreeTimes = null;
	private Hashtable<CourseRequest, Set<Section>> iPreferredSections = null;
	private Request iSelectedRequest = null;
	private Section iSelectedSection = null;
	private Student iStudent = null;
	private TreeSet<Suggestion> iSuggestions = new TreeSet<Suggestion>();
	private int iMaxDepth = 4;
	private long iTimeout = 5000;
	private int iMaxSuggestions = 20;
	private long iT0, iT1;
	private boolean iTimeoutReached = false;
	private int iNrSolutionsSeen = 0;
	private StudentSectioningModel iModel;
	private Hashtable<Request, List<Enrollment>> iValues = new Hashtable<Request, List<Enrollment>>();
	private long iLastSuggestionId = 0;
	private Query iFilter = null;
	private Date iFirstDate = null;
	protected Comparator<Enrollment> iComparator = null;
	protected int iMatched = 0;
	protected int iMaxSectionsWithPenalty = 0;
	
	public SuggestionsBranchAndBound(DataProperties properties, Student student,
			Hashtable<CourseRequest, Set<Section>> requiredSections,
			Set<FreeTimeRequest> requiredFreeTimes,
			Hashtable<CourseRequest, Set<Section>> preferredSections,
			Request selectedRequest, Section selectedSection, String filter, Date firstDate, int maxSectionsWithPenalty) {
		iRequiredSections = requiredSections;
		iRequiredFreeTimes = requiredFreeTimes;
		iPreferredSections = preferredSections;
		iSelectedRequest = selectedRequest;
		iSelectedSection = selectedSection;
		iStudent = student;
		iModel = (StudentSectioningModel)selectedRequest.getModel();
		iMaxDepth = properties.getPropertyInt("Suggestions.MaxDepth", iMaxDepth);
		iTimeout = properties.getPropertyLong("Suggestions.Timeout", iTimeout);
		iMaxSuggestions = properties.getPropertyInt("Suggestions.MaxSuggestions", iMaxSuggestions);
		iMaxSectionsWithPenalty = maxSectionsWithPenalty;
		iFilter = (filter == null || filter.isEmpty() ? null : new Query(filter));
		iFirstDate = firstDate;
		iComparator = new Comparator<Enrollment>() {
            private HashMap<Enrollment, Double> iValues = new HashMap<Enrollment, Double>();
            private Double value(Enrollment e) {
                Double value = iValues.get(e);
                if (value == null) {
                    value = iModel.getStudentWeights().getWeight(e,
                                    (iModel.getDistanceConflict() == null ? null : iModel.getDistanceConflict().conflicts(e)),
                                    (iModel.getTimeOverlaps() == null ? null : iModel.getTimeOverlaps().freeTimeConflicts(e)));
                    iValues.put(e, value);       
                }
                return value;
            }
            public int compare(Enrollment e1, Enrollment e2) {
                return value(e2).compareTo(value(e1));
            }
        };
	}
	
	public long getTime() { return iT1 - iT0; }
	public boolean isTimeoutReached() { return iTimeoutReached; }
	public int getNrSolutionsSeen() { return iNrSolutionsSeen; }
	
	@SuppressWarnings("unchecked")
	public TreeSet<Suggestion> computeSuggestions() {
		iT0 = System.currentTimeMillis();
		iTimeoutReached = false; iNrSolutionsSeen = 0; iSuggestions.clear();
		
        ArrayList<Request> requests2resolve = new ArrayList<Request>(); 
        requests2resolve.add(iSelectedRequest);
        TreeSet<Request> altRequests2resolve = new TreeSet<Request>(); 

        for (Map.Entry<CourseRequest, Set<Section>> entry: iPreferredSections.entrySet()) {
			CourseRequest request = entry.getKey();
			Set<Section> sections = entry.getValue();
			if (!sections.isEmpty() && sections.size() == sections.iterator().next().getSubpart().getConfig().getSubparts().size())
				request.assign(0, request.createEnrollment(sections));
			else if (!request.equals(iSelectedRequest)) {
				if (sections.isEmpty())
					altRequests2resolve.add(request);
				else
					requests2resolve.add(request);
			}
		}
        
        for (Request request: iStudent.getRequests()) {
        	if (request.getAssignment() == null && request instanceof FreeTimeRequest) {
        		FreeTimeRequest ft = (FreeTimeRequest)request;
        		Enrollment enrollment = ft.createEnrollment();
        		if (iModel.conflictValues(enrollment).isEmpty()) ft.assign(0, enrollment);
        	}
        }
        
        for (Request request: iStudent.getRequests()) {
    		request.setInitialAssignment(request.getAssignment());
        }
        
        backtrack(requests2resolve, altRequests2resolve, 0, iMaxDepth, false);
        
		iT1 = System.currentTimeMillis();
		return iSuggestions;
	}
		
    @SuppressWarnings("unchecked")
	protected void backtrack(ArrayList<Request> requests2resolve, TreeSet<Request> altRequests2resolve, int idx, int depth, boolean alt) {
        if (!iTimeoutReached && iTimeout > 0 && System.currentTimeMillis() - iT0 > iTimeout)
            iTimeoutReached = true;
        int nrUnassigned = requests2resolve.size() - idx;
        if (nrUnassigned==0) {
        	List<FreeTimeRequest> okFreeTimes = new ArrayList<FreeTimeRequest>();
        	int sectionsWithPenalty = 0;
        	for (Request r: iStudent.getRequests()) {
        		if (iMaxSectionsWithPenalty >= 0 && r.getAssignment() != null && r instanceof CourseRequest) {
        			for (Section s: r.getAssignment().getSections())
        				if (s.getPenalty() >= 0) sectionsWithPenalty ++;
        		}
        		if (r.getAssignment() == null && r instanceof FreeTimeRequest) {
        			FreeTimeRequest ft = (FreeTimeRequest)r;
            		Enrollment enrollment = ft.createEnrollment();
            		if (iModel.conflictValues(enrollment).isEmpty()) {
            			ft.assign(0, enrollment);
            			okFreeTimes.add(ft);
            		}
        		}
        	}
        	if (iMaxSectionsWithPenalty >= 0 && sectionsWithPenalty > iMaxSectionsWithPenalty) return;
    		Suggestion s = new Suggestion(requests2resolve);
    		if (iSuggestions.size() >= iMaxSuggestions && iSuggestions.last().compareTo(s) <= 0) return;
    		if (iMatched != 1) {
        		for (Iterator<Suggestion> i = iSuggestions.iterator(); i.hasNext();) {
        			Suggestion x = (Suggestion)i.next();
            		if (x.sameSelectedSection()) {
            			if (x.compareTo(s) <= 0) return;
            			i.remove();
            		}
            	}
    		}
    		s.init();
			iSuggestions.add(s);
			if (iSuggestions.size() > iMaxSuggestions) iSuggestions.remove(iSuggestions.last());
			for (FreeTimeRequest ft: okFreeTimes)
				ft.unassign(0);
        	return;
        }
        if (!canContinue(requests2resolve, idx, depth)) return;
        Request request = requests2resolve.get(idx);
        for (Enrollment enrollment: values(request)) {
            if (!canContinueEvaluation()) break;
            if (!isAllowed(enrollment)) continue;
            if (enrollment.equals(request.getAssignment())) continue;
            if (enrollment.getAssignments().isEmpty() && alt) continue;
            Set<Enrollment> conflicts = iModel.conflictValues(enrollment);
            if (!checkBound(requests2resolve, idx, depth, enrollment, conflicts)) continue;
            Enrollment current = (Enrollment)request.getAssignment();
            ArrayList<Request> newVariables2resolve = new ArrayList<Request>(requests2resolve);
            for (Iterator<Enrollment> i=conflicts.iterator();i.hasNext();) {
                Enrollment conflict = i.next();
                conflict.variable().unassign(0);
                if (!newVariables2resolve.contains(conflict.variable()))
                    newVariables2resolve.add((Request)conflict.variable());
            }
            if (current!=null) current.variable().unassign(0);
            enrollment.variable().assign(0, enrollment);
            if (enrollment.getAssignments().isEmpty()) {
            	if (altRequests2resolve != null && !altRequests2resolve.isEmpty()) {
                	Suggestion lastBefore = (iSuggestions.isEmpty() ? null : iSuggestions.last());
                	int sizeBefore = iSuggestions.size();
                	for (Request r: altRequests2resolve) {
                		newVariables2resolve.add(r);
                		backtrack(newVariables2resolve, null, idx+1, depth, true);
                		newVariables2resolve.remove(r);
                	}
                	Suggestion lastAfter = (iSuggestions.isEmpty() ? null : iSuggestions.last());
                	int sizeAfter = iSuggestions.size();
                	// did not succeeded with an alternative -> try without it
                	if (sizeBefore == sizeAfter && (sizeAfter < iMaxSuggestions || sizeAfter == 0 || lastAfter.compareTo(lastBefore) == 0))
                		backtrack(newVariables2resolve, altRequests2resolve, idx+1, depth-1, alt);
            	} else {
            		backtrack(newVariables2resolve, altRequests2resolve, idx+1, depth-1, alt);
            	}
            } else {
                backtrack(newVariables2resolve, altRequests2resolve, idx+1, depth-1, alt);
            }
            if (current==null)
                request.unassign(0);
            else
                request.assign(0, current);
            for (Iterator<Enrollment> i=conflicts.iterator();i.hasNext();) {
                Value conflict = i.next();
                conflict.variable().assign(0, conflict);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
	protected List<Enrollment> values(final Request request) {
    	List<Enrollment> values = iValues.get(request);
    	if (values != null) return values;
    	if (request instanceof CourseRequest) {
    		CourseRequest cr = (CourseRequest)request;
    		values = (cr.equals(iSelectedRequest) ? cr.getAvaiableEnrollments() : cr.getAvaiableEnrollmentsSkipSameTime());
            Collections.sort(values, iComparator);
    	} else {
    		values = new ArrayList<Enrollment>();
    		values.add(((FreeTimeRequest)request).createEnrollment());
    	}
		if (canLeaveUnassigned(request)) {
    		Config config = null;
    		if (request instanceof CourseRequest)
    			config = (Config)((Course)((CourseRequest)request).getCourses().get(0)).getOffering().getConfigs().get(0);
    		values.add(new Enrollment(request, 0, config, new HashSet<Section>()));
		}
		iValues.put(request, values);
		if (request.equals(iSelectedRequest) && iFilter != null && request instanceof CourseRequest) {
			for (Iterator<Enrollment> i = values.iterator(); i.hasNext();) {
				Enrollment enrollment = i.next();
        		if (enrollment.getAssignments() != null && !enrollment.getAssignments().isEmpty()) {
    				boolean match = false;
        			for (Iterator<Section> j = enrollment.getSections().iterator(); j.hasNext();) {
        				Section section = j.next();
            			if (iSelectedSection != null) {
            				if (section.getSubpart().getId() == iSelectedSection.getSubpart().getId()) {
            					if (iFilter.match(new SectionMatcher(enrollment.getCourse(), section))) { match = true; break; }
            				}
            				if (section.getSubpart().getConfig().getId() != iSelectedSection.getSubpart().getConfig().getId() &&
            					section.getSubpart().getInstructionalType().equals(iSelectedSection.getSubpart().getInstructionalType())) {
            					if (iFilter.match(new SectionMatcher(enrollment.getCourse(), section))) { match = true; break; }
            				}
            			} else {
            				if (iFilter.match(new SectionMatcher(enrollment.getCourse(), section))) { match = true; break; }
            			}
        			}
        			if (!match) i.remove();
        		}
			}
		}
		if (request.equals(iSelectedRequest)) iMatched = values.size();
        return values;
    }
    
    private class SectionMatcher implements Query.TermMatcher {
    	private Course iCourse;
    	private Section iSection;
    	
    	public SectionMatcher(Course course, Section section) {
    		iCourse = course;
    		iSection = section;
    	}

		@Override
		public boolean match(String attr, String term) {
			if (term.isEmpty()) return true;
			if (attr == null || attr.equals("crn") || attr.equals("id") || attr.equals("externalId") || attr.equals("exid") || attr.equals("name")) {
				if (iSection.getName(iCourse.getId()) != null && iSection.getName(iCourse.getId()).toLowerCase().startsWith(term.toLowerCase()))
					return true;
			}
			if (attr == null || attr.equals("day")) {
				if (iSection.getTime() == null && term.equalsIgnoreCase("none")) return true;
				if (iSection.getTime() != null) {
					int day = parseDay(term);
					if (day > 0 && (iSection.getTime().getDayCode() & day) == day) return true;
				}
			}
			if (attr == null || attr.equals("time")) {
				if (iSection.getTime() == null && term.equalsIgnoreCase("none")) return true;
				if (iSection.getTime() != null) {
					int start = parseStart(term);
					if (start >= 0 && iSection.getTime().getStartSlot() == start) return true;
				}
			}
			if (attr != null && attr.equals("before")) {
				if (iSection.getTime() != null) {
					int end = parseStart(term);
					if (end >= 0 && iSection.getTime().getStartSlot() + iSection.getTime().getLength() - iSection.getTime().getBreakTime() / 5 <= end) return true;
				}
			}
			if (attr != null && attr.equals("after")) {
				if (iSection.getTime() != null) {
					int start = parseStart(term);
					if (start >= 0 && iSection.getTime().getStartSlot() >= start) return true;
				}
			}
			if (attr == null || attr.equals("date")) {
				if (iSection.getTime() == null && term.equalsIgnoreCase("none")) return true;
				if (iSection.getTime() != null && !iSection.getTime().getWeekCode().isEmpty()) {
					Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_PATTERN);
			    	Calendar cal = Calendar.getInstance(Locale.US); cal.setLenient(true);
			    	cal.setTime(iFirstDate);
			    	for (int i = 0; i < iSection.getTime().getWeekCode().size(); i++) {
			    		if (iSection.getTime().getWeekCode().get(i)) {
			    			DayCode day = null;
			    			switch (cal.get(Calendar.DAY_OF_WEEK)) {
			    			case Calendar.MONDAY:
			    				day = DayCode.MON; break;
			    			case Calendar.TUESDAY:
			    				day = DayCode.TUE; break;
			    			case Calendar.WEDNESDAY:
			    				day = DayCode.WED; break;
			    			case Calendar.THURSDAY:
			    				day = DayCode.THU; break;
			    			case Calendar.FRIDAY:
			    				day = DayCode.FRI; break;
			    			case Calendar.SATURDAY:
			    				day = DayCode.SAT; break;
			    			case Calendar.SUNDAY:
			    				day = DayCode.SUN; break;
			    			}
			    			if ((iSection.getTime().getDayCode() & day.getCode()) == day.getCode()) {
				    			int d = cal.get(Calendar.DAY_OF_MONTH);
				    			int m = cal.get(Calendar.MONTH) + 1;
				    			if (df.format(cal.getTime()).equalsIgnoreCase(term) || eq(d + "." + m + ".",term) || eq(m + "/" + d, term)) return true;
			    			}
			    		}
			    		cal.add(Calendar.DAY_OF_YEAR, 1);
			    	}
				}
			}
			if (attr == null || attr.equals("room")) {
				if ((iSection.getRooms() == null || iSection.getRooms().isEmpty()) && term.equalsIgnoreCase("none")) return true;
				if (iSection.getRooms() != null) {
					for (RoomLocation r: iSection.getRooms()) {
						if (has(r.getName(), term)) return true;
					}
				}
			}
			if (attr == null || attr.equals("instr") || attr.equals("instructor")) {
				if (attr != null && (iSection.getChoice().getInstructorNames() == null || iSection.getChoice().getInstructorNames().isEmpty()) && term.equalsIgnoreCase("none")) return true;
				for (String instructor: iSection.getChoice().getInstructorNames().split(":")) {
					String[] nameEmail = instructor.split("\\|");
					if (has(nameEmail[0], term)) return true;
					if (nameEmail.length == 2) {
						String email = nameEmail[1];
						if (email.indexOf('@') >= 0) email = email.substring(0, email.indexOf('@'));
						if (eq(email, term)) return true;
					}
				}
			}
			if (attr != null && iSection.getTime() != null) {
				int start = parseStart(attr + ":" + term);
				if (start >= 0 && iSection.getTime().getStartSlot() == start) return true;
			}
			return false;
		}
		
		private boolean eq(String name, String term) {
			if (name == null) return false;
			return name.equalsIgnoreCase(term);
		}

		private boolean has(String name, String term) {
			if (name == null) return false;
			for (String t: name.split(" "))
				if (t.equalsIgnoreCase(term)) return true;
			return false;
		}
		
		private int parseDay(String token) {
			int days = 0;
			boolean found = false;
			do {
				found = false;
				for (int i=0; i<CONSTANTS.longDays().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.longDays()[i].toLowerCase())) {
						days |= DayCode.values()[i].getCode(); 
						token = token.substring(CONSTANTS.longDays()[i].length());
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
				for (int i=0; i<CONSTANTS.days().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.days()[i].toLowerCase())) {
						days |= DayCode.values()[i].getCode(); 
						token = token.substring(CONSTANTS.days()[i].length());
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
				for (int i=0; i<CONSTANTS.days().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.days()[i].substring(0,2).toLowerCase())) {
						days |= DayCode.values()[i].getCode(); 
						token = token.substring(2);
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
				for (int i=0; i<CONSTANTS.shortDays().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.shortDays()[i].toLowerCase())) {
						days |= DayCode.values()[i].getCode(); 
						token = token.substring(CONSTANTS.shortDays()[i].length());
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
				for (int i=0; i<CONSTANTS.freeTimeShortDays().length; i++) {
					if (token.toLowerCase().startsWith(CONSTANTS.freeTimeShortDays()[i].toLowerCase())) {
						days |= DayCode.values()[i].getCode(); 
						token = token.substring(CONSTANTS.freeTimeShortDays()[i].length());
						while (token.startsWith(" ")) token = token.substring(1);
						found = true;
					}
				}
			} while (found);
			return (token.isEmpty() ? days : 0);
		}
		
		private int parseStart(String token) {
			int startHour = 0, startMin = 0;
			String number = "";
			while (!token.isEmpty() && token.charAt(0) >= '0' && token.charAt(0) <= '9') { number += token.substring(0, 1); token = token.substring(1); }
			if (number.isEmpty()) return -1;
			if (number.length() > 2) {
				startHour = Integer.parseInt(number) / 100;
				startMin = Integer.parseInt(number) % 100;
			} else {
				startHour = Integer.parseInt(number);
			}
			while (token.startsWith(" ")) token = token.substring(1);
			if (token.startsWith(":")) {
				token = token.substring(1);
				while (token.startsWith(" ")) token = token.substring(1);
				number = "";
				while (!token.isEmpty() && token.charAt(0) >= '0' && token.charAt(0) <= '9') { number += token.substring(0, 1); token = token.substring(1); }
				if (number.isEmpty()) return -1;
				startMin = Integer.parseInt(number);
			}
			while (token.startsWith(" ")) token = token.substring(1);
			boolean hasAmOrPm = false;
			if (token.toLowerCase().startsWith("am")) { token = token.substring(2); hasAmOrPm = true; }
			if (token.toLowerCase().startsWith("a")) { token = token.substring(1); hasAmOrPm = true; }
			if (token.toLowerCase().startsWith("pm")) { token = token.substring(2); hasAmOrPm = true; if (startHour<12) startHour += 12; }
			if (token.toLowerCase().startsWith("p")) { token = token.substring(1); hasAmOrPm = true; if (startHour<12) startHour += 12; }
			if (startHour < 7 && !hasAmOrPm) startHour += 12;
			if (startMin % 5 != 0) startMin = 5 * ((startMin + 2)/ 5);
			if (startHour == 7 && startMin == 0 && !hasAmOrPm) startHour += 12;
			return (60 * startHour + startMin) / 5;
		}
    }
	
    protected boolean canContinue(ArrayList<Request> requests2resolve, int idx, int depth) {
        if (depth<=0) return false;
        if (iTimeoutReached) return false;
        return true;
    }
    
    protected boolean canContinueEvaluation() {
        return !iTimeoutReached;
    }
        
    protected boolean checkBound(ArrayList<Request> requests2resolve, int idx, int depth, Enrollment value, Set<Enrollment> conflicts) {
    	if (idx > 0 && !conflicts.isEmpty())
    		return false;
        int nrUnassigned = requests2resolve.size()-idx;
        if ((nrUnassigned + conflicts.size() > depth)) {
            return false;
        }
        for (Enrollment conflict: conflicts) {
            int confIdx = requests2resolve.indexOf(conflict.variable());
            if (confIdx >= 0 && confIdx <= idx) return false;
        }
        if (iMaxSectionsWithPenalty >= 0) {
        	int sectionsWithPenalty = 0;
        	for (Request r: iStudent.getRequests()) {
        		Enrollment e = r.getAssignment();
        		if (r.equals(value.variable())) { e = value; }
        		else if (conflicts.contains(e)) { e = null; }
        		if (e != null && e.isCourseRequest()) {
        			for (Section s: e.getSections())
        				if (s.getPenalty() >= 0) sectionsWithPenalty ++;
        		}
        	}
        	if (sectionsWithPenalty > iMaxSectionsWithPenalty) return false;
        }
        //TODO: Also check the bound
        return true;
    }
	
	public boolean isAllowed(Enrollment enrollment) {
		if (iRequiredSections != null && enrollment.getRequest() instanceof CourseRequest) {
			// Obey required sections
			Set<Section> required = iRequiredSections.get(enrollment.getRequest());
			if (required != null && !required.isEmpty()) {
				if (enrollment.getAssignments() == null) return false;
				for (Section r: required)
					if (!enrollment.getAssignments().contains(r)) return false;
			}
		}
		if (enrollment.getRequest().equals(iSelectedRequest)) {
			// Selected request must be assigned
			if (enrollment.getAssignments() == null || enrollment.getAssignments().isEmpty()) return false;
			// Selected section must be assigned differently
			if (iSelectedSection != null && enrollment.getAssignments().contains(iSelectedSection)) return false;
		}
		return true;
	}
	
	public boolean canLeaveUnassigned(Request request) {
		if (request instanceof CourseRequest) {
			if (iRequiredSections != null) {
				// Request with required section must be assigned
				Set<Section> required = iRequiredSections.get(request);
				if (required != null && !required.isEmpty()) return false;
			}
		} else {
			// Free time is required
			if (iRequiredFreeTimes.contains(request)) return false;
		}
		// Selected request must be assigned
		if (request.equals(iSelectedRequest)) return false;
		return true;
	}
	
	protected int compare(Suggestion s1, Suggestion s2) {
		return Double.compare(s1.getValue(), s2.getValue());
	}
	
	public class Suggestion implements Comparable<Suggestion> {
		private double iValue = 0.0;
		private int iNrUnassigned = 0;
		private int iUnassignedPriority = 0;
	   	private int iNrChanges = 0;
	    
		private long iId = iLastSuggestionId++;
    	private Enrollment[] iEnrollments;
    	private Section iSelectedEnrollment = null;
    	private boolean iSelectedEnrollmentChangeTime = false;
    	private TreeSet<Section> iSelectedSections = new TreeSet<Section>(new EnrollmentSectionComparator());
		
    	@SuppressWarnings("unchecked")
    	public Suggestion(ArrayList<Request> resolvedRequests) {
        	for (Request request: resolvedRequests) {
        		Enrollment enrollment = (Enrollment)request.getAssignment();
        		if (enrollment.getAssignments().isEmpty()) {
        			iNrUnassigned ++;
        			iUnassignedPriority += request.getPriority();
        		}
        		iValue += (enrollment == null || enrollment.getAssignments() == null || enrollment.getAssignments().isEmpty() ? 0.0 : enrollment.toDouble());
        		if (request.getInitialAssignment() != null && enrollment.isCourseRequest()) {
            		Enrollment original = (Enrollment)request.getInitialAssignment();
        			for (Iterator<Section> i = enrollment.getSections().iterator(); i.hasNext();) {
        				Section section = i.next();
        				Section originalSection = null;
        				for (Iterator<Section> j = original.getSections().iterator(); j.hasNext();) {
        					Section x = j.next();
        					if (x.getSubpart().getId() == section.getSubpart().getId()) {
        						originalSection = x; break;
        					}
        				}
        				if (originalSection == null || !ToolBox.equals(section.getTime(), originalSection.getTime()) ||
        					!ToolBox.equals(section.getRooms(), originalSection.getRooms()))
        					iNrChanges ++;
        			}
        		}
        	}
        	if (iSelectedRequest != null && iSelectedSection != null) {
        		Enrollment enrollment = (Enrollment)iSelectedRequest.getAssignment();
        		if (enrollment.getAssignments() != null && !enrollment.getAssignments().isEmpty()) {
        			for (Iterator<Section> i = enrollment.getSections().iterator(); i.hasNext();) {
        				Section section = i.next();
        				if (section.getSubpart().getId() == iSelectedSection.getSubpart().getId()) {
        					iSelectedEnrollment = section; break;
        				}
        				if (section.getSubpart().getConfig().getId() != iSelectedSection.getSubpart().getConfig().getId() &&
        					section.getSubpart().getInstructionalType().equals(iSelectedSection.getSubpart().getInstructionalType())) {
        					iSelectedEnrollment = section; break;
        				}
        			}        			
        		}
        	}
        	if (iSelectedEnrollment != null)
        		iSelectedEnrollmentChangeTime = !ToolBox.equals(iSelectedEnrollment.getTime(), iSelectedSection.getTime());
        	if (iSelectedRequest != null) {
        		Enrollment enrollment = (Enrollment)iSelectedRequest.getAssignment();
        		if (enrollment.isCourseRequest() && enrollment.getAssignments() != null && !enrollment.getAssignments().isEmpty())
        			iSelectedSections.addAll(enrollment.getSections());
        	}
 		}
		
		public void init() {
        	iEnrollments = new Enrollment[iStudent.getRequests().size()];
        	for (int i = 0; i < iStudent.getRequests().size(); i++) {
    			Request r = (Request)iStudent.getRequests().get(i);
        		iEnrollments[i] = (Enrollment)r.getAssignment();
        		if (iEnrollments[i] == null) {
        			Config c = null;
            		if (r instanceof CourseRequest)
            			c = (Config)((Course)((CourseRequest)r).getCourses().get(0)).getOffering().getConfigs().get(0);
            		iEnrollments[i] = new Enrollment(r, 0, c, null);
        		}
        	}
		}
		
		public Enrollment[] getEnrollments() { return iEnrollments; }
		public double getValue() { return iValue; }
		public int getNrUnassigned() { return iNrUnassigned; }
		public double getAverageUnassignedPriority() { return ((double)iUnassignedPriority) / iNrUnassigned; }
		public int getNrChanges() { return iNrChanges; }
		
		@SuppressWarnings("unchecked")
		public boolean sameSelectedSection() {
        	if (iSelectedRequest != null && iSelectedEnrollment != null) {
        		Enrollment enrollment = (Enrollment)iSelectedRequest.getAssignment();
        		if (enrollment != null && enrollment.getAssignments().contains(iSelectedEnrollment)) return true;
        		if (iSelectedEnrollmentChangeTime) {
        			Section selectedEnrollment = null;
        			for (Iterator<Section> i = enrollment.getSections().iterator(); i.hasNext();) {
        				Section section = i.next();
        				if (section.getSubpart().getId() == iSelectedSection.getSubpart().getId()) {
        					selectedEnrollment = section; break;
        				}
        				if (section.getSubpart().getConfig().getId() != iSelectedSection.getSubpart().getConfig().getId() &&
        					section.getSubpart().getInstructionalType().equals(iSelectedSection.getSubpart().getInstructionalType())) {
        					selectedEnrollment = section; break;
        				}
        			}			
            		if (selectedEnrollment != null && ToolBox.equals(selectedEnrollment.getTime(), iSelectedEnrollment.getTime())) return true;
        		}
        	}
        	return false;
		}
		
		public int compareTo(Suggestion suggestion) {
			int cmp = Double.compare(getNrUnassigned(), suggestion.getNrUnassigned());
			if (cmp != 0) return cmp;
			if (getNrUnassigned() > 0) {
				cmp = Double.compare(suggestion.getAverageUnassignedPriority(), getAverageUnassignedPriority());
				if (cmp != 0) return cmp;
			}
			cmp = Double.compare(getNrChanges(), suggestion.getNrChanges());
			if (cmp != 0) return cmp;
			
			Iterator<Section> i1 = iSelectedSections.iterator();
			Iterator<Section> i2 = suggestion.iSelectedSections.iterator();
			SectionAssignmentComparator c = new SectionAssignmentComparator();
			while (i1.hasNext() && i2.hasNext()) {
				cmp = c.compare(i1.next(), i2.next());
				if (cmp != 0) return cmp;
			}

			cmp = compare(this, suggestion);
			if (cmp != 0) return cmp;

			return Double.compare(iId, suggestion.iId);
		}
	}
	
	public class EnrollmentSectionComparator implements Comparator<Section> {
	    public boolean isParent(Section s1, Section s2) {
			Section p1 = s1.getParent();
			if (p1==null) return false;
			if (p1.equals(s2)) return true;
			return isParent(p1, s2);
		}

		public int compare(Section a, Section b) {
			if (iSelectedSection != null && iSelectedSection.getSubpart().getId() == a.getSubpart().getId()) return -1;
			if (iSelectedSection != null && iSelectedSection.getSubpart().getId() == b.getSubpart().getId()) return 1;

			if (isParent(a, b)) return 1;
	        if (isParent(b, a)) return -1;

	        int cmp = a.getSubpart().getInstructionalType().compareToIgnoreCase(b.getSubpart().getInstructionalType());
			if (cmp != 0) return cmp;
			
			return Double.compare(a.getId(), b.getId());
		}
	}
	
	public class SectionAssignmentComparator implements Comparator<Section> {
		public int compare(Section a, Section b) {
			TimeLocation t1 = (a == null ? null : a.getTime());
			TimeLocation t2 = (b == null ? null : b.getTime());
			if (t1 != null && t2 != null) {
				ArrayList<DayCode> d1 = DayCode.toDayCodes(t1.getDayCode());
				ArrayList<DayCode> d2 = DayCode.toDayCodes(t2.getDayCode());
				for (int i = 0; i < Math.min(d1.size(), d2.size()); i++) {
					int cmp = Double.compare(d1.get(i).getIndex(), d2.get(i).getIndex());
					if (cmp != 0) return cmp;
				}
				int cmp = Double.compare(t1.getStartSlot(), t2.getStartSlot());
				if (cmp != 0) return cmp;
			}
			String r1 = (a == null || a.getRooms() == null ? null : a.getRooms().toString());
			String r2 = (b == null || b.getRooms() == null ? null : b.getRooms().toString());
			if (r1 != null && r2 != null) {
				int cmp = r1.compareToIgnoreCase(r2);
				if (cmp != 0) return cmp;
			}
			
			return 0;
		}
	}

	public int getNrMatched() {
		return iMatched;
	}

}
