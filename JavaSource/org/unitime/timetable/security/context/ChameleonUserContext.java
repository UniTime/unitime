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
package org.unitime.timetable.security.context;
import java.util.Iterator;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
public class ChameleonUserContext extends UniTimeUserContext implements UserContext.Chameleon {
	private static final long serialVersionUID = 1L;
	private UserContext iOriginalUser;
	
	public ChameleonUserContext(String userId, String userName, UserContext originalUser) {
		super(userId, originalUser.getUsername(), userName, null, originalUser.getCurrentAcademicSessionId());
		
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
