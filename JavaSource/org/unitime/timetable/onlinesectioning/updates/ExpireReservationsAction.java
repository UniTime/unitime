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
		
		Lock lock = server.lockOffering(offering.getOfferingId(), null, true);
		try {
			// Expire reservations
			// no longer needed
			for (XReservation reservation: reservations) {
				helper.getAction().addOther(OnlineSectioningLog.Entity.newBuilder()
						.setUniqueId(reservation.getReservationId())
						.setType(OnlineSectioningLog.Entity.EntityType.RESERVATION));
				
				helper.info("Expiring reservation " + reservation.getReservationId() + "...");
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
