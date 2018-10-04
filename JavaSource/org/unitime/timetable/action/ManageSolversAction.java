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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.cpsolver.ifs.util.DataProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.form.ManageSolversForm;
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
import org.unitime.timetable.util.LookupTables;


/** 
 * @author Tomas Muller
 */
@Service("/manageSolvers")
public class ManageSolversAction extends Action {
	protected static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	private static Formats.Format<Date> sDF = Formats.getDateFormat(Formats.Pattern.DATE_TIME_STAMP);
	
	@Autowired SessionContext sessionContext;
	
	@Autowired SolverService<SolverProxy> courseTimetablingSolverService;
	@Autowired SolverService<ExamSolverProxy> examinationSolverService;
	@Autowired SolverService<StudentSolverProxy> studentSectioningSolverService;
	@Autowired SolverService<InstructorSchedulingProxy> instructorSchedulingSolverService;
	@Autowired SolverServerService solverServerService;
	
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

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		ManageSolversForm myForm = (ManageSolversForm) form;

        // Check Access.
		sessionContext.checkPermission(Right.ManageSolvers);

        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        
        if ("Select".equals(op) && request.getParameter("owner") != null && request.getParameter("type") != null) {
        	SolverType type = SolverType.valueOf(request.getParameter("type"));
        	String puid = request.getParameter("owner");
        	switch(type) {
        	case COURSE:
            	sessionContext.setAttribute(SessionAttribute.CourseTimetablingUser, puid);
            	sessionContext.removeAttribute(SessionAttribute.CourseTimetablingSolver);
            	if (ApplicationProperty.LegacySolver.isTrue()) {
            		return mapping.findForward("showSolver");
            	} else {
            		response.sendRedirect("gwt.jsp?page=solver&type=course");
            		return null;
            	}
        	case EXAM:
                sessionContext.setAttribute(SessionAttribute.ExaminationUser, puid);
                sessionContext.removeAttribute(SessionAttribute.ExaminationSolver);
                LookupTables.setupExamTypes(request, sessionContext.getUser().getCurrentAcademicSessionId());
            	if (ApplicationProperty.LegacySolver.isTrue()) {
            		return mapping.findForward("showExamSolver");
            	} else {
            		response.sendRedirect("gwt.jsp?page=solver&type=exam");
            		return null;
            	}
        	case STUDENT:
                sessionContext.setAttribute(SessionAttribute.StudentSectioningUser, puid);
                sessionContext.removeAttribute(SessionAttribute.StudentSectioningSolver);
                if (ApplicationProperty.LegacySolver.isTrue()) {
            		return mapping.findForward("showStudentSolver");
            	} else {
            		response.sendRedirect("gwt.jsp?page=solver&type=student");
            		return null;
            	}
        	case INSTRUCTOR:
                sessionContext.setAttribute(SessionAttribute.InstructorSchedulingUser, puid);
                sessionContext.removeAttribute(SessionAttribute.InstructorSchedulingSolver);
                response.sendRedirect("gwt.jsp?page=solver&type=instructor");
                return null;
        	}
        }
        
        if ("Unload".equals(op) && request.getParameter("owner") != null && request.getParameter("type") != null) {
        	SolverType type = SolverType.valueOf(request.getParameter("type"));
        	String puid = request.getParameter("owner");
        	switch(type) {
        	case COURSE:
            	sessionContext.setAttribute(SessionAttribute.CourseTimetablingUser, puid);
            	sessionContext.removeAttribute(SessionAttribute.CourseTimetablingSolver);
            	courseTimetablingSolverService.removeSolver();
            	break;
        	case EXAM:
                sessionContext.setAttribute(SessionAttribute.ExaminationUser, puid);
                sessionContext.removeAttribute(SessionAttribute.ExaminationSolver);
                examinationSolverService.removeSolver();
                break;
        	case STUDENT:
                sessionContext.setAttribute(SessionAttribute.StudentSectioningUser, puid);
                sessionContext.removeAttribute(SessionAttribute.StudentSectioningSolver);
                studentSectioningSolverService.removeSolver();
                break;
        	case INSTRUCTOR:
        		sessionContext.setAttribute(SessionAttribute.InstructorSchedulingUser, puid);
                sessionContext.removeAttribute(SessionAttribute.InstructorSchedulingSolver);
                instructorSchedulingSolverService.removeSolver();
                break;
        	}
        }
        
