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

import org.cpsolver.studentsct.reservation.GroupReservation;
import org.cpsolver.studentsct.reservation.ReservationOverride;
import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.unitime.timetable.model.IndividualOverrideReservation;
import org.unitime.timetable.model.IndividualReservation;
import org.unitime.timetable.model.OverrideReservation;
import org.unitime.timetable.model.Student;

/**
 * @author Tomas Muller
 */
@SerializeWith(XIndividualReservation.XIndividualReservationSerializer.class)
public class XIndividualReservation extends XReservation {
	private static final long serialVersionUID = 1L;
	private Set<Long> iStudentIds = new HashSet<Long>();
	private Integer iLimit = null;
    private Boolean iExpired = null;
    
    public XIndividualReservation() {
        super();
    }
    
    public XIndividualReservation(ObjectInput in) throws IOException, ClassNotFoundException {
    	super();
    	readExternal(in);
    }

    public XIndividualReservation(XOffering offering, IndividualReservation reservation) {
        super(XReservationType.Individual, offering, reservation);
        for (Student student: reservation.getStudents())
        	iStudentIds.add(student.getUniqueId());
    }
    
    public XIndividualReservation(XOffering offering, OverrideReservation reservation) {
        super(XReservationType.Override, offering, reservation);
        for (Student student: reservation.getStudents())
        	iStudentIds.add(student.getUniqueId());
        setMustBeUsed(reservation.getOverrideType().isMustBeUsed());
        setAllowOverlap(reservation.getOverrideType().isAllowTimeConflict());
        setCanAssignOverLimit(reservation.getOverrideType().isAllowOverLimit());
        iExpired = reservation.getOverrideType().isExpired();
    }
    
    public XIndividualReservation(XOffering offering, IndividualOverrideReservation reservation) {
        super(XReservationType.Override, offering, reservation);
        for (Student student: reservation.getStudents())
        	iStudentIds.add(student.getUniqueId());
        setMustBeUsed(reservation.isMustBeUsed());
        setAllowOverlap(reservation.isAllowOverlap());
        setCanAssignOverLimit(reservation.isCanAssignOverLimit());
        if (reservation.isAlwaysExpired()) iExpired = true; else iType = XReservationType.Individual;
    }

    public XIndividualReservation(org.cpsolver.studentsct.reservation.IndividualReservation reservation) {
        super(XReservationType.Individual, reservation);
        iStudentIds.addAll(reservation.getStudentIds());
    }
    
    public XIndividualReservation(ReservationOverride reservation) {
        super(XReservationType.Override, reservation);
        iStudentIds.addAll(reservation.getStudentIds());
    }

    public XIndividualReservation(GroupReservation reservation) {
        super(XReservationType.Group, reservation);
        iStudentIds.addAll(reservation.getStudentIds());
        iLimit = (int)Math.round(reservation.getReservationLimit());
        if (reservation.isAllowDisabled())
        	setAllowDisabled(true);
    }

    /**
     * Reservation is applicable for all students in the reservation
     */
    @Override
    public boolean isApplicable(XStudent student, XCourseId course) {
        return iStudentIds.contains(student.getStudentId());
    }
    
    /**
     * Students in the reservation
     */
    public Set<Long> getStudentIds() {
        return iStudentIds;
    }

    /**
     * Reservation limit == number of students in the reservation
     */
    @Override
    public int getReservationLimit() {
        return (iLimit == null ? iStudentIds.size() : iLimit);
    }
    
    @Override
    public boolean isExpired() {
    	return (getType() == XReservationType.Override && iExpired != null ? iExpired.booleanValue() : super.isExpired());
    }
    
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	super.readExternal(in);
    	
    	int nrStudents = in.readInt();
    	iStudentIds.clear();
    	for (int i = 0; i < nrStudents; i++)
    		iStudentIds.add(in.readLong());

    	iLimit = in.readInt();
    	if (iLimit == -2) iLimit = null;
    	
    	if (getType() == XReservationType.Override) {
    		switch (in.readByte()) {
    		case 0:
    			iExpired = false; break;
    		case 1:
    			iExpired = true; break;
    		default:
    			iExpired = null; break;
    		}
    	}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		
		out.writeInt(iStudentIds.size());
		for (Long studentId: iStudentIds)
			out.writeLong(studentId);
		
		out.writeInt(iLimit == null ? -2 : iLimit);
		
		if (getType() == XReservationType.Override) {
			out.writeByte(iExpired == null ? 2 : iExpired.booleanValue() ? 1 : 0);
		}
	}
	
	public static class XIndividualReservationSerializer implements Externalizer<XIndividualReservation> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XIndividualReservation object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XIndividualReservation readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XIndividualReservation(input);
		}
	}
}