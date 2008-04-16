/*
 * UniTime 3.0 (University Course Timetabling & Student Sectioning Application)
 * Copyright (C) 2007, UniTime.org, and individual contributors
 * as indicated by the @authors tag.
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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;

import org.unitime.commons.web.Web;
import org.unitime.timetable.model.base.BaseExamPeriod;
import org.unitime.timetable.model.dao.ExamPeriodDAO;
import org.unitime.timetable.util.Constants;


public class ExamPeriod extends BaseExamPeriod implements Comparable<ExamPeriod> {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ExamPeriod () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ExamPeriod (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public ExamPeriod (
	        java.lang.Long uniqueId,
	        org.unitime.timetable.model.Session session,
	        java.lang.Integer dateOffset,
	        java.lang.Integer startSlot,
	        java.lang.Integer length,
	        org.unitime.timetable.model.PreferenceLevel prefLevel,
	        java.lang.Integer examType) {

		super (
			uniqueId,
			session,
			dateOffset,
			startSlot,
			length,
			prefLevel,
			examType);
	}

/*[CONSTRUCTOR MARKER END]*/
	
	public static String PERIOD_ATTR_NAME = "periodList";

	public Date getStartDate() {
	    Calendar c = Calendar.getInstance(Locale.US);
	    c.setTime(getSession().getExamBeginDate());
	    c.add(Calendar.DAY_OF_YEAR, getDateOffset());
	    return c.getTime();
	}
	
   public void setStartDate(Date startDate) {
       long diff = startDate.getTime()-getSession().getExamBeginDate().getTime();
       setDateOffset((int)Math.round(diff/(1000.0 * 60 * 60 * 24)));
    }
	
	public int getStartHour() {
	    return (Constants.SLOT_LENGTH_MIN*getStartSlot()+Constants.FIRST_SLOT_TIME_MIN) / 60;
	}
	
    public int getStartMinute() {
        return (Constants.SLOT_LENGTH_MIN*getStartSlot()+Constants.FIRST_SLOT_TIME_MIN) % 60;
    }
    
    public Date getStartTime() {
        Calendar c = Calendar.getInstance(Locale.US);
        c.setTime(getSession().getExamBeginDate());
        c.add(Calendar.DAY_OF_YEAR, getDateOffset());
        c.set(Calendar.HOUR, getStartHour());
        c.set(Calendar.MINUTE, getStartMinute());
        return c.getTime();
    }
    
    public int getEndSlot() {
        return getStartSlot() + getLength();
    }
    
    public int getEndHour() {
        return (Constants.SLOT_LENGTH_MIN*getEndSlot()+Constants.FIRST_SLOT_TIME_MIN) / 60;
    }
    
    public int getEndMinute() {
        return (Constants.SLOT_LENGTH_MIN*getEndSlot()+Constants.FIRST_SLOT_TIME_MIN) % 60;
    }
    
    public Date getEndTime() {
        Calendar c = Calendar.getInstance(Locale.US);
        c.setTime(getSession().getExamBeginDate());
        c.add(Calendar.DAY_OF_YEAR, getDateOffset());
        c.set(Calendar.HOUR, getEndHour());
        c.set(Calendar.MINUTE, getEndMinute());
        return c.getTime();
    }


    public String getName() {
        return
            new SimpleDateFormat("EEE MM/dd hh:mmaa").format(getStartTime())+
            " - "+
            new SimpleDateFormat("hh:mmaa").format(getEndTime());
    }

    public String getAbbreviation() {
        return new SimpleDateFormat("MM/dd hh:mmaa").format(getStartTime());
    }

    public int compareTo(ExamPeriod period) {
    	int cmp = getExamType().compareTo(period.getExamType());
    	if (cmp!=0) return cmp;
	    cmp = getDateOffset().compareTo(period.getDateOffset());
	    if (cmp!=0) return cmp;
	    return getStartSlot().compareTo(period.getStartSlot());
	}
    
    public static TreeSet findAll(HttpServletRequest request, Integer type) throws Exception {
        return findAll(Session.getCurrentAcadSession(Web.getUser(request.getSession())).getUniqueId(), type);
    }
    
    public static TreeSet findAll(Long sessionId, Integer type) {
    	TreeSet ret = new TreeSet();
    	if (type==null)
    		ret.addAll(new ExamPeriodDAO().getSession().
                createQuery("select ep from ExamPeriod ep where ep.session.uniqueId=:sessionId").
                setLong("sessionId", sessionId).
                setCacheable(true).
                list());
    	else
    		ret.addAll(new ExamPeriodDAO().getSession().
                    createQuery("select ep from ExamPeriod ep where ep.session.uniqueId=:sessionId and ep.examType=:type").
                    setLong("sessionId", sessionId).
                    setInteger("type", type).
                    setCacheable(true).
                    list());
        return ret;
    }
    
    public static ExamPeriod findByDateStart(Long sessionId, int dateOffset, int startSlot) {
        return (ExamPeriod)new ExamPeriodDAO().getSession().createQuery(
                "select ep from ExamPeriod ep where " +
                "ep.session.uniqueId=:sessionId and ep.dateOffset=:dateOffset and ep.startSlot=:startSlot").
                setLong("sessionId", sessionId).
                setInteger("dateOffset", dateOffset).
                setInteger("startSlot", startSlot).setCacheable(true).uniqueResult();
    }
    
    public static ExamPeriod findByIndex(Long sessionId, Integer type, Integer idx) {
        if (idx==null || idx<0) return null;
        int x = 0;
        TreeSet periods = findAll(sessionId, type);
        for (Iterator i=periods.iterator();i.hasNext();x++) {
            ExamPeriod period = (ExamPeriod)i.next();
            if (x==idx) return period;
        }
        return (periods.isEmpty()?null:(ExamPeriod)periods.last());
    }
    
    public String toString() {
        return getAbbreviation();
    }
    
    public boolean isBackToBack(ExamPeriod period, boolean isDayBreakBackToBack) {
        if (!isDayBreakBackToBack && !period.getDateOffset().equals(getDateOffset())) return false;
        for (Iterator i=findAll(getSession().getUniqueId(), getExamType()).iterator();i.hasNext();) {
            ExamPeriod p = (ExamPeriod)i.next();
            if (compareTo(p)<0 && p.compareTo(period)<0) return false;
            if (compareTo(p)>0 && p.compareTo(period)>0) return false;
        }
        return true;
    }
    
    public boolean overlap(Assignment assignment) {
        return overlap(assignment, Constants.EXAM_TRAVEL_TIME_SLOTS);
    }
    
    public boolean overlap(Assignment assignment, int nrTravelSlots) {
        //check date pattern
        DatePattern dp = assignment.getDatePattern();
        int dpIndex = getDateOffset()-getSession().getExamBeginOffset()-(dp.getOffset()==null?0:dp.getOffset());
        if (dp.getPattern()==null || dpIndex<0 || dpIndex>=dp.getPattern().length() || dp.getPattern().charAt(dpIndex)!='1') return false;
        
        //check day of week
        Calendar cal = Calendar.getInstance(Locale.US);
        cal.setTime(getSession().getExamBeginDate());
        cal.add(Calendar.DAY_OF_YEAR, getDateOffset());
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY    : if ((assignment.getDays() & Constants.DAY_CODES[Constants.DAY_MON])==0) return false; break;
            case Calendar.TUESDAY   : if ((assignment.getDays() & Constants.DAY_CODES[Constants.DAY_TUE])==0) return false; break;
            case Calendar.WEDNESDAY : if ((assignment.getDays() & Constants.DAY_CODES[Constants.DAY_WED])==0) return false; break;
            case Calendar.THURSDAY  : if ((assignment.getDays() & Constants.DAY_CODES[Constants.DAY_THU])==0) return false; break;
            case Calendar.FRIDAY    : if ((assignment.getDays() & Constants.DAY_CODES[Constants.DAY_FRI])==0) return false; break;
            case Calendar.SATURDAY  : if ((assignment.getDays() & Constants.DAY_CODES[Constants.DAY_SAT])==0) return false; break;
            case Calendar.SUNDAY    : if ((assignment.getDays() & Constants.DAY_CODES[Constants.DAY_SUN])==0) return false; break;
        }
        
        //check time
        return getStartSlot() - nrTravelSlots < assignment.getStartSlot() + assignment.getSlotPerMtg() && assignment.getStartSlot() < getStartSlot() + getLength() + nrTravelSlots;
    }
    
    public boolean overlap(Meeting meeting) {
        return overlap(meeting, Constants.EXAM_TRAVEL_TIME_SLOTS);
    }
    
    public boolean overlap(Meeting meeting, int nrTravelSlots) {
        if (!meeting.getMeetingDate().equals(getStartDate())) return false;
        return getStartSlot() - nrTravelSlots < meeting.getStopPeriod() && meeting.getStartPeriod() < getStartSlot() + getLength() + nrTravelSlots;
    }
    
    public List findOverlappingClassMeetings() {
        return findOverlappingClassMeetings(Constants.EXAM_TRAVEL_TIME_SLOTS);
    }

    public List findOverlappingClassMeetings(int nrTravelSlots) {
        return new ExamPeriodDAO().getSession().createQuery(
                "select m from Meeting m where " +
                "m.eventType.reference=:eventType and "+
                "m.meetingDate=:startDate and m.startPeriod < :endSlot and m.stopPeriod > :startSlot")
                .setDate("startDate", getStartDate())
                .setInteger("startSlot", getStartSlot()-nrTravelSlots)
                .setInteger("endSlot", getEndSlot()+nrTravelSlots)
                .setString("eventType", EventType.sEventTypeClass)
                .setCacheable(true)
                .list();
    } 
    
    public List findOverlappingClassMeetings(Long classId) {
        return findOverlappingClassMeetings(classId, Constants.EXAM_TRAVEL_TIME_SLOTS);
    }
    
    public List findOverlappingClassMeetings(Long classId, int nrTravelSlots) {
        return new ExamPeriodDAO().getSession().createQuery(
                "select m from Meeting m inner join m.event.relatedCourses r where " +
                "m.eventType.reference=:eventType and "+
                "m.meetingDate=:startDate and m.startPeriod < :endSlot and m.stopPeriod > :startSlot and " +
                "r.ownerType=:classType and r.ownerId=:classId")
                .setDate("startDate", getStartDate())
                .setInteger("startSlot", getStartSlot()-nrTravelSlots)
                .setInteger("endSlot", getEndSlot()+nrTravelSlots)
                .setString("eventType", EventType.sEventTypeClass)
                .setInteger("classType", ExamOwner.sOwnerTypeClass)
                .setLong("classId", classId)
                .setCacheable(true)
                .list();
    } 
    
    public int getIndex() {
        int index = 0;
        for (Iterator i=findAll(getSession().getUniqueId(), getExamType()).iterator();i.hasNext();) {
            if (compareTo((ExamPeriod)i.next())>0) index++;
        }
        return index;
    }
    
    public int getDayOfWeek() {
        Calendar c = Calendar.getInstance(Locale.US);
        c.setTime(getSession().getExamBeginDate());
        c.add(Calendar.DAY_OF_YEAR, getDateOffset());
        return c.get(Calendar.DAY_OF_WEEK);
    }

    
    public boolean weakOverlap(Meeting meeting) {
        return getDayOfWeek()==meeting.getDayOfWeek() && getStartSlot() < meeting.getStopPeriod() && meeting.getStartPeriod() < getStartSlot() + getLength();
    }

}