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
package org.unitime.timetable.model;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.unitime.timetable.model.base.BaseReservation;

/**
 * @author Tomas Muller
 */
public abstract class Reservation extends BaseReservation implements Comparable<Reservation> {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Reservation () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Reservation (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public boolean isExpired() {
		if (getStartDate() == null && getExpirationDate() == null) return false;
		Calendar c = Calendar.getInstance(Locale.US);
		c.set(Calendar.HOUR_OF_DAY, 0);
		c.set(Calendar.MINUTE, 0);
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		return ((getStartDate() != null && c.getTime().before(getStartDate())) ||
				(getExpirationDate() != null && getExpirationDate().before(c.getTime())));
	}
	
	public abstract boolean isApplicable(Student student, CourseRequest request);
	
	public int getReservationLimit() {
		Integer cap = getLimitCap();
		if (cap != null)
			return min(cap, getLimit() == null ? -1 : getLimit().intValue());
		return (getLimit() == null ? -1 : getLimit().intValue());
	}
	
	private boolean hasClass(Class_ clazz) {
		for (Class_ other: getClasses()) {
			if (clazz.equals(other) || other.isParentOf(clazz) || clazz.isParentOf(other)) return true;
		}
		return false;
	}
	
	public boolean isMatching(List<StudentClassEnrollment> enrollment) {
		if (enrollment.isEmpty()) return false;
		if (!getConfigurations().isEmpty()) {
			for (StudentClassEnrollment e: enrollment) {
				if (!getConfigurations().contains(e.getClazz().getSchedulingSubpart().getInstrOfferingConfig()))
					return false;
			}
		}
		if (!getClasses().isEmpty()) {
			for (StudentClassEnrollment e: enrollment) {
				if (!hasClass(e.getClazz())) return false;
			}
		}
		return true;
	}
	
	public boolean isMatching(Class_ clazz) {
		if (!getConfigurations().isEmpty() && !getConfigurations().contains(clazz.getSchedulingSubpart().getInstrOfferingConfig()))
			return false;
		if (!getClasses().isEmpty() && !hasClass(clazz))
			return false;
		return true;
	}
	
	public abstract int getPriority();
	public abstract boolean isCanAssignOverLimit();
	public abstract boolean isMustBeUsed();
	public abstract boolean isAllowOverlap();
	public boolean isAlwaysExpired() { return false; }
	
	@Override
    public int compareTo(Reservation r) {
        if (getPriority() != r.getPriority()) {
            return (getPriority() < r.getPriority() ? -1 : 1);
        }
        int cmp = Double.compare(getRestrictivity(), r.getRestrictivity());
        if (cmp != 0) return cmp;
        return getUniqueId().compareTo(r.getUniqueId());
    }
	
	public double getRestrictivity() {
		if (getConfigurations().isEmpty()) return 1.0;
		
		double restrictivity = ((double)getConfigurations().size()) / getInstructionalOffering().getInstrOfferingConfigs().size();
		if (getClasses().isEmpty()) return restrictivity;

		Map<SchedulingSubpart, Integer> counts = new HashMap<SchedulingSubpart, Integer>();
		for (Class_ clazz: getClasses()) {
			Integer old = counts.get(clazz.getSchedulingSubpart());
			counts.put(clazz.getSchedulingSubpart(), 1 + (old == null ? 0 : old.intValue()));
		}
		for (Map.Entry<SchedulingSubpart, Integer> entry: counts.entrySet()) {
			restrictivity *= ((double)entry.getValue().intValue()) / entry.getKey().getClasses().size(); 
		}
		return restrictivity;
    }
    
    protected Map<Long, Set<Long>> getSections() {
    	Map<Long, Set<Long>> ret = new HashMap<Long, Set<Long>>();
    	for (Class_ clazz: getClasses()) {
    		while (clazz != null) {
                Set<Long> sections = ret.get(clazz.getSchedulingSubpart().getUniqueId());
                if (sections == null) {
                    sections = new HashSet<Long>();
                    ret.put(clazz.getSchedulingSubpart().getUniqueId(), sections);
                }
                sections.add(clazz.getUniqueId());
                clazz = clazz.getParentClass();
            }
    	}
    	return ret;
    }
    
    public int getReservedAvailableSpace() {
        // Unlimited
        if (getReservationLimit() < 0) return Integer.MAX_VALUE;
        
        return getReservationLimit() - countEnrollmentsForReservation();
    }
    
    private int countEnrollmentsForReservation() {
    	Set<Long> checked = new HashSet<Long>();
    	Set<Long> students = new HashSet<Long>();
    	for (InstrOfferingConfig config: getInstructionalOffering().getInstrOfferingConfigs())
    		for (SchedulingSubpart subpart: config.getSchedulingSubparts())
    			for (Class_ clazz: subpart.getClasses())
    				for (StudentClassEnrollment e: clazz.getStudentEnrollments())
    					if (e.getCourseRequest() != null && checked.add(e.getCourseRequest().getUniqueId()) && isApplicable(e.getStudent(), e.getCourseRequest()) && isMatching(e.getCourseRequest().getClassEnrollments())) {
    						students.add(e.getStudent().getUniqueId());
    					}
    	return students.size();
    }
    
    protected Set<InstrOfferingConfig> getAllConfigurations() {
    	Set<InstrOfferingConfig> configs = new HashSet<InstrOfferingConfig>();
    	if (getConfigurations() != null)
    		configs.addAll(getConfigurations());
    	if (getClasses() != null)
    		for (Class_ clazz: getClasses())
    			configs.add(clazz.getSchedulingSubpart().getInstrOfferingConfig());
    	return configs;
    }
    
    protected Map<SchedulingSubpart, Set<Class_>> getAllSections() {
    	Map<SchedulingSubpart, Set<Class_>> ret = new HashMap<SchedulingSubpart, Set<Class_>>();
    	for (Class_ clazz: getClasses()) {
    		while (clazz != null) {
                Set<Class_> sections = ret.get(clazz.getSchedulingSubpart());
                if (sections == null) {
                    sections = new HashSet<Class_>();
                    ret.put(clazz.getSchedulingSubpart(), sections);
                }
                sections.add(clazz);
                clazz = clazz.getParentClass();
            }
    	}
    	return ret;
    }
    
    public Integer getLimitCap() {
    	Set<InstrOfferingConfig> configs = getAllConfigurations();
    	if (configs.isEmpty()) return null;
    	
    	// config cap
    	int cap = 0;
    	for (InstrOfferingConfig config: configs)
    		cap = add(cap, config.isUnlimitedEnrollment() ? -1 : config.getLimit());
    	
    	for (Set<Class_> sections: getAllSections().values()) {
            // subpart cap
            int subpartCap = 0;
            for (Class_ section: sections)
                subpartCap = add(subpartCap, section.getClassLimit());
            
            // minimize
            cap = min(cap, subpartCap);
        }
        
        return (cap < 0 ? null : new Integer(cap));
    }

    private static int min(int l1, int l2) {
        return (l1 < 0 ? l2 : l2 < 0 ? l1 : Math.min(l1, l2));
    }

    private static int add(int l1, int l2) {
        return (l1 < 0 ? -1 : l2 < 0 ? -1 : l1 + l2);
    }
}
