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
package org.unitime.timetable.onlinesectioning.model;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.unitime.timetable.model.CourseReservation;

/**
 * @author Tomas Muller
 */
@SerializeWith(XCourseReservation.XCourseReservationSerializer.class)
public class XCourseReservation extends XReservation {
	private static final long serialVersionUID = 1L;
	private XCourseId iCourseId;
	private int iLimit = -1;
    
    public XCourseReservation() {
    	super();
    }
    
    public XCourseReservation(ObjectInput in) throws IOException, ClassNotFoundException {
    	super();
    	readExternal(in);
    }
    
    public XCourseReservation(XOffering offering, CourseReservation reservation) {
    	super(XReservationType.Course, offering, reservation);
    	iCourseId = new XCourseId(reservation.getCourse());
    	for (XCourse course: offering.getCourses())
    		if (course.getCourseId().equals(iCourseId))
    			iLimit = course.getLimit();
    }
    
    public XCourseReservation(org.cpsolver.studentsct.reservation.CourseReservation reservation) {
    	super(XReservationType.Course, reservation);
    	iCourseId = new XCourseId(reservation.getCourse());
    	iLimit = reservation.getCourse().getLimit();
    }
    
    /** Course offering id */
    public Long getCourseId() {
    	return iCourseId.getCourseId();
    }

    /** Instructional offering id */
    public Long getOfferingId() {
    	return iCourseId.getOfferingId();
    }

    /**
     * Curriculum reservation cannot go over the limit
     */
    @Override
    public boolean canAssignOverLimit() {
        return false;
    }
    
    /**
     * Course reservation do not need to be used
     */
    @Override
    public boolean mustBeUsed() {
        return false;
    }

    /**
     * Reservation limit (-1 for unlimited)
     */
    @Override
    public int getReservationLimit() {
        return iLimit;
    }
        
    /**
     * Check the courses
     */
    @Override
    public boolean isApplicable(XStudent student) {
    	for (XRequest request: student.getRequests()) {
    		if (request instanceof XCourseRequest && ((XCourseRequest)request).getCourseIds().contains(iCourseId))
    			return true;
    	}
    	return false;
    }
    
    @Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	super.readExternal(in);
    	iCourseId = new XCourseId(in);
    	iLimit = in.readInt();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		iCourseId.writeExternal(out);
		out.writeInt(iLimit);
	}
	
	public static class XCourseReservationSerializer implements Externalizer<XCourseReservation> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XCourseReservation object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XCourseReservation readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XCourseReservation(input);
		}
	}
}