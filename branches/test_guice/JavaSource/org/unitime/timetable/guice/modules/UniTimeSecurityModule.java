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
package org.unitime.timetable.guice.modules;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.aopalliance.intercept.MethodInterceptor;
import org.unitime.timetable.security.annotations.CheckDepartment;
import org.unitime.timetable.security.annotations.CheckForPermission;
import org.unitime.timetable.security.annotations.CheckForRight;
import org.unitime.timetable.security.annotations.CheckForRole;
import org.unitime.timetable.security.annotations.CheckHasRole;
import org.unitime.timetable.security.annotations.CheckIsAuthenticated;
import org.unitime.timetable.security.annotations.CheckSession;
import org.unitime.timetable.security.interceptors.DepartmentInterceptor;
import org.unitime.timetable.security.interceptors.HasRoleInterceptor;
import org.unitime.timetable.security.interceptors.IsAuthenticatedInterceptor;
import org.unitime.timetable.security.interceptors.PermissionInterceptor;
import org.unitime.timetable.security.interceptors.RightInterceptor;
import org.unitime.timetable.security.interceptors.RoleInterceptor;
import org.unitime.timetable.security.interceptors.SessionInterceptor;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.matcher.Matcher;
import com.google.inject.matcher.Matchers;

public class UniTimeSecurityModule extends AbstractModule {
	private final Set<Object> iToBeInjected = new HashSet<Object>();
	private boolean iSelfInjectionInitialized = false;
	
	@Override
	protected void configure() {
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(CheckForPermission.class), new PermissionInterceptor());
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(CheckForRight.class), new RightInterceptor());
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(CheckForRole.class), new RoleInterceptor());
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(CheckHasRole.class), new HasRoleInterceptor());
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(CheckIsAuthenticated.class), new IsAuthenticatedInterceptor());
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(CheckSession.class), new SessionInterceptor());
		bindInterceptor(Matchers.any(), Matchers.annotatedWith(CheckDepartment.class), new DepartmentInterceptor());
	}
	
	@Override
	protected void bindInterceptor(Matcher<? super Class<?>> classMatcher, Matcher<? super Method> methodMatcher, MethodInterceptor... interceptors) {
		registerForInjection(interceptors);
		super.bindInterceptor(classMatcher, methodMatcher, interceptors);
	}
	
	protected <T> void registerForInjection(T... objects) {
		ensureSelfInjection();
		if (objects != null)
			for (T object : objects)
				if (object != null) iToBeInjected.add(object);
	}
	
	private void ensureSelfInjection() {
		if (!iSelfInjectionInitialized) {
			bind(UniTimeSecurityModule.class).toInstance(this);
			iSelfInjectionInitialized = true;
		}
	}
	
	 @Inject void injectRegisteredObjects(Injector injector) {
		 for (Object injectee : iToBeInjected)
			 injector.injectMembers(injectee);
	 }
}
