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
*/package org.unitime.timetable.security.context;

import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.authority.SimpleAuthority;
import org.unitime.timetable.security.rights.Right;

public class SimpleUserContext extends AbstractUserContext {
	private static final long serialVersionUID = 1L;
	private String iId, iName;
	
	@Override
	public String getExternalUserId() { return iId; }
	
	public void setExternalUsetId(String id) { iId = id; }

	@Override
	public String getName() { return iName; }
	
	public void setName(String name) { iName = name; }

	@Override
	public String getPassword() { return null; }

	@Override
	public String getUsername() { return null; }
	
	public void setCurrentRole(String role, Long sessionId) {
		UserAuthority auth = new SimpleAuthority(0l, sessionId, role, "", role) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean hasRight(Right right) {
				switch (right) {
				case CanSelectAsCurrentRole:
					return true;
				default:
					return false;
				}
			}
		};
		addAuthority(auth);
		setCurrentAuthority(auth);
	}
}
