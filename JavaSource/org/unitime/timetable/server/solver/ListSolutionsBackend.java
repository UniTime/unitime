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

import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.ListSolutionsRequest;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.ListSolutionsResponse;
import org.unitime.timetable.gwt.shared.CourseTimetablingSolverInterface.SolutionOperation;
import org.unitime.timetable.gwt.shared.SolverInterface.ProgressLogLevel;
import org.unitime.timetable.gwt.shared.SolverInterface.SolutionInfo;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverConfiguration;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverOwner;
import org.unitime.timetable.gwt.shared.SolverInterface.SolverType;
import org.unitime.timetable.gwt.shared.TableInterface.TableCellInterface;
import org.unitime.timetable.gwt.shared.TableInterface.TableHeaderIterface;
import org.unitime.timetable.gwt.shared.TableInterface.TableRowInterface;
import org.unitime.timetable.interfaces.ExternalSolutionCommitAction;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SolverParameter;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.dao.SolutionDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.model.dao.SolverPredefinedSettingDAO;
import org.unitime.timetable.security.Qualifiable;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.jgroups.SolverServer;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.ui.LogInfo;
import org.unitime.timetable.solver.ui.PropertiesInfo;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.Formats.Format;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.cpsolver.ifs.util.DataProperties;
import org.cpsolver.ifs.util.Progress;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.ListSolutionsForm;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(ListSolutionsRequest.class)
public class ListSolutionsBackend implements GwtRpcImplementation<ListSolutionsRequest, ListSolutionsResponse> {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	protected static Format<Date> sTS = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
	
	@Autowired SolverServerService solverServerService;

