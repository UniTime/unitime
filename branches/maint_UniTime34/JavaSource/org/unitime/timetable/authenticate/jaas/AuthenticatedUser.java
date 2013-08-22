/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2008 - 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.authenticate.jaas;

import java.io.Serializable;
import java.security.Principal;

/**
 * Represents an authenticated and authorized timetable user
 */
final public class AuthenticatedUser implements Principal, Serializable, HasExternalId {
	private static final long serialVersionUID = 11L;

	String iName, iExternalId;

	public AuthenticatedUser(String name, String externalId) {
		iName = name;
		iExternalId = externalId;
	}

	public boolean equals(Object obj) {
		return getName().equals(((Principal)obj).getName());
	}

	public int hashCode() {
		return getName().hashCode();
	}

	public String toString() {
		return getName();
	}

	public String getName() {
		return iName;
	}
	
	@Override
	public String getExternalId() {
		return iExternalId;
	}
}