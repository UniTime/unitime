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
package org.unitime.timetable.action;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpSession;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.cpsolver.ifs.util.DataProperties;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.BlankForm;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SolverParameterGroup.SolverType;
import org.unitime.timetable.model.SolverPredefinedSetting;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.ExamTypeDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SolverGroupDAO;
import org.unitime.timetable.model.dao.SolverPredefinedSettingDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.basic.GetInfo;
import org.unitime.timetable.security.qualifiers.SimpleQualifier;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.CommonSolverInterface;
import org.unitime.timetable.solver.SolverProxy;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.jgroups.SolverServer;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.solver.studentsct.StudentSolverProxy;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;
import org.unitime.timetable.util.LookupTables;


/** 
 * @author Tomas Muller
 */
@Action(value = "manageSolvers", results = {
		@Result(name = "showSolvers", type = "tiles", location = "manageSolvers.tiles")
	})
@TilesDefinition(name = "manageSolvers.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Manage Solvers"),
		@TilesPutAttribute(name = "body", value = "/tt/manageSolvers.jsp")
	})
public class ManageSolversAction extends UniTimeAction<BlankForm> {
	private static final long serialVersionUID = 5315110666093536200L;
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	private static Formats.Format<Date> sDF = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
	private static Logger sLog = LogManager.getLogger(ManageSolversAction.class); 
	