	@Override
	public ListSolutionsResponse execute(ListSolutionsRequest request, SessionContext context) {
		context.checkPermission(Right.Timetables);
		
		SolverProxy solver = courseTimetablingSolverService.getSolver();
		
		ListSolutionsResponse response = new ListSolutionsResponse();
		response.setOperation(request.getOperation());
		
		Set<Long> selectedSolutionIds = new HashSet<Long>();
		String solutionIdsStr = (String)context.getAttribute(SessionAttribute.SelectedSolution);
		if (solutionIdsStr != null)
			for (String solutionId: solutionIdsStr.split(","))
				if (!solutionId.isEmpty())
					selectedSolutionIds.add(Long.valueOf(solutionId));
		
		Transaction tx = null;
		org.hibernate.Session hibSession = SolutionDAO.getInstance().getSession();
		try {
			switch (request.getOperation()) {
			case UPDATE_NOTE:
				if (request.hasSolutionIds()) {
					tx = hibSession.beginTransaction();
					for (Long solutionId: request.getSolutionIds()) {
						Solution solution = SolutionDAO.getInstance().get(solutionId, hibSession);
						context.checkPermission(solution, Right.TimetablesSolutionChangeNote);
						String note = request.getNote();
	       				if (note != null && note.length() > 1000)
	       					note = note.substring(0,1000);
	       				solution.setNote(note);
	       				hibSession.saveOrUpdate(solution);
					}
				}
				break;
			case COMMIT:
				if (request.hasSolutionIds()) {
					tx = hibSession.beginTransaction();
					Set<Long> ids = new HashSet<Long>();
					for (Long solutionId: request.getSolutionIds()) {
						Solution solution = SolutionDAO.getInstance().get(solutionId, hibSession);
						context.checkPermission(solution.getOwner(), Right.TimetablesSolutionCommit);
						ids.add(solutionId);
						List<Solution> solutions = hibSession.createCriteria(Solution.class).add(Restrictions.eq("owner",solution.getOwner())).list();
						HashSet<Solution> touchedSolutionSet = new HashSet<Solution>();
						for (Solution s: solutions) {
            				if (s.equals(solution)) continue;
            				if (s.isCommited()) {
             					touchedSolutionSet.add(s);
             					ids.add(s.getUniqueId());
            				}
            			}
            			touchedSolutionSet.add(solution);
            			if (context.hasPermission(solution, Right.TimetablesSolutionChangeNote)) solution.setNote(request.getNote());
            			response.setSuccess(solution.commitSolution(response.getErrors(), hibSession, context.getUser().getExternalUserId()));
            			hibSession.update(solution);
            	    	String className = ApplicationProperty.ExternalActionSolutionCommit.value();
            	    	if (className != null && !className.isEmpty()) {
            	    		ExternalSolutionCommitAction commitAction = (ExternalSolutionCommitAction) (Class.forName(className).newInstance());              	    		
            	    		commitAction.performExternalSolutionCommitAction(touchedSolutionSet, hibSession);
            	    	}
					}
					solverServerService.getLocalServer().refreshCourseSolution(ids.toArray(new Long[ids.size()]));
				}
				break;
			case UNCOMMIT:
				if (request.hasSolutionIds()) {
					Set<Long> ids = new HashSet<Long>();
					tx = hibSession.beginTransaction();
					for (Long solutionId: request.getSolutionIds()) {
						Solution solution = SolutionDAO.getInstance().get(solutionId, hibSession);
						context.checkPermission(solution.getOwner(), Right.TimetablesSolutionCommit);
						ids.add(solutionId);
						if (context.hasPermission(solution, Right.TimetablesSolutionChangeNote)) solution.setNote(request.getNote());
						solution.uncommitSolution(hibSession, context.getUser().getExternalUserId());
            			String className = ApplicationProperty.ExternalActionSolutionCommit.value();
            			if (className != null && !className.isEmpty()) {
            	    		ExternalSolutionCommitAction commitAction = (ExternalSolutionCommitAction) (Class.forName(className).newInstance());
            	    		HashSet<Solution> solutions = new HashSet<Solution>();
            	    		solutions.add(solution);
            	    		commitAction.performExternalSolutionCommitAction(solutions, hibSession);
            	    	}
					}
					solverServerService.getLocalServer().refreshCourseSolution(ids.toArray(new Long[ids.size()]));
				}
				break;
			case DELETE:
				if (request.hasSolutionIds()) {
					tx = hibSession.beginTransaction();
					for (Long solutionId: request.getSolutionIds()) {
						Solution solution = SolutionDAO.getInstance().get(solutionId, hibSession);
						if (solution.isCommited()) {
            				context.checkPermission(solution.getOwner(), Right.TimetablesSolutionCommit);
            				solution.uncommitSolution(hibSession, context.getUser().getExternalUserId());
                	    	String className = ApplicationProperty.ExternalActionSolutionCommit.value();
                	    	if (className != null && !className.isEmpty()) {
                	    		ExternalSolutionCommitAction commitAction = (ExternalSolutionCommitAction) (Class.forName(className).newInstance());
                	    		HashSet<Solution> solutions = new HashSet<Solution>();
                	    		solutions.add(solution);
                	    		commitAction.performExternalSolutionCommitAction(solutions, hibSession);
                	    	}
            			}
						context.checkPermission(solution, Right.TimetablesSolutionDelete);
						selectedSolutionIds.remove(solution.getUniqueId());
						solution.delete(hibSession);
					}
				}
				break;
			case LOAD:
				if (solver != null && solver.isWorking()) throw new GwtRpcException(MESSAGES.warnSolverIsWorking());
				if (selectedSolutionIds.isEmpty()) throw new GwtRpcException(MESSAGES.errorListSolutionsNoSolutionSelected());
				context.checkPermission(new ArrayList<Long>(selectedSolutionIds), "Solution", Right.TimetablesSolutionLoad);
				String solutionIds = "", ownerIds = "";
				for (Long solutionId: selectedSolutionIds) {
					Solution solution = SolutionDAO.getInstance().get(solutionId, hibSession);
					solutionIds = (solutionIds.isEmpty() ? "" : ",") + solution.getUniqueId();
					ownerIds = (ownerIds.isEmpty() ? "": ",") + solution.getOwner().getUniqueId();
				}
				DataProperties configLoad = courseTimetablingSolverService.createConfig(request.getConfigurationId(), null);
				configLoad.setProperty("General.SolverGroupId", ownerIds);
				configLoad.setProperty("General.SolutionId", solutionIds);
	    	    if (request.getHost() != null) configLoad.setProperty("General.Host", request.getHost());
	    	    solver = courseTimetablingSolverService.createSolver(configLoad);
				break;
			case LOAD_EMPTY:
				if (solver != null && solver.isWorking()) throw new GwtRpcException(MESSAGES.warnSolverIsWorking());
				if (!request.hasOwner()) throw new GwtRpcException(MESSAGES.errorListSolutionsNoOwnerSelected());
				context.checkPermission(request.getOwnerId(), "SolverGroup", Right.TimetablesSolutionLoadEmpty);
				DataProperties configLoadEmpty = courseTimetablingSolverService.createConfig(request.getConfigurationId(), null);
				configLoadEmpty.setProperty("General.SolverGroupId", request.getOwnerId().toString());
	    	    if (request.getHost() != null) configLoadEmpty.setProperty("General.Host", request.getHost());
	    	    solver = courseTimetablingSolverService.createSolver(configLoadEmpty);
				break;
			case SELECT:
				if (request.hasSolutionIds()) {
					for (Long solutionId: request.getSolutionIds()) {
						Solution solution = SolutionDAO.getInstance().get(solutionId, hibSession);
						if (solution != null) {
							for (Iterator<Long> i = selectedSolutionIds.iterator(); i.hasNext(); ) {
								Solution other = SolutionDAO.getInstance().get(i.next(), hibSession);
								if (other != null && other.getOwner().equals(solution.getOwner())) i.remove();
							}
							selectedSolutionIds.add(solutionId);
						}
					}
				}
				break;
			case DESELECT:
				if (request.hasSolutionIds()) {
					for (Long solutionId: request.getSolutionIds())
						selectedSolutionIds.remove(solutionId);
				}
				break;
			case UNLOAD:
				if (solver == null) throw new GwtRpcException(MESSAGES.warnSolverNotStarted());
	        	if (solver.isWorking()) throw new GwtRpcException(MESSAGES.warnSolverIsWorking());
	        	courseTimetablingSolverService.removeSolver();
	        	solver = null;
	        	break;
			case RELOAD:
				if (solver == null) throw new GwtRpcException(MESSAGES.warnSolverNotStarted());
	        	if (solver.isWorking()) throw new GwtRpcException(MESSAGES.warnSolverIsWorking());
				courseTimetablingSolverService.reload(courseTimetablingSolverService.createConfig(solver.getProperties().getPropertyLong("General.SettingsId", null), null));
				break;
			case SAVE:
			case SAVE_AS_NEW:
			case SAVE_COMMIT:
			case SAVE_AS_NEW_COMMIT:
				if (solver == null) throw new GwtRpcException(MESSAGES.warnSolverNotStarted());
	        	if (solver.isWorking()) throw new GwtRpcException(MESSAGES.warnSolverIsWorking());
	        	solver.setNote(request.getNote());
	        	boolean isNew = (request.getOperation() == SolutionOperation.SAVE_AS_NEW || request.getOperation() == SolutionOperation.SAVE_AS_NEW_COMMIT);
	        	boolean isCommit = (request.getOperation() == SolutionOperation.SAVE_COMMIT || request.getOperation() == SolutionOperation.SAVE_AS_NEW_COMMIT);
	        	courseTimetablingSolverService.getSolver().save(isNew, isCommit);
	        	break;
			}
			if (tx != null && tx.isActive()) tx.commit();
		} catch (Exception e) {
			if (tx != null && tx.isActive()) tx.rollback();
			throw new GwtRpcException(e.getMessage(), e);
		}
		
		solutionIdsStr = "";
		for (Long solutionId: selectedSolutionIds)
			solutionIdsStr += (solutionIdsStr.isEmpty() ? "" : ",") + solutionId;
		context.setAttribute(SessionAttribute.SelectedSolution, solutionIdsStr.isEmpty() ? null : solutionIdsStr);
		
		SolverPageBackend.fillSolverWarnings(context, solver, SolverType.COURSE, response);
				
		fillSelectedSolutions(response, context, selectedSolutionIds, solver);
		fillSolutions(response, context, selectedSolutionIds);
		fillHosts(response, context);
		fillConfigurations(response, context);
		fillSolverStatus(response, context, solver);
		fillSolverLog(response, context, solver);
		fillOwners(response, context);
		fillAvailableOperations(response, context, solver);
		fillSolverInfos(response, context, solver);
		
		return response;
	}
	
