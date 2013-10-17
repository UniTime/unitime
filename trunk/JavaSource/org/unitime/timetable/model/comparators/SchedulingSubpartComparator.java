/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model.comparators;

import java.util.Comparator;

import org.unitime.timetable.model.SchedulingSubpart;


/**
 * Compares scheduling subparts by itype
 * 
 * @author Heston Fernandes, Tomas Muller
 */
public class SchedulingSubpartComparator implements Comparator {
	private Long subjectUID;
	
    public SchedulingSubpartComparator(Long subjectUID) {
        this.subjectUID = subjectUID;
    }
    
    public SchedulingSubpartComparator() {
    	this(null);
    }

    public boolean isParent(SchedulingSubpart s1, SchedulingSubpart s2) {
		SchedulingSubpart p1 = s1.getParentSubpart();
		if (p1==null) return false;
		if (p1.equals(s2)) return true;
		return isParent(p1, s2);
	}

    public int compare(Object o1, Object o2) {
        SchedulingSubpart s1 = (SchedulingSubpart) o1;
        SchedulingSubpart s2 = (SchedulingSubpart) o2;
        
        if (!s1.getInstrOfferingConfig().equals(s2.getInstrOfferingConfig())) {
        	Comparator cmp = new InstrOfferingConfigComparator(subjectUID);
        	return cmp.compare(s1.getInstrOfferingConfig(), s2.getInstrOfferingConfig());
        }
        
        if (isParent(s1,s2)) return 1;
        if (isParent(s2,s1)) return -1;
        
        if (s1.getParentSubpart() != null || s2.getParentSubpart() != null) {
        	SchedulingSubpart p1 = s1; int d1 = 0;
        	while (p1.getParentSubpart() != null) { p1 = p1.getParentSubpart(); d1 ++; }
        	
        	SchedulingSubpart p2 = s2; int d2 = 0;
        	while (p2.getParentSubpart() != null) { p2 = p2.getParentSubpart(); d2 ++; }
        	
        	if (d1 < d2) {
        		int cmp = compare(s1, s2.getParentSubpart());
        		if (cmp != 0) return cmp;
        	} else if (d1 > d2) {
        		int cmp = compare(s1.getParentSubpart(), s2);
        		if (cmp != 0) return cmp;
        	} else {
        		int cmp = compare(s1.getParentSubpart(), s2.getParentSubpart());
        		if (cmp != 0) return cmp;
        	}
        }
        
        int cmp = s1.getItype().getItype().compareTo(s2.getItype().getItype());
        if (cmp!=0) return cmp;
        
        return s1.getUniqueId().compareTo(s2.getUniqueId());
    }

    public Long getSubjectUID() {
        return subjectUID;
    }

    public void setSubjectUID(Long subjectUID) {
        this.subjectUID = subjectUID;
    }
}
