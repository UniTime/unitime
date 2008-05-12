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

	/**
	 * Constructor for required fields
	 */
	public Event (
		java.lang.Long uniqueId,
		java.lang.Integer minCapacity,
		java.lang.Integer maxCapacity) {

		super (
			uniqueId,
			minCapacity,
			maxCapacity);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public static final int sEventTypeClass = 0;
	public static final int sEventTypeFinalExam = 1;
	public static final int sEventTypeMidtermExam = 2;
	public static final int sEventTypeCourse = 3;
	public static final int sEventTypeSpecial = 4;
	public static final String[] sEventTypes = new String[] {
	    "Class Event",
	    "Final Examination Event",
	    "Midterm Examination Event",
	    "Course Event",
	    "Special Event"
	};

	public abstract int getEventType();
	
	public String getEventTypeLabel() { return sEventTypes[getEventType()]; }
	
    public abstract Set<Student> getStudents();
    
    public abstract Set<DepartmentalInstructor> getInstructors();

    public static void deleteFromEvents(org.hibernate.Session hibSession, Integer ownerType, Long ownerId) {
        for (Iterator i=hibSession.createQuery("select r from CourseEvent e inner join e.relatedCourses r where "+
                "r.ownerType=:ownerType and r.ownerId=:ownerId")
                .setInteger("ownerType", ownerType)
                .setLong("ownerId", ownerId).iterate();i.hasNext();) {
            RelatedCourseInfo relatedCourse = (RelatedCourseInfo)i.next();
            CourseEvent event = relatedCourse.getEvent();
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
	            new SimpleDateFormat("MM/dd").format(getMeetings().first().getMeetingDate())+
	            (getMeetings().size()>1?" - "+new SimpleDateFormat("MM/dd").format(getMeetings().last().getMeetingDate()):"")+" "+
	            getMeetings().first().startTime()+" - "+
	            getMeetings().first().stopTime()+
	            (getMeetings().first().getLocation()==null?"":" "+getMeetings().first().getLocation().getLabel());
	    }
	}
	
}