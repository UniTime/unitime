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
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.NameFormat;


/**
 *  Compares instructors for a class
 *  COMPARE_BY_LEAD - if both are not lead then compares by percent share.  
 *  COMPARE_BY_PCT_SHARE - if both have the same percent share then compares by name
 *  
 *  @author Heston Fernandes
 */
public class InstructorComparator implements Comparator<ClassInstructor> {
	
	public static enum CompareBy {
		NAME,
		LEAD,
		PCT_SHARE,
	}

    private CompareBy iCompareBy = CompareBy.LEAD;
    private NameFormat iNameFormat = NameFormat.defaultFormat();
    
    public InstructorComparator() {}
    
    public InstructorComparator(SessionContext cx) {
    	if (cx != null && ApplicationProperty.InstructorsDropdownFollowNameFormatting.isTrue())
    		setNameFormat(UserProperty.NameFormat.get(cx.getUser()));
    }
    
    public InstructorComparator setCompareBy(CompareBy compareBy) {
        iCompareBy = compareBy;
        return this;
    }
    
    public InstructorComparator setNameFormat(NameFormat nf) {
    	iNameFormat = nf;
    	return this;
    }
    
    public InstructorComparator setNameFormat(String nf) {
    	iNameFormat = NameFormat.fromReference(nf);
    	return this;
    }
    
    public static int compareStrings(String s1, String s2) {
    	return (s1==null?"":s1.toUpperCase()).compareTo(s2==null?"":s2.toUpperCase());
    }
    
    public int compare(ClassInstructor ci1, ClassInstructor ci2) {
        if (ci1.getUniqueId().equals(ci2.getUniqueId())) return 0;
        
        if (iCompareBy == CompareBy.LEAD && !ci1.isLead().equals(ci2.isLead())) {
            // lead goes first
        	return ci1.isLead() ? -1 : 1;
        }
        
        if ((iCompareBy == CompareBy.LEAD || iCompareBy == CompareBy.PCT_SHARE) && !ci1.getPercentShare().equals(ci2.getPercentShare())) {
        	// highest share first
            return ci2.getPercentShare().compareTo(ci1.getPercentShare());
        }
        
        int cmp = iNameFormat.format(ci1.getInstructor()).compareToIgnoreCase(iNameFormat.format(ci2.getInstructor()));
        if (cmp != 0) return cmp;
        
        // same instructor -> check responsibility
        cmp = (ci1.getResponsibility() == null ? "" : ci1.getResponsibility().getAbbreviation()).compareToIgnoreCase(ci2.getResponsibility() == null ? "" : ci2.getResponsibility().getAbbreviation());
        if (cmp != 0) return cmp;
        
        // fall back to unique ids
        return ci1.getInstructor().getUniqueId().compareTo(ci2.getInstructor().getUniqueId());
    }
}
