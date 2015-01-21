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
