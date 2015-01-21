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
 
package org.unitime.timetable.model;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;


import org.cpsolver.ifs.util.ToolBox;
import org.unitime.timetable.model.base.BaseEvent;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.Formats;


/**
 * @author Tomas Muller, Stephanie Schluttenhofer, Zuzana Mullerova
 */
public abstract class Event extends BaseEvent implements Comparable<Event> {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public Event () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public Event (java.lang.Long uniqueId) {
		super(uniqueId);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public static final int sEventTypeClass = 0;
	public static final int sEventTypeFinalExam = 1;
	public static final int sEventTypeMidtermExam = 2;
	public static final int sEventTypeCourse = 3;
	public static final int sEventTypeSpecial = 4;
	public static final int sEventTypeUnavailable = 5;
	public static final String[] sEventTypes = new String[] {
	    "Class Event",
	    "Final Examination Event",
	    "Midterm Examination Event",
	    "Course Related Event",
	    "Special Event",
	    "Not Available Event"
	};
    public static final String[] sEventTypesAbbv = new String[] {
        "Class",
        "Final Exam",
        "Midterm Exam",
        "Course",
        "Special",
        "Not Available"
    };

	public abstract int getEventType();
	
	public String getEventTypeLabel() { return sEventTypes[getEventType()]; }
	
	public String getEventTypeAbbv() { return sEventTypesAbbv[getEventType()]; }
	
    public abstract Set<Student> getStudents();
    
    public abstract Collection<StudentClassEnrollment> getStudentClassEnrollments();
    
    public abstract Collection<Long> getStudentIds();
    
    public abstract Set<DepartmentalInstructor> getInstructors();

    public static void deleteFromEvents(org.hibernate.Session hibSession, Integer ownerType, Long ownerId) {
        for (Iterator i=hibSession.createQuery("select r from CourseEvent e inner join e.relatedCourses r where "+
                "r.ownerType=:ownerType and r.ownerId=:ownerId")
                .setInteger("ownerType", ownerType)
                .setLong("ownerId", ownerId).list().iterator();i.hasNext();) {
            RelatedCourseInfo relatedCourse = (RelatedCourseInfo)i.next();
            CourseEvent event = relatedCourse.getEvent();
            event.getRelatedCourses().remove(relatedCourse);
            relatedCourse.setOwnerId(null);
            relatedCourse.setCourse(null);
            hibSession.delete(relatedCourse);
            hibSession.saveOrUpdate(event);
        }
    }
    
    public static void deleteFromEvents(org.hibernate.Session hibSession, Class_ clazz) {
    	deleteFromEvents(hibSession, ExamOwner.sOwnerTypeClass, clazz.getUniqueId());
    }
    public static void deleteFromEvents(org.hibernate.Session hibSession, InstrOfferingConfig config) {
    	deleteFromEvents(hibSession, ExamOwner.sOwnerTypeConfig, config.getUniqueId());
    }
    public static void deleteFromEvents(org.hibernate.Session hibSession, InstructionalOffering offering) {
    	deleteFromEvents(hibSession, ExamOwner.sOwnerTypeOffering, offering.getUniqueId());
    }
    public static void deleteFromEvents(org.hibernate.Session hibSession, CourseOffering course) {
    	deleteFromEvents(hibSession, ExamOwner.sOwnerTypeCourse, course.getUniqueId());
    }
    
	public String toString() {
		return (this.getEventName());
	}
	
	public String eventCapacityDisplayString(){
		String s = "";
		if (getMinCapacity() != null){
			s += getMinCapacity().toString();
			if (!getMaxCapacity().equals(getMinCapacity())){
				s = s + "-" + getMaxCapacity().toString();
			}
		}
		return(s);
	}
	public int compareTo (Event e) {
		if (getEventName()!=e.getEventName()) {
			return getEventName().compareTo(e.getEventName());
		} else return (getUniqueId() == null ? new Long(-1) : getUniqueId()).compareTo(e.getUniqueId() == null ? -1 : e.getUniqueId());
	}

