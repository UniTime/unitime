/*
 * UniTime 3.3 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
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

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.server.CheckMaster;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;

/**
 * @author Tomas Muller
 */
@CheckMaster(Master.REQUIRED)
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
	public boolean check(OnlineSectioningServer server, XStudent student, XOffering offering, XCourseRequest request) {
		if (request.getEnrollment() == null) return true;
		List<XSection> sections = offering.getSections(request.getEnrollment());
		XConfig config = offering.getConfig(request.getEnrollment().getConfigId());
		if (config == null || sections.size() != config.getSubparts().size()) return false;
		for (XSection s1: sections) {
			for (XSection s2: sections) {
				if (s1.getSectionId() < s2.getSectionId() && s1.isOverlapping(offering.getDistributions(), s2)) return false;
				if (!s1.getSectionId().equals(s2.getSectionId()) && s1.getSubpartId().equals(s2.getSubpartId())) return false;
			}
			if (!offering.getSubpart(s1.getSubpartId()).getConfigId().equals(config.getConfigId())) return false;
		}
		for (XRequest r: student.getRequests())
			if (r instanceof XCourseRequest && !r.getRequestId().equals(request.getRequestId()) && ((XCourseRequest)r).getEnrollment() != null) {
				XEnrollment e = ((XCourseRequest)r).getEnrollment();
				XOffering other = server.getOffering(e.getOfferingId());
				if (other != null) {
					List<XSection> assignment = other.getSections(e);
					for (XSection section: sections)
						if (section.isOverlapping(offering.getDistributions(), assignment)) {
							if (request.isAlternative() && !r.isAlternative()) return false;
							if (request.isAlternative() == r.isAlternative() && request.getPriority() > r.getPriority()) return false;
						}
				}
			}
		return true;
	}
}
