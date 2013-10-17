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

import org.unitime.commons.NaturalOrderComparator;
import org.unitime.timetable.model.InstrOfferingConfig;


/**
 * @author Tomas Muller
 */
public class InstrOfferingConfigComparator implements Comparator {
	private Long subjectUID;
	private Comparator iCmp = new NaturalOrderComparator();
	
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
		int cmp = iCmp.compare(ic1.getName(), ic2.getName());
		if (cmp!=0) return cmp;
		return ic1.getUniqueId().compareTo(ic2.getUniqueId());
	}

    public Long getSubjectUID() {
        return subjectUID;
    }

    public void setSubjectUID(Long subjectUID) {
        this.subjectUID = subjectUID;
    }
}
