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
package org.unitime.timetable.onlinesectioning.model;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.coursett.Constants;
import org.cpsolver.coursett.model.Placement;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.ifs.util.DistanceMetric;
import org.cpsolver.studentsct.constraint.LinkedSections;
import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.Offering;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Student;
import org.cpsolver.studentsct.model.Subpart;
import org.cpsolver.studentsct.model.Unavailability;
import org.cpsolver.studentsct.online.OnlineConfig;
import org.cpsolver.studentsct.online.OnlineReservation;
import org.cpsolver.studentsct.online.OnlineSection;
import org.cpsolver.studentsct.reservation.CourseRestriction;
import org.cpsolver.studentsct.reservation.CurriculumOverride;
import org.cpsolver.studentsct.reservation.CurriculumRestriction;
import org.cpsolver.studentsct.reservation.DummyReservation;
import org.cpsolver.studentsct.reservation.GroupReservation;
import org.cpsolver.studentsct.reservation.IndividualRestriction;
import org.cpsolver.studentsct.reservation.ReservationOverride;
import org.cpsolver.studentsct.reservation.Restriction;
import org.cpsolver.studentsct.reservation.UniversalOverride;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseReservation;
import org.unitime.timetable.model.CurriculumOverrideReservation;
import org.unitime.timetable.model.CurriculumReservation;
import org.unitime.timetable.model.GroupOverrideReservation;
import org.unitime.timetable.model.IndividualOverrideReservation;
import org.unitime.timetable.model.IndividualReservation;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.LearningCommunityReservation;
import org.unitime.timetable.model.OverrideReservation;
import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.model.StudentGroupReservation;
import org.unitime.timetable.model.StudentSchedulingRule;
import org.unitime.timetable.model.UniversalOverrideReservation;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.StudentMatcher;
import org.unitime.timetable.solver.studentsct.StudentSolver;

/**
 * @author Tomas Muller
 */
public class XOffering implements Serializable, Externalizable {
    private static final long serialVersionUID = 1L;
	private Long iUniqueId = null;
    private String iName = null;
    private List<XConfig> iConfigs = new ArrayList<XConfig>();
    private List<XCourse> iCourses = new ArrayList<XCourse>();
    private List<XReservation> iReservations = new ArrayList<XReservation>();
    private List<XDistribution> iDistrubutions = new ArrayList<XDistribution>();
    private List<XRestriction> iRestrictions = new ArrayList<XRestriction>();
    private boolean iWaitList = false, iReSchedule = false;

    public XOffering() {
    }
    
    public XOffering(ObjectInput in) throws IOException, ClassNotFoundException {
    	readExternal(in);
    }
    
    public XOffering(InstructionalOffering offering, Collection<XDistribution> distributions, OnlineSectioningHelper helper) {
    	iUniqueId = offering.getUniqueId();
    	iName = offering.getCourseName();
    	iWaitList = offering.effectiveWaitList();
    	iReSchedule = offering.effectiveReSchedule();
    	for (CourseOffering course: offering.getCourseOfferings())
    		if (course.isAllowStudentScheduling())
    			iCourses.add(new XCourse(course, helper));
    	for (InstrOfferingConfig config: offering.getInstrOfferingConfigs())
    		iConfigs.add(new XConfig(config, helper));
        for (Reservation reservation: offering.getReservations()) {
        	if (reservation instanceof OverrideReservation) {
        		iReservations.add(new XIndividualReservation(this, (OverrideReservation)reservation));
        	} else if (reservation instanceof IndividualOverrideReservation) {
        		iReservations.add(new XIndividualReservation(this, (IndividualOverrideReservation)reservation));
        	} else if (reservation instanceof IndividualReservation) {
        		iReservations.add(new XIndividualReservation(this, (IndividualReservation)reservation));
        	} else if (reservation instanceof GroupOverrideReservation) {
        		iReservations.add(new XGroupReservation(this, (GroupOverrideReservation)reservation));
        	} else if (reservation instanceof LearningCommunityReservation) {
        		iReservations.add(new XLearningCommunityReservation(this, (LearningCommunityReservation)reservation));
        	} else if (reservation instanceof StudentGroupReservation) {
        		iReservations.add(new XGroupReservation(this, (StudentGroupReservation)reservation));
        	} else if (reservation instanceof CurriculumOverrideReservation) {
        		iReservations.add(new XCurriculumReservation(this, (CurriculumOverrideReservation)reservation));
        	} else if (reservation instanceof CurriculumReservation) {
        		iReservations.add(new XCurriculumReservation(this, (CurriculumReservation)reservation));
        	} else if (reservation instanceof CourseReservation) {
        		iReservations.add(new XCourseReservation(this, (CourseReservation)reservation));
        	} else if (reservation instanceof UniversalOverrideReservation) {
        		iReservations.add(new XUniversalReservation(this, (UniversalOverrideReservation)reservation));
        	}
        }
        if (offering.isByReservationOnly())
        	iReservations.add(new XDummyReservation(this));
        if (distributions != null)
        	iDistrubutions.addAll(distributions);
        
        Collections.sort(iConfigs);
    }
    
