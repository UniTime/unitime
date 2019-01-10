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
package org.unitime.timetable.onlinesectioning.test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningTestFwk;
import org.unitime.timetable.onlinesectioning.basic.GetAssignment;
import org.unitime.timetable.onlinesectioning.basic.GetRequest;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.solver.ComputeSuggestionsAction;
import org.unitime.timetable.onlinesectioning.solver.FindAssignmentAction;
import org.unitime.timetable.onlinesectioning.updates.EnrollStudent;

import com.google.protobuf.CodedInputStream;

/**
 * @author Tomas Muller
 */
public class ReplayLogTest extends OnlineSectioningTestFwk {
	private File iLogFile = null;
	private Map<String, Long> iStudentIds = null;
	private Map<String, Long> iCourseIds = null;
	private Map<String, Long> iClassIds = null;
	
	private String[] sOkErrors = new String[] {
			"Unable to enroll into .*, the class is no longer available\\.",
			"No courses requested\\.",
	};
	
	private ReplayLogTest(File logFile) {
		iLogFile = logFile;
	}
	
	private OnlineSectioningLog.ExportedLog readLog(CodedInputStream cin) throws IOException {
		if (cin.isAtEnd()) return null;
		int size = cin.readInt32();
		int limit = cin.pushLimit(size);
		OnlineSectioningLog.ExportedLog ret = OnlineSectioningLog.ExportedLog.parseFrom(cin);
		cin.popLimit(limit);
		cin.resetSizeCounter();
		return ret;
	}
	
	private Long toStudentId(OnlineSectioningLog.Entity student) {
		if (iStudentIds == null) return student.getUniqueId();
		Long id = iStudentIds.get(student.getExternalId());
		return id == null ? student.getUniqueId() : id;
	}
	
	private Long toCourseId(OnlineSectioningLog.Entity course) {
		if (iCourseIds == null) return course.getUniqueId();
		return iCourseIds.get(course.getName());
	}
	
	private Long toClassId(OnlineSectioningLog.Section section) {
		if (iClassIds == null) return section.getClazz().getUniqueId();
		return iClassIds.get(section.getCourse().getName() + " " + section.getSubpart().getName() + " " + section.getClazz().getName());
	}
	
	private CourseRequestInterface.Request toRequest(OnlineSectioningLog.Request request) {
		CourseRequestInterface.Request ret = new CourseRequestInterface.Request();
		for (OnlineSectioningLog.Time time: request.getFreeTimeList()) {
			CourseRequestInterface.FreeTime freeTime = new CourseRequestInterface.FreeTime();
			freeTime.setStart(time.getStart());
			freeTime.setLength(time.getLength());
			for (DayCode c: DayCode.toDayCodes(time.getDays()))
				freeTime.addDay(c.ordinal());
			RequestedCourse rc = new RequestedCourse();
			ret.addRequestedCourse(rc);
			rc.addFreeTime(freeTime);
		}
		for (int i = 0; i < request.getCourseCount(); i++) {
			RequestedCourse rc = new RequestedCourse();
			if (request.getCourse(i).hasUniqueId())
				rc.setCourseId(request.getCourse(i).getUniqueId());
			rc.setCourseName(request.getCourse(i).getName());
			ret.addRequestedCourse(rc);
		}
		if (request.hasWaitList())
			ret.setWaitList(request.getWaitList());
		if (request.hasCritical())
			ret.setCritical(request.getCritical());
		return ret;
	}
	
	private ClassAssignmentInterface.ClassAssignment toAssignment(OnlineSectioningLog.Section section) {
		Long classId = toClassId(section);
		if (classId == null) return null;
		ClassAssignmentInterface.ClassAssignment a = new ClassAssignmentInterface.ClassAssignment();
		a.setClassId(classId);
		a.setClassNumber(section.getClazz().getExternalId());
		a.setSection(section.getClazz().getName());
		if (section.hasTime()) {
			a.setStart(section.getTime().getStart());
			a.setLength(section.getTime().getStart());
			a.setDatePattern(section.getTime().getPattern());
			for (DayCode c: DayCode.toDayCodes(section.getTime().getDays()))
				a.addDay(c.ordinal());
		}
		for (OnlineSectioningLog.Entity instructor: section.getInstructorList()) {
			a.addInstructor(instructor.getName());
			a.addInstructoEmail(instructor.hasExternalId() ? instructor.getExternalId() : null);
		}
		for (OnlineSectioningLog.Entity location: section.getLocationList()) {
			a.addRoom(location.getUniqueId(), location.getName());
		}
		a.setPinned(section.getPreference() == OnlineSectioningLog.Section.Preference.REQUIRED);
		a.setCourseId(toCourseId(section.getCourse()));
		String course = section.getCourse().getName(); int idx = course.indexOf(' ');
		a.setSubject(course.substring(0, idx));
		a.setCourseNbr(course.substring(idx + 1));
		a.setSubpart(section.getSubpart().getName());
		return a;
	}
	
