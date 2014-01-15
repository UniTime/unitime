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
import java.util.HashSet;
import java.util.Set;

import net.sf.cpsolver.studentsct.reservation.GroupReservation;

import org.infinispan.commons.marshall.Externalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.unitime.timetable.model.IndividualReservation;
import org.unitime.timetable.model.Student;

/**
 * @author Tomas Muller
 */
@SerializeWith(XIndividualReservation.XIndividualReservationSerializer.class)
public class XIndividualReservation extends XReservation {
	private static final long serialVersionUID = 1L;
	private Set<Long> iStudentIds = new HashSet<Long>();
	private Integer iLimit = null;
    
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
    
    public XIndividualReservation(net.sf.cpsolver.studentsct.reservation.IndividualReservation reservation) {
        super(XReservationType.Individual, reservation);
        iStudentIds.addAll(reservation.getStudentIds());
    }
    
    public XIndividualReservation(GroupReservation reservation) {
        super(XReservationType.Group, reservation);
        iStudentIds.addAll(reservation.getStudentIds());
        iLimit = (int)Math.round(reservation.getReservationLimit());
    }

    /**
     * Individual reservations are the only reservations that can be assigned over the limit.
     */
    @Override
    public boolean canAssignOverLimit() {
    	switch (getType()) {
    	case Individual:
    		return true;
    	default:
    		return false;
    	}
    }
    
    /**
     * Individual or group reservation must be used (unless it is expired)
     * @return true if not expired, false if expired
     */
    @Override
    public boolean mustBeUsed() {
        return !isExpired();
    }

    /**
     * Individual reservations are of the top priority
     */
    @Override
    public int getPriority() {
    	switch (getType()) {
    	case Individual:
    		return 0;
    	default:
    		return 1;
    	}
    }

    /**
     * Reservation is applicable for all students in the reservation
     */
    @Override
    public boolean isApplicable(XStudent student) {
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
    
    /**
     * Overlaps are allowed for individual reservations. 
     */
    @Override
    public boolean isAllowOverlap() {
    	switch (getType()) {
    	case Individual:
    		return true;
    	default:
    		return false;
    	}
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
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		
		out.writeInt(iStudentIds.size());
		for (Long studentId: iStudentIds)
			out.writeLong(studentId);
		
		out.writeInt(iLimit == null ? -2 : iLimit);
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