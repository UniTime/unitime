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
package org.unitime.timetable.security;

import java.util.Collection;
import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;

public interface UserContext extends UserDetails {
	
	public String getExternalUserId();
	
	public String getName();
	
	public UserAuthority getCurrentAuthority();
	
	public void setCurrentAuthority(UserAuthority authority);
	
	public boolean hasAuthority(UserAuthority authority);
	
	public boolean hasAuthority(String type, String reference, Long academicSessionId);
	
	public UserAuthority getAuthority(String type, String reference, Long academicSessionId);
	
	public boolean hasAuthority(String type, Long uniqueId);
	
	public UserAuthority getAuthority(String type, Long uniqueId);
	
	public boolean hasAuthority(String authority);
	
	public UserAuthority getAuthority(String authority);
	
	@Override
	public Collection<? extends UserAuthority> getAuthorities();

	//TODO: Use getCurrentAuthority.getAcademicSessionId() instead
	@Deprecated
	public Long getCurrentAcademicSessionId();
	
	//TODO: Use getCurrentAuthority.getRole() instead
	@Deprecated
	public String getCurrentRole();

	//TODO: Use hasAuthority(DepartmentAuthority.TYPE, departmentId) instead
	@Deprecated
	public boolean hasDepartment(Long departmentId);
	
	//TODO: Use hasAuthority(role, null, getCurrentAuthority.getAcademicSessionId()) instead
	@Deprecated
	public boolean hasRole(String role);
	
	public String getProperty(String key);
	
	public void setProperty(String key, String value);
	
	public Map<String, String> getProperties();
	
	public static interface Chameleon {
		public UserContext getOriginalUserContext();
	}
}
