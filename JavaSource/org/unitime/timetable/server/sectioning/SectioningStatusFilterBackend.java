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
package org.unitime.timetable.server.sectioning;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.client.sectioning.SectioningStatusFilterBox.SectioningStatusFilterRpcRequest;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.server.UniTimePrincipal;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.server.DatabaseServer;
import org.unitime.timetable.onlinesectioning.status.SectioningStatusFilterAction;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.ProxyHolder;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(SectioningStatusFilterRpcRequest.class)
public class SectioningStatusFilterBackend implements GwtRpcImplementation<SectioningStatusFilterRpcRequest, FilterRpcResponse> {
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private static Log sLog = LogFactory.getLog(SectioningStatusFilterBackend.class);
	
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
				if (context.getUser().getCurrentAuthority() != null)
					request.setOption("role", context.getUser().getCurrentAuthority().getRole());
			}

			if (online) {
				Long sessionId = getStatusPageSessionId(context);

				OnlineSectioningServer server = solverServerService.getOnlineStudentSchedulingContainer().getSolver(sessionId.toString());
				if (server == null) {
					ProxyHolder<Long, OnlineSectioningServer> h = (ProxyHolder<Long, OnlineSectioningServer>)context.getAttribute(SessionAttribute.OnlineSchedulingDummyServer);
					if (h != null && h.isValid(sessionId))
						server = h.getProxy();
					else {
						Session session = SessionDAO.getInstance().get(sessionId);
						if (session == null)
							throw new SectioningException(MSG.exceptionBadSession()); 
						server = new DatabaseServer(new AcademicSessionInfo(session), false);
						context.setAttribute(SessionAttribute.OnlineSchedulingDummyServer, new ProxyHolder<Long, OnlineSectioningServer>(sessionId, server));
					}
				}

				context.checkPermission(server.getAcademicSession().getUniqueId(), "Session", Right.SchedulingDashboard);
				request.setSessionId(server.getAcademicSession().getUniqueId());
				
				return server.execute(server.createAction(SectioningStatusFilterAction.class).forRequest(request)
						.withPinOps(online && context.hasPermission(sessionId, Right.StudentSchedulingCanReleasePin)),
						currentUser(context));
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
			Long sessionId = (Long)context.getAttribute(SessionAttribute.OnlineSchedulingLastSession);
			if (sessionId != null) return sessionId;
		} else {
			return user.getCurrentAcademicSessionId();
		}
		throw new SectioningException(MSG.exceptionNoAcademicSession());
	}
	
	private OnlineSectioningLog.Entity currentUser(SessionContext context) {
		UserContext user = context.getUser();
		UniTimePrincipal principal = (UniTimePrincipal)context.getAttribute(SessionAttribute.OnlineSchedulingUser);
		if (user != null) {
			return OnlineSectioningLog.Entity.newBuilder()
				.setExternalId(user.getTrueExternalUserId())
				.setName(user.getTrueName() == null ? user.getUsername() : user.getTrueName())
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
