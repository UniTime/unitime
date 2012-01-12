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
package org.unitime.timetable.guice.context;

import javax.annotation.Nullable;

import org.unitime.commons.User;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.security.roles.Role;
import org.unitime.timetable.security.roles.UniTimeRoles;
import org.unitime.timetable.util.Constants;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.servlet.RequestScoped;

@RequestScoped
public class UniTimeUserContext implements UserContext {
	private User iUser;
	private transient TimetableManager iManager;
	@Inject Injector iInjector;

	@Inject
	UniTimeUserContext(@Nullable User user) {
		iUser = user;
	}

	@Override
	public boolean isAuthenticated() {
		return (iUser != null);
	}
	
	@Override
	public String getId() {
		return (iUser == null ? null : iUser.getId());
	}

	@Override
	public String getName() {
		return (iUser == null ? null : iUser.getName());
	}
	
	@Override
	public Role getRole() {
		return iInjector.getInstance(iUser == null || iUser.getRole() == null ? UniTimeRoles.NO_ROLE.toRole() : UniTimeRoles.fromReference(iUser.getRole()));
	}

	public TimetableManager getManager() {
		if (iManager == null && iUser != null)
			iManager = TimetableManager.getManager(iUser);
		return iManager;
	}
	
	public Long getSessionId() {
		return (iUser == null ? null : (Long)iUser.getAttribute(Constants.SESSION_ID_ATTR_NAME));
	}

	@Override
	public Long getAcademicSessionId() {
		return iUser == null ? null : (Long)iUser.getAttribute(Constants.SESSION_ID_ATTR_NAME);
	}

	@Override
	public boolean hasDepartment(Long uniqueId) {
		if (getManager() == null) return false;
		for (Department dept: getManager().getDepartments())
			if (dept.getUniqueId().equals(uniqueId)) return true;
		return false;
	}
}
