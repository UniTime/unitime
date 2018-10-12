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
package org.unitime.timetable.server.solver;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverPageMessages;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverPageMessagesRequest;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverType;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.solver.CommonSolverInterface;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.instructor.InstructorSchedulingProxy;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(SolverPageMessagesRequest.class)
public class SolverPageMessagesBackend implements GwtRpcImplementation<SolverPageMessagesRequest, SolverPageMessages> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;
	@Autowired SolverService<StudentSolverProxy> studentSectioningSolverService;
	@Autowired SolverService<InstructorSchedulingProxy> instructorSchedulingSolverService;

	@Override
	public SolverPageMessages execute(SolverPageMessagesRequest request, SessionContext context) {
		SolverPageMessages ret = new SolverPageMessages();
		SolverPageBackend.fillSolverWarnings(context, getSolver(request.getType()), request.getType(), ret);
		return ret;
	}
	
	protected CommonSolverInterface getSolver(SolverType type) {
		switch (type) {
		case COURSE:
			return courseTimetablingSolverService.getSolver();
		case EXAM:
			return examinationSolverService.getSolver();
		case STUDENT:
			return studentSectioningSolverService.getSolver();
		case INSTRUCTOR:
			return instructorSchedulingSolverService.getSolver();
		default:
			throw new IllegalArgumentException(MESSAGES.errorSolverInvalidType(type.name()));
		}
	}
}
