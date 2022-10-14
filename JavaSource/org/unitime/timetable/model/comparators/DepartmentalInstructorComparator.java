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
import org.unitime.timetable.util.NameFormat;


/**
 * Compares Staff based on specified criteria
 * Defaults to compare by name
 * 
 * @author Heston Fernandes
 */
public class DepartmentalInstructorComparator implements Comparator<DepartmentalInstructor> {
	
	public static enum CompareBy {
		NAME, POSITION;
	}
    
    private CompareBy iCompareBy = CompareBy.NAME;
    private NameFormat iFormat = NameFormat.LAST_FIRST_MIDDLE;
    
    public DepartmentalInstructorComparator() {
    }
    
    public DepartmentalInstructorComparator(NameFormat nameFormat) {
    	iFormat = nameFormat;
    }
    
    public DepartmentalInstructorComparator(String nameFormat) {
    	iFormat = NameFormat.fromReference(nameFormat);
    }
    		
    public DepartmentalInstructorComparator(CompareBy compareBy) {
    	iCompareBy = compareBy;
    }
    
    public DepartmentalInstructorComparator(CompareBy compareBy, NameFormat nameFormat) {
    	iCompareBy = compareBy;
    	iFormat = nameFormat;
    }
    
    public DepartmentalInstructorComparator(CompareBy compareBy, String nameFormat) {
    	iCompareBy = compareBy;
    	iFormat = NameFormat.fromReference(nameFormat);
    }


    public int compare(DepartmentalInstructor s1, DepartmentalInstructor s2) {
        if (iCompareBy == CompareBy.POSITION) {
        	Integer l1 = (s1.getPositionType() == null ? -1 : s1.getPositionType().getSortOrder());
        	Integer l2 = (s2.getPositionType() == null ? -1 : s2.getPositionType().getSortOrder());
        	if (!l1.equals(l2)) return l1.compareTo(l2);
        }
        return  iFormat.format(s1).compareToIgnoreCase(iFormat.format(s2));
    }

}
