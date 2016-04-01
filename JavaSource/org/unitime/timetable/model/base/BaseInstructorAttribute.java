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

import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.InstructorAttributeType;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseInstructorAttribute implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iCode;
	private String iName;

	private InstructorAttributeType iType;
	private InstructorAttribute iParentAttribute;
	private Session iSession;
	private Department iDepartment;
	private Set<InstructorAttribute> iChildAttributes;
	private Set<DepartmentalInstructor> iInstructors;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_CODE = "code";
	public static String PROP_NAME = "name";

	public BaseInstructorAttribute() {
		initialize();
	}

	public BaseInstructorAttribute(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getCode() { return iCode; }
	public void setCode(String code) { iCode = code; }

	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	public InstructorAttributeType getType() { return iType; }
	public void setType(InstructorAttributeType type) { iType = type; }

	public InstructorAttribute getParentAttribute() { return iParentAttribute; }
	public void setParentAttribute(InstructorAttribute parentAttribute) { iParentAttribute = parentAttribute; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public Department getDepartment() { return iDepartment; }
	public void setDepartment(Department department) { iDepartment = department; }

	public Set<InstructorAttribute> getChildAttributes() { return iChildAttributes; }
	public void setChildAttributes(Set<InstructorAttribute> childAttributes) { iChildAttributes = childAttributes; }
	public void addTochildAttributes(InstructorAttribute instructorAttribute) {
		if (iChildAttributes == null) iChildAttributes = new HashSet<InstructorAttribute>();
		iChildAttributes.add(instructorAttribute);
	}

	public Set<DepartmentalInstructor> getInstructors() { return iInstructors; }
	public void setInstructors(Set<DepartmentalInstructor> instructors) { iInstructors = instructors; }
	public void addToinstructors(DepartmentalInstructor departmentalInstructor) {
		if (iInstructors == null) iInstructors = new HashSet<DepartmentalInstructor>();
		iInstructors.add(departmentalInstructor);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof InstructorAttribute)) return false;
		if (getUniqueId() == null || ((InstructorAttribute)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((InstructorAttribute)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "InstructorAttribute["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "InstructorAttribute[" +
			"\n	Code: " + getCode() +
			"\n	Department: " + getDepartment() +
			"\n	Name: " + getName() +
			"\n	ParentAttribute: " + getParentAttribute() +
			"\n	Session: " + getSession() +
			"\n	Type: " + getType() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
