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

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.unitime.timetable.security.rights.HasRights;

/**
 * @author Tomas Muller
 */
public interface UserAuthority extends GrantedAuthority, HasRights {
	public Long getUniqueId();
	public String getRole();
	public String getLabel();
	
	public UserQualifier getAcademicSession();
	
	public List<? extends UserQualifier> getQualifiers();
	public List<? extends UserQualifier> getQualifiers(String type);
	public boolean hasQualifier(Qualifiable qualifiable);
	public UserQualifier getQualifier(Qualifiable qualifiable);
	public void addQualifier(UserQualifier qualifier);
	public void addQualifier(Qualifiable qualifiable);
}