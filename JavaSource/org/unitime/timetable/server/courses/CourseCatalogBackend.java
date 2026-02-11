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
package org.unitime.timetable.server.courses;

import java.net.URL;
import java.net.URLEncoder;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.offerings.CourseCatalogPage.CatalogRequest;
import org.unitime.timetable.gwt.client.offerings.CourseCatalogPage.CatalogResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.custom.CourseDetailsProvider;
import org.unitime.timetable.onlinesectioning.custom.CourseUrlProvider;
import org.unitime.timetable.onlinesectioning.custom.Customization;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(CatalogRequest.class)
public class CourseCatalogBackend implements GwtRpcImplementation<CatalogRequest, CatalogResponse>, CourseUrlProvider {
	private static final long serialVersionUID = 2754144696775723663L;
	static protected GwtMessages MSG = Localization.create(GwtMessages.class);
	
	@Override
	public CatalogResponse execute(CatalogRequest request, SessionContext context) {
		Session session = null;
		if (request.getTerm() != null && !request.getTerm().isEmpty()) {
			session = SessionDAO.getInstance().getSession().createQuery(
					"from Session s where (s.academicTerm || s.academicYear) = :term or (s.academicTerm || s.academicYear || s.academicInitiative) = :term or cast(s.uniqueId as string) = :term", Session.class
					).setParameter("term", request.getTerm()).setMaxResults(1).uniqueResult();
			if (session == null)
				throw new GwtRpcException(MSG.errorSessionNotFound(request.getTerm()));
		} else if (context.getUser() != null) {
			session = SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId());
		}
		if (session == null)
			throw new GwtRpcException(MSG.authenticationNoSession());
		
		context.checkPermissionAnyAuthority(session, Right.CourseCatalog);
		
		CourseDetailsProvider provider = Customization.CourseDetailsProvider.getProvider();
		if (provider == null)
			throw new GwtRpcException("Course details provider not configured.");
		CatalogResponse response = new CatalogResponse();
		response.setContent(provider.getDetails(new AcademicSessionInfo(session), request.getSubject(), request.getCourseNbr()));
		return response;
	}

	@Override
	public URL getCourseUrl(AcademicSessionInfo session, String subject, String courseNbr) throws SectioningException {
		try {
			return new URL(ApplicationProperty.UniTimeUrl.value() + "/catalog?term=" + URLEncoder.encode(session.toCompactString(), "utf-8")
				+ "&subjectCode=" + URLEncoder.encode(subject, "utf-8")
				+ "&courseNumber=" + URLEncoder.encode(courseNbr, "utf-8"));
		} catch (Exception e) {
			throw new SectioningException("Failed to get course URL: " + e.getMessage(), e);
		}
	}
}
