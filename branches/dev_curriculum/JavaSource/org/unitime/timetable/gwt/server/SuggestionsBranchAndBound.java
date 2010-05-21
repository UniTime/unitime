/*
 * UniTime 4.0 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.server;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sf.cpsolver.coursett.model.TimeLocation;
import net.sf.cpsolver.ifs.model.Model;
import net.sf.cpsolver.ifs.model.Value;
import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.ifs.util.ToolBox;
import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.FreeTimeRequest;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;

public class SuggestionsBranchAndBound {
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
	private Model iModel;
	private Hashtable<Request, Collection<Enrollment>> iValues = new Hashtable<Request, Collection<Enrollment>>();
	private long iLastSuggestionId = 0;
	
	public SuggestionsBranchAndBound(DataProperties properties, Student student,
			Hashtable<CourseRequest, Set<Section>> requiredSections,
			Set<FreeTimeRequest> requiredFreeTimes,
			Hashtable<CourseRequest, Set<Section>> preferredSections,
			Request selectedRequest, Section selectedSection) {
		iRequiredSections = requiredSections;
		iRequiredFreeTimes = requiredFreeTimes;
		iPreferredSections = preferredSections;
		iSelectedRequest = selectedRequest;
		iSelectedSection = selectedSection;
		iStudent = student;
		iModel = selectedRequest.getModel();
		iMaxDepth = properties.getPropertyInt("Suggestions.MaxDepth", iMaxDepth);
		iTimeout = properties.getPropertyLong("Suggestions.Timeout", iTimeout);
		iMaxSuggestions = properties.getPropertyInt("Suggestions.MaxSuggestions", iMaxSuggestions);
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
        
        for (Enumeration<Request> e = iStudent.getRequests().elements(); e.hasMoreElements();) {
        	Request request = e.nextElement();
        	if (request.getAssignment() == null && request instanceof FreeTimeRequest) {
        		FreeTimeRequest ft = (FreeTimeRequest)request;
        		Enrollment enrollment = ft.createEnrollment();
        		if (iModel.conflictValues(enrollment).isEmpty()) ft.assign(0, enrollment);
        	}
        }
        
        for (Enumeration<Request> e = iStudent.getRequests().elements(); e.hasMoreElements();) {
        	Request request = e.nextElement();
    		request.setInitialAssignment(request.getAssignment());
        }
		
        backtrack(requests2resolve, altRequests2resolve, 0, iMaxDepth);
        
		iT1 = System.currentTimeMillis();
		return iSuggestions;
	}
		
    @SuppressWarnings("unchecked")
	protected void backtrack(ArrayList<Request> requests2resolve, TreeSet<Request> altRequests2resolve, int idx, int depth) {
        if (!iTimeoutReached && iTimeout > 0 && System.currentTimeMillis() - iT0 > iTimeout)
            iTimeoutReached = true;
        int nrUnassigned = requests2resolve.size() - idx;
        if (nrUnassigned==0) {
    		Suggestion s = new Suggestion(requests2resolve);
    		if (iSuggestions.size() >= iMaxSuggestions && iSuggestions.last().compareTo(s) <= 0) return;
    		for (Iterator<Suggestion> i = iSuggestions.iterator(); i.hasNext();) {
    			Suggestion x = (Suggestion)i.next();
        		if (x.sameSelectedSection()) {
        			if (x.compareTo(s) <= 0) return;
        			i.remove();
        		}
        	}
    		s.init();
			iSuggestions.add(s);
			if (iSuggestions.size() > iMaxSuggestions) iSuggestions.remove(iSuggestions.last());
        	return;
        }
        if (!canContinue(requests2resolve, idx, depth)) return;
        Request request = requests2resolve.get(idx);
        for (Enrollment enrollment: values(request)) {
            if (!canContinueEvaluation()) break;
            if (!isAllowed(enrollment)) continue;
            if (enrollment.equals(request.getAssignment())) continue;
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
                	for (Request r: altRequests2resolve) {
                		newVariables2resolve.add(r);
                		backtrack(newVariables2resolve, null, idx+1, depth);
                		newVariables2resolve.remove(r);
                	}
            	} else {
                    backtrack(newVariables2resolve, altRequests2resolve, idx+1, depth-1);
            	}
            } else {
                backtrack(newVariables2resolve, altRequests2resolve, idx+1, depth-1);
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
	protected Collection<Enrollment> values(Request request) {
    	Collection<Enrollment> values = iValues.get(request);
    	if (values != null) return values;
    	if (request instanceof CourseRequest) {
    		CourseRequest cr = (CourseRequest)request;
    		values = (cr.equals(iSelectedRequest) ? cr.getAvaiableEnrollments() : cr.getAvaiableEnrollmentsSkipSameTime());
    	} else {
    		values = new ArrayList<Enrollment>();
    		values.add(((FreeTimeRequest)request).createEnrollment());
    	}
		if (canLeaveUnassigned(request)) {
    		Config config = null;
    		if (request instanceof CourseRequest)
    			config = (Config)((Course)((CourseRequest)request).getCourses().firstElement()).getOffering().getConfigs().firstElement();
    		values.add(new Enrollment(request, 0, config, new HashSet<Section>()));
		}
		iValues.put(request, values);
        return values;
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
        int nrUnassigned = requests2resolve.size()-idx;
        if ((nrUnassigned + conflicts.size() > depth)) {
            return false;
        }
        for (Enrollment conflict: conflicts) {
            int confIdx = requests2resolve.indexOf(conflict.variable());
            if (confIdx >= 0 && confIdx <= idx) return false;
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
	
	public double value(Enrollment enrollment) {
		if (enrollment.getAssignments() == null || enrollment.getAssignments().isEmpty()) return 0.0;
		return enrollment.toDouble();
	}
	
	public double bound(Request request) {
		return request.getBound();
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
        		iValue += value(enrollment);
        		if (request.getInitialAssignment() != null && enrollment.isCourseRequest()) {
            		Enrollment original = (Enrollment)request.getInitialAssignment();
        			for (Iterator<Section> i = enrollment.getAssignments().iterator(); i.hasNext();) {
        				Section section = i.next();
        				Section originalSection = null;
        				for (Iterator<Section> j = original.getAssignments().iterator(); j.hasNext();) {
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
        			for (Iterator<Section> i = enrollment.getAssignments().iterator(); i.hasNext();) {
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
        			iSelectedSections.addAll(enrollment.getAssignments());
        	}
 		}
		
		public void init() {
        	iEnrollments = new Enrollment[iStudent.getRequests().size()];
        	for (int i = 0; i < iStudent.getRequests().size(); i++) {
    			Request r = (Request)iStudent.getRequests().elementAt(i);
        		iEnrollments[i] = (Enrollment)r.getAssignment();
        		if (iEnrollments[i] == null) {
        			Config c = null;
            		if (r instanceof CourseRequest)
            			c = (Config)((Course)((CourseRequest)r).getCourses().firstElement()).getOffering().getConfigs().firstElement();
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
        			for (Iterator<Section> i = enrollment.getAssignments().iterator(); i.hasNext();) {
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

			cmp = Double.compare(getValue(), suggestion.getValue());
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


}
