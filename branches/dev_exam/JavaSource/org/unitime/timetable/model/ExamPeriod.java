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
	        java.lang.Integer length) {

		super (
			uniqueId,
			session,
			dateOffset,
			startSlot,
			length);
	}

/*[CONSTRUCTOR MARKER END]*/

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
	    int cmp = getDateOffset().compareTo(period.getDateOffset());
	    if (cmp!=0) return cmp;
	    return getStartSlot().compareTo(period.getStartSlot());
	}
    
    public static TreeSet findAll(HttpServletRequest request) throws Exception {
        return findAll(Session.getCurrentAcadSession(Web.getUser(request.getSession())).getUniqueId());
    }
    
    public static TreeSet findAll(Long sessionId) {
        TreeSet ret = new TreeSet();
        ret.addAll(new ExamPeriodDAO().getSession().
                createQuery("select ep from ExamPeriod ep where ep.session.uniqueId=:sessionId").
                setLong("sessionId", sessionId).
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
}