    public XOffering(Offering offering, Collection<LinkedSections> links) {
    	iUniqueId = offering.getId();
    	iName = offering.getName();
    	for (Course course: offering.getCourses())
    		iCourses.add(new XCourse(course));
    	for (Config config: offering.getConfigs())
    		iConfigs.add(new XConfig(config));
    	for (org.cpsolver.studentsct.reservation.Reservation reservation: offering.getReservations()) {
    		if (reservation instanceof ReservationOverride) {
        		iReservations.add(new XIndividualReservation((ReservationOverride)reservation));
    		} else if (reservation instanceof org.cpsolver.studentsct.reservation.LearningCommunityReservation) {
    			iReservations.add(new XLearningCommunityReservation((org.cpsolver.studentsct.reservation.LearningCommunityReservation)reservation));
        	} else if (reservation instanceof GroupReservation) {
        		iReservations.add(new XIndividualReservation((GroupReservation)reservation));
    		} else if (reservation instanceof org.cpsolver.studentsct.reservation.IndividualReservation) {
        		iReservations.add(new XIndividualReservation((org.cpsolver.studentsct.reservation.IndividualReservation)reservation));
    		} else if (reservation instanceof CurriculumOverride) {
    			iReservations.add(new XCurriculumReservation((CurriculumOverride)reservation));
        	} else if (reservation instanceof org.cpsolver.studentsct.reservation.CurriculumReservation) {
        		iReservations.add(new XCurriculumReservation((org.cpsolver.studentsct.reservation.CurriculumReservation)reservation));
        	} else if (reservation instanceof org.cpsolver.studentsct.reservation.CourseReservation) {
        		iReservations.add(new XCourseReservation((org.cpsolver.studentsct.reservation.CourseReservation)reservation));
        	} else if (reservation instanceof UniversalOverride) {
        		iReservations.add(new XUniversalReservation((org.cpsolver.studentsct.reservation.UniversalOverride)reservation));
        	}  else if (reservation instanceof DummyReservation) {
        		iReservations.add(new XDummyReservation(this));
        	}
    	}
    	for (org.cpsolver.studentsct.reservation.Restriction restriction: offering.getRestrictions()) {
    		if (restriction instanceof IndividualRestriction) {
    			iRestrictions.add(new XIndividualRestriction((IndividualRestriction)restriction));
    		} else if (restriction instanceof CurriculumRestriction) {
    			iRestrictions.add(new XCurriculumRestriction((CurriculumRestriction)restriction));
    		} else if (restriction instanceof CourseRestriction) {
    			iRestrictions.add(new XCourseRestriction((CourseRestriction)restriction));
    		}    		
    	}
		Set<Set<Long>> ignConf = new HashSet<Set<Long>>();
		long id = 1;
		if (links != null)
			for (LinkedSections link: links)
				if (link.getOfferings().contains(offering))
					iDistrubutions.add(new XDistribution(link, id++));
		for (Config config: offering.getConfigs())
			for (Subpart subpart: config.getSubparts())
				for (Section section: subpart.getSections())
					if (section.getIgnoreConflictWithSectionIds() != null) {
						HashSet<Long> ids = new HashSet<Long>(section.getIgnoreConflictWithSectionIds()); ids.add(section.getId());
						if (ids.size() > 1 && !ignConf.add(ids))
							iDistrubutions.add(new XDistribution(XDistributionType.IngoreConflicts, id++, offering.getId(), ids));
					}
    }
    
    /** Offering id */
    public Long getOfferingId() {
        return iUniqueId;
    }

    /** Offering name */
    public String getName() {
        return iName;
    }

    /** Possible configurations */
    public List<XConfig> getConfigs() {
        return iConfigs;
    }

    /**
     * List of courses. One instructional offering can contain multiple courses
     * (names under which it is offered)
     */
    public List<XCourse> getCourses() {
        return iCourses;
    }
    
    public boolean hasCrossList() {
    	return iCourses.size() > 1;
    }
    
    public boolean isWaitList() {
    	return iWaitList;
    }
    
    public boolean isReSchedule() {
    	return iReSchedule;
    }
    
    public XCourse getControllingCourse() {
    	for (XCourse course: getCourses())
    		if (course.isControlling()) return course;
    	return (getCourses().isEmpty() ? null : getCourses().get(0));
    }
    
    /** Course of this offering with the given id */
    public XCourse getCourse(Long courseId) {
    	if (courseId == null) {
    		for (XCourse course: getCourses())
    			if (course.getCourseName().equals(getName())) return course;
    		return getCourses().get(0);
    	}
    	for (XCourse course: getCourses())
    		if (course.getCourseId().equals(courseId)) return course;
    	return null;
    }
    
    /** Course of this offering with the given id */
    public XCourse getCourse(XCourseId courseId) {
    	for (XCourse course: getCourses())
    		if (course.getCourseId().equals(courseId.getCourseId())) return course;
    	return null;
    }
    
    /** List of sections of the given enrollment */
    public List<XSection> getSections(XEnrollment enrollment) {
    	if (enrollment == null) return null;
    	List<XSection> sections = new ArrayList<XSection>();
        for (XConfig config : getConfigs()) {
            for (XSubpart subpart : config.getSubparts()) {
                for (XSection section : subpart.getSections()) {
                    if (enrollment.getSectionIds().contains(section.getSectionId()))
                    	sections.add(section);
                }
            }
        }
        return sections;
    }

    /**
     * Return section of the given id, if it is part of one of this offering
     * configurations.
     */
    public XSection getSection(Long sectionId) {
    	if (sectionId == null) return null;
        for (XConfig config : getConfigs()) {
            for (XSubpart subpart : config.getSubparts()) {
                for (XSection section : subpart.getSections()) {
                    if (section.getSectionId().equals(sectionId))
                        return section;
                }
            }
        }
        return null;
    }
    
    /**
     * Return sections of the given external id, if it is part of one of this offering
     * configurations.
     */
    public List<XSection> getSections(Long courseId, String externalId) {
    	List<XSection> ret = new ArrayList<XSection>();
    	for (XConfig config : getConfigs()) {
            for (XSubpart subpart : config.getSubparts()) {
                for (XSection section : subpart.getSections()) {
                    if (externalId.equals(section.getExternalId(courseId)))
                    	ret.add(section);
                }
            }
        }
    	return ret;
    }
    
    /**
     * Return subpart of the given id, if it is part of this offering configuraions.
     */
    public XSubpart getSubpart(Long subpartId) {
    	if (subpartId == null) return null;
        for (XConfig config : getConfigs()) {
            for (XSubpart subpart : config.getSubparts()) {
                if (subpart.getSubpartId().equals(subpartId))
                	return subpart;
            }
        }
        return null;
    }
    
