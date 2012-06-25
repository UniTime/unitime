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
package org.unitime.timetable.security.authority;

import org.springframework.security.core.GrantedAuthority;
import org.unitime.timetable.security.UserAuthority;

public abstract class SimpleAuthority implements UserAuthority {
	private static final long serialVersionUID = 1L;
	private Long iUniqueId;
	private Long iAcademicSessionId;
	private String iReference;
	private String iLabel;
	private String iRole;
	private String iAuthority;
	
	public SimpleAuthority(Long uniqueId, Long academicSessionId, String role, String reference, String label) {
		iUniqueId = uniqueId;
		iAcademicSessionId = academicSessionId;
		iReference = reference;
		iLabel = label;
		iRole = role;
		iAuthority = (iRole + "_" + iReference).toUpperCase().replace(' ', '_');
	}
	
	@Override
	public Long getUniqueId() { return iUniqueId; }
	
	@Override
	public Long getAcademicSessionId() { return iAcademicSessionId; }
	
	@Override
	public String getReference() { return iReference; }
	
	@Override
	public String getLabel() { return iLabel; }
	
	@Override
	public String getRole() { return iRole; }
	
	@Override
	public String getAuthority() { return iAuthority; }
	
	public String toString() { return getAuthority(); }
	
	public int hashCode() { return getAuthority().hashCode(); }
	
	public boolean equals(Object o) {
		if (o == null || !(o instanceof GrantedAuthority)) return false;
		return getAuthority().equals(((GrantedAuthority)o).getAuthority());
	}
}
