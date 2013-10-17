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

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.base.BaseSpecialEvent;



/**
 * @author Tomas Muller
 */
public class SpecialEvent extends BaseSpecialEvent {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public SpecialEvent () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public SpecialEvent (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/

    public Set<Student> getStudents() {
        return new HashSet<Student>();
    }
    
    public Set<DepartmentalInstructor> getInstructors() {
        return new HashSet<DepartmentalInstructor>();
    }
    
    public int getEventType() { return sEventTypeSpecial; }

    public Collection<Long> getStudentIds() { return null; }

    @Override
	public Collection<StudentClassEnrollment> getStudentClassEnrollments() {
    	return null;
    }
}