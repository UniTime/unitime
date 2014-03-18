/*
 * UniTime 3.2 - 3.5 (University Timetabling Application)
 * Copyright (C) 2011 - 2013, UniTime LLC, and individual contributors
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
package org.unitime.timetable.onlinesectioning;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.cpsolver.coursett.Constants;
import org.cpsolver.coursett.model.RoomLocation;
import org.cpsolver.coursett.model.TimeLocation;
import org.cpsolver.studentsct.model.Course;
import org.cpsolver.studentsct.model.CourseRequest;
import org.cpsolver.studentsct.model.Enrollment;
import org.cpsolver.studentsct.model.FreeTimeRequest;
import org.cpsolver.studentsct.model.Request;
import org.cpsolver.studentsct.model.SctAssignment;
import org.cpsolver.studentsct.model.Section;
import org.cpsolver.studentsct.reservation.CourseReservation;
import org.cpsolver.studentsct.reservation.CurriculumReservation;
import org.cpsolver.studentsct.reservation.GroupReservation;
import org.cpsolver.studentsct.reservation.IndividualReservation;
import org.cpsolver.studentsct.reservation.Reservation;
import org.hibernate.CacheMode;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.onlinesectioning.model.XExactTimeConversion;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XFreeTimeRequest;
import org.unitime.timetable.onlinesectioning.model.XInstructor;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XReservationId;
import org.unitime.timetable.onlinesectioning.model.XRoom;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XTime;

/**
 * @author Tomas Muller
 */
public class OnlineSectioningHelper {
    protected static Log sLog = LogFactory.getLog(OnlineSectioningHelper.class);
	private static StudentSectioningConstants CFG = Localization.create(StudentSectioningConstants.class);
	public static boolean sTransactionCreatesNewHibSession = false;

    public static enum LogLevel {
    	DEBUG(OnlineSectioningLog.Message.Level.DEBUG),
    	INFO(OnlineSectioningLog.Message.Level.INFO),
    	WARN(OnlineSectioningLog.Message.Level.WARN),
    	ERROR(OnlineSectioningLog.Message.Level.ERROR),
    	FATAL(OnlineSectioningLog.Message.Level.FATAL);
    	
    	private OnlineSectioningLog.Message.Level iProtoLevel;
    	
    	LogLevel(OnlineSectioningLog.Message.Level level) { iProtoLevel = level; }
    	
    	OnlineSectioningLog.Message.Level level() { return iProtoLevel; }
    };
    protected List<MessageHandler> iMessageHandlers = new ArrayList<MessageHandler>();
    protected org.hibernate.Session iHibSession = null;
    protected org.hibernate.Transaction iTx = null;
    protected int iFlushIfNeededCounter = 0;
    protected OnlineSectioningLog.Log.Builder iLog = OnlineSectioningLog.Log.newBuilder();
    protected OnlineSectioningLog.Entity iUser = null;
    protected static int sBatchSize = 100;
    protected CacheMode iCacheMode = null;
    protected XExactTimeConversion iExactTimeConversion = null;
    
    public OnlineSectioningHelper(OnlineSectioningLog.Entity user, CacheMode cacheMode) {
    	this(null, user, cacheMode);
    }
    
    public OnlineSectioningHelper(OnlineSectioningLog.Entity user) {
    	this(null, user, null);
    }
    
    public OnlineSectioningHelper(org.hibernate.Session hibSession, OnlineSectioningLog.Entity user) {
    	this(hibSession, user, null);
    }
    
    public OnlineSectioningHelper(org.hibernate.Session hibSession, OnlineSectioningLog.Entity user, CacheMode cacheMode) {
    	iHibSession = hibSession;
    	iUser = user;
    	iCacheMode = cacheMode;
    }
    
    public OnlineSectioningLog.Entity getUser() { return iUser; }

    public void log(Message m) {
    	if (m.getLevel() != LogLevel.DEBUG) {
        	OnlineSectioningLog.Message.Builder l = OnlineSectioningLog.Message.newBuilder()
        		.setLevel(m.getLevel().level())
				.setText(m.getMessage())
				.setTimeStamp(System.currentTimeMillis());
        	if (m.getThrowable() != null)
        		l.setException(m.getThrowable().getClass().getName() + ": " + m.getThrowable().getMessage());
        	iLog.addMessage(l);
    	}
    	for (MessageHandler h: iMessageHandlers)
    		h.onMessage(m);
    }

