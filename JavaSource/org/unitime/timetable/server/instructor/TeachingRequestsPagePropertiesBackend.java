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

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.instructor.InstructorAvailabilityWidget.InstructorAvailabilityModel;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.RoomInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.AttributeTypeInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.PreferenceInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.SubjectAreaInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsPagePropertiesRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsPagePropertiesResponse;
import org.unitime.timetable.gwt.shared.RoomInterface.RoomSharingOption;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstructorAttributeType;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.InstructorAttributeTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.instructor.InstructorSchedulingProxy;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.RequiredTimeTable;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(TeachingRequestsPagePropertiesRequest.class)
public class TeachingRequestsPagePropertiesBackend implements GwtRpcImplementation<TeachingRequestsPagePropertiesRequest, TeachingRequestsPagePropertiesResponse> {
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	@Autowired SolverService<InstructorSchedulingProxy> instructorSchedulingSolverService;

	@Override
	public TeachingRequestsPagePropertiesResponse execute(TeachingRequestsPagePropertiesRequest request, SessionContext context) {
		context.checkPermission(Right.InstructorScheduling);
		InstructorSchedulingProxy solver = instructorSchedulingSolverService.getSolver();
		Long ownerId = null;
		if (solver != null)
			ownerId = solver.getProperties().getPropertyLong("General.SolverGroupId", null);
		TeachingRequestsPagePropertiesResponse ret = new TeachingRequestsPagePropertiesResponse();
		for (SubjectArea sa: SubjectArea.getUserSubjectAreas(context.getUser(), true)) {
			if (ownerId != null && (sa.getDepartment().getSolverGroup() == null || !ownerId.equals(sa.getDepartment().getSolverGroup().getUniqueId())))
				continue;
			boolean hasTeachingPreference = false;
			for (DepartmentalInstructor di: sa.getDepartment().getInstructors())
				if (di.getTeachingPreference() != null && !PreferenceLevel.sProhibited.equals(di.getTeachingPreference().getPrefProlog())) {
					hasTeachingPreference = true;
					break;
				}
			if (!hasTeachingPreference) continue;
			SubjectAreaInterface subject = new SubjectAreaInterface();
			subject.setId(sa.getUniqueId());
			subject.setAbbreviation(sa.getSubjectAreaAbbreviation());
			subject.setLabel(sa.getTitle());
			ret.addSubjectArea(subject);
		}
		for (Department d: Department.getUserDepartments(context.getUser())) {
			if (ownerId != null && (d.getSolverGroup() == null || !ownerId.equals(d.getSolverGroup().getUniqueId())))
				continue;
			boolean hasTeachingPreference = false;
			for (DepartmentalInstructor di: d.getInstructors())
				if (di.getTeachingPreference() != null && !PreferenceLevel.sProhibited.equals(di.getTeachingPreference().getPrefProlog())) {
					hasTeachingPreference = true;
					break;
				}
			if (!hasTeachingPreference) continue;
			DepartmentInterface department = new DepartmentInterface();
			department.setId(d.getUniqueId());
			department.setDeptCode(d.getDeptCode());
			department.setLabel(d.getName());
			department.setTitle(d.getLabel());
			department.setAbbreviation(d.getAbbreviation());
			ret.addDepartment(department);
		}
		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList()) {
			ret.addPreference(new PreferenceInterface(pref.getUniqueId(), PreferenceLevel.prolog2bgColor(pref.getPrefProlog()), pref.getPrefProlog(), pref.getPrefName(), pref.getAbbreviation(), true));
		}
		String sa = (String)context.getAttribute(SessionAttribute.OfferingsSubjectArea);
		if (Constants.ALL_OPTION_VALUE.equals(sa))
			ret.setLastSubjectAreaId(-1l);
		else if (sa != null) {
			if (sa.indexOf(',') >= 0) sa = sa.substring(0, sa.indexOf(','));
			ret.setLastSubjectAreaId(Long.valueOf(sa));
		}
		String deptId = (String)context.getAttribute(SessionAttribute.DepartmentId);
		if (deptId != null) {
			try {
				ret.setLastDepartmentId(Long.valueOf(deptId));
			} catch (NumberFormatException e) {}
		}
		for (InstructorAttributeType type: (List<InstructorAttributeType>)InstructorAttributeTypeDAO.getInstance().getSession().createQuery(
				"from InstructorAttributeType order by label").setCacheable(true).list()) {
			AttributeTypeInterface t = new AttributeTypeInterface();
			t.setId(type.getUniqueId());
			t.setAbbreviation(type.getReference());
			t.setLabel(type.getLabel());
			t.setConjunctive(type.isConjunctive());
			t.setRequired(type.isRequired());
			ret.addAttributeType(t);
		}
		
		for (int i = 0; true; i++) {
			String mode = ApplicationProperty.RoomSharingMode.value(String.valueOf(1 + i), i < CONSTANTS.roomSharingModes().length ? CONSTANTS.roomSharingModes()[i] : null);
			if (mode == null || mode.isEmpty()) break;
			ret.addMode(new RoomInterface.RoomSharingDisplayMode(mode));
		}
		
		ret.setHasSolver(solver != null);
		
		InstructorAvailabilityModel model = new InstructorAvailabilityModel();
		for (int i = 0; true; i++) {
			String mode = ApplicationProperty.RoomSharingMode.value(String.valueOf(1 + i), i < CONSTANTS.roomSharingModes().length ? CONSTANTS.roomSharingModes()[i] : null);
			if (mode == null || mode.isEmpty()) break;
			model.addMode(new RoomInterface.RoomSharingDisplayMode(mode));
		}
		model.setDefaultEditable(true);
		for (PreferenceLevel pref: PreferenceLevel.getPreferenceLevelList(true)) {
			if (PreferenceLevel.sRequired.equals(pref.getPrefProlog())) continue;
			RoomSharingOption option = new RoomSharingOption(model.char2id(PreferenceLevel.prolog2char(pref.getPrefProlog())), pref.prefcolor(), "", pref.getPrefName(), true); 
			model.addOption(option);
			if (PreferenceLevel.sNeutral.equals(pref.getPrefProlog()))
				model.setDefaultOption(option);		
		}
		String defaultGridSize = RequiredTimeTable.getTimeGridSize(context.getUser());
		if (defaultGridSize != null)
			for (int i = 0; i < model.getModes().size(); i++) {
				if (model.getModes().get(i).getName().equals(defaultGridSize)) {
					model.setDefaultMode(i); break;
				}
			}
		model.setDefaultHorizontal(CommonValues.HorizontalGrid.eq(context.getUser().getProperty(UserProperty.GridOrientation)));
		model.setNoteEditable(false);
		ret.setInstructorAvailabilityModel(model);
		
		return ret;
	}

}