    /**
     * Return config of the given id, if it is part of this offering configuraions.
     */
    public XConfig getConfig(Long configId) {
    	if (configId == null) return null;
        for (XConfig config : getConfigs()) {
            if (config.getConfigId().equals(configId))
            	return config;
        }
        return null;
    }
    
    /** Return set of instructional types, union over all configurations. */
    public Set<String> getInstructionalTypes() {
        Set<String> instructionalTypes = new HashSet<String>();
        for (XConfig config : getConfigs()) {
            for (XSubpart subpart : config.getSubparts()) {
                instructionalTypes.add(subpart.getInstructionalType());
            }
        }
        return instructionalTypes;
    }

    /**
     * Return list of all subparts of the given isntructional type for this
     * offering.
     */
    public Set<XSubpart> getSubparts(String instructionalType) {
        Set<XSubpart> subparts = new HashSet<XSubpart>();
        for (XConfig config : getConfigs()) {
            for (XSubpart subpart : config.getSubparts()) {
                if (instructionalType.equals(subpart.getInstructionalType()))
                    subparts.add(subpart);
            }
        }
        return subparts;
    }

    @Override
    public String toString() {
        return iName;
    }
    
    /** Reservations associated with this offering */
    public List<XReservation> getReservations() { return iReservations; }
    
    public List<XRestriction> getRestrictions() { return iRestrictions; }
    
    /**
     * Get reservations that require this section
     */
    public List<XReservation> getSectionReservations(Long sectionId) {
    	List<XReservation> ret = new ArrayList<XReservation>();
    	for (XReservation reservation: iReservations)
    		for (Set<Long> sectionIds: reservation.getSections().values())
    			if (sectionIds.contains(sectionId))
    				ret.add(reservation);
        return ret;
    }
    
    /**
     * Get reservations that require this config
     */
    public List<XReservation> getConfigReservations(Long configId) {
    	List<XReservation> ret = new ArrayList<XReservation>();
    	for (XReservation reservation: iReservations)
    		if (reservation.getConfigsIds().contains(configId))
    			ret.add(reservation);
        return ret;
    }
    
    public int getUnreservedSectionSpace(Long sectionId, XEnrollments enrollments) {
    	XSection section = getSection(sectionId);
    	Long configId = getSubpart(section.getSubpartId()).getConfigId();
    	// section is unlimited -> there is unreserved space unless there is an unlimited reservation too 
        // (in which case there is no unreserved space)
        if (section.getLimit() < 0) {
            // exclude reservations that are not directly set on this section
            for (XReservation r: getSectionReservations(sectionId)) {
                // ignore expired reservations
                if (r.isExpired()) continue;
                // ignore reservations NOT set directly on the section
                if (!r.hasSectionRestriction(sectionId)) continue;
                // there is an unlimited reservation -> no unreserved space
                if (r.getLimit(configId) < 0) return 0;
            }
            return Integer.MAX_VALUE;
        }
        
        int available = section.getLimit() - enrollments.countEnrollmentsForSection(sectionId);
        // exclude reservations that are not directly set on this section
        for (XReservation r: getSectionReservations(sectionId)) {
            // ignore expired reservations
            if (r.isExpired()) continue;
            // ignore reservations NOT set directly on the section
            if (!r.hasSectionRestriction(sectionId)) continue;
            // unlimited reservation -> all the space is reserved
            if (r.getLimit(configId) < 0.0) return 0;
            // compute space that can be potentially taken by this reservation
            int reserved = r.getReservedAvailableSpace(enrollments, configId);
            // deduct the space from available space
            available -= Math.max(0, reserved);
        }
        
        return available;
    }
    
    public int getUnreservedConfigSpace(Long configId, XEnrollments enrollments) {
    	XConfig config = getConfig(configId);
        // configuration is unlimited -> there is unreserved space unless there is an unlimited reservation too 
        // (in which case there is no unreserved space)
        if (config.getLimit() < 0) {
            // exclude reservations that are not directly set on this section
            for (XReservation r: getConfigReservations(configId)) {
                // ignore expired reservations
                if (r.isExpired()) continue;
                // ignore reservations NOT set directly on the config
                if (!r.hasConfigRestriction(configId)) continue;
                // there is an unlimited reservation -> no unreserved space
                if (r.getLimit(configId) < 0) return 0;
            }
            return Integer.MAX_VALUE;
        }
        
        int available = config.getLimit() - enrollments.countEnrollmentsForConfig(configId);
        // exclude reservations that are not directly set on this section
        for (XReservation r: getConfigReservations(configId)) {
            // ignore expired reservations
            if (r.isExpired()) continue;
            // ignore reservations NOT set directly on the config
            if (!r.hasConfigRestriction(configId)) continue;
            // unlimited reservation -> all the space is reserved
            if (r.getLimit(configId) < 0) return 0;
            // compute space that can be potentially taken by this reservation
            double reserved = r.getReservedAvailableSpace(enrollments, configId);
            // deduct the space from available space
            available -= Math.max(0, reserved);
        }
        
        return available;
    }
    
    public int getUnreservedSpace(XEnrollments enrollments) {
        // compute available space
        int available = 0;
        for (XConfig config: getConfigs()) {
            available += config.getLimit() - enrollments.countEnrollmentsForConfig(config.getConfigId());
            // offering is unlimited -> there is unreserved space unless there is an unlimited reservation too 
            // (in which case there is no unreserved space)
            if (config.getLimit() < 0) {
                for (XReservation r: getReservations()) {
                    // ignore expired reservations
                    if (r.isExpired()) continue;
                    // there is an unlimited reservation -> no unreserved space
                    if (r.getLimit() < 0) return 0;
                }
                return Integer.MAX_VALUE;
            }
        }
        
        // compute reserved space (out of the available space)
        int reserved = 0;
        for (XReservation r: getReservations()) {
            // ignore expired reservations
            if (r.isExpired()) continue;
            // unlimited reservation -> no unreserved space
            if (r.getLimit() < 0) return 0;
            reserved += Math.max(0.0,  r.getReservedAvailableSpace(enrollments));
        }
        
        return available - reserved;
    }
    