    public void debug(String msg) {
        log(new Message(LogLevel.DEBUG, msg));
    }
    
    public void info(String msg) {
        log(new Message(LogLevel.INFO, msg));
    }
    
    public void warn(String msg) {
        log(new Message(LogLevel.WARN, msg));
    }
    
    public void error(String msg) {
        log(new Message(LogLevel.ERROR, msg));
    }
    
    public void fatal(String msg) {
        log(new Message(LogLevel.FATAL, msg));
    }
    
    public void debug(String msg, Throwable t) {
        log(new Message(LogLevel.DEBUG, msg, t));
    }
    
    public void info(String msg, Throwable t) {
        log(new Message(LogLevel.INFO, msg, t));
    }
    
    public void warn(String msg, Throwable t) {
        log(new Message(LogLevel.WARN, msg, t));
    }
    
    public void error(String msg, Throwable t) {
        log(new Message(LogLevel.ERROR, msg, t));
    }
    
    public void fatal(String msg, Throwable t) {
        log(new Message(LogLevel.FATAL, msg, t));
    }

    public org.hibernate.Session getHibSession() {
    	if (iHibSession == null) {
    		iHibSession = new _RootDAO().getSession();
    		if (iCacheMode != null) iHibSession.setCacheMode(iCacheMode);
    	}
        return iHibSession;
    }
    
    public boolean beginTransaction() {
        try {
        	if (iTx != null) return false;
        	
            iHibSession = (sTransactionCreatesNewHibSession ? new _RootDAO().createNewSession() : getHibSession());
            
            if (iCacheMode != null) {
            	debug("Using hibernate cache mode " + iCacheMode + ".");
                iHibSession.setCacheMode(iCacheMode);
            }
            
            iTx = iHibSession.beginTransaction();
            debug("Transaction started.");
            return true;
        } catch (Exception e) {
            fatal("Unable to begin transaction, reason: "+e.getMessage(),e);
            return false;
        }
    }
    
    public boolean commitTransaction() {
        try {
        	if (iTx == null) return false;
            iTx.commit();
            iTx = null;
            debug("Transaction committed.");
            return true;
        } catch (Exception e) {
            fatal("Unable to commit transaction, reason: "+e.getMessage(),e);
            return false;
        } finally {
            if (sTransactionCreatesNewHibSession && iHibSession!=null && iHibSession.isOpen()) {
                iHibSession.close();
                iHibSession = null;
            }
        }
    }

    public boolean rollbackTransaction() {
        try {
        	if (iTx == null) return false;
            iTx.rollback();
        	iTx = null;
            info("Transaction rollbacked.");
            return true;
        } catch (Exception e) {
            fatal("Unable to rollback transaction, reason: "+e.getMessage(),e);
            return false;
        } finally {
            if (sTransactionCreatesNewHibSession && iHibSession!=null && iHibSession.isOpen()) {
                iHibSession.close();
                iHibSession = null;
            }
        }
    }
    
    public boolean flush(boolean commit) {
        try {
            getHibSession().flush(); getHibSession().clear();
            if (commit && iTx!=null) {
                iTx.commit();
                iTx = getHibSession().beginTransaction();
            }
            return true;
        } catch (Exception e) {
            fatal("Unable to flush current session, reason: "+e.getMessage(),e);
            return false;
        }
    }
    
    public boolean flushIfNeeded(boolean commit) {
        iFlushIfNeededCounter++;
        if (iFlushIfNeededCounter>=sBatchSize) {
            iFlushIfNeededCounter = 0;
            return flush(commit);
        }
        return true;
    }
    
    public interface MessageHandler {
    	public void onMessage(Message message);
    }
    
    public void addMessageHandler(MessageHandler h) {
    	iMessageHandlers.add(h);
    }
    
    public static class Message {
    	private LogLevel iLevel;
    	private String iMessage;
    	private Throwable iThrowable;
    	
