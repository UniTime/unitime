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
package org.unitime.timetable.solver.ui;

import java.io.Serializable;

import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.comparators.ClassComparator;


/**
 * @author Tomas Muller
 */
public class UnassignedClassRow implements Serializable, Comparable {
	private static final long serialVersionUID = 1L;
	private String iOnClick = null;
	private String iName = null;
	private String iInstructor = null;
	private int iNrStudents = 0;
	private String iInitial = null;
	private int iOrd = -1;
	private transient Class_ iClazz = null;
	
	public UnassignedClassRow(String onClick, String name, String instructor, int nrStudents, String initial, int ord) {
		iOnClick = onClick;
		iName = name;
		iNrStudents = nrStudents;
		iInstructor = instructor;
		iInitial = initial;
		iOrd = ord;
	}
	
	public UnassignedClassRow(String onClick, String name, String instructor, int nrStudents, String initial, Class_ clazz) {
		iOnClick = onClick;
		iName = name;
		iNrStudents = nrStudents;
		iInstructor = instructor;
		iInitial = initial;
		iClazz = clazz;
	}

	public String getOnClick() { return iOnClick; }
	public String getName() { return iName; }
	public String getInstructor() { return iInstructor; }
	public int getNrStudents() { return iNrStudents; }
	public String getInitial() { return iInitial; }

	public int compareTo(Object o) {
		if (o==null || !(o instanceof UnassignedClassRow)) return -1;
		UnassignedClassRow ucr = (UnassignedClassRow)o;
		if (iOrd>=0 && ucr.iOrd>=0) {
			int cmp = Double.compare(iOrd, ucr.iOrd);
			if (cmp!=0) return cmp;
		} else if (iClazz!=null && ucr.iClazz!=null) {
			int cmp = (new ClassComparator(ClassComparator.COMPARE_BY_HIERARCHY)).compare(iClazz, ucr.iClazz);
			if (cmp!=0) return cmp;
		}
		return getName().compareTo(((UnassignedClassRow)o).getName());
	}
}
