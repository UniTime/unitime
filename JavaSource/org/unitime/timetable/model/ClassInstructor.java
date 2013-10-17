/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2008 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model;

import org.unitime.timetable.model.base.BaseClassInstructor;



/**
 * @author Stephanie Schluttenhofer, Tomas Muller
 */
public class ClassInstructor extends BaseClassInstructor implements Comparable {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ClassInstructor () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ClassInstructor (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

	public String nameLastNameFirst(){
		if (this.getInstructor() != null){
			return(this.getInstructor().nameLastNameFirst());
		} else {
			return(new String());
		}
	}
	
	public String nameFirstNameFirst() {
		if (this.getInstructor() != null){
			return(this.getInstructor().nameFirstNameFirst());
		} else {
			return(new String());
		}
	}

    public int compareTo(Object o) {
        if (o==null || !(o instanceof ClassInstructor)) return -1;
        ClassInstructor i = (ClassInstructor)o;
        int cmp = nameLastNameFirst().compareToIgnoreCase(i.nameLastNameFirst());
        if (cmp!=0) return cmp;
        return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(i.getUniqueId() == null ? -1 : i.getUniqueId());
    }
    
    public String toString(){
    	return(nameLastNameFirst());
    }
}
