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
