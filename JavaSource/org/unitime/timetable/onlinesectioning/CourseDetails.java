/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning;

import java.io.Serializable;
import java.util.List;

import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.Enrollment;

import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;

public class CourseDetails implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int iEnrollment = 0, iLimit = 0;
	
	public CourseDetails(XCourse course, List<XCourseRequest> requests) {
		if (requests != null)
			for (XCourseRequest r: requests)
				if (r.getEnrollment() != null && r.getEnrollment().getCourseId().equals(course.getCourseId()))
					iEnrollment ++;
		iLimit = course.getLimit();
	}
	
	public CourseDetails(Course course) {
		iEnrollment = 0;
		for (Enrollment e: course.getEnrollments())
			if (!e.getStudent().isDummy()) iEnrollment ++;
		iLimit = course.getLimit();
	}
	
	public CourseDetails(CourseOffering course) {
		iEnrollment = course.getEnrollment();
        iLimit = 0;
        boolean unlimited = false;
        for (InstrOfferingConfig config: course.getInstructionalOffering().getInstrOfferingConfigs()) {
        	if (config.isUnlimitedEnrollment()) unlimited = true;
        	iLimit += config.getLimit();
        }
        if (course.getReservation() != null)
        	iLimit = course.getReservation();
        if (iLimit >= 9999) unlimited = true;
        if (unlimited) iLimit = -1;
	}
	
	public int getEnrollment() { return iEnrollment; }
	public void setEnrollment(int enrollment) { iEnrollment = enrollment; }
	
	public int getLimit() { return iLimit; }
	public void setLimit(int limit) { iLimit = limit; }
}
