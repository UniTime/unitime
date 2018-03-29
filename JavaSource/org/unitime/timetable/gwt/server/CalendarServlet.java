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
package org.unitime.timetable.gwt.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeSet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.cpsolver.coursett.model.TimeLocation;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.unitime.commons.CalendarVTimeZoneGenerator;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.action.PersonalizedExamReportAction;
import org.unitime.timetable.events.EventDetailBackend;
import org.unitime.timetable.events.QueryEncoderBackend;
import org.unitime.timetable.export.events.EventsExportEventsToICal;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.ExamStatus;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.Meeting;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.CurriculumDAO;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.model.dao.ExamDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.server.CourseDetailsBackend;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;

import biweekly.ICalVersion;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.component.VFreeBusy;
import biweekly.io.text.ICalWriter;
import biweekly.parameter.Role;
import biweekly.property.Attendee;
import biweekly.property.CalendarScale;
import biweekly.property.DateEnd;
import biweekly.property.DateStart;
import biweekly.property.ExceptionDates;
import biweekly.property.Method;
import biweekly.property.Organizer;
import biweekly.property.Status;
import biweekly.util.Recurrence;
import biweekly.util.Recurrence.DayOfWeek;
import biweekly.util.Recurrence.Frequency;


/**
 * @author Tomas Muller
 */