	public static List findAll() {
	    return new EventDAO().getSession().createQuery(
	            "select e from Event e"
	            )
	            .setCacheable(true)
	            .list();
	}
	
	public TreeSet<MultiMeeting> getMultiMeetings() {
	    return getMultiMeetings(getMeetings());
	}
	
    public static TreeSet<MultiMeeting> getMultiMeetings(Collection meetings) {
        Date now = new Date();
        TreeSet<MultiMeeting> ret = new TreeSet<MultiMeeting>();
        HashSet<Meeting> meetingSet = new HashSet<Meeting>(meetings);
        while (!meetingSet.isEmpty()) {
            Meeting meeting = null;
            for (Meeting m : meetingSet)
                if (meeting==null || meeting.getMeetingDate().compareTo(m.getMeetingDate())>0)
                    meeting = m;
            meetingSet.remove(meeting);
            Hashtable<Long,Meeting> similar = new Hashtable(); 
            TreeSet<Integer> dow = new TreeSet<Integer>(); dow.add(meeting.getDayOfWeek());
            boolean past = meeting.getStartTime().before(now);
            for (Meeting m : meetingSet) {
                if (ToolBox.equals(m.getEvent().getUniqueId(),meeting.getEvent().getUniqueId()) &&
                    ToolBox.equals(m.getStartPeriod(),meeting.getStartPeriod()) &&
                    ToolBox.equals(m.getStartOffset(),meeting.getStartOffset()) &&
                    ToolBox.equals(m.getStopPeriod(),meeting.getStopPeriod()) &&
                    ToolBox.equals(m.getStopOffset(),meeting.getStopOffset()) &&
                    ToolBox.equals(m.getLocationPermanentId(),meeting.getLocationPermanentId()) &&
                    past==m.getStartTime().before(now) &&
                    m.isApproved()==meeting.isApproved()) {
                    dow.add(m.getDayOfWeek());
                    similar.put(m.getMeetingDate().getTime(),m);
                }
            }
            TreeSet<Meeting> multi = new TreeSet<Meeting>(); multi.add(meeting);
            if (!similar.isEmpty()) {
                Calendar c = Calendar.getInstance(Locale.US);
                c.setTimeInMillis(meeting.getMeetingDate().getTime());
                while (true) {
                    do {
                        c.add(Calendar.DAY_OF_YEAR, 1);
                    } while (!dow.contains(c.get(Calendar.DAY_OF_WEEK)));
                    Meeting m = similar.get(c.getTimeInMillis()); 
                    if (m==null) break;
                    multi.add(m);
                    meetingSet.remove(m);
                }
            }
            ret.add(new MultiMeeting(multi,past));
        }
        return ret;
    }
	
	public static class MultiMeeting implements Comparable<MultiMeeting> {
	    private TreeSet<Meeting> iMeetings;
	    private boolean iPast = false;
	    
	    public MultiMeeting(TreeSet<Meeting> meetings, boolean past) {
	        iMeetings = meetings;
	        iPast = past;
	    }
	    
	    public boolean isPast() { return iPast; }
	    
	    public TreeSet<Meeting> getMeetings() { return iMeetings; }

	    public int compareTo(MultiMeeting m) {
	        return getMeetings().first().compareTo(m.getMeetings().first());
	    }
	    
	    public String getDays() {
	        return getDays(Constants.DAY_NAME, Constants.DAY_NAMES_SHORT);
	    }
	    
