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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.client.page.SolverWarnings.SolverWarningsRequest;
import org.unitime.timetable.gwt.client.page.SolverWarnings.SolverWarningsResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.service.SolverService;

@GwtRpcImplements(SolverWarningsRequest.class)
public class SolverWarningsBackend implements GwtRpcImplementation<SolverWarningsRequest, SolverWarningsResponse> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);

	@Autowired SolverService<ExamSolverProxy> examinationSolverService;
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;

	@Override
	public SolverWarningsResponse execute(SolverWarningsRequest request, SessionContext context) {
		SolverWarningsResponse warn = null;
		switch (request.getType()) {
		case solver:
			warn = getSolverWarningCheckSolution(context);
			if (warn != null) return warn.setWarning(true);
			return getCurrentAssignmentWarning(context); 
		case assignments:
			warn = getCurrentAssignmentWarning(context);
			if (warn != null) return warn;
			return getExamAssignmentWarning(context, false);
		case exam:
			return getExamAssignmentWarning(context, true);
		}
		return null;
	}
	
	public SolverWarningsResponse getSolverWarningCheckSolution(SessionContext context) {
		if (!context.hasPermission(Right.CourseTimetabling)) return null; // no permission
		
		// Return warning of the solver loaded in memory (if there is a running solver)
		try {
			SolverProxy proxy = courseTimetablingSolverService.getSolver();
			if (proxy!=null)
				return new SolverWarningsResponse(proxy.getProperties().getProperty("General.SolverWarnings"));
		} catch (Exception e){}

		// Return warning of the selected solution (warning that was present when the solution was saved, if there is a solution loaded in memory)
		String id = (String)context.getAttribute("Solver.selectedSolutionId");
		if (id != null && !id.isEmpty()) {
			String warn = "";
			for (String solutionId: id.split(",")) {
				Solution solution = SolutionDAO.getInstance().get(Long.valueOf(solutionId));
				for (SolverParameter p: solution.getParameters()) {
					if ("General.SolverWarnings".equals(p.getDefinition().getName()) && p.getValue() != null) {
						if (!warn.isEmpty()) warn += "<br>";
						warn += p.getValue();
					}
				}
 			}
			return warn.isEmpty() ? null : new SolverWarningsResponse(warn);
		}
		
		// Compute warning from solver groups
		Set<SolverGroup> solverGroups = SolverGroup.getUserSolverGroups(context.getUser());
		if (solverGroups.isEmpty()) return null; // no solver groups
		
		String warn = "";
		int maxDistPriority = Integer.MIN_VALUE;
		int nrWarns = 0;
		for (SolverGroup sg: solverGroups)
			maxDistPriority = Math.max(maxDistPriority, sg.getMaxDistributionPriority());
		
		for (SolverGroup sg: SolverGroup.findBySessionId(context.getUser().getCurrentAcademicSessionId())) {
			if (solverGroups.contains(sg)) continue;
			if (sg.getMinDistributionPriority() < maxDistPriority && sg.getCommittedSolution() == null) {
				if (nrWarns > 0) warn += "<br>";
				List<String> subjects = new ArrayList<String>();
				boolean dept = false;
				for (Department d: sg.getDepartments()) {
					if (d.isExternalManager().booleanValue()) {
						subjects.add(d.getExternalMgrAbbv());
					} else {
						dept = true;
						for (SubjectArea sa: d.getSubjectAreas())
							subjects.add(sa.getSubjectAreaAbbreviation());
					}
				}
				if (dept)
					warn += MESSAGES.warnSolverNoCommittedSolutionDepartmental(sg.getAbbv(), SolverPageBackend.toString(subjects));
				else
					warn += MESSAGES.warnSolverNoCommittedSolutionExternal(sg.getAbbv(), SolverPageBackend.toString(subjects));
				nrWarns++;
				if (nrWarns >= 3) {
					warn += "<br>...";
					break;
				}
			}
		}
		
		return warn.isEmpty() ? null : new SolverWarningsResponse(warn);
	}
	
	public SolverWarningsResponse getCurrentAssignmentWarning(SessionContext context) {
		if (!context.hasPermission(Right.Timetables)) return null; // no permission
		
		// Return warning of the solver loaded in memory (if there is a running solver)
		try {
			SolverProxy proxy = courseTimetablingSolverService.getSolver();
			if (proxy != null) {
				Long[] solverGroupId = proxy.getProperties().getPropertyLongArry("General.SolverGroupId", null);
				List<String> names = new ArrayList<String>();
				boolean interactive = proxy.getProperties().getPropertyBoolean("General.InteractiveMode", false);
				if (solverGroupId != null) {
					for (int i = 0; i < solverGroupId.length; i++) {
						SolverGroup sg = SolverGroupDAO.getInstance().get(solverGroupId[i]);
						names.add(sg == null ? MESSAGES.notApplicable() : solverGroupId.length <= 3 ? sg.getName() : sg.getAbbv());
				   }
				}
				if (names == null || names.isEmpty()) names.add(MESSAGES.notApplicable());
				return new SolverWarningsResponse(
						interactive ? "gwt.jsp?page=listSolutions" : "gwt.jsp?page=solver&type=course",
						MESSAGES.infoSolverShowingSolution(SolverPageBackend.toString(names))
						);
			}
		} catch (Exception e) {}

		// Return warning of the selected solution (warning that was present when the solution was saved, if there is a solution loaded in memory)
		String id = (String)context.getAttribute("Solver.selectedSolutionId");
		if (id != null && !id.isEmpty()) {
			List<String> names = new ArrayList<String>();
			String[] solutionIds = id.split(",");
			for (int i = 0; i < solutionIds.length; i++) {
				Solution solution = SolutionDAO.getInstance().get(Long.valueOf(solutionIds[i]));
				names.add(solutionIds.length <= 3 ? solution.getOwner().getName() : solution.getOwner().getAbbv());
			}
			return new SolverWarningsResponse(
					"gwt.jsp?page=listSolutions",
					names.isEmpty() ? null : names.size() == 1 ? MESSAGES.infoSolverShowingSelectedSolution(names.get(0)) : MESSAGES.infoSolverShowingSelectedSolutions(SolverPageBackend.toString(names))
					);
		}

		return null;
	}
	
	public SolverWarningsResponse getExamAssignmentWarning(SessionContext context, boolean checkExamType) {
		if (!context.hasPermission(Right.ExaminationSolver)) return null; // no permission
		
		// Return warning of the solver loaded in memory (if there is a running solver)
		try {
			ExamSolverProxy proxy = examinationSolverService.getSolver();
			if (proxy != null) {
				ExamType type = ExamTypeDAO.getInstance().get(proxy.getExamTypeId());
				if (type != null) {
					if (checkExamType) {
						Long selectedExamTypeId = (Long)context.getAttribute(SessionAttribute.ExamType);
						if (selectedExamTypeId != null && !selectedExamTypeId.equals(type.getUniqueId())) return null;
					}
					return new SolverWarningsResponse(
						"gwt.jsp?page=solver&type=exam",
						MESSAGES.infoExamSolverShowingSolution(type.getLabel())
					);
				}
			}
		} catch (Exception e) {}
		
		return null;
	}

}
