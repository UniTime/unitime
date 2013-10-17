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

import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;

/**
 * 
 * @author Tomas Muller
 *
 */
public class NavigationComparator implements Comparator {
	ClassComparator iClassComparator = new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY);
	SchedulingSubpartComparator iSchedulingSubpartComparator = new SchedulingSubpartComparator();
	InstructionalOfferingComparator iInstructionalOfferingComparator = new InstructionalOfferingComparator(null);
	InstrOfferingConfigComparator iInstrOfferingConfigComparator = new InstrOfferingConfigComparator(null);
	
	public int compare(Object o1, Object o2) {
		if (o1 instanceof Class_)
			return iClassComparator.compare((Class_)o1, (Class_)o2);
		if (o1 instanceof SchedulingSubpart)
			return iSchedulingSubpartComparator.compare(o1, o2);
		if (o1 instanceof InstrOfferingConfig) {
			if (iInstrOfferingConfigComparator.getSubjectUID()==null) {
				InstrOfferingConfig ioc = (InstrOfferingConfig)o1;
				iInstrOfferingConfigComparator.setSubjectUID(ioc.getControllingCourseOffering().getSubjectArea().getUniqueId());
			}
			return iInstrOfferingConfigComparator.compare(o1, o2);
		}
		if (o1 instanceof InstructionalOffering) {
			if (iInstructionalOfferingComparator.getSubjectUID()==null) {
				InstructionalOffering io = (InstructionalOffering)o1;
				iInstructionalOfferingComparator.setSubjectUID(io.getControllingCourseOffering().getSubjectArea().getUniqueId());
			}
			return iInstructionalOfferingComparator.compare(o1, o2);
		}
		return o1.toString().compareTo(o2.toString());
	}

}