	private String owner, type, onlineId, host, solver;
	public String getOwner() { return owner; }
	public void setOwner(String owner) { this.owner = owner; }
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }
	public String getOnlineId() { return onlineId; }
	public void setOnlineId(String onlineId) { this.onlineId = onlineId; }
	public String getHost() { return host; }
	public void setHost(String host) { this.host = host; }
	public String getSolver() { return solver; }
	public void setSolver(String solver) { this.solver = solver; }
	
	protected SolverService<? extends CommonSolverInterface> getSolverService(SolverType type) {
		switch (type) {
		case COURSE:
			return getCourseTimetablingSolverService();
		case EXAM:
			return getExaminationSolverService();
		case STUDENT:
			return getStudentSectioningSolverService();
		case INSTRUCTOR:
			return getInstructorSchedulingSolverService();
		default:
			throw new IllegalArgumentException(MESSAGES.errorSolverInvalidType(type.name()));
		}
	}

	public String execute() throws Exception {
        // Check Access.
		sessionContext.checkPermission(Right.ManageSolvers);
        
        if ("Select".equals(op) && owner != null && type != null) {
        	switch(SolverType.valueOf(type)) {
        	case COURSE:
            	sessionContext.setAttribute(SessionAttribute.CourseTimetablingUser, owner);
            	sessionContext.removeAttribute(SessionAttribute.CourseTimetablingSolver);
        		response.sendRedirect("gwt.jsp?page=solver&type=course");
        		return null;
        	case EXAM:
                sessionContext.setAttribute(SessionAttribute.ExaminationUser, owner);
                sessionContext.removeAttribute(SessionAttribute.ExaminationSolver);
                LookupTables.setupExamTypes(request, sessionContext.getUser().getCurrentAcademicSessionId());
        		response.sendRedirect("gwt.jsp?page=solver&type=exam");
        		return null;
        	case STUDENT:
                sessionContext.setAttribute(SessionAttribute.StudentSectioningUser, owner);
                sessionContext.removeAttribute(SessionAttribute.StudentSectioningSolver);
        		response.sendRedirect("gwt.jsp?page=solver&type=student");
        		return null;
        	case INSTRUCTOR:
                sessionContext.setAttribute(SessionAttribute.InstructorSchedulingUser, owner);
                sessionContext.removeAttribute(SessionAttribute.InstructorSchedulingSolver);
                response.sendRedirect("gwt.jsp?page=solver&type=instructor");
                return null;
        	}
        }
        
        if ("Unload".equals(op) && owner != null && type != null) {
        	switch(SolverType.valueOf(type)) {
        	case COURSE:
            	sessionContext.setAttribute(SessionAttribute.CourseTimetablingUser, owner);
            	sessionContext.removeAttribute(SessionAttribute.CourseTimetablingSolver);
            	getCourseTimetablingSolverService().removeSolver();
            	break;
        	case EXAM:
                sessionContext.setAttribute(SessionAttribute.ExaminationUser, owner);
                sessionContext.removeAttribute(SessionAttribute.ExaminationSolver);
                getExaminationSolverService().removeSolver();
                break;
        	case STUDENT:
                sessionContext.setAttribute(SessionAttribute.StudentSectioningUser, owner);
                sessionContext.removeAttribute(SessionAttribute.StudentSectioningSolver);
                getStudentSectioningSolverService().removeSolver();
                break;
        	case INSTRUCTOR:
        		sessionContext.setAttribute(SessionAttribute.InstructorSchedulingUser, owner);
                sessionContext.removeAttribute(SessionAttribute.InstructorSchedulingSolver);
                getInstructorSchedulingSolverService().removeSolver();
                break;
        	}
        }
        
        if ("Unload".equals(op) && onlineId!=null) {
        	if (host != null) {
        		SolverServer server = getSolverServerService().getServer(host);
        		if (server != null) {
        			server.getOnlineStudentSchedulingContainer().unloadSolver(onlineId);
        		} else {
        			getSolverServerService().getOnlineStudentSchedulingContainer().unloadSolver(onlineId);
        		}
        	} else {
        		getSolverServerService().getOnlineStudentSchedulingContainer().unloadSolver(onlineId);
        	}
        }
        
        if ("Reload".equals(op) && onlineId!=null) {
			OnlineSectioningServer solver = getSolverServerService().getOnlineStudentSchedulingContainer().getSolver(onlineId);
			if (solver != null) {
				solver.reload();
			}
        }
        
        if ("Deselect".equals(op)) {
        	sessionContext.removeAttribute(SessionAttribute.CourseTimetablingUser);
        	sessionContext.removeAttribute(SessionAttribute.CourseTimetablingSolver);
            sessionContext.removeAttribute(SessionAttribute.ExaminationUser);
            sessionContext.removeAttribute(SessionAttribute.ExaminationSolver);
            sessionContext.removeAttribute(SessionAttribute.StudentSectioningUser);
            sessionContext.removeAttribute(SessionAttribute.StudentSectioningSolver);
            sessionContext.removeAttribute(SessionAttribute.InstructorSchedulingUser);
            sessionContext.removeAttribute(SessionAttribute.InstructorSchedulingSolver);
        }
        
        if ("Shutdown".equals(op)) {
        	SolverServer server = getSolverServerService().getServer(solver);
        	if (server != null)
        		server.shutdown();
        }
        
        if ("Reset".equals(op)) {
        	SolverServer server = getSolverServerService().getServer(solver);
        	if (server != null)
        		server.reset(false);
        }

        if ("Enable".equals(op)) {
        	SolverServer server = getSolverServerService().getServer(solver);
        	if (server != null)
        		server.setUsageBase(0);
        }

        if ("Disable".equals(op)) {
        	SolverServer server = getSolverServerService().getServer(solver);
        	if (server != null)
        		server.setUsageBase(1000);
        }

        return "showSolvers";
	}
	
	public static String getSolverOwner(DataProperties solverProperties) {
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
	
	public static String getSolverSession(DataProperties solverProperties) {
		Long sessionId = solverProperties.getPropertyLong("General.SessionId", null);
		if (sessionId != null) {
			Session session = SessionDAO.getInstance().get(sessionId);
			if (session != null) return session.getLabel();
		}
		return MESSAGES.itemNotApplicable();
	}
	
	public static String getSolverConfiguration(DataProperties solverProperties) {
		Long settingsId = solverProperties.getPropertyLong("General.SettingsId", null);
		if (settingsId != null) {
			SolverPredefinedSetting setting = SolverPredefinedSettingDAO.getInstance().get(settingsId);
			if (setting != null) return setting.getDescription();
		}
		return solverProperties.getProperty("Basic.Mode",MESSAGES.itemNotApplicable());
	}
	
	public String getOnClick(DataProperties solverProperties, SolverType type) {
		Long sessionId = solverProperties.getPropertyLong("General.SessionId", null);
		String ownerId = solverProperties.getProperty("General.OwnerPuid");
		if (sessionId != null && sessionId.equals(sessionContext.getUser().getCurrentAcademicSessionId()) && ownerId != null)
			return "onClick=\"document.location='manageSolvers.action?op=Select&type=" + type.name() + "&owner=" + ownerId + "';\"";
		return null;
	}
	
	public static String getSolverStatus(CommonSolverInterface solver) {
		String status = MESSAGES.itemNotApplicable();
		try {
			status = (String)solver.getProgress().get("STATUS");
		} catch (Exception e) {}
		return status;
	}
	
	public static String getSolverOperations(DataProperties solverProperties, SolverType type) {
		String operations = "";
		String ownerId = solverProperties.getProperty("General.OwnerPuid");
		if (ownerId != null) {
			operations += "<input type=\"button\" value=\"" + MESSAGES.actionSolverUnload() + "\" onClick=\"" +
            	"if (confirm('" + MESSAGES.confirmUnloadSolver() + "')) " +
            	"document.location='manageSolvers.action?op=Unload&type=" + type.name() + "&owner=" + ownerId+ "';" +
            	" event.cancelBubble=true;\">";
		}
		return operations;
	}
	
	public static String getSolverMemory(DataProperties solverProperties, SolverType type) {
		String ownerId = solverProperties.getProperty("General.OwnerPuid");
		if (ownerId != null) {
			switch (type) {
			case COURSE:
				return "<span name='UniTimeGWT:SolverAllocatedMem' style='display: none;'>C" + ownerId + "</span>";
			case STUDENT:
				return "<span name='UniTimeGWT:SolverAllocatedMem' style='display: none;'>S" + ownerId + "</span>";
			case EXAM:
				return "<span name='UniTimeGWT:SolverAllocatedMem' style='display: none;'>X" + ownerId + "</span>";
			case INSTRUCTOR:
				return "<span name='UniTimeGWT:SolverAllocatedMem' style='display: none;'>I" + ownerId + "</span>";
			}
		}
		return MESSAGES.itemNotApplicable();
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
		public Comparable getComparable(Date value) { return value == null ? new Date() : value; }
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
		MEMORY(MESSAGES.colSolverMem(), new StringSolverProperty() {
			@Override
			public String getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				return getSolverMemory(properties, type);
			}
			@Override
			public Comparable getComparable(String value) { return null; }
			@Override
			public boolean isVisible() { return ApplicationProperty.ManageSolversComputeMemoryUses.isTrue(); }
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
		OPERATIONS(MESSAGES.colSolverOperations(), new StringSolverProperty() {
			@Override
			public String getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				return getSolverOperations(properties, type);
			}
			@Override
			public Comparable getComparable(String value) { return null; }
		}),
		;
		
		private String iName;
		private SolverProperty<?> iProperty;
		private SolverType iType;
		SolverProperties(SolverType type, String name, SolverProperty<?> property) {
			iType = type; iName = name; iProperty = property;
		}
		SolverProperties(String name, SolverProperty<?> property) { this(null, name, property); }
		public String getName() { return iName; }
		public String getAlignment() { return "left"; }
		public boolean getDefaultOrder() { return true; }
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
	
	public SolverType[] getSolverTypes() {
		return SolverType.values();
	}
	
	public String getSolverTable(SolverType type) {
		try {
			WebTable.setOrder(sessionContext,"manageSolvers.ord[" + type + "]", request.getParameter("ord" + type.ordinal()), 1);
			List<SolverProperties> props = SolverProperties.applicable(type);
			
			String[] names = new String[props.size()];
			String[] align = new String[props.size()];
			boolean[] ord = new boolean[props.size()];
			for (int i = 0; i < props.size(); i++) {
				SolverProperties p = props.get(i);
				names[i] = p.getName();
				align[i] = p.getAlignment();
				ord[i] = p.getDefaultOrder();
			}
			WebTable webTable = new WebTable(props.size(), getTableName(type), "manageSolvers.action?ord" + type.ordinal() + "=%%", names, align, ord);
			webTable.setRowStyle("white-space:nowrap");
			int nrLines = 0;
			
			SolverService<? extends CommonSolverInterface> service = getSolverService(type);
			CommonSolverInterface selected = service.getSolverNoSessionCheck();
			String selectedId = (selected == null ? null : selected.getUser());
			
			List<CommonSolverInterface> solvers = new ArrayList<CommonSolverInterface>(service.getSolvers().values());
			for (CommonSolverInterface solver: solvers) {
				if (solver == null) continue;
				DataProperties properties = solver.getProperties();
				if (properties == null) continue;
				Map<String,String> info = solver.statusSolutionInfo();
				
	            String bgColor = null;
	        	if (selectedId != null && selectedId.equals(solver.getUser()))
	        		bgColor = "rgb(168,187,225)";
	        	
	        	String[] line = new String[props.size()];
	        	Comparable[] cmp = new Comparable[props.size()];
	        	for (int i = 0; i < props.size(); i++) {
	    			SolverProperty<Object> p = (SolverProperty<Object>)props.get(i).getProperty();
	    			Object o = p.getValue(solver, type, properties, info);
	    			line[i] = p.getText(o);
	    			cmp[i] = p.getComparable(o);
	        	}
	        	webTable.addLine(getOnClick(properties, type), line, cmp).setBgColor(bgColor);
	        	nrLines ++;
			}
			if (nrLines==0)
				webTable.addLine(null, new String[] {"<i>" + MESSAGES.infoNoSolver() + "</i>"}, null, null );
			return webTable.printTable(WebTable.getOrder(sessionContext, "manageSolvers.ord[" + type + "]"));
		} catch (Exception e) {
			sLog.error(e);
			return e.getMessage();
		}
	}
	
	public boolean hasServers() {
		return true;
	}

	public String getServers() {
		try {
			WebTable.setOrder(sessionContext,"manageSolvers.ord[SERVERS]", request.getParameter("ords"),1);
			
			boolean ops = sessionContext.hasPermission(Right.SessionIndependent);
			
			WebTable webTable = new WebTable( 12,
					MESSAGES.sectAvailableServers(), "manageSolvers.action?ords=%%",
					new String[] {
							MESSAGES.colServerHost(),
							MESSAGES.colServerVersion(),
							MESSAGES.colServerStarted(),
							MESSAGES.colServerAvailableMemory(),
							MESSAGES.colServerNrCores(),
							MESSAGES.colServerPing(),
							MESSAGES.colServerUsage(),
							MESSAGES.colServerNrInstances(),
							MESSAGES.colServerActive(),
							MESSAGES.colServerWorking(),
							MESSAGES.colServerPassivated(),
							MESSAGES.colServerOperations()},
					new String[] {"left", "left", "left", "left", "left", "left", "left", "left", "left","left","left","left"},
					null );
			webTable.setRowStyle("white-space:nowrap");
			
			DecimalFormat df = new DecimalFormat("0.00");
			
			int nrLines = 0;
			
			for (SolverServer server: getSolverServerService().getServers(false)) {
                    if (!server.isActive()) {
                        webTable.addLine(null, new String[] {
                                server.getHost(),
                                "<i>" + MESSAGES.serverInactive() + "</i>",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                "",
                                ""
                                },
                            new Comparable[] {
                        		server.getHost(),
                                "",
                                null,
                                Long.valueOf(-1),
                                Integer.valueOf(-1),
                                Long.valueOf(-1),
                                Long.valueOf(-1),
                                Integer.valueOf(-1),
                                Integer.valueOf(-1),
                                Integer.valueOf(-1),
                                Integer.valueOf(-1),
                                null
                        });
                        continue;
                    }
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
                    String op = "";
                    if (ops) {
                        if (usage >= 1000) {
                            op+="<input type=\"button\" value=\"" + MESSAGES.actionServerEnable() + "\" onClick=\"if (confirm('" +
                            		MESSAGES.configServerEnable(server.getHost()) +
                            		"')) document.location='manageSolvers.action?op=Enable&solver="+server.getHost()+"';\">&nbsp;&nbsp;";
                        } else {
                            op+="<input type=\"button\" value=\"" + MESSAGES.actionServerDisable() + "\" onClick=\"if (confirm('" +
                            		MESSAGES.confirmServerDisable(server.getHost()) + "')) document.location='manageSolvers.action?op=Disable&solver="+server.getHost()+"';\">&nbsp;&nbsp;";
                        }
                        if (server.isCoordinator()) {
                        	op+="<input type=\"button\" value=\"" + MESSAGES.actionServerReset() + "\" onClick=\"if (confirm('" +
                        			MESSAGES.confirmServerReset(server.getHost()) + "')) document.location='manageSolvers.action?op=Reset&solver="+server.getHost()+"';\">&nbsp;&nbsp;";
                        }
                        if (!local) {
                        	op+="<input type=\"button\" value=\"" + MESSAGES.actionServerShutdown() + "\" onClick=\"if (confirm('" +
                        			MESSAGES.confirmServerShutdown(server.getHost()) + "')) document.location='manageSolvers.action?op=Shutdown&solver="+server.getHost()+"';\">&nbsp;&nbsp;";
                        }
                    }
                    Set<String> flags = new TreeSet<String>();
                    if (local) flags.add(MESSAGES.serverFlagTomcat());
                    if (server.isCoordinator()) flags.add(MESSAGES.serverFlagCoordinator());
                    if (!server.isAvailable()) flags.add(MESSAGES.serverFlagUnavailable());
                    webTable.addLine(null, new String[] {
                            server.getHost() + (flags.isEmpty() ? "" : " " + flags.toString()),
                            (version==null||"-1".equals(version)?"<i>" + MESSAGES.itemNotApplicable() + "</i>":version),
                            (startTime==null?"<i>" + MESSAGES.itemNotApplicable() + "</i>":sDF.format(startTime)),
                            df.format( ((double)mem)/1024/1024)+" MB",
                            String.valueOf(cores),
                            (t1-t0)+" ms",
                            String.valueOf(usage),
                            String.valueOf(nrActive+nrPassivated),
                            String.valueOf(nrActive),
                            String.valueOf(nrWorking),
                            String.valueOf(nrPassivated),
                            op
                            },
                        new Comparable[] {
                            server.getHost(),
                            version,
                            startTime,
                            Long.valueOf(t1-t0),
                            Integer.valueOf(cores),
                            Long.valueOf(mem),
                            Long.valueOf(usage),
                            Integer.valueOf(nrActive+nrPassivated),
                            Integer.valueOf(nrActive),
                            Integer.valueOf(nrWorking),
                            Integer.valueOf(nrPassivated),
                            null
                    });
                    nrLines++;
            }
			

			if (nrLines==0)
				webTable.addLine(null, new String[] {"<i>" + MESSAGES.infoNoServerRunning() + "</i>"}, null, null );

			return webTable.printTable(WebTable.getOrder(sessionContext,"manageSolvers.ord[SERVERS]"));
	    } catch (Exception e) {
	    	sLog.error(e);
	        return e.getMessage();
	    }
	}
       
	public String getOnlineSolvers() {
        try {
            WebTable.setOrder(sessionContext,"manageSolvers.ord[ONLINE]",request.getParameter("ordo"),1);
            
            boolean mem = ApplicationProperty.ManageSolversComputeMemoryUses.isTrue();
            
            WebTable webTable = (mem ?
         		   new WebTable( 14,
         				   MESSAGES.sectManageSolversOnline(), "manageSolvers.action?ordo=%%",
         				   new String[] {
         						   MESSAGES.colCreated(),
         						   MESSAGES.colSession(),
         						   MESSAGES.colSolverHost(),
         						   MESSAGES.colSolverMode(),
         						   MESSAGES.colSolverMem(),
         						   MESSAGES.colSolverOverallValue(),
        						   MESSAGES.colSolverStudCourseReqs(),
         						   MESSAGES.colSolverStud1stChoice(),
         						   MESSAGES.colSolverStudCompleteStuds(),
         						   MESSAGES.colSolverStudDistanceConfs(),
         						   MESSAGES.colSolverStudTimeOverlaps(),
         						   MESSAGES.colSolverStudAvgDisbalance(),
         						   MESSAGES.colSolverStudDisbOver10(),
         						   MESSAGES.colSolverOperations()},
         				   new String[] {"left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left"},
         				   null ) :
                    new WebTable( 13,
                    		MESSAGES.sectManageSolversOnline(), "manageSolvers.action?ordo=%%",
                 		   new String[] {
                 				  MESSAGES.colCreated(),
                 				  MESSAGES.colSession(),
                 				  MESSAGES.colSolverHost(),
                 				  MESSAGES.colSolverMode(),
                 				  MESSAGES.colSolverOverallValue(),
                 				  MESSAGES.colSolverStudCourseReqs(),
                 				  MESSAGES.colSolverStud1stChoice(),
                 				  MESSAGES.colSolverStudCompleteStuds(),
                 				  MESSAGES.colSolverStudDistanceConfs(),
                 				  MESSAGES.colSolverStudTimeOverlaps(),
                 				  MESSAGES.colSolverStudAvgDisbalance(),
                 				  MESSAGES.colSolverStudDisbOver10(),
                 				  MESSAGES.colSolverOperations()},
                            new String[] {"left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left"},
                            null )
            			);
            webTable.setRowStyle("white-space:nowrap");
            
            int nrLines = 0;
            
            List<SolverServer> servers = getSolverServerService().getServers(true);
            for (SolverServer server: servers) {
                for (String sessionId : server.getOnlineStudentSchedulingContainer().getSolvers()) {
             	   OnlineSectioningServer solver = server.getOnlineStudentSchedulingContainer().getSolver(sessionId);
             	   if (solver==null) continue;
             	   DataProperties properties = solver.getConfig();
             	   if (properties==null) continue;
                    String sessionLabel = solver.getAcademicSession().toString();
                    String mode = solver.getAcademicSession().isSectioningEnabled() ? "Online" : "Assistant";
                    Map<String,String> info = (solver.isReady() ? solver.execute(solver.createAction(GetInfo.class), null) : null);
                    String assigned = (info == null ? null : info.get("Assigned variables"));
                    String totVal = (info == null ? null : info.get("Overall solution value"));
                    String compSch = (info == null ? null : info.get("Students with complete schedule"));
                    String distConf = (info == null ? null : info.get("Student distance conflicts"));
                    String time = (info == null ? null : info.get("Time overlapping conflicts"));
                    String disb = (info == null ? null : info.get("Average disbalance"));
                    String disb10 = (info == null ? null : info.get("Sections disbalanced by 10% or more"));
                    String choice1 = (info == null ? null : info.get("Assigned priority course requests"));
                    Date loaded = new Date(solver.getConfig().getPropertyLong("General.StartUpDate", 0));

                    String op = "";
                    if (!sessionContext.getUser().getAuthorities(sessionContext.getUser().getCurrentAuthority().getRole(), new SimpleQualifier("Session", Long.valueOf(sessionId))).isEmpty()) {
                        if (solver.isReady()) {
                      	   op += "<input type=\"button\" value=\"" + MESSAGES.actionOnlineSolverReload() + "\" onClick=\"" +
                         			"if (confirm('" + MESSAGES.confirmOnlineReload() + "')) " +
                         			"document.location='manageSolvers.action?op=Reload&onlineId=" + sessionId + "';" + 
                         			" event.cancelBubble=true;\">&nbsp;&nbsp;";
                         }
                        op += "<input type=\"button\" value=\"" + MESSAGES.actionOnlineSolverShutdown() + "\" onClick=\"" +
                     			"if (confirm('" + MESSAGES.confrimOnlineShutdown() + "')) " +
                     			"document.location='manageSolvers.action?op=Unload&onlineId=" + sessionId + "&host=" + server.getHost() + "';" + 
                     			" event.cancelBubble=true;\">";
                    }
                    
                    if (mem) {
                        webTable.addLine(null, new String[] {
                                (loaded.getTime() <= 0 ? MESSAGES.itemNotApplicable() : sDF.format(loaded)),
                                sessionLabel,
                                solver.getHost(),
                                mode,
                                "<span name='UniTimeGWT:SolverAllocatedMem' style='display: none;'>O" + server.getHost() + ":" + sessionId + "</span>",
                                (totVal==null?"":totVal),
                                (assigned==null?"":assigned),
                                (choice1==null?"":choice1),
                                (compSch==null?"":compSch), 
                                (distConf==null?"":distConf),
                                (time==null?"":time),
                                (disb==null?"":disb),
                                (disb10==null?"":disb10),
                                op},
                            new Comparable[] {
                                loaded,
                                sessionLabel,
                                solver.getHost(),
                                mode, 
                                null,
                                (totVal==null?"":totVal),
                                (assigned==null?"":assigned),
                                (choice1==null?"":choice1),
                                (compSch==null?"":compSch), 
                                (distConf==null?"":distConf),
                                (time==null?"":time),
                                (disb==null?"":disb),
                                (disb10==null?"":disb10),
                                null});
                    } else {
                        webTable.addLine(null, new String[] {
                                (loaded.getTime() <= 0 ? MESSAGES.itemNotApplicable() : sDF.format(loaded)),
                                sessionLabel,
                                solver.getHost(),
                                mode,
                                (totVal==null?"":totVal),
                                (assigned==null?"":assigned),
                                (choice1==null?"":choice1),
                                (compSch==null?"":compSch), 
                                (distConf==null?"":distConf),
                                (time==null?"":time),
                                (disb==null?"":disb),
                                (disb10==null?"":disb10),
                                op},
                            new Comparable[] {
                                loaded,
                                sessionLabel,
                                solver.getHost(),
                                mode, 
                                (totVal==null?"":totVal),
                                (assigned==null?"":assigned),
                                (choice1==null?"":choice1),
                                (compSch==null?"":compSch), 
                                (distConf==null?"":distConf),
                                (time==null?"":time),
                                (disb==null?"":disb),
                                (disb10==null?"":disb10),
                                null});                    	   
                    }
                    nrLines++;
                }
            }
            if (nrLines==0)
                webTable.addLine(null, new String[] {"<i>" + MESSAGES.infoNoOnlineSolverRunning() + "</i>"}, null, null );
            return webTable.printTable(WebTable.getOrder(sessionContext,"manageSolvers.ord[ONLINE]"));
        } catch (Exception e) {
        	sLog.error(e);
        	return e.getMessage();
        }
    }
	
	public boolean canDeselect() {
		HttpSession session = request.getSession();
		return (session.getAttribute("ManageSolver.puid")!=null || session.getAttribute("ManageSolver.examPuid")!=null || session.getAttribute("ManageSolver.sectionPuid")!=null || session.getAttribute("ManageSolver.instrPuid") != null);
	}
	
	public String getDeselect() {
		return MESSAGES.actionSolverDeselect();
	}
}