    	public Message(LogLevel level, String message) {
    		this(level, message, null);
    	}
    	
    	public Message(LogLevel level, String message, Throwable t) {
    		iLevel = level; iMessage = message; iThrowable = t;
    	}
    	
    	public String toString() {
    		return iLevel.name() + ": " + iMessage + (iThrowable == null ? "": " (" + iThrowable.getMessage() + ")");
    	}
    	
    	public LogLevel getLevel() { return iLevel; }
    	public String getMessage() { return iMessage; }
    	public Throwable getThrowable() { return iThrowable; }
    	
    	public String toHtml() {
    		switch (iLevel) {
			case DEBUG:
	        	return "<font color='gray'>&nbsp;&nbsp;--" + iMessage + "</font>";
			case INFO:
				return iMessage;
			case WARN:
				return "<font color='orange'>" + iMessage + "</font>";
			case ERROR:
				return "<font color='red'>" + iMessage + "</font>";
			case FATAL:
				return "<font color='red'><b>" + iMessage + "</b></font>";
			default:
				return iMessage;
    		}
    	}
    }
    
    public static class DefaultMessageLogger implements MessageHandler {
    	private Log iLog;
    	
    	public DefaultMessageLogger(Log log) {
    		iLog = log;
    	}
    	
		@Override
		public void onMessage(Message message) {
			switch (message.getLevel()) {
			case DEBUG:
				iLog.debug(message.getMessage(), message.getThrowable());
				break;
			case INFO:
				iLog.info(message.getMessage(), message.getThrowable());
				break;
			case WARN:
				iLog.warn(message.getMessage(), message.getThrowable());
				break;
			case ERROR:
				iLog.error(message.getMessage(), message.getThrowable());
				break;
			case FATAL:
				iLog.fatal(message.getMessage(), message.getThrowable());
				break;
			default:
				iLog.info(message.getMessage(), message.getThrowable());
			}
		}
    }
    
    public XExactTimeConversion getExactTimeConversion() {
    	if (iExactTimeConversion == null)
    		iExactTimeConversion = new XExactTimeConversion(getHibSession());
    	return iExactTimeConversion;
    }
    
    public String getStudentNameFormat() {
    	return ApplicationProperties.getProperty("unitime.enrollment.student.name", DepartmentalInstructor.sNameFormatLastFirstMiddle);
    }
    
    public String getInstructorNameFormat() {
    	return ApplicationProperties.getProperty("unitime.enrollment.instructor.name", DepartmentalInstructor.sNameFormatInitialLast);
    }
    
    public String getApproverName(String externalId, Long sessionId) {
    	if (externalId == null) return null;
    	TimetableManager mgr = (TimetableManager)getHibSession().createQuery( "from TimetableManager where externalUniqueId = :externalId")
				.setString("externalId", externalId)
				.setCacheable(true).setMaxResults(1).uniqueResult();
		if (mgr != null)
			return mgr.getName();
		    		
		DepartmentalInstructor instr = (DepartmentalInstructor)getHibSession().createQuery(
				"from DepartmentalInstructor where externalUniqueId = :externalId and department.session.uniqueId = :sessionId")
				.setString("externalId", externalId)
				.setLong("sessionId", sessionId)
				.setCacheable(true).setMaxResults(1).uniqueResult();
		return instr == null ? externalId : instr.nameLastNameFirst();
    }
    
    public OnlineSectioningLog.Action.Builder addAction(OnlineSectioningAction<?> action, AcademicSessionInfo session) {
    	OnlineSectioningLog.Action.Builder a = OnlineSectioningLog.Action.newBuilder();
    	a.setOperation(action.name());
    	a.setSession(OnlineSectioningLog.Entity.newBuilder()
    			.setUniqueId(session.getUniqueId())
    			.setName(session.toCompactString())
    			);
    	a.setStartTime(System.currentTimeMillis());
    	if (iUser != null)
    		a.setUser(iUser);
    	iLog.addAction(a);
    	return iLog.getActionBuilder(iLog.getActionCount() - 1);
    }
    
    public OnlineSectioningLog.Action.Builder getAction() {
    	return iLog.getActionBuilder(0);
    }
    
