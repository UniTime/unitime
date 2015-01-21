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

import org.unitime.timetable.model.DepartmentalInstructor;


/**
 * Compares Staff based on specified criteria
 * Defaults to compare by name
 * 
 * @author Heston Fernandes
 */
public class DepartmentalInstructorComparator implements Comparator {

    public static final short COMPARE_BY_NAME = 1;
    public static final short COMPARE_BY_POSITION = 2;
    
    private short compareBy;
    
    public DepartmentalInstructorComparator() {
        compareBy = COMPARE_BY_NAME;
    }
    
    public DepartmentalInstructorComparator(short compareBy) {
        if (compareBy!=COMPARE_BY_NAME 
                && compareBy!=COMPARE_BY_POSITION) {
            this.compareBy = COMPARE_BY_NAME;
        }
        else {
            this.compareBy = compareBy;
        }
    }

    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof DepartmentalInstructor) || o1==null)
            throw new ClassCastException("o1 Class must be of type DepartmentalInstructor and cannot be null");
        if (!(o2 instanceof DepartmentalInstructor) || o2==null)
            throw new ClassCastException("o2 Class must be of type DepartmentalInstructor and cannot be null");
        
        DepartmentalInstructor s1 = (DepartmentalInstructor) o1;
        DepartmentalInstructor s2 = (DepartmentalInstructor) o2;
        
        if (compareBy==COMPARE_BY_POSITION) {
            Integer l1 = new Integer(-1);
            if (s1.getPositionType()!=null) 
                l1 = s1.getPositionType().getSortOrder();
            
            Integer l2 = new Integer(-1);
            if (s2.getPositionType()!=null)
                l2 = s2.getPositionType().getSortOrder();
            
            int ret = l1.compareTo(l2);
            if (ret!=0)
                return ret;
        }
        
        return  s1.nameLastNameFirst().compareToIgnoreCase(s2.nameLastNameFirst());
    }

}
