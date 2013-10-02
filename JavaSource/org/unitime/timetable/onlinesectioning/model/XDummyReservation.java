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

public class XDummyReservation extends XReservation {
	private static final long serialVersionUID = 1L;


	public XDummyReservation() {
		super();
	}
	
	public XDummyReservation(XOffering offering) {
		super(XReservationType.Dummy, offering, null);
	}

    /**
     * Dummy reservation is unlimited
     */
    @Override
    public int getReservationLimit() {
        return -1;
    }

    /**
     * Dummy reservation has low priority
     */
    @Override
    public int getPriority() {
        return 4;
    }

    /**
     * Dummy reservation is not applicable to any students
     */
    @Override
    public boolean isApplicable(XStudent student) {
        return false;
    }

    /**
     * Dummy reservation cannot go over the limit
     */
    @Override
    public boolean canAssignOverLimit() {
        return false;
    }

    
    /**
     * Dummy reservation do not need to be used
     */
    @Override
    public boolean mustBeUsed() {
        return false;
    }

}