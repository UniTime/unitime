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
package org.unitime.timetable.events;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface.EventRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingConflictInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.evaluation.UniTimePermissionCheck;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
public abstract class EventAction<T extends EventRpcRequest<R>, R extends GwtRpcResponse> implements GwtRpcImplementation<T, R> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	protected static Formats.Format<Date> sDateFormat = Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT);

	@Override
	public R execute(T request, SessionContext context) {
		// Check basic permissions
		checkPermission(request, context);
		
		// Execute action
		return execute(request, new EventContext(context, request.getSessionId()));
	}
	
	protected void checkPermission(T request, SessionContext context) {
		context.checkPermissionAnyAuthority(Right.Events, new SimpleQualifier("Session", request.getSessionId()));
	}

	public abstract R execute(T request, EventContext context);
	
	protected static Formats.Format<Date> getDateFormat() {
		return sDateFormat;
	}
	
	protected static String toString(MeetingInterface meeting) {
		return (meeting instanceof MeetingConflictInterface ? ((MeetingConflictInterface)meeting).getName() + " " : "") +
				(meeting.getMeetingDate() == null ? "" : getDateFormat().format(meeting.getMeetingDate()) + " ") +
				meeting.getAllocatedTime(CONSTANTS) + (meeting.hasLocation() ? " " + meeting.getLocationName() : "");
	}
	
	protected static String toString(Meeting meeting) {
		return (meeting.getMeetingDate() == null ? "" : getDateFormat().format(meeting.getMeetingDate()) + " ") +
				time2string(meeting.getStartPeriod(), 0) + " - " + time2string(meeting.getStopPeriod(), 0) +
				(meeting.getLocation() == null ? " " + meeting.getLocation().getLabel() : "");
	}
	
	protected static String time2string(int slot, int offset) {
		int min = 5 * slot + offset;
		if (min == 0) return CONSTANTS.timeMidnight();
		if (min == 720) return CONSTANTS.timeNoon();
		if (min == 1440) return CONSTANTS.timeMidnightEnd();
		int h = min / 60;
        int m = min % 60;
        if (CONSTANTS.useAmPm()) {
        	return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h == 24 ? "a" : h >= 12 ? "p" : "a");
		} else {
			return h + ":" + (m < 10 ? "0" : "") + m;
		}
	}
	
	public static class EventContext implements SessionContext {
		private SessionContext iContext;
		private Qualifiable[] iFilter;
		
		private Date iToday, iBegin, iEnd;
		
		private UserContext iUser;
		private boolean iAllowEditPast = false;
		
		public EventContext(SessionContext context, UserContext user, Long sessionId) {
			iContext = (context instanceof EventContext ? ((EventContext)context).iContext : context);
			iUser = user;
			
			if (sessionId == null)
				sessionId = context.getUser().getCurrentAcademicSessionId();
			
			iFilter = new Qualifiable[] { new SimpleQualifier("Session", sessionId) };
				
			Calendar cal = Calendar.getInstance(Localization.getJavaLocale());
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			iToday = cal.getTime();
			
			Session session = SessionDAO.getInstance().get(sessionId);
			if (session != null) {
				iBegin = session.getEventBeginDate();
				cal.setTime(session.getEventEndDate());
				cal.add(Calendar.DAY_OF_YEAR, 1);
				iEnd = cal.getTime();
			}
			
			if (user != null) {
				String role = (user.getCurrentAuthority() == null ? null : user.getCurrentAuthority().getRole()); 
				for (UserAuthority authority: user.getAuthorities()) {
					if (authority.getAcademicSession() != null && authority.getAcademicSession().getQualifierId().equals(sessionId) && (role == null || role.equals(authority.getRole()))) {
						iUser = new UniTimePermissionCheck.UserContextWrapper(user, authority);
						if (role != null) iFilter = new Qualifiable[] { new SimpleQualifier("Session", sessionId), new SimpleQualifier("Role", role) };
						break;
					}
				}
			}
			
			iAllowEditPast = context.hasPermission(Right.EventEditPast);
		}
		
		public EventContext(SessionContext context, Long sessionId) {
			this(context, context.getUser(), sessionId);
		}
		
		public boolean isOutside(Date date) {
			return date == null || (iBegin != null && date.before(iBegin)) || (iEnd != null && !date.before(iEnd));
		}
		public boolean isPast(Date date) {
			return !iAllowEditPast && (date == null || date.before(iToday));
		}
		public boolean isPastOrOutside(Date date) {
			return isPast(date) || isOutside(date);
		}

		@Override
		public boolean isAuthenticated() { return iUser != null; }
		@Override
		public UserContext getUser() { return iUser; }
		@Override
		public boolean isHttpSessionNew() { return iContext.isHttpSessionNew(); }
		@Override
		public String getHttpSessionId() { return iContext.getHttpSessionId(); }
		@Override
		public Object getAttribute(String name) { return iContext.getAttribute(name); }
		@Override
		public void removeAttribute(String name) { iContext.removeAttribute(name); }
		@Override
		public void setAttribute(String name, Object value) { iContext.setAttribute(name, value); }
		@Override
		public void removeAttribute(SessionAttribute attribute) { iContext.removeAttribute(attribute); }
		@Override
		public void setAttribute(SessionAttribute attribute, Object value) { iContext.setAttribute(attribute, value); }
		@Override
		public Object getAttribute(SessionAttribute attribute) { return iContext.getAttribute(attribute); }
		public PageAccessException getException() {
			if (iContext.isAuthenticated()) return new PageAccessException(MESSAGES.authenticationInsufficient());
			return new PageAccessException(iContext.isHttpSessionNew() ? MESSAGES.authenticationExpired() : MESSAGES.authenticationRequired());
		}
		@Override
		public void checkPermission(Right right) {
			if (!iContext.hasPermissionAnyAuthority(right, iFilter)) throw getException();
		}

		@Override
		public void checkPermission(Serializable targetId, String targetType, Right right) {
			if (!iContext.hasPermissionAnyAuthority(targetId, targetType, right, iFilter)) throw getException();
		}

		@Override
		public void checkPermission(Object targetObject, Right right) {
			if (!iContext.hasPermissionAnyAuthority(targetObject, right, iFilter)) throw getException();
		}

		@Override
		public boolean hasPermission(Right right) {
			return iContext.hasPermissionAnyAuthority(right, iFilter);
		}

		@Override
		public boolean hasPermission(Serializable targetId, String targetType, Right right) {
			return iContext.hasPermissionAnyAuthority(targetId, targetType, right, iFilter);
		}

		@Override
		public boolean hasPermission(Object targetObject, Right right) {
			return iContext.hasPermissionAnyAuthority(targetObject, right, iFilter);
		}

		@Override
		public boolean hasPermissionAnyAuthority(Right right, Qualifiable... filter) {
			return iContext.hasPermissionAnyAuthority(right, filter == null || filter.length == 0 ? iFilter : filter);
		}
		@Override
		public boolean hasPermissionAnyAuthority(Serializable targetId, String targetType, Right right, Qualifiable... filter) {
			return iContext.hasPermissionAnyAuthority(targetId, targetType, right, filter == null || filter.length == 0 ? iFilter : filter);
		}
		@Override
		public boolean hasPermissionAnyAuthority(Object targetObject, Right right, Qualifiable... filter) {
			return iContext.hasPermissionAnyAuthority(targetObject, right, filter == null || filter.length == 0 ? iFilter : filter);
		}

		@Override
		public void checkPermissionAnyAuthority(Right right, Qualifiable... filter) {
			iContext.checkPermissionAnyAuthority(right, filter == null || filter.length == 0 ? iFilter : filter);
		}

		@Override
		public void checkPermissionAnyAuthority(Serializable targetId, String targetType, Right right, Qualifiable... filter) {
			iContext.checkPermissionAnyAuthority(targetId, targetType, right, filter == null || filter.length == 0 ? iFilter : filter);
		}

		@Override
		public void checkPermissionAnyAuthority(Object targetObject, Right right, Qualifiable... filter) {
			iContext.checkPermissionAnyAuthority(targetObject, right, filter == null || filter.length == 0 ? iFilter : filter);
		}
		
	}

}