    public void logOption(String key, String value) {
    	getAction().addOptionBuilder().setKey(key).setValue(value);
    }
    
    public OnlineSectioningLog.Log getLog() {
    	return iLog.build();
    }
    
    public static OnlineSectioningLog.Section toProto(ClassAssignmentInterface.ClassAssignment assignment) {
		OnlineSectioningLog.Section.Builder section = OnlineSectioningLog.Section.newBuilder();
		if (assignment.getClassId() != null) {
			section.setClazz(
					OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(assignment.getClassId())
					.setExternalId(assignment.getSection())
					.setName(assignment.getClassNumber()));
		}
		if (assignment.getSubpartId() != null) {
			section.setSubpart(
					OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(assignment.getSubpartId())
					.setName(assignment.getSubpart()));
		}
		if (assignment.getCourseId() != null) {
			section.setCourse(
					OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(assignment.getCourseId())
					.setName(assignment.getSubject() + " " + assignment.getCourseNbr()));
		}
		if (assignment.isAssigned()) {
			OnlineSectioningLog.Time.Builder time = OnlineSectioningLog.Time.newBuilder();
			time.setDays(DayCode.toInt(DayCode.toDayCodes(assignment.getDays())));
			time.setStart(assignment.getStart());
			time.setLength(assignment.getLength());
			if (assignment.hasDatePattern())
				time.setPattern(assignment.getDatePattern());
			section.setTime(time);
		}
		if (assignment.hasInstructors()) {
			for (int i = 0; i < assignment.getInstructors().size(); i++) {
				OnlineSectioningLog.Entity.Builder instructor = OnlineSectioningLog.Entity.newBuilder();
				instructor.setName(assignment.getInstructors().get(i));
				String email = assignment.getInstructorEmails().get(i);
				if (!email.isEmpty())
					instructor.setExternalId(email);
				section.addInstructor(instructor);
			}
		}
		if (assignment.hasRoom()) {
			for (String room: assignment.getRooms())
				section.addLocation(OnlineSectioningLog.Entity.newBuilder()
						.setName(room)
						);
		}
		return section.build();
    }
    
    public static OnlineSectioningLog.Section.Builder toProto(SctAssignment a) {
    	return toProto(a, null, null);
    }

    public static OnlineSectioningLog.Section.Builder toProto(SctAssignment a, Enrollment e) {
    	OnlineSectioningLog.Section.Builder section = toProto(a, e == null ? null: e.getCourse(), e == null ? null : e.getReservation());
    	if (e.getTimeStamp() != null)
    		section.setTimeStamp(e.getTimeStamp());
    	return section;
    }

    public static OnlineSectioningLog.Section.Builder toProto(SctAssignment a, Course c) {
    	return toProto(a, c, null);
    }

