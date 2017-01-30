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

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingAssignmentsPageRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestInfo;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.instructor.TeachingRequestsFilterBackend.TeachingRequestMatcher;
import org.unitime.timetable.solver.instructor.InstructorSchedulingProxy;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.spring.SpringApplicationContextHolder;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(TeachingAssignmentsPageRequest.class)
public class TeachingAssignmentsBackend extends InstructorSchedulingBackendHelper implements GwtRpcImplementation<TeachingAssignmentsPageRequest, GwtRpcResponseList<InstructorInfo>> {
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	@Autowired SolverService<InstructorSchedulingProxy> instructorSchedulingSolverService;
	
	@Override
	public GwtRpcResponseList<InstructorInfo> execute(TeachingAssignmentsPageRequest request, SessionContext context) {
		if (instructorSchedulingSolverService == null)
			instructorSchedulingSolverService = (SolverService<InstructorSchedulingProxy>)SpringApplicationContextHolder.getBean("instructorSchedulingSolverService");

		context.checkPermission(Right.InstructorScheduling);
		if (!request.getFilter().hasOption("instructorId")) {
			Long departmentId = null;
			if (request.getFilter().hasOption("departmentId")) {
				departmentId = Long.valueOf(request.getFilter().getOption("departmentId"));
			} else if (request.getFilter().hasOption("department")) {
				Department department = Department.findByDeptCode(request.getFilter().getOption("department"), context.getUser().getCurrentAcademicSessionId());
				if (department != null) {
					departmentId = department.getUniqueId();
					request.getFilter().setOption("departmentId", departmentId.toString());
				}
			}
			context.setAttribute(SessionAttribute.DepartmentId, departmentId == null ? null : departmentId.toString());
		}
		if (!request.getFilter().hasSessionId())
			request.getFilter().setSessionId(context.getUser().getCurrentAcademicSessionId());
		
		InstructorSchedulingProxy solver = instructorSchedulingSolverService.getSolver();
		if (solver != null)
			return new GwtRpcResponseList<InstructorInfo>(solver.getInstructors(request.getFilter()));
		else {
			Context cx = new Context(context, solver);
			Query q = TeachingRequestsFilterBackend.toQuery(request.getFilter());
			
			GwtRpcResponseList<InstructorInfo> ret = new GwtRpcResponseList<InstructorInfo>();
			List<DepartmentalInstructor> instructors = TeachingRequestsFilterBackend.getQuery(request.getFilter(), context.getUser())
					.select("distinct i").query(DepartmentalInstructorDAO.getInstance().getSession()).list();
	    	for (DepartmentalInstructor instructor: instructors) {
	    		InstructorInfo info = getInstructorInfo(instructor, cx);
	    		if (info != null && q.match(new TeachingRequestMatcher(info, request.getFilter()))) {
	    			for (TeachingRequestInfo ar: info.getAssignedRequests())
	    				ar.setMatchingFilter(q.match(new TeachingRequestMatcher(ar, info, request.getFilter())));
	    			ret.add(info);
	    		}
	    	}
	    	Collections.sort(ret);
	    	return ret;
		}
	}
}
