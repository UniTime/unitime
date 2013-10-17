/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
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
			if (this.equals(e.getCourseRequest()) || (e.getCourseRequest() == null && getCourseOffering().equals(e.getCourseOffering())))
				ret.add(e);
    	}
    	return ret;
    }


}
