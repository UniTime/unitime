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

import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.cpsolver.ifs.util.DataProperties;
import net.sf.cpsolver.studentsct.extension.DistanceConflict;
import net.sf.cpsolver.studentsct.extension.TimeOverlapsCounter;
import net.sf.cpsolver.studentsct.heuristics.selection.BranchBoundSelection;
import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.FreeTimeRequest;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Student;
import net.sf.cpsolver.studentsct.model.Subpart;

/**
 * @author Tomas Muller
 */
public class SuggestionSelection extends BranchBoundSelection implements OnlineSectioningSelection {
	protected Set<FreeTimeRequest> iRequiredFreeTimes;
	protected Hashtable<CourseRequest, Config> iRequiredConfig = null;
	protected Hashtable<CourseRequest, Hashtable<Subpart, Section>> iRequiredSection = null;
	protected Hashtable<CourseRequest, Set<Section>> iPreferredSections = null;
	/** add up to 50% for preferred sections */
	private double iPreferenceFactor = 0.500;
	
    public SuggestionSelection(DataProperties properties) {
    	super(properties);
    	iPreferenceFactor = properties.getPropertyDouble("StudentWeights.PreferenceFactor", iPreferenceFactor);
    }
    
	@Override
	public void setPreferredSections(Hashtable<CourseRequest, Set<Section>> preferredSections) {
    	iPreferredSections = preferredSections;
	}

	@Override
	public void setRequiredSections(Hashtable<CourseRequest, Set<Section>> requiredSections) {
		iRequiredConfig = new Hashtable<CourseRequest, Config>();
		iRequiredSection = new Hashtable<CourseRequest, Hashtable<Subpart,Section>>();
    	if (requiredSections != null) {
    		for (Map.Entry<CourseRequest, Set<Section>> entry: requiredSections.entrySet()) {
    			Hashtable<Subpart, Section> subSection = new Hashtable<Subpart, Section>();
    			iRequiredSection.put(entry.getKey(), subSection);
    			for (Section section: entry.getValue()) {
    				if (subSection.isEmpty())
    					iRequiredConfig.put(entry.getKey(), section.getSubpart().getConfig());
    				subSection.put(section.getSubpart(), section);
    			}
    		}
    	}
	}

	@Override
	public void setRequiredFreeTimes(Set<FreeTimeRequest> requiredFreeTimes) {
    	iRequiredFreeTimes = requiredFreeTimes;
	}

	@Override
	public BranchBoundNeighbour select(Student student) {
		return getSelection(student).select();
	}
	
	@Override
	public void setModel(OnlineSectioningModel model) {
		super.setModel(model);
	}
    
    public Selection getSelection(Student student) {
        return new Selection(student);
    }

    public class Selection extends BranchBoundSelection.Selection {
        public Selection(Student student) {
        	super(student);
        }
        
        public boolean isAllowed(int idx, Enrollment enrollment) {
        	if (enrollment.isCourseRequest()) {
        		CourseRequest request = (CourseRequest)enrollment.getRequest();
        		Config reqConfig = iRequiredConfig.get(request);
        		if (reqConfig != null) {
        			if (!reqConfig.equals(enrollment.getConfig())) return false;
            		Hashtable<Subpart, Section> reqSections = iRequiredSection.get(request);
        			for (Section section: enrollment.getSections()) {
        				Section reqSection = reqSections.get(section.getSubpart());
        				if (reqSection == null) continue;
        				if (!section.equals(reqSection)) return false;
        			}
        		}
        	} else if (iRequiredFreeTimes.contains(enrollment.getRequest())) {
        		if (enrollment.getAssignments() == null || enrollment.getAssignments().isEmpty()) return false;
        	}
        	return true;
        }
        
        @Override
        public boolean inConflict(int idx, Enrollment enrollment) {
        	return super.inConflict(idx, enrollment) || !isAllowed(idx, enrollment);
        }
        
        @Override
        public Enrollment firstConflict(int idx, Enrollment enrollment) {
            Enrollment conflict = super.firstConflict(idx, enrollment);
            if (conflict!=null) return conflict;
            return (isAllowed(idx, enrollment) ? null : enrollment);
        }
        
        @Override
        protected boolean canLeaveUnassigned(Request request) {
        	if (request instanceof CourseRequest) {
        		if (iRequiredConfig.get(request) != null) return false;
        	} else if (iRequiredFreeTimes.contains(request)) return false;
        	return true;
        }
        
        @Override
        protected List<Enrollment> values(final CourseRequest request) {
        	return super.values(request);
        }
        
        @Override
        protected double getWeight(Enrollment enrollment, Set<DistanceConflict.Conflict> distanceConflicts, Set<TimeOverlapsCounter.Conflict> timeOverlappingConflicts) {
        	double weight = super.getWeight(enrollment, distanceConflicts, timeOverlappingConflicts);
        	if (enrollment.isCourseRequest() && iPreferredSections != null) {
        		Set<Section> preferred = iPreferredSections.get((CourseRequest)enrollment.getRequest());
        		if (preferred != null && !preferred.isEmpty()) {
        			double nrPreferred = 0;
        			for (Section section: enrollment.getSections())
        				if (preferred.contains(section)) nrPreferred ++;
        			double preferredFraction = nrPreferred / preferred.size();
        			weight *= 1.0 + iPreferenceFactor * preferredFraction; // add up to 50% for preferred sections
        		}
        	}
        	return weight;
        }
        
        @Override
        protected double getBound(Request r) {
        	double bound = super.getBound(r);
        	if (r instanceof CourseRequest) {
        		Set<Section> preferred = iPreferredSections.get((CourseRequest)r);
        		if (preferred != null && !preferred.isEmpty())
        			bound *= (1.0 + iPreferenceFactor); // add 50% if can be preferred
        	}
        	return bound;
        }

    }
}
