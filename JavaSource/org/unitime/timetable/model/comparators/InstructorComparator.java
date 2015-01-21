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

import org.unitime.timetable.model.ClassInstructor;


/**
 *  Compares instructors for a class
 *  COMPARE_BY_LEAD - if both are not lead then compares by percent share.  
 *  COMPARE_BY_PCT_SHARE - if both have the same percent share then compares by name
 *  
 *  @author Heston Fernandes
 */
public class InstructorComparator implements Comparator {

    public final short COMPARE_BY_NAME = 1;
    public final short COMPARE_BY_LEAD = 2;
    public final short COMPARE_BY_PCT_SHARE = 3;
    private short compareBy;
    
    public InstructorComparator() {
        this.compareBy = COMPARE_BY_NAME;
    }
    
    public void setCompareBy(short compareBy) {
        this.compareBy = compareBy;
    }
    
    public static int compareStrings(String s1, String s2) {
    	return (s1==null?"":s1.toUpperCase()).compareTo(s2==null?"":s2.toUpperCase());
    }
    
    public int compare(Object o1, Object o2) {
        
        // Check if objects are of class Instructional Offering
        if (!(o1 instanceof ClassInstructor)) {
            throw new ClassCastException(
                    "o1 Class must be of type ClassInstructor");
        }
        if (!(o2 instanceof ClassInstructor)) {
            throw new ClassCastException(
                    "o2 Class must be of type ClassInstructor");
        }

        ClassInstructor ci1 = (ClassInstructor) o1;
        ClassInstructor ci2 = (ClassInstructor) o2;

        if (ci1.getUniqueId().equals(ci2.getUniqueId())) return 0;
        
       	int cmp = Double.compare(ci1.isLead().booleanValue()?0:1,ci2.isLead().booleanValue()?0:1);
       	if (cmp!=0) return cmp;
        
        if (compareBy==COMPARE_BY_LEAD || compareBy==COMPARE_BY_PCT_SHARE) {
            cmp = ci1.getPercentShare().compareTo(ci1.getPercentShare());
            if (cmp!=0) return cmp;
        }
        
        cmp = compareStrings(ci1.getInstructor().getLastName(),ci2.getInstructor().getLastName());
        if (cmp!=0) return cmp;
        cmp = compareStrings(ci1.getInstructor().getFirstName(),ci2.getInstructor().getFirstName());
        if (cmp!=0) return cmp;
        cmp = compareStrings(ci1.getInstructor().getMiddleName(),ci2.getInstructor().getMiddleName());
        if (cmp!=0) return cmp;
        
        return ci1.getInstructor().getUniqueId().compareTo(ci2.getInstructor().getUniqueId());
    }
}
