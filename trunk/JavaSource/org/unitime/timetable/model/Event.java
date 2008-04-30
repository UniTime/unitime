/* 
 * UniTime 3.1 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2008, UniTime.org
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */ 
 
package org.unitime.timetable.model;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import net.sf.cpsolver.ifs.util.ToolBox;

import org.unitime.timetable.model.base.BaseEvent;
import org.unitime.timetable.model.dao.EventDAO;
import org.unitime.timetable.util.Constants;


public class Event extends BaseEvent implements Comparable<Event> {
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

	/**
	 * Constructor for required fields
	 */
	public Event (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.EventType eventType,
		java.lang.Integer minCapacity,
		java.lang.Integer maxCapacity) {

		super (
			uniqueId,
			eventType,
			minCapacity,
			maxCapacity);
	}

/*[CONSTRUCTOR MARKER END]*/

	
	
    public static List findEventsOfSubjectArea(org.hibernate.Session hibSession, Long subjectAreaId, Integer eventType) {
        return hibSession.createQuery(
                "select distinct e from Event e inner join e.relatedCourses r where " +
                "r.course.subjectArea.uniqueId=:subjectAreaId and e.eventType.uniqueId=:eventType")
                .setLong("subjectAreaId", subjectAreaId)
                .setInteger("examType", eventType)
                .setCacheable(true)
                .list();
    }
    
    public static List findEventsOfSubjectArea(Long subjectAreaId, Integer eventType) {
        return (findEventsOfSubjectArea((new EventDAO().getSession()), subjectAreaId, eventType));
    }
     
    public static List findExamsOfCourseOffering(org.hibernate.Session hibSession, Long courseOfferingId, Integer eventType) {
        return hibSession.createQuery(
                "select distinct e from Event e inner join e.relatedCourses r where " +
                "r.course.uniqueId=:courseOfferingId and e.eventType.uniqueId=:eventType")
                .setLong("courseOfferingId", courseOfferingId)
                .setInteger("eventType", eventType)
                .setCacheable(true)
                .list();
    }
    
    public static List findExamsOfCourseOffering(Long courseOfferingId, Integer eventType) {
        return (findExamsOfCourseOffering((new EventDAO().getSession()),courseOfferingId, eventType));
    }
 
    public static List findEventsOfCourse(org.hibernate.Session hibSession, Long subjectAreaId, String courseNbr, Integer eventType) {
        if (courseNbr==null || courseNbr.trim().length()==0) return findEventsOfSubjectArea(subjectAreaId, eventType);
        return hibSession.createQuery(
                "select distinct e from Event e inner join e.relatedCourses r where " +
                "r.course.subjectArea.uniqueId=:subjectAreaId and e.eventType.uniqueId=:eventType and "+
                (courseNbr.indexOf('*')>=0?"r.course.courseNbr like :courseNbr":"r.course.courseNbr=:courseNbr"))
                .setLong("subjectAreaId", subjectAreaId)
                .setString("courseNbr", courseNbr.trim().replaceAll("\\*", "%"))
                .setInteger("eventType", eventType)
                .setCacheable(true)
                .list();
    }

    
    public static List findCourseRelatedEventsOfTypeOwnedBy(org.hibernate.Session hibSession, Long eventType, Long ownerId, Integer ownerType) {
        return hibSession.createQuery(
                "select distinct e from Event e inner join e.relatedCourses r where " +
                "r.ownerId=:ownerId and r.ownerType=:ownerType and e.eventType.uniqueId=:eventType")
                .setLong("ownerId", ownerId)
                .setInteger("ownerType", ownerType)
                .setLong("eventType", eventType)
                .setCacheable(true)
                .list();
    }
    
    public static List findCourseRelatedEventsOfTypeOwnedBy(Long eventType, Long ownerId, Integer ownerType) {
    	return(findCourseRelatedEventsOfTypeOwnedBy((new EventDAO().getSession()), eventType, ownerId, ownerType));
    }
    
