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
package org.unitime.timetable.model;

import java.util.Date;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.base.BaseIndividualOverrideReservation;
import org.unitime.timetable.onlinesectioning.model.XReservation.Flags;

public class IndividualOverrideReservation extends BaseIndividualOverrideReservation {
	private static final long serialVersionUID = 1L;
	
	public IndividualOverrideReservation() {
		super();
	}
	
	public int getFlagsNotNull() {
		if (getFlags() == null) return 0;
		return getFlags();
	}
	
    /**
     * True if holding this reservation allows a student to have attend overlapping class. 
     */
	@Override
    public boolean isAllowOverlap() { return Flags.AllowOverlap.in(getFlagsNotNull()); }
    
    public void setAllowOverlap(boolean allowOverlap) { setFlags(Flags.AllowOverlap.set(getFlagsNotNull(), allowOverlap)); }
    
    /**
     * True if holding this reservation allows a student to have attend a class that is disabled for student scheduling. 
     */
    public boolean isAllowDisabled() { return Flags.AllowDiabled.in(getFlagsNotNull()); }
    
    public void setAllowDisabled(boolean allowDisabled) { setFlags(Flags.AllowDiabled.set(getFlagsNotNull(), allowDisabled)); }

    /**
     * True if can go over the course / config / section limit. Only to be used in the online sectioning. 
      */
    @Override
    public boolean isCanAssignOverLimit() { return Flags.CanAssignOverLimit.in(getFlagsNotNull()); }
    
    public void setCanAssignOverLimit(boolean canAssignOverLimit) { setFlags(Flags.CanAssignOverLimit.set(getFlagsNotNull(), canAssignOverLimit)); }
    
    /**
     * If true, student must use the reservation (if applicable)
     */
    @Override
    public boolean isMustBeUsed() { return Flags.MustBeUsed.in(getFlagsNotNull()); }
    
    public void setMustBeUsed(boolean mustBeUsed) { setFlags(Flags.MustBeUsed.set(getFlagsNotNull(), mustBeUsed)); }
    
    /**
     * If true, the reservation is override (it is always expired)
     */
    @Override
    public boolean isAlwaysExpired() { return Flags.AlwaysExpired.in(getFlagsNotNull()); }
    
    public void setAlwaysExpired(boolean override) { setFlags(Flags.AlwaysExpired.set(getFlagsNotNull(), override)); }
        
	@Override
	public boolean isExpired() {
		return (isAlwaysExpired() ? true : super.isExpired());
	}
	
	@Override
	public Date getStartDate() {
		return (isAlwaysExpired() ? null : super.getStartDate());		
	}
	
	@Override
	public Date getExpirationDate() {
		return (isAlwaysExpired() ? null : super.getExpirationDate());		
	}
	
	@Override
	public int getPriority() {
		return (isAlwaysExpired() ? ApplicationProperty.ReservationPriorityOverride.intValue() : ApplicationProperty.ReservationPriorityIndividual.intValue());
	}
}
