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
import java.util.HashSet;
import java.util.Set;

import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.unitime.timetable.model.LearningCommunityReservation;
import org.unitime.timetable.model.StudentGroupType;
import org.unitime.timetable.onlinesectioning.model.XStudent.XGroup;

/**
 * @author Tomas Muller
 */
@SerializeWith(XLearningCommunityReservation.XLearningCommunityReservationSerializer.class)
public class XLearningCommunityReservation extends XReservation {
	private static final long serialVersionUID = 1L;
	private int iLimit;
	private Set<Long> iStudentIds = new HashSet<Long>();
    private XCourseId iCourseId;
    private XGroup iGroup;

    public XLearningCommunityReservation() {
    	super();
    }
    
    public XLearningCommunityReservation(ObjectInput in) throws IOException, ClassNotFoundException {
    	super();
    	readExternal(in);
    }
    
    public XLearningCommunityReservation(XOffering offering, LearningCommunityReservation reservation) {
    	super(XReservationType.LearningCommunity, offering, reservation);
        iLimit = (reservation.getLimit() == null ? -1 : reservation.getLimit());
        iCourseId = new XCourseId(reservation.getCourse());
        iGroup = new XGroup(reservation.getGroup());
        if (reservation.getGroup().getType() != null && reservation.getGroup().getType().getAllowDisabledSection() == StudentGroupType.AllowDisabledSection.WithGroupReservation)
        	setAllowDisabled(true);
    }
    
    public XLearningCommunityReservation(org.cpsolver.studentsct.reservation.LearningCommunityReservation reservation) {
    	super(XReservationType.LearningCommunity, reservation);
    	iStudentIds.addAll(reservation.getStudentIds());
    	iCourseId = new XCourseId(reservation.getCourse());
    	iLimit = (int)Math.round(reservation.getLimit());
    	setAllowDisabled(reservation.isAllowDisabled());
    }
    
    public XGroup getGroup() {
    	return iGroup;
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
     * Reservation limit
     */
    @Override
    public int getReservationLimit() {
        return iLimit;
    }
    
    /**
     * Reservation is applicable for all students in the reservation
     */
    @Override
    public boolean isApplicable(XStudent student, XCourseId course) {
    	if (iGroup == null) {
    		if (!iStudentIds.contains(student.getStudentId())) return false;	
    	} else {
    		if (!student.getGroups().contains(iGroup)) return false;
    	}
        if (course != null)
    		return iCourseId.equals(course);
    	for (XRequest request: student.getRequests()) {
    		if (request instanceof XCourseRequest && ((XCourseRequest)request).getCourseIds().contains(iCourseId))
    			return true;
    	}
    	return false;
    }
    
    /**
     * Students in the reservation
     */
    public Set<Long> getStudentIds() {
        return iStudentIds;
    }
    
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	super.readExternal(in);
    	
    	if (in.readBoolean()) {
    		iGroup = new XGroup(in);
    	} else {
    		iGroup = null;
    	}
    	
    	int nrStudents = in.readInt();
    	iStudentIds.clear();
    	for (int i = 0; i < nrStudents; i++)
    		iStudentIds.add(in.readLong());
    	
    	iCourseId = new XCourseId(in);
    	iLimit = in.readInt();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		
		if (iGroup != null) {
			out.writeBoolean(true);
			iGroup.writeExternal(out);
		}
		
		out.writeInt(iStudentIds.size());
		for (Long studentId: iStudentIds)
			out.writeLong(studentId);
		
		iCourseId.writeExternal(out);
		out.writeInt(iLimit);
	}
	
	public static class XLearningCommunityReservationSerializer implements Externalizer<XLearningCommunityReservation> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XLearningCommunityReservation object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XLearningCommunityReservation readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XLearningCommunityReservation(input);
		}
	}
}
