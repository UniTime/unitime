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
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.InstructorInterface.TeachingRequestsPageRequest;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TeachingRequest;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.gwt.shared.InstructorInterface.InstructorInfo;
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
@GwtRpcImplements(TeachingRequestsPageRequest.class)
public class TeachingRequestsPageBackend extends InstructorSchedulingBackendHelper implements GwtRpcImplementation<TeachingRequestsPageRequest, GwtRpcResponseList<TeachingRequestInfo>> {
	@Autowired SolverService<InstructorSchedulingProxy> instructorSchedulingSolverService;
	
	@Override
	public GwtRpcResponseList<TeachingRequestInfo> execute(TeachingRequestsPageRequest request, SessionContext context) {
		if (instructorSchedulingSolverService == null)
			instructorSchedulingSolverService = (SolverService<InstructorSchedulingProxy>)SpringApplicationContextHolder.getBean("instructorSchedulingSolverService");
		
		context.checkPermission(Right.InstructorScheduling);
		if (!request.getFilter().hasOption("offeringId")) {
			String subjectId = null;
			if (request.getFilter().hasOption("subjectId")) {
				subjectId = request.getFilter().getOption("subjectId");
			} else if (request.getFilter().hasOption("subject")) {
				SubjectArea subject = SubjectArea.findByAbbv(context.getUser().getCurrentAcademicSessionId(), request.getFilter().getOption("subject"));
				if (subject != null) {
					subjectId = subject.getUniqueId().toString();
					request.getFilter().setOption("subjectId", subjectId);
				}
			}
			context.setAttribute(SessionAttribute.OfferingsSubjectArea, subjectId == null ? "-1" : subjectId);
		}
		if (!request.getFilter().hasSessionId())
			request.getFilter().setSessionId(context.getUser().getCurrentAcademicSessionId());
		
		InstructorSchedulingProxy solver = instructorSchedulingSolverService.getSolver();
		if (solver != null && !request.getFilter().hasOption("offeringId"))
			return new GwtRpcResponseList<TeachingRequestInfo>(solver.getTeachingRequests(request.getFilter()));
		else {
			Context cx = new Context(context, solver);
			Query q = TeachingRequestsFilterBackend.toQuery(request.getFilter());

			GwtRpcResponseList<TeachingRequestInfo> ret = new GwtRpcResponseList<TeachingRequestInfo>();
			List<TeachingRequest> requests = TeachingRequestsFilterBackend.getRequestQuery(request.getFilter(), context.getUser()).select("r").query(DepartmentalInstructorDAO.getInstance().getSession()).list();
	    	for (TeachingRequest tr: requests) {
	    		if (tr.isCancelled()) continue;
	    		TeachingRequestInfo info = getRequest(tr, cx, true);
	    		if (info != null && q.match(new TeachingRequestMatcher(info, request.getFilter()))) {
    				for (InstructorInfo ii: info.getInstructors())
    					ii.setMatchingFilter(q.match(new TeachingRequestMatcher(info, ii, request.getFilter())));
	    			ret.add(info);
	    		}
	    	}
	    	Collections.sort(ret);
	    	return ret;
		}
	}
}