public class CalendarServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	protected static final GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	private static Logger sLog = Logger.getLogger(CalendarServlet.class);

	@Override
	public void init() {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}
	
	@Autowired SessionContext sessionContext;
	private SessionContext getSessionContext() { return sessionContext; }
	
	@Autowired SolverServerService solverServerService;
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Params params = null;
		String q = request.getParameter("q");
		if (q != null) {
			params = new QParams(q);
		} else {
			params = new HttpParams(request);
		}
		Long sessionId = null;
		if (params.getParameter("sid") != null) {
			sessionId = Long.valueOf(params.getParameter("sid"));
		} else {
			UserContext user = (getSessionContext() == null ? null : getSessionContext().getUser());
			if (user != null)
				sessionId = (Long)user.getCurrentAcademicSessionId();
			else
				sessionId = (Long)request.getSession().getAttribute("sessionId");
		}
		if (params.getParameter("term") != null) {
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			try {
				List<Long> sessions = hibSession.createQuery("select s.uniqueId from Session s where " +
						"s.academicTerm || s.academicYear = :term or " +
						"s.academicTerm || s.academicYear || s.academicInitiative = :term").
						setString("term", params.getParameter("term")).list();
				if (!sessions.isEmpty())
					sessionId = sessions.get(0);
			} finally {
				hibSession.close();
			}
		}
		if (sessionId == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No academic session provided.");
			return;
		}
		Session session = SessionDAO.getInstance().get(sessionId);
		if (session == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Academic session does not exist.");
			return;
		}
		String classIds = params.getParameter("cid");
    	String fts = params.getParameter("ft");
    	String examIds = params.getParameter("xid");
    	String eventIds = params.getParameter("eid");
    	String userId = params.getParameter("uid");
    	if (q == null) userId = QueryEncoderBackend.decode(userId);
   
		response.setContentType("text/calendar; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader( "Content-Disposition", "attachment; filename=\"schedule.ics\"" );
        
		ICalendar ical = new ICalendar();
		ical.setVersion(ICalVersion.V2_0);
		ical.setCalendarScale(CalendarScale.gregorian());
		ical.setMethod(new Method("PUBLISH"));
		ical.setExperimentalProperty("X-WR-CALNAME", "UniTime Schedule");
		ical.setExperimentalProperty("X-WR-TIMEZONE", TimeZone.getDefault().getID());
		ical.setProductId("-//UniTime LLC/UniTime " + Constants.getVersion() + " Schedule//EN");

		org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
		try {
			EventsExportEventsToICal exporter = new EventsExportEventsToICal();
        	if (classIds != null && !classIds.isEmpty()) {
        		for (String classId: classIds.split(",")) {
        			if (classId.isEmpty()) continue;
        			String[] courseAndClassId = classId.split("-");
        			if (courseAndClassId.length != 2) continue;
    				CourseOffering course = CourseOfferingDAO.getInstance().get(Long.valueOf(courseAndClassId[0]), hibSession);
    				Class_ clazz = Class_DAO.getInstance().get(Long.valueOf(courseAndClassId[1]), hibSession);
    				if (course == null || clazz == null) continue;
            		printClass(course, clazz, ical);
        		}
        	}
        	if (fts != null && !fts.isEmpty()) {
    			Date dpFirstDate = DateUtils.getDate(1, session.getPatternStartMonth(), session.getSessionStartYear());
    			BitSet weekCode = session.getDefaultDatePattern().getPatternBitSet();
        		for (String ft: fts.split(",")) {
        			if (ft.isEmpty()) continue;
        			String[] daysStartLen = ft.split("-");
        			if (daysStartLen.length != 3) continue;
        			printFreeTime(dpFirstDate, weekCode, daysStartLen[0], Integer.parseInt(daysStartLen[1]), Integer.parseInt(daysStartLen[2]), ical);
        		}
        	}
            if (examIds != null && !examIds.isEmpty()) {
            	for (String examId: examIds.split(",")) {
            		if (examId.isEmpty()) continue;
            		try {
                		Exam exam = ExamDAO.getInstance().get(Long.valueOf(examId), hibSession);
                		if (exam != null)
                			printExam(exam, ical);
            		} catch (NumberFormatException e) {}
            	}
            }
            if (eventIds != null && !eventIds.isEmpty()) {
            	for (String eventId: eventIds.split(",")) {
            		if (eventId.isEmpty()) continue;
            		try {
            			Event event = EventDAO.getInstance().get(Long.valueOf(eventId), hibSession);
            			if (event != null)
            				exporter.print(ical, EventDetailBackend.getEventDetail(session, event, null));
            		} catch (NumberFormatException e) {}
            	}
            }
            if (userId != null && !userId.isEmpty()) {
                for (DepartmentalInstructor instructor: (List<DepartmentalInstructor>)hibSession.createQuery("select i from DepartmentalInstructor i " +
                		"where i.externalUniqueId = :externalId and i.department.session.uniqueId = :sessionId").
                		setLong("sessionId", sessionId).setString("externalId", userId).list()) {
                	if (!PersonalizedExamReportAction.canDisplay(instructor.getDepartment().getSession())) continue;
                	for (ExamType t: ExamType.findAll(hibSession)) {
                		ExamStatus status = ExamStatus.findStatus(hibSession, instructor.getSession().getUniqueId(), t.getUniqueId());
                		DepartmentStatusType type = (status == null || status.getStatus() == null ? instructor.getSession().getStatusType() : status.getStatus());
                		if (t.getType() == ExamType.sExamTypeFinal && type.canNoRoleReportExamFinal()) {
                			for (Exam exam: instructor.getExams(t))
                				printExam(exam, ical);
                		} else if (t.getType() == ExamType.sExamTypeMidterm && type.canNoRoleReportExamMidterm()) {
                			for (Exam exam: instructor.getExams(t))
                				printExam(exam, ical);
                		}
                	}
                    if (instructor.getDepartment().getSession().getStatusType().canNoRoleReportClass()) {
                        for (ClassInstructor ci: instructor.getClasses()) {
                            printClass(ci.getClassInstructing().getSchedulingSubpart().getInstrOfferingConfig().getControllingCourseOffering(), ci.getClassInstructing(), ical);
                        }
                    }
                }
                for (Student student: (List<Student>)hibSession.createQuery("select s from Student s where " +
                		"s.externalUniqueId=:externalId and s.session.uniqueId = :sessionId").
                		setLong("sessionId", sessionId).setString("externalId", userId).list()) {
                	if (!PersonalizedExamReportAction.canDisplay(student.getSession())) continue;
                	for (ExamType t: ExamType.findAll(hibSession)) {
                		ExamStatus status = ExamStatus.findStatus(hibSession, student.getSession().getUniqueId(), t.getUniqueId());
                		DepartmentStatusType type = (status == null || status.getStatus() == null ? student.getSession().getStatusType() : status.getStatus());
                		if (t.getType() == ExamType.sExamTypeFinal && type.canNoRoleReportExamFinal()) {
                			for (Exam exam: student.getExams(t))
                				printExam(exam, ical);
                		} else if (t.getType() == ExamType.sExamTypeMidterm && type.canNoRoleReportExamMidterm()) {
                			for (Exam exam: student.getExams(t))
                				printExam(exam, ical);
                		}
                	}
                    if (student.getSession().getStatusType().canNoRoleReportClass()) {
                        for (Iterator i=student.getClassEnrollments().iterator();i.hasNext();) {
                            StudentClassEnrollment sce = (StudentClassEnrollment)i.next();
                            printClass(sce.getCourseOffering(), sce.getClazz(), ical);
                        }
                    }
                }
            }
        } catch (Exception e) {
        	Debug.error(e.getMessage(), e);
        	response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        }
		
		PrintWriter out = response.getWriter();
        ICalWriter writer = new ICalWriter(out, ICalVersion.V2_0);
		try {
			try {
				writer.getTimezoneInfo().setGenerator(new CalendarVTimeZoneGenerator());
				writer.getTimezoneInfo().setDefaultTimeZone(TimeZone.getDefault());
			} catch (IllegalArgumentException e) {
	        	sLog.warn("Failed to set default time zone: " + e.getMessage());
	        }
        	writer.write(ical);
        	writer.flush();
        	out.flush();
		} finally {
			out.close();
			writer.close();
		}
	}

	private void printExam(Exam exam, ICalendar ical) throws IOException {
		if (exam.getAssignedPeriod() == null) return;

        VEvent vevent = new VEvent();
        vevent.setSequence(0);
        vevent.setUid(exam.getUniqueId().toString());
    	DateStart dstart = new DateStart(exam.getAssignedPeriod().getStartTime(), true);
    	vevent.setDateStart(dstart);
        Calendar endTime = Calendar.getInstance(); endTime.setTime(exam.getAssignedPeriod().getStartTime());
        endTime.add(Calendar.MINUTE, exam.getLength());
    	DateEnd dend = new DateEnd(endTime.getTime(), true);
    	vevent.setDateEnd(dend);
    	vevent.setSummary(exam.getLabel()+" ("+exam.getExamType().getLabel()+" Exam)");
        if (!exam.getAssignedRooms().isEmpty()) {
            String rooms = "";
            for (Iterator i=new TreeSet(exam.getAssignedRooms()).iterator();i.hasNext();) {
                Location location = (Location)i.next();
                if (rooms.length()>0) rooms+=", ";
                rooms+=location.getLabel();
            }
            vevent.setLocation(rooms);
        }
        vevent.setStatus(Status.confirmed());
        ical.addEvent(vevent);
	}

	private void printClass(CourseOffering course, Class_ clazz, ICalendar ical) throws IOException {
		Assignment assignment = clazz.getCommittedAssignment();
		if (assignment == null) return;
		TimeLocation time = assignment.getTimeLocation();
		if (time == null || time.getWeekCode().isEmpty()) return;
		
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat tf = new SimpleDateFormat("HHmmss");
        tf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date dpFirstDate = DateUtils.getDate(1, clazz.getSession().getPatternStartMonth(), clazz.getSession().getSessionStartYear());
    	Calendar cal = Calendar.getInstance(Locale.US); cal.setLenient(true);
    	cal.setTime(dpFirstDate);
    	int idx = time.getWeekCode().nextSetBit(0);
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	cal.set(Calendar.HOUR_OF_DAY, Constants.toHour(time.getStartSlot()));
    	cal.set(Calendar.MINUTE, Constants.toMinute(time.getStartSlot()));
    	cal.set(Calendar.SECOND, 0);
    	Date first = null;
    	while (idx < time.getWeekCode().size() && first == null) {
    		if (time.getWeekCode().get(idx)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		switch (dow) {
        		case Calendar.MONDAY:
        			if ((time.getDayCode() & DayCode.MON.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.TUESDAY:
        			if ((time.getDayCode() & DayCode.TUE.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.WEDNESDAY:
        			if ((time.getDayCode() & DayCode.WED.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.THURSDAY:
        			if ((time.getDayCode() & DayCode.THU.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.FRIDAY:
        			if ((time.getDayCode() & DayCode.FRI.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.SATURDAY:
        			if ((time.getDayCode() & DayCode.SAT.getCode()) != 0) first = cal.getTime();
        			break;
        		case Calendar.SUNDAY:
        			if ((time.getDayCode() & DayCode.SUN.getCode()) != 0) first = cal.getTime();
        			break;
        		}
        	}
    		if (first == null) {
        		cal.add(Calendar.DAY_OF_YEAR, 1); idx++;
    		}
    	}
    	if (first == null) return;
    	cal.add(Calendar.MINUTE, Constants.SLOT_LENGTH_MIN * time.getLength() - time.getBreakTime());
    	Date firstEnd = cal.getTime();
    	int fidx = idx;
    	
    	cal.setTime(dpFirstDate);
    	idx = time.getWeekCode().length() - 1;
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	cal.set(Calendar.HOUR_OF_DAY, Constants.toHour(time.getStartSlot()));
    	cal.set(Calendar.MINUTE, Constants.toMinute(time.getStartSlot()));
    	cal.set(Calendar.SECOND, 0);
    	cal.add(Calendar.MINUTE, Constants.SLOT_LENGTH_MIN * time.getLength() - time.getBreakTime());
    	Date last = null;
    	while (idx >= 0 && last == null) {
    		if (time.getWeekCode().get(idx)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		switch (dow) {
        		case Calendar.MONDAY:
        			if ((time.getDayCode() & DayCode.MON.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.TUESDAY:
        			if ((time.getDayCode() & DayCode.TUE.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.WEDNESDAY:
        			if ((time.getDayCode() & DayCode.WED.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.THURSDAY:
        			if ((time.getDayCode() & DayCode.THU.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.FRIDAY:
        			if ((time.getDayCode() & DayCode.FRI.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.SATURDAY:
        			if ((time.getDayCode() & DayCode.SAT.getCode()) != 0) last = cal.getTime();
        			break;
        		case Calendar.SUNDAY:
        			if ((time.getDayCode() & DayCode.SUN.getCode()) != 0) last = cal.getTime();
        			break;
        		}
        	}
    		if (last == null) {
        		cal.add(Calendar.DAY_OF_YEAR, -1); idx--;    			
    		}
    	}
    	if (last == null) return;
    	
    	cal.setTime(dpFirstDate);
    	idx = fidx;
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	cal.set(Calendar.HOUR_OF_DAY, Constants.toHour(time.getStartSlot()));
    	cal.set(Calendar.MINUTE, Constants.toMinute(time.getStartSlot()));
    	cal.set(Calendar.SECOND, 0);

    	VEvent vevent = new VEvent();
    	DateStart dstart = new DateStart(first, true);
    	vevent.setDateStart(dstart);
    	DateEnd dend = new DateEnd(firstEnd, true);
    	vevent.setDateEnd(dend);
    	
    	Recurrence.Builder recur = new Recurrence.Builder(Frequency.WEEKLY);
    	for (Iterator<DayCode> i = DayCode.toDayCodes(time.getDayCode()).iterator(); i.hasNext(); ) {
        	switch (i.next()) {
        	case MON:
        		recur.byDay(DayOfWeek.MONDAY); break;
        	case TUE:
        		recur.byDay(DayOfWeek.TUESDAY); break;
        	case WED:
        		recur.byDay(DayOfWeek.WEDNESDAY); break;
        	case THU:
        		recur.byDay(DayOfWeek.THURSDAY); break;
        	case FRI:
        		recur.byDay(DayOfWeek.FRIDAY); break;
        	case SAT:
        		recur.byDay(DayOfWeek.SATURDAY); break;
        	case SUN:
        		recur.byDay(DayOfWeek.SUNDAY); break;
        	}
        }
        recur.workweekStarts(DayOfWeek.MONDAY).until(last);
        vevent.setRecurrenceRule(recur.build());

        ExceptionDates exdates = new ExceptionDates();
    	while (idx < time.getWeekCode().length()) {
    		int dow = cal.get(Calendar.DAY_OF_WEEK);
    		boolean offered = false;
    		switch (dow) {
    		case Calendar.MONDAY:
    			if ((time.getDayCode() & DayCode.MON.getCode()) != 0) offered = true;
    			break;
    		case Calendar.TUESDAY:
    			if ((time.getDayCode() & DayCode.TUE.getCode()) != 0) offered = true;
    			break;
    		case Calendar.WEDNESDAY:
    			if ((time.getDayCode() & DayCode.WED.getCode()) != 0) offered = true;
    			break;
    		case Calendar.THURSDAY:
    			if ((time.getDayCode() & DayCode.THU.getCode()) != 0) offered = true;
    			break;
    		case Calendar.FRIDAY:
    			if ((time.getDayCode() & DayCode.FRI.getCode()) != 0) offered = true;
    			break;
    		case Calendar.SATURDAY:
    			if ((time.getDayCode() & DayCode.SAT.getCode()) != 0) offered = true;
    			break;
    		case Calendar.SUNDAY:
    			if ((time.getDayCode() & DayCode.SUN.getCode()) != 0) offered = true;
    			break;
    		}
    		if (!offered) {
        		cal.add(Calendar.DAY_OF_YEAR, 1); idx++;
        		continue;
    		}
        	cal.set(Calendar.HOUR_OF_DAY, Constants.toHour(time.getStartSlot()));
        	cal.set(Calendar.MINUTE, Constants.toMinute(time.getStartSlot()));
        	cal.set(Calendar.SECOND, 0);
    		if (!time.getWeekCode().get(idx)) {
    			exdates.addValue(cal.getTime());
    		}
    		cal.add(Calendar.DAY_OF_YEAR, 1); idx++;
    	}
    	if (!exdates.getValues().isEmpty())
        	vevent.addExceptionDates(exdates);

    	vevent.setUid(clazz.getUniqueId().toString());
    	vevent.setSequence(0);
    	vevent.setSummary(clazz.getClassLabel(course));
        String desc = (course.getTitle() == null ? "" : course.getTitle());
        if (course.getConsentType() != null)
        	desc += " (" + course.getConsentType().getLabel() + ")";
		vevent.setDescription(desc);
		if (!assignment.getRooms().isEmpty()) {
			String loc = "";
        	for (Location r: assignment.getRooms()) {
        		if (!loc.isEmpty()) loc += ", ";
        		loc += r.getLabel();
        	}
        	vevent.setLocation(loc);
		}
        try {
        	URL url = CourseDetailsBackend.getCourseUrl(new AcademicSessionInfo(course.getInstructionalOffering().getSession()), course.getSubjectAreaAbbv(), course.getCourseNbr());
        	if (url != null)
        		vevent.setUrl(url.toString());
        } catch (Exception e) {
        	e.printStackTrace();
        }
        if (clazz.isDisplayInstructor()) {
            for (ClassInstructor instructor: clazz.getClassInstructors()) {
				if (vevent.getOrganizer() == null) {
					Organizer organizer = new Organizer(instructor.getInstructor().getNameLastFirst(), (instructor.getInstructor().getEmail() != null ? instructor.getInstructor().getEmail() : ""));
					vevent.setOrganizer(organizer);
				} else {
					Attendee attendee = new Attendee(instructor.getInstructor().getNameLastFirst(), (instructor.getInstructor().getEmail() != null ? instructor.getInstructor().getEmail() : ""));
					attendee.setRole(Role.CHAIR);
					vevent.addAttendee(attendee);
				}
            }
        }
        vevent.setStatus(Status.confirmed());
        ical.addEvent(vevent);
	}
	
	private static void printFreeTime(Date dpFirstDate, BitSet weekCode, String days, int start, int len, ICalendar ical) throws IOException {
		Calendar cal = Calendar.getInstance(Locale.US); cal.setLenient(true);
    	cal.setTime(dpFirstDate);

    	int idx = weekCode.nextSetBit(0);
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	cal.set(Calendar.HOUR_OF_DAY, Constants.toHour(start));
    	cal.set(Calendar.MINUTE, Constants.toMinute(start));
    	cal.set(Calendar.SECOND, 0);
    	Date first = null;
    	while (idx < weekCode.size() && first == null) {
    		if (weekCode.get(idx)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		switch (dow) {
        		case Calendar.MONDAY:
        			if (days.contains(DayCode.MON.getAbbv())) first = cal.getTime();
        			break;
        		case Calendar.TUESDAY:
        			if (days.contains(DayCode.TUE.getAbbv())) first = cal.getTime();
        			break;
        		case Calendar.WEDNESDAY:
        			if (days.contains(DayCode.WED.getAbbv())) first = cal.getTime();
        			break;
        		case Calendar.THURSDAY:
        			if (days.contains(DayCode.THU.getAbbv())) first = cal.getTime();
        			break;
        		case Calendar.FRIDAY:
        			if (days.contains(DayCode.FRI.getAbbv())) first = cal.getTime();
        			break;
        		case Calendar.SATURDAY:
        			if (days.contains(DayCode.SAT.getAbbv())) first = cal.getTime();
        			break;
        		case Calendar.SUNDAY:
        			if (days.contains(DayCode.SUN.getAbbv())) first = cal.getTime();
        			break;
        		}
        	}
    		cal.add(Calendar.DAY_OF_YEAR, 1); idx++;
    	}
    	if (first == null) return;
    	
    	cal.setTime(dpFirstDate);
    	idx = weekCode.length() - 1;
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	cal.set(Calendar.HOUR_OF_DAY, Constants.toHour(start));
    	cal.set(Calendar.MINUTE, Constants.toMinute(start));
    	cal.set(Calendar.SECOND, 0);
    	cal.add(Calendar.MINUTE, Constants.SLOT_LENGTH_MIN * len);
    	Date last = null;
    	while (idx >= 0 && last == null) {
    		if (weekCode.get(idx)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		switch (dow) {
        		case Calendar.MONDAY:
        			if (days.contains(DayCode.MON.getAbbv())) last = cal.getTime();
        			break;
        		case Calendar.TUESDAY:
        			if (days.contains(DayCode.TUE.getAbbv())) last = cal.getTime();
        			break;
        		case Calendar.WEDNESDAY:
        			if (days.contains(DayCode.WED.getAbbv())) last = cal.getTime();
        			break;
        		case Calendar.THURSDAY:
        			if (days.contains(DayCode.THU.getAbbv())) last = cal.getTime();
        			break;
        		case Calendar.FRIDAY:
        			if (days.contains(DayCode.FRI.getAbbv())) last = cal.getTime();
        			break;
        		case Calendar.SATURDAY:
        			if (days.contains(DayCode.SAT.getAbbv())) last = cal.getTime();
        			break;
        		case Calendar.SUNDAY:
        			if (days.contains(DayCode.SUN.getAbbv())) last = cal.getTime();
        			break;
        		}
        	}
    		cal.add(Calendar.DAY_OF_YEAR, -1); idx--;
    	}
    	if (last == null) return;
    	
    	VFreeBusy vfree = new VFreeBusy();
    	DateStart dstart = new DateStart(first, true);
    	vfree.setDateStart(dstart);
    	Calendar c = Calendar.getInstance(Locale.US); c.setTime(first); c.add(Calendar.MINUTE, Constants.SLOT_LENGTH_MIN * len);
    	DateEnd dend = new DateEnd(c.getTime(), true);
    	vfree.setDateEnd(dend);
    	vfree.addComment("Free Time");
    	ical.addFreeBusy(vfree);

    	cal.setTime(dpFirstDate);
    	idx = weekCode.nextSetBit(0);
    	cal.add(Calendar.DAY_OF_YEAR, idx);
    	while (idx < weekCode.length()) {
    		if (weekCode.get(idx)) {
        		int dow = cal.get(Calendar.DAY_OF_WEEK);
        		boolean offered = false;
        		switch (dow) {
        		case Calendar.MONDAY:
        			if (days.contains(DayCode.MON.getAbbv())) offered = true;
        			break;
        		case Calendar.TUESDAY:
        			if (days.contains(DayCode.TUE.getAbbv())) offered = true;
        			break;
        		case Calendar.WEDNESDAY:
        			if (days.contains(DayCode.WED.getAbbv())) offered = true;
        			break;
        		case Calendar.THURSDAY:
        			if (days.contains(DayCode.THU.getAbbv())) offered = true;
        			break;
        		case Calendar.FRIDAY:
        			if (days.contains(DayCode.FRI.getAbbv())) offered = true;
        			break;
        		case Calendar.SATURDAY:
        			if (days.contains(DayCode.SAT.getAbbv())) offered = true;
        			break;
        		case Calendar.SUNDAY:
        			if (days.contains(DayCode.SUN.getAbbv())) offered = true;
        			break;
        		}
        		if (offered) {
        	    	cal.set(Calendar.HOUR_OF_DAY, Constants.toHour(start));
        	    	cal.set(Calendar.MINUTE, Constants.toMinute(start));
        	    	cal.set(Calendar.SECOND, 0);
        	    	
        	    	vfree = new VFreeBusy();
        	    	dstart = new DateStart(cal.getTime(), true);
        	    	vfree.setDateStart(dstart);
        	    	cal.add(Calendar.MINUTE, Constants.SLOT_LENGTH_MIN * len);
        	    	dend = new DateEnd(cal.getTime(), true);
        	    	vfree.setDateEnd(dend);
        	    	vfree.addComment("Free Time");
        	    	ical.addFreeBusy(vfree);
        		}
    		}
    		cal.add(Calendar.DAY_OF_YEAR, 1); idx++;
    	}
	}

	public static interface Params {
		public String getParameter(String name);
		public String[] getParameterValues(String name);
		public Enumeration<String> getParameterNames();
	}

	public static class HttpParams implements Params {
		private Map<String, String[]> iParams = new HashMap<String, String[]>();
		
		HttpParams(HttpServletRequest request) {
			for (Enumeration e = request.getParameterNames(); e.hasMoreElements(); ) {
				String name = (String)e.nextElement();
				iParams.put(name, request.getParameterValues(name));
			}
		}

		@Override
		public String getParameter(String name) {
			String[] values = iParams.get(name);
			return (values == null || values.length <= 0 ? null : values[0]);
		}

		@Override
		public String[] getParameterValues(String name) {
			return iParams.get(name);
		}
		
		@Override
		public Enumeration<String> getParameterNames() {
			final Iterator<String> iterator = iParams.keySet().iterator();
			return new Enumeration<String>() {
				@Override
				public boolean hasMoreElements() {
					return iterator.hasNext();
				}
				@Override
				public String nextElement() {
					return iterator.next();
				}
			};
		}
	}
	
	public static class QParams implements Params {
		private Map<String, List<String>> iParams = new HashMap<String, List<String>>();
		
		QParams(String q) throws UnsupportedEncodingException {
			for (String p: QueryEncoderBackend.decode(q).split("&")) {
				String name = p.substring(0, p.indexOf('='));
				String value = URLDecoder.decode(p.substring(p.indexOf('=') + 1), "UTF-8");
				List<String> values = iParams.get(name);
				if (values == null) {
					values = new ArrayList<String>();
					iParams.put(name, values);
				}
				values.add(value);
			}
		}
		@Override
		public String getParameter(String name) {
			List<String> values = iParams.get(name);
			return (values == null || values.isEmpty() ? null : values.get(0));
		}
		@Override
		public String[] getParameterValues(String name) {
			List<String> values = iParams.get(name);
			if (values == null) return null;
			String[] ret = new String[values.size()];
			values.toArray(ret);
			return ret;
		}
		@Override
		public Enumeration<String> getParameterNames() {
			final Iterator<String> iterator = iParams.keySet().iterator();
			return new Enumeration<String>() {
				@Override
				public boolean hasMoreElements() {
					return iterator.hasNext();
				}
				@Override
				public String nextElement() {
					return iterator.next();
				}
			};
		}
		
	}
	
	public class ICalendarMeeting implements Comparable<ICalendarMeeting>{
		private DateTime iStart, iEnd;
		private String iLocation;
		private Status iStatus;
		
		public ICalendarMeeting(Meeting meeting) {
			iStart = new DateTime(meeting.getStartTime());
			iEnd = new DateTime(meeting.getStopTime());
			iLocation = (meeting.getLocation() == null ? "" : meeting.getLocation().getLabel());
			iStatus = meeting.isApproved() ? Status.confirmed() : Status.tentative();
		}
		
		public DateTime getStart() { return iStart; }
		public DateStart getDateStart() {
			DateStart ds = new DateStart(iStart.toDate(), true);
			return ds;
		}
		
		public DateTime getEnd() { return iEnd; }
		public DateEnd getDateEnd() {
			DateEnd de = new DateEnd(iEnd.toDate(), true);
			return de;
		}

		public String getLocation() { return iLocation; }
		public Status getStatus() { return iStatus; }
		
		public boolean merge(ICalendarMeeting m) {
			if (m.getStart().equals(getStart()) && m.getEnd().equals(getEnd())) {
				if (m.getStatus() == Status.tentative()) iStatus = Status.tentative();
				iLocation += ", " + m.getLocation();
				return true;
			}
			return false;
		}
		
		public boolean same(ICalendarMeeting m) {
			return m.getStart().getSecondOfDay() == getStart().getSecondOfDay() && m.getEnd().getSecondOfDay() == getEnd().getSecondOfDay() &&
					getLocation().equals(m.getLocation()) && getStatus().equals(m.getStatus());
		}
		
		public int compareTo(ICalendarMeeting m) {
			int cmp = getStart().compareTo(m.getStart());
			if (cmp != 0) return cmp;
			return getEnd().compareTo(m.getEnd());
		}
		
	}

}