        if ("Unload".equals(op) && request.getParameter("onlineId")!=null) {
        	String id = request.getParameter("onlineId");
        	if (request.getParameter("host") != null) {
        		SolverServer server = solverServerService.getServer(request.getParameter("host"));
        		if (server != null) {
        			server.getOnlineStudentSchedulingContainer().unloadSolver(id);
        		} else {
        			solverServerService.getOnlineStudentSchedulingContainer().unloadSolver(id);
        		}
        	} else {
        		solverServerService.getOnlineStudentSchedulingContainer().unloadSolver(id);
        	}
        }
        
        if ("Reload".equals(op) && request.getParameter("onlineId")!=null) {
        	String id = request.getParameter("onlineId");
			OnlineSectioningServer solver = solverServerService.getOnlineStudentSchedulingContainer().getSolver(id);
			if (solver != null && solver.isMaster()) {
				solver.setProperty("ReadyToServe", Boolean.FALSE);
				solver.setProperty("ReloadIsNeeded", Boolean.TRUE);
				solver.releaseMasterLockIfHeld();
			}
        }
        
        if ("Unmaster".equals(op) && request.getParameter("onlineId")!=null) {
        	String id = request.getParameter("onlineId");
        	if (request.getParameter("host") != null) {
        		SolverServer server = solverServerService.getServer(request.getParameter("host"));
        		if (server != null) {
        			OnlineSectioningServer solver = server.getOnlineStudentSchedulingContainer().getSolver(id);
        			if (solver != null) solver.releaseMasterLockIfHeld();
        		}
        	} else {
        		OnlineSectioningServer solver = solverServerService.getOnlineStudentSchedulingContainer().getSolver(id);
        		if (solver != null) solver.releaseMasterLockIfHeld();
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
        	SolverServer server = solverServerService.getServer(request.getParameter("solver"));
        	if (server != null)
        		server.shutdown();
        }
        
        if ("Reset".equals(op)) {
        	SolverServer server = solverServerService.getServer(request.getParameter("solver"));
        	if (server != null)
        		server.reset();
        }

        if ("Start Using".equals(op)) {
        	SolverServer server = solverServerService.getServer(request.getParameter("solver"));
        	if (server != null)
        		server.setUsageBase(0);
        }

        if ("Stop Using".equals(op)) {
        	SolverServer server = solverServerService.getServer(request.getParameter("solver"));
        	if (server != null)
        		server.setUsageBase(1000);
        }

        for (SolverType type: SolverType.values())
        	createSolverTable(request, type);
        if (sessionContext.hasPermission(Right.SessionIndependent))
        	getServers(request);
        getOnlineSolvers(request);
        return mapping.findForward("showSolvers");
	}
	
