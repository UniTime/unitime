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
package org.unitime.timetable.onlinesectioning.updates;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XReservation;
import org.unitime.timetable.onlinesectioning.server.CheckMaster;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;

/**
 * @author Tomas Muller
 */
@CheckMaster(Master.REQUIRED)
public class ExpireReservationsAction extends CheckOfferingAction {
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);

	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		helper.beginTransaction();
		try {
			helper.info("Checking for expired reservations..."); 
			Hashtable<XOffering, List<XReservation>> reservations2expire = new Hashtable<XOffering, List<XReservation>>();
			for (org.unitime.timetable.model.Reservation expiredReservation: (List<org.unitime.timetable.model.Reservation>)helper.getHibSession().createQuery(
					"select r from Reservation r where " +
					"r.instructionalOffering.session.uniqueId = :sessionId and " +
					"r.expirationDate is not null and r.expirationDate < current_timestamp()")
					.setLong("sessionId", server.getAcademicSession().getUniqueId()).list()) {
				XOffering offering = server.getOffering(expiredReservation.getInstructionalOffering().getUniqueId());
				if (offering == null) continue;
				XReservation reservation = null;
				for (XReservation r: offering.getReservations())
					if (r.getReservationId().equals(expiredReservation.getUniqueId())) { reservation = r; break; }
				if (reservation == null || reservation.isExpired()) continue; // already expired
				List<XReservation> reservations = reservations2expire.get(offering);
				if (reservations == null) {
					reservations = new ArrayList<XReservation>();
					reservations2expire.put(offering, reservations);
				}
				reservations.add(reservation);
			}
			helper.commitTransaction();
			for (Map.Entry<XOffering, List<XReservation>> entry: reservations2expire.entrySet()) {
				expireReservation(entry.getKey(), entry.getValue(), server, helper);
			}
		} catch (Exception e) {
			helper.rollbackTransaction();
			if (e instanceof SectioningException)
				throw (SectioningException)e;
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		}
		return true;
	}
	
	public void expireReservation(XOffering offering, List<XReservation> reservations, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		// offering is locked -> assuming that the offering will get checked when it is unlocked
		if (server.isOfferingLocked(offering.getOfferingId())) return;
		
		Lock lock = server.lockOffering(offering.getOfferingId(), null, name());
		try {
			// Expire reservations
			// no longer needed
			for (XReservation reservation: reservations) {
				helper.getAction().addOther(OnlineSectioningLog.Entity.newBuilder()
						.setUniqueId(reservation.getReservationId())
						.setType(OnlineSectioningLog.Entity.EntityType.RESERVATION));
				
				helper.debug("Expiring reservation " + reservation.getReservationId() + "...");
				assert reservation.isExpired();
			}

			if (server.getAcademicSession().isSectioningEnabled()) {
				// Re-check offering
				checkOffering(server, helper, offering);
				// Update enrollment counters
				// updateEnrollmentCounters(server, helper, offering);
			}
			
		} finally {
			lock.release();
		}
	}

	@Override
	public String name() {
		return "expire-reservations";
	}

}
