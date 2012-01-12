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
import org.unitime.timetable.security.roles.NoRole;

import com.google.inject.Inject;
import com.google.inject.Injector;

public class HasRoleInterceptor implements MethodInterceptor {
	@Inject Injector iInjector;
	
	public HasRoleInterceptor() {
	}

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		UserContext context = iInjector.getInstance(UserContext.class);
		if (!context.isAuthenticated() || context.getRole() == null || context.getRole() instanceof NoRole)
			throw new PageAccessException("Role check failed for method " + invocation.getMethod().getName() + " (no role)");
		return invocation.proceed();
	}
}
