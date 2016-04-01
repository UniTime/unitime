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

import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.InstructorInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeTypeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.GetInstructorAttributesRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.PositionInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInterface;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstructorAttribute;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.dao.InstructorAttributeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.NameFormat;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(GetInstructorAttributesRequest.class)
public class GetInstructorAttributesBackend implements GwtRpcImplementation<GetInstructorAttributesRequest, GwtRpcResponseList<AttributeInterface>>{

	@Override
	public GwtRpcResponseList<AttributeInterface> execute(GetInstructorAttributesRequest request, SessionContext context) {
		context.checkPermission(request.getDepartmentId(), "Department", Right.InstructorAttributes);
		
		NameFormat instructorNameFormat = NameFormat.fromReference(UserProperty.NameFormat.get(context.getUser()));
		boolean sortByLastName = CommonValues.SortByLastName.eq(UserProperty.SortNames.get(context.getUser()));

		GwtRpcResponseList<AttributeInterface> response = new GwtRpcResponseList<AttributeInterface>();
		for (InstructorAttribute attribute: (List<InstructorAttribute>)InstructorAttributeDAO.getInstance().getSession().createQuery(
				"from InstructorAttribute a where a.session.uniqueId = :sessionId and (a.department is null or a.department.uniqueId = :departmentId) order by a.name"
				).setLong("sessionId", context.getUser().getCurrentAcademicSessionId()).setLong("departmentId", request.getDepartmentId()).setCacheable(true).list()) {
			AttributeInterface a = new AttributeInterface();
			a.setId(attribute.getUniqueId());
			a.setParentId(attribute.getParentAttribute() == null ? null : attribute.getParentAttribute().getUniqueId());
			a.setParentName(attribute.getParentAttribute() == null ? null : attribute.getParentAttribute().getName());
			a.setCode(attribute.getCode());
			a.setName(attribute.getName());
			a.setCanDelete(context.hasPermission(attribute, Right.InstructorAttributeDelete));
			a.setCanEdit(context.hasPermission(attribute, Right.InstructorAttributeEdit));
			a.setCanAssign(context.hasPermission(attribute, Right.InstructorAttributeAssign));
			a.setCanChangeType(attribute.getChildAttributes().isEmpty());
			if (attribute.getType() != null) {
				AttributeTypeInterface t = new AttributeTypeInterface();
				t.setId(attribute.getType().getUniqueId());
				t.setAbbreviation(attribute.getType().getReference());
				t.setLabel(attribute.getType().getLabel());
				t.setConjunctive(attribute.getType().isConjunctive());
				t.setRequired(attribute.getType().isRequired());
				a.setType(t);
			}
			if (attribute.getDepartment() != null) {
				DepartmentInterface d = new DepartmentInterface();
				d.setId(attribute.getDepartment().getUniqueId());
				d.setAbbreviation(attribute.getDepartment().getAbbreviation());
				d.setDeptCode(attribute.getDepartment().getDeptCode());
				d.setLabel(attribute.getDepartment().getName());
				d.setTitle(attribute.getDepartment().getLabel());
				a.setDepartment(d);
			}
			for (DepartmentalInstructor instructor: attribute.getInstructors()) {
				if (!instructor.getDepartment().getUniqueId().equals(request.getDepartmentId())) continue;
				InstructorInterface i = new InstructorInterface();
				i.setId(instructor.getUniqueId());
				i.setFirstName(instructor.getFirstName());
				i.setMiddleName(instructor.getMiddleName());
				i.setLastName(instructor.getLastName());
				i.setFormattedName(instructorNameFormat.format(instructor));
				if (sortByLastName)
					i.setOrderName(instructor.nameLastNameFirst());
				i.setExternalId(instructor.getExternalUniqueId());
				if (instructor.getPositionType() != null) {
					PositionInterface p = new PositionInterface();
					p.setId(instructor.getPositionType().getUniqueId());
					p.setAbbreviation(instructor.getPositionType().getReference());
					p.setLabel(instructor.getPositionType().getLabel());
					p.setSortOrder(instructor.getPositionType().getSortOrder());
					i.setPosition(p);
				}
				PreferenceLevel pref = instructor.getTeachingPreference();
				if (pref == null) pref = PreferenceLevel.getPreferenceLevel(PreferenceLevel.sProhibited);
				i.setTeachingPreference(new PreferenceInterface(pref.getUniqueId(), PreferenceLevel.prolog2bgColor(pref.getPrefProlog()), pref.getPrefProlog(), pref.getPrefName(), pref.getAbbreviation(), true));
				i.setMaxLoad(instructor.getMaxLoad());
				a.addInstructor(i);
			}
			response.add(a);
		}
		
		return response;
	}

}
