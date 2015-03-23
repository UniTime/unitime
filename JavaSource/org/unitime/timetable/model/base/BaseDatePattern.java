/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseDatePattern implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iName;
	private String iPattern;
	private Integer iOffset;
	private Integer iType;
	private Boolean iVisible;
	private Integer iNumberOfWeeks;

	private Session iSession;
	private Set<DatePattern> iParents;
	private Set<Department> iDepartments;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_NAME = "name";
	public static String PROP_PATTERN = "pattern";
	public static String PROP_OFFSET = "offset";
	public static String PROP_TYPE = "type";
	public static String PROP_VISIBLE = "visible";
	public static String PROP_NR_WEEKS = "numberOfWeeks";

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

	public Integer getNumberOfWeeks() { return iNumberOfWeeks; }
	public void setNumberOfWeeks(Integer numberOfWeeks) { iNumberOfWeeks = numberOfWeeks; }

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
			"\n	NumberOfWeeks: " + getNumberOfWeeks() +
			"\n	Offset: " + getOffset() +
			"\n	Pattern: " + getPattern() +
			"\n	Session: " + getSession() +
			"\n	Type: " + getType() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Visible: " + getVisible() +
			"]";
	}
}
