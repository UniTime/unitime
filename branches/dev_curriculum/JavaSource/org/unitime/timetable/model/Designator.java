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
package org.unitime.timetable.model;

import org.unitime.commons.User;
import org.unitime.timetable.model.base.BaseDesignator;




public class Designator extends BaseDesignator implements Comparable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Designator () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Designator (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	/**
	 * Checks whether current user can edit the designator list
	 */
	public boolean canUserEdit(User user) {
	    if (user.isAdmin())
	        return true;
	    
		TimetableManager tm = TimetableManager.getManager(user);
		if (tm == null) return false;
		
		if (!tm.getDepartments().contains(this.getSubjectArea().getDepartment()))
		    return false;
		
		DepartmentStatusType dst = this.getSubjectArea().getDepartment().effectiveStatusType();
		return (dst.canOwnerEdit() || dst.canOwnerLimitedEdit());
	}
	
	public int compareTo(Object o) {
		if (o==null || !(o instanceof Designator)) return -1;
		Designator d = (Designator)o;
		try {
			int c1 = Integer.parseInt(getCode());
			int c2 = Integer.parseInt(d.getCode());
			int cmp = (c1<c2?-1:c1>c2?1:0);
			if (cmp!=0) return cmp;
		} catch (NumberFormatException e) {
			int cmp = getCode().compareTo(d.getCode());
			if (cmp!=0) return cmp;
		}
		int cmp = getSubjectArea().compareTo(d.getSubjectArea());
		if (cmp!=0) return cmp;
		
		cmp = getInstructor().compareTo(d.getInstructor());
		if (cmp!=0) return cmp;
		
		return getUniqueId().compareTo(d.getUniqueId());
	}
    
    public String toString() {
        return getCode() + (getSubjectArea()==null?"":" ("+getSubjectArea().getSubjectAreaAbbreviation()+")");
    }
	
}
