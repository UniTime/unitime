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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingAssignmentsPageRequest;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.instructor.InstructorSchedulingProxy;
import org.unitime.timetable.solver.service.SolverService;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(TeachingAssignmentsPageRequest.class)
public class TeachingAssignmentsBackend extends InstructorSchedulingBackendHelper implements GwtRpcImplementation<TeachingAssignmentsPageRequest, GwtRpcResponseList<InstructorInfo>> {
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	@Autowired SolverService<InstructorSchedulingProxy> instructorSchedulingSolverService;
	
	@Override
	public GwtRpcResponseList<InstructorInfo> execute(TeachingAssignmentsPageRequest request, SessionContext context) {
		context.checkPermission(Right.InstructorSchedulingSolver);
		context.setAttribute(SessionAttribute.DepartmentId, request.getDepartmentId() == null ? "-1" : String.valueOf(request.getDepartmentId()));
		InstructorSchedulingProxy solver = instructorSchedulingSolverService.getSolver();
		if (solver != null)
			return new GwtRpcResponseList<InstructorInfo>(solver.getInstructors(request.getDepartmentId()));
		else {
			Set<String> commonItypes = getCommonItypes();
			String nameFormat = UserProperty.NameFormat.get(context.getUser());
			
			GwtRpcResponseList<InstructorInfo> ret = new GwtRpcResponseList<InstructorInfo>();
			org.hibernate.Session hibSession = Class_DAO.getInstance().getSession();
			List<DepartmentalInstructor> instructors = null;
			if (request.getDepartmentId() == null) {
				List<Long> departmentIds = new ArrayList<Long>();
				for (Department d: Department.getUserDepartments(context.getUser())) {
					for (DepartmentalInstructor di: d.getInstructors())
						if (di.getTeachingPreference() != null && !PreferenceLevel.sProhibited.equals(di.getTeachingPreference().getPrefProlog())) {
							departmentIds.add(d.getUniqueId());
							break;
						}
				}
				if (departmentIds.isEmpty()) return ret;
				instructors = (List<DepartmentalInstructor>)hibSession.createQuery(
						"select distinct i from DepartmentalInstructor i where " +
						"i.department.uniqueId in :departmentIds and i.teachingPreference.prefProlog != :prohibited and i.maxLoad > 0.0"
						).setParameterList("departmentIds", departmentIds).setString("prohibited", PreferenceLevel.sProhibited).list();
			} else {
				instructors = (List<DepartmentalInstructor>)hibSession.createQuery(
						"select distinct i from DepartmentalInstructor i where " +
				    	"i.department.uniqueId = :departmentId and i.teachingPreference.prefProlog != :prohibited and i.maxLoad > 0.0"
						).setLong("departmentId", request.getDepartmentId()).setString("prohibited", PreferenceLevel.sProhibited).list();
			}
	    	for (DepartmentalInstructor instructor: instructors) {
	    		ret.add(getInstructorInfo(instructor, nameFormat, commonItypes));
	    	}
	    	Collections.sort(ret);
	    	return ret;
		}
	}
}
