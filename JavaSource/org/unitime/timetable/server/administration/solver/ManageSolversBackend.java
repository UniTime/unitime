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
package org.unitime.timetable.server.administration.solver;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cpsolver.ifs.util.DataProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.client.admin.ManageSolversPage.ManageSolversRequest;
import org.unitime.timetable.gwt.client.admin.ManageSolversPage.ManageSolversResponse;
import org.unitime.timetable.gwt.client.admin.ManageSolversPage.Operation;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.SolverParameterGroup.SolverType;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.model.dao.SolverPredefinedSettingDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.basic.GetInfo;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.CommonSolverInterface;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.instructor.InstructorSchedulingProxy;
import org.unitime.timetable.solver.jgroups.SolverServer;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;

@GwtRpcImplements(ManageSolversRequest.class)
public class ManageSolversBackend implements GwtRpcImplementation<ManageSolversRequest, ManageSolversResponse>{
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	private static Formats.Format<Date> sDF = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
	private static Logger sLog = LogManager.getLogger(ManageSolversBackend.class); 

	@Autowired SolverServerService solverServerService;
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;
	@Autowired SolverService<StudentSolverProxy> studentSectioningSolverService;
	@Autowired SolverService<InstructorSchedulingProxy> instructorSchedulingSolverService;

	@Override
	public ManageSolversResponse execute(ManageSolversRequest request, SessionContext context) {
		context.checkPermission(Right.ManageSolvers);
		switch (request.getOperation()) {
		case COURSE_SOLVERS:
			return getSolverTable(SolverType.COURSE, request, context);
		case SELECT_SOLVER_COURSE:
			context.setAttribute(SessionAttribute.CourseTimetablingUser, request.getId());
			context.removeAttribute(SessionAttribute.CourseTimetablingSolver);
			return new ManageSolversResponse("solver?type=course");
		case UNLOAD_SOLVER_COURSE:
			context.setAttribute(SessionAttribute.CourseTimetablingUser, request.getId());
			context.removeAttribute(SessionAttribute.CourseTimetablingSolver);
			courseTimetablingSolverService.removeSolver();
			return getSolverTable(SolverType.COURSE, request, context);
		case DESELECT_SOLVER_COURSE:
			context.removeAttribute(SessionAttribute.CourseTimetablingUser);
			context.removeAttribute(SessionAttribute.CourseTimetablingSolver);
			return getSolverTable(SolverType.COURSE, request, context);

		case EXAM_SOLVERS:
			return getSolverTable(SolverType.EXAM, request, context);
		case SELECT_SOLVER_EXAM:
			context.setAttribute(SessionAttribute.ExaminationUser, request.getId());
			context.removeAttribute(SessionAttribute.ExaminationSolver);
			return new ManageSolversResponse("solver?type=exam");
		case UNLOAD_SOLVER_EXAM:
			context.setAttribute(SessionAttribute.ExaminationUser, request.getId());
			context.removeAttribute(SessionAttribute.ExaminationSolver);
			examinationSolverService.removeSolver();
			return getSolverTable(SolverType.EXAM, request, context);
		case DESELECT_SOLVER_EXAM:
			context.removeAttribute(SessionAttribute.ExaminationUser);
			context.removeAttribute(SessionAttribute.ExaminationSolver);
			return getSolverTable(SolverType.EXAM, request, context);

		case INSTRUCTOR_SOLVERS:
			return getSolverTable(SolverType.INSTRUCTOR, request, context);
		case SELECT_SOLVER_INSTRUCTOR:
			context.setAttribute(SessionAttribute.InstructorSchedulingUser, request.getId());
			context.removeAttribute(SessionAttribute.InstructorSchedulingSolver);
			return new ManageSolversResponse("solver?type=instructor");
		case UNLOAD_SOLVER_INSTRUCTOR:
			context.setAttribute(SessionAttribute.InstructorSchedulingUser, request.getId());
			context.removeAttribute(SessionAttribute.InstructorSchedulingSolver);
			instructorSchedulingSolverService.removeSolver();
			return getSolverTable(SolverType.INSTRUCTOR, request, context);
		case DESELECT_SOLVER_INSTRUCTOR:
			context.removeAttribute(SessionAttribute.InstructorSchedulingUser);
			context.removeAttribute(SessionAttribute.InstructorSchedulingSolver);
			return getSolverTable(SolverType.INSTRUCTOR, request, context);

		case STUDENT_SOLVERS:
			return getSolverTable(SolverType.STUDENT, request, context);
		case SELECT_SOLVER_STUDENT:
			context.setAttribute(SessionAttribute.StudentSectioningUser, request.getId());
			context.removeAttribute(SessionAttribute.StudentSectioningSolver);
			return new ManageSolversResponse("solver?type=student");
		case UNLOAD_SOLVER_STUDENT:
			context.setAttribute(SessionAttribute.StudentSectioningUser, request.getId());
			context.removeAttribute(SessionAttribute.StudentSectioningSolver);
			studentSectioningSolverService.removeSolver();
			return getSolverTable(SolverType.STUDENT, request, context);
		case DESELECT_SOLVER_STUDENT:
			context.removeAttribute(SessionAttribute.StudentSectioningUser);
			context.removeAttribute(SessionAttribute.StudentSectioningSolver);
			return getSolverTable(SolverType.STUDENT, request, context);
			
		case ONLINE_SOLVERS:
			return getOnlineServers(context);
		case ONLINE_RELOAD:
			OnlineSectioningServer onlineSolver = solverServerService.getOnlineStudentSchedulingContainer().getSolver(request.getId());
			if (onlineSolver != null) onlineSolver.reload();
			return getOnlineServers(context);
		case ONLINE_SHUTDOWN:
			solverServerService.getOnlineStudentSchedulingContainer().unloadSolver(request.getId());
			return getOnlineServers(context);
			
		case SERVERS:
			return getServers(context);
		case SERVER_DISABLE:
			context.checkPermission(Right.SessionIndependent);
			SolverServer server = solverServerService.getServer(request.getId());
        	if (server != null)
        		server.setUsageBase(1000);
        	return getServers(context);
		case SERVER_ENABLE:
			context.checkPermission(Right.SessionIndependent);
			server = solverServerService.getServer(request.getId());
        	if (server != null)
        		server.setUsageBase(0);
        	return getServers(context);
		case SERVER_HIBERNATE:
			context.checkPermission(Right.SessionIndependent);
			server = solverServerService.getServer(request.getId());
			if (server != null)
        		server.reconnectHibernate();
        	else {
        		try {
        			HibernateUtil.reconnect(null);
        		} catch (Exception e) {
        			sLog.error("Failed to reconnect Hiberante: " + e.getMessage(), e);
        		}
        	}
        	return getServers(context);
		case SERVER_SHUTDOWN:
			context.checkPermission(Right.SessionIndependent);
			server = solverServerService.getServer(request.getId());
        	if (server != null)
        		server.shutdown();
        	return getServers(context);
		case SERVER_RECONNECT:
			context.checkPermission(Right.SessionIndependent);
			server = solverServerService.getServer(request.getId());
        	if (server != null)
        		server.reconnect();
        	return getServers(context);
		case SERVER_RESET:
			context.checkPermission(Right.SessionIndependent);
			server = solverServerService.getServer(request.getId());
        	if (server != null)
        		server.reset(false);
        	return getServers(context);
		}
		return null;
	}
	
