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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.cpsolver.studentsct.model.Config;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.model.Subpart;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.Reservation;

/**
 * @author Tomas Muller
 */
public abstract class XReservation extends XReservationId implements Comparable<XReservation> {
	private static final long serialVersionUID = 1L;
	
    private Date iExpirationDate, iStartDate;
    private Set<Long> iConfigs = new HashSet<Long>();
    private Map<Long, Set<Long>> iSections = new HashMap<Long, Set<Long>>();
    private Set<Long> iIds = new HashSet<Long>();
    private int iLimitCap = -1;
    private double iRestrictivity = 1.0;
    
    private int iPriority = 1000;
    private int iFlags = 0;
    
    public static enum Flags {
    	MustBeUsed,
    	CanAssignOverLimit,
    	AllowOverlap,
    	AllowDiabled,
    	AlwaysExpired,
    	;
    	public int flag() { return 1 << ordinal(); }
		public boolean in(int flags) {
			return (flags & flag()) != 0;
		}
		public int set(int flags, boolean value) {
			if (value)
				return flags | flag();
			else
				return flags & ~flag();
		}
    }
    
    public XReservation() {
    	super();
    }
    
    public XReservation(XReservationType type, XOffering offering, Reservation reservation) {
    	super(type, offering.getOfferingId(), (reservation == null ? -1l : reservation.getUniqueId()));
    	if (reservation != null) {
        	iExpirationDate = reservation.getExpirationDate();
        	iStartDate = reservation.getStartDate();
        	for (InstrOfferingConfig config: reservation.getConfigurations()) {
        		iConfigs.add(config.getUniqueId());
        		iIds.add(-config.getUniqueId());
        	}
        	for (Class_ clazz: reservation.getClasses()) {
        		iIds.add(clazz.getUniqueId());
        		iConfigs.add(clazz.getSchedulingSubpart().getInstrOfferingConfig().getUniqueId());
        		while (clazz != null) {
                    Set<Long> sections = iSections.get(clazz.getSchedulingSubpart().getUniqueId());
                    if (sections == null) {
                        sections = new HashSet<Long>();
                        iSections.put(clazz.getSchedulingSubpart().getUniqueId(), sections);
                    }
                    sections.add(clazz.getUniqueId());
                    clazz = clazz.getParentClass();
                }
        	}
    	}
        switch (type) {
        case Individual:
        	setPriority(ApplicationProperty.ReservationPriorityIndividual.intValue());
        	setCanAssignOverLimit(ApplicationProperty.ReservationCanOverLimitIndividual.isTrue());
        	setMustBeUsed(ApplicationProperty.ReservationMustBeUsedIndividual.isTrue());
        	setAllowOverlap(ApplicationProperty.ReservationAllowOverlapIndividual.isTrue());
        	break;
        case Group:
        	setPriority(ApplicationProperty.ReservationPriorityGroup.intValue());
        	setCanAssignOverLimit(ApplicationProperty.ReservationCanOverLimitGroup.isTrue());
        	setMustBeUsed(ApplicationProperty.ReservationMustBeUsedGroup.isTrue());
        	setAllowOverlap(ApplicationProperty.ReservationAllowOverlapGroup.isTrue());
        	break;
        case LearningCommunity:
        	setPriority(ApplicationProperty.ReservationPriorityLearningCommunity.intValue());
        	setCanAssignOverLimit(ApplicationProperty.ReservationCanOverLimitLearningCommunity.isTrue());
        	setMustBeUsed(ApplicationProperty.ReservationMustBeUsedLearningCommunity.isTrue());
        	setAllowOverlap(ApplicationProperty.ReservationAllowOverlapLearningCommunity.isTrue());
        	break;
        case Curriculum:
        	setPriority(ApplicationProperty.ReservationPriorityCurriculum.intValue());
        	setCanAssignOverLimit(ApplicationProperty.ReservationCanOverLimitCurriculum.isTrue());
        	setMustBeUsed(ApplicationProperty.ReservationMustBeUsedCurriculum.isTrue());
        	setAllowOverlap(ApplicationProperty.ReservationAllowOverlapCurriculum.isTrue());
        	break;
        case Course:
        	setPriority(ApplicationProperty.ReservationPriorityCourse.intValue());
        	setCanAssignOverLimit(ApplicationProperty.ReservationCanOverLimitCourse.isTrue());
        	setMustBeUsed(ApplicationProperty.ReservationMustBeUsedCourse.isTrue());
        	setAllowOverlap(ApplicationProperty.ReservationAllowOverlapCourse.isTrue());
        	break;
        case IndividualOverride:
        case GroupOverride:
        	setPriority(ApplicationProperty.ReservationPriorityOverride.intValue());
        	break;
        case Dummy:
        	setPriority(ApplicationProperty.ReservationPriorityDummy.intValue());
        	break;
        }
        if (!iConfigs.isEmpty() && !canAssignOverLimit()) {
        	// config cap
        	int cap = 0;
        	for (XConfig config: offering.getConfigs()) {
        		if (iConfigs.contains(config.getConfigId()))
        			cap = add(cap, config.getLimit());
        	}
        	for (XConfig config: offering.getConfigs()) {
        		for (XSubpart subpart: config.getSubparts()) {
        			Set<Long> sections = iSections.get(subpart.getSubpartId());
        			if (sections == null) continue;
        			// subpart cap
        			int subpartCap = 0;
        			for (XSection section: subpart.getSections())
        				if (sections.contains(section.getSectionId()))
        					subpartCap = add(subpartCap, section.getLimit());
            		// minimize
            		cap = min(cap, subpartCap);
        		}
        	}
        	iLimitCap = cap;
        }
        iRestrictivity = computeRestrictivity(offering);
    }
    
