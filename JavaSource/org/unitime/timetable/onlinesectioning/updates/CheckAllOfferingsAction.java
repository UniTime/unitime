/*
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

import java.util.List;

import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;

public class CheckAllOfferingsAction extends CheckOfferingAction{
	private static final long serialVersionUID = 1L;
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	
	public CheckAllOfferingsAction() {
		super();
	}
	
	@Override
	public Boolean execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		List<Long> offeringIds = null;
		helper.beginTransaction();
		try {
			offeringIds = helper.getHibSession().createQuery(
					"select io.uniqueId from InstructionalOffering io " + 
					"where io.session.uniqueId = :sessionId and io.notOffered = false")
					.setLong("sessionId", server.getAcademicSession().getUniqueId()).list();
			helper.commitTransaction();
		} catch (Exception e) {
			helper.rollbackTransaction();
			if (e instanceof SectioningException)
				throw (SectioningException)e;
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		}
		
		helper.info("Checking all offerings for " + server.getAcademicSession() + "...");
		Lock lock = server.lockAll();
		try {
			for (Long offeringId: offeringIds)
				checkOffering(server, helper, server.getOffering(offeringId));
		} finally {
			lock.release();
		}
		
		helper.info("Updating enrollment counts...");
		
		helper.info("Check done.");
		return true;
	}
	
	@Override
	public boolean check(Enrollment e) {
		if (e.getSections().size() != e.getConfig().getSubparts().size()) return false;
		for (Section s1: e.getSections())
			for (Section s2: e.getSections()) {
				if (s1.getId() < s2.getId() && s1.isOverlapping(s2)) return false;
				if (s1.getId() != s2.getId() && s1.getSubpart().getId() == s2.getSubpart().getId()) return false;
			}
		for (Request r: e.getStudent().getRequests()) {
			if (r.getId() != e.getRequest().getId() && r.getInitialAssignment() != null && r.getInitialAssignment().isOverlapping(e)) {
				if (e.getRequest().isAlternative() && !r.isAlternative())
					return false;
				if (e.getRequest().isAlternative() == r.isAlternative() && e.getRequest().getPriority() > r.getPriority())
					return false;
			}
		}
		return true;
	}

}
