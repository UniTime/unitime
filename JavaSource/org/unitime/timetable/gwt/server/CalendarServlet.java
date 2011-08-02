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
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
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
import java.util.TimeZone;
import java.util.TreeSet;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.action.PersonalizedExamReportAction;
import org.unitime.timetable.gwt.shared.EventInterface;
import org.unitime.timetable.gwt.shared.EventInterface.MeetingInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceInterface;
import org.unitime.timetable.gwt.shared.EventInterface.ResourceType;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.Event;
import org.unitime.timetable.model.Exam;
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
import org.unitime.timetable.model.dao.LocationDAO;
import org.unitime.timetable.model.dao.MeetingDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.onlinesectioning.CourseInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningService;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.DateUtils;

import net.sf.cpsolver.coursett.model.RoomLocation;
import net.sf.cpsolver.coursett.model.TimeLocation;
import net.sf.cpsolver.studentsct.model.Enrollment;
import net.sf.cpsolver.studentsct.model.FreeTimeRequest;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;

/**
 * @author Tomas Muller
 */
public class CalendarServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger sLog = Logger.getLogger(CalendarServlet.class);
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String q = request.getParameter("q");
		HashMap<String, String> params = new HashMap<String, String>();
		if (q != null) {
			sLog.info(decode(q));
			for (String p: decode(q).split("&")) {
				params.put(p.substring(0, p.indexOf('=')), p.substring(p.indexOf('=') + 1));
			}
		} else {
			for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements(); ) {
				String name = e.nextElement();
				params.put(name, request.getParameter(name));
			}
		}
		Long sessionId = null;
		if (params.get("sid") != null) {
			sessionId = Long.valueOf(params.get("sid"));
		} else {
			User user = Web.getUser(request.getSession());
			if (user != null)
				sessionId = (Long)user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
			else
				sessionId = (Long)request.getSession().getAttribute("sessionId");
		}
		if (params.get("term") != null) {
			org.hibernate.Session hibSession = CurriculumDAO.getInstance().getSession();
			try {
				List<Long> sessions = hibSession.createQuery("select s.uniqueId from Session s where " +
						"s.academicTerm || s.academicYear = :term or " +
						"s.academicTerm || s.academicYear || s.academicInitiative = :term").
						setString("term", params.get("term")).list();
				if (!sessions.isEmpty())
					sessionId = sessions.get(0);
			} finally {
				hibSession.close();
			}
		}
		if (sessionId == null)
			throw new ServletException("No academic session provided.");
		OnlineSectioningServer server = OnlineSectioningService.getInstance(sessionId);
    	String classIds = params.get("cid");
    	String fts = params.get("ft");
    	String examIds = params.get("xid");
    	String eventIds = params.get("eid");
    	String meetingIds = params.get("mid");
    	String userId = params.get("uid");
    	if (q == null) userId = decode(userId);
    	String type = params.get("type");
    	String id = params.get("id");
   
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
            out.println("PRODID:-//UniTime " + Constants.VERSION + "." + Constants.BLD_NUMBER.replaceAll("@build.number@", "?") + "/Schedule Calendar//NONSGML v1.0//EN");
            if (classIds != null && !classIds.isEmpty()) {
            	for (String classId: classIds.split(",")) {
            		if (classId.isEmpty()) continue;
            		String[] courseAndClassId = classId.split("-");
            		if (courseAndClassId.length != 2) continue;
            		try {
            			if (server == null) {
            				CourseOffering course = CourseOfferingDAO.getInstance().get(Long.valueOf(courseAndClassId[0]), hibSession);
            				Class_ clazz = Class_DAO.getInstance().get(Long.valueOf(courseAndClassId[1]), hibSession);
            				if (course == null || clazz == null) continue;
                    		printClass(course, clazz, out);
            			} else {
                    		CourseInfo course = server.getCourseInfo(Long.valueOf(courseAndClassId[0]));
                    		Section section = server.getSection(Long.valueOf(courseAndClassId[1]));
                    		if (course == null || section == null) continue;
                    		printSection(server, course, section, out);
            			}
            		} catch (NumberFormatException e) {}
            	}
            }
        	if (fts != null && !fts.isEmpty()) {
        		Date dpFirstDate = null;
        		BitSet weekCode = null;
        		if (server == null) {
        			Session session = SessionDAO.getInstance().get(sessionId, hibSession);
        			dpFirstDate = DateUtils.getDate(1, session.getPatternStartMonth(), session.getSessionStartYear());
        			weekCode = session.getDefaultDatePattern().getPatternBitSet();
        		} else {
        			dpFirstDate = server.getAcademicSession().getDatePatternFirstDate();
        			weekCode = server.getAcademicSession().getFreeTimePattern();
        		}
        		for (String ft: fts.split(",")) {
        			if (ft.isEmpty()) continue;
        			String[] daysStartLen = ft.split("-");
        			if (daysStartLen.length != 3) continue;
        			printFreeTime(dpFirstDate, weekCode, daysStartLen[0], Integer.parseInt(daysStartLen[1]), Integer.parseInt(daysStartLen[2]), out);
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
                    	for (Exam exam: instructor.getExams(Exam.sExamTypeMidterm)) {
                    		printExam(exam, out);
                    	}
                    if (instructor.getDepartment().getSession().getStatusType().canNoRoleReportExamFinal())
                    	for (Exam exam: instructor.getExams(Exam.sExamTypeFinal)) {
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
                		for (Exam exam: student.getExams(Exam.sExamTypeFinal))
                			printExam(exam, out);
                	}
                	if (student.getSession().getStatusType().canNoRoleReportExamMidterm()) {
                		for (Exam exam: student.getExams(Exam.sExamTypeMidterm))
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
            if (type != null && id != null) {
            	ResourceInterface r = new ResourceInterface();
            	r.setSessionId(sessionId);
            	r.setId(Long.valueOf(id));
            	String ext = params.get("ext");
            	if (ext != null)
            		r.setExternalId(ext);
            	r.setType(ResourceType.valueOf(type.toUpperCase()));
            	if (r.getType() == ResourceType.ROOM)
            		r.setName(LocationDAO.getInstance().get(r.getId(), hibSession).getLabel());
        		for (EventInterface e: new EventServlet().findEvents(r, false))
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
	
	public static String getCalendar(OnlineSectioningServer server, net.sf.cpsolver.studentsct.model.Student student) throws IOException {
		if (student == null) return null;
		StringWriter buffer = new StringWriter();
		PrintWriter out = new PrintWriter(buffer);
        out.println("BEGIN:VCALENDAR");
        out.println("VERSION:2.0");
        out.println("CALSCALE:GREGORIAN");
        out.println("METHOD:PUBLISH");
        out.println("X-WR-CALNAME:UniTime Schedule");
        out.println("X-WR-TIMEZONE:"+TimeZone.getDefault().getID());
        out.println("PRODID:-//UniTime " + Constants.VERSION + "." + Constants.BLD_NUMBER.replaceAll("@build.number@", "?") + "/Schedule Calendar//NONSGML v1.0//EN");
		for (Request request: student.getRequests()) {
			Enrollment enrollment = request.getAssignment();
			if (enrollment == null) continue;
			if (enrollment.isCourseRequest()) {
				CourseInfo course = server.getCourseInfo(enrollment.getCourse().getId());
				for (Section section: enrollment.getSections())
					printSection(server, course, section, out);
			} else {
				FreeTimeRequest ft = (FreeTimeRequest)request;
				printFreeTime(server.getAcademicSession().getDatePatternFirstDate(), server.getAcademicSession().getFreeTimePattern(), 
						DayCode.toString(ft.getTime().getDayCode()), ft.getTime().getStartSlot(), ft.getTime().getLength(), out);
			}
		}
	    out.println("END:VCALENDAR");
    	out.flush();
		out.close();
		return buffer.toString();		
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
            if (event.hasInstructor()) {
            	String[] instructor = event.getInstructor().split("\\|");
            	String[] email = event.getEmail().split("\\|");
            	for (int i = 0; i < instructor.length; i++) {
            		out.println((i == 0 ? "ORGANIZER" : "ATTENDEE") + ";ROLE=CHAIR;CN=\"" + instructor[i].trim() + "\":MAILTO:" + ("-".equals(email[i]) ? "" : email[i]));
            	}
            } else if (event.hasSponsor()) {
            	out.println("ORGANIZER;ROLE=CHAIR;CN=\"" + event.getSponsor() + "\":MAILTO:" + (event.getEmail() == null ? "" : event.getEmail()));
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
        out.println("SUMMARY:"+exam.getLabel()+" ("+ApplicationProperties.getProperty("tmtbl.exam.name.type."+Exam.sExamTypes[exam.getExamType()],Exam.sExamTypes[exam.getExamType()])+" Exam)");
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
        if (course.getInstructionalOffering().getConsentType() != null)
        	desc += " (" + course.getInstructionalOffering().getConsentType().getLabel() + ")";
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
	
	private static void printSection(OnlineSectioningServer server, CourseInfo course, Section section, PrintWriter out) throws IOException {
		TimeLocation time = section.getTime();
		if (time == null || time.getWeekCode().isEmpty()) return;
		
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat tf = new SimpleDateFormat("HHmmss");
        tf.setTimeZone(TimeZone.getTimeZone("UTC"));

    	Calendar cal = Calendar.getInstance(Locale.US); cal.setLenient(true);
    	cal.setTime(server.getAcademicSession().getDatePatternFirstDate());
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
    	
    	cal.setTime(server.getAcademicSession().getDatePatternFirstDate());
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
    	
    	cal.setTime(server.getAcademicSession().getDatePatternFirstDate());
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
    	printMeetingRest(server, course, section, out);
        for (ArrayList<String> x: extra) {
            out.println("BEGIN:VEVENT");
            for (String s: x) out.println(s);
            printMeetingRest(server, course, section, out);
        }
	}
	
	@SuppressWarnings("unchecked")
	private static void printMeetingRest(OnlineSectioningServer server, CourseInfo course, Section section, PrintWriter out) throws IOException {
        out.println("UID:" + section.getId());
        out.println("SEQUENCE:0");
        out.println("SUMMARY:" + course.getSubjectArea() + " " + course.getCourseNbr() + " " +
        		section.getSubpart().getName() + " " + section.getName(course.getUniqueId()));
        String desc = (course.getTitle() == null ? "" : course.getTitle());
		if (course.getConsent() != null && !course.getConsent().isEmpty())
			desc += " (" + course.getConsent() + ")";
			out.println("DESCRIPTION:" + desc);
        if (section.getRooms() != null && !section.getRooms().isEmpty()) {
        	String loc = "";
        	for (RoomLocation r: section.getRooms()) {
        		if (!loc.isEmpty()) loc += ", ";
        		loc += r.getName();
        	}
        	out.println("LOCATION:" + loc);
        }
        try {
        	URL url = server.getSectionUrl(course.getUniqueId(), section);
        	if (url != null) out.println("URL:" + url.toString());
        } catch (Exception e) {
        	e.printStackTrace();
        }
        boolean org = false;
		if (section.getChoice().getInstructorNames() != null && !section.getChoice().getInstructorNames().isEmpty()) {
			String[] instructors = section.getChoice().getInstructorNames().split(":");
			for (String instructor: instructors) {
				String[] nameEmail = instructor.split("\\|");
				//out.println("CONTACT:" + nameEmail[0] + (nameEmail[1].isEmpty() ? "" : " <" + nameEmail[1]) + ">");
				if (!org) {
					out.println("ORGANIZER;ROLE=CHAIR;CN=\"" + nameEmail[0] + "\":MAILTO:" + ( nameEmail.length > 1 ? nameEmail[1] : ""));
					org = true;
				} else {
					out.println("ATTENDEE;ROLE=CHAIR;CN=\"" + nameEmail[0] + "\":MAILTO:" + ( nameEmail.length > 1 ? nameEmail[1] : ""));
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
	
	private static SecretKey secret() throws NoSuchAlgorithmException, InvalidKeySpecException {
		byte salt[] = new byte[] { (byte)0x33, (byte)0x7b, (byte)0x09, (byte)0x0e, (byte)0xcf, (byte)0x5a, (byte)0x58, (byte)0xd9 };
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		KeySpec spec = new PBEKeySpec(ApplicationProperties.getProperty("unitime.encode.secret", "ThisIs8Secret").toCharArray(), salt, 1024, 128);
		SecretKey key = factory.generateSecret(spec);
		return new SecretKeySpec(key.getEncoded(), "AES");
	}
	
	public static String encode(String text) {
		try {
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secret());
			return new BigInteger(cipher.doFinal(text.getBytes())).toString(36);
		} catch (Exception e) {
			sLog.warn("Encoding failed: " + e.getMessage());
			try {
				return URLEncoder.encode(text, "ISO-8859-1");
			} catch (UnsupportedEncodingException x) {
				return null;
			}
		}
	}
	
	public static String decode(String text) {
		try {
			if (text == null || text.isEmpty()) return null;
			Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secret());
			return new String(cipher.doFinal(new BigInteger(text, 36).toByteArray()));
		} catch (Exception e) {
			sLog.warn("Decoding failed: " + e.getMessage());
			try {
				return URLDecoder.decode(text, "ISO-8859-1");
			} catch (UnsupportedEncodingException x) {
				return null;
			}
		}
	}


}