    public static List findCourseRelatedEventsOfTypeOwnedBy(org.hibernate.Session hibSession, Long eventType, CourseOffering courseOffering){
    	return(findCourseRelatedEventsOfTypeOwnedBy(hibSession, eventType, courseOffering.getUniqueId(), ExamOwner.sOwnerTypeCourse));
    }

    public static List findCourseRelatedEventsOfTypeOwnedBy(Long eventType, CourseOffering courseOffering){
    	return(findCourseRelatedEventsOfTypeOwnedBy((new EventDAO().getSession()), eventType, courseOffering));
    }
    
    public static List findCourseRelatedEventsOfTypeOwnedBy(org.hibernate.Session hibSession, Long eventType, InstructionalOffering instructionalOffering){
    	return(findCourseRelatedEventsOfTypeOwnedBy(hibSession, eventType, instructionalOffering.getUniqueId(), ExamOwner.sOwnerTypeOffering));
    }

    public static List findCourseRelatedEventsOfTypeOwnedBy(Long eventType, InstructionalOffering instructionalOffering){
    	return(findCourseRelatedEventsOfTypeOwnedBy((new EventDAO().getSession()), eventType, instructionalOffering));
    }
  
    public static List findCourseRelatedEventsOfTypeOwnedBy(org.hibernate.Session hibSession, Long eventType, InstrOfferingConfig instrOffrConfig){
    	return(findCourseRelatedEventsOfTypeOwnedBy(hibSession, eventType, instrOffrConfig.getUniqueId(), ExamOwner.sOwnerTypeConfig));
    }

    public static List findCourseRelatedEventsOfTypeOwnedBy(Long eventType, InstrOfferingConfig instrOffrConfig){
    	return(findCourseRelatedEventsOfTypeOwnedBy((new EventDAO().getSession()), eventType, instrOffrConfig));
    }
  
    public static List findCourseRelatedEventsOfTypeOwnedBy(org.hibernate.Session hibSession, Long eventType, Class_ clazz){
    	return(findCourseRelatedEventsOfTypeOwnedBy(hibSession, eventType, clazz.getUniqueId(), ExamOwner.sOwnerTypeClass));
    }

    public static List findCourseRelatedEventsOfTypeOwnedBy(Long eventType, Class_ clazz){
    	return(findCourseRelatedEventsOfTypeOwnedBy((new EventDAO().getSession()), eventType, clazz));
    }
  
    
    
    public static List findEventsOfCourse(Long subjectAreaId, String courseNbr, Integer eventType) {
        if (courseNbr==null || courseNbr.trim().length()==0) return findEventsOfSubjectArea(subjectAreaId, eventType);
        return (findEventsOfCourse((new EventDAO().getSession()), subjectAreaId, courseNbr, eventType));
    }
    
    public Set<Student> getStudents() {
        HashSet<Student> students = new HashSet();
        for (Iterator i=getRelatedCourses().iterator();i.hasNext();)
            students.addAll(((RelatedCourseInfo)i.next()).getStudents());
        return students;
  
    }
    
    public Set<Long> getStudentIds() {
        HashSet<Long> students = new HashSet();
        for (Iterator<?> i=getRelatedCourses().iterator();i.hasNext();)
            students.addAll(((RelatedCourseInfo)i.next()).getStudentIds());
        return students;
    }
    
    public Set<DepartmentalInstructor> getInstructors() {
        HashSet<DepartmentalInstructor> instructors = new HashSet();
        for (Iterator i=getRelatedCourses().iterator();i.hasNext();)
            instructors.addAll(((RelatedCourseInfo)i.next()).getInstructors());
        return instructors;
    }

    public int countStudents() {
        int nrStudents = 0;
        for (Iterator i=getRelatedCourses().iterator();i.hasNext();)
            nrStudents += ((RelatedCourseInfo)i.next()).countStudents();
        return nrStudents;
       
    }
    
