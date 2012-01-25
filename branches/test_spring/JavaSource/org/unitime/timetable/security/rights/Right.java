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
package org.unitime.timetable.security.rights;

import org.springframework.security.core.GrantedAuthority;

public enum Right {
	IsSystemAdmin,
	IsAdmin,
	
	ClassEdit,
	
	CurriculumAdd,
	CurriculumView,
	CurriculumEdit,
	CurriculumDelete,

	;
	
	GrantedAuthority iAuthority = null;
	private Right() { iAuthority = new Permission(this); }
	
	GrantedAuthority toAuthority() { return iAuthority; }
	
	private static class Permission implements GrantedAuthority {
		private static final long serialVersionUID = 1L;
		private String iPerm = null;
		
		public Permission(Right right) {
			iPerm = "PERM" + right.name().replaceAll("(\\p{Lu})", "_$1").toUpperCase();
		}
		
		@Override
		public String getAuthority() {
			return iPerm;
		}
	}

}
