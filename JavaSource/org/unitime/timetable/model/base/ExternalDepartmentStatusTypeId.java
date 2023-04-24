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

import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import java.io.Serializable;

import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ExternalDepartmentStatusType;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public class ExternalDepartmentStatusTypeId implements Serializable {
	private static final long serialVersionUID = 1L;

	private Department iExternalDepartment;
	private Department iDepartment;

	public ExternalDepartmentStatusTypeId() {}

	public ExternalDepartmentStatusTypeId(Department externalDepartment, Department department) {
		iExternalDepartment = externalDepartment;
		iDepartment = department;
	}

	@Id
	@ManyToOne(optional = false)
	@JoinColumn(name = "ext_dept_id")
	public Department getExternalDepartment() { return iExternalDepartment; }
	public void setExternalDepartment(Department externalDepartment) { iExternalDepartment = externalDepartment; }

	@Id
	@ManyToOne(optional = false)
	@JoinColumn(name = "department_id")
	public Department getDepartment() { return iDepartment; }
	public void setDepartment(Department department) { iDepartment = department; }


	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ExternalDepartmentStatusType)) return false;
		ExternalDepartmentStatusType externalDepartmentStatusType = (ExternalDepartmentStatusType)o;
		if (getExternalDepartment() == null || externalDepartmentStatusType.getExternalDepartment() == null || !getExternalDepartment().equals(externalDepartmentStatusType.getExternalDepartment())) return false;
		if (getDepartment() == null || externalDepartmentStatusType.getDepartment() == null || !getDepartment().equals(externalDepartmentStatusType.getDepartment())) return false;
		return true;
	}

	@Override
	public int hashCode() {
		if (getExternalDepartment() == null || getDepartment() == null) return super.hashCode();
		return getExternalDepartment().hashCode() ^ getDepartment().hashCode();
	}

}