    /**
     * Return minimum of two limits where -1 counts as unlimited (any limit is smaller)
     */
    private static int min(int l1, int l2) {
        return (l1 < 0 ? l2 : l2 < 0 ? l1 : Math.min(l1, l2));
    }
    
    /**
     * Add two limits where -1 counts as unlimited (unlimited plus anything is unlimited)
     */
    private static int add(int l1, int l2) {
        return (l1 < 0 ? -1 : l2 < 0 ? -1 : l1 + l2);
    }
    
    /**
     * Compute offering limit excluding cancelled sections
     */
    public int getLimit() {
    	int offeringLimit = 0;
    	for (XConfig config: getConfigs()) {
    		Integer configLimit = null;
    		for (XSubpart subpart: config.getSubparts()) {
    			int subpartLimit = 0;
    			for (XSection section: subpart.getSections()) {
    				if (!section.isCancelled()) {
    					subpartLimit = add(subpartLimit, section.getLimit());
    				}
    			}
    			if (configLimit == null)
    				configLimit = subpartLimit;
    			else
    				configLimit = min(configLimit, subpartLimit);
    		}
    		if (configLimit != null)
    			offeringLimit = add(offeringLimit, min(configLimit, config.getLimit()));
    	}
    	return offeringLimit;
    }
    
    /**
     * Course availability excluding disabled sections and enrollments using disabled sections
     * @param requests course requests for this offering
     * @param courseId course in question
     * @return [number of enrollments, course limit]
     */
    public int[] getCourseAvailability(Collection<XCourseRequest> requests, XCourse course) {
    	int offeringLimit = 0;
    	Set<Long> hidden = new HashSet<Long>();
    	for (XConfig config: getConfigs()) {
    		Integer configLimit = null;
    		for (XSubpart subpart: config.getSubparts()) {
    			int subpartLimit = 0;
    			for (XSection section: subpart.getSections()) {
    				if (section.isEnabledForScheduling() && !section.isCancelled()) {
    					subpartLimit = add(subpartLimit, section.getLimit());
    				} else {
    					hidden.add(section.getSectionId());
    				}
    			}
    			if (configLimit == null)
    				configLimit = subpartLimit;
    			else
    				configLimit = min(configLimit, subpartLimit);
    		}
    		if (configLimit != null)
    			offeringLimit = add(offeringLimit, min(configLimit, config.getLimit()));
    	}
    	int enrl = 0;
    	int req = 0;
    	if (requests != null)
    		requests: for (XCourseRequest r: requests) {
				if (r.getEnrollment() != null && r.getEnrollment().getCourseId().equals(course.getCourseId())) {
					if (!hidden.isEmpty())
						for (Long s: r.getEnrollment().getSectionIds())
		    				if (hidden.contains(s)) continue requests;
					enrl ++;
				}
				if (!r.isAlternative() && r.getEnrollment() == null && r.getCourseIds().get(0).equals(course)) {
					req ++;
				}
    		}
    	return new int[] { enrl,  min(course.getLimit(), offeringLimit), req };
    }
    
    /** True if there are reservations for this offering */
    public boolean hasReservations() { return !iReservations.isEmpty(); }
    
    public boolean hasRestrictions() { return !iRestrictions.isEmpty(); }
    
    @Override
    public boolean equals(Object o) {
        if (o == null || !(o instanceof XOffering)) return false;
        return getOfferingId().equals(((XOffering)o).getOfferingId());
    }
    
    @Override
    public int hashCode() {
        return (int) (getOfferingId() ^ (getOfferingId() >>> 32));
    }

    public XReservationId guessReservation(Collection<XCourseRequest> other, XStudent student, XEnrollment enrollment) {
    	if (!enrollment.getOfferingId().equals(getOfferingId())) return null;
    	
    	Set<XReservation> reservations = new TreeSet<XReservation>();
    	boolean mustBeUsed = false;
    	for (XReservation reservation: getReservations()) {
    		if (reservation.isApplicable(student, enrollment)) {
    			if (reservation.equals(enrollment.getReservation()) && reservation.isIncluded(enrollment.getConfigId(), getSections(enrollment))) return reservation;
    			if (!mustBeUsed && reservation.mustBeUsed()) { reservations.clear(); mustBeUsed = true; }
    			if (mustBeUsed && !reservation.mustBeUsed()) continue;
    			reservations.add(reservation);
    		}
    	}
    	if (reservations.isEmpty()) return null;
    	
    	List<XSection> sections = getSections(enrollment);
    	for (XReservation reservation: reservations) {
    		if (reservation.isIncluded(enrollment.getConfigId(), sections)) {
    			if (reservation.getLimit(enrollment.getConfigId()) < 0.0 || other == null || mustBeUsed)
    				return new XReservationId(reservation.getType(), getOfferingId(), reservation.getReservationId());
    			int used = 0;
    			for (XCourseRequest r: other)
    				if (r.getEnrollment() != null && r.getEnrollment().getOfferingId().equals(getOfferingId()) && !enrollment.getStudentId().equals(r.getStudentId()) && reservation.equals(r.getEnrollment().getReservation())) used ++;
    			if (used < reservation.getLimit(enrollment.getConfigId()))
    				return new XReservationId(reservation.getType(), getOfferingId(), reservation.getReservationId());
    		}
    	}
    	return null;
    }
    
