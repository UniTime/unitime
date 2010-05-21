/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2008, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.authenticate.jaas;



/**
 * Represents an authenticated and authorized timetable user
 * @author Heston Fernandes
 */
final public class TimetablePrincipal 
	implements java.security.Principal, java.io.Serializable {
	
	private static final long serialVersionUID = 11L;
	
	/**
     * @serial
     */
	String name = "";
	
	public TimetablePrincipal() {
		super();
	}

	public TimetablePrincipal(String name) {
		if (name == null)
		    throw new NullPointerException("Invalid Principal Name");

		setName(name);
	}

	private void setName(String name) {
		this.name = name;
	}

	public boolean equals(Object obj) {
		return getName().equals( ((TimetablePrincipal)obj).getName() );
	}

	public int hashCode() {
		return getName().hashCode();
	}

	public String toString() {		
		return getName();
	}

	public String getName() {
		return name;
	}
	
}
