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
package org.unitime.timetable.model;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.model.base.BaseCourseRequest;



/**
 * @author Tomas Muller
 */
public class CourseRequest extends BaseCourseRequest implements Comparable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public CourseRequest () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public CourseRequest (java.lang.Long uniqueId) {
		super(uniqueId);
	}
	
/*[CONSTRUCTOR MARKER END]*/
    
    public int compareTo(Object o) {
        if (o==null || !(o instanceof CourseRequest)) return -1;
        CourseRequest cr = (CourseRequest)o;
        int cmp = getOrder().compareTo(cr.getOrder());
        if (cmp!=0) return cmp;
        return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(cr.getUniqueId() == null ? -1 : cr.getUniqueId());
    }
    
    public List<StudentClassEnrollment> getClassEnrollments() {
    	List<StudentClassEnrollment> ret = new ArrayList<StudentClassEnrollment>();
    	for (StudentClassEnrollment e: getCourseDemand().getStudent().getClassEnrollments()) {
			if (getCourseOffering().equals(e.getCourseOffering()))
				ret.add(e);
    	}
    	return ret;
    }


}