	protected void fillHosts(ListSolutionsResponse response, SessionContext context) {
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
	
	protected void fillConfigurations(ListSolutionsResponse response, SessionContext context) {
		int appearance = SolverPredefinedSetting.APPEARANCE_TIMETABLES;
		String defaultConfig = "Interactive";

		List<SolverPredefinedSetting> configs = (List<SolverPredefinedSetting>)SolverPredefinedSettingDAO.getInstance().getSession().createQuery(
				"from SolverPredefinedSetting s where s.appearance = :appearance"
				).setInteger("appearance", appearance).setCacheable(true).list();
		
		for (SolverPredefinedSetting config: configs) {
			SolverConfiguration c = new SolverConfiguration();
			c.setId(config.getUniqueId());
			c.setName(config.getDescription());
			response.addConfiguration(c);
			if (response.getConfigurationId() == null && defaultConfig.equals(config.getName()))
				response.setConfigurationId(c.getId());
		}
	}
	
	protected static String fix(String pref) {
		if (!MESSAGES.notApplicable().equals(pref) && pref.indexOf('/') >= 0)
			pref = pref.substring(0, pref.indexOf('/')).trim();
		if (!MESSAGES.notApplicable().equals(pref) && pref.indexOf(' ') >= 0)
			pref = pref.substring(0, pref.indexOf(' ')).trim();
		return pref;
	}
	
	protected void fillSolutions(ListSolutionsResponse response, SessionContext context, Set<Long> selectedIds) {
		boolean committedOnly = !context.hasPermission(Right.Solver);
		boolean listAll = context.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent);
		
		Collection<Solution> solutions = null;
		if (listAll)
			solutions = Solution.findBySessionId(context.getUser().getCurrentAcademicSessionId());
		else {
			List<Serializable> solverGroupIds = new ArrayList<Serializable>();
			for (Qualifiable owner: context.getUser().getCurrentAuthority().getQualifiers("SolverGroup"))
				solverGroupIds.add(owner.getQualifierId());
			solutions = SolutionDAO.getInstance().getSession().createQuery("from Solution where owner.uniqueId in :solverGroupIds").setParameterList("solverGroupIds", solverGroupIds).list();
		}
		
		if (solutions == null || solutions.isEmpty()) {
			response.setMessage(committedOnly ? MESSAGES.errorListSolutionsNoCommitted() : MESSAGES.errorListSolutionsNoSaved());
			return;
		}
		
		for (Solution solution: solutions) {
			if (committedOnly && !solution.isCommited().booleanValue()) continue;
			
			String settings = null;
			String type = null;
			for (Iterator j=solution.getParameters().iterator();j.hasNext();) {
				SolverParameter p = (SolverParameter)j.next();
				if ("General.SettingsId".equals(p.getDefinition().getName())) {
					SolverPredefinedSetting set = SolverPredefinedSettingDAO.getInstance().get(Long.valueOf(p.getValue()));
					if (set != null) settings = set.getDescription();
				}
				if ("Basic.Mode".equals(p.getDefinition().getName())) {
					type = p.getValue();
				}
			}
			type = (settings == null ? type== null ? MESSAGES.listSolutionsUnknown() : type : settings);
			
			PropertiesInfo globalInfo = (PropertiesInfo)solution.getInfo("GlobalInfo");
			
			response.addRow(new TableRowInterface(
					solution.getUniqueId(), selectedIds != null && selectedIds.contains(solution.getUniqueId()),
					new TableCellInterface<Date>(solution.getCreated(), sTS.format(solution.getCreated())),
					new TableCellInterface<String>(type),
					new TableCellInterface<Date>(solution.isCommited() ? solution.getCommitDate() : null, solution.isCommited() ? sTS.format(solution.getCommitDate()) : ""),
					new TableCellInterface<String>(solution.getOwner().getAbbv()).setTitle(solution.getOwner().getName()),
					new TableCellInterface<String>(globalInfo == null ? MESSAGES.listSolutionsUnknown() : fix(globalInfo.getProperty("Assigned variables", MESSAGES.notApplicable()))),
					new TableCellInterface<String>(globalInfo == null ? MESSAGES.listSolutionsUnknown() : fix(globalInfo.getProperty("Overall solution value", MESSAGES.notApplicable()))),
					new TableCellInterface<String>(globalInfo == null ? MESSAGES.listSolutionsUnknown() : fix(globalInfo.getProperty("Time preferences", MESSAGES.notApplicable()))),
					new TableCellInterface<String>(globalInfo == null ? MESSAGES.listSolutionsUnknown() : globalInfo.getProperty("Student conflicts", MESSAGES.notApplicable())
							.replaceAll(" \\[","(").replaceAll("\\]",")").replaceAll(", ",",").replaceAll("hard:","h").replaceAll("distance:","d").replaceAll("commited:","c").replaceAll("committed:","c")),
					new TableCellInterface<String>(globalInfo == null ? MESSAGES.listSolutionsUnknown() : fix(globalInfo.getProperty("Room preferences", MESSAGES.notApplicable()))),
					new TableCellInterface<String>(globalInfo == null ? MESSAGES.listSolutionsUnknown() : fix(globalInfo.getProperty("Distribution preferences", MESSAGES.notApplicable()))),
					new TableCellInterface<String>(globalInfo == null ? MESSAGES.listSolutionsUnknown() : fix(globalInfo.getProperty("Back-to-back instructor preferences", MESSAGES.notApplicable()))),
					new TableCellInterface<String>(globalInfo == null ? MESSAGES.listSolutionsUnknown() : fix(globalInfo.getProperty("Too big rooms", MESSAGES.notApplicable()))),
					new TableCellInterface<String>(globalInfo == null ? MESSAGES.listSolutionsUnknown() : fix(globalInfo.getProperty("Useless half-hours", MESSAGES.notApplicable()))),
					new TableCellInterface<String>(globalInfo == null ? MESSAGES.listSolutionsUnknown() : fix(globalInfo.getProperty("Perturbations: Total penalty", MESSAGES.notApplicable()))),
					new TableCellInterface<String>(solution.getNote())
					));
		}
		
		response.setHeader(
				new TableHeaderIterface(MESSAGES.colCreated()),
				new TableHeaderIterface(MESSAGES.colSolverConfiguration()),
				new TableHeaderIterface(MESSAGES.colCommitted()),
				new TableHeaderIterface(MESSAGES.colOwner()),
				new TableHeaderIterface(MESSAGES.colShortAssignedVariables()),
				new TableHeaderIterface(MESSAGES.colShortTotalValue()),
				new TableHeaderIterface(MESSAGES.colShortTimePref()),
				new TableHeaderIterface(MESSAGES.colShortStudentConflicts()),
				new TableHeaderIterface(MESSAGES.colShortRoomPref()),
				new TableHeaderIterface(MESSAGES.colShortDistPref()),
				new TableHeaderIterface(MESSAGES.colShortInstructorBtbPref()),
				new TableHeaderIterface(MESSAGES.colShortTooBigRooms()),
				new TableHeaderIterface(MESSAGES.colShortUselessHalfHours()),
				new TableHeaderIterface(MESSAGES.colShortPerturbations()),
				new TableHeaderIterface(MESSAGES.colNote()));
	}
	
