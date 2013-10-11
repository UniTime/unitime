/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.timetable.gwt.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.action.PersonalizedExamReportAction;
import org.unitime.timetable.events.EventLookupBackend;
import org.unitime.timetable.events.QueryEncoderBackend;
import org.unitime.timetable.events.EventAction.EventContext;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ContactInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventFilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.EventLookupRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.gwt.shared.EventInterface.RoomFilterRpcRequest;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Exam;
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
import org.unitime.timetable.model.dao.MeetingDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.updates.CalendarExport;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserAuthority;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.context.UniTimeUserContext;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;

import net.sf.cpsolver.coursett.model.TimeLocation;

/**
 * @author Tomas Muller
 */
public class CalendarServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	
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
		if (sessionId == null)
			throw new ServletException("No academic session provided.");
		OnlineSectioningServer server = solverServerService.getOnlineStudentSchedulingContainer().getSolver(sessionId.toString());
    	String classIds = params.getParameter("cid");
    	String fts = params.getParameter("ft");
    	String examIds = params.getParameter("xid");
    	String eventIds = params.getParameter("eid");
    	String meetingIds = params.getParameter("mid");
    	String userId = params.getParameter("uid");
    	if (q == null) userId = QueryEncoderBackend.decode(userId);
    	String type = params.getParameter("type");
   
		response.setContentType("text/calendar; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		response.setHeader( "Content-Disposition", "attachment; filename=\"schedule.ics\"" );
        
		PrintWriter out = response.getWriter();
		org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
		try {
            out.println("BEGIN:VCALENDAR");
            out.println("VERSION:2.0");
            out.println("CALSCALE:GREGORIAN");
            out.println("METHOD:PUBLISH");
            out.println("X-WR-CALNAME:UniTime Schedule");
            out.println("X-WR-TIMEZONE:"+TimeZone.getDefault().getID());
            out.println("PRODID:-//UniTime " + Constants.getVersion() + "/Schedule Calendar//NONSGML v1.0//EN");
            if (server != null) {
            	out.println(server.execute(new CalendarExport(classIds, fts), null));
            } else { 
            	if (classIds != null && !classIds.isEmpty()) {
            		for (String classId: classIds.split(",")) {
            			if (classId.isEmpty()) continue;
            			String[] courseAndClassId = classId.split("-");
            			if (courseAndClassId.length != 2) continue;
        				CourseOffering course = CourseOfferingDAO.getInstance().get(Long.valueOf(courseAndClassId[0]), hibSession);
        				Class_ clazz = Class_DAO.getInstance().get(Long.valueOf(courseAndClassId[1]), hibSession);
        				if (course == null || clazz == null) continue;
                		printClass(course, clazz, out);
            		}
            	}
            	if (fts != null && !fts.isEmpty()) {
        			Session session = SessionDAO.getInstance().get(sessionId, hibSession);
        			Date dpFirstDate = DateUtils.getDate(1, session.getPatternStartMonth(), session.getSessionStartYear());
        			BitSet weekCode = session.getDefaultDatePattern().getPatternBitSet();
	        		for (String ft: fts.split(",")) {
	        			if (ft.isEmpty()) continue;
	        			String[] daysStartLen = ft.split("-");
	        			if (daysStartLen.length != 3) continue;
	        			printFreeTime(dpFirstDate, weekCode, daysStartLen[0], Integer.parseInt(daysStartLen[1]), Integer.parseInt(daysStartLen[2]), out);
	        		}
            	}
            }
            if (examIds != null && !examIds.isEmpty()) {
            	for (String examId: examIds.split(",")) {
            		if (examId.isEmpty()) continue;
            		try {
                		Exam exam = ExamDAO.getInstance().get(Long.valueOf(examId), hibSession);
                		if (exam != null)
                			printExam(exam, out);
            		} catch (NumberFormatException e) {}
            	}
            }
            if (eventIds != null && !eventIds.isEmpty()) {
            	for (String eventId: eventIds.split(",")) {
            		if (eventId.isEmpty()) continue;
            		try {
            			Event event = EventDAO.getInstance().get(Long.valueOf(eventId), hibSession);
            			if (event != null)
            				printEvent(event, null, out);
            		} catch (NumberFormatException e) {}
            	}
            }
            if (meetingIds != null && !meetingIds.isEmpty()) {
            	Hashtable<Long, List<Meeting>> meetings = new Hashtable<Long, List<Meeting>>();
            	for (String meetingId: meetingIds.split(",")) {
            		if (meetingId.isEmpty()) continue;
            		try {
            			Meeting meeting = MeetingDAO.getInstance().get(Long.valueOf(meetingId), hibSession);
            			if (meeting != null) {
            				List<Meeting> m = meetings.get(meeting.getEvent().getUniqueId());
            				if (m == null) {
            					m = new ArrayList<Meeting>();
            					meetings.put(meeting.getEvent().getUniqueId(), m);
            				}
            				m.add(meeting);
            			}
            		} catch (NumberFormatException e) {}
            	}
            	for (List<Meeting> eventMeetings: meetings.values()) {
            		printEvent(eventMeetings.get(0).getEvent(), eventMeetings, out);
            	}
            }
            if (userId != null && !userId.isEmpty()) {
                for (DepartmentalInstructor instructor: (List<DepartmentalInstructor>)hibSession.createQuery("select i from DepartmentalInstructor i " +
                		"where i.externalUniqueId = :externalId and i.department.session.uniqueId = :sessionId").
                		setLong("sessionId", sessionId).setString("externalId", userId).list()) {
                	if (!PersonalizedExamReportAction.canDisplay(instructor.getDepartment().getSession())) continue;
                    if (instructor.getDepartment().getSession().getStatusType().canNoRoleReportExamMidterm())
                    	for (Exam exam: instructor.getExams(ExamType.sExamTypeMidterm)) {
                    		printExam(exam, out);
                    	}
                    if (instructor.getDepartment().getSession().getStatusType().canNoRoleReportExamFinal())
                    	for (Exam exam: instructor.getExams(ExamType.sExamTypeFinal)) {
                    		printExam(exam, out);
                    	}
                    if (instructor.getDepartment().getSession().getStatusType().canNoRoleReportClass()) {
                        for (ClassInstructor ci: instructor.getClasses()) {
                            printClass(ci.getClassInstructing().getSchedulingSubpart().getInstrOfferingConfig().getControllingCourseOffering(), ci.getClassInstructing(), out);
                        }
                    }
                }
                for (Student student: (List<Student>)hibSession.createQuery("select s from Student s where " +
                		"s.externalUniqueId=:externalId and s.session.uniqueId = :sessionId").
                		setLong("sessionId", sessionId).setString("externalId", userId).list()) {
                	if (!PersonalizedExamReportAction.canDisplay(student.getSession())) continue;
                	if (student.getSession().getStatusType().canNoRoleReportExamFinal()) {
                		for (Exam exam: student.getExams(ExamType.sExamTypeFinal))
                			printExam(exam, out);
                	}
                	if (student.getSession().getStatusType().canNoRoleReportExamMidterm()) {
                		for (Exam exam: student.getExams(ExamType.sExamTypeMidterm))
                			printExam(exam, out);
                	}
                    if (student.getSession().getStatusType().canNoRoleReportClass()) {
                        for (Iterator i=student.getClassEnrollments().iterator();i.hasNext();) {
                            StudentClassEnrollment sce = (StudentClassEnrollment)i.next();
                            printClass(sce.getCourseOffering(), sce.getClazz(), out);
                        }
                    }
                }
            }
            if (type != null) {
            	EventLookupRpcRequest r = new EventLookupRpcRequest();
            	r.setSessionId(sessionId);
            	String id = params.getParameter("id");
            	if (id != null) r.setResourceId(Long.valueOf(id));
            	String ext = params.getParameter("ext");
            	if (ext != null)
            		r.setResourceExternalId(ext);
            	r.setResourceType(ResourceType.valueOf(type.toUpperCase()));
            	EventFilterRpcRequest eventFilter = new EventFilterRpcRequest();
            	eventFilter.setSessionId(sessionId);
            	r.setEventFilter(eventFilter);
            	RoomFilterRpcRequest roomFilter = new RoomFilterRpcRequest();
            	roomFilter.setSessionId(sessionId);
            	boolean hasRoomFilter = false;
            	for (Enumeration<String> e = params.getParameterNames(); e.hasMoreElements(); ) {
            		String command = e.nextElement();
            		if (command.equals("e:text")) {
            			eventFilter.setText(params.getParameter("e:text"));
            	} else if (command.startsWith("e:")) {
            			for (String value: params.getParameterValues(command))
            				eventFilter.addOption(command.substring(2), value);
            		} else if (command.equals("r:text")) {
            			hasRoomFilter = true;
            			roomFilter.setText(params.getParameter("r:text"));
            		} else if (command.startsWith("r:")) {
            			hasRoomFilter = true;
            			for (String value: params.getParameterValues(command))
            				roomFilter.addOption(command.substring(2), value);
            		}
            	}
            	if (hasRoomFilter)
            		r.setRoomFilter(roomFilter);
            	String user = params.getParameter("user");
            	UserContext u = sessionContext.getUser();
            	if (u == null && user != null) {
            		u = new UniTimeUserContext(user, null, null, null);
            		String role = params.getParameter("role");
            		if (role != null) {
            			for (UserAuthority a: u.getAuthorities()) {
            				if (a.getAcademicSession() != null && a.getAcademicSession().getQualifierId().equals(sessionId) && role.equals(a.getRole())) {
            					u.setCurrentAuthority(a); break;
            				}
            			}
            		}
            	}
            	for (EventInterface e: new EventLookupBackend().findEvents(r, new EventContext(sessionContext, u, sessionId)))
        			printEvent(e, out);
            }
            out.println("END:VCALENDAR");
        	out.flush();
        } finally {
        	if (hibSession.isOpen()) 
        		hibSession.close();
        	out.close();
        }
	}
	
	private void printEvent(EventInterface event, PrintWriter out) throws IOException {
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat tf = new SimpleDateFormat("HHmmss");
        tf.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        Hashtable<String, String> date2loc = new Hashtable<String, String>();
        Hashtable<String, Boolean> approved = new Hashtable<String, Boolean>();
        for (MeetingInterface m: event.getMeetings()) {
        	Date startTime = new Date(m.getStartTime());
        	Date stopTime = new Date(m.getStopTime());
            String date = df.format(startTime) + "T" + tf.format(startTime) + "Z/" + df.format(stopTime) + "T" + tf.format(stopTime) + "Z";
            String loc = m.getLocationName();
            String l = date2loc.get(date);
            date2loc.put(date, (l == null || l.isEmpty() ? "" : l + ", ") + loc);
            Boolean a = approved.get(date);
            approved.put(date, (a == null || a) && m.isApproved());
        }
        
        String firstDate = null;
        for (String date : new TreeSet<String>(date2loc.keySet())) {
        	String loc = date2loc.get(date);
        	String start = date.substring(0, date.indexOf('/'));
        	String end = date.substring(date.indexOf('/') + 1);
            out.println("BEGIN:VEVENT");
            out.println("SEQUENCE:0");
            out.println("UID:"+event.getId());
            out.println("SUMMARY:"+event.getName());
            out.println("DESCRIPTION:"+(event.getInstruction() != null ? event.getInstruction() : event.getType()));
            out.println("DTSTART:" + start);
            out.println("DTEND:" + end);
            if (firstDate == null) {
            	firstDate = date;
            	String rdate = "";
                for (String d : new TreeSet<String>(date2loc.keySet())) {
                	if (d.equals(date)) continue;
                	if (!rdate.isEmpty()) rdate += ",";
                	rdate += d;
            	}
            	if (!rdate.isEmpty())
            		out.println("RDATE;VALUE=PERIOD:" + rdate);
            } else {
    	        out.println("RECURRENCE-ID:" + start);
            }
            out.println("LOCATION:" + loc);
            out.println("STATUS:" + (approved.get(date) ? "CONFIRMED" : "TENTATIVE"));
            if (event.hasInstructors()) {
            	int idx = 0;
            	for (ContactInterface instructor: event.getInstructors()) {
            		out.println((idx++ == 0 ? "ORGANIZER" : "ATTENDEE") + ";ROLE=CHAIR;CN=\"" + instructor.getName(MESSAGES) + "\":MAILTO:" + (instructor.hasEmail() ? instructor.getEmail() : ""));
            	}
            } else if (event.hasSponsor()) {
            	out.println("ORGANIZER;ROLE=CHAIR;CN=\"" + event.getSponsor().getName() + "\":MAILTO:" + (event.getSponsor().hasEmail() ? event.getSponsor().getEmail() : ""));
            }
            out.println("END:VEVENT");	
        }
	}
	
	private void printEvent(Event event, Collection<Meeting> meetings, PrintWriter out) throws IOException {
		if (meetings == null)
			meetings = event.getMeetings();
		
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat tf = new SimpleDateFormat("HHmmss");
        tf.setTimeZone(TimeZone.getTimeZone("UTC"));
        
        Hashtable<String, String> date2loc = new Hashtable<String, String>();
        Hashtable<String, Boolean> approved = new Hashtable<String, Boolean>();
        for (Meeting m: meetings) {
            String date = df.format(m.getStartTime()) + "T" + tf.format(m.getStartTime()) + "Z/" + df.format(m.getStopTime()) + "T" + tf.format(m.getStopTime()) + "Z";
            String loc = (m.getLocation() == null ? "" : m.getLocation().getLabel());
            String l = date2loc.get(date);
            date2loc.put(date, (l == null || l.isEmpty() ? "" : l + ", ") + loc);
            Boolean a = approved.get(date);
            approved.put(date, (a == null || a) && m.isApproved());
        }
        
        String firstDate = null;
        for (String date : new TreeSet<String>(date2loc.keySet())) {
        	String loc = date2loc.get(date);
        	String start = date.substring(0, date.indexOf('/'));
        	String end = date.substring(date.indexOf('/') + 1);
            out.println("BEGIN:VEVENT");
            out.println("SEQUENCE:0");
            out.println("UID:"+event.getUniqueId());
            out.println("SUMMARY:"+event.getEventName());
            out.println("DESCRIPTION:"+event.getEventTypeLabel());
            out.println("DTSTART:" + start);
            out.println("DTEND:" + end);
            if (firstDate == null) {
            	firstDate = date;
            	String rdate = "";
                for (String d : new TreeSet<String>(date2loc.keySet())) {
                	if (d.equals(date)) continue;
                	if (!rdate.isEmpty()) rdate += ",";
                	rdate += d;
            	}
            	if (!rdate.isEmpty())
            		out.println("RDATE;VALUE=PERIOD:" + rdate);
            } else {
    	        out.println("RECURRENCE-ID:" + start);
            }
            if (event.getSponsoringOrganization() != null) {
            	out.println("ORGANIZER;ROLE=CHAIR;CN=\"" + event.getSponsoringOrganization().getName() + "\":MAILTO:" +
            			(event.getSponsoringOrganization().getEmail() == null ? "" : event.getSponsoringOrganization().getEmail()));
            }
            out.println("LOCATION:" + loc);
            out.println("STATUS:" + (approved.get(date) ? "CONFIRMED" : "TENTATIVE"));
            out.println("END:VEVENT");	
        }
	}
	
	private void printExam(Exam exam, PrintWriter out) throws IOException {
		if (exam.getAssignedPeriod() == null) return;
		
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat tf = new SimpleDateFormat("HHmmss");
        tf.setTimeZone(TimeZone.getTimeZone("UTC"));

        out.println("BEGIN:VEVENT");
        out.println("SEQUENCE:0");
        out.println("UID:"+exam.getUniqueId());
        out.println("DTSTART:"+df.format(exam.getAssignedPeriod().getStartTime())+"T"+tf.format(exam.getAssignedPeriod().getStartTime())+"Z");
        Calendar endTime = Calendar.getInstance(); endTime.setTime(exam.getAssignedPeriod().getStartTime());
        endTime.add(Calendar.MINUTE, exam.getLength());
        out.println("DTEND:"+df.format(endTime.getTime())+"T"+tf.format(endTime.getTime())+"Z");
        out.println("SUMMARY:"+exam.getLabel()+" ("+exam.getExamType().getLabel()+" Exam)");
        if (!exam.getAssignedRooms().isEmpty()) {
            String rooms = "";
            for (Iterator i=new TreeSet(exam.getAssignedRooms()).iterator();i.hasNext();) {
                Location location = (Location)i.next();
                if (rooms.length()>0) rooms+=", ";
                rooms+=location.getLabel();
            }
            out.println("LOCATION:"+rooms);
        }
		out.println("STATUS:CONFIRMED");	
        out.println("END:VEVENT");      
	}

	private void printClass(CourseOffering course, Class_ clazz, PrintWriter out) throws IOException {
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

        out.println("BEGIN:VEVENT");
        out.println("DTSTART:" + df.format(first) + "T" + tf.format(first) + "Z");
        out.println("DTEND:" + df.format(firstEnd) + "T" + tf.format(firstEnd) + "Z");
        out.print("RRULE:FREQ=WEEKLY;BYDAY=");
        for (Iterator<DayCode> i = DayCode.toDayCodes(time.getDayCode()).iterator(); i.hasNext(); ) {
        	out.print(i.next().getName().substring(0, 2).toUpperCase());
        	if (i.hasNext()) out.print(",");
        }
        out.println(";WKST=MO;UNTIL=" + df.format(last) + "T" + tf.format(last) + "Z");
        ArrayList<ArrayList<String>> extra = new ArrayList<ArrayList<String>>();
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
    		if (time.getWeekCode().get(idx)) {
    			if (!tf.format(first).equals(tf.format(cal.getTime()))) {
    				ArrayList<String> x = new ArrayList<String>(); extra.add(x);
    		        x.add("RECURRENCE-ID:" + df.format(cal.getTime()) + "T" + tf.format(first) + "Z");
    		        x.add("DTSTART:" + df.format(cal.getTime()) + "T" + tf.format(cal.getTime()) + "Z");
    		    	cal.add(Calendar.MINUTE, Constants.SLOT_LENGTH_MIN * time.getLength() - time.getBreakTime());
    		        x.add("DTEND:" + df.format(cal.getTime()) + "T" + tf.format(cal.getTime()) + "Z");
    			}
    		} else {
    			out.println("EXDATE:" + df.format(cal.getTime()) + "T" + tf.format(first) + "Z");
    		}
    		cal.add(Calendar.DAY_OF_YEAR, 1); idx++;
    	}
    	printClassRest(course, clazz, assignment, out);
        for (ArrayList<String> x: extra) {
            out.println("BEGIN:VEVENT");
            for (String s: x) out.println(s);
        	printClassRest(course, clazz, assignment, out);
        }
	}
	
	private void printClassRest(CourseOffering course, Class_ clazz, Assignment assignment, PrintWriter out) throws IOException {
        out.println("UID:" + clazz.getUniqueId());
        out.println("SEQUENCE:0");
        out.println("SUMMARY:" + clazz.getClassLabel(course));
        String desc = (course.getTitle() == null ? "" : course.getTitle());
        if (course.getConsentType() != null)
        	desc += " (" + course.getConsentType().getLabel() + ")";
			out.println("DESCRIPTION:" + desc);
		if (!assignment.getRooms().isEmpty()) {
			String loc = "";
        	for (Location r: assignment.getRooms()) {
        		if (!loc.isEmpty()) loc += ", ";
        		loc += r.getLabel();
        	}
        	out.println("LOCATION:" + loc);
		}
        if (clazz.isDisplayInstructor()) {
        	boolean org = false;
            for (ClassInstructor instructor: clazz.getClassInstructors()) {
				if (!org) {
					out.println("ORGANIZER;ROLE=CHAIR;CN=\"" + instructor.getInstructor().getNameLastFirst() + "\":MAILTO:" + ( instructor.getInstructor().getEmail() != null ? instructor.getInstructor().getEmail() : ""));
					org = true;
				} else {
					out.println("ATTENDEE;ROLE=CHAIR;CN=\"" + instructor.getInstructor().getNameLastFirst() + "\":MAILTO:" + ( instructor.getInstructor().getEmail() != null ? instructor.getInstructor().getEmail() : ""));
				}
    		}
        }
		out.println("STATUS:CONFIRMED");	
        out.println("END:VEVENT");
	}
	
	private static void printFreeTime(Date dpFirstDate, BitSet weekCode, String days, int start, int len, PrintWriter out) throws IOException {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat tf = new SimpleDateFormat("HHmmss");
        tf.setTimeZone(TimeZone.getTimeZone("UTC"));

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
    	
    	out.println("BEGIN:VFREEBUSY");
        out.println("DTSTART:" + df.format(first) + "T" + tf.format(first) + "Z");
    	cal.add(Calendar.MINUTE, Constants.SLOT_LENGTH_MIN * len);
        out.println("DTEND:" + df.format(last) + "T" + tf.format(last) + "Z");
        out.println("COMMENT:Free Time");

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
                    out.print("FREEBUSY:" + df.format(cal.getTime()) + "T" + tf.format(cal.getTime()) + "Z");
                	cal.add(Calendar.MINUTE, Constants.SLOT_LENGTH_MIN * len);
                    out.println("/" + df.format(cal.getTime()) + "T" + tf.format(cal.getTime()) + "Z");
        		}
    		}
    		cal.add(Calendar.DAY_OF_YEAR, 1); idx++;
    	}
    	
        out.println("END:VFREEBUSY");
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

}
