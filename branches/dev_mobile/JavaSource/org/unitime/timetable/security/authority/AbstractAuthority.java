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
package org.unitime.timetable.security.authority;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserQualifier;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.HasRights;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
public abstract class AbstractAuthority implements UserAuthority {
	private static final long serialVersionUID = 1L;
	private Long iUniqueId;
	private String iRole;
	private String iLabel;
	private List<UserQualifier> iQualifiers = new ArrayList<UserQualifier>();
	private UserQualifier iSession = null;
	private Set<Right> iRights = new HashSet<Right>();

	public AbstractAuthority(Long uniqueId, String role, String label, HasRights permissions) {
		iUniqueId = uniqueId;
		iRole = role;
		iLabel = label;
		for (Right right: Right.values())
			if (permissions.hasRight(right)) iRights.add(right);
	}
	
	@Override
	public Long getUniqueId() { return iUniqueId; }
	
	@Override
	public UserQualifier getAcademicSession() { return iSession; }
	
	@Override
	public String getLabel() { return iLabel; }
	
	@Override
	public String getRole() { return iRole; }
		
	@Override
	public String getAuthority() {
		UserQualifier session = getAcademicSession();
		return (getRole() + (session == null ? "" : "_" + session.getQualifierReference())).toUpperCase().replace(' ', '_');
	}
	
	@Override
	public boolean hasRight(Right right) {
		return iRights.contains(right);
	}
	
	public String toString() { return getAuthority() + " " + getQualifiers(); }
	
	public int hashCode() { return getAuthority().hashCode(); }
	
	public boolean equals(Object o) {
		if (o == null || !(o instanceof GrantedAuthority)) return false;
		return getAuthority().equals(((GrantedAuthority)o).getAuthority());
	}

	@Override
	public List<? extends UserQualifier> getQualifiers() {
		return iQualifiers;
	}

	@Override
	public List<? extends UserQualifier> getQualifiers(String type) {
		List<UserQualifier> ret = new ArrayList<UserQualifier>();
		for (UserQualifier q: getQualifiers())
			if (type == null || type.equals(q.getQualifierType()))
				ret.add(q);
		return ret;
	}
	
	@Override
	public boolean hasQualifier(Qualifiable qualifiable) {
		return getQualifier(qualifiable) != null;
	}

	@Override
	public UserQualifier getQualifier(Qualifiable qualifiable) {
		for (UserQualifier q: getQualifiers())
			if (q.equals(qualifiable)) return q;
		return null;
	}

	@Override
	public void addQualifier(UserQualifier qualifier) {
		if ("Session".equalsIgnoreCase(qualifier.getQualifierType()))
			iSession = qualifier;
		iQualifiers.add(qualifier);
	}

	@Override
	public void addQualifier(Qualifiable qualifiable) {
		addQualifier(new SimpleQualifier(qualifiable));
	}
}