	private FindAssignmentAction convertFindAssignment(OnlineSectioningLog.Action action) {
		CourseRequestInterface request = new CourseRequestInterface();
		Collection<ClassAssignmentInterface.ClassAssignment> assignment = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
		
		request.setAcademicSessionId(action.getSession().getUniqueId());
		request.setStudentId(toStudentId(action.getStudent()));
		for (OnlineSectioningLog.Request r: action.getRequestList()) {
			if (r.getAlternative())
				request.getAlternatives().add(toRequest(r));
			else
				request.getCourses().add(toRequest(r));
			
			for (OnlineSectioningLog.Section section: r.getSectionList()) {
				ClassAssignmentInterface.ClassAssignment a = toAssignment(section);
				if (a != null)
					assignment.add(a);
			}
		}
		if (request.getCourses().isEmpty()) return null;
		sLog.debug("Find assignment for " + request.getCourses() + " (" + assignment + ")");
		return createAction(FindAssignmentAction.class).forRequest(request).withAssignment(assignment);
	}
	
	private ComputeSuggestionsAction convertSuggestions(OnlineSectioningLog.Action action) {
		CourseRequestInterface request = new CourseRequestInterface();
		Collection<ClassAssignmentInterface.ClassAssignment> assignment = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
		ClassAssignmentInterface.ClassAssignment selected = null;
		
		request.setAcademicSessionId(action.getSession().getUniqueId());
		request.setStudentId(toStudentId(action.getStudent()));
		for (OnlineSectioningLog.Request r: action.getRequestList()) {
			if (r.getAlternative())
				request.getAlternatives().add(toRequest(r));
			else
				request.getCourses().add(toRequest(r));

			for (OnlineSectioningLog.Section section: r.getSectionList()) {
				ClassAssignmentInterface.ClassAssignment a = toAssignment(section);
				
				if (a != null) {
					if (section.getPreference() == OnlineSectioningLog.Section.Preference.SELECTED)
						selected = a;
					
					assignment.add(a);
				}
			}
		}
		
		if (request.getCourses().isEmpty() || selected == null) return null;
		sLog.debug("Find suggestions for " + request.getCourses() + " (" + selected + ")");
		return createAction(ComputeSuggestionsAction.class).forRequest(request).withAssignment(assignment).withSelection(selected);
	}
	
	public EnrollStudent convertEnrollFromReload(OnlineSectioningLog.Action action) {
		CourseRequestInterface request = new CourseRequestInterface();
		List<ClassAssignmentInterface.ClassAssignment> assignment = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
		
		request.setAcademicSessionId(action.getSession().getUniqueId());
		request.setStudentId(toStudentId(action.getStudent()));
		for (OnlineSectioningLog.Request r: action.getRequestList()) {
			if (r.getAlternative())
				request.getAlternatives().add(toRequest(r));
			else
				request.getCourses().add(toRequest(r));
		}
		
		for (OnlineSectioningLog.Enrollment enrollment: action.getEnrollmentList()) {
			if (enrollment.getType() == OnlineSectioningLog.Enrollment.EnrollmentType.STORED) {
				for (OnlineSectioningLog.Section section: enrollment.getSectionList()) {
					ClassAssignmentInterface.ClassAssignment a = toAssignment(section);
					if (a != null)
						assignment.add(a);
				}
			}
		}

		sLog.debug("Enroll for " + request.getCourses() + " (" + assignment + ")");
		return createAction(EnrollStudent.class).forStudent(request.getStudentId()).withRequest(request).withAssignment(assignment);
	}
	
	private OnlineSectioningAction<?> convert(OnlineSectioningLog.Action action) {
		if (action.getOperation().equals("section"))
			return convertFindAssignment(action);
		if (action.getOperation().equals("suggestions"))
			return convertSuggestions(action);
		if (action.getOperation().equals("reload-student"))
			return convertEnrollFromReload(action);
		return null;
	}
	
