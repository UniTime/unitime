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
package org.unitime.timetable.model.comparators;

import java.util.Comparator;

import org.unitime.timetable.model.ClassInstructor;

public class ClassInstructorComparator implements Comparator {
	private ClassComparator iCC = null;
	public ClassInstructorComparator(ClassComparator cc) {
		iCC = cc;
	}
	
	public int compare(Object o1, Object o2) {
		ClassInstructor ci1 = (ClassInstructor)o1;
		ClassInstructor ci2 = (ClassInstructor)o2;
		return iCC.compare(ci1.getClassInstructing(),ci2.getClassInstructing());
	}
}
