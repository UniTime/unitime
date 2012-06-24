/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.updates;

import java.util.ArrayList;
import java.util.Collection;

import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;

@Deprecated
public class UpdateEnrollmentCountsAction implements OnlineSectioningAction<Boolean>{
	private static final long serialVersionUID = 1L;
	private Collection<Long> iOfferingIds;
	
	public UpdateEnrollmentCountsAction(Long... offeringIds) {
		iOfferingIds = new ArrayList<Long>();
		for (Long offeringId: offeringIds)
			iOfferingIds.add(offeringId);
	}
	
	public UpdateEnrollmentCountsAction(Collection<Long> offeringIds) {
		iOfferingIds = offeringIds;
	}
	
	public Collection<Long> getOfferingIds() { return iOfferingIds; }

	@Override
	@Deprecated
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		/*
		for (Long offeringId: getOfferingIds()) {
			// offering is locked -> assuming that the offering will get checked when it is unlocked
			if (server.isOfferingLocked(offeringId)) continue;
			// lock and update the offering
			Lock lock = server.lockOffering(offeringId, null, false);
			try {
				CheckOfferingAction.updateEnrollmentCounters(server, helper, server.getOffering(offeringId));
			} finally {
				lock.release();
			}
		}
		*/
		return true;
	}

	@Override
	public String name() {
		return "update-enrollment-counts";
	}
	
}