    public static OnlineSectioningLog.Section.Builder toProto(SctAssignment a, Course c, Reservation r) {
		OnlineSectioningLog.Section.Builder section = OnlineSectioningLog.Section.newBuilder();
		if (a instanceof Section) {
			Section s = (Section)a;
			section.setClazz(
					OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(s.getId())
					.setExternalId(c == null ? s.getName() : s.getName(c.getId()))
					.setName(s.getName(-1l))
					);
			section.setSubpart(
					OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(s.getSubpart().getId())
					.setName(s.getSubpart().getName())
					.setExternalId(s.getSubpart().getInstructionalType())
					);
			
			if (s.getChoice().getInstructorNames() != null && !s.getChoice().getInstructorNames().isEmpty()) {
				String[] instructors = s.getChoice().getInstructorNames().split(":");
				String[] instructorIds = s.getChoice().getInstructorIds().split(":");
				for (int i = 0; i < Math.min(instructorIds.length, instructors.length); i++) {
					String[] nameEmail = instructors[i].split("\\|");
					String id = instructorIds[i];
					OnlineSectioningLog.Entity.Builder instructor = OnlineSectioningLog.Entity.newBuilder()
						.setUniqueId(Long.valueOf(id))
						.setName(nameEmail[0]);
					if (nameEmail.length >= 2)
						instructor.setExternalId( nameEmail[1]);
					section.addInstructor(instructor);
				}
			}
		}
		if (c != null) {
			section.setCourse(
					OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(c.getId())
					.setName(c.getName()));
		}
		if (a.getTime() != null) {
			OnlineSectioningLog.Time.Builder time = OnlineSectioningLog.Time.newBuilder();
			time.setDays(a.getTime().getDayCode());
			time.setStart(a.getTime().getStartSlot());
			time.setLength(a.getTime().getLength());
			if (a.getTime().getDatePatternName() != null && !a.getTime().getDatePatternName().isEmpty())
				time.setPattern(a.getTime().getDatePatternName());
			else if (a instanceof FreeTimeRequest)
				time.setPattern("Free Time");
			section.setTime(time);
		}
		if (a.getRooms() != null) {
			for (RoomLocation room: a.getRooms()) {
				section.addLocation(OnlineSectioningLog.Entity.newBuilder()
						.setUniqueId(room.getId())
						.setName(room.getName())
						);
			}
		}
    	if (r != null) {
    		OnlineSectioningLog.Entity.Builder reservation = OnlineSectioningLog.Entity.newBuilder()
    			.setUniqueId(r.getId());
    		if (r instanceof GroupReservation)
    			reservation.setType(OnlineSectioningLog.Entity.EntityType.GROUP_RESERVATION);
    		else if (r instanceof IndividualReservation)
    			reservation.setType(OnlineSectioningLog.Entity.EntityType.INDIVIDUAL_RESERVATION);
    		else if (r instanceof CurriculumReservation) {
    			reservation.setType(OnlineSectioningLog.Entity.EntityType.CURRICULUM_RESERVATION);
    			CurriculumReservation cr = (CurriculumReservation)r;
    			reservation.setName(cr.getAcademicArea() + (cr.getClassifications().isEmpty() ? "" : " " + cr.getClassifications()) + (cr.getMajors().isEmpty() ? "" : cr.getMajors()));
    		} else if (r instanceof CourseReservation)
    			reservation.setType(OnlineSectioningLog.Entity.EntityType.RESERVATION);
    		section.setReservation(reservation);
    	}
    	return section;
    }
    
    public static OnlineSectioningLog.Request.Builder toProto(Request r) {
    	OnlineSectioningLog.Request.Builder request = OnlineSectioningLog.Request.newBuilder();
    	request.setPriority(r.getPriority());
    	request.setAlternative(r.isAlternative());
    	if (r instanceof FreeTimeRequest) {
    		FreeTimeRequest ft = (FreeTimeRequest)r;
    		if (ft.getTime() != null) {
    			request.addFreeTime(OnlineSectioningLog.Time.newBuilder()
    					.setDays(ft.getTime().getDayCode())
    					.setStart(ft.getTime().getStartSlot())
    					.setLength(ft.getTime().getLength()));
    		}
    	} else if (r instanceof CourseRequest) {
    		CourseRequest cr = (CourseRequest)r;
    		for (Course course: cr.getCourses()) {
    			request.addCourse(OnlineSectioningLog.Entity.newBuilder()
    					.setUniqueId(course.getId())
    					.setName(course.getName()));
    		}
    		if (cr.getTimeStamp() != null)
    			request.setTimeStamp(cr.getTimeStamp());
        	request.setWaitList(cr.isWaitlist());
    	}
    	return request;
    }
    
    public static OnlineSectioningLog.Request.Builder toProto(XRequest r) {
    	OnlineSectioningLog.Request.Builder request = OnlineSectioningLog.Request.newBuilder();
    	request.setPriority(r.getPriority());
    	request.setAlternative(r.isAlternative());
    	if (r instanceof XFreeTimeRequest) {
    		XFreeTimeRequest ft = (XFreeTimeRequest)r;
    		if (ft.getTime() != null) {
    			request.addFreeTime(OnlineSectioningLog.Time.newBuilder()
    					.setDays(ft.getTime().getDays())
    					.setStart(ft.getTime().getSlot())
    					.setLength(ft.getTime().getLength()));
    		}
    	} else if (r instanceof XCourseRequest) {
    		XCourseRequest cr = (XCourseRequest)r;
    		for (XCourseId course: cr.getCourseIds()) {
    			request.addCourse(OnlineSectioningLog.Entity.newBuilder()
    					.setUniqueId(course.getCourseId())
    					.setName(course.getCourseName()));
    		}
    		if (cr.getTimeStamp() != null)
    			request.setTimeStamp(cr.getTimeStamp().getTime());
        	request.setWaitList(cr.isWaitlist());
    	}
    	return request;
    }
    
