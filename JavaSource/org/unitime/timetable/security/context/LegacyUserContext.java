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
package org.unitime.timetable.security.context;

import java.util.List;

import org.unitime.commons.User;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.authority.RoleAuthority;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.util.Constants;

public class LegacyUserContext extends UniTimeUserContext {
	private User iUser;
	private static final long serialVersionUID = 1L;
	
	public LegacyUserContext(User user) {
		super(user.getId(), user.getLogin(), user.getName(), null);
		iUser = user;
		Long sessionId = (Long)user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
		if (sessionId != null && user.getRole() != null) {
			List<? extends UserAuthority> authorities = getAuthorities(user.getCurrentRole(), new SimpleQualifier("Session", sessionId));
			if (!authorities.isEmpty())
				setCurrentAuthority(authorities.get(0));
		}
	}
	
	@Override
	public void setCurrentAuthority(UserAuthority authority) {
		super.setCurrentAuthority(authority);
		iUser.setAttribute(Constants.SESSION_ID_ATTR_NAME, authority.getAcademicSession().getQualifierId());
		if (authority instanceof RoleAuthority)
			iUser.setRole(authority.getRole());
	}

}
