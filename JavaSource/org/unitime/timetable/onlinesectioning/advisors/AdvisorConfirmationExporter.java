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
package org.unitime.timetable.onlinesectioning.advisors;

import java.io.IOException;

import org.hibernate.CacheMode;
import org.springframework.stereotype.Service;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.AdvisingStudentDetails;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.StudentStatusInfo;
import org.unitime.timetable.model.Advisor;
import org.unitime.timetable.model.AdvisorCourseRequest;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.AdvisorCourseRequestDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.NameFormat;

import com.lowagie.text.DocumentException;

/**
 * @author Tomas Muller
 */
@Service("org.unitime.timetable.export.Exporter:acrf.pdf")
public class AdvisorConfirmationExporter implements Exporter {
	protected static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	
	@Override
	public String reference() {
		return "acrf.pdf";
	}

	@Override
	public void export(ExportHelper helper) throws IOException {
		boolean isAdvisor = false;
		String externalId = helper.getParameter("id");
		if (externalId == null) {
			if (!helper.getSessionContext().isAuthenticated())
				throw new IllegalArgumentException(MSG.exceptionUserNotLoggedIn());
			externalId = helper.getSessionContext().getUser().getExternalUserId();
		} else {
			helper.getSessionContext().checkPermissionAnyAuthority(helper.getAcademicSessionId(), Right.AdvisorCourseRequests);
			isAdvisor = true;
		}
		if (externalId == null)
			throw new IllegalArgumentException(MSG.exceptionBadStudentId());
		if (externalId.isEmpty())
			throw new IllegalArgumentException(MSG.exceptionUserNotLoggedIn());
		
		Long sessionId = helper.getAcademicSessionId();
		if (sessionId == null)
			throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
		
		Student student = Student.findByExternalId(helper.getAcademicSessionId(), externalId);
		if (student == null)
			throw new IllegalArgumentException(MSG.exceptionNoStudent());
		
		SessionDAO.getInstance().getSession().setCacheMode(CacheMode.REFRESH);
		
		AdvisingStudentDetails details = new AdvisingStudentDetails();
		details.setSessionId(sessionId);
		details.setStudentId(student.getUniqueId());
		details.setStudentName(student.getName(NameFormat.LAST_FIRST_MIDDLE.reference()));
		details.setStudentExternalId(student.getExternalUniqueId());
		details.setSessionName(student.getSession().getLabel());
		details.setStudentEmail(student.getEmail());
		
		if (isAdvisor) {
			Advisor advisor = Advisor.findByExternalId(helper.getSessionContext().getUser().getExternalUserId(), sessionId);
			if (advisor != null)
				details.setAdvisorEmail(advisor.getEmail());
		}
		if (!details.hasAdvisorEmail()) {
			AdvisorCourseRequest lastAcr = null;
			for (AdvisorCourseRequest acr: student.getAdvisorCourseRequests()) {
				if (lastAcr == null || lastAcr.getTimestamp().before(acr.getTimestamp())) lastAcr = acr;
			}
			if (lastAcr != null) {
				Advisor advisor = Advisor.findByExternalId(lastAcr.getChangedBy(), sessionId);
				if (advisor != null)
					details.setAdvisorEmail(advisor.getEmail());
			}
		}
		if (!details.hasAdvisorEmail()) {
			String email = null;
			for (Advisor a: student.getAdvisors()) {
				if (a.getEmail() != null && !a.getEmail().isEmpty()) {
					email = (email == null ? "" : email + "\n") + a.getEmail();
				}
			}
			details.setAdvisorEmail(email);;
		}
		if (!details.hasAdvisorEmail() && isAdvisor) {
			TimetableManager manager = TimetableManager.findByExternalId(helper.getSessionContext().getUser().getExternalUserId());
			if (manager != null)
				details.setAdvisorEmail(manager.getEmailAddress());
		}
		
		if (student.getSectioningStatus() != null) {
			StudentStatusInfo status = new StudentStatusInfo();
			status.setUniqueId(student.getSectioningStatus().getUniqueId());
			status.setReference(student.getSectioningStatus().getReference());
			status.setLabel(student.getSectioningStatus().getLabel());
			details.setStatus(status);
		} else if (student.getSession().getDefaultSectioningStatus() != null) {
			StudentStatusInfo info = new StudentStatusInfo();
			info.setUniqueId(null);
			info.setReference("");
			info.setLabel(MSG.studentStatusSessionDefault(student.getSession().getDefaultSectioningStatus().getLabel()));
			info.setEffectiveStart(null); info.setEffectiveStop(null);
			details.setStatus(info);
		} else {
			StudentStatusInfo info = new StudentStatusInfo();
			info.setReference("");
			info.setLabel(MSG.studentStatusSystemDefault());
			info.setAllEnabled();
			details.setStatus(info);
		}
		
		details.setRequest(AdvisorGetCourseRequests.getRequest(student, AdvisorCourseRequestDAO.getInstance().getSession()));
		
		helper.setup("application/pdf", "crf-" + student.getSession().getAcademicTerm() + student.getSession().getAcademicYear() +
				(isAdvisor ? "-" + student.getName(NameFormat.LAST_FIRST_MIDDLE.reference()).replaceAll("[&$\\+,/:;=\\?@<>\\[\\]\\{\\}\\|\\^\\~%#`\\t\\s\\n\\r \\\\]", "") + "-" + student.getExternalUniqueId() : "") + ".pdf", false);
		try {
			new AdvisorConfirmationPDF(details).generatePdfConfirmation(helper.getOutputStream());
		} catch (DocumentException e) {
			throw new IOException(e.getMessage(), e);
		}
	}

}