    public static List<OnlineSectioningLog.Request> toProto(CourseRequestInterface request) {
    	List<OnlineSectioningLog.Request> ret = new ArrayList<OnlineSectioningLog.Request>();
    	int priority = 0;
    	for (CourseRequestInterface.Request r: request.getCourses()) {
    		if (!r.hasRequestedCourse() && !r.hasRequestedFreeTime()) continue;
    		OnlineSectioningLog.Request.Builder rq = OnlineSectioningLog.Request.newBuilder();
    		rq.setPriority(priority++);
    		rq.setWaitList(r.hasRequestedCourse() && r.isWaitList());
    		rq.setAlternative(false);
    		if (r.hasRequestedFreeTime()) {
        		for (CourseRequestInterface.FreeTime ft: r.getRequestedFreeTime()) {
        			rq.addFreeTime(OnlineSectioningLog.Time.newBuilder()
        					.setDays(DayCode.toInt(DayCode.toDayCodes(ft.getDays())))
        					.setStart(ft.getStart())
        					.setLength(ft.getLength()));
        		}
    		}
    		if (r.hasRequestedCourse()) {
    			rq.addCourse(OnlineSectioningLog.Entity.newBuilder()
    					.setName(r.getRequestedCourse()));
    		}
    		if (r.hasFirstAlternative()) {
    			rq.addCourse(OnlineSectioningLog.Entity.newBuilder()
    					.setName(r.getFirstAlternative()));
    		}
    		if (r.hasSecondAlternative()) {
    			rq.addCourse(OnlineSectioningLog.Entity.newBuilder()
    					.setName(r.getSecondAlternative()));
    		}
    		ret.add(rq.build());
    	}
    	priority = 0;
    	for (CourseRequestInterface.Request r: request.getAlternatives()) {
    		if (!r.hasRequestedCourse() && !r.hasRequestedFreeTime()) continue;
    		OnlineSectioningLog.Request.Builder rq = OnlineSectioningLog.Request.newBuilder();
    		rq.setPriority(priority++);
    		rq.setAlternative(true);
    		rq.setWaitList(r.hasRequestedCourse() && r.isWaitList());
    		if (r.hasRequestedFreeTime()) {
        		for (CourseRequestInterface.FreeTime ft: r.getRequestedFreeTime()) {
        			rq.addFreeTime(OnlineSectioningLog.Time.newBuilder()
        					.setDays(DayCode.toInt(DayCode.toDayCodes(ft.getDays())))
        					.setStart(ft.getStart())
        					.setLength(ft.getLength()));
        		}
    		}
    		if (r.hasRequestedCourse()) {
    			rq.addCourse(OnlineSectioningLog.Entity.newBuilder()
    					.setName(r.getRequestedCourse()));
    		}
    		if (r.hasFirstAlternative()) {
    			rq.addCourse(OnlineSectioningLog.Entity.newBuilder()
    					.setName(r.getFirstAlternative()));
    		}
    		if (r.hasSecondAlternative()) {
    			rq.addCourse(OnlineSectioningLog.Entity.newBuilder()
    					.setName(r.getSecondAlternative()));
    		}
    		ret.add(rq.build());
    	}
    	return ret;
    }
    
    public static OnlineSectioningLog.Section.Builder toProto(XSection a, XEnrollment e) {
    	OnlineSectioningLog.Section.Builder section = toProto(a, e == null ? null: e, e == null ? null : e.getReservation());
    	if (e.getTimeStamp() != null)
    		section.setTimeStamp(e.getTimeStamp().getTime());
    	return section;
    }
    
    public static OnlineSectioningLog.Section.Builder toProto(XSection a) {
    	return toProto(a, null, null); 
    }
    
