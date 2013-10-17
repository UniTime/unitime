/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2010 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.model.base;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ConstraintInfo;
import org.unitime.timetable.model.SolverInfo;

/**
 * @author Tomas Muller
 */
public abstract class BaseConstraintInfo extends SolverInfo implements Serializable {
	private static final long serialVersionUID = 1L;

	private Set<Assignment> iAssignments;


	public BaseConstraintInfo() {
		initialize();
	}

	public BaseConstraintInfo(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Set<Assignment> getAssignments() { return iAssignments; }
	public void setAssignments(Set<Assignment> assignments) { iAssignments = assignments; }
	public void addToassignments(Assignment assignment) {
		if (iAssignments == null) iAssignments = new HashSet<Assignment>();
		iAssignments.add(assignment);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ConstraintInfo)) return false;
		if (getUniqueId() == null || ((ConstraintInfo)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ConstraintInfo)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "ConstraintInfo["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ConstraintInfo[" +
			"\n	Definition: " + getDefinition() +
			"\n	Opt: " + getOpt() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Value: " + getValue() +
			"]";
	}
}
