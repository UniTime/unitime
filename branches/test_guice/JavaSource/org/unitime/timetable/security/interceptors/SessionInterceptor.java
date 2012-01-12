/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2012, UniTime LLC, and individual contributors
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
package org.unitime.timetable.security.interceptors;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.unitime.timetable.guice.context.UserContext;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.annotations.CheckSession;
import org.unitime.timetable.security.permissions.SimpleSessionPermission;
import org.unitime.timetable.security.rights.Right;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class SessionInterceptor implements MethodInterceptor {
	@Inject Injector iInjector;
	
	public SessionInterceptor() {
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		UserContext context = iInjector.getInstance(UserContext.class);
		SimpleSessionPermission permission = iInjector.getInstance(SimpleSessionPermission.class);
		CheckSession check = invocation.getMethod().getAnnotation(CheckSession.class);
		Session session = SessionDAO.getInstance().get(context.getAcademicSessionId());
		boolean match = false;
		for (Right right: check.right()) {
			if (permission.check(session, right)) {
				match = true;
				if (!check.all()) break;
			} else if (check.all()) {
				throw new PageAccessException("Session check failed for method " + invocation.getMethod().getName());
			}
		}
		if (!match)
			throw new PageAccessException("Session check failed for method " + invocation.getMethod().getName());
		return invocation.proceed();
	}
}
