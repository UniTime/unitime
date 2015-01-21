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
