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
package org.unitime.timetable.server.instructor;

import java.util.List;

import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeTypeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorAttributePropertiesInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorAttributePropertiesRequest;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.InstructorAttributeType;
import org.unitime.timetable.model.dao.InstructorAttributeTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(InstructorAttributePropertiesRequest.class)
public class InstructorAttributePropertiesBackend implements GwtRpcImplementation<InstructorAttributePropertiesRequest, InstructorAttributePropertiesInterface>{

	@Override
	public InstructorAttributePropertiesInterface execute(InstructorAttributePropertiesRequest request, SessionContext context) {
		context.checkPermission(Right.InstructorAttributes);
		
		InstructorAttributePropertiesInterface response = new InstructorAttributePropertiesInterface();
		for (Department department: Department.getUserDepartments(context.getUser())) {
			DepartmentInterface d = new DepartmentInterface();
			d.setId(department.getUniqueId());
			d.setAbbreviation(department.getAbbreviation());
			d.setDeptCode(department.getDeptCode());
			d.setLabel(department.getName());
			d.setTitle(department.getLabel());
			d.setCanSeeAttributes(context.hasPermission(department, Right.InstructorAttributes));
			d.setCanAddAttribute(context.hasPermission(department, Right.InstructorAttributeAdd));
			response.addDepartment(d);
		}
		
		for (InstructorAttributeType type: (List<InstructorAttributeType>)InstructorAttributeTypeDAO.getInstance().getSession().createQuery(
				"from InstructorAttributeType order by label").setCacheable(true).list()) {
			AttributeTypeInterface t = new AttributeTypeInterface();
			t.setId(type.getUniqueId());
			t.setAbbreviation(type.getReference());
			t.setLabel(type.getLabel());
			t.setConjunctive(type.isConjunctive());
			t.setRequired(type.isRequired());
			response.addAttributeType(t);
		}
		
		response.setCanAddGlobalAttribute(context.hasPermission(Right.InstructorGlobalAttributeEdit));
		
		if (response.getDepartments().size() == 1) {
			response.setLastDepartmentId(response.getDepartments().get(0).getId());
		} else {
			String deptId = (String)context.getAttribute(SessionAttribute.DepartmentId);
			if (deptId != null) {
				try {
					response.setLastDepartmentId(Long.valueOf(deptId));
				} catch (NumberFormatException e) {}
			}
		}
		
		return response;
	}

}
