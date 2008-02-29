/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
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
package org.unitime.timetable.model.comparators;

import java.util.Comparator;

import org.unitime.timetable.model.SchedulingSubpart;


/**
 * Compares scheduling subparts by itype
 * 
 * @author Heston Fernandes
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
