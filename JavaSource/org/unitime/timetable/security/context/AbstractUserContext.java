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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.UserQualifier;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;

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
		if (hasAuthority(authority))
			iCurrentAuthority = authority;
		else
			throw new RuntimeException("Invalid authority.");
	}

	@Override
	public boolean hasAuthority(UserAuthority authority) {
		return getAuthorities().contains(authority);
	}

	@Override
	public boolean hasAuthority(String role, Long uniqueId) {
		return getAuthority(role, uniqueId) != null;
	}

	@Override
	public UserAuthority getAuthority(String role, Long uniqueId) {
		UserQualifier session = (getCurrentAuthority() == null ? null : getCurrentAuthority().getAcademicSession());
		for (UserAuthority authority: getAuthorities()) {
			if (role != null && !role.equals(authority.getRole())) continue;
			if (uniqueId != null && uniqueId.equals(authority.getUniqueId()))
				return authority;
			if (uniqueId == null && session != null && session.equals(authority.getAcademicSession()))
				return authority;
		}
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
		return (Long)(getCurrentAuthority() == null || getCurrentAuthority().getAcademicSession() == null ? null : getCurrentAuthority().getAcademicSession().getQualifierId());
	}

	@Override
	public boolean hasDepartment(Long departmentId) {
		return getCurrentAuthority() == null ? false : getCurrentAuthority().hasQualifier(new SimpleQualifier(Department.class.getSimpleName(), departmentId));
	}

	@Override
	public boolean hasRole(String role) {
		return hasAuthority(role, null);
	}

	@Override
	public Collection<? extends UserAuthority> getAuthorities() {
		return iAuthorities;
	}
	
	@Override
	public List<? extends UserAuthority> getAuthorities(String role, Qualifiable... filter) {
		List<UserAuthority> ret = new ArrayList<UserAuthority>();
		authorities: for (UserAuthority authority: getAuthorities()) {
			if (role != null && !role.equals(authority.getRole())) continue authorities;
			for (Qualifiable q: filter)
				if (!authority.hasQualifier(q)) continue authorities;
			ret.add(authority);
		}
		return ret;
	}

	protected void addAuthority(UserAuthority authority) {
		authority.addQualifier(new SimpleQualifier("Role", authority.getRole()));
		iAuthorities.add(authority);
	}

	@Override
	public String getProperty(String key) {
		return iProperties.get(key);
	}
	
	@Override
	public String getProperty(String key, String defaultValue) {
		String value = getProperty(key);
		return (value != null ? value : defaultValue);
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
	
	@Override
	public String getProperty(UserProperty property) {
		return getProperty(property.key(), property.defaultValue());
	}
	
	@Override
	public void setProperty(UserProperty property, String value) {
		setProperty(property.key(), value);
	}

}
