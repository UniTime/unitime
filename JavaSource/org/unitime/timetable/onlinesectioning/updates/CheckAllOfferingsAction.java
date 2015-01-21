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
		if (!offering.getOfferingId().equals(request.getEnrollment().getOfferingId())) return true;
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
		if (!offering.isAllowOverlap(student, request.getEnrollment().getConfigId(), sections))
			for (XRequest r: student.getRequests())
			if (r instanceof XCourseRequest && !r.getRequestId().equals(request.getRequestId()) && ((XCourseRequest)r).getEnrollment() != null) {
				XEnrollment e = ((XCourseRequest)r).getEnrollment();
				XOffering other = server.getOffering(e.getOfferingId());
				if (other != null) {
					List<XSection> assignment = other.getSections(e);
					if (!other.isAllowOverlap(student, e.getConfigId(), assignment))
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
