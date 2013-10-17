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

import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Session;

/**
 * @author Tomas Muller
 */
public abstract class BaseDatePattern implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private String iPattern;
	private Integer iOffset;
	private Integer iType;
	private Boolean iVisible;

	private Session iSession;
	private Set<DatePattern> iParents;
	private Set<Department> iDepartments;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_NAME = "name";
	public static String PROP_PATTERN = "pattern";
	public static String PROP_OFFSET = "offset";
	public static String PROP_TYPE = "type";
	public static String PROP_VISIBLE = "visible";

	public BaseDatePattern() {
		initialize();
	}

	public BaseDatePattern(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public String getPattern() { return iPattern; }
	public void setPattern(String pattern) { iPattern = pattern; }

	public Integer getOffset() { return iOffset; }
	public void setOffset(Integer offset) { iOffset = offset; }

	public Integer getType() { return iType; }
	public void setType(Integer type) { iType = type; }

	public Boolean isVisible() { return iVisible; }
	public Boolean getVisible() { return iVisible; }
	public void setVisible(Boolean visible) { iVisible = visible; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public Set<DatePattern> getParents() { return iParents; }
	public void setParents(Set<DatePattern> parents) { iParents = parents; }
	public void addToparents(DatePattern datePattern) {
		if (iParents == null) iParents = new HashSet<DatePattern>();
		iParents.add(datePattern);
	}

	public Set<Department> getDepartments() { return iDepartments; }
	public void setDepartments(Set<Department> departments) { iDepartments = departments; }
	public void addTodepartments(Department department) {
		if (iDepartments == null) iDepartments = new HashSet<Department>();
		iDepartments.add(department);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof DatePattern)) return false;
		if (getUniqueId() == null || ((DatePattern)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((DatePattern)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "DatePattern["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "DatePattern[" +
			"\n	Name: " + getName() +
			"\n	Offset: " + getOffset() +
			"\n	Pattern: " + getPattern() +
			"\n	Session: " + getSession() +
			"\n	Type: " + getType() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Visible: " + getVisible() +
			"]";
	}
}
