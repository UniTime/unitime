/*
 * UniTime 3.4 - 3.5 (University Timetabling Application)
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
package org.unitime.timetable.server.exams;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.events.EventEnrollmentsBackend;
import org.unitime.timetable.gwt.client.sectioning.ExaminationEnrollmentTable.ExaminationEnrollmentsRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.Conflict;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.Enrollment;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamOwner;
import org.unitime.timetable.model.ExamPeriod;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.exam.ExamSolverProxy;
import org.unitime.timetable.solver.exam.ui.ExamAssignment;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.BackToBackConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.DirectConflict;
import org.unitime.timetable.solver.exam.ui.ExamAssignmentInfo.MoreThanTwoADayConflict;
import org.unitime.timetable.solver.service.SolverService;
import org.unitime.timetable.util.Formats;

/**
 * @author Tomas Muller
 */
@GwtRpcImplements(ExaminationEnrollmentsRpcRequest.class)
public class ExaminationEnrollmentsBackend implements GwtRpcImplementation<ExaminationEnrollmentsRpcRequest, GwtRpcResponseList<ClassAssignmentInterface.Enrollment>> {
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);

	@Autowired SolverService<ExamSolverProxy> examinationSolverService;
	
	@Override
	public GwtRpcResponseList<Enrollment> execute(ExaminationEnrollmentsRpcRequest request, SessionContext context) {
		Exam exam = ExamDAO.getInstance().get(request.getExamId());
		
		context.checkPermission(exam, Right.ExaminationDetail);
		ExamSolverProxy proxy = examinationSolverService.getSolver();
		if (proxy != null && !exam.getExamType().getUniqueId().equals(proxy.getExamTypeId())) proxy = null;
		
		ExamAssignmentInfo assignment = null;
		ExamPeriod period = null;
		if (proxy != null) {
			assignment = proxy.getAssignmentInfo(exam.getUniqueId());
			period = (assignment == null ? null : assignment.getPeriod());
		} else {
			assignment = new ExamAssignmentInfo(exam, false);
			period = exam.getAssignedPeriod();
		}
		
		GwtRpcResponseList<ClassAssignmentInterface.Enrollment> ret = EventEnrollmentsBackend.convert(exam.getStudentClassEnrollments(), null);
		
		
		Formats.Format<Date> df = Formats.getDateFormat(Formats.Pattern.DATE_EXAM_PERIOD);

		Map<Long, List<Meeting>> conflicts = computeConflicts(exam.getUniqueId(), period);
		if (conflicts != null) {
			for (ClassAssignmentInterface.Enrollment enrollment: ret) {
				List<Meeting> conf = conflicts.get(enrollment.getStudent().getId());
				if (conf == null) continue;
				
				Map<Event, TreeSet<Meeting>> events = new HashMap<Event, TreeSet<Meeting>>();
				for (Meeting m: conf) {
					TreeSet<Meeting> ms = events.get(m.getEvent());
					if (ms == null) { ms = new TreeSet<Meeting>(); events.put(m.getEvent(), ms); }
					ms.add(m);
				}
				for (Event confEvent: new TreeSet<Event>(events.keySet())) {
					Conflict conflict = new Conflict();
					conflict.setName(confEvent.getEventName());
					conflict.setType(confEvent.getEventTypeAbbv());
					String lastDate = null, lastTime = null, lastRoom = null;
					for (Meeting m: events.get(confEvent)) {
						String date = df.format(m.getMeetingDate());
						if (lastDate == null) {
							conflict.setDate(date);
						} else if (lastDate.equals(date)) {
							conflict.setDate(conflict.getDate() + "<br>");
						} else {
							conflict.setDate(conflict.getDate() + "<br>" + date);
						}
						lastDate = date;
						
						String time = m.startTime() + " - " + m.stopTime();
						if (lastTime == null) {
							conflict.setTime(time);
						} else if (lastTime.equals(time)) {
							conflict.setTime(conflict.getTime() + "<br>");
						} else {
							conflict.setTime(conflict.getTime() + "<br>" + time);
						}
						lastTime = time;
						
						String room = m.getLocation() == null ? "" : m.getLocation().getLabel();
						if (lastRoom == null) {
							conflict.setRoom(room);
						} else if (lastRoom.equals(room)) {
							conflict.setRoom(conflict.getRoom() + "<br>");
						} else {
							conflict.setRoom(conflict.getRoom() + "<br>" + room);
						}
						lastRoom = room;
					}

					conflict.setStyle("dc");
					enrollment.addConflict(conflict);
				}
			}
		}
		
		if (assignment != null) {
	    	for (DirectConflict conflict: assignment.getDirectConflicts()) {
	    		ExamAssignment other = conflict.getOtherExam();
	    		if (other == null) continue;
	    		
	    		Conflict conf = new Conflict();
	    		conf.setName(other.getExamName());
    			conf.setType("Direct");
    			conf.setDate(df.format(other.getPeriod().getStartDate()));
    			conf.setTime(other.getTime(false));
    			conf.setRoom(other.getRoomsName(false, ", "));
    			conf.setStyle("dc");
				
				for (Long studentId: conflict.getStudents()) {
	    			for (ClassAssignmentInterface.Enrollment enrollment: ret) {
	    				if (enrollment.getStudent().getId() == studentId)
	    					enrollment.addConflict(conf);
	    			}
	    		}
	    	}
	    	for (BackToBackConflict conflict: assignment.getBackToBackConflicts()) {
	    		ExamAssignment other = conflict.getOtherExam();
	    		Conflict conf = new Conflict();
	    		conf.setName(other.getExamName());
				conf.setType("Back-To-Back");
				conf.setDate(df.format(other.getPeriod().getStartDate()));
				conf.setTime(other.getTime(false));
				conf.setRoom(other.getRoomsName(false, ", "));
				conf.setStyle("b2b");
				
				for (Long studentId: conflict.getStudents()) {
	    			for (ClassAssignmentInterface.Enrollment enrollment: ret) {
	    				if (enrollment.getStudent().getId() == studentId)
	    					enrollment.addConflict(conf);
	    			}
	    		}
	    	}
	    	for (MoreThanTwoADayConflict conflict: assignment.getMoreThanTwoADaysConflicts()) {
	    		String name = null, date = null, time = null, room = null;
	    		for (ExamAssignment other: conflict.getOtherExams()) {
	    			if (name == null) {
	    				name = other.getExamName();
	    				date = df.format(other.getPeriod().getStartDate());
	    				time = other.getTime(false);
	    				room = other.getRoomsName(false, ", ");
	    			} else {
	    				name += "<br>" + other.getExamName();
	    				date += "<br>" + df.format(other.getPeriod().getStartDate());
	    				time += "<br>" + other.getTime(false);
	    				room += "<br>" + other.getRoomsName(false, ", ");
	    			}
	    		}
	    		
	    		Conflict conf = new Conflict();
	    		conf.setName(name);
				conf.setType("&gt;2 A Day");
				conf.setDate(date);
				conf.setTime(time);
				conf.setRoom(room);
				conf.setStyle("m2d");
				for (Long studentId: conflict.getStudents()) {
	    			for (ClassAssignmentInterface.Enrollment enrollment: ret) {
	    				if (enrollment.getStudent().getId() == studentId)
	    					enrollment.addConflict(conf);
	    			}
	    		}
	    	}
		}
		
		return ret;
	}
	
	static String where(int type, int idx) {
		switch (type) {
		case ExamOwner.sOwnerTypeClass:
			return " and o" + idx + ".ownerType = " + type + " and o" + idx + ".ownerId = s" + idx + ".clazz.uniqueId";
		case ExamOwner.sOwnerTypeConfig:
			return " and o" + idx + ".ownerType = " + type + " and o" + idx + ".ownerId = s" + idx + ".clazz.schedulingSubpart.instrOfferingConfig.uniqueId";
		case ExamOwner.sOwnerTypeCourse:
			return " and o" + idx + ".ownerType = " + type + " and o" + idx + ".ownerId = s" + idx + ".courseOffering.uniqueId";
		case ExamOwner.sOwnerTypeOffering:
			return " and o" + idx + ".ownerType = " + type + " and o" + idx + ".ownerId = s" + idx + ".courseOffering.instructionalOffering.uniqueId";
		default:
			return "";
		}
	}
	
	private Map<Long, List<Meeting>> computeConflicts(Long examId, ExamPeriod period) {
		if (period == null) return null;

		Map<Long, List<Meeting>> conflicts = new HashMap<Long, List<Meeting>>();
		
        org.hibernate.Session hibSession = EventDAO.getInstance().getSession();

        int nrTravelSlotsClassEvent = ApplicationProperty.ExaminationTravelTimeClass.intValue();
        int nrTravelSlotsCourseEvent = ApplicationProperty.ExaminationTravelTimeCourse.intValue();
        
        // class events
        for (int t2 = 0; t2 < ExamOwner.sOwnerTypes.length; t2++) {
        	for (Object[] o: (List<Object[]>)hibSession.createQuery(
        			"select s1.student.uniqueId, m1" +
        			" from StudentClassEnrollment s1, ClassEvent e1 inner join e1.meetings m1, Exam e2 inner join e2.owners o2, StudentClassEnrollment s2" +
        			" where e2.uniqueId = :examId and e1.clazz = s1.clazz and s1.student = s2.student" +
        			where(t2, 2) + 
        			" and m1.meetingDate = :meetingDate and m1.startPeriod < :endSlot and :startSlot < m1.stopPeriod")
        			.setLong("examId", examId)
        			.setDate("meetingDate", period.getStartDate())
        			.setInteger("startSlot", period.getStartSlot() - nrTravelSlotsClassEvent)
        			.setInteger("endSlot", period.getEndSlot() + nrTravelSlotsClassEvent)
        			.list()) {
        		Long studentId = (Long)o[0];
        		Meeting meeting = (Meeting)o[1];
        		List<Meeting> meetings = conflicts.get(studentId);
        		if (meetings == null) {
        			meetings = new ArrayList<Meeting>(); conflicts.put(studentId, meetings);
        		}
        		meetings.add(meeting);
        	}
        }
        
    	// course events
        for (int t1 = 0; t1 < ExamOwner.sOwnerTypes.length; t1++) {
            for (int t2 = 0; t2 < ExamOwner.sOwnerTypes.length; t2++) {
            	for (Object[] o: (List<Object[]>)hibSession.createQuery(
            			"select s1.student.uniqueId, m1" +
            			" from StudentClassEnrollment s1, CourseEvent e1 inner join e1.meetings m1 inner join e1.relatedCourses o1, Exam e2 inner join e2.owners o2, StudentClassEnrollment s2" +
            			" where e2.uniqueId = :examId and s1.student = s2.student" +
            			where(t1, 1) + where(t2, 2) +
            			" and m1.meetingDate = :meetingDate and m1.startPeriod < :endSlot and :startSlot < m1.stopPeriod and e1.reqAttendance = true and m1.approvalStatus = 1")
            			.setLong("examId", examId)
            			.setDate("meetingDate", period.getStartDate())
            			.setInteger("startSlot", period.getStartSlot() - nrTravelSlotsCourseEvent)
            			.setInteger("endSlot", period.getEndSlot() + nrTravelSlotsCourseEvent)
            			.list()) {
            		Long studentId = (Long)o[0];
            		Meeting meeting = (Meeting)o[1];
            		List<Meeting> meetings = conflicts.get(studentId);
            		if (meetings == null) {
            			meetings = new ArrayList<Meeting>(); conflicts.put(studentId, meetings);
            		}
            		meetings.add(meeting);
            	}
            }
        }
    	
    	return conflicts;
	}

}
