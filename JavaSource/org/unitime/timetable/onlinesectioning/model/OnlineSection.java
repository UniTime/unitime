/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning.model;

import net.sf.cpsolver.coursett.model.Placement;
import net.sf.cpsolver.studentsct.model.Section;
import net.sf.cpsolver.studentsct.model.Subpart;

/**
 * @author Tomas Muller
 */
public class OnlineSection extends Section {
	private int iEnrollment = 0;

	public OnlineSection(long id, int limit, String name, Subpart subpart, Placement placement, String instructorIds, String instructorNames, Section parent) {
		super(id, limit, name, subpart, placement, instructorIds, instructorNames, parent);
	}
	
	public void setEnrollment(int enrollment) { iEnrollment = enrollment; }
	public int getEnrollment() { return iEnrollment; }
}