    public boolean hasIndividualReservation(XStudent student, XCourseId course) {
    	for (XReservation reservation: getReservations()) {
    		if (reservation instanceof XIndividualReservation && reservation.isApplicable(student, course) && reservation.mustBeUsed() && !reservation.isExpired())
    			return true;
    	}
    	return false;
    }
    
    public boolean hasGroupReservation(XStudent student, XCourseId course) {
    	for (XReservation reservation: getReservations()) {
    		if (reservation instanceof XGroupReservation && reservation.isApplicable(student, course) && reservation.mustBeUsed() && !reservation.isExpired())
    			return true;
    		if (reservation instanceof XLearningCommunityReservation && reservation.isApplicable(student, course) && reservation.mustBeUsed() && !reservation.isExpired())
    			return true;
    	}
    	return false;
    }
    
    public boolean hasLearningCommunityReservation(XStudent student, XCourseId course) {
    	for (XReservation reservation: getReservations()) {
    		if (reservation instanceof XLearningCommunityReservation && reservation.isApplicable(student, course) && reservation.mustBeUsed() && !reservation.isExpired())
    			return true;
    	}
    	return false;
    }
    
	public int distance(DistanceMetric m, Section s1, Section s2) {
        if (s1.getPlacement()==null || s2.getPlacement()==null) return 0;
        TimeLocation t1 = s1.getTime();
        TimeLocation t2 = s2.getTime();
        if (!t1.shareDays(t2) || !t1.shareWeeks(t2)) return 0;
        int a1 = t1.getStartSlot(), a2 = t2.getStartSlot();
        if (m.doComputeDistanceConflictsBetweenNonBTBClasses()) {
        	if (a1 + t1.getNrSlotsPerMeeting() <= a2) {
        		int dist = Placement.getDistanceInMinutes(m, s1.getPlacement(), s2.getPlacement());
        		if (dist > t1.getBreakTime() + Constants.SLOT_LENGTH_MIN * (a2 - a1 - t1.getLength()))
        			return dist;
        	}
        } else {
        	if (a1+t1.getNrSlotsPerMeeting()==a2)
        		return Placement.getDistanceInMinutes(m, s1.getPlacement(), s2.getPlacement());
        }
        return 0;
    }	
	
	public static class EnrollmentSectionComparator implements Comparator<Section> {
	    public boolean isParent(Section s1, Section s2) {
			Section p1 = s1.getParent();
			if (p1==null) return false;
			if (p1.equals(s2)) return true;
			return isParent(p1, s2);
		}

		public int compare(Section a, Section b) {
			if (isParent(a, b)) return 1;
	        if (isParent(b, a)) return -1;

	        int cmp = a.getSubpart().getInstructionalType().compareToIgnoreCase(b.getSubpart().getInstructionalType());
			if (cmp != 0) return cmp;
			
			return Double.compare(a.getId(), b.getId());
		}
	}
	
	public Course toCourse(Long courseId, XStudent student, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		Course course = toCourse(courseId, student, server.getExpectations(getOfferingId()), getDistributions(), server.getEnrollments(getOfferingId()), server.getAcademicSession().getDayOfWeekOffset());
		if (!(server instanceof StudentSolver) && student != null) {
			XSchedulingRule rule = server.getSchedulingRule(student,
					StudentSchedulingRule.Mode.Online,
					helper.hasAvisorPermission(),
					helper.hasAdminPermission());
			if (rule != null) {
				if (rule.isDisjunctive()) {
					if (rule.hasCourseName() && rule.matchesCourseName(course.getName())) {
						// no restriction needed
					} else if (rule.hasCourseType() && rule.matchesCourseType(course.getType())) {
						// no restriction needed
					} else if (rule.hasInstructionalMethod()) {
						List<Config> matchingConfigs = new ArrayList<Config>();
		        		for (Config config: course.getOffering().getConfigs()) {
		        			if (rule.matchesInstructionalMethod(config.getInstructionalMethodReference()))
		        				matchingConfigs.add(config);	
		        		}
		        		if (matchingConfigs.size() != course.getOffering().getConfigs().size()) {
		        			Restriction clonnedRestriction = new IndividualRestriction(-1l, course.getOffering(), student.getStudentId());
		        			for (Config c: matchingConfigs)
		        				clonnedRestriction.addConfig(c);
		        		}
					} else {
						new IndividualRestriction(-1l, course.getOffering(), student.getStudentId());
					}
				} else {
					if (!rule.matchesCourseName(course.getName()) || !rule.matchesCourseType(course.getType())) {
						new IndividualRestriction(-1l, course.getOffering(), student.getStudentId());
					} else if (rule.getInstructonalMethod() != null) {
						List<Config> matchingConfigs = new ArrayList<Config>();
		        		for (Config config: course.getOffering().getConfigs()) {
		        			if (rule.matchesInstructionalMethod(config.getInstructionalMethodReference()))
		        				matchingConfigs.add(config);	
		        		}
		        		if (matchingConfigs.size() != course.getOffering().getConfigs().size()) {
		        			Restriction clonnedRestriction = new IndividualRestriction(-1l, course.getOffering(), student.getStudentId());
		        			for (Config c: matchingConfigs)
		        				clonnedRestriction.addConfig(c);
		        		}
					}
				}
			} else {
				String filter = server.getConfig().getProperty("Load.OnlineOnlyStudentFilter", null);
				if (filter != null && !filter.isEmpty()) {
					if (new Query(filter).match(new StudentMatcher(student, server.getAcademicSession().getDefaultSectioningStatus(), server, false))) {
						String cn = server.getConfig().getProperty("Load.OnlineOnlyCourseNameRegExp");
						String im = server.getConfig().getProperty("Load.OnlineOnlyInstructionalModeRegExp");
						if (cn != null && !cn.isEmpty() && !course.getName().matches(cn)) {
							new IndividualRestriction(-1l, course.getOffering(), student.getStudentId());
						} else if (im != null) {
							List<Config> matchingConfigs = new ArrayList<Config>();
			        		for (Config config: course.getOffering().getConfigs()) {
			        			if (im.isEmpty()) {
			        				if (config.getInstructionalMethodReference() == null || config.getInstructionalMethodReference().isEmpty())
			        					matchingConfigs.add(config);	
			        			} else {
			        				if (config.getInstructionalMethodReference() != null && config.getInstructionalMethodReference().matches(im)) {
			        					matchingConfigs.add(config);
			        				}
			        			}
			        		}
			        		if (matchingConfigs.size() != course.getOffering().getConfigs().size()) {
			        			Restriction clonnedRestriction = new IndividualRestriction(-1l, course.getOffering(), student.getStudentId());
			        			for (Config c: matchingConfigs)
			        				clonnedRestriction.addConfig(c);
			        		}
						}
					} else if (server.getConfig().getPropertyBoolean("Load.OnlineOnlyExclusiveCourses", false)) {
						String cn = server.getConfig().getProperty("Load.OnlineOnlyCourseNameRegExp");
						String im = server.getConfig().getProperty("Load.ResidentialInstructionalModeRegExp");
						if (cn != null && !cn.isEmpty() && course.getName().matches(cn)) {
							new IndividualRestriction(-1l, course.getOffering(), student.getStudentId());
						} else if (im != null) {
							List<Config> matchingConfigs = new ArrayList<Config>();
			        		for (Config config: course.getOffering().getConfigs()) {
			        			if (im.isEmpty()) {
			        				if (config.getInstructionalMethodReference() == null || config.getInstructionalMethodReference().isEmpty())
			        					matchingConfigs.add(config);	
			        			} else {
			        				if (config.getInstructionalMethodReference() != null && config.getInstructionalMethodReference().matches(im)) {
			        					matchingConfigs.add(config);
			        				}
			        			}
			        		}
			        		if (matchingConfigs.size() != course.getOffering().getConfigs().size()) {
			        			Restriction clonnedRestriction = new IndividualRestriction(-1l, course.getOffering(), student.getStudentId());
			        			for (Config c: matchingConfigs)
			        				clonnedRestriction.addConfig(c);
			        		}
						}
					}
				}				
			}
		}
		return course;
	}