	protected void fillSelectedSolutions(ListSolutionsResponse response, SessionContext context, Set<Long> selectedIds, SolverProxy solver) {
		if (selectedIds == null) return;
		for (Long solutionId: selectedIds) {
			Solution solution = SolutionDAO.getInstance().get(solutionId);
			if (solution == null) continue;
			SolutionInfo si = new SolutionInfo();
			si.setCreated(sTS.format(solution.getCreated()));
			if (solution.isCommited())
				si.setCommitted(sTS.format(solution.getCommitDate()));
			si.setNote(solution.getNote());
			si.setOwner(solution.getOwner().getName());
			si.setId(solutionId);
			response.setCanExecute(solutionId, SolutionOperation.UPDATE_NOTE, context.hasPermission(solution, Right.TimetablesSolutionChangeNote));
			response.setCanExecute(solutionId, SolutionOperation.COMMIT, !solution.isCommited() && context.hasPermission(solution.getOwner(), Right.TimetablesSolutionCommit));
			response.setCanExecute(solutionId, SolutionOperation.UNCOMMIT, solution.isCommited() &&  context.hasPermission(solution.getOwner(), Right.TimetablesSolutionCommit));
			response.setCanExecute(solutionId, SolutionOperation.DELETE, !solution.isCommited() && context.hasPermission(solution, Right.TimetablesSolutionDelete));
			response.setCanExecute(solutionId, SolutionOperation.EXPORT, context.hasPermission(solution, Right.TimetablesSolutionExportCsv));
			response.setCanExecute(solutionId, SolutionOperation.LOAD, solver == null && context.hasPermission(solution, Right.TimetablesSolutionLoad));
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
		}
	}
	
