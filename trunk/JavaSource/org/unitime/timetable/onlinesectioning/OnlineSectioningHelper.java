/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2011, UniTime LLC, and individual contributors
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

import net.sf.cpsolver.coursett.model.RoomLocation;
import net.sf.cpsolver.studentsct.model.Assignment;
import net.sf.cpsolver.studentsct.model.Course;
import net.sf.cpsolver.studentsct.model.CourseRequest;
import net.sf.cpsolver.studentsct.model.FreeTimeRequest;
import net.sf.cpsolver.studentsct.model.Request;
import net.sf.cpsolver.studentsct.model.Section;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.CourseRequestInterface;
import org.unitime.timetable.model.dao._RootDAO;

/**
 * @author Tomas Muller
 */
public class OnlineSectioningHelper {
    protected static Log sLog = LogFactory.getLog(OnlineSectioningHelper.class);
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
    protected static int sBatchSize = 100;
    
    public OnlineSectioningHelper() {
    }
    
    public OnlineSectioningHelper(org.hibernate.Session hibSession) {
    	iHibSession = hibSession;
    }

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
        return iHibSession;
    }
    
    public boolean beginTransaction() {
        try {
            iHibSession = new _RootDAO().createNewSession();
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
            iTx.commit();
            debug("Transaction committed.");
            return true;
        } catch (Exception e) {
            fatal("Unable to commit transaction, reason: "+e.getMessage(),e);
            return false;
        } finally {
            if (iHibSession!=null && iHibSession.isOpen())
                iHibSession.close();
        }
    }

    public boolean rollbackTransaction() {
        try {
            iTx.rollback();
            info("Transaction rollbacked.");
            return true;
        } catch (Exception e) {
            fatal("Unable to rollback transaction, reason: "+e.getMessage(),e);
            return false;
        } finally {
            if (iHibSession!=null && iHibSession.isOpen())
                iHibSession.close();
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
    
    public OnlineSectioningLog.Action.Builder addAction(OnlineSectioningAction<?> action, AcademicSessionInfo session) {
    	OnlineSectioningLog.Action.Builder a = OnlineSectioningLog.Action.newBuilder();
    	a.setOperation(action.name());
    	a.setSession(OnlineSectioningLog.Entity.newBuilder()
    			.setUniqueId(session.getUniqueId())
    			.setName(session.toCompactString())
    			);
    	a.setStartTime(System.currentTimeMillis());
    	iLog.addAction(a);
    	return iLog.getActionBuilder(iLog.getActionCount() - 1);
    }
    
    public OnlineSectioningLog.Action.Builder getAction() {
    	return iLog.getActionBuilder(0);
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
					.setName(assignment.getSubject() + " " + assignment.getCourseNbr() + " " + assignment.getSubpart() + " " + assignment.getSection()));
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
		}
		if (assignment.hasInstructors()) {
			for (String instructor: assignment.getInstructors())
				section.addInstructor(OnlineSectioningLog.Entity.newBuilder()
						.setName(instructor)
						.setType(OnlineSectioningLog.Entity.EntityType.INSTRUCTOR)
						);
		}
		if (assignment.hasRoom()) {
			for (String room: assignment.getRooms())
				section.addLocation(OnlineSectioningLog.Entity.newBuilder()
						.setName(room)
						.setType(OnlineSectioningLog.Entity.EntityType.LOCATION)
						);
		}
		return section.build();
    }
    
    public static OnlineSectioningLog.Section.Builder toProto(Assignment a, Course c) {
		OnlineSectioningLog.Section.Builder section = OnlineSectioningLog.Section.newBuilder();
		if (a instanceof Section) {
			Section s = (Section)a;
			section.setClazz(
					OnlineSectioningLog.Entity.newBuilder()
					.setUniqueId(s.getId())
					.setExternalId(c == null ? s.getName() : s.getName(c.getId()))
					.setName(c == null ? s.getSubpart().getConfig().getOffering().getName() + " " + s.getSubpart().getName() + " " + s.getName() :
						c.getName() + " " + s.getSubpart().getName() + " " + s.getName(c.getId()))
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
    					.setName(r.getRequestedCourse())
    					.setType(OnlineSectioningLog.Entity.EntityType.COURSE));
    		}
    		if (r.hasFirstAlternative()) {
    			rq.addCourse(OnlineSectioningLog.Entity.newBuilder()
    					.setName(r.getFirstAlternative())
    					.setType(OnlineSectioningLog.Entity.EntityType.COURSE));
    		}
    		if (r.hasSecondAlternative()) {
    			rq.addCourse(OnlineSectioningLog.Entity.newBuilder()
    					.setName(r.getSecondAlternative())
    					.setType(OnlineSectioningLog.Entity.EntityType.COURSE));
    		}
    		ret.add(rq.build());
    	}
    	priority = 0;
    	for (CourseRequestInterface.Request r: request.getAlternatives()) {
    		if (!r.hasRequestedCourse() && !r.hasRequestedFreeTime()) continue;
    		OnlineSectioningLog.Request.Builder rq = OnlineSectioningLog.Request.newBuilder();
    		rq.setPriority(priority++);
    		rq.setAlternative(true);
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
    					.setName(r.getRequestedCourse())
    					.setType(OnlineSectioningLog.Entity.EntityType.COURSE));
    		}
    		if (r.hasFirstAlternative()) {
    			rq.addCourse(OnlineSectioningLog.Entity.newBuilder()
    					.setName(r.getFirstAlternative())
    					.setType(OnlineSectioningLog.Entity.EntityType.COURSE));
    		}
    		if (r.hasSecondAlternative()) {
    			rq.addCourse(OnlineSectioningLog.Entity.newBuilder()
    					.setName(r.getSecondAlternative())
    					.setType(OnlineSectioningLog.Entity.EntityType.COURSE));
    		}
    		ret.add(rq.build());
    	}
    	return ret;
    }
    
	public static long getCpuTime() {
		return ManagementFactory.getThreadMXBean().isCurrentThreadCpuTimeSupported() ? ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() : 1000000l * System.currentTimeMillis();
	}

}