    public static void deleteFromEvents(org.hibernate.Session hibSession, Integer ownerType, Long ownerId) {
        for (Iterator i=hibSession.createQuery("select r from Event e inner join e.relatedCourses r where "+
                "r.ownerType=:ownerType and r.ownerId=:ownerId")
                .setInteger("ownerType", ownerType)
                .setLong("ownerId", ownerId).iterate();i.hasNext();) {
            RelatedCourseInfo relatedCourse = (RelatedCourseInfo)i.next();
            Event event = relatedCourse.getEvent();
            event.getRelatedCourses().remove(relatedCourse);
            relatedCourse.setOwnerId(null);
            relatedCourse.setCourse(null);
            hibSession.delete(relatedCourse);
            if (event.getRelatedCourses().isEmpty()) {
                hibSession.delete(event);
            } else {
                hibSession.saveOrUpdate(event);
            }
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
	
	public int compareTo (Event e) {
		if (getEventName()!=e.getEventName()) {
			return getEventName().compareTo(e.getEventName());
		} else return getUniqueId().compareTo(e.getUniqueId());
	}

	public static List findAll() {
	    return new EventDAO().getSession().createQuery(
	            "select e from Event e"
	            )
	            .setCacheable(true)
	            .list();
	}
	
	public static Event findClassEvent(Long classId) {
	    return (Event)new EventDAO().getSession().createQuery(
	            "select e from Event e inner join e.relatedCourses r where "+
	            "e.eventType.reference=:eventType and "+
	            "r.ownerType=:classType and r.ownerId=:classId")
	            .setString("eventType", EventType.sEventTypeClass)
	            .setInteger("classType", ExamOwner.sOwnerTypeClass)
	            .setLong("classId", classId)
	            .setCacheable(true).uniqueResult();
	}
	
	public TreeSet<MultiMeeting> getMultiMeetings() {
	    TreeSet<MultiMeeting> ret = new TreeSet<MultiMeeting>();
	    TreeSet<Meeting> meetings = new TreeSet<Meeting>();
	    meetings.addAll(getMeetings());
	    while (!meetings.isEmpty()) {
	        Meeting meeting = meetings.first(); meetings.remove(meeting);
	        Hashtable<Long,Meeting> similar = new Hashtable(); 
	        TreeSet<Integer> dow = new TreeSet<Integer>(); dow.add(meeting.getDayOfWeek()); 
	        for (Meeting m : meetings) {
	            if (ToolBox.equals(m.getStartPeriod(),meeting.getStartPeriod()) &&
	                ToolBox.equals(m.getStartOffset(),meeting.getStartOffset()) &&
	                ToolBox.equals(m.getStopPeriod(),meeting.getStopPeriod()) &&
	                ToolBox.equals(m.getStopOffset(),meeting.getStopOffset()) &&
	                ToolBox.equals(m.getLocationPermanentId(),meeting.getLocationPermanentId()) &&
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
	                meetings.remove(m);
	            }
	        }
	        ret.add(new MultiMeeting(multi));
	    }
	    return ret;
	}
	
	public static class MultiMeeting implements Comparable<MultiMeeting> {
	    private TreeSet<Meeting> iMeetings;
	    
	    public MultiMeeting(TreeSet<Meeting> meetings) {
	        iMeetings = meetings;
	    }
	    
	    public TreeSet<Meeting> getMeetings() { return iMeetings; }

	    public int compareTo(MultiMeeting m) {
	        return getMeetings().first().compareTo(m.getMeetings().first());
	    }
	    
	    public String getDays() {
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
	                ret += (nrDays==1?Constants.DAY_NAME:Constants.DAY_NAMES_SHORT)[i];
	        }
            return ret;
	    }
	    
	    public String toString() {
	        return getDays()+" "+
	            new SimpleDateFormat("MM/dd").format(getMeetings().first().getMeetingDate())+
	            (getMeetings().size()>1?" - "+new SimpleDateFormat("MM/dd").format(getMeetings().last().getMeetingDate()):"")+" "+
	            getMeetings().first().startTime()+" - "+
	            getMeetings().first().stopTime()+
	            (getMeetings().first().getLocation()==null?"":" "+getMeetings().first().getLocation().getLabel());
	    }
	}
	
}