    public Course toCourse(Long courseId, XStudent student, XExpectations expectations, Collection<XDistribution> distributions, XEnrollments enrollments, int dayOfWeekOffset) {
		Offering clonedOffering = new Offering(getOfferingId(), getName());
		XCourse course = getCourse(courseId);
		int courseLimit = course.getLimit();
		if (courseLimit >= 0) {
			courseLimit -= enrollments.countEnrollmentsForCourse(course.getCourseId());
			if (courseLimit < 0) courseLimit = 0;
			for (XEnrollment enrollment: enrollments.getEnrollmentsForCourse(course.getCourseId())) {
				if (enrollment.getStudentId().equals(student.getStudentId())) { courseLimit++; break; }
			}
		}
		Course clonedCourse = new Course(course.getCourseId(), course.getSubjectArea(), course.getCourseNumber(), clonedOffering, courseLimit, course.getProjected());
		clonedCourse.setNote(course.getNote());
		clonedCourse.setType(course.getType());
		clonedCourse.setTitle(course.getTitle());
		Hashtable<Long, Config> configs = new Hashtable<Long, Config>();
		Hashtable<Long, Subpart> subparts = new Hashtable<Long, Subpart>();
		Hashtable<Long, Section> sections = new Hashtable<Long, Section>();
		for (XConfig config: getConfigs()) {
			int configLimit = config.getLimit();
			int configEnrl = enrollments.countEnrollmentsForConfig(config.getConfigId());
			boolean configStudent = false;
			for (XEnrollment enrollment: enrollments.getEnrollmentsForConfig(config.getConfigId()))
				if (enrollment.getStudentId().equals(student.getStudentId())) { configEnrl--; configStudent = true; break; }
			if (configLimit >= 0) {
				configLimit -= configEnrl;
				if (configLimit < 0) configLimit = 0;
				if (configStudent && configLimit == 0) configLimit = 1;
			}
			OnlineConfig clonedConfig = new OnlineConfig(config.getConfigId(), configLimit, config.getName(), clonedOffering);
			if (config.getInstructionalMethod() != null) {
				clonedConfig.setInstructionalMethodId(config.getInstructionalMethod().getUniqueId());
				clonedConfig.setInstructionalMethodName(config.getInstructionalMethod().getLabel());
				clonedConfig.setInstructionalMethodReference(config.getInstructionalMethod().getReference());
			}
			clonedConfig.setEnrollment(configEnrl);
			configs.put(config.getConfigId(), clonedConfig);
			for (XSubpart subpart: config.getSubparts()) {
				Subpart clonedSubpart = new Subpart(subpart.getSubpartId(), subpart.getInstructionalType(), subpart.getName(), clonedConfig,
						(subpart.getParentId() == null ? null: subparts.get(subpart.getParentId())));
				clonedSubpart.setAllowOverlap(subpart.isAllowOverlap());
				clonedSubpart.setCredit(subpart.getCredit(courseId));
				subparts.put(subpart.getSubpartId(), clonedSubpart);
				for (XSection section: subpart.getSections()) {
					int limit = section.getLimit();
					int enrl = enrollments.countEnrollmentsForSection(section.getSectionId());
					boolean std = false;
					for (XEnrollment enrollment: enrollments.getEnrollmentsForSection(section.getSectionId()))
						if (enrollment.getStudentId().equals(student.getStudentId())) { enrl--; std = true; break; }
					if (limit >= 0) {
						// limited section, deduct enrollments
						limit -= enrl;
						if (limit < 0) limit = 0; // over-enrolled, but not unlimited
						if (std && limit == 0) limit = 1; // allow enrolled student in
					}
					OnlineSection clonedSection = new OnlineSection(section.getSectionId(), limit,
							section.getName(course.getCourseId()), clonedSubpart, section.toPlacement(), section.toInstructors(),
							(section.getParentId() == null ? null : sections.get(section.getParentId())));
					clonedSection.setName(-1l, section.getName(-1l));
					clonedSection.setNote(section.getNote());
					clonedSection.setSpaceExpected(expectations == null ? 0.0 : expectations.getExpectedSpace(section.getSectionId()));
					clonedSection.setEnrollment(enrl);
					clonedSection.setCancelled(section.isCancelled());
					clonedSection.setEnabled(section.isEnabledForScheduling());
					clonedSection.setAlwaysEnabled(std);
					clonedSection.setOnline(section.isOnline());
					clonedSection.setPast(section.isPast());
					clonedSection.setDayOfWeekOffset(dayOfWeekOffset);
					if (distributions != null)
						for (XDistribution distribution: distributions)
							if (distribution.getDistributionType() == XDistributionType.IngoreConflicts && distribution.hasSection(section.getSectionId()))
								for (Long id: distribution.getSectionIds())
									if (!id.equals(section.getSectionId()))
										clonedSection.addIgnoreConflictWith(id);
			        if (limit > 0) {
			        	double available = Math.round(clonedSection.getSpaceExpected() - limit);
						clonedSection.setPenalty(available / section.getLimit());
			        }
					sections.put(section.getSectionId(), clonedSection);
				}
			}
		}
		for (XReservation reservation: getReservations()) {
			int reservationLimit = (int)Math.round(reservation.getLimit());
			if (reservationLimit >= 0) {
				reservationLimit -= enrollments.countEnrollmentsForReservation(reservation.getReservationId());
				if (reservationLimit < 0) reservationLimit = 0;
				for (XEnrollment enrollment: enrollments.getEnrollmentsForReservation(reservation.getReservationId())) {
					if (enrollment.getStudentId().equals(student.getStudentId())) { reservationLimit++; break; }
				}
				if (reservationLimit <= 0 && !(reservation.mustBeUsed() && !reservation.isExpired())) continue;
			}
			boolean applicable = reservation.isApplicable(student, course);
			if (reservation instanceof XCourseReservation)
				applicable = ((XCourseReservation)reservation).getCourseId().equals(courseId);
			if (reservation instanceof XDummyReservation) {
				// Ignore by reservation only flag (dummy reservation) when the student is already enrolled in the course
				for (XEnrollment enrollment: enrollments.getEnrollmentsForCourse(courseId))
					if (enrollment.getStudentId().equals(student.getStudentId())) { applicable = true; break; }
			}
			if (!applicable && reservation.isExpired()) continue;
			org.cpsolver.studentsct.reservation.Reservation clonedReservation = new OnlineReservation(reservation.getType().ordinal(),
					reservation.getReservationId(), clonedOffering,
					reservation.getPriority(), reservation.canAssignOverLimit(), reservationLimit, 
					applicable, reservation.mustBeUsed(), reservation.isAllowOverlap(), reservation.isExpired(), reservation.isOverride());
			clonedReservation.setAllowDisabled(reservation.isAllowDisabled());
			clonedReservation.setNeverIncluded(reservation.neverIncluded());
			clonedReservation.setBreakLinkedSections(reservation.canBreakLinkedSections());
			for (Long configId: reservation.getConfigsIds())
				clonedReservation.addConfig(configs.get(configId));
			for (Map.Entry<Long, Set<Long>> entry: reservation.getSections().entrySet()) {
				Set<Section> clonedSections = new HashSet<Section>();
				for (Long sectionId: entry.getValue())
					clonedSections.add(sections.get(sectionId));
				clonedReservation.getSections().put(subparts.get(entry.getKey()), clonedSections);
			}
		}
		for (XRestriction restriction: getRestrictions()) {
			if (restriction.isApplicable(student, course)) {
				IndividualRestriction clonnedRestriction = new IndividualRestriction(restriction.getRestrictionId(), clonedOffering, student.getStudentId());
				for (Long configId: restriction.getConfigsIds())
					clonnedRestriction.addConfig(configs.get(configId));
				for (Map.Entry<Long, Set<Long>> entry: restriction.getSections().entrySet()) {
					Set<Section> clonedSections = new HashSet<Section>();
					for (Long sectionId: entry.getValue())
						clonedSections.add(sections.get(sectionId));
					clonnedRestriction.getSections().put(subparts.get(entry.getKey()), clonedSections);
				}
			}
		}
		
		return clonedCourse;
    }
    
