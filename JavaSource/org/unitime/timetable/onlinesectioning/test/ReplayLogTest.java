/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2013, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
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
		return id == null ? student.getUniqueId() : null;
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
			ret.addRequestedFreeTime(freeTime);
		}
		for (int i = 0; i < request.getCourseCount(); i++) {
			switch (i) {
			case 0:
				ret.setRequestedCourse(request.getCourse(i).getName());
				break;
			case 1:
				ret.setFirstAlternative(request.getCourse(i).getName());
				break;
			case 2:
				ret.setSecondAlternative(request.getCourse(i).getName());
				break;
			}
		}
		if (request.hasWaitList())
			ret.setWaitList(request.getWaitList());
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
			a.addRoom(location.getName());
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
		return new FindAssignmentAction(request, assignment);
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
		return new ComputeSuggestionsAction(request, assignment, selected, null);
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
		return new EnrollStudent(request.getStudentId(), request, assignment);
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
	            	for (OnlineSectioningLog.Action action: log.getActionList()) {
	            		if (studentId == null && action.hasStudent() && action.getStudent().hasExternalId())
	            			studentId = toStudentId(action.getStudent());
	            		OnlineSectioningAction<?> a = convert(action);
	            		if (a != null) {
	            			actions.add(a);
	            		}
	            	}
	            	if (studentId != null && !actions.isEmpty())
	            		operations.add(new ReplayOperation(studentId, actions));
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
				sLog.warn("Failed to run " + action.name() + " for " + iStudentId + ": " + e.getMessage());
			}
			return ret;
		}

		@Override
		public double execute(OnlineSectioningServer s) {
			iServer = s;
			
			XStudent student = iServer.getStudent(iStudentId);
			EnrollStudent back = null;
			if (student != null) {
				CourseRequestInterface request = iServer.execute(new GetRequest(iStudentId), user());
				ClassAssignmentInterface assignment = iServer.execute(new GetAssignment(iStudentId), user());
				List<ClassAssignmentInterface.ClassAssignment> assignments = new ArrayList<ClassAssignmentInterface.ClassAssignment>();
				if (assignment != null)
					for (ClassAssignmentInterface.CourseAssignment ca: assignment.getCourseAssignments())
						assignments.addAll(ca.getClassAssignments());
				back = new EnrollStudent(iStudentId, request, assignments);
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
		new ReplayLogTest(new File(args[0])).test(-1, Integer.valueOf(System.getProperty("nrConcurrent", "10")));
	}
}
