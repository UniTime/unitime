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
import org.cpsolver.studentsct.model.Instructor;
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
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.IdValue;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface.RequestedCourse;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.onlinesectioning.model.XExactTimeConversion;
import org.unitime.timetable.onlinesectioning.model.XConfig;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XFreeTimeRequest;
import org.unitime.timetable.onlinesectioning.model.XInstructor;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XReservationId;
import org.unitime.timetable.onlinesectioning.model.XRoom;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XSubpart;
import org.unitime.timetable.onlinesectioning.model.XTime;
import org.unitime.timetable.util.NameFormat;

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
        	if (iLog.getActionCount() > 0)
        		iLog.getActionBuilder(iLog.getActionCount() - 1).addMessage(l);
        	else
        		iLog.addMessage(l);
    	}
    	for (MessageHandler h: iMessageHandlers)
    		h.onMessage(m);
    }
    
    public boolean isDebugEnabled() {
    	for (MessageHandler h: iMessageHandlers)
    		if (h.isDebugEnabled()) return true;
    	return false;
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
    	public boolean isDebugEnabled();
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

		@Override
		public boolean isDebugEnabled() {
			return iLog.isDebugEnabled();
		}
    }
    
    public XExactTimeConversion getExactTimeConversion() {
    	if (iExactTimeConversion == null)
    		iExactTimeConversion = new XExactTimeConversion(getHibSession());
    	return iExactTimeConversion;
    }
    
    public NameFormat getStudentNameFormat() {
    	return NameFormat.fromReference(ApplicationProperty.OnlineSchedulingStudentNameFormat.value());
    }
    
    public NameFormat getInstructorNameFormat() {
    	return NameFormat.fromReference(ApplicationProperty.OnlineSchedulingInstructorNameFormat.value());
    }
    
    public String getDatePatternFormat() {
    	return ApplicationProperty.DatePatternFormatUseDates.value();
    }
    
    public boolean isAlternativeCourseEnabled() {
    	return ApplicationProperty.StudentSchedulingAlternativeCourse.isTrue();
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
			OnlineSectioningLog.Entity.Builder e = OnlineSectioningLog.Entity.newBuilder();
			e.setUniqueId(assignment.getClassId());
			if (assignment.getSection() != null)
				e.setExternalId(assignment.getSection());
			if (assignment.getClassNumber() != null)
				e.setName(assignment.getClassNumber());
			section.setClazz(e);
		}
		if (assignment.getSubpartId() != null) {
			OnlineSectioningLog.Entity.Builder e = OnlineSectioningLog.Entity.newBuilder();
			e.setUniqueId(assignment.getSubpartId());
			if (assignment.getSubpart() != null)
				e.setName(assignment.getSubpart());
			section.setSubpart(e);
		}
		if (assignment.getCourseId() != null) {
			OnlineSectioningLog.Entity.Builder e = OnlineSectioningLog.Entity.newBuilder();
			e.setUniqueId(assignment.getCourseId());
			if (assignment.getSubject() != null && assignment.getCourseNbr() != null)
				e.setName(assignment.getSubject() + " " + assignment.getCourseNbr());
			section.setCourse(e);
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
			for (IdValue room: assignment.getRooms()) {
				OnlineSectioningLog.Entity.Builder e = OnlineSectioningLog.Entity.newBuilder();
				e.setName(room.getValue());
				if (room.getId() != null) e.setUniqueId(room.getId());
				section.addLocation(e);
			}
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
			
			if (s.hasInstructors()) {
				for (Instructor i: s.getInstructors()) {
					OnlineSectioningLog.Entity.Builder instructor = OnlineSectioningLog.Entity.newBuilder()
						.setUniqueId(i.getId())
						.setName(i.getName());
					if (i.getExternalId() != null)
						instructor.setExternalId(i.getExternalId());
					else if (i.getEmail() != null)
						instructor.setExternalId(i.getEmail());
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
    		if (!r.hasRequestedCourse()) continue;
    		OnlineSectioningLog.Request.Builder rq = OnlineSectioningLog.Request.newBuilder();
    		rq.setPriority(priority++);
    		rq.setWaitList(r.hasRequestedCourse() && r.isWaitList());
    		rq.setAlternative(false);
			for (RequestedCourse rc: r.getRequestedCourse()) {
				if (rc.isFreeTime()) {
	        		for (CourseRequestInterface.FreeTime ft: rc.getFreeTime()) {
	        			rq.addFreeTime(OnlineSectioningLog.Time.newBuilder()
	        					.setDays(DayCode.toInt(DayCode.toDayCodes(ft.getDays())))
	        					.setStart(ft.getStart())
	        					.setLength(ft.getLength()));
	        		}
				} else if (rc.isCourse()) {
    				OnlineSectioningLog.Entity.Builder e = OnlineSectioningLog.Entity.newBuilder();
    				if (rc.hasCourseId()) e.setUniqueId(rc.getCourseId());
    				if (rc.hasCourseName()) e.setName(rc.getCourseName());
    				rq.addCourse(e);
				}
			}
    		ret.add(rq.build());
    	}
    	priority = 0;
    	for (CourseRequestInterface.Request r: request.getAlternatives()) {
    		if (!r.hasRequestedCourse()) continue;
    		OnlineSectioningLog.Request.Builder rq = OnlineSectioningLog.Request.newBuilder();
    		rq.setPriority(priority++);
    		rq.setAlternative(true);
    		rq.setWaitList(r.hasRequestedCourse() && r.isWaitList());
    		for (RequestedCourse rc: r.getRequestedCourse()) {
				if (rc.isFreeTime()) {
	        		for (CourseRequestInterface.FreeTime ft: rc.getFreeTime()) {
	        			rq.addFreeTime(OnlineSectioningLog.Time.newBuilder()
	        					.setDays(DayCode.toInt(DayCode.toDayCodes(ft.getDays())))
	        					.setStart(ft.getStart())
	        					.setLength(ft.getLength()));
	        		}
				} else if (rc.isCourse()) {
    				OnlineSectioningLog.Entity.Builder e = OnlineSectioningLog.Entity.newBuilder();
    				if (rc.hasCourseId()) e.setUniqueId(rc.getCourseId());
    				if (rc.hasCourseName()) e.setName(rc.getCourseName());
    				rq.addCourse(e);
				}
			}
    		ret.add(rq.build());
    	}
    	return ret;
    }
    
    public static OnlineSectioningLog.Section.Builder toProto(XSection a, XEnrollment e) {
    	OnlineSectioningLog.Section.Builder section = toProto(a, e == null ? null: e, e == null ? null : e.getReservation());
    	if (e != null && e.getTimeStamp() != null)
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
    
    public static OnlineSectioningLog.Section.Builder toProto(Class_ clazz, CourseOffering course) {
		OnlineSectioningLog.Section.Builder section = OnlineSectioningLog.Section.newBuilder();
		section.setClazz(
				OnlineSectioningLog.Entity.newBuilder()
				.setUniqueId(clazz.getUniqueId())
				.setExternalId(clazz.getExternalId(course))
				.setName(clazz.getClassSuffix(course))
				);
		section.setSubpart(
				OnlineSectioningLog.Entity.newBuilder()
				.setUniqueId(clazz.getSchedulingSubpart().getUniqueId())
				.setName(clazz.getSchedulingSubpart().getItypeDesc().trim())
				.setExternalId(clazz.getSchedulingSubpart().getItype().getItype().toString())
				);
		if (clazz.isDisplayInstructor())
			for (ClassInstructor ci: clazz.getClassInstructors()) {
				OnlineSectioningLog.Entity.Builder instructor = OnlineSectioningLog.Entity.newBuilder().setUniqueId(ci.getInstructor().getUniqueId()).setName(NameFormat.LAST_FIRST_MIDDLE.format(ci.getInstructor()));
				if (ci.getInstructor().getEmail() != null)
					instructor.setExternalId(ci.getInstructor().getEmail());
				else if (ci.getInstructor().getExternalUniqueId() != null)
					instructor.setExternalId(ci.getInstructor().getExternalUniqueId());
				section.addInstructor(instructor);
			}
		if (course != null) {
			section.setCourse(
					OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(course.getUniqueId())
					.setName(course.getCourseName()));
		}
		if (clazz.getCommittedAssignment() != null) {
			OnlineSectioningLog.Time.Builder time = OnlineSectioningLog.Time.newBuilder();
			time.setDays(clazz.getCommittedAssignment().getDays());
			time.setStart(clazz.getCommittedAssignment().getStartSlot());
			time.setLength(clazz.getCommittedAssignment().getSlotPerMtg());
			if (clazz.getCommittedAssignment().getDatePattern() != null)
				time.setPattern(clazz.getCommittedAssignment().getDatePattern().getName());
			section.setTime(time);
		}
		if (clazz.getCommittedAssignment() != null) {
			for (Location location: clazz.getCommittedAssignment().getRooms()) {
				OnlineSectioningLog.Entity.Builder room = OnlineSectioningLog.Entity.newBuilder()
						.setUniqueId(location.getUniqueId())
						.setName(location.getLabel());
				if (location.getExternalUniqueId() != null)
						room.setExternalId(location.getExternalUniqueId());
				section.addLocation(room);
			}
		}
    	return section;
    }
    
	public static long getCpuTime() {
		return ManagementFactory.getThreadMXBean().isCurrentThreadCpuTimeSupported() ? ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() : 1000000l * System.currentTimeMillis();
	}
	
	public static String getTimeString(int slot) {
		return getTimeString(slot, 0);
	}
	
	public static String getTimeString(int slot, int breakTime) {
        int min = slot * Constants.SLOT_LENGTH_MIN + Constants.FIRST_SLOT_TIME_MIN - breakTime;
        int h = min / 60;
        int m = min % 60;
        if (CFG.useAmPm())
        	return (h > 12 ? h - 12 : h) + ":" + (m < 10 ? "0" : "") + m + (h >= 12 ? "p" : "a");
        else
        	return h + ":" + (m < 10 ? "0" : "") + m;
	}
	
	public static String toString(XTime t) {
		return DayCode.toString(t.getDays()) + " " + getTimeString(t.getSlot()) + " - " + getTimeString(t.getSlot() + t.getLength(), t.getBreakTime());
	}

	public static String toString(XFreeTimeRequest f) {
		return CFG.freePrefix() + toString(f.getTime());
	}

	public static String toString(TimeLocation t) {
		return DayCode.toString(t.getDayCode()) + " " + getTimeString(t.getStartSlot()) + " - " + getTimeString(t.getStartSlot() + t.getLength(), t.getBreakTime());
	}

	public static String toString(FreeTimeRequest f) {
		return CFG.freePrefix() + toString(f.getTime());
	}
	
	public String getPin() {
		if (getUser().getParameterCount() > 0)
			for (OnlineSectioningLog.Property p: getUser().getParameterList())
				if ("pin".equals(p.getKey())) return p.getValue();
		return null;
	}
	
	public String getStudentExternalId() {
		if (getUser().getParameterCount() > 0)
			for (OnlineSectioningLog.Property p: getUser().getParameterList())
				if ("student".equals(p.getKey())) return p.getValue();
		return getUser().getExternalId();
	}
	
	public String getSpecialRegistrationId() {
		if (getUser().getParameterCount() > 0)
			for (OnlineSectioningLog.Property p: getUser().getParameterList())
				if ("specreg".equals(p.getKey())) return p.getValue();
		return null;
	}
	
	public static OnlineSectioningLog.CourseRequestOption.Builder toPreference(OnlineSectioningServer server, RequestedCourse rc, XCourseId c) {
		if (!rc.isCourse() || (!rc.hasSelectedClasses() && !rc.hasSelectedIntructionalMethods())) return null;
		if (c == null) c = server.getCourse(rc.getCourseId(), rc.getCourseName());
		if (c == null) return null;
		XOffering offering = server.getOffering(c.getOfferingId());
		if (offering == null) return null;
		OnlineSectioningLog.CourseRequestOption.Builder preference = OnlineSectioningLog.CourseRequestOption.newBuilder();
		preference.setType(OnlineSectioningLog.CourseRequestOption.OptionType.REQUEST_PREFERENCE);
		if (rc.hasSelectedIntructionalMethods()) {
			for (XConfig config: offering.getConfigs()) {
				if (config.getInstructionalMethod() != null && (rc.isSelectedIntructionalMethod(config.getInstructionalMethod().getLabel()) || rc.isSelectedIntructionalMethod(config.getInstructionalMethod().getReference())))
					preference.addInstructionalMethod(OnlineSectioningLog.Entity.newBuilder()
							.setUniqueId(config.getInstructionalMethod().getUniqueId())
							.setExternalId(config.getInstructionalMethod().getReference())
							.setName(config.getInstructionalMethod().getLabel()));
			}
		}
		if (rc.hasSelectedClasses()) {
			for (XConfig config: offering.getConfigs())
				for (XSubpart subpart: config.getSubparts())
					for (XSection section: subpart.getSections())
						if (rc.isSelectedClass(section.getName(c.getCourseId())) || rc.isSelectedClass(subpart.getName() + " " + section.getName(c.getCourseId())) || rc.isSelectedClass(section.getExternalId(c.getCourseId())))
							preference.addSection(OnlineSectioningHelper.toProto(section));
		}
		return preference;
	}
	
	public static OnlineSectioningLog.CourseRequestOption.Builder toPreference(CourseOffering co, RequestedCourse rc) {
		if (!rc.isCourse() || (!rc.hasSelectedClasses() && !rc.hasSelectedIntructionalMethods())) return null;
		OnlineSectioningLog.CourseRequestOption.Builder preference = OnlineSectioningLog.CourseRequestOption.newBuilder();
		preference.setType(OnlineSectioningLog.CourseRequestOption.OptionType.REQUEST_PREFERENCE);
		if (rc.hasSelectedIntructionalMethods()) {
			for (InstrOfferingConfig config: co.getInstructionalOffering().getInstrOfferingConfigs()) {
				if (config.getInstructionalMethod() != null && (rc.isSelectedIntructionalMethod(config.getInstructionalMethod().getLabel()) || rc.isSelectedIntructionalMethod(config.getInstructionalMethod().getReference())))
					preference.addInstructionalMethod(OnlineSectioningLog.Entity.newBuilder()
							.setUniqueId(config.getInstructionalMethod().getUniqueId())
							.setExternalId(config.getInstructionalMethod().getReference())
							.setName(config.getInstructionalMethod().getLabel()));
			}
		}
		if (rc.hasSelectedClasses()) {
			for (InstrOfferingConfig config: co.getInstructionalOffering().getInstrOfferingConfigs())
				for (SchedulingSubpart subpart: config.getSchedulingSubparts())
					for (Class_ clazz: subpart.getClasses())
						if (rc.isSelectedClass(clazz.getClassSuffix(co)) || rc.isSelectedClass(clazz.getExternalId(co)) || rc.isSelectedClass(clazz.getItypeDesc().trim() + " " + clazz.getSectionNumberString()))
							preference.addSection(OnlineSectioningHelper.toProto(clazz, co));
		}
		return preference;
	}
	
	public static void fillPreferencesIn(RequestedCourse rc, OnlineSectioningLog.CourseRequestOption pref) {
		if (pref != null) {
			if (pref.getInstructionalMethodCount() > 0)
				for (OnlineSectioningLog.Entity im: pref.getInstructionalMethodList())
					rc.setSelectedIntructionalMethod(im.getName(), true);
			if (pref.getSectionCount() > 0)
				for (OnlineSectioningLog.Section s: pref.getSectionList())
					rc.setSelectedClass((s.getClazz().getExternalId().length() <= 4 ? s.getSubpart().getName() + " ": "") + s.getClazz().getExternalId(), true);
		}
	}
}