    public void addDistribution(XDistribution distribution) {
    	iDistrubutions.add(distribution);
    }
    
    public List<XDistribution> getDistributions() {
    	return iDistrubutions;
    }
    
    public boolean hasLinkedSections() {
    	if (iDistrubutions != null) {
    		for (XDistribution link: iDistrubutions)
    			if (link.getDistributionType() == XDistributionType.LinkedSections)
    				return true;
    	}
    	return false;
    }
    
    public boolean isAllowOverlap(XEnrollment enrollment) {
    	if (enrollment.getReservation() == null) return false;
    	for (XReservation reservation: getReservations()) {
    		if (reservation.equals(enrollment.getReservation()) && reservation.isAllowOverlap()) return true;
    	}
    	return false;
    }
    
    public boolean isAllowOverlap(XStudent student, Long configId, XCourseId course, List<XSection> assignment) {
    	for (XReservation reservation: getReservations()) {
    		if (reservation.isAllowOverlap() && reservation.isApplicable(student, course) && reservation.isIncluded(configId, assignment))
    			return true;
    	}
    	return false;
    }
    
    public Set<String> getInstructorExternalIds() {
    	Set<String> ret = new HashSet<String>();
    	for (XConfig config: getConfigs())
    		for (XSubpart subpart: config.getSubparts())
    			for (XSection section: subpart.getSections())
    				for (XInstructor instructor: section.getAllInstructors())
    					if (instructor.hasExternalId())
    						ret.add(instructor.getExternalId());
    	return ret;
    }
    
