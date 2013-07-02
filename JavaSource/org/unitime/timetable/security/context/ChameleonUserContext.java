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
import java.util.Iterator;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;

public class ChameleonUserContext extends UniTimeUserContext implements UserContext.Chameleon {
	private static final long serialVersionUID = 1L;
	private UserContext iOriginalUser;
	
	public ChameleonUserContext(String userId, UserContext originalUser) {
		super(userId, originalUser.getUsername(), originalUser.getName(), null);
		
		// Original user is session dependent -> remove all session independent authorities from the new user
		if (originalUser.getCurrentAuthority() == null || !originalUser.getCurrentAuthority().hasRight(Right.SessionIndependent)) {
			for (Iterator<? extends UserAuthority> i = getAuthorities().iterator(); i.hasNext(); ) {
				UserAuthority authority = i.next();
				if (authority.hasRight(Right.SessionIndependent))
					i.remove();
			}
			if (getCurrentAuthority() != null && getCurrentAuthority().hasRight(Right.SessionIndependent)) {
				List<? extends UserAuthority> authorities = getAuthorities(null, new SimpleQualifier("Session", originalUser.getCurrentAcademicSessionId()));
				if (!authorities.isEmpty())
					setCurrentAuthority(authorities.get(0));
				else
					throw new AccessDeniedException("Access denied for " + super.getName().trim() + ": not enough permissions for role " + getCurrentAuthority().getRole() + ".");
			}
			if (getAuthorities().isEmpty())
				throw new AccessDeniedException("Access denied for " + super.getName().trim() + ": no role available.");
		}
		
		iOriginalUser = originalUser;
		if (iOriginalUser instanceof UserContext.Chameleon)
			iOriginalUser = ((UserContext.Chameleon)iOriginalUser).getOriginalUserContext();
		if (originalUser.getCurrentAuthority() != null) {
			UserAuthority authority = getAuthority(originalUser.getCurrentAuthority().getAuthority());
			if (authority != null)
				setCurrentAuthority(authority);
		}
	}
	
	@Override
	public UserContext getOriginalUserContext() { return iOriginalUser; }
	
	@Override
	public String getName() {
		return super.getName() + " (A)";
	}

}
