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

import java.util.List;
import java.util.StringTokenizer;

import org.cpsolver.ifs.util.Progress;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SolverInterface.ProgressLogLevel;
import org.unitime.timetable.gwt.shared.SolverInterface.SolutionLog;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverLogPageRequest;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverLogPageResponse;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverType;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.CommonSolverInterface;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.instructor.InstructorSchedulingProxy;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;
import org.unitime.timetable.solver.ui.LogInfo;
import org.unitime.timetable.webutil.BackTracker;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(SolverLogPageRequest.class)
public class SolverLogPageBackend implements GwtRpcImplementation<SolverLogPageRequest, SolverLogPageResponse> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;
	@Autowired SolverService<StudentSolverProxy> studentSectioningSolverService;
	@Autowired SolverService<InstructorSchedulingProxy> instructorSchedulingSolverService;

	@Override
	public SolverLogPageResponse execute(SolverLogPageRequest request, SessionContext context) {
		switch (request.getType()) {
		case COURSE:
			context.checkPermission(Right.SolverLog);
			break;
		case EXAM:
			context.checkPermission(Right.ExaminationSolverLog);
			break;
		case STUDENT:
			context.checkPermission(Right.StudentSectioningSolverLog);
			break;
		case INSTRUCTOR:
			context.checkPermission(Right.InstructorSchedulingSolverLog);
			break;
		}
		
		ProgressLogLevel level = request.getLevel();
		if (level == null) level = ProgressLogLevel.INFO;
		
		SolverService<? extends CommonSolverInterface> service = getSolverService(request.getType());
		CommonSolverInterface solver = service.getSolver();
		
		SolverLogPageResponse response = new SolverLogPageResponse(level);
		if (solver == null) {
			if (request.getType() == SolverType.COURSE) {
				String solutionIdsStr = (String)context.getAttribute(SessionAttribute.SelectedSolution);
				if (solutionIdsStr != null && !solutionIdsStr.isEmpty()) {
					for (StringTokenizer s = new StringTokenizer(solutionIdsStr,","); s.hasMoreTokens(); ) {
						Solution solution = SolutionDAO.getInstance().get(Long.valueOf(s.nextToken()));
						if (solution != null) {
							LogInfo log = (LogInfo)solution.getInfo("LogInfo");
							if (log != null) {
								SolutionLog sl = new SolutionLog(solution.getOwner().getName());
								for (Progress.Message m: log.getLog()) {
									if (m.getLevel() >= level.ordinal()) {
										sl.addMessage(m.getLevel(), m.getDate(), m.getMessage(), m.getTrace());
									}
								}
								response.addSolutionLog(sl);
							}
						}
					}
					return response;
				}
				throw new GwtRpcException(MESSAGES.warnSolverNotStartedSolutionNotSelected());
			}
			throw new GwtRpcException(MESSAGES.warnSolverNotStarted());
		}
		
		List<Progress.Message> log = solver.getProgressLog(level.ordinal(), null, request.getLastDate());
		if (log != null)
			for (Progress.Message m: log)
				response.addMessage(m.getLevel(), m.getDate(), m.getMessage(), m.getTrace());
		
		switch (request.getType()) {
		case COURSE:
			BackTracker.markForBack(context, "gwt.jsp?page=solverlog&type=course", MESSAGES.pageCourseTimetablingSolverLog(), true, true);
			break;
		case EXAM:
			BackTracker.markForBack(context, "gwt.jsp?page=solverlog&type=exam", MESSAGES.pageExaminationTimetablingSolverLog(), true, true);
			break;
		case INSTRUCTOR:
			BackTracker.markForBack(context, "gwt.jsp?page=solverlog&type=instructor", MESSAGES.pageInstructorSchedulingSolverLog(), true, true);
			break;
		case STUDENT:
			BackTracker.markForBack(context, "gwt.jsp?page=solverlog&type=student", MESSAGES.pageStudentSchedulingSolverLog(), true, true);
			break;
		}

		return response;
	}
	
	protected SolverService<? extends CommonSolverInterface> getSolverService(SolverType type) {
		switch (type) {
		case COURSE:
			return courseTimetablingSolverService;
		case EXAM:
			return examinationSolverService;
		case STUDENT:
			return studentSectioningSolverService;
		case INSTRUCTOR:
			return instructorSchedulingSolverService;
		default:
			throw new IllegalArgumentException(MESSAGES.errorSolverInvalidType(type.name()));
		}
	}
}
