/*
 * UniTime 3.4 (University Timetabling Application)
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
package org.unitime.timetable.onlinesectioning.test;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningTestFwk;
import org.unitime.timetable.onlinesectioning.updates.ReloadStudent;

public class ReloadTest extends OnlineSectioningTestFwk {

	@Override
	public List<Operation> operations() {
		org.hibernate.Session hibSession = new _RootDAO().getSession();
		
		List<Operation> loadRequests = new ArrayList<Operation>();
		
		for (final Long studentId: (List<Long>)hibSession.createQuery(
				"select s.uniqueId from Student s where s.session.uniqueId = :sessionId")
				.setLong("sessionId", getServer().getAcademicSession().getUniqueId()).list()) {
			loadRequests.add(new Operation() {
				@Override
				public double execute(OnlineSectioningServer s) {
					sLog.info("Reloading " + studentId + " ...");
					s.execute(new ReloadStudent(studentId), user());
					sLog.info("  -- " + studentId + " reloaded");
					return 1.0;
				}
			});
		}
		
		for (final Long offeringId: (List<Long>)hibSession.createQuery(
				"select io.uniqueId from InstructionalOffering io where io.session.uniqueId = :sessionId and io.notOffered = false")
				.setLong("sessionId", getServer().getAcademicSession().getUniqueId()).list()) {
			loadRequests.add(new Operation() {
				@Override
				public double execute(OnlineSectioningServer s) {
					sLog.info("Locking " + offeringId + " ...");
					s.lockOffering(offeringId);
					sLog.info("  -- " + offeringId + " locked");
					long delay = Math.round(Math.random() * 300000);
					sLog.info("Sleeping for " + delay + " ms  ...");
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {}
					sLog.info("Unlocking " + offeringId + " ...");
					s.unlockOffering(offeringId);
					sLog.info("  -- " + offeringId + " unlocked");
					return 1.0;
				}
			});
		}
		
		hibSession.close();
		
		return loadRequests;
	}
	
	public static void main(String args[]) {
		new ReloadTest().test(-1, 100);
	}

}