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

import org.unitime.timetable.model.StudentGroupReservation;

public class XGroupReservation extends XReservation {
	private static final long serialVersionUID = 1L;
	private int iLimit;
    private String iGroup;

    public XGroupReservation() {
    	super();
    }
    
    public XGroupReservation(XOffering offering, StudentGroupReservation reservation) {
    	super(XReservationType.Group, offering, reservation);
        iLimit = reservation.getLimit();
        iGroup = reservation.getGroup().getGroupAbbreviation();
    }
    
    /**
     * Group reservations are of the second highest priority
     */
    @Override
    public int getPriority() {
        return 1;
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
}