    public void fillInUnavailabilities(Student student) {
    	if (student.getExternalId() == null || student.getExternalId().isEmpty()) return;
    	Set<Long> sections = new HashSet<Long>();
    	for (XConfig config: getConfigs())
			for (XSubpart subpart: config.getSubparts())
				for (XSection section: subpart.getSections()) {
					if (section.getTime() != null && !section.isCancelled())
						for (XInstructor instructor: section.getAllInstructors())
							if (student.getExternalId().equals(instructor.getExternalId()) && sections.add(section.getSectionId())) {
								Unavailability ua = new Unavailability(student,
										new Section(section.getSectionId(), section.getLimit(), getName() + " " + subpart.getName() + " " + section.getName(), null, section.toPlacement(), null),
										instructor.isAllowOverlap());
								ua.setTeachingAssignment(true);
							}
				}
    }
    
	private boolean check(XStudent student, XCourseRequest cr, XConfig config, XCourseId courseId, AcademicSessionInfo session, HashSet<XSection> sections, int idx) {
		if (config.getSubparts().size() == idx) {
			if (isNotAllowed(student, courseId, config, sections)) return false;
            return true;
		} else {
			XSubpart subpart = config.getSubparts().get(idx);
			for (XSection section : subpart.getSections()) {
                if (section.isCancelled()) continue;
                if (!cr.isRequired(this, config, section, courseId)) continue;
                if (section.getParentId() != null) {
                	XSection parent = null;
                	for (XSection p: sections)
                		if (section.getParentId().equals(p.getSectionId())) { parent = p; break; }
                	if (parent == null) continue;
                }
                if (section.isOverlapping(getDistributions(), sections)) continue;
                if (!student.isAllowDisabled() && !section.isEnabled(student, session)) continue;
                sections.add(section);
                if (check(student, cr, config, courseId, session, sections, idx + 1))
                	return true;
                sections.remove(section);
			}
		}
		return false;
	}
	
	private boolean isNotAllowed(XStudent student, XCourseId courseId, XConfig config, Collection<XSection> sections) {
		for (XRestriction r: iRestrictions) {
			if (r.isApplicable(student, courseId) && !r.isIncluded(config.getConfigId(), sections))
				return true;
		}
		return false;
	}
	
	public boolean hasInconsistentRequirements(XStudent student, XCourseRequest cr, XCourseId courseId, AcademicSessionInfo session) {
		if (!student.hasRequirements(courseId)) return false;
		for (XConfig config: iConfigs) {
			if (check(student, cr, config, courseId, session, new HashSet<XSection>(), 0)) return false;
		}
		return true;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		iUniqueId = in.readLong();
		iName = (String)in.readObject();
		iWaitList = in.readBoolean();
		iReSchedule = in.readBoolean();
		
		int nrConfigs = in.readInt();
		iConfigs.clear();
		for (int i = 0; i < nrConfigs; i++)
			iConfigs.add(new XConfig(in));
		
		int nrCourses = in.readInt();
		for (int i = 0; i < nrCourses; i++)
			iCourses.add(new XCourse(in));
		
		int nrReservations = in.readInt();
		for (int i = 0; i < nrReservations; i++) {
			switch (XReservationType.values()[in.readInt()]) {
			case Course:
				iReservations.add(new XCourseReservation(in));
				break;
			case Curriculum:
			case CurriculumOverride:
				iReservations.add(new XCurriculumReservation(in));
				break;
			case Dummy:
				iReservations.add(new XDummyReservation(in));
				break;
			case Group:
			case GroupOverride:
				iReservations.add(new XGroupReservation(in));
				break;
			case Individual:
			case IndividualOverride:
			case IndividualGroup:
				iReservations.add(new XIndividualReservation(in));
				break;
			case LearningCommunity:
				iReservations.add(new XLearningCommunityReservation(in));
				break;
			case Universal:
				iReservations.add(new XUniversalReservation(in));
				break;
			}
		}
		
		int nrRestrictions = in.readInt();
		for (int i = 0; i < nrRestrictions; i++) {
			switch (XReservationType.values()[in.readInt()]) {
			case Course:
				iRestrictions.add(new XCourseRestriction(in));
				break;
			case Curriculum:
				iRestrictions.add(new XCurriculumRestriction(in));
				break;
			case Individual:
				iRestrictions.add(new XIndividualRestriction(in));
				break;
			}
		}
		
		int nrDistributions = in.readInt();
		for (int i = 0; i < nrDistributions; i++)
			iDistrubutions.add(new XDistribution(in));
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(iUniqueId);
		out.writeObject(iName);
		out.writeBoolean(iWaitList);
		out.writeBoolean(iReSchedule);
		
		out.writeInt(iConfigs.size());
		for (XConfig config: iConfigs)
			config.writeExternal(out);
		
		out.writeInt(iCourses.size());
		for (XCourse course: iCourses)
			course.writeExternal(out);
		
		out.writeInt(iReservations.size());
		for (XReservation reservation: iReservations) {
			if (reservation.getType() == XReservationType.Group && reservation instanceof XIndividualReservation)
				out.writeInt(XReservationType.IndividualGroup.ordinal());
			else
				out.writeInt(reservation.getType().ordinal());
			reservation.writeExternal(out);
		}
		
		out.writeInt(iRestrictions.size());
		for (XRestriction restriction: iRestrictions) {
			out.writeInt(restriction.getType().ordinal());
			restriction.writeExternal(out);
		}
		
		out.writeInt(iDistrubutions.size());
		for (XDistribution distribution: iDistrubutions)
			distribution.writeExternal(out);
	}
}