	public static String getSolverOwner(DataProperties solverProperties) {
    	String owner = solverProperties.getProperty("General.OwnerPuid", null);
    	if (owner != null) {
    		if (owner.startsWith("PUBLISHED_")) return "Published";
    		TimetableManager mgr = TimetableManager.findByExternalId(owner);
    		if (mgr != null)
    			owner = mgr.getShortName();
    	} else {
    		owner = "N/A";
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
		if (problem == null || problem.isEmpty()) problem = "N/A";
		if ("N/A".equals(problem)) return owner;
		if ("N/A".equals(owner)) return problem;
		if (!owner.equals(problem)) return owner + " as " + problem;
		return owner;
    }
	
	public static String getSolverSession(DataProperties solverProperties) {
		Long sessionId = solverProperties.getPropertyLong("General.SessionId", null);
		if (sessionId != null) {
			Session session = SessionDAO.getInstance().get(sessionId);
			if (session != null) return session.getLabel();
		}
		return "N/A";
	}
	
	public static String getSolverConfiguration(DataProperties solverProperties) {
		Long settingsId = solverProperties.getPropertyLong("General.SettingsId", null);
		if (settingsId != null) {
			SolverPredefinedSetting setting = SolverPredefinedSettingDAO.getInstance().get(settingsId);
			if (setting != null) return setting.getDescription();
		}
		return solverProperties.getProperty("Basic.Mode","N/A");
	}
	
	public String getOnClick(DataProperties solverProperties, SolverType type) {
		Long sessionId = solverProperties.getPropertyLong("General.SessionId", null);
		String ownerId = solverProperties.getProperty("General.OwnerPuid");
		if (sessionId != null && sessionId.equals(sessionContext.getUser().getCurrentAcademicSessionId()) && ownerId != null)
			return "onClick=\"document.location='manageSolvers.do?op=Select&type=" + type.name() + "&owner=" + ownerId + "';\"";
		return null;
	}
	
	public static String getSolverStatus(CommonSolverInterface solver) {
		String status = "N/A";
		try {
			status = (String)solver.getProgress().get("STATUS");
		} catch (Exception e) {}
		return status;
	}
	
	public static String getSolverOperations(DataProperties solverProperties, SolverType type) {
		String operations = "";
		String ownerId = solverProperties.getProperty("General.OwnerPuid");
		if (ownerId != null) {
			operations += "<input type=\"button\" value=\"Unload\" onClick=\"" +
            	"if (confirm('Do you really want to unload this solver?')) " +
            	"document.location='manageSolvers.do?op=Unload&type=" + type.name() + "&owner=" + ownerId+ "';" +
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
		return "N/A";
	}
	
	public static interface SolverProperty<T> {
		public T getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String,String> info);
		public String getText(T value);
		public Comparable getComparable(T value);
	}
	
	public static abstract class DateSolverProperty implements SolverProperty<Date> {
		@Override
		public String getText(Date value) { return value == null ? "N/A" : sDF.format(value); }
		public Comparable getComparable(Date value) { return value == null ? new Date() : value; }
	}
	
	public static abstract class StringSolverProperty implements SolverProperty<String> {
		@Override
		public String getText(String value) { return value == null ? "N/A" : value; }
		public Comparable getComparable(String value) { return value == null ? "" : value; }
	}
	
	public static abstract class IntegerSolverProperty implements SolverProperty<Integer> {
		@Override
		public String getText(Integer value) { return value == null ? "N/A" : String.valueOf(value); }
		public Comparable getComparable(Integer value) { return value == null ? 0 : value; }
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
		CREATED("Created", new DateSolverProperty() {
			@Override
			public Date getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				return solver.getLoadedDate();
			}
		}),
		LAST_USED("Last Used", new DateSolverProperty() {
			@Override
			public Date getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				return solver.getLastUsed();
			}
		}),
		SESSION("Session", new StringSolverProperty() {
			@Override
			public String getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				return getSolverSession(properties);
			}
		}),
		HOST("Host", new StringSolverProperty() {
			@Override
			public String getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				return solver.getHost();
			}
		}),
		CONFIG("Config", new StringSolverProperty() {
			@Override
			public String getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				return getSolverConfiguration(properties);
			}
		}),
		STATUS("Status", new StringSolverProperty() {
			@Override
			public String getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				return getSolverStatus(solver);
			}
		}),
		OWNER("Owner", new StringSolverProperty() {
			@Override
			public String getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				if (solver instanceof StudentSolverProxy && ((StudentSolverProxy)solver).isPublished()) return "Published";
				return getSolverOwner(properties);
			}
		}),
		MEMORY("Mem", new StringSolverProperty() {
			@Override
			public String getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				return getSolverMemory(properties, type);
			}
			@Override
			public Comparable getComparable(String value) { return null; }
		}),
		NR_CORES("Cores",  new IntegerSolverProperty() {
			@Override
			public Integer getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				return Math.abs(properties.getPropertyInt("Parallel.NrSolvers", 4));
			}
		}),
		ASSIGNED_VAR("Assign", new InfoSolverProperty("Assigned variables", true)),
		TOTAL("Total", new InfoSolverProperty("Overall solution value", true)),
		COURSE_TIME_PREF(SolverType.COURSE, "Time", new InfoSolverProperty("Time preferences", true)),
		COURSE_STUDENT_CONF(SolverType.COURSE, "Stud", new StringSolverProperty() {
			@Override
			public String getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				String studConf = (info == null ? null : info.get("Student conflicts"));
				if (studConf != null)
					return studConf.replaceAll(" \\[","(").replaceAll("\\]",")").replaceAll(", ",",").replaceAll("hard:","h").replaceAll("distance:","d").replaceAll("commited:","c").replaceAll("committed:","c");
				else
					return null;
			}
		}),
		COURSE_ROOM_PREF(SolverType.COURSE, "Room", new InfoSolverProperty("Room preferences", true)),
		COURSE_DIST_PREF(SolverType.COURSE, "Distr", new InfoSolverProperty("Distribution preferences", true)),
		COURSE_BTB_INSTR_PREF(SolverType.COURSE, "Instr", new InfoSolverProperty("Back-to-back instructor preferences", true)),
		// COURSE_TOO_BIG(SolverType.COURSE, "TooBig", new InfoSolverProperty("Too big rooms", true)),
		// COURSE_USELESS(SolverType.COURSE, "Useless", new InfoSolverProperty("Useless half-hours", true)),
		COURSE_PERTURBATIONS(SolverType.COURSE, "Pert", new InfoSolverProperty("Perturbations: Total penalty", false)),
		COURSE_NOTE(SolverType.COURSE, "Note", new StringSolverProperty() {
			@Override
			public String getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				return properties.getProperty("General.Note", "").replaceAll("\n","<br>");
			}
		}),
		EXAM_STUD_CONF(SolverType.EXAM, "StudConf", new StringSolverProperty() {
			@Override
			public String getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				if (info == null) return null;
				String dc = info.get("Direct Conflicts");
                String m2d = info.get("More Than 2 A Day Conflicts");
                String btb = (String)info.get("Back-To-Back Conflicts");
                return (dc == null ? "0" : dc) + ", " + (m2d == null ? "0" : m2d) + ", " + (btb == null ? "0" : btb);
			}
		}),
		EXAM_INSTR_CONF(SolverType.EXAM, "InstConf", new StringSolverProperty() {
			@Override
			public String getValue(CommonSolverInterface solver, SolverType type, DataProperties properties, Map<String, String> info) {
				if (info == null) return null;
				String dc = info.get("Instructor Direct Conflicts");
                String m2d = info.get("Instructor More Than 2 A Day Conflicts");
                String btb = (String)info.get("Instructor Back-To-Back Conflicts");
                return (dc == null ? "0" : dc) + ", " + (m2d == null ? "0" : m2d) + ", " + (btb == null ? "0" : btb);
			}
		}),
		EXAM_PERIOD_PREF(SolverType.EXAM, "Period", new InfoSolverProperty("Period Penalty", true)),
		EXAM_ROOM_PREF(SolverType.EXAM, "Room", new InfoSolverProperty("Room Penalty", true)),
		EXAM_ROOM_SPLIT(SolverType.EXAM, "RoomSplit", new InfoSolverProperty("Room Split Penalty", true)),
		EXAM_ROOM_SIZE(SolverType.EXAM, "RoomSize", new InfoSolverProperty("Room Size Penalty", true)),
		EXAM_DIST_PREF(SolverType.EXAM, "Distr", new InfoSolverProperty("Distribution Penalty", true)),
		EXAM_ROTATION(SolverType.EXAM, "Rot", new InfoSolverProperty("Exam Rotation Penalty", true)),
		EXAM_PERTURBATIONS(SolverType.EXAM, "Pert", new InfoSolverProperty("Perturbation Penalty", true)),
		STUDENT_ASSIGNED_CR(SolverType.STUDENT, "CourseReqs", new InfoSolverProperty("Assigned course requests", true)),
		STUDENT_COMPLETE(SolverType.STUDENT, "CompleteSt", new InfoSolverProperty("Students with complete schedule", true)),
		STUDENT_SELECTION(SolverType.STUDENT, "Selection", new InfoSolverProperty("Selection", true)),
		STUDENT_DIST_CONF(SolverType.STUDENT, "DistanceCf", new InfoSolverProperty("Student distance conflicts", true)),
		STUDENT_TIME_OVERLAPS(SolverType.STUDENT, "TimeCf", new InfoSolverProperty("Time overlapping conflicts", true)),
		//STUDENT_FREE_OVERLAPS(SolverType.STUDENT, "FreeConf", new InfoSolverProperty("Free time overlapping conflicts", true)),
		//STUDENT_DISBALANCE_AVG(SolverType.STUDENT, "AvgDisb", new InfoSolverProperty("Average disbalance", true)),
		//STUDENT_DISBALANCE_10P(SolverType.STUDENT, "Disb[>=10%]", new InfoSolverProperty("Sections disbalanced by 10% or more", true)),
		STUDENT_PERTURBATIONS(SolverType.STUDENT, "Pert", new InfoSolverProperty("Perturbations: Total penalty", true)),
		INSTRUCTOR_ATTR_PREF(SolverType.INSTRUCTOR, "Attribute", new InfoSolverProperty("Attribute Preferences", true)),
		INSTRUCTOR_COURSE_PREF(SolverType.INSTRUCTOR, "Course", new InfoSolverProperty("Course Preferences", true)),
		INSTRUCTOR_INSTR_PREF(SolverType.INSTRUCTOR, "Instructor", new InfoSolverProperty("Instructor Preferences", true)),
		INSTRUCTOR_TEACH_PREF(SolverType.INSTRUCTOR, "Teaching", new InfoSolverProperty("Teaching Preferences", true)),
		INSTRUCTOR_TIME_PREF(SolverType.INSTRUCTOR, "Time", new InfoSolverProperty("Time Preferences", true)),
		INSTRUCTOR_SAME_INSTRUCTOR(SolverType.INSTRUCTOR, "SameInstr", new InfoSolverProperty("Same Instructor", true)),
		INSTRUCTOR_SAME_LECTURE(SolverType.INSTRUCTOR, "SameLect", new InfoSolverProperty("Same Lecture", true)),
		INSTRUCTOR_BTB(SolverType.INSTRUCTOR, "BTB", new InfoSolverProperty("Back To Back", true)),
		INSTRUCTOR_SAME_DAYS(SolverType.INSTRUCTOR, "SameDays", new InfoSolverProperty("Same Days", true)),
		INSTRUCTOR_SAME_ROOM(SolverType.INSTRUCTOR, "SameRoom", new InfoSolverProperty("Same Room", true)),
		INSTRUCTOR_PERTURBATIONS(SolverType.INSTRUCTOR, "Original", new InfoSolverProperty("Original Instructor", true)),
		OPERATIONS("Operation(s)", new StringSolverProperty() {
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
				if (p.getType() == null || p.getType() == type) ret.add(p);
			}
			return ret;
		}
	}
	
	protected static String getTableName(SolverType type) {
		switch (type) {
		case COURSE: return "Manage Course Timetabling Solvers";
		case EXAM: return "Manage Examination Timetabling Solvers";
		case STUDENT: return "Manage Batch Student Scheduling Solvers";
		case INSTRUCTOR: return "Manage Instructor Scheduling Solvers";
		}
		return "Manage " + Constants.toInitialCase(type.name()) + " Solvers";
	}
	
	private void createSolverTable(HttpServletRequest request, SolverType type) {
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
		WebTable webTable = new WebTable(props.size(), getTableName(type), "manageSolvers.do?ord" + type.ordinal() + "=%%", names, align, ord);
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
			webTable.addLine(null, new String[] {"<i>No solver is running.</i>"}, null, null );
		request.setAttribute("ManageSolvers.table[" + type + "]", webTable.printTable(WebTable.getOrder(sessionContext, "manageSolvers.ord[" + type + "]")));
	}
	

	private void getServers(HttpServletRequest request) throws Exception {
		try {
			WebTable.setOrder(sessionContext,"manageSolvers.ord[SERVERS]", request.getParameter("ords"),1);
			
			WebTable webTable = new WebTable( 12,
					"Available Servers", "manageSolvers.do?ords=%%",
					new String[] {"Host", "Version", "Started", "Available Memory", "NrCores", "Ping", "Usage", "NrInstances", "Active", "Working", "Passivated", "Operation(s)"},
					new String[] {"left", "left", "left", "left", "left", "left", "left", "left", "left","left","left","left"},
					null );
			webTable.setRowStyle("white-space:nowrap");
			
			DecimalFormat df = new DecimalFormat("0.00");
			
			int nrLines = 0;
			
			for (SolverServer server: solverServerService.getServers(false)) {
                    if (!server.isActive()) {
                        webTable.addLine(null, new String[] {
                                server.getHost(),
                                "<i>inactive</i>",
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
                                new Long(-1),
                                new Integer(-1),
                                new Long(-1),
                                new Long(-1),
                                new Integer(-1),
                                new Integer(-1),
                                new Integer(-1),
                                new Integer(-1),
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
                    if (usage >= 1000) {
                        op+="<input type=\"button\" value=\"Enable\" onClick=\"if (confirm('Do you really want to enable server "+server.getHost()+" for the new solver instances?')) document.location='manageSolvers.do?op=Start%20Using&solver="+server.getHost()+"';\">&nbsp;&nbsp;";
                    } else {
                        op+="<input type=\"button\" value=\"Disable\" onClick=\"if (confirm('Do you really want to disable server "+server.getHost()+" for the new solver instances?')) document.location='manageSolvers.do?op=Stop%20Using&solver="+server.getHost()+"';\">&nbsp;&nbsp;";
                    }
                	op+="<input type=\"button\" value=\"Reset\" onClick=\"if (confirm('Do you really want to reset server "+server.getHost()+"?')) document.location='manageSolvers.do?op=Reset&solver="+server.getHost()+"';\">&nbsp;&nbsp;";
                    if (!local) {
                    	op+="<input type=\"button\" value=\"Shutdown\" onClick=\"if (confirm('Do you really want to shutdown server "+server.getHost()+"?')) document.location='manageSolvers.do?op=Shutdown&solver="+server.getHost()+"';\">&nbsp;&nbsp;";
                    }
                    Set<String> flags = new TreeSet<String>();
                    if (local) flags.add("tomcat");
                    if (server.isCoordinator()) flags.add("coordinator");
                    if (!server.isAvailable()) flags.add("unavailable");
                    webTable.addLine(null, new String[] {
                            server.getHost() + (flags.isEmpty() ? "" : " " + flags.toString()),
                            (version==null||"-1".equals(version)?"<i>N/A</i>":version),
                            (startTime==null?"<i>N/A</i>":sDF.format(startTime)),
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
                            new Long(t1-t0),
                            new Integer(cores),
                            new Long(mem),
                            new Long(usage),
                            new Integer(nrActive+nrPassivated),
                            new Integer(nrActive),
                            new Integer(nrWorking),
                            new Integer(nrPassivated),
                            null
                    });
                    nrLines++;
            }
			

			if (nrLines==0)
				webTable.addLine(null, new String[] {"<i>No solver server is running.</i>"}, null, null );

			request.setAttribute("ManageSolvers.table[SERVERS]",webTable.printTable(WebTable.getOrder(sessionContext,"manageSolvers.ord[SERVERS]")));
			
	    } catch (Exception e) {
	        throw new Exception(e);
	    }
	}
       
       private void getOnlineSolvers(HttpServletRequest request) throws Exception {
           try {
               WebTable.setOrder(sessionContext,"manageSolvers.ord[ONLINE]",request.getParameter("ordo"),1);
               
               WebTable webTable = new WebTable( 14,
                       "Manage Online Scheduling Servers", "manageSolvers.do?ordo=%%",
                       new String[] {"Created", "Session", "Host", "Mode", "Mem", "Assign", "Total", "CompSched", "DistConf", "TimeConf", "FreeConf", "AvgDisb", "Disb[>=10%]", "Operation(s)"},
                       new String[] {"left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left", "left"},
                       null );
               webTable.setRowStyle("white-space:nowrap");
               
               int nrLines = 0;
               
               List<SolverServer> servers = solverServerService.getServers(true);
               for (SolverServer server: servers) {
                   for (String sessionId : server.getOnlineStudentSchedulingContainer().getSolvers()) {
                	   OnlineSectioningServer solver = server.getOnlineStudentSchedulingContainer().getSolver(sessionId);
                	   if (solver==null) continue;
                	   DataProperties properties = solver.getConfig();
                	   if (properties==null) continue;
                       if (sessionContext.getUser().getAuthorities(sessionContext.getUser().getCurrentAuthority().getRole(), new SimpleQualifier("Session", Long.valueOf(sessionId))).isEmpty()) continue;
                       String sessionLabel = solver.getAcademicSession().toString();
                       String mode = solver.getAcademicSession().isSectioningEnabled() ? "Online" : "Assistant";
                       Map<String,String> info = (solver.isReady() ? solver.execute(solver.createAction(GetInfo.class), null) : null);
                       String assigned = (info == null ? null : info.get("Assigned variables"));
                       String totVal = (info == null ? null : info.get("Overall solution value"));
                       String compSch = (info == null ? null : info.get("Students with complete schedule"));
                       String distConf = (info == null ? null : info.get("Student distance conflicts"));
                       String time = (info == null ? null : info.get("Time overlapping conflicts"));
                       String free = (info == null ? null : info.get("Free time overlapping conflicts"));
                       String disb = (info == null ? null : info.get("Average disbalance"));
                       String disb10 = (info == null ? null : info.get("Sections disbalanced by 10% or more"));
                       Date loaded = new Date(solver.getConfig().getPropertyLong("General.StartUpDate", 0));

                       String op = "";
                       if (solver.isMaster() && solver.isReady()) {
                    	   op += "<input type=\"button\" value=\"Reload\" onClick=\"" +
                       			"if (confirm('Do you really want to reload this server?')) " +
                       			"document.location='manageSolvers.do?op=Reload&onlineId=" + sessionId + "';" + 
                       			" event.cancelBubble=true;\">&nbsp;&nbsp;";
                       }
                       if (solver.isMaster() && servers.size() > 1) {
                           op += "<input type=\"button\" value=\"Shutdown All\" onClick=\"" +
                        			"if (confirm('Do you really want to shutdown this server?')) " +
                        			"document.location='manageSolvers.do?op=Unload&onlineId=" + sessionId + "';" + 
                        			" event.cancelBubble=true;\">&nbsp;&nbsp;";
                           op += "<input type=\"button\" value=\"Un-Master\" onClick=\"" +
                         			"if (confirm('Do you really want to un-master this server?')) " +
                         			"document.location='manageSolvers.do?op=Unmaster&onlineId=" + sessionId + "&host=" + server.getHost() + "';" + 
                         			" event.cancelBubble=true;\">";
                       } else {
                           op += "<input type=\"button\" value=\"Shutdown\" onClick=\"" +
                        			"if (confirm('Do you really want to shutdown this server?')) " +
                        			"document.location='manageSolvers.do?op=Unload&onlineId=" + sessionId + "&host=" + server.getHost() + "';" + 
                        			" event.cancelBubble=true;\">";
                       }
                       
                       webTable.addLine(null, new String[] {
                                   (loaded.getTime() <= 0 ? "N/A" : sDF.format(loaded)),
                                   sessionLabel,
                                   solver.getHost() + (solver.isMaster() ? " (master)" : ""),
                                   mode,
                                   "<span name='UniTimeGWT:SolverAllocatedMem' style='display: none;'>O" + server.getHost() + ":" + sessionId + "</span>",
                                   (assigned==null?"N/A":assigned),
                                   (totVal==null?"N/A":totVal),
                                   (compSch==null?"N/A":compSch), 
                                   (distConf==null?"N/A":distConf),
                                   (time==null?"N/A":time),
                                   (free==null?"N/A":free),
                                   (disb==null?"N/A":disb),
                                   (disb10==null?"N/A":disb10),
                                   op},
                               new Comparable[] {
                                   loaded,
                                   sessionLabel,
                                   solver.getHost(),
                                   mode, 
                                   null,
                                   (assigned==null?"":assigned),
                                   (totVal==null?"":totVal),
                                   (compSch==null?"":compSch), 
                                   (distConf==null?"":distConf),
                                   (time==null?"":time),
                                   (free==null?"":free),
                                   (disb==null?"":disb),
                                   (disb10==null?"":disb10),
                                   null});
                           nrLines++;
                   }
               }
               if (nrLines==0)
                   webTable.addLine(null, new String[] {"<i>There is no online student scheduling server running at the moment.</i>"}, null, null );
               request.setAttribute("ManageSolvers.table[ONLINE]",webTable.printTable(WebTable.getOrder(sessionContext,"manageSolvers.ord[ONLINE]")));
               
           } catch (Exception e) {
               throw new Exception(e);
           }
       }
}

