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
package org.unitime.timetable.server;

import java.net.URL;

import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.sectioning.CourseDetailsWidget.CourseDetailsRpcRequest;
import org.unitime.timetable.gwt.client.sectioning.CourseDetailsWidget.CourseDetailsRpcResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.custom.CourseDetailsProvider;
import org.unitime.timetable.onlinesectioning.custom.CourseUrlProvider;
import org.unitime.timetable.security.SessionContext;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(CourseDetailsRpcRequest.class)
public class CourseDetailsBackend implements GwtRpcImplementation<CourseDetailsRpcRequest, CourseDetailsRpcResponse> {

	@Override
	public CourseDetailsRpcResponse execute(CourseDetailsRpcRequest request, SessionContext context) {
		org.hibernate.Session hibSession = CourseOfferingDAO.getInstance().getSession();
		
		AcademicSessionInfo session = null;
		String subject = null, course = null;
		if (request.hasCourseId()) {
			CourseOffering courseOffering = (request.getCourseId() < 0 
					? InstructionalOfferingDAO.getInstance().get(-request.getCourseId(), hibSession).getControllingCourseOffering()
					: CourseOfferingDAO.getInstance().get(request.getCourseId(), hibSession));
			session = new AcademicSessionInfo(courseOffering.getSubjectArea().getSession());
			subject = courseOffering.getSubjectAreaAbbv();
			course = courseOffering.getCourseNbr();
		} else {
			SubjectArea subjectArea = SubjectAreaDAO.getInstance().get(request.getSubjectId(), hibSession);
			session = new AcademicSessionInfo(subjectArea.getSession());
			subject = subjectArea.getSubjectAreaAbbreviation();
			course = request.getCourseNumber();
		}
		
		CourseDetailsRpcResponse response = new CourseDetailsRpcResponse();
		response.setDetails(getCourseDetails(session, subject, course));
		URL url = getCourseUrl(session, subject, course);
		if (url != null) response.setLink(url.toString());
		
		return response;
	}
	
	public static String getCourseDetails(AcademicSessionInfo session, String subject, String courseNbr) {
		try {
			String providerClass = ApplicationProperty.CustomizationCourseDetails.value();
			if (providerClass != null)
				return ((CourseDetailsProvider)Class.forName(providerClass).newInstance()).getDetails(session, subject, courseNbr);
		} catch (Exception e) {}
		return null;
	}
	
	public static URL getCourseUrl(AcademicSessionInfo session, String subject, String courseNbr) {
		try {
			String providerClass = ApplicationProperty.CustomizationCourseLink.value();
			if (providerClass != null)
				return ((CourseUrlProvider)Class.forName(providerClass).newInstance()).getCourseUrl(session, subject, courseNbr);
		} catch (Exception e) {}
		return null;
	}

}
