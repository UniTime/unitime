/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2015, UniTime LLC, and individual contributors
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
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
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
		helper.getSessionContext().checkPermission(Right.ApiRetrieveRoles);

		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		List<Session> sessions = helper.getHibSession().createQuery("from Session order by academicInitiative, sessionBeginDateTime").list();
		List<SessionInfo> response = new ArrayList<SessionInfo>();

		String externalId = helper.getParameter("id");
		if (externalId == null || externalId.isEmpty()) {
			for (Session session: sessions) {
				if (session.getStatusType() == null || session.getStatusType().isTestSession()) continue;
				response.add(new SessionInfo(format, session, null, null));
			}
		} else {
			if (ApplicationProperty.ApiTrimLeadingZerosFromUserExternalIds.isTrue())
				while (externalId.startsWith("0")) externalId = externalId.substring(1);
			
			UniTimeUserContext context = new UniTimeUserContext(externalId, null, null, null);
			
			for (Session session: sessions) {
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
		}
		
		helper.setResponse(response);
	}
	
	class SessionInfo {
		Long iSessionId;
		String iReference;
		Boolean iSelected;
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
		
		SessionInfo(DateFormat format, Session session, List<String> roles, Boolean selected) {
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
			iSelected = selected;
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
					if (type.getType() == ExamType.sExamTypeFinal && status.canNoRoleReportExamFinal())
						iExams.add(type.getReference());
					if (type.getType() == ExamType.sExamTypeMidterm && status.canNoRoleReportExamMidterm())
						iExams.add(type.getReference());
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

	@Override
	protected String getName() {
		return "roles";
	}
}
