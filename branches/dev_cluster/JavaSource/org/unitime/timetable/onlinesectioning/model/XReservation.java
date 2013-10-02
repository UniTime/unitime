/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.model;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.sf.cpsolver.studentsct.model.Config;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Subpart;

import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.Reservation;

public abstract class XReservation extends XReservationId implements Comparable<XReservation> {
	private static final long serialVersionUID = 1L;
	
    private Date iExpirationDate;
    private Long iOfferingId;
    private Set<Long> iConfigs = new HashSet<Long>();
    private Map<Long, Set<Long>> iSections = new HashMap<Long, Set<Long>>();
    private int iLimitCap = -1;
    private double iRestrictivity = 1.0;
    
    public XReservation() {
    	super();
    }
    
    public XReservation(XReservationType type, XOffering offering, Reservation reservation) {
    	super(type, offering.getOfferingId(), (reservation == null ? -1l : reservation.getUniqueId()));
    	if (reservation != null) {
            iOfferingId = reservation.getInstructionalOffering().getUniqueId();
        	iExpirationDate = reservation.getExpirationDate();
        	for (InstrOfferingConfig config: reservation.getConfigurations())
        		iConfigs.add(config.getUniqueId());
        	for (Class_ clazz: reservation.getClasses()) {
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
    	} else {
    		iOfferingId = offering.getOfferingId();
    	}
        if (!iConfigs.isEmpty()) {
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
    
    public XReservation(XReservationType type, net.sf.cpsolver.studentsct.reservation.Reservation reservation) {
    	iOfferingId = reservation.getOffering().getId();
    	iLimitCap = (int)Math.round(reservation.getLimitCap());
    	iRestrictivity = reservation.getRestrictivity();
    	iExpirationDate = (reservation.isExpired() ? new Date(0) : null);
    	for (Config config: reservation.getConfigs())
    		iConfigs.add(config.getId());
    	for (Map.Entry<Subpart, Set<Section>> entry: reservation.getSections().entrySet()) {
    		Set<Long> sections = new HashSet<Long>();
    		for (Section section: entry.getValue())
    			sections.add(section.getId());
    		iSections.put(entry.getKey().getId(), sections);
    	}
    }
    
    /**
     * Reservation limit
     */
    public abstract int getReservationLimit();
    
    
    /** Reservation priority (e.g., individual reservations first) */
    public abstract int getPriority();
    
    /**
     * Returns true if the student is applicable for the reservation
     * @param student a student 
     * @return true if student can use the reservation to get into the course / configuration / section
     */
    public abstract boolean isApplicable(XStudent student);

    /**
     * Instructional offering on which the reservation is set.
     */
    public Long getOfferingId() { return iOfferingId; }
    
    /**
     * One or more configurations on which the reservation is set (optional).
     */
    public Set<Long> getConfigsIds() { return iConfigs; }
        
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
    public abstract boolean canAssignOverLimit();
    
    /**
     * If true, student must use the reservation (if applicable)
     */
    public abstract boolean mustBeUsed();
    
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
    public boolean isAllowOverlap() {
        return false;
    }
    
    /**
     * True if the reservation is expired. If a reservation is expired, it works as ordinary reservation
     * (especially the flags mutBeUsed and isAllowOverlap), except it does not block other students
     * of getting into the offering / config / section.
     */
    public boolean isExpired() {
    	if (iExpirationDate == null) return false;
		Calendar c = Calendar.getInstance(Locale.US);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return iExpirationDate.before(c.getTime());
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
}