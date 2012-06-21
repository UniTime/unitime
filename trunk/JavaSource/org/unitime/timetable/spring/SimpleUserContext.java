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
*/package org.unitime.timetable.spring;

import java.util.HashMap;
import java.util.Map;

public class SimpleUserContext implements UserContext {
	private String iId, iName, iRole;
	private Long iSessionId;
	private Map<String, String> iProperties = new HashMap<String, String>();

	@Override
	public String getExternalUserId() { return iId; }
	
	public void setExternalUsetId(String id) { iId = id; }

	@Override
	public String getName() { return iName; }
	
	public void setName(String name) { iName = name; }

	@Override
	public Long getCurrentAcademicSessionId() { return iSessionId; }
	
	public void setCurrentAcademicSessionId(Long sessioId) { iSessionId = sessioId; }

	@Override
	public String getCurrentRole() { return iRole; }
	
	public void setCurrentRole(String role) { iRole = role; }

	@Override
	public boolean hasRole(String role) { return role == null ? iRole == null : role.equals(iRole); }

	@Override
	public String getProperty(String key) { return iProperties.get(key); }

	@Override
	public void setProperty(String key, String value) {
		if (value == null)
			iProperties.remove(key);
		else
			iProperties.put(key, value);
	}

	@Override
	public Map<String, String> getProperties() { return iProperties; }

	@Override
	public boolean hasDepartment(Long departmentId) { return false; }
}
