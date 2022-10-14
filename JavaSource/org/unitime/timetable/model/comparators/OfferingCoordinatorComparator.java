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

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.NameFormat;


/**
 *  @author Tomas Muller
 */
public class OfferingCoordinatorComparator implements Comparator<OfferingCoordinator> {
	
	public static enum CompareBy {
		NAME,
		PCT_SHARE
	}

    private CompareBy iCompareBy = CompareBy.PCT_SHARE;
	
    private NameFormat iNameFormat = NameFormat.defaultFormat();
    
    public OfferingCoordinatorComparator() {}
    
    public OfferingCoordinatorComparator(SessionContext cx) {
    	if (cx != null && ApplicationProperty.InstructorsDropdownFollowNameFormatting.isTrue())
    		setNameFormat(UserProperty.NameFormat.get(cx.getUser()));
    }
    
    public OfferingCoordinatorComparator setCompareBy(CompareBy compareBy) {
        iCompareBy = compareBy;
        return this;
    }
    
    public OfferingCoordinatorComparator setNameFormat(NameFormat nf) {
    	iNameFormat = nf;
    	return this;
    }
    
    public OfferingCoordinatorComparator setNameFormat(String nf) {
    	iNameFormat = NameFormat.fromReference(nf);
    	return this;
    }
    
    public int compare(OfferingCoordinator o1, OfferingCoordinator o2) {
    	if (iCompareBy == CompareBy.PCT_SHARE && !o1.getPercentShare().equals(o2.getPercentShare())) {
        	// highest share first
            return o2.getPercentShare().compareTo(o1.getPercentShare());
        }
    	
        int cmp = iNameFormat.format(o1.getInstructor()).compareToIgnoreCase(iNameFormat.format(o2.getInstructor()));
        if (cmp != 0) return cmp;
        
        return o1.compareTo(o2);
    }
}
