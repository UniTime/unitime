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
package org.unitime.timetable.onlinesectioning.model;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * @author Tomas Muller
 */
public class XCourseRestriction extends XRestriction {
	private static final long serialVersionUID = 1L;
	private XCourseId iCourseId;
	private int iLimit = -1;
    
    public XCourseRestriction() {
    	super();
    }
    
    public XCourseRestriction(ObjectInput in) throws IOException, ClassNotFoundException {
    	super();
    	readExternal(in);
    }
    
    public XCourseRestriction(org.cpsolver.studentsct.reservation.CourseRestriction reservation) {
    	super(XRestrictionType.Course, reservation);
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
     * Check the courses
     */
    @Override
    public boolean isApplicable(XStudent student, XCourseId course) {
    	if (course != null)
    		return iCourseId.equals(course);
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
}