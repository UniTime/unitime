/*
 * UniTime 3.5 (University Timetabling Application)
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
package org.unitime.timetable.server.sectioning;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.client.sectioning.SectioningStatusFilterBox.SectioningStatusFilterRpcRequest;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.UniTimePrincipal;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.status.SectioningStatusFilterAction;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(SectioningStatusFilterRpcRequest.class)
public class SectioningStatusFilterBackend implements GwtRpcImplementation<SectioningStatusFilterRpcRequest, FilterRpcResponse> {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private static Logger sLog = Logger.getLogger(SectioningStatusFilterBackend.class);
	
	private @Autowired SolverService<StudentSolverProxy> studentSectioningSolverService;
	
	private @Autowired SolverServerService solverServerService;
	
	@Override
	public FilterRpcResponse execute(SectioningStatusFilterRpcRequest request, SessionContext context) {
		try {
			boolean online = "true".equals(request.getOption("online"));
			
			if (context.isAuthenticated()) {
				request.setOption("user", context.getUser().getExternalUserId());
				if (context.getUser().getCurrentAuthority() != null && context.getUser().getCurrentAuthority().hasRight(Right.ConsentApproval))
					request.setOption("approval", "true");
			}

			if (online) {
				Long sessionId = getStatusPageSessionId(context);

				OnlineSectioningServer server = solverServerService.getOnlineStudentSchedulingContainer().getSolver(sessionId.toString());
				if (server == null)
					throw new SectioningException(MSG.exceptionBadSession());

				context.checkPermission(server.getAcademicSession().getUniqueId(), "Session", Right.SchedulingDashboard);
				request.setSessionId(server.getAcademicSession().getUniqueId());
				
				return server.execute(server.createAction(SectioningStatusFilterAction.class).forRequest(request), currentUser(context));				
			} else {
				OnlineSectioningServer server = studentSectioningSolverService.getSolver();
				if (server == null) 
					throw new SectioningException(MSG.exceptionNoSolver());
				
				context.checkPermission(server.getAcademicSession().getUniqueId(), "Session", Right.StudentSectioningSolverDashboard);
				request.setSessionId(server.getAcademicSession().getUniqueId());

				return server.execute(server.createAction(SectioningStatusFilterAction.class).forRequest(request), currentUser(context));				
			}
		} catch (PageAccessException e) {
			throw e;
		} catch (SectioningException e) {
			throw e;
		} catch  (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new SectioningException(MSG.exceptionUnknown(e.getMessage()), e);
		}
	}
	
	private Long getStatusPageSessionId(SessionContext context) throws SectioningException, PageAccessException {
		UserContext user = context.getUser();
		if (user == null)
			throw new PageAccessException(context.isHttpSessionNew() ? MSG.exceptionHttpSessionExpired() : MSG.exceptionLoginRequired());
		if (user.getCurrentAcademicSessionId() == null) {
			Long sessionId = getLastSessionId(context);
			if (sessionId != null) return sessionId;
		} else {
			return user.getCurrentAcademicSessionId();
		}
		throw new SectioningException(MSG.exceptionNoAcademicSession());
	}
	
	public Long getLastSessionId(SessionContext context) {
		Long lastSessionId = (Long)context.getAttribute("sessionId");
		if (lastSessionId == null) {
			UserContext user = context.getUser();
			if (user != null) {
				Long sessionId = user.getCurrentAcademicSessionId();
				if (sessionId != null)
					lastSessionId = sessionId;
			}
		}
		return lastSessionId;
	}
	
	private OnlineSectioningLog.Entity currentUser(SessionContext context) {
		UserContext user = context.getUser();
		UniTimePrincipal principal = (UniTimePrincipal)context.getAttribute("user");
		if (user != null) {
			return OnlineSectioningLog.Entity.newBuilder()
				.setExternalId(user.getExternalUserId())
				.setName(user.getName() == null ? user.getUsername() : user.getName())
				.setType(context.hasPermission(Right.StudentSchedulingAdvisor) ?
						 OnlineSectioningLog.Entity.EntityType.MANAGER : OnlineSectioningLog.Entity.EntityType.STUDENT).build();
		} else if (principal != null) {
			return OnlineSectioningLog.Entity.newBuilder()
				.setExternalId(principal.getExternalId())
				.setName(principal.getName())
				.setType(OnlineSectioningLog.Entity.EntityType.STUDENT).build();
		} else {
			return null;
		}
		
	}

}