	protected void fillSolverStatus(ListSolutionsResponse response, SessionContext context, SolverProxy solver) {
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
			if (response.getConfigurationId() != null && response.getConfiguration(response.getConfigurationId()) == null) {
				SolverPredefinedSetting cfg = SolverPredefinedSettingDAO.getInstance().get(response.getConfigurationId());
				if (cfg != null && cfg.getAppearance() == SolverPredefinedSetting.APPEARANCE_SOLVER) {
					SolverConfiguration c = new SolverConfiguration();
					c.setId(cfg.getUniqueId());
					c.setName(cfg.getDescription());
					response.addConfiguration(c);
				}
			}
		} else {
			response.setSolverStatus(MESSAGES.solverStatusNotStarted());
		}
	}
	
	protected void fillSolverLog(ListSolutionsResponse response, SessionContext context, SolverProxy solver) {
		if (solver != null) {
			List<Progress.Message> log = solver.getProgressLog(ProgressLogLevel.WARN.ordinal(), "Loading input data ...", null);
			if (log != null)
				for (Progress.Message m: log)
					response.addMessage(m.getLevel(), m.getDate(), m.getMessage(), m.getTrace());
		}
	}
	
	protected void fillOwners(ListSolutionsResponse response, SessionContext context) {
		for (SolverGroup owner: SolverGroup.getUserSolverGroups(context.getUser())) {
			if (context.hasPermission(owner, Right.TimetablesSolutionLoadEmpty))
				response.addSolverOwner(new SolverOwner(owner.getUniqueId(), owner.getName()));
		}
	}

	protected void fillAvailableOperations(ListSolutionsResponse response, SessionContext context, SolverProxy solver) {
		if (solver != null) {
			response.setCanExecute(-1l, SolutionOperation.CHECK, solver.isWorking());
			response.setCanExecute(-1l, SolutionOperation.UPDATE_NOTE, !solver.isWorking());
			if (!solver.isWorking()) {
				response.setCanExecute(-1l, SolutionOperation.UNLOAD, true);
				response.setCanExecute(-1l, SolutionOperation.RELOAD, true);
				Long[] owners = solver.getProperties().getPropertyLongArry("General.SolverGroupId", null);
				if (context.hasPermission(owners, "SolverGroup", Right.SolverSolutionExportCsv))
					response.setCanExecute(-1l, SolutionOperation.EXPORT, true);
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
				if (context.hasPermission(owners, "SolverGroup", Right.SolverSolutionSave)) {
					response.setCanExecute(-1l, SolutionOperation.SAVE_AS_NEW, true);
					if (hasSolution && canOverwrite)
						response.setCanExecute(-1l, SolutionOperation.SAVE, true);
				}
				if (context.hasPermission(owners, "SolverGroup", Right.TimetablesSolutionCommit)) {
					response.setCanExecute(-1l, SolutionOperation.SAVE_AS_NEW_COMMIT, true);
					if (hasSolution)
						response.setCanExecute(-1l, SolutionOperation.SAVE_COMMIT, true);
				}
			}
		} else {
			response.setCanExecute(-1l, SolutionOperation.LOAD_EMPTY, context.hasPermission(Right.TimetablesSolutionLoadEmpty));
		}
	}
	
	protected void fillSolverInfos(ListSolutionsResponse response, SessionContext context, SolverProxy solver) {
		Map<String, String> info = (solver == null ? null : solver.currentSolutionInfo());
		if (info != null) {
			SolutionInfo si = new SolutionInfo();
			Date loaded = solver.getLoadedDate();
			si.setCreated(loaded == null ? null : sTS.format(loaded)); 
			si.setNote(solver.getNote());
			TreeSet<String> keys = new TreeSet<String>(new ListSolutionsForm.InfoComparator());
			keys.addAll(info.keySet());
			for (String key: keys)
				si.addPair(key, info.get(key));
			response.setCurrentSolution(si);
		}
		if (solver != null) {
			if (response.hasOwerIds()) response.getOwnerIds().clear();
			Long[] owners = solver.getProperties().getPropertyLongArry("General.SolverGroupId", null);
			String ownerString = "";
			if (owners != null)
				for (Long owner: owners) {
					response.addOwnerId(owner);
					SolverGroup g = SolverGroupDAO.getInstance().get(owner);
					if (g != null)
						ownerString += (ownerString.isEmpty() ? "" : "<br>") + g.getName();
				}
			if (!ownerString.isEmpty() && response.hasCurrentSolution())
				response.getCurrentSolution().setOwner(ownerString);
		}
	}

}
