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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.authority.DepartmentAuthority;
import org.unitime.timetable.security.rights.Right;

public abstract class AbstractUserContext implements UserContext {
	private static final long serialVersionUID = 1L;
	private UserAuthority iCurrentAuthority = null;
	private Set<UserAuthority> iAuthorities = new HashSet<UserAuthority>();
	private Map<String, String> iProperties = new HashMap<String, String>();

	@Override
	public String getPassword() { return null; }

	@Override
	public boolean isAccountNonExpired() { return true; }

	@Override
	public boolean isAccountNonLocked() { return true; }

	@Override
	public boolean isCredentialsNonExpired() { return true; }

	@Override
	public boolean isEnabled() { return true; }

	@Override
	public UserAuthority getCurrentAuthority() { return iCurrentAuthority; }

	@Override
	public void setCurrentAuthority(UserAuthority authority) {
		if (hasAuthority(authority) && authority.hasRight(Right.CanSelectAsCurrentRole))
			iCurrentAuthority = authority;
		else
			throw new RuntimeException("Invalid authority.");
	}

	@Override
	public boolean hasAuthority(UserAuthority authority) {
		return getAuthorities().contains(authority);
	}

	@Override
	public boolean hasAuthority(String type, String reference, Long academicSessionId) {
		return getAuthority(type, reference, academicSessionId) != null;
	}

	@Override
	public UserAuthority getAuthority(String type, String reference, Long academicSessionId) {
		for (UserAuthority authority: getAuthorities())
			if (type.equals(authority.getRole()) && 
				(reference == null || reference.equals(authority.getReference())) &&
				(academicSessionId == null || academicSessionId.equals(authority.getAcademicSessionId())))
				return authority;
		return null;
	}
	
	@Override
	public boolean hasAuthority(String type, Long uniqueId) {
		return getAuthority(type, uniqueId) != null;
	}

	@Override
	public UserAuthority getAuthority(String type, Long uniqueId) {
		for (UserAuthority authority: getAuthorities())
			if (type.equals(authority.getRole()) && uniqueId.equals(authority.getUniqueId()))
				return authority;
		return null;
	}
	
	@Override
	public boolean hasAuthority(String authority) {
		return getAuthority(authority) != null;
	}
	
	public UserAuthority getAuthority(String authority) {
		for (UserAuthority a: getAuthorities())
			if (authority.equals(a.getAuthority())) return a;
		return null;
	}

	@Override
	public Long getCurrentAcademicSessionId() {
		return (getCurrentAuthority() == null ? null : getCurrentAuthority().getAcademicSessionId());
	}

	@Override
	public String getCurrentRole() {
		return (getCurrentAuthority() == null ? null : getCurrentAuthority().getRole());
	}

	@Override
	public boolean hasDepartment(Long departmentId) {
		return hasAuthority(DepartmentAuthority.TYPE, departmentId);
	}

	@Override
	public boolean hasRole(String role) {
		return hasAuthority(role, null, getCurrentAcademicSessionId());
	}

	@Override
	public Collection<? extends UserAuthority> getAuthorities() {
		return iAuthorities;
	}
	
	protected void addAuthority(UserAuthority authority) {
		iAuthorities.add(authority);
	}

	@Override
	public String getProperty(String key) {
		return iProperties.get(key);
	}

	@Override
	public void setProperty(String key, String value) {
		if (value == null) {
			iProperties.remove(key);
		} else {
			iProperties.put(key, value);
		}
	}

	@Override
	public Map<String, String> getProperties() {
		return iProperties;
	}
}