	public ManageSolversResponse getServers(SessionContext context) {
		ManageSolversResponse response = new ManageSolversResponse();
		TableInterface table = new TableInterface();
		response.setTable(table);
		boolean hasOps = context.hasPermission(Right.SessionIndependent);
		
		table.setId("ManageSolvers[SERVERS]");
		table.setDefaultSortCookie(MESSAGES.colServerHost());
		table.setName(MESSAGES.sectAvailableServers());
		
		LineInterface header = table.addHeader();
		header.addCell(MESSAGES.colServerHost());
		header.addCell(MESSAGES.colServerVersion());
		header.addCell(MESSAGES.colServerStarted());
		header.addCell(MESSAGES.colServerAvailableMemory());
		header.addCell(MESSAGES.colServerNrCores());
		header.addCell(MESSAGES.colServerPing());
		header.addCell(MESSAGES.colServerUsage());
		header.addCell(MESSAGES.colServerNrInstances());
		header.addCell(MESSAGES.colServerActive());
		header.addCell(MESSAGES.colServerWorking());
		header.addCell(MESSAGES.colServerWorking());
		if (hasOps)
			header.addCell(MESSAGES.colOperations());
		for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    		cell.addStyle("white-space: pre-wrap;");
    		cell.setSortable(true);
    	}
		DecimalFormat df = new DecimalFormat("0.00");
		
