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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.Query;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.model.base.BaseTimePattern;
import org.unitime.timetable.model.dao.TimePatternDAO;
import org.unitime.timetable.webutil.RequiredTimeTable;



public class TimePattern extends BaseTimePattern implements Comparable {
    private static final long serialVersionUID = 1L;
    
    public static final int sTypeStandard = 0;
    public static final int sTypeEvening  = 1;
    public static final int sTypeSaturday = 2;
    public static final int sTypeMorning  = 3;
    public static final int sTypeExtended = 4;
    public static final int sTypeExactTime = 5;
    public static final String[] sTypes = new String[] {
    	"Standard", "Evening", "Saturday", "Morning", "Extended", "Exact Time"
    };

    /** Request attribute name for available time patterns **/
    public static String TIME_PATTERN_ATTR_NAME = "timePatternsList";

    /*[CONSTRUCTOR MARKER BEGIN]*/
	public TimePattern () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public TimePattern (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public TimePattern (
		java.lang.Long uniqueId,
		Session session) {

		super (
			uniqueId,
			session);
	}

    /*[CONSTRUCTOR MARKER END]*/

    public static Vector findAll(HttpServletRequest request, Boolean visible) throws Exception {
    	User user = Web.getUser(request.getSession());
    	Session session = Session.getCurrentAcadSession(user);
    	return findAll(session, visible);
    }

    public static Vector findAll(Session session, Boolean visible) {
    	return findAll(session.getUniqueId(), visible);
    }
    
    public static Vector findAll(Long sessionId, Boolean visible) {
    	String query = "from TimePattern tp " +
    				 	"where tp.session.uniqueId=:sessionId";
    	if (visible!=null) 
    	    query += " and visible=:visible";
    	
    	org.hibernate.Session hibSession = new TimePatternDAO().getSession();
    	Query q = hibSession.createQuery(query);
    	q.setCacheable(true);
    	q.setLong("sessionId", sessionId.longValue());
    	if (visible!=null) 
    	    q.setBoolean("visible", visible.booleanValue());
    	
        Vector v = new Vector(q.list());
        Collections.sort(v);
        return v;
    }
    
    public static Vector findApplicable(HttpServletRequest request, int minPerWeek, boolean includeExactTime, Department department) throws Exception {
    	User user = Web.getUser(request.getSession());
    	Session session = Session.getCurrentAcadSession(user);
    	TimetableManager mgr = TimetableManager.getManager(user);
    	boolean includeExtended = user.isAdmin() || (mgr!=null && mgr.isExternalManager());
    	return findByMinPerWeek(session, false, includeExtended, includeExactTime, minPerWeek,(includeExtended?null:department));
    }
    
    public static Vector findByMinPerWeek(Session session, boolean includeHidden, boolean includeExtended, boolean includeExactTime, int minPerWeek, Department department) {
    	return findByMinPerWeek(session.getUniqueId(),includeHidden,includeExtended,includeExactTime,minPerWeek,department); 
    }
    
    public static Vector findByMinPerWeek(Long sessionId, boolean includeHidden, boolean includeExtended, boolean includeExactTime, int minPerWeek, Department department) {
    	Vector list = null;
    	if (includeExactTime && department==null) {
    		list = new Vector((new TimePatternDAO()).getSession().
        			createQuery("select distinct p from TimePattern as p "+
        					"where p.session.uniqueId=:sessionId and "+
        					(!includeHidden?"p.visible=true and ":"")+
        					"(p.type="+sTypeExactTime+" or ( p.type!="+sTypeExactTime+" and "+
        					(!includeExtended?"p.type!="+sTypeExtended+" and ":"")+
        					"p.minPerMtg * p.nrMeetings = :minPerWeek ))").
        					setLong("sessionId",sessionId.longValue()).
        					setInteger("minPerWeek",minPerWeek).
        					setCacheable(true).list());
    	} else {
    		list = new Vector((new TimePatternDAO()).getSession().
    			createQuery("select distinct p from TimePattern as p "+
    					"where p.session.uniqueId=:sessionId and "+
    					"p.type!="+sTypeExactTime+" and "+
    					(!includeHidden?"p.visible=true and ":"")+
    					(!includeExtended?"p.type!="+sTypeExtended+" and ":"")+
    					"p.minPerMtg * p.nrMeetings = :minPerWeek").
    					setLong("sessionId",sessionId.longValue()).
    					setInteger("minPerWeek",minPerWeek).
    					setCacheable(true).list());
    	}
    	
    	if (!includeExtended && department!=null) {
    		for (Iterator i=department.getTimePatterns().iterator();i.hasNext();) {
    			TimePattern tp = (TimePattern)i.next();
    			if (tp.getMinPerMtg().intValue()*tp.getNrMeetings().intValue()!=minPerWeek) continue;
    			if (tp.getType().intValue()!=sTypeExtended) continue;
    			if (!includeHidden && !tp.isVisible().booleanValue()) continue;
    			list.add(tp);
    		}
    	}
    	
    	if (includeExactTime && department!=null) {
    		for (Iterator i=department.getTimePatterns().iterator();i.hasNext();) {
    			TimePattern tp = (TimePattern)i.next();
    			if (tp.getType().intValue()!=sTypeExactTime) continue;
    			list.add(tp);
    			break;
    		}
    	}
    	
    	Collections.sort(list);
    	
        return list;
    }
    
    public static TimePattern findByName(HttpServletRequest request, String name) throws Exception {
    	User user = Web.getUser(request.getSession());
    	Session session = Session.getCurrentAcadSession(user);
    	boolean includeExtended = user.isAdmin();
    	return findByName(session, name);
    }
    
    public static TimePattern findByName(Session session, String name) {
    	return findByName(session.getUniqueId(), name);
    }

    public static TimePattern findByName(Long sessionId, String name) {
    	List list = (new TimePatternDAO()).getSession().
    		createQuery("select distinct p from TimePattern as p "+
    					"where p.session.uniqueId=:sessionId and "+
    					"p.name=:name").
    					setLong("sessionId",sessionId.longValue()).
    					setText("name",name).setCacheable(true).list();
    	if (list==null || list.isEmpty())
    		return null;
    	return (TimePattern)list.get(0);
    }
    
    /**
     * Returns time string only. The subclasses append the type 
     */
    public String toString() {
        return getName();
    }

    public boolean equals(Object o) {
        if ((o==null) || !(o instanceof TimePattern))
            return false;

        return getUniqueId().equals(((TimePattern)o).getUniqueId());
    }
    
    public int compareTo(Object o) {
        if ((o==null) || !(o instanceof TimePattern))
            return -1;

        TimePattern t = (TimePattern) o;
        
        int cmp = getType().compareTo(t.getType());
        
        if (cmp!=0) return cmp;

        cmp = -getNrMeetings().compareTo(t.getNrMeetings());
        
        if (cmp!=0) return cmp;
        
        cmp = getMinPerMtg().compareTo(t.getMinPerMtg());
        if (cmp!=0) return cmp;
        
        int nrComb = getTimes().size()*getDays().size();
        int nrCombT = t.getTimes().size()*t.getDays().size();
        cmp = Double.compare(nrComb, nrCombT);
        if (cmp!=0) return cmp;
        
        return getName().compareTo(t.getName());
    }
    
    public TimePatternModel getTimePatternModel() {
    	return getTimePatternModel(null, true);
    }
    public TimePatternModel getTimePatternModel(boolean allowHardPreferences) {
    	return getTimePatternModel(null, allowHardPreferences);
    }
    public TimePatternModel getTimePatternModel(Assignment assignment, boolean allowHardPreferences) {
    	return new TimePatternModel(this, assignment, allowHardPreferences);
    }
    
    public static Set findAllUsed(Session session) {
    	return findAllUsed(session.getUniqueId()); 
    }
    
    public static Set findAllUsed(Long sessionId) {
    	TreeSet ret = new TreeSet(
    			(new TimePatternDAO()).
        		getSession().
        		createQuery("select distinct tp from TimePref as p inner join p.timePattern as tp where tp.session.uniqueId=:sessionId").
				setLong("sessionId",sessionId.longValue()).
				setCacheable(true).list());
    	ret.addAll((new TimePatternDAO()).
        		getSession().
        		createQuery("select distinct tp from Assignment as a inner join a.timePattern as tp where tp.session.uniqueId=:sessionId").
				setLong("sessionId",sessionId.longValue()).
				setCacheable(true).list());
    	return ret;
    }
    
    public boolean isEditable() {
    	return !findAllUsed(getSession()).contains(this);
    }
    
    public static RequiredTimeTable getDefaultRequiredTimeTable() {
    	return new RequiredTimeTable(new TimePatternModel());
    }
    
    public RequiredTimeTable getRequiredTimeTable(boolean allowHardPreferences) {
    	return getRequiredTimeTable(null, allowHardPreferences);
    }
    public RequiredTimeTable getRequiredTimeTable(Assignment assignment, boolean allowHardPreferences) {
    	return new RequiredTimeTable(getTimePatternModel(assignment, allowHardPreferences));
    }
    
    public Set getDepartments(Long sessionId) {
    	TreeSet ret = new TreeSet();
    	for (Iterator i=getDepartments().iterator();i.hasNext();) {
    		Department d = (Department)i.next();
    		if (sessionId==null || d.getSession().getUniqueId().equals(sessionId))
    			ret.add(d);
    	}
    	return ret;
    }
    
    public Integer getBreakTime() {
    	Integer breakTime = super.getBreakTime();
    	if (breakTime!=null) return breakTime;
    	if (getSlotsPerMtg()==null)
    		return new Integer(10);
		if (getSlotsPerMtg().intValue()%12==0)
			return new Integer(10);
		if (getSlotsPerMtg().intValue()>6)
			return new Integer(15);
		if (getType().intValue()==sTypeExactTime)
			return new Integer(10);
		return new Integer(0);
    }
    
	public Object clone() {
		TimePattern newTimePattern = new TimePattern();
		newTimePattern.setBreakTime(getBreakTime());
		if (getDays() != null){
			TimePatternDays origTpDays = null;
			TimePatternDays newTpDays = null;
			for(Iterator dIt = getDays().iterator(); dIt.hasNext();){
				origTpDays = (TimePatternDays) dIt.next();
				newTpDays = new TimePatternDays();
				newTpDays.setDayCode(origTpDays.getDayCode());
				newTimePattern.addTodays(newTpDays);
			}
		}
		newTimePattern.setMinPerMtg(getMinPerMtg());
		newTimePattern.setName(getName());
		newTimePattern.setNrMeetings(getNrMeetings());
		newTimePattern.setSlotsPerMtg(getSlotsPerMtg());
		if (getTimes() != null){
			TimePatternTime origTpTime = null;
			TimePatternTime newTpTime = null;
			for (Iterator it = getTimes().iterator(); it.hasNext();){
				origTpTime = (TimePatternTime) it.next();
				newTpTime = new TimePatternTime();
				newTpTime.setStartSlot(origTpTime.getStartSlot());
				newTimePattern.addTotimes(newTpTime);
			}
		}
		newTimePattern.setSession(getSession());
		newTimePattern.setType(getType());
		newTimePattern.setVisible(isVisible());
		return newTimePattern;
	}

}
