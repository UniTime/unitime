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

import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructionalOffering;


/**
 *  @author Heston Fernandes
 */
public class InstructionalOfferingComparator implements Comparator {
    
    private Long subjectUID;

    public InstructionalOfferingComparator(Long subjectUID) {
        this.subjectUID = subjectUID;
    }

    public int compare(Object o1, Object o2) {
        InstructionalOffering i1 = (InstructionalOffering) o1;
        InstructionalOffering i2 = (InstructionalOffering) o2;

        if (i1.getCourseOfferings()==null || i1.getCourseOfferings().isEmpty())
            throw new IndexOutOfBoundsException("i1 - Instructional Offering must have at least on Course Offering");

        if (i2.getCourseOfferings()==null || i2.getCourseOfferings().isEmpty())
            throw new IndexOutOfBoundsException("i2 - Instructional Offering must have at least on Course Offering");
        
        if (i1.getUniqueId().equals(i2.getUniqueId())) return 0;
        
        CourseOffering co1 = i1.findSortCourseOfferingForSubjectArea(getSubjectUID());
        CourseOffering co2 = i2.findSortCourseOfferingForSubjectArea(getSubjectUID());
        
        int cmp = co1.getSubjectAreaAbbv().compareTo(co2.getSubjectAreaAbbv());
        if (cmp!=0) return cmp;

        cmp = (co1.getCourseNbr().compareTo(co2.getCourseNbr()));
        if (cmp!=0) return cmp;
        
        return co1.getUniqueId().compareTo(co2.getUniqueId());
    }

    /**
     * @return Returns the subjectUID.
     */
    public Long getSubjectUID() {
        return subjectUID;
    }

    /**
     * @param subjectUID
     *            The subjectUID to set.
     */
    public void setSubjectUID(Long subjectUID) {
        this.subjectUID = subjectUID;
    }
}
