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
package org.unitime.timetable.api.connectors;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.unitime.timetable.api.ApiConnector;
import org.unitime.timetable.api.ApiHelper;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.ExamStatus;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.context.UniTimeUserContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("/api/roles")
public class RolesConnector extends ApiConnector {
	
	@Override
	public void doGet(ApiHelper helper) throws IOException {
		String externalId = helper.getParameter("id");
		if (externalId == null || externalId.isEmpty())
			throw new IllegalArgumentException("Parameter ID not provided");
		if (ApplicationProperty.ApiTrimLeadingZerosFromUserExternalIds.isTrue())
			while (externalId.startsWith("0")) externalId = externalId.substring(1);
		
		helper.getSessionContext().checkPermission(Right.ApiRetrieveRoles);
		
		List<SessionInfo> response = new ArrayList<SessionInfo>();
		UniTimeUserContext context = new UniTimeUserContext(externalId, null, null, null);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
		for (Session session: (List<Session>)hibSession.createQuery("from Session order by academicInitiative, sessionBeginDateTime").list()) {
			if (session.getStatusType() == null || session.getStatusType().isTestSession()) continue;
			List<String> roles = new ArrayList<String>();
			for (UserAuthority authority: context.getAuthorities(null, session)) {
				if (Roles.ROLE_NONE.equals(authority.getRole())) continue;
				if (Roles.ROLE_ANONYMOUS.equals(authority.getRole())) continue;
				roles.add(authority.getRole());
			}
			if (roles.isEmpty()) continue;
			response.add(new SessionInfo(format, session, roles, session.getUniqueId().equals(context.getCurrentAcademicSessionId())));
		}
		
		helper.setResponse(response);
	}
	
	class SessionInfo {
		Long iSessionId;
		String iReference;
		boolean iSelected;
		String iYear;
		String iTerm;
		String iCampus;
		String iExternalTerm;
		String iExternalCampus;
		String iBeginDate;
		String iEndDate;
		String iClassEndDate;
		String iExamBeginDate;
		String iEventBeginDate;
		String iEventEndDate;
		StatusInfo iStatus;
		List<String> iRoles;
		
		SessionInfo(DateFormat format, Session session, List<String> roles, boolean selected) {
			iSessionId = session.getUniqueId();
			iReference = session.getReference();
			iYear = session.getAcademicYear();
			iTerm = session.getAcademicTerm();
			iCampus = session.getAcademicInitiative();
			iBeginDate = format.format(session.getSessionBeginDateTime());
			iEndDate = format.format(session.getSessionEndDateTime());
			iClassEndDate = format.format(session.getClassesEndDateTime());
			iExamBeginDate = format.format(session.getExamBeginDate());
			iEventBeginDate = format.format(session.getEventBeginDate());
			iEventEndDate = format.format(session.getEventEndDate());
			if (session.getStatusType() != null) {
				iStatus = new StatusInfo(session);
			}
			iRoles = roles;
		}
	}
	
	class StatusInfo {
		String iRefenrece;
		String iLabel;
		Boolean iClasses;
		List<String> iExams;
		String iStudentScheduling;
		
		StatusInfo(Session session) {
			DepartmentStatusType status = session.getStatusType();
			if (status != null) {
				iRefenrece = status.getReference();
				iLabel = status.getLabel();
				iClasses = status.canNoRoleReportClass();
				iExams = new ArrayList<String>();
				for (ExamType type: ExamType.findAllUsed(session.getUniqueId())) {
					ExamStatus examStatus = ExamStatus.findStatus(session.getUniqueId(), type.getUniqueId());
					if (examStatus != null) {
						if (type.getType() == ExamType.sExamTypeFinal && examStatus.effectiveStatus().canNoRoleReportExamFinal())
							iExams.add(type.getReference());
						if (type.getType() == ExamType.sExamTypeMidterm && examStatus.effectiveStatus().canNoRoleReportExamMidterm())
							iExams.add(type.getReference());
					} else {
						if (type.getType() == ExamType.sExamTypeFinal && status.canNoRoleReportExamFinal())
							iExams.add(type.getReference());
						if (type.getType() == ExamType.sExamTypeMidterm && status.canNoRoleReportExamMidterm())
							iExams.add(type.getReference());
					}
				}
				if (status.canOnlineSectionStudents())
					iStudentScheduling = "online";
				else if (status.canSectionAssistStudents())
					iStudentScheduling = "assistant";
				else if (status.canPreRegisterStudents())
					iStudentScheduling = "registration";
			}
		}
	}

}