	    public String getDays(String[] dayNames, String[] shortDyNames) {
	        int nrDays = 0;
	        int dayCode = 0;
	        for (Meeting meeting : getMeetings()) {
	            int dc = 0;
	            switch (meeting.getDayOfWeek()) {
                case Calendar.MONDAY    : dc = Constants.DAY_CODES[Constants.DAY_MON]; break;
                case Calendar.TUESDAY   : dc = Constants.DAY_CODES[Constants.DAY_TUE]; break;
                case Calendar.WEDNESDAY : dc = Constants.DAY_CODES[Constants.DAY_WED]; break;
                case Calendar.THURSDAY  : dc = Constants.DAY_CODES[Constants.DAY_THU]; break;
                case Calendar.FRIDAY    : dc = Constants.DAY_CODES[Constants.DAY_FRI]; break;
                case Calendar.SATURDAY  : dc = Constants.DAY_CODES[Constants.DAY_SAT]; break;
                case Calendar.SUNDAY    : dc = Constants.DAY_CODES[Constants.DAY_SUN]; break;
	            }
	            if ((dayCode & dc)==0) nrDays++;
	            dayCode |= dc;
	        }
            String ret = "";
	        for (int i=0;i<Constants.DAY_CODES.length;i++) {
	            if ((dayCode & Constants.DAY_CODES[i])!=0)
	                ret += (nrDays==1?dayNames:shortDyNames)[i];
	        }
            return ret;
	    }
	    
	    public String toString() {
	        return getDays()+" "+
	        	Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT).format(getMeetings().first().getMeetingDate())+
	            (getMeetings().size()>1?" - "+Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT).format(getMeetings().last().getMeetingDate()):"")+" "+
	            (getMeetings().first().isAllDay()?"All Day":getMeetings().first().startTime()+" - "+getMeetings().first().stopTime())+
	            (getMeetings().first().getLocation()==null?"":" "+getMeetings().first().getLocation().getLabel());
	    }
	    
