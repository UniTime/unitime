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

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.InstructorAttributeType;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
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

	public BaseInstructorAttribute() {
	}

	public BaseInstructorAttribute(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "attribute_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "attribute_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "code", nullable = false, length = 20)
	public String getCode() { return iCode; }
	public void setCode(String code) { iCode = code; }

	@Column(name = "name", nullable = false, length = 60)
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "type_id", nullable = false)
	public InstructorAttributeType getType() { return iType; }
	public void setType(InstructorAttributeType type) { iType = type; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "parent_id", nullable = true)
	public InstructorAttribute getParentAttribute() { return iParentAttribute; }
	public void setParentAttribute(InstructorAttribute parentAttribute) { iParentAttribute = parentAttribute; }

	@ManyToOne(optional = false)
	@JoinColumn(name = "session_id", nullable = false)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "department_id", nullable = true)
	public Department getDepartment() { return iDepartment; }
	public void setDepartment(Department department) { iDepartment = department; }

	@OneToMany(mappedBy = "parentAttribute")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<InstructorAttribute> getChildAttributes() { return iChildAttributes; }
	public void setChildAttributes(Set<InstructorAttribute> childAttributes) { iChildAttributes = childAttributes; }
	public void addToChildAttributes(InstructorAttribute instructorAttribute) {
		if (iChildAttributes == null) iChildAttributes = new HashSet<InstructorAttribute>();
		iChildAttributes.add(instructorAttribute);
	}
	@Deprecated
	public void addTochildAttributes(InstructorAttribute instructorAttribute) {
		addToChildAttributes(instructorAttribute);
	}

	@ManyToMany(mappedBy = "attributes")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<DepartmentalInstructor> getInstructors() { return iInstructors; }
	public void setInstructors(Set<DepartmentalInstructor> instructors) { iInstructors = instructors; }
	public void addToInstructors(DepartmentalInstructor departmentalInstructor) {
		if (iInstructors == null) iInstructors = new HashSet<DepartmentalInstructor>();
		iInstructors.add(departmentalInstructor);
	}
	@Deprecated
	public void addToinstructors(DepartmentalInstructor departmentalInstructor) {
		addToInstructors(departmentalInstructor);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof InstructorAttribute)) return false;
		if (getUniqueId() == null || ((InstructorAttribute)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((InstructorAttribute)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