    public XReservation(XReservationType type, org.cpsolver.studentsct.reservation.Reservation reservation) {
    	super(type, reservation.getOffering().getId(), reservation.getId());
    	iLimitCap = (int)Math.round(reservation.getLimitCap());
    	iRestrictivity = reservation.getRestrictivity();
    	iExpirationDate = (reservation.isExpired() ? new Date(0) : null);
    	iStartDate = null;
    	for (Config config: reservation.getConfigs()) {
    		iConfigs.add(config.getId());
    		iIds.add(-config.getId());
    	}
    	for (Map.Entry<Subpart, Set<Section>> entry: reservation.getSections().entrySet()) {
    		Set<Long> sections = new HashSet<Long>();
    		for (Section section: entry.getValue()) {
    			sections.add(section.getId());
    			iIds.add(section.getId());
    		}
    		iSections.put(entry.getKey().getId(), sections);
    	}
    	for (Config config: reservation.getOffering().getConfigs()) {
    		for (Subpart subpart: config.getSubparts()) {
    			for (Section section: subpart.getSections()) {
    				if (iIds.contains(section.getId())) {
    					iIds.remove(-config.getId());
    					Section parent = section.getParent();
    					while (parent != null) {
    						iIds.remove(parent.getId());
    						parent = parent.getParent();
    					}
    				}
    			}
    		}
    	}
    	setPriority(reservation.getPriority());
    	setMustBeUsed(reservation.mustBeUsed());
    	setCanAssignOverLimit(reservation.canAssignOverLimit());
    	setAllowOverlap(reservation.isAllowOverlap());
    	setAllowDisabled(reservation.isAllowDisabled());
    }
    
    /**
     * Reservation limit
     */
    public abstract int getReservationLimit();
    
    
    /** Reservation priority (e.g., individual reservations first) */
    public int getPriority() {
    	return iPriority;
    }
    
    public void setPriority(int priority) {
    	iPriority = priority;
    }
    
    /**
     * Returns true if the student is applicable for the reservation
     * @param student a student 
     * @return true if student can use the reservation to get into the course / configuration / section
     */
    public abstract boolean isApplicable(XStudent student, XCourseId course);

    /**
     * One or more configurations on which the reservation is set (optional).
     */
    public Set<Long> getConfigsIds() { return iConfigs; }
    
    public boolean hasConfigRestriction(Long configId) { return iIds.contains(-configId); }
    public boolean hasSectionRestriction(Long sectionId) { return iIds.contains(sectionId); }
        