	@Override
	public List<Operation> operations() {
		if ("true".equals(System.getProperty("convertIds", "true"))) {
			org.hibernate.Session hibSession = new _RootDAO().getSession();
			try {
				iStudentIds = new HashMap<String, Long>();
				for (final Object[] o: (List<Object[]>)hibSession.createQuery(
						"select s.uniqueId, s.externalUniqueId from Student s where s.session.uniqueId = :sessionId")
						.setLong("sessionId", getServer().getAcademicSession().getUniqueId()).list()) {
					iStudentIds.put((String)o[1], (Long)o[0]);
				}
				iCourseIds = new HashMap<String, Long>();
				for (final Object[] o: (List<Object[]>)hibSession.createQuery(
						"select co.uniqueId, co.subjectAreaAbbv || ' ' || co.courseNbr from CourseOffering co where co.subjectArea.session.uniqueId = :sessionId")
						.setLong("sessionId", getServer().getAcademicSession().getUniqueId()).list()) {
					iCourseIds.put((String)o[1], (Long)o[0]);
				}
				iClassIds = new HashMap<String, Long>();
				for (Class_ c: (List<Class_>)hibSession.createQuery(
						"select c  from Class_ c inner join c.schedulingSubpart s inner join s.instrOfferingConfig.instructionalOffering io inner join io.courseOfferings co where io.session.uniqueId = :sessionId")
						.setLong("sessionId", getServer().getAcademicSession().getUniqueId()).list()) {
					iClassIds.put(c.getClassLabel(hibSession), c.getUniqueId());
				}
			} finally {
				hibSession.close();
			}
		}
		
		int nrTasks = Integer.valueOf(System.getProperty("nrTasks", "-1"));
		List<Operation> operations = new ArrayList<OnlineSectioningTestFwk.Operation>();
		try {
			FileInputStream in = new FileInputStream(iLogFile);
			try {
				CodedInputStream cin = CodedInputStream.newInstance(in);
	            cin.setSizeLimit(1024*1024*1024); // 1 GB
	            OnlineSectioningLog.ExportedLog log = null;
	            while ((log = readLog(cin)) != null) {
	            	List<OnlineSectioningAction<?>> actions = new ArrayList<OnlineSectioningAction<?>>();
	            	Long studentId = null;
	            	boolean hasSectionOrSuggestion = false;
	            	for (OnlineSectioningLog.Action action: log.getActionList()) {
	            		if (studentId == null && action.hasStudent() && action.getStudent().hasExternalId())
	            			studentId = toStudentId(action.getStudent());
	            		OnlineSectioningAction<?> a = convert(action);
	            		if (a != null) {
	            			if (a instanceof FindAssignmentAction || a instanceof ComputeSuggestionsAction)
	            				hasSectionOrSuggestion = true;
	            			actions.add(a);
	            		}
	            	}
	            	if (studentId != null && !actions.isEmpty() && hasSectionOrSuggestion)
	            		operations.add(new ReplayOperation(studentId, actions));
	            	
	            	if (nrTasks > 0 && operations.size() >= 3 * nrTasks) break;
	            }
			} finally {
				in.close();
			}			
		} catch (IOException e) {
			sLog.fatal("Failed to load log: " + e.getMessage(), e);
		}
		return operations;
	}
	
	private class ReplayOperation implements Operation {
		private Long iStudentId = null;
		private List<OnlineSectioningAction<?>> iActions = null;
		private OnlineSectioningServer iServer;
		private int iGood = 0;
		
		public ReplayOperation(Long studentId, List<OnlineSectioningAction<?>> actions) {
			iStudentId = studentId;
			iActions = actions;
		}
		
		private <E> E executeAction(OnlineSectioningAction<E> action) {
			E ret = null;
			try {
				ret = iServer.execute(action, user());
				iGood ++;
			} catch (SectioningException e) {
				for (String ok: sOkErrors)
					if (e.getMessage() != null && e.getMessage().matches(ok)) return null;
				sLog.warn("Failed to run " + action.name() + " for " + iStudentId + ": " + e.getMessage(), e);
			}
			return ret;
		}

		@Override
		public double execute(OnlineSectioningServer s) {
			iServer = s;
			
			XStudent student = iServer.getStudent(iStudentId);
			EnrollStudent back = null;
			if (student != null) {
				CourseRequestInterface request = iServer.execute(createAction(GetRequest.class).forStudent(iStudentId), user());
				ClassAssignmentInterface assignment = iServer.execute(createAction(GetAssignment.class).forStudent(iStudentId), user());
				List<ClassAssignmentInterface.ClassAssignment> assignments = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
				if (assignment != null)
					for (ClassAssignmentInterface.CourseAssignment ca: assignment.getCourseAssignments())
						assignments.addAll(ca.getClassAssignments());
				back = s.createAction(EnrollStudent.class).forStudent(iStudentId).withRequest(request).withAssignment(assignments);
			}

			
			boolean undo = false; 
			
			int nrActions = 0;
			try {
				for (OnlineSectioningAction<?> action: iActions) {
					if (action instanceof EnrollStudent) {
						if (back != null && iServer.getAcademicSession().isSectioningEnabled()) {
							undo = true;
							executeAction(action);
							nrActions ++;
						}
					} else {
						executeAction(action);
						nrActions ++;
					}
				}
			} catch (Exception e) {}
			
			if (undo && back != null) {
				executeAction(back);
				nrActions ++;
			}
			
			return nrActions == 0 ? 1.0 : ((double) iGood) / nrActions;
		}
		
	}
	
	public static void main(String args[]) {
		new ReplayLogTest(new File(args[0])).test(
				Integer.valueOf(System.getProperty("nrTasks", "-1")),
				Integer.valueOf(System.getProperty("nrConcurrent", "10")));
	}
}
