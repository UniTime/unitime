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
package org.unitime.timetable.server.menu;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.TreeSet;

import org.cpsolver.ifs.util.DataProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.action.ManageSolversAction;
import org.unitime.timetable.gwt.client.sectioning.PublishedSectioningSolutionsTable.InfoComparator;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.command.server.GwtRpcLogging;
import org.unitime.timetable.gwt.command.server.GwtRpcLogging.Level;
import org.unitime.timetable.gwt.resources.CPSolverMessages;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.MenuInterface.InfoPairInterface;
import org.unitime.timetable.gwt.shared.MenuInterface.SolverInfoInterface;
import org.unitime.timetable.gwt.shared.MenuInterface.SolverInfoRpcRequest;
import org.unitime.timetable.model.SolverParameterGroup.SolverType;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.CommonSolverInterface;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.instructor.InstructorSchedulingProxy;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.service.StudentSectioningSolverService;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(SolverInfoRpcRequest.class)
@GwtRpcLogging(Level.DISABLED)
public class SolverInfoBackend implements GwtRpcImplementation<SolverInfoRpcRequest, SolverInfoInterface> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static CPSolverMessages SOLVERMSG = Localization.create(CPSolverMessages.class);

	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;
	@Autowired StudentSectioningSolverService studentSectioningSolverService;
	@Autowired SolverService<InstructorSchedulingProxy> instructorSchedulingSolverService;

	@Override
	public SolverInfoInterface execute(SolverInfoRpcRequest request, SessionContext context) {
		CommonSolverInterface solver = studentSectioningSolverService.getSolver(false);
		if (solver == null) solver = examinationSolverService.getSolver();
		if (solver == null) solver = courseTimetablingSolverService.getSolver();
		if (solver == null) solver = instructorSchedulingSolverService.getSolver();
		if (solver == null && context.hasPermission(Right.StudentSectioningSolverDashboard))
			solver = studentSectioningSolverService.getPublishedSolver(); 
		SolverInfoInterface info = getInfo(solver, request.isIncludeSolutionInfo());
		if (solver != null && solver.getType() == SolverType.STUDENT && info != null && !context.hasPermission(Right.StudentSectioningSolver))
			info.setUrl("batchsctdash");
		return info;
	}
	
	public SolverInfoInterface getInfo(CommonSolverInterface solver, boolean includeSolutionInfo) {
		if (solver == null) return null;
		Map progress = solver.getProgress();
		if (progress == null) return null;
		
		SolverInfoInterface ret = new SolverInfoInterface();
		DataProperties properties = solver.getProperties();
		String progressStatus = (String)progress.get("STATUS");
		String progressPhase = (String)progress.get("PHASE");
		long progressCur = ((Long)progress.get("PROGRESS")).longValue();
		long progressMax = ((Long)progress.get("MAX_PROGRESS")).longValue();
		String version = (String)progress.get("VERSION");
		if (version==null || "-1".equals(version)) version = "N/A";
		double progressPercent = 100.0*((double)(progressCur<progressMax?progressCur:progressMax))/((double)progressMax);
		String ownerName = ManageSolversAction.getSolverOwner(properties);
		if (ownerName.length() > 50)
			ownerName = ownerName.substring(0,47) + "...";

		Map<String, String> translations = null;
		switch (solver.getType()) {
		case COURSE:
			ret.setType(MESSAGES.solverCourse());
			ret.setUrl("solver?type=course");
			translations = SOLVERMSG.courseInfoMessages();
			break;
		case EXAM:
			ret.setType(MESSAGES.solverExamination());
			ret.setUrl("solver?type=exam");
			translations = SOLVERMSG.examInfoMessages();
			break;
		case STUDENT:
			ret.setType(MESSAGES.solverStudent());
			ret.setUrl("solver?type=student");
			translations = SOLVERMSG.studentInfoMessages();
			break;
		case INSTRUCTOR:
			ret.setType(MESSAGES.solverInstructor());
			ret.setUrl("solver?type=instructor");
			translations = SOLVERMSG.instructorInfoMessages();
			break;
		}
		ret.addPair(MESSAGES.fieldType(), ret.getType());
		ret.addPair(MESSAGES.fieldSolver(), progressStatus);
		ret.setSolver(progressStatus);
		ret.addPair(MESSAGES.fieldPhase(), progressPhase);
		if (progressMax>0)
			ret.addPair(MESSAGES.fieldProgress(), (progressCur<progressMax?progressCur:progressMax) + " of " + progressMax + " (" + new DecimalFormat("0.0").format(progressPercent) + "%)");
		ret.addPair(MESSAGES.fieldOwner(), ownerName);
		ret.addPair(MESSAGES.fieldHost(), solver.getHost());
		ret.addPair(MESSAGES.fieldSession(), SessionDAO.getInstance().get(properties.getPropertyLong("General.SessionId",null)).getLabel());
		InfoPairInterface p = ret.addPair(MESSAGES.fieldVersion(), version);
		
		if (includeSolutionInfo) {
			Map<String,String> info = null;
			try {
				info = solver.statusSolutionInfo();
			} catch (Exception e) {}
			if (info != null && !info.isEmpty()) {
				p.setSeparator(true);
				TreeSet<String> keys = new TreeSet<String>(new InfoComparator());
				keys.addAll(info.keySet());
				for (String key: keys) {
					String translatedKey = (translations == null ? null : translations.get(key));
					if (translatedKey != null)
						ret.addPair(translatedKey, info.get(key));
					else
						ret.addPair(key, info.get(key));
				}
			}
		}
	return ret;
		
	}
}