		for (SolverServer server: solverServerService.getServers(false)) {
			if (!server.isActive()) {
				LineInterface line = table.addLine();
				line.addCell(server.getHost());
				line.addCell(MESSAGES.serverInactive()).addStyle("font-style: italic;");
				continue;
			}
			
			LineInterface line = table.addLine();
			int nrActive = 0;
            int nrPassivated = 0;
            int nrWorking = 0;
            long mem = server.getAvailableMemory();
            long t0 = System.currentTimeMillis();
            long usage = server.getUsage();
            long t1 = System.currentTimeMillis();
            for (String user: server.getCourseSolverContainer().getSolvers()) {
                SolverProxy solver = server.getCourseSolverContainer().getSolver(user);
                if (solver == null) continue;
                if (solver.isPassivated()) {
                    nrPassivated++;
                } else {
                    nrActive++;
                    if (solver.isWorking())
                        nrWorking++;
                }
            }
            for (String user: server.getExamSolverContainer().getSolvers()) {
                ExamSolverProxy solver = server.getExamSolverContainer().getSolver(user);
                if (solver == null) continue;
                if (solver.isPassivated()) {
                    nrPassivated++;
                } else {
                    nrActive++;
                    if (solver.isWorking())
                        nrWorking++;
                }
            }
            String version = server.getVersion();
            Date startTime = server.getStartTime();
            boolean local = server.isLocal();
            int cores = server.getAvailableProcessors();
            
            Set<String> flags = new TreeSet<String>();
            if (local) flags.add(MESSAGES.serverFlagTomcat());
            if (server.isCoordinator()) flags.add(MESSAGES.serverFlagCoordinator());
            if (!server.isAvailable()) flags.add(MESSAGES.serverFlagUnavailable());
            
            line.addCell(server.getHost() + (flags.isEmpty() ? "" : " " + flags.toString())).setComparable(server.getHost());
            if (version == null || "-1".equals(version))
            	line.addCell(MESSAGES.itemNotApplicable()).setComparable(version).addStyle("font-style: italic;");
            else
            	line.addCell(version);
            if (startTime == null)
            	line.addCell(MESSAGES.itemNotApplicable()).setComparable(startTime).addStyle("font-style: italic;");
            else
            	line.addCell(sDF.format(startTime));
            line.addCell(df.format( ((double)mem)/1024/1024)+" MB");
            line.addCell(String.valueOf(cores));
            line.addCell((t1-t0) + " ms").setComparable(t1 - t0);
            line.addCell(String.valueOf(usage)).setComparable(usage);
            line.addCell(String.valueOf(nrActive+nrPassivated)).setComparable(nrActive+nrPassivated);
            line.addCell(String.valueOf(nrActive)).setComparable(nrActive);
            line.addCell(String.valueOf(nrWorking)).setComparable(nrWorking);
            line.addCell(String.valueOf(nrPassivated)).setComparable(nrPassivated);
            if (hasOps) {
            	CellInterface ops = line.addCell().setTextAlignment(Alignment.RIGHT);
            	if (usage >= 1000)
            		ops.addButton().setText(MESSAGES.actionServerEnable())
            			.setUrl("#" + Operation.SERVER_ENABLE.name() + ":" + server.getHost())
            			.setConfirm(MESSAGES.configServerEnable(server.getHost()));
            	else
            		ops.addButton().setText(MESSAGES.actionServerDisable())
        				.setUrl("#" + Operation.SERVER_DISABLE.name() + ":" + server.getHost())
        				.setConfirm(MESSAGES.confirmServerDisable(server.getHost()));
            	if (server.isCoordinator())
            		ops.addButton().setText(MESSAGES.actionServerReset())
        				.setUrl("#" + Operation.SERVER_RESET.name() + ":" + server.getHost())
        				.setConfirm(MESSAGES.confirmServerReset(server.getHost()));
            	if (ApplicationProperty.SolverClusterEnabled.isTrue()) {
            		ops.addButton().setText(MESSAGES.actionServerReconnect())
        				.setUrl("#" + Operation.SERVER_RECONNECT.name() + ":" + server.getHost())
        				.setConfirm(MESSAGES.confirmServerReconnect(server.getHost()));
            	}
            	ops.addButton().setText(MESSAGES.actionServerReconnectHibernate())
					.setUrl("#" + Operation.SERVER_HIBERNATE.name() + ":" + server.getHost())
					.setConfirm(MESSAGES.confirmServerReconnectHibernate());
            	if (!local)
            		ops.addButton().setText(MESSAGES.actionServerShutdown())
						.setUrl("#" + Operation.SERVER_SHUTDOWN.name() + ":" + server.getHost())
						.setConfirm(MESSAGES.confirmServerShutdown(server.getHost()));
            }
		}
		return response;
	}
	
	public ManageSolversResponse getOnlineServers(SessionContext context) {
		ManageSolversResponse response = new ManageSolversResponse();
		TableInterface table = new TableInterface();
		response.setTable(table);
		
		table.setId("ManageSolvers[ONLINE]");
		table.setDefaultSortCookie(MESSAGES.colCreated());
		table.setName(MESSAGES.sectManageSolversOnline());
		
		LineInterface header = table.addHeader();
		header.addCell(MESSAGES.colCreated());
		header.addCell(MESSAGES.colSession());
		header.addCell(MESSAGES.colSolverHost());
		header.addCell(MESSAGES.colSolverMode());
		header.addCell(MESSAGES.colSolverOverallValue());
		header.addCell(MESSAGES.colSolverStudCourseReqs());
		header.addCell(MESSAGES.colSolverStud1stChoice());
		header.addCell(MESSAGES.colSolverStudCompleteStuds());
		header.addCell(MESSAGES.colSolverStudDistanceConfs());
		header.addCell(MESSAGES.colSolverStudTimeOverlaps());
		header.addCell(MESSAGES.colSolverStudAvgDisbalance());
		header.addCell(MESSAGES.colSolverStudDisbOver10());
		header.addCell(MESSAGES.colOperations());
		for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    		cell.addStyle("white-space: pre-wrap;");
    		cell.setSortable(true);
    	}
		
		for (SolverServer server: solverServerService.getServers(true)) {
			for (String sessionId : server.getOnlineStudentSchedulingContainer().getSolvers()) {
				OnlineSectioningServer solver = server.getOnlineStudentSchedulingContainer().getSolver(sessionId);
				if (solver==null) continue;
				DataProperties properties = solver.getConfig();
				if (properties==null) continue;
				
				LineInterface line = table.addLine();
				Date loaded = new Date(solver.getConfig().getPropertyLong("General.StartUpDate", 0));
				if (loaded.getTime() <= 0)
					line.addCell(MESSAGES.itemNotApplicable()).setComparable(loaded)
						.addStyle("font-style: italic;");
				else
					line.addCell(sDF.format(loaded)).setComparable(loaded);
				line.addCell(solver.getAcademicSession().toString());
				line.addCell(solver.getHost());
				line.addCell(solver.getAcademicSession().isSectioningEnabled() ? "Online" : "Assistant");
				
				Map<String,String> info = (solver.isReady() ? solver.execute(solver.createAction(GetInfo.class), null) : null);
				line.addCell(info == null ? null : info.get("Overall solution value"));
				line.addCell(info == null ? null : info.get("Assigned variables"));
				line.addCell(info == null ? null : info.get("Assigned priority course requests"));
				line.addCell(info == null ? null : info.get("Students with complete schedule"));
				line.addCell(info == null ? null : info.get("Student distance conflicts"));
				line.addCell(info == null ? null : info.get("Time overlapping conflicts"));
				line.addCell(info == null ? null : info.get("Average disbalance"));
				line.addCell(info == null ? null : info.get("Sections disbalanced by 10% or more"));
				
				CellInterface ops = line.addCell();
				if (!context.getUser().getAuthorities(context.getUser().getCurrentAuthority().getRole(), new SimpleQualifier("Session", Long.valueOf(sessionId))).isEmpty()) {
                    if (solver.isReady()) {
                    	ops.addButton().setText(MESSAGES.actionOnlineSolverReload())
    						.setUrl("#" + Operation.ONLINE_RELOAD.name() + ":" + sessionId)
    						.setConfirm(MESSAGES.confirmOnlineReload());
                    }
                	ops.addButton().setText(MESSAGES.actionOnlineSolverShutdown())
						.setUrl("#" + Operation.ONLINE_SHUTDOWN.name() + ":" + sessionId)
						.setConfirm(MESSAGES.confrimOnlineShutdown());
				}
			}
		}
		return response;
	}
	
	protected static String getSolverOwner(DataProperties solverProperties) {
    	String owner = solverProperties.getProperty("General.OwnerPuid", null);
    	if (owner != null) {
    		if (owner.startsWith("PUBLISHED_")) return "Published";
    		TimetableManager mgr = TimetableManager.findByExternalId(owner);
    		if (mgr != null)
    			owner = mgr.getShortName();
    	} else {
    		owner = MESSAGES.itemNotApplicable();
    	}
    	Long[] solverGroupId = solverProperties.getPropertyLongArry("General.SolverGroupId",null);
		String problem = null;
		if (solverGroupId != null) {
			problem = "";
			for (int i = 0; i < solverGroupId.length; i++) {
				SolverGroup g = SolverGroupDAO.getInstance().get(solverGroupId[i]);
				if (g != null)
					problem += (i == 0 ? "" : " & ") + g.getAbbv();
			}
		} else {
			Long examTypeId = solverProperties.getPropertyLong("Exam.Type", null);
			if (examTypeId != null) {
				ExamType type = ExamTypeDAO.getInstance().get(examTypeId);
				if (type != null) problem = type.getLabel();
			}
		}
		if (problem == null || problem.isEmpty()) problem = MESSAGES.itemNotApplicable();
		if (MESSAGES.itemNotApplicable().equals(problem)) return owner;
		if (MESSAGES.itemNotApplicable().equals(owner)) return problem;
		if (!owner.equals(problem)) return MESSAGES.solverOwner(owner, problem);
		return owner;
    }
	
	protected static String getSolverSession(DataProperties solverProperties) {
		Long sessionId = solverProperties.getPropertyLong("General.SessionId", null);
		if (sessionId != null) {
			Session session = SessionDAO.getInstance().get(sessionId);
			if (session != null) return session.getLabel();
		}
		return MESSAGES.itemNotApplicable();
	}
	
	protected static String getSolverConfiguration(DataProperties solverProperties) {
		Long settingsId = solverProperties.getPropertyLong("General.SettingsId", null);
		if (settingsId != null) {
			SolverPredefinedSetting setting = SolverPredefinedSettingDAO.getInstance().get(settingsId);
			if (setting != null) return setting.getDescription();
		}
		return solverProperties.getProperty("Basic.Mode",MESSAGES.itemNotApplicable());
	}
	
	protected String getSelectId(SessionContext context, DataProperties solverProperties) {
		Long sessionId = solverProperties.getPropertyLong("General.SessionId", null);
		String ownerId = solverProperties.getProperty("General.OwnerPuid");
		if (sessionId != null && sessionId.equals(context.getUser().getCurrentAcademicSessionId()) && ownerId != null)
			return ownerId;
		return null;
	}
	
	protected static String getSolverStatus(CommonSolverInterface solver) {
		String status = MESSAGES.itemNotApplicable();
		try {
			status = (String)solver.getProgress().get("STATUS");
		} catch (Exception e) {}
		return status;
	}
	
	protected static Operation getSelectOperation(SolverType type) {
		switch (type) {
		case COURSE: return Operation.SELECT_SOLVER_COURSE;
		case EXAM: return Operation.SELECT_SOLVER_EXAM;
		case INSTRUCTOR: return Operation.SELECT_SOLVER_INSTRUCTOR;
		case STUDENT: return Operation.SELECT_SOLVER_STUDENT;
		default: return Operation.SELECT_SOLVER_COURSE;
		}
	}
	
	protected static Operation getUnloadOperation(SolverType type) {
		switch (type) {
		case COURSE: return Operation.UNLOAD_SOLVER_COURSE;
		case EXAM: return Operation.UNLOAD_SOLVER_EXAM;
		case INSTRUCTOR: return Operation.UNLOAD_SOLVER_INSTRUCTOR;
		case STUDENT: return Operation.UNLOAD_SOLVER_STUDENT;
		default: return Operation.UNLOAD_SOLVER_COURSE;
		}
	}
	
	protected ManageSolversResponse getSolverTable(SolverType type, ManageSolversRequest request, SessionContext context) {
		ManageSolversResponse response = new ManageSolversResponse();
		TableInterface table = new TableInterface();
		response.setTable(table);
		
		table.setId("ManageSolvers[" + type.name() + "]");
		table.setDefaultSortCookie(MESSAGES.colSolverCreated());
		table.setName(getTableName(type));
		
		List<SolverProperties> props = SolverProperties.applicable(type);
		
		LineInterface header = table.addHeader();
		for (SolverProperties prop: props)
			header.addCell(prop.getName());
		header.addCell(MESSAGES.colOperations());
		for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    		cell.addStyle("white-space: pre-wrap;");
    		cell.setSortable(true);
    	}
		
		SolverService<? extends CommonSolverInterface> service = getSolverService(type);
		CommonSolverInterface selected = service.getSolverNoSessionCheck();
		String selectedId = (selected == null ? null : selected.getUser());
		
		List<CommonSolverInterface> solvers = new ArrayList<CommonSolverInterface>(service.getSolvers().values());
		for (CommonSolverInterface solver: solvers) {
			if (solver == null) continue;
			DataProperties properties = solver.getProperties();
			if (properties == null) continue;
			Map<String,String> info = solver.bestSolutionInfo();
			if (info == null)
				info = solver.currentSolutionInfo();

			LineInterface line = table.addLine();
			
        	if (selectedId != null && selectedId.equals(solver.getUser())) {
        		if (!selectedId.equals(context.getUser().getExternalUserId()))
        			response.setCanDeselect(true);
        		line.setBgColor("#b7d4fb");
        	}
        	for (SolverProperties prop: props) {
        		SolverProperty<Object> p = (SolverProperty<Object>)prop.getProperty();
        		Object o = p.getValue(solver, type, properties, info);
        		line.addCell(p.getText(o)).setComparable(p.getComparable(o));
        	}
        	
        	CellInterface ops = line.addCell();
        	String selectId = getSelectId(context, properties);
        	if (selectId != null)
        		line.setURL("#" + getSelectOperation(type) + ":" + selectId);
        	ops.addButton()
        		.setText(MESSAGES.actionSolverUnload())
        		.setUrl("#" + getUnloadOperation(type) + ":" + solver.getUser())
        		.setConfirm(MESSAGES.confirmUnloadSolver());
		}
		
		return response;
	}
	
	public static interface SolverProperty<T> {
		public T getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String,String> info);
		public String getText(T value);
		public Comparable getComparable(T value);
		public boolean isVisible();
	}
	
	public static abstract class DateSolverProperty implements SolverProperty<Date> {
		@Override
		public String getText(Date value) { return value == null ? "" : sDF.format(value); }
		public Comparable getComparable(Date value) { return value == null ? new Date(0l) : value; }
		@Override
		public boolean isVisible() { return true; }
	}
	
	public static abstract class StringSolverProperty implements SolverProperty<String> {
		@Override
		public String getText(String value) { return value == null ? "" : value; }
		public Comparable getComparable(String value) { return value == null ? "" : value; }
		@Override
		public boolean isVisible() { return true; }
	}
	
	public static abstract class IntegerSolverProperty implements SolverProperty<Integer> {
		@Override
		public String getText(Integer value) { return value == null ? "" : String.valueOf(value); }
		public Comparable getComparable(Integer value) { return value == null ? 0 : value; }
		@Override
		public boolean isVisible() { return true; }
	}
	
	public static class InfoSolverProperty extends StringSolverProperty {
		private String iName;
		private boolean iStrip;
		public InfoSolverProperty(String name, boolean strip) { iName = name; iStrip = strip; }
		@Override
		public String getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
			if (info == null) return null;
			String ret = info.get(iName);
			if (ret != null && iStrip && ret.indexOf(' ') > 0)
				ret = ret.substring(0, ret.indexOf(' '));
			return ret;
		}		
	}
	
	public static enum SolverProperties {
		CREATED(MESSAGES.colSolverCreated(), new DateSolverProperty() {
			@Override
			public Date getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				return solver.getLoadedDate();
			}
		}),
		LAST_USED(MESSAGES.colSolverLastUsed(), new DateSolverProperty() {
			@Override
			public Date getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				return solver.getLastUsed();
			}
		}),
		SESSION(MESSAGES.colSolverSession(), new StringSolverProperty() {
			@Override
			public String getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				return getSolverSession(properties);
			}
		}),
		HOST(MESSAGES.colSolverHost(), new StringSolverProperty() {
			@Override
			public String getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				return solver.getHost();
			}
		}),
		CONFIG(MESSAGES.colSolverConfigShort(), new StringSolverProperty() {
			@Override
			public String getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				return getSolverConfiguration(properties);
			}
		}),
		STATUS(MESSAGES.colSolverStatus(), new StringSolverProperty() {
			@Override
			public String getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				return getSolverStatus(solver);
			}
		}),
		OWNER(MESSAGES.colSolverOwner(), new StringSolverProperty() {
			@Override
			public String getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				if (solver instanceof StudentSolverProxy && ((StudentSolverProxy)solver).isPublished()) return "Published";
				return getSolverOwner(properties);
			}
		}),
		NR_CORES(MESSAGES.colSolverCores(),  new IntegerSolverProperty() {
			@Override
			public Integer getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				return Math.abs(properties.getPropertyInt("Parallel.NrSolvers", 4));
			}
		}),
		ASSIGNED_VAR(MESSAGES.colSolverAssignedVariables(), new InfoSolverProperty("Assigned variables", true)),
		TOTAL(MESSAGES.colSolverOverallValue(), new InfoSolverProperty("Overall solution value", true)),
		COURSE_TIME_PREF(SolverType.COURSE, MESSAGES.colSolverTimePrefs(), new InfoSolverProperty("Time preferences", true)),
		COURSE_STUDENT_CONF(SolverType.COURSE, MESSAGES.colSolverStudentConfs(), new StringSolverProperty() {
			@Override
			public String getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				String studConf = (info == null ? null : info.get("Student conflicts"));
				if (studConf != null)
					return studConf.replaceAll(" \\[","(").replaceAll("\\]",")").replaceAll(", ",",").replaceAll("hard:","h").replaceAll("distance:","d").replaceAll("commited:","c").replaceAll("committed:","c");
				else
					return null;
			}
		}),
		COURSE_ROOM_PREF(SolverType.COURSE, MESSAGES.colSolverRoomPrefs(), new InfoSolverProperty("Room preferences", true)),
		COURSE_DIST_PREF(SolverType.COURSE, MESSAGES.colSolverDistrPrefs(), new InfoSolverProperty("Distribution preferences", true)),
		COURSE_BTB_INSTR_PREF(SolverType.COURSE, MESSAGES.colSolverBtbInstrPrefs(), new InfoSolverProperty("Back-to-back instructor preferences", true)),
		// COURSE_TOO_BIG(SolverType.COURSE, "TooBig", new InfoSolverProperty("Too big rooms", true)),
		// COURSE_USELESS(SolverType.COURSE, "Useless", new InfoSolverProperty("Useless half-hours", true)),
		COURSE_PERTURBATIONS(SolverType.COURSE, MESSAGES.colSolverPerturbations(), new InfoSolverProperty("Perturbations: Total penalty", false)),
		COURSE_NOTE(SolverType.COURSE, MESSAGES.colSolverNote(), new StringSolverProperty() {
			@Override
			public String getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				return properties.getProperty("General.Note", "").replaceAll("\n","<br>");
			}
		}),
		EXAM_STUD_CONF(SolverType.EXAM, MESSAGES.colSolverExamStudentConfs(), new StringSolverProperty() {
			@Override
			public String getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				if (info == null) return null;
				String dc = info.get("Direct Conflicts");
                String m2d = info.get("More Than 2 A Day Conflicts");
                String btb = (String)info.get("Back-To-Back Conflicts");
                return (dc == null ? "0" : dc) + ", " + (m2d == null ? "0" : m2d) + ", " + (btb == null ? "0" : btb);
			}
		}),
		EXAM_INSTR_CONF(SolverType.EXAM, MESSAGES.colSolverExamInstrConfs(), new StringSolverProperty() {
			@Override
			public String getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				if (info == null) return null;
				String dc = info.get("Instructor Direct Conflicts");
                String m2d = info.get("Instructor More Than 2 A Day Conflicts");
                String btb = (String)info.get("Instructor Back-To-Back Conflicts");
                return (dc == null ? "0" : dc) + ", " + (m2d == null ? "0" : m2d) + ", " + (btb == null ? "0" : btb);
			}
		}),
		EXAM_PERIOD_PREF(SolverType.EXAM, MESSAGES.colSolverExamPeriodPref(), new InfoSolverProperty("Period Penalty", true)),
		EXAM_ROOM_PREF(SolverType.EXAM, MESSAGES.colSolverExamRoomPref(), new InfoSolverProperty("Room Penalty", true)),
		EXAM_ROOM_SPLIT(SolverType.EXAM, MESSAGES.colSolverExamRoomSplits(), new InfoSolverProperty("Room Split Penalty", true)),
		EXAM_ROOM_SIZE(SolverType.EXAM, MESSAGES.colSolverExamRoomSize(), new InfoSolverProperty("Room Size Penalty", true)),
		EXAM_DIST_PREF(SolverType.EXAM, MESSAGES.colSolverExamDistrPrefs(), new InfoSolverProperty("Distribution Penalty", true)),
		EXAM_ROTATION(SolverType.EXAM, MESSAGES.colSolverExamRotation(), new InfoSolverProperty("Exam Rotation Penalty", true)),
		EXAM_PERTURBATIONS(SolverType.EXAM, MESSAGES.colSolverExamPerturbations(), new InfoSolverProperty("Perturbation Penalty", true)),
		STUDENT_ASSIGNED_CR(SolverType.STUDENT, MESSAGES.colSolverStudCourseReqs(), new InfoSolverProperty("Assigned course requests", true)),
		STUDENT_ASSIGNED_PRIORITY(SolverType.STUDENT, MESSAGES.colSolverStud1stChoice(), new InfoSolverProperty("Assigned priority course requests", true)),
		STUDENT_COMPLETE(SolverType.STUDENT, MESSAGES.colSolverStudCompleteStuds(), new InfoSolverProperty("Students with complete schedule", true)),
		STUDENT_SELECTION(SolverType.STUDENT, MESSAGES.colSolverStudSelection(), new InfoSolverProperty("Selection", true)),
		STUDENT_DIST_CONF(SolverType.STUDENT, MESSAGES.colSolverStudDistanceConfs(), new InfoSolverProperty("Student distance conflicts", true)),
		STUDENT_TIME_OVERLAPS(SolverType.STUDENT, MESSAGES.colSolverStudTimeOverlaps(), new InfoSolverProperty("Time overlapping conflicts", true)),
		// STUDENT_FREE_OVERLAPS(SolverType.STUDENT, MESSAGES.colSolverStudFreeConf(), new InfoSolverProperty("Free time overlapping conflicts", true)),
		STUDENT_DISBALANCE_AVG(SolverType.STUDENT, MESSAGES.colSolverStudAvgDisbalance(), new InfoSolverProperty("Average disbalance", true)),
		STUDENT_DISBALANCE_10P(SolverType.STUDENT, MESSAGES.colSolverStudDisbOver10(), new InfoSolverProperty("Sections disbalanced by 10% or more", true)),
		STUDENT_PERTURBATIONS(SolverType.STUDENT, MESSAGES.colSolverStudPerturbations(), new InfoSolverProperty("Perturbations: Total penalty", true)),
		INSTRUCTOR_ATTR_PREF(SolverType.INSTRUCTOR, MESSAGES.colSolverInstrAtributePrefs(), new InfoSolverProperty("Attribute Preferences", true)),
		INSTRUCTOR_COURSE_PREF(SolverType.INSTRUCTOR, MESSAGES.colSolverInstrCoursePrefs(), new InfoSolverProperty("Course Preferences", true)),
		INSTRUCTOR_INSTR_PREF(SolverType.INSTRUCTOR, MESSAGES.colSolverInstrInstructorPrefs(), new InfoSolverProperty("Instructor Preferences", true)),
		INSTRUCTOR_TEACH_PREF(SolverType.INSTRUCTOR, MESSAGES.colSolverInstrTeachingPrefs(), new InfoSolverProperty("Teaching Preferences", true)),
		INSTRUCTOR_TIME_PREF(SolverType.INSTRUCTOR, MESSAGES.colSolverInstrTimePrefs(), new InfoSolverProperty("Time Preferences", true)),
		INSTRUCTOR_SAME_INSTRUCTOR(SolverType.INSTRUCTOR, MESSAGES.colSolverInstrSameInstructor(), new InfoSolverProperty("Same Instructor", true)),
		INSTRUCTOR_SAME_LECTURE(SolverType.INSTRUCTOR, MESSAGES.colSolverInstrSameLecture(), new InfoSolverProperty("Same Lecture", true)),
		INSTRUCTOR_BTB(SolverType.INSTRUCTOR, MESSAGES.colSolverInstrBTB(), new InfoSolverProperty("Back To Back", true)),
		INSTRUCTOR_SAME_DAYS(SolverType.INSTRUCTOR, MESSAGES.colSolverInstrSameDays(), new InfoSolverProperty("Same Days", true)),
		INSTRUCTOR_SAME_ROOM(SolverType.INSTRUCTOR, MESSAGES.colSolverInstrSameRoom(), new InfoSolverProperty("Same Room", true)),
		INSTRUCTOR_PERTURBATIONS(SolverType.INSTRUCTOR, MESSAGES.colSolverInstrOriginalInstructor(), new InfoSolverProperty("Original Instructor", true)),
		;
		
		private String iName;
		private SolverProperty<?> iProperty;
		private SolverType iType;
		SolverProperties(SolverType type, String name, SolverProperty<?> property) {
			iType = type; iName = name; iProperty = property;
		}
		SolverProperties(String name, SolverProperty<?> property) { this(null, name, property); }
		public String getName() { return iName; }
		public SolverProperty<?> getProperty() { return iProperty; }
		public SolverType getType() { return iType; }
		public static List<SolverProperties> applicable(SolverType type) {
			List<SolverProperties> ret = new ArrayList<SolverProperties>();
			for (SolverProperties p: values()) {
				if ((p.getType() == null || p.getType() == type) && p.isVisible()) ret.add(p);
			}
			return ret;
		}
		public boolean isVisible() { return iProperty.isVisible(); }
	}
	
	protected static String getTableName(SolverType type) {
		switch (type) {
		case COURSE: return MESSAGES.sectManageSolversCourse();
		case EXAM: return MESSAGES.sectManageSolversExam();
		case STUDENT: return MESSAGES.sectManageSolversStudent();
		case INSTRUCTOR: return MESSAGES.sectManageSolversInstructor();
		}
		return "Manage " + Constants.toInitialCase(type.name()) + " Solvers";
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
