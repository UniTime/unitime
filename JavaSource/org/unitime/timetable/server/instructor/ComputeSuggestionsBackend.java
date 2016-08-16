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

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.InstructorInterface.AssignmentInfo;
import org.unitime.timetable.gwt.shared.InstructorInterface.ComputeSuggestionsRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.SuggestionsResponse;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.TeachingRequest;
import org.unitime.timetable.model.dao.DepartmentalInstructorDAO;
import org.unitime.timetable.model.dao.TeachingRequestDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.instructor.InstructorSchedulingProxy;
import org.unitime.timetable.solver.service.SolverService;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(ComputeSuggestionsRequest.class)
public class ComputeSuggestionsBackend extends InstructorSchedulingBackendHelper implements GwtRpcImplementation<ComputeSuggestionsRequest, SuggestionsResponse>{
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Autowired SolverService<InstructorSchedulingProxy> instructorSchedulingSolverService;

	@Override
	public SuggestionsResponse execute(ComputeSuggestionsRequest request, SessionContext context) {
		context.checkPermission(Right.InstructorSchedulingSolver);
		InstructorSchedulingProxy solver = instructorSchedulingSolverService.getSolver();
		if (solver != null)
			return solver.computeSuggestions(request);

		SuggestionsResponse response = new SuggestionsResponse();
		
		Context cx = new Context(context);
		Suggestion s = new Suggestion();
		for (AssignmentInfo ai: request.getAssignments()) {
			TeachingRequest tr = TeachingRequestDAO.getInstance().get(ai.getRequest().getRequestId());
			if (tr == null) continue;
			DepartmentalInstructor instructor = (ai.getInstructor() == null ? null : DepartmentalInstructorDAO.getInstance().get(ai.getInstructor().getInstructorId()));
			if (instructor != null)
				s.set(tr, ai.getIndex(), instructor);
		}
		response.setCurrentAssignment(s.toInfo(cx));
		
		if (request.getSelectedInstructorId() != null) {
			DepartmentalInstructor instructor = DepartmentalInstructorDAO.getInstance().get(request.getSelectedInstructorId());
			if (instructor == null) return null;
			TeachingRequest tr = null;
			if (request.getSelectedRequestId() != null)
				tr = TeachingRequestDAO.getInstance().get(request.getSelectedRequestId());
			computeDomainForInstructor(response, instructor, tr, cx);
		} else if (request.getSelectedRequestId() != null) {
			TeachingRequest tr = TeachingRequestDAO.getInstance().get(request.getSelectedRequestId());
			if (tr == null) return null;
			cx.setBase(s);
			computeDomainForClass(response, tr, request.getSelectedIndex(), cx);
		}
		return response;
	}
}
