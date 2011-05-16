/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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

import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.TimetableManager;

public abstract class BaseSolverGroup implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private String iAbbv;

	private Session iSession;
	private Set<TimetableManager> iTimetableManagers;
	private Set<Department> iDepartments;
	private Set<Solution> iSolutions;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_NAME = "name";
	public static String PROP_ABBV = "abbv";

	public BaseSolverGroup() {
		initialize();
	}

	public BaseSolverGroup(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public String getAbbv() { return iAbbv; }
	public void setAbbv(String abbv) { iAbbv = abbv; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public Set<TimetableManager> getTimetableManagers() { return iTimetableManagers; }
	public void setTimetableManagers(Set<TimetableManager> timetableManagers) { iTimetableManagers = timetableManagers; }
	public void addTotimetableManagers(TimetableManager timetableManager) {
		if (iTimetableManagers == null) iTimetableManagers = new HashSet<TimetableManager>();
		iTimetableManagers.add(timetableManager);
	}

	public Set<Department> getDepartments() { return iDepartments; }
	public void setDepartments(Set<Department> departments) { iDepartments = departments; }
	public void addTodepartments(Department department) {
		if (iDepartments == null) iDepartments = new HashSet<Department>();
		iDepartments.add(department);
	}

	public Set<Solution> getSolutions() { return iSolutions; }
	public void setSolutions(Set<Solution> solutions) { iSolutions = solutions; }
	public void addTosolutions(Solution solution) {
		if (iSolutions == null) iSolutions = new HashSet<Solution>();
		iSolutions.add(solution);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof SolverGroup)) return false;
		if (getUniqueId() == null || ((SolverGroup)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((SolverGroup)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "SolverGroup["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "SolverGroup[" +
			"\n	Abbv: " + getAbbv() +
			"\n	Name: " + getName() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
