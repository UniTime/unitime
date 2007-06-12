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

import org.unitime.timetable.model.InstrOfferingConfig;


public class InstrOfferingConfigComparator implements Comparator {
	private Long subjectUID;
	
    public InstrOfferingConfigComparator(Long subjectUID) {
        this.subjectUID = subjectUID;
    }
	
	public int compare(Object o1, Object o2) {
		InstrOfferingConfig ic1 = (InstrOfferingConfig)o1;
		InstrOfferingConfig ic2 = (InstrOfferingConfig)o2;
		if (!ic1.getInstructionalOffering().equals(ic2.getInstructionalOffering())) {
			Comparator cmp = new InstructionalOfferingComparator(subjectUID);
			return cmp.compare(ic1.getInstructionalOffering(),ic2.getInstructionalOffering());
		}
		return ic1.getUniqueId().compareTo(ic2.getUniqueId());
	}

    public Long getSubjectUID() {
        return subjectUID;
    }

    public void setSubjectUID(Long subjectUID) {
        this.subjectUID = subjectUID;
    }
}
