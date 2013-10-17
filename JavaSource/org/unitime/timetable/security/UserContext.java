/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
 * Copyright (C) 2012 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;
import org.unitime.timetable.defaults.UserProperty;

/**
 * @author Tomas Muller
 */
public interface UserContext extends UserDetails {
	
	public String getExternalUserId();
	
	public String getName();
	
	public String getEmail();
	
	public UserAuthority getCurrentAuthority();
	
	public void setCurrentAuthority(UserAuthority authority);
	
	public boolean hasAuthority(UserAuthority authority);
	
	public boolean hasAuthority(String role, Long uniqueId);
	
	public UserAuthority getAuthority(String role, Long uniqueId);
	
	public boolean hasAuthority(String authority);
	
	public UserAuthority getAuthority(String authority);
	
	@Override
	public Collection<? extends UserAuthority> getAuthorities();

	public List<? extends UserAuthority> getAuthorities(String role, Qualifiable... filter);

	public Long getCurrentAcademicSessionId();
	
	public boolean hasDepartment(Long departmentId);
	
	public boolean hasRole(String role);
	
	public String getProperty(String key);
	
	public String getProperty(String key, String defaultValue);
	
	public void setProperty(String key, String value);
	
	public String getProperty(UserProperty property);
	
	public void setProperty(UserProperty property, String value);
	
	public Map<String, String> getProperties();
	
	public static interface Chameleon {
		public UserContext getOriginalUserContext();
	}
}
