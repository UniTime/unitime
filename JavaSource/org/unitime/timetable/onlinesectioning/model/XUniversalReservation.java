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

import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.model.UniversalOverrideReservation;
import org.unitime.timetable.onlinesectioning.status.StatusPageSuggestionsAction.StudentMatcher;

import jakarta.persistence.Transient;

/**
 * @author Tomas Muller
 */
public class XUniversalReservation extends XReservation {
	private static final long serialVersionUID = 1L;
	private int iLimit;
    private Boolean iOverride;
    private String iFilter;
    
    
	public XUniversalReservation() {
    	super();
    }
    
    public XUniversalReservation(ObjectInput in) throws IOException, ClassNotFoundException {
    	super();
    	readExternal(in);
    }
    
    public XUniversalReservation(XOffering offering, UniversalOverrideReservation reservation) {
    	super(XReservationType.Universal, offering, reservation);
    	iLimit = (reservation.getLimit() == null ? -1 : reservation.getLimit());
    	iFilter = reservation.getFilter();
    	iOverride = reservation.isAlwaysExpired();
        setMustBeUsed(reservation.isMustBeUsed());
        setAllowOverlap(reservation.isAllowOverlap());
        setCanAssignOverLimit(reservation.isCanAssignOverLimit());
    }

    public XUniversalReservation(org.cpsolver.studentsct.reservation.UniversalOverride reservation) {
    	super(XReservationType.Universal, reservation);
    	iLimit = (int)Math.round(reservation.getReservationLimit());
    	iFilter = reservation.getFilter();
    	iOverride = reservation.isOverride();
    }
    
    /**
     * Reservation limit (-1 for unlimited)
     */
    @Override
    public int getReservationLimit() {
        return iLimit;
    }
    
    /**
     * Student filter
     */
    public String getFilter() {
        return iFilter;
    }
    
    @Override
    public boolean isOverride() { return iOverride; }
    
    @Override
    public boolean isExpired() {
    	return (isOverride() ? true : super.isExpired());
    }
    
    @Override
    public boolean isAlwaysExpired() {
    	return iOverride;
    }
    
    transient Query iQuery = null;
	@Transient
	public Query getStudentQuery() {
		if (iQuery == null)
			iQuery = new Query(getFilter());
		return iQuery;
	}

    /**
     * Check the area, classifications and majors
     */
    @Override
    public boolean isApplicable(XStudent student, XCourseId course) {
    	return getFilter() != null && !getFilter().isEmpty() && getStudentQuery().match(
    			new StudentMatcher(student, null, null, false)
    			);
    }
    
    @Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	super.readExternal(in);
    	iFilter = (String)in.readObject();
    	iLimit = in.readInt();
		iOverride = in.readBoolean();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(iFilter);
		out.writeInt(iLimit);
		out.writeBoolean(iOverride);
	}
}