	    public String toShortString() {
	    	return getDays(Constants.DAY_NAMES_SHORT, Constants.DAY_NAMES_SHORT)+" "+
	            	Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT).format(getMeetings().first().getMeetingDate())+
	                (getMeetings().size()>1?" - "+Formats.getDateFormat(Formats.Pattern.DATE_EVENT_SHORT).format(getMeetings().last().getMeetingDate()):"")+" "+
	                (getMeetings().first().isAllDay()?"All Day":getMeetings().first().startTime())+
	                (getMeetings().first().getLocation()==null?"":" "+getMeetings().first().getLocation().getLabel());
	    }
	}
	
	public Session getSession() { return null; }
	
	public static Hashtable<Event,Set<Long>> findStudentConflicts(Date meetingDate, int startSlot, int endSlot, Set<Long> studentIds) {
        Hashtable<Event,Set<Long>> ret = new Hashtable();
        if (studentIds==null || studentIds.isEmpty()) return ret;

        String students = "";
        int nrStudents = 0;
        for (Long id: studentIds) {
        	students += (students.length()==0?"":",")+id;
        	nrStudents++;
        	if (nrStudents == 1000) {
                //class events
                for (Iterator i=EventDAO.getInstance().getSession().createQuery(
                        "select e, s.student.uniqueId from "+
                        "ClassEvent e inner join e.meetings m inner join e.clazz.studentEnrollments s where "+
                        "m.meetingDate=:meetingDate and m.startPeriod < :endSlot and m.stopPeriod > :startSlot and s.student.uniqueId in ("+students+")")
                        .setDate("meetingDate", meetingDate)
                        .setInteger("startSlot", startSlot)
                        .setInteger("endSlot", endSlot)
                        .setCacheable(true).list().iterator();i.hasNext();) {
                    Object[] o = (Object[])i.next();
                    Event event = (Event)o[0];
                    long studentId = (Long)o[1];
                    Set<Long> conf = ret.get(event);
                    if (conf==null) { conf = new HashSet(); ret.put(event, conf); }
                    conf.add(studentId);
                }
                
                //examination events
                for (Iterator i=EventDAO.getInstance().getSession().createQuery(
                        "select e, s.student.uniqueId from "+
                        "ExamEvent e inner join e.meetings m inner join e.exam.owners o, StudentClassEnrollment s where "+
                        "m.meetingDate=:meetingDate and m.startPeriod < :endSlot and m.stopPeriod > :startSlot and s.student.uniqueId in ("+students+") and ("+
                        "(o.ownerType=:classType and s.clazz.uniqueId=o.ownerId) or "+
                        "(o.ownerType=:configType and s.clazz.schedulingSubpart.instrOfferingConfig.uniqueId=o.ownerId) or "+
                        "(o.ownerType=:courseType and s.courseOffering.uniqueId=o.ownerId) or "+
                        "(o.ownerType=:offeringType and s.courseOffering.instructionalOffering.uniqueId=o.ownerId))")
                        .setDate("meetingDate", meetingDate)
                        .setInteger("startSlot", startSlot)
                        .setInteger("endSlot", endSlot)
                        .setInteger("classType", ExamOwner.sOwnerTypeClass)
                        .setInteger("configType", ExamOwner.sOwnerTypeConfig)
                        .setInteger("courseType", ExamOwner.sOwnerTypeCourse)
                        .setInteger("offeringType", ExamOwner.sOwnerTypeOffering)
                        .setCacheable(true).list().iterator();i.hasNext();) {
                    Object[] o = (Object[])i.next();
                    Event event = (Event)o[0];
                    long studentId = (Long)o[1];
                    Set<Long> conf = ret.get(event);
                    if (conf==null) { conf = new HashSet(); ret.put(event, conf); }
                    conf.add(studentId);
                }
                
                //course events with required attendance
                for (Iterator i=EventDAO.getInstance().getSession().createQuery(
                        "select e, s.student.uniqueId from "+
                        "CourseEvent e inner join e.meetings m inner join e.relatedCourses o, StudentClassEnrollment s where e.reqAttendance=true and m.approvalStatus = 1 and "+
                        "m.meetingDate=:meetingDate and m.startPeriod < :endSlot and m.stopPeriod > :startSlot and s.student.uniqueId in ("+students+") and ("+
                        "(o.ownerType=:classType and s.clazz.uniqueId=o.ownerId) or "+
                        "(o.ownerType=:configType and s.clazz.schedulingSubpart.instrOfferingConfig.uniqueId=o.ownerId) or "+
                        "(o.ownerType=:courseType and s.courseOffering.uniqueId=o.ownerId) or "+
                        "(o.ownerType=:offeringType and s.courseOffering.instructionalOffering.uniqueId=o.ownerId))")
                        .setDate("meetingDate", meetingDate)
                        .setInteger("startSlot", startSlot)
                        .setInteger("endSlot", endSlot)
                        .setInteger("classType", ExamOwner.sOwnerTypeClass)
                        .setInteger("configType", ExamOwner.sOwnerTypeConfig)
                        .setInteger("courseType", ExamOwner.sOwnerTypeCourse)
                        .setInteger("offeringType", ExamOwner.sOwnerTypeOffering)
                        .setCacheable(true).list().iterator();i.hasNext();) {
                    Object[] o = (Object[])i.next();
                    Event event = (Event)o[0];
                    long studentId = (Long)o[1];
                    Set<Long> conf = ret.get(event);
                    if (conf==null) { conf = new HashSet(); ret.put(event, conf); }
                    conf.add(studentId);
                }
                nrStudents = 0; students = "";
        	}
        }
        
        if (nrStudents > 0) {
            //class events
            for (Iterator i=EventDAO.getInstance().getSession().createQuery(
                    "select e, s.student.uniqueId from "+
                    "ClassEvent e inner join e.meetings m inner join e.clazz.studentEnrollments s where "+
                    "m.meetingDate=:meetingDate and m.startPeriod < :endSlot and m.stopPeriod > :startSlot and s.student.uniqueId in ("+students+")")
                    .setDate("meetingDate", meetingDate)
                    .setInteger("startSlot", startSlot)
                    .setInteger("endSlot", endSlot)
                    .setCacheable(true).list().iterator();i.hasNext();) {
                Object[] o = (Object[])i.next();
                Event event = (Event)o[0];
                long studentId = (Long)o[1];
                Set<Long> conf = ret.get(event);
                if (conf==null) { conf = new HashSet(); ret.put(event, conf); }
                conf.add(studentId);
            }
            
            //examination events
            for (Iterator i=EventDAO.getInstance().getSession().createQuery(
                    "select e, s.student.uniqueId from "+
                    "ExamEvent e inner join e.meetings m inner join e.exam.owners o, StudentClassEnrollment s where "+
                    "m.meetingDate=:meetingDate and m.startPeriod < :endSlot and m.stopPeriod > :startSlot and s.student.uniqueId in ("+students+") and ("+
                    "(o.ownerType=:classType and s.clazz.uniqueId=o.ownerId) or "+
                    "(o.ownerType=:configType and s.clazz.schedulingSubpart.instrOfferingConfig.uniqueId=o.ownerId) or "+
                    "(o.ownerType=:courseType and s.courseOffering.uniqueId=o.ownerId) or "+
                    "(o.ownerType=:offeringType and s.courseOffering.instructionalOffering.uniqueId=o.ownerId))")
                    .setDate("meetingDate", meetingDate)
                    .setInteger("startSlot", startSlot)
                    .setInteger("endSlot", endSlot)
                    .setInteger("classType", ExamOwner.sOwnerTypeClass)
                    .setInteger("configType", ExamOwner.sOwnerTypeConfig)
                    .setInteger("courseType", ExamOwner.sOwnerTypeCourse)
                    .setInteger("offeringType", ExamOwner.sOwnerTypeOffering)
                    .setCacheable(true).list().iterator();i.hasNext();) {
                Object[] o = (Object[])i.next();
                Event event = (Event)o[0];
                long studentId = (Long)o[1];
                Set<Long> conf = ret.get(event);
                if (conf==null) { conf = new HashSet(); ret.put(event, conf); }
                conf.add(studentId);
            }
            
            //course events with required attendance
            for (Iterator i=EventDAO.getInstance().getSession().createQuery(
                    "select e, s.student.uniqueId from "+
                    "CourseEvent e inner join e.meetings m inner join e.relatedCourses o, StudentClassEnrollment s where e.reqAttendance=true and m.approvalStatus = 1 and "+
                    "m.meetingDate=:meetingDate and m.startPeriod < :endSlot and m.stopPeriod > :startSlot and s.student.uniqueId in ("+students+") and ("+
                    "(o.ownerType=:classType and s.clazz.uniqueId=o.ownerId) or "+
                    "(o.ownerType=:configType and s.clazz.schedulingSubpart.instrOfferingConfig.uniqueId=o.ownerId) or "+
                    "(o.ownerType=:courseType and s.courseOffering.uniqueId=o.ownerId) or "+
                    "(o.ownerType=:offeringType and s.courseOffering.instructionalOffering.uniqueId=o.ownerId))")
                    .setDate("meetingDate", meetingDate)
                    .setInteger("startSlot", startSlot)
                    .setInteger("endSlot", endSlot)
                    .setInteger("classType", ExamOwner.sOwnerTypeClass)
                    .setInteger("configType", ExamOwner.sOwnerTypeConfig)
                    .setInteger("courseType", ExamOwner.sOwnerTypeCourse)
                    .setInteger("offeringType", ExamOwner.sOwnerTypeOffering)
                    .setCacheable(true).list().iterator();i.hasNext();) {
                Object[] o = (Object[])i.next();
                Event event = (Event)o[0];
                long studentId = (Long)o[1];
                Set<Long> conf = ret.get(event);
                if (conf==null) { conf = new HashSet(); ret.put(event, conf); }
                conf.add(studentId);
            }
        }

        return ret;
    }
}
