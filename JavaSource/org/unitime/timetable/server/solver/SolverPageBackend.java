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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.Progress;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.ListSolutionsForm;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.SolverInterface.HasPageMessages;
import org.unitime.timetable.gwt.shared.SolverInterface.PageMessage;
import org.unitime.timetable.gwt.shared.SolverInterface.PageMessageType;
import org.unitime.timetable.gwt.shared.SolverInterface.ProgressLogLevel;
import org.unitime.timetable.gwt.shared.SolverInterface.SolutionInfo;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverConfiguration;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverOperation;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverOwner;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverPageRequest;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverPageResponse;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverParameter;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverType;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SolverParameterDef;
import org.unitime.timetable.model.SolverParameterGroup;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.model.dao.SolverParameterDefDAO;
import org.unitime.timetable.model.dao.SolverPredefinedSettingDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.CommonSolverInterface;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.instructor.InstructorSchedulingProxy;
import org.unitime.timetable.solver.jgroups.SolverServer;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;
import org.unitime.timetable.solver.ui.LogInfo;
import org.unitime.timetable.solver.ui.PropertiesInfo;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(SolverPageRequest.class)
public class SolverPageBackend implements GwtRpcImplementation<SolverPageRequest, SolverPageResponse> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
	@Autowired SolverServerService solverServerService;
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;
	@Autowired SolverService<StudentSolverProxy> studentSectioningSolverService;
	@Autowired SolverService<InstructorSchedulingProxy> instructorSchedulingSolverService;

	@Override
	public SolverPageResponse execute(SolverPageRequest request, SessionContext context) {
		switch (request.getType()) {
		case COURSE:
			context.checkPermission(Right.Solver);
			break;
		case EXAM:
			context.checkPermission(Right.ExaminationSolver);
			break;
		case STUDENT:
			context.checkPermission(Right.StudentSectioningSolver);
			break;
		case INSTRUCTOR:
			context.checkPermission(Right.InstructorSchedulingSolver);
			break;
		}
		
		SolverPageResponse response = new SolverPageResponse();
		response.setSolverType(request.getType());
		response.setOperation(request.getOperation());

		SolverService<? extends CommonSolverInterface> service = getSolverService(request.getType());
		CommonSolverInterface solver = executeOperation(context, service, request, response);
		
		fillHosts(context, request, response);
		fillOwners(context, request, response);
		fillParameters(context, request, response);
		fillSolverInfos(context, solver, request, response);
		fillSolverLog(context, solver, request, response);
		fillSolverStatus(context, solver, request, response);
		fillAvailableOperations(context, solver, request, response);
		fillSolverWarnings(context, solver, request, response);

		return response;
	}
	
	protected CommonSolverInterface executeOperation(SessionContext context, SolverService<? extends CommonSolverInterface> service, SolverPageRequest request, SolverPageResponse response) {
		CommonSolverInterface solver = service.getSolver();

		switch (request.getOperation()) {
		case RESTORE_BEST:
        	if (solver == null) throw new GwtRpcException(MESSAGES.warnSolverNotStarted());
        	if (solver.isWorking()) throw new GwtRpcException(MESSAGES.warnSolverIsWorking());
        	solver.restoreBest();
			break;

		case SAVE_BEST:
        	if (solver == null) throw new GwtRpcException(MESSAGES.warnSolverNotStarted());
        	if (solver.isWorking()) throw new GwtRpcException(MESSAGES.warnSolverIsWorking());
        	solver.saveBest();
			break;

		case CLEAR:
        	if (solver == null) throw new GwtRpcException(MESSAGES.warnSolverNotStarted());
        	if (solver.isWorking()) throw new GwtRpcException(MESSAGES.warnSolverIsWorking());
        	solver.clear();
			break;

		case SAVE:
		case SAVE_AS_NEW:
		case SAVE_AS_NEW_COMMIT:
		case SAVE_COMMIT:
		case SAVE_UNCOMMIT:
        	if (solver == null) throw new GwtRpcException(MESSAGES.warnSolverNotStarted());
        	if (solver.isWorking()) throw new GwtRpcException(MESSAGES.warnSolverIsWorking());
        	switch (request.getType()) {
			case COURSE:
				Long[] owners = solver.getProperties().getPropertyLongArry("General.SolverGroupId", null);
				context.checkPermission(owners, "SolverGroup", Right.SolverSolutionSave);
				if (request.getOperation() == SolverOperation.SAVE_AS_NEW_COMMIT || request.getOperation() == SolverOperation.SAVE_COMMIT)
					context.checkPermission(owners, "SolverGroup", Right.TimetablesSolutionCommit);
				if (solver.bestSolutionInfo() != null) solver.restoreBest();
	        	((SolverProxy)solver).save(
	        			request.getOperation() == SolverOperation.SAVE_AS_NEW || request.getOperation() == SolverOperation.SAVE_AS_NEW_COMMIT,
	        			request.getOperation() == SolverOperation.SAVE_AS_NEW_COMMIT || request.getOperation() == SolverOperation.SAVE_COMMIT
	        			);
				break;
			case EXAM:
				solver.save();
				break;
			case STUDENT:
	        	SolverParameterDef statusToSet = SolverParameterDef.findByNameType("Save.StudentSectioningStatusToSet", SolverParameterGroup.SolverType.STUDENT);
	        	if (statusToSet != null) {
	        		solver.setProperty("Save.StudentSectioningStatusToSet", request.getParameter(statusToSet.getUniqueId()));
	        	}
				solver.save();
				break;
			case INSTRUCTOR:
				solver.setProperty("Save.Commit", (request.getOperation() == SolverOperation.SAVE_AS_NEW_COMMIT || request.getOperation() == SolverOperation.SAVE_COMMIT) ? "true" : "false");
				solver.save();
        	}
        	break;
        	
		case UNLOAD:
        	if (solver == null) throw new GwtRpcException(MESSAGES.warnSolverNotStarted());
        	if (solver.isWorking()) throw new GwtRpcException(MESSAGES.warnSolverIsWorking());
        	service.removeSolver();
        	request.clear();
        	response.setRefresh(true);
        	solver = null;
        	break;
        	
		case RELOAD:
        	if (solver == null) throw new GwtRpcException(MESSAGES.warnSolverNotStarted());
        	if (solver.isWorking()) throw new GwtRpcException(MESSAGES.warnSolverIsWorking());
        	service.reload(service.createConfig(request.getConfigurationId(), request.getParameters()));
        	response.setRefresh(true);
        	break;
        	
		case START:
		case LOAD:
			if (solver != null && solver.isWorking()) throw new GwtRpcException(MESSAGES.warnSolverIsWorking());
			
			Long settingsId = request.getConfigurationId();
			DataProperties config = service.createConfig(settingsId, request.getParameters());
			if (request.getHost() != null)
				config.setProperty("General.Host", request.getHost());
			config.setProperty("General.StartSolver", request.getOperation() == SolverOperation.START ? "true" : "false");
			if (request.getType() == SolverType.COURSE) {
	        	String solutionId = (String)context.getAttribute("Solver.selectedSolutionId");
	    	    if (solutionId != null)
	    	    	config.setProperty("General.SolutionId", solutionId);
			}
			if (request.hasOwerIds()) {
				String ownerIds = "";
				for (Long ownerId: request.getOwnerIds())
					ownerIds += (ownerIds.isEmpty() ? "" : ",") + ownerId;
				if (request.getType() == SolverType.COURSE)
					config.setProperty("General.SolverGroupId", ownerIds);	
				else if (request.getType() == SolverType.INSTRUCTOR)
					config.setProperty("General.SolverGroupId", ownerIds);
				else if (request.getType() == SolverType.EXAM)
					config.setProperty("Exam.Type", ownerIds);
			}
			if (solver == null) {
				solver = service.createSolver(config);
			} else {
        		solver.setProperties(config);
        		solver.start();
        	}
			response.setRefresh(true);
			break;
			
		case STOP:
			if (solver==null) throw new GwtRpcException(MESSAGES.warnSolverNotStarted());
			if (solver.isRunning()) solver.stopSolver();
			break;
		
		case STUDENT_SECTIONING:
        	if (solver == null) throw new GwtRpcException(MESSAGES.warnSolverNotStarted());
        	if (solver.isWorking()) throw new GwtRpcException(MESSAGES.warnSolverIsWorking());
        	if (request.getType() == SolverType.COURSE)
        		((SolverProxy)solver).finalSectioning();
        	break;
        	
		case INIT:
			response.setRefresh(true);
			break;
		}
		
		return solver;
	}
	
	protected void fillHosts(SessionContext context, SolverPageRequest request, SolverPageResponse response) {
		if (context.hasPermission(Right.CanSelectSolverServer)) {
			for (SolverServer server: solverServerService.getServers(true))
				response.addHost(server.getHost());
			if (response.hasHosts())
				Collections.sort(response.getHosts());
			if (ApplicationProperty.SolverLocalEnabled.isTrue() && (!response.hasHosts() || !response.getHosts().contains("local")))
				response.addHost(0, "local");
			response.addHost(0, "auto");
		}
	}
	
	protected void fillOwners(SessionContext context, SolverPageRequest request, SolverPageResponse response) {
		switch (request.getType()) {
		case COURSE:
			for (SolverGroup owner: SolverGroup.getUserSolverGroups(context.getUser())) {
				if (context.hasPermission(owner, Right.TimetablesSolutionLoadEmpty))
					response.addSolverOwner(new SolverOwner(owner.getUniqueId(), owner.getName()));
			}
			if (response.hasSolverOwners() && response.getSolverOwners().size() == 1)
				response.addOwnerId(response.getSolverOwners().get(0).getId());
			else if (response.hasSolverOwners() && response.getSolverOwners().size() > 1)
				response.setAllowMultipleOwners(true);
			break;
		case EXAM:
			for (ExamType type: ExamType.findAllUsedApplicable(context.getUser(), DepartmentStatusType.Status.ExamTimetable)) {
				response.addSolverOwner(new SolverOwner(type.getUniqueId(), type.getLabel()));
			}
			break;
		case INSTRUCTOR:
			for (SolverGroup owner: SolverGroup.getUserSolverGroups(context.getUser())) {
				if (context.hasPermission(owner, Right.InstructorScheduling))
					response.addSolverOwner(new SolverOwner(owner.getUniqueId(), owner.getName()));
			}
			break;			
		}
	}
	
	protected void fillParameters(SessionContext context, SolverPageRequest request, SolverPageResponse response) {
		SolverParameterGroup.SolverType type = null;
		String group = null;
		int appearance = 0;
		String defaultConfig = null;
		switch (request.getType()) {
		case COURSE:
			type = SolverParameterGroup.SolverType.COURSE;
			group = "Basic";
			appearance = SolverPredefinedSetting.APPEARANCE_SOLVER;
			defaultConfig = "Default.Solver";
			break;
		case EXAM:
			type = SolverParameterGroup.SolverType.EXAM;
			group = "ExamBasic";
			appearance = SolverPredefinedSetting.APPEARANCE_EXAM_SOLVER;
			defaultConfig = "Exam.Default";
			break;
		case STUDENT:
			type = SolverParameterGroup.SolverType.STUDENT;
			group = "StudentSctBasic";
			appearance = SolverPredefinedSetting.APPEARANCE_STUDENT_SOLVER;
			defaultConfig = "StudentSct.Default";
			break;
		case INSTRUCTOR:
			type = SolverParameterGroup.SolverType.INSTRUCTOR;
			group = "InstrSchd.Basic";
			appearance = SolverPredefinedSetting.APPEARANCE_INSTRUCTOR_SOLVER;
			defaultConfig = "InstrSchd.Default";
			break;
		default:
			 throw new IllegalArgumentException(MESSAGES.errorSolverInvalidType(request.getType().name()));
		}
		List<SolverParameterDef> parameters = (List<SolverParameterDef>)SolverParameterDefDAO.getInstance().getSession().createQuery(
				"from SolverParameterDef d where d.visible = true and d.group.type = :type and d.group.name = :group order by d.order")
				.setInteger("type", type.ordinal()).setString("group", group).setCacheable(true).list();
		for (SolverParameterDef def: parameters) {
			SolverParameter p = new SolverParameter();
			p.setId(def.getUniqueId());
			p.setKey(def.getName());
			p.setName(def.getDescription());
			p.setDefaultValue(def.getDefault());
			p.setType(def.getType());
			if (request.hasParameters())
				p.setValue(request.getParameter(p.getId()));
			response.addParameter(p);
		}
		List<SolverPredefinedSetting> configs = (List<SolverPredefinedSetting>)SolverPredefinedSettingDAO.getInstance().getSession().createQuery(
				"from SolverPredefinedSetting s where s.appearance = :appearance"
				).setInteger("appearance", appearance).setCacheable(true).list();
		
		response.setConfigurationId(request.getConfigurationId());
		for (SolverPredefinedSetting config: configs) {
			SolverConfiguration c = new SolverConfiguration();
			c.setId(config.getUniqueId());
			c.setName(config.getDescription());
			for (org.unitime.timetable.model.SolverParameter p: config.getParameters()) {
				if (p.getDefinition().isVisible() && p.getDefinition().getGroup().getType() == type.ordinal() && group.equals(p.getDefinition().getGroup().getName())) {
					c.addParameter(p.getDefinition().getUniqueId(), p.getValue());
				}
			}
			response.addConfiguration(c);
			if (request.getConfigurationId() == null && defaultConfig.equals(config.getName())) {
				response.setConfigurationId(c.getId());
				for (org.unitime.timetable.model.SolverParameter p: config.getParameters()) {
					if (p.getDefinition().isVisible() && p.getDefinition().getGroup().getType() == type.ordinal() && group.equals(p.getDefinition().getGroup().getName())) {
						response.getParameter(p.getDefinition().getUniqueId()).setValue(p.getValue());
					}
				}
			}
		}
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
	
	protected void fillSolverInfos(SessionContext context, CommonSolverInterface solver, SolverPageRequest request, SolverPageResponse response) {
		Map<String, String> info = (solver == null ? null : solver.currentSolutionInfo());
		if (info != null) {
			SolutionInfo si = new SolutionInfo();
			TreeSet<String> keys = new TreeSet<String>(new ListSolutionsForm.InfoComparator());
			keys.addAll(info.keySet());
			for (String key: keys)
				si.addPair(key, info.get(key));
			response.setCurrentSolution(si);
		}
		Map<String, String> best = (solver == null ? null : solver.bestSolutionInfo());
		if (best != null) {
			SolutionInfo si = new SolutionInfo();
			TreeSet<String> keys = new TreeSet<String>(new ListSolutionsForm.InfoComparator());
			keys.addAll(best.keySet());
			for (String key: keys)
				si.addPair(key, best.get(key));
			response.setBestSolution(si);
		}
		if (solver != null) {
			if (response.hasOwerIds()) response.getOwnerIds().clear();
			switch (request.getType()) {
			case COURSE:
				Long[] owners = solver.getProperties().getPropertyLongArry("General.SolverGroupId", null);
				if (owners != null)
					for (Long owner: owners)
						response.addOwnerId(owner);
				break;
			case EXAM:
				Long owner = solver.getProperties().getPropertyLong("Exam.Type", null);
				if (owner != null)
					response.addOwnerId(owner);
				break;
			case INSTRUCTOR:
				owner = solver.getProperties().getPropertyLong("General.SolverGroupId", null);
				if (owner != null)
					response.addOwnerId(owner);
				break;
			}
		}
		if (solver == null && request.getType() == SolverType.COURSE) {
			String id = (String)context.getAttribute("Solver.selectedSolutionId");
			if (id != null && !id.isEmpty()) {
				for (StringTokenizer s = new StringTokenizer(id,","); s.hasMoreTokens();) {
	 				Solution solution = SolutionDAO.getInstance().get(Long.valueOf(s.nextToken()));
	 				if (solution == null) continue;
	 				SolutionInfo si = new SolutionInfo();
	 				si.setName(solution.getOwner().getName());
					LogInfo logInfo = (LogInfo)solution.getInfo("LogInfo");
					if (logInfo != null)
						for (Progress.Message m: logInfo.getLog()) {
							if (m.getLevel() == ProgressLogLevel.STAGE.ordinal() && "Loading input data ...".equals(m.getMessage()) && si.hasLog())
								si.getLog().clear();
							if (m.getLevel() >= ProgressLogLevel.WARN.ordinal())
								si.addMessage(m.getLevel(), m.getDate(), m.getMessage(), m.getTrace());
						}
	 				PropertiesInfo propInfo = (PropertiesInfo)solution.getInfo("GlobalInfo");
	 				if (propInfo != null) {
	 					TreeSet<String> keys = new TreeSet<String>(new ListSolutionsForm.InfoComparator());
	 					for (Object o: propInfo.keySet()) keys.add((String)o);
	 					for (String key: keys)
	 						si.addPair(key, propInfo.getProperty(key));
	 					response.addSelectedSolution(si);
	 				}
	 				response.addOwnerId(solution.getOwner().getUniqueId());
				}
			}
		}
	}
	
	protected void fillSolverLog(SessionContext context, CommonSolverInterface solver, SolverPageRequest request, SolverPageResponse response) {
		if (solver != null) {
			List<Progress.Message> log = solver.getProgressLog(ProgressLogLevel.WARN.ordinal(), "Loading input data ...", null);
			if (log != null)
				for (Progress.Message m: log)
					response.addMessage(m.getLevel(), m.getDate(), m.getMessage(), m.getTrace());
		}
	}
	
	protected void fillSolverStatus(SessionContext context, CommonSolverInterface solver, SolverPageRequest request, SolverPageResponse response) {
		if (solver != null) {
			response.setLoadDate(solver.getLoadedDate());
			response.setWorking(solver.isRunning() || solver.isWorking());
			try {
				Map p = solver.getProgress();
				response.setSolverStatus((String)p.get("STATUS"));
				long progressMax = ((Long)p.get("MAX_PROGRESS")).longValue();
				if (progressMax > 0) {
					String progress = (String)p.get("PHASE");
					long progressCur = ((Long)p.get("PROGRESS")).longValue();
					double progressPercent = 100.0*((double)(progressCur < progressMax ? progressCur : progressMax)) / ((double)progressMax);
					progress += " ("+new DecimalFormat("0.0").format(progressPercent)+"%)";
					response.setSolverProgress(progress);
				}
			} catch (Exception e) {}
			DataProperties config = solver.getProperties();
			response.setConfigurationId(config.getPropertyLong("General.SettingsId", null));
			if (response.hasParameters()) {
				for (SolverParameter p :response.getParameters())
					p.setValue(config.getProperty(p.getKey()));
			}
		} else {
			response.setSolverStatus(MESSAGES.solverStatusNotStarted());
		}
	}
	
	protected void fillAvailableOperations(SessionContext context, CommonSolverInterface solver, SolverPageRequest request, SolverPageResponse response) {
		if (solver == null) {
			response.setCanExecute(SolverOperation.LOAD, SolverOperation.START, SolverOperation.CHECK);
		} else {
			response.setCanExecute(SolverOperation.CHECK);
			if (solver.isRunning())
				response.setCanExecute(SolverOperation.STOP);
			else if (!solver.isWorking()) {
				response.setCanExecute(SolverOperation.START, SolverOperation.UNLOAD, SolverOperation.SAVE_BEST, SolverOperation.RELOAD);
				if (solver.bestSolutionInfo() != null)
					response.setCanExecute(SolverOperation.RESTORE_BEST);
				switch (request.getType()) {
				case COURSE:
					Long[] owners = solver.getProperties().getPropertyLongArry("General.SolverGroupId", null);
					if (context.hasPermission(owners, "SolverGroup", Right.SolverSolutionExportCsv))
						response.setCanExecute(SolverOperation.EXPORT_CSV);
					if (context.hasPermission(owners, "SolverGroup", Right.SolverSolutionExportXml))
						response.setCanExecute(SolverOperation.EXPORT_XML);
					if (((SolverProxy)solver).hasFinalSectioning())
						response.setCanExecute(SolverOperation.STUDENT_SECTIONING);
					Long[] iSolutionIds = solver.getProperties().getPropertyLongArry("General.SolutionId",null);
					boolean hasSolution = false;
					boolean canOverwrite = true;
					if (iSolutionIds != null && iSolutionIds.length > 0) {
						for (int i = 0; i < iSolutionIds.length; i++) {
							Solution solution = (iSolutionIds[i] == null ? null : SolutionDAO.getInstance().get(iSolutionIds[i]));
							if (solution != null) {
								hasSolution = true;
								if (solution.getCommited()) canOverwrite = false;
							}
						}
					}
					response.setCanExecute(SolverOperation.SAVE_AS_NEW);
					if (hasSolution && canOverwrite)
						response.setCanExecute(SolverOperation.SAVE);
					if (context.hasPermission(owners, "SolverGroup", Right.TimetablesSolutionCommit)) {
						response.setCanExecute(SolverOperation.SAVE_AS_NEW_COMMIT);
						if (hasSolution)
							response.setCanExecute(SolverOperation.SAVE_COMMIT);
					}
					break;
				case EXAM:
					response.setCanExecute(SolverOperation.CLEAR);
					if (context.hasPermission(Right.ExaminationSolutionExportXml))
						response.setCanExecute(SolverOperation.EXPORT_XML);
					hasSolution = Exam.hasTimetable(context.getUser().getCurrentAcademicSessionId());
					response.setCanExecute(hasSolution ? SolverOperation.SAVE : SolverOperation.SAVE_AS_NEW);
					break;
				case STUDENT:
					response.setCanExecute(SolverOperation.CLEAR);
					if (context.hasPermission(Right.StudentSectioningSolutionExportXml))
						response.setCanExecute(SolverOperation.EXPORT_XML);
					hasSolution = Session.hasStudentSchedule(context.getUser().getCurrentAcademicSessionId());
					response.setCanExecute(hasSolution ? SolverOperation.SAVE : SolverOperation.SAVE_AS_NEW);
					break;
				case INSTRUCTOR:
					response.setCanExecute(SolverOperation.CLEAR);
					if (context.hasPermission(Right.InstructorSchedulingSolutionExportXml))
						response.setCanExecute(SolverOperation.EXPORT_XML);
					if (solver.getProperties().getPropertyBoolean("Save.Commit", false))
						response.setCanExecute(SolverOperation.SAVE_UNCOMMIT);
					else
						response.setCanExecute(SolverOperation.SAVE);
					response.setCanExecute(SolverOperation.SAVE_COMMIT);
				}
			}
		}
	}
	
	protected void fillSolverWarnings(SessionContext context, CommonSolverInterface solver, SolverPageRequest request, SolverPageResponse response) {
		fillSolverWarnings(context, solver, request.getType(), response);
	}
	
	public static void fillSolverWarnings(SessionContext context, CommonSolverInterface solver, SolverType solverType, HasPageMessages response) {
		switch (solverType) {
		case EXAM:
			if (solver != null) {
				ExamType type = ExamTypeDAO.getInstance().get(solver.getProperties().getPropertyLong("Exam.Type", null));
				if (type != null) {
					String ts = solver.getProperties().getProperty("RoomAvailability.TimeStamp");
                    if (ts == null)
                    	response.addPageMessage(new PageMessage(PageMessageType.WARNING, MESSAGES.warnExamSolverNoRoomAvailability(type.getLabel().toLowerCase())));
                    else
                    	response.addPageMessage(new PageMessage(PageMessageType.INFO, MESSAGES.infoExamSolverRoomAvailabilityLastUpdated(type.getLabel().toLowerCase(), ts)));
					response.addPageMessage(new PageMessage(PageMessageType.INFO, MESSAGES.infoExamSolverShowingSolution(type.getLabel()), "gwt.jsp?page=solver&type=exam"));
				}
			}
			break;
		case COURSE:
			if (solver != null) {
				String warn = solver.getProperties().getProperty("General.SolverWarnings");
				if (warn != null && !warn.isEmpty())
					response.addPageMessage(new PageMessage(PageMessageType.WARNING, warn));
				Long[] solverGroupId = solver.getProperties().getPropertyLongArry("General.SolverGroupId", null);
				List<String> names = new ArrayList<String>();
				boolean interactive = solver.getProperties().getPropertyBoolean("General.InteractiveMode", false);
				if (solverGroupId != null) {
					for (int i = 0; i < solverGroupId.length; i++) {
						SolverGroup sg = SolverGroupDAO.getInstance().get(solverGroupId[i]);
						names.add(sg == null ? MESSAGES.notApplicable() : solverGroupId.length <= 3 ? sg.getName() : sg.getAbbv());
				   }
				}
				if (names == null || names.isEmpty()) names.add(MESSAGES.notApplicable());
				response.addPageMessage(new PageMessage(PageMessageType.INFO, MESSAGES.infoSolverShowingSolution(toString(names)), interactive ? "listSolutions.do" : "gwt.jsp?page=solver&type=course"));
            	/*
            	String ts = solver.getProperties().getProperty("RoomAvailability.TimeStamp");
            	if (ts==null)
            		response.addPageMessage(new PageMessage(PageMessageType.WARNING, MESSAGES.warnCourseSolverNoRoomAvailability()));
                else
                	response.addPageMessage(new PageMessage(PageMessageType.INFO, MESSAGES.infoCourseSolverRoomAvailabilityLastUpdated(ts)));
                */
			} else {
				String id = (String)context.getAttribute("Solver.selectedSolutionId");
				String warn = "";
				if (id != null && !id.isEmpty()) {
					List<String> names = new ArrayList<String>();
					String[] solutionIds = id.split(",");
					for (int i = 0; i < solutionIds.length; i++) {
						Solution solution = SolutionDAO.getInstance().get(Long.valueOf(solutionIds[i]));
						names.add(solutionIds.length <= 3 ? solution.getOwner().getName() : solution.getOwner().getAbbv());
						for (org.unitime.timetable.model.SolverParameter p: solution.getParameters()) {
							if ("General.SolverWarnings".equals(p.getDefinition().getName()) && p.getValue() != null) {
								if (!warn.isEmpty()) warn += "<br>";
								warn += p.getValue();
							}
						}
					}
					if (warn.isEmpty()) {
						Set<SolverGroup> solverGroups = SolverGroup.getUserSolverGroups(context.getUser());
						if (!solverGroups.isEmpty()) {
							int maxDistPriority = Integer.MIN_VALUE;
							int nrWarns = 0;
							for (SolverGroup sg: solverGroups)
								maxDistPriority = Math.max(maxDistPriority, sg.getMaxDistributionPriority());
							for (SolverGroup sg: SolverGroup.findBySessionId(context.getUser().getCurrentAcademicSessionId())) {
								if (solverGroups.contains(sg)) continue;
								if (sg.getMinDistributionPriority() < maxDistPriority && sg.getCommittedSolution() == null) {
									if (nrWarns > 0) warn += "<br>";
									boolean dept = false;
									List<String> subjects = new ArrayList<String>();
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
										warn += MESSAGES.warnSolverNoCommittedSolutionDepartmental(sg.getAbbv(), toString(subjects));
									else
										warn += MESSAGES.warnSolverNoCommittedSolutionExternal(sg.getAbbv(), toString(subjects));
									nrWarns++;
									if (nrWarns >= 3) {
										warn += "<br>...";
										break;
									}
								}
							}
						}
					}
					if (warn != null && !warn.isEmpty())
						response.addPageMessage(new PageMessage(PageMessageType.WARNING, warn));
					response.addPageMessage(new PageMessage(PageMessageType.INFO, (names.size() == 1 ? MESSAGES.infoSolverShowingSelectedSolution(names.get(0)) : MESSAGES.infoSolverShowingSelectedSolutions(toString(names))), "listSolutions.do"));
				}
			}
			break;
		}
	}

	public static String toString(Collection<String> items) {
		if (items == null || items.isEmpty()) return "";
		if (items.size() == 1) return items.iterator().next();
		if (items.size() == 2) {
			Iterator<String> i = items.iterator();
			return MESSAGES.itemSeparatorPair(i.next(), i.next());
		} else {
			Iterator<String> i = items.iterator();
			String list = i.next();
			while (i.hasNext()) {
				String item = i.next();
				if (i.hasNext())
					list = MESSAGES.itemSeparatorMiddle(list, item);
				else
					list = MESSAGES.itemSeparatorLast(list, item);
			}
			return list;
		}
	}
}