    public static OnlineSectioningLog.Section.Builder toProto(XSection s, XCourseId c, XReservationId r) {
		OnlineSectioningLog.Section.Builder section = OnlineSectioningLog.Section.newBuilder();
		section.setClazz(
				OnlineSectioningLog.Entity.newBuilder()
				.setUniqueId(s.getSectionId())
				.setExternalId(c == null ? s.getName() : s.getName(c.getCourseId()))
				.setName(s.getName(-1l))
				);
		section.setSubpart(
				OnlineSectioningLog.Entity.newBuilder()
				.setUniqueId(s.getSubpartId())
				.setName(s.getSubpartName())
				.setExternalId(s.getInstructionalType())
				);
		
		for (XInstructor i: s.getInstructors()) {
			OnlineSectioningLog.Entity.Builder instructor = OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(i.getIntructorId())
					.setName(i.getName());
			if (i.getEmail() != null)
				instructor.setExternalId(i.getEmail());
			else if (i.getExternalId() != null)
				instructor.setExternalId(i.getExternalId());
			section.addInstructor(instructor);
		}
		if (c != null) {
			section.setCourse(
					OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(c.getCourseId())
					.setName(c.getCourseName()));
		}
		if (s.getTime() != null) {
			OnlineSectioningLog.Time.Builder time = OnlineSectioningLog.Time.newBuilder();
			time.setDays(s.getTime().getDays());
			time.setStart(s.getTime().getSlot());
			time.setLength(s.getTime().getLength());
			if (s.getTime().getDatePatternName() != null && !s.getTime().getDatePatternName().isEmpty())
				time.setPattern(s.getTime().getDatePatternName());
			section.setTime(time);
		}
		if (s.getRooms() != null) {
			for (XRoom rm: s.getRooms()) {
				OnlineSectioningLog.Entity.Builder room = OnlineSectioningLog.Entity.newBuilder()
						.setUniqueId(rm.getUniqueId())
						.setName(rm.getName());
				if (rm.getExternalId() != null)
						room.setExternalId(rm.getExternalId());
				section.addLocation(room);
			}
		}
    	if (r != null) {
    		OnlineSectioningLog.Entity.Builder reservation = OnlineSectioningLog.Entity.newBuilder()
    			.setUniqueId(r.getReservationId());
    		switch (r.getType()) {
    		case Group:
    			reservation.setType(OnlineSectioningLog.Entity.EntityType.GROUP_RESERVATION);
    			break;
    		case Curriculum:
    			reservation.setType(OnlineSectioningLog.Entity.EntityType.CURRICULUM_RESERVATION);
    			break;
    		case Individual:
    			reservation.setType(OnlineSectioningLog.Entity.EntityType.INDIVIDUAL_RESERVATION);
    			break;
    		case Course:
    			reservation.setType(OnlineSectioningLog.Entity.EntityType.COURSE_RESERVATION);
    			break;
    		default:
    			reservation.setType(OnlineSectioningLog.Entity.EntityType.RESERVATION);
    		}
    		section.setReservation(reservation);
    	}
    	return section;
    }
    
	public static long getCpuTime() {
		return ManagementFactory.getThreadMXBean().isCurrentThreadCpuTimeSupported() ? ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() : 1000000l * System.currentTimeMillis();
	}
	
	public static String getTimeString(int slot) {
        int min = slot * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN;
        int h = min / 60;
        int m = min % 60;
        if (CFG.useAmPm())
        	return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h >= 12 ? "p" : "a");
        else
        	return h + ":" + (m < 10 ? "0" : "") + m;
	}
	
	public static String toString(XTime t) {
		return DayCode.toString(t.getDays()) + " " + getTimeString(t.getSlot()) + " - " + getTimeString(t.getSlot() + t.getLength());
	}

	public static String toString(XFreeTimeRequest f) {
		return CFG.freePrefix() + toString(f.getTime());
	}

	public static String toString(TimeLocation t) {
		return DayCode.toString(t.getDayCode()) + " " + getTimeString(t.getStartSlot()) + " - " + getTimeString(t.getStartSlot() + t.getLength());
	}

	public static String toString(FreeTimeRequest f) {
		return CFG.freePrefix() + toString(f.getTime());
	}
}
