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
import org.unitime.timetable.model.StudentGroupReservation;

/**
 * @author Tomas Muller
 */
@SerializeWith(XGroupReservation.XCourseReservationSerializer.class)
public class XGroupReservation extends XReservation {
	private static final long serialVersionUID = 1L;
	private int iLimit;
    private String iGroup;

    public XGroupReservation() {
    	super();
    }
    
    public XGroupReservation(ObjectInput in) throws IOException, ClassNotFoundException {
    	super();
    	readExternal(in);
    }
    
    public XGroupReservation(XOffering offering, StudentGroupReservation reservation) {
    	super(XReservationType.Group, offering, reservation);
        iLimit = (reservation.getLimit() == null ? -1 : reservation.getLimit());
        iGroup = reservation.getGroup().getGroupAbbreviation();
    }
    
    public String getGroup() {
    	return iGroup;
    }
    
    /**
     * Group reservations can not be assigned over the limit.
     */
    @Override
    public boolean canAssignOverLimit() {
        return false;
    }

    /**
     * Reservation limit
     */
    @Override
    public int getReservationLimit() {
        return iLimit;
    }
    
    /**
     * Overlaps are allowed for individual reservations. 
     */
    @Override
    public boolean isAllowOverlap() {
        return false;
    }

	@Override
	public boolean isApplicable(XStudent student) {
		return student.getGroups().contains(iGroup);
	}

    /**
     * Individual or group reservation must be used (unless it is expired)
     * @return true if not expired, false if expired
     */
	@Override
	public boolean mustBeUsed() {
		return true;
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	super.readExternal(in);
    	iGroup = (String)in.readObject();
    	iLimit = in.readInt();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(iGroup);
		out.writeInt(iLimit);
	}
	
	public static class XCourseReservationSerializer implements Externalizer<XGroupReservation> {
		private static final long serialVersionUID = 1L;

		@Override
		public void writeObject(ObjectOutput output, XGroupReservation object) throws IOException {
			object.writeExternal(output);
		}

		@Override
		public XGroupReservation readObject(ObjectInput input) throws IOException, ClassNotFoundException {
			return new XGroupReservation(input);
		}
	}
}