    /**
     * One or more sections on which the reservation is set (optional).
     */
    public Map<Long, Set<Long>> getSections() { return iSections; }
    
    /**
     * One or more sections on which the reservation is set (optional).
     */
    public Set<Long> getSectionIds(Long subpartId) {
        return iSections.get(subpartId);
    }
    
    /**
     * True if can go over the course / config / section limit. Only to be used in the online sectioning. 
      */
    public boolean canAssignOverLimit() { return Flags.CanAssignOverLimit.in(iFlags); }
    
    public void setCanAssignOverLimit(boolean canAssignOverLimit) {
    	iFlags = Flags.CanAssignOverLimit.set(iFlags, canAssignOverLimit);
    	if (canAssignOverLimit) iLimitCap = -1;
    }
    
    /**
     * If true, student must use the reservation (if applicable)
     */
    public boolean mustBeUsed() { return Flags.MustBeUsed.in(iFlags); }
    
    public void setMustBeUsed(boolean mustBeUsed) { iFlags = Flags.MustBeUsed.set(iFlags, mustBeUsed); }
    
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
     * Reservation limit capped the limit cap (see {@link Reservation#getLimitCap()})
     */
    public int getLimit() {
        return min(iLimitCap, getReservationLimit());
    }
    
    /**
     * True if holding this reservation allows a student to have attend overlapping class. 
     */
    public boolean isAllowOverlap() { return Flags.AllowOverlap.in(iFlags); }
    
    public void setAllowOverlap(boolean allowOverlap) { iFlags = Flags.AllowOverlap.set(iFlags, allowOverlap); }
    
    /**
     * True if holding this reservation allows a student to have attend a class that is disabled for student scheduling. 
     */
    public boolean isAllowDisabled() { return Flags.AllowDiabled.in(iFlags); }
    
    public void setAllowDisabled(boolean allowDisabled) { iFlags = Flags.AllowDiabled.set(iFlags, allowDisabled); }

    
    /**
     * True if the reservation is expired. If a reservation is expired, it works as ordinary reservation
     * (especially the flags mutBeUsed and isAllowOverlap), except it does not block other students
     * of getting into the offering / config / section.
     */
    public boolean isExpired() {
    	if (iExpirationDate == null && iStartDate == null) return false;
		Calendar c = Calendar.getInstance(Locale.US);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return ((iStartDate != null && c.getTime().before(iStartDate)) ||
				(iExpirationDate != null && iExpirationDate.before(c.getTime())));
    }
    
    /**
     * Return true if the given enrollment meets the reservation.
     */
    public boolean isIncluded(Long configId, List<XSection> sections) {
        // If there are configurations, check the configuration
        if (!iConfigs.isEmpty() && !iConfigs.contains(configId)) return false;
        
        // Check all the sections of the enrollment
        for (XSection section: sections) {
            Set<Long> reserved = iSections.get(section.getSubpartId());
            if (reserved != null && !reserved.contains(section.getSectionId()))
                return false;
        }
        
        return true;
    }
    
    public boolean isIncluded(XOffering offering, Long configId, XSection section) {
    	if (!iConfigs.isEmpty() && !iConfigs.contains(configId)) return false;
    	
    	XSection s = section;
    	while (s != null) {
    		Set<Long> reserved = iSections.get(s.getSubpartId());
    		if (reserved != null && !reserved.contains(s.getSectionId())) return false;
    		s = (s.getParentId() == null ? null : offering.getSection(s.getParentId()));
    	}
    	
    	return true;
    }
    
    /**
     * Reservation restrictivity (estimated percentage of enrollments that include this reservation, 1.0 reservation on the whole offering)
     */
    public double getRestrictivity() {
    	return iRestrictivity;
    }
    
    private double computeRestrictivity(XOffering offering) {
        if (iConfigs.isEmpty()) return 1.0;
        int nrChoices = 0, nrMatchingChoices = 0;
        for (XConfig config: offering.getConfigs()) {
            int x[] = nrChoices(config, 0, new HashSet<XSection>(), iConfigs.contains(config.getConfigId()));
            nrChoices += x[0];
            nrMatchingChoices += x[1];
        }
        return ((double)nrMatchingChoices) / nrChoices;
    }
    
    
    /** Number of choices and number of chaing choices in the given sub enrollment */
    private int[] nrChoices(XConfig config, int idx, HashSet<XSection> sections, boolean matching) {
        if (config.getSubparts().size() == idx) {
            return new int[]{1, matching ? 1 : 0};
        } else {
            XSubpart subpart = config.getSubparts().get(idx);
            Set<Long> matchingSections = iSections.get(subpart.getSubpartId());
            int choicesThisSubpart = 0;
            int matchingChoicesThisSubpart = 0;
            for (XSection section : subpart.getSections()) {
                if (section.getParentId() != null) {
                	XSection parent = null;
                	for (XSection s: sections)
                		if (s.getSectionId().equals(section.getParentId())) { parent = s; break; }
                	if (parent == null) continue;
                }
                if (section.isOverlapping(null, sections)) continue;
                sections.add(section);
                boolean m = matching && (matchingSections == null || matchingSections.contains(section.getSectionId()));
                int[] x = nrChoices(config, 1 + idx, sections, m);
                choicesThisSubpart += x[0];
                matchingChoicesThisSubpart += x[1];
                sections.remove(section);
            }
            return new int[] {choicesThisSubpart, matchingChoicesThisSubpart};
        }
    }
    
    /**
     * Priority first, than restrictivity (more restrictive first), than id 
     */
    @Override
    public int compareTo(XReservation r) {
        if (getPriority() != r.getPriority()) {
            return (getPriority() < r.getPriority() ? -1 : 1);
        }
        int cmp = Double.compare(getRestrictivity(), r.getRestrictivity());
        if (cmp != 0) return cmp;
        return getReservationId().compareTo(r.getReservationId());
    }
    
    /**
     * Available reserved space
     * @param excludeRequest excluding given request (if not null)
     **/
    public int getReservedAvailableSpace(XEnrollments enrollments) {
        // Unlimited
        if (getLimit() < 0) return Integer.MAX_VALUE;
        
        return getLimit() - enrollments.countEnrollmentsForReservation(getReservationId());
    }
    
    @Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	super.readExternal(in);
    	iExpirationDate = (in.readBoolean() ? new Date(in.readLong()) : null);
    	
    	int nrConfigs = in.readInt();
    	iConfigs.clear();
    	for (int i = 0; i < nrConfigs; i++)
    		iConfigs.add(in.readLong());
    	
    	int nrSubparts = in.readInt();
    	iSections.clear(); 
    	for (int i = 0; i < nrSubparts; i++) {
    		Set<Long> sections = new HashSet<Long>();
    		iSections.put(in.readLong(), sections);
    		int nrSection = in.readInt();
    		for (int j = 0; j < nrSection; j++) {
    			sections.add(in.readLong());
    		}
    	}
    	iLimitCap = in.readInt();
    	iRestrictivity = in.readDouble();
    	iPriority = in.readInt();
    	iFlags = in.readInt();
    	iIds.clear();
    	int nrIds = in.readInt();
    	for (int i = 0; i < nrIds; i++)
    		iIds.add(in.readLong());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeBoolean(iExpirationDate != null);
		if (iExpirationDate != null)
			out.writeLong(iExpirationDate.getTime());
		
		out.writeInt(iConfigs.size());
		for (Long config: iConfigs)
			out.writeLong(config);
		
		Set<Map.Entry<Long, Set<Long>>> entries = iSections.entrySet();
		out.writeInt(entries.size());
		for (Map.Entry<Long, Set<Long>> entry: entries) {
			out.writeLong(entry.getKey());
			out.writeInt(entry.getValue().size());
			for (Long id: entry.getValue())
				out.writeLong(id);
		}
		
		out.writeInt(iLimitCap);
		out.writeDouble(iRestrictivity);
		out.writeInt(iPriority);
		out.writeInt(iFlags);
		
		out.writeInt(iIds.size());
		for (Long id: iIds)
			out.writeLong(id);
	}